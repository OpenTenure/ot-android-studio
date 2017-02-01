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
package org.fao.sola.clients.android.opentenure.form.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.OpenTenurePreferencesActivity;
import org.fao.sola.clients.android.opentenure.form.FormTemplate;
import org.fao.sola.clients.android.opentenure.maps.MainMapFragment;
import org.fao.sola.clients.android.opentenure.model.Configuration;
import org.fao.sola.clients.android.opentenure.model.SurveyFormTemplate;
import org.fao.sola.clients.android.opentenure.network.API.CommunityServerAPIUtilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class FormRetriever extends AsyncTask<Void, Integer, Integer> {

	private String oldDefaultFormTemplateUrl;
	private String defaultFormTemplateUrl;
	private String allFormTemplatesUrl;

	private String getFormUrl(Context context, String urlPattern) {
		SharedPreferences OpenTenurePreferences = PreferenceManager
				.getDefaultSharedPreferences(context);

		String formUrl = OpenTenurePreferences.getString(
				OpenTenurePreferencesActivity.FORM_URL_PREF, "");

		if (formUrl.equalsIgnoreCase("")) {
			// If no explicit URL is set for the dynamic form or if only the
			// server url has been specified
			// use the default one for the explicitly configured server
			// or the default one
			formUrl = String.format(urlPattern,
					OpenTenurePreferences.getString(
							OpenTenurePreferencesActivity.CS_URL_PREF,
							OpenTenureApplication._DEFAULT_COMMUNITY_SERVER));

		}

		return formUrl;
	}

	public FormRetriever(Context context) {
		oldDefaultFormTemplateUrl = getFormUrl(context, CommunityServerAPIUtilities.HTTPS_OLDDEFAULTFORM);
		defaultFormTemplateUrl = getFormUrl(context, CommunityServerAPIUtilities.HTTPS_DEFAULTFORM);
		allFormTemplatesUrl = getFormUrl(context, CommunityServerAPIUtilities.HTTPS_ALLFORMS);
	}

	protected void onPreExecute() {
	}
	
	private int saveOldDefaultForm(){

		InputStream formStream = null;
		int result = 0;

		try {
			Log.d(this.getClass().getName(),
					"Getting default dynamic survey form from: " + oldDefaultFormTemplateUrl);

			URL formUrl = new URL(oldDefaultFormTemplateUrl);
			HttpURLConnection formConnection = (HttpURLConnection) formUrl.openConnection();
			formConnection.connect();
			formStream = formConnection.getInputStream();
			String formBody = getBody(formStream);
			Log.d(this.getClass().getName(), "Got dynamic survey form: " + formBody);

			if (formBody.trim().equals("{}"))
				return 100;

			FormTemplate form = FormTemplate.fromJson(formBody);
			result = SurveyFormTemplate.saveDefaultFormTemplate(form);
			formStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (formStream != null) {
				try {
					formStream.close();
				} catch (IOException ignore) {
				}
			}
		}
		return result;

	}

	private int saveDefaultForm(){

		InputStream formStream = null;
		int result = 0;

		try {
			Log.d(this.getClass().getName(),
					"Getting default dynamic survey form from: " + defaultFormTemplateUrl);

			URL formUrl = new URL(defaultFormTemplateUrl);
			HttpURLConnection formConnection = (HttpURLConnection) formUrl.openConnection();
			formConnection.connect();
			formStream = formConnection.getInputStream();
			String formBody = getBody(formStream);
			Log.d(this.getClass().getName(), "Got dynamic survey form: " + formBody);

			if (formBody.trim().equals("{}"))
				return 100;

			FormTemplate form = FormTemplate.fromJson(formBody);
			result = SurveyFormTemplate.saveDefaultFormTemplate(form);
			formStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (formStream != null) {
				try {
					formStream.close();
				} catch (IOException ignore) {
				}
			}
		}
		return result;

	}

	private int saveAllForms(){
		InputStream formsStream = null;
		int result = 0;

		try {
			Log.d(this.getClass().getName(),
					"Getting all dynamic survey forms from: " + allFormTemplatesUrl);

			URL formsUrl = new URL(allFormTemplatesUrl);
			HttpURLConnection formsConnection = (HttpURLConnection) formsUrl.openConnection();
			formsConnection.connect();
			formsStream = formsConnection.getInputStream();
			String formsBody = getBody(formsStream);
			Log.d(this.getClass().getName(), "Got all dynamic survey forms: " + formsBody);

			if (formsBody.trim().equals("[]") || formsBody.trim().equals("[{}]"))
				return 100;

			FormTemplate[] forms = FormTemplate.fromJsonArray(formsBody);
			
			for(FormTemplate form : forms){
				result = SurveyFormTemplate.saveFormTemplate(form);
			}
			formsStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (formsStream != null) {
				try {
					formsStream.close();
				} catch (IOException ignore) {
				}
			}
		}
		return result;

	}

	protected Integer doInBackground(Void... params) {

		int result = 0;
		
		// Try to get all forms
		
		result = saveAllForms();
		if(result != 0 && result != 100){
			// At least one form retrieved
			result = saveDefaultForm();
		}else if(result == 0){
			// Maybe the server does not support the new
			// 'all forms + default' API
			// let's try the old 'get default only' one
			result = saveOldDefaultForm();
		}
		return result;

	}

	private String getBody(InputStream inputStream) throws IOException {

		String body = null;
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;

		try {
			if (inputStream != null) {
				bufferedReader = new BufferedReader(new InputStreamReader(
						inputStream));
				char[] charBuffer = new char[128];
				int bytesRead = -1;
				while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
					stringBuilder.append(charBuffer, 0, bytesRead);
				}
			} else {
				stringBuilder.append("");
			}
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException ex) {
					throw ex;
				}
			}
		}

		body = stringBuilder.toString();
		return body;
	}

	protected void onPostExecute(Integer result) {

		if (result > 0) {
			OpenTenureApplication.getInstance().setCheckedForm(true);

			synchronized (OpenTenureApplication.getInstance()) {

				if (OpenTenureApplication.getInstance()
						.isCheckedCommunityArea()
						&& OpenTenureApplication.getInstance()
								.isCheckedDocTypes()
						&& OpenTenureApplication.getInstance()
								.isCheckedIdTypes()
						&& OpenTenureApplication.getInstance()
								.isCheckedLandUses()
						&& OpenTenureApplication.getInstance()
								.isCheckedLanguages()
						&& OpenTenureApplication.getInstance().isCheckedTypes()
						&& OpenTenureApplication.getInstance()
								.isCheckedGeometryRequired()

				) {

					OpenTenureApplication.getInstance().setInitialized(true);

					Configuration conf = Configuration
							.getConfigurationByName("isInitialized");
					conf.setValue("true");
					conf.update();

					FragmentActivity fa = (FragmentActivity) OpenTenureApplication
							.getNewsFragment();
					if (fa != null)
						fa.invalidateOptionsMenu();

					Configuration latitude = Configuration
							.getConfigurationByName(MainMapFragment.MAIN_MAP_LATITUDE);
					if (latitude != null)
						latitude.delete();

					MainMapFragment mapFrag = OpenTenureApplication
							.getMapFragment();

					mapFrag.boundCameraToInterestArea();

				}
			}
		}
	}
}
