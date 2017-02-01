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
package org.fao.sola.clients.android.opentenure.button.listener;

import org.fao.sola.clients.android.opentenure.ExporterTask;

import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.network.GetAttachmentTask;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class PreExportClaimTask extends AsyncTask<Object, Void, Object[]> {

	Context mContext;
	View view;
	String claimId;
	protected ProgressDialog progressDialog;


	public PreExportClaimTask(Context context) {
		super();
		mContext = context;

	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		progressDialog = ProgressDialog.show(mContext,
				mContext.getString(R.string.title_export),mContext.getString(R.string.title_export), true, false);
	}



	@Override
	protected Object[] doInBackground(Object... params) {
		// TODO Auto-generated method stub
		
		claimId = (String) params[0];
		
		return params;

	}
	
	
	@Override
	protected void onPostExecute(Object[] params) {
		
		if (this.claimId != null) {
			
			progressDialog.dismiss();

			AlertDialog.Builder metadataDialog = new AlertDialog.Builder(
					mContext);

			metadataDialog.setTitle(R.string.password);

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

							ExporterTask task = new ExporterTask(mContext);
							
							String [] params = {password,claimId};							
							
							task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);							

							return;

						}
					});

			metadataDialog.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
							return;
						}
					});

			metadataDialog.show();

		} else {
			Toast toast = Toast.makeText(mContext,
					R.string.message_save_claim_before_submit,
					Toast.LENGTH_SHORT);
			toast.show();
		}
		
		return ;		
		
	}

}
