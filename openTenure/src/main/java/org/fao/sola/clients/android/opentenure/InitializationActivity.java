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

import org.fao.sola.clients.android.opentenure.form.server.FormRetriever;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.Configuration;
import org.fao.sola.clients.android.opentenure.model.Database;
import org.fao.sola.clients.android.opentenure.model.Task;
import org.fao.sola.clients.android.opentenure.network.API.CommunityServerAPI;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;

import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class InitializationActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.initialization_activity);
		OpenTenure.setLocale(this);

		final Database db = OpenTenureApplication.getInstance().getDatabase();
		// Init database
		Context ctx = this;
		Runnable okRun = new Runnable() {
			@Override
			public void run() {
				Log.d(this.getClass().getName(), "db not encrypted");
				checkPerformDbUpgrades();
				StartOpenTenure start = new StartOpenTenure(ctx);
				start.execute();
			}
		};
		Runnable cancelRun = new Runnable() {
			@Override
			public void run() {
				Log.d(this.getClass().getName(), "db is still closed");
				finish();
			}
		};

		db.unlock(this, okRun, cancelRun);
	}

	private void checkPerformDbUpgrades() {
		// Check for pending upgrades
		if (OpenTenureApplication.getInstance().getDatabase().getUpgradePath().size() > 0) {
			OpenTenureApplication.getInstance().getDatabase().performUpgrade();
			Log.d(this.getClass().getName(), "DB upgraded to version: "
					+ Configuration.getConfigurationValue("DBVERSION"));
		}
	}

	public class StartOpenTenure extends AsyncTask<Void, Void, Void> {

		private Context context;
		
		public StartOpenTenure(Context context){
			this.context = context;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			Log.d(this.getClass().getName(),
					"check to see if the application is initialized");
			
			OpenTenureApplication.getInstance().setNetworkError(false);
			Claim.resetClaimUploading();
			
			String serverProtoVersion = CommunityServerAPI.getServerProtoVersion();
			String expectedProtoVersion = Configuration.getConfigurationValue(Configuration.PROTOVERSION_NAME);
			Toast toast;

			if(expectedProtoVersion != null && serverProtoVersion != null){
				if(expectedProtoVersion.compareTo(serverProtoVersion) > 0){
					toast = Toast.makeText(getBaseContext(), R.string.message_update_server, Toast.LENGTH_LONG);
					toast.show();
				}else if(expectedProtoVersion.compareTo(serverProtoVersion) < 0){
					toast = Toast.makeText(getBaseContext(), R.string.message_update_client, Toast.LENGTH_LONG);
					toast.show();
				}
			}

			// Cleanup pending tiles download tasks in case of unclean shutdown
			Task.deleteAllTasks();
			Intent i = new Intent(InitializationActivity.this, OpenTenure.class);
			startActivity(i);
			finish();
		}
	}
}
