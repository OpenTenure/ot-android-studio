/**
 * ******************************************************************************************
 * Copyright (C) 2015 - Food and Agriculture Organization of the United Nations (FAO).
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

import org.fao.sola.clients.android.opentenure.model.Bookmark;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class BookmarksFragment extends ListFragment {

	private View rootView;
	private static final String FILTER_KEY = "filter";
	private String filter = null;

	public BookmarksFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.bookmarks_list, container, false);
		setHasOptionsMenu(true);
		EditText inputSearch = (EditText) rootView
				.findViewById(R.id.filter_input_field);
		inputSearch.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		inputSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2,
					int arg3) {
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				filter = arg0.toString();
				((BookmarksListAdapter) getListAdapter()).getFilter().filter(
						filter);
			}
		});

		update();

		if(savedInstanceState != null && savedInstanceState.getString(FILTER_KEY) != null){
			filter = savedInstanceState.getString(FILTER_KEY);
			((BookmarksListAdapter) getListAdapter()).getFilter().filter(filter);
		}
		
		// Set this reference at Application level to refresh 
		

		return rootView;
	}

	@Override
	public void onResume() {
		update();
		if(filter != null){
			((BookmarksListAdapter) getListAdapter()).getFilter().filter(filter);
		}
		super.onResume();
	};

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent resultIntent = new Intent();
		resultIntent.putExtra(SelectBookmarkActivity.BOOKMARK_ID_KEY,
				((TextView) v.findViewById(R.id.bookmark_id)).getText());
		getActivity().setResult(
				SelectBookmarkActivity.SELECT_BOOKMARK_ACTIVITY_RESULT,
				resultIntent);
		getActivity().finish();
	}

	protected void update() {
		List<Bookmark> bookmarks = Bookmark.getAllBookmarks();
		List<BookmarkListTO> bookmarkListTOs = new ArrayList<BookmarkListTO>();

		for (Bookmark book : bookmarks) {
			BookmarkListTO bto = new BookmarkListTO();
				bto.setId(book.getBookmarkId());
				bto.setSlogan(book.getName());
				bookmarkListTOs.add(bto);
		}
		ArrayAdapter<BookmarkListTO> adapter = new BookmarksListAdapter(
				rootView.getContext(), bookmarkListTOs);
		setListAdapter(adapter);
		adapter.notifyDataSetChanged();
	}
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(FILTER_KEY, filter);
		super.onSaveInstanceState(outState);
	}
	
	public void refresh(){
		
		update();
	}
}
