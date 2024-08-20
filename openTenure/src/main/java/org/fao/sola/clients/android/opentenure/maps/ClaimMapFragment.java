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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.fao.sola.clients.android.opentenure.ClaimDispatcher;
import org.fao.sola.clients.android.opentenure.MapLabel;
import org.fao.sola.clients.android.opentenure.ModeDispatcher;
import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.SelectBookmarkActivity;
import org.fao.sola.clients.android.opentenure.maps.MainMapFragment.MapType;
import org.fao.sola.clients.android.opentenure.model.Bookmark;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.Configuration;
import org.fao.sola.clients.android.opentenure.model.HoleVertex;
import org.fao.sola.clients.android.opentenure.model.UserLayer;

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
import com.google.android.gms.maps.model.BitmapDescriptor;
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

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
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
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ClaimMapFragment extends Fragment implements SensorEventListener {

	enum MapMode {
		add_boundary, add_non_boundary, measure, edit_hole, insert_boundary
	}
	private static final String OSM_MAPNIK_BASE_URL = "http://a.tile.openstreetmap.org/{z}/{x}/{y}.png";
	private static final String OSM_MAPQUEST_BASE_URL = "http://otile1.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png";
	protected static final float MEASURE_Z_INDEX = 3.0f;
	private static final int BOOKMARK_RESULT = 100;

	private MapMode mapMode = MapMode.add_boundary;
	private MapType mapType = MapType.map_provider_google_normal;
	private View mapView;
	private GoogleMap map;
	private EditablePropertyBoundary currentProperty;
	private EditablePropertyBoundary currentHole;
	private List<BasePropertyBoundary> visibleProperties;
	private List<Claim> allClaims;
	private MultiPolygon visiblePropertiesMultiPolygon;
	private ClaimDispatcher claimActivity;
	private ModeDispatcher modeActivity;
	private final static String MAP_TYPE = "__MAP_TYPE__";
	private double snapLat;
	private double snapLon;
	private boolean isRotating = false;
	private boolean isFollowing = false;
	private Marker myLocation;
	private CameraPosition newCameraPosition;
	private boolean adjacenciesReset = false;
	private Marker distanceStart;
	private Marker distanceEnd;
	private Marker distanceMarker;
	private Marker bookmark;
	private Polyline distanceSegment;
	LatLng lastCameraPosition = null;
	private FusedLocationProviderClient fusedLocationClient;
	private LocationCallback locationCallback;
	private LocationRequest locationRequest;
	private TextView txtCoords;
	private Location lastKnownLocation;
	private MenuItem mapToolsMenu;
	private Menu menu;

	// device sensor manager
	private SensorManager mSensorManager;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			claimActivity = (ClaimDispatcher) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement ClaimDispatcher");
		}
		try {
			modeActivity = (ModeDispatcher) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement ModeDispatcher");
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(MAP_TYPE, mapType.toString());
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.claim_map, menu);
		mapToolsMenu = menu.findItem(R.id.action_change_map_mode);

		if (modeActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RO) == 0) {
			menu.removeItem(R.id.action_add_from_gps);
			mapToolsMenu.getSubMenu().removeItem(R.id.action_add_boundary);
			mapToolsMenu.getSubMenu().removeItem(R.id.action_insert_boundary);
			menu.findItem(R.id.action_change_map_mode).getSubMenu().removeItem(R.id.action_add_non_boundary);
			mapToolsMenu.getSubMenu().removeItem(R.id.action_edit_hole);
			mapMode = MapMode.measure;
		}

		Claim claim = Claim.getClaim(claimActivity.getClaimId());
		if (claim != null && !claim.isUploadable()) {
			menu.removeItem(R.id.action_new_picture);
		}

		switch (mapMode) {
			case add_boundary:
				mapToolsMenu.getSubMenu().findItem(R.id.action_add_boundary).setChecked(true);
				break;
			case measure:
				mapToolsMenu.getSubMenu().findItem(R.id.action_measure).setChecked(true);
				break;
			case insert_boundary:
				mapToolsMenu.getSubMenu().findItem(R.id.action_insert_boundary).setChecked(true);
				break;
			case add_non_boundary:
				mapToolsMenu.getSubMenu().findItem(R.id.action_add_non_boundary).setChecked(true);
				break;
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
		stopLocationUpdates();
		// to stop the listener and save battery
		mSensorManager.unregisterListener(this);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	public void reloadBoundary() {
		if (map != null) {
			currentProperty.reload();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		Fragment map = getFragmentManager().findFragmentById(R.id.claim_map_fragment);
		try {
			if (map.isResumed()) {
				FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
				ft.remove(map);
				ft.commit();
			}
		} catch (Exception e) {
		}
		super.onDestroyView();
	}

	private void centerMapOnCurrentProperty(CancelableCallback callback) {
		if (currentProperty.getCenter() != null) {
			final int CLAIM_MAP_SIZE = 800;
			final int CLAIM_MAP_PADDING = 50;
			CameraPosition oldCameraPosition;

			// A property exists for the claim
			// so we center on it
			LatLngBounds llb = currentProperty.getBounds();
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
			if (bookmark != null) {
				bookmark.remove();
			}
			bookmark = createMapBookmarkMarker(new LatLng(bm.getLat(), bm.getLon()), bm.getName());
			try {
				map.moveCamera(CameraUpdateFactory.newLatLngZoom(
						new LatLng(bm.getLat(), bm.getLon()), zoom));
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
		mapView = inflater.inflate(R.layout.fragment_claim_map, container, false);
		setHasOptionsMenu(true);
		txtCoords = (TextView) mapView.findViewById(R.id.txtCoords);
		final ClaimMapFragment that = this;

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
				txtCoords.setText(String.format(getResources().getString(R.string.accuracy), lastKnownLocation.getAccuracy()));
			}
		};

		SupportMapFragment mapViewFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.claim_map_fragment);
		mapViewFragment.getExtendedMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(GoogleMap googleMap) {
				map = googleMap;
				ClusteringSettings settings = new ClusteringSettings();
				settings.clusterOptionsProvider(new OpenTenureClusterOptionsProvider(getResources()));
				settings.addMarkersDynamically(true);
				map.setClustering(settings);

				// Set compass location to the right
				try {
					final ViewGroup parent = (ViewGroup) mapViewFragment.getView().findViewWithTag("GoogleMapMyLocationButton").getParent();
					container.post(new Runnable() {
						@Override
						public void run() {
							try {
								Resources r = getResources();
								//convert our dp margin into pixels
								int marginPixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, r.getDisplayMetrics());
								// Get the map compass view
								View mapCompass = parent.getChildAt(4);

								// create layoutParams, giving it our wanted width and height(important, by default the width is "match parent")
								RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(mapCompass.getHeight(),mapCompass.getHeight());
								// position on top right
								rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
								rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
								rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
								rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
								//give compass margin
								rlp.setMargins(marginPixels, marginPixels, marginPixels, marginPixels);
								mapCompass.setLayoutParams(rlp);
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					});
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				map.setInfoWindowAdapter(new PopupAdapter(inflater));

				map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
					@Override
					public void onCameraIdle() {
						hideVisibleProperties();
						reloadVisibleProperties(false);
						showVisibleProperties();
						currentProperty.redrawProperty();
						currentProperty.refreshMarkerEditControls();
						lastCameraPosition = null;
						newCameraPosition = map.getCameraPosition();
						  /*if (!adjacenciesReset) {
							  currentProperty.resetAdjacency(visibleProperties);
							  adjacenciesReset = true;
						  }*/
					}
				});

				map.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
					@Override
					public void onCameraMove() {
						LatLng newCameraPosition = map.getCameraPosition().target;
						if (currentProperty.hasSelectedMarker()) {
							if (lastCameraPosition != null) {
								// Calculate shift
								LatLng selectedMarkerPosition = currentProperty.getSelectedMarkerPosition();
								if (selectedMarkerPosition != null) {
									double latDiff = newCameraPosition.latitude - lastCameraPosition.latitude;
									double lngDiff = newCameraPosition.longitude - lastCameraPosition.longitude;

									LatLng newMarkerPosition = new LatLng(selectedMarkerPosition.latitude + latDiff, selectedMarkerPosition.longitude + lngDiff);
									currentProperty.updateSelectedMarker(newMarkerPosition);

									// Snapping option
									/*
									PointPairDistance ppd = new PointPairDistance();
									DistanceToPoint.computeDistance(visiblePropertiesMultiPolygon, new Coordinate(newMarkerPosition.longitude, newMarkerPosition.latitude), ppd);

									if (ppd.getDistance() < BasePropertyBoundary.SNAP_THRESHOLD) {
										snapLat = ppd.getCoordinate(0).y;
										snapLon = ppd.getCoordinate(0).x;
										currentProperty.updateSelectedMarker(new LatLng(snapLat, snapLon));
									} else {
										snapLat = 0.0;
										snapLon = 0.0;
										currentProperty.updateSelectedMarker(newMarkerPosition);
									}*/
								}
							}
						}
						lastCameraPosition = newCameraPosition;
					}
				});

				if (savedInstanceState != null && savedInstanceState.getString(MAP_TYPE) != null) {
					// probably an orientation change don't move the view but
					// restore the current type of the map
					mapType = MapType.valueOf(savedInstanceState.getString(MAP_TYPE));
					loadLayers(true);
				} else {
					// restore the latest map type used on the main map
					try {
						mapType = MapType.valueOf(Configuration.getConfigurationValue(MainMapFragment.MAIN_MAP_TYPE));
					} catch (Exception e) {
						mapType = MapType.map_provider_google_normal;
					}
					// don't draw properties since we might not have loaded them yet
					loadLayers(false);
				}

				hideVisibleProperties();
				currentProperty = new EditablePropertyBoundary(mapView.getContext(), map, claimActivity.getClaimId(), claimActivity, visibleProperties, modeActivity.getMode());

				centerMapOnCurrentProperty(null);
				reloadVisibleProperties(true);
				showVisibleProperties();
				drawAreaOfInterest();

				if (modeActivity.getMode().compareTo(ModeDispatcher.Mode.MODE_RW) == 0) {
					// Allow adding, removing and dragging markers
					map.setOnMapLongClickListener(new OnMapLongClickListener() {
						@Override
						public void onMapLongClick(final LatLng position) {
							currentProperty.addMarker(position, mapMode);
						}
					});

					map.setOnMarkerDragListener(new OnMarkerDragListener() {
						@Override
						public void onMarkerDrag(Marker mark) {
							PointPairDistance ppd = new PointPairDistance();
							DistanceToPoint.computeDistance(visiblePropertiesMultiPolygon, new Coordinate(mark.getPosition().longitude, mark.getPosition().latitude), ppd);

							if (ppd.getDistance() < BasePropertyBoundary.SNAP_THRESHOLD) {
								snapLat = ppd.getCoordinate(0).y;
								snapLon = ppd.getCoordinate(0).x;
								mark.setPosition(new LatLng(snapLat, snapLon));
							} else {
								snapLat = 0.0;
								snapLon = 0.0;
							}
							currentProperty.onMarkerDrag(mark);
						}

						@Override
						public void onMarkerDragEnd(Marker mark) {
							if (snapLat != 0.0 && snapLon != 0.0) {
								mark.setPosition(new LatLng(snapLat, snapLon));
							}
							currentProperty.onMarkerDragEnd(mark);
						}

						@Override
						public void onMarkerDragStart(Marker mark) {
							currentProperty.onMarkerDragStart(mark);
						}

					});

				}

				map.setOnMarkerClickListener(new OnMarkerClickListener() {
					@Override
					public boolean onMarkerClick(final Marker mark) {
						switch (mapMode) {
							case add_boundary:
							case insert_boundary:
							case add_non_boundary:
							case edit_hole:
								return currentProperty.handleMarkerClick(mark, mapMode);
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
							case insert_boundary:
							case add_non_boundary:
							case edit_hole:
								break;
							case measure:
								cancelDistance();
								break;
							default:
								break;
						}
					}
				});

				mSensorManager = (SensorManager) mapView.getContext().getSystemService(Context.SENSOR_SERVICE);
				currentProperty.calculateGeometry(true);
				currentProperty.redrawProperty();
			}
		});

		ImageButton btnLayers = mapView.findViewById(R.id.btnLayers);
		btnLayers.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent mapLayersActivity = new Intent(getContext(), MapLayersActivity.class);
				startActivityForResult(mapLayersActivity, MapLayersActivity.REQUEST_CODE);
			}
		});

		return mapView;
	}

	private boolean handleDistanceMarkerClick(Marker mark) {
		hideDistance();

		if ((distanceStart != null && mark.equals(distanceStart))
				|| (distanceEnd != null && mark.equals(distanceEnd))) {
			// Second click on a selected marker: stop measuring
			colorDistanceMarkers(false);
			distanceStart = null;
			distanceEnd = null;
		} else if (distanceEnd != null && distanceStart == null) {
			// measureStart selected
			distanceStart = mark;
			colorDistanceMarkers(true);
		} else if (distanceStart != null && distanceEnd == null) {
			// measureEnd selected
			distanceEnd = mark;
			colorDistanceMarkers(true);
		} else if (distanceStart != null && distanceEnd != null) {
			// new measureEnd selected
			colorDistanceMarkers(false);
			distanceStart = distanceEnd;
			distanceEnd = mark;
			colorDistanceMarkers(true);
		} else if (distanceStart == null && distanceEnd == null) {
			// measureStart selected
			distanceStart = mark;
			distanceStart.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ot_orange_marker));
			colorDistanceMarkers(true);
		}
		if (distanceStart != null && distanceEnd != null) {
			showDistance();
		}
		return true;
	}

	private void colorDistanceMarkers(boolean selected) {
		BitmapDescriptor icon;
		if(selected) {
			icon = BitmapDescriptorFactory.fromResource(R.drawable.ot_orange_marker);
		} else {
			icon = BitmapDescriptorFactory.fromResource(R.drawable.ot_blue_marker);
		}

		if(distanceStart != null) {
			distanceStart.setIcon(icon);
		}
		if(distanceEnd != null) {
			distanceEnd.setIcon(icon);
		}
	}

	private void showDistance() {
		if (distanceStart != null && distanceEnd != null) {
			double distance = SphericalUtil.computeDistanceBetween(distanceStart.getPosition(), distanceEnd.getPosition());

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

			distanceMarker = map.addMarker(
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
		colorDistanceMarkers(false);
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

	private void reloadVisibleProperties(boolean updateArea) {

		LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
		Polygon boundsPoly = getPolygon(bounds);

		if (allClaims == null) {
			allClaims = Claim.getSimplifiedClaimsForMap();
		}

		visibleProperties = new ArrayList<BasePropertyBoundary>();

		for (Claim claim : allClaims) {
			if (!claim.getClaimId().equalsIgnoreCase(claimActivity.getClaimId())) {
				BasePropertyBoundary bpb = new BasePropertyBoundary(mapView.getContext(), map, claim.getClaimId(), updateArea);
				Polygon claimPoly = bpb.getPolygon();
				if (claimPoly != null && claimPoly.intersects(boundsPoly)) {
					visibleProperties.add(bpb);
				}
			}
		}
		currentProperty.setOtherProperties(visibleProperties);

		List<Polygon> visiblePropertiesPolygonList = new ArrayList<Polygon>();

		for (BasePropertyBoundary visibleProperty : visibleProperties) {
			if (visibleProperty.getVertices() != null && visibleProperty.getVertices().size() > 0) {
				visiblePropertiesPolygonList.add(visibleProperty.getPolygon());
			}
		}
		Polygon[] visiblePropertiesPolygons = new Polygon[visiblePropertiesPolygonList.size()];
		visiblePropertiesPolygonList.toArray(visiblePropertiesPolygons);

		GeometryFactory gf = new GeometryFactory();
		visiblePropertiesMultiPolygon = gf.createMultiPolygon(visiblePropertiesPolygons);
		visiblePropertiesMultiPolygon.setSRID(Constants.SRID);
	}

	private void loadLayers(boolean forceDraw) {
		for (TileOverlay tiles : map.getTileOverlays()) {
			tiles.remove();
		}

		boolean redrawClaims = false;

		// Add base layer
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
				map.addTileOverlay(new TileOverlayOptions().tileProvider(mapNikTileProvider));
				redrawClaims = true;
				break;
			case map_provider_osm_mapquest:
				OsmTileProvider mapQuestTileProvider = new OsmTileProvider(256, 256, OSM_MAPQUEST_BASE_URL);
				map.setMapType(GoogleMap.MAP_TYPE_NONE);
				map.addTileOverlay(new TileOverlayOptions().tileProvider(mapQuestTileProvider));
				redrawClaims = true;
				break;
			case map_provider_local_tiles:
				map.setMapType(GoogleMap.MAP_TYPE_NONE);
				map.addTileOverlay(new TileOverlayOptions().tileProvider(new LocalMapTileProvider()));
				redrawClaims = true;
				break;
			case map_provider_geoserver:
				map.setMapType(GoogleMap.MAP_TYPE_NONE);
				map.addTileOverlay(new TileOverlayOptions().tileProvider(new WmsMapTileProvider(256, 256)));
				redrawClaims = true;
				break;
			case map_provider_empty:
				map.setMapType(GoogleMap.MAP_TYPE_NONE);
				redrawClaims = true;
				break;
			default:
				break;
		}

		// Add user layers
		List<UserLayer> userLayers = UserLayer.getUserLayers(false);
		if (userLayers != null && userLayers.size() > 0) {
			for (UserLayer userLayer : userLayers) {
				if (userLayer.getEnabled()) {
					File mbtileFile = new File(userLayer.getFilePath());
					if (mbtileFile.exists()) {
						try {
							TileOverlayOptions opts = new TileOverlayOptions();
							opts.tileProvider(new MbTilesProvider(mbtileFile));
							map.addTileOverlay(opts);
							redrawClaims = true;
						} catch (Exception ex) {
							Log.d("UserLayer", "Failed to add user map layer. " + ex.getMessage());
							ex.printStackTrace();
						}
					}
				}
			}
		}

		if (redrawClaims && forceDraw) {
			redrawProperties();
		}
	}

	private void redrawProperties() {
		currentProperty.redrawProperty();
		redrawVisibleProperties();
		drawAreaOfInterest();

	}

	private void showVisibleProperties() {
		if (visibleProperties != null) {
			for (BasePropertyBoundary visibleProperty : visibleProperties) {
				visibleProperty.showProperty();
			}
		}

	}

	private void redrawVisibleProperties() {
		hideVisibleProperties();
		showVisibleProperties();
	}

	private void drawAreaOfInterest() {
		CommunityArea area = new CommunityArea(map);
		area.drawInterestArea();
	}

	private void hideVisibleProperties() {
		if (visibleProperties != null) {
			for (BasePropertyBoundary visibleProperty : visibleProperties) {
				visibleProperty.hideProperty();
			}
		}
	}

	private void uncheckMapToolsMenus() {
		if(mapToolsMenu != null){
			MenuItem addBoundary = mapToolsMenu.getSubMenu().findItem(R.id.action_add_boundary);
			MenuItem insertBoundary = mapToolsMenu.getSubMenu().findItem(R.id.action_insert_boundary);
			MenuItem measure = mapToolsMenu.getSubMenu().findItem(R.id.action_measure);

			if(addBoundary != null) {
				addBoundary.setChecked(false);
			}
			if(insertBoundary != null) {
				insertBoundary.setChecked(false);
			}
			if(measure != null) {
				measure.setChecked(false);
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
				mapMode = MapMode.add_boundary;
				currentProperty.deselect();
				uncheckMapToolsMenus();
				item.setChecked(true);
				cancelDistance();
				return true;
			case R.id.action_insert_boundary:
				mapMode = MapMode.insert_boundary;
				currentProperty.deselect();
				uncheckMapToolsMenus();
				item.setChecked(true);
				cancelDistance();
				return true;
			case R.id.action_add_non_boundary:
				mapMode = MapMode.add_non_boundary;
				currentProperty.deselect();
				cancelDistance();
				return true;
			case R.id.action_measure:
				mapMode = MapMode.measure;
				currentProperty.deselect();
				uncheckMapToolsMenus();
				item.setChecked(true);
				cancelDistance();
				return true;
			case R.id.action_edit_hole:
				mapMode = MapMode.edit_hole;
				currentProperty.deselect();
				cancelDistance();
				List<List<HoleVertex>> holesVertices = currentProperty.getHolesVertices();
				final CharSequence[] options;
				if (holesVertices != null) {
					options = new CharSequence[holesVertices.size() + 1];
					for (int i = 0; i < holesVertices.size(); i++) {
						options[i] = i + "";
					}
				} else {
					options = new CharSequence[1];
				}
				options[options.length - 1] = getResources().getString(R.string.new_hole);
				AlertDialog.Builder selectHoleDialog = new AlertDialog.Builder(
						mapView.getContext()).setTitle(getResources().getString(R.string.title_select_hole)).setItems(options, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(
							DialogInterface dialog,
							int which) {
						if (which == options.length - 1) {
							currentProperty.setSelectedHoleNumber(-1);
						} else {
							currentProperty.setSelectedHoleNumber(which);
						}
					}
				});
				selectHoleDialog.show();
				return true;
			case R.id.action_export_geo:
				currentProperty.saveGeometry();
				toast = Toast.makeText(mapView.getContext(), R.string.message_exported, Toast.LENGTH_SHORT);
				toast.show();
				return true;
			case R.id.action_new_picture:
				centerMapOnCurrentProperty(new CancelableCallback() {

					@Override
					public void onFinish() {
						currentProperty.saveSnapshot();
					}

					@Override
					public void onCancel() {
					}
				});
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
						Toast.makeText(getActivity().getBaseContext(), R.string.check_location_service, Toast.LENGTH_LONG).show();
					}
				}
				return true;
			case R.id.action_add_from_gps:
				LatLng newLocation = null;
				if(lastKnownLocation != null){
					newLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
				}

				if (newLocation != null && newLocation.latitude != 0.0 && newLocation.longitude != 0.0) {
					currentProperty.addMarker(newLocation, newLocation, mapMode);
				} else {
					Toast.makeText(getActivity().getBaseContext(), R.string.check_location_service, Toast.LENGTH_LONG).show();
				}
				return true;
			case R.id.action_rotate:
				if (!isRotating && lastKnownLocation != null) {
					menu.findItem(R.id.action_rotate).setVisible(false);
					menu.findItem(R.id.action_stop_rotating).setVisible(true);
					mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_UI);
					isRotating = true;
				} else {
					Toast.makeText(getActivity().getBaseContext(), R.string.check_location_service, Toast.LENGTH_LONG).show();
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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (data != null) { // No selection has been done
			switch (requestCode) {
				case BOOKMARK_RESULT:
					String bookmarkId = data.getStringExtra(SelectBookmarkActivity.BOOKMARK_ID_KEY);
					centerMapOnBookmark(bookmarkId);
					break;
			}
		}
		if(requestCode == MapLayersActivity.REQUEST_CODE && resultCode == MapLayersActivity.RESPONSE_CODE){
			try {
				mapType = MapType.valueOf(Configuration.getConfigurationValue(MainMapFragment.MAIN_MAP_TYPE));
			} catch (Exception e) {
				mapType = MapType.map_provider_google_normal;
			}
			loadLayers(true);
		}
		super.onActivityResult(requestCode, resultCode, data);
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