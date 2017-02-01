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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.filesystem.json.JsonUtilities;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.model.ClaimType;
import org.fao.sola.clients.android.opentenure.model.Configuration;
import org.fao.sola.clients.android.opentenure.network.LoginActivity;
import org.fao.sola.clients.android.opentenure.network.LogoutTask;

import com.ipaulpro.afilechooser.utils.FileUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class LocalClaimsFragment extends ListFragment {

	private static final int CLAIM_RESULT = 100;
	private static final int REQUEST_IMPORT = 2404;
	private static final String FILTER_KEY = "filter";
	private View rootView;
	private List<String> excludeClaimIds = new ArrayList<String>();
	private ModeDispatcher mainActivity;
	private String filter = null;
	private String fullPath = null;
	private File dest;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mainActivity = (ModeDispatcher) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement ModeDispatcher");
		}
	}

	public LocalClaimsFragment() {
	}

	public void setExcludeClaimIds(List<String> excludeClaimIds) {
		this.excludeClaimIds = excludeClaimIds;
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		MenuItem itemIn;
		MenuItem itemOut;

		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Log.d(this.getClass().getName(), "Is the user logged in ? : "
				+ OpenTenureApplication.isLoggedin());

		if (mainActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RW) == 0) {
			if (OpenTenureApplication.isLoggedin()) {
				itemIn = menu.findItem(R.id.action_login);
				itemIn.setVisible(false);
				itemOut = menu.findItem(R.id.action_logout);
				itemOut.setVisible(true);

			} else {

				itemIn = menu.findItem(R.id.action_login);
				itemIn.setVisible(true);
				itemOut = menu.findItem(R.id.action_logout);
				itemOut.setVisible(false);
			}
		}

		super.onPrepareOptionsMenu(menu);

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		inflater.inflate(R.menu.local_claims, menu);
		setRetainInstance(true);
		if (mainActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RO) == 0) {
			menu.removeItem(R.id.action_new);
			menu.removeItem(R.id.action_login);
			menu.removeItem(R.id.action_logout);
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle item selection

		switch (item.getItemId()) {
		case R.id.action_backup_db:
			OpenTenureApplication.getInstance().getDatabase().exportDB();
			String backupMessage = String.format(OpenTenureApplication
					.getContext().getString(R.string.message_db_backed_up));

			Toast backupToast = Toast.makeText(
					OpenTenureApplication.getContext(), backupMessage,
					Toast.LENGTH_LONG);
			backupToast.show();
			return true;
		case R.id.action_new:

			if (!Boolean.parseBoolean(Configuration.getConfigurationByName(
					"isInitialized").getValue())) {
				String newMessage = String.format(OpenTenureApplication
						.getContext().getString(
								R.string.message_app_not_yet_initialized));

				Toast newToast = Toast.makeText(
						OpenTenureApplication.getContext(), newMessage,
						Toast.LENGTH_LONG);
				newToast.show();

				return true;
			}

			Intent intent = new Intent(rootView.getContext(),
					ClaimActivity.class);

			intent.putExtra(ClaimActivity.CLAIM_ID_KEY,
					ClaimActivity.CREATE_CLAIM_ID);
			intent.putExtra(ClaimActivity.MODE_KEY, mainActivity.getMode()
					.toString());
			startActivityForResult(intent, CLAIM_RESULT);
			return true;
		case R.id.action_import_zip:

			if (!Boolean.parseBoolean(Configuration.getConfigurationByName(
					"isInitialized").getValue())) {
				String newMessage = String.format(OpenTenureApplication
						.getContext().getString(
								R.string.message_app_not_yet_initialized));

				Toast newToast = Toast.makeText(
						OpenTenureApplication.getContext(), newMessage,
						Toast.LENGTH_LONG);
				newToast.show();

				return true;
			}

			Intent getContentIntent = FileUtils.createGetContentIntent();

			intent = Intent.createChooser(getContentIntent, getResources()
					.getString(R.string.choose_file));
			try {
				startActivityForResult(intent, REQUEST_IMPORT);
			} catch (Exception e) {
				Log.d(this.getClass().getName(),
						"Unable to start file chooser intent due to "
								+ e.getMessage());
			}
			return true;
		case R.id.action_login:

			if (!Boolean.parseBoolean(Configuration.getConfigurationByName(
					"isInitialized").getValue())) {
				Toast toast;
				String toastMessage = String.format(OpenTenureApplication
						.getContext().getString(
								R.string.message_app_not_yet_initialized));

				toast = Toast.makeText(OpenTenureApplication.getContext(),
						toastMessage, Toast.LENGTH_LONG);
				toast.show();

				return true;
			}

			OpenTenureApplication.setActivity(getActivity());

			Context context = getActivity().getApplicationContext();
			Intent intent2 = new Intent(context, LoginActivity.class);
			startActivity(intent2);

			OpenTenureApplication.setActivity(getActivity());

			return true;

		case R.id.action_logout:

			try {

				LogoutTask logoutTask = new LogoutTask();

				logoutTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
						getActivity());

			} catch (Exception e) {
				Log.d("Details", "An error ");

				e.printStackTrace();
			}

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {

		case REQUEST_IMPORT:
			if (resultCode == com.ipaulpro.afilechooser.FileChooserActivity.RESULT_OK) {

				Uri uri = data.getData();

				fullPath = FileUtils.getPath(rootView.getContext(), uri);

				Log.d(this.getClass().getName(), "Selected file: " + fullPath);

				if (fullPath.endsWith(".zip")) {

					dest = FileSystemUtilities.copyFileInImportFolder(new File(
							fullPath));

					if (!dest.exists() || !dest.isFile()) {

						String newMessage = "Error preparing import of "
								+ fullPath;

						Toast newToast = Toast.makeText(rootView.getContext(),
								newMessage, Toast.LENGTH_LONG);
						newToast.show();

						return;
					}
				} else {

					String newMessage = OpenTenureApplication
							.getContext()
							.getString(
									R.string.message_claim_import_not_claim_archive);

					Toast newToast = Toast.makeText(rootView.getContext(),
							newMessage, Toast.LENGTH_LONG);
					newToast.show();

					return;
				}

				AlertDialog.Builder metadataDialog = new AlertDialog.Builder(
						rootView.getContext());

				metadataDialog.setTitle(R.string.password);

				final EditText input = new EditText(rootView.getContext());

				input.setInputType(InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_VARIATION_PASSWORD);
				input.setTransformationMethod(PasswordTransformationMethod
						.getInstance());
				metadataDialog.setView(input);

				metadataDialog.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								String password = input.getText().toString();
								dialog.dismiss();

								new PreImportTask(rootView.getContext())
										.execute(password, dest);

								return;

							}
						});

				metadataDialog.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {

								try {
									FileSystemUtilities
											.deleteFilesInFolder(dest
													.getParentFile());
								} catch (IOException e) {
									// TODO Auto-generated catch block
									System.out.println("Error deleting files "
											+ e.getLocalizedMessage());
									e.printStackTrace();
								}
								return;
							}
						});

				metadataDialog.show();

			}

		default:
			update();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.local_claims_list, container,
				false);
		setHasOptionsMenu(true);
		setRetainInstance(true);
		EditText inputSearch = (EditText) rootView
				.findViewById(R.id.filter_input_field);
		inputSearch.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		inputSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
					int arg3) {
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				filter = arg0.toString();
				((LocalClaimsListAdapter) getListAdapter()).getFilter().filter(
						filter);
			}
		});

		update();

		if (savedInstanceState != null
				&& savedInstanceState.getString(FILTER_KEY) != null) {
			filter = savedInstanceState.getString(FILTER_KEY);
			((LocalClaimsListAdapter) getListAdapter()).getFilter().filter(
					filter);
		}

		OpenTenureApplication.setLocalClaimsFragment(this);

		return rootView;
	}

	@Override
	public void onResume() {
		update();
		if (filter != null) {
			((LocalClaimsListAdapter) getListAdapter()).getFilter().filter(
					filter);
		}
		super.onResume();
	};

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (mainActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RW) == 0) {
			Intent intent = new Intent(rootView.getContext(),
					ClaimActivity.class);
			String claimId = ((TextView) v.findViewById(R.id.claim_id))
					.getText().toString();
			intent.putExtra(ClaimActivity.CLAIM_ID_KEY, claimId);
			Claim claim = Claim.getClaim(claimId);
			if (!claim.getStatus().equalsIgnoreCase(ClaimStatus._CREATED)
					&& !claim.getStatus().equalsIgnoreCase(
							ClaimStatus._UPLOAD_ERROR)
					&& !claim.getStatus().equalsIgnoreCase(
							ClaimStatus._UPLOAD_INCOMPLETE)) {
				intent.putExtra(ClaimActivity.MODE_KEY,
						ModeDispatcher.Mode.MODE_RO.toString());
			} else {
				intent.putExtra(ClaimActivity.MODE_KEY, mainActivity.getMode()
						.toString());
			}
			startActivityForResult(intent, CLAIM_RESULT);
		} else {
			Intent resultIntent = new Intent();
			String claimId = ((TextView) v.findViewById(R.id.claim_id))
					.getText().toString();
			resultIntent.putExtra(ClaimActivity.CLAIM_ID_KEY, claimId);
			getActivity().setResult(
					SelectClaimActivity.SELECT_CLAIM_ACTIVITY_RESULT,
					resultIntent);
			getActivity().finish();
		}
	}

	protected void update() {
		List<Claim> claims = Claim.getSimplifiedClaimsForList();
		List<ClaimListTO> claimListTOs = new ArrayList<ClaimListTO>();
		DisplayNameLocalizer dnl = new DisplayNameLocalizer(
				OpenTenureApplication.getInstance().getLocalization());

		for (Claim claim : claims) {
			if (excludeClaimIds != null
					&& !excludeClaimIds.contains(claim.getClaimId())) {

				ClaimListTO cto = new ClaimListTO();
				String claimName = claim.getName().equalsIgnoreCase("") ? rootView
						.getContext().getString(R.string.default_claim_name)
						: claim.getName();
				String slogan = claimName
						+ ", "
						+ OpenTenureApplication.getContext().getResources()
								.getString(R.string.by)
						+ ": "
						+ claim.getPerson().getFirstName()
						+ " "
						+ claim.getPerson().getLastName()
						+ " "
						+ OpenTenureApplication.getContext().getResources()
								.getString(R.string.type)
						+ ": "
						+ dnl.getLocalizedDisplayName(new ClaimType()
								.getDisplayValueByType(claim.getType()));

				if (claim.getRecorderName() != null)
					slogan = slogan
							+ "\r\n"
							+ OpenTenureApplication.getContext().getResources()
									.getString(R.string.recorded_by) + " "
							+ claim.getRecorderName();

				cto.setSlogan(slogan);
				cto.setId(claim.getClaimId());
				cto.setModifiable(claim.isModifiable());
				cto.setPersonId(claim.getPerson().getPersonId());
				cto.setAttachments(claim.getAttachments());

				if (claim.getClaimNumber() != null)
					cto.setNumber(claim.getClaimNumber());
				else
					cto.setNumber("");

				cto.setStatus(claim.getStatus());

				int days = JsonUtilities.remainingDays(claim
						.getChallengeExpiryDate());

				if (claim.isUploadable())
					cto.setRemaingDays(getResources().getString(
							R.string.message_remaining_days)
							+ days);
				else
					cto.setRemaingDays("");

				claimListTOs.add(cto);
			}
		}

		OpenTenureApplication.getInstance().clearClaimsList();

		ArrayAdapter<ClaimListTO> adapter = new LocalClaimsListAdapter(
				rootView.getContext(), claimListTOs, mainActivity.getMode());
		setListAdapter(adapter);
		adapter.notifyDataSetChanged();

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(FILTER_KEY, filter);
		super.onSaveInstanceState(outState);
	}

	public void refresh() {
		// the list of changing claims is no more necessary. before rendering
		// the list of claims is cleaned

		update();
	}

}
