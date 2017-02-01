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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fao.sola.clients.android.opentenure.ClaimDispatcher;
import org.fao.sola.clients.android.opentenure.ModeDispatcher;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.maps.ClaimMapFragment.MapMode;
import org.fao.sola.clients.android.opentenure.maps.markers.ActiveMarkerRegistrar;
import org.fao.sola.clients.android.opentenure.maps.markers.DownMarker;
import org.fao.sola.clients.android.opentenure.maps.markers.LeftMarker;
import org.fao.sola.clients.android.opentenure.maps.markers.RightMarker;
import org.fao.sola.clients.android.opentenure.maps.markers.UpMarker;
import org.fao.sola.clients.android.opentenure.model.Adjacency;
import org.fao.sola.clients.android.opentenure.model.Attachment;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.MD5;
import org.fao.sola.clients.android.opentenure.model.PropertyLocation;
import org.fao.sola.clients.android.opentenure.model.Vertex;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.GoogleMap.SnapshotReadyCallback;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.vividsolutions.jts.algorithm.distance.DistanceToPoint;
import com.vividsolutions.jts.algorithm.distance.PointPairDistance;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineSegment;

public class EditablePropertyBoundary extends BasePropertyBoundary {

	public static final String DEFAULT_MAP_FILE_NAME = "_map_.jpg";
	public static final String DEFAULT_GEOM_FILE_NAME = "_geom_.csv";
	public static final String DEFAULT_MAP_FILE_TYPE = "cadastralMap";
	public static final String DEFAULT_MAP_MIME_TYPE = "image/jpeg";
	private Map<Marker, Vertex> verticesMap;
	private List<BasePropertyBoundary> otherProperties;
	private ClaimDispatcher claimActivity;
	private ActiveMarkerRegistrar amr;
	private ModeDispatcher.Mode mode;

	private UpMarker up;
	private DownMarker down;
	private LeftMarker left;
	private RightMarker right;
	private Marker remove;
	private Marker moveTo;
	private Marker relativeEdit;
	private Marker cancel;
	private Marker target;
	private Marker add;
	private Marker selectedMarker;

	public boolean handleMarkerClick(final Marker mark, MapMode mapMode){
		if(handleMarkerEditClick(mark, mapMode)){
			return true;
		}else if(handleRelativeMarkerEditClick(mark, mapMode)){
			return true;
		}else if(handlePropertyBoundaryMarkerClick(mark, mapMode)){
			return true;
		}else if(handlePropertyLocationMarkerClick(mark, mapMode)){
			return true;
		}else{
			return handleClick(mark, mapMode);
		}
	}
	
	private boolean handleMarkerEditClick(Marker mark, MapMode mapMode){
		if(mode.compareTo(ModeDispatcher.Mode.MODE_RO) == 0 || remove == null || relativeEdit == null || cancel == null){
			return false;
		}
		try {
			if (mark.equals(remove)) {
				return removeSelectedMarker();
			}
			if (mark.equals(relativeEdit)) {
				showRelativeMarkerEditControls();
				return true;
			}
			if (mark.equals(cancel)) {
				deselect();
				return true;
			}
		} catch (UnsupportedOperationException e) {
			// Clustered markers have no ID and may throw this
		}
		return false;
	}

	private boolean handleRelativeMarkerEditClick(Marker mark, MapMode mapMode){
		if(mode.compareTo(ModeDispatcher.Mode.MODE_RO) == 0 || up == null || down == null || left == null || right == null || add == null || moveTo == null || cancel == null || target == null){
			return false;
		}
		
		try {
			if (amr.onClick(mark)) {
				return true;
			}else if (mark.equals(add)) {
				Log.d(this.getClass().getName(),"add");
				return addMarker(mapMode);
			}else if (mark.equals(moveTo)) {
				Log.d(this.getClass().getName(),"moveTo");
				return moveMarker();
			}else if (mark.equals(cancel)) {
				Log.d(this.getClass().getName(),"cancel");
				deselect();
				return true;
			}else if (mark.equals(target)) {
				Log.d(this.getClass().getName(),"target");
				return true;
			}else{
				return false;
			}
		} catch (UnsupportedOperationException e) {
			// Clustered markers have no ID and may throw this
		}
		return false;
	}
	
	private boolean handlePropertyBoundaryMarkerClick(final Marker mark, MapMode mapMode){
		if (verticesMap.containsKey(mark)) {
			if(mode.compareTo(ModeDispatcher.Mode.MODE_RW) == 0){
				deselect();
				selectedMarker = mark;
				selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker());
				selectedMarker.showInfoWindow();
				showMarkerEditControls();
				return true;
			}else{
				mark.showInfoWindow();
				return true;
			}
		}
		return false;
		
	}

	private boolean handlePropertyLocationMarkerClick(final Marker mark, MapMode mapMode){
		if (propertyLocationsMap.containsKey(mark)) {
			if(mode.compareTo(ModeDispatcher.Mode.MODE_RW) == 0){
				deselect();
				selectedMarker = mark;
				selectedMarker.setIcon(BitmapDescriptorFactory.defaultMarker());
				selectedMarker.showInfoWindow();
				showMarkerEditControls();
				return true;
			}else{
				mark.showInfoWindow();
				return true;
			}
		}
		return false;
		
	}

	private boolean handleClick(Marker mark, MapMode mapMode){
		try{
			// Can only be a click on the property name, deselect and let the event flow

			if(propertyMarker != null && mark.equals(propertyMarker)){

				if(mode.compareTo(ModeDispatcher.Mode.MODE_RW) == 0){
					deselect();
				}

				if(!mark.isInfoWindowShown()){
					mark.showInfoWindow();
					return true;
				}
			}
		} catch (UnsupportedOperationException e) {
			// Clustered markers have no ID and may throw this
		}
		// Let the flow continue in order to center the map around selected marker and display info window
		return false;
	}

	public void onMarkerDragStart(Marker mark) {
		if(verticesMap.containsKey(mark)){
			onPropertyBoundaryMarkerDragStart(mark);
		}else if(propertyLocationsMap.containsKey(mark)){
			onPropertyLocationMarkerDragStart(mark);
		}
	}

	public void onMarkerDragEnd(Marker mark) {
		if(verticesMap.containsKey(mark)){
			onPropertyBoundaryMarkerDragEnd(mark);
		}else if(propertyLocationsMap.containsKey(mark)){
			onPropertyLocationMarkerDragEnd(mark);
		}
	}

	public void onMarkerDrag(Marker mark) {
		if(verticesMap.containsKey(mark)){
			onPropertyBoundaryMarkerDrag(mark);
		}else if(propertyLocationsMap.containsKey(mark)){
			onPropertyLocationMarkerDrag(mark);
		}
	}

	private boolean removeSelectedMarker(){

		if (verticesMap.containsKey(selectedMarker)) {
			return removeSelectedPropertyBoundaryVertex();
		}else if (propertyLocationsMap.containsKey(selectedMarker)) {
			return removeSelectedPropertyLocation();
		}
		return false;
		
	}
	
	private boolean removeSelectedPropertyLocation(){
		PropertyLocation loc = propertyLocationsMap.remove(selectedMarker);
		removePropertyLocationMarker(selectedMarker);
		loc.delete();
		hideMarkerEditControls();
		selectedMarker = null;
		return true;
	}
	
	private boolean removeSelectedPropertyBoundaryVertex(){
		removePropertyBoundaryMarker(selectedMarker);
		redrawBoundary();
		updateVertices();
		resetAdjacency(otherProperties);
		hideMarkerEditControls();
		calculateGeometry(Claim.getClaim(claimId), true);
		redrawBoundary();
		selectedMarker = null;
		return true;
	}
	
	private boolean addMarker(MapMode mapMode){
		
		addMarker(target.getPosition(), mapMode);
		deselect();
		return true;
	}

	private boolean movePropertyLocationMarker(){
		Marker newMark = createLocationMarker(target.getPosition(), selectedMarker.getTitle());
		PropertyLocation loc = propertyLocationsMap.remove(selectedMarker);
		loc.setMapPosition(target.getPosition());
		loc.update();
		hideMarkerEditControls();
		selectedMarker.remove();
		selectedMarker = null;
		propertyLocationsMap.put(newMark, loc);
		return true;
	}
	
	private boolean movePropertyBoundaryMarker(){
		insertVertex(target.getPosition(), verticesMap.get(selectedMarker).getGPSPosition());
		removePropertyBoundaryMarker(selectedMarker);
		hideMarkerEditControls();
		selectedMarker = null;
		redrawBoundary();
		updateVertices();
		resetAdjacency(otherProperties);
		calculateGeometry(Claim.getClaim(claimId), true);
		return true;
	}
	
	private boolean moveMarker(){

		// Insert a marker at target position and remove the selected

		if (verticesMap.containsKey(selectedMarker)) {
			return movePropertyBoundaryMarker();
		}else if (propertyLocationsMap.containsKey(selectedMarker)) {
			return movePropertyLocationMarker();
		}
		return false;
		
	}

	public void deselect(){
		hideMarkerEditControls();
		if(selectedMarker != null){
			if(verticesMap.containsKey(selectedMarker)){
				selectedMarker.setIcon(BitmapDescriptorFactory
						.fromResource(R.drawable.ot_blue_marker));
			}else{
				selectedMarker.setIcon(BitmapDescriptorFactory
						.fromResource(R.drawable.ot_orange_marker));
			}
		selectedMarker = null;
		}
	}
	
	private void hideMarkerEditControls(){
		if(up!=null){
			up.hide();
			amr.remove(up);
		}
		if(down != null){
			down.hide();
			amr.remove(down);
		}
		if(left != null){
			left.hide();
			amr.remove(left);
		}
		if(right != null){
			right.hide();
			amr.remove(right);
		}
		if(target != null){
			target.remove();
			target = null;
		}
		if(relativeEdit != null){
			relativeEdit.remove();
			relativeEdit = null;
		}
		if(remove != null){
			remove.remove();
			remove = null;
		}
		if(add != null){
			add.remove();
			add = null;
		}
		if(moveTo != null){
			moveTo.remove();
			moveTo = null;
		}
		if(cancel != null){
			cancel.remove();
			cancel = null;
		}

	}

	private Point getControlRelativeEditPosition(Point markerScreenPosition, int markerWidth, int markerHeight){
		return new Point(markerScreenPosition.x, markerScreenPosition.y + 2*markerHeight);
	}

	private Point getControlRemovePosition(Point markerScreenPosition, int markerWidth, int markerHeight){
		return new Point(markerScreenPosition.x - 2*markerWidth, markerScreenPosition.y + 2*markerHeight);
	}

	private Point getControlAddPosition(Point markerScreenPosition, int markerWidth, int markerHeight){
		return new Point(markerScreenPosition.x - 2*markerWidth, markerScreenPosition.y + 2*markerHeight);
	}

	private Point getControlMoveToPosition(Point markerScreenPosition, int markerWidth, int markerHeight){
		return new Point(markerScreenPosition.x, markerScreenPosition.y + 2*markerHeight);
	}

	private Point getControlCancelPosition(Point markerScreenPosition, int markerWidth, int markerHeight){
		return new Point(markerScreenPosition.x + 2*markerWidth, markerScreenPosition.y + 2*markerHeight);
	}

	private Point getControlTargetPosition(Point markerScreenPosition, int markerWidth, int markerHeight){
		return new Point(markerScreenPosition.x, markerScreenPosition.y);
	}
	
	public void refreshMarkerEditControls(){
		
		if(selectedMarker == null){
			return;
		}

		// Reposition visible edit controls (excluding target)
		
		Projection projection = map.getProjection();
		Point screenPosition = projection.toScreenLocation(selectedMarker.getPosition());

		Bitmap bmp = BitmapFactory
				.decodeResource(context.getResources(), R.drawable.ot_blue_marker);
		int iconHeight = bmp.getHeight();
		int iconWidth = bmp.getWidth();

		if(up!=null){
			up.refresh(screenPosition, iconWidth, iconHeight);
		}
		if(down != null){
			down.refresh(screenPosition, iconWidth, iconHeight);
		}
		if(left != null){
			left.refresh(screenPosition, iconWidth, iconHeight);
		}
		if(right != null){
			right.refresh(screenPosition, iconWidth, iconHeight);
		}
		if(relativeEdit != null){
			relativeEdit.setPosition(projection.fromScreenLocation(getControlRelativeEditPosition(screenPosition, iconWidth, iconHeight)));
		}
		if(remove != null){
			remove.setPosition(projection.fromScreenLocation(getControlRemovePosition(screenPosition, iconWidth, iconHeight)));
		}
		if(add != null){
			add.setPosition(projection.fromScreenLocation(getControlAddPosition(screenPosition, iconWidth, iconHeight)));
		}
		if(moveTo != null){
			moveTo.setPosition(projection.fromScreenLocation(getControlMoveToPosition(screenPosition, iconWidth, iconHeight)));
		}
		if(cancel != null){
			cancel.setPosition(projection.fromScreenLocation(getControlCancelPosition(screenPosition, iconWidth, iconHeight)));
		}

	}

	private void showMarkerEditControls() {
		
		hideMarkerEditControls();
		
		Projection projection = map.getProjection();
		Point markerScreenPosition = projection.toScreenLocation(selectedMarker.getPosition());

		Bitmap bmp = BitmapFactory
				.decodeResource(context.getResources(), R.drawable.ot_blue_marker);
		int markerHeight = bmp.getHeight();
		int markerWidth = bmp.getWidth();

		remove = map.addMarker(new MarkerOptions()
		.position(projection.fromScreenLocation(getControlRemovePosition(markerScreenPosition, markerWidth, markerHeight)))
		.anchor(0.5f, 0.5f)
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_menu_close_clear_cancel)));
		remove.setClusterGroup(Constants.MARKER_EDIT_REMOVE_GROUP);
		relativeEdit = map.addMarker(new MarkerOptions()
		.position(projection.fromScreenLocation(getControlRelativeEditPosition(markerScreenPosition, markerWidth, markerHeight)))
		.anchor(0.5f, 0.5f)
		.title("0.0 m")
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_action_move)));
		relativeEdit.setClusterGroup(Constants.MARKER_EDIT_RELATIVE_EDIT_GROUP);
		cancel = map.addMarker(new MarkerOptions()
		.position(projection.fromScreenLocation(getControlCancelPosition(markerScreenPosition, markerWidth, markerHeight)))
		.anchor(0.5f, 0.5f)
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_menu_block)));
		cancel.setClusterGroup(Constants.MARKER_EDIT_CANCEL_GROUP);
	}

	private void showRelativeMarkerEditControls() {
		
		Projection projection = map.getProjection();
		Point markerScreenPosition = projection.toScreenLocation(selectedMarker.getPosition());

		Bitmap bmp = BitmapFactory
				.decodeResource(context.getResources(), R.drawable.ot_blue_marker);
		int markerHeight = bmp.getHeight();
		int markerWidth = bmp.getWidth();

		hideMarkerEditControls();
		
		target = map.addMarker(new MarkerOptions()
		.position(projection.fromScreenLocation(getControlTargetPosition(markerScreenPosition, markerWidth, markerHeight)))
		.anchor(0.5f, 0.5f)
		.title("0.0 m")
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_menu_mylocation)));
		target.setClusterGroup(Constants.TARGET_MARKERS_GROUP);

		add = map.addMarker(new MarkerOptions()
		.position(projection.fromScreenLocation(getControlAddPosition(markerScreenPosition, markerWidth, markerHeight)))
		.anchor(0.5f, 0.5f)
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_menu_add)));
		add.setClusterGroup(Constants.MARKER_RELATIVE_EDIT_ADD_GROUP);
		moveTo = map.addMarker(new MarkerOptions()
		.position(projection.fromScreenLocation(getControlMoveToPosition(markerScreenPosition, markerWidth, markerHeight)))
		.anchor(0.5f, 0.5f)
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_menu_goto)));
		moveTo.setClusterGroup(Constants.MARKER_RELATIVE_EDIT_MOVE_TO_GROUP);
		cancel = map.addMarker(new MarkerOptions()
		.position(projection.fromScreenLocation(getControlCancelPosition(markerScreenPosition, markerWidth, markerHeight)))
		.anchor(0.5f, 0.5f)
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_menu_block)));
		cancel.setClusterGroup(Constants.MARKER_RELATIVE_EDIT_CANCEL_GROUP);

		up = new UpMarker(context,selectedMarker,target, map);
		up.show(projection, markerScreenPosition, markerWidth, markerHeight);

		amr.add(up);
		
		down = new DownMarker(context,selectedMarker,target, map);
		down.show(projection, markerScreenPosition, markerWidth, markerHeight);
		amr.add(down);

		left = new LeftMarker(context,selectedMarker,target, map);
		left.show(projection, markerScreenPosition, markerWidth, markerHeight);
		amr.add(left);

		right = new RightMarker(context,selectedMarker,target, map);
		right.show(projection, markerScreenPosition, markerWidth, markerHeight);
		amr.add(right);

	}

	public EditablePropertyBoundary(final Context context, final GoogleMap map, final Claim claim,
			final ClaimDispatcher claimActivity, final List<BasePropertyBoundary> existingProperties, ModeDispatcher.Mode mode) {
		super(context, map, claim, true);
		this.claimActivity = claimActivity;
		this.mode = mode;
		this.selectedMarker = null;
		this.otherProperties = existingProperties;
		this.amr = new ActiveMarkerRegistrar();
		verticesMap = new HashMap<Marker, Vertex>();
		if (vertices != null && vertices.size() > 0) {
			for (Vertex vertex : vertices) {
				Marker mark = createMarker(vertex.getSequenceNumber(), vertex.getMapPosition());
				verticesMap.put(mark, vertex);
			}
		}
		showPropertyLocations();
	}
	
	public void setOtherProperties(List<BasePropertyBoundary> otherProperties) {
		this.otherProperties = otherProperties;
	}

	private List<BasePropertyBoundary> findAdjacentProperties(
			List<BasePropertyBoundary> properties) {
		List<BasePropertyBoundary> adjacentProperties = null;
		for (BasePropertyBoundary property : properties) {
			if (polygon != null && property.getPolygon() != null
					&& polygon.distance(property.getPolygon()) < SNAP_THRESHOLD) {
				if (adjacentProperties == null) {
					adjacentProperties = new ArrayList<BasePropertyBoundary>();
				}
				adjacentProperties.add(property);
			}
		}
		return adjacentProperties;
	}

	protected void reload(){
		Log.d(this.getClass().getName(), "Reloading claim on fragment change");

		for(Marker mark:verticesMap.keySet()){
			mark.remove();
		}
		claimId = claimActivity.getClaimId();
		super.reload();
		verticesMap = new HashMap<Marker, Vertex>();
		if (vertices != null && vertices.size() > 0) {
			for (Vertex vertex : vertices) {
				Marker mark = createMarker(vertex.getSequenceNumber(), vertex.getMapPosition());
				verticesMap.put(mark, vertex);
			}
		}
	}
	
	public void updateVertices() {

		Vertex.deleteVertices(claimActivity.getClaimId());

		for (int i = 0; i < vertices.size(); i++) {
			Vertex vertex = vertices.get(i);
			vertex.setSequenceNumber(i);
			Vertex.createVertex(vertex);
		}
		calculateGeometry(Claim.getClaim(claimId), true);
		redrawBoundary();

	}

	public void updatePropertyLocations() {

		PropertyLocation.deletePropertyLocations(claimActivity.getClaimId());

		for (PropertyLocation location : propertyLocationsMap.values()) {
			location.create();
		}

	}
	protected void resetAdjacency(List<BasePropertyBoundary> existingProperties) {

		List<BasePropertyBoundary> adjacentProperties = findAdjacentProperties(existingProperties);
		Adjacency.deleteAdjacencies(claimId);

		if (adjacentProperties != null) {

			for (BasePropertyBoundary adjacentProperty : adjacentProperties) {

				Adjacency adj = new Adjacency();
				adj.setSourceClaimId(claimId);
				adj.setDestClaimId(adjacentProperty.getClaimId());
				adj.setCardinalDirection(getCardinalDirection(adjacentProperty));
				adj.create();
			}
		}
	}

	private void onPropertyLocationMarkerDragStart(Marker mark) {
		dragPropertyLocationMarker(mark);
	}

	private void onPropertyLocationMarkerDragEnd(Marker mark) {
		dragPropertyLocationMarker(mark);
		updatePropertyLocations();
	}

	private void onPropertyLocationMarkerDrag(Marker mark) {
		dragPropertyLocationMarker(mark);
	}

	private void onPropertyBoundaryMarkerDragStart(Marker mark) {
		dragPropertyBoundaryMarker(mark);
	}

	private void onPropertyBoundaryMarkerDragEnd(Marker mark) {
		dragPropertyBoundaryMarker(mark);
		updateVertices();
		resetAdjacency(otherProperties);
	}

	private void onPropertyBoundaryMarkerDrag(Marker mark) {
		dragPropertyBoundaryMarker(mark);
	}

	private void dragPropertyBoundaryMarker(Marker mark) {
		verticesMap.get(mark).setMapPosition(mark.getPosition());
		redrawBoundary();
	}

	private void dragPropertyLocationMarker(Marker mark) {
		propertyLocationsMap.get(mark).setMapPosition(mark.getPosition());
	}

	private void removePropertyLocationMarker(Marker mark) {
		propertyLocationsMap.remove(mark);
		mark.remove();
	}

	private void removePropertyBoundaryMarker(Marker mark) {
		vertices.remove(verticesMap.remove(mark));
		mark.remove();
	}

	public void insertVertex(LatLng mapPosition, LatLng gpsPosition) {

		if (claimActivity.getClaimId() == null) {
			// Useless to add markers without a claim
			Toast toast = Toast.makeText(context,
					R.string.message_save_claim_before_adding_content,
					Toast.LENGTH_SHORT);
			toast.show();
			return;
		}

		Marker mark = createMarker(vertices.size(), mapPosition);
		Vertex newVertex = new Vertex(mapPosition, gpsPosition);
		newVertex.setClaimId(claimActivity.getClaimId());
		verticesMap.put(mark, newVertex);

		if (vertices.size() < 2) {
			// no need to calculate the insertion point
			vertices.add(newVertex);
			return;
		}

		double minDistance = Double.MAX_VALUE;
		int insertIndex = 0;

		// calculate the insertion point
		for (int i = 0; i < vertices.size(); i++) {

			Vertex from = vertices.get(i);
			Vertex to = null;

			if (i == vertices.size() - 1) {
				to = vertices.get(0);
			} else {
				to = vertices.get(i + 1);
			}

			PointPairDistance ppd = new PointPairDistance();
			DistanceToPoint.computeDistance(
					new LineSegment(from.getMapPosition().longitude, from
							.getMapPosition().latitude,
							to.getMapPosition().longitude,
							to.getMapPosition().latitude),
					new Coordinate(newVertex.getMapPosition().longitude,
							newVertex.getMapPosition().latitude), ppd);

			double currDistance = ppd.getDistance();

			if (currDistance < minDistance) {
				minDistance = currDistance;
				insertIndex = i + 1;
			}

		}
		vertices.add(insertIndex, newVertex);
	}

	public void addMarker(final LatLng mapPosition, MapMode mapMode){
		addMarker(mapPosition, Vertex.INVALID_POSITION, mapMode);
	}
	
	public void addMarker(final LatLng mapPosition, final LatLng gpsPosition, MapMode mapMode){

		if (claimActivity.getClaimId() == null) {
			// Useless to add markers without a claim
			Toast toast = Toast.makeText(
					context,
					R.string.message_save_claim_before_adding_content,
					Toast.LENGTH_SHORT);
			toast.show();
			return;
		}
		
		if(mapMode == MapMode.add_boundary){

			AlertDialog.Builder dialog = new AlertDialog.Builder(
					context);
			dialog.setTitle(R.string.message_add_marker);
			dialog.setMessage("Lon: " + mapPosition.longitude + ", lat: "
					+ mapPosition.latitude);

			dialog.setPositiveButton(R.string.confirm,
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {

							insertVertex(mapPosition, gpsPosition);
							updateVertices();
							redrawBoundary();
							resetAdjacency(otherProperties);
							calculateGeometry(Claim.getClaim(claimId), true);
							redrawBoundary();
						}
					});
			dialog.setNegativeButton(R.string.cancel,
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
						}
					});

			dialog.show();
			
		}else if(mapMode == MapMode.add_non_boundary){
			
			AlertDialog.Builder dialog = new AlertDialog.Builder(
					context);
			dialog.setTitle(R.string.message_add_non_boundary_marker);
			dialog.setMessage("Lon: " + mapPosition.longitude + ", lat: "
					+ mapPosition.latitude);
			dialog.setPositiveButton(R.string.confirm,
					new OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog,
						int which) {
					AlertDialog.Builder locationDescriptionDialog = new AlertDialog.Builder(
							context);
					locationDescriptionDialog
							.setTitle(R.string.title_add_non_boundary);
					final EditText locationDescriptionInput = new EditText(
							context);
					locationDescriptionInput
							.setInputType(InputType.TYPE_CLASS_TEXT);
					locationDescriptionDialog
							.setView(locationDescriptionInput);
					locationDescriptionDialog
							.setMessage(context.getResources()
									.getString(
											R.string.message_enter_description));

					locationDescriptionDialog
							.setPositiveButton(
									R.string.confirm,
									new OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											String locationDescription = locationDescriptionInput
													.getText()
													.toString();
											addPropertyLocation(mapPosition, gpsPosition, locationDescription);
										}
									});
					locationDescriptionDialog
							.setNegativeButton(
									R.string.cancel,
									new OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
										}
									});

					locationDescriptionDialog.show();

				}
			});
			dialog.setNegativeButton(R.string.cancel,
					new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
						}
					});

			dialog.show();
		}

	
	}

	public void addPropertyLocation(LatLng mapPosition, LatLng gpsPosition, String description) {

		if (claimActivity.getClaimId() == null) {
			// Useless to add markers without a claim
			Toast toast = Toast.makeText(context,
					R.string.message_save_claim_before_adding_content,
					Toast.LENGTH_SHORT);
			toast.show();
			return;
		}

		Marker mark = createLocationMarker(mapPosition, description);
		PropertyLocation loc = new PropertyLocation(mapPosition, gpsPosition);
		loc.setClaimId(claimActivity.getClaimId());
		loc.setDescription(description);
		loc.create();
		propertyLocationsMap.put(mark, loc);
	}

	private Marker createMarker(int index, LatLng position) {
		Marker marker;
		if(mode.compareTo(ModeDispatcher.Mode.MODE_RW) == 0){
			marker = map.addMarker(new MarkerOptions()
			.position(position)
			.title(index + ", Lat: " + position.latitude + ", Lon: " + position.longitude)
			.draggable(true)
			.icon(BitmapDescriptorFactory
					.fromResource(R.drawable.ot_blue_marker)));
		}else{
			marker = map.addMarker(new MarkerOptions()
			.position(position)
			.title(index + ", Lat: " + position.latitude + ", Lon: " + position.longitude)
			.icon(BitmapDescriptorFactory
					.fromResource(R.drawable.ot_blue_marker)));
		}
		// To prevent vertices to cluster when they are too close
		// assign each vertex to its own group
		marker.setClusterGroup(Constants.BASE_PROPERTY_BOUNDARY_MARKERS_GROUP + verticesMap.size());
		return marker;
	}

	@Override
	protected Marker createLocationMarker(LatLng position, String description) {
		Marker marker;
		if(mode.compareTo(ModeDispatcher.Mode.MODE_RW) == 0){
			marker = map.addMarker(new MarkerOptions()
			.position(position)
			.title(description)
			.draggable(true)
			.icon(BitmapDescriptorFactory
					.fromResource(R.drawable.ot_orange_marker)));
		}else{
			marker = map.addMarker(new MarkerOptions()
			.position(position)
			.title(description)
			.icon(BitmapDescriptorFactory
					.fromResource(R.drawable.ot_orange_marker)));
		}
		marker.setClusterGroup(Constants.BASE_PROPERTY_LOCATION_MARKERS_GROUP + propertyLocationsMap.size());
		return marker;
	}
	
	public void saveGeometry(){
		String claimId = claimActivity.getClaimId();

		if (claimId != null) {
			Claim claim = Claim.getClaim(claimId);
			BufferedWriter bw = null;
			String path = FileSystemUtilities.getExportFolder().getPath()
					+ File.separator
					+ claim.getName()
					+ DEFAULT_GEOM_FILE_NAME;

			try {
				bw = new BufferedWriter(new FileWriter(path));
				
				bw.write(
						"SEQUENCE_NUMBER" + FileSystemUtilities._CSV_FIELD_SEPARATOR
						+ "GPS_LAT" + FileSystemUtilities._CSV_FIELD_SEPARATOR
						+ "GPS_LON" + FileSystemUtilities._CSV_FIELD_SEPARATOR
						+ "MAP_LAT" + FileSystemUtilities._CSV_FIELD_SEPARATOR
						+ "MAP_LON" + FileSystemUtilities._CSV_REC_TERMINATOR
						);
				bw.flush();
				
				for(Vertex vertex:claim.getVertices()){
					bw.write(vertex.getSequenceNumber() + FileSystemUtilities._CSV_FIELD_SEPARATOR);
							LatLng gpsPosition = vertex.getGPSPosition();
							if(gpsPosition != null){
								bw.write(gpsPosition.latitude + FileSystemUtilities._CSV_FIELD_SEPARATOR
								+ gpsPosition.longitude + FileSystemUtilities._CSV_FIELD_SEPARATOR);
							}	
							else{
								bw.write("null" + FileSystemUtilities._CSV_FIELD_SEPARATOR
								+ "null" + FileSystemUtilities._CSV_FIELD_SEPARATOR);
							}
							LatLng mapPosition = vertex.getMapPosition();
							if(mapPosition != null){
								bw.write(mapPosition.latitude + FileSystemUtilities._CSV_FIELD_SEPARATOR
								+ mapPosition.longitude + FileSystemUtilities._CSV_REC_TERMINATOR);
							}	
							else{
								bw.write("null" + FileSystemUtilities._CSV_FIELD_SEPARATOR
								+ "null" + FileSystemUtilities._CSV_REC_TERMINATOR);
							}
							bw.flush();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (bw != null) {
					try {
						bw.close();
					} catch (Throwable ignore) {
					}
				}
			}
		}

	}

	public void saveSnapshot() {

		if (claimActivity.getClaimId() != null) {
			map.snapshot(new SnapshotReadyCallback() {

				@Override
				public void onSnapshotReady(Bitmap bmp) {
					FileOutputStream out = null;
					String claimId = claimActivity.getClaimId();
					String path = FileSystemUtilities
							.getAttachmentFolder(claimId)
							+ File.separator
							+ DEFAULT_MAP_FILE_NAME;
					try {
						out = new FileOutputStream(path);
						bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
						Claim claim = Claim.getClaim(claimId);
						for (Attachment att : claim.getAttachments()) {
							if (att.getFileName().equals(DEFAULT_MAP_FILE_NAME)) {
								att.delete();
							}
						}
						Attachment att = new Attachment();
						att.setClaimId(claimId);
						att.setDescription(context.getResources().getString(
								R.string.action_map));
						att.setFileName(DEFAULT_MAP_FILE_NAME);
						att.setFileType(DEFAULT_MAP_FILE_TYPE);						
						att.setMimeType(DEFAULT_MAP_MIME_TYPE);
						att.setMD5Sum(MD5.calculateMD5(new File(path)));
						att.setPath(path);
						att.setSize(new File(path).length());
						att.create();
						Toast toast = Toast.makeText(context,
								R.string.message_map_snapshot_saved,
								Toast.LENGTH_SHORT);
						toast.show();
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (out != null) {
							try {
								out.close();
							} catch (Throwable ignore) {
							}
						}
					}
				}
			});
		}else{
			Toast toast = Toast.makeText(context,
					R.string.message_save_claim_before_adding_content,
					Toast.LENGTH_SHORT);
			toast.show();
		}
	}
}
