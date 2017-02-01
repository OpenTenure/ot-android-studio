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

import java.io.File;
import java.util.List;

import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.model.Owner;
import org.fao.sola.clients.android.opentenure.model.Person;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class OwnersListAdapter extends ArrayAdapter<String> {

	private static int resource;
	private final Context context;
	private List<String> owners;
	private LayoutInflater inflater;
	private Claim claim;
	private static final int PERSON_RESULT = 100;
	private static Activity activity;
	private ModeDispatcher mainActivity;

	public OwnersListAdapter(Context context, List<String> owners,
			String claimId, Activity shareDetailsActivity) {
		super(context, resource, owners);
		// TODO Auto-generated constructor stub
		this.context = context;
		this.owners = owners;
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.claim = Claim.getClaim(claimId);
		this.activity = shareDetailsActivity;

		try {
			mainActivity = (ModeDispatcher) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement ModeDispatcher");
		}
	}

	static class ViewHolder {
		TextView id;
		TextView slogan;
		ImageView picture;
		ImageView removeIcon;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder vh;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.owner_list_item, parent,
					false);
			vh = new ViewHolder();
			vh.slogan = (TextView) convertView.findViewById(R.id.person_slogan);
			vh.id = (TextView) convertView.findViewById(R.id.person_id);
			vh.removeIcon = (ImageView) convertView
					.findViewById(R.id.remove_person);
			vh.picture = (ImageView) convertView
					.findViewById(R.id.person_picture);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}

		Person person = Person.getPerson(owners.get(position));
		vh.slogan.setText(person.getFirstName() + " " + person.getLastName());
		vh.id.setText(owners.get(position));

		vh.removeIcon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				owners.remove(position);
				notifyDataSetChanged();
			}
		});

		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, PersonActivity.class);
				intent.putExtra(PersonActivity.PERSON_ID_KEY,
						(((TextView) v.findViewById(R.id.person_id)).getText()));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra(PersonActivity.MODE_KEY, mainActivity.getMode()
						.toString());
				context.startActivity(intent);
			}

		});

		// File file = Person.getPersonPictureFile(person.getPersonId());
		vh.picture.setImageBitmap(Person.getPersonPicture(context,
				person.getPersonId(), 128));

		if (!claim.getStatus().equals(ClaimStatus._CREATED)
				&& !claim.getStatus().equals(ClaimStatus._UPLOAD_ERROR)
				&& !claim.getStatus().equals(ClaimStatus._UPLOAD_INCOMPLETE)) {

			vh.removeIcon.setVisibility(View.INVISIBLE);

		}

		return convertView;

	}

}
