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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import org.fao.sola.clients.android.opentenure.AttachmentViewHolder;
import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.ViewHolder;
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.model.Attachment;
import org.fao.sola.clients.android.opentenure.model.AttachmentStatus;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.model.MD5;
import org.fao.sola.clients.android.opentenure.network.API.CommunityServerAPI;
import org.fao.sola.clients.android.opentenure.network.response.ApiResponse;
import org.fao.sola.clients.android.opentenure.network.response.UploadChunkPayload;
import org.fao.sola.clients.android.opentenure.network.response.UploadChunksResponse;
import org.fao.sola.clients.android.opentenure.network.response.ViewHolderResponse;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Here transfers one chunk at time. Return true if all the chunks of an
 * attachment are correctly transferred .
 */

public class UploadChunksTask extends AsyncTask<Object, ViewHolderResponse, ViewHolderResponse> {

	@Override
	protected ViewHolderResponse doInBackground(Object... params) {

		boolean success = false;
		DataInputStream dis = null;

		UploadChunksResponse upResponse = new UploadChunksResponse();
		ViewHolderResponse vhr = new ViewHolderResponse();

		try {

			Attachment attachment = Attachment.getAttachment((String) params[0]);

			File toTransfer = new File(attachment.getPath());

			FileInputStream fis = new FileInputStream(toTransfer);
			upResponse.setAttachmentId((String) params[0]);

			dis = new DataInputStream(fis);
			dis.skipBytes((int) attachment.getUploadedBytes());

			Integer startPosition = (int) attachment.getUploadedBytes();

			for (;;) {

				byte[] chunk = new byte[50000];

				int rsz = dis.read(chunk, 0, chunk.length);
				if (rsz > 0) {
					UploadChunkPayload payload = new UploadChunkPayload();

					if (rsz < 50000)
						chunk = Arrays.copyOfRange(chunk, 0, rsz);

					payload.setMd5(MD5.calculateMD5(chunk));

					payload.setAttachmentId((String) params[0]);
					payload.setClaimId(attachment.getClaimId());
					payload.setId(UUID.randomUUID().toString());
					payload.setSize((long) rsz);
					payload.setStartPosition(startPosition);

					startPosition = startPosition + rsz;

					Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls()
							.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
					String json = gson.toJson(payload);

					/***
					 * Calling the server.....
					 ***/

					ApiResponse res = CommunityServerAPI.uploadChunk(json, chunk);

					if (res.getHttpStatusCode() == 200) {

						attachment.updateUploadedBytes(startPosition);
						success = true;

						vhr.setRes(upResponse);
						vhr.setVh((AttachmentViewHolder) params[1]);

						publishProgress(vhr);

					}
					if (res.getHttpStatusCode() == 100) {
						success = false;
						break;
					}

					if (res.getHttpStatusCode() == 105) {
						success = false;
						break;
					}

				} else
					break;
			}

			upResponse.setSuccess(success);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (dis != null) {
				try {
					dis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		vhr.setRes(upResponse);
		vhr.setVh((ViewHolder) params[1]);

		return vhr;
	}

	/*
	 * 
	 * 
	 * (non-Javadoc)
	 * 
	 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
	 * 
	 * Publish the progress
	 */
	@Override
	protected void onProgressUpdate(ViewHolderResponse... holders) {

		ViewHolderResponse vhr = holders[0];
		UploadChunksResponse res = (UploadChunksResponse) vhr.getRes();
		Attachment att = Attachment.getAttachment(res.getAttachmentId());
		ProgressBar bar = ((AttachmentViewHolder) vhr.getVh()).getBarAttachment();
		TextView status = ((AttachmentViewHolder) vhr.getVh()).getAttachmentStatus();

		float factor = (float) att.getUploadedBytes() / att.getSize();
		int progress = (int) (factor * 100);

		if (status != null) {
			status.setText(att.getStatus() + ": " + progress + " %");
			status.setTextColor(OpenTenureApplication.getContext().getResources().getColor(R.color.status_created));
		}
		if (bar != null) {
			bar.setProgress(progress);
			bar.setProgress(progress);
		}
		super.onProgressUpdate(holders);

	}

	@Override
	protected void onPostExecute(final ViewHolderResponse vhr) {

		UploadChunksResponse res = (UploadChunksResponse) vhr.getRes();
		AttachmentViewHolder vh = (AttachmentViewHolder) vhr.getVh();
		Claim claim = null;

		if (res != null && res.getSuccess() != null && res.getSuccess()) {

			/*
			 * All the Chunk of the claim are uploaded . Call SaveAttachment to
			 * close the flow. There 's the risk of a infinite loop
			 */

			SaveAttachmentTask sat = new SaveAttachmentTask();
			sat.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, res.getAttachmentId(), vhr.getVh());

		} else {

			int progress;
			Attachment att;

			switch (res.getHttpStatusCode()) {
			case 100:

				/*
				 * 
				 * UnknowHostException
				 */

				att = Attachment.getAttachment(res.getAttachmentId());

				att.setStatus(AttachmentStatus._UPLOAD_INCOMPLETE);
				att.update();

				claim = Claim.getClaim(att.getClaimId());

				if (claim.getStatus().equals(ClaimStatus._UPLOADING)) {
					claim.setStatus(ClaimStatus._UPLOAD_INCOMPLETE);
					claim.update();
				} else if (claim.getStatus().equals(ClaimStatus._UPDATING)) {
					claim.setStatus(ClaimStatus._UPDATE_INCOMPLETE);
					claim.update();
				}

				float factor = (float) att.getUploadedBytes() / att.getSize();
				progress = (int) (factor * 100);

				if (vh.getBarAttachment() != null)
					vh.getBarAttachment().setProgress(progress);

				if (vh.getAttachmentStatus() != null) {
					vh.getAttachmentStatus().setText(
							OpenTenureApplication.getContext().getResources().getString(R.string.upload_error));
					vh.getAttachmentStatus().setTextColor(
							OpenTenureApplication.getContext().getResources().getColor(R.color.status_created));
					vh.getAttachmentStatus().setVisibility(View.VISIBLE);
					vh.getSend().setVisibility(View.VISIBLE);
				} else if (vh.getStatus() != null) {
					vh.getStatus().setText(
							OpenTenureApplication.getContext().getResources().getString(R.string.upload_error));
					vh.getStatus().setTextColor(
							OpenTenureApplication.getContext().getResources().getColor(R.color.status_created));
					vh.getStatus().setVisibility(View.VISIBLE);
					vh.getSend().setVisibility(View.VISIBLE);
				}

				if (vh.getBar() != null) {
					progress = FileSystemUtilities.getUploadProgress(claim.getClaimId(), claim.getStatus());
					vh.getBar().setProgress(progress);
				}
				// vh.getIconLocal().setVisibility(View.VISIBLE);
				// vh.getIconUnmoderated().setVisibility(View.GONE);

				break;

			case 105:

				att = Attachment.getAttachment(res.getAttachmentId());

				att.setStatus(AttachmentStatus._UPLOAD_ERROR);
				att.update();

				claim = Claim.getClaim(att.getClaimId());

				if (claim.getStatus().equals(ClaimStatus._UPLOADING)) {
					claim.setStatus(ClaimStatus._UPLOAD_ERROR);
					claim.update();
				} else if (claim.getStatus().equals(ClaimStatus._UPDATING)) {
					claim.setStatus(ClaimStatus._UPDATE_ERROR);
					claim.update();
				}

				if (vh.getAttachmentStatus() != null) {
					vh.getAttachmentStatus().setText(
							OpenTenureApplication.getContext().getResources().getString(R.string.upload_error));
					vh.getAttachmentStatus().setTextColor(
							OpenTenureApplication.getContext().getResources().getColor(R.color.status_created));
					vh.getAttachmentStatus().setVisibility(View.VISIBLE);
				} else if (vh.getStatus() != null) {
					vh.getStatus().setText(
							OpenTenureApplication.getContext().getResources().getString(R.string.upload_error));
					vh.getStatus().setTextColor(
							OpenTenureApplication.getContext().getResources().getColor(R.color.status_created));
					vh.getStatus().setVisibility(View.VISIBLE);
				}

				// vh.getIconLocal().setVisibility(View.VISIBLE);
				// vh.getIconUnmoderated().setVisibility(View.GONE);

				break;

			case 400:

				att = Attachment.getAttachment(res.getAttachmentId());

				att.setStatus(AttachmentStatus._UPLOAD_ERROR);
				att.update();

				claim = Claim.getClaim(att.getClaimId());

				if (claim.getStatus().equals(ClaimStatus._UPLOADING)) {
					claim.setStatus(ClaimStatus._UPLOAD_ERROR);
					claim.update();
				} else if (claim.getStatus().equals(ClaimStatus._UPDATING)) {
					claim.setStatus(ClaimStatus._UPDATE_ERROR);
					claim.update();
				}
				if (vh.getAttachmentStatus() != null) {
					vh.getAttachmentStatus().setText(
							OpenTenureApplication.getContext().getResources().getString(R.string.upload_error));
					vh.getAttachmentStatus().setTextColor(
							OpenTenureApplication.getContext().getResources().getColor(R.color.status_created));
					vh.getAttachmentStatus().setVisibility(View.VISIBLE);
				} else if (vh.getStatus() != null) {
					vh.getStatus().setText(
							OpenTenureApplication.getContext().getResources().getString(R.string.upload_error));
					vh.getStatus().setTextColor(
							OpenTenureApplication.getContext().getResources().getColor(R.color.status_created));
					vh.getStatus().setVisibility(View.VISIBLE);
				}

				// vh.getIconLocal().setVisibility(View.VISIBLE);
				// vh.getIconUnmoderated().setVisibility(View.GONE);

				break;

			case 404:

				att = Attachment.getAttachment(res.getAttachmentId());

				att.setStatus(AttachmentStatus._UPLOAD_ERROR);
				att.update();

				claim = Claim.getClaim(att.getClaimId());

				if (claim.getStatus().equals(ClaimStatus._UPLOADING)) {
					claim.setStatus(ClaimStatus._UPLOAD_ERROR);
					claim.update();
				} else if (claim.getStatus().equals(ClaimStatus._UPDATING)) {
					claim.setStatus(ClaimStatus._UPDATE_ERROR);
					claim.update();
				}
				if (vh.getAttachmentStatus() != null) {
					vh.getAttachmentStatus().setText(
							OpenTenureApplication.getContext().getResources().getString(R.string.upload_error));
					vh.getAttachmentStatus().setTextColor(
							OpenTenureApplication.getContext().getResources().getColor(R.color.status_created));
					vh.getAttachmentStatus().setVisibility(View.VISIBLE);
				} else if (vh.getStatus() != null) {
					vh.getStatus().setText(
							OpenTenureApplication.getContext().getResources().getString(R.string.upload_error));
					vh.getStatus().setTextColor(
							OpenTenureApplication.getContext().getResources().getColor(R.color.status_created));
					vh.getStatus().setVisibility(View.VISIBLE);
				}

				// vh.getIconLocal().setVisibility(View.VISIBLE);
				// vh.getIconUnmoderated().setVisibility(View.GONE);

				break;

			default:

				att = Attachment.getAttachment(res.getAttachmentId());

				att.setStatus(AttachmentStatus._UPLOAD_INCOMPLETE);
				att.update();

				claim = Claim.getClaim(att.getClaimId());

				if (claim.getStatus().equals(ClaimStatus._UPDATING)) {

					claim.setStatus(ClaimStatus._UPDATE_INCOMPLETE);
					claim.update();
				} else if (claim.getStatus().equals(ClaimStatus._UPLOADING)) {

					claim.setStatus(ClaimStatus._UPLOAD_INCOMPLETE);
					claim.update();
				}

				factor = (float) att.getUploadedBytes() / att.getSize();
				progress = (int) (factor * 100);

				if (vh.getBarAttachment() != null) {
					vh.getBarAttachment().setProgress(progress);
					vh.getBarAttachment().setVisibility(View.VISIBLE);
				}

				if (vh.getAttachmentStatus() != null) {
					vh.getAttachmentStatus().setText(
							OpenTenureApplication.getContext().getResources().getString(R.string.upload_incomplete)
									+ ": " + progress + " %");
					vh.getAttachmentStatus().setTextColor(
							OpenTenureApplication.getContext().getResources().getColor(R.color.status_created));
					vh.getAttachmentStatus().setVisibility(View.VISIBLE);
					vh.getSend().setVisibility(View.VISIBLE);
				} else if (vh.getStatus() != null) {
					progress = FileSystemUtilities.getUploadProgress(claim.getClaimId(), claim.getStatus());
					vh.getStatus().setText(
							OpenTenureApplication.getContext().getResources().getString(R.string.upload_incomplete)
									+ ": " + progress + " %");
					vh.getStatus().setTextColor(
							OpenTenureApplication.getContext().getResources().getColor(R.color.status_created));
					vh.getStatus().setVisibility(View.VISIBLE);
					vh.getSend().setVisibility(View.VISIBLE);
				}

				if (vh.getBar() != null) {
					progress = FileSystemUtilities.getUploadProgress(claim.getClaimId(), claim.getStatus());
					vh.getBar().setProgress(progress);
				}
				// vh.getIconLocal().setVisibility(View.VISIBLE);
				// vh.getIconUnmoderated().setVisibility(View.GONE);

				break;
			}

		}

	}

}
