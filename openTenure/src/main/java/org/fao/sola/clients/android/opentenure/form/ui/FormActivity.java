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
import java.util.List;
import java.util.Locale;

import org.fao.sola.clients.android.opentenure.ModeDispatcher;
import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.form.FormPayload;
import org.fao.sola.clients.android.opentenure.form.FormTemplate;
import org.fao.sola.clients.android.opentenure.form.SectionElementPayload;
import org.fao.sola.clients.android.opentenure.form.SectionTemplate;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.Menu;

import com.astuetz.PagerSlidingTabStrip;

public class FormActivity extends FragmentActivity {

	public static final String MODE_KEY = "mode";
	public static final String FORM_PAYLOAD_KEY = "formPayload";
	public static final String FORM_TEMPLATE_KEY = "formTemplate";
	public static final int FORM_RESULT = 9876;
	private FormPayload originalForm;
	private FormPayload editedForm;
	private FormTemplate formTemplate;
	private ModeDispatcher.Mode mode;
	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	PagerSlidingTabStrip tabs;
	List<String> titles;
	SparseArray<Fragment> fragmentReferences = new SparseArray<Fragment>();

	@Override
	public void onDestroy() {
		super.onDestroy();
		OpenTenureApplication.getInstance().getDatabase().sync();
	}
	
	@Override
	public void onBackPressed() {
		
		AlertDialog.Builder exitDialog = new AlertDialog.Builder(this);
		exitDialog.setTitle(R.string.title_exit_dialog);
		exitDialog.setMessage(getResources().getString(R.string.message_exit_dialog));

		exitDialog.setPositiveButton(R.string.confirm, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				FormActivity.super.onBackPressed();
			}
		});
		exitDialog.setNegativeButton(R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		exitDialog.show();
	}

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
		outState.putString(FORM_PAYLOAD_KEY, editedForm.toJson());
		super.onSaveInstanceState(outState);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		String savedInstanceForm = null;

		if (savedInstanceState != null) {
			savedInstanceForm = savedInstanceState.getString(FORM_PAYLOAD_KEY);
		}

		String intentTemplate = getIntent().getExtras().getString(FORM_TEMPLATE_KEY);
		
		if(intentTemplate != null){
			formTemplate = FormTemplate.fromJson(intentTemplate);
		}

		String intentForm = getIntent().getExtras().getString(FORM_PAYLOAD_KEY);
		
		if(intentForm != null){
			originalForm = FormPayload.fromJson(intentForm);
		}

		if(savedInstanceForm != null) {
			editedForm = FormPayload.fromJson(savedInstanceForm);
		} else {
			editedForm = new FormPayload(originalForm);
		}
		
		mode = ModeDispatcher.Mode
				.valueOf(getIntent().getStringExtra(MODE_KEY));
		setContentView(R.layout.activity_form);

		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		mViewPager = (ViewPager) findViewById(R.id.form_pager);
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		mViewPager.setAdapter(mSectionsPagerAdapter);
		tabs.setIndicatorColor(getResources().getColor(
				R.color.ab_tab_indicator_opentenure));
		tabs.setViewPager(mViewPager);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear(); 
		getMenuInflater().inflate(R.menu.form, menu);
		return true;
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public void destroyItem(android.view.ViewGroup container, int position,
				Object object) {

			fragmentReferences.remove(position);

		}

		@Override
		public Fragment getItem(int position) {

			Fragment fragment;

			
			
			
			
			
			SectionTemplate sectionTemplate = formTemplate.getSectionTemplateList().get(position);
			if(sectionTemplate == null){
				return null;
			}
			if(sectionTemplate.getMaxOccurrences() > 1){
				fragment = new SectionFragment();
				((SectionFragment)fragment).setTemplate(sectionTemplate);
				((SectionFragment)fragment).setPayload(editedForm.getSectionPayloadList().get(position));
				((SectionFragment)fragment).setMode(mode);
			}else{
				if(editedForm.getSectionPayloadList().get(position).getSectionElementPayloadList().size()==0){
					editedForm.getSectionPayloadList().get(position).getSectionElementPayloadList().add(new SectionElementPayload(sectionTemplate));
				}
				fragment = new SectionElementFragment();
				((SectionElementFragment)fragment).setTemplate(sectionTemplate);
				((SectionElementFragment)fragment).setPayload(editedForm.getSectionPayloadList().get(position).getSectionElementPayloadList().get(0));
				((SectionElementFragment)fragment).setMode(mode);
			}
			fragmentReferences.put(position, fragment);
			return fragment;
		}

		@Override
		public int getCount() {
			titles = new ArrayList<String>();
			for(SectionTemplate section : formTemplate.getSectionTemplateList()){
				titles.add(section.getDisplayName());
			}
			return titles.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			return titles.get(position).toUpperCase(l);
		}
	}
}
