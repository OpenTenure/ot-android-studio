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

import org.fao.sola.clients.android.opentenure.AttachmentViewHolder;
import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.ViewHolder;
import org.fao.sola.clients.android.opentenure.model.AttachmentStatus;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.network.API.CommunityServerAPI;
import org.fao.sola.clients.android.opentenure.network.response.SaveAttachmentResponse;
import org.fao.sola.clients.android.opentenure.network.response.ViewHolderResponse;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class AddClaimantAttachmentTask extends
		AsyncTask<ViewHolderResponse, Void, ViewHolderResponse> {

	@Override
	protected ViewHolderResponse doInBackground(ViewHolderResponse... params) {
		String attachmentId = null;
		String claimId = null;
		ViewHolderResponse vhRes = params[0];
		try {
		
			
			SaveAttachmentResponse saveAttRes = (SaveAttachmentResponse) vhRes.getRes();
			attachmentId = saveAttRes.getAttachmentId();
			claimId = saveAttRes.getClaimId();
			
			saveAttRes = CommunityServerAPI.addClaimantAttachment(claimId,
					attachmentId);
			
			vhRes.setRes(saveAttRes);
			return vhRes;

		} catch (Throwable ex) {

			Log.d("CommunityServerAPI",
					"An error has occurred during add claimant attachment:"
							+ ex.getMessage());
			ex.printStackTrace();

			SaveAttachmentResponse saRes = new SaveAttachmentResponse();
			saRes.setClaimId(claimId);
			saRes.setAttachmentId(attachmentId);
			saRes.setHttpStatusCode(100);
			saRes.setMessage("");

			vhRes.setRes(saRes);
			
			return vhRes;
		}
	}

	@Override
	protected void onPostExecute(final ViewHolderResponse vhResponse) {

		Toast toast;
		
		
		
		SaveAttachmentResponse saRes =  (SaveAttachmentResponse) vhResponse.getRes();
		
		

		int status = saRes.getHttpStatusCode();

		switch (status) {

		case 200:

			toast = Toast.makeText(OpenTenureApplication.getContext(),
					R.string.message_added_attachment, Toast.LENGTH_LONG);
			toast.show();
			
			
			
			Claim claim = Claim.getClaim(saRes.getClaimId());
			claim.setStatus(ClaimStatus._UNMODERATED);
			claim.update();
			
			try {
				AttachmentViewHolder	 vh = (AttachmentViewHolder) vhResponse.getVh();
				
				vh.getBarAttachment().setVisibility(View.INVISIBLE);
				vh.getSendIcon().setVisibility(View.INVISIBLE);
				vh.getAttachmentStatus().setTextColor(OpenTenureApplication.getContext().getResources().getColor(
						R.color.status_unmoderated));
				vh.getAttachmentStatus().setText(AttachmentStatus._UPLOADED);
				vh.getAttachmentStatus().setVisibility(View.VISIBLE);
				vh.getRemoveIcon().setVisibility(View.INVISIBLE);
			} catch (ClassCastException e) {
				AttachmentViewHolder vh = (AttachmentViewHolder) vhResponse.getVh();
				
				vh.getBarAttachment().setVisibility(View.INVISIBLE);
				vh.getSend().setVisibility(View.INVISIBLE);
				vh.getStatus().setTextColor(OpenTenureApplication.getContext().getResources().getColor(
						R.color.status_unmoderated));
				vh.getStatus().setText(AttachmentStatus._UPLOADED);
				vh.getStatus().setVisibility(View.VISIBLE);
				//vh.getRemove().setVisibility(View.INVISIBLE);
			}
			
			

			break;
			
		case 400:

			toast = Toast.makeText(OpenTenureApplication.getContext(),
					saRes.getMessage(), Toast.LENGTH_LONG);
			toast.show();

			break;	

		case 100:
			toast = Toast.makeText(OpenTenureApplication.getContext(),
					R.string.message_adding_attachment_not_ok, Toast.LENGTH_LONG);
			toast.show();
			break;
		}
	}
}
