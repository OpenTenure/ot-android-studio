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

import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.model.Person;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AdjacentClaimsListAdapter extends
		ArrayAdapter<AdjacentClaimListTO> {
	private final Context context;
	private List<AdjacentClaimListTO> claims;
	LayoutInflater inflater;

	public AdjacentClaimsListAdapter(Context context,
			List<AdjacentClaimListTO> claims) {
		super(context, R.layout.adjacent_claims_list_item, claims);
		this.context = context;
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.claims = claims;
	}

	static class ViewHolder {
		TextView id;
		TextView slogan;
		TextView status;
		TextView cardinalDirection;
		ImageView picture;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.adjacent_claims_list_item,
					parent, false);
			vh = new ViewHolder();
			vh.slogan = (TextView) convertView.findViewById(R.id.claim_slogan);
			vh.id = (TextView) convertView.findViewById(R.id.claim_id);
			vh.status = (TextView) convertView.findViewById(R.id.claim_status);
			vh.cardinalDirection = (TextView) convertView
					.findViewById(R.id.cardinal_direction);
			vh.picture = (ImageView) convertView
					.findViewById(R.id.claimant_picture);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		
		
		vh.slogan.setText(claims.get(position).getSlogan());
		if(claims.get(position).getStatus().equals(ClaimStatus._CHALLENGED))
		vh.status.setText(ClaimStatus._CHALLENGED);
		if(claims.get(position).getStatus().equals(ClaimStatus._CREATED))
			vh.status.setText(R.string.created);
		if(claims.get(position).getStatus().equals(ClaimStatus._MODERATED))
			vh.status.setText(R.string.moderated);
		if(claims.get(position).getStatus().equals(ClaimStatus._UNMODERATED))
			vh.status.setText(R.string.unmoderated);
		if(claims.get(position).getStatus().equals(ClaimStatus._UPDATE_ERROR))
			vh.status.setText(R.string.update_error);
		if(claims.get(position).getStatus().equals(ClaimStatus._UPDATE_ERROR))
			vh.status.setText(R.string.update_error);
		if(claims.get(position).getStatus().equals(ClaimStatus._UPDATE_INCOMPLETE))
			vh.status.setText(R.string.update_incomplete);
		if(claims.get(position).getStatus().equals(ClaimStatus._UPDATING))
			vh.status.setText(R.string.updating);
		if(claims.get(position).getStatus().equals(ClaimStatus._UPLOAD_ERROR))
			vh.status.setText(R.string.upload_error);
		if(claims.get(position).getStatus().equals(ClaimStatus._UPLOAD_INCOMPLETE))
			vh.status.setText(R.string.upload_incomplete);
		if(claims.get(position).getStatus().equals(ClaimStatus._UPLOADING))
			vh.status.setText(R.string.uploading);
		if(claims.get(position).getStatus().equals(ClaimStatus._REJECTED))
			vh.status.setText(R.string.rejected);
		if(claims.get(position).getStatus().equals(ClaimStatus._REVIEWED))
			vh.status.setText(R.string.reviewed);
		
		vh.id.setTextSize(8);
		vh.id.setText(claims.get(position).getId());
		vh.cardinalDirection.setText(claims.get(position)
				.getCardinalDirection());
		vh.picture.setImageBitmap(Person.getPersonPicture(context, Claim
				.getClaim(claims.get(position).getId()).getPerson()
				.getPersonId(), 96));
		
		if(OpenTenureApplication.getInstance().getLocale().toString().startsWith("ar")){
		vh.slogan.setTextAlignment(View.TEXT_DIRECTION_LOCALE);
		vh.id.setTextAlignment(View.TEXT_DIRECTION_LOCALE);
		vh.status.setTextAlignment(View.TEXT_DIRECTION_LOCALE);
		vh.cardinalDirection.setTextAlignment(View.TEXT_DIRECTION_LOCALE);
		}
		return convertView;
	}
}