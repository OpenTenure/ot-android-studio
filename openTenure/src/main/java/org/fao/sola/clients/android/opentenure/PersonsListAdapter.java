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

import org.fao.sola.clients.android.opentenure.button.listener.DeletePersonListener;
import org.fao.sola.clients.android.opentenure.model.Person;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

public class PersonsListAdapter extends ArrayAdapter<PersonListTO> implements
		Filterable {
	private final Context context;
	private final List<PersonListTO> originalPersons;
	private List<PersonListTO> filteredPersons;
	private List<PersonListTO> persons;
	LayoutInflater inflater;
	private ModeDispatcher.Mode mode;

	public PersonsListAdapter(Context context, List<PersonListTO> persons, ModeDispatcher.Mode mode) {
		super(context, R.layout.persons_list_item, persons);
		this.context = context;
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.originalPersons = new ArrayList<PersonListTO>(persons);
		this.persons = persons;
		this.filteredPersons = null;
		this.mode = mode;
	}

	@Override
	public Filter getFilter() {

		Filter filter = new Filter() {

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {

				String filterString = constraint.toString();

				filteredPersons = new ArrayList<PersonListTO>();
				for (PersonListTO pto : originalPersons) {
					String lcase = pto.getSlogan().toLowerCase(
							Locale.getDefault());
					if (lcase.contains(filterString.toLowerCase(Locale
							.getDefault()))) {
						filteredPersons.add(pto);
					}
				}

				FilterResults results = new FilterResults();
				results.count = filteredPersons.size();
				results.values = filteredPersons;
				return results;
			}

			@Override
			protected void publishResults(CharSequence constraint,
					FilterResults results) {
				persons = (ArrayList<PersonListTO>) results.values;

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
		return persons.size();
	}

	static class ViewHolder {
		TextView id;
		TextView slogan;
		ImageView picture;
		ImageView remove;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder vh;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.persons_list_item, parent,
					false);
			vh = new ViewHolder();
			vh.slogan = (TextView) convertView.findViewById(R.id.person_slogan);
			vh.id = (TextView) convertView.findViewById(R.id.person_id);

			vh.remove = (ImageView) convertView
					.findViewById(R.id.remove_person);

			vh.picture = (ImageView) convertView
					.findViewById(R.id.person_picture);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();

		}

		if (!persons.get(position).hasClaimOrShare()
				&& mode.compareTo(ModeDispatcher.Mode.MODE_RW) == 0) {

			vh.remove.setOnClickListener(new DeletePersonListener(persons.get(
					position).getId()));
			vh.remove.setVisibility(View.VISIBLE);
		} else
			vh.remove.setVisibility(View.INVISIBLE);

		vh.slogan.setText(persons.get(position).getSlogan());
		vh.id.setTextSize(8);
		vh.id.setText(persons.get(position).getId());
		vh.picture
				.setImageBitmap(Person.getPersonPicture(context, persons.get(position).getId(),
						96));

		return convertView;
	}
}