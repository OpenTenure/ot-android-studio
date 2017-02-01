/**
 * ******************************************************************************************
 * Copyright (C) 2014 - Food and Agriculture Organization of the United Nations (FAO).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice,this list
 *       of conditions and the following disclaimer.
 *    2. Redistributions in binary form must reproduce the above copyright notice,this list
 *       of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *    3. Neither the name of FAO nor the names of its contributors may be used to endorse or
 *       promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
 * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * *********************************************************************************************
 */
package org.fao.sola.clients.android.opentenure;

import java.util.ArrayList;
import java.util.List;

import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ChallengingClaimsFragment extends ListFragment {

	private View rootView;
	private ClaimDispatcher claimActivity;
	private ModeDispatcher modeActivity;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			claimActivity = (ClaimDispatcher) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement ClaimDispatcher");
		}
		try {
			modeActivity = (ModeDispatcher) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement ModeDispatcher");
		}
	}

	public ChallengingClaimsFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.challenging_claims_list, container,
				false);
		setHasOptionsMenu(true);
		setRetainInstance(true);
		update();

		return rootView;
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (modeActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RW) == 0){
			

			Intent intent = new Intent(rootView.getContext(),
					ClaimActivity.class);
			intent.putExtra(ClaimActivity.CLAIM_ID_KEY, ((TextView)v.findViewById(R.id.claim_id)).getText());
			intent.putExtra(ClaimActivity.MODE_KEY, ModeDispatcher.Mode.MODE_RO.toString());
			startActivity(intent);
		}
	}

	protected void update() {

		String claimId = claimActivity.getClaimId();

		if(claimId != null){

			List<Claim> challengingClaims = Claim.getChallengingClaims(claimId);
			List<ClaimListTO> claimListTOs = new ArrayList<ClaimListTO>();

			for(Claim challengingClaim : challengingClaims){
				
				ClaimListTO cto = new ClaimListTO();
				cto.setSlogan(challengingClaim.getName() + ", " + getResources().getString(R.string.by) + ": " + challengingClaim.getPerson().getFirstName()+ " " + challengingClaim.getPerson().getLastName());
				cto.setId(challengingClaim.getClaimId());
				
				cto.setModifiable(challengingClaim.isModifiable());
				if(challengingClaim.getStatus().equals(ClaimStatus._UPLOADING)) 
					cto.setStatus(challengingClaim.getStatus());
				else if(challengingClaim.getStatus().equals(ClaimStatus._UNMODERATED)){
					cto.setStatus("uploaded");
					}
				else cto.setStatus(" ");
				claimListTOs.add(cto);
			}
			ArrayAdapter<ClaimListTO> adapter = new ChallengingClaimsListAdapter(rootView.getContext(), claimListTOs, modeActivity.getMode());
			setListAdapter(adapter);
			adapter.notifyDataSetChanged();

		}
	}
}
