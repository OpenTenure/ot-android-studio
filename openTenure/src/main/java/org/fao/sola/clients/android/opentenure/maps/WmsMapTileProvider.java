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

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.OpenTenurePreferencesActivity;
import org.fao.sola.clients.android.opentenure.model.Tile;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.UrlTileProvider;

public class WmsMapTileProvider extends UrlTileProvider{
	// Web Mercator upper left corner of the world map.
	private static final double[] TILE_ORIGIN = {-20037508.34789244, 20037508.34789244};
	//array indexes for that data
	private static final int ORIG_X = 0; 
	private static final int ORIG_Y = 1; // "

	// Size of square world map in meters, using WebMerc projection.
	private static final double MAP_SIZE = 20037508.34789244 * 2;

	// array indexes for array to hold bounding boxes.
	public static final int MINX = 0;
	public static final int MINY = 1;
	public static final int MAXX = 2;
	public static final int MAXY = 3;

	// array indexes for array to hold tile x y.
	public static final int X = 0;
	public static final int Y = 1;

	// array indexes for array to hold tile x y.
	public static final int NORTH_EAST_X = 0;
	public static final int NORTH_EAST_Y = 1;
	public static final int SOUTH_WEST_X = 2;
	public static final int SOUTH_WEST_Y = 3;

    private static final String version = "1.1.0";
    private static final String request = "GetMap";
    private static final String format = "image/png";
    private static final String srs = "EPSG:"+Constants.SRID;
    private static final String service = "WMS";
    private static final String styles = "";

    final String URL_STRING;
    
    public WmsMapTileProvider(int width, int height, SharedPreferences preferences) {
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
    
    private String getUrl(double[] bbox){
        return String.format(Locale.US, URL_STRING, bbox[MINX], 
                bbox[MINY], bbox[MAXX], bbox[MAXY]);
    }

    @Override
    public synchronized URL getTileUrl(int x, int y, int zoom) {
        try {       
            Log.d("TileRequest", "x = " + x + ", y = " + y + ", zoom = " + zoom);

            double[] bbox = getBoundingBox(x, y, zoom);

            String urlString = getUrl(bbox);

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

	public static int[] getXY(LatLng northeast, LatLng southwest, int zoom) {
		double tileSize = MAP_SIZE / Math.pow(2, zoom);
		int[] tile = new int[4];
		tile[NORTH_EAST_X] = BigDecimal.valueOf((northeast.longitude - TILE_ORIGIN[ORIG_X]) / tileSize).intValue();
		tile[NORTH_EAST_Y] = - BigDecimal.valueOf((northeast.latitude - TILE_ORIGIN[ORIG_Y]) / tileSize).intValue();
		tile[SOUTH_WEST_X] = BigDecimal.valueOf((southwest.longitude - TILE_ORIGIN[ORIG_X]) / tileSize).intValue();
		tile[SOUTH_WEST_Y] = - BigDecimal.valueOf((southwest.latitude - TILE_ORIGIN[ORIG_Y]) / tileSize).intValue();
		return tile;
	}

	public static int[] getXY(LatLng coord, int zoom) {
		double tileSize = MAP_SIZE / Math.pow(2, zoom);
		int[] tile = new int[2];
		tile[X] = BigDecimal.valueOf((coord.longitude - TILE_ORIGIN[ORIG_X]) / tileSize).intValue();
		tile[Y] = - BigDecimal.valueOf((coord.latitude - TILE_ORIGIN[ORIG_Y]) / tileSize).intValue();
		return tile;
	}

	public static double mercatorFromLatitude(double latitude) {
	    double radians = Math.log(Math.tan(Math.toRadians(latitude+90.0)/2));
	    double mercator = Math.toDegrees(radians);
	    return mercator;
	}
	
	public static int[] tileOfCoordinate(LatLng coord, int zoom) {
	    int[] result = new int[2];
		int noTiles = (1 << zoom);
	    double longitudeSpan = 360.0 / noTiles;
	    result[X] = BigDecimal.valueOf((coord.longitude +180.0)/longitudeSpan).intValue();
	    result[Y] = -(BigDecimal.valueOf(((noTiles * (mercatorFromLatitude(coord.latitude) - 180.0)))/360.0).intValue());

	    return result;
	}
	
	public static double latitudeFromMercator(double mercator) {
	    double radians = Math.atan(Math.exp(Math.toRadians(mercator)));
	    double latitude = Math.toDegrees(2 * radians) - 90;
	    return latitude;
	}
	
	public static LatLngBounds boundsOfTile(int x, int y, int zoom) {
	    int noTiles = (1 << zoom);
	    double longitudeSpan = 360.0 / noTiles;
	    double longitudeMin = -180.0 + x * longitudeSpan;

	    double mercatorMax = 180 - (((double) y) / noTiles) * 360;
	    double mercatorMin = 180 - (((double) y + 1) / noTiles) * 360;
	    double latitudeMax = latitudeFromMercator(mercatorMax);
	    double latitudeMin = latitudeFromMercator(mercatorMin);

	    LatLngBounds bounds = new LatLngBounds(new LatLng(latitudeMin, longitudeMin), new LatLng(latitudeMax, longitudeMin + longitudeSpan));
	    return bounds;
	}
	
	public List<Tile> getTilesForLatLngBounds(LatLngBounds llb, int startZoom, int endZoom){
		List<Tile> tiles = new ArrayList<Tile>();
		
		int[] northeast = tileOfCoordinate(llb.northeast, startZoom);
		int[] southwest = tileOfCoordinate(llb.southwest, startZoom);
		
		for(int zoom = startZoom ; zoom <= endZoom; zoom++){
			
			for(int x = southwest[X] ; x <= northeast[X] ; x++){
				for(int y = northeast[Y] ; y <= southwest[Y] ; y++){
					Tile tile = new Tile();
					tile.setUrl(getUrl(getBoundingBox(x, y, zoom)));
					tile.setFileName(OpenTenureApplication.getContext().getExternalFilesDir(null).getAbsolutePath() + "/tiles/" + zoom + "/" + x + "/" + y + ".png");
					tiles.add(tile);
				}
			}

			// At each subsequent level of zoom, tiles indexes double

			northeast[X] *= 2;
			northeast[Y] *= 2;
			southwest[X] *= 2;
			southwest[Y] *= 2;
		}
		
		return tiles;
	}

}