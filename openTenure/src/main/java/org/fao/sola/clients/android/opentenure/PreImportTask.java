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
import java.io.FileInputStream;

import java.io.IOException;
import java.io.InputStream;

import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.filesystem.ZipUtilities;

import org.fao.sola.clients.android.opentenure.filesystem.json.model.Claim;
import org.fao.sola.clients.android.opentenure.form.FormPayload;
import org.fao.sola.clients.android.opentenure.form.FormTemplate;
import org.fao.sola.clients.android.opentenure.model.SurveyFormTemplate;
import org.fao.sola.clients.android.opentenure.network.API.CommunityServerAPIUtilities;

import com.google.gson.Gson;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

/**
 * AsyncTask to prepare the import of zipped claim. Unzip the the claim and
 * verify the conditions before import
 * 
 * @author Gianluca
 * 
 */
public class PreImportTask extends AsyncTask<Object, Void, Object[]> {

	protected ProgressDialog progressDialog;
	private Context mContext;
	private File zip;
	private File claimFolder;
	private File importFolder;

	public PreImportTask(Context context) {
		super();
		mContext = context;

	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		progressDialog = ProgressDialog.show(mContext,
				mContext.getString(R.string.title_import),
				mContext.getString(R.string.message_claim_import_preparing),
				true, false);
	}

	@Override
	protected Object[] doInBackground(Object... params) {

		Object results[] = new Object[3];

		try {
			zip = (File) params[1];

			Log.d("PreImportTask", "Unzipping.... ");

			int result = ZipUtilities.UnzipFilesWithAESEncryption(
					(String) params[0], (File) params[1]);

			results[0] = result;

			progressDialog.dismiss();

		} catch (Exception e) {

			progressDialog.dismiss();
			Log.d("PreImportTask",
					"And error has occured unzipping the compressed claim : "
							+ e.getStackTrace());
			results[0] = 0;

			return results;
		}

		return results;

	}

	@Override
	protected void onPostExecute(Object[] params) {

		boolean isSameForm = true;

		importFolder = zip.getParentFile();
		if ((Integer) params[0] == 1) {

			/*
			 * The Claim is unzipped . Now check if the claim is already present
			 * and if the dynamic part is aligned
			 */

			try {

				zip.delete();

				File[] list = importFolder.listFiles();

				claimFolder = list[0];

				if (claimFolder.exists() && claimFolder.isDirectory()) {

					/* If the claim folder exist search for claim.json file */

					File claimJson = new File(claimFolder, "claim.json");

					if (claimJson.exists()) {

						InputStream is;

						is = new FileInputStream(claimJson);

						String json = CommunityServerAPIUtilities.Slurp(is,
								1024);

						Log.d("PreImportTask", "CLAIM JSON STRING " + json);

						/* Parsing the metadata file */

						Gson gson = new Gson();
						Claim claim = gson.fromJson(json, Claim.class);

						org.fao.sola.clients.android.opentenure.model.Claim claimDB = org.fao.sola.clients.android.opentenure.model.Claim
								.getClaim(claim.getId());

						System.out.println("serverUrl " + claim.getServerUrl());

						/* Check if claim is already in the client */
						if (claim.getServerUrl() != null) {

							String serverAddress = "";

							// Server ulr
							SharedPreferences preferences = PreferenceManager
									.getDefaultSharedPreferences(OpenTenureApplication
											.getContext());

							String serverUrl = preferences
									.getString(
											OpenTenurePreferencesActivity.CS_URL_PREF,
											OpenTenureApplication._DEFAULT_COMMUNITY_SERVER);

							if (!serverUrl.trim().equals(""))
								serverAddress = serverUrl.split("//")[1];

							if (!claim.getServerUrl().equals(serverAddress)) {

								FileSystemUtilities
										.deleteFilesInFolder(importFolder);

								Toast toast;

								String message = OpenTenureApplication
										.getContext()
										.getString(
												R.string.message_claim_import_claim_not_belongs_to_community);

								toast = Toast.makeText(
										OpenTenureApplication.getContext(),
										message, Toast.LENGTH_LONG);

								toast.show();

								return;
							}
						}
						/* Check if claim is already in the client */
						if (claimDB != null) {

							FileSystemUtilities
									.deleteFilesInFolder(importFolder);

							Toast toast;

							String message = OpenTenureApplication
									.getContext()
									.getString(
											R.string.message_claim_import_already_present);

							toast = Toast.makeText(
									OpenTenureApplication.getContext(),
									message, Toast.LENGTH_LONG);

							toast.show();

							return;
						} else {
							/*
							 * Check if the dynamic form is aligned with the one
							 * in the server
							 */
							FormTemplate formTemplate = SurveyFormTemplate
									.getDefaultSurveyFormTemplate();
							FormPayload originalFormPayload = new FormPayload(
									formTemplate);

							if (!claim
									.getDynamicForm()
									.getFormTemplateName()
									.equals(originalFormPayload
											.getFormTemplateName()))

								isSameForm = false;

						}

					}

				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				System.out.println("Exception e : " + e.getMessage());
			}

			/* The dialog to launch the final import process */
			AlertDialog.Builder metadataDialog = new AlertDialog.Builder(
					mContext);

			metadataDialog.setTitle(OpenTenureApplication.getContext()
					.getString(R.string.title_import));
			if (isSameForm)
				metadataDialog.setMessage(OpenTenureApplication.getContext()
						.getString(R.string.message_claim_ready_to_import));
			else
				metadataDialog
						.setMessage(OpenTenureApplication
								.getContext()
								.getString(
										R.string.message_claim_ready_to_import_no_dynamic));

			metadataDialog.setPositiveButton(R.string.confirm,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();

							new ImportTask(mContext).execute(claimFolder);

							return;

						}
					});

			metadataDialog.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {

							try {
								/*
								 * In case of cancel delete all files in import
								 * folder
								 */

								FileSystemUtilities
										.deleteFilesInFolder(importFolder);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								System.out.println("Error deleting files "
										+ e.getLocalizedMessage());
								e.printStackTrace();
							}
							return;
						}
					});

			metadataDialog.show();

		} else if ((Integer) params[0] == 2) {

			/* PAssword Error . Try Again */

			AlertDialog.Builder metadataDialog = new AlertDialog.Builder(
					mContext);

			metadataDialog.setTitle(OpenTenureApplication.getContext()
					.getString(R.string.message_claim_import_password_error));

			final EditText input = new EditText(mContext);

			input.setInputType(InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_VARIATION_PASSWORD);
			input.setTransformationMethod(PasswordTransformationMethod
					.getInstance());
			metadataDialog.setView(input);

			metadataDialog.setPositiveButton(R.string.confirm,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

							String password = input.getText().toString();
							dialog.dismiss();

							new PreImportTask(mContext).execute(password, zip);

							return;

						}
					});

			metadataDialog.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {

							try {
								/*
								 * If cancel operation delete all file in import
								 * folder
								 */


								FileSystemUtilities
										.deleteFilesInFolder(importFolder);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								System.out.println("Error deleting files "
										+ e.getLocalizedMessage());
								e.printStackTrace();
							}
							return;
						}
					});

			metadataDialog.show();

		} else if ((Integer) params[0] == 0) {

			/* Generic error */

			Toast toast;

			String message = "Error preparing import";

			toast = Toast.makeText(OpenTenureApplication.getContext(), message,
					Toast.LENGTH_LONG);
			toast.show();

			try {
				FileSystemUtilities.deleteFilesInFolder(importFolder);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Error deleting files "
						+ e.getLocalizedMessage());
				e.printStackTrace();
			}

		} else if ((Integer) params[0] == 3) {

			/* Zip file is not a compressed claim */

			Toast toast;

			String message = OpenTenureApplication.getContext().getString(
					R.string.message_claim_import_not_compressed_claim);

			Log.d("PreImportTask", "Zip file is not a compressed claim");

			toast = Toast.makeText(OpenTenureApplication.getContext(), message,
					Toast.LENGTH_LONG);
			toast.show();

			try {
				FileSystemUtilities.deleteFilesInFolder(importFolder);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("Error deleting files "
						+ e.getLocalizedMessage());
				e.printStackTrace();
			}

		}

	}

}
