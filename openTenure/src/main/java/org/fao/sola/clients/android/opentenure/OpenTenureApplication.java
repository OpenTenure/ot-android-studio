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

import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.maps.MainMapFragment;
import org.fao.sola.clients.android.opentenure.model.ClaimType;
import org.fao.sola.clients.android.opentenure.model.Database;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.AndroidHttpClient;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

public class OpenTenureApplication extends Application {
	private static final String VERSION_NOT_FOUND = "Not found";
	private static OpenTenureApplication sInstance;
	private Database database;
	private static Context context;
	private boolean networkError = false;
	private boolean checkedTypes = false;
	private boolean checkedDocTypes = false;
	private boolean checkedIdTypes = false;
	private boolean checkedLandUses = false;
	private boolean checkedLanguages = false;
	private boolean checkedGeometryRequired = false;

	private boolean checkedCommunityArea = false;
	private boolean checkedForm = false;
	private boolean initialized = false;
	private static final String SEMAPHORE = "semaphore";
	private static final String _KHMER_LOCALIZATION = "km-KH";
	private static final String _ALBANIAN_LOCALIZATION = "sq-AL";
	private static final String _ARABIC_LOCALIZATION = "ar-JO";
	private static final String _BURMESE_LOCALIZATION = "my-MM";

	private String localization;
	private Locale locale;
	private boolean khmer = false;
	private boolean albanian = false;
	private boolean burmese = false;
	private static boolean loggedin;
	private static String username;
	private static Activity activity;
	private static List<ClaimType> claimTypes;
	private static AndroidHttpClient mHttpClient;
	private static CookieStore cookieStore;
	private static HttpContext http_context;
	private static MainMapFragment mapFragment;
	private static ClaimDocumentsFragment documentsFragment;
	private static ClaimDetailsFragment detailsFragment;
	private static OwnersFragment ownersFragment;
	private static String claimId;
	private List<String> changingClaims = null;

	private static View personsView;
	private static LocalClaimsFragment localClaimsFragment;
	private static FragmentActivity newsFragmentActivity;
	public static String _DEFAULT_COMMUNITY_SERVER = "https://demo.opentenure.org";

	private static volatile int claimsToDownload = 0;
	private static volatile int initialClaimsToDownload = 0;
	private static volatile int claimsDownloaded = 0;

	public static OpenTenureApplication getInstance() {
		return sInstance;
	}

	public Database getDatabase() {
		return database;
	}

	public static OwnersFragment getOwnersFragment() {
		return ownersFragment;
	}

	public static void setOwnersFragment(OwnersFragment ownersFragment) {
		OpenTenureApplication.ownersFragment = ownersFragment;
	}

	public static ClaimDocumentsFragment getDocumentsFragment() {
		return documentsFragment;
	}

	public static void setDocumentsFragment(
			ClaimDocumentsFragment documentsFragment) {
		OpenTenureApplication.documentsFragment = documentsFragment;
	}

	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}

	public boolean isConnectedWifi(Context context) {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return (netInfo != null && netInfo.isConnected() && netInfo.getType() == ConnectivityManager.TYPE_WIFI);
	}

	public boolean isConnectedMobile(Context context) {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return (netInfo != null && netInfo.isConnected() && netInfo.getType() == ConnectivityManager.TYPE_MOBILE);
	}

	public static String getConnectionType(int type, int subType) {
		if (type == ConnectivityManager.TYPE_WIFI) {
			return "TYPE_WIFI";
		} else if (type == ConnectivityManager.TYPE_MOBILE) {
			switch (subType) {
			case TelephonyManager.NETWORK_TYPE_UNKNOWN:
				return "TYPE_UNKNOWN";
			case TelephonyManager.NETWORK_TYPE_1xRTT:
				return "TYPE_1XRTT"; // ~ 50-100 kbps
			case TelephonyManager.NETWORK_TYPE_CDMA:
				return "TYPE_CDMA"; // ~ 14-64 kbps
			case TelephonyManager.NETWORK_TYPE_EDGE:
				return "TYPE_EDGE"; // ~ 50-100 kbps
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
				return "TYPE_EVDO_0"; // ~ 400-1000 kbps
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
				return "TYPE_EVDO_A"; // ~ 600-1400 kbps
			case TelephonyManager.NETWORK_TYPE_GPRS:
				return "TYPE_GPRS"; // ~ 100 kbps
			case TelephonyManager.NETWORK_TYPE_HSDPA:
				return "TYPE_HSDPA"; // ~ 2-14 Mbps
			case TelephonyManager.NETWORK_TYPE_HSPA:
				return "TYPE_HSPA"; // ~ 700-1700 kbps
			case TelephonyManager.NETWORK_TYPE_HSUPA:
				return "TYPE_HSUPA"; // ~ 1-23 Mbps
			case TelephonyManager.NETWORK_TYPE_LTE:
				return "TYPE_LTE"; // ~ 50-1000 Mbps
			case TelephonyManager.NETWORK_TYPE_UMTS:
				return "TYPE_UMTS"; // ~ 400-7000 kbps
			case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
				return "TYPE_IDEN"; // ~25 kbps

				// Unknown
			default:
				return "TYPE_UNKNOWN";
			}
		} else {
			return "TYPE UNKNOWN";
		}
	}

	@Override
	public void onCreate() {

		context = getApplicationContext();
		sInstance = this;
		sInstance.initializeInstance();

		OpenTenure.setLocale(this);
		FileSystemUtilities.createClaimsFolder();
		FileSystemUtilities.createClaimantsFolder();
		FileSystemUtilities.createOpenTenureFolder();
		FileSystemUtilities.createCertificatesFolder();
		FileSystemUtilities.createImportFolder();
		FileSystemUtilities.createExportFolder();
		// Get current software version from preferences and new one from
		// package info for migration
		String version = null;
		try {
			version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			version = VERSION_NOT_FOUND;
		}
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getContext());
		// Use new version as default for new one
		String currentVersion = preferences.getString(
				OpenTenurePreferencesActivity.SOFTWARE_VERSION_PREF,
				VERSION_NOT_FOUND);
		OpenTenurePreferencesMigrator.migratePreferences(preferences,
				currentVersion, version);
		super.onCreate();

	}

	protected void initializeInstance() {
		// Start without a DB encryption password
		database = new Database(getApplicationContext(), "");

	}

	public static HttpContext getHttp_context() {
		return http_context;
	}

	public static void setHttp_context(HttpContext http_context) {
		OpenTenureApplication.http_context = http_context;
	}

	public static CookieStore getCoockieStore() {
		if (cookieStore != null)
			return cookieStore;
		else {
			cookieStore = new BasicCookieStore();
			return cookieStore;
		}

	}

	public static void setCoockieStore(CookieStore coockieStore) {
		OpenTenureApplication.cookieStore = coockieStore;
	}

	public static boolean isLoggedin() {
		return loggedin;
	}

	public static void setLoggedin(boolean loggedin) {
		OpenTenureApplication.loggedin = loggedin;
	}

	public static String getUsername() {
		return username;
	}

	public static void setUsername(String username) {
		OpenTenureApplication.username = username;
	}

	public static Context getContext() {
		return context;
	}

	public static void setContext(Context context) {
		OpenTenureApplication.context = context;
	}

	public boolean isCheckedDocTypes() {
		return checkedDocTypes;
	}

	public void setCheckedDocTypes(boolean checkedDocTypes) {
		this.checkedDocTypes = checkedDocTypes;
	}

	public boolean isCheckedLanguages() {
		return checkedLanguages;
	}

	public void setCheckedLanguages(boolean checkedLanguages) {
		this.checkedLanguages = checkedLanguages;
	}

	public boolean isCheckedGeometryRequired() {
		return checkedGeometryRequired;
	}

	public void setCheckedGeometryRequired(boolean checkedGeometryRequired) {
		this.checkedGeometryRequired = checkedGeometryRequired;
	}

	/*
	 * Return the single instance of the inizialized HttpClient that handle
	 * connection and session to the server
	 */
	public static synchronized AndroidHttpClient getHttpClient() {

		if (mHttpClient != null)
			return mHttpClient;
		else
			return prepareClient();
	}

	/*
	 * Return the single instance of the inizialized HttpClient that handle
	 * connection and session to the server
	 */
	public static synchronized void closeHttpClient() {

		mHttpClient.close();
		mHttpClient = null;

	}

	public static Activity getActivity() {
		return activity;
	}

	public static void setActivity(Activity activity) {
		OpenTenureApplication.activity = activity;
	}

	public static MainMapFragment getMapFragment() {
		return mapFragment;
	}

	public static void setMapFragment(MainMapFragment mapFragment) {
		OpenTenureApplication.mapFragment = mapFragment;
	}

	public boolean isCheckedTypes() {
		return checkedTypes;
	}

	public boolean isCheckedIdTypes() {
		return checkedIdTypes;
	}

	public void setCheckedIdTypes(boolean checkedIdTypes) {
		this.checkedIdTypes = checkedIdTypes;
	}

	public boolean isCheckedLandUses() {
		return checkedLandUses;
	}

	public void setCheckedLandUses(boolean checkedLandUses) {
		this.checkedLandUses = checkedLandUses;
	}

	public boolean isCheckedForm() {
		return checkedForm;
	}

	public void setCheckedForm(boolean checkedForm) {
		this.checkedForm = checkedForm;
	}

	public void setCheckedTypes(boolean checkedTypes) {
		this.checkedTypes = checkedTypes;
	}

	public static View getPersonsView() {
		return personsView;
	}

	public static void setPersonsView(View personsView) {
		OpenTenureApplication.personsView = personsView;
	}

	public static List<ClaimType> getClaimTypes() {
		return claimTypes;
	}

	public static void setClaimTypes(List<ClaimType> claimTypes) {
		OpenTenureApplication.claimTypes = claimTypes;
	}

	public boolean isCheckedCommunityArea() {
		return checkedCommunityArea;
	}

	public void setCheckedCommunityArea(boolean checkedCommunityArea) {
		this.checkedCommunityArea = checkedCommunityArea;
	}

	public static LocalClaimsFragment getLocalClaimsFragment() {
		return localClaimsFragment;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public static FragmentActivity getNewsFragment() {
		return newsFragmentActivity;
	}

	public static void setNewsFragment(FragmentActivity newsFragment) {
		OpenTenureApplication.newsFragmentActivity = newsFragment;
	}

	public static int getClaimsToDownload() {
		synchronized (SEMAPHORE) {
			return claimsToDownload;
		}
	}

	public static int getInitialClaimsToDownload() {
		synchronized (SEMAPHORE) {
			return initialClaimsToDownload;
		}
	}

	public static void decrementClaimsToDownload() {
		synchronized (SEMAPHORE) {
			claimsToDownload--;

			System.out.println("claimsToDownload " + claimsToDownload);
		}
	}

	public static void setClaimsToDownload(int initialClaimsToDownload) {
		synchronized (SEMAPHORE) {
			claimsToDownload = initialClaimsToDownload;
			OpenTenureApplication.initialClaimsToDownload = initialClaimsToDownload;
		}
	}

	public static int getDownloadCompletion() {

		synchronized (SEMAPHORE) {
			return (int) ((((float) (initialClaimsToDownload - claimsToDownload) / (float) initialClaimsToDownload)) * 100.0);
		}

	}

	public static int getClaimsDownloaded() {
		synchronized (SEMAPHORE) {
			return claimsDownloaded;
		}
	}

	public static void setClaimsDownloaded(int claimsDownloaded) {
		synchronized (SEMAPHORE) {
			OpenTenureApplication.claimsDownloaded = claimsDownloaded;
		}
	}

	public String getLocalization() {
		return localization;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	public boolean isNetworkError() {
		return networkError;
	}

	public void setNetworkError(boolean networkError) {
		this.networkError = networkError;
	}

	public void setLocalization(String localization) {
		this.localization = localization;
	}

	public static ClaimDetailsFragment getDetailsFragment() {
		return detailsFragment;
	}

	public static void setDetailsFragment(ClaimDetailsFragment detailsFragment) {
		OpenTenureApplication.detailsFragment = detailsFragment;
	}

	public static String getClaimId() {
		return claimId;
	}

	public static void setClaimId(String claimId) {
		OpenTenureApplication.claimId = claimId;
	}

	public boolean isKhmer() {
		return khmer;
	}

	public void setKhmer(boolean khmer) {
		this.khmer = khmer;
	}

	public boolean isAlbanian() {
		return albanian;
	}

	public void setAlbanian(boolean albanian) {
		this.albanian = albanian;
	}

	public boolean isBurmese() {
		return burmese;
	}

	public void setBurmese(boolean burmese) {
		this.burmese = burmese;
	}

	public void setLocalization(Locale locale) {

		Resources.getSystem().getConfiguration().setLocale(locale);

		locale.getDisplayLanguage();
		if (isKhmer()) {

			localization = OpenTenureApplication._KHMER_LOCALIZATION;
		} else if (isAlbanian()) {

			localization = OpenTenureApplication._ALBANIAN_LOCALIZATION;

		} else if (isBurmese()) {

			localization = OpenTenureApplication._BURMESE_LOCALIZATION;

		} else if (locale.getLanguage().toLowerCase(locale).equals("ar")) {

			localization = _ARABIC_LOCALIZATION;

		} else {

			localization = Resources.getSystem().getConfiguration().locale
					.getLanguage()
					+ "-"
					+ Resources.getSystem().getConfiguration().locale
							.getCountry();
		}
		setLocale(locale);
		System.out.println("Localization is now: " + localization);

	}

	public static void setLocalClaimsFragment(
			LocalClaimsFragment localClaimsFragment) {
		OpenTenureApplication.localClaimsFragment = localClaimsFragment;
	}

	/*
	 * Initialize the single istance of AndroidHttpClient that will handle the
	 * connections to the server
	 */
	private static AndroidHttpClient prepareClient() {

		try {

			mHttpClient = AndroidHttpClient.newInstance("Android", context);
			http_context = new BasicHttpContext();

			cookieStore = getCoockieStore();
			http_context.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

			Log.d("OpenTEnureApplication", "Initialized HTTP Client");

		} catch (Throwable e) {
			e.printStackTrace();

		}
		return mHttpClient;

	}

	// This set of methods is for handling the rendering of claims currently
	// changing status
	public void addClaimtoList(String id) {
		synchronized (this) {

			if (changingClaims == null)
				changingClaims = new ArrayList<String>();
			changingClaims.add(id);
		}

	}

	public void clearClaimsList() {
		synchronized (this) {

			if (changingClaims == null)
				changingClaims = new ArrayList<String>();
			changingClaims.clear();

		}
	}

	public List<String> getChangingClaims() {
		synchronized (this) {
			if (changingClaims == null)
				changingClaims = new ArrayList<String>();
			return this.changingClaims;

		}
	}
}