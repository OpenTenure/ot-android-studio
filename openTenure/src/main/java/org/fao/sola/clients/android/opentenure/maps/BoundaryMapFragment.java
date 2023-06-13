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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.androidmapsextensions.ClusterGroup;
import com.androidmapsextensions.ClusteringSettings;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.GoogleMap.CancelableCallback;
import com.androidmapsextensions.GoogleMap.OnCameraChangeListener;
import com.androidmapsextensions.GoogleMap.OnMapClickListener;
import com.androidmapsextensions.GoogleMap.OnMapLongClickListener;
import com.androidmapsextensions.GoogleMap.OnMarkerClickListener;
import com.androidmapsextensions.GoogleMap.OnMarkerDragListener;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.OnMapReadyCallback;
import com.androidmapsextensions.Polyline;
import com.androidmapsextensions.PolylineOptions;
import com.androidmapsextensions.SupportMapFragment;
import com.androidmapsextensions.TileOverlay;
import com.androidmapsextensions.TileOverlayOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;
import com.vividsolutions.jts.algorithm.distance.DistanceToPoint;
import com.vividsolutions.jts.algorithm.distance.PointPairDistance;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import org.fao.sola.clients.android.opentenure.BoundaryActivity;
import org.fao.sola.clients.android.opentenure.BoundaryDispatcher;
import org.fao.sola.clients.android.opentenure.MapLabel;
import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.SelectBookmarkActivity;
import org.fao.sola.clients.android.opentenure.maps.MainMapFragment.MapType;
import org.fao.sola.clients.android.opentenure.model.Bookmark;
import org.fao.sola.clients.android.opentenure.model.Boundary;
import org.fao.sola.clients.android.opentenure.model.Configuration;
import org.fao.sola.clients.android.opentenure.tools.GisUtility;

import java.util.ArrayList;
import java.util.List;

public class BoundaryMapFragment extends Fragment  implements SensorEventListener {
	private BoundaryDispatcher boundaryDispatcher;
	private BoundaryActivity boundaryActivity;
	private static final int MAP_LABEL_FONT_SIZE = 16;
	private static final String OSM_MAPNIK_BASE_URL = "http://a.tile.openstreetmap.org/{z}/{x}/{y}.png";
	private static final String OSM_MAPQUEST_BASE_URL = "http://otile1.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png";
	private static final float CUSTOM_TILE_PROVIDER_Z_INDEX = 1.0f;
	protected static final float MEASURE_Z_INDEX = 3.0f;
	private static final int BOOKMARK_RESULT = 100;

	private EditableBoundary.MapMode mapMode = EditableBoundary.MapMode.add_boundary;
	private MapType mapType = MapType.map_provider_google_normal;
	private View mapView;
	private MapLabel label;
	private GoogleMap map;
	private EditableBoundary currentBoundary;
	private List<BaseBoundary> visibleBoundaries;
	private List<Boundary> allBoundaries;
	private MultiPolygon visibleBoundariesMultiPolygon;
	private final static String MAP_TYPE = "__MAP_TYPE__";
	private double snapLat;
	private double snapLon;
	private Menu menu;
	private boolean isRotating = false;
	private boolean isFollowing = false;
	private Marker myLocation;
	private CameraPosition newCameraPosition;
	private Marker distanceStart;
	private Marker distanceEnd;
	private Marker distanceMarker;
	private Marker bookmark;
	private Polyline distanceSegment;
	private FusedLocationProviderClient fusedLocationClient;
	private LocationCallback locationCallback;
	private LocationRequest locationRequest;
	private Location lastKnownLocation;

	// device sensor manager
	private SensorManager mSensorManager;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			boundaryActivity = (BoundaryActivity) activity;
			boundaryDispatcher = (BoundaryDispatcher) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must be BoundaryActivity implementing BoundaryDispatcher");
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(MAP_TYPE, mapType.toString());
		super.onSaveInstanceState(outState);

	}

	private void stopLocationUpdates() {
		fusedLocationClient.removeLocationUpdates(locationCallback);
	}

	private void startLocationUpdates() {
		if (ActivityCompat.checkSelfPermission(OpenTenureApplication.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
				&& ActivityCompat.checkSelfPermission(OpenTenureApplication.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
		}
		fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.boundary_map, menu);
		menu.findItem(R.id.action_stop_rotating).setVisible(false);

		MenuItem saveAction = menu.findItem(R.id.action_save);
		saveAction.setVisible(editable());

		if (!editable()) {
			menu.removeItem(R.id.action_add_from_gps);
			menu.findItem(R.id.action_change_map_mode).getSubMenu().removeItem(R.id.action_add_boundary);
			mapMode = EditableBoundary.MapMode.measure;
		}
		this.menu = menu;
		super.onCreateOptionsMenu(menu, inflater);
		setHasOptionsMenu(true);
		setRetainInstance(true);
	}

	@Override
	public void onResume() {
		super.onResume();
		startLocationUpdates();
		if (isRotating) {
			mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
					SensorManager.SENSOR_DELAY_UI);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
		stopLocationUpdates();
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		Fragment map = getFragmentManager().findFragmentById(R.id.boundary_map_fragment);
		Fragment label = getFragmentManager().findFragmentById(R.id.boundary_map_provider_label);
		try {
			if (map.isResumed()) {
				FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
				ft.remove(map);
				ft.commit();
			}
			if (label.isResumed()) {
				FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
				ft.remove(label);
				ft.commit();
			}
		} catch (Exception e) {
		}
		super.onDestroyView();
	}

	private void centerMapOnCurrentBoundary(CancelableCallback callback) {
		if (currentBoundary.getCenter() != null) {
			final int CLAIM_MAP_SIZE = 800;
			final int CLAIM_MAP_PADDING = 50;
			CameraPosition oldCameraPosition;

			// A property exists for the claim
			// so we center on it
			LatLngBounds llb = currentBoundary.getBounds();
			oldCameraPosition = map.getCameraPosition();
			newCameraPosition = map.getCameraPosition();
			map.animateCamera(
					CameraUpdateFactory.newLatLngBounds(llb, CLAIM_MAP_SIZE, CLAIM_MAP_SIZE, CLAIM_MAP_PADDING),
					callback);
			if (oldCameraPosition.equals(newCameraPosition) && callback != null) {
				// NOTE: THIS IS A DIRTY HACK
				// animateCamera will not invoke callback.onFinish if there is
				// no need to move the camera
				// so, if the map was never moved, we need to force it
				callback.onFinish();
			}
		} else {
			// No property exists for this claim
			// so we center where we left the main map
			String zoom = Configuration.getConfigurationValue(MainMapFragment.MAIN_MAP_ZOOM);
			String latitude = Configuration.getConfigurationValue(MainMapFragment.MAIN_MAP_LATITUDE);
			String longitude = Configuration.getConfigurationValue(MainMapFragment.MAIN_MAP_LONGITUDE);

			if (zoom != null && latitude != null && longitude != null) {
				try {
					map.moveCamera(CameraUpdateFactory.newLatLngZoom(
							new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)),
							Float.parseFloat(zoom)));
				} catch (Exception e) {
				}
			}
		}
	}

	private void centerMapOnBookmark(String bookmarkId) {
		Bookmark bm = Bookmark.getBookmark(bookmarkId);
		if (bm != null) {
			// No property exists for this claim
			// so we center where we left the main map
			float zoom = map.getMaxZoomLevel() - MainMapFragment.MAX_ZOOM_LEVELS_TO_DOWNLOAD;
			if(bookmark != null){
				bookmark.remove();
			}
			bookmark = createMapBookmarkMarker(new LatLng(bm.getLat(),bm.getLon()),bm.getName());
			try {
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(
						new LatLng(bm.getLat(), bm.getLon()),zoom));
			} catch (Exception e) {
			}
		}
	}

	private Marker createMapBookmarkMarker(LatLng position, String description) {
		Marker marker;
		marker = map.addMarker(new MarkerOptions().position(position).title(description)
				.clusterGroup(ClusterGroup.NOT_CLUSTERED)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_bookmark)));
		return marker;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setRetainInstance(true);
		super.onCreateView(inflater, container, savedInstanceState);
		mapView = inflater.inflate(R.layout.fragment_boundary_map, container, false);
		setHasOptionsMenu(true);
		label = (MapLabel) getChildFragmentManager().findFragmentById(R.id.boundary_map_provider_label);
		label.changeTextProperties(MAP_LABEL_FONT_SIZE,	getActivity().getResources().getString(R.string.map_provider_google_normal));
		final BoundaryMapFragment that = this;

		// Location client
		fusedLocationClient = LocationServices.getFusedLocationProviderClient(OpenTenureApplication.getContext());
		locationRequest = LocationRequest.create();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setInterval(5000);
		locationRequest.setFastestInterval(1000);

		locationCallback = new LocationCallback() {
			@Override
			public void onLocationResult(LocationResult locationResult) {
				if (locationResult == null) {
					return;
				}
				lastKnownLocation = locationResult.getLastLocation();
			}
		};

		SupportMapFragment mapViewFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.boundary_map_fragment);
		mapViewFragment.getExtendedMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(GoogleMap googleMap) {
				map = googleMap;
				ClusteringSettings settings = new ClusteringSettings();
				settings.clusterOptionsProvider(new OpenTenureClusterOptionsProvider(getResources()));
				settings.addMarkersDynamically(true);
				map.setClustering(settings);

				//MapsInitializer.initialize(this.getActivity());
				map.setInfoWindowAdapter(new PopupAdapter(inflater));
				map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
					@Override
					public void onCameraIdle() {
						hideVisibleBoundaries();
						reloadVisibleBoundaries(false);
						showVisibleBoundaries();
						currentBoundary.redrawBoundary();
						currentBoundary.refreshMarkerEditControls();
						newCameraPosition = map.getCameraPosition();
					}
				});

				if (savedInstanceState != null && savedInstanceState.getString(MAP_TYPE) != null) {
					// probably an orientation change don't move the view but
					// restore the current type of the map
					mapType = MapType.valueOf(savedInstanceState.getString(MAP_TYPE));
					setMapType(true);
				} else {
					// restore the latest map type used on the main map
					try {
						mapType = MapType.valueOf(Configuration.getConfigurationValue(MainMapFragment.MAIN_MAP_TYPE));
					} catch (Exception e) {
						mapType = MapType.map_provider_google_normal;
					}
					// don't draw properties since we might not have loaded them yet
					setMapType(false);
				}

				hideVisibleBoundaries();
				currentBoundary = new EditableBoundary(mapView.getContext(), map, boundaryActivity.getBoundary(), visibleBoundaries, editable());

				centerMapOnCurrentBoundary(null);
				reloadVisibleBoundaries(true);
				showVisibleBoundaries();
				drawAreaOfInterest();

				if (editable()) {
					// Allow adding, removing and dragging markers
					map.setOnMapLongClickListener(new OnMapLongClickListener() {

						@Override
						public void onMapLongClick(final LatLng position) {
							currentBoundary.addMarker(position, mapMode);
						}
					});

					map.setOnMarkerDragListener(new OnMarkerDragListener() {
						@Override
						public void onMarkerDrag(Marker mark) {
							PointPairDistance ppd = new PointPairDistance();
							DistanceToPoint.computeDistance(visibleBoundariesMultiPolygon,
									new Coordinate(mark.getPosition().longitude, mark.getPosition().latitude), ppd);

							if (ppd.getDistance() < BasePropertyBoundary.SNAP_THRESHOLD) {
								snapLat = ppd.getCoordinate(0).y;
								snapLon = ppd.getCoordinate(0).x;
								mark.setPosition(new LatLng(snapLat, snapLon));
							} else {
								snapLat = 0.0;
								snapLon = 0.0;
							}
							currentBoundary.onMarkerDrag(mark);
						}

						@Override
						public void onMarkerDragEnd(Marker mark) {
							if (snapLat != 0.0 && snapLon != 0.0) {
								mark.setPosition(new LatLng(snapLat, snapLon));
							}
							currentBoundary.onMarkerDragEnd(mark);
						}

						@Override
						public void onMarkerDragStart(Marker mark) {
							currentBoundary.onMarkerDragStart(mark);
						}

					});

				}

				map.setOnMarkerClickListener(new OnMarkerClickListener() {

					@Override
					public boolean onMarkerClick(final Marker mark) {
						switch (mapMode) {
							case add_boundary:
								return currentBoundary.handleMarkerClick(mark);
							case measure:
								return handleDistanceMarkerClick(mark);
							default:
								return false;
						}
					}
				});

				map.setOnMapClickListener(new OnMapClickListener() {

					@Override
					public void onMapClick(final LatLng position) {
						switch (mapMode) {
							case add_boundary:
							case measure:
								cancelDistance();
								break;
							default:
								break;
						}
					}
				});

				mSensorManager = (SensorManager) mapView.getContext().getSystemService(Context.SENSOR_SERVICE);
				currentBoundary.redrawBoundary();
			}
		});

		return mapView;
	}

	private boolean handleDistanceMarkerClick(Marker mark) {
		hideDistance();
		if ((distanceStart != null && mark.equals(distanceStart))
				|| (distanceEnd != null && mark.equals(distanceEnd))) {
			// Second click on a selected marker: stop measuring
			distanceStart = null;
			distanceEnd = null;
		} else if (distanceEnd != null && distanceStart == null) {
			// measureStart selected
			distanceStart = mark;
		} else if (distanceStart != null && distanceEnd == null) {
			// measureEnd selected
			distanceEnd = mark;
		} else if (distanceStart != null && distanceEnd != null) {
			// new measureEnd selected
			distanceStart = distanceEnd;
			distanceEnd = mark;
		} else if (distanceStart == null && distanceEnd == null) {
			// measureStart selected
			distanceStart = mark;
		}
		if (distanceStart != null && distanceEnd != null) {
			showDistance();
		}
		return true;
	}

	private void showDistance() {
		if (distanceStart != null && distanceEnd != null) {
			double distance = SphericalUtil.computeDistanceBetween(distanceStart.getPosition(),
					distanceEnd.getPosition());

			if (distance < 0.01) {
				return;
			}

			PolylineOptions polylineOptions = new PolylineOptions();
			polylineOptions.add(distanceStart.getPosition());
			polylineOptions.add(distanceEnd.getPosition());
			polylineOptions.zIndex(MEASURE_Z_INDEX);
			polylineOptions.width(2);
			polylineOptions.color(Color.BLACK);
			distanceSegment = map.addPolyline(polylineOptions);

			distanceMarker = map
					.addMarker(
							new MarkerOptions()
									.position(new LatLng((distanceStart.getPosition().latitude
											+ distanceEnd.getPosition().latitude) / 2,
											(distanceStart.getPosition().longitude + distanceEnd.getPosition().longitude) / 2))
									.anchor(0.5f, 1.0f));
			distanceMarker.setAlpha(0.0f);
			distanceMarker.setInfoWindowAnchor(.5f, 1.0f);
			distanceMarker.setClusterGroup(ClusterGroup.NOT_CLUSTERED);
			String title = String.format(OpenTenureApplication.getInstance().getLocale(), "%s: %.1f %s",
					getResources().getString(R.string.markers_distance_label), distance,
					getResources().getString(R.string.meters));
			distanceMarker.setTitle(title);
			distanceMarker.showInfoWindow();
		}
	}

	private void hideDistance() {
		if (distanceMarker != null) {
			distanceMarker.remove();
			distanceMarker = null;
		}
		if (distanceSegment != null) {
			distanceSegment.remove();
			distanceSegment = null;
		}
	}

	private void cancelDistance() {
		hideDistance();
		distanceStart = null;
		distanceEnd = null;
	}

	private Polygon getPolygon(LatLngBounds bounds) {
		GeometryFactory gf = new GeometryFactory();
		Coordinate[] coords = new Coordinate[5];

		coords[0] = new Coordinate(bounds.northeast.longitude, bounds.northeast.latitude);
		coords[1] = new Coordinate(bounds.northeast.longitude, bounds.southwest.latitude);
		coords[2] = new Coordinate(bounds.southwest.longitude, bounds.southwest.latitude);
		coords[3] = new Coordinate(bounds.southwest.longitude, bounds.northeast.latitude);
		coords[4] = new Coordinate(bounds.northeast.longitude, bounds.northeast.latitude);

		Polygon polygon = gf.createPolygon(coords);
		polygon.setSRID(Constants.SRID);
		return polygon;
	}

	private void reloadVisibleBoundaries(boolean updateArea) {

		LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
		Polygon boundsPoly = getPolygon(bounds);

		if (allBoundaries == null) {
			allBoundaries = Boundary.getBoundariesByStatus(null);
		}

		visibleBoundaries = new ArrayList<BaseBoundary>();
		for (Boundary b : allBoundaries) {
			if (!b.getId().equalsIgnoreCase(boundaryActivity.getBoundary().getId())) {
				BaseBoundary bpb = new BaseBoundary(mapView.getContext(), map, b);
				Polygon claimPoly = bpb.getPolygon();
				if (claimPoly != null && claimPoly.intersects(boundsPoly)) {
					visibleBoundaries.add(bpb);
				}
			}
		}

		currentBoundary.setOtherBoundaries(visibleBoundaries);
		List<Polygon> visibleBoundariesPolygonList = new ArrayList<Polygon>();

		for (BaseBoundary visibleBoundary : visibleBoundaries) {
			if (visibleBoundary.getVertices() != null && visibleBoundary.getVertices().size() > 0) {
				visibleBoundariesPolygonList.add(visibleBoundary.getPolygon());
			}
		}

		Polygon[] visibleBoundariesPolygons = new Polygon[visibleBoundariesPolygonList.size()];
		visibleBoundariesPolygonList.toArray(visibleBoundariesPolygons);

		GeometryFactory gf = new GeometryFactory();
		visibleBoundariesMultiPolygon = gf.createMultiPolygon(visibleBoundariesPolygons);
		visibleBoundariesMultiPolygon.setSRID(Constants.SRID);
	}

	private void setMapLabel() {

		String mode = null;

		switch (mapMode) {
			case add_boundary:
				mode = ": " + getResources().getString(R.string.action_add_boundary);
				break;
			case measure:
				mode = ": " + getResources().getString(R.string.action_measure);
				break;
		}

		switch (mapType) {
			case map_provider_google_normal:
				label.changeTextProperties(MAP_LABEL_FONT_SIZE,
						getResources().getString(R.string.map_provider_google_normal) + mode);
				break;
			case map_provider_google_satellite:
				label.changeTextProperties(MAP_LABEL_FONT_SIZE,
						getResources().getString(R.string.map_provider_google_satellite) + mode);
				break;
			case map_provider_google_hybrid:
				label.changeTextProperties(MAP_LABEL_FONT_SIZE,
						getResources().getString(R.string.map_provider_google_hybrid) + mode);
				break;
			case map_provider_google_terrain:
				label.changeTextProperties(MAP_LABEL_FONT_SIZE,
						getResources().getString(R.string.map_provider_google_terrain) + mode);
				break;
			case map_provider_osm_mapnik:
				label.changeTextProperties(MAP_LABEL_FONT_SIZE,
						getResources().getString(R.string.map_provider_osm_mapnik) + mode);
				break;
			case map_provider_osm_mapquest:
				label.changeTextProperties(MAP_LABEL_FONT_SIZE,
						getResources().getString(R.string.map_provider_osm_mapquest) + mode);
				break;
			case map_provider_local_tiles:
				label.changeTextProperties(MAP_LABEL_FONT_SIZE,
						getResources().getString(R.string.map_provider_local_tiles) + mode);
				break;
			case map_provider_geoserver:
				label.changeTextProperties(MAP_LABEL_FONT_SIZE,
						getResources().getString(R.string.map_provider_geoserver) + mode);
				break;
			default:
				break;
		}
	}

	private void setMapType(boolean drawBoundaries) {

		for (TileOverlay tiles : map.getTileOverlays()) {
			tiles.remove();
		}

		switch (mapType) {
			case map_provider_google_normal:
				map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				break;
			case map_provider_google_satellite:
				map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
				break;
			case map_provider_google_hybrid:
				map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
				break;
			case map_provider_google_terrain:
				map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
				break;
			case map_provider_osm_mapnik:
				OsmTileProvider mapNikTileProvider = new OsmTileProvider(256, 256, OSM_MAPNIK_BASE_URL);
				map.setMapType(GoogleMap.MAP_TYPE_NONE);
				map.addTileOverlay(
						new TileOverlayOptions().tileProvider(mapNikTileProvider).zIndex(CUSTOM_TILE_PROVIDER_Z_INDEX));
				if (drawBoundaries) {
					redrawBoundaries();
				}
				break;
			case map_provider_osm_mapquest:
				OsmTileProvider mapQuestTileProvider = new OsmTileProvider(256, 256, OSM_MAPQUEST_BASE_URL);
				map.setMapType(GoogleMap.MAP_TYPE_NONE);
				map.addTileOverlay(
						new TileOverlayOptions().tileProvider(mapQuestTileProvider).zIndex(CUSTOM_TILE_PROVIDER_Z_INDEX));
				if (drawBoundaries) {
					redrawBoundaries();
				}
				break;
			case map_provider_local_tiles:
				map.setMapType(GoogleMap.MAP_TYPE_NONE);
				map.addTileOverlay(new TileOverlayOptions().tileProvider(new LocalMapTileProvider())
						.zIndex(CUSTOM_TILE_PROVIDER_Z_INDEX));
				if (drawBoundaries) {
					redrawBoundaries();
				}
				break;
			case map_provider_geoserver:
				map.setMapType(GoogleMap.MAP_TYPE_NONE);
				map.addTileOverlay(new TileOverlayOptions().tileProvider(new WmsMapTileProvider(256, 256, PreferenceManager.getDefaultSharedPreferences(mapView.getContext()))));
				if (drawBoundaries) {
					redrawBoundaries();
				}
				break;
			default:
				break;
		}
		setMapLabel();
	}

	private void redrawBoundaries() {
		currentBoundary.redrawBoundary();
		redrawVisibleProperties();
		drawAreaOfInterest();
	}

	private void showVisibleBoundaries() {
		if (visibleBoundaries != null) {
			for (BaseBoundary visibleBoundary : visibleBoundaries) {
				visibleBoundary.showBoundary();
			}
		}

	}

	private void redrawVisibleProperties() {
		hideVisibleBoundaries();
		showVisibleBoundaries();
	}

	private void drawAreaOfInterest() {
		CommunityArea area = new CommunityArea(map);
		area.drawInterestArea();
	}

	private void hideVisibleBoundaries() {
		if (visibleBoundaries != null) {
			for (BaseBoundary visibleBoundary : visibleBoundaries) {
				visibleBoundary.hideBoundary();
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// handle item selection
		Toast toast;
		switch (item.getItemId()) {
			case R.id.action_settings:
				return true;
			case R.id.action_add_boundary:
				mapMode = EditableBoundary.MapMode.add_boundary;
				currentBoundary.deselect();
				cancelDistance();
				setMapLabel();
				return true;
			case R.id.action_measure:
				mapMode = EditableBoundary.MapMode.measure;
				currentBoundary.deselect();
				cancelDistance();
				setMapLabel();
				return true;
			case R.id.map_provider_google_normal:
				mapType = MapType.map_provider_google_normal;
				setMapType(true);
				return true;
			case R.id.map_provider_google_satellite:
				mapType = MapType.map_provider_google_satellite;
				setMapType(true);
				return true;
			case R.id.map_provider_google_hybrid:
				mapType = MapType.map_provider_google_hybrid;
				setMapType(true);
				return true;
			case R.id.map_provider_google_terrain:
				mapType = MapType.map_provider_google_terrain;
				setMapType(true);
				return true;
			case R.id.map_provider_osm_mapnik:
				mapType = MapType.map_provider_osm_mapnik;
				setMapType(true);
				return true;
			case R.id.map_provider_osm_mapquest:
				mapType = MapType.map_provider_osm_mapquest;
				setMapType(true);
				return true;
			case R.id.map_provider_local_tiles:
				mapType = MapType.map_provider_local_tiles;
				setMapType(true);
				return true;
			case R.id.map_provider_geoserver:
				mapType = MapType.map_provider_geoserver;
				setMapType(true);
				return true;
			case R.id.action_save:
				boundaryActivity.onSave();
				return true;
			case R.id.action_center_and_follow:
				if (isFollowing) {
					isFollowing = false;
					myLocation.remove();
					myLocation = null;
				} else {
					LatLng currentLocation = null;
					if(lastKnownLocation != null){
						currentLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
					}

					if (currentLocation != null && currentLocation.latitude != 0.0 && currentLocation.longitude != 0.0) {
						map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18), 1000, null);
						myLocation = map.addMarker(new MarkerOptions().position(currentLocation).anchor(0.5f, 0.5f)
								.title(mapView.getContext().getResources().getString(R.string.title_i_m_here))
								.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_menu_mylocation)));
						myLocation.setClusterGroup(Constants.MY_LOCATION_MARKERS_GROUP);
						isFollowing = true;

					} else {
						Toast.makeText(getActivity().getBaseContext(), R.string.check_location_service, Toast.LENGTH_LONG)
								.show();
					}
				}
				return true;
			case R.id.action_add_from_gps:
				LatLng newLocation = null;

				if(lastKnownLocation != null){
					newLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
				}

				if (newLocation != null && newLocation.latitude != 0.0 && newLocation.longitude != 0.0) {
					currentBoundary.addMarker(newLocation, mapMode);
				} else {
					Toast.makeText(getActivity().getBaseContext(), R.string.check_location_service, Toast.LENGTH_LONG)
							.show();
				}
				return true;
			case R.id.action_rotate:
				if (!isRotating && lastKnownLocation != null) {
					menu.findItem(R.id.action_rotate).setVisible(false);
					menu.findItem(R.id.action_stop_rotating).setVisible(true);
					mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
							SensorManager.SENSOR_DELAY_UI);
					isRotating = true;
				} else {
					Toast.makeText(getActivity().getBaseContext(), R.string.check_location_service, Toast.LENGTH_LONG)
							.show();
				}
				return true;
			case R.id.action_stop_rotating:
				menu.findItem(R.id.action_rotate).setVisible(true);
				menu.findItem(R.id.action_stop_rotating).setVisible(false);
				mSensorManager.unregisterListener(this);
				map.stopAnimation();
				isRotating = false;
				return true;
			case R.id.action_select_bookmark:
				Intent intent = new Intent(getActivity().getBaseContext(), SelectBookmarkActivity.class);
				startActivityForResult(intent, BOOKMARK_RESULT);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (data != null) { // No selection has been done

			switch (requestCode) {
				case BOOKMARK_RESULT:
					String bookmarkId = data.getStringExtra(SelectBookmarkActivity.BOOKMARK_ID_KEY);
					Log.d(this.getClass().getName(), "Selected bookmark: " + bookmarkId);
					centerMapOnBookmark(bookmarkId);
					break;
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	private boolean editable(){
		return boundaryActivity.getBoundary().getStatusCode().equals("pending") && !boundaryActivity.getBoundary().isProcessed();
	}

	public String getGeom(){
		try {
			return GisUtility.getWktPolygonFromVertices(currentBoundary.getVertices());
		} catch (Exception e) {
			Toast toast = Toast.makeText(boundaryActivity.getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT);
			toast.show();
			return null;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// nothing to do as of now
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR && lastKnownLocation != null && map != null) {
			GeomagneticField gField = new GeomagneticField(
					(float)lastKnownLocation.getLatitude(),
					(float)lastKnownLocation.getLongitude(),
					(float)lastKnownLocation.getAltitude(),
					lastKnownLocation.getTime());
			float mDeclination = gField.getDeclination();
			float[] mRotationMatrix = new float[16];
			SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
			float[] orientation = new float[3];
			SensorManager.getOrientation(mRotationMatrix, orientation);
			float bearing = (float) (Math.toDegrees(orientation[0]) + mDeclination);
			CameraPosition currentPosition = map.getCameraPosition();

			CameraPosition newPosition = new CameraPosition.Builder(currentPosition).bearing(bearing).build();

			map.animateCamera(CameraUpdateFactory.newCameraPosition(newPosition));
		}
	}
}