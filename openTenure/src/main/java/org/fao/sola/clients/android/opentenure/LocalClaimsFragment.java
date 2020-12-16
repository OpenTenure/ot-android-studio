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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.filesystem.json.JsonUtilities;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.model.ClaimType;
import org.fao.sola.clients.android.opentenure.model.Configuration;
import org.fao.sola.clients.android.opentenure.network.LoginActivity;
import org.fao.sola.clients.android.opentenure.network.LogoutTask;
import org.fao.sola.clients.android.opentenure.tools.StringUtility;

import com.ipaulpro.afilechooser.utils.FileUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
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
import android.widget.TextView;
import android.widget.Toast;

public class LocalClaimsFragment extends ListFragment {

    private static final int CLAIM_RESULT = 100;
    private static final int REQUEST_IMPORT = 2404;
    private static final String FILTER_KEY = "filter";
    private static final String SHOW_DELETED_KEY = "show_deleted";
    private View rootView;
    private List<String> excludeClaimIds = new ArrayList<String>();
    private ModeDispatcher mainActivity;
    private String filter = null;
    private String fullPath = null;
    private MenuItem showDeletedMenuItem = null;
    private boolean showDeleted = false;
    private File dest;
    private LocalClaimsListAdapter.SortBy sortBy = LocalClaimsListAdapter.SortBy.CREATION_DATE_DESC;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mainActivity = (ModeDispatcher) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement ModeDispatcher");
        }
    }

    public LocalClaimsFragment() {
    }

    public void setExcludeClaimIds(List<String> excludeClaimIds) {
        this.excludeClaimIds = excludeClaimIds;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem itemIn;
        MenuItem itemOut;

        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.d(this.getClass().getName(), "Is the user logged in ? : "
                + OpenTenureApplication.isLoggedin());

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

        inflater.inflate(R.menu.local_claims, menu);
        setRetainInstance(true);

        showDeletedMenuItem = menu.findItem(R.id.action_show_deleted);
        showDeletedMenuItem.setChecked(showDeleted);

        if (mainActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RO) == 0) {
            menu.removeItem(R.id.action_new);
            menu.removeItem(R.id.action_login);
            menu.removeItem(R.id.action_logout);
            menu.removeItem(R.id.action_show_deleted);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle item selection

        switch (item.getItemId()) {
            case R.id.action_backup_db:
                OpenTenureApplication.getInstance().getDatabase().exportDB();
                String backupMessage = String.format(OpenTenureApplication
                        .getContext().getString(R.string.message_db_backed_up));

                Toast backupToast = Toast.makeText(
                        OpenTenureApplication.getContext(), backupMessage,
                        Toast.LENGTH_LONG);
                backupToast.show();
                return true;
            case R.id.action_new:
                if (!Boolean.parseBoolean(Configuration.getConfigurationByName("isInitialized").getValue())) {
                    String newMessage = String.format(OpenTenureApplication
                            .getContext().getString(
                                    R.string.message_app_not_yet_initialized));

                    Toast newToast = Toast.makeText(OpenTenureApplication.getContext(), newMessage, Toast.LENGTH_LONG);
                    newToast.show();

                    return true;
                }

                AlertDialog.Builder dlgCreateClaim = new AlertDialog.Builder(getContext());
                dlgCreateClaim.setTitle(R.string.confirm);
                dlgCreateClaim.setMessage(getContext().getString(R.string.confirm_create_claim));

                dlgCreateClaim.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(rootView.getContext(), ClaimActivity.class);

                        intent.putExtra(ClaimActivity.CLAIM_ID_KEY, ClaimActivity.CREATE_CLAIM_ID);
                        intent.putExtra(ClaimActivity.MODE_KEY, mainActivity.getMode().toString());
                        startActivityForResult(intent, CLAIM_RESULT);
                    }
                });
                dlgCreateClaim.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                dlgCreateClaim.show();

                return true;
            case R.id.action_import_zip:

                if (!Boolean.parseBoolean(Configuration.getConfigurationByName("isInitialized").getValue())) {
                    String newMessage = String.format(OpenTenureApplication
                            .getContext().getString(
                                    R.string.message_app_not_yet_initialized));

                    Toast newToast = Toast.makeText(OpenTenureApplication.getContext(), newMessage, Toast.LENGTH_LONG);
                    newToast.show();

                    return true;
                }

                Intent getContentIntent = FileUtils.createGetContentIntent();
                Intent intent = Intent.createChooser(getContentIntent, getResources()
                        .getString(R.string.choose_file));

                try {
                    startActivityForResult(intent, REQUEST_IMPORT);
                } catch (Exception e) {
                    Log.d(this.getClass().getName(), "Unable to start file chooser intent due to " + e.getMessage());
                }
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
            case R.id.action_show_deleted:
                showDeleted = !showDeletedMenuItem.isChecked();
                showDeletedMenuItem.setChecked(showDeleted);
                update();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQUEST_IMPORT:
                if (resultCode == com.ipaulpro.afilechooser.FileChooserActivity.RESULT_OK) {

                    Uri uri = data.getData();

                    fullPath = FileUtils.getPath(rootView.getContext(), uri);

                    Log.d(this.getClass().getName(), "Selected file: " + fullPath);

                    if (fullPath.endsWith(".zip")) {

                        dest = FileSystemUtilities.copyFileInImportFolder(new File(
                                fullPath));

                        if (!dest.exists() || !dest.isFile()) {

                            String newMessage = "Error preparing import of "
                                    + fullPath;

                            Toast newToast = Toast.makeText(rootView.getContext(),
                                    newMessage, Toast.LENGTH_LONG);
                            newToast.show();

                            return;
                        }
                    } else {

                        String newMessage = OpenTenureApplication
                                .getContext()
                                .getString(
                                        R.string.message_claim_import_not_claim_archive);

                        Toast newToast = Toast.makeText(rootView.getContext(),
                                newMessage, Toast.LENGTH_LONG);
                        newToast.show();

                        return;
                    }

                    AlertDialog.Builder metadataDialog = new AlertDialog.Builder(
                            rootView.getContext());

                    metadataDialog.setTitle(R.string.password);

                    final EditText input = new EditText(rootView.getContext());

                    input.setInputType(InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    input.setTransformationMethod(PasswordTransformationMethod
                            .getInstance());
                    metadataDialog.setView(input);

                    metadataDialog.setPositiveButton(R.string.confirm,
                            new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {

                                    String password = input.getText().toString();
                                    dialog.dismiss();

                                    new PreImportTask(rootView.getContext())
                                            .execute(password, dest);

                                    return;

                                }
                            });

                    metadataDialog.setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog,
                                                    int which) {

                                    try {
                                        FileSystemUtilities
                                                .deleteFilesInFolder(dest
                                                        .getParentFile());
                                    } catch (IOException e) {
                                        // TODO Auto-generated catch block
                                        System.out.println("Error deleting files "
                                                + e.getLocalizedMessage());
                                        e.printStackTrace();
                                    }
                                    return;
                                }
                            });

                    metadataDialog.show();

                }

            default:
                update();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.local_claims_list, container,false);
        setHasOptionsMenu(true);
        setRetainInstance(true);
        EditText inputSearch = (EditText) rootView.findViewById(R.id.filter_input_field);
        Button btnSortByCreationDate = (Button) rootView.findViewById(R.id.btnSortByCreationDate);
        Button btnSortByClaimDesc = (Button) rootView.findViewById(R.id.btnSortByClaimDesc);
        Button btnSortByClaimNum = (Button) rootView.findViewById(R.id.btnSortByClaimNum);
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
                ((LocalClaimsListAdapter) getListAdapter()).getFilter().filter(filter);
            }
        });

        if (savedInstanceState != null) {
            if (savedInstanceState.getString(FILTER_KEY) != null) {
                filter = savedInstanceState.getString(FILTER_KEY);
                //((LocalClaimsListAdapter) getListAdapter()).getFilter().filter(filter);
            }
            showDeleted = savedInstanceState.getBoolean(SHOW_DELETED_KEY);
        }

        View.OnClickListener sortListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getId() == R.id.btnSortByCreationDate){
                    btnSortByClaimDesc.setCompoundDrawablesWithIntrinsicBounds(R.drawable.sort, 0, 0, 0);
                    btnSortByClaimNum.setCompoundDrawablesWithIntrinsicBounds(R.drawable.sort, 0, 0, 0);

                    if(sortBy == LocalClaimsListAdapter.SortBy.CREATION_DATE_DESC) {
                        sortBy = LocalClaimsListAdapter.SortBy.CREATION_DATE_ASC;
                        btnSortByCreationDate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ascending, 0, 0, 0);
                    } else {
                        sortBy = LocalClaimsListAdapter.SortBy.CREATION_DATE_DESC;
                        btnSortByCreationDate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.descending, 0, 0, 0);
                    }
                } else if(v.getId() == R.id.btnSortByClaimDesc){
                    btnSortByCreationDate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.sort, 0, 0, 0);
                    btnSortByClaimNum.setCompoundDrawablesWithIntrinsicBounds(R.drawable.sort, 0, 0, 0);

                    if(sortBy == LocalClaimsListAdapter.SortBy.CLAIM_NAME_DESC) {
                        sortBy = LocalClaimsListAdapter.SortBy.CLAIM_NAME_ASC;
                        btnSortByClaimDesc.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ascending, 0, 0, 0);
                    } else {
                        sortBy = LocalClaimsListAdapter.SortBy.CLAIM_NAME_DESC;
                        btnSortByClaimDesc.setCompoundDrawablesWithIntrinsicBounds(R.drawable.descending, 0, 0, 0);
                    }
                } else if(v.getId() == R.id.btnSortByClaimNum){
                    btnSortByCreationDate.setCompoundDrawablesWithIntrinsicBounds(R.drawable.sort, 0, 0, 0);
                    btnSortByClaimDesc.setCompoundDrawablesWithIntrinsicBounds(R.drawable.sort, 0, 0, 0);

                    if(sortBy == LocalClaimsListAdapter.SortBy.CLAIM_NUM_DESC) {
                        sortBy = LocalClaimsListAdapter.SortBy.CLAIM_NUM_ASC;
                        btnSortByClaimNum.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ascending, 0, 0, 0);
                    } else {
                        sortBy = LocalClaimsListAdapter.SortBy.CLAIM_NUM_DESC;
                        btnSortByClaimNum.setCompoundDrawablesWithIntrinsicBounds(R.drawable.descending, 0, 0, 0);
                    }
                }

                ((LocalClaimsListAdapter) getListAdapter()).setSortBy(sortBy);
            }
        };

        btnSortByCreationDate.setOnClickListener(sortListener);
        btnSortByClaimDesc.setOnClickListener(sortListener);
        btnSortByClaimNum.setOnClickListener(sortListener);

        update();
        OpenTenureApplication.setLocalClaimsFragment(this);

        return rootView;
    }

    @Override
    public void onResume() {
        update();
        if (filter != null) {
            ((LocalClaimsListAdapter) getListAdapter()).getFilter().filter(filter);
        }
        ((LocalClaimsListAdapter) getListAdapter()).setSortBy(sortBy);
        super.onResume();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        String claimId = ((TextView) v.findViewById(R.id.claim_id)).getText().toString();
        Claim claim = Claim.getClaim(claimId);

        if (mainActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RW) == 0) {
            Intent intent = new Intent(rootView.getContext(), ClaimActivity.class);
            intent.putExtra(ClaimActivity.CLAIM_ID_KEY, claimId);

            if ((!claim.getStatus().equalsIgnoreCase(ClaimStatus._CREATED)
                    && !claim.getStatus().equalsIgnoreCase(ClaimStatus._UPLOAD_ERROR)
                    && !claim.getStatus().equalsIgnoreCase(ClaimStatus._UPLOAD_INCOMPLETE))
                    || claim.isDeleted()) {
                intent.putExtra(ClaimActivity.MODE_KEY, ModeDispatcher.Mode.MODE_RO.toString());
            } else {
                intent.putExtra(ClaimActivity.MODE_KEY, mainActivity.getMode().toString());
            }
            startActivityForResult(intent, CLAIM_RESULT);
        } else {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(ClaimActivity.CLAIM_ID_KEY, claimId);
            getActivity().setResult(SelectClaimActivity.SELECT_CLAIM_ACTIVITY_RESULT, resultIntent);
            getActivity().finish();
        }
    }

    protected void update() {
        List<Claim> claims = Claim.getSimplifiedClaimsForList();
        List<ClaimListTO> claimListTOs = new ArrayList<ClaimListTO>();
        DisplayNameLocalizer dnl = new DisplayNameLocalizer(OpenTenureApplication.getInstance().getLocalization());

        for (Claim claim : claims) {
            if (excludeClaimIds != null && !excludeClaimIds.contains(claim.getClaimId())) {
                if((claim.isDeleted() && showDeleted) || (!claim.isDeleted() && !showDeleted)){
                    ClaimListTO cto = new ClaimListTO();
                    String claimName = claim.getName().equalsIgnoreCase("") ? rootView
                            .getContext().getString(R.string.default_claim_name) : claim.getName();
                    String claimDate = claim.getDateOfStart() != null ? claim.getDateOfStart().toString() : "...";

                    String slogan = claim.getSlogan(getContext());

                    if(!StringUtility.isEmpty(claim.getType())){
                        slogan += ", " + OpenTenureApplication.getContext().getResources().getString(R.string.type)
                                + ": " + dnl.getLocalizedDisplayName(new ClaimType().getDisplayValueByType(claim.getType()));
                    }

                    slogan += ", " + OpenTenureApplication.getContext().getResources().getString(R.string.occupiedSince) + ": " + claimDate ;

                    if (claim.getRecorderName() != null) {
                        slogan += "\r\n" + OpenTenureApplication.getContext().getResources()
                                .getString(R.string.recorded_by) + " " + claim.getRecorderName();
                    }

                    cto.setName(claimName);
                    cto.setSlogan(slogan);
                    cto.setDeleted(claim.isDeleted());
                    cto.setId(claim.getClaimId());
                    cto.setModifiable(claim.isModifiable());
                    cto.setDateOfStart(claim.getDateOfStart());
                    if(claim.getPerson() != null){
                        cto.setPersonId(claim.getPerson().getPersonId());
                    }
                    cto.setAttachments(claim.getAttachments());
                    cto.setCreationDate(claim.getCreationDate());

                    if (claim.getClaimNumber() != null)
                        cto.setNumber(claim.getClaimNumber());
                    else
                        cto.setNumber("");

                    cto.setStatus(claim.getStatus());

                    int days = JsonUtilities.remainingDays(claim.getChallengeExpiryDate());

                    if (claim.isUploadable())
                        cto.setRemaingDays(getResources().getString(
                                R.string.message_remaining_days) + days);
                    else
                        cto.setRemaingDays("");

                    claimListTOs.add(cto);
                }
            }
        }

        OpenTenureApplication.getInstance().clearClaimsList();

        ArrayAdapter<ClaimListTO> adapter = new LocalClaimsListAdapter(
                rootView.getContext(), claimListTOs, mainActivity.getMode());
        setListAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(FILTER_KEY, filter);
        if(showDeletedMenuItem != null) {
            outState.putBoolean(SHOW_DELETED_KEY, showDeletedMenuItem.isChecked());
        }
        super.onSaveInstanceState(outState);
    }

    public void refresh() {
        // the list of changing claims is no more necessary. before rendering
        // the list of claims is cleaned

        update();
    }

}
