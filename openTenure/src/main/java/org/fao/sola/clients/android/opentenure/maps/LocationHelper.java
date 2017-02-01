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

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;

import com.google.android.gms.maps.model.LatLng;

import android.hardware.GeomagneticField;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class LocationHelper {

	public static final String CURRENT = "CURRENT";
	private static final long LOCATION_LISTENER_INTERVAL_FAST = 1 * 1000;
	private static final long LOCATION_LISTENER_INTERVAL_SLOW = 60 * 1000;
	private static final float LOCATION_LISTENER_SHORT_DISTANCE = 1;
	private static final float LOCATION_LISTENER_LONG_DISTANCE = 10;
	private static final double HOME_LATITUDE = 41.8825;
	private static final double HOME_LONGITUDE = 12.4882;

	private LocationManager locationManager;
	private Location currentLocation = null;
	private LocationListener customListener;
	private GeomagneticField magField = null;


	public GeomagneticField getMagField() {
		return magField;
	}

	public Location getCurrentLocation() {
		return currentLocation;
	}

	public void setCustomListener(LocationListener customListener) {
		this.customListener = customListener;
	}

	LocationHelper(LocationManager locationManager) {
		this.locationManager = locationManager;
	}

	public void start() {
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				LOCATION_LISTENER_INTERVAL_FAST, LOCATION_LISTENER_SHORT_DISTANCE, gpsLL);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER,
				LOCATION_LISTENER_INTERVAL_FAST, LOCATION_LISTENER_SHORT_DISTANCE, networkLL);
	}

	public void stop() {
		locationManager.removeUpdates(gpsLL);
		locationManager.removeUpdates(networkLL);
	}

	public void hurryUp() {
		locationManager.removeUpdates(gpsLL);
		locationManager.removeUpdates(networkLL);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				LOCATION_LISTENER_INTERVAL_FAST, LOCATION_LISTENER_SHORT_DISTANCE, gpsLL);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER,
				LOCATION_LISTENER_INTERVAL_FAST, LOCATION_LISTENER_SHORT_DISTANCE, networkLL);
	}

	public void slowDown() {
		locationManager.removeUpdates(gpsLL);
		locationManager.removeUpdates(networkLL);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				LOCATION_LISTENER_INTERVAL_SLOW, LOCATION_LISTENER_LONG_DISTANCE, gpsLL);
		locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER,
				LOCATION_LISTENER_INTERVAL_SLOW, LOCATION_LISTENER_LONG_DISTANCE, networkLL);
	}

	public LatLng getLastKnownLocation() {
		Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

		// prefer the newer
		if(gpsLocation != null && networkLocation != null && networkLocation.getTime() > gpsLocation.getTime()){
			return new LatLng(networkLocation.getLatitude(), networkLocation.getLongitude());
		}else if(gpsLocation != null && networkLocation != null && networkLocation.getTime() <= gpsLocation.getTime()){
			return new LatLng(gpsLocation.getLatitude(), gpsLocation.getLongitude());
		}else if(gpsLocation != null){
			// or prefer GPS if existing
			return new LatLng(gpsLocation.getLatitude(), gpsLocation.getLongitude());
		}else if (networkLocation != null){
			// or prefer network if existing
			return new LatLng(networkLocation.getLatitude(), networkLocation.getLongitude());
		}else{
			// or prefer the latest stored in OT
			org.fao.sola.clients.android.opentenure.model.Location otLocation = org.fao.sola.clients.android.opentenure.model.Location
					.getLocation(CURRENT);
			if (otLocation != null) {
				return new LatLng(otLocation.getLat(), otLocation.getLon());
			} else {
				// Last resort FAO HQ
				return new LatLng(HOME_LATITUDE, HOME_LONGITUDE);
			}
		}
	}

	LocationListener networkLL = new LocationListener() {

		public void onLocationChanged(Location location) {

			if(currentLocation == null || (currentLocation != null && location.getTime() > currentLocation.getTime())){
				currentLocation = new Location (location);
				magField = new GeomagneticField(
			            (float)currentLocation.getLatitude(),
			            (float)currentLocation.getLongitude(),
			            (float)currentLocation.getAltitude(),
			            System.currentTimeMillis()
			        );
				if (OpenTenureApplication.getInstance().getDatabase().isOpen()) {
					org.fao.sola.clients.android.opentenure.model.Location loc = org.fao.sola.clients.android.opentenure.model.Location
							.getLocation(CURRENT);
					loc.setLat(location.getLatitude());
					loc.setLon(location.getLongitude());
					loc.update();
				}
			}
			
			Log.d(this.getClass().getName(), "onLocationChanged");
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.d(this.getClass().getName(), "onStatusChanged");
		}

		public void onProviderEnabled(String provider) {
			Log.d(this.getClass().getName(), "onProviderEnabled");
		}

		public void onProviderDisabled(String provider) {
			Log.d(this.getClass().getName(), "onProviderDisabled");
		}
	};

	LocationListener gpsLL = new LocationListener() {

		public void onLocationChanged(Location location) {

			if(currentLocation == null || (currentLocation != null && location.getTime() > currentLocation.getTime())){
				currentLocation = new Location (location);
				magField = new GeomagneticField(
			            (float)currentLocation.getLatitude(),
			            (float)currentLocation.getLongitude(),
			            (float)currentLocation.getAltitude(),
			            System.currentTimeMillis()
			        );
				if (OpenTenureApplication.getInstance().getDatabase().isOpen()) {
					org.fao.sola.clients.android.opentenure.model.Location loc = org.fao.sola.clients.android.opentenure.model.Location
							.getLocation(CURRENT);
					loc.setLat(location.getLatitude());
					loc.setLon(location.getLongitude());
					loc.update();
				}
			}

			if(customListener != null){
				customListener.onLocationChanged(location);
			}
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.d(this.getClass().getName(), "onStatusChanged");
		}

		public void onProviderEnabled(String provider) {
			Log.d(this.getClass().getName(), "onProviderEnabled");
		}

		public void onProviderDisabled(String provider) {
			Log.d(this.getClass().getName(), "onProviderDisabled");
		}
	};

}
