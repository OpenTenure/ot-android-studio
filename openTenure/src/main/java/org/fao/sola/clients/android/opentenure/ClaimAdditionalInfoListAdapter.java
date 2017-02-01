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
package org.fao.sola.clients.android.opentenure;

import java.util.List;

import org.fao.sola.clients.android.opentenure.model.AdditionalInfo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ClaimAdditionalInfoListAdapter extends ArrayAdapter<String> {
		
	  private final Context context;
	  private final List<String> slogans;
	  private final List<String> ids;
	  private ModeDispatcher.Mode mode;
	
	
	public ClaimAdditionalInfoListAdapter(Context context, List<String> slogans, List<String> ids, ModeDispatcher.Mode mode) {
	    super(context, R.layout.claim_additional_info_list_item, slogans);
	    this.context = context;
	    this.slogans = slogans;
	    this.ids = ids;
	    this.mode = mode;
	  }
	
	@Override
	  public View getView(final int position, View convertView, ViewGroup parent) {
	    LayoutInflater inflater = (LayoutInflater) context
	        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.claim_additional_info_list_item, parent, false);
	    TextView slogan = (TextView) rowView.findViewById(R.id.additional_info_description);
	    TextView id = (TextView) rowView.findViewById(R.id.additional_info_id);
	    slogan.setText(slogans.get(position));
	    id.setTextSize(8);
	    id.setText(ids.get(position));

	    if (mode.compareTo(ModeDispatcher.Mode.MODE_RW) == 0) {

	    	ImageView picture = (ImageView) rowView.findViewById(R.id.remove_icon);

		    picture.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					AlertDialog.Builder confirmNewPasswordDialog = new AlertDialog.Builder(context);
					confirmNewPasswordDialog.setTitle(R.string.action_remove_additional_info);
					confirmNewPasswordDialog.setMessage(slogans.get(position) + ": "+ context.getResources().getString(R.string.message_remove_additional_info));

					confirmNewPasswordDialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							AdditionalInfo.getAdditionalInfo(ids.get(position)).delete();
							Toast
							.makeText(context,
									R.string.additional_info_removed,
									Toast.LENGTH_SHORT).show();
							slogans.remove(position);
							ids.remove(position);
							notifyDataSetChanged();
						}
					});
					confirmNewPasswordDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});

					confirmNewPasswordDialog.show();
					
				}
			});
		}else{
			rowView.findViewById(R.id.remove_icon).setVisibility(View.INVISIBLE);
		}
	    return rowView;
	    
	}
	
	
	
	
	
	

}
