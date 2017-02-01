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
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.maps.MainMapFragment;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.Configuration;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.github.amlcurran.showcaseview.ApiUtils;
import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

public class OpenTenure extends FragmentActivity implements ModeDispatcher,
		OnShowcaseEventListener, View.OnClickListener {

	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;
	PagerSlidingTabStrip tabs;
	Mode mode = Mode.MODE_RW;

	// SHOWCASE VARIABLES
	ShowcaseView sv;
	private int counter = 0;
	int numberOfClaims = 0;
	private final ApiUtils apiUtils = new ApiUtils();
	public static final String FIRST_RUN_OT_ACTIVITY = "__FIRST_RUN_OT_ACTIVITY__";
	public static final String language = "language";
	public static final String albanian_language = "albanian_language";
	public static final String default_language = "default_language";
	public static final String khmer_language = "khmer_language";
	public static final String burmese_language = "burmese_language";
	public static final String tiles_provider = "tiles_provider";
	public static final String tms_tiles_provider = "tms_tiles_provider";
	public static final String geoserver_tiles_provider = "geoserver_tiles_provider";
	// END SHOW CASE

	@Override
	public void onPause() {
		OpenTenureApplication.getInstance().getDatabase().sync();
		super.onPause();
	};
	
	private static void setLocale(Context context, Locale locale){
		Locale.setDefault(locale);
		android.content.res.Configuration config = new android.content.res.Configuration();
		config.locale = locale;

		context.getResources().updateConfiguration(config,
				context.getResources().getDisplayMetrics());

		OpenTenureApplication.getInstance().setLocalization(locale);
	}

	public static void setLocale(Context context){
		SharedPreferences OpenTenurePreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		
		String language = OpenTenurePreferences.getString(OpenTenure.language, OpenTenure.default_language);
		
		Log.d(OpenTenure.class.getName(), "Setting locale for language: " + language);
		
		OpenTenureApplication.getInstance().setKhmer(language.equals(OpenTenure.khmer_language));
		OpenTenureApplication.getInstance().setAlbanian(language.equals(OpenTenure.albanian_language));
		OpenTenureApplication.getInstance().setBurmese(language.equals(OpenTenure.burmese_language));
		if (OpenTenureApplication.getInstance().isKhmer()) {
			setLocale(context, new Locale("km"));
		} else if (OpenTenureApplication.getInstance().isAlbanian()) {
			setLocale(context, new Locale("sq"));
		} else if (OpenTenureApplication.getInstance().isBurmese()) {
			setLocale(context, new Locale("my"));
		}else{
			setLocale(context, Locale.getDefault());
		}
	}

	@Override
	public void onResume() {
		OpenTenureApplication.getInstance().getDatabase().open();
		setLocale(this);
		super.onResume();
	};

	@Override
	public void onDestroy() {
		cleanup();
		super.onDestroy();
	};

	@Override
	public void onBackPressed() {
		AlertDialog.Builder exitDialog = new AlertDialog.Builder(this);
		exitDialog.setTitle(R.string.title_exit_dialog);
		exitDialog.setMessage(getResources().getString(
				R.string.message_exit_dialog));
		exitDialog.setPositiveButton(R.string.confirm, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				cleanup();
				OpenTenure.super.onBackPressed();
			}
		});
		exitDialog.setNegativeButton(R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		exitDialog.show();
	}

	public void cleanup() {
		OpenTenureApplication.getInstance().getDatabase().close();
		OpenTenureApplication.getInstance().setCheckedCommunityArea(false);
		OpenTenureApplication.getInstance().setCheckedDocTypes(false);
		OpenTenureApplication.getInstance().setCheckedForm(false);
		OpenTenureApplication.getInstance().setCheckedIdTypes(false);
		OpenTenureApplication.getInstance().setCheckedLandUses(false);
		OpenTenureApplication.getInstance().setCheckedTypes(false);
		OpenTenureApplication.getInstance().setInitialized(false);
	}

	private String getFirstRun() {
		String result = "False";
		Configuration firstRun = Configuration
				.getConfigurationByName(FIRST_RUN_OT_ACTIVITY);

		if (firstRun != null) {
			result = firstRun.getValue();
			firstRun.setValue("False");
			firstRun.update();
		} else {
			firstRun = new Configuration();
			firstRun.setName(FIRST_RUN_OT_ACTIVITY);
			firstRun.setValue("False");
			firstRun.create();
			result = "True";
		}
		return result;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setLocale(this);
		setContentView(R.layout.activity_open_tenure);
		ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

		Log.d(this.getClass().getName(),
				"Starting with " + activityManager.getMemoryClass()
						+ "MB of memory for the application.");

		tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		mViewPager = (ViewPager) findViewById(R.id.open_tenure_pager);
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		mViewPager.setAdapter(mSectionsPagerAdapter);
		tabs.setIndicatorColor(getResources().getColor(
				R.color.ab_tab_indicator_opentenure));
		tabs.setViewPager(mViewPager);

		// ShowCase Main
		if (getFirstRun().contentEquals("True")) {
			sv = new ShowcaseView.Builder(this, true).setTarget(Target.NONE)
					.setContentTitle(getString(R.string.showcase_main_title))
					.setContentText(getString(R.string.showcase_main_message))
					.setStyle(R.style.CustomShowcaseTheme)
					.setOnClickListener(this).build();
			sv.setButtonText(getString(R.string.next));
			sv.setSkipButtonText(getString(R.string.skip));
			setAlpha(0.2f, tabs.getTabsContainer().getChildAt(0), tabs
					.getTabsContainer().getChildAt(1), tabs.getTabsContainer()
					.getChildAt(2), tabs.getTabsContainer().getChildAt(3),
					mViewPager);
		}

		if (!OpenTenureApplication.getInstance().isOnline()) {
			// Alert the user about missing connection
			AlertDialog.Builder alertConnectionBuilder = new AlertDialog.Builder(
					this);
			alertConnectionBuilder.setTitle(R.string.title_no_connection);
			alertConnectionBuilder.setMessage(getResources().getString(
					R.string.message_no_connection_at_startup));

			alertConnectionBuilder.setPositiveButton(R.string.confirm,
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});

			final AlertDialog alertConnectionDialog = alertConnectionBuilder
					.create();
			alertConnectionDialog.show();
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
			sv.hide();
			setAlpha(1.0f, tabs.getTabsContainer().getChildAt(0), tabs
					.getTabsContainer().getChildAt(1), tabs.getTabsContainer()
					.getChildAt(2), tabs.getTabsContainer().getChildAt(3),
					tabs, mViewPager);
			counter = 0;
		}

		Configuration conf = Configuration
				.getConfigurationByName("isInitialized");
		numberOfClaims = Claim.getNumberOfClaims();

		switch (counter) {
		case 0:
			sv.setShowcase(
					new ViewTarget(tabs.getTabsContainer().getChildAt(0)), true);
			sv.setContentTitle(getString(R.string.title_news).toUpperCase(
					Locale.getDefault()));
			sv.setContentText(getString(R.string.showcase_news_message));
			mViewPager.setCurrentItem(0);
			setAlpha(1.0f, tabs.getTabsContainer().getChildAt(0));
			break;

		case 1:
			sv.setShowcase(new ViewTarget(findViewById(R.id.action_lock)), true);
			sv.setContentTitle("  ");
			sv.setContentText(getString(R.string.showcase_actionNews_message));
			break;

		case 2:
			sv.setShowcase(
					new ViewTarget(tabs.getTabsContainer().getChildAt(1)), true);
			sv.setContentTitle(getString(R.string.title_map).toUpperCase(
					Locale.getDefault()));
			sv.setContentText(getString(R.string.showcase_map_message));
			setAlpha(1.0f, tabs.getTabsContainer().getChildAt(1));
			mViewPager.setCurrentItem(1);
			break;
		case 3:
			
			if(findViewById(R.id.action_download_claims) != null){
			sv.setContentTitle("  ");
			sv.setShowcase(new ViewTarget(
					findViewById(R.id.action_download_claims)), true);
			sv.setContentText(getString(R.string.showcase_actionMap_message));
			break;
			}
			
		case 4:
			sv.setShowcase(
					new ViewTarget(tabs.getTabsContainer().getChildAt(2)), true);
			sv.setContentTitle(getString(R.string.title_claims).toUpperCase(
					Locale.getDefault()));
			sv.setContentText(getString(R.string.showcase_claims_message));
			setAlpha(1.0f, tabs.getTabsContainer().getChildAt(2));
			mViewPager.setCurrentItem(2);
			break;
		case 5:
			if (numberOfClaims > 0) {

				sv.setShowcase(new ViewTarget(mViewPager), true);
				sv.setContentTitle("  ");
				sv.setContentText(getString(R.string.showcase_claim_select_message));
			} else {
				sv.setShowcase(new ViewTarget(findViewById(R.id.action_new)),
						true);
				sv.setContentTitle("  ");
				sv.setContentText(getString(R.string.showcase_actionClaims_message));

			}
			break;

		case 6:
			if (numberOfClaims > 0) {
				sv.setShowcase(new ViewTarget(findViewById(R.id.action_new)),
						true);
				sv.setContentText(getString(R.string.showcase_actionClaims_message));
			}

			else {
				mViewPager.setCurrentItem(0);

				if ((conf == null)
						|| ((conf != null) && (conf.getValue()
								.equalsIgnoreCase("false")))) {

					mViewPager.setCurrentItem(0);

					sv.setShowcase(new ViewTarget(tabs.getTabsContainer()
							.getChildAt(0)), true);
					sv.setContentText(getString(R.string.showcase_actionAlert1_message));
					setAlpha(1.0f, tabs);
				} else {
					sv.hide();
					mViewPager.setCurrentItem(0);
					setAlpha(1.0f, tabs, mViewPager);
					counter = 0;
				}
			}
			break;
		case 7:

			if (numberOfClaims > 0) {
				if ((conf == null)
						|| ((conf != null) && (conf.getValue()
								.equalsIgnoreCase("false")))) {
					sv.setShowcase(new ViewTarget(tabs.getTabsContainer()
							.getChildAt(0)), true);
					sv.setContentText(getString(R.string.showcase_actionAlert1_message));
					setAlpha(1.0f, tabs);
				} else {
					sv.hide();
					mViewPager.setCurrentItem(0);
					setAlpha(1.0f, tabs, mViewPager);
					counter = 0;
				}
			}

			else {
				if ((conf == null)
						|| ((conf != null) && (conf.getValue()
								.equalsIgnoreCase("false")))) {
					sv.setShowcase(new ViewTarget(
							findViewById(R.id.action_alert)), true);
					sv.setContentText(getString(R.string.showcase_actionAlert_message));
					sv.setButtonText(getString(R.string.close));
					setAlpha(1.0f, tabs);
				} else {
					sv.hide();
					mViewPager.setCurrentItem(0);
					setAlpha(1.0f, tabs, mViewPager);
					counter = 0;
				}
			}
			break;
		case 8:
			if ((conf == null)
					|| ((conf != null) && (conf.getValue()
							.equalsIgnoreCase("false")))) {
				sv.setShowcase(new ViewTarget(findViewById(R.id.action_alert)),
						true);
				sv.setContentText(getString(R.string.showcase_actionAlert_message));
				sv.setButtonText(getString(R.string.close));
				setAlpha(1.0f, tabs);
			} else {
				sv.hide();
				mViewPager.setCurrentItem(0);
				setAlpha(1.0f, tabs, mViewPager);
				counter = 0;
			}
			break;
		case 9:
			sv.hide();
			mViewPager.setCurrentItem(0);
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

	// END SHOWCASE

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.open_tenure, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent intent = new Intent();
			intent.setClass(OpenTenure.this,
					OpenTenurePreferencesActivity.class);
			startActivityForResult(intent, OpenTenurePreferencesActivity.REQUEST_CODE);
			return true;
		case R.id.action_export_log:
			exportLog();
			String backupMessage = String.format(OpenTenureApplication
					.getContext().getString(R.string.message_log_exported));

			Toast backupToast = Toast.makeText(
					OpenTenureApplication.getContext(), backupMessage,
					Toast.LENGTH_LONG);
			backupToast.show();
			return true;
		case R.id.action_showcase:

			// ShowCase Tutorial
			sv = new ShowcaseView.Builder(this, true).setTarget(Target.NONE)
					.setContentTitle(getString(R.string.showcase_main_title))
					.setContentText(getString(R.string.showcase_main_message))
					.setStyle(R.style.CustomShowcaseTheme)
					.setOnClickListener(this).build();
			sv.setButtonText(getString(R.string.next));
			sv.setSkipButtonText(getString(R.string.skip));
			mViewPager.setCurrentItem(0);
			setAlpha(0.2f, tabs.getTabsContainer().getChildAt(0), tabs
					.getTabsContainer().getChildAt(1), tabs.getTabsContainer()
					.getChildAt(2), tabs.getTabsContainer().getChildAt(3),
					mViewPager);

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void exportLog(){
		String exportPath = null;
		try{
			Log.d(this.getClass().getName(), "**** Open Tenure Application Log ****");
		    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss", Locale.US);
			exportPath = FileSystemUtilities.getOpentenureFolder() + File.separator + dateFormat.format(new Date()) + "-log-export.txt";

	         new ProcessBuilder()
	         .command("logcat", "-d","-f",exportPath)
	         .redirectErrorStream(true)
	         .start();
	    	 // Clear log to avoid duplicate lines
	         new ProcessBuilder()
	         .command("logcat", "-c")
	         .redirectErrorStream(true)
	         .start();
	    } catch (Exception e) {
	    }
	}

	public void restart(){
		Context context = OpenTenureApplication
				.getContext();
		Intent mStartActivity = OpenTenureApplication
				.getContext()
				.getPackageManager()
				.getLaunchIntentForPackage(
						OpenTenureApplication
								.getContext()
								.getPackageName());
		mStartActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		int mPendingIntentId = 123456;
		PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		// setWindow is not used for compatibility with API level 17
		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, mPendingIntent);
		cleanup();
		finish();
		System.exit(0);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if(resultCode == OpenTenurePreferencesActivity.RESULT_CODE_RESTART
				&& requestCode == OpenTenurePreferencesActivity.REQUEST_CODE){
			
			AlertDialog.Builder restartDialog = new AlertDialog.Builder(this);
			restartDialog.setTitle(R.string.title_restart_dialog);
			restartDialog.setMessage(getResources().getString(
					R.string.message_restart_dialog));
			restartDialog.setPositiveButton(R.string.confirm, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					restart();
				}
			});
			restartDialog.setNegativeButton(R.string.cancel, new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			restartDialog.show();
		}else{
			super.onActivityResult(requestCode, resultCode, data);
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
				return new NewsFragment();
			case 1:
				return new MainMapFragment();
				// case 2:
				// return new PersonsFragment();
			case 2:
				return new LocalClaimsFragment();
			}
			return null;
		}

		@Override
		public int getCount() {

			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_news).toUpperCase(l);
			case 1:
				return getString(R.string.title_map).toUpperCase(l);
			case 2:
				return getString(R.string.title_claims).toUpperCase(l);
			}
			return null;
		}
	}

	public void doOnBackPressed() {
		super.onBackPressed();
	}

	@Override
	public Mode getMode() {
		return mode;
	}

}
