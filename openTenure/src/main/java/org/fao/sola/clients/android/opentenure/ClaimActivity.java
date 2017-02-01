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

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.fao.sola.clients.android.opentenure.form.FormPayload;
import org.fao.sola.clients.android.opentenure.form.FormTemplate;
import org.fao.sola.clients.android.opentenure.form.SectionElementPayload;
import org.fao.sola.clients.android.opentenure.form.SectionTemplate;
import org.fao.sola.clients.android.opentenure.form.ui.SectionElementFragment;
import org.fao.sola.clients.android.opentenure.form.ui.SectionFragment;
import org.fao.sola.clients.android.opentenure.maps.ClaimMapFragment;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.Configuration;
import org.fao.sola.clients.android.opentenure.model.SurveyFormTemplate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.astuetz.PagerSlidingTabStrip;
import com.github.amlcurran.showcaseview.ApiUtils;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

public class ClaimActivity extends FragmentActivity implements ClaimDispatcher,
		ModeDispatcher, ClaimListener, OnShowcaseEventListener,
		View.OnClickListener, FormDispatcher {

	public static final String CLAIM_ID_KEY = "claimId";
	public static final String MODE_KEY = "mode";
	public static final String CREATE_CLAIM_ID = "create";
	private static final int NUMBER_OF_STATIC_SECTIONS = 6;
	private ModeDispatcher.Mode mode;
	private String claimId = null;
	private FormPayload originalFormPayload;
	private FormPayload editedFormPayload;
	private FormTemplate formTemplate;

	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	PagerSlidingTabStrip tabs;
	SparseArray<Fragment> fragmentReferences = new SparseArray<Fragment>();

	// SHOWCASE Variables
	ShowcaseView sv;
	private int counter = 0;
	private final ApiUtils apiUtils = new ApiUtils();
	public static final String FIRST_RUN_CLAIM_ACTIVITY = "__FIRST_RUN_CLAIM_ACTIVITY__";

	// END SHOW CASE

	@Override
	public void onDestroy() {
		super.onDestroy();
		OpenTenureApplication.getInstance().getDatabase().sync();
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK) {

			final ClaimDetailsFragment fragment = (ClaimDetailsFragment) fragmentReferences
					.get(0);

			if (fragment != null) {
				if (fragment.checkChanges()) {
					return true;
				} else {
					return super.onKeyDown(keyCode, event);
				}
			} else
				return super.onKeyDown(keyCode, event);
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
		OpenTenureApplication.getInstance().getDatabase().open();
		OpenTenure.setLocale(this);
		super.onResume();

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(CLAIM_ID_KEY, claimId);
		outState.putString(MODE_KEY, mode.toString());
		super.onSaveInstanceState(outState);

	}

	private String getFirstRun() {
		String result = "False";
		Configuration firstRun = Configuration
				.getConfigurationByName(FIRST_RUN_CLAIM_ACTIVITY);

		if (firstRun != null) {
			result = firstRun.getValue();
			firstRun.setValue("False");
			firstRun.update();
		} else {
			firstRun = new Configuration();
			firstRun.setName(FIRST_RUN_CLAIM_ACTIVITY);
			firstRun.setValue("False");
			firstRun.create();
			result = "True";
		}
		return result;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		System.out.println("onCreate()");
		OpenTenure.setLocale(this);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_claim);

		String savedInstanceClaimId = null;
		String savedInstanceMode = null;

		// Setup the form before creating the section adapter

		if (savedInstanceState != null) {

			savedInstanceClaimId = savedInstanceState.getString(CLAIM_ID_KEY);
			savedInstanceMode = savedInstanceState.getString(MODE_KEY);
		}
		String localClaimId = null;

		if (savedInstanceClaimId == null) {
			localClaimId = getIntent().getExtras().getString(CLAIM_ID_KEY);
		} else {
			localClaimId = savedInstanceClaimId;
		}

		if (savedInstanceMode == null) {
			mode = ModeDispatcher.Mode.valueOf(getIntent().getStringExtra(
					MODE_KEY));
		} else {
			mode = ModeDispatcher.Mode.valueOf(savedInstanceMode);
		}

		if (localClaimId != null
				&& !localClaimId.equalsIgnoreCase(CREATE_CLAIM_ID)) {
			setClaimId(localClaimId);
		}

		// Setup the form before creating the section adapter

		setupDynamicSections();

		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		mViewPager = (ViewPager) findViewById(R.id.claim_pager);
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		mViewPager.setAdapter(mSectionsPagerAdapter);
		tabs.setIndicatorColor(getResources().getColor(
				R.color.ab_tab_indicator_opentenure));
		tabs.setViewPager(mViewPager);

		// ShowCase Main
		if (getFirstRun().contentEquals("True")) {
			sv = new ShowcaseView.Builder(this, true)
					.setTarget(
							new ViewTarget(tabs.getTabsContainer()
									.getChildAt(0)))
					.setContentTitle(getString(R.string.showcase_claim_title))
					.setContentText(getString(R.string.showcase_claim_message))
					.setStyle(R.style.CustomShowcaseTheme)
					.setOnClickListener(this).build();
			sv.setButtonText(getString(R.string.next));
			sv.setSkipButtonText(getString(R.string.skip));
			setAlpha(0.2f, tabs.getTabsContainer().getChildAt(0), tabs
					.getTabsContainer().getChildAt(1), tabs.getTabsContainer()
					.getChildAt(2), tabs.getTabsContainer().getChildAt(3), tabs
					.getTabsContainer().getChildAt(4), tabs.getTabsContainer()
					.getChildAt(5), mViewPager);
		}

	}

	private void setAlpha(float alpha, View... views) {
		if (apiUtils.isCompatWithHoneycomb()) {
			for (View view : views) {
				if (view != null)
					view.setAlpha(alpha);
			}
		}
	}

	@Override
	public void onClick(View v) {

		if (v.toString().indexOf("skip") > 0) {
			counter = 0;
			sv.hide();
			setAlpha(1.0f, tabs.getTabsContainer().getChildAt(0), tabs
					.getTabsContainer().getChildAt(1), tabs.getTabsContainer()
					.getChildAt(2), tabs.getTabsContainer().getChildAt(3), tabs
					.getTabsContainer().getChildAt(4), tabs.getTabsContainer()
					.getChildAt(5), tabs.getTabsContainer().getChildAt(6),
					tabs, mViewPager);
			mViewPager.setCurrentItem(0);
			counter = 0;
			return;
		}

		switch (counter) {
		// case 0:
		// sv.setShowcase(
		// new ViewTarget(tabs.getTabsContainer().getChildAt(0)), true);
		// sv.setContentTitle(getString(R.string.title_claim).toUpperCase());
		// sv.setContentText(getString(R.string.showcase_claim_message));
		// mViewPager.setCurrentItem(0);
		// setAlpha(1.0f, tabs.getTabsContainer().getChildAt(0));
		// break;

		// case 0:
		// sv.setScrollContainer(true);
		// sv.setShowcase(new ViewTarget(findViewById(R.id.action_export)),
		// true);
		// sv.setContentTitle("  ");
		// sv.setContentText(getString(R.string.showcase_actionClaimDetails_message));
		// break;
		case 0:
			sv.setShowcase(
					new ViewTarget(tabs.getTabsContainer().getChildAt(1)), true);
			sv.setContentTitle(getString(R.string.owners).toUpperCase(
					Locale.getDefault()));
			sv.setContentText(getString(R.string.showcase_claim_shares_message));
			setAlpha(1.0f, tabs.getTabsContainer().getChildAt(1));
			mViewPager.setCurrentItem(1);
			break;
		case 1:
			sv.setShowcase(
					new ViewTarget(tabs.getTabsContainer().getChildAt(2)), true);
			sv.setContentTitle(getString(R.string.title_claim_documents)
					.toUpperCase(Locale.getDefault()));
			sv.setContentText(getString(R.string.showcase_claim_document_message));
			setAlpha(1.0f, tabs.getTabsContainer().getChildAt(2));
			mViewPager.setCurrentItem(2);
			break;
		case 2:
			sv.setShowcase(
					new ViewTarget(tabs.getTabsContainer().getChildAt(3)), true);
			sv.setContentTitle(getString(R.string.title_claim_adjacencies)
					.toUpperCase(Locale.getDefault()));
			sv.setContentText(getString(R.string.showcase_claim_adjacencies_message));
			setAlpha(1.0f, tabs.getTabsContainer().getChildAt(3));
			mViewPager.setCurrentItem(3);
			break;
		case 3:
			sv.setShowcase(
					new ViewTarget(tabs.getTabsContainer().getChildAt(4)), true);
			sv.setContentTitle(getString(R.string.title_claim_map).toUpperCase(
					Locale.getDefault()));
			sv.setContentText(getString(R.string.showcase_claim_map_message));
			setAlpha(1.0f, tabs.getTabsContainer().getChildAt(4));
			mViewPager.setCurrentItem(4);
			break;
		case 4:
			sv.setShowcase(new ViewTarget(mViewPager), true);
			sv.setContentTitle("  ");
			sv.setContentText(getString(R.string.showcase_claim_mapdraw_message));
			mViewPager.setCurrentItem(4);
			break;

		case 5:
			sv.setShowcase(new ViewTarget(
					findViewById(R.id.action_center_and_follow)), true);
			sv.setContentTitle("  ");
			sv.setContentText(getString(R.string.showcase_actionClaimMap_message));
			break;
		case 6:
			sv.setShowcase(
					new ViewTarget(tabs.getTabsContainer().getChildAt(5)), true);
			sv.setContentTitle(getString(R.string.title_claim_challenges)
					.toUpperCase(Locale.getDefault()));
			sv.setContentText(getString(R.string.showcase_claim_challenges_message));
			setAlpha(1.0f, tabs.getTabsContainer().getChildAt(5));
			mViewPager.setCurrentItem(5);
			break;

		case 7:
			sv.setShowcase(
					new ViewTarget(tabs.getTabsContainer().getChildAt(0)), true);
			sv.setContentTitle(("  "));
			sv.setContentText(getString(R.string.showcase_end_message));
			setAlpha(0.6f, tabs.getTabsContainer().getChildAt(0), tabs
					.getTabsContainer().getChildAt(1), tabs.getTabsContainer()
					.getChildAt(2), tabs.getTabsContainer().getChildAt(3), tabs
					.getTabsContainer().getChildAt(4), tabs.getTabsContainer()
					.getChildAt(5), tabs);
			sv.setButtonText(getString(R.string.close));
			mViewPager.setCurrentItem(0);
			break;
		case 8:
			sv.hide();
			setAlpha(1.0f, tabs, mViewPager);
			counter = 0;
			break;
		}

		counter++;
	}

	@Override
	public void onShowcaseViewHide(ShowcaseView showcaseView) {
		// if (apiUtils.isCompatWithHoneycomb()) {
		// listView.setAlpha(1f);
		// }
		// buttonBlocked.setText(R.string.button_show);
		// //buttonBlocked.setEnabled(false);
	}

	@Override
	public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
	}

	@Override
	public void onShowcaseViewShow(ShowcaseView showcaseView) {
		// dimView(listView);
		// buttonBlocked.setText(R.string.button_hide);
		// //buttonBlocked.setEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_claim_showcase:
			// ShowCase Tutorial
			counter = 0;
			sv = new ShowcaseView.Builder(this, true)
					.setTarget(
							new ViewTarget(tabs.getTabsContainer()
									.getChildAt(0)))
					.setContentTitle(getString(R.string.showcase_claim_title))
					.setContentText(getString(R.string.showcase_claim_message))
					.setStyle(R.style.CustomShowcaseTheme)
					.setOnClickListener(this).build();
			sv.setButtonText(getString(R.string.next));
			sv.setSkipButtonText(getString(R.string.skip));
			setAlpha(0.2f, tabs.getTabsContainer().getChildAt(0), tabs
					.getTabsContainer().getChildAt(1), tabs.getTabsContainer()
					.getChildAt(2), tabs.getTabsContainer().getChildAt(3), tabs
					.getTabsContainer().getChildAt(4), tabs.getTabsContainer()
					.getChildAt(5), mViewPager);
			return true;
			// Respond to the action bar's Up/Home button
		case android.R.id.home:
			// This is called when the Home (Up) button is pressed in the action
			// bar.

			final ClaimDetailsFragment fragment = (ClaimDetailsFragment) fragmentReferences
					.get(0);

			if (fragment != null) {
				if (fragment.checkChanges()) {

					// Intent upIntent;
					//
					// upIntent = NavUtils.getParentActivityIntent(this);
					// upIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
					// | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					// startActivity(upIntent);
					// finish();

					return true;
				} else {
					Intent upIntent;

					upIntent = NavUtils.getParentActivityIntent(this);
					upIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
							| Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(upIntent);
					finish();

					return true;
				}
			} else {

				Intent upIntent;

				upIntent = NavUtils.getParentActivityIntent(this);
				upIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
						| Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(upIntent);
				finish();

				return true;
			}
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// END SHOWCASE

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		getMenuInflater().inflate(R.menu.claim, menu);
		return true;
	}

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public void destroyItem(android.view.ViewGroup container, int position,
				Object object) {

			// fragmentReferences.remove(position);

		}

		@Override
		public Fragment getItem(int position) {

			Fragment fragment;
			int sectionPosition = position - NUMBER_OF_STATIC_SECTIONS;
			switch (position) {
			case 0:
				fragment = new ClaimDetailsFragment();
				break;
			case 1:
				fragment = new OwnersFragment();
				break;
			case 2:
				fragment = new ClaimDocumentsFragment();
				break;
			case 3:
				fragment = new AdjacentClaimsFragment();
				break;
			case 4:
				fragment = new ClaimMapFragment();
				break;
			case 5:
				fragment = new ChallengingClaimsFragment();
				break;
			default:
				List<SectionTemplate> sectionTemplateList = formTemplate
						.getSectionTemplateList();
				if (sectionTemplateList == null
						|| sectionTemplateList.size() <= 0
						|| sectionTemplateList.size() <= sectionPosition) {
					SectionElementFragment sef = new SectionElementFragment();
					sef.setTemplate(new SectionTemplate());
					sef.setPayload(new SectionElementPayload());
					sef.setMode(mode);
					return sef;
				}
				SectionTemplate sectionTemplate = sectionTemplateList
						.get(sectionPosition);
				if (sectionTemplate == null) {
					SectionElementFragment sef = new SectionElementFragment();
					sef.setTemplate(new SectionTemplate());
					sef.setPayload(new SectionElementPayload());
					sef.setMode(mode);
					return sef;
				}
				if (sectionTemplate.getMaxOccurrences() > 1) {
					fragment = new SectionFragment();
					((SectionFragment)fragment).setTemplate(sectionTemplate);
					((SectionFragment)fragment).setPayload(editedFormPayload
							.getSectionPayloadList().get(sectionPosition));
					((SectionFragment)fragment).setMode(mode);
				} else {
					if (editedFormPayload.getSectionPayloadList()
							.get(sectionPosition)
							.getSectionElementPayloadList().size() == 0) {
						editedFormPayload
								.getSectionPayloadList()
								.get(sectionPosition)
								.getSectionElementPayloadList()
								.add(new SectionElementPayload(sectionTemplate));
					}
					fragment = new SectionElementFragment();
					((SectionElementFragment)fragment).setTemplate(sectionTemplate);
					((SectionElementFragment)fragment).setPayload(editedFormPayload
							.getSectionPayloadList().get(sectionPosition)
							.getSectionElementPayloadList().get(0));
					((SectionElementFragment)fragment).setMode(mode);
				}
			}
			fragmentReferences.put(position, fragment);
			return fragment;
		}

		@Override
		public int getCount() {

			return NUMBER_OF_STATIC_SECTIONS + getNumberOfSections();

		}

		private int getNumberOfSections() {
			if (editedFormPayload != null) {
				return editedFormPayload.getSectionPayloadList().size();
			} else if (formTemplate != null) {
				return formTemplate.getSectionTemplateList().size();
			} else {
				return 0;
			}
		}

		private String getSectionTitle(int position) {
			DisplayNameLocalizer dnl = new DisplayNameLocalizer(
					OpenTenureApplication.getInstance().getLocalization());
			String sectionTitle = null;
			if (editedFormPayload != null) {
				sectionTitle = editedFormPayload.getSectionPayloadList()
						.get(position).getDisplayName().toUpperCase(Locale.US);
			} else if (formTemplate != null) {
				sectionTitle = formTemplate.getSectionTemplateList()
						.get(position).getDisplayName()
						.toUpperCase(Locale.getDefault());
			} else {
				sectionTitle = "";
			}
			return dnl.getLocalizedDisplayName(sectionTitle);

		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();

			int sectionPosition = position - NUMBER_OF_STATIC_SECTIONS;

			switch (position) {
			case 0:
				return getString(R.string.title_claim).toUpperCase(l);
			case 1:
				return getString(R.string.owners).toUpperCase(l);
			case 2:
				return getString(R.string.title_claim_documents).toUpperCase(l);
			case 3:
				return getString(R.string.title_claim_adjacencies).toUpperCase(
						l);
			case 4:
				return getString(R.string.title_claim_map).toUpperCase(l);
			case 5:
				return getString(R.string.title_claim_challenges)
						.toUpperCase(l);
			default:
				return getSectionTitle(sectionPosition);
			}
		}
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
	public String getClaimId() {
		return claimId;
	}

	@Override
	public Mode getMode() {
		return mode;
	}

	@Override
	public void onClaimSaved() {
		ClaimMapFragment claimMapFragment = (ClaimMapFragment) fragmentReferences
				.get(4);
		if (editedFormPayload != null) {
			editedFormPayload.setClaimId(claimId);
		}
		if (originalFormPayload != null) {
			originalFormPayload.setClaimId(claimId);
		}
		if (claimMapFragment != null)
			claimMapFragment.onClaimSaved();
	}

	@Override
	public FormPayload getEditedFormPayload() {
		return editedFormPayload;
	}

	@Override
	public FormTemplate getFormTemplate() {
		return formTemplate;
	}

	@Override
	public FormPayload getOriginalFormPayload() {
		return originalFormPayload;
	}

	@Override
	public void resetOriginalFormPayload() {
		// TODO Auto-generated method stub

		originalFormPayload = editedFormPayload;

	}

}
