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

import org.fao.sola.clients.android.opentenure.AttachmentViewHolder;
import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.model.Attachment;
import org.fao.sola.clients.android.opentenure.network.GetAttachmentTask;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class DownloadAttachmentListener implements OnClickListener {

	AttachmentViewHolder vh;
	Attachment attachment;

	public DownloadAttachmentListener(Attachment att, AttachmentViewHolder vh) {

		this.vh = vh;
		this.attachment = att;

	}

	@Override
	public void onClick(final View v) {

		if(OpenTenureApplication.getInstance().isConnectedWifi(v.getContext())){
			downloadAttachment(v);
		}else{

			// Avoid to automatically download claims over mobile data
			AlertDialog.Builder confirmDownloadBuilder = new AlertDialog.Builder(
					v.getContext());
			confirmDownloadBuilder.setTitle(R.string.title_confirm_data_transfer);
			confirmDownloadBuilder.setMessage(v.getResources().getString(
					R.string.message_data_over_mobile));

			confirmDownloadBuilder.setPositiveButton(R.string.confirm,
					new android.content.DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							downloadAttachment(v);
						}
					});
			confirmDownloadBuilder.setNegativeButton(R.string.cancel,
					new android.content.DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
						}
					});

			final AlertDialog confirmDownloadDialog = confirmDownloadBuilder.create();
			confirmDownloadDialog.show();
		}
		

	}
	
	private void downloadAttachment(View v){
		Object[] params = new Object[2];
		params[0] = attachment;
		params[1] = vh;
		
		
		if (!OpenTenureApplication.isLoggedin()) {

			Toast toast = Toast.makeText(v.getContext(),
					R.string.message_login_before, Toast.LENGTH_SHORT);
			toast.show();
			return ;

		}
		
		vh.getBarAttachment().setVisibility(View.VISIBLE);
		vh.getAttachmentStatus().setVisibility(View.GONE);
		

		GetAttachmentTask task = new GetAttachmentTask();
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);

		Toast toast = Toast.makeText(OpenTenureApplication.getContext(),
				R.string.message_downloading_attachment, Toast.LENGTH_LONG);
		toast.show();
	}

}
