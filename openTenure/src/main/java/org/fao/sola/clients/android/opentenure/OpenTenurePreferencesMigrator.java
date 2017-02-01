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

import android.content.SharedPreferences;
import android.util.Log;

public class OpenTenurePreferencesMigrator {
	
	private static final String VERSION_1_1_0 = "1.1.0";

	public static void migratePreferences(SharedPreferences preferences, String currentVersion, String newVersion){
		
		if(VERSION_1_1_0.compareTo(currentVersion)>0
				&& VERSION_1_1_0.compareTo(newVersion)<=0){
			migratePreferences_1_1_0(preferences);
			Log.d(OpenTenurePreferencesMigrator.class.getName(), "Preferences migrated to version " + VERSION_1_1_0);
		}

		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(OpenTenurePreferencesActivity.SOFTWARE_VERSION_PREF, newVersion);
		editor.commit();
	}
	
	public static void migratePreferences_1_1_0(SharedPreferences preferences){

		String language = OpenTenure.default_language;
		
		if(preferences.getBoolean(OpenTenure.albanian_language, false)){
			language = OpenTenure.albanian_language;
		}
		if(preferences.getBoolean(OpenTenure.khmer_language, false)){
			language = OpenTenure.khmer_language;
		}
		if(preferences.getBoolean(OpenTenure.burmese_language, false)){
			language = OpenTenure.burmese_language;
		}
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(OpenTenure.language, language);
		editor.commit();
		
		String csUrl = preferences.getString(
				OpenTenurePreferencesActivity.CS_URL_PREF, "") ;
		String formUrl = preferences.getString(
				OpenTenurePreferencesActivity.FORM_URL_PREF, csUrl) ;

		if(formUrl.equalsIgnoreCase(csUrl) || formUrl.equalsIgnoreCase(csUrl+"/")){
			// There's no longer need to specify the dynamic form URL
			// if it's the default form on CS url as of 1.1.0
			editor = preferences.edit();
			editor.putString(OpenTenurePreferencesActivity.FORM_URL_PREF, "");
			editor.commit();
		}
	}

}
