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
import org.fao.sola.clients.android.opentenure.network.UpdateClaimTypesTask;
import org.fao.sola.clients.android.opentenure.network.UpdateCommunityArea;
import org.fao.sola.clients.android.opentenure.network.UpdateDocumentTypesTask;
import org.fao.sola.clients.android.opentenure.network.UpdateIdTypesTask;
import org.fao.sola.clients.android.opentenure.network.UpdateLandUsesTask;
import org.fao.sola.clients.android.opentenure.network.UpdateLanguagesTask;
import org.fao.sola.clients.android.opentenure.network.UpdateParcelGeoRequiredTask;
import org.fao.sola.clients.android.opentenure.network.API.CommunityServerAPI;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
		// Create it, if it doesn't exist
		db.init();
		// Try to open it
		db.open();
		if (!db.isOpen()) {
			// We failed so we ask for a password
			AlertDialog.Builder dbPasswordDialog = new AlertDialog.Builder(this);
			dbPasswordDialog.setTitle(R.string.message_db_locked);
			final EditText dbPasswordInput = new EditText(this);
			dbPasswordInput.setInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_VARIATION_PASSWORD);
			dbPasswordDialog.setView(dbPasswordInput);
			dbPasswordDialog.setMessage(getResources().getString(
					R.string.message_db_password));

			dbPasswordDialog.setPositiveButton(R.string.confirm,
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							db.setPassword(dbPasswordInput.getText().toString());
							db.open();
							if (db.isOpen()) {
								checkPerformDbUpgrades();
								createInitializationConfig();
								Log.d(this.getClass().getName(), "db opened");
								StartOpenTenure start = new StartOpenTenure(getBaseContext());
								start.execute();
							} else {
								Log.d(this.getClass().getName(),
										"db is still closed");
								finish();
							}
						}
					});
			dbPasswordDialog.setNegativeButton(R.string.cancel,
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Log.d(this.getClass().getName(),
									"password not provided");
							finish();
						}
					});
			dbPasswordDialog.show();
		} else {
			Log.d(this.getClass().getName(), "db not encrypted");
			checkPerformDbUpgrades();
			createInitializationConfig();
			StartOpenTenure start = new StartOpenTenure(this);
			start.execute();
		}

	}
	
	private void createInitializationConfig(){
		Configuration conf = Configuration
				.getConfigurationByName("isInitialized");
		if (conf == null) {

			conf = new Configuration();
			conf.setName("isInitialized");
			conf.setValue("false");
			conf.create();
		}
	}

	private void checkPerformDbUpgrades() {
		// Check for pending upgrades
		if (OpenTenureApplication.getInstance().getDatabase().getUpgradePath()
				.size() > 0) {
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

			if (Boolean.parseBoolean(Configuration.getConfigurationByName(
					"isInitialized").getValue())) {

				Log.d(this.getClass().getName(),
						"starting tasks for static data download");

				if (!OpenTenureApplication.getInstance().isCheckedTypes()) {
					Log.d(this.getClass().getName(),
							"starting tasks for claim type download");

					UpdateClaimTypesTask updateCT = new UpdateClaimTypesTask();
					updateCT.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

				}

				if (!OpenTenureApplication.getInstance().isCheckedDocTypes()) {
					Log.d(this.getClass().getName(),
							"starting tasks for document type download");

					UpdateDocumentTypesTask updateCT = new UpdateDocumentTypesTask();
					updateCT.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

				}

				if (!OpenTenureApplication.getInstance().isCheckedIdTypes()) {
					Log.d(this.getClass().getName(),
							"starting tasks for ID type download");

					UpdateIdTypesTask updateIdType = new UpdateIdTypesTask();
					updateIdType
							.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}
				if (!OpenTenureApplication.getInstance().isCheckedLandUses()) {
					Log.d(this.getClass().getName(),
							"starting tasks for land use type download");

					UpdateLandUsesTask updateLu = new UpdateLandUsesTask();
					updateLu.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}
				if (!OpenTenureApplication.getInstance().isCheckedLanguages()) {
					Log.d(this.getClass().getName(),
							"starting tasks for languages download");

					UpdateLanguagesTask updateLang = new UpdateLanguagesTask();
					updateLang.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}
				if (!OpenTenureApplication.getInstance()
						.isCheckedCommunityArea()) {
					Log.d(this.getClass().getName(),
							"starting tasks for community area download");

					UpdateCommunityArea updateArea = new UpdateCommunityArea();
					updateArea
							.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}
				
				if (!OpenTenureApplication.getInstance().isCheckedGeometryRequired()) {
					Log.d(this.getClass().getName(),
							"starting tasks for parcel geometry setting download");

					UpdateParcelGeoRequiredTask updateGeo = new UpdateParcelGeoRequiredTask();
					updateGeo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}
				
				if (!OpenTenureApplication.getInstance().isCheckedForm()) {
					Log.d(this.getClass().getName(),
							"starting tasks for form retrieval");

					FormRetriever formRetriever = new FormRetriever(context);
					formRetriever
							.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}
			}
			
			int setted = Claim.resetClaimUploading();
			
			System.out.println("Claim cambiati in created : " + setted);
			
			
			String serverProtoVersion = CommunityServerAPI.getServerProtoVersion();
			String expectedProtoVersion = Configuration.getConfigurationValue(Configuration.PROTOVERSION_NAME);
			Toast toast;

			if(expectedProtoVersion != null && serverProtoVersion != null){

				if(expectedProtoVersion.compareTo(serverProtoVersion) > 0){
					toast = Toast.makeText(getBaseContext(),
							R.string.message_update_server, Toast.LENGTH_LONG);
					toast.show();
				}else if(expectedProtoVersion.compareTo(serverProtoVersion) < 0){
					toast = Toast.makeText(getBaseContext(),
							R.string.message_update_client, Toast.LENGTH_LONG);
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
