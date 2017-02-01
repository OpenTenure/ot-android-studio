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

import java.util.Locale;

import org.fao.sola.clients.android.opentenure.ClaimActivity;
import org.fao.sola.clients.android.opentenure.ClaimDispatcher;
import org.fao.sola.clients.android.opentenure.ClaimListener;
import org.fao.sola.clients.android.opentenure.FormDispatcher;
import org.fao.sola.clients.android.opentenure.ModeDispatcher;
import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.PersonFragment;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.form.FormPayload;
import org.fao.sola.clients.android.opentenure.form.FormTemplate;
import org.fao.sola.clients.android.opentenure.form.SectionElementPayload;
import org.fao.sola.clients.android.opentenure.form.SectionTemplate;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.SurveyFormTemplate;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;

import com.astuetz.PagerSlidingTabStrip;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;

public class SectionElementActivity extends FragmentActivity implements
		ClaimDispatcher, FormDispatcher {

	public static final String CREATE_CLAIM_ID = "create";
	public static final String SECTION_ELEMENT_POSITION_KEY = "sectionElementPosition";
	public static final int SECTION_ELEMENT_POSITION_NEW = -1;
	public static final int SECTION_ELEMENT_POSITION_DISCARD = -2;
	public static final String SECTION_TEMPLATE_KEY = "sectionTemplate";
	public static final String SECTION_ELEMENT_PAYLOAD_KEY = "sectionElementPayload";
	public static final String MODE_KEY = "mode";
	public static final int SECTION_ELEMENT_ACTIVITY_REQUEST_CODE = 4321;
	public static final String HIDE_SAVE_BUTTON_KEY = "hideButtonKey";
	private int sectionElementPosition;
	private ModeDispatcher.Mode mode;
	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;
	private PagerSlidingTabStrip tabs;
	private SectionElementPayload originalElement;
	private SectionElementPayload editedElement;
	private SectionTemplate elementTemplate;
	private SectionElementFragment elementFragment;
	private String claimId = null;
	private FormPayload originalFormPayload;
	private FormPayload editedFormPayload;
	private FormTemplate formTemplate;
	private static final String SECTION_ELEMENT_SAVED = "sectionSaved";

	@Override
	public void onDestroy() {
		super.onDestroy();
		OpenTenureApplication.getInstance().getDatabase().sync();
	};

	@Override
	public void onPause() {
		super.onPause();
		OpenTenureApplication.getInstance().getDatabase().sync();
	};

	@Override
	public void onResume() {
		super.onResume();
		OpenTenureApplication.getInstance().getDatabase().open();
	};

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(SECTION_ELEMENT_POSITION_KEY, sectionElementPosition);
		outState.putString(SECTION_ELEMENT_PAYLOAD_KEY, editedElement.toJson());
		outState.putString(SECTION_TEMPLATE_KEY, elementTemplate.toJson());
		outState.putString(MODE_KEY, mode.toString());
		super.onSaveInstanceState(outState);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		int savedPosition = SECTION_ELEMENT_POSITION_NEW;
		String savedElement = null;
		String savedElementTemplate = null;
		String savedMode = null;

		if (savedInstanceState != null) {
			savedPosition = savedInstanceState.getInt(
					SECTION_ELEMENT_POSITION_KEY, SECTION_ELEMENT_POSITION_NEW);
			savedElement = savedInstanceState
					.getString(SECTION_ELEMENT_PAYLOAD_KEY);
			savedElementTemplate = savedInstanceState
					.getString(SECTION_TEMPLATE_KEY);
			savedMode = savedInstanceState.getString(MODE_KEY);
		}

		int intentPosition = getIntent().getExtras().getInt(
				SECTION_ELEMENT_POSITION_KEY, SECTION_ELEMENT_POSITION_NEW);

		if (savedPosition != SECTION_ELEMENT_POSITION_NEW) {
			sectionElementPosition = savedPosition;
		} else {
			sectionElementPosition = intentPosition;
		}

		String intentElement = getIntent().getExtras().getString(
				SECTION_ELEMENT_PAYLOAD_KEY);
		if (intentElement != null) {
			originalElement = SectionElementPayload.fromJson(intentElement);
		}

		if (savedElement != null) {
			editedElement = SectionElementPayload.fromJson(savedElement);
		} else {
			editedElement = new SectionElementPayload(originalElement);
		}
		String intentTemplate = getIntent().getExtras().getString(
				SECTION_TEMPLATE_KEY);
		if (savedElementTemplate != null) {
			elementTemplate = SectionTemplate.fromJson(savedElementTemplate);
		} else {
			elementTemplate = SectionTemplate.fromJson(intentTemplate);
		}

		if (savedMode != null) {
			mode = ModeDispatcher.Mode.valueOf(savedMode);
		} else {
			mode = ModeDispatcher.Mode.valueOf(getIntent().getStringExtra(
					MODE_KEY));
		}
		elementFragment = new SectionElementFragment();
		elementFragment.setTemplate(elementTemplate);
		elementFragment.setPayload(editedElement);
		elementFragment.setMode(mode);

		setContentView(R.layout.activity_field_group);

		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		mViewPager = (ViewPager) findViewById(R.id.field_group_pager);
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		mViewPager.setAdapter(mSectionsPagerAdapter);
		tabs.setIndicatorColor(getResources().getColor(
				R.color.ab_tab_indicator_opentenure));
		tabs.setViewPager(mViewPager);

		// Setup the form before creating the section adapter
		claimId = OpenTenureApplication.getInstance().getClaimId();

		setupDynamicSections();

	}

	@Override
	public void onBackPressed() {
		
		
		if (mode == ModeDispatcher.Mode.MODE_RO
				|| originalElement.toJson().equalsIgnoreCase(
						editedElement.toJson())) {
			Intent resultIntent = new Intent();
			resultIntent.putExtra(SECTION_ELEMENT_POSITION_KEY,
					SECTION_ELEMENT_POSITION_DISCARD);
			resultIntent.putExtra(SECTION_ELEMENT_PAYLOAD_KEY, elementFragment
					.getEditedElement().toJson());
			setResult(RESULT_CANCELED, resultIntent);
			
			finish();
		} else {

			AlertDialog.Builder exitDialog = new AlertDialog.Builder(this);
			exitDialog.setTitle(R.string.title_save_claim_dialog);
			exitDialog.setMessage(getResources().getString(
					R.string.message_save_changes));

			exitDialog.setPositiveButton(R.string.confirm,
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent resultIntent = new Intent();
							resultIntent.putExtra(SECTION_ELEMENT_POSITION_KEY,
									sectionElementPosition);
							resultIntent.putExtra(SECTION_ELEMENT_PAYLOAD_KEY,
									editedElement.toJson());
							setResult(RESULT_OK, resultIntent);
							finish();
						}
					});
			exitDialog.setNegativeButton(R.string.cancel,
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Intent resultIntent = new Intent();
							resultIntent.putExtra(SECTION_ELEMENT_POSITION_KEY,
									SECTION_ELEMENT_POSITION_DISCARD);
							resultIntent.putExtra(SECTION_ELEMENT_PAYLOAD_KEY,
									originalElement.toJson());
							setResult(RESULT_CANCELED, resultIntent);
							finish();
						}
					});
			exitDialog.show();

		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {		
			
				return super.onKeyDown(keyCode, event);
		} else
			return super.onKeyDown(keyCode, event);
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {

			switch (position) {
			case 0:
				return elementFragment;
			}
			return null;
		}

		@Override
		public int getCount() {
			return 1;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return elementTemplate.getElementDisplayName().toUpperCase(l);
			}
			return null;
		}
	}

	public void setupDynamicSections() {
		if (claimId != null && !claimId.equalsIgnoreCase(CREATE_CLAIM_ID)) {
			// setting up for an existing claim
			Claim claim = Claim.getClaim(claimId);
			originalFormPayload = claim.getDynamicForm();
			if (originalFormPayload != null) {
				// There's a payload already attached to this claim
				editedFormPayload = new FormPayload(originalFormPayload);
				// Try to retrieve its template
				formTemplate = SurveyFormTemplate
						.getFormTemplateByName(originalFormPayload
								.getFormTemplateName());
				if (formTemplate == null) {
					// We don't have the original template for this payload
					// let's try to rebuild it from the payload itself
					formTemplate = new FormTemplate(originalFormPayload);

				}

			} else {
				// A payload has not been created for this claim
				// so we refer to the default template for the dynamic part
				formTemplate = SurveyFormTemplate
						.getDefaultSurveyFormTemplate();
				originalFormPayload = new FormPayload(formTemplate);
				originalFormPayload.setClaimId(claimId);
				editedFormPayload = new FormPayload(originalFormPayload);
			}
		} else {
			// It's a newly created claim
			// so we refer to the default template for the dynamic part
			formTemplate = SurveyFormTemplate.getDefaultSurveyFormTemplate();
			originalFormPayload = new FormPayload(formTemplate);
			editedFormPayload = new FormPayload(originalFormPayload);
		}
	}

	@Override
	public FormPayload getEditedFormPayload() {
		return editedFormPayload;
	}

	@Override
	public FormPayload getOriginalFormPayload() {
		// TODO Auto-generated method stub
		return originalFormPayload;
	}

	@Override
	public FormTemplate getFormTemplate() {
		// TODO Auto-generated method stub
		return formTemplate;
	}

	@Override
	public void setClaimId(String claimId) {
		this.claimId = claimId;
		if (claimId != null && !claimId.equalsIgnoreCase(CREATE_CLAIM_ID)) {
			Claim claim = Claim.getClaim(claimId);
			setTitle(getResources().getString(R.string.app_name) + ": "
					+ claim.getName());
		}

	}

	@Override
	public String getClaimId() {
		// TODO Auto-generated method stub
		return claimId;
	}

	@Override
	public void resetOriginalFormPayload() {
		// TODO Auto-generated method stub
		originalFormPayload = editedFormPayload;
	}


}
