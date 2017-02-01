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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.fao.sola.clients.android.opentenure.button.listener.SubmitClaimListener;
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.model.Person;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ChallengingClaimsListAdapter extends ArrayAdapter<ClaimListTO> implements
		Filterable {
	private final Context context;
	private final List<ClaimListTO> originalClaims;
	private List<ClaimListTO> filteredClaims;
	private List<ClaimListTO> claims;
	LayoutInflater inflater;

	public ChallengingClaimsListAdapter(Context context, List<ClaimListTO> claims,
			ModeDispatcher.Mode mode) {
		super(context, R.layout.claims_list_item, claims);
		this.context = context;
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.originalClaims = new ArrayList<ClaimListTO>(claims);
		this.claims = claims;
		this.filteredClaims = null;
	}

	@Override
	public Filter getFilter() {

		Filter filter = new Filter() {

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {

				String filterString = constraint.toString();

				filteredClaims = new ArrayList<ClaimListTO>();
				for (ClaimListTO cto : originalClaims) {
					String lcase = cto.getSlogan().toLowerCase(
							Locale.getDefault());
					if (lcase.contains(filterString.toLowerCase(Locale
							.getDefault()))) {
						filteredClaims.add(cto);
					}
				}

				FilterResults results = new FilterResults();
				results.count = filteredClaims.size();
				results.values = filteredClaims;
				return results;
			}

			@Override
			protected void publishResults(CharSequence constraint,
					FilterResults results) {
				claims = (ArrayList<ClaimListTO>) results.values;

				if (results.count > 0) {
					notifyDataSetChanged();
				} else {
					notifyDataSetInvalidated();
				}
			}
		};
		return filter;
	}

	@Override
	public int getCount() {
		return claims.size();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.claims_list_item, parent,
					false);
			vh = new ViewHolder();
			vh.slogan = (TextView) convertView.findViewById(R.id.claim_slogan);
			vh.number = (TextView) convertView.findViewById(R.id.claim_number);
			vh.id = (TextView) convertView.findViewById(R.id.claim_id);
			vh.status = (TextView) convertView.findViewById(R.id.claim_status);
			vh.iconUnmoderated = (ImageView) convertView
					.findViewById(R.id.status_unmoderated);
			vh.iconLocal = (ImageView) convertView
					.findViewById(R.id.status_local);
			vh.iconModerated = (ImageView) convertView
					.findViewById(R.id.status_moderated);
			vh.iconChallenged = (ImageView) convertView
					.findViewById(R.id.status_challenged);
			vh.iconWithdrawn = (ImageView) convertView
					.findViewById(R.id.status_withdrawn);
			vh.iconReviewed = (ImageView) convertView
					.findViewById(R.id.status_reviewed);
			vh.challengeExpiryDate = (TextView) convertView
					.findViewById(R.id.claim_challenging_time);
			vh.picture = (ImageView) convertView
					.findViewById(R.id.claimant_picture);
			vh.send = (ImageView) convertView
					.findViewById(R.id.action_submit_to_server);
			vh.remove = (ImageView) convertView
					.findViewById(R.id.action_remove_claim);
			vh.remove.setVisibility(View.INVISIBLE);
			vh.send.setVisibility(View.INVISIBLE);
			
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
			vh.iconUnmoderated.setVisibility(View.GONE);
			vh.iconLocal.setVisibility(View.GONE);
			vh.iconModerated.setVisibility(View.GONE);
			vh.iconChallenged.setVisibility(View.GONE);
			vh.iconWithdrawn.setVisibility(View.GONE);
			vh.iconReviewed.setVisibility(View.GONE);
		}
		vh.slogan.setText(claims.get(position).getSlogan());

		vh.remove.setOnClickListener(new ClaimDeleteListener(claims.get(
				position).getId(), vh));
		vh.send.setOnClickListener(new SubmitClaimListener(claims.get(position)
				.getId(), vh));

		if (claims.get(position).getStatus().equals(ClaimStatus._CREATED)) {
			vh.number.setVisibility(View.GONE);
			vh.iconLocal.setVisibility(View.VISIBLE);
			vh.iconUnmoderated.setVisibility(View.GONE);

			vh.bar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
			vh.status.setVisibility(View.GONE);
			vh.bar.setVisibility(View.GONE);
			if (claims.get(position).isModifiable())
				vh.send.setVisibility(View.INVISIBLE);
			else
				vh.send.setVisibility(View.INVISIBLE);

		}
		if (claims.get(position).getStatus().equals(ClaimStatus._UPLOADING)) {

			vh.iconLocal.setVisibility(View.VISIBLE);
			vh.number.setTextSize(8);
			if (claims.get(position).getNumber() != null)
				vh.number.setText(claims.get(position).getNumber());
			vh.status.setTextColor(context.getResources().getColor(
					R.color.status_created));

			int progress = FileSystemUtilities.getUploadProgress(claims.get(position).getId(), claims.get(position).getStatus());

			// Setting the update value in the progress bar
			vh.bar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
			vh.bar.setVisibility(View.VISIBLE);
			vh.bar.setProgress(progress);

			vh.status.setText(claims.get(position).getStatus() + " " + progress
					+ " %");

			vh.status.setVisibility(View.VISIBLE);
			vh.send.setVisibility(View.INVISIBLE);

		}
		if (claims.get(position).getStatus().equals(ClaimStatus._UPDATING)) {

			vh.iconLocal.setVisibility(View.GONE);
			vh.iconUnmoderated.setVisibility(View.VISIBLE);
			vh.number.setTextSize(8);
			if (claims.get(position).getNumber() != null)
				vh.number.setText(claims.get(position).getNumber());
			vh.status.setTextColor(context.getResources().getColor(
					R.color.status_created));

			int progress = FileSystemUtilities.getUploadProgress(claims.get(position).getId(), claims.get(position).getStatus());

			// Setting the update value in the progress bar
			vh.bar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
			vh.bar.setVisibility(View.VISIBLE);
			vh.bar.setProgress(progress);

			vh.status.setText(claims.get(position).getStatus() + " " + progress
					+ " %");

			vh.status.setVisibility(View.VISIBLE);
			vh.send.setVisibility(View.INVISIBLE);
		}
		if (claims.get(position).getStatus().equals(ClaimStatus._UPLOAD_ERROR)) {

			vh.iconLocal.setVisibility(View.VISIBLE);
			vh.number.setTextSize(8);
			if (claims.get(position).getNumber() != null)
				vh.number.setText(claims.get(position).getNumber());
			vh.status.setTextColor(context.getResources().getColor(
					R.color.status_created));
			vh.status.setText(claims.get(position).getStatus());
			vh.bar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
			vh.bar.setVisibility(View.GONE);

			vh.status.setVisibility(View.VISIBLE);
			if (claims.get(position).isModifiable())
				vh.send.setVisibility(View.INVISIBLE);
			else
				vh.send.setVisibility(View.INVISIBLE);

		}
		if (claims.get(position).getStatus().equals(ClaimStatus._UPDATE_ERROR)) {

			vh.iconLocal.setVisibility(View.GONE);
			vh.iconUnmoderated.setVisibility(View.VISIBLE);
			vh.number.setTextSize(8);
			if (claims.get(position).getNumber() != null)
				vh.number.setText(claims.get(position).getNumber());
			vh.status.setTextColor(context.getResources().getColor(
					R.color.status_created));
			vh.status.setText(claims.get(position).getStatus());
			vh.bar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
			vh.bar.setVisibility(View.GONE);

			vh.status.setVisibility(View.VISIBLE);

			vh.send.setVisibility(View.INVISIBLE);
		}
		if (claims.get(position).getStatus().equals(ClaimStatus._UPLOAD_INCOMPLETE)) {

			vh.iconLocal.setVisibility(View.VISIBLE);
			vh.number.setTextSize(8);
			if (claims.get(position).getNumber() != null)
				vh.number.setText(claims.get(position).getNumber());
			vh.status.setTextColor(context.getResources().getColor(
					R.color.status_created));

			int progress = FileSystemUtilities.getUploadProgress(claims.get(position).getId(), claims.get(position).getStatus());
			// Setting the update value in the progress bar
			vh.bar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
			vh.bar.setVisibility(View.VISIBLE);
			vh.bar.setProgress(progress);

			vh.status.setText(claims.get(position).getStatus() + " " + progress
					+ " %");
			vh.status.setVisibility(View.VISIBLE);
			vh.send.setVisibility(View.INVISIBLE);

		}
		if (claims.get(position).getStatus().equals(ClaimStatus._UPDATE_INCOMPLETE)) {

			vh.iconLocal.setVisibility(View.GONE);
			vh.iconUnmoderated.setVisibility(View.VISIBLE);
			vh.number.setTextSize(8);
			if (claims.get(position).getNumber() != null)
				vh.number.setText(claims.get(position).getNumber());
			vh.status.setTextColor(context.getResources().getColor(
					R.color.status_created));

			int progress = FileSystemUtilities.getUploadProgress(claims.get(position).getId(), claims.get(position).getStatus());
			// Setting the update value in the progress bar
			vh.bar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
			vh.bar.setVisibility(View.VISIBLE);
			vh.bar.setProgress(progress);

			vh.status.setText(claims.get(position).getStatus() + " " + progress
					+ " %");
			vh.status.setVisibility(View.VISIBLE);

			vh.send.setVisibility(View.INVISIBLE);

		}
		if (claims.get(position).getStatus().equals(ClaimStatus._UNMODERATED)) {

			vh.iconUnmoderated.setVisibility(View.VISIBLE);
			vh.number.setTextSize(8);
			vh.number.setText(claims.get(position).getNumber());
			vh.bar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
			vh.status.setVisibility(View.GONE);
			vh.bar.setVisibility(View.GONE);

			vh.send.setVisibility(View.INVISIBLE);

		}

		if (claims.get(position).getStatus().equals(ClaimStatus._MODERATED)) {

			vh.iconModerated.setVisibility(View.VISIBLE);
			vh.number.setTextSize(8);
			vh.number.setText(claims.get(position).getNumber());
			vh.bar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
			vh.status.setVisibility(View.GONE);
			vh.bar.setVisibility(View.GONE);
			vh.send.setVisibility(View.INVISIBLE);

		}
		if (claims.get(position).getStatus().equals(ClaimStatus._CHALLENGED)) {

			vh.iconChallenged.setVisibility(View.VISIBLE);
			vh.number.setTextSize(8);
			vh.number.setText(claims.get(position).getNumber());
			vh.bar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
			vh.status.setVisibility(View.GONE);

			vh.send.setVisibility(View.INVISIBLE);

		}

		if (claims.get(position).getStatus().equals(ClaimStatus._WITHDRAWN)) {

			vh.iconWithdrawn.setVisibility(View.VISIBLE);
			vh.number.setTextSize(8);
			vh.number.setText(claims.get(position).getNumber());
			vh.bar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
			vh.status.setVisibility(View.GONE);
			vh.bar.setVisibility(View.GONE);
			vh.send.setVisibility(View.INVISIBLE);

		}
		
		if (claims.get(position).getStatus().equals(ClaimStatus._REVIEWED)) {

			vh.iconReviewed.setVisibility(View.VISIBLE);
			vh.number.setTextSize(8);
			vh.number.setText(claims.get(position).getNumber());
			vh.bar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
			vh.status.setVisibility(View.GONE);
			vh.bar.setVisibility(View.GONE);
			vh.send.setVisibility(View.INVISIBLE);

		}

		vh.challengeExpiryDate.setText(claims.get(position).getRemaingDays());
		vh.id.setTextSize(8);
		vh.id.setText(claims.get(position).getId());
		// vh.bar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
		vh.number.setTextSize(8);
		vh.number.setVisibility(View.VISIBLE);

		vh.number.setText(claims.get(position).getNumber());
		vh.position = position;

		vh.picture.setImageBitmap(Person.getPersonPicture(context,
				claims.get(position).getPersonId(),
				96));

		return convertView;
	}
}