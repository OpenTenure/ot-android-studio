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
import org.fao.sola.clients.android.opentenure.model.Person;
import org.fao.sola.clients.android.opentenure.model.ShareProperty;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

public class OwnersFragment extends ListFragment {

	private View rootView;
	private ClaimDispatcher claimActivity;
	private ModeDispatcher modeActivity;
	public static final String MODE_KEY = "mode";

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();

		inflater.inflate(R.menu.owners, menu);

		if (modeActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RO) == 0) {
			menu.removeItem(R.id.action_new);
		}

		super.onCreateOptionsMenu(menu, inflater);
		setHasOptionsMenu(true);
		setRetainInstance(true);
	}

	@Override
	public void onResume() {
		super.onResume();

		update();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_new:
			Claim claim = Claim.getClaim(claimActivity.getClaimId());
			if (claim == null) {

				Toast toast = Toast.makeText(rootView.getContext(),
						R.string.message_save_claim_before_adding_content,
						Toast.LENGTH_LONG);
				toast.show();
			} else if (claim.getAvailableShares() > 0) {

				Intent intent = new Intent(rootView.getContext(),
						ShareDetailsActivity.class);

				intent.putExtra("claimId", claimActivity.getClaimId());
				intent.putExtra(MODE_KEY, modeActivity.getMode());

				startActivityForResult(intent, 0);

			} else {
				Toast toast = Toast.makeText(rootView.getContext(),
						R.string.message_no_available_shares_adding_new,
						Toast.LENGTH_LONG);
				toast.show();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);

		}

	}

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

	public OwnersFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.share_list, container, false);
		setHasOptionsMenu(true);
		setRetainInstance(true);
		update();
		
		
		OpenTenureApplication.setOwnersFragment(this);
		return rootView;
	}

	protected void update() {
		String claimId = claimActivity.getClaimId();

		if (claimId != null) {

			Claim claim = Claim.getClaim(claimId);

			List<ShareProperty> shares = claim.getShares();
			List<SharesListTO> sharesListTOs = new ArrayList<SharesListTO>();

			int i = 0;
			for (ShareProperty share : shares) {

				SharesListTO olto = new SharesListTO();

				olto.setSlogan(getString(R.string.title_share) + " " + ++i
						+ " : " + share.getShares() + "/100");
				olto.setId(share.getId());
				olto.setShares(share.getShares());
				sharesListTOs.add(olto);

			}

			ArrayAdapter<SharesListTO> adapter = null;

			if (modeActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RO) == 0) {
				adapter = new SharesListAdapter(rootView.getContext(),
						sharesListTOs, claim.getClaimId(), true);
			} else {
				adapter = new SharesListAdapter(rootView.getContext(),
						sharesListTOs, claim.getClaimId(), false);
			}

			setListAdapter(adapter);
			adapter.notifyDataSetChanged();

		}
	}
}
