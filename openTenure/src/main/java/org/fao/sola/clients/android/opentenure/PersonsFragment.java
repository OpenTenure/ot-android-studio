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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.fao.sola.clients.android.opentenure.model.Configuration;
import org.fao.sola.clients.android.opentenure.model.Person;
import org.fao.sola.clients.android.opentenure.tools.StringUtility;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.ListFragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

public class PersonsFragment extends ListFragment implements PersonsListAdapter.OnSloganClickedListener {

	private View rootView;
	private static final int PERSON_RESULT = 100;
	public static final int ADD_PERSON_RESULT = 101;
	private static final String FILTER_KEY = "filter";
	private List<String> excludePersonIds = new ArrayList<String>();
	private String filter = null;
	private String personType;
	private ModeDispatcher mainActivity;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			mainActivity = (ModeDispatcher) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement ModeDispatcher");
		}
	}

	public static PersonsFragment newInstance(String personType) {
		Bundle bundle = new Bundle();
		bundle.putString("personType", personType);

		PersonsFragment pf = new PersonsFragment();
		pf.setArguments(bundle);

		return pf;
	}

	private void readBundle(Bundle bundle) {
		if (bundle != null) {
			personType = bundle.getString("personType");
		}
	}

	public void setExcludePersonIds(List<String> excludePersonIds) {
		this.excludePersonIds = excludePersonIds;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.persons, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_new:

				if (!Boolean.parseBoolean(Configuration.getConfigurationByName("isInitialized").getValue())) {
					Toast toast;
					String toastMessage = String.format(OpenTenureApplication
							.getContext().getString(
									R.string.message_app_not_yet_initialized));
					toast = Toast.makeText(OpenTenureApplication.getContext(), toastMessage, Toast.LENGTH_LONG);
					toast.show();
					return true;
				}

				AlertDialog.Builder dialog = new AlertDialog.Builder(rootView.getContext());

				dialog.setTitle(R.string.new_entity);
				dialog.setMessage(R.string.message_entity_type);

				dialog.setPositiveButton(R.string.person,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Intent intent = new Intent(rootView.getContext(), PersonActivity.class);
								intent.putExtra(PersonActivity.PERSON_ID_KEY, PersonActivity.CREATE_PERSON_ID);
								intent.putExtra(PersonActivity.ENTITY_TYPE, PersonActivity.TYPE_PERSON);
								intent.putExtra(PersonActivity.MODE_KEY, mainActivity.getMode().toString());
								startActivityForResult(intent, ADD_PERSON_RESULT);
							}
						});

				dialog.setNegativeButton(R.string.group,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								Intent intent = new Intent(rootView.getContext(), PersonActivity.class);
								intent.putExtra(PersonActivity.PERSON_ID_KEY, PersonActivity.CREATE_PERSON_ID);
								intent.putExtra(PersonActivity.ENTITY_TYPE, PersonActivity.TYPE_GROUP);
								intent.putExtra(PersonActivity.MODE_KEY, mainActivity.getMode().toString());
								startActivityForResult(intent, ADD_PERSON_RESULT);
							}
						});

				dialog.show();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case ADD_PERSON_RESULT:
				if (data != null) {
					CharSequence personId = data.getCharSequenceExtra(PersonActivity.PERSON_ID_KEY);
					sendPersonBackToClaimDetails(personId);
				}
				break;
			default:
				update();
				super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.persons_list, container, false);
		readBundle(getArguments());
		setHasOptionsMenu(true);
		EditText inputSearch = (EditText) rootView.findViewById(R.id.filter_input_field);
		inputSearch.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

		inputSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				filter = arg0.toString();
				((PersonsListAdapter) getListAdapter()).getFilter().filter(filter);
			}
		});

		update();

		if(savedInstanceState != null && savedInstanceState.getString(FILTER_KEY) != null){
			filter = savedInstanceState.getString(FILTER_KEY);
			((PersonsListAdapter) getListAdapter()).getFilter().filter(filter);
		}
		return rootView;
	}

	@Override
	public void onResume() {
		update();
		if(filter != null){
			((PersonsListAdapter) getListAdapter()).getFilter().filter(filter);
		}
		super.onResume();
	};

	private void sendPersonBackToClaimDetails(CharSequence id) {
		Intent resultIntent = new Intent();
		resultIntent.putExtra(PersonActivity.PERSON_ID_KEY,	id);
		getActivity().setResult(SelectPersonActivity.SELECT_PERSON_ACTIVITY_RESULT,	resultIntent);
		getActivity().finish();
	}

	protected void update() {
		List<Person> persons = Person.getAllPersons();
		Collections.sort(persons, new Comparator<Person>() {
			public int compare(Person p1, Person p2) {
				if (p1.getLastName()==null) { p1.setLastName(""); }
				if (p2.getLastName()==null) { p2.setLastName(""); }
				int result = p1.getLastName().compareTo(p2.getLastName());
				return result!=0 ? result : p1.getFirstName().compareTo(p2.getFirstName());
			}
		});
		List<String> idsWithClaimOrShare = Person.getIdsWithSharesOrClaims();
		List<PersonListTO> personListTOs = new ArrayList<PersonListTO>();

		for (Person person : persons) {
			if (personType.equals(person.getPersonType())) {
				if (!excludePersonIds.contains(person.getPersonId())) {
					PersonListTO pto = new PersonListTO();
					pto.setId(person.getPersonId());
					pto.setPersonType(person.getPersonType());
					String slogan = "<b>" + person.getFirstName() + " " + person.getLastName() + "</b>";

					// Add ID
					if(!StringUtility.empty(person.getIdNumber()).equals("")){
						slogan += "<br>" + getResources().getString(R.string.id_number) + ": " + person.getIdNumber();
					}
					// Add DOB
					if(person.getDateOfBirth() != null){
						String strDateOfBirth = "";
						if(person.getPersonType().equalsIgnoreCase(PersonActivity.TYPE_PERSON)){
							strDateOfBirth = getResources().getString(R.string.date_of_birth_simple);
						} else {
							strDateOfBirth = getResources().getString(R.string.date_of_establishment_label);
						}
						slogan += "<br>" + strDateOfBirth + ": " + person.getDateOfBirth();
					}

					pto.setSlogan(slogan);

					personListTOs.add(pto);
					if (idsWithClaimOrShare.contains(person.getPersonId())) {
						pto.setHasClaimOrShare(true);
					} else {
						pto.setHasClaimOrShare(false);
					}
				}
			}
		}
		PersonsListAdapter adapter = new PersonsListAdapter(rootView.getContext(), personListTOs, mainActivity.getMode());
		adapter.setOnSloganClickedListener(this);

		setListAdapter(adapter);
		adapter.notifyDataSetChanged();

	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(FILTER_KEY, filter);
		super.onSaveInstanceState(outState);
	}

	public void refresh(){
		update();
	}

	@Override
	public void onSloganClicked(String personId) {
		sendPersonBackToClaimDetails(personId);
	}
}
