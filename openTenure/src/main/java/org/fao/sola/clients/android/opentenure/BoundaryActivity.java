/**
 * ******************************************************************************************
 * <p>
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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;

import org.fao.sola.clients.android.opentenure.maps.BoundaryMapFragment;
import org.fao.sola.clients.android.opentenure.model.Boundary;
import org.fao.sola.clients.android.opentenure.tools.StringUtility;

import java.util.Locale;
import java.util.UUID;

public class BoundaryActivity extends FragmentActivity implements BoundaryDispatcher {

    public static final String BOUNDARY_ID_KEY = "boundaryId";
    private Boundary boundary;
    private boolean isNew = false;
    private BoundaryDetailsFragment boundaryDetailsFragment;
    private BoundaryMapFragment boundaryMapFragment;

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;
    PagerSlidingTabStrip tabs;
    SparseArray<Fragment> fragmentReferences = new SparseArray<Fragment>();

    @Override
    public void onDestroy() {
        OpenTenureApplication.getInstance().getDatabase().sync();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            goBack();
            return false;
        } else
            return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onPause() {
        super.onPause();
        OpenTenureApplication.getInstance().getDatabase().sync();
    }

    @Override
    public void onResume() {
        OpenTenureApplication.getInstance().getDatabase().open();
        OpenTenure.setLocale(this);
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        OpenTenure.setLocale(this);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boundary);

        String boundaryId = null;

        if (savedInstanceState != null) {
            boundaryId = savedInstanceState.getString(BOUNDARY_ID_KEY);
        }

        if (boundaryId == null) {
            boundaryId = getIntent().getExtras().getString(BOUNDARY_ID_KEY);
        }

        if (boundaryId == null || boundaryId.equals("")) {
            boundary = new Boundary();
            boundary.setId(UUID.randomUUID().toString());
            boundary.setStatusCode("pending");
            boundary.setProcessed(false);
            isNew = true;
        } else {
            boundary = Boundary.getById(boundaryId);
            isNew = false;
        }

        if (!StringUtility.isEmpty(boundary.getName())) {
            setTitle(boundary.getName());
        } else {
            setTitle(getResources().getString(R.string.boundary_new));
        }

        tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        mViewPager = (ViewPager) findViewById(R.id.boundary_pager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabs.setIndicatorColor(getResources().getColor(R.color.ab_tab_indicator_opentenure));
        tabs.setViewPager(mViewPager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This is called when the Home (Up) button is pressed in the action bar.
                goBack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void goBack() {

        if (boundaryDetailsFragment != null) {
            if (boundaryDetailsFragment.hasChanges()) {
                // Show message
                AlertDialog.Builder saveChangesDialog = new AlertDialog.Builder(boundaryDetailsFragment.getActivity());
                saveChangesDialog.setTitle(R.string.title_save_boundary_dialog);
                saveChangesDialog.setMessage(OpenTenureApplication.getContext().getString(R.string.message_discard_changes));

                saveChangesDialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                         navigateToParent();
                    }
                });

                saveChangesDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
                saveChangesDialog.show();
            } else {
                navigateToParent();
            }
        } else {
            navigateToParent();
        }
    }

    private void navigateToParent() {
        Intent upIntent;
        upIntent = NavUtils.getParentActivityIntent(this);
        upIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(upIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.claim, menu);
        return true;
    }

    public Boundary getBoundary() {
        return boundary;
    }

    public void setBoundary(Boundary boundary) {
        this.boundary = boundary;
    }

    @Override
    public boolean onSave() {
        if (boundaryDetailsFragment != null) {
            // Validate geometry
            String geom = "";
            if (boundaryMapFragment != null) {
                geom = boundaryMapFragment.getGeom();
                if (StringUtility.isEmpty(geom)) {
					Toast toast = Toast.makeText(getBaseContext(), R.string.message_error_boundary_geom_empty, Toast.LENGTH_SHORT);
					toast.show();
					return false;
                }
                boundary.setGeom(geom);
            }

            boolean result = boundaryDetailsFragment.save();
            if (!result) {
                return result;
            }

            if (isNew) {
                boundary.insert();
            } else {
                boundary.update();
            }
            navigateToParent();
        }
        return true;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void destroyItem(android.view.ViewGroup container, int position, Object object) {
        }

        @Override
        public Fragment getItem(int position) {

            Fragment fragment = null;
            switch (position) {
                case 0:
                    boundaryDetailsFragment = new BoundaryDetailsFragment();
                    fragment = boundaryDetailsFragment;
                    break;
                case 1:
                    boundaryMapFragment = new BoundaryMapFragment();
                    fragment = boundaryMapFragment;
                    break;
            }
            fragmentReferences.put(position, fragment);
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();

            switch (position) {
                case 0:
                    return getString(R.string.title_boundary_details).toUpperCase(l);
                case 1:
                    return getString(R.string.title_boundary_map).toUpperCase(l);
            }
            return "";
        }
    }
}
