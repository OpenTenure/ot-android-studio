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

import java.util.Locale;

import org.fao.sola.clients.android.opentenure.model.Person;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;

import com.astuetz.PagerSlidingTabStrip;

public class PersonActivity extends FragmentActivity implements
		PersonDispatcher, ModeDispatcher {

	public static final String PERSON_ID_KEY = "personId";
	public static final String ENTIY_TYPE = "entityType";

	public static final String MODE_KEY = "mode";
	public static final String CREATE_PERSON_ID = "create_person";
	public static final String TYPE_GROUP = "group";
	public static final String TYPE_PERSON = "person";
	private ModeDispatcher.Mode mode = ModeDispatcher.Mode.MODE_RW;
	private String personId = null;
	private String entityType = null;
	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	PagerSlidingTabStrip tabs;

	@Override
	public void onDestroy() {
		super.onDestroy();
		OpenTenureApplication.getInstance().getDatabase().sync();
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {

			final PersonFragment fragment = (PersonFragment) mSectionsPagerAdapter
					.getItem(0);

			if (fragment != null) {
				if (fragment.checkChanges(this)) {
					return true;
				} else {

					return super.onKeyDown(keyCode, event);
				}
			} else {
				return super.onKeyDown(keyCode, event);
			}
		} else
			return super.onKeyDown(keyCode, event);
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
		outState.putString(PERSON_ID_KEY, personId);

		Intent resultIntent = new Intent();

		resultIntent.putExtra(PersonActivity.PERSON_ID_KEY, personId);
		// Set The Result in Intent
		setResult(2, resultIntent);
		super.onSaveInstanceState(outState);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		Intent intent = getIntent();

		setEntityType(intent.getStringExtra(ENTIY_TYPE));

		String aMode = intent.getStringExtra(MODE_KEY);
		if (aMode != null)
			mode = ModeDispatcher.Mode.valueOf(aMode);
		else
			mode = ModeDispatcher.Mode.MODE_RW;
		setContentView(R.layout.activity_person);

		if ((getEntityType() != null && getEntityType().equalsIgnoreCase(
				TYPE_GROUP))
				|| (getPersonId() != null && Person.getPerson(personId)
						.getPersonType().equalsIgnoreCase(Person._GROUP)))
			this.setTitle(R.string.title_activity_group);

		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		mViewPager = (ViewPager) findViewById(R.id.person_pager);
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		String savedInstancePersonId = null;

		if (savedInstanceState != null) {
			savedInstancePersonId = savedInstanceState.getString(PERSON_ID_KEY);
		}

		String intentPersonId = getIntent().getExtras()
				.getString(PERSON_ID_KEY);

		if (savedInstancePersonId != null) {
			setPersonId(savedInstancePersonId);
		} else if (intentPersonId != null
				&& !intentPersonId.equalsIgnoreCase(CREATE_PERSON_ID)) {
			setPersonId(intentPersonId);

		}

		mViewPager.setAdapter(mSectionsPagerAdapter);
		tabs.setIndicatorColor(getResources().getColor(
				R.color.ab_tab_indicator_opentenure));
		tabs.setViewPager(mViewPager);

	}

	@Override
	public void onStart() {
		super.onStart();

	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {

			switch (position) {
			case 0:
				return new PersonFragment();
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

				String personId = getPersonId();

				if (getEntityType() != null) {

					if (getEntityType().equalsIgnoreCase(TYPE_GROUP))
						return getString(R.string.title_group_details)
								.toUpperCase(l);
					else
						return getString(R.string.title_person_details)
								.toUpperCase(l);

				} else {

					if (personId != null
							&& Person.getPerson(personId).getPersonType()
									.equalsIgnoreCase(Person._GROUP))
						return getString(R.string.title_group_details)
								.toUpperCase(l);
					else
						return getString(R.string.title_person_details)
								.toUpperCase(l);
				}
			}
			return null;
		}
	}

	@Override
	public void setPersonId(String personId) {
		this.personId = personId;
		if (personId != null && !personId.equalsIgnoreCase(CREATE_PERSON_ID)) {
			Person person = Person.getPerson(personId);
			setTitle(getResources().getString(R.string.app_name) + ": "
					+ person.getFirstName() + " " + person.getLastName());
		}
	}

	@Override
	public String getPersonId() {
		return personId;
	}

	@Override
	public Mode getMode() {
		return mode;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

}
