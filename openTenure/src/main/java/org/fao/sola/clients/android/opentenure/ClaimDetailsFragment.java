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
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.fao.sola.clients.android.opentenure.button.listener.SaveDetailsListener;
import org.fao.sola.clients.android.opentenure.button.listener.SaveDetailsNegativeListener;
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.form.FieldConstraint;
import org.fao.sola.clients.android.opentenure.form.FormPayload;
import org.fao.sola.clients.android.opentenure.form.FormTemplate;
import org.fao.sola.clients.android.opentenure.maps.BasePropertyBoundary;
import org.fao.sola.clients.android.opentenure.maps.EditablePropertyBoundary;
import org.fao.sola.clients.android.opentenure.model.Attachment;
import org.fao.sola.clients.android.opentenure.model.Boundary;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.model.ClaimType;
import org.fao.sola.clients.android.opentenure.model.Configuration;
import org.fao.sola.clients.android.opentenure.model.HoleVertex;
import org.fao.sola.clients.android.opentenure.model.LandUse;
import org.fao.sola.clients.android.opentenure.model.Owner;
import org.fao.sola.clients.android.opentenure.model.Person;
import org.fao.sola.clients.android.opentenure.model.ShareProperty;
import org.fao.sola.clients.android.opentenure.model.Vertex;
import org.fao.sola.clients.android.opentenure.print.PDFClaimExporter;
import org.fao.sola.clients.android.opentenure.tools.StringUtility;
import org.h2.util.StringUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

public class ClaimDetailsFragment extends Fragment {

	View rootView;
	private ClaimDispatcher claimActivity;
	private ModeDispatcher modeActivity;
	private FormDispatcher formDispatcher;
	private ClaimListener claimListener;
	private Map<String, String> keyValueMapLandUse;
	private Map<String, String> valueKeyMapLandUse;
	private Map<String, String> keyValueClaimTypesMap;
	private Map<String, String> valueKeyClaimTypesMap;
	private boolean challengedJustLoaded = false;
	private List<Boundary> boundaries;

	private static final int PERSON_RESULT = 100;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			claimActivity = (ClaimDispatcher) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement ClaimDispatcher");
		}
		try {
			modeActivity = (ModeDispatcher) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement ModeDispatcher");
		}
		try {
			claimListener = (ClaimListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement ClaimListener");
		}
		try {
			formDispatcher = (FormDispatcher) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement FormDispatcher");
		}
	}

	public ClaimDetailsFragment() {
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

		inflater.inflate(R.menu.claim_details, menu);
		super.onCreateOptionsMenu(menu, inflater);
		Claim claim = Claim.getClaim(claimActivity.getClaimId());

		if (claim != null && !claim.isModifiable()) {
			menu.removeItem(R.id.action_save);
		}

		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null) { // No selection has been done
			switch (requestCode) {
				case PERSON_RESULT:
					String personId = data.getStringExtra(PersonActivity.PERSON_ID_KEY);
					Person claimant = Person.getPerson(personId);
					loadClaimant(claimant);

					// Check if claimant is not matching owners and show a warning
					List<ShareProperty> shares = ShareProperty.getShares(claimActivity.getClaimId());
					if(shares != null && shares.size() > 0 && claimant != null){
						boolean matching = false;
						for(ShareProperty share: shares){
							if(matching) {
								break;
							}
							List<Owner> owners = Owner.getOwners(share.getId());
							if (owners != null && owners.size() > 0) {
								for (Owner owner : owners) {
									if (owner.getPersonId().equalsIgnoreCase(claimant.getPersonId())) {
										matching = true;
										break;
									}
								}
							}
						}
						if(!matching){
							Toast toast = Toast.makeText(rootView.getContext(),	R.string.message_claimant_not_matching_owners, Toast.LENGTH_LONG);
							toast.show();
						}
					}
					break;
				case SelectClaimActivity.SELECT_CLAIM_ACTIVITY_RESULT:
					String claimId = data.getStringExtra(ClaimActivity.CLAIM_ID_KEY);
					Claim challengedClaim = Claim.getClaim(claimId);

					loadChallengedClaim(challengedClaim);
					challengedJustLoaded = true;
					break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		OpenTenureApplication.setClaimId(claimActivity.getClaimId());
		OpenTenureApplication.setDetailsFragment(this);

		rootView = inflater.inflate(R.layout.fragment_claim_details, container, false);
		setHasOptionsMenu(true);

		InputMethodManager imm = (InputMethodManager) rootView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);

		Claim claim = Claim.getClaim(claimActivity.getClaimId());
		preload(claim);

		load(claim);

		ProgressBar bar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
		TextView status = (TextView) rootView.findViewById(R.id.claim_status);

		if (claim != null) {
			if (!claim.getStatus().equals(ClaimStatus._UPLOADING)
					&& !claim.getStatus().equals(ClaimStatus._UPDATE_INCOMPLETE)
					&& !claim.getStatus().equals(ClaimStatus._UPLOAD_INCOMPLETE)
					&& !claim.getStatus().equals(ClaimStatus._UPDATING)) {
				bar.setVisibility(View.GONE);
				status.setVisibility(View.GONE);
			} else {
				status = (TextView) rootView.findViewById(R.id.claim_status);
				int progress = FileSystemUtilities.getUploadProgress(claim.getClaimId(), claim.getStatus());

				// Setting the update value in the progress bar
				bar.setVisibility(View.VISIBLE);
				bar.setProgress(progress);
				status.setVisibility(View.VISIBLE);
				status.setText(claim.getStatus() + " " + progress + " %");
			}
		}

		String claimantId = ((TextView) rootView.findViewById(R.id.claimant_id)).getText().toString();
		TextView claimantSlogan = (TextView) rootView.findViewById(R.id.claimant_slogan);
		ImageView claimantRemove = (ImageView) rootView.findViewById(R.id.action_remove_person);
		View claimantPhoto = (View) rootView.findViewById(R.id.claimant_picture);
		Button addClaimantBtn = (Button) rootView.findViewById(R.id.claimant_button);

		if (OpenTenureApplication.getInstance().getLocalization().startsWith("ar")) {
			claimantSlogan.setTextDirection(View.TEXT_DIRECTION_LOCALE);
			claimantSlogan.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
		}
		if (claimantId == null || claimantId.trim().equals("")) {
			claimantSlogan.setVisibility(View.GONE);
			claimantPhoto.setVisibility(View.GONE);
			claimantRemove.setVisibility(View.GONE);
			addClaimantBtn.setVisibility(View.VISIBLE);
		}

		claimantSlogan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String claimantId = ((TextView) rootView.findViewById(R.id.claimant_id)).getText().toString();
				Person claimant = Person.getPerson(claimantId);
				Intent intent = new Intent(v.getContext(), PersonActivity.class);
				intent.putExtra(PersonActivity.PERSON_ID_KEY, claimantId);
				intent.putExtra(PersonActivity.ENTITY_TYPE, claimant.getPersonType());
				if(modeActivity != null) {
					intent.putExtra(PersonActivity.MODE_KEY, modeActivity.getMode().toString());
				}
				startActivityForResult(intent, PERSON_RESULT);
			}
		});

		addClaimantBtn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(rootView.getContext(), SelectPersonActivity.class);
						intent.putExtra(PersonActivity.PERSON_ID_KEY, PersonActivity.CREATE_PERSON_ID);
						intent.putExtra(PersonActivity.MODE_KEY, modeActivity.getMode().toString());
						startActivityForResult(intent, PERSON_RESULT);
					}
				});

		claimantRemove.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				AlertDialog.Builder dialogClearClaimant = new AlertDialog.Builder(v.getContext());

				dialogClearClaimant.setTitle(R.string.title_remove_claimant_dialog);
				dialogClearClaimant.setMessage(getContext().getString(R.string.confirm_claimant_removal));

				dialogClearClaimant.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						loadClaimant(null);
					}
				});
				dialogClearClaimant.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});
				dialogClearClaimant.show();
			}
		});

		if (modeActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RW) == 0) {
			((View) rootView.findViewById(R.id.challenge_button)).setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent(rootView.getContext(), SelectClaimActivity.class);
							// Excluding from the list of claims that can be
							// challenged
							ArrayList<String> excludeList = new ArrayList<String>();
							List<Claim> claims = Claim.getSimplifiedClaims();
							for (Claim claim : claims) {
								// Challenges and local claims not yet
								// uploaded
								if (claim.getChallengedClaim() != null
										|| claim.isDeleted()
										|| claim.getStatus().equalsIgnoreCase(Claim.Status.created.toString())
										|| claim.getStatus().equalsIgnoreCase(Claim.Status.uploading.toString())
										|| !claim.isUploadable()) {
									excludeList.add(claim.getClaimId());
								}
							}
							intent.putStringArrayListExtra(
									SelectClaimActivity.EXCLUDE_CLAIM_IDS_KEY,
									excludeList);
							startActivityForResult(
									intent,
									SelectClaimActivity.SELECT_CLAIM_ACTIVITY_RESULT);
						}
					});
		}

		return rootView;
	}

	private void preload(Claim claim) {

		boolean onlyActiveValues;
		if (claim == null)
			onlyActiveValues = true;
		else
			onlyActiveValues = (claim.getStatus()
					.equals(ClaimStatus._MODERATED)
					|| claim.getStatus().equals(ClaimStatus._REJECTED)
					|| claim.getStatus().equals(ClaimStatus._REVIEWED) || claim
					.getStatus().equals(ClaimStatus._REJECTED));

		// Claim Types Spinner
		Spinner spinner = (Spinner) rootView.findViewById(R.id.claimTypesSpinner);

		ClaimType ct = new ClaimType();

		keyValueClaimTypesMap = ct.getKeyValueMap(OpenTenureApplication.getInstance().getLocalization(), onlyActiveValues);
		valueKeyClaimTypesMap = ct.getValueKeyMap(OpenTenureApplication.getInstance().getLocalization(), onlyActiveValues);
		List<String> list = new ArrayList<String>();

		SortedSet<String> keys = new TreeSet<String>(
				keyValueClaimTypesMap.keySet());
		for (String key : keys) {
			String value = keyValueClaimTypesMap.get(key);
			list.add(value);
		}

		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(
				OpenTenureApplication.getContext(), R.layout.my_spinner, list) {
		};
		dataAdapter.setDropDownViewResource(R.layout.my_spinner);

		spinner.setAdapter(dataAdapter);

		// Land Uses Spinner
		Spinner spinnerLU = (Spinner) rootView.findViewById(R.id.landUseSpinner);

		LandUse lu = new LandUse();
		keyValueMapLandUse = lu.getKeyValueMap(OpenTenureApplication.getInstance().getLocalization(), onlyActiveValues);
		valueKeyMapLandUse = lu.getValueKeyMap(OpenTenureApplication.getInstance().getLocalization(), onlyActiveValues);

		List<String> landUseslist = new ArrayList<String>();
		keys = new TreeSet<String>(keyValueMapLandUse.keySet());
		for (String key : keys) {
			String value = keyValueMapLandUse.get(key);
			landUseslist.add(value);
		}

		ArrayAdapter<String> dataAdapterLU = new ArrayAdapter<String>(
				OpenTenureApplication.getContext(), R.layout.my_spinner, landUseslist) {
		};
		dataAdapterLU.setDropDownViewResource(R.layout.my_spinner);

		spinnerLU.setAdapter(dataAdapterLU);

		// Boundary
		boundaries = Boundary.getFormattedBoundariesAll(true);
		Spinner spinnerBoundaries = (Spinner) rootView.findViewById(R.id.boundarySpinner);
		spinnerBoundaries.setAdapter(new ArrayAdapter(OpenTenureApplication.getContext(), R.layout.my_spinner, boundaries));
		((ArrayAdapter) spinnerBoundaries.getAdapter()).setDropDownViewResource(R.layout.my_spinner);

		// Claimant
		((TextView) rootView.findViewById(R.id.claimant_id)).setTextSize(8);
		((TextView) rootView.findViewById(R.id.claimant_id)).setText("");
		ImageView claimantImageView = (ImageView) rootView.findViewById(R.id.claimant_picture);

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_contact_picture);

		claimantImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 128, 128, true));

		// Challenged claim
		((TextView) rootView.findViewById(R.id.challenge_to_claim_id)).setTextSize(8);
		((TextView) rootView.findViewById(R.id.challenge_to_claim_id)).setText("");

		// Challenged claimant
		ImageView challengedClaimantImageView = (ImageView) rootView.findViewById(R.id.challenge_to_claimant_picture);
		challengedClaimantImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 128, 128, true));

		EditText dateOfStart = (EditText) rootView.findViewById(R.id.date_of_start_input_field);
	}

	private void loadChallengedClaim(Claim challengedClaim) {

		if (challengedClaim != null) {

			Person challengedPerson = challengedClaim.getPerson();
			((TextView) rootView.findViewById(R.id.challenge_to_claim_id))
					.setTextSize(8);
			((TextView) rootView.findViewById(R.id.challenge_to_claim_id))
					.setText(challengedClaim.getClaimId());
			((TextView) rootView.findViewById(R.id.challenge_to_claim_slogan))
					.setBackgroundColor(getResources().getColor(
							R.color.light_background_opentenure));
			((TextView) rootView.findViewById(R.id.challenge_to_claim_slogan))
					.setText(getResources().getString(
							R.string.title_challenged_claims)
							+ " "
							+ challengedClaim.getName()
							+ ", "
							+ getResources().getString(R.string.by)
							+ ": "
							+ challengedPerson.getFirstName()
							+ " "
							+ challengedPerson.getLastName()
							+ ", "
							+ getResources().getString(R.string.status)
							+ challengedClaim.getStatus());
			((TextView) rootView.findViewById(R.id.challenge_to_claim_slogan))
					.setVisibility(View.VISIBLE);
			ImageView challengedClaimantImageView = (ImageView) rootView
					.findViewById(R.id.challenge_to_claimant_picture);

			((View) rootView.findViewById(R.id.challenge_button))
					.setEnabled(false);

			// File challengedPersonPictureFile = Person
			// .getPersonPictureFile(challengedPerson.getPersonId());
			challengedClaimantImageView
					.setImageBitmap(Person.getPersonPicture(
							rootView.getContext(),
							challengedPerson.getPersonId(), 128));

			ImageView challengedClaimantRemoveButton = (ImageView) rootView
					.findViewById(R.id.action_remove_challenge);

			if (modeActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RO) != 0)
				challengedClaimantRemoveButton.setVisibility(View.VISIBLE);

			challengedClaimantRemoveButton
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {

							((TextView) rootView
									.findViewById(R.id.challenge_to_claim_id))
									.setText("");

							((TextView) rootView
									.findViewById(R.id.challenge_to_claim_slogan))
									.setVisibility(View.GONE);

							((ImageView) rootView
									.findViewById(R.id.action_remove_challenge))
									.setVisibility(View.INVISIBLE);
							((View) rootView
									.findViewById(R.id.challenge_button))
									.setEnabled(true);

						}
					});

		} else {

			((TextView) rootView.findViewById(R.id.challenge_to_claim_slogan))
					.setVisibility(View.GONE);
			((TextView) rootView.findViewById(R.id.challenge_to_claim_slogan))
					.setVisibility(View.GONE);
		}
	}

	private void loadClaimant(Person claimant) {
		TextView slogan = (TextView) rootView.findViewById(R.id.claimant_slogan);
		ImageView claimantRemove = (ImageView) rootView.findViewById(R.id.action_remove_person);
		TextView claimantId = (TextView) rootView.findViewById(R.id.claimant_id);
		Button addClaimant = (Button) rootView.findViewById(R.id.claimant_button);
		ImageView claimantPhoto = (ImageView) rootView.findViewById(R.id.claimant_picture);

		if (claimant != null) {
			if (OpenTenureApplication.getInstance().getLocale().toString().startsWith("ar")) {
				slogan.setTextDirection(View.TEXT_DIRECTION_LOCALE);
				slogan.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
			}

			claimantId.setText(claimant.getPersonId());
			slogan.setVisibility(View.VISIBLE);

			String sloganText = "<b>" + claimant.getFirstName() + " " + claimant.getLastName() + "</b><i>";

			// Add ID
			if(!StringUtility.empty(claimant.getIdNumber()).equals("")){
				sloganText += "<br>" + getResources().getString(R.string.id_number) + ": " + claimant.getIdNumber();
			}
			// Add DOB
			if(claimant.getDateOfBirth() != null){
				String strDateOfBirth = "";
				if(claimant.getPersonType().equalsIgnoreCase(PersonActivity.TYPE_PERSON)){
					strDateOfBirth = getResources().getString(R.string.date_of_birth_simple);
				} else {
					strDateOfBirth = getResources().getString(R.string.date_of_establishment_label);
				}
				sloganText += "<br>" + strDateOfBirth + ": " + claimant.getDateOfBirth();
			}
			sloganText += "</i>";

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				slogan.setText(Html.fromHtml(sloganText, Html.FROM_HTML_MODE_COMPACT));
			} else {
				slogan.setText(Html.fromHtml(sloganText));
			}

			claimantPhoto.setImageBitmap(Person.getPersonPicture(rootView.getContext(), claimant.getPersonId(), 128));

			if (modeActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RO) == 0) {
				claimantRemove.setVisibility(View.GONE);
			} else {
				claimantRemove.setVisibility(View.VISIBLE);
			}

			addClaimant.setVisibility(View.GONE);
			if(claimant.getPersonType().equalsIgnoreCase(Person._PHYSICAL)) {
				claimantPhoto.setVisibility(View.VISIBLE);
			} else {
				claimantPhoto.setVisibility(View.GONE);
			}
		} else {
			claimantId.setText("");
			claimantRemove.setVisibility(View.GONE);
			slogan.setVisibility(View.GONE);
			claimantPhoto.setVisibility(View.GONE);
			addClaimant.setVisibility(View.VISIBLE);
		}
	}

	public void reloadArea(Claim claim) {
		((TextView) rootView.findViewById(R.id.claim_area_label)).setText(R.string.claim_area_label);
		((TextView) rootView.findViewById(R.id.claim_area_label)).setVisibility(View.VISIBLE);
		((TextView) rootView.findViewById(R.id.claim_area)).setText(claim.getClaimArea() + " " + OpenTenureApplication.getContext().getString(
				R.string.square_meters));
		((TextView) rootView.findViewById(R.id.claim_area)).setVisibility(View.VISIBLE);
	}

	public void load(Claim claim) {
		if (claim != null) {

			boolean onlyActiveValues = (!claim.getStatus().equals(
					ClaimStatus._MODERATED)
					&& claim.getStatus().equals(ClaimStatus._REJECTED)
					&& !claim.getStatus().equals(ClaimStatus._REVIEWED)
					&& !claim.getStatus().equals(ClaimStatus._UNMODERATED)
					&& !claim.getStatus().equals(ClaimStatus._UPLOAD_ERROR) && !claim
					.getStatus().equals(ClaimStatus._UPLOAD_INCOMPLETE));

			if (OpenTenureApplication.getInstance().getLocale().toString().startsWith("ar")) {
				((EditText) rootView.findViewById(R.id.claim_name_input_field))
						.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
				((EditText) rootView.findViewById(R.id.claim_name_input_field))
						.setTextDirection(View.TEXT_DIRECTION_LOCALE);
			}
			((EditText) rootView.findViewById(R.id.claim_name_input_field))
					.setText(claim.getName());
			((Spinner) rootView.findViewById(R.id.claimTypesSpinner))
					.setSelection(new ClaimType().getIndexByCodeType(
							claim.getType(), onlyActiveValues));

			((Spinner) rootView.findViewById(R.id.landUseSpinner))
					.setSelection(new LandUse().getIndexByCodeType(
							claim.getLandUse(), onlyActiveValues));

			if(claim.getBoundaryId() != null && boundaries != null){
				for(int i = 0; i < boundaries.size(); i++){
					if(claim.getBoundaryId().equals(boundaries.get(i).getId())){
						((Spinner) rootView.findViewById(R.id.boundarySpinner)).setSelection(i);
						break;
					}
				}
			}

			((EditText) rootView.findViewById(R.id.claim_notes_input_field)).setText(claim.getNotes());

			if (claim.getClaimArea() > 0) {
				((TextView) rootView.findViewById(R.id.claim_area_label)).setText(R.string.claim_area_label);
				((TextView) rootView.findViewById(R.id.claim_area_label)).setVisibility(View.VISIBLE);
				((TextView) rootView.findViewById(R.id.claim_area)).setText(claim.getClaimArea() + " " + OpenTenureApplication.getContext().getString(
								R.string.square_meters));
				((TextView) rootView.findViewById(R.id.claim_area)).setVisibility(View.VISIBLE);
			}

			if (claim.getDateOfStart() != null) {
				((EditText) rootView
						.findViewById(R.id.date_of_start_input_field))
						.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.US)
								.format(claim.getDateOfStart()));
			} else {
				((EditText) rootView
						.findViewById(R.id.date_of_start_input_field))
						.setText("");
			}
			if (modeActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RO) == 0) {
				((EditText) rootView.findViewById(R.id.claim_name_input_field))
						.setFocusable(false);
				((Spinner) rootView.findViewById(R.id.claimTypesSpinner))
						.setFocusable(false);
				((Spinner) rootView.findViewById(R.id.claimTypesSpinner))
						.setClickable(false);

				((Spinner) rootView.findViewById(R.id.landUseSpinner))
						.setFocusable(false);
				((Spinner) rootView.findViewById(R.id.landUseSpinner))
						.setClickable(false);
				((EditText) rootView
						.findViewById(R.id.date_of_start_input_field))
						.setFocusable(false);
				((EditText) rootView
						.findViewById(R.id.date_of_start_input_field))
						.setLongClickable(false);
				((EditText) rootView
						.findViewById(R.id.date_of_start_input_field))
						.setOnTouchListener(null);

				((EditText) rootView.findViewById(R.id.claim_notes_input_field)).setFocusable(false);
			}

			Person claimant = null;
			String claimantId = ((TextView) rootView
					.findViewById(R.id.claimant_id)).getText().toString();

			if (claimantId == null || claimantId.trim().equals(""))
				claimant = claim.getPerson();
			else
				claimant = Person.getPerson(claimantId);
			loadClaimant(claimant);

			if (challengedJustLoaded) {
				challengedJustLoaded = false;
			} else
				loadChallengedClaim(claim.getChallengedClaim());
		}
	}

	public int saveClaim() {
		Person person = Person.getPerson(((TextView) rootView.findViewById(R.id.claimant_id)).getText().toString());
		Claim challengedClaim = Claim
				.getClaim(((TextView) rootView
						.findViewById(R.id.challenge_to_claim_id)).getText()
						.toString());

		Claim claim = new Claim();
		String claimName = ((EditText) rootView
				.findViewById(R.id.claim_name_input_field)).getText()
				.toString();

		if (claimName == null || claimName.trim().equals(""))
			return 3;
		claim.setName(claimName);

		String displayValue = (String) ((Spinner) rootView.findViewById(R.id.claimTypesSpinner)).getSelectedItem();
		claim.setType(valueKeyClaimTypesMap.get(displayValue));

		String landUseDispValue = (String) ((Spinner) rootView.findViewById(R.id.landUseSpinner)).getSelectedItem();
		claim.setLandUse(valueKeyMapLandUse.get(landUseDispValue));

		String boundaryId = ((Boundary)((Spinner) rootView.findViewById(R.id.boundarySpinner)).getSelectedItem()).getId();
		if(boundaryId == null || boundaryId.equals("")){
			claim.setBoundaryId(null);
		} else {
			claim.setBoundaryId(boundaryId);
		}

		String notes = ((EditText) rootView
				.findViewById(R.id.claim_notes_input_field)).getText()
				.toString();

		claim.setNotes(notes);

		String startDate = ((EditText) rootView
				.findViewById(R.id.date_of_start_input_field)).getText()
				.toString();

		java.util.Date dob = null;

		if (startDate != null && !startDate.trim().equals("")) {
			try {

				dob = new SimpleDateFormat("yyyy-MM-dd", Locale.US)
						.parse(startDate);

				if (dob != null)
					claim.setDateOfStart(new Date(dob.getTime()));

			} catch (ParseException e) {
				e.printStackTrace();
				dob = null;
				return 2;
			}

		}

		if (person == null)
			return 4;

		claim.setPerson(person);
		claim.setChallengedClaim(challengedClaim);
		// Still allow saving the claim if the dynamic part contains errors
		isFormValid();

		claim.setDynamicForm(formDispatcher.getEditedFormPayload());
		claim.setVersion("0");

		if (claim.create() == 1) {
			List<Vertex> vertices = Vertex.getVertices(claim.getClaimId());
			if (challengedClaim != null
					&& (vertices == null || vertices.size() == 0)) {
				copyVerticesFromChallengedClaim(challengedClaim.getClaimId(),
						claim.getClaimId());
			}

			OpenTenureApplication.setClaimId(claim.getClaimId());

			FileSystemUtilities.createClaimFileSystem(claim.getClaimId());
			claimActivity.setClaimId(claim.getClaimId());

			if (createPersonAsOwner(person) == 0)
				return 0;

			claimListener.onClaimSaved();
			return 1;

		} else
			return 5;

	}

	private void copyVerticesFromChallengedClaim(String challengedClaimId,
												 String challengingClaimId) {
		Log.d(this.getClass().getName(), "copying vertices from "
				+ challengedClaimId + " to " + challengingClaimId);
		// delete eventually existing vertices
		Vertex.deleteVertices(challengingClaimId);
		// get vertices from the challenged claim
		List<Vertex> vertices = Vertex.getVertices(challengedClaimId);
		List<Vertex> copiedVertices = new ArrayList<Vertex>();
		for (Vertex vertex : vertices) {
			Vertex copiedVertex = new Vertex(vertex);
			// associate it to the challenging claim id
			copiedVertex.setClaimId(challengingClaimId);
			copiedVertices.add(copiedVertex);
		}
		// save them again
		Vertex.createVertices(copiedVertices);

		// delete eventually existing holes vertices
		HoleVertex.deleteVertices(challengingClaimId);
		// get vertices from the challenged claim
		List<List<HoleVertex>> holes = HoleVertex.getHoles(challengedClaimId);
		List<List<HoleVertex>> copiedHoles = new ArrayList<List<HoleVertex>>();
		for(List<HoleVertex> hole:holes){
			List<HoleVertex> copiedHole = new ArrayList<HoleVertex>();
			for (HoleVertex vertex : hole) {
				HoleVertex copiedVertex = new HoleVertex(vertex);
				// associate it to the challenging claim id
				copiedVertex.setClaimId(challengingClaimId);
				copiedHole.add(copiedVertex);
			}
			copiedHoles.add(copiedHole);
		}
		// save them again
		HoleVertex.createVertices(copiedHoles);
	}

	public int updateClaim() {

		Person person = Person.getPerson(((TextView) rootView.findViewById(R.id.claimant_id)).getText().toString());

		if (OpenTenureApplication.getInstance().getLocale().toString().startsWith("ar")) {
			((View) rootView.findViewById(R.id.claimant_slogan)).setTextDirection(View.TEXT_DIRECTION_LOCALE);
			((View) rootView.findViewById(R.id.claimant_slogan)).setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
		}
		if (person != null)
			((View) rootView.findViewById(R.id.claimant_slogan)).setVisibility(View.VISIBLE);

		// Check geometry
		if(!BasePropertyBoundary.isValidateGeometry(claimActivity.getClaimId())){
			return 10;
		}

		Claim challengedClaim = Claim.getClaim(((TextView) rootView.findViewById(R.id.challenge_to_claim_id)).getText().toString());

		// Claim claim = Claim.getClaim(claimActivity.getClaimId());
		Claim claim = Claim.getClaim(claimActivity.getClaimId());
		claim.setClaimId(claimActivity.getClaimId());
		claim.setName(((EditText) rootView.findViewById(R.id.claim_name_input_field)).getText().toString());

		if (claim.getName() == null || claim.getName().trim().equals(""))
			return 0;

		String displayValue = (String) ((Spinner) rootView.findViewById(R.id.claimTypesSpinner)).getSelectedItem();
		claim.setType(valueKeyClaimTypesMap.get(displayValue));

		String landUseDispValue = (String) ((Spinner) rootView.findViewById(R.id.landUseSpinner)).getSelectedItem();
		claim.setLandUse(valueKeyMapLandUse.get(landUseDispValue));

		String boundaryId = ((Boundary)((Spinner) rootView.findViewById(R.id.boundarySpinner)).getSelectedItem()).getId();
		if(boundaryId == null || boundaryId.equals("")){
			claim.setBoundaryId(null);
		} else {
			claim.setBoundaryId(boundaryId);
		}

		String notes = ((EditText) rootView.findViewById(R.id.claim_notes_input_field)).getText().toString();
		claim.setNotes(notes);

		String startDate = ((EditText) rootView.findViewById(R.id.date_of_start_input_field)).getText().toString();
		java.util.Date occupiedSince = null;

		if (startDate != null && !startDate.trim().equals("")) {
			try {
				occupiedSince = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(startDate);
				if (occupiedSince != null)
					claim.setDateOfStart(new Date(occupiedSince.getTime()));

			} catch (ParseException e) {
				e.printStackTrace();
				return 2;
			}
		}

		// Still allow saving the claim if the dynamic part contains errors
		isFormValid();

		if (createPersonAsOwner(person) == 0)
			return 0;

		claim.setPerson(person);
		claim.setChallengedClaim(challengedClaim);
		claim.setDynamicForm(formDispatcher.getEditedFormPayload());

		int result = claim.update();

		if (challengedClaim != null) {
			List<Vertex> vertices = Vertex.getVertices(claim.getClaimId());
			if (vertices == null || vertices.size() == 0) {
				copyVerticesFromChallengedClaim(challengedClaim.getClaimId(),
						claim.getClaimId());
			}
		}

		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle item selection
		Toast toast;
		switch (item.getItemId()) {

			case R.id.action_save:

				if (claimActivity.getClaimId() == null) {
					int resultSave = saveClaim();
					if (resultSave == 1) {
						toast = Toast.makeText(rootView.getContext(),
								R.string.message_saved, Toast.LENGTH_SHORT);
						toast.show();
					}
					if (resultSave == 2) {
						toast = Toast.makeText(rootView.getContext(),
								R.string.message_error_startdate,
								Toast.LENGTH_SHORT);
						toast.show();
					} else if (resultSave == 3) {
						toast = Toast.makeText(rootView.getContext(),
								R.string.message_unable_to_save_missing_claim_name,
								Toast.LENGTH_SHORT);
						toast.show();
					} else if (resultSave == 4) {
						toast = Toast.makeText(rootView.getContext(),
								R.string.message_unable_to_save_missing_person,
								Toast.LENGTH_SHORT);
						toast.show();
					} else if (resultSave == 5) {
						toast = Toast
								.makeText(rootView.getContext(),
										R.string.message_unable_to_save,
										Toast.LENGTH_SHORT);
						toast.show();
					}
				} else {
					int updated = updateClaim();

					if (updated == 1) {
						toast = Toast.makeText(rootView.getContext(),
								R.string.message_saved, Toast.LENGTH_SHORT);
						toast.show();

					} else if (updated == 2) {
						toast = Toast.makeText(rootView.getContext(),
								R.string.message_error_startdate,
								Toast.LENGTH_SHORT);
						toast.show();
					} else if (updated == 10) {
						toast = Toast.makeText(rootView.getContext(),
								R.string.message_invalid_geom,
								Toast.LENGTH_SHORT);
						toast.show();
					} else {
						toast = Toast
								.makeText(rootView.getContext(),
										R.string.message_unable_to_save,
										Toast.LENGTH_SHORT);
						toast.show();
					}
				}

				return true;

			case R.id.action_print:
				Claim claim = Claim.getClaim(claimActivity.getClaimId());
				boolean mapPresent = false;
				boolean mapToDownload = false;
				String path = null;

				if (claim == null) {
					toast = Toast.makeText(rootView.getContext(),
							R.string.message_save_snapshot_before_printing,
							Toast.LENGTH_LONG);
					toast.show();
					return true;
				}

				for (Attachment attachment : claim.getAttachments()) {
					if (EditablePropertyBoundary.DEFAULT_MAP_FILE_NAME
							.equalsIgnoreCase(attachment.getFileName())
							&& EditablePropertyBoundary.DEFAULT_MAP_FILE_TYPE
							.equalsIgnoreCase(attachment.getFileType())
							&& EditablePropertyBoundary.DEFAULT_MAP_MIME_TYPE
							.equalsIgnoreCase(attachment.getMimeType())) {
						mapPresent = true;
						path = attachment.getPath();
						if (!path.startsWith("content://") && !(new File(path).exists())) {
							mapToDownload=true;
						}
					}
				}
				if (!mapPresent) {
					toast = Toast.makeText(rootView.getContext(),
							R.string.message_save_snapshot_before_printing,
							Toast.LENGTH_LONG);
					toast.show();
					return true;
				}
				if (mapToDownload) {

					toast = Toast.makeText(rootView.getContext(),
							R.string.message_download_snapshot_before_printing,
							Toast.LENGTH_LONG);
					toast.show();
					return true;
				}
				try {
					PDFClaimExporter pdf = new PDFClaimExporter(
							rootView.getContext(), claim, false, PDFClaimExporter.PageType.A4);

					Intent intent = new Intent(Intent.ACTION_VIEW);
					File basePath =
							new File(Environment.getExternalStorageDirectory(),"Open Tenure/Certificates");
					File file =
							new File(basePath,pdf.getFileName());

					Uri uri = FileProvider.getUriForFile(rootView.getContext(), BuildConfig.APPLICATION_ID, file);
					intent.setDataAndType(uri,"application/pdf");
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | FLAG_GRANT_WRITE_URI_PERMISSION) ;

					startActivity(intent);

				} catch (Error e) {
					toast = Toast.makeText(rootView.getContext(),
							R.string.message_not_supported_on_this_device,
							Toast.LENGTH_SHORT);
					toast.show();
				}

				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public boolean isClaimChanged(Claim claim){

		if (!claim.getName().equalsIgnoreCase(
				((EditText) rootView
						.findViewById(R.id.claim_name_input_field))
						.getText().toString())){
			Log.d(this.getClass().getName(), "Claim name has changed");
			return true;
		}

		Person person = Person.getPerson(((TextView) rootView
				.findViewById(R.id.claimant_id)).getText().toString());
		if (person == null || !claim.getPerson().getPersonId().equalsIgnoreCase(person.getPersonId())){
			Log.d(this.getClass().getName(), "Claimant has changed");
			return true;
		}

		Claim challengedClaim = Claim.getClaim(((TextView) rootView
				.findViewById(R.id.challenge_to_claim_id))
				.getText().toString());
		if ((challengedClaim == null && claim.getChallengedClaim() != null)
				|| (challengedClaim != null && claim.getChallengedClaim() == null)
				|| (challengedClaim != null && claim.getChallengedClaim() != null
				&& !claim.getChallengedClaim().getClaimId().equalsIgnoreCase(challengedClaim.getClaimId()))){
			Log.d(this.getClass().getName(), "Challenged claim has changed");
			return true;
		}

		String claimType = (String) ((Spinner) rootView
				.findViewById(R.id.claimTypesSpinner))
				.getSelectedItem();

		if (!claim.getType().equalsIgnoreCase(valueKeyClaimTypesMap.get(claimType))){
			Log.d(this.getClass().getName(), "Claim type has changed");
			return true;
		}

		String landUseDispValue = (String) ((Spinner) rootView
				.findViewById(R.id.landUseSpinner))
				.getSelectedItem();
		if ((claim.getLandUse() == null && valueKeyMapLandUse.get(landUseDispValue) != null)
				|| (!claim.getLandUse().equals(valueKeyMapLandUse.get(landUseDispValue)))){
			Log.d(this.getClass().getName(), "Land use has changed");
			return true;
		}

		String boundaryId = ((Boundary)((Spinner) rootView.findViewById(R.id.boundarySpinner)).getSelectedItem()).getId();
		if (!StringUtils.equals(claim.getBoundaryId(), boundaryId)){
			Log.d(this.getClass().getName(), "Boundary has changed");
			return true;
		}

		String notes = ((EditText) rootView
				.findViewById(R.id.claim_notes_input_field))
				.getText().toString();

		if (claim.getNotes() != null && !claim.getNotes().equals(notes)){
			Log.d(this.getClass().getName(), "Claim notes have changed");
			return true;
		}
		String startDate = ((EditText) rootView
				.findViewById(R.id.date_of_start_input_field))
				.getText().toString();

		if (claim.getDateOfStart() == null ^ startDate.trim().equalsIgnoreCase("")) {

			Log.d(this.getClass().getName(), "Rights start date has changed");
			return true;

		}
		if (!startDate.trim().equalsIgnoreCase("")) {

			try {
				java.util.Date dos = new SimpleDateFormat(
						"yyyy-MM-dd", Locale.US)
						.parse(startDate);

				Date date = new Date(
						dos.getTime());

				if (claim.getDateOfStart()
						.compareTo(date) != 0) {
					Log.d(this.getClass().getName(), "Rights start date has changed");
					return true;
				}
			} catch (ParseException e) {
				e.printStackTrace();
				return true;
			}
		}

		return isFormChanged();
	}

	public boolean hasFragmentValues(){

		if (!((EditText) rootView
				.findViewById(R.id.claim_name_input_field)).getText()
				.toString().trim().equals("")){
			Log.d(this.getClass().getName(), "Claim name has value");
			return true;
		}

		String person = ((TextView) rootView
				.findViewById(R.id.claimant_id)).getText().toString();
		if (person != null && !person.trim().equals("")){
			Log.d(this.getClass().getName(), "Claimant has value");
			return true;
		}

		String challengedClaim = ((TextView) rootView
				.findViewById(R.id.challenge_to_claim_id))
				.getText().toString();
		if (challengedClaim != null
				&& !challengedClaim.trim().equals("")){
			Log.d(this.getClass().getName(), "Challenged claim has value");
			return true;
		}

		if (!((EditText) rootView
				.findViewById(R.id.claim_notes_input_field))
				.getText().toString().trim().equals("")){
			Log.d(this.getClass().getName(), "Claim notes have value");
			return true;
		}

		if (!((EditText) rootView
				.findViewById(R.id.date_of_start_input_field))
				.getText().toString().trim().equals("")){
			Log.d(this.getClass().getName(), "Rights start date has value");
			return true;
		}

		return isFormChanged();

	}

	public boolean checkChanges() {

		Claim claim = Claim.getClaim(claimActivity.getClaimId());

		if ((claim != null && isClaimChanged(claim)) || (claim == null && hasFragmentValues())) {

			AlertDialog.Builder saveChangesDialog = new AlertDialog.Builder(
					this.getActivity());
			saveChangesDialog.setTitle(R.string.title_save_claim_dialog);
			String dialogMessage = OpenTenureApplication.getContext()
					.getString(R.string.message_discard_changes);

			saveChangesDialog.setMessage(dialogMessage);

			saveChangesDialog.setPositiveButton(R.string.confirm,
					new SaveDetailsNegativeListener(this));

			saveChangesDialog.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
											int which) {
							return;
						}
					});
			saveChangesDialog.show();
			return true;
		}
		return false;
	}
	@Override
	public void onResume() {
		Claim claim = Claim.getClaim(claimActivity.getClaimId());
		load(claim);

		super.onResume();

	};

	public int createPersonAsOwner(Person claimant) {
		try {
			List<ShareProperty> shares = ShareProperty.getShares(claimActivity.getClaimId());

			int value = 0;

			for (Iterator<ShareProperty> iterator = shares.iterator(); iterator.hasNext();) {
				ShareProperty shareProperty = (ShareProperty) iterator.next();
				value = value + shareProperty.getShares();
			}

			int shareValue = 100 - value;

			if (shareValue > 0) {
				ShareProperty share = new ShareProperty();

				share.setClaimId(claimActivity.getClaimId());
				share.setShares(shareValue);

				share.create();

				Person claimantCopy = claimant;
				//Person claimantCopy = claimant.copy();
				//claimantCopy.create();

				/*File personImg = new File(
						FileSystemUtilities.getClaimantFolder(claimant
								.getPersonId())
								+ File.separator
								+ claimant.getPersonId() + ".jpg");

				if (personImg != null)
					FileSystemUtilities.copyFileInClaimantFolder(
							claimantCopy.getPersonId(), personImg);*/

				Owner owner = new Owner();
				owner.setPersonId(claimantCopy.getPersonId());
				owner.setShareId(share.getId());

				owner.create();

				OpenTenureApplication.getOwnersFragment().update();
			}
			return 1;

		} catch (Exception e) {
			Log.d("Details", "An error " + e.getMessage());
			e.printStackTrace();
			return 0;
		}
	}

	private boolean isFormValid() {
		FormPayload formPayload = formDispatcher.getEditedFormPayload();
		FormTemplate formTemplate = formDispatcher.getFormTemplate();
		FieldConstraint constraint = null;
		DisplayNameLocalizer dnl = new DisplayNameLocalizer(OpenTenureApplication.getInstance().getLocalization());

		if ((constraint = formTemplate.getFailedConstraint(formPayload, dnl)) != null) {
			Toast.makeText(rootView.getContext(), dnl.getLocalizedDisplayName(constraint.displayErrorMsg()), Toast.LENGTH_SHORT).show();
			return false;
		} else {
			return true;
		}
	}

	private boolean isFormChanged() {
		FormPayload editedFormPayload = formDispatcher.getEditedFormPayload();
		FormPayload originalFormPayload = formDispatcher
				.getOriginalFormPayload();

		if (((editedFormPayload != null) && (originalFormPayload == null))
				|| ((editedFormPayload == null) && (originalFormPayload != null))
				|| !editedFormPayload.toJson().equalsIgnoreCase(
				originalFormPayload.toJson())) {
			Log.d(this.getClass().getName(), "Dynamic form has changed");
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

}

