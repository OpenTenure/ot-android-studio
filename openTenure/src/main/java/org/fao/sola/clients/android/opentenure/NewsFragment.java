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

import org.fao.sola.clients.android.opentenure.model.Link;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.ListFragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class NewsFragment extends ListFragment {
	private View rootView;
	private String filter = null;
	private static final String FILTER_KEY = "filter";

	public NewsFragment() {
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.news, menu);
		OpenTenureApplication.setNewsFragment(getActivity());
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_news, container, false);
		setHasOptionsMenu(true);
		EditText inputSearch = (EditText) rootView.findViewById(R.id.filter_input_field);
		inputSearch.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		inputSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				filter = arg0.toString();
				((NewsListAdapter) getListAdapter()).getFilter().filter(
						filter);
			}
		});

		update();

		if (savedInstanceState != null && savedInstanceState.getString(FILTER_KEY) != null) {
			filter = savedInstanceState.getString(FILTER_KEY);
			((NewsListAdapter) getListAdapter()).getFilter().filter(filter);
		}
		update();
		if (savedInstanceState != null && savedInstanceState.getString(FILTER_KEY) != null) {
			filter = savedInstanceState.getString(FILTER_KEY);
			((NewsListAdapter) getListAdapter()).getFilter().filter(filter);
		}

		OpenTenureApplication.setActivity(getActivity());
		return rootView;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(((TextView) v.findViewById(R.id.url)).getText()
							.toString()));
			startActivity(intent);
		} catch (Exception e) {
			Toast.makeText(
					rootView.getContext(),
					getResources().getString(R.string.message_invalid_url)
							+ ": "
							+ ((TextView) v.findViewById(R.id.url)).getText()
									.toString(), Toast.LENGTH_SHORT).show();
		}
	}

	protected void update() {
		List<Link> links = Link.getLinks();
		List<String> news = new ArrayList<String>();
		List<String> urls = new ArrayList<String>();

		for(Link link:links){
			news.add(link.getDesc());
			urls.add(link.getUrl());
		}

		ArrayAdapter<Link> adapter = new NewsListAdapter(rootView.getContext(), links);
		setListAdapter(adapter);
		adapter.notifyDataSetChanged();
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(FILTER_KEY, filter);
		super.onSaveInstanceState(outState);
	}
}
