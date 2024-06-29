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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fao.sola.clients.android.opentenure.DisplayNameLocalizer;
import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.model.Adjacency.CardinalDirection;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimType;
import org.fao.sola.clients.android.opentenure.model.HoleVertex;
import org.fao.sola.clients.android.opentenure.model.PropertyLocation;
import org.fao.sola.clients.android.opentenure.model.Vertex;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;

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

public class BasePropertyBoundary {

	static final float BOUNDARY_Z_INDEX = 2.0f;
	static final double SNAP_THRESHOLD = 0.00005;

	protected String name;
	protected Claim claim;
	protected String claimId;
	List<Vertex> vertices = new ArrayList<Vertex>();
	List<List<HoleVertex>> holesVertices = new ArrayList<List<HoleVertex>>();
	Map<Marker, PropertyLocation> locationsMap = new HashMap<Marker, PropertyLocation>();
	private boolean locationsVisible = false;

	public boolean isLocationsVisible() {
		return locationsVisible;
	}

    public List<Vertex> getVertices() {
        return vertices;
    }

    List<List<HoleVertex>> getHolesVertices() {
        return holesVertices;
    }

    protected Context context;
	private com.androidmapsextensions.Polygon displayPolygon = null;
	protected Polygon polygon = null;
	protected GoogleMap map;
	protected LatLng center = null;
	protected double area = 0.0;
	Marker propertyMarker = null;
	protected LatLngBounds bounds = null;
	protected int color = Color.BLUE;

	public String getName() {
		return name;
	}

	public String getClaimId() {
		return claimId;
	}

	public LatLng getCenter() {
		return center;
	}

	public Polygon getPolygon() {
		return polygon;
	}

	public Marker getMarker() {
		return propertyMarker;
	}

	public LatLngBounds getBounds() {
		return bounds;
	}

	protected void reload() {
		if (claimId != null) {
			loadClaim(false);
		}
	}

	protected void loadClaim(boolean updateArea) {
		claim = Claim.getClaim(claimId);
		if(claim != null){
			vertices = claim.getVertices();
			holesVertices = claim.getHolesVertices();
			name = claim.getName() == null || claim.getName().equalsIgnoreCase("") ? context
					.getResources().getString(R.string.default_claim_name) : claim
					.getName();

			String status = claim.getStatus();
			claimId = claim.getClaimId();

			if (status != null) {

				switch (Claim.Status.valueOf(status)) {

					case unmoderated:
						color = context.getResources().getColor(
								R.color.status_unmoderated);
						break;
					case withdrawn:
						color = context.getResources().getColor(
								R.color.status_withdrawn);
						break;
					case moderated:
						color = context.getResources().getColor(
								R.color.status_moderated);
						break;
					case reviewed:
						color = context.getResources()
								.getColor(R.color.status_reviewed);
						break;

					case challenged:
						color = context.getResources().getColor(
								R.color.status_challenged);
						break;
					default:
						color = context.getResources().getColor(R.color.status_created);
						break;
				}
			}

			if (vertices != null && vertices.size() > 0) {
				calculateGeometry(updateArea);
			}
		}
	}

	public BasePropertyBoundary(final Context context, final GoogleMap map,
			String claimId, boolean updateArea) {
		this.context = context;
		this.map = map;
		if (claimId != null) {
            this.claimId = claimId;
			loadClaim(updateArea);
		}
	}

	protected void calculateGeometry(boolean updateArea) {

		if (vertices == null || vertices.size() <= 0) {
			return;
		}
		if (vertices.size() <= 1) {
			center = vertices.get(0).getMapPosition();
			bounds = new LatLngBounds(center, center);
			return;
		}

		GeometryFactory gf = new GeometryFactory();

		// need at least 4 coordinates for a three vertices polygon
		Coordinate[] coords = new Coordinate[(vertices.size() + 1) > 4 ? (vertices.size() + 1) : 4];

		int i = 0;

		for (Vertex vertex : vertices) {
			coords[i++] = new Coordinate(vertex.getMapPosition().longitude, vertex.getMapPosition().latitude);
		}

		if (vertices.size() == 2) {
			// the source is a line segment so we replicate the second vertex to
			// create a three vertices polygon
			coords[i++] = new Coordinate(vertices.get(1).getMapPosition().longitude, vertices.get(1).getMapPosition().latitude);
		}

		// then we close the polygon
		coords[coords.length - 1] = new Coordinate(vertices.get(0).getMapPosition().longitude, vertices.get(0).getMapPosition().latitude);

		polygon = gf.createPolygon(coords);
		polygon.setSRID(Constants.SRID);
		validateGeometry();

		if ((claim != null)
				&& updateArea
				&& OpenTenureApplication.getClaimId() != null
				&& (OpenTenureApplication.getClaimId().equals(claim.getClaimId()))) {

			calculateArea();

			if (claim.getClaimArea() != area) {
				claim.updateArea((long) area);
				claim.setClaimArea((long) area);
				OpenTenureApplication.getDetailsFragment().reloadArea(claim);
			}
		}

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
			center = new LatLng(polygon.getInteriorPoint().getCoordinate().y, polygon.getInteriorPoint().getCoordinate().x);
		} catch (Exception e) {
			center = new LatLng(polygon.getCentroid().getCoordinate().y, polygon.getCentroid().getCoordinate().x);
		}
	}

	protected void calculateArea() {
		if (vertices == null || vertices.size() <= 0 || vertices.size() <= 1) {
			return;
		}

		area = (long) Math.round(Vertex.getArea(vertices) - HoleVertex.getArea(holesVertices));

		int digit = 0;

		if (area > 100 && area < 1000) {
			digit = (int) (Math.abs(area) % 10);
			if (digit > 0 && digit < 7)
				area = area - digit;
			else if (digit >= 7 && digit < 9)
				area = area + 10 - digit;
			else if (digit == 9)
				area = area + 1;
		}

		if (area > 1000 && area < 10000) {
			digit = (int) (Math.abs(area) % 100);
			if (digit > 0 && digit < 70)
				area = area - digit;
			else if (digit >= 70 && digit <= 99)
				area = area + 100 - digit;

		}

		if (area > 10000 && area < 100000) {
			digit = (int) (Math.abs(area) % 1000);

			if (digit > 0 && digit < 700)
				area = area - digit;
			else if (digit >= 700 && digit <= 999)
				area = area + 1000 - digit;
		}

		if (area > 100000) {
			digit = (int) (Math.abs(area) % 10000);

			if (digit > 0 && digit < 7000)
				area = area - digit;
			else if (digit >= 7000 && digit <= 9999)
				area = area + 10000 - digit;
		}
	}

	public static boolean isValidateGeometry(String claimId){
		Claim claim = Claim.getClaim(claimId);
		if(claim == null) {
			return true;
		}

		List<Vertex> vertices = claim.getVertices();
		List<List<HoleVertex>> holeVertices = claim.getHolesVertices();

		if(vertices == null || vertices.size() <= 0){
			return true;
		}
		return isValidateGeometry(vertices, holeVertices);
	}

	public static boolean isValidateGeometry(List<Vertex> vertices, List<List<HoleVertex>> holeVertices){
		GeometryFactory gf = new GeometryFactory();

		Geometry shell = Vertex.mapShell(vertices);
		LinearRing[] holes = HoleVertex.mapHoles(holeVertices);
		Geometry geometry;
		if(shell instanceof LinearRing){
			if(holes != null && holes.length > 0){
				boolean hasHoles = false;
				for(LinearRing hole:holes){
					try {
						gf.createLinearRing(hole.getCoordinates());
						if(hole.getNumPoints() > 0){
							hasHoles = true;
							break;
						}
					}catch (Exception e){
						continue;
					}
				}
				if(hasHoles){
					geometry = gf.createPolygon((LinearRing)shell, holes);
				}else{
					geometry = gf.createPolygon(shell.getCoordinates());
				}
			}else{
				geometry = gf.createPolygon(shell.getCoordinates());
			}
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

	public boolean validateGeometry(){
		return BasePropertyBoundary.isValidateGeometry(vertices, holesVertices);
	}

	private Marker createPropertyMarker(LatLng position, String title) {
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
			name = context.getResources()
					.getString(R.string.default_claim_name);
			tf.getTextBounds(name, 0, name.length(), boundsText);
		}

		Bitmap.Config conf = Bitmap.Config.ARGB_8888;
		Bitmap bmpText = Bitmap.createBitmap(boundsText.width(),
				boundsText.height() - boundsText.bottom, conf);

		Canvas canvasText = new Canvas(bmpText);
		canvasText.drawText(name, canvasText.getWidth() / 2,
				canvasText.getHeight(), tf);

		Marker marker = map.addMarker(new MarkerOptions().position(position)
				.title(title).icon(BitmapDescriptorFactory.fromBitmap(bmpText))
				.anchor(0.5f, 1));
		marker.setClusterGroup(Constants.PROPERTY_LABEL_MARKERS_GROUP);
		return marker;
	}

	void hideProperty() {
		if (displayPolygon != null) {
			displayPolygon.remove();
		}
		if (propertyMarker != null) {
			propertyMarker.remove();
		}
	}

	void
    redrawProperty() {
		hideProperty();
		showProperty();
	}

	void showProperty() {

		if (vertices.size() <= 0) {
			return;
		}

		PolygonOptions polygonOptions = new PolygonOptions();
		for (Vertex vertex:vertices) {
			polygonOptions.add(vertex.getMapPosition());
		}
		if(holesVertices != null && holesVertices.size() >0){
			for(List<HoleVertex> holeVertices:holesVertices){
				if(holeVertices != null && holeVertices.size()>0){
					List<LatLng> hole = new ArrayList<LatLng>();
					for(HoleVertex holeVertex:holeVertices){
						hole.add(holeVertex.getMapPosition());
					}
					polygonOptions.addHole(hole);
				}
			}
		}

		polygonOptions.zIndex(BOUNDARY_Z_INDEX);
		polygonOptions.strokeColor(color);
		polygonOptions.strokeWidth(4);
		polygonOptions.fillColor(color & 0x0fffffff); // 50% transparency
		displayPolygon = map.addPolygon(polygonOptions);

		ClaimType ct = new ClaimType();
		String areaString = null;

		DisplayNameLocalizer dnl = new DisplayNameLocalizer(OpenTenureApplication.getInstance().getLanguageCode());

		calculateArea();

		areaString = OpenTenureApplication.getContext().getString(
				R.string.claim_area_label)
				+ " "
				+ (long) area
				+ " "
				+ OpenTenureApplication.getContext().getString(
						R.string.square_meters);

		propertyMarker = createPropertyMarker(
				center,
				claim.getSlogan(context)
						+ ", "
						+ context.getString(R.string.type)
						+ ": "
						+ dnl.getLocalizedDisplayName(ct.getDisplayValueByCode(claim.getType())) + ", "
						+ areaString);

	}

	void showLocations() {

		if (claimId == null) {
			return;
		}

		for (PropertyLocation location : PropertyLocation
				.getPropertyLocations(claimId)) {
			Marker marker = createLocationMarker(location.getMapPosition(),
					location.getDescription());
			locationsMap.put(marker, location);
		}
		locationsVisible = true;
	}

	protected Marker createLocationMarker(LatLng position, String description) {
		Marker marker = map.addMarker(new MarkerOptions()
				.position(position)
				.title(description)
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.ot_blue_marker)));
		marker.setClusterGroup(ClusterGroup.NOT_CLUSTERED);
		return marker;

	}

	CardinalDirection getCardinalDirection(BasePropertyBoundary dest) {
		double deltaX = dest.getCenter().longitude - center.longitude;
		double deltaY = dest.getCenter().latitude - center.latitude;
		if (deltaX == 0) {
			return deltaY > 0 ? CardinalDirection.NORTH
					: CardinalDirection.SOUTH;
		}
		double slope = deltaY / deltaX;
		if (slope >= -1.0 / 3.0 && slope < 1.0 / 3.0) {
			return deltaX > 0 ? CardinalDirection.EAST : CardinalDirection.WEST;
		} else if (slope >= 1.0 / 3.0 && slope < 3.0) {
			return deltaY > 0 ? CardinalDirection.NORTHEAST
					: CardinalDirection.SOUTHWEST;
		} else if (slope >= 3.0 || slope <= -3.0) {
			return deltaY > 0 ? CardinalDirection.NORTH
					: CardinalDirection.SOUTH;
		} else {
			return deltaY > 0 ? CardinalDirection.NORTHWEST
					: CardinalDirection.SOUTHEAST;
		}
	}

}
