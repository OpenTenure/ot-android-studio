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
import java.util.Locale;

import org.fao.sola.clients.android.opentenure.model.ShareProperty;

import com.astuetz.PagerSlidingTabStrip;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

public class ShareDetailsActivityTRY extends FragmentActivity {

	private String claimId;
	ShareProperty share;
	List<String> ownerList = new ArrayList<String>();
	View rootView;
	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	PagerSlidingTabStrip tabs;

	/**
	 * Identifier for the example fragment.
	 */
	public static final int FRAGMENT_SHAREDETAILS = 0;

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
		
//		Locale locale = new Locale("km-KM");
//		Locale.setDefault(locale);
//		android.content.res.Configuration config = new android.content.res.Configuration();
//		config.locale = locale;
//		getBaseContext().getResources().updateConfiguration(config,
//		      getBaseContext().getResources().getDisplayMetrics());
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		setContentView(R.layout.share_details);
		
//		Locale locale = new Locale("km-KM");
//		Locale.setDefault(locale);
//		android.content.res.Configuration config = new android.content.res.Configuration();
//		config.locale = locale;
//		getBaseContext().getResources().updateConfiguration(config,
//		      getBaseContext().getResources().getDisplayMetrics());

		claimId = intent.getStringExtra("claimId");

		this.setTitle(R.string.title_claim_owners);

		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);

		mViewPager = (ViewPager) findViewById(R.id.claim_pager);

		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		mViewPager.setAdapter(mSectionsPagerAdapter);
		tabs.setIndicatorColor(getResources().getColor(
				R.color.ab_tab_indicator_opentenure));
		tabs.setViewPager(mViewPager);

	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public android.support.v4.app.Fragment getItem(int position) {

			switch (position) {
			case 0:
				return new ShareDetailsFragment();
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

				return "SHARE DETAILS";
			default:
				return null;

			}
		}
	}
}
