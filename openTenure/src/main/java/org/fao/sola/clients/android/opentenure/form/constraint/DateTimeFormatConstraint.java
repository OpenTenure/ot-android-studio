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

import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.fao.sola.clients.android.opentenure.form.FieldConstraint;
import org.fao.sola.clients.android.opentenure.form.FieldConstraintType;
import org.fao.sola.clients.android.opentenure.form.FieldPayload;
import org.fao.sola.clients.android.opentenure.form.FieldType;

public class DateTimeFormatConstraint extends FieldConstraint {
	
	public DateTimeFormatConstraint(){
		super();
		fieldConstraintType=FieldConstraintType.DATETIME;
		addApplicableType(FieldType.DATE);
		addApplicableType(FieldType.TIME);
		this.errorMsg = "Value {1} of {0} is not in {2} format";
	}

	public DateTimeFormatConstraint(DateTimeFormatConstraint dtfc){
		super(dtfc);
		this.format = new String(dtfc.getFormat());
	}

	public DateTimeFormatConstraint(String format){
		super();
		fieldConstraintType=FieldConstraintType.DATETIME;
		addApplicableType(FieldType.DATE);
		addApplicableType(FieldType.TIME);
		setFormat(format!=null ? format : "yyyy-MM-dd");
	}

	@Override
	public boolean check(String externalDisplayName, FieldPayload fieldPayload) {
		displayErrorMsg = null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(format!=null ? format : "yyyy-MM-dd", Locale.US);
			String payload = fieldPayload.getStringPayload();
			if(payload != null){
				sdf.parse(payload);
			}
		} catch (ParseException e) {
			if(externalDisplayName != null){
				displayErrorMsg = MessageFormat.format(errorMsg, externalDisplayName, fieldPayload.getStringPayload(), format);
			}else{
				displayErrorMsg = MessageFormat.format(errorMsg, displayName, fieldPayload.getStringPayload(), format);
			}
			return false;
		}
		return true;
	}
}
