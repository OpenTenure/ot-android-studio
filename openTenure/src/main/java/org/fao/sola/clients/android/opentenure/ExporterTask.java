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

import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.filesystem.ZipUtilities;
import org.fao.sola.clients.android.opentenure.filesystem.json.JsonUtilities;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.Person;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class ExporterTask extends AsyncTask<String, Void, String[]> {

	protected ProgressDialog progressDialog;
	private Context mContext;

	public ExporterTask(Context context) {
		super();
		mContext = context;

	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		progressDialog = ProgressDialog.show(mContext,
				mContext.getString(R.string.title_export),
				mContext.getString(R.string.message_export), true, false);
	}

	@Override
	protected String[] doInBackground(String... params) {

		try {
			String claimId = (String) params[1];

			FileSystemUtilities.deleteCompressedClaim((String) params[1]);

			Person person = Claim.getClaim(claimId).getPerson();

			// Here the claimant picture is added as attachment just before to
			// submit claim

			person.addPersonPictureAsAttachment(claimId);
			File claimantFolder = FileSystemUtilities.getClaimantFolder(person
					.getPersonId());
			File image = new File(claimantFolder, person.getPersonId() + ".jpg");

			if (image.exists()) {

				FileSystemUtilities.copyFileInAttachFolder(claimId, image);
			}

			// Json file creation
			JsonUtilities.createClaimJson(claimId);

			// Creation of zip file from Claim folder
			ZipUtilities.AddFilesWithAESEncryption((String) params[0],
					(String) params[1]);

			progressDialog.dismiss();
			params[0] = "true";

			return params;
		} catch (Exception e) {
			Log.d("ExporterTask",
					"And error has occured creating the compressed claim ");
			params[0] = "false";
			return params;
		}

	}

	@Override
	protected void onPostExecute(String[] params) {

		if (Boolean.parseBoolean(params[0])) {

			Toast toast;
			String message = "";
			if (!OpenTenureApplication.getInstance().isKhmer()) {
				message = String.format(OpenTenureApplication.getContext()
						.getString(R.string.message_claim_exported, Claim.getClaim((String)params[1]).getName()));
			} else {
				message = OpenTenureApplication.getContext().getString(
						R.string.message_claim_exported)
						+ " " + Claim.getClaim((String)params[1]).getName();

			}
			toast = Toast.makeText(OpenTenureApplication.getContext(), message,
					Toast.LENGTH_LONG);
			toast.show();

		} else {

			Toast toast;

			String message = String
					.format(OpenTenureApplication.getContext().getString(
							R.string.message_claim_exported_error, Claim.getClaim((String)params[1]).getName()));

			toast = Toast.makeText(OpenTenureApplication.getContext(), message,
					Toast.LENGTH_LONG);
			toast.show();

		}

	}

}
