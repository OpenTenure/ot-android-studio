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


import org.fao.sola.clients.android.opentenure.button.listener.PreExportClaimListener;
import org.fao.sola.clients.android.opentenure.button.listener.SubmitClaimListener;
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.model.Person;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LocalClaimsListAdapter extends ArrayAdapter<ClaimListTO> implements
		Filterable {
	private final Context context;
	private final List<ClaimListTO> originalClaims;
	private List<ClaimListTO> filteredClaims;
	private List<ClaimListTO> claims;
	LayoutInflater inflater;

	public LocalClaimsListAdapter(Context context, List<ClaimListTO> claims,
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
					String lcase = cto.getSlogan().toLowerCase(Locale.getDefault());
					if (lcase.contains(filterString.toLowerCase(Locale.getDefault()))) {
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder vh;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.claims_list_item, parent,false);
			vh = new ViewHolder();
			vh.slogan = (TextView) convertView.findViewById(R.id.claim_slogan);
			if(claims.get(position).isDeleted()){
				vh.slogan.setTextColor(Color.rgb(200, 0, 0));
				vh.slogan.setPaintFlags(vh.slogan.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
			}

			vh.number = (TextView) convertView.findViewById(R.id.claim_number);
			vh.id = (TextView) convertView.findViewById(R.id.claim_id);
			vh.status = (TextView) convertView.findViewById(R.id.claim_status);
			vh.iconUnmoderated = (ImageView) convertView.findViewById(R.id.status_unmoderated);
			vh.iconLocal = (ImageView) convertView.findViewById(R.id.status_local);
			vh.iconModerated = (ImageView) convertView.findViewById(R.id.status_moderated);
			vh.iconChallenged = (ImageView) convertView.findViewById(R.id.status_challenged);
			vh.iconWithdrawn = (ImageView) convertView.findViewById(R.id.status_withdrawn);
			vh.iconReviewed = (ImageView) convertView.findViewById(R.id.status_reviewed);
			vh.challengeExpiryDate = (TextView) convertView.findViewById(R.id.claim_challenging_time);
			vh.picture = (ImageView) convertView.findViewById(R.id.claimant_picture);
			vh.send = (ImageView) convertView.findViewById(R.id.action_submit_to_server);
			vh.export = (ImageView) convertView.findViewById(R.id.action_export_to_server);
			vh.remove = (ImageView) convertView.findViewById(R.id.action_remove_claim);

			vh.undoDelete = (ImageView) convertView.findViewById(R.id.action_undo_delete);
			vh.undoDelete.setVisibility(View.GONE);
			vh.undoDelete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					AlertDialog.Builder dialog = new AlertDialog.Builder(view.getContext());

					dialog.setTitle(R.string.action_undo_delete);
					dialog.setMessage(R.string.message_undo_delete);

					dialog.setPositiveButton(R.string.confirm,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// Mark claim as deleted
									Claim.markDeleted(claims.get(position).getId(), false);
									OpenTenureApplication.getLocalClaimsFragment().refresh();
									OpenTenureApplication.getMapFragment().refreshMap();
								}
							});

					dialog.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
								}
							});
					dialog.show();
				}
			});
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

		vh.remove.setOnClickListener(new ClaimDeleteListener(claims.get(position).getId(), vh));
		vh.send.setOnClickListener(new SubmitClaimListener(claims.get(position).getId(), vh));
		vh.export.setOnClickListener(new PreExportClaimListener(claims.get(position).getId()));

		String realStatus = "";
		if (OpenTenureApplication.getInstance().getChangingClaims().contains(claims.get(position).getId())) {
			realStatus = Claim.getClaim(claims.get(position).getId()).getStatus();
		} else
			realStatus = claims.get(position).getStatus();

		if (realStatus.equals(ClaimStatus._CREATED)) {
			vh.number.setVisibility(View.GONE);
			vh.iconLocal.setVisibility(View.VISIBLE);
			vh.iconUnmoderated.setVisibility(View.GONE);

			vh.bar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
			vh.status.setVisibility(View.GONE);
			vh.bar.setVisibility(View.GONE);
			if (claims.get(position).isModifiable())
				vh.send.setVisibility(View.VISIBLE);
			else
				vh.send.setVisibility(View.INVISIBLE);

		}
		if (realStatus.equals(ClaimStatus._UPLOADING)) {

			vh.iconLocal.setVisibility(View.VISIBLE);
			vh.number.setTextSize(8);
			if (claims.get(position).getNumber() != null)
				vh.number.setText(claims.get(position).getNumber());
			vh.status.setTextColor(context.getResources().getColor(
					R.color.status_created));

			int progress = FileSystemUtilities.getUploadProgress(
					claims.get(position).getId(), claims.get(position)
							.getStatus());

			// Setting the update value in the progress bar
			vh.bar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
			vh.bar.setVisibility(View.VISIBLE);
			vh.bar.setProgress(progress);

			vh.status.setText(getContext().getResources().getString(
					R.string.uploading)
					+ " " + progress + " %");
			
			vh.status.setVisibility(View.VISIBLE);
			vh.send.setVisibility(View.INVISIBLE);

		}
		if (realStatus.equals(ClaimStatus._UPDATING)) {

			vh.iconLocal.setVisibility(View.GONE);
			vh.iconUnmoderated.setVisibility(View.VISIBLE);
			vh.number.setTextSize(8);
			if (claims.get(position).getNumber() != null)
				vh.number.setText(claims.get(position).getNumber());
			vh.status.setTextColor(context.getResources().getColor(
					R.color.status_created));

			int progress = FileSystemUtilities.getUploadProgress(
					claims.get(position).getId(), claims.get(position)
							.getStatus());

			// Setting the update value in the progress bar
			vh.bar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
			vh.bar.setVisibility(View.VISIBLE);
			vh.bar.setProgress(progress);

			vh.status.setText(getContext().getResources().getString(
					R.string.updating)
					+ " " + progress + " %");

			vh.status.setVisibility(View.VISIBLE);
			vh.send.setVisibility(View.INVISIBLE);
		}
		if (realStatus.equals(ClaimStatus._UPLOAD_ERROR)) {

			vh.iconLocal.setVisibility(View.VISIBLE);
			vh.number.setTextSize(8);
			if (claims.get(position).getNumber() != null)
				vh.number.setText(claims.get(position).getNumber());
			vh.status.setTextColor(context.getResources().getColor(
					R.color.status_created));
			vh.status.setText(getContext().getResources().getString(
					R.string.upload_error));
			vh.status.setTextColor(context.getResources().getColor(
					R.color.status_challenged));
			vh.bar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
			vh.bar.setVisibility(View.GONE);

			vh.status.setVisibility(View.VISIBLE);
			if (claims.get(position).isModifiable())
				vh.send.setVisibility(View.VISIBLE);
			else
				vh.send.setVisibility(View.INVISIBLE);

		}
		if (realStatus.equals(ClaimStatus._UPDATE_ERROR)) {

			vh.iconLocal.setVisibility(View.GONE);
			vh.iconUnmoderated.setVisibility(View.VISIBLE);
			vh.number.setTextSize(8);
			if (claims.get(position).getNumber() != null)
				vh.number.setText(claims.get(position).getNumber());
			vh.status.setTextColor(context.getResources().getColor(
					R.color.status_created));
			vh.status.setText(getContext().getResources().getString(
					R.string.update_error));
			vh.bar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
			vh.bar.setVisibility(View.GONE);

			vh.status.setVisibility(View.VISIBLE);
			vh.send.setVisibility(View.INVISIBLE);
		}
		if (realStatus.equals(ClaimStatus._UPLOAD_INCOMPLETE)) {

			vh.iconLocal.setVisibility(View.VISIBLE);
			vh.number.setTextSize(8);
			if (claims.get(position).getNumber() != null)
				vh.number.setText(claims.get(position).getNumber());
			vh.status.setTextColor(context.getResources().getColor(R.color.status_created));

			int progress = FileSystemUtilities.getUploadProgress(
					claims.get(position).getId(), claims.get(position).getStatus());
			// Setting the update value in the progress bar
			vh.bar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
			vh.bar.setVisibility(View.VISIBLE);
			vh.bar.setProgress(progress);

			vh.status.setText(getContext().getResources().getString(
					R.string.upload_incomplete)
					+ " " + progress + " %");
			vh.status.setVisibility(View.VISIBLE);
			vh.send.setVisibility(View.VISIBLE);

		}
		if (realStatus.equals(ClaimStatus._UPDATE_INCOMPLETE)) {

			vh.iconLocal.setVisibility(View.GONE);
			vh.iconUnmoderated.setVisibility(View.VISIBLE);
			vh.number.setTextSize(8);
			if (claims.get(position).getNumber() != null)
				vh.number.setText(claims.get(position).getNumber());
			vh.status.setTextColor(context.getResources().getColor(
					R.color.status_created));

			int progress = FileSystemUtilities.getUploadProgress(
					claims.get(position).getId(), claims.get(position)
							.getStatus());
			// Setting the update value in the progress bar
			vh.bar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
			vh.bar.setVisibility(View.VISIBLE);
			vh.bar.setProgress(progress);

			vh.status.setText(getContext().getResources().getString(
					R.string.update_incomplete)
					+ " " + progress + " %");
			vh.status.setVisibility(View.VISIBLE);

			vh.send.setVisibility(View.INVISIBLE);

		}
		if (realStatus.equals(ClaimStatus._UNMODERATED)) {

			vh.iconUnmoderated.setVisibility(View.VISIBLE);
			vh.number.setTextSize(8);
			vh.number.setText(claims.get(position).getNumber());
			vh.bar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
			vh.status.setVisibility(View.GONE);
			vh.bar.setVisibility(View.GONE);

			vh.send.setVisibility(View.INVISIBLE);

		}

		if (realStatus.equals(ClaimStatus._MODERATED)) {

			vh.iconModerated.setVisibility(View.VISIBLE);
			vh.number.setTextSize(8);
			vh.number.setText(claims.get(position).getNumber());
			vh.bar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
			vh.status.setVisibility(View.GONE);
			vh.bar.setVisibility(View.GONE);
			vh.send.setVisibility(View.INVISIBLE);

		}
		if (realStatus.equals(ClaimStatus._CHALLENGED)) {

			vh.iconChallenged.setVisibility(View.VISIBLE);
			vh.number.setTextSize(8);
			vh.number.setText(claims.get(position).getNumber());
			vh.bar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
			vh.status.setVisibility(View.GONE);

			vh.send.setVisibility(View.INVISIBLE);

		}

		if (realStatus.equals(ClaimStatus._WITHDRAWN)) {

			vh.iconWithdrawn.setVisibility(View.VISIBLE);
			vh.number.setTextSize(8);
			vh.number.setText(claims.get(position).getNumber());
			vh.bar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
			vh.status.setVisibility(View.GONE);
			vh.bar.setVisibility(View.GONE);
			vh.send.setVisibility(View.INVISIBLE);

		}

		if (realStatus.equals(ClaimStatus._REVIEWED)) {

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
				claims.get(position).getPersonId(), 96));

		if (OpenTenureApplication.getInstance().getLocale().toString().startsWith("ar")) {
			vh.slogan.setGravity(View.TEXT_DIRECTION_LOCALE);
			vh.number.setGravity(View.TEXT_DIRECTION_LOCALE);
			vh.id.setGravity(View.TEXT_DIRECTION_LOCALE);
			vh.challengeExpiryDate.setGravity(View.TEXT_DIRECTION_LOCALE);
		}

		if(claims.get(position).isDeleted()){
			vh.send.setVisibility(View.GONE);
			vh.export.setVisibility(View.GONE);
			vh.undoDelete.setVisibility(View.VISIBLE);
		}

		return convertView;
	}
}