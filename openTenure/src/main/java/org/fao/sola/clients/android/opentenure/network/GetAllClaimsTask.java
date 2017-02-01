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

import java.util.List;
import java.util.Map;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.network.API.CommunityServerAPI;
import org.fao.sola.clients.android.opentenure.network.response.Claim;
import org.fao.sola.clients.android.opentenure.network.response.GetClaimsInput;

import com.google.android.gms.maps.model.LatLngBounds;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Get the list of all claims within the current map box .
 * 
 * */
public class GetAllClaimsTask extends AsyncTask<Object, Void, GetClaimsInput> {

	@Override
	protected GetClaimsInput doInBackground(Object... params) {

		if (params[0] == null) {
			List<Claim> listClaim = (List<Claim>) CommunityServerAPI
					.getAllClaims();
			
			GetClaimsInput claimToRetrieve = new GetClaimsInput();
			claimToRetrieve.setClaims(listClaim);
			claimToRetrieve.setMapView((View) params[1]);

			return claimToRetrieve;

		} else {

			/*
			 * Here in the case of current box bounds
			 */

			GetClaimsInput claimToRetrieve = new GetClaimsInput();

			/* In case there's no connection*/
			if (!OpenTenureApplication.getInstance().isOnline()) {
				claimToRetrieve.setMapView((View) params[1]);
				return claimToRetrieve;
			}

			String[] coordinates = buildCoordinates((LatLngBounds) params[0]);
			List<Claim> listClaim = (List<Claim>) CommunityServerAPI
					.getAllClaimsByBox(coordinates);

			claimToRetrieve.setClaims(listClaim);
			claimToRetrieve.setMapView((View) params[1]);

			return claimToRetrieve;

		}

	}

	@Override
	protected void onPostExecute(final GetClaimsInput input) {

		Toast toast;

		if (input.getClaims() == null || input.getClaims().size() == 0) {

			if (!OpenTenureApplication.getInstance().isOnline()) {
				toast = Toast.makeText(OpenTenureApplication.getContext(),
						OpenTenureApplication.getContext().getResources()
								.getString(R.string.message_connection_error),
						Toast.LENGTH_LONG);

			} else {
				toast = Toast.makeText(
						OpenTenureApplication.getContext(),
						OpenTenureApplication
								.getContext()
								.getResources()
								.getString(
										R.string.message_no_claim_to_download),
						Toast.LENGTH_LONG);

			}

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

			return;
		}
		
		
		// Before to start downloading claims the input is divided in two and assigned to two thread. 
		
		OpenTenureApplication.setClaimsToDownload(input.getClaims().size());
		OpenTenureApplication.setClaimsDownloaded(0);
		
		
		
		List<Claim> inputList = input.getClaims();
		Map<String, org.fao.sola.clients.android.opentenure.model.Claim> claimsMap = org.fao.sola.clients.android.opentenure.model.Claim.getSimplifiedClaimsForDownload();
		
		List<Claim> inputList1 = inputList.subList(0, (inputList.size()/2)/2);
		List<Claim> inputList2 = inputList.subList((inputList.size()/2)/2, (inputList.size()/2));
		List<Claim> inputList3 = inputList.subList((inputList.size()/2), (inputList.size()/2)+(inputList.size()/4));
		List<Claim> inputList4 = inputList.subList((inputList.size()/2)+(inputList.size()/4), inputList.size());
		
		
		GetClaimsInput input2 = new GetClaimsInput();
		
		input2.setClaimId(input.getClaimId());
		input2.setClaims(inputList2);
		//input2.setDownloaded(input.getDownloaded());
		input2.setHttpStatusCode(input.getHttpStatusCode());
		input2.setMapView(input.getMapView());
		input2.setMessage(input.getMessage());
		input2.setFirst(false);
		
		GetClaimsInput input3 = new GetClaimsInput();
		
		input3.setClaimId(input.getClaimId());
		input3.setClaims(inputList3);
		//input3.setDownloaded(input.getDownloaded());
		input3.setHttpStatusCode(input.getHttpStatusCode());
		input3.setMapView(input.getMapView());
		input3.setMessage(input.getMessage());
		input3.setFirst(false);
		
		
		
		GetClaimsInput input4 = new GetClaimsInput();
		input4.setClaimId(input.getClaimId());
		input4.setClaims(inputList4);
		//input4.setDownloaded(input.getDownloaded());
		input4.setHttpStatusCode(input.getHttpStatusCode());
		input4.setMapView(input.getMapView());
		input4.setMessage(input.getMessage());
		input4.setFirst(false);
		

		input.setClaims(inputList1);
		input.setFirst(true);
		
		GetClaimsTask task1 = new GetClaimsTask();
		task1.setClaimsMap(claimsMap);
		task1.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,input);		
		
		GetClaimsTask task2 = new GetClaimsTask();
		task2.setClaimsMap(claimsMap);
		task2.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,input2);
		
		GetClaimsTask task3 = new GetClaimsTask();
		task3.setClaimsMap(claimsMap);
		task3.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,input3);
		
		GetClaimsTask task4 = new GetClaimsTask();
		task4.setClaimsMap(claimsMap);
		task4.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,input4);

		return;

	}

	private String[] buildCoordinates(LatLngBounds bounds) {

		String[] coordinates = new String[4];

		String minX = new String("" + bounds.southwest.longitude);
		String minY = new String("" + bounds.southwest.latitude);
		String maxX = new String("" + bounds.northeast.longitude);
		String maxY = new String("" + bounds.northeast.latitude);

		if (minX.startsWith("-"))
			minX = minX.substring(0, 11);
		else
			minX = minX.substring(0, 10);

		if (minY.startsWith("-"))
			minY = minY.substring(0, 11);
		else
			minY = minY.substring(0, 10);

		if (maxX.startsWith("-"))
			maxX = maxX.substring(0, 11);
		else
			maxX = maxX.substring(0, 10);

		if (maxY.startsWith("-"))
			maxY = maxY.substring(0, 11);
		else
			maxY = maxY.substring(0, 10);

		coordinates[0] = minX;
		coordinates[1] = minY;
		coordinates[2] = maxX;
		coordinates[3] = maxY;

		return coordinates;
	}

}
