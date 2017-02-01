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

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.model.Configuration;
import org.fao.sola.clients.android.opentenure.network.UpdateClaimTypesTask;
import org.fao.sola.clients.android.opentenure.network.UpdateCommunityArea;
import org.fao.sola.clients.android.opentenure.network.UpdateDocumentTypesTask;
import org.fao.sola.clients.android.opentenure.network.UpdateIdTypesTask;
import org.fao.sola.clients.android.opentenure.network.UpdateLandUsesTask;
import org.fao.sola.clients.android.opentenure.network.UpdateLanguagesTask;
import org.fao.sola.clients.android.opentenure.network.UpdateParcelGeoRequiredTask;

import android.app.Dialog;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class InitializationAlertListener implements OnClickListener{

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		Log.d(this.getClass().getName(),
				"starting tasks for static data download");
		
		OpenTenureApplication.getInstance().setNetworkError(false);

		Configuration conf = Configuration
				.getConfigurationByName("isInitialized");
		if (conf == null) {

			conf = new Configuration();
			conf.setName("isInitialized");
			conf.setValue("false");
			conf.create();

		}

		if (!OpenTenureApplication.getInstance().isCheckedTypes()) {
			Log.d(this.getClass().getName(),
					"starting tasks for claim type download");

			UpdateClaimTypesTask updateCT = new UpdateClaimTypesTask();
			updateCT.execute();

		}

		if (!OpenTenureApplication.getInstance().isCheckedDocTypes()) {
			Log.d(this.getClass().getName(),
					"starting tasks for document type download");

			UpdateDocumentTypesTask updateCT = new UpdateDocumentTypesTask();
			updateCT.execute();

		}

		if (!OpenTenureApplication.getInstance().isCheckedIdTypes()) {
			Log.d(this.getClass().getName(),
					"starting tasks for ID type download");

			UpdateIdTypesTask updateIdType = new UpdateIdTypesTask();
			updateIdType.execute();
		}
		if (!OpenTenureApplication.getInstance().isCheckedLandUses()) {
			Log.d(this.getClass().getName(),
					"starting tasks for land use type download");

			UpdateLandUsesTask updateLu = new UpdateLandUsesTask();
			updateLu.execute();
		}
		if (!OpenTenureApplication.getInstance().isCheckedLandUses()) {
			Log.d(this.getClass().getName(),
					"starting tasks for languages download");

			UpdateLanguagesTask updateLang = new UpdateLanguagesTask();
			updateLang.execute();
		}
		if (!OpenTenureApplication.getInstance().isCheckedCommunityArea()) {
			Log.d(this.getClass().getName(),
					"starting tasks for community area download");

			UpdateCommunityArea updateArea = new UpdateCommunityArea();
			updateArea.execute();
		}
		
		if (!OpenTenureApplication.getInstance().isCheckedGeometryRequired()) {
			Log.d(this.getClass().getName(),
					"starting tasks for parcel geomtry setting download");

			UpdateParcelGeoRequiredTask updateGeo = new UpdateParcelGeoRequiredTask();
			updateGeo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
		
		if (!OpenTenureApplication.getInstance().isCheckedForm()) {
			Log.d(this.getClass().getName(),
					"starting tasks for form retrieval");

//			 FormRetriever formRetriever = new FormRetriever();
//			 formRetriever.setFormUrl(formUrl);
//			 formRetriever.execute();
		}
		
		final Dialog dialog = new Dialog(v.getContext());
		dialog.setContentView(R.layout.custom_remove_claim);
		dialog.setTitle(R.string.withdraw_claim);
		
		final Button confirmButton = (Button) dialog
				.findViewById(R.id.ClaimWithdrawnConfirm);
		confirmButton.setText(R.string.confirm);

		confirmButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

		

				dialog.dismiss();

			}
		});
		
	}

}
