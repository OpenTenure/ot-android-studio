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
import org.fao.sola.clients.android.opentenure.model.Owner;
import org.fao.sola.clients.android.opentenure.model.ShareProperty;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SharesListAdapter extends ArrayAdapter<SharesListTO> {
	private final Context context;
	private List<SharesListTO> shares;
	private LayoutInflater inflater;
	private int availableShares;
	private boolean readOnly;
	private String claimId;

	public SharesListAdapter(Context context, List<SharesListTO> shares,
			String claimId, boolean readOnly) {
		super(context, R.layout.share_list_item, shares);
		this.context = context;
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.shares = shares;
		this.readOnly = readOnly;
		this.claimId = claimId;
		setAvailableShares();
	}

	private void setAvailableShares() {
		this.availableShares = Claim.MAX_SHARES_PER_CLAIM;
		for (SharesListTO share : shares) {
			availableShares -= share.getShares();
		}
	}

	static class ViewHolder {

		TextView slogan;
		TextView owners_num;
		Spinner shares;
		ImageView picture;
		ImageView removeIcon;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder vh;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.share_list_item, parent,
					false);
			vh = new ViewHolder();
			vh.slogan = (TextView) convertView.findViewById(R.id.share_slogan);

			vh.shares = (Spinner) convertView.findViewById(R.id.share_shares);
			vh.owners_num = (TextView) convertView
					.findViewById(R.id.share_owners_num);
			vh.removeIcon = (ImageView) convertView
					.findViewById(R.id.remove_icon);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}

		int numb = Owner.getOwners(shares.get(position).getId()).size();

		vh.owners_num.setText(OpenTenureApplication.getContext().getString(
				R.string.owners)
				+ " : " + numb);

		vh.slogan.setText(shares.get(position).getSlogan());
		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, ShareDetailsActivity.class);
				intent.putExtra(ShareDetailsActivity.SHARE_ID,
						shares.get(position).getId());

				ShareProperty share = ShareProperty.getShare(shares.get(
						position).getId());
				if (Claim.getClaim(share.getClaimId()).isModifiable())
					intent.putExtra(PersonActivity.MODE_KEY,
							ModeDispatcher.Mode.MODE_RW.toString());
				else
					intent.putExtra(PersonActivity.MODE_KEY,
							ModeDispatcher.Mode.MODE_RO.toString());
				context.startActivity(intent);
			}

		});

		int sharesValue = shares.get(position).getShares();
		String shareId = shares.get(position).getId();

		if (sharesValue >= 1) {
			vh.shares.setSelection(sharesValue - 1);
		}

		vh.shares.setEnabled(!readOnly);
		vh.shares.setFocusable(!readOnly);
		vh.shares.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				int sharesValue = pos + 1;

				shares.get(position).setShares(sharesValue);
				ShareProperty share = ShareProperty.getShare(shares.get(
						position).getId());

				int numb = Owner.getOwners(share.getId()).size();

				shares.get(position).setOwners_num(numb);

				if (sharesValue > share.getShares() + availableShares) {

					Toast.makeText(context,
							R.string.message_no_available_shares,
							Toast.LENGTH_LONG).show();
					((Spinner) view.getParent()).setSelection(share.getShares() - 1);

				} else {
					share.setShares(sharesValue);
					share.updateShare();
					setAvailableShares();
					notifyDataSetChanged();
					shares.get(position).setSlogan(
							OpenTenureApplication.getContext().getString(
									R.string.title_share)
									+ " "
									+ (position + 1)
									+ " : "
									+ share.getShares() + "/100");
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// Do nothing
			}
		});

		if (!readOnly) {
			vh.removeIcon.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					AlertDialog.Builder removeOwnerDialog = new AlertDialog.Builder(
							context);
					removeOwnerDialog.setTitle(R.string.action_remove_owner);
					removeOwnerDialog.setMessage(shares.get(position)
							.getSlogan()
							+ ": "
							+ context.getResources().getString(
									R.string.message_remove_owner));

					removeOwnerDialog.setPositiveButton(R.string.confirm,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									ShareProperty.getShare(
											shares.get(position).getId())
											.deleteShare();
									shares.remove(position);
									Toast.makeText(context,
											R.string.owner_removed,
											Toast.LENGTH_SHORT).show();
									setAvailableShares();
									notifyDataSetChanged();
								}
							});
					removeOwnerDialog.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							});

					removeOwnerDialog.show();

				}
			});
		} else {
			((ViewManager) convertView).removeView(vh.removeIcon);
		}

		return convertView;
	}
}