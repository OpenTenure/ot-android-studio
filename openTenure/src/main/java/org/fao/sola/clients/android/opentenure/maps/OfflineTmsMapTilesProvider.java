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
package org.fao.sola.clients.android.opentenure.maps;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.OpenTenurePreferencesActivity;

import android.content.SharedPreferences;

public class OfflineTmsMapTilesProvider extends OfflineTilesProvider{

    private String URL_STRING;
    
    public OfflineTmsMapTilesProvider(int width, int height, SharedPreferences preferences) {
    	super(width, height);
		URL_STRING = preferences.getString(
				OpenTenurePreferencesActivity.TMS_URL_PREF,
				"http://host/path?x=%d&y=%d&z=%d");
	}
    
    protected String getUrl(int x, int y, int zoom){
        return String.format(Locale.US, URL_STRING, x, y, zoom);
    }

	@Override
	public URL getTileUrl(int x, int y, int zoom) {
        try {       
            URL url = null;

            try {
                url = new URL(String.format(Locale.US, URL_STRING, x, y, zoom));
            } 
            catch (MalformedURLException e) {
                throw new AssertionError(e);
            }
            return url;
        }
        catch (RuntimeException e) {
            throw e;
        }

	}

	protected TilesProviderType getType() {
		return TilesProviderType.TMS;
	}

	protected String getBaseStorageDir() {
		return OpenTenureApplication.getContext().getExternalFilesDir(null).getAbsolutePath() + "/tiles/" + getType() + "/" ;
	}

	protected String getTilesSuffix() {
		return ".jpg";
	}

}