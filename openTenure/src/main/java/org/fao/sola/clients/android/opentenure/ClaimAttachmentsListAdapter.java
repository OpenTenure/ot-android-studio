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
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.fao.sola.clients.android.opentenure.button.listener.DownloadAttachmentListener;
import org.fao.sola.clients.android.opentenure.button.listener.UploadAttachmentListener;
import org.fao.sola.clients.android.opentenure.model.Attachment;
import org.fao.sola.clients.android.opentenure.model.AttachmentStatus;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.model.DocumentType;
import org.fao.sola.clients.android.opentenure.tools.StringUtility;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.AdapterView;
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
	private String claimId;
	private boolean readOnly;
	private Map<String, String> keyValueDocTypes;
	private List<String> slogans;
	private List<Attachment> attachments;
	boolean onlyActive ;
	
	public ClaimAttachmentsListAdapter(Context context, List<Attachment> attachments, List<String> slogans, String claimId, boolean readOnly) {
		super(context, R.layout.claim_attachments_list_item, slogans);
		this.slogans = slogans;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.context = context;
		this.attachments = attachments;
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
		Attachment attachment = attachments.get(position);

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.claim_attachments_list_item, parent, false);
			vh = new AttachmentViewHolder();

			vh.id = (TextView) convertView.findViewById(R.id.attachment_id);
			vh.slogan = (TextView) convertView.findViewById(R.id.attachment_description);
			//vh.fileIcon = (ImageView) convertView.findViewById(R.id.fileIcon);
			vh.attachmentType = (Spinner) convertView.findViewById(R.id.attachment_type);

			// Attachment Type Spinner set up
			DocumentType dt = new DocumentType();
			keyValueDocTypes = dt.getKeyValueMap(OpenTenureApplication.getInstance().getLocalization(),onlyActive);

			List<String> list = new ArrayList<String>();
			TreeSet<String> keys = new TreeSet<String>(keyValueDocTypes.keySet());
			for (String key : keys) {
				String value = keyValueDocTypes.get(key);
				list.add(value);
			}

			ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(
					OpenTenureApplication.getContext(), R.layout.my_spinner, list) {
			};
			dataAdapter.setDropDownViewResource(R.layout.my_spinner);

			vh.attachmentType.setAdapter(dataAdapter);
			vh.attachmentType.setSelection(dt.getIndexByCodeType(attachment.getFileType(),onlyActive));

			vh.barAttachment = (ProgressBar) convertView.findViewById(R.id.progress_bar_attachment);
			vh.attachmentStatus = (TextView) convertView.findViewById(R.id.attachment_status);
			vh.downloadIcon = (ImageView) convertView.findViewById(R.id.download_file);
			vh.removeIcon = (ImageView) convertView.findViewById(R.id.remove_icon);
			vh.sendIcon = (ImageView) convertView.findViewById(R.id.action_submit_attachment);
			vh.viewIcon = (ImageView) convertView.findViewById(R.id.viewFile);

			String fileName = attachment.getFileName();
			if(!StringUtility.isEmpty(fileName)) {
				// Check resource exists
				if (fileName.lastIndexOf(".") > 0 && fileName.lastIndexOf(".") < fileName.length()) {
					String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
					// Handle new MS Office extentions
					if(ext.equalsIgnoreCase("docx")){
						ext = "doc";
					}
					if(ext.equalsIgnoreCase("xls")){
						ext = "xls";
					}
					int imageId = context.getResources().getIdentifier(ext.toLowerCase(), "drawable", context.getPackageName());
					if(!StringUtility.isEmpty(ext) &&  imageId != 0){
						vh.slogan.setCompoundDrawablesRelativeWithIntrinsicBounds(context.getDrawable(imageId),null, null,null);
					}
				}
			}

			convertView.setTag(vh);
		} else {
			vh = (AttachmentViewHolder) convertView.getTag();
		}

		Claim claim = Claim.getClaim(claimId);
		boolean isEditable = !readOnly;
		boolean isForUpload = false;

		if ((!claim.getStatus().equals(ClaimStatus._CREATED)
				&& !claim.getStatus().equals(ClaimStatus._UPLOADING)
				&& !claim.getStatus().equals(ClaimStatus._UPLOAD_INCOMPLETE) && !claim.getStatus().equals(ClaimStatus._UPLOAD_ERROR))
				&& claim.isUploadable()) {

			if (attachment.getStatus().equals(AttachmentStatus._CREATED)
					|| attachment.getStatus().equals(AttachmentStatus._UPLOAD_ERROR)
					|| attachment.getStatus().equals(AttachmentStatus._UPLOAD_INCOMPLETE)) {
				isForUpload = true;
			}
		}

		if(!isEditable){
			// If not editable, check that this attachment is not newly created for further uploading
			isEditable = isForUpload;
		}

		if(!StringUtility.isEmpty(attachment.getDescription())){
			vh.slogan.setText(attachment.getDescription());
		} else {
			vh.slogan.setText(attachment.getFileType());
		}

		vh.id.setText(attachment.getAttachmentId());

		if (attachment.getStatus().equals(AttachmentStatus._UPLOADED)) {
			vh.attachmentStatus.setText(OpenTenureApplication.getContext()
					.getResources().getString(R.string.uploaded));
			vh.attachmentStatus.setTextColor(context.getResources().getColor(R.color.status_created));
			vh.barAttachment.setVisibility(View.GONE);
		} else if (attachment.getStatus().equals(AttachmentStatus._UPLOADING)) {
			vh.barAttachment.setVisibility(View.VISIBLE);
			vh.getSendIcon().setVisibility(View.GONE);
			/* Setting progress barAttachment */
			float factor = (float) ((float) attachment.getUploadedBytes() / attachment.getSize());
			int progress = (int) (factor * 100);
			vh.barAttachment.setProgress(progress);
			vh.attachmentStatus.setText(OpenTenureApplication.getContext()
					.getResources().getString(R.string.uploading) + " :" + progress + "%");
			vh.attachmentStatus.setTextColor(context.getResources().getColor(R.color.status_created));
		} else if (attachment.getStatus().equals(AttachmentStatus._CREATED)) {
			vh.attachmentStatus.setText(OpenTenureApplication.getContext()
					.getResources().getString(R.string.created));
			vh.attachmentStatus.setTextColor(context.getResources().getColor(R.color.status_created));
			vh.barAttachment.setVisibility(View.GONE);
		} else if (attachment.getStatus().equals(AttachmentStatus._DOWNLOAD_FAILED)) {
			vh.attachmentStatus.setText(OpenTenureApplication.getContext()
					.getResources().getString(R.string.download_failed));
			vh.attachmentStatus.setTextColor(context.getResources().getColor(R.color.status_challenged));
			vh.barAttachment.setVisibility(View.GONE);
		} else if (attachment.getStatus().equals(AttachmentStatus._DOWNLOADING)) {
			vh.attachmentStatus.setText(OpenTenureApplication.getContext()
					.getResources().getString(R.string.downloading));
			vh.attachmentStatus.setTextColor(context.getResources().getColor(R.color.status_created));
			vh.barAttachment.setVisibility(View.VISIBLE);
			/* Setting progress barAttachment */
			float factor = (float) ((float) attachment.getDownloadedBytes() / attachment.getSize());
			int progress = (int) (factor * 100);
			vh.barAttachment.setProgress(progress);
		} else if (attachment.getStatus().equals(AttachmentStatus._DOWNLOAD_INCOMPLETE)) {
			vh.attachmentStatus.setText(OpenTenureApplication.getContext()
					.getResources().getString(R.string.download_incomplete));
			vh.attachmentStatus.setTextColor(context.getResources().getColor(R.color.status_created));
			vh.barAttachment.setVisibility(View.GONE);
		} else if (attachment.getStatus().equals(AttachmentStatus._UPLOAD_INCOMPLETE)) {
			vh.attachmentStatus.setText(OpenTenureApplication.getContext()
					.getResources().getString(R.string.upload_incomplete));
			vh.attachmentStatus.setTextColor(context.getResources().getColor(R.color.status_created));
			vh.barAttachment.setVisibility(View.GONE);
		} else if (attachment.getStatus().equals(AttachmentStatus._UPLOAD_ERROR)) {
			vh.attachmentStatus.setText(OpenTenureApplication.getContext()
					.getResources().getString(R.string.upload_error));
			vh.attachmentStatus.setTextColor(context.getResources().getColor(R.color.status_challenged));
			vh.barAttachment.setVisibility(View.GONE);
		}

		vh.viewIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					Uri uri = attachment.getPath().contains("content://") ? Uri.parse(attachment.getPath()) : Uri.parse("content://"+BuildConfig.APPLICATION_ID+attachment.getPath());

					context.grantUriPermission(BuildConfig.APPLICATION_ID, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
					i.setDataAndType(uri, attachment.getMimeType());
					OpenTenureApplication.getDocumentsFragment().startActivity(i);
				} catch (ActivityNotFoundException e) {
					Log.d(this.getClass().getName(),"No Activity Found Exception to handle :" + attachment.getFileName());
					Toast.makeText(OpenTenureApplication.getContext(),
							OpenTenureApplication
									.getContext()
									.getResources()
									.getString(
											R.string.message_no_application) + " " + attachment.getFileName(),
							Toast.LENGTH_LONG).show();
					e.getMessage();
				}
				catch (Throwable t) {
					Log.d(this.getClass().getName(), "Error opening :" + attachment.getFileName());
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

		if (isEditable) {
			vh.removeIcon.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String attachmentName = vh.slogan.getText().toString();

					if(StringUtility.isEmpty(attachmentName)){
						attachmentName = vh.attachmentType.getSelectedItem().toString();
					}

					AlertDialog.Builder deleteDialog = new AlertDialog.Builder(context);
					deleteDialog.setTitle(R.string.action_remove_attachment);
					deleteDialog.setMessage(attachmentName + ": "
							+ context.getResources().getString(
									R.string.message_remove_attachment));

					deleteDialog.setPositiveButton(
							R.string.confirm,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									Attachment.getAttachment(attachments.get(position).getAttachmentId()).delete();
									Toast.makeText(context,
											R.string.attachment_removed,
											Toast.LENGTH_SHORT).show();
									slogans.remove(position);
									attachments.remove(position);
									notifyDataSetChanged();
								}
							});
					deleteDialog.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
								}
							});
					deleteDialog.show();
				}
			});

			// Auto save on document type change
			vh.attachmentType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					updateAttachment(attachment, vh);
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {
				}
			});

			// Auto save on document description change
			vh.slogan.addTextChangedListener(new TextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {}

				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				}

				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					updateAttachment(attachment, vh);
				}
			});

			vh.removeIcon.setVisibility(View.VISIBLE);
		} else {
			vh.slogan.setFocusable(false);
			vh.slogan.setClickable(false);
			vh.attachmentType.setEnabled(false);
			vh.attachmentType.setFocusable(false);
			vh.removeIcon.setVisibility(View.GONE);
			vh.sendIcon.setVisibility(View.GONE);
		}

		if ((!claim.getStatus().equals(ClaimStatus._CREATED)
				&& !claim.getStatus().equals(ClaimStatus._UPLOADING) && !attachment
				.getStatus().equals(AttachmentStatus._DOWNLOADING))
				&& (attachment.getPath() == null || attachment.getPath().equals(""))) {
			vh.downloadIcon.setVisibility(View.VISIBLE);
			vh.downloadIcon.setOnClickListener(new DownloadAttachmentListener(attachment, vh));
			vh.viewIcon.setVisibility(View.GONE);
		} else {
			vh.downloadIcon.setVisibility(View.GONE);
			vh.viewIcon.setVisibility(View.VISIBLE);
		}

		if (isForUpload) {
			vh.sendIcon.setVisibility(View.VISIBLE);
			vh.sendIcon.setOnClickListener(new UploadAttachmentListener(attachment, vh));
		} else {
			vh.sendIcon.setVisibility(View.GONE);
		}

		return convertView;
	}

	private void updateAttachment(Attachment attachment, AttachmentViewHolder vh) {
		DocumentType dt = new DocumentType();
		attachment.setDescription(vh.getSlogan().getText().toString());
		attachment.setFileType(dt.getTypebyDisplayVaue(vh.getAttachmentType().getSelectedItem().toString()));
		attachment.update();
	}
}