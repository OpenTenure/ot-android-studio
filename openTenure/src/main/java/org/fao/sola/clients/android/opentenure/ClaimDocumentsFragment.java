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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import org.fao.sola.clients.android.opentenure.filesystem.FilePathUtilities;
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.model.Attachment;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.model.DocumentType;
import org.fao.sola.clients.android.opentenure.model.MD5;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ListFragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
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

	public Uri getOutputMediaUri(int type) {
		String fileName;

		// Create a file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
		if (type == MEDIA_TYPE_IMAGE) {
			fileName = "IMG_" + timeStamp + ".jpg";
			fileType = "image";
			mimeType = "image/jpeg";
		} else if (type == MEDIA_TYPE_VIDEO) {
			fileName = "VID_" + timeStamp + ".mp4";
			fileType = "video";
			mimeType = "video/mp4";
		} else {
			return null;
		}
		Log.d(this.getClass().getName(), "File name is: " + fileName);

		try {
			File basePath = new File(OpenTenureApplication.getContext().getExternalFilesDir(null), "claims/claim_" + claimActivity.getClaimId() + "/attachments");
			if (!basePath.exists()) {
				new File(basePath.getPath()).mkdirs();
			}
			File image = new File(basePath, fileName);
			if (!image.exists()) {
				image.createNewFile();
			}
			Uri contentUri = FileProvider.getUriForFile(this.getContext(), BuildConfig.APPLICATION_ID, image);
			Log.d(this.getClass().getName(), "File uri is "+contentUri+". File path is "+image.getPath());
			return contentUri;
		} catch (IOException e) {
			Log.e(this.getClass().getName(), "Error creating file for image capture. " + e.getMessage());
			return null;
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
				if (resultCode == Activity.RESULT_OK) {
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
							FileSystemUtilities.rotateAndCompressImage(getContext(), uri);

							String md5sum="";
							try (InputStream is = getContext().getContentResolver().openInputStream(uri)) {
								md5sum = MD5.calculateMD5(is);
							} catch (Exception e) {
								Log.e(this.getClass().getName(), "Could not calculate md5");
							}

							File att = new File(uri.getPath());

							Attachment attachment = new Attachment();
							attachment.setClaimId(claimActivity.getClaimId());
							attachment.setDescription(fileDescription.getText().toString());
							attachment.setFileName(att.getName());
							attachment.setFileType((valueKeyDocTypes.get((String) spinner.getSelectedItem())));
							attachment.setMimeType(mimeType);
							attachment.setMD5Sum(md5sum);
							attachment.setPath(uri.toString());//att.getAbsolutePath());
							attachment.setSize((long) FileSystemUtilities.getFileSizeFromUri(getContext(), uri));

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
							OpenTenureApplication.getContext().getContentResolver().delete(uri, null, null);
							dialog.dismiss();
						}
					});

					dialog.show();

				}

				break;
			case REQUEST_CHOOSER:
				if (resultCode == com.ipaulpro.afilechooser.FileChooserActivity.RESULT_OK) {

					if (data.getDataString().contains("document/raw%3A")) {
						String path = data.getDataString().substring(data.getDataString().indexOf("document/raw%3A")+15);
						try {
							path = URLDecoder.decode(path, StandardCharsets.UTF_8.toString());
							File file = new File(path);
							uri = FileProvider.getUriForFile(this.getContext(), BuildConfig.APPLICATION_ID, file);
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
					} else {
						uri = data.getData();
					}
					Log.d(this.getClass().getName(), "Selected file: " + uri.toString());// FileUtils.getPath(rootView.getContext(), uri));

					fileType = "document";

					// get mime type
					ContentResolver cr = getContext().getContentResolver();
					mimeType = cr.getType(uri);

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
							File copy;
							try (InputStream in = getContext().getContentResolver().openInputStream(uri)) {
								copy = FileSystemUtilities.copyStreamInAttachFolder(claimActivity.getClaimId(), in,
										FilePathUtilities.getFileName(uri, getActivity().getContentResolver()));
							} catch (IOException e) {
								e.printStackTrace();
								return;
							}

							Attachment attachment = new Attachment();
							attachment.setClaimId(claimActivity.getClaimId());
							attachment.setDescription(fileDescription.getText().toString());
							attachment.setFileName(copy.getName());

							attachment.setFileType(valueKeyDocTypes.get((String) spinner.getSelectedItem()));
							attachment.setMimeType(mimeType);
							attachment.setMD5Sum(MD5.calculateMD5(copy));

							String contentPath = FileProvider.getUriForFile(OpenTenureApplication.getContext(), BuildConfig.APPLICATION_ID, copy).toString();//.getPath();

							attachment.setPath(contentPath);
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

				intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE).addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
				uri = getOutputMediaUri(MEDIA_TYPE_IMAGE);
				this.getContext().grantUriPermission(BuildConfig.APPLICATION_ID, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

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

	}

	public void update() {
		String claimId = claimActivity.getClaimId();
		List<Attachment> attachments = new ArrayList<>();
		List<String> slogans = new ArrayList<>();

		if (claimId != null) {
			Claim claim = Claim.getClaim(claimId);
			attachments = claim.getAttachments();

			if(attachments != null){
				// Remove person photo
				for (int i = 0; i < attachments.size(); i++) {
					if (claim.getPerson() != null && attachments.get(i).getAttachmentId().equals(claim.getPerson().getPersonId()))  {
						attachments.remove(i);
						break;
					}
				}

				for(Attachment attachment: attachments){
					slogans.add(attachment.getDescription());
				}
			}
		}

		ArrayAdapter<String> adapter = null;
		if (mainActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RO) == 0) {
			adapter = new ClaimAttachmentsListAdapter(rootView.getContext(), attachments, slogans, claimId, true);
		} else {
			adapter = new ClaimAttachmentsListAdapter(rootView.getContext(), attachments, slogans, claimId, false);
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