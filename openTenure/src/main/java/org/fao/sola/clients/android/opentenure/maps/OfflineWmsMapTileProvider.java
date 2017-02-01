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
import android.util.Log;

public class OfflineWmsMapTileProvider extends OfflineTilesProvider{
	// Web Mercator upper left corner of the world map.
	private static final double[] TILE_ORIGIN = {-20037508.34789244, 20037508.34789244};
	//array indexes for that data
	private static final int ORIG_X = 0; 
	private static final int ORIG_Y = 1;

	// Size of square world map in meters, using WebMerc projection.
	private static final double MAP_SIZE = 20037508.34789244 * 2.0;

	// array indexes for array to hold bounding boxes.
	public static final int MINX = 0;
	public static final int MINY = 1;
	public static final int MAXX = 2;
	public static final int MAXY = 3;

    private static final String version = "1.1.0";
    private static final String request = "GetMap";
    private static final String format = "image/png";
    private static final String srs = "EPSG:"+Constants.SRID;
    private static final String service = "WMS";
    private static final String styles = "";

    final String URL_STRING;
    
    public OfflineWmsMapTileProvider(int width, int height, SharedPreferences preferences) {
    	super(width, height);
		String baseURL = preferences.getString(
				OpenTenurePreferencesActivity.GEOSERVER_URL_PREF,
				"http://demo.flossola.org:8080/geoserver/sola/wms");
		String layer = preferences.getString(
				OpenTenurePreferencesActivity.GEOSERVER_LAYER_PREF,
				"sola:nz_orthophoto");
		URL_STRING = baseURL + 
	            "/wms?layers=" + layer + 
	            "&version=" + version + 
	            "&service=" + service + 
	            "&request=" + request + 
	            "&transparent=true&styles=" + styles + 
	            "&format=" + format + 
	            "&srs=" + srs + 
	            "&bbox=%f,%f,%f,%f" + 
	            "&width=" + Integer.toString(width) + 
	            "&height=" + Integer.toString(height);
	}
    
    protected String getUrl(int x, int y, int zoom){
    	
    	double[] bbox = getBoundingBox(x, y, zoom);
        return String.format(Locale.US, URL_STRING, bbox[MINX], 
                bbox[MINY], bbox[MAXX], bbox[MAXY]);
    }

    @Override
    public synchronized URL getTileUrl(int x, int y, int zoom) {
        try {       
            Log.d("TileRequest", "x = " + x + ", y = " + y + ", zoom = " + zoom);

            String urlString = getUrl(x, y, zoom);

            Log.d("GeoServerTileURL", urlString);

            URL url = null;

            try {
                url = new URL(urlString);
            } 
            catch (MalformedURLException e) {
                throw new AssertionError(e);
            }
            return url;
        }
        catch (RuntimeException e) {
            Log.d("GeoServerTileException", "getTile x=" + x + ", y=" + y + ", zoomLevel=" + zoom + " raised an exception", e);
            throw e;
        }

    }
    
    // Returns bounding box given tile x/y indexes and a zoom level.
	public static double[] getBoundingBox(int x, int y, int zoom) {

		double tileSize = MAP_SIZE / Math.pow(2, zoom);
	    double minx = TILE_ORIGIN[ORIG_X] + x * tileSize;
	    double maxx = TILE_ORIGIN[ORIG_X] + (x+1) * tileSize;
	    double miny = TILE_ORIGIN[ORIG_Y] - (y+1) * tileSize;
	    double maxy = TILE_ORIGIN[ORIG_Y] - y * tileSize;

	    double[] bbox = new double[4];
	    bbox[MINX] = minx;
	    bbox[MINY] = miny;
	    bbox[MAXX] = maxx;
	    bbox[MAXY] = maxy;

	    return bbox;
	}

	protected TilesProviderType getType() {
		return TilesProviderType.GeoServer;
	}

	protected String getBaseStorageDir() {
		return OpenTenureApplication.getContext().getExternalFilesDir(null).getAbsolutePath() + "/tiles/" + getType() + "/";
	}

	protected String getTilesSuffix() {
		return ".png";
	}

}