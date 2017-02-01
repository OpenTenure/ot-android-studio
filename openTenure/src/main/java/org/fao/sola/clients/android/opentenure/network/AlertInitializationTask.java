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
package org.fao.sola.clients.android.opentenure.network;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.OpenTenurePreferencesActivity;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.model.Configuration;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.view.View;

public class AlertInitializationTask extends
		AsyncTask<View, View, View> {

	private ProgressDialog dialog;

	public AlertInitializationTask(Activity activity) {

		this.dialog = new ProgressDialog(activity);

	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		// Setting the alert dialog
		dialog.setMessage(OpenTenureApplication.getContext().getResources()
				.getString(R.string.message_app_initializing));
		dialog.setTitle(R.string.message_title_app_initializing);
		dialog.show();

	}

	@Override
	protected View doInBackground(View... params) {

		View input = (View) params[0];

		int i = 0;
		while (i <= 80) {

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {

				e.printStackTrace();
			}

			i++;
		}

		return input;
	}

	protected void onPostExecute(final View input) {

		dialog.dismiss();

		System.out.println("Network error :" + OpenTenureApplication.getInstance().isNetworkError());
		
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(input
				.getContext());
		if (!Boolean.parseBoolean(Configuration.getConfigurationByName(
				"isInitialized").getValue())
				&& (OpenTenureApplication.getInstance().isOnline() && !OpenTenureApplication.getInstance().isNetworkError())) {

			alertDialog.setTitle(R.string.message_title_app_not_initialized);
			alertDialog.setMessage(OpenTenureApplication.getContext()
					.getResources()
					.getString(R.string.message_app_not_initialized));

		} else if (!Boolean.parseBoolean(Configuration.getConfigurationByName(
				"isInitialized").getValue())
				&& (!OpenTenureApplication.getInstance().isOnline() || OpenTenureApplication.getInstance().isNetworkError())) {

			alertDialog.setTitle(R.string.message_title_app_not_initialized);
			alertDialog.setMessage(OpenTenureApplication.getContext()
					.getResources()
					.getString(R.string.message_app_not_initialized_network));
		} else {
			alertDialog.setTitle(R.string.message_title_app_initialized);

			SharedPreferences OpenTenurePreferences = PreferenceManager
					.getDefaultSharedPreferences(OpenTenureApplication
							.getContext());

			if (!OpenTenurePreferences
					.getString(OpenTenurePreferencesActivity.CS_URL_PREF,
							OpenTenureApplication._DEFAULT_COMMUNITY_SERVER)
					.trim()
					.equals(OpenTenureApplication._DEFAULT_COMMUNITY_SERVER))
				alertDialog.setMessage(OpenTenureApplication.getContext()
						.getResources()
						.getString(R.string.message_app_initialized));
			else
				alertDialog.setMessage(OpenTenureApplication.getContext()
						.getResources()
						.getString(R.string.message_app_initialized_default));
		}

		alertDialog.setPositiveButton(R.string.confirm, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				return;

			}
		});

		alertDialog.show();

	}

}
