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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.fao.sola.clients.android.opentenure.button.listener.PreExportClaimListener;
import org.fao.sola.clients.android.opentenure.button.listener.SubmitClaimListener;
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.filesystem.json.JsonUtilities;
import org.fao.sola.clients.android.opentenure.form.FieldConstraint;
import org.fao.sola.clients.android.opentenure.form.FormPayload;
import org.fao.sola.clients.android.opentenure.form.FormTemplate;
import org.fao.sola.clients.android.opentenure.model.Boundary;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.model.Configuration;
import org.fao.sola.clients.android.opentenure.model.DocumentType;
import org.fao.sola.clients.android.opentenure.model.Person;
import org.fao.sola.clients.android.opentenure.model.Vertex;
import org.fao.sola.clients.android.opentenure.network.SaveBoundaryTask;
import org.fao.sola.clients.android.opentenure.network.SaveClaimTask;
import org.fao.sola.clients.android.opentenure.print.PDFClaimExporter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BoundariesListAdapter extends ArrayAdapter<Boundary> implements Filterable {

	private final Context context;
	private final List<Boundary> originalBoundaries;
	private List<Boundary> filteredBoundaries;
	private List<Boundary> boundaries;
	LayoutInflater inflater;

	public BoundariesListAdapter(Context context, List<Boundary> boundaries) {
		super(context, R.layout.boundaries_list_item, boundaries);
		this.context = context;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.originalBoundaries = new ArrayList<Boundary>(boundaries);
		this.boundaries = boundaries;
		this.filteredBoundaries = null;
	}

	@Override
	public Filter getFilter() {

		Filter filter = new Filter() {

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {

				String filterString = constraint.toString();

				filteredBoundaries = new ArrayList<Boundary>();
				for (Boundary boundary : originalBoundaries) {
					String lcase = boundary.getName().toLowerCase(Locale.getDefault());
					if (lcase.contains(filterString.toLowerCase(Locale.getDefault()))) {
						filteredBoundaries.add(boundary);
					}
				}

				FilterResults results = new FilterResults();
				results.count = filteredBoundaries.size();
				results.values = filteredBoundaries;
				return results;
			}

			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				boundaries = (ArrayList<Boundary>) results.values;
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
		return boundaries.size();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		BoundaryListItemViewHolder vh;
		Boundary boundary = boundaries.get(position);

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.boundaries_list_item, parent,false);

			vh = new BoundaryListItemViewHolder();
			vh.setBoundary(boundary);
			vh.name = (TextView) convertView.findViewById(R.id.boundary_name);
			vh.processed = (ImageView) convertView.findViewById(R.id.status_approved);
			vh.bar = (ProgressBar) convertView.findViewById(R.id.progress_bar);
			vh.delete = (ImageView) convertView.findViewById(R.id.action_delete);
			vh.delete.setOnClickListener(new DeleteButtonHandler(vh));
			vh.send = (ImageView) convertView.findViewById(R.id.action_submit_to_server);
			vh.send.setOnClickListener(new SendButtonHandler(vh));
			convertView.setTag(vh);
		} else {
			vh = (BoundaryListItemViewHolder) convertView.getTag();
		}

		vh.name.setText(boundary.getDisplayName());

		// Show/hide status icon and send button
		if(boundary.isProcessed() && boundary.getStatusCode().equals("pending")){
			vh.processed.setVisibility(View.VISIBLE);
		} else {
			vh.processed.setVisibility(View.GONE);
		}

		if(!boundary.isProcessed() && boundary.getStatusCode().equals("pending")){
			vh.send.setVisibility(View.VISIBLE);
			vh.delete.setVisibility(View.VISIBLE);
		} else {
			vh.send.setVisibility(View.GONE);
			vh.delete.setVisibility(View.GONE);
		}

		if (OpenTenureApplication.getInstance().getLocale().toString().startsWith("ar")) {
			vh.name.setGravity(View.TEXT_DIRECTION_LOCALE);
		}
		return convertView;
	}

	private class DeleteButtonHandler implements View.OnClickListener {
		BoundaryListItemViewHolder vh;

		public DeleteButtonHandler(BoundaryListItemViewHolder vh){
			this.vh = vh;
		}

		@Override
		public void onClick(View view) {
			try {
				AlertDialog.Builder dialog = new AlertDialog.Builder(view.getContext());

				dialog.setTitle(R.string.action_remove_boundary);
				dialog.setMessage(R.string.message_remove_boundary);

				dialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// Check if boundary can be deleted
						List<Boundary> children = Boundary.getChildrenBoundaries(vh.getBoundary().getId());
						if(children != null && children.size() > 0){
							Toast toast = Toast.makeText(OpenTenureApplication.getContext(), R.string.boundary_has_children, Toast.LENGTH_LONG);
							toast.show();
							return;
						}

						vh.getBoundary().delete();
						boundaries.remove(vh.getBoundary());
						notifyDataSetChanged();
					}
				});

				dialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});

				dialog.show();
			} catch (Exception ex){
			}
		}
	}

	private class SendButtonHandler implements View.OnClickListener {
		BoundaryListItemViewHolder vh;

		public SendButtonHandler(BoundaryListItemViewHolder vh){
			this.vh = vh;
		}

		@Override
		public void onClick(View view) {
			try {
				if (!OpenTenureApplication.isLoggedin()) {
					Toast toast = Toast.makeText(view.getContext(), R.string.message_login_before, Toast.LENGTH_LONG);
					toast.show();
					return;
				} else {
					// Submit boundary
					view.setVisibility(View.GONE);
					vh.delete.setVisibility(View.GONE);
					vh.getBar().setVisibility(View.VISIBLE);
					SaveBoundaryTask saveBoundaryTask = new SaveBoundaryTask();
					saveBoundaryTask.execute(vh);
				}
			} catch (Exception ex){
				vh.getBar().setVisibility(View.GONE);
				view.setVisibility(View.VISIBLE);
				vh.delete.setVisibility(View.VISIBLE);
			}
		}
	}
}
