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

import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ViewHolder {
	TextView id;
	TextView number;
	TextView slogan;
	TextView status;
	ProgressBar bar;
	TextView challengeExpiryDate;
	ImageView picture;
	ImageView send;
	ImageView export;
	ImageView remove;
	ImageView iconLocal;
	ImageView iconModerated;
	ImageView iconUnmoderated;
	ImageView iconChallenged;
	ImageView iconWithdrawn;
	ImageView iconReviewed;
	int position;
	
	
	
	public TextView getId() {
		return id;
	}
	public void setId(TextView id) {
		this.id = id;
	}	
	public TextView getNumber() {
		return number;
	}
	public void setNumber(TextView number) {
		this.number = number;
	}
	public TextView getSlogan() {
		return slogan;
	}
	public void setSlogan(TextView slogan) {
		this.slogan = slogan;
	}
	public TextView getStatus() {
		return status;
	}
	public void setStatus(TextView status) {
		this.status = status;
	}
	public ProgressBar getBar() {
		return bar;
	}
	public void setBar(ProgressBar bar) {
		this.bar = bar;
	}
	public TextView getChallengeExpiryDate() {
		return challengeExpiryDate;
	}
	public void setChallengeExpiryDate(TextView challengeExpiryDate) {
		this.challengeExpiryDate = challengeExpiryDate;
	}
	public ImageView getPicture() {
		return picture;
	}
	public void setPicture(ImageView picture) {
		this.picture = picture;
	}
	public ImageView getSend() {
		return send;
	}
	public void setSend(ImageView send) {
		this.send = send;
	}
	public ImageView getRemove() {
		return remove;
	}
	public void setRemove(ImageView remove) {
		this.remove = remove;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	public ImageView getIconLocal() {
		return iconLocal;
	}
	public void setIconLocal(ImageView iconLocal) {
		this.iconLocal = iconLocal;
	}
	public ImageView getIconModerated() {
		return iconModerated;
	}
	public void setIconModerated(ImageView iconModerated) {
		this.iconModerated = iconModerated;
	}
	public ImageView getIconUnmoderated() {
		return iconUnmoderated;
	}
	public void setIconUnmoderated(ImageView iconUnmoderated) {
		this.iconUnmoderated = iconUnmoderated;
	}
	public ImageView getIconChallenged() {
		return iconChallenged;
	}
	public void setIconChallenged(ImageView iconChallenged) {
		this.iconChallenged = iconChallenged;
	}
	public ImageView getIconWithdrawn() {
		return iconWithdrawn;
	}
	public void setIconWithdrawn(ImageView iconWithdrawn) {
		this.iconWithdrawn = iconWithdrawn;
	}
	public ImageView getIconReviewed() {
		return iconReviewed;
	}
	public void setIconReviewed(ImageView iconReviewed) {
		this.iconReviewed = iconReviewed;
	}
	
	
	
	
	
	
	
}
