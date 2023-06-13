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
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import org.fao.sola.clients.android.opentenure.model.Boundary;
import org.fao.sola.clients.android.opentenure.model.BoundaryStatus;
import org.fao.sola.clients.android.opentenure.model.BoundaryType;
import org.fao.sola.clients.android.opentenure.tools.StringUtility;

import java.util.List;
import java.util.Map;

public class BoundaryDetailsFragment extends Fragment {

    View rootView;
    private BoundaryActivity boundaryActivity;
    private Map<String, String> boundaryTypes;
    private Map<String, String> boundaryStatuses;
    private List<Boundary> parentBoundaries;
    private EditText txtName;
    private EditText txtAuthority;
    private Spinner cbxParent;
    private EditText txtTypeName;
    private EditText txtStatusName;
    private String boundaryTypeCode = null;
    private BoundaryDispatcher boundaryDispatcher;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            boundaryActivity = (BoundaryActivity) activity;
            boundaryDispatcher = (BoundaryDispatcher) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must be BoundaryActivity implementing BoundaryDispatcher");
        }
    }

    public BoundaryDetailsFragment() {
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.boundary_details, menu);

        if(!editable()) {
            menu.removeItem(R.id.action_save);
        }

        super.onCreateOptionsMenu(menu, inflater);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_boundary_details, container, false);
        setHasOptionsMenu(true);

        // setRetainInstance(true);
        InputMethodManager imm = (InputMethodManager) rootView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);

        boolean isEditable = editable();

        // Get fields
        txtName = (EditText) rootView.findViewById(R.id.txtName);
        txtAuthority = (EditText) rootView.findViewById(R.id.txtAuthority);
        cbxParent = (Spinner) rootView.findViewById(R.id.cbxParent);
        txtTypeName = (EditText) rootView.findViewById(R.id.txtTypeName);
        txtStatusName = (EditText) rootView.findViewById(R.id.txtStatusName);

        txtName.setEnabled(isEditable);
        txtAuthority.setEnabled(isEditable);
        cbxParent.setEnabled(isEditable);

        parentBoundaries = Boundary.getFormattedParentBoundaries(true);
        cbxParent.setAdapter(new ArrayAdapter(OpenTenureApplication.getContext(), R.layout.my_spinner, parentBoundaries));
        ((ArrayAdapter) cbxParent.getAdapter()).setDropDownViewResource(R.layout.my_spinner);
        cbxParent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long arg3) {
                boundaryTypeCode = getBoundaryTypeCodeByParentId(((Boundary)cbxParent.getSelectedItem()).getId());
                setBoundaryTypeName();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        return rootView;
    }

    private boolean editable(){
        return boundaryActivity.getBoundary().getStatusCode().equals("pending") && !boundaryActivity.getBoundary().isProcessed();
    }

    @Override
    public void onResume() {
        load();
        super.onResume();
    }

    public void load() {
        Boundary boundary = boundaryActivity.getBoundary();

        if (boundary == null) {
            return;
        }

        if (OpenTenureApplication.getInstance().getLocale().toString().startsWith("ar")) {
            txtName.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            txtName.setTextDirection(View.TEXT_DIRECTION_LOCALE);

            txtAuthority.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);
            txtAuthority.setTextDirection(View.TEXT_DIRECTION_LOCALE);
        }

        if(boundary.getName() != null){
            txtName.setText(boundary.getName());
        } else {
            txtName.setText("");
        }

        if(boundary.getAuthorityName() != null){
            txtAuthority.setText(boundary.getAuthorityName());
        } else {
            txtAuthority.setText("");
        }

        if(boundary.getParentId() != null && parentBoundaries != null){
            for (int i = 0; i < parentBoundaries.size(); i++) {
                if (boundary.getParentId().equals(parentBoundaries.get(i).getId())) {
                    cbxParent.setSelection(i);
                    break;
                }
            }
        }

        boundaryTypes = BoundaryType.getKeyValueMap(false);
        boundaryStatuses = BoundaryStatus.getKeyValueMap(false);
        boundaryTypeCode = boundary.getTypeCode();

        setBoundaryTypeName();
        setBoundaryStatusName();
    }

    private void setBoundaryTypeName(){
        if(boundaryTypes == null || boundaryTypeCode == null || boundaryTypeCode.equals("")){
            txtTypeName.setText("");
        } else {
            for (String code : boundaryTypes.keySet()) {
                if(boundaryTypeCode.equals(code)){
                    txtTypeName.setText(boundaryTypes.get(code));
                    break;
                }
            }
        }
    }

    private void setBoundaryStatusName(){
        Boundary boundary = boundaryActivity.getBoundary();
        if(boundaryStatuses == null || boundary.getStatusCode() == null || boundary.getStatusCode().equals("")){
            txtStatusName.setText("");
        } else {
            for (String code : boundaryStatuses.keySet()) {
                if(boundary.getStatusCode().equals(code)){
                    txtStatusName.setText(boundaryStatuses.get(code));
                    break;
                }
            }
        }
    }

    public String getBoundaryTypeCodeByParentId(String parentId) {
        if (boundaryTypes == null || boundaryTypes.size() < 1) {
            return null;
        }

        Object[] typeCodes = boundaryTypes.keySet().toArray();

        if (parentId == null || parentId.equals("")) {
            return typeCodes[0].toString();
        }

        // Look for parent
        Boundary parent = Boundary.getById(parentId);
        if (parent == null) {
            return null;
        }

        if (parent.getTypeCode() == null || parent.getTypeCode().equals("")) {
            return null;
        }

        for (int i = 0; i < typeCodes.length; i++) {
            if (typeCodes[i].toString().equals(parent.getTypeCode())) {
                if (i < typeCodes.length - 1) {
                    // Return next level
                    return typeCodes[i + 1].toString();
                } else {
                    // Return current level
                    return typeCodes[i].toString();
                }
            }
        }
        return null;
    }

    public boolean save() {
        // Validate fields
        Toast toast;
        Boundary boundary = boundaryActivity.getBoundary();

        if(txtName.getText().toString().replace(" ", "").equals("")){
            toast = Toast.makeText(rootView.getContext(), R.string.message_error_boundary_name_empty, Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }

        if(boundaryTypeCode == null || boundaryTypeCode.equals("")){
            toast = Toast.makeText(rootView.getContext(), R.string.message_error_boundary_type_empty, Toast.LENGTH_SHORT);
            toast.show();
            return false;
        }

        Boundary parent = (Boundary)cbxParent.getSelectedItem();

        if (parent.getId() == null || parent.getId().equals("")) {
            boundary.setParentId(null);
        } else {
            // Check parent
            if (boundary.getId().equalsIgnoreCase(parent.getId())) {
                toast = Toast.makeText(rootView.getContext(), R.string.message_error_boundary_same_as_parent, Toast.LENGTH_SHORT);
                toast.show();
                return false;
            }
            // Get child records
            List<Boundary> childBoundaries = Boundary.getAllChildrenBoundaries(boundary.getId());
            if (childBoundaries != null && childBoundaries.size() > 0) {
                for (Boundary child : childBoundaries) {
                    if (child.getId().equalsIgnoreCase(boundary.getParentId())) {
                        toast = Toast.makeText(rootView.getContext(), R.string.message_error_boundary_parent_is_child, Toast.LENGTH_SHORT);
                        toast.show();
                        return false;
                    }
                }
            }
            boundary.setParentId(parent.getId());
        }

        boundary.setName(txtName.getText().toString().replace(" ", ""));
        boundary.setTypeCode(boundaryTypeCode);
        boundary.setAuthorityName(txtAuthority.getText().toString().replace(" ", ""));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                boundaryDispatcher.onSave();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean hasChanges() {
        Boundary boundary = boundaryActivity.getBoundary();

        if (!StringUtility.empty(boundary.getName()).equals(txtName.getText().toString())) {
            return true;
        }

        if (!StringUtility.empty(boundary.getAuthorityName()).equals(txtAuthority.getText().toString())) {
            return true;
        }

        String parentId = ((Boundary) cbxParent.getSelectedItem()).getId();
        if (!StringUtility.empty(boundary.getParentId()).equals(StringUtility.empty(parentId))) {
            return true;
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

}
