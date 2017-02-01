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
package org.fao.sola.clients.android.opentenure.form.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.fao.sola.clients.android.opentenure.ClaimDispatcher;
import org.fao.sola.clients.android.opentenure.DisplayNameLocalizer;
import org.fao.sola.clients.android.opentenure.FormDispatcher;
import org.fao.sola.clients.android.opentenure.ModeDispatcher.Mode;
import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.form.FieldConstraint;
import org.fao.sola.clients.android.opentenure.form.FieldConstraintOption;
import org.fao.sola.clients.android.opentenure.form.FieldPayload;
import org.fao.sola.clients.android.opentenure.form.FieldTemplate;
import org.fao.sola.clients.android.opentenure.form.FormPayload;
import org.fao.sola.clients.android.opentenure.form.FormTemplate;
import org.fao.sola.clients.android.opentenure.form.SectionElementPayload;
import org.fao.sola.clients.android.opentenure.form.SectionPayload;
import org.fao.sola.clients.android.opentenure.form.SectionTemplate;
import org.fao.sola.clients.android.opentenure.form.constraint.OptionConstraint;
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
import android.widget.Toast;

public class SectionFragment extends ListFragment {

	private static final String SECTION_PAYLOAD_KEY = "sectionPayload";
	private static final String SECTION_TEMPLATE_KEY = "sectionTemplate";
	
	private View rootView;
	private SectionPayload sectionPayload;
	private SectionTemplate sectionTemplate;
	private Mode mode;
	private ClaimDispatcher claimActivity;
	private FormDispatcher formDispatcher;

	public void setPayload(SectionPayload section){
		this.sectionPayload = section;
	}

	public void setTemplate(SectionTemplate sectionTemplate){
		this.sectionTemplate = sectionTemplate;
	}

	public void setMode(Mode mode){
		this.mode = mode;
	}

	public SectionFragment(){
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
					+ " must implement ClaimDispatcher " + activity.getLocalClassName());
		}
		try {
			formDispatcher = (FormDispatcher) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement FormDispatcher");
		}

	}

	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.multiple_field_group, menu);
		if(mode == Mode.MODE_RO){
			MenuItem item = menu.findItem(R.id.action_new);
			item.setVisible(false);
		}
		Claim claim = Claim.getClaim(claimActivity.getClaimId());
		if (claim == null || !claim.isModifiable()) {
			menu.removeItem(R.id.action_save);
		}
		
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_new:
			Intent intent = new Intent(rootView.getContext(),
					SectionElementActivity.class);
			intent.putExtra(SectionElementActivity.SECTION_ELEMENT_POSITION_KEY, SectionElementActivity.SECTION_ELEMENT_POSITION_NEW);
			intent.putExtra(SectionElementActivity.SECTION_ELEMENT_PAYLOAD_KEY, new SectionElementPayload(sectionTemplate).toJson());
			intent.putExtra(SectionElementActivity.SECTION_TEMPLATE_KEY, sectionTemplate.toJson());
			intent.putExtra(SectionElementActivity.HIDE_SAVE_BUTTON_KEY, true);
			intent.putExtra(SectionElementActivity.MODE_KEY, mode.toString());
			startActivityForResult(intent, SectionElementActivity.SECTION_ELEMENT_ACTIVITY_REQUEST_CODE);
			return true;
			
		case R.id.action_save:

			Toast toast;
			Claim claim = Claim.getClaim(claimActivity.getClaimId());
			int updated = updateClaim();

			if (updated == 1) {
				toast = Toast.makeText(rootView.getContext(),
						R.string.message_saved, Toast.LENGTH_SHORT);
				
				toast.show();

			} else if (updated == 2) {
				toast = Toast.makeText(rootView.getContext(),
						R.string.message_error_startdate, Toast.LENGTH_SHORT);
				toast.show();
			} else {
				toast = Toast.makeText(rootView.getContext(),
						R.string.message_unable_to_save, Toast.LENGTH_SHORT);
				toast.show();
			}
			
			return true;
		
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (data != null) { // No selection has been done

			if(resultCode == Activity.RESULT_OK
					&& requestCode == SectionElementActivity.SECTION_ELEMENT_ACTIVITY_REQUEST_CODE){
				
				String fieldGroup = data
				.getStringExtra(SectionElementActivity.SECTION_ELEMENT_PAYLOAD_KEY);
				int position = data
				.getIntExtra(SectionElementActivity.SECTION_ELEMENT_POSITION_KEY, SectionElementActivity.SECTION_ELEMENT_POSITION_NEW);

				if(position == SectionElementActivity.SECTION_ELEMENT_POSITION_NEW){
					// A new element was created and confirmed
					SectionElementPayload newSectionElement = SectionElementPayload.fromJson(fieldGroup);
					newSectionElement.setSectionPayloadId(sectionPayload.getId());
					sectionPayload.getSectionElementPayloadList().add(newSectionElement);
				}else{
					// Changes to an existing element have been made and confirmed
					SectionElementPayload newSectionElement = SectionElementPayload.fromJson(fieldGroup);
					sectionPayload.getSectionElementPayloadList().set(position, newSectionElement);
				}
				update();
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.multiple_field_group_list, container,
				false);
		setHasOptionsMenu(true);
		

		if(savedInstanceState != null && savedInstanceState.getString(SECTION_PAYLOAD_KEY) != null){
			sectionPayload = SectionPayload.fromJson(savedInstanceState.getString(SECTION_PAYLOAD_KEY));
		}
		if(savedInstanceState != null && savedInstanceState.getString(SECTION_TEMPLATE_KEY) != null){
			sectionTemplate = SectionTemplate.fromJson(savedInstanceState.getString(SECTION_TEMPLATE_KEY));
		}
		if(savedInstanceState != null && savedInstanceState.getString(SectionElementActivity.MODE_KEY) != null){
			mode = Mode.valueOf(savedInstanceState.getString(SectionElementActivity.MODE_KEY));
		}
		
		update();
		InputMethodManager imm = (InputMethodManager) rootView.getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInputFromWindow(rootView.getWindowToken(), 0, InputMethodManager.HIDE_IMPLICIT_ONLY);
		return rootView;
	}
	
	private void update() {

			List<SectionElementListTO> ownersListTOs = new ArrayList<SectionElementListTO>();

			List<SectionElementPayload> sectionElementPayloadList = sectionPayload.getSectionElementPayloadList();

			if(sectionElementPayloadList != null){

				for(SectionElementPayload sectionElement : sectionElementPayloadList){
					
					SectionElementListTO fglto = new SectionElementListTO();
					fglto.setName(ownersListTOs.size() + "");
					StringBuffer sb = new StringBuffer();
					List<FieldPayload> fieldPayloadList = sectionElement.getFieldPayloadList();

					if(fieldPayloadList != null){
					
						for(FieldPayload field:fieldPayloadList){
							if(sb.length() != 0){
								sb.append(",");
							}
							List<FieldConstraintOption> options = null;
							List<FieldTemplate> fieldTemplateList = sectionTemplate.getFieldTemplateList();
							if(fieldTemplateList != null){
								Iterator<FieldTemplate> iterator = fieldTemplateList.iterator();
								if(iterator.hasNext()){
									FieldTemplate template = iterator.next();
									List<FieldConstraint> fieldConstraintList = template.getFieldConstraintList();
									if(fieldConstraintList != null){
										for(FieldConstraint constraint:fieldConstraintList){
											if(constraint instanceof OptionConstraint){
												options = ((OptionConstraint)constraint).getFieldConstraintOptionList(); 
											}
										}
									}
								}
							}
							
							String fieldPayload = null;

							if(field.getStringPayload() != null)
								fieldPayload = field.getStringPayload();
							if(field.getBigDecimalPayload() != null)
								fieldPayload = field.getBigDecimalPayload().toString();
							if(field.getBooleanPayload() != null)
								fieldPayload = field.getBooleanPayload().toString();
							
							if(options != null){
								for(FieldConstraintOption option:options){
									if(fieldPayload.equalsIgnoreCase(option.getName())){
										fieldPayload = option.getDisplayName();
									}
								}
							}

							sb.append(fieldPayload);
						}
					}
					fglto.setSlogan(sb.toString());
					fglto.setJson(sectionElement.toJson());
					ownersListTOs.add(fglto);
				}
			}
			ArrayAdapter<SectionElementListTO> adapter = null;

			adapter = new SectionElementListAdapter(this, rootView.getContext(), ownersListTOs, sectionPayload, sectionTemplate, mode);

			setListAdapter(adapter);
			adapter.notifyDataSetChanged();

	}
	
	public int updateClaim() {

		int result = 0;
		// Claim claim = Claim.getClaim(claimActivity.getClaimId());
		Claim claim = Claim.getClaim(claimActivity.getClaimId());		
		// Still allow saving the claim if the dynamic part contains errors
		
		isFormValid();		
		claim.setDynamicForm(formDispatcher.getEditedFormPayload());

			
		
		result = claim.update();
		
		if(result == 1)
			formDispatcher.resetOriginalFormPayload();

		return result;
	}
	
	private boolean isFormValid() {
		FormPayload formPayload = formDispatcher.getEditedFormPayload();
		FormTemplate formTemplate = formDispatcher.getFormTemplate();
		FieldConstraint constraint = null;
		DisplayNameLocalizer dnl = new DisplayNameLocalizer(
				OpenTenureApplication.getInstance().getLocalization());
		if ((constraint = formTemplate.getFailedConstraint(formPayload,dnl)) != null) {
			Toast.makeText(rootView.getContext(), constraint.displayErrorMsg(),
					Toast.LENGTH_SHORT).show();
			return false;
		} else {
			return true;
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(SECTION_PAYLOAD_KEY, sectionPayload.toJson());
		outState.putString(SECTION_TEMPLATE_KEY, sectionTemplate.toJson());
		outState.putString(SectionElementActivity.MODE_KEY, mode.toString());
		
		super.onSaveInstanceState(outState);
	}
}
