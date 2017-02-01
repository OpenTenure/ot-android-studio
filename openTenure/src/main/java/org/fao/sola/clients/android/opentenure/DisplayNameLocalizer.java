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


public class DisplayNameLocalizer {
	/*
	 * Display names in all available languages are concatenated using '::::' as a separator
	 * current application language is coded as an integer identifying the place of the label
	 * localized according to that language in the concatenated string
	 */
	private static final String separator = "::::";
	private int currentLanguageItemOrder;
	private int defaultLanguageItemOrder;
	
	public DisplayNameLocalizer(String localization){
		currentLanguageItemOrder = 1;
		defaultLanguageItemOrder = 1;

		org.fao.sola.clients.android.opentenure.model.Language defaultLanguage = org.fao.sola.clients.android.opentenure.model.Language.getDefaultLanguage();
		
		if(defaultLanguage != null){
			defaultLanguageItemOrder = defaultLanguage.getItemOrder();
		}
		
		org.fao.sola.clients.android.opentenure.model.Language currentLanguage = org.fao.sola.clients.android.opentenure.model.Language.getLanguage(OpenTenureApplication.getInstance().getLocalization());
		
		if(currentLanguage != null){
			currentLanguageItemOrder = currentLanguage.getItemOrder();
		}
		
	}
	
	public String getLocalizedDisplayName(String unlocalizedDisplayName){
		
		int langOrder = currentLanguageItemOrder -1;
		int defaultLangOrder = defaultLanguageItemOrder -1;
		
		/*
		 * try to return requested language, then default language, then first, then the string itself
		 */

		if (unlocalizedDisplayName == null) {
			return null;
		}
		if(unlocalizedDisplayName.indexOf(separator) == -1){
			return unlocalizedDisplayName;
		}

		String[] tokens = unlocalizedDisplayName.split(separator);

		if(tokens.length == 1 || langOrder < 0 || defaultLangOrder < 0){
			return tokens[0];
		}
		if(tokens.length > langOrder && tokens[langOrder] != null && !"".equalsIgnoreCase(tokens[langOrder])){
			return tokens[langOrder];
		}
		if(tokens.length > defaultLangOrder && tokens[defaultLangOrder] != null && !"".equalsIgnoreCase(tokens[defaultLangOrder])){
			return tokens[defaultLangOrder];
		}
		return tokens[0];
	}
}
