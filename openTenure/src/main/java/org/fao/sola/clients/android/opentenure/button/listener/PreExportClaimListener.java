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

import java.util.Iterator;
import java.util.List;

import org.fao.sola.clients.android.opentenure.ExporterTask;
import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.SharesListTO;
import org.fao.sola.clients.android.opentenure.model.Attachment;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ShareProperty;
import org.fao.sola.clients.android.opentenure.model.Vertex;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;

public class PreExportClaimListener implements OnClickListener {

	View view;
	String claimId;

	public PreExportClaimListener(String claimId) {

		this.claimId = claimId;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		boolean isMapCreated = true;
		boolean areSharesComplete = true;
		boolean areFilesPResent = true;
		int availableShares;
		String message = "";

		view = v;
		if (this.claimId != null) {

			Claim claim = Claim.getClaim(claimId);

			AlertDialog.Builder metadataDialog = new AlertDialog.Builder(
					v.getContext());

			/* Here checks what the claim is missing */

			/* First checks maps */
			List<Vertex> vertices = Vertex.getVertices(claimId);

			if (vertices.size() < 3) {
				isMapCreated = false;
			}

			availableShares = Claim.MAX_SHARES_PER_CLAIM;
			List<ShareProperty> shares = claim.getShares();
			for (Iterator iterator = shares.iterator(); iterator.hasNext();) {
				ShareProperty shareProperty = (ShareProperty) iterator.next();
				availableShares -= shareProperty.getShares();
			}

			
			if (availableShares > 0) {
				areSharesComplete = false;

			}

			List<Attachment> attachments = claim.getAttachments();
			for (Iterator iterator = attachments.iterator(); iterator.hasNext();) {
				Attachment attachment = (Attachment) iterator.next();
				if (attachment.getPath().equals(""))
					areFilesPResent = false;
			}


			metadataDialog.setTitle(OpenTenureApplication.getContext()
					.getString(R.string.title_export));

			if (!isMapCreated || !areSharesComplete || !areFilesPResent) {
				message = OpenTenureApplication.getContext().getString(
						R.string.message_warnings)
						+ "\n";
				
			}
			if (!isMapCreated) {
				message = message
						+ " - "
						+ OpenTenureApplication.getContext().getString(
								R.string.message_map_not_yet_draw) + "\n";
				
			}
			if (!areSharesComplete) {
				message = message
						+ " - "
						+ OpenTenureApplication.getContext().getString(
								R.string.message_available_shares) + "\n";
				
			}
			if (!areFilesPResent) {
				message = message
						+ " - "
						+ OpenTenureApplication.getContext().getString(
								R.string.message_files_not_present) + "\n";
				
			}

			metadataDialog.setMessage(message);
			metadataDialog.setPositiveButton(R.string.confirm,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();

							new PreExportClaimTask(view.getContext())
									.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,claimId);
							
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

		}
	}

}
