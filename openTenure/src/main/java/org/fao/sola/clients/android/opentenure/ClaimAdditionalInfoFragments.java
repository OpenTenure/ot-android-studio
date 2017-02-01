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
import org.fao.sola.clients.android.opentenure.model.AdditionalInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

public class ClaimAdditionalInfoFragments extends ListFragment {

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

	public ClaimAdditionalInfoFragments() {
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		setRetainInstance(true);
		inflater.inflate(R.menu.claim_metadata, menu);

		if (modeActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RO) == 0) {
			menu.removeItem(R.id.action_new_metadata);
		}

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.claim_documents_list, container,
				false);
		setHasOptionsMenu(true);
		setRetainInstance(true);

		update();
		return rootView;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle item selection

		switch (item.getItemId()) {

		case R.id.action_new_metadata:
			if (claimActivity.getClaimId() == null) {
				Toast toast = Toast
						.makeText(rootView.getContext(),
								R.string.message_create_before_edit,
								Toast.LENGTH_SHORT);
				toast.show();
				return true;
			}

			final View mView;

			AlertDialog.Builder metadataDialog = new AlertDialog.Builder(
					rootView.getContext());
			metadataDialog.setTitle(R.string.new_additional_info);
			LayoutInflater inflater = getActivity().getLayoutInflater();

			// Inflate and set the layout for the dialog
			// Pass null as the parent view because its going in the dialog
			// layout

			mView = inflater.inflate(R.layout.metadata_dialog, null);
			metadataDialog.setView(mView);

			metadataDialog.setPositiveButton(R.string.confirm,
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

							EditText key = (EditText) mView
									.findViewById(R.id.metadataKey);
							EditText value = (EditText) mView
									.findViewById(R.id.metadataValue);

							AdditionalInfo additionalInfo = new AdditionalInfo();

							additionalInfo.setClaimId(claimActivity
									.getClaimId());
							additionalInfo.setName(key.getText().toString());
							additionalInfo.setValue(value.getText().toString());

							additionalInfo.create();
							update();

						}
					});
			metadataDialog.setNegativeButton(R.string.cancel,
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});

			metadataDialog.show();

		default:

			return super.onOptionsItemSelected(item);
		}
	}

	protected void update() {
		String claimId = claimActivity.getClaimId();
		List<AdditionalInfo> additionalInfo;
		List<String> ids = new ArrayList<String>();
		List<String> slogans = new ArrayList<String>();

		if (claimId != null) {
			Claim claim = Claim.getClaim(claimId);
			additionalInfo = claim.getAdditionalInfo();
			for (AdditionalInfo meta : additionalInfo) {
				String slogan = meta.getName() + " = " + meta.getValue();
				slogans.add(slogan);
				ids.add(meta.getAdditionalInfoId());
			}
		}
		ArrayAdapter<String> adapter = new ClaimAdditionalInfoListAdapter(
				rootView.getContext(), slogans, ids, modeActivity.getMode());
		setListAdapter(adapter);
		adapter.notifyDataSetChanged();

	}

}
