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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ShareDetailsFragment extends Fragment {

	String claimId;
	List<String> ownerList = new ArrayList<String>();
	ShareProperty share;

	/**
	 * Identifier for the example fragment.
	 */
	public static final int FRAGMENT_EXAMPLE = 1;

	/**
	 * The adapter definition of the fragments.
	 */
	private FragmentPagerAdapter _fragmentPagerAdapter;

	/**
	 * The ViewPager that hosts the section contents.
	 */
	private ViewPager _viewPager;

	/**
	 * List of fragments.
	 */
	private List<Fragment> _fragments = new ArrayList<Fragment>();

	private View rootView;

	public ShareDetailsFragment() {
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
	public void onStart() {
		super.onStart();

		claimId = getArguments().getString("claimId");

	}

	// @Override
	// public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	//
	// menu.clear();
	//
	// inflater.inflate(R.menu.share_details, menu);
	//
	// super.onCreateOptionsMenu(menu, inflater);
	// Claim claim = Claim.getClaim(claimId);
	// if (claim != null && !claim.isModifiable()) {
	//
	// menu.removeItem(R.id.action_new);
	// menu.removeItem(R.id.action_save);
	// }
	//
	//
	// setHasOptionsMenu(true);
	// setRetainInstance(true);
	// }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		setRetainInstance(true);
		rootView = inflater.inflate(R.layout.share_details, container, false);
		setHasOptionsMenu(true);

		return rootView;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_new:
			Claim claim = Claim.getClaim(claimId);

			if (claim.getAvailableShares() > 0) {

				Intent intent = new Intent(OpenTenureApplication.getContext(),
						SelectPersonActivity.class);

				// SOLA DB cannot store the same person twice

				ArrayList<String> idsWithSaresOrClaims = Person
						.getIdsWithSharesOrClaims();
				intent.putStringArrayListExtra(
						SelectPersonActivity.EXCLUDE_PERSON_IDS_KEY,
						idsWithSaresOrClaims);

				startActivityForResult(intent,
						SelectPersonActivity.SELECT_PERSON_ACTIVITY_RESULT);
			} else {

				Toast toast = Toast.makeText(
						OpenTenureApplication.getContext(),
						R.string.message_no_available_shares,
						Toast.LENGTH_SHORT);
				toast.show();
			}
			return true;
		case R.id.action_save:

			save();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (data != null) { // No selection has been done

			switch (requestCode) {
			case SelectPersonActivity.SELECT_PERSON_ACTIVITY_RESULT:
				String personId = data
						.getStringExtra(PersonActivity.PERSON_ID_KEY);

				if (claimId != null) {

					Claim claim = Claim.getClaim(claimId);

					ownerList.add(personId);
					// ShareProperty share = ShareProperty.getShare(claimId,
					// personId);

					if (share != null) {
						Toast.makeText(OpenTenureApplication.getContext(),
								R.string.message_already_owner,
								Toast.LENGTH_LONG).show();
					} else {
						claim.addOwner(personId, claim.getAvailableShares());
					}
				}

				update();
				break;
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	protected void update() {

		if (claimId != null) {

			Claim claim = Claim.getClaim(claimId);

			int i = 0;

			ArrayAdapter<String> adapter = null;

			adapter = new OwnersListAdapter(OpenTenureApplication.getContext(),
					this.ownerList, claimId, getActivity());

			// adapter = new OwnersListAdapter(context, owners);
			ListView ownerList = (ListView) rootView
					.findViewById(R.id.owner_list);
			ownerList.setAdapter(adapter);
			adapter.notifyDataSetChanged();

		}
	}

	private int save() {

		return 0;
	}

}
