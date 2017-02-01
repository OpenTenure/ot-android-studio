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

import org.fao.sola.clients.android.opentenure.model.Link;

import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

public class OpenTenurePreferencesFragment extends PreferenceFragment {
	
	private class PrefChangeListener implements SharedPreferences.OnSharedPreferenceChangeListener{

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {

			// LANGUAGE SELECTION REQUIRES RESTARTING THE APPLICATION

			if (key.equals(OpenTenure.language)) {
				getActivity()
						.setResult(
								OpenTenurePreferencesActivity.RESULT_CODE_RESTART);
			}
			if (key.equals(OpenTenurePreferencesActivity.CS_URL_PREF)) {
				Link link = Link.getLink(Link.ID_CS_URL);
				if(link != null){
					SharedPreferences OpenTenurePreferences = PreferenceManager
							.getDefaultSharedPreferences(OpenTenureApplication.getContext());
					String csUrl = OpenTenurePreferences.getString(
							OpenTenurePreferencesActivity.CS_URL_PREF,
							OpenTenureApplication._DEFAULT_COMMUNITY_SERVER);
					link.setUrl(csUrl);
					link.update();
				}
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		SharedPreferences OpenTenurePreferences = PreferenceManager
				.getDefaultSharedPreferences(OpenTenureApplication.getContext());
		OpenTenurePreferences
				.registerOnSharedPreferenceChangeListener(new PrefChangeListener());
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		SharedPreferences OpenTenurePreferences = PreferenceManager
				.getDefaultSharedPreferences(OpenTenureApplication.getContext());
		OpenTenurePreferences
				.registerOnSharedPreferenceChangeListener(new PrefChangeListener());
		EditTextPreference versionPref = (EditTextPreference)findPreference(OpenTenurePreferencesActivity.SOFTWARE_VERSION_PREF);
		String version;
		try {
			version = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			version = "Not found";
		}
		versionPref.setTitle(getString(R.string.software_version_title) + ": " + version);
	}
}
