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

import java.util.List;

import org.fao.sola.clients.android.opentenure.DisplayNameLocalizer;
import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.OpenTenurePreferencesActivity;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.ViewHolder;
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.filesystem.json.JsonUtilities;
import org.fao.sola.clients.android.opentenure.form.FieldConstraint;
import org.fao.sola.clients.android.opentenure.form.FormPayload;
import org.fao.sola.clients.android.opentenure.form.FormTemplate;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.model.Configuration;
import org.fao.sola.clients.android.opentenure.model.DocumentType;
import org.fao.sola.clients.android.opentenure.model.Person;
import org.fao.sola.clients.android.opentenure.model.Vertex;
import org.fao.sola.clients.android.opentenure.network.SaveClaimTask;
import org.fao.sola.clients.android.opentenure.print.PDFClaimExporter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class SubmitClaimListener implements OnClickListener {

	String claimId;
	ViewHolder vh;

	public SubmitClaimListener(String claimId, ViewHolder vh) {

		this.claimId = claimId;
		this.vh = vh;
	}

	@Override
	public void onClick(View v) {
		doIt(v);
	}

	private void doIt(final View v) {

		if (!OpenTenureApplication.isLoggedin()) {
			Toast toast = Toast.makeText(v.getContext(),
					R.string.message_login_before, Toast.LENGTH_LONG);
			toast.show();
			return;

		} else {
			if (OpenTenureApplication.getInstance().isConnectedWifi(
					v.getContext())) {
				submitClaim(v);
			} else {
				// Avoid to automatically download claims over mobile data
				AlertDialog.Builder confirmDownloadBuilder = new AlertDialog.Builder(
						v.getContext());
				confirmDownloadBuilder
						.setTitle(R.string.title_confirm_data_transfer);
				confirmDownloadBuilder.setMessage(v.getResources().getString(
						R.string.message_data_over_mobile));

				confirmDownloadBuilder.setPositiveButton(R.string.confirm,
						new android.content.DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								submitClaim(v);
							}
						});
				confirmDownloadBuilder.setNegativeButton(R.string.cancel,
						new android.content.DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
							}
						});

				final AlertDialog confirmDownloadDialog = confirmDownloadBuilder
						.create();
				confirmDownloadDialog.show();
			}
			return;
		}

	}

	private void submitClaim(View v) {
		if (claimId != null) {

			Person person = Claim.getClaim(claimId).getPerson();
			Claim claim = Claim.getClaim(claimId);

			// Check is claim is already in uploading status(double click on
			// send issue)
			if (claim.getStatus().equals(ClaimStatus._UPLOADING)) {
				return;
			}

			boolean isDefaultCertificateDocumentTypeAvailable = false;
			DocumentType dt = DocumentType.getDocumentType(PDFClaimExporter.DEFAULT_CERTIFICATE_DOCUMENT_TYPE);
			if(dt!= null && PDFClaimExporter.DEFAULT_CERTIFICATE_DOCUMENT_TYPE.equalsIgnoreCase(dt.getType()) && dt.isActive()){
				isDefaultCertificateDocumentTypeAvailable = true;
				Log.d(this.getClass().getName(),"Automatic attachment of claim summary is enabled");
			}else{
				if(dt != null){
					Log.i(this.getClass().getName(),"Automatic attachment of claim summary is disabled due to " + dt.toString());
				}else{
					Log.i(this.getClass().getName(),"Automatic attachment of claim summary is disabled");
				}
			}
			if(isDefaultCertificateDocumentTypeAvailable){
				// Here the printed certificate is added as attachment just before to
				// submit claim
				
				try {
					PDFClaimExporter pdf = new PDFClaimExporter(
							v.getContext(), claim, true);
					pdf.addAsAttachment(v.getContext(), claimId);

				} catch (Error e) {
					Toast toast = Toast.makeText(v.getContext(),
							R.string.message_not_supported_on_this_device,
							Toast.LENGTH_SHORT);
					toast.show();
					Log.w(this.getClass().getName(),"Exporting a PDF is not supported on this device");
				}
			}

			// Here the claimant picture is added as attachment just before to
			// submit claim
			person.addPersonPictureAsAttachment(claimId);

			/* Checking if the Geometry is mandatory for claim's submission */
			List<Vertex> vertices = Vertex.getVertices(claimId);

			if (Boolean.parseBoolean(Configuration.getConfigurationByName(
					"geometryRequired").getValue())) {

				if (vertices.size() < 3) {

					Toast toast = Toast.makeText(v.getContext(),
							R.string.message_map_not_yet_draw,
							Toast.LENGTH_LONG);
					toast.show();
					return;
				}
			}

			JsonUtilities.createClaimJson(claimId);

			Log.d(this.getClass().getName(),
					"mapGeometry: " + Vertex.mapWKTFromVertices(vertices));
			Log.d(this.getClass().getName(),
					"gpsGeometry: " + Vertex.gpsWKTFromVertices(vertices));

			FormPayload payload = claim.getDynamicForm();
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(v.getContext());
			boolean enableFormValidation = preferences.getBoolean(OpenTenurePreferencesActivity.FORM_VALIDATION_PREF, true);

			if (payload != null && enableFormValidation) {

				FormTemplate template = payload.getFormTemplate();

				if (template != null) {

					DisplayNameLocalizer dnl = new DisplayNameLocalizer(
							OpenTenureApplication.getInstance().getLocalization());
					FieldConstraint failedConstraint = template
							.getFailedConstraint(payload, dnl);

					if (failedConstraint != null) {
						Toast toast = Toast.makeText(v.getContext(),
								dnl.getLocalizedDisplayName(failedConstraint.getErrorMsg()),
								Toast.LENGTH_LONG);
						toast.show();
						return;
					}
				}
			}

			int progress = FileSystemUtilities.getUploadProgress(claimId,
					claim.getStatus());

			vh.getBar().setVisibility(View.VISIBLE);
			vh.getBar().setProgress(progress);

			String status = claim.getStatus();
			if (status.equals(ClaimStatus._MODERATED)
					|| status.equals(ClaimStatus._UPDATE_ERROR)
					|| status.equals(ClaimStatus._UPDATE_INCOMPLETE))
				vh.getStatus().setText(
						ClaimStatus._UPDATING + ": " + progress + " %");
			else
				vh.getStatus().setText(
						ClaimStatus._UPLOADING + ": " + progress + " %");
			vh.getStatus().setTextColor(
					OpenTenureApplication.getContext().getResources()
							.getColor(R.color.status_created));
			vh.getStatus().setVisibility(View.VISIBLE);

			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			SaveClaimTask saveClaimtask = new SaveClaimTask();
			saveClaimtask.execute(claimId, vh);
		} else {
			Toast toast = Toast.makeText(v.getContext(),
					R.string.message_save_claim_before_submit,
					Toast.LENGTH_SHORT);
			toast.show();
		}
	}

}
