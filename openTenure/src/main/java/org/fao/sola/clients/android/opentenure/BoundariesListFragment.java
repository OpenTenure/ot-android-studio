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
package org.fao.sola.clients.android.opentenure;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.fao.sola.clients.android.opentenure.model.Boundary;
import org.fao.sola.clients.android.opentenure.model.Configuration;
import org.fao.sola.clients.android.opentenure.network.API.CommunityServerAPI;
import org.fao.sola.clients.android.opentenure.network.BoundaryTask;
import org.fao.sola.clients.android.opentenure.network.LoginActivity;
import org.fao.sola.clients.android.opentenure.network.LogoutTask;

import java.util.List;

public class BoundariesListFragment extends ListFragment {
    private static final String FILTER_KEY = "filterForBoundaries";
    private View rootView;
    private ModeDispatcher mainActivity;
    private String filter = null;
    private List<Boundary> boundaries;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mainActivity = (ModeDispatcher) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ModeDispatcher");
        }
    }

    public BoundariesListFragment() {
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem itemIn;
        MenuItem itemOut;

        if (mainActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RW) == 0) {
            if (OpenTenureApplication.isLoggedin()) {
                itemIn = menu.findItem(R.id.action_login);
                itemIn.setVisible(false);
                itemOut = menu.findItem(R.id.action_logout);
                itemOut.setVisible(true);
            } else {
                itemIn = menu.findItem(R.id.action_login);
                itemIn.setVisible(true);
                itemOut = menu.findItem(R.id.action_logout);
                itemOut.setVisible(false);
            }
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.boundaries_list, menu);
        setRetainInstance(true);
        if (mainActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RO) == 0) {
            menu.removeItem(R.id.action_refresh);
            menu.removeItem(R.id.action_new);
            menu.removeItem(R.id.action_login);
            menu.removeItem(R.id.action_logout);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new:
                if (!Boolean.parseBoolean(Configuration.getConfigurationByName("isInitialized").getValue())) {
                    String newMessage = String.format(OpenTenureApplication.getContext().getString(R.string.message_app_not_yet_initialized));

                    Toast newToast = Toast.makeText(OpenTenureApplication.getContext(), newMessage, Toast.LENGTH_LONG);
                    newToast.show();
                    return true;
                }

                Intent intent = new Intent(rootView.getContext(), BoundaryActivity.class);
                intent.putExtra(BoundaryActivity.BOUNDARY_ID_KEY, "");
                startActivityForResult(intent, 100);
                return true;
            case R.id.action_refresh:

                AsyncTask<Void, Void, List<org.fao.sola.clients.android.opentenure.network.response.Boundary>> task = new AsyncTask<Void, Void, List<org.fao.sola.clients.android.opentenure.network.response.Boundary>>() {
                    @Override
                    protected void onPreExecute() {
                        item.setEnabled(false);
                    }

                    @Override
                    protected List<org.fao.sola.clients.android.opentenure.network.response.Boundary> doInBackground(Void... voids) {
                        return CommunityServerAPI.getBoundaries();
                    }

                    @Override
                    protected void onPostExecute(List<org.fao.sola.clients.android.opentenure.network.response.Boundary> boundaries) {
                        if (boundaries != null) {
                            org.fao.sola.clients.android.opentenure.model.Boundary.updateBoundariesFromResponse(boundaries);
                        }
                        update();
                        item.setEnabled(true);
                        Toast newToast = Toast.makeText(OpenTenureApplication.getContext(),
                                OpenTenureApplication.getContext().getString(R.string.action_boundaries_updated),
                                Toast.LENGTH_LONG);
                        newToast.show();
                    }
                };

                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                return true;
            case R.id.action_login:
                if (!Boolean.parseBoolean(Configuration.getConfigurationByName("isInitialized").getValue())) {
                    Toast toast;
                    String toastMessage = String.format(OpenTenureApplication
                            .getContext().getString(
                                    R.string.message_app_not_yet_initialized));
                    toast = Toast.makeText(OpenTenureApplication.getContext(), toastMessage, Toast.LENGTH_LONG);
                    toast.show();
                    return true;
                }

                OpenTenureApplication.setActivity(getActivity());

                Context context = getActivity().getApplicationContext();
                Intent intent2 = new Intent(context, LoginActivity.class);
                startActivity(intent2);

                OpenTenureApplication.setActivity(getActivity());
                return true;

            case R.id.action_logout:
                try {
                    LogoutTask logoutTask = new LogoutTask();
                    logoutTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getActivity());
                } catch (Exception e) {
                    Log.d("Details", "An error ");
                    e.printStackTrace();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        update();
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.boundaries_list, container, false);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        EditText inputSearch = (EditText) rootView.findViewById(R.id.filter_input_field);
        inputSearch.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable arg0) {
                filter = arg0.toString();
                ((BoundariesListAdapter) getListAdapter()).getFilter().filter(filter);
            }
        });

        update();

        if (savedInstanceState != null && savedInstanceState.getString(FILTER_KEY) != null) {
            filter = savedInstanceState.getString(FILTER_KEY);
            ((BoundariesListAdapter) getListAdapter()).getFilter().filter(filter);
        }
        return rootView;
    }

    @Override
    public void onResume() {
        update();
        if (filter != null) {
            ((BoundariesListAdapter) getListAdapter()).getFilter().filter(filter);
        }
        super.onResume();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(rootView.getContext(), BoundaryActivity.class);
        intent.putExtra(BoundaryActivity.BOUNDARY_ID_KEY, boundaries.get(position).getId());
        startActivityForResult(intent, 100);
    }

    protected void update() {
        boundaries = Boundary.getFormattedBoundariesAll(false);

        ArrayAdapter<Boundary> adapter = new BoundariesListAdapter(rootView.getContext(), boundaries);
        setListAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(FILTER_KEY, filter);
        super.onSaveInstanceState(outState);
    }

    public void refresh() {
        update();
    }
}
