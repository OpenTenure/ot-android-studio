/**
 * ******************************************************************************************
 * Copyright (C) 2014 - Food and Agriculture Organization of the United Nations (FAO).
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,this list
 * of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,this list
 * of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 * 3. Neither the name of FAO nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 * <p>
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

import android.os.AsyncTask;
import androidx.fragment.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.fao.sola.clients.android.opentenure.BoundaryListItemViewHolder;
import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.filesystem.json.JsonUtilities;
import org.fao.sola.clients.android.opentenure.network.API.CommunityServerAPI;
import org.fao.sola.clients.android.opentenure.network.response.ResultResponse;

/**
 * Submits boundary for saving to the server
 *
 * **/
public class SaveBoundaryTask extends AsyncTask<Object, ResultResponse, ResultResponse> {

    private BoundaryListItemViewHolder vh;

    @Override
    protected ResultResponse doInBackground(Object... params) {
        vh = (BoundaryListItemViewHolder) params[0];
        String json = JsonUtilities.boundaryToJson(vh.getBoundary().convertToResponse());
        return CommunityServerAPI.saveBoundary(json);
    }

    protected void onPostExecute(final ResultResponse response) {
        boolean success = false;
        try {
            Toast toast;
            switch (response.getHttpStatusCode()) {
                case 100: {
                    /* UnknownHostException: */
                    toast = Toast.makeText(
                            OpenTenureApplication.getContext(),
                            OpenTenureApplication.getContext().getResources()
                                    .getString(R.string.message_submission_boundary_error)
                                    + "  "
                                    + OpenTenureApplication
                                    .getContext()
                                    .getResources()
                                    .getString(
                                            R.string.message_connection_error),
                            Toast.LENGTH_LONG);
                    toast.show();
                    break;
                }

                case 105: {
					/* IOException: */
                    toast = Toast.makeText(OpenTenureApplication.getContext(),
                            OpenTenureApplication.getContext().getResources()
                                    .getString(R.string.message_submission_boundary_error)
                                    + " " + response.getMessage(), Toast.LENGTH_LONG);
                    toast.show();
                    break;
                }
                case 110: {
                    toast = Toast.makeText(OpenTenureApplication.getContext(),
                            OpenTenureApplication.getContext().getResources()
                                    .getString(R.string.message_submission_boundary_error)
                                    + " " + response.getMessage(), Toast.LENGTH_LONG);
                    toast.show();
                    break;
                }

                case 454: {
                    Log.d("CommunityServerAPI", "SAVE BOUNDARY JSON RESPONSE " + response.getMessage());

                    toast = Toast.makeText(OpenTenureApplication.getContext(),
                            OpenTenureApplication.getContext().getResources()
                                    .getString(R.string.message_submission_boundary_error)
                                    + " " + response.getMessage(), Toast.LENGTH_LONG);
                    toast.show();
                    break;
                }

                // User cannot access the project recorded for the boundary
                case 460: {
                    toast = Toast.makeText(OpenTenureApplication.getContext(),
                            OpenTenureApplication.getContext().getResources()
                                    .getString(R.string.message_project_not_accessible_error), Toast.LENGTH_LONG);
                    toast.show();
                    break;
                }

                case 200: {
					/* OK */
                    try {
                        // Update boundary to set it as processed
                        success = true;
                        vh.getBoundary().markProcessed(response.getResult());
                    } catch (Exception e) {
                        Log.d("CommunityServerAPI", "Error uploading boundary " + e.getMessage());
                        e.printStackTrace();
                    }

                    toast = Toast.makeText(OpenTenureApplication.getContext(),
                            String.format(OpenTenureApplication.getContext().getResources().getString(R.string.message_boundary_submitted), vh.getBoundary().getName()),
                            Toast.LENGTH_LONG);
                    toast.show();
                    break;
                }

                case 403: {
					/* Error Login */
                    Log.d("CommunityServerAPI", "SAVE BOUNDARY JSON RESPONSE " + response.getMessage());

                    toast = Toast
                            .makeText(
                                    OpenTenureApplication.getContext(),
                                    OpenTenureApplication
                                            .getContext()
                                            .getResources()
                                            .getString(
                                                    R.string.message_submission_boundary_error)
                                            + " "
                                            + response.getHttpStatusCode()
                                            + "  "
                                            + OpenTenureApplication
                                            .getContext()
                                            .getResources()
                                            .getString(
                                                    R.string.message_login_no_more_valid),
                                    Toast.LENGTH_LONG);
                    toast.show();

                    OpenTenureApplication.setLoggedin(false);
                    FragmentActivity fa = (FragmentActivity) OpenTenureApplication.getActivity();
                    fa.invalidateOptionsMenu();
                    break;
                }
                case 404: {
                    Log.d("CommunityServerAPI", "SAVE BOUNDARY JSON RESPONSE " + response.getMessage());

                    toast = Toast
                            .makeText(
                                    OpenTenureApplication.getContext(),
                                    OpenTenureApplication
                                            .getContext()
                                            .getResources()
                                            .getString(R.string.message_submission_boundary_error)
                                            + " "
                                            + OpenTenureApplication
                                            .getContext()
                                            .getResources()
                                            .getString(
                                                    R.string.message_service_not_available),
                                    Toast.LENGTH_LONG);
                    toast.show();
                    break;
                }
                case 400: {

                    Log.d("CommunityServerAPI", "SAVE BOUNDARY JSON RESPONSE " + response.getMessage());

                    toast = Toast.makeText(OpenTenureApplication.getContext(),
                            OpenTenureApplication.getContext().getResources()
                                    .getString(R.string.message_submission_boundary_error)
                                    + " ," + response.getMessage(), Toast.LENGTH_LONG);
                    toast.show();
                    break;
                }


                case 500:
                    Log.d("CommunityServerAPI", "SAVE BOUNDARY JSON RESPONSE " + response.getMessage());
                    toast = Toast.makeText(OpenTenureApplication.getContext(),
                            OpenTenureApplication.getContext().getResources()
                                    .getString(R.string.message_submission_boundary_error)
                                    + " ," + response.getMessage(), Toast.LENGTH_LONG);
                    toast.show();

                default:
                    break;
            }
        } catch (Exception ex) {
            Log.d("CommunityServerAPI", "SAVE BOUNDARY JSON RESPONSE " + ex.getMessage());
        }

        updateViewHolder(success);
        return;
    }

    private void updateViewHolder(boolean success) {
        vh.getBar().setVisibility(View.GONE);
        if (success) {
            vh.getSend().setVisibility(View.GONE);
            vh.getDelete().setVisibility(View.GONE);
            vh.getProcessed().setVisibility(View.VISIBLE);
        } else {
            vh.getSend().setVisibility(View.VISIBLE);
            vh.getDelete().setVisibility(View.VISIBLE);
            vh.getProcessed().setVisibility(View.GONE);
        }
    }
}
