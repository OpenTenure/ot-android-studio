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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

public class AttachmentViewHolder extends ViewHolder {

	ImageView downloadIcon;
	ImageView removeIcon;
	ImageView sendIcon;
	ImageView saveIcon;
	ProgressBar barAttachment;
	TextView attachmentStatus;
	TextView attachmentFileType;
	Spinner attachmentType;
	LinearLayout clickableArea;
	LinearLayout clickableArea2;


	public ImageView getDownloadIcon() {
		return downloadIcon;
	}

	public void setDownloadIcon(ImageView downloadIcon) {
		this.downloadIcon = downloadIcon;
	}

	public ImageView getRemoveIcon() {
		return removeIcon;
	}

	public void setRemoveIcon(ImageView removeIcon) {
		this.removeIcon = removeIcon;
	}

	public ImageView getSendIcon() {
		return sendIcon;
	}

	public void setSendIcon(ImageView sendIcon) {
		this.sendIcon = sendIcon;
	}

	public ProgressBar getBarAttachment() {
		return barAttachment;
	}

	public LinearLayout getClickableArea() {
		return clickableArea;
	}

	public void setClickableArea(LinearLayout clickableArea) {
		this.clickableArea = clickableArea;
	}

	public void setBarAttachment(ProgressBar barAttachment) {
		this.barAttachment = barAttachment;
	}

	public TextView getAttachmentStatus() {
		return attachmentStatus;
	}

	public void setAttachmentStatus(TextView attachmentStatus) {
		this.attachmentStatus = attachmentStatus;
	}

	public TextView getAttachmentFileType() {
		return attachmentFileType;
	}

	public void setAttachmentFileType(TextView attachmentFileType) {
		this.attachmentFileType = attachmentFileType;
	}

	public Spinner getAttachmentType() {
		return attachmentType;
	}

	public void setAttachmentType(Spinner attachmentType) {
		this.attachmentType = attachmentType;
	}

	public ImageView getSaveIcon() {
		return saveIcon;
	}

	public void setSaveIcon(ImageView saveIcon) {
		this.saveIcon = saveIcon;
	}

	public LinearLayout getClickableArea2() {
		return clickableArea2;
	}

	public void setClickableArea2(LinearLayout clickableArea2) {
		this.clickableArea2 = clickableArea2;
	}


}
