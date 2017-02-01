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
package org.fao.sola.clients.android.opentenure.form.constraint;

import java.math.BigDecimal;
import java.text.MessageFormat;

import org.fao.sola.clients.android.opentenure.form.FieldConstraint;
import org.fao.sola.clients.android.opentenure.form.FieldConstraintType;
import org.fao.sola.clients.android.opentenure.form.FieldPayload;
import org.fao.sola.clients.android.opentenure.form.FieldType;

public class LengthConstraint extends FieldConstraint {

	public LengthConstraint() {
		super();
		fieldConstraintType = FieldConstraintType.LENGTH;
		addApplicableType(FieldType.TEXT);
		addApplicableType(FieldType.DOCUMENT);
		this.errorMsg = "Length {1} of value {2} in {0} is not between {3} and {4}";
	}

	public LengthConstraint(LengthConstraint lc) {
		super(lc);
		setMinValue(lc.getMinValue());
		setMaxValue(lc.getMaxValue());
	}

	public LengthConstraint(BigDecimal minValue, BigDecimal maxValue) {
		super();
		fieldConstraintType = FieldConstraintType.LENGTH;
		addApplicableType(FieldType.TEXT);
		addApplicableType(FieldType.DOCUMENT);
		setMinValue(minValue);
		setMaxValue(maxValue);
	}

	@Override
	public void setMinValue(BigDecimal minValue) {
		if(minValue != null){
			this.minValue = new BigDecimal(minValue.intValue());
		}else{
			this.minValue = null;
		}
	}

	@Override
	public void setMaxValue(BigDecimal maxValue) {
		if(maxValue != null){
			this.maxValue = new BigDecimal(maxValue.intValue());
		}else{
			this.maxValue = null;
		}
	}

	@Override
	public boolean check(String externalDisplayName, FieldPayload fieldPayload) {
		displayErrorMsg = null;
		if ((minValue != null && fieldPayload != null
				&& fieldPayload.getStringPayload() != null && minValue.compareTo(new BigDecimal(fieldPayload.getStringPayload().length())) > 0)
				|| (maxValue != null && fieldPayload != null
						&& fieldPayload.getStringPayload() != null && maxValue.compareTo(new BigDecimal(fieldPayload.getStringPayload().length())) < 0)) {
			if(externalDisplayName != null){
				displayErrorMsg = MessageFormat.format(errorMsg, externalDisplayName, fieldPayload.getStringPayload().length(), fieldPayload.getStringPayload(), minValue, maxValue);
			}else{
				displayErrorMsg = MessageFormat.format(errorMsg, displayName, fieldPayload.getStringPayload().length(), fieldPayload.getStringPayload(), minValue, maxValue);
			}
			return false;
		}
		return true;
	}
}
