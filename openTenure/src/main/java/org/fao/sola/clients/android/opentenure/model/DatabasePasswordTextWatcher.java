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
package org.fao.sola.clients.android.opentenure.model;

import org.fao.sola.clients.android.opentenure.R;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

public class DatabasePasswordTextWatcher implements TextWatcher{

	private static final long MIN_TIME_BETWEEN_TOAST = 500;
	private static final int MIN_PASSWORD_LENGTH = 6;

	long lastTime = System.currentTimeMillis();
	EditText input = null;
	Context context;
	
	public DatabasePasswordTextWatcher(EditText input, Context context){
		this.input = input;
		this.context = context;
	}

	@Override
	public void afterTextChanged(Editable s) {
		isValid();
	}
	
	private boolean checkPasswordCharacter(String s){
		
		for (int i = 0; i < s.length(); i++) {
			if(!Character.isLetterOrDigit(s.charAt(i))){
				return false;
			}
		}
		return true;
	}

	public boolean isValid(){
		if("".equalsIgnoreCase(input.getText().toString())){
			return true;
		}else if (input.getText().length() < MIN_PASSWORD_LENGTH) {
			input.setTextColor(Color.RED);
			if (System.currentTimeMillis() - lastTime > MIN_TIME_BETWEEN_TOAST) {
				String message = String.format(context.getResources().getString(R.string.message_password_too_short), MIN_PASSWORD_LENGTH);
				Toast.makeText(context,
						message,
						Toast.LENGTH_SHORT).show();
			}
			lastTime = System.currentTimeMillis();
			return false;
		}else if (!checkPasswordCharacter(input.getText().toString())) {
			input.setTextColor(Color.RED);
			if (System.currentTimeMillis() - lastTime > MIN_TIME_BETWEEN_TOAST) {
				Toast.makeText(context,
						context.getResources().getString(R.string.message_password_character_is_wrong),
						Toast.LENGTH_SHORT).show();
			}
			lastTime = System.currentTimeMillis();
			return false;

		} else {
			input.setTextColor(Color.BLACK);
			return true;
		}
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before,
			int count) {

	}
}

