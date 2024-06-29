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

import java.util.ArrayList;
import java.util.List;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.model.Vertex;
import org.fao.sola.clients.android.opentenure.tools.GisUtility;

import com.androidmapsextensions.Polyline;
import com.androidmapsextensions.PolylineOptions;
import com.androidmapsextensions.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class CommunityArea {
	GoogleMap map = null;
	protected static ArrayList<Polyline> polylines = null;

	public void drawInterestArea() {
		String boundary = null;
		if(polylines == null) {
			polylines = new ArrayList<Polyline>();
		}

		// Clear first
		removeFromMap();
		polylines.clear();

		if(OpenTenureApplication.getInstance().getProject() != null) {
			boundary = OpenTenureApplication.getInstance().getProject().getBoundary();
		}

		if(boundary == null || boundary.equals(""))
			return;

		Geometry cAreaGeom = GisUtility.getGeomFromWkt(boundary);

		if (cAreaGeom instanceof Polygon) {
			addToMap((Polygon) cAreaGeom);
		} else if (cAreaGeom instanceof MultiPolygon) {
			MultiPolygon mPolygon = (MultiPolygon) cAreaGeom;
			for(int i = 0; i < mPolygon.getNumGeometries(); i++) {
				if(mPolygon.getGeometryN(i) instanceof Polygon) {
					addToMap((Polygon) mPolygon.getGeometryN(i));
				}
			}
		}
	}

	private void addToMap (Polygon geom) {
		List<Vertex> communityAreaVertices = Vertex.shellFromPolygon(geom);
		PolylineOptions polylineOptions = new PolylineOptions();

		for (int i = 0; i < communityAreaVertices.size(); i++) {
			polylineOptions.add(communityAreaVertices.get(i).getMapPosition());
		}

		// Needed in order to close the polyline
		polylineOptions.add(communityAreaVertices.get(0).getMapPosition());

		polylineOptions.zIndex(BasePropertyBoundary.BOUNDARY_Z_INDEX);
		polylineOptions.width(4);
		polylineOptions.color(OpenTenureApplication.getContext().getResources().getColor(R.color.community_area));
		polylines.add(map.addPolyline(polylineOptions));
	}

	public CommunityArea(GoogleMap map) {
		this.map = map;
	}

	public static ArrayList<Polyline> getPolylines() {
		return polylines;
	}

	public static List<LatLng> getPoints(){
		List<LatLng> points = new ArrayList<LatLng>();
		if(polylines != null && !polylines.isEmpty()) {
			for (Polyline polyline: polylines) {
				points.addAll(polyline.getPoints());
			}
		}
		return points;
	}

	public static void removeFromMap() {
		if(polylines != null && !polylines.isEmpty()) {
			for (Polyline polyline: polylines) {
				polyline.remove();
			}
		}
	}
}
