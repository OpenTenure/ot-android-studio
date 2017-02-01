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

import org.fao.sola.clients.android.opentenure.model.Link;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

public class NewsListAdapter extends ArrayAdapter<Link> {
	private final List<Link> originalLinks;
	private List<Link> filteredLinks;
	private List<Link> links;
	LayoutInflater inflater;

	public NewsListAdapter(Context context, List<Link> links) {
		super(context, R.layout.news_list_item, links);
		this.links = links;
		this.originalLinks = new ArrayList<Link>(links);
		this.filteredLinks = null;
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public Filter getFilter() {

		Filter filter = new Filter() {

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {

				String filterString = constraint.toString();

				filteredLinks = new ArrayList<Link>();
				for (Link cto : originalLinks) {
					String lcase = cto.getDesc().toLowerCase(
							Locale.getDefault());
					if (lcase.contains(filterString.toLowerCase(Locale
							.getDefault()))) {
						filteredLinks.add(cto);
					}
				}

				FilterResults results = new FilterResults();
				results.count = filteredLinks.size();
				results.values = filteredLinks;
				return results;
			}

			@Override
			protected void publishResults(CharSequence constraint,
					FilterResults results) {
				links = (ArrayList<Link>) results.values;

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
		return links.size();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		NewsHolder nh;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.news_list_item, parent, false);
			nh = new NewsHolder();
			nh.desc = (TextView) convertView.findViewById(R.id.news_slogan);
			nh.url = (TextView) convertView.findViewById(R.id.url);
			convertView.setTag(nh);
		} else {
			nh = (NewsHolder) convertView.getTag();
		}

		nh.getdesc().setText(links.get(position).getDesc());
		nh.getUrl().setTextSize(8);
		nh.getUrl().setText(links.get(position).getUrl());
		return convertView;
	}
	
	public class NewsHolder {
		TextView desc;
		TextView url;
		public TextView getdesc() {
			return desc;
		}
		public void setDesc(TextView desc) {
			this.desc = desc;
		}	
		public TextView getUrl() {
			return url;
		}
		public void setUrl(TextView url) {
			this.url = url;
		}
	}
}