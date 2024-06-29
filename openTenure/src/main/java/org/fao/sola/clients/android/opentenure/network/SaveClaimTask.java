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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.fao.sola.clients.android.opentenure.AttachmentViewHolder;
import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.ViewHolder;
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.filesystem.json.JsonUtilities;
import org.fao.sola.clients.android.opentenure.model.AttachmentStatus;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.network.API.CommunityServerAPI;
import org.fao.sola.clients.android.opentenure.network.response.Attachment;
import org.fao.sola.clients.android.opentenure.network.response.SaveClaimResponse;
import org.fao.sola.clients.android.opentenure.network.response.ViewHolderResponse;

import android.view.View;
import android.os.AsyncTask;
import androidx.fragment.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

/**
 * The save Claim upload all the meta data about the Claim to the community
 * server, sending the claim.json . If the server is missing some claims for
 * that claim ,
 * 
 * **/
public class SaveClaimTask extends AsyncTask<Object, ViewHolderResponse, ViewHolderResponse> {

	@Override
	protected ViewHolderResponse doInBackground(Object... params) {
		String claimId = (String) params[0];
		ViewHolder vh = (ViewHolder) params[1];
		String json = FileSystemUtilities.getJsonClaim(claimId);

		SaveClaimResponse res = CommunityServerAPI.saveClaim(json);
		res.setClaimId(claimId);

		ViewHolderResponse vhr = new ViewHolderResponse();
		vhr.setRes(res);
		vhr.setVh(vh);

		return vhr;

	}

	protected void onPostExecute(final ViewHolderResponse vhr) {

		Toast toast;

		SaveClaimResponse res = (SaveClaimResponse) vhr.getRes();

		Claim claim = Claim.getClaim(res.getClaimId());

		switch (res.getHttpStatusCode()) {

		case 100: {
			/* UnknownHostException: */

			if (claim.getStatus().equals(ClaimStatus._CREATED)
					|| claim.getStatus().equals(ClaimStatus._UPLOAD_INCOMPLETE)
					|| claim.getStatus().equals(ClaimStatus._UPLOAD_ERROR)) {
				claim.setStatus(ClaimStatus._UPLOAD_INCOMPLETE);
				claim.update();

			}
			if (claim.getStatus().equals(ClaimStatus._UNMODERATED)
					|| claim.getStatus().equals(ClaimStatus._UPDATE_ERROR)
					|| claim.getStatus().equals(ClaimStatus._UPDATE_INCOMPLETE)) {

				claim.setStatus(ClaimStatus._UPDATE_INCOMPLETE);
				claim.update();

			}

			toast = Toast.makeText(
					OpenTenureApplication.getContext(),
					OpenTenureApplication.getContext().getResources()
							.getString(R.string.message_submission_error)
							+ "  "
							+ OpenTenureApplication
									.getContext()
									.getResources()
									.getString(
											R.string.message_connection_error),
					Toast.LENGTH_LONG);
			toast.show();

			ViewHolder vh = vhr.getVh();

			vh.getStatus().setText(
					OpenTenureApplication.getContext().getResources()
							.getString(R.string.update_incomplete));
			vh.getStatus().setVisibility(View.VISIBLE);

			break;

		}

		// User cannot access the project recorded for the claim
		case 460: {
			toast = Toast.makeText(OpenTenureApplication.getContext(),
					OpenTenureApplication.getContext().getResources()
							.getString(R.string.message_project_not_accessible_error), Toast.LENGTH_LONG);
			toast.show();
			break;
		}

		case 105: {
			/* IOException: */

			if (claim.getStatus().equals(ClaimStatus._CREATED)
					|| claim.getStatus().equals(ClaimStatus._UPLOAD_INCOMPLETE)
					|| claim.getStatus().equals(ClaimStatus._UPLOAD_ERROR)) {
				claim.setStatus(ClaimStatus._UPLOAD_ERROR);
				claim.update();

			}
			if (claim.getStatus().equals(ClaimStatus._UNMODERATED)
					|| claim.getStatus().equals(ClaimStatus._UPDATE_ERROR)
					|| claim.getStatus().equals(ClaimStatus._UPDATE_INCOMPLETE)) {
				claim.setStatus(ClaimStatus._UPDATE_ERROR);
				claim.update();

			}

			toast = Toast.makeText(OpenTenureApplication.getContext(),
					OpenTenureApplication.getContext().getResources()
							.getString(R.string.message_submission_error)
							+ " " + res.getMessage(), Toast.LENGTH_LONG);
			toast.show();

			ViewHolder vh = vhr.getVh();

			vh.getStatus().setText(
					OpenTenureApplication.getContext().getResources()
							.getString(R.string.update_error));
			vh.getStatus().setVisibility(View.VISIBLE);
			vh.getBar().setVisibility(View.GONE);

			break;

		}
		case 110: {

			if (claim.getStatus().equals(ClaimStatus._CREATED)
					|| claim.getStatus().equals(ClaimStatus._UPLOAD_INCOMPLETE)
					&& claim.getStatus().equals(ClaimStatus._UPLOAD_ERROR)) {
				claim.setStatus(ClaimStatus._UPLOAD_ERROR);
				claim.update();

			}
			if (claim.getStatus().equals(ClaimStatus._UNMODERATED)
					|| claim.getStatus().equals(ClaimStatus._UPDATE_ERROR)
					|| claim.getStatus().equals(ClaimStatus._UPDATE_INCOMPLETE)) {
				claim.setStatus(ClaimStatus._UPDATE_ERROR);
				claim.update();

			}

			toast = Toast.makeText(OpenTenureApplication.getContext(),
					OpenTenureApplication.getContext().getResources()
							.getString(R.string.message_submission_error)
							+ " " + res.getMessage(), Toast.LENGTH_LONG);
			toast.show();

			ViewHolder vh = vhr.getVh();

			vh.getStatus().setText(
					OpenTenureApplication.getContext().getResources()
							.getString(R.string.update_error));
			vh.getStatus().setVisibility(View.VISIBLE);
			vh.getBar().setVisibility(View.GONE);

			break;

		}

		case 200: {

			/* OK */

			try {

				TimeZone tz = TimeZone.getTimeZone("UTC");
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd'T'HH:mm:ss", Locale.US);
				sdf.setTimeZone(tz);
				Date date = sdf.parse(res.getChallengeExpiryDate());

				claim.setChallengeExpiryDate(new java.sql.Date(date.getTime()));
				claim.setClaimNumber(res.getNr());

				claim.setStatus(ClaimStatus._UNMODERATED);
				claim.setRecorderName(OpenTenureApplication.getUsername());
				claim.update();
				System.out.println("Challenging Exp Date : "
						+ claim.getChallengeExpiryDate());
			} catch (Exception e) {
				Log.d("CommunityServerAPI",
						"Error uploading the claim " + res.getMessage());
				e.printStackTrace();
			}

			toast = Toast.makeText(OpenTenureApplication.getContext(),
					OpenTenureApplication.getContext().getResources()
							.getString(R.string.message_submitted),
					Toast.LENGTH_LONG);
			toast.show();

			ViewHolder vh = vhr.getVh();

			vh.getNumber().setText(res.getNr());
			vh.getNumber().setVisibility(View.VISIBLE);

			vh.getBar().setVisibility(View.GONE);

			int days = JsonUtilities.remainingDays(claim
					.getChallengeExpiryDate());

			vh.getChallengeExpiryDate().setText(
					OpenTenureApplication.getContext().getResources()
							.getString(R.string.message_remaining_days)
							+ days);

			vh.getChallengeExpiryDate().setVisibility(View.VISIBLE);
			vh.getStatus().setVisibility(View.GONE);

			vh.getIconLocal().setVisibility(View.GONE);
			vh.getIconUnmoderated().setVisibility(View.VISIBLE);
			vh.getSend().setVisibility(View.INVISIBLE);
			break;
		}

		case 403: {
			/* Error Login */

			Log.d("CommunityServerAPI",
					"SAVE CLAIM JSON RESPONSE " + res.getMessage());

			toast = Toast
					.makeText(
							OpenTenureApplication.getContext(),
							OpenTenureApplication
									.getContext()
									.getResources()
									.getString(
											R.string.message_submission_error)
									+ " "
									+ res.getHttpStatusCode()
									+ "  "
									+ OpenTenureApplication
											.getContext()
											.getResources()
											.getString(
													R.string.message_login_no_more_valid),
							Toast.LENGTH_LONG);
			toast.show();

			OpenTenureApplication.setLoggedin(false);

			FragmentActivity fa = (FragmentActivity) OpenTenureApplication
					.getActivity();
			fa.invalidateOptionsMenu();

			ViewHolder vh = vhr.getVh();

			vh.getStatus().setVisibility(View.GONE);
			vh.getBar().setVisibility(View.GONE);

			break;
		}
		case 404: {

			/* Error Not Found */

			Log.d("CommunityServerAPI",
					"SAVE CLAIM JSON RESPONSE " + res.getMessage());

			toast = Toast
					.makeText(
							OpenTenureApplication.getContext(),
							OpenTenureApplication
									.getContext()
									.getResources()
									.getString(
											R.string.message_submission_error)
									+ " "
									+ OpenTenureApplication
											.getContext()
											.getResources()
											.getString(
													R.string.message_service_not_available),
							Toast.LENGTH_LONG);
			toast.show();

			ViewHolder vh = vhr.getVh();

			if (claim.getStatus().equals(ClaimStatus._CREATED)
					|| claim.getStatus().equals(ClaimStatus._UPLOAD_INCOMPLETE)
					|| claim.getStatus().equals(ClaimStatus._UPLOAD_ERROR)) {
				claim.setStatus(ClaimStatus._UPLOAD_ERROR);
				claim.update();

				vh.getStatus().setText(
						OpenTenureApplication.getContext().getResources()
								.getString(R.string.upload_error));
				vh.getStatus().setVisibility(View.VISIBLE);
				vh.getBar().setVisibility(View.GONE);

			}
			if (claim.getStatus().equals(ClaimStatus._UNMODERATED)
					|| claim.getStatus().equals(ClaimStatus._UPDATE_ERROR)
					|| claim.getStatus().equals(ClaimStatus._UPDATE_INCOMPLETE)) {
				claim.setStatus(ClaimStatus._UPDATE_ERROR);
				claim.update();

				vh.getStatus().setText(
						OpenTenureApplication.getContext().getResources()
								.getString(R.string.update_error));
				vh.getStatus().setVisibility(View.VISIBLE);
				vh.getBar().setVisibility(View.GONE);

			}

			break;
		}

		case 452: {

			/* Missing Attachments */

			if (claim.getStatus().equals(ClaimStatus._CREATED)
					|| claim.getStatus().equals(ClaimStatus._UPLOAD_INCOMPLETE)
					|| claim.getStatus().equals(ClaimStatus._UPLOAD_ERROR)) {
				claim.setStatus(ClaimStatus._UPLOADING);
				claim.update();

			}

			if (claim.getStatus().equals(ClaimStatus._UNMODERATED)
					|| claim.getStatus().equals(ClaimStatus._UPDATE_ERROR)
					|| claim.getStatus().equals(ClaimStatus._UPDATE_INCOMPLETE)) {
				claim.setStatus(ClaimStatus._UPDATING);
				claim.update();

			}
			toast = Toast.makeText(OpenTenureApplication.getContext(),
					OpenTenureApplication.getContext().getResources()
							.getString(R.string.message_uploading),
					Toast.LENGTH_LONG);
			toast.show();

			ViewHolder vh = vhr.getVh();
			vh.getSend().setVisibility(View.INVISIBLE);

			AttachmentViewHolder avh = new AttachmentViewHolder();

			avh.setBar(vh.getBar());
			avh.setChallengeExpiryDate(vh.getChallengeExpiryDate());
			avh.setIconChallenged(vh.getIconChallenged());
			avh.setIconLocal(vh.getIconLocal());
			avh.setIconModerated(vh.getIconModerated());
			avh.setIconReviewed(vh.getIconReviewed());
			avh.setIconUnmoderated(vh.getIconUnmoderated());
			avh.setIconWithdrawn(vh.getIconWithdrawn());

			avh.setId(vh.getId());
			avh.setNumber(vh.getNumber());
			avh.setPicture(vh.getPicture());
			avh.setPosition(vh.getPosition());
			avh.setRemove(vh.getRemove());
			avh.setSend(vh.getSend());
			avh.setSlogan(vh.getSlogan());
			avh.setStatus(vh.getStatus());

			int progress = FileSystemUtilities.getUploadProgress(
					claim.getClaimId(), claim.getStatus());

			if (claim.getStatus().equals(ClaimStatus._UNMODERATED))
				vh.getStatus().setText(
						OpenTenureApplication.getContext().getResources()
								.getString(R.string.updating)
								+ ": " + progress + " %");
			else
				vh.getStatus().setText(
						OpenTenureApplication.getContext().getResources()
								.getString(R.string.uploading)
								+ ": " + progress + " %");

			// This is missing attachments that have to be uploaded to the server
			List<Attachment> list = res.getAttachments();

			// Check existing claims attachments and if any not matching the list of missing ones, that means it was already uploaded.
			// This happens in the case of using existing claimant with photo, which was already uploaded to server in some other claim.
			// In such case we should mark it as uploaded
			for (Iterator<org.fao.sola.clients.android.opentenure.model.Attachment> iterator = claim.getAttachments().iterator(); iterator.hasNext();) {
				org.fao.sola.clients.android.opentenure.model.Attachment existingAttachment = (org.fao.sola.clients.android.opentenure.model.Attachment) iterator.next();
				boolean found = false;

				for (Iterator<Attachment> iterator2 = list.iterator(); iterator2.hasNext();) {
					Attachment missingAttachment = (Attachment) iterator2.next();
					if(existingAttachment.getAttachmentId().equalsIgnoreCase(missingAttachment.getId())){
						found = true;
						break;
					}
				}

				if(!found) {
					// Attachment exists on the server, mark it as downloaded
					existingAttachment.setStatus(AttachmentStatus._UPLOADED);
					org.fao.sola.clients.android.opentenure.model.Attachment.updateAttachment(existingAttachment);
				}
			}

			// Run saving
			for (Iterator<Attachment> iterator = list.iterator(); iterator.hasNext();) {
				Attachment attachment = (Attachment) iterator.next();
				SaveAttachmentTask saveAttachmentTask = new SaveAttachmentTask();
				saveAttachmentTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, attachment.getId(), avh);
			}

			break;
		}
		case 450: {

			Log.d("CommunityServerAPI",
					"SAVE CLAIM JSON RESPONSE " + res.getMessage());

			if (claim.getStatus().equals(ClaimStatus._CREATED)
					|| claim.getStatus().equals(ClaimStatus._UPLOADING)
					|| claim.getStatus().equals(ClaimStatus._UPLOAD_INCOMPLETE)
					|| claim.getStatus().equals(ClaimStatus._UPLOAD_ERROR)) {
				claim.setStatus(ClaimStatus._UPLOAD_ERROR);
				claim.update();

			} else {
				claim.setStatus(ClaimStatus._UPDATE_ERROR);
				claim.update();

			}

			toast = Toast.makeText(OpenTenureApplication.getContext(),
					OpenTenureApplication.getContext().getResources()
							.getString(R.string.message_submission_error)
							+ " ," + res.getMessage(), Toast.LENGTH_LONG);
			toast.show();

			ViewHolder vh = vhr.getVh();

			if (claim.getStatus().equals(ClaimStatus._UNMODERATED))
				vh.getStatus().setText(
						OpenTenureApplication.getContext().getResources()
								.getString(R.string.update_error));
			else
				vh.getStatus().setText(
						OpenTenureApplication.getContext().getResources()
								.getString(R.string.upload_error));

			vh.getStatus().setVisibility(View.VISIBLE);
			vh.getBar().setVisibility(View.GONE);

			break;
		}
		case 400: {

			Log.d("CommunityServerAPI",
					"SAVE CLAIM JSON RESPONSE " + res.getMessage());

			if (claim.getStatus().equals(ClaimStatus._CREATED)
					|| claim.getStatus().equals(ClaimStatus._UPLOADING)
					|| claim.getStatus().equals(ClaimStatus._UPLOAD_INCOMPLETE)
					|| claim.getStatus().equals(ClaimStatus._UPLOAD_ERROR)) {
				claim.setStatus(ClaimStatus._UPLOAD_ERROR);
				claim.update();

			} else {
				claim.setStatus(ClaimStatus._UPDATE_ERROR);
				claim.update();

			}

			toast = Toast.makeText(OpenTenureApplication.getContext(),
					OpenTenureApplication.getContext().getResources()
							.getString(R.string.message_submission_error)
							+ " ," + res.getMessage(), Toast.LENGTH_LONG);
			toast.show();

			ViewHolder vh = vhr.getVh();

			if (claim.getStatus().equals(ClaimStatus._UNMODERATED))
				vh.getStatus().setText(
						OpenTenureApplication.getContext().getResources()
								.getString(R.string.update_error));
			else
				vh.getStatus().setText(
						OpenTenureApplication.getContext().getResources()
								.getString(R.string.upload_error));
			vh.getStatus().setVisibility(View.VISIBLE);
			vh.getBar().setVisibility(View.GONE);
		}
			break;

		case 500:

			Log.d("CommunityServerAPI",
					"SAVE CLAIM JSON RESPONSE " + res.getMessage());

			if (claim.getStatus().equals(ClaimStatus._CREATED)
					|| claim.getStatus().equals(ClaimStatus._UPLOADING)
					|| claim.getStatus().equals(ClaimStatus._UPLOAD_INCOMPLETE)
					|| claim.getStatus().equals(ClaimStatus._UPLOAD_ERROR)) {
				claim.setStatus(ClaimStatus._UPLOAD_ERROR);
				claim.update();

			} else {
				claim.setStatus(ClaimStatus._UPDATE_ERROR);
				claim.update();

			}

			toast = Toast.makeText(OpenTenureApplication.getContext(),
					OpenTenureApplication.getContext().getResources()
							.getString(R.string.message_submission_error)
							+ " ," + res.getMessage(), Toast.LENGTH_LONG);
			toast.show();

			ViewHolder vh = vhr.getVh();

			if (claim.getStatus().equals(ClaimStatus._UNMODERATED))
				vh.getStatus().setText(
						OpenTenureApplication.getContext().getResources()
								.getString(R.string.update_error));
			else
				vh.getStatus().setText(
						OpenTenureApplication.getContext().getResources()
								.getString(R.string.upload_error));

			vh.getStatus().setVisibility(View.VISIBLE);
			vh.getBar().setVisibility(View.GONE);

		default:
			break;
		}

		return;

	}

}
