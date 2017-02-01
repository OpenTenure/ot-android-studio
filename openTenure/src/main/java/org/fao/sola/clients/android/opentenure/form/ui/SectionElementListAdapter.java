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
package org.fao.sola.clients.android.opentenure.form.ui;

import java.util.List;

import org.fao.sola.clients.android.opentenure.ModeDispatcher.Mode;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.form.SectionPayload;
import org.fao.sola.clients.android.opentenure.form.SectionTemplate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SectionElementListAdapter extends ArrayAdapter<SectionElementListTO> {
	private final SectionFragment fragment;
	private final Context context;
	private List<SectionElementListTO> sectionElements;
	private SectionTemplate sectionTemplate;
	private SectionPayload sectionPayload;
	private LayoutInflater inflater;
	private Mode mode;

	public SectionElementListAdapter(SectionFragment fragment, Context context, List<SectionElementListTO> sectionElements, SectionPayload sectionPayload, SectionTemplate sectionTemplate, Mode mode) {
		super(context, R.layout.multiple_field_group_list_item, sectionElements);
		this.fragment = fragment;
		this.context = context;
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.sectionElements = sectionElements;
		this.sectionTemplate = sectionTemplate;
		this.sectionPayload = sectionPayload;
		this.mode = mode;
	}
	
	static class ViewHolder {
		TextView name;
		TextView slogan;
		ImageView removeIcon;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder vh;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.multiple_field_group_list_item,
					parent, false);
			vh = new ViewHolder();
			vh.name = (TextView) convertView.findViewById(R.id.field_group_name);
			vh.slogan = (TextView) convertView.findViewById(R.id.field_group_slogan);
			vh.removeIcon = (ImageView) convertView
					.findViewById(R.id.remove_icon);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		vh.name.setTextSize(8);
		vh.name.setText(sectionElements.get(position).getName());

		vh.slogan.setText(sectionElements.get(position).getSlogan());
		vh.slogan.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context,
						SectionElementActivity.class);
				intent.putExtra(SectionElementActivity.SECTION_ELEMENT_POSITION_KEY, position);
				intent.putExtra(SectionElementActivity.SECTION_ELEMENT_PAYLOAD_KEY, sectionElements.get(position).getJson());
				intent.putExtra(SectionElementActivity.SECTION_TEMPLATE_KEY, sectionTemplate.toJson());
				intent.putExtra(SectionElementActivity.MODE_KEY, mode.toString());
				intent.putExtra(SectionElementActivity.HIDE_SAVE_BUTTON_KEY, true);
				fragment.startActivityForResult(intent, SectionElementActivity.SECTION_ELEMENT_ACTIVITY_REQUEST_CODE);
			}
			
		});

		if(mode == Mode.MODE_RW){
			vh.removeIcon.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					AlertDialog.Builder removeOwnerDialog = new AlertDialog.Builder(
							context);
					removeOwnerDialog
							.setTitle(R.string.action_remove_section_element);
					removeOwnerDialog.setMessage(sectionElements.get(position).getSlogan()
							+ ": "
							+ context.getResources().getString(
									R.string.message_remove_section_element));

					removeOwnerDialog.setPositiveButton(
							R.string.confirm,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									sectionElements.remove(position);
									sectionPayload.getSectionElementPayloadList().remove(position);
									Toast.makeText(context,
											R.string.section_element_removed,
											Toast.LENGTH_SHORT).show();
									notifyDataSetChanged();
								}
							});
					removeOwnerDialog.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							});

					removeOwnerDialog.show();

				}
			});
		}else{
			((ViewManager)convertView).removeView(vh.removeIcon);
		}

		return convertView;
	}
}