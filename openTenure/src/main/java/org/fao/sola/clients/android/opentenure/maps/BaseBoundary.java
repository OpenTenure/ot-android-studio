/**
 * 
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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;

import com.androidmapsextensions.ClusterGroup;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.PolygonOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;

import org.fao.sola.clients.android.opentenure.DisplayNameLocalizer;
import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.model.Adjacency.CardinalDirection;
import org.fao.sola.clients.android.opentenure.model.Boundary;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimType;
import org.fao.sola.clients.android.opentenure.model.HoleVertex;
import org.fao.sola.clients.android.opentenure.model.PropertyLocation;
import org.fao.sola.clients.android.opentenure.model.Vertex;
import org.fao.sola.clients.android.opentenure.tools.GisUtility;
import org.fao.sola.clients.android.opentenure.tools.StringUtility;

import java.util.ArrayList;
import java.util.List;

public class BaseBoundary {

	static final float BOUNDARY_Z_INDEX = 2.0f;
	static final double SNAP_THRESHOLD = 0.00005;

	protected String name;
	protected Boundary boundary;
	List<LatLng> vertices = new ArrayList<LatLng>();

    List<LatLng> getVertices() {
        return vertices;
    }

    protected Context context;
	private com.androidmapsextensions.Polygon displayPolygon = null;
	protected Polygon polygon = null;
	protected GoogleMap map;
	protected LatLng center = null;
	protected double area = 0.0;
	Marker boundaryMarker = null;
	protected LatLngBounds bounds = null;
	protected int color = Color.BLUE;
	protected int transparency = 0x0fffffff;

	public String getName() {
		return name;
	}

	public LatLng getCenter() {
		return center;
	}

	public Polygon getPolygon() {
		return polygon;
	}

	public Marker getMarker() {
		return boundaryMarker;
	}

	public LatLngBounds getBounds() {
		return bounds;
	}

	public BaseBoundary(final Context context, final GoogleMap map, Boundary boundary) {
		this.context = context;
		this.map = map;
		this.boundary = boundary;
		loadBoundary();
	}

	protected void loadBoundary(){
		if(boundary != null){
			vertices = GisUtility.getVertices(boundary.getGeom());
			name = StringUtility.isEmpty(boundary.getName()) ? context.getResources().getString(R.string.default_boundary_name) : boundary.getName();

			if(StringUtility.empty(boundary.getStatusCode()).equals("approved")){
				color = context.getResources().getColor(R.color.status_moderated);
				transparency = 0;
			} else {
				color = context.getResources().getColor(R.color.status_created);
				transparency = 0x0fffffff;
			}
		}
		if (vertices != null && vertices.size() > 0) {
			calculateGeometry();
		}
	}

	protected boolean validateGeometry(){
		GeometryFactory gf = new GeometryFactory();
		Geometry shell = GisUtility.getGeomFromVertices(vertices);
		Geometry geometry;
		if(shell instanceof LinearRing){
			geometry = gf.createPolygon(shell.getCoordinates());
			geometry.setSRID(Constants.SRID);
		}else{
			geometry = shell;
		}
		boolean isValid;
		if(geometry != null){
			isValid = geometry.isValid();
		}else{
			isValid = false;
		}
		return isValid;
	}

	private Marker createMarker(LatLng position, String title) {
		Rect boundsText = new Rect();
		Paint tf = new Paint();
		tf.setTypeface(Typeface.create((String) null, Typeface.NORMAL));
		tf.setTextSize(20);
		tf.setTextAlign(Align.CENTER);
		tf.setAntiAlias(true);
		tf.setColor(color);
		try {
			tf.getTextBounds(name, 0, name.length(), boundsText);
		} catch (Exception e) {
			name = context.getResources().getString(R.string.default_boundary_name);
			tf.getTextBounds(name, 0, name.length(), boundsText);
		}

		Bitmap.Config conf = Bitmap.Config.ARGB_8888;
		Bitmap bmpText = Bitmap.createBitmap(boundsText.width(), boundsText.height() - boundsText.bottom, conf);

		Canvas canvasText = new Canvas(bmpText);
		canvasText.drawText(name, canvasText.getWidth() / 2, canvasText.getHeight(), tf);

		Marker marker = map.addMarker(new MarkerOptions().position(position)
				.title(title).icon(BitmapDescriptorFactory.fromBitmap(bmpText))
				.anchor(0.5f, 1));
		marker.setClusterGroup(Constants.BOUNDARY_MARKERS_GROUP);
		return marker;
	}

	protected void hideBoundary() {
		if (displayPolygon != null) {
			displayPolygon.remove();
		}
		if (boundaryMarker != null) {
			boundaryMarker.remove();
		}
	}

	protected void redrawBoundary() {
		hideBoundary();
		showBoundary();
	}

	protected void showBoundary() {
		if (vertices.size() <= 0) {
			return;
		}

		PolygonOptions polygonOptions = new PolygonOptions();
		for (LatLng vertex: vertices) {
			polygonOptions.add(vertex);
		}

		polygonOptions.zIndex(BOUNDARY_Z_INDEX);
		polygonOptions.strokeColor(color);
		polygonOptions.strokeWidth(4);
		polygonOptions.fillColor(color & transparency); // 50% transparency
		displayPolygon = map.addPolygon(polygonOptions);

		boundaryMarker = createMarker(center, StringUtility.empty(boundary.getName()));
	}

	protected void calculateGeometry() {

		if (vertices == null || vertices.size() <= 0) {
			return;
		}
		if (vertices.size() <= 1) {
			center = vertices.get(0);
			bounds = new LatLngBounds(center, center);
			return;
		}

		GeometryFactory gf = new GeometryFactory();

		// need at least 4 coordinates for a three vertices polygon
		Coordinate[] coords = new Coordinate[(vertices.size() + 1) > 4 ? (vertices.size() + 1) : 4];

		int i = 0;

		for (LatLng vertex : vertices) {
			coords[i++] = new Coordinate(vertex.longitude, vertex.latitude);
		}

		if (vertices.size() == 2) {
			// the source is a line segment so we replicate the second vertex to
			// create a three vertices polygon
			coords[i++] = new Coordinate(
					vertices.get(1).longitude, vertices.get(1).latitude);
		}

		// then we close the polygon

		coords[coords.length - 1] = new Coordinate(vertices.get(0).longitude, vertices.get(0).latitude);

		polygon = gf.createPolygon(coords);
		polygon.setSRID(Constants.SRID);
		validateGeometry();

		Geometry envelope = polygon.getEnvelope();

		switch (envelope.getCoordinates().length) {
			case 1:
				// the envelope is a point
				bounds = new LatLngBounds(new LatLng(
						envelope.getCoordinates()[0].y,
						envelope.getCoordinates()[0].x), new LatLng(
						envelope.getCoordinates()[0].y,
						envelope.getCoordinates()[0].x));
				break;
			case 2:
				// the envelop is a line segment
				bounds = new LatLngBounds(new LatLng(
						envelope.getCoordinates()[0].y,
						envelope.getCoordinates()[0].x), new LatLng(
						envelope.getCoordinates()[1].y,
						envelope.getCoordinates()[1].x));
				break;
			default:
				bounds = new LatLngBounds(new LatLng(
						envelope.getCoordinates()[0].y,
						envelope.getCoordinates()[0].x), new LatLng(
						envelope.getCoordinates()[2].y,
						envelope.getCoordinates()[2].x));
		}

		try {
			center = new LatLng(polygon.getInteriorPoint().getCoordinate().y,
					polygon.getInteriorPoint().getCoordinate().x);
		} catch (Exception e) {
			center = new LatLng(polygon.getCentroid().getCoordinate().y,
					polygon.getCentroid().getCoordinate().x);
		}
	}

	CardinalDirection getCardinalDirection(BaseBoundary dest) {
		double deltaX = dest.getCenter().longitude - center.longitude;
		double deltaY = dest.getCenter().latitude - center.latitude;
		if (deltaX == 0) {
			return deltaY > 0 ? CardinalDirection.NORTH	: CardinalDirection.SOUTH;
		}
		double slope = deltaY / deltaX;
		if (slope >= -1.0 / 3.0 && slope < 1.0 / 3.0) {
			return deltaX > 0 ? CardinalDirection.EAST : CardinalDirection.WEST;
		} else if (slope >= 1.0 / 3.0 && slope < 3.0) {
			return deltaY > 0 ? CardinalDirection.NORTHEAST	: CardinalDirection.SOUTHWEST;
		} else if (slope >= 3.0 || slope <= -3.0) {
			return deltaY > 0 ? CardinalDirection.NORTH	: CardinalDirection.SOUTH;
		} else {
			return deltaY > 0 ? CardinalDirection.NORTHWEST	: CardinalDirection.SOUTHEAST;
		}
	}

}
