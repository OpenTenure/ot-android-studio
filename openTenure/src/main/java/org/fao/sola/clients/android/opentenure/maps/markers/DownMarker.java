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
package org.fao.sola.clients.android.opentenure.maps.markers;

import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.maps.Constants;

import android.content.Context;
import android.graphics.Point;

import com.androidmapsextensions.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;

public class DownMarker extends TargetMoverMarker {
	
	private Context context;

	public DownMarker(Context context, Marker selectedMarker, Marker target, GoogleMap map) {
		super(selectedMarker, target, map);
		this.context = context;
	}
	
	public void refresh(Point screenPosition, int iconWidth, int iconHeight){
		if(marker != null){
			Projection projection = map.getProjection();
			marker.setRotation(DOWN_INITIAL_ROTATION);
			marker.setPosition(projection.fromScreenLocation(getControlDownPosition(screenPosition, iconWidth, iconHeight)));
		}
	}
	
	public boolean onClick(Marker marker) {
		Projection projection = map.getProjection();
		Point screenLocation = projection.toScreenLocation(target.getPosition());
		screenLocation.y += PIXELS_PER_STEP;
		target.setPosition(projection.fromScreenLocation(screenLocation));
		target.setTitle("Lat: " + target.getPosition().latitude + ", Lon: " + target.getPosition().longitude + ", Dist: " + getTargetDistance());
		target.showInfoWindow();
		return true;
	}

	public void show(Projection projection, Point screenPosition, int iconWidth, int iconHeight){
		marker = map.addMarker(new MarkerOptions()
		.position(projection.fromScreenLocation(getControlDownPosition(screenPosition, iconWidth, iconHeight)))
		.anchor(0.5f, 0.5f)
		.title(context.getString(R.string.down))
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_menu_forward)).rotation(DOWN_INITIAL_ROTATION));
		marker.setClusterGroup(Constants.MARKER_RELATIVE_EDIT_DOWN_GROUP);
	}

	private Point getControlDownPosition(Point markerScreenPosition, int markerWidth, int markerHeight){
		return new Point(markerScreenPosition.x, markerScreenPosition.y + 8*markerHeight);
	}

}
