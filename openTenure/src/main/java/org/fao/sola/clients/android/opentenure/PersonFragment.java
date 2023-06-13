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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;


import org.fao.sola.clients.android.opentenure.button.listener.ConfirmExit;
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.model.IdType;
import org.fao.sola.clients.android.opentenure.model.Person;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;


import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.fragment.app.Fragment;
import androidx.core.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

public class PersonFragment extends Fragment {

	private View rootView;
	private PersonDispatcher personActivity;
	private ModeDispatcher mainActivity;
	private final Calendar localCalendar = Calendar.getInstance();
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	private File personPictureFile;
	private ImageView claimantImageView;
	private Button changeButton;
	private boolean allowSave = true;
	private Map<String, String> keyValueMapIdTypes;
	private Map<String, String> valueKeyMapIdTypes;
	boolean isPerson = true;
	boolean onlyActive = true;
	

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			personActivity = (PersonDispatcher) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement PersonDispatcher");
		}
		try {
			mainActivity = (ModeDispatcher) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement ModeDispatcher");
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.person, menu);

		if (!allowSave) {
			menu.removeItem(R.id.action_save);
		}

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		if(personActivity.getPersonId() == null)
			onlyActive = true;
		else 
			onlyActive = false;

		if ((personActivity.getEntityType() != null && personActivity.getEntityType().equalsIgnoreCase(PersonActivity.TYPE_GROUP))
				|| (personActivity.getPersonId() != null
				&& Person.getPerson(personActivity.getPersonId()).getPersonType().equals(Person._GROUP))) {

			rootView = inflater.inflate(R.layout.fragment_group, container, false);
			setHasOptionsMenu(true);

			OpenTenureApplication.setPersonsView(rootView);

			InputMethodManager imm = (InputMethodManager) rootView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);

			if (personActivity.getPersonId() != null) {
				loadGroup(personActivity.getPersonId());
			}
			return rootView;
		} else {

			rootView = inflater.inflate(R.layout.fragment_person, container, false);
			setHasOptionsMenu(true);
			InputMethodManager imm = (InputMethodManager) rootView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);

			OpenTenureApplication.setPersonsView(rootView);

			preload();

			EditText dateOfBirth = (EditText) rootView.findViewById(R.id.date_of_birth_input_field);

			claimantImageView = (ImageView) rootView.findViewById(R.id.claimant_picture);
			claimantImageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (personPictureFile != null) {
						Person.deleteAllBmp(personActivity.getPersonId());
						Uri uri = FileProvider.getUriForFile(rootView.getContext(), BuildConfig.APPLICATION_ID, personPictureFile); //Uri.fromFile(personPictureFile)

						Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
						intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
						startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
					} else {
						Toast toast = Toast.makeText(
								rootView.getContext(),
								R.string.message_save_person_before_adding_content,
								Toast.LENGTH_SHORT);
						toast.show();
					}
				}
			});
			if (personActivity.getPersonId() != null) {
				load(personActivity.getPersonId());
			}

			return rootView;
		}
	}

	private void preload() {
		// ID TYPE Spinner
		Spinner spinnerIT = (Spinner) rootView.findViewById(R.id.id_type_spinner);
		Spinner spinnerGender = (Spinner) rootView.findViewById(R.id.gender_spinner);

		IdType it = new IdType();
		SortedSet<String> keys;

		/* Mapping id type localization */
		keyValueMapIdTypes = it.getKeyValueMap(OpenTenureApplication.getInstance().getLocalization(),onlyActive);
		valueKeyMapIdTypes = it.getValueKeyMap(OpenTenureApplication.getInstance().getLocalization(),onlyActive);

		List<String> idTypelist = new ArrayList<String>();
		keys = new TreeSet<String>(keyValueMapIdTypes.keySet());
		for (String key : keys) {
			String value = keyValueMapIdTypes.get(key);
			idTypelist.add(value);
		}

		List<String> genderList = new ArrayList<String>();

		genderList.add(OpenTenureApplication.getContext().getResources()
				.getString(R.string.gender_masculine));
		genderList.add(OpenTenureApplication.getContext().getResources()
				.getString(R.string.gender_feminine));

		ArrayAdapter<String> dataAdapterIT = new ArrayAdapter<String>(
				OpenTenureApplication.getContext(), R.layout.my_spinner,
				idTypelist) {
		};

		ArrayAdapter<String> dataAdapterGender = new ArrayAdapter<String>(
				OpenTenureApplication.getContext(), R.layout.my_spinner,
				genderList) {
		};

		spinnerIT.setAdapter(dataAdapterIT);
		spinnerGender.setAdapter(dataAdapterGender);
	}

	private void load(String personId) {
		
		Person person = Person.getPerson(personId);
		((EditText) rootView.findViewById(R.id.first_name_input_field)).setText(person.getFirstName());
		((EditText) rootView.findViewById(R.id.last_name_input_field)).setText(person.getLastName());
		((EditText) rootView.findViewById(R.id.date_of_birth_input_field)).setText(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(person.getDateOfBirth()));
		((EditText) rootView.findViewById(R.id.postal_address_input_field)).setText(person.getPostalAddress());
		((EditText) rootView.findViewById(R.id.email_address_input_field)).setText(person.getEmailAddress());
		((EditText) rootView.findViewById(R.id.contact_phone_number_input_field)).setText(person.getContactPhoneNumber());
		((Spinner) rootView.findViewById(R.id.id_type_spinner)).setSelection(new IdType().getIndexByCodeType(person.getIdType(),onlyActive));

		if (person.getGender().equals("M")) {
			((Spinner) rootView.findViewById(R.id.gender_spinner)).setSelection(0);
		} else
			((Spinner) rootView.findViewById(R.id.gender_spinner)).setSelection(1);

		((EditText) rootView.findViewById(R.id.id_number)).setText(person.getIdNumber());

		if (person.hasUploadedClaims()) {
			((EditText) rootView.findViewById(R.id.first_name_input_field)).setFocusable(false);
			((EditText) rootView.findViewById(R.id.last_name_input_field)).setFocusable(false);
			((EditText) rootView.findViewById(R.id.date_of_birth_input_field)).setFocusable(false);
			((EditText) rootView.findViewById(R.id.date_of_birth_input_field)).setLongClickable(false);
			((EditText) rootView.findViewById(R.id.date_of_birth_input_field)).setClickable(false);
			((EditText) rootView.findViewById(R.id.postal_address_input_field)).setFocusable(false);
			((EditText) rootView.findViewById(R.id.email_address_input_field)).setFocusable(false);
			((EditText) rootView.findViewById(R.id.contact_phone_number_input_field)).setFocusable(false);
			((Spinner) rootView.findViewById(R.id.id_type_spinner)).setFocusable(false);
			((Spinner) rootView.findViewById(R.id.id_type_spinner)).setClickable(false);
			((ImageView) rootView.findViewById(R.id.claimant_picture)).setClickable(false);
			((Spinner) rootView.findViewById(R.id.gender_spinner)).setFocusable(false);
			((Spinner) rootView.findViewById(R.id.gender_spinner)).setClickable(false);
			((EditText) rootView.findViewById(R.id.id_number)).setFocusable(false);

			allowSave = false;
			getActivity().invalidateOptionsMenu();
		}

		personPictureFile = Person.getPersonPictureFile(person.getPersonId());
		claimantImageView.setImageBitmap(Person.getPersonPicture(rootView.getContext(), person.getPersonId(), 128));
	}

	private void loadGroup(String personId) {
		Person person = Person.getPerson(personId);
		((EditText) rootView.findViewById(R.id.first_name_input_field)).setText(person.getFirstName());
		if (person.getDateOfBirth() != null)
			((EditText) rootView
					.findViewById(R.id.date_of_establishment_input_field))
					.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(person.getDateOfBirth()));

		((EditText) rootView.findViewById(R.id.id_number)).setText(person.getIdNumber());
		((EditText) rootView.findViewById(R.id.postal_address_input_field)).setText(person.getPostalAddress());
		((EditText) rootView.findViewById(R.id.email_address_input_field)).setText(person.getEmailAddress());
		((EditText) rootView.findViewById(R.id.contact_phone_number_input_field)).setText(person.getContactPhoneNumber());

		if (person.hasUploadedClaims()) {
			((EditText) rootView.findViewById(R.id.first_name_input_field)).setFocusable(false);
			((EditText) rootView.findViewById(R.id.date_of_establishment_input_field)).setFocusable(false);
			((EditText) rootView.findViewById(R.id.date_of_establishment_input_field)).setLongClickable(false);
			((EditText) rootView.findViewById(R.id.date_of_establishment_input_field)).setClickable(false);
			((EditText) rootView.findViewById(R.id.id_number)).setFocusable(false);
			((EditText) rootView.findViewById(R.id.postal_address_input_field)).setFocusable(false);
			((EditText) rootView.findViewById(R.id.email_address_input_field)).setFocusable(false);
			((EditText) rootView.findViewById(R.id.contact_phone_number_input_field)).setFocusable(false);
			allowSave = false;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
				if (resultCode == Activity.RESULT_OK) {
					Uri uri = FileProvider.getUriForFile(rootView.getContext(), BuildConfig.APPLICATION_ID, personPictureFile);
					FileSystemUtilities.rotateAndCompressImage(getContext(), uri);

					try {
						claimantImageView.setImageBitmap(Person.getPersonPicture(rootView.getContext(),	personActivity.getPersonId(), 128));
					} catch (Exception e) {
						claimantImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_contact_picture));
					}
				}
			break;
		}
	}

	public int savePerson() {
		Person person = new Person();
		person.setFirstName(((EditText) rootView.findViewById(R.id.first_name_input_field)).getText().toString());
		person.setLastName(((EditText) rootView.findViewById(R.id.last_name_input_field)).getText().toString());
		java.util.Date dob;
		try {
			dob = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(((EditText) rootView
					.findViewById(R.id.date_of_birth_input_field)).getText().toString());
		} catch (ParseException e) {
			e.printStackTrace();
			return 4;
		}

		person.setDateOfBirth(new Date(dob.getTime()));
		person.setPostalAddress(((EditText) rootView.findViewById(R.id.postal_address_input_field)).getText().toString());
		person.setEmailAddress(((EditText) rootView.findViewById(R.id.email_address_input_field)).getText().toString());
		person.setContactPhoneNumber(((EditText) rootView.findViewById(R.id.contact_phone_number_input_field)).getText().toString());
		person.setContactPhoneNumber(((EditText) rootView.findViewById(R.id.contact_phone_number_input_field)).getText().toString());
		String idTypeDispValue = (String) ((Spinner) rootView.findViewById(R.id.id_type_spinner)).getSelectedItem();
		person.setIdType(valueKeyMapIdTypes.get(idTypeDispValue));
		person.setIdNumber(((EditText) rootView.findViewById(R.id.id_number)).getText().toString());
		String gender = (String) ((Spinner) rootView.findViewById(R.id.gender_spinner)).getSelectedItem();
		if (gender.equals(OpenTenureApplication.getContext().getResources()
				.getString(R.string.gender_feminine)))
			person.setGender("F");
		else
			person.setGender("M");
			person.setPersonType(Person._PHYSICAL);

		if (person.getFirstName() == null || person.getFirstName().trim().equals(""))
			return 2;

		if (person.getLastName() == null || person.getLastName().trim().equals(""))
			return 3;

		if (person.getGender() == null)
			return 5;

		if (person.create() == 1) {
			personActivity.setPersonId(person.getPersonId());
			personActivity.setEntityType(person.getPersonType());
			personPictureFile = Person.getPersonPictureFile(person.getPersonId());
			return 1;
		}
		return 0;
	}

	public int saveGroup() {
		Person person = new Person();
		person.setFirstName(((EditText) rootView.findViewById(R.id.first_name_input_field)).getText().toString());
		person.setLastName("");

		java.util.Date doe = null;
		try {
			String date = ((EditText) rootView.findViewById(R.id.date_of_establishment_input_field)).getText().toString();
			if (date != null && !date.trim().equals(""))
				doe = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
			return 4;
		}
		if (doe != null)
			person.setDateOfBirth(new Date(doe.getTime()));

		person.setPostalAddress(((EditText) rootView.findViewById(R.id.postal_address_input_field)).getText().toString());
		person.setEmailAddress(((EditText) rootView.findViewById(R.id.email_address_input_field)).getText().toString());
		person.setContactPhoneNumber(((EditText) rootView.findViewById(R.id.contact_phone_number_input_field)).getText().toString());
		person.setIdNumber(((EditText) rootView.findViewById(R.id.id_number)).getText().toString());
		person.setPersonType(Person._GROUP);

		if (person.getFirstName() == null || person.getFirstName().trim().equals(""))
			return 2;

		if (person.create() == 1) {
			personActivity.setPersonId(person.getPersonId());
			personActivity.setEntityType(person.getPersonType());
			return 1;
		}
		return 0;
	}

	public int updateGroup(PersonActivity personActivity) {

		if (rootView == null)
			rootView = OpenTenureApplication.getPersonsView();
		Person person;

		if (this.personActivity == null)
			this.personActivity = personActivity;

		person = Person.getPerson(this.personActivity.getPersonId());

		person.setFirstName(((EditText) rootView.findViewById(R.id.first_name_input_field)).getText().toString());
		person.setLastName("");

		java.util.Date doe = null;
		try {
			String date = ((EditText) rootView.findViewById(R.id.date_of_establishment_input_field)).getText().toString();
			if (date != null && !date.trim().equals(""))
				doe = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
			return 4;
		}
		if (doe != null)
			person.setDateOfBirth(new Date(doe.getTime()));

		person.setPostalAddress(((EditText) rootView.findViewById(R.id.postal_address_input_field)).getText().toString());
		person.setEmailAddress(((EditText) rootView.findViewById(R.id.email_address_input_field)).getText().toString());
		person.setContactPhoneNumber(((EditText) rootView.findViewById(R.id.contact_phone_number_input_field)).getText().toString());
		person.setIdNumber(((EditText) rootView.findViewById(R.id.id_number)).getText().toString());
		person.setPersonType(Person._GROUP);

		if (person.getFirstName() == null || person.getFirstName().trim().equals(""))
			return 2;

		if (person.update() == 1) {
			this.personActivity.setPersonId(person.getPersonId());
			this.personActivity.setEntityType(person.getPersonType());
			return 1;
		}
		return 0;
	}

	public int updatePerson(String personId) {

		if (rootView == null)
			rootView = OpenTenureApplication.getPersonsView();

		Person person = Person.getPerson(personId);
		person.setFirstName(((EditText) rootView.findViewById(R.id.first_name_input_field)).getText().toString());
		person.setLastName(((EditText) rootView.findViewById(R.id.last_name_input_field)).getText().toString());
		java.util.Date dob;
		try {
			dob = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(((EditText) rootView
							.findViewById(R.id.date_of_birth_input_field)).getText().toString());
		} catch (ParseException e) {
			e.printStackTrace();
			return 4;
		}
		person.setDateOfBirth(new Date(dob.getTime()));

		person.setPostalAddress(((EditText) rootView.findViewById(R.id.postal_address_input_field)).getText().toString());
		person.setEmailAddress(((EditText) rootView.findViewById(R.id.email_address_input_field)).getText().toString());
		person.setContactPhoneNumber(((EditText) rootView.findViewById(R.id.contact_phone_number_input_field)).getText().toString());
		person.setPersonType(Person._PHYSICAL);
		String idTypeDispValue = (String) ((Spinner) rootView.findViewById(R.id.id_type_spinner)).getSelectedItem();
		person.setIdType(valueKeyMapIdTypes.get(idTypeDispValue));
		person.setIdNumber(((EditText) rootView.findViewById(R.id.id_number)).getText().toString());

		String gender = (String) ((Spinner) rootView.findViewById(R.id.gender_spinner)).getSelectedItem();
		if (gender.equals(OpenTenureApplication.getContext().getResources()
				.getString(R.string.gender_feminine)))
			person.setGender("F");
		else
			person.setGender("M");

		if (person.getFirstName() == null || person.getFirstName().trim().equals(""))
			return 2;

		if (person.getLastName() == null || person.getLastName().trim().equals(""))
			return 3;

		if (person.getGender() == null)
			return 5;

		return person.update();
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Toast toast;
		switch (item.getItemId()) {

		case R.id.action_save:

			if (personActivity.getPersonId() == null && (personActivity.getEntityType() != null && personActivity
							.getEntityType().equalsIgnoreCase(PersonActivity.TYPE_PERSON))) {

				int saved = savePerson();
				if (saved == 1) {
					toast = Toast.makeText(rootView.getContext(), R.string.message_saved, Toast.LENGTH_SHORT);
					toast.show();

					Intent resultIntent = new Intent();
					resultIntent.putExtra(PersonActivity.PERSON_ID_KEY,personActivity.getPersonId());
					getActivity().setResult(PersonsFragment.ADD_PERSON_RESULT, resultIntent);
					//getActivity().finish();
				} else if (saved == 2) {
					toast = Toast.makeText(rootView.getContext(),
							R.string.message_error_mandatory_field_first_name,
							Toast.LENGTH_SHORT);
					toast.show();
				} else if (saved == 3) {
					toast = Toast.makeText(rootView.getContext(),
							R.string.message_error_mandatory_field_last_name,
							Toast.LENGTH_SHORT);
					toast.show();
				} else if (saved == 4) {
					toast = Toast.makeText(rootView.getContext(),
							R.string.message_error_mandatory_birthdate,
							Toast.LENGTH_SHORT);
					toast.show();
				} else if (saved == 5) {
					toast = Toast.makeText(rootView.getContext(),
							R.string.message_error_mandatory_field_gender,
							Toast.LENGTH_SHORT);
					toast.show();
				} else {
					toast = Toast
							.makeText(rootView.getContext(),
									R.string.message_unable_to_save,
									Toast.LENGTH_SHORT);
					toast.show();
				}
			} else if (personActivity.getPersonId() == null
					&& (personActivity.getEntityType() != null && personActivity
							.getEntityType().equalsIgnoreCase(PersonActivity.TYPE_GROUP))) {

				int saved = saveGroup();
				if (saved == 1) {
					toast = Toast.makeText(rootView.getContext(),
							R.string.message_saved, Toast.LENGTH_SHORT);
					toast.show();

					Intent resultIntent = new Intent();
					resultIntent.putExtra(PersonActivity.PERSON_ID_KEY,personActivity.getPersonId());
					getActivity().setResult(PersonsFragment.ADD_PERSON_RESULT, resultIntent);
				} else {
					toast = Toast
							.makeText(rootView.getContext(),
									R.string.message_unable_to_save,
									Toast.LENGTH_SHORT);
					toast.show();
				}

			} else if (personActivity.getPersonId() != null
					&& (Person.getPerson(personActivity.getPersonId())
							.getPersonType().equalsIgnoreCase(PersonActivity.TYPE_GROUP))) {

				int saved = updateGroup(null);
				if (saved == 1) {
					toast = Toast.makeText(rootView.getContext(),
							R.string.message_saved, Toast.LENGTH_SHORT);
					toast.show();

					Intent resultIntent = new Intent();

					resultIntent.putExtra(PersonActivity.PERSON_ID_KEY, personActivity.getPersonId());
					// Set The Result in Intent
					((PersonActivity) getActivity()).setResult(2, resultIntent);
				} else {
					toast = Toast
							.makeText(rootView.getContext(),
									R.string.message_unable_to_save,
									Toast.LENGTH_SHORT);
					toast.show();
				}

			} else {

				int updated = updatePerson(personActivity.getPersonId());

				if (updated == 1) {
					toast = Toast.makeText(rootView.getContext(),
							R.string.message_saved, Toast.LENGTH_SHORT);
					toast.show();

					Intent resultIntent = new Intent();

					resultIntent.putExtra(PersonActivity.PERSON_ID_KEY, personActivity.getPersonId());
					// Set The Result in Intent
					((PersonActivity) getActivity()).setResult(2, resultIntent);

				} else if (updated == 2) {
					toast = Toast.makeText(rootView.getContext(),
							R.string.message_error_mandatory_field_first_name,
							Toast.LENGTH_SHORT);
					toast.show();
				} else if (updated == 3) {
					toast = Toast.makeText(rootView.getContext(),
							R.string.message_error_mandatory_field_last_name,
							Toast.LENGTH_SHORT);
					toast.show();
				} else if (updated == 4) {
					toast = Toast.makeText(rootView.getContext(),
							R.string.message_error_mandatory_birthdate,
							Toast.LENGTH_SHORT);
					toast.show();
				} else if (updated == 5) {
					toast = Toast.makeText(rootView.getContext(),
							R.string.message_error_mandatory_field_gender,
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
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public boolean checkChanges(PersonActivity personActivity) {

		String entityType = personActivity.getEntityType();

		if (entityType == null)
			entityType = Person.getPerson(personActivity.getPersonId()).getPersonType();

		if (entityType.equalsIgnoreCase(Person._GROUP))
			return checkChangesGroup(personActivity);
		else
			return checkChangesPerson(personActivity);

	}

	public boolean checkChangesGroup(PersonActivity personActivity) {

		View rootView = null;

		boolean changed = false;
		Person person = Person.getPerson(personActivity.getPersonId());
		rootView = OpenTenureApplication.getPersonsView();

		if (person != null) {

			if (person.getPersonType().equalsIgnoreCase(Person._GROUP)) {

				String name = ((EditText) rootView
						.findViewById(R.id.first_name_input_field)).getText()
						.toString();

				if (!person.getFirstName().equals(name))
					changed = true;

				else {

					String postal_address = ((EditText) rootView
							.findViewById(R.id.postal_address_input_field))
							.getText().toString();

					if ((postal_address == null || postal_address
							.equalsIgnoreCase(""))
							&& (person.getPostalAddress() != null && !person
									.getPostalAddress().equals("")))

						changed = true;

					else if ((postal_address != null && !postal_address
							.equalsIgnoreCase(""))
							&& (person.getPostalAddress() == null || person
									.getPostalAddress().equals("")))
						changed = true;

					else if ((postal_address != null && person
							.getPostalAddress() != null)
							&& !postal_address
									.equals(person.getPostalAddress()))
						changed = true;

					else {

						String email = ((EditText) rootView
								.findViewById(R.id.email_address_input_field))
								.getText().toString();

						if ((email == null || email.equals(""))
								&& (person.getEmailAddress() != null && !person
										.getEmailAddress().equals("")))

							changed = true;

						else if ((email != null && !email.equals(""))
								&& (person.getEmailAddress() == null || person
										.getEmailAddress().equals("")))
							changed = true;
						else if ((person.getEmailAddress() != null && email != null)
								&& !person.getEmailAddress().equalsIgnoreCase(
										email))
							changed = true;
						else {
							String numberId = ((EditText) rootView
									.findViewById(R.id.id_number)).getText()
									.toString();

							if ((numberId == null || numberId.equals(""))
									&& (person.getIdNumber() != null && !person
											.getIdNumber().equals("")))

								changed = true;
							else if ((numberId != null && !numberId.equals(""))
									&& (person.getIdNumber() == null || person
											.getIdNumber().equals("")))

								changed = true;

							else if ((numberId != null && person.getIdNumber() != null)
									&& !person.getIdNumber().equals(numberId))
								changed = true;

							else {

								String contact = ((EditText) rootView
										.findViewById(R.id.contact_phone_number_input_field))
										.getText().toString();

								if ((contact == null || contact.equals(""))
										&& (person.getContactPhoneNumber() != null && !person
												.getContactPhoneNumber()
												.equals("")))

									changed = true;

								else if ((contact != null && !contact
										.equals(""))
										&& (person.getContactPhoneNumber() == null || person
												.getContactPhoneNumber()
												.equals("")))
									changed = true;
								else if ((contact != null && person
										.getContactPhoneNumber() != null)
										&& !contact.equals(person
												.getContactPhoneNumber()))
									changed = true;

								else {

									String dateEstablishment = ((EditText) rootView
											.findViewById(R.id.date_of_establishment_input_field))
											.getText().toString();

									if (person.getDateOfBirth() == null
											|| person.getDateOfBirth().equals(
													"")) {

										if (dateEstablishment != null
												&& !dateEstablishment
														.equals(""))
											changed = true;
									} else {
										java.util.Date dob = null;

										if (dateEstablishment != null
												&& !dateEstablishment.trim()
														.equals("")) {

											try {
												dob = new SimpleDateFormat(
														"yyyy-MM-dd", Locale.US)
														.parse(dateEstablishment);

												Date date = new Date(
														dob.getTime());

												if (person.getDateOfBirth()
														.compareTo(date) != 0)
													changed = true;

											} catch (ParseException e) {
												e.printStackTrace();
												dob = null;

											}

										}

									}

								}

							}

						}

					}

				}

			}

		} else {

			String name = ((EditText) rootView
					.findViewById(R.id.first_name_input_field)).getText()
					.toString();
			if (name != null && !name.trim().equals(""))
				changed = true;

			String postal_address = ((EditText) rootView
					.findViewById(R.id.postal_address_input_field)).getText()
					.toString();

			if (postal_address != null && !postal_address.trim().equals(""))
				changed = true;

			String email = ((EditText) rootView
					.findViewById(R.id.email_address_input_field)).getText()
					.toString();
			if (email != null && !email.trim().equals(""))
				changed = true;

			String numberId = ((EditText) rootView.findViewById(R.id.id_number))
					.getText().toString();

			if (numberId != null && !numberId.trim().equals(""))
				changed = true;

			String contact = ((EditText) rootView
					.findViewById(R.id.contact_phone_number_input_field))
					.getText().toString();
			if (contact != null && !contact.trim().equals(""))
				changed = true;

			String dateEstablishment = ((EditText) rootView
					.findViewById(R.id.date_of_establishment_input_field))
					.getText().toString();
			if (dateEstablishment != null
					&& !dateEstablishment.trim().equals(""))
				changed = true;

		}

		if (changed) {

			AlertDialog.Builder saveChangesDialog = new AlertDialog.Builder(
					rootView.getContext());
			saveChangesDialog.setTitle(R.string.title_save_person_dialog);
			String dialogMessage = OpenTenureApplication.getContext()
					.getString(R.string.message_discard_changes);

			saveChangesDialog.setMessage(dialogMessage);

			saveChangesDialog.setPositiveButton(R.string.confirm,
					new ConfirmExit(personActivity));

			saveChangesDialog.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							return;
						}
					});
			saveChangesDialog.show();

		}

		return false;

	}


	public boolean isPersonChanged(Person person, View rootView){

		String name = ((EditText) rootView
				.findViewById(R.id.first_name_input_field)).getText()
				.toString();

		if (!person.getFirstName().equals(name))
			return true;

		String lastName = ((EditText) rootView
				.findViewById(R.id.last_name_input_field)).getText()
				.toString();

		if (!person.getLastName().equals(lastName))
			return true;

		String postal_address = ((EditText) rootView
				.findViewById(R.id.postal_address_input_field))
				.getText().toString();

		if ((postal_address == null || postal_address
				.equalsIgnoreCase(""))
				&& (person.getPostalAddress() != null && !person
				.getPostalAddress().equals("")))
			return true;

		else if ((postal_address != null && !postal_address
				.equalsIgnoreCase(""))
				&& (person.getPostalAddress() == null || person
				.getPostalAddress().equals("")))
			return true;

		else if (!postal_address.equals(person.getPostalAddress()))
			return true;

		String email = ((EditText) rootView
				.findViewById(R.id.email_address_input_field))
				.getText().toString();

		if ((email == null || email.equals(""))
				&& (person.getEmailAddress() != null && !person
				.getEmailAddress().equals("")))

			return true;

		else if ((email != null && !email.equals(""))
				&& (person.getEmailAddress() == null || person
				.getEmailAddress().equals("")))
			return true;
		else if ((email != null && person.getEmailAddress() != null)
				&& !person.getEmailAddress().equalsIgnoreCase(
				email))
			return true;

		String numberId = ((EditText) rootView
				.findViewById(R.id.id_number)).getText()
				.toString();

		if ((numberId == null || numberId.equals(""))
				&& (person.getIdNumber() != null && !person
				.getIdNumber().equals("")))

			return true;
		else if ((numberId != null && !numberId.equals(""))
				&& (person.getIdNumber() == null || person
				.getIdNumber().equals("")))

			return true;

		else if ((person.getIdNumber() != null && numberId != null)
				&& !person.getIdNumber().equals(numberId))
			return true;

		String idType = (String) ((Spinner) rootView
				.findViewById(R.id.id_type_spinner))
				.getSelectedItem();


		if ((idType != null && person.getIdType() != null)
				&& !person.getIdType().trim().equals(
				valueKeyMapIdTypes
						.get(idType).trim()))
			return true;

		String contact = ((EditText) rootView
				.findViewById(R.id.contact_phone_number_input_field))
				.getText().toString();

		if ((contact == null || contact.equals(""))
				&& (person.getContactPhoneNumber() != null && !person
				.getContactPhoneNumber()
				.equals("")))

			return true;

		else if ((contact != null && !contact
				.equals(""))
				&& (person.getContactPhoneNumber() == null || person
				.getContactPhoneNumber()
				.equals("")))
			return true;
		else if ((contact != null && person
				.getContactPhoneNumber() != null)
				&& !contact.equals(person
				.getContactPhoneNumber()))
			return true;

		String dateOfBirth = ((EditText) rootView
				.findViewById(R.id.date_of_birth_input_field))
				.getText().toString();

		if (person.getDateOfBirth() == null ^ dateOfBirth.trim().equalsIgnoreCase("")) {

			Log.d(this.getClass().getName(), "Date of birth has changed");
			return true;

		}
		if (!dateOfBirth.trim().equalsIgnoreCase("")) {

			try {
				java.util.Date dob = new SimpleDateFormat(
						"yyyy-MM-dd", Locale.US)
						.parse(dateOfBirth);

				Date date = new Date(dob.getTime());

				if (person.getDateOfBirth().compareTo(date) != 0) {
					Log.d(this.getClass().getName(), "Date of birth has changed");
					return true;
				}
			} catch (ParseException e) {
				e.printStackTrace();
				return true;
			}
		}

		return false;
	}

	public boolean hasFragmentValues(View rootView){
		String name = ((EditText) rootView
				.findViewById(R.id.first_name_input_field)).getText()
				.toString();
		if (name != null && !name.trim().equals(""))
			return true;

		String lastName = ((EditText) rootView
				.findViewById(R.id.last_name_input_field)).getText()
				.toString();
		if (lastName != null && !lastName.trim().equals(""))
			return true;

		String postal_address = ((EditText) rootView
				.findViewById(R.id.postal_address_input_field)).getText()
				.toString();

		if (postal_address != null && !postal_address.trim().equals(""))
			return true;

		String email = ((EditText) rootView
				.findViewById(R.id.email_address_input_field)).getText()
				.toString();
		if (email != null && !email.trim().equals(""))
			return true;

		String numberId = ((EditText) rootView.findViewById(R.id.id_number))
				.getText().toString();

		if (numberId != null && !numberId.trim().equals(""))
			return true;

		String contact = ((EditText) rootView
				.findViewById(R.id.contact_phone_number_input_field))
				.getText().toString();
		if (contact != null && !contact.trim().equals(""))
			return true;

		String dateOfBirth = ((EditText) rootView
				.findViewById(R.id.date_of_birth_input_field)).getText()
				.toString();
		if (dateOfBirth != null && !dateOfBirth.trim().equals(""))
			return true;

		return false;
	}

	public boolean checkChangesPerson(PersonActivity personActivity) {

		if (valueKeyMapIdTypes == null)
			valueKeyMapIdTypes = new IdType()
					.getValueKeyMap(OpenTenureApplication.getInstance()
							.getLocalization(),onlyActive);

		Person person = Person.getPerson(personActivity.getPersonId());
		View rootView = OpenTenureApplication.getPersonsView();

		if ((person != null && isPersonChanged(person, rootView)) || (person == null && hasFragmentValues(rootView))) {

			AlertDialog.Builder saveChangesDialog = new AlertDialog.Builder(
					rootView.getContext());
			saveChangesDialog.setTitle(R.string.title_save_person_dialog);
			String dialogMessage = OpenTenureApplication.getContext()
					.getString(R.string.message_discard_changes);

			saveChangesDialog.setMessage(dialogMessage);

			saveChangesDialog.setPositiveButton(R.string.confirm,
					new ConfirmExit(personActivity));

			saveChangesDialog.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							return;
						}
					});

			saveChangesDialog.show();

		}

		return false;

	}
}