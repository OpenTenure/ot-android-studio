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

import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.Polyline;
import com.androidmapsextensions.PolylineOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

public class BasePropertyBoundary {

	protected static final float BOUNDARY_Z_INDEX = 2.0f;
	public static final double SNAP_THRESHOLD = 0.00005;

	protected String name;
	protected String claimId;
	protected String claimSlogan;
	protected String claimType;
	protected List<Vertex> vertices = new ArrayList<Vertex>();
	protected Map<Marker, PropertyLocation> propertyLocationsMap = new HashMap<Marker, PropertyLocation>();
	protected boolean propertyLocationsVisible = false;

	public boolean isPropertyLocationsVisible() {
		return propertyLocationsVisible;
	}

	public List<Vertex> getVertices() {
		return vertices;
	}

	protected Context context;
	protected Polyline polyline = null;
	protected Polygon polygon = null;
	protected GoogleMap map;
	protected LatLng center = null;
	protected double area = 0.0;
	protected Marker propertyMarker = null;
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
			Claim claim = Claim.getClaim(claimId);
			loadClaim(claim, false);
		}
	}

	protected void loadClaim(Claim claim, boolean updateArea) {
		vertices = claim.getVertices();
		name = claim.getName() == null || claim.getName().equalsIgnoreCase("") ? context
				.getResources().getString(R.string.default_claim_name) : claim
				.getName();

		String status = claim.getStatus();
		claimId = claim.getClaimId();
		claimSlogan = claim.getSlogan(context);
		claimType = claim.getType();

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
			calculateGeometry(claim, updateArea);
		}
	}

	public BasePropertyBoundary(final Context context, final GoogleMap map,
			final Claim claim, boolean updateArea) {
		this.context = context;
		this.map = map;
		if (claim != null) {
			loadClaim(claim, updateArea);
		}
	}

	protected void calculateGeometry(Claim claim, boolean updateArea) {

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
		Coordinate[] coords = new Coordinate[(vertices.size() + 1) > 4 ? (vertices
				.size() + 1) : 4];

		int i = 0;

		List<LatLng> coordList = new ArrayList<LatLng>();
		for (Vertex vertex : vertices) {
			coords[i++] = new Coordinate(vertex.getMapPosition().longitude,
					vertex.getMapPosition().latitude);
			coordList.add(vertex.getMapPosition());
		}

		if (vertices.size() == 2) {
			// the source is a line segment so we replicate the second vertex to
			// create a three vertices polygon
			coords[i++] = new Coordinate(
					vertices.get(1).getMapPosition().longitude, vertices.get(1)
							.getMapPosition().latitude);
			coordList.add(vertices.get(1).getMapPosition());
		}

		// then we close the polygon

		coords[coords.length - 1] = new Coordinate(vertices.get(0)
				.getMapPosition().longitude,
				vertices.get(0).getMapPosition().latitude);
		coordList.add(vertices.get(0).getMapPosition());

		polygon = gf.createPolygon(coords);
		polygon.setSRID(Constants.SRID);

		if ((claim != null)
				&& updateArea
				&& OpenTenureApplication.getClaimId() != null
				&& (OpenTenureApplication.getClaimId().equals(claim
						.getClaimId()))) {

			area = SphericalUtil.computeArea(coordList);
			area = (long) Math.round(area);

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
			center = new LatLng(polygon.getInteriorPoint().getCoordinate().y,
					polygon.getInteriorPoint().getCoordinate().x);
		} catch (Exception e) {
			center = new LatLng(polygon.getCentroid().getCoordinate().y,
					polygon.getCentroid().getCoordinate().x);
		}
	}

	protected Marker createPropertyMarker(LatLng position, String title) {
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
			if (name != null)
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

	public void hideBoundary() {
		if (polyline != null) {
			polyline.remove();
		}
		if (propertyMarker != null) {
			propertyMarker.remove();
		}
	}

	public void redrawBoundary() {
		hideBoundary();
		showBoundary();
	}

	public void showBoundary() {

		if (vertices.size() <= 0) {
			return;
		}

		PolylineOptions polylineOptions = new PolylineOptions();
		for (int i = 0; i < vertices.size(); i++) {
			polylineOptions.add(vertices.get(i).getMapPosition());
		}
		polylineOptions.add(vertices.get(0).getMapPosition()); // Needed in
																// order to
																// close the
																// polyline
		polylineOptions.zIndex(BOUNDARY_Z_INDEX);
		polylineOptions.width(4);
		polylineOptions.color(color);
		polyline = map.addPolyline(polylineOptions);
		ClaimType ct = new ClaimType();
		String areaString = null;

		DisplayNameLocalizer dnl = new DisplayNameLocalizer(
				OpenTenureApplication.getInstance().getLocalization());

		areaString = OpenTenureApplication.getContext().getString(
				R.string.claim_area_label)
				+ " "
				+ (long) area
				+ " "
				+ OpenTenureApplication.getContext().getString(
						R.string.square_meters);

		// if (area < 10000) {
		// areaString = String.format(Locale.US, ", Area: %.2f m2", area);
		// } else if (area >= 10000 && area < 1000000) {
		// areaString = String.format(Locale.US, ", Area: %.2f ha",
		// area / 10000);
		// } else {
		// areaString = String.format(Locale.US, ", Area: %.2f km2",
		// area / 1000000);
		// }
		propertyMarker = createPropertyMarker(
				center,
				claimSlogan
						+ ", "
						+ context.getString(R.string.type)
						+ ": "
						+ dnl.getLocalizedDisplayName(ct
								.getDisplayValueByType(claimType)) + ", "
						+ areaString);

	}

	public void showPropertyLocations() {

		if (claimId == null) {
			return;
		}

		for (PropertyLocation location : PropertyLocation
				.getPropertyLocations(claimId)) {
			Marker marker = createLocationMarker(location.getMapPosition(),
					location.getDescription());
			propertyLocationsMap.put(marker, location);
		}
		propertyLocationsVisible = true;
	}

	protected Marker createLocationMarker(LatLng position, String description) {
		Marker marker = map.addMarker(new MarkerOptions()
				.position(position)
				.title(description)
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.ot_blue_marker)));
		marker.setClusterGroup(Constants.BASE_PROPERTY_LOCATION_MARKERS_GROUP
				+ propertyLocationsMap.size());
		return marker;

	}

	public void hidePropertyLocations() {

		if (propertyLocationsMap != null) {
			for (Marker marker : propertyLocationsMap.keySet()) {
				// Just hiding the marker, no need to delete the location from
				// DB
				marker.remove();
			}
			propertyLocationsMap = new HashMap<Marker, PropertyLocation>();
		}
		propertyLocationsVisible = false;
	}

	public CardinalDirection getCardinalDirection(BasePropertyBoundary dest) {
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
