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
import java.util.Locale;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.astuetz.PagerSlidingTabStrip;

public class SelectPersonActivity extends FragmentActivity implements ModeDispatcher {

	public static final int SELECT_PERSON_ACTIVITY_RESULT = 100;
	public static final String EXCLUDE_PERSON_IDS_KEY = "excludePersonIds";

	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;
	private PagerSlidingTabStrip tabs;
	private ArrayList<String> excludePersonIds;

	@Override
	public void onDestroy() {
		super.onDestroy();
		OpenTenureApplication.getInstance().getDatabase().sync();
	};
	
	@Override
	public void onPause() {
		super.onPause();
		OpenTenureApplication.getInstance().getDatabase().sync();;
	};
	
	@Override
	public void onResume() {
		super.onResume();
		OpenTenureApplication.getInstance().getDatabase().open();
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_person);

		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		mViewPager = (ViewPager) findViewById(R.id.person_pager);
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());
		
		

		mViewPager.setAdapter(mSectionsPagerAdapter);
		tabs.setIndicatorColor(getResources().getColor(R.color.ab_tab_indicator_opentenure));
		tabs.setViewPager(mViewPager);
		
		if (savedInstanceState != null
				&& savedInstanceState.getStringArrayList(EXCLUDE_PERSON_IDS_KEY) != null) {
			excludePersonIds = savedInstanceState.getStringArrayList(EXCLUDE_PERSON_IDS_KEY);
		} else {
			excludePersonIds = getIntent().getStringArrayListExtra(EXCLUDE_PERSON_IDS_KEY);
		}
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {

			switch (position) {
			case 0:
				
				PersonsFragment pf = new PersonsFragment();
				if(excludePersonIds != null){
					pf.setExcludePersonIds(excludePersonIds);
				}
				return pf;
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
				return getString(R.string.title_persons).toUpperCase(l);
			}
			return null;
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putStringArrayList(EXCLUDE_PERSON_IDS_KEY, excludePersonIds);
	};

	@Override
	public Mode getMode() {
		return ModeDispatcher.Mode.MODE_RO;
	}
}
