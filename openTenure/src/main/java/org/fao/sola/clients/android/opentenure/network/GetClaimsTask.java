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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.filesystem.json.SaveDownloadedClaim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.network.API.CommunityServerAPI;
import org.fao.sola.clients.android.opentenure.network.response.Claim;
import org.fao.sola.clients.android.opentenure.network.response.GetClaimsInput;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * Asynk task that downloads from server all the claims of the list passed as
 * input
 * 
 * */
public class GetClaimsTask extends
		AsyncTask<GetClaimsInput, GetClaimsInput, GetClaimsInput> {
	Map<String, org.fao.sola.clients.android.opentenure.model.Claim> claimsMap;

	public Map<String, org.fao.sola.clients.android.opentenure.model.Claim> getClaimsMap() {
		return claimsMap;
	}

	public void setClaimsMap(
			Map<String, org.fao.sola.clients.android.opentenure.model.Claim> claimsMap) {
		this.claimsMap = claimsMap;
	}

	@Override
	protected GetClaimsInput doInBackground(GetClaimsInput... params) {
		boolean success = true;

		GetClaimsInput input = params[0];
		List<Claim> claims = input.getClaims();

		input.setResult(true);
		

		for (Iterator<Claim> iterator = claims.iterator(); iterator.hasNext();) {
			Claim claimToDownload = (Claim) iterator.next();

			/* For each claim downloadable check the status and the version */

			org.fao.sola.clients.android.opentenure.model.Claim claim = claimsMap.get(claimToDownload.getId());

			if (claim != null
					&& (claimToDownload.getStatusCode().equals(
							ClaimStatus._WITHDRAWN) || claimToDownload
							.getStatusCode().equals(ClaimStatus._REJECTED))) {

				/* In this case the claim will be removed locally */
				Log.d(this.getClass().getName(), "The claim  "
						+ claimToDownload.getId() + " should be deleted");

				if (org.fao.sola.clients.android.opentenure.model.Claim.deleteCascade(claimToDownload.getId()) != 0) {

					FileSystemUtilities.deleteClaim(claimToDownload.getId());
				}

				//input.setDownloaded(input.getDownloaded() + 1);
				OpenTenureApplication.decrementClaimsToDownload();
				publishProgress(input);

			}
			if (claim == null
					&& (claimToDownload.getStatusCode().equals(
							ClaimStatus._WITHDRAWN) || claimToDownload
							.getStatusCode().equals(ClaimStatus._REJECTED))) {

				Log.d(this.getClass().getName(),
						"The claim in not present locally  "
								+ claimToDownload.getId()
								+ "but shall not be downloaded");

				/*
				 * In this case the claim is not present locally and due is
				 * stage the client does not have to retrieve it
				 */

				OpenTenureApplication.decrementClaimsToDownload();
				publishProgress(input);

			} else if ((claim == null)
					|| (!claimToDownload.getVersion()
							.equals(claim.getVersion()))) {

				Log.d(this.getClass().getName(), "The claim  "
						+ claimToDownload.getId() + " shall be downloaded");

				org.fao.sola.clients.android.opentenure.filesystem.json.model.Claim downloadedClaim = CommunityServerAPI
						.getClaim(claimToDownload.getId());

				if (downloadedClaim == null) {
					success = false;

				} else {
					success = SaveDownloadedClaim.save(downloadedClaim);
				}

				if (success == false) {

					input.setResult(success);
					OpenTenureApplication.decrementClaimsToDownload();
					publishProgress(input);

				} else {
					OpenTenureApplication.setClaimsDownloaded(OpenTenureApplication.getClaimsDownloaded() + 1);
					OpenTenureApplication.decrementClaimsToDownload();
					publishProgress(input);

				}

			}

			else {
				Log.d(this.getClass().getName(), "The claim  "
						+ claimToDownload.getId() + " shall not be downloaded");

				OpenTenureApplication.decrementClaimsToDownload();
				publishProgress(input);
			}
			
		}

		return input;

	}

	@Override
	protected void onProgressUpdate(GetClaimsInput... progress) {

		GetClaimsInput input = progress[0];

		View mapView = input.getMapView();

		if (mapView != null) {

			ProgressBar bar = (ProgressBar) mapView
					.findViewById(R.id.progress_bar);

			bar.setVisibility(View.VISIBLE);

			TextView label = (TextView) mapView
					.findViewById(R.id.download_claim_label);
			label.setVisibility(View.VISIBLE);

			bar.setProgress(OpenTenureApplication.getDownloadCompletion());
		}

	}

	@Override
	protected void onPostExecute(final GetClaimsInput input) {


		Toast toast;

		if (OpenTenureApplication.getClaimsToDownload() <= 0) {
			if (input.isResult()) {

				toast = Toast.makeText(OpenTenureApplication.getContext(),
						OpenTenureApplication.getContext().getResources()
								.getString(R.string.message_claims_downloaded),
						Toast.LENGTH_LONG);
								
				
				OpenTenureApplication.getMapFragment().refreshMap();
				OpenTenureApplication.getLocalClaimsFragment().refresh();
				

				toast.show();

				View mapView = input.getMapView();

				if (mapView != null) {

					ProgressBar bar = (ProgressBar) mapView
							.findViewById(R.id.progress_bar);
					bar.setVisibility(View.GONE);

					TextView label = (TextView) mapView
							.findViewById(R.id.download_claim_label);
					label.setVisibility(View.GONE);

				}				

			} else if (!input.isResult()) {

				String message = String
						.format(OpenTenureApplication
								.getContext()
								.getResources()
								.getString(
										R.string.message_error_downloading_claims),
								OpenTenureApplication.getClaimsDownloaded());
				
				OpenTenureApplication.getMapFragment().refreshMap();
				OpenTenureApplication.getLocalClaimsFragment().refresh();
				

				toast = Toast.makeText(OpenTenureApplication.getContext(),
						message, Toast.LENGTH_LONG);
				toast.show();

				View mapView = input.getMapView();

				if (mapView != null) {

					ProgressBar bar = (ProgressBar) mapView
							.findViewById(R.id.progress_bar);

					bar.setVisibility(View.GONE);
					TextView label = (TextView) mapView
							.findViewById(R.id.download_claim_label);
					label.setVisibility(View.GONE);
				}

				
			} else {

				View mapView = input.getMapView();

				if (mapView != null) {

					ProgressBar bar = (ProgressBar) mapView
							.findViewById(R.id.progress_bar);

					bar.setVisibility(View.GONE);
					TextView label = (TextView) mapView
							.findViewById(R.id.download_claim_label);
					label.setVisibility(View.GONE);

				}
			}
		} else {
			Log.d(this.getClass().getName(), "Task completed - Is not the main task");
		}

	}
}
