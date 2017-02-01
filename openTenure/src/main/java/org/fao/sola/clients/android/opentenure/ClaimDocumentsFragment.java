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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.model.Attachment;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.model.DocumentType;
import org.fao.sola.clients.android.opentenure.model.MD5;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

public class ClaimDocumentsFragment extends ListFragment {

	private static final int REQUEST_CHOOSER = 1234;
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	public static final String URI_KEY = "__URI__";
	public static final String FILE_TYPE_KEY = "__FILE_TYPE__";
	public static final String MIME_TYPE_KEY = "__MIME_TYPE__";
	private Uri uri;
	private String fileType;
	private String mimeType;
	private View rootView;
	boolean onlyActive;

	private ClaimDispatcher claimActivity;
	private ModeDispatcher mainActivity;

	private Map<String, String> keyValueDocTypes;
	private Map<String, String> valueKeyDocTypes;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			claimActivity = (ClaimDispatcher) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement ClaimDispatcher");
		}
		try {
			mainActivity = (ModeDispatcher) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement ModeDispatcher");
		}
	}

	public ClaimDocumentsFragment() {
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);

		if (this.isVisible()) {
			update();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();

		inflater.inflate(R.menu.claim_documents, menu);

		Claim claim = Claim.getClaim(claimActivity.getClaimId());
		if (claim != null && !claim.isUploadable()) {
			menu.removeItem(R.id.action_new_picture);
			menu.removeItem(R.id.action_new_attachment);
		}

		super.onCreateOptionsMenu(menu, inflater);
		setHasOptionsMenu(true);
		setRetainInstance(true);
	}

	public File getOutputMediaFile(int type) {
		// To check that the sdcard is mounted use
		// Environment.getExternalStorageState()
		String fileName;

		File mediaStorageDir = FileSystemUtilities.getAttachmentFolder(claimActivity.getClaimId());

		// Create a file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
		if (type == MEDIA_TYPE_IMAGE) {
			fileName = mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg";
			fileType = "image";
			mimeType = "image/jpeg";
		} else if (type == MEDIA_TYPE_VIDEO) {
			fileName = mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4";
			fileType = "video";
			mimeType = "video/mp4";
		} else {
			return null;
		}
		Log.d(this.getClass().getName(), "File name is: " + fileName);

		return new File(fileName);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
			if (resultCode == Activity.RESULT_OK) {
				final String filePath = FileUtils.getPath(rootView.getContext(), uri);
				Log.d(this.getClass().getName(), "Captured image: " + filePath);

				// custom dialog
				final Dialog dialog = new Dialog(rootView.getContext());
				dialog.setContentView(R.layout.custom_add_document);
				dialog.setTitle(R.string.new_file);

				// Attachment Description
				final EditText fileDescription = (EditText) dialog.findViewById(R.id.fileDocumentDescription);
				fileDescription.setHint(R.string.add_description);

				// Code Types Spinner

				/* Mapping id type localization */
				DocumentType dt = new DocumentType();

				keyValueDocTypes = dt.getKeyValueMap(OpenTenureApplication.getInstance().getLocalization(), onlyActive);
				valueKeyDocTypes = dt.getValueKeyMap(OpenTenureApplication.getInstance().getLocalization(), onlyActive);

				final Spinner spinner = (Spinner) dialog.findViewById(R.id.documentTypesSpinner);

				List<String> list = new ArrayList<String>();
				TreeSet<String> keys = new TreeSet<String>(keyValueDocTypes.keySet());
				for (String key : keys) {
					String value = keyValueDocTypes.get(key);
					list.add(value);
					// do something
				}

				// List<String> list =
				// dt.getDocumentTypesDisplayValues(OpenTenureApplication.getInstance().getLocalization());

				ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(OpenTenureApplication.getContext(),
						android.R.layout.simple_spinner_item, list) {
				};
				dataAdapter.setDropDownViewResource(R.layout.document_spinner);

				spinner.setAdapter(dataAdapter);

				// Confirm Button

				final Button confirmButton = (Button) dialog.findViewById(R.id.fileDocumentConfirm);
				confirmButton.setText(R.string.confirm);

				confirmButton.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						File original = new File(filePath);
						File copy = null;
						Log.d(this.getClass().getName(), "Attachment size : " + original.length());

						if (original.length() > 800000) {

							copy = FileSystemUtilities.reduceJpeg(original);

							if (copy != null) {

								Log.d(this.getClass().getName(), "Reduced size to : " + copy.length());
								original.delete();
								
								if (copy.renameTo(new File(filePath))) {
									Log.d(this.getClass().getName(), "Renamed : " + copy.getName() + " to "
											+ filePath);
								} else {
									Log.e(this.getClass().getName(), "Can't rename : " + copy.getName() + " to "
											+ filePath);
								}
							}
						} else {
							copy = original;
						}

						// Recreate the file object to take into account that the file has been renamed
						File att = new File(filePath);

						Attachment attachment = new Attachment();
						attachment.setClaimId(claimActivity.getClaimId());
						attachment.setDescription(fileDescription.getText().toString());
						attachment.setFileName(att.getName());
						attachment.setFileType((valueKeyDocTypes.get((String) spinner.getSelectedItem())));
						attachment.setMimeType(mimeType);
						attachment.setMD5Sum(MD5.calculateMD5(att));
						attachment.setPath(att.getAbsolutePath());
						attachment.setSize(att.length());

						attachment.create();
						update();

						dialog.dismiss();

					}
				});

				// Cancel Button

				final Button cancelButton = (Button) dialog.findViewById(R.id.fileDocumentConfirmCancel);
				cancelButton.setText(R.string.cancel);

				cancelButton.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						new File(filePath).delete();
						dialog.dismiss();
					}
				});

				dialog.show();

			}

			break;
		case REQUEST_CHOOSER:
			if (resultCode == com.ipaulpro.afilechooser.FileChooserActivity.RESULT_OK) {

				uri = data.getData();

				Log.d(this.getClass().getName(), "Selected file: " + FileUtils.getPath(rootView.getContext(), uri));

				fileType = "document";
				mimeType = FileUtils.getMimeType(rootView.getContext(), uri);

				// custom dialog
				final Dialog dialog = new Dialog(rootView.getContext());
				dialog.setContentView(R.layout.custom_add_document);
				dialog.setTitle(R.string.new_file);

				// Attachment Description
				final EditText fileDescription = (EditText) dialog.findViewById(R.id.fileDocumentDescription);
				fileDescription.setHint(R.string.add_description);

				// Code Types Spinner
				final Spinner spinner = (Spinner) dialog.findViewById(R.id.documentTypesSpinner);

				DocumentType dt = new DocumentType();

				keyValueDocTypes = dt.getKeyValueMap(OpenTenureApplication.getInstance().getLocalization(), onlyActive);
				valueKeyDocTypes = dt.getValueKeyMap(OpenTenureApplication.getInstance().getLocalization(), onlyActive);

				List<String> list = new ArrayList<String>();
				TreeSet<String> keys = new TreeSet<String>(keyValueDocTypes.keySet());
				for (String key : keys) {
					String value = keyValueDocTypes.get(key);
					list.add(value);
					// do something
				}

				ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(OpenTenureApplication.getContext(),
						android.R.layout.simple_spinner_item, list) {
				};
				dataAdapter.setDropDownViewResource(R.layout.document_spinner);

				spinner.setAdapter(dataAdapter);

				final Button confirmButton = (Button) dialog.findViewById(R.id.fileDocumentConfirm);
				confirmButton.setText(R.string.confirm);

				confirmButton.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						File copy = FileSystemUtilities.copyFileInAttachFolder(claimActivity.getClaimId(),
								FileUtils.getFile(rootView.getContext(), uri));

						Attachment attachment = new Attachment();
						attachment.setClaimId(claimActivity.getClaimId());
						attachment.setDescription(fileDescription.getText().toString());
						attachment.setFileName(copy.getName());

						attachment.setFileType(valueKeyDocTypes.get((String) spinner.getSelectedItem()));
						attachment.setMimeType(mimeType);
						attachment.setMD5Sum(MD5.calculateMD5(copy));
						attachment.setPath(copy.getAbsolutePath());
						attachment.setSize(copy.length());

						attachment.create();
						update();

						dialog.dismiss();

					}
				});

				final Button cancelButton = (Button) dialog.findViewById(R.id.fileDocumentConfirmCancel);
				cancelButton.setText(R.string.cancel);

				cancelButton.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						dialog.dismiss();
					}
				});

				dialog.show();
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle item selection
		Toast toast;
		Intent intent;
		switch (item.getItemId()) {

		case R.id.action_new_picture:
			if (claimActivity.getClaimId() == null) {
				toast = Toast.makeText(rootView.getContext(), R.string.message_create_before_edit, Toast.LENGTH_SHORT);
				toast.show();
				return true;
			}
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			uri = Uri.fromFile(getOutputMediaFile(MEDIA_TYPE_IMAGE));
			Log.d(this.getClass().getName(), "Passing " + uri + " to MediaStore intent");
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

			// start the image capture Intent
			startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
			return true;
		case R.id.action_new_attachment:
			if (claimActivity.getClaimId() == null) {
				toast = Toast.makeText(rootView.getContext(), R.string.message_create_before_edit, Toast.LENGTH_SHORT);
				toast.show();
				return true;
			}
			Intent getContentIntent = FileUtils.createGetContentIntent();

			intent = Intent.createChooser(getContentIntent, getResources().getString(R.string.choose_file));
			try {
				startActivityForResult(intent, REQUEST_CHOOSER);
			} catch (Exception e) {
				Log.d(this.getClass().getName(), "Unable to start file chooser intent due to " + e.getMessage());
			}
			return true;

		default:
			return super.onOptionsItemSelected(item);

		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.claim_documents_list, container, false);

		String claimId = claimActivity.getClaimId();
		if (claimId != null) {
			Claim claim = Claim.getClaim(claimId);

			onlyActive = (!claim.getStatus().equals(ClaimStatus._MODERATED)
					&& !claim.getStatus().equals(ClaimStatus._REJECTED)
					&& !claim.getStatus().equals(ClaimStatus._REVIEWED));
		}

		setRetainInstance(true);
		setHasOptionsMenu(true);
		if (savedInstanceState != null) {
			String savedUri = savedInstanceState.getString(URI_KEY);
			if (savedUri != null)
				uri = Uri.parse(savedUri);
			fileType = savedInstanceState.getString(FILE_TYPE_KEY);
			mimeType = savedInstanceState.getString(MIME_TYPE_KEY);
		}

		update();

		OpenTenureApplication.setDocumentsFragment(this);
		return rootView;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (uri != null)
			outState.putString(URI_KEY, uri.toString());
		outState.putString(FILE_TYPE_KEY, fileType);
		outState.putString(MIME_TYPE_KEY, mimeType);
		super.onSaveInstanceState(outState);
	};

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		/*
		 * 
		 * */

		String attachmentId = ((TextView) v.findViewById(R.id.attachment_id)).getText().toString();
		Attachment att = Attachment.getAttachment(attachmentId);

		if (att != null && att.getPath() != null && !att.getPath().equals("")) {

			try {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.parse("file://" + att.getPath()), att.getMimeType());
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(intent);
			} catch (ActivityNotFoundException e) {

				Log.d(this.getClass().getName(), "No Activity Found Exception to handle :" + att.getFileName());

				Toast.makeText(OpenTenureApplication.getContext(),
						OpenTenureApplication.getContext().getResources().getString(R.string.message_no_application)
								+ " " + att.getFileName(),
						Toast.LENGTH_LONG).show();

				e.getMessage();
			}

			catch (Throwable t) {

				Log.d(this.getClass().getName(), "Error opening :" + att.getFileName());

				Toast.makeText(OpenTenureApplication.getContext(), OpenTenureApplication.getContext().getResources()
						.getString(R.string.message_error_opening_file), Toast.LENGTH_LONG).show();
			}

		}

	}

	public void update() {
		String claimId = claimActivity.getClaimId();
		List<Attachment> attachments;
		List<String> ids = new ArrayList<String>();
		List<String> slogans = new ArrayList<String>();
		List<String> stati = new ArrayList<String>();

		if (claimId != null) {

			Claim claim = Claim.getClaim(claimId);
			attachments = claim.getAttachments();
			for (Attachment attachment : attachments) {
				if (!attachment.getAttachmentId().equals(claim.getPerson().getPersonId()))

				{
					String slogan = attachment.getDescription() + " - " + (attachment.getFileType()) + " - "
							+ attachment.getMimeType();
					slogans.add(slogan);
					ids.add(attachment.getAttachmentId());

					stati.add(attachment.getStatus());
				}
			}
		}

		ArrayAdapter<String> adapter = null;
		if (mainActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RO) == 0) {
			adapter = new ClaimAttachmentsListAdapter(rootView.getContext(), slogans, ids, claimId, true);
		} else {
			adapter = new ClaimAttachmentsListAdapter(rootView.getContext(), slogans, ids, claimId, false);
		}
		setListAdapter(adapter);
		adapter.notifyDataSetChanged();

	}

	public Uri getUri() {
		return uri;
	}

	public void setUri(Uri uri) {
		this.uri = uri;
	}
}