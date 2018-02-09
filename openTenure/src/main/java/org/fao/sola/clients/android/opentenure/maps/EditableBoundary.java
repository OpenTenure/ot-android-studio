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
import org.fao.sola.clients.android.opentenure.model.Boundary;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.HoleVertex;
import org.fao.sola.clients.android.opentenure.model.MD5;
import org.fao.sola.clients.android.opentenure.model.PropertyLocation;
import org.fao.sola.clients.android.opentenure.model.Vertex;
import org.fao.sola.clients.android.opentenure.tools.GisUtility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditableBoundary extends BaseBoundary {

	enum MapMode {
		add_boundary, measure
	};

	public static final String DEFAULT_MAP_FILE_NAME = "_map_.jpg";
	private static final String DEFAULT_GEOM_FILE_NAME = "_geom_.csv";
	public static final String DEFAULT_MAP_FILE_TYPE = "cadastralMap";
	public static final String DEFAULT_MAP_MIME_TYPE = "image/jpeg";
	private Map<Marker, LatLng> verticesMap;
	private Boundary boundary;
	private List<BaseBoundary> otherBoundaries;
	private ActiveMarkerRegistrar amr;
	private MapMode mapMode = MapMode.add_boundary;
	private boolean editable;

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
	private LatLng draggedVertex;

	boolean handleMarkerClick(final Marker mark){
		if(handleMarkerEditClick(mark)){
			return true;
		}else if(handleRelativeMarkerEditClick(mark)){
			return true;
        }else if(handleBoundaryMarkerClick(mark)){
            return true;
        }else{
			return handleClick(mark);
		}
	}

	private boolean handleMarkerEditClick(Marker mark){
		if(!editable || remove == null || relativeEdit == null || cancel == null){
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

	private boolean handleRelativeMarkerEditClick(Marker mark){
		if(!editable || up == null || down == null || left == null || right == null || add == null || moveTo == null || cancel == null || target == null){
			return false;
		}

		try {
			if (amr.onClick(mark)) {
				return true;
			}else if (mark.equals(add)) {
				return addMarker();
			}else if (mark.equals(moveTo)) {
				return moveMarker();
			}else if (mark.equals(cancel)) {
				deselect();
				return true;
			}else if (mark.equals(target)) {
				return true;
			}else{
				return false;
			}
		} catch (UnsupportedOperationException e) {
			// Clustered markers have no ID and may throw this
		}
		return false;
	}

	private boolean handleBoundaryMarkerClick(final Marker mark){
		if (verticesMap.containsKey(mark)) {
			if(editable){
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

	private boolean handleClick(Marker mark){
		try{
			// Can only be a click on the property name, deselect and let the event flow
			if(boundaryMarker != null && mark.equals(boundaryMarker)){
				if(editable){
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
		}
	}

	void onMarkerDragEnd(Marker mark) {
		if(verticesMap.containsKey(mark)){
			onBoundaryMarkerDragEnd(mark);
		}
	}

	void onMarkerDrag(Marker mark) {
		if(verticesMap.containsKey(mark)){
			onBoundaryMarkerDrag(mark);
		}
	}

	private boolean removeSelectedMarker(){
		if (verticesMap.containsKey(selectedMarker)) {
			return removeSelectedBoundaryVertex();
		}
		return false;
	}

	private boolean removeSelectedBoundaryVertex(){
		removeBoundaryVertexAndMarker(selectedMarker);
		hideMarkerEditControls();
		calculateGeometry();
		redrawBoundary();
		selectedMarker = null;
		return true;
	}

	private boolean addMarker(){
		addMarker(target.getPosition(), mapMode);
		deselect();
		return true;
	}

    private boolean moveBoundaryMarker(){
        Marker mark = insertBoundaryMarker(target.getPosition());
        LatLng vertex = removeBoundaryVertexAndMarker(selectedMarker);
        if(validateGeometry()){
            hideMarkerEditControls();
            selectedMarker = null;
			calculateGeometry();
            redrawBoundary();
            return true;
        }else{
            hideMarkerEditControls();
            vertices.remove(verticesMap.remove(mark));
			mark.remove();
			insertBoundaryMarker(selectedMarker.getPosition());
            redrawBoundary();
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
        }
		return false;
	}

	void deselect(){
		hideMarkerEditControls();
		if(selectedMarker != null){
			if(verticesMap.containsKey(selectedMarker)){
				selectedMarker.setIcon(BitmapDescriptorFactory
						.fromResource(R.drawable.ot_blue_marker));
			} else{
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

		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.ot_blue_marker);
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

		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.ot_blue_marker);
		int markerHeight = bmp.getHeight();
		int markerWidth = bmp.getWidth();

		remove = map.addMarker(new MarkerOptions()
				.position(projection.fromScreenLocation(getControlRemovePosition(markerScreenPosition, markerWidth, markerHeight)))
				.anchor(0.5f, 0.5f)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_menu_close_clear_cancel)));
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

		Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.ot_blue_marker);
		int markerHeight = bmp.getHeight();
		int markerWidth = bmp.getWidth();

		hideMarkerEditControls();

		target = map.addMarker(new MarkerOptions()
				.position(projection.fromScreenLocation(getControlTargetPosition(markerScreenPosition, markerWidth, markerHeight)))
				.anchor(0.5f, 0.5f)
				.title("0.0 m")
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_menu_mylocation)));
		target.setClusterGroup(ClusterGroup.NOT_CLUSTERED);

		add = map.addMarker(new MarkerOptions()
				.position(projection.fromScreenLocation(getControlAddPosition(markerScreenPosition, markerWidth, markerHeight)))
				.anchor(0.5f, 0.5f)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_menu_add)));
		add.setClusterGroup(ClusterGroup.NOT_CLUSTERED);
		moveTo = map.addMarker(new MarkerOptions()
				.position(projection.fromScreenLocation(getControlMoveToPosition(markerScreenPosition, markerWidth, markerHeight)))
				.anchor(0.5f, 0.5f)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_menu_goto)));
		moveTo.setClusterGroup(ClusterGroup.NOT_CLUSTERED);
		cancel = map.addMarker(new MarkerOptions()
				.position(projection.fromScreenLocation(getControlCancelPosition(markerScreenPosition, markerWidth, markerHeight)))
				.anchor(0.5f, 0.5f)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_menu_block)));
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

	EditableBoundary(final Context context, final GoogleMap map, Boundary boundary, final List<BaseBoundary> existingBoundaries, boolean editable) {
		super(context, map, boundary);
		this.boundary = boundary;
		this.editable = editable;
		this.selectedMarker = null;
		this.otherBoundaries = existingBoundaries;
		this.amr = new ActiveMarkerRegistrar();
        verticesMap = new HashMap<Marker, LatLng>();
        if (vertices != null && vertices.size() > 0) {
        	int i = 1;
            for (LatLng vertex : vertices) {
                Marker mark = createBoundaryMarker(i, vertex);
                verticesMap.put(mark, vertex);
                i+=1;
            }
        }
	}
	
	void setOtherBoundaries(List<BaseBoundary> otherBoundaries) {
		this.otherBoundaries = otherBoundaries;
	}

	private List<BaseBoundary> findAdjacentBoundaries(List<BaseBoundary> boundaries) {
		List<BaseBoundary> adjacentBoundaries = null;
		for (BaseBoundary boundary : boundaries) {
			if (polygon != null && boundary.getPolygon() != null && polygon.distance(boundary.getPolygon()) < SNAP_THRESHOLD) {
				if (adjacentBoundaries == null) {
					adjacentBoundaries = new ArrayList<BaseBoundary>();
				}
				adjacentBoundaries.add(boundary);
			}
		}
		return adjacentBoundaries;
	}

	protected void reload(){
		for(Marker mark:verticesMap.keySet()){
			mark.remove();
		}

		super.loadBoundary();
		verticesMap = new HashMap<>();

		if (vertices != null && vertices.size() > 0) {
			int i = 1;
			for (LatLng vertex : vertices) {
				Marker mark = createBoundaryMarker(i, vertex);
				verticesMap.put(mark, vertex);
				i+=1;
			}
		}
	}

	private void onBoundaryMarkerDragStart(Marker mark) {
		draggedVertex = new LatLng(verticesMap.get(mark).latitude, verticesMap.get(mark).longitude);
		updateVertexPosition(mark);
		redrawBoundary();
	}

	private void onBoundaryMarkerDragEnd(Marker mark) {
		updateVertexPosition(mark);
		if(validateGeometry()){
			calculateGeometry();
			redrawBoundary();
		}else{
			Toast toast = Toast.makeText(
					context,
					R.string.message_invalid_marker_position,
					Toast.LENGTH_SHORT);
			toast.show();
			updateVertexPosition(mark, draggedVertex);
			redrawBoundary();
		}
		draggedVertex = null;
	}

	private void onBoundaryMarkerDrag(Marker mark) {
		updateVertexPosition(mark);
		redrawBoundary();
	}

	private void updateVertexPosition(Marker mark) {
		int i = vertices.indexOf(verticesMap.get(mark));
		verticesMap.put(mark, mark.getPosition());
		if(i > -1){
			vertices.set(i, mark.getPosition());
		}
	}

	private void updateVertexPosition(Marker mark, LatLng vertex) {
		int i = vertices.indexOf(verticesMap.get(mark));
		mark.setPosition(vertex);
		verticesMap.put(mark, vertex);
		if(i > -1){
			vertices.set(i, mark.getPosition());
		}
	}

	private LatLng removeBoundaryVertexAndMarker(Marker mark) {
        LatLng vertex = verticesMap.remove(mark);
		vertices.remove(vertex);
		mark.remove();
        return vertex;
	}

    private Marker insertBoundaryMarker(LatLng mapPosition) {
        Marker mark = createBoundaryMarker(vertices.size(), mapPosition);
        LatLng newVertex = new LatLng(mapPosition.latitude, mapPosition.longitude);
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
            LatLng from = vertices.get(i);
            LatLng to = null;

            if (i == vertices.size() - 1) {
                to = vertices.get(0);
            } else {
                to = vertices.get(i + 1);
            }

            PointPairDistance ppd = new PointPairDistance();
            DistanceToPoint.computeDistance(
                    new LineSegment(from.longitude, from.latitude, to.longitude, to.latitude),
                    new Coordinate(newVertex.longitude, newVertex.latitude), ppd);

            double currDistance = ppd.getDistance();

            if (currDistance < minDistance) {
                minDistance = currDistance;
                insertIndex = i + 1;
            }
        }
        vertices.add(insertIndex, newVertex);
        return mark;
    }

	public void addMarker(final LatLng mapPosition, MapMode mapMode){
		if(mapMode == MapMode.add_boundary){
            Marker mark = insertBoundaryMarker(mapPosition);
            if(mark != null){
                if(validateGeometry()){
					calculateGeometry();
                    redrawBoundary();
                }else{
                    vertices.remove(verticesMap.remove(mark));
					redrawBoundary();
                    Toast toast = Toast.makeText(
                            context,
                            R.string.message_invalid_marker_position,
                            Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
		}
	}

	private Marker createBoundaryMarker(int sequenceNumber, LatLng position) {
		Marker marker;
		if(editable){
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

	void saveGeometry(){
		if (boundary != null) {
			BufferedWriter bw = null;
			String path = FileSystemUtilities.getExportFolder().getPath()
					+ File.separator
					+ boundary.getName()
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

				int i = 1;
				for(LatLng vertex: GisUtility.getVertices(boundary.getGeom())){
					bw.write("-1" + FileSystemUtilities._CSV_FIELD_SEPARATOR);
					bw.write(i + FileSystemUtilities._CSV_FIELD_SEPARATOR);
					LatLng gpsPosition = vertex;
					if(gpsPosition != null){
						bw.write(gpsPosition.latitude + FileSystemUtilities._CSV_FIELD_SEPARATOR
								+ gpsPosition.longitude + FileSystemUtilities._CSV_FIELD_SEPARATOR);
					}
					else{
						bw.write("null" + FileSystemUtilities._CSV_FIELD_SEPARATOR
								+ "null" + FileSystemUtilities._CSV_FIELD_SEPARATOR);
					}
					LatLng mapPosition = vertex;
					if(mapPosition != null){
						bw.write(mapPosition.latitude + FileSystemUtilities._CSV_FIELD_SEPARATOR
								+ mapPosition.longitude + FileSystemUtilities._CSV_REC_TERMINATOR);
					}
					else{
						bw.write("null" + FileSystemUtilities._CSV_FIELD_SEPARATOR
								+ "null" + FileSystemUtilities._CSV_REC_TERMINATOR);
					}
					bw.flush();
					i+=1;
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
}
