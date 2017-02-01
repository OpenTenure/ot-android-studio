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
import org.fao.sola.clients.android.opentenure.filesystem.json.SaveZippedClaim;
import org.fao.sola.clients.android.opentenure.filesystem.json.model.Claim;
import org.fao.sola.clients.android.opentenure.network.API.CommunityServerAPIUtilities;

import com.google.gson.Gson;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * Async Task to import an unzipped claim into the application database moving
 * also the claim's file
 * 
 * @author Gianluca
 * 
 */
public class ImportTask extends AsyncTask<Object, Void, Object[]> {
	private Context mContext;
	private File claimFolder;
	private File importFolder;

	protected ProgressDialog progressDialog;

	public ImportTask(Context context) {
		super();
		mContext = context;

	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		progressDialog = ProgressDialog.show(mContext,
				mContext.getString(R.string.title_export),
				mContext.getString(R.string.message_export), true, false);

		importFolder = FileSystemUtilities.getImportFolder();
	}

	@Override
	protected Object[] doInBackground(Object... params) {
		// TODO Auto-generated method stub

		Claim claim = null;
		Object[] results = (Object[]) new Object[2];
		results[0] = false;

		try {

			claimFolder = (File) params[0];

			File claimJson = new File(claimFolder, "claim.json");
			if (claimJson.exists()) {

				InputStream is;

				is = new FileInputStream(claimJson);

				String json = CommunityServerAPIUtilities.Slurp(is, 1024);

				Log.d("ImportTask", "CLAIM JSON STRING " + json);

				/* Parsing the metadata file */

				Gson gson = new Gson();
				claim = gson.fromJson(json, Claim.class);

				/*
				 * Calling the import function passing the the instance of
				 * parsed json and the file pointer to the unzipped claim
				 */
				results[0] = SaveZippedClaim.save(claim, claimFolder);

				/* Cleaning the import folder */
				FileSystemUtilities.deleteFilesInFolder(importFolder);

			}

		} catch (Exception e) {

			results[0] = false;

			return results;
		}

		results[1] = claim;
		return results;
	}

	@Override
	protected void onPostExecute(Object[] params) {

		boolean result = (boolean) params[0];
		Claim claim = (Claim) params[1];

		if (result) {
			/* positive case */

			progressDialog.dismiss();

			LocalClaimsFragment frag = OpenTenureApplication
					.getLocalClaimsFragment();
			frag.refresh();

			Toast toast;
			String message = OpenTenureApplication.getContext().getString(
					R.string.message_claim_imported)
					+ " "
					+ org.fao.sola.clients.android.opentenure.model.Claim
							.getClaim(claim.getId()).getName();

			Log.d("ImportTask", message);

			toast = Toast.makeText(mContext, message, Toast.LENGTH_LONG);
			toast.show();

		} else {
			/* negative case */

			progressDialog.dismiss();

			try {
				FileSystemUtilities.deleteFilesInFolder(importFolder);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.d("ImportTask", "Error deleting files");
				e.printStackTrace();
			}

			Toast toast;

			String message = OpenTenureApplication.getContext().getString(
					R.string.message_claim_not_imported);

			Log.d("ImportTask", message);

			toast = Toast.makeText(mContext, message, Toast.LENGTH_LONG);
			toast.show();

		}

	}

}
