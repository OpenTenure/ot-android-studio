/**d
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

import org.fao.sola.clients.android.opentenure.model.AdjacenciesNotes;
import org.fao.sola.clients.android.opentenure.model.Adjacency;
import org.fao.sola.clients.android.opentenure.model.Claim;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AdjacentClaimsFragment extends ListFragment {

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

	public AdjacentClaimsFragment() {
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {

		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		super.onPrepareOptionsMenu(menu);

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();

		inflater.inflate(R.menu.adjacencies, menu);

		super.onCreateOptionsMenu(menu, inflater);
		
		Claim claim = Claim.getClaim(claimActivity.getClaimId());
		
		if (claim != null && !claim.isModifiable() ) {
			menu.removeItem(R.id.action_save);
		}
		
		setHasOptionsMenu(true);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.adjacent_claims_list, container,
				false);

		setHasOptionsMenu(true);
		setRetainInstance(true);
		InputMethodManager imm = (InputMethodManager) rootView.getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);

		Claim claim = Claim.getClaim(claimActivity.getClaimId());
		if (claim != null)
			load(claim);

		update();

		return rootView;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (modeActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RW) == 0) {

			Intent intent = new Intent(rootView.getContext(),
					ClaimActivity.class);
			intent.putExtra(ClaimActivity.CLAIM_ID_KEY,
					((TextView) v.findViewById(R.id.claim_id)).getText());
			intent.putExtra(ClaimActivity.MODE_KEY,
					ModeDispatcher.Mode.MODE_RO.toString());
			startActivity(intent);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle item selection
		switch (item.getItemId()) {

		case R.id.action_save:

			if (AdjacenciesNotes
					.getAdjacenciesNotes(claimActivity.getClaimId()) != null)
				return updateNotes();

			return save();
		}
		return false;
	}

	protected boolean save() {

		Toast toast;
		String claimId = claimActivity.getClaimId();
		
		if(claimId == null){			
			toast = Toast.makeText(OpenTenureApplication.getContext(),
					R.string.message_save_claim_before_adding_content, Toast.LENGTH_LONG);
			toast.show();
			return true;			
		}

		AdjacenciesNotes adjacenciesNotes = new AdjacenciesNotes();
		adjacenciesNotes.setClaimId(claimId);
		adjacenciesNotes.setNorthAdjacency(((EditText) rootView
				.findViewById(R.id.north_adjacency)).getText().toString());

		adjacenciesNotes.setEastAdjacency(((EditText) rootView
				.findViewById(R.id.east_adjacency)).getText().toString());

		adjacenciesNotes.setSouthAdjacency(((EditText) rootView
				.findViewById(R.id.south_adjacency)).getText().toString());

		adjacenciesNotes.setWestAdjacency(((EditText) rootView
				.findViewById(R.id.west_adjacency)).getText().toString());

		int result = AdjacenciesNotes.createAdjacenciesNotes(adjacenciesNotes);

		

		if (result == 1) {

			toast = Toast.makeText(OpenTenureApplication.getContext(),
					R.string.message_adjacencies_saved, Toast.LENGTH_LONG);
			toast.show();

			return true;
		} else {

			toast = Toast.makeText(OpenTenureApplication.getContext(),
					R.string.message_adjacencies_not_saved, Toast.LENGTH_LONG);
			toast.show();

			return false;
		}
	}

	protected boolean updateNotes() {

		
		Toast toast;
		String claimId = claimActivity.getClaimId();
		
		if(claimId == null){			
			toast = Toast.makeText(OpenTenureApplication.getContext(),
					R.string.message_save_claim_before_adding_content, Toast.LENGTH_LONG);
			toast.show();
			return true;			
		}

		AdjacenciesNotes adjacenciesNotes = new AdjacenciesNotes();
		adjacenciesNotes.setClaimId(claimId);
		adjacenciesNotes.setNorthAdjacency(((EditText) rootView
				.findViewById(R.id.north_adjacency)).getText().toString());

		adjacenciesNotes.setEastAdjacency(((EditText) rootView
				.findViewById(R.id.east_adjacency)).getText().toString());

		adjacenciesNotes.setSouthAdjacency(((EditText) rootView
				.findViewById(R.id.south_adjacency)).getText().toString());

		adjacenciesNotes.setWestAdjacency(((EditText) rootView
				.findViewById(R.id.west_adjacency)).getText().toString());

		int result = AdjacenciesNotes.updateAdjacenciesNotes(adjacenciesNotes);

		if (result == 1) {

			toast = Toast.makeText(OpenTenureApplication.getContext(),
					R.string.message_adjacencies_saved, Toast.LENGTH_LONG);
			toast.show();

			return true;
		} else {

			toast = Toast.makeText(OpenTenureApplication.getContext(),
					R.string.message_adjacencies_not_saved, Toast.LENGTH_LONG);
			toast.show();

			return false;
		}
	}

	protected void update() {

		String claimId = claimActivity.getClaimId();

		if (claimId != null) {

			List<Adjacency> adjacencies = Adjacency.getAdjacencies(claimId);
			List<AdjacentClaimListTO> claimListTOs = new ArrayList<AdjacentClaimListTO>();

			for (Adjacency adjacency : adjacencies) {

				Claim adjacentClaim;
				String direction;

				if (claimActivity.getClaimId().equalsIgnoreCase(
						adjacency.getSourceClaimId())) {
					adjacentClaim = Claim.getClaim(adjacency.getDestClaimId());
					direction = Adjacency.getCardinalDirection(
							rootView.getContext(),
							adjacency.getCardinalDirection());
				} else {
					adjacentClaim = Claim
							.getClaim(adjacency.getSourceClaimId());
					direction = Adjacency.getCardinalDirection(rootView
							.getContext(), Adjacency
							.getReverseCardinalDirection(adjacency
									.getCardinalDirection()));
				}

				AdjacentClaimListTO acto = new AdjacentClaimListTO();
				acto.setSlogan(adjacentClaim.getName() + ", "
						+ getResources().getString(R.string.by) + ": "
						+ adjacentClaim.getPerson().getFirstName() + " "
						+ adjacentClaim.getPerson().getLastName());
				acto.setId(adjacentClaim.getClaimId());
				acto.setCardinalDirection(direction);
				acto.setStatus(adjacentClaim.getStatus());

				claimListTOs.add(acto);
			}
			ArrayAdapter<AdjacentClaimListTO> adapter = new AdjacentClaimsListAdapter(
					rootView.getContext(), claimListTOs);
			setListAdapter(adapter);
			adapter.notifyDataSetChanged();

		}
	}

	public void load(Claim claim) {

		AdjacenciesNotes adNotes = AdjacenciesNotes.getAdjacenciesNotes(claim
				.getClaimId());

		if (claim != null && adNotes != null) {

			((EditText) rootView.findViewById(R.id.north_adjacency))
					.setText(adNotes.getNorthAdjacency());

			((EditText) rootView.findViewById(R.id.south_adjacency))
					.setText(adNotes.getSouthAdjacency());

			((EditText) rootView.findViewById(R.id.east_adjacency))
					.setText(adNotes.getEastAdjacency());

			((EditText) rootView.findViewById(R.id.west_adjacency))
					.setText(adNotes.getWestAdjacency());

			if (modeActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RO) == 0) {
				((EditText) rootView.findViewById(R.id.north_adjacency))
						.setFocusable(false);
				((EditText) rootView.findViewById(R.id.north_adjacency))
				.setLongClickable(false);

				((EditText) rootView.findViewById(R.id.south_adjacency))
						.setFocusable(false);
				((EditText) rootView.findViewById(R.id.south_adjacency))
				.setLongClickable(false);

				((EditText) rootView.findViewById(R.id.east_adjacency))
						.setFocusable(false);
				((EditText) rootView.findViewById(R.id.east_adjacency))
				.setLongClickable(false);

				((EditText) rootView.findViewById(R.id.west_adjacency))
						.setFocusable(false);
				((EditText) rootView.findViewById(R.id.west_adjacency))
				.setLongClickable(false);
			}

		}
	}

}
