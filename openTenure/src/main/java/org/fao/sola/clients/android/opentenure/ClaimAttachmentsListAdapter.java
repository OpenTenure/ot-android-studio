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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.fao.sola.clients.android.opentenure.button.listener.DownloadAttachmentListener;
import org.fao.sola.clients.android.opentenure.button.listener.UpdateAttachmentListener;
import org.fao.sola.clients.android.opentenure.button.listener.UploadAttachmentListener;
import org.fao.sola.clients.android.opentenure.model.Attachment;
import org.fao.sola.clients.android.opentenure.model.AttachmentStatus;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.model.DocumentType;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ClaimAttachmentsListAdapter extends ArrayAdapter<String> {
	private final Context context;
	private LayoutInflater inflater;
	private final List<String> slogans;
	private final List<String> ids;
	private String claimId;
	private boolean readOnly;
	private Map<String, String> keyValueDocTypes;
	private Map<String, String> valueKeyDocTypes;
	boolean onlyActive ;
	
	public ClaimAttachmentsListAdapter(Context context, List<String> slogans,
			List<String> ids, String claimId, boolean readOnly) {
		super(context, R.layout.claim_attachments_list_item, slogans);
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.context = context;
		this.slogans = slogans;
		this.ids = ids;
		this.claimId = claimId;
		this.readOnly = readOnly;
		
		
		if (claimId != null) {
			Claim claim = Claim.getClaim(this.claimId);
				
			onlyActive =  (!claim.getStatus()
								.equals(ClaimStatus._REVIEWED) &&  !claim.getStatus().equals(ClaimStatus._MODERATED) && !claim.getStatus().equals(ClaimStatus._REJECTED));
			
			}
		else onlyActive = true;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		AttachmentViewHolder vh;

		String props[] = slogans.get(position).trim().split("-");
		
		

		if (convertView == null) {
			convertView = inflater.inflate(
					R.layout.claim_attachments_list_item, parent, false);
			vh = new AttachmentViewHolder();

			vh.id = (TextView) convertView.findViewById(R.id.attachment_id);
			vh.slogan = (TextView) convertView
					.findViewById(R.id.attachment_description);
			vh.attachmentFileType = (TextView) convertView
					.findViewById(R.id.attachment_file_type);
			vh.attachmentType = (Spinner) convertView
					.findViewById(R.id.attachment_type);

			// Attachment Type Spinner set up
			DocumentType dt = new DocumentType();
			
			keyValueDocTypes = dt.getKeyValueMap(OpenTenureApplication
					.getInstance().getLocalization(),onlyActive);
			valueKeyDocTypes = dt.getValueKeyMap(OpenTenureApplication
					.getInstance().getLocalization(),onlyActive);
			
			
			final Spinner spinner = (Spinner) convertView
					.findViewById(R.id.documentTypesSpinner);
			

			List<String> list = new ArrayList<String>();
			TreeSet<String> keys = new TreeSet<String>(keyValueDocTypes.keySet());
			for (String key : keys) {
				String value = keyValueDocTypes.get(key);
				list.add(value);
				// do something
			}

			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(
					OpenTenureApplication.getContext(), R.layout.my_spinner,
					list) {
			};
			dataAdapter.setDropDownViewResource(R.layout.my_spinner);

			vh.attachmentType.setAdapter(dataAdapter);

			vh.attachmentType.setSelection(dt.getIndexByCodeType(props[1]
					.trim(),onlyActive));

			// **********************

			vh.barAttachment = (ProgressBar) convertView
					.findViewById(R.id.progress_bar_attachment);
			vh.attachmentStatus = (TextView) convertView
					.findViewById(R.id.attachment_status);
			vh.downloadIcon = (ImageView) convertView
					.findViewById(R.id.download_file);
			vh.saveIcon = (ImageView) convertView
					.findViewById(R.id.update_attachment);
			vh.removeIcon = (ImageView) convertView
					.findViewById(R.id.remove_icon);
			vh.sendIcon = (ImageView) convertView
					.findViewById(R.id.action_submit_attachment);
			vh.clickableArea = (LinearLayout) convertView
					.findViewById(R.id.clickable_area);
			vh.clickableArea2 = (LinearLayout) convertView
					.findViewById(R.id.clickable_area2);
			convertView.setTag(vh);
		} else {
			vh = (AttachmentViewHolder) convertView.getTag();
		}

		vh.slogan.setText(props[0].trim());
		vh.attachmentFileType.setText(props[2].trim());
		vh.id.setTextSize(8);
		vh.id.setText(ids.get(position));

		String attachmentId = vh.id.getText().toString();
		final Attachment att = Attachment.getAttachment(attachmentId);

		if (att.getStatus().equals(AttachmentStatus._UPLOADED)) {
			vh.attachmentStatus.setText(OpenTenureApplication.getContext()
					.getResources().getString(R.string.uploaded));
			vh.attachmentStatus.setTextColor(context.getResources().getColor(
					R.color.status_unmoderated));
			vh.barAttachment.setVisibility(View.GONE);
			vh.getSendIcon().setVisibility(View.INVISIBLE);
		} else if (att.getStatus().equals(AttachmentStatus._UPLOADING)) {

			vh.barAttachment.setVisibility(View.VISIBLE);
			vh.getSendIcon().setVisibility(View.INVISIBLE);
			/* Setting progress barAttachment */
			float factor = (float) ((float) att.getUploadedBytes() / att
					.getSize());
			int progress = (int) (factor * 100);
			vh.barAttachment.setProgress(progress);

			vh.attachmentStatus.setText(OpenTenureApplication.getContext()
					.getResources().getString(R.string.uploading)
					+ " :" + progress + "%");
			vh.attachmentStatus.setTextColor(context.getResources().getColor(
					R.color.status_created));
		} else if (att.getStatus().equals(AttachmentStatus._CREATED)) {
			vh.attachmentStatus.setText(OpenTenureApplication.getContext()
					.getResources().getString(R.string.created));
			vh.attachmentStatus.setTextColor(context.getResources().getColor(
					R.color.status_created));
			vh.barAttachment.setVisibility(View.GONE);
			vh.removeIcon.setVisibility(View.VISIBLE);
			vh.saveIcon.setVisibility(View.VISIBLE);

		} else if (att.getStatus().equals(AttachmentStatus._DOWNLOAD_FAILED)) {
			vh.attachmentStatus.setText(OpenTenureApplication.getContext()
					.getResources().getString(R.string.download_failed));
			vh.attachmentStatus.setTextColor(context.getResources().getColor(
					R.color.status_challenged));
			vh.barAttachment.setVisibility(View.GONE);
			vh.getSendIcon().setVisibility(View.INVISIBLE);
		} else if (att.getStatus().equals(AttachmentStatus._DOWNLOADING)) {
			vh.attachmentStatus.setText(OpenTenureApplication.getContext()
					.getResources().getString(R.string.downloading));
			vh.attachmentStatus.setTextColor(context.getResources().getColor(
					R.color.status_created));
			vh.barAttachment.setVisibility(View.VISIBLE);
			vh.getSendIcon().setVisibility(View.INVISIBLE);
			/* Setting progress barAttachment */
			float factor = (float) ((float) att.getDownloadedBytes() / att
					.getSize());

			int progress = (int) (factor * 100);

			vh.barAttachment.setProgress(progress);
		} else if (att.getStatus()
				.equals(AttachmentStatus._DOWNLOAD_INCOMPLETE)) {
			vh.attachmentStatus.setText(OpenTenureApplication.getContext()
					.getResources().getString(R.string.download_incomplete));
			vh.attachmentStatus.setTextColor(context.getResources().getColor(
					R.color.status_created));
			vh.barAttachment.setVisibility(View.GONE);
			vh.getSendIcon().setVisibility(View.INVISIBLE);
		} else if (att.getStatus().equals(AttachmentStatus._UPLOAD_INCOMPLETE)) {
			vh.attachmentStatus.setText(OpenTenureApplication.getContext()
					.getResources().getString(R.string.upload_incomplete));
			vh.attachmentStatus.setTextColor(context.getResources().getColor(
					R.color.status_created));
			vh.barAttachment.setVisibility(View.GONE);
		} else if (att.getStatus().equals(AttachmentStatus._UPLOAD_ERROR)) {
			vh.attachmentStatus.setText(OpenTenureApplication.getContext()
					.getResources().getString(R.string.upload_error));
			vh.attachmentStatus.setTextColor(context.getResources().getColor(
					R.color.status_challenged));
			vh.barAttachment.setVisibility(View.GONE);
			vh.saveIcon.setVisibility(View.VISIBLE);
		}

		if ((!readOnly || att.getStatus().equals(AttachmentStatus._CREATED)
				|| att.getStatus().equals(AttachmentStatus._UPLOAD_INCOMPLETE)
				|| att.getStatus().equals(AttachmentStatus._UPLOADED) && new File(att.getPath()).exists())) {
			vh.clickableArea.setVisibility(View.VISIBLE);
			vh.clickableArea2.setVisibility(View.VISIBLE);

			vh.clickableArea.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					try {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setDataAndType(
								Uri.parse("file://" + att.getPath()),
								att.getMimeType());
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						OpenTenureApplication.getDocumentsFragment()
								.startActivity(intent);
					} catch (ActivityNotFoundException e) {

						Log.d(this.getClass().getName(),
								"No Activity Found Exception to handle :"
										+ att.getFileName());

						Toast.makeText(
								OpenTenureApplication.getContext(),
								OpenTenureApplication
										.getContext()
										.getResources()
										.getString(
												R.string.message_no_application)
										+ " " + att.getFileName(),
								Toast.LENGTH_LONG).show();

						e.getMessage();
					}

					catch (Throwable t) {

						Log.d(this.getClass().getName(), "Error opening :"
								+ att.getFileName());

						Toast.makeText(
								OpenTenureApplication.getContext(),
								OpenTenureApplication
										.getContext()
										.getResources()
										.getString(
												R.string.message_error_opening_file),
								Toast.LENGTH_LONG).show();
					}
				}
			});

			vh.clickableArea2.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					try {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setDataAndType(
								Uri.parse("file://" + att.getPath()),
								att.getMimeType());
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						OpenTenureApplication.getDocumentsFragment()
								.startActivity(intent);
					} catch (ActivityNotFoundException e) {

						Log.d(this.getClass().getName(),
								"No Activity Found Exception to handle :"
										+ att.getFileName());

						Toast.makeText(
								OpenTenureApplication.getContext(),
								OpenTenureApplication
										.getContext()
										.getResources()
										.getString(
												R.string.message_no_application)
										+ " " + att.getFileName(),
								Toast.LENGTH_LONG).show();

						e.getMessage();
					}

					catch (Throwable t) {

						Log.d(this.getClass().getName(), "Error opening :"
								+ att.getFileName());

						Toast.makeText(
								OpenTenureApplication.getContext(),
								OpenTenureApplication
										.getContext()
										.getResources()
										.getString(
												R.string.message_error_opening_file),
								Toast.LENGTH_LONG).show();
					}
				}
			});
		}
		if (!readOnly || att.getStatus().equals(AttachmentStatus._UPLOAD_ERROR)
				|| att.getStatus().equals(AttachmentStatus._CREATED)
				|| att.getStatus().equals(AttachmentStatus._UPLOAD_INCOMPLETE)
				|| att.getStatus().equals(AttachmentStatus._UPLOADED)) {

			vh.removeIcon.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					AlertDialog.Builder confirmNewPasswordDialog = new AlertDialog.Builder(
							context);
					confirmNewPasswordDialog
							.setTitle(R.string.action_remove_attachment);
					confirmNewPasswordDialog.setMessage(slogans.get(position)
							+ ": "
							+ context.getResources().getString(
									R.string.message_remove_attachment));

					confirmNewPasswordDialog.setPositiveButton(
							R.string.confirm,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Attachment.getAttachment(ids.get(position))
											.delete();
									Toast.makeText(context,
											R.string.attachment_removed,
											Toast.LENGTH_SHORT).show();
									slogans.remove(position);
									ids.remove(position);
									notifyDataSetChanged();
								}
							});
					confirmNewPasswordDialog.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							});

					confirmNewPasswordDialog.show();

				}
			});

			vh.getRemoveIcon().setVisibility(View.VISIBLE);
		} else {

			((ViewManager) convertView).removeView(vh.removeIcon);

			vh.slogan.setFocusable(false);
			vh.attachmentType.setClickable(false);
			vh.attachmentType.setFocusable(false);

		}

		if (!readOnly || att.getStatus().equals(AttachmentStatus._UPLOAD_ERROR)
				|| att.getStatus().equals(AttachmentStatus._CREATED)) {

			vh.saveIcon
					.setOnClickListener(new UpdateAttachmentListener(att, vh));
		}

		Claim claim = Claim.getClaim(claimId);

		if ((!claim.getStatus().equals(ClaimStatus._CREATED)
				&& !claim.getStatus().equals(ClaimStatus._UPLOADING) && !att
				.getStatus().equals(AttachmentStatus._DOWNLOADING))
				&& (att.getPath() == null || att.getPath().equals(""))) {

			vh.downloadIcon.setVisibility(View.VISIBLE);
		} else {

			vh.downloadIcon.setVisibility(View.INVISIBLE);
		}

		if ((!claim.getStatus().equals(ClaimStatus._CREATED)
				&& !claim.getStatus().equals(ClaimStatus._UPLOADING)
				&& !claim.getStatus().equals(ClaimStatus._UPLOAD_INCOMPLETE) && !claim
				.getStatus().equals(ClaimStatus._UPLOAD_ERROR))
				&& claim.isUploadable()) {

			if (att.getStatus().equals(AttachmentStatus._CREATED)
					|| att.getStatus().equals(AttachmentStatus._UPLOAD_ERROR)
					|| att.getStatus().equals(
							AttachmentStatus._UPLOAD_INCOMPLETE)) {
				vh.sendIcon.setVisibility(View.VISIBLE);
				vh.saveIcon.setVisibility(View.VISIBLE);
			}
		}

		vh.downloadIcon.setOnClickListener(new DownloadAttachmentListener(att,
				vh));

		vh.sendIcon.setOnClickListener(new UploadAttachmentListener(att, vh));
		vh.removeIcon.setVisibility(View.VISIBLE);

		return convertView;
	}
}