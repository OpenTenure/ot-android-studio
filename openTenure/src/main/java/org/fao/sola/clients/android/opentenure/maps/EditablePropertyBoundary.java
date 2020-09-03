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

import org.fao.sola.clients.android.opentenure.BuildConfig;
import org.fao.sola.clients.android.opentenure.ClaimDispatcher;
import org.fao.sola.clients.android.opentenure.ModeDispatcher;
import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
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
import org.fao.sola.clients.android.opentenure.model.HoleVertex;
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
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.androidmapsextensions.ClusterGroup;
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
	private static final String DEFAULT_GEOM_FILE_NAME = "_geom_.csv";
	public static final String DEFAULT_MAP_FILE_TYPE = "cadastralMap";
	public static final String DEFAULT_MAP_MIME_TYPE = "image/jpeg";
	private Map<Marker, Vertex> verticesMap;
	private Map<Marker, HoleVertex> holesVerticesMap;
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
	private Vertex draggedVertex;
	private HoleVertex draggedHoleVertex;

    public int getSelectedHoleNumber() {
        return selectedHoleNumber;
    }

    public void setSelectedHoleNumber(int selectedHoleNumber) {
        this.selectedHoleNumber = selectedHoleNumber;
    }

    private int selectedHoleNumber;

	boolean handleMarkerClick(final Marker mark, MapMode mapMode){
		if(handleMarkerEditClick(mark, mapMode)){
			return true;
		}else if(handleRelativeMarkerEditClick(mark, mapMode)){
			return true;
        }else if(handleBoundaryMarkerClick(mark, mapMode)){
            return true;
        }else if(handleHoleMarkerClick(mark, mapMode)){
            return true;
		}else if(handleLocationMarkerClick(mark, mapMode)){
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

	private boolean handleBoundaryMarkerClick(final Marker mark, MapMode mapMode){
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

	private boolean handleHoleMarkerClick(final Marker mark, MapMode mapMode){
		if (holesVerticesMap.containsKey(mark)) {
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

	private boolean handleLocationMarkerClick(final Marker mark, MapMode mapMode){
		if (locationsMap.containsKey(mark)) {
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

	void onMarkerDragStart(Marker mark) {
		if(verticesMap.containsKey(mark)){
			onBoundaryMarkerDragStart(mark);
		}else if(holesVerticesMap.containsKey(mark)){
			onHoleMarkerDragStart(mark);
		}else if(locationsMap.containsKey(mark)){
			onLocationMarkerDragStart(mark);
		}
	}

	void onMarkerDragEnd(Marker mark) {
		if(verticesMap.containsKey(mark)){
			onBoundaryMarkerDragEnd(mark);
		}else if(holesVerticesMap.containsKey(mark)){
			onHoleMarkerDragEnd(mark);
		}else if(locationsMap.containsKey(mark)){
			onLocationMarkerDragEnd(mark);
		}
	}

	void onMarkerDrag(Marker mark) {
		if(verticesMap.containsKey(mark)){
			onBoundaryMarkerDrag(mark);
		}else if(holesVerticesMap.containsKey(mark)){
			onHoleMarkerDrag(mark);
		}else if(locationsMap.containsKey(mark)){
			onLocationMarkerDrag(mark);
		}
	}

	private boolean removeSelectedMarker(){

		if (verticesMap.containsKey(selectedMarker)) {
			return removeSelectedBoundaryVertex();
		}else if (holesVerticesMap.containsKey(selectedMarker)) {
			return removeSelectedHoleVertex();
		}else if (locationsMap.containsKey(selectedMarker)) {
			return removeSelectedLocation();
		}
		return false;
		
	}
	
	private boolean removeSelectedLocation(){
		PropertyLocation loc = locationsMap.remove(selectedMarker);
		removeLocationMarker(selectedMarker);
		loc.delete();
		hideMarkerEditControls();
		selectedMarker = null;
		return true;
	}

	private boolean removeSelectedBoundaryVertex(){
		removeBoundaryVertexAndMarker(selectedMarker);
		updateVertices();
		resetAdjacency(otherProperties);
		hideMarkerEditControls();
		calculateGeometry(true);
		redrawProperty();
		selectedMarker = null;
		return true;
	}

	private boolean removeSelectedHoleVertex(){
		removeHoleVertexAndMarker(selectedMarker);
		updateHolesVertices();
		hideMarkerEditControls();
		calculateGeometry(true);
		redrawProperty();
		selectedMarker = null;
		return true;
	}

	private boolean addMarker(MapMode mapMode){
		
		addMarker(target.getPosition(), mapMode);
		deselect();
		return true;
	}

	private boolean moveLocationMarker(){
		Marker newMark = createLocationMarker(target.getPosition(), selectedMarker.getTitle());
		PropertyLocation loc = locationsMap.remove(selectedMarker);
		loc.setMapPosition(target.getPosition());
		loc.update();
		hideMarkerEditControls();
		selectedMarker.remove();
		selectedMarker = null;
		locationsMap.put(newMark, loc);
		return true;
	}

    private boolean moveBoundaryMarker(){
        Marker mark = insertBoundaryMarker(target.getPosition(), verticesMap.get(selectedMarker).getGPSPosition());
        Vertex vertex = removeBoundaryVertexAndMarker(selectedMarker);
        if(validateGeometry()){
            hideMarkerEditControls();
            selectedMarker = null;
            updateVertices();
            resetAdjacency(otherProperties);
            calculateGeometry(true);
            redrawProperty();
            return true;
        }else{
            hideMarkerEditControls();
            vertices.remove(verticesMap.remove(mark));
			mark.remove();
			insertBoundaryMarker(selectedMarker.getPosition(), vertex.getGPSPosition());
            redrawProperty();
			selectedMarker = null;
            return false;
        }
    }

    private boolean moveMarker(){

		// Insert a marker at target position and remove the selected

		if (verticesMap.containsKey(selectedMarker)) {
            if(moveBoundaryMarker()){
                return true;
            }else{
                Toast toast = Toast.makeText(context,
                        R.string.message_invalid_marker_position,
                        Toast.LENGTH_SHORT);
                toast.show();
                return false;
            }
        }else if (holesVerticesMap.containsKey(selectedMarker)) {
            if(moveHoleMarker()){
                return true;
            }else{
                Toast toast = Toast.makeText(context,
                        R.string.message_invalid_marker_position,
                        Toast.LENGTH_SHORT);
                toast.show();
                return false;
            }
        }else if (locationsMap.containsKey(selectedMarker)) {
			return moveLocationMarker();
		}
		return false;
		
	}

	void deselect(){
		hideMarkerEditControls();
		if(selectedMarker != null){
			if(verticesMap.containsKey(selectedMarker)){
				selectedMarker.setIcon(BitmapDescriptorFactory
						.fromResource(R.drawable.ot_blue_marker));
			}else if(holesVerticesMap.containsKey(selectedMarker)){
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
		remove.setClusterGroup(ClusterGroup.NOT_CLUSTERED);
		relativeEdit = map.addMarker(new MarkerOptions()
		.position(projection.fromScreenLocation(getControlRelativeEditPosition(markerScreenPosition, markerWidth, markerHeight)))
		.anchor(0.5f, 0.5f)
		.title("0.0 m")
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_action_move)));
		relativeEdit.setClusterGroup(ClusterGroup.NOT_CLUSTERED);
		cancel = map.addMarker(new MarkerOptions()
		.position(projection.fromScreenLocation(getControlCancelPosition(markerScreenPosition, markerWidth, markerHeight)))
		.anchor(0.5f, 0.5f)
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_menu_block)));
		cancel.setClusterGroup(ClusterGroup.NOT_CLUSTERED);
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
		target.setClusterGroup(ClusterGroup.NOT_CLUSTERED);

		add = map.addMarker(new MarkerOptions()
		.position(projection.fromScreenLocation(getControlAddPosition(markerScreenPosition, markerWidth, markerHeight)))
		.anchor(0.5f, 0.5f)
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_menu_add)));
		add.setClusterGroup(ClusterGroup.NOT_CLUSTERED);
		moveTo = map.addMarker(new MarkerOptions()
		.position(projection.fromScreenLocation(getControlMoveToPosition(markerScreenPosition, markerWidth, markerHeight)))
		.anchor(0.5f, 0.5f)
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_menu_goto)));
		moveTo.setClusterGroup(ClusterGroup.NOT_CLUSTERED);
		cancel = map.addMarker(new MarkerOptions()
		.position(projection.fromScreenLocation(getControlCancelPosition(markerScreenPosition, markerWidth, markerHeight)))
		.anchor(0.5f, 0.5f)
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_menu_block)));
		cancel.setClusterGroup(ClusterGroup.NOT_CLUSTERED);

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

	EditablePropertyBoundary(final Context context, final GoogleMap map, String claimId,
			final ClaimDispatcher claimActivity, final List<BasePropertyBoundary> existingProperties, ModeDispatcher.Mode mode) {
		super(context, map, claimId, true);
		this.claimActivity = claimActivity;
		this.mode = mode;
		this.selectedMarker = null;
		this.otherProperties = existingProperties;
		this.amr = new ActiveMarkerRegistrar();
        verticesMap = new HashMap<Marker, Vertex>();
        if (vertices != null && vertices.size() > 0) {
            for (Vertex vertex : vertices) {
                Marker mark = createBoundaryMarker(vertex.getSequenceNumber(), vertex.getMapPosition());
                verticesMap.put(mark, vertex);
            }
        }
        holesVerticesMap = new HashMap<Marker, HoleVertex>();
        if (holesVertices != null && holesVertices.size() > 0) {
            for (List<HoleVertex> holeVertices : holesVertices) {
                if (holeVertices != null && holeVertices.size() > 0) {
                    for (HoleVertex holeVertex : holeVertices) {
                        Marker mark = createHoleMarker(holeVertex.getHoleNumber(), holeVertex.getSequenceNumber(), holeVertex.getMapPosition());
                        holesVerticesMap.put(mark, holeVertex);
                    }
                }
            }
        }
		showLocations();
	}
	
	void setOtherProperties(List<BasePropertyBoundary> otherProperties) {
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
				Marker mark = createBoundaryMarker(vertex.getSequenceNumber(), vertex.getMapPosition());
				verticesMap.put(mark, vertex);
			}
		}
		for(Marker mark:holesVerticesMap.keySet()){
			mark.remove();
		}
        holesVerticesMap = new HashMap<Marker, HoleVertex>();
        if (holesVertices != null && holesVertices.size() > 0) {
            for (List<HoleVertex> holeVertices : holesVertices) {
                if (holeVertices != null && holeVertices.size() > 0) {
                    for (HoleVertex holeVertex : holeVertices) {
                        Marker mark = createHoleMarker(holeVertex.getHoleNumber(), holeVertex.getSequenceNumber(), holeVertex.getMapPosition());
                        holesVerticesMap.put(mark, holeVertex);
                    }
                }
            }
        }
	}

    private void updateVertices() {

        Vertex.deleteVertices(claimActivity.getClaimId());

        for (int i = 0; i < vertices.size(); i++) {
            Vertex vertex = vertices.get(i);
            vertex.setSequenceNumber(i);
            Vertex.createVertex(vertex);
        }
    }

    private void updateHolesVertices() {
        HoleVertex.deleteVertices(claimActivity.getClaimId());

        int i=0;
        for (List<HoleVertex> hole:holesVertices) {
            int j=0;
            for (HoleVertex vertex:hole) {
                vertex.setHoleNumber(i);
                vertex.setSequenceNumber(j);
                HoleVertex.createVertex(vertex);
                j++;
            }
            i++;
        }
    }

    private void updatePropertyLocations() {

		PropertyLocation.deletePropertyLocations(claimActivity.getClaimId());

		for (PropertyLocation location : locationsMap.values()) {
			location.create();
		}

	}
	void resetAdjacency(List<BasePropertyBoundary> existingProperties) {

        if(claim != null){
            List<BasePropertyBoundary> adjacentProperties = findAdjacentProperties(existingProperties);
            Adjacency.deleteAdjacencies(claim.getClaimId());

            if (adjacentProperties != null) {

                for (BasePropertyBoundary adjacentProperty : adjacentProperties) {

                    Adjacency adj = new Adjacency();
                    adj.setSourceClaimId(claim.getClaimId());
                    adj.setDestClaimId(adjacentProperty.getClaimId());
                    adj.setCardinalDirection(getCardinalDirection(adjacentProperty));
                    adj.create();
                }
            }
        }
	}

	private void onLocationMarkerDragStart(Marker mark) {
		dragLocationMarker(mark);
	}

	private void onLocationMarkerDragEnd(Marker mark) {
		dragLocationMarker(mark);
		updatePropertyLocations();
	}

	private void onLocationMarkerDrag(Marker mark) {
		dragLocationMarker(mark);
	}

	private void onBoundaryMarkerDragStart(Marker mark) {
		draggedVertex = new Vertex(verticesMap.get(mark));
		updateVertexPosition(mark);
		redrawProperty();
	}

	private void onBoundaryMarkerDragEnd(Marker mark) {
		updateVertexPosition(mark);
		if(validateGeometry()){
			updateVertices();
			calculateGeometry(true);
			redrawProperty();
			resetAdjacency(otherProperties);
		}else{
			Toast toast = Toast.makeText(
					context,
					R.string.message_invalid_marker_position,
					Toast.LENGTH_SHORT);
			toast.show();
			updateVertexPosition(mark, draggedVertex);
			redrawProperty();
		}
		draggedVertex = null;
	}

	private void onBoundaryMarkerDrag(Marker mark) {
		updateVertexPosition(mark);
		redrawProperty();
	}

	private void updateHoleVertexPosition(Marker mark) {
		holesVerticesMap.get(mark).setMapPosition(mark.getPosition());
	}

	private void updateHoleVertexPosition(Marker mark, HoleVertex vertex) {
		mark.setPosition(vertex.getMapPosition());
		holesVerticesMap.get(mark).setMapPosition(vertex.getMapPosition());
	}

	private void onHoleMarkerDragStart(Marker mark) {
		draggedHoleVertex = new HoleVertex(holesVerticesMap.get(mark));
		updateHoleVertexPosition(mark);
		redrawProperty();
	}

	private boolean moveHoleMarker(){
		Marker mark = insertHoleMarker(target.getPosition(), holesVerticesMap.get(selectedMarker).getGPSPosition());
		HoleVertex vertex = removeHoleVertexAndMarker(selectedMarker);
		if(validateGeometry()){
			hideMarkerEditControls();
			selectedMarker = null;
			updateHolesVertices();
			calculateGeometry(true);
			redrawProperty();
			return true;
		}else{
			hideMarkerEditControls();
			HoleVertex newVertex = removeHoleVertexAndMarker(mark);
			insertHoleMarker(selectedMarker.getPosition(), vertex.getGPSPosition());
			redrawProperty();
			selectedMarker = null;
			return false;
		}
	}

	private void onHoleMarkerDragEnd(Marker mark) {
		updateHoleVertexPosition(mark);
		if(validateGeometry()){
			updateHolesVertices();
			calculateGeometry(true);
			redrawProperty();
		}else{
			Toast toast = Toast.makeText(
					context,
					R.string.message_invalid_marker_position,
					Toast.LENGTH_SHORT);
			toast.show();
			updateHoleVertexPosition(mark, draggedHoleVertex);
			redrawProperty();
		}
		draggedHoleVertex = null;
	}

	private void onHoleMarkerDrag(Marker mark) {
		updateHoleVertexPosition(mark);
		redrawProperty();
	}

	private void updateVertexPosition(Marker mark) {
		verticesMap.get(mark).setMapPosition(mark.getPosition());
	}

	private void updateVertexPosition(Marker mark, Vertex vertex) {
		mark.setPosition(vertex.getMapPosition());
		verticesMap.get(mark).setMapPosition(vertex.getMapPosition());
	}

	private void dragLocationMarker(Marker mark) {
		locationsMap.get(mark).setMapPosition(mark.getPosition());
	}

	private void removeLocationMarker(Marker mark) {
		locationsMap.remove(mark);
		mark.remove();
	}

	private HoleVertex removeHoleVertexAndMarker(Marker mark) {
        HoleVertex vertex = holesVerticesMap.remove(mark);
        List<HoleVertex> hole = null;
		if(vertex != null){
            for(int i = 0; i < holesVertices.size(); i++){
                if(holesVertices.get(i).remove(vertex)){
                    hole = holesVertices.get(i);
                    break;
                }
            }
        }
        if(hole != null && hole.isEmpty()){
            holesVertices.remove(hole);
        }
		mark.remove();
        return vertex;
	}

	private Vertex removeBoundaryVertexAndMarker(Marker mark) {
        Vertex vertex = verticesMap.remove(mark);
		vertices.remove(vertex);
		mark.remove();
        return vertex;
	}

    private Marker insertBoundaryMarker(LatLng mapPosition, LatLng gpsPosition) {

        if (claimActivity.getClaimId() == null) {
            // Useless to add markers without a claim
            Toast toast = Toast.makeText(context,
                    R.string.message_save_claim_before_adding_content,
                    Toast.LENGTH_SHORT);
            toast.show();
            return null;
        }

        Marker mark = createBoundaryMarker(vertices.size(), mapPosition);
        Vertex newVertex = new Vertex(mapPosition, gpsPosition);
        newVertex.setClaimId(claimActivity.getClaimId());
        verticesMap.put(mark, newVertex);

        if (vertices.size() < 2) {
            // no need to calculate the insertion point
            vertices.add(newVertex);
            return mark;
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
        return mark;
    }

    private Marker insertHoleMarker(LatLng mapPosition, LatLng gpsPosition) {

        if (claimActivity.getClaimId() == null) {
            // Useless to add markers without a claim
            Toast toast = Toast.makeText(context,
                    R.string.message_save_claim_before_adding_content,
                    Toast.LENGTH_SHORT);
            toast.show();
            return null;
        }

		List<HoleVertex> selectedHole;

		if(holesVertices == null){
			holesVertices = new ArrayList<List<HoleVertex>>();
		}
		if(selectedHoleNumber < 0 || selectedHoleNumber > holesVertices.size() - 1){
			selectedHole = new ArrayList<HoleVertex>();
            selectedHoleNumber = holesVertices.size();
			holesVertices.add(selectedHole);
		}else{
			selectedHole = holesVertices.get(selectedHoleNumber);
		}

        Marker mark = createHoleMarker(selectedHoleNumber, selectedHole.size(), mapPosition);
        HoleVertex newVertex = new HoleVertex(mapPosition, gpsPosition);
        newVertex.setClaimId(claimActivity.getClaimId());
        newVertex.setHoleNumber(selectedHoleNumber);
        holesVerticesMap.put(mark, newVertex);


        if (selectedHole.size() < 2) {
            // no need to calculate the insertion point
            selectedHole.add(newVertex);
            return mark;
        }

        double minDistance = Double.MAX_VALUE;
        int insertIndex = 0;

        // calculate the insertion point
        for (int i = 0; i < selectedHole.size(); i++) {

            HoleVertex from = selectedHole.get(i);
            HoleVertex to = null;

            if (i == selectedHole.size() - 1) {
                to = selectedHole.get(0);
            } else {
                to = selectedHole.get(i + 1);
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
        newVertex.setSequenceNumber(insertIndex);
        selectedHole.add(insertIndex, newVertex);
        return mark;
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
            Marker mark = insertBoundaryMarker(mapPosition, gpsPosition);
            if(mark != null){
                if(validateGeometry()){
                    updateVertices();
                    resetAdjacency(otherProperties);
                    calculateGeometry(true);
                    redrawProperty();
                }else{
                    vertices.remove(verticesMap.remove(mark));
                    redrawProperty();
                    Toast toast = Toast.makeText(
                            context,
                            R.string.message_invalid_marker_position,
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
		}else if(mapMode == MapMode.add_non_boundary){
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
                                    addLocation(mapPosition, gpsPosition, locationDescription);
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

		}else if(mapMode == MapMode.edit_hole){

            Marker mark = insertHoleMarker(mapPosition, gpsPosition);
			if(mark != null){
				if(validateGeometry()){
					updateHolesVertices();
					calculateGeometry(true);
					redrawProperty();
				}else{
					HoleVertex newVertex = removeHoleVertexAndMarker(mark);
					redrawProperty();
					Toast toast = Toast.makeText(
							context,
							R.string.message_invalid_marker_position,
							Toast.LENGTH_SHORT);
					toast.show();
				}
			}
        }
	}

	private void addLocation(LatLng mapPosition, LatLng gpsPosition, String description) {

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
		locationsMap.put(mark, loc);
	}

	private Marker createBoundaryMarker(int sequenceNumber, LatLng position) {
		Marker marker;
		if(mode.compareTo(ModeDispatcher.Mode.MODE_RW) == 0){
			marker = map.addMarker(new MarkerOptions()
					.position(position)
					.title(sequenceNumber + ", Lat: " + position.latitude + ", Lon: " + position.longitude)
					.draggable(true)
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.ot_blue_marker)));
		}else{
			marker = map.addMarker(new MarkerOptions()
					.position(position)
					.title(sequenceNumber + ", Lat: " + position.latitude + ", Lon: " + position.longitude)
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.ot_blue_marker)));
		}
		// To prevent vertices to cluster when they are too close
		// assign each vertex to its own group
		marker.setClusterGroup(ClusterGroup.NOT_CLUSTERED);
		return marker;
	}

	private Marker createHoleMarker(int holeNumber, int sequenceNumber, LatLng position) {
		Marker marker;
		if(mode.compareTo(ModeDispatcher.Mode.MODE_RW) == 0){
			marker = map.addMarker(new MarkerOptions()
					.position(position)
					.title("(" + holeNumber + ", " + sequenceNumber + "), Lat: " + position.latitude + ", Lon: " + position.longitude)
					.draggable(true)
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.ot_blue_marker)));
		}else{
			marker = map.addMarker(new MarkerOptions()
					.position(position)
					.title("(" + holeNumber + ", " + sequenceNumber + "), Lat: " + position.latitude + ", Lon: " + position.longitude)
					.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.ot_blue_marker)));
		}
		// To prevent vertices to cluster when they are too close
		// assign each vertex to its own group
		marker.setClusterGroup(ClusterGroup.NOT_CLUSTERED);
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
		marker.setClusterGroup(ClusterGroup.NOT_CLUSTERED);
		return marker;
	}
	
	void saveGeometry(){
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
						"HOLE_NUMBER" + FileSystemUtilities._CSV_FIELD_SEPARATOR
						+ "SEQUENCE_NUMBER" + FileSystemUtilities._CSV_FIELD_SEPARATOR
						+ "GPS_LAT" + FileSystemUtilities._CSV_FIELD_SEPARATOR
						+ "GPS_LON" + FileSystemUtilities._CSV_FIELD_SEPARATOR
						+ "MAP_LAT" + FileSystemUtilities._CSV_FIELD_SEPARATOR
						+ "MAP_LON" + FileSystemUtilities._CSV_REC_TERMINATOR
						);
				bw.flush();

				for(Vertex vertex:claim.getVertices()){
					bw.write("-1" + FileSystemUtilities._CSV_FIELD_SEPARATOR);
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
				for(List<HoleVertex> hole:claim.getHolesVertices()){
					for(HoleVertex vertex:hole){
						bw.write(vertex.getHoleNumber() + FileSystemUtilities._CSV_FIELD_SEPARATOR);
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

	void saveSnapshot() {

		if (claimActivity.getClaimId() != null) {
			map.snapshot(new SnapshotReadyCallback() {

				@Override
				public void onSnapshotReady(Bitmap bmp) {
					String claimId = claimActivity.getClaimId();
					String path = FileSystemUtilities
							.getAttachmentFolder(claimId)
							+ File.separator
							+ DEFAULT_MAP_FILE_NAME;
					try (FileOutputStream out = new FileOutputStream(path)){
						bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
						File file = new File(path);
						Uri uri = FileProvider.getUriForFile(OpenTenureApplication.getContext(), BuildConfig.APPLICATION_ID, file);
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
						att.setPath(uri.toString());
						att.setSize(new File(path).length());
						att.create();
						Toast toast = Toast.makeText(context,
								R.string.message_map_snapshot_saved,
								Toast.LENGTH_SHORT);
						toast.show();
					} catch (Exception e) {
						e.printStackTrace();
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
