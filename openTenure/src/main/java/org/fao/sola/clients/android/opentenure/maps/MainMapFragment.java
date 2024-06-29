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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fao.sola.clients.android.opentenure.MapLabel;
import org.fao.sola.clients.android.opentenure.OpenTenure;
import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.maps.OfflineTilesProvider.TilesProviderType;
import org.fao.sola.clients.android.opentenure.model.Bookmark;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.Configuration;
import org.fao.sola.clients.android.opentenure.model.Task;
import org.fao.sola.clients.android.opentenure.model.Tile;
import org.fao.sola.clients.android.opentenure.model.UserLayer;
import org.fao.sola.clients.android.opentenure.network.GetAllClaimsTask;
import org.fao.sola.clients.android.opentenure.tools.StringUtility;

import com.androidmapsextensions.ClusterGroup;
import com.androidmapsextensions.ClusteringSettings;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.GoogleMap.OnMapLongClickListener;
import com.androidmapsextensions.GoogleMap.OnMarkerClickListener;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.OnMapReadyCallback;
import com.androidmapsextensions.SupportMapFragment;
import com.androidmapsextensions.TileOverlay;
import com.androidmapsextensions.TileOverlayOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainMapFragment extends SupportMapFragment {

	public static final String MAIN_MAP_ZOOM = "__MAIN_MAP_ZOOM__";
	public static final String MAIN_MAP_LATITUDE = "__MAIN_MAP_LATITUDE__";
	public static final String MAIN_MAP_LONGITUDE = "__MAIN_MAP_LONGITUDE__";
	public static final String MAIN_MAP_TYPE = "__MAIN_MAP_PROVIDER__";
	private static final int MAP_LABEL_FONT_SIZE = 16;
	public static final float MAX_ZOOM_LEVELS_TO_DOWNLOAD = 3.0f;
	private static final int MAX_TILES_IN_DOWNLOAD_QUEUE = 1000;
	private static final String OSM_MAPNIK_BASE_URL = "http://a.tile.openstreetmap.org/{z}/{x}/{y}.png";
	private static final String OSM_MAPQUEST_BASE_URL = "http://otile1.mqcdn.com/tiles/1.0.0/osm/{z}/{x}/{y}.png";
	private Map<Marker, Bookmark> mapBookmarksMap = new HashMap<Marker, Bookmark>();

	public enum MapType {
		map_provider_google_normal, map_provider_google_satellite, map_provider_google_hybrid, map_provider_google_terrain,
		map_provider_osm_mapnik, map_provider_osm_mapquest, map_provider_local_tiles, map_provider_geoserver, map_provider_empty
	};

	private MapType mapType = MapType.map_provider_google_normal;
	private View mapView;
	private GoogleMap map;
	private LocationHelper lh;
	private List<BasePropertyBoundary> visibleProperties;
	private List<Claim> allClaims;
	private MultiPolygon visiblePropertiesMultiPolygon;
	private boolean isFollowing = false;
	private Marker myLocation;
	private Marker remove;
	private Marker cancel;
	private Marker selectedMarker;
	private CommunityArea communityArea;
	private MenuItem menuDownloadTiles;

	private boolean handleMarkerEditClick(Marker mark) {
		if (remove == null || cancel == null) {
			return false;
		}
		try {
			if (mark.equals(remove)) {
				return removeSelectedMarker();
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

	private boolean removeSelectedMarker() {

		if (mapBookmarksMap.containsKey(selectedMarker)) {
			return removeSelectedMapBookmark();
		}
		return false;

	}

	private boolean removeSelectedMapBookmark() {
		Bookmark mb = mapBookmarksMap.remove(selectedMarker);
		mapBookmarksMap.remove(selectedMarker);
		selectedMarker.remove();
		mb.delete();
		hideMarkerEditControls();
		selectedMarker = null;
		return true;
	}

	private void hideMarkerEditControls() {
		if (remove != null) {
			remove.remove();
			remove = null;
		}
		if (cancel != null) {
			cancel.remove();
			cancel = null;
		}

	}

	public void deselect() {
		hideMarkerEditControls();
		if (selectedMarker != null) {
			if (mapBookmarksMap.containsKey(selectedMarker)) {
				selectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.map_bookmark));
			}
			selectedMarker = null;
		}
	}

	private LocationListener myLocationListener = new LocationListener() {

		public void onLocationChanged(Location location) {
			if (isFollowing && myLocation != null) {
				myLocation.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
			}
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onProviderDisabled(String provider) {
		}
	};

	private void showMarkerEditControls() {

		hideMarkerEditControls();

		Projection projection = map.getProjection();
		Point markerScreenPosition = projection.toScreenLocation(selectedMarker.getPosition());

		Bitmap bmp = BitmapFactory
				.decodeResource(mapView.getResources(), R.drawable.map_bookmark_selected);
		int markerHeight = bmp.getHeight();
		int markerWidth = bmp.getWidth();

		remove = map.addMarker(new MarkerOptions()
				.position(projection.fromScreenLocation(getControlRemovePosition(markerScreenPosition, markerWidth, markerHeight)))
				.anchor(0.5f, 0.5f)
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.ic_menu_close_clear_cancel)));
		remove.setClusterGroup(ClusterGroup.NOT_CLUSTERED);
		cancel = map.addMarker(new MarkerOptions()
				.position(projection.fromScreenLocation(getControlCancelPosition(markerScreenPosition, markerWidth, markerHeight)))
				.anchor(0.5f, 0.5f)
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.ic_menu_block)));
		cancel.setClusterGroup(ClusterGroup.NOT_CLUSTERED);
	}

	private Point getControlRemovePosition(Point markerScreenPosition, int markerWidth, int markerHeight){
		return new Point(markerScreenPosition.x - markerWidth, markerScreenPosition.y + markerHeight);
	}

	private Point getControlCancelPosition(Point markerScreenPosition, int markerWidth, int markerHeight){
		return new Point(markerScreenPosition.x + markerWidth, markerScreenPosition.y + markerHeight);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
	}

	private void showHideTilesDownload() {
		if (map != null && menuDownloadTiles != null) {
			float currentZoomLevel = map.getCameraPosition().zoom;
			float maxSupportedZoomLevel = map.getMaxZoomLevel();
			if (currentZoomLevel >= (maxSupportedZoomLevel - MAX_ZOOM_LEVELS_TO_DOWNLOAD) && OpenTenureApplication.getInstance().isInitialized()) {
				menuDownloadTiles.setVisible(true);
			} else {
				menuDownloadTiles.setVisible(false);
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(mapView != null){
			return mapView;
		}
		super.onCreateView(inflater, container, savedInstanceState);
		mapView = inflater.inflate(R.layout.main_map, container, false);
		setHasOptionsMenu(true);

		SupportMapFragment mapViewFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.main_map_fragment);
		mapViewFragment.getExtendedMapAsync(new OnMapReadyCallback() {
			@Override
			public void onMapReady(GoogleMap googleMap) {
				map = googleMap;
				ClusteringSettings settings = new ClusteringSettings();
				settings.clusterOptionsProvider(new OpenTenureClusterOptionsProvider(getResources()));
				settings.addMarkersDynamically(true);
				try {
					map.setClustering(settings);
				} catch (Throwable i) {
					final int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(OpenTenureApplication.getContext());
					if (status != ConnectionResult.SUCCESS) {
						Log.d(this.getClass().getName(), GooglePlayServicesUtil.getErrorString(status));

						// ask user to update google play services.
						Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, getActivity(), 1);
						dialog.show();

						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						System.exit(0);
					}
				}

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

				reloadVisibleProperties();

				lh = new LocationHelper((LocationManager) getActivity().getBaseContext().getSystemService(Context.LOCATION_SERVICE));
				lh.start();

				map.setInfoWindowAdapter(new PopupAdapter(getLayoutInflater()));
				map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
					@Override
					public void onCameraIdle() {
						showHideTilesDownload();
						storeCameraPosition(map.getCameraPosition());
						reloadVisibleProperties();
					}
				});

				try {
					mapType = MapType.valueOf(Configuration.getConfigurationValue(MainMapFragment.MAIN_MAP_TYPE));
				} catch (Exception e) {
					mapType = MapType.map_provider_empty;
				}
				if (savedInstanceState != null && savedInstanceState.getString(MAIN_MAP_TYPE) != null) {
					// probably an orientation change don't move the view but
					// restore the current type of the map
					mapType = MapType.valueOf(savedInstanceState.getString(MAIN_MAP_TYPE));
				} else {
					// restore the latest map type used on the main map
					try {
						mapType = MapType.valueOf(Configuration.getConfigurationValue(MainMapFragment.MAIN_MAP_TYPE));
					} catch (Exception e) {
						mapType = MapType.map_provider_empty;
					}
				}

				loadLayers();

				String zoom = Configuration.getConfigurationValue(MainMapFragment.MAIN_MAP_ZOOM);
				String latitude = Configuration.getConfigurationValue(MainMapFragment.MAIN_MAP_LATITUDE);
				String longitude = Configuration.getConfigurationValue(MainMapFragment.MAIN_MAP_LONGITUDE);

				// If we previously used the map
				if (zoom != null && latitude != null && longitude != null) {
					try {

						// Let's start from where we left it
						map.moveCamera(CameraUpdateFactory.newLatLngZoom(
								new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude)),
								Float.parseFloat(zoom)));
					} catch (Exception e) {
					}
				} else {
					if (OpenTenureApplication.getInstance().isInitialized()) {

						LatLngBounds.Builder bounds;
						// setup map

						bounds = new LatLngBounds.Builder();
						// Get vertices of the community area
						List<LatLng> K = CommunityArea.getPoints();

						for (LatLng cn : K) {
							// Make sure that the vertices are in the displayed area
							bounds.include(cn);

						}
						// set bounds with all the map points
						map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 400, 400, 10));
					}
				}

				map.setInfoWindowAdapter(new PopupAdapter(getLayoutInflater()));
				map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
					@Override
					public void onCameraIdle() {
						getActivity().invalidateOptionsMenu();
						storeCameraPosition(map.getCameraPosition());
						reloadVisibleProperties();
					}
				});

				redrawVisibleProperties();

				map.setOnMapLongClickListener(new OnMapLongClickListener() {
					@Override
					public void onMapLongClick(final LatLng position) {
						addMapBookmark(position);
					}
				});

				map.setOnMarkerClickListener(new OnMarkerClickListener() {
					@Override
					public boolean onMarkerClick(final Marker mark) {
						return handleMarkerClick(mark);
					}
				});

				List<Bookmark> allBookmarks = Bookmark.getAllBookmarks();

				for(Bookmark bm : allBookmarks){
					mapBookmarksMap.put(createMapBookmarkMarker(new LatLng(bm.getLat(), bm.getLon()), bm.getName()),bm);
				}
			}
		});

		OpenTenureApplication.setMapFragment(this);
		OpenTenureApplication.setActivity(getActivity());

		ImageButton btnLayers = mapView.findViewById(R.id.btnLayers);
		btnLayers.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View view) {
				Intent mapLayersActivity = new Intent(getContext(), MapLayersActivity.class);
				startActivityForResult(mapLayersActivity, MapLayersActivity.REQUEST_CODE);
			}
		});

		return mapView;
	}

	private boolean handleMarkerClick(final Marker mark) {
		if (handleMarkerEditClick(mark)) {
			return true;
		}else if(handleMapBookmarkMarkerClick(mark)){
			return true;
		} else {
			return handleClick(mark);
		}
	}

	private boolean handleMapBookmarkMarkerClick(final Marker mark){
		if (mapBookmarksMap.containsKey(mark)) {
			deselect();
			selectedMarker = mark;
			selectedMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.map_bookmark_selected));
			selectedMarker.showInfoWindow();
			showMarkerEditControls();
			return true;
		}
		return false;

	}

	private boolean handleClick(Marker mark) {
		try {
			deselect();
			if (!mark.isInfoWindowShown()) {
				mark.showInfoWindow();
				return true;
			}
		} catch (UnsupportedOperationException e) {
			// Clustered markers have no ID and may throw this
		}
		// Let the flow continue in order to center the map around selected
		// marker and display info window
		return false;
	}

	private void addMapBookmark(final LatLng position) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(mapView.getContext());
		dialog.setTitle(R.string.message_add_map_bookmark_marker);
		dialog.setMessage("Lon: " + position.longitude + ", lat: " + position.latitude);
		dialog.setPositiveButton(R.string.confirm, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				AlertDialog.Builder locationDescriptionDialog = new AlertDialog.Builder(mapView.getContext());
				locationDescriptionDialog.setTitle(R.string.title_add_map_bookmark);
				final EditText bookmarkDescriptionInput = new EditText(mapView.getContext());
				bookmarkDescriptionInput.setInputType(InputType.TYPE_CLASS_TEXT);
				locationDescriptionDialog.setView(bookmarkDescriptionInput);
				locationDescriptionDialog
						.setMessage(mapView.getContext().getResources().getString(R.string.message_enter_description));

				locationDescriptionDialog.setPositiveButton(R.string.confirm, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						String bookmarkDescription = bookmarkDescriptionInput.getText().toString();
						addMapBookmark(position, bookmarkDescription);
					}
				});
				locationDescriptionDialog.setNegativeButton(R.string.cancel, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				});

				locationDescriptionDialog.show();

			}
		});
		dialog.setNegativeButton(R.string.cancel, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		});

		dialog.show();
	}

	private void addMapBookmark(LatLng position, String name) {

		Marker mark = createMapBookmarkMarker(position, name);
		Bookmark mb = new Bookmark();
		mb.setName(name);
		mb.setLat(position.latitude);
		mb.setLon(position.longitude);
		mb.create();
		mapBookmarksMap.put(mark, mb);
	}

	protected Marker createMapBookmarkMarker(LatLng position, String description) {
		Marker marker;
		marker = map.addMarker(new MarkerOptions().position(position).title(description)
				.clusterGroup(ClusterGroup.NOT_CLUSTERED)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_bookmark)));
		return marker;
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

	private void hideVisibleProperties() {

		if (visibleProperties != null) {
			for (BasePropertyBoundary visibleProperty : visibleProperties) {
				visibleProperty.hideProperty();
			}
		}
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	@Override
	public void onDestroyView() {
		if(map != null) {
			lh.stop();
		}
		Fragment map = getFragmentManager().findFragmentById(R.id.main_map_fragment);
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

	@Override
	public void onResume() {
		super.onResume();
		refreshMap();
		if(map != null) {
			lh.hurryUp();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if(map != null) {
			lh.slowDown();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		if(map != null) {
			lh.stop();
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.map, menu);
		boolean isInitialized = OpenTenureApplication.getInstance().isInitialized();

		menuDownloadTiles = menu.findItem(R.id.action_download_tiles);
		MenuItem menuZoomToCommunity = menu.findItem(R.id.action_center_community_area);
		MenuItem menuDownloadClaims = menu.findItem(R.id.action_download_claims);

		if(menuDownloadTiles != null && !isInitialized){
			menuDownloadTiles.setVisible(false);
		} else {
			showHideTilesDownload();
		}

		if(menuZoomToCommunity != null){
			menuZoomToCommunity.setVisible(isInitialized);
		}
		if(menuDownloadClaims != null){
			menuDownloadClaims.setVisible(isInitialized);
		}

		super.onCreateOptionsMenu(menu, inflater);
	}

	public void loadLayers() {
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
		if(userLayers != null && userLayers.size() > 0) {
			for(UserLayer userLayer : userLayers) {
				if(userLayer.getEnabled()){
					File mbtileFile = new File(userLayer.getFilePath());
					if(mbtileFile.exists()) {
						try {
							TileOverlayOptions opts = new TileOverlayOptions();
							opts.tileProvider(new MbTilesProvider(mbtileFile));
							map.addTileOverlay(opts);
							redrawClaims = true;
						} catch (Exception ex){
							Log.d("UserLayer","Failed to add user map layer. " + ex.getMessage());
							ex.printStackTrace();
						}
					}
				}
			}
		}

		if(redrawClaims) {
			redrawVisibleProperties();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(MAIN_MAP_TYPE, mapType.toString());
		super.onSaveInstanceState(outState);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_center_and_follow:
				if (isFollowing) {
					isFollowing = false;
					myLocation.remove();
					myLocation = null;
					lh.setCustomListener(null);
				} else {
					LatLng currentLocation = lh.getLastKnownLocation();

					if (currentLocation != null && currentLocation.latitude != 0.0 && currentLocation.longitude != 0.0) {
						map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18), 1000, null);
						myLocation = map.addMarker(new MarkerOptions().position(currentLocation).anchor(0.5f, 0.5f)
								.title(mapView.getContext().getResources().getString(R.string.title_i_m_here))
								.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_menu_mylocation)));
						myLocation.setClusterGroup(Constants.MY_LOCATION_MARKERS_GROUP);
						lh.setCustomListener(myLocationListener);
						isFollowing = true;

					} else {
						Toast.makeText(getActivity().getBaseContext(), R.string.check_location_service, Toast.LENGTH_LONG)
								.show();
					}
				}
				return true;

			case R.id.action_center_community_area:
				if (checkInitialized()) {
					boundCameraToInterestArea();
				}
				return true;

			case R.id.action_download_claims:

				if (!OpenTenureApplication.getInstance().isInitialized()) {
					Toast toast;
					String toastMessage = String.format(OpenTenureApplication.getContext().getString(R.string.message_app_not_yet_initialized));

					toast = Toast.makeText(OpenTenureApplication.getContext(), toastMessage, Toast.LENGTH_LONG);
					toast.show();

					return true;
				}

				if (!OpenTenureApplication.isLoggedin()) {
					Toast toast = Toast.makeText(OpenTenureApplication.getContext(), R.string.message_login_before, Toast.LENGTH_LONG);
					toast.show();
					return true;
				} else {
					if (OpenTenureApplication.getInstance().isConnectedWifi(mapView.getContext())) {
						downloadClaims();
					} else {
						// Avoid to automatically download claims over mobile data
						AlertDialog.Builder confirmDownloadBuilder = new AlertDialog.Builder(mapView.getContext());
						confirmDownloadBuilder.setTitle(R.string.title_confirm_data_transfer);
						confirmDownloadBuilder.setMessage(getResources().getString(R.string.message_data_over_mobile));

						confirmDownloadBuilder.setPositiveButton(R.string.confirm, new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								downloadClaims();
							}
						});
						confirmDownloadBuilder.setNegativeButton(R.string.cancel, new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						});

						final AlertDialog confirmDownloadDialog = confirmDownloadBuilder.create();
						confirmDownloadDialog.show();
					}
					return true;
				}
			case R.id.action_download_tiles:

				if (!OpenTenureApplication.getInstance().isInitialized()) {
					Toast toast;
					String toastMessage = String
							.format(OpenTenureApplication.getContext().getString(R.string.message_app_not_yet_initialized));

					toast = Toast.makeText(OpenTenureApplication.getContext(), toastMessage, Toast.LENGTH_LONG);
					toast.show();

					return true;
				}

				if (!OpenTenureApplication.getInstance().isOnline()) {
					Toast.makeText(mapView.getContext(),
							mapView.getContext().getResources().getString(R.string.error_connection), Toast.LENGTH_LONG)
							.show();

					return true;
				} else {
					if (OpenTenureApplication.getInstance().isConnectedWifi(mapView.getContext())) {
						downloadTiles();
					} else {
						// Avoid to automatically download tiles over mobile data
						AlertDialog.Builder confirmDownloadBuilder = new AlertDialog.Builder(mapView.getContext());
						confirmDownloadBuilder.setTitle(R.string.title_confirm_data_transfer);
						confirmDownloadBuilder.setMessage(getResources().getString(R.string.message_data_over_mobile));

						confirmDownloadBuilder.setPositiveButton(R.string.confirm, new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								downloadTiles();
							}
						});
						confirmDownloadBuilder.setNegativeButton(R.string.cancel, new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
							}
						});

						final AlertDialog confirmDownloadDialog = confirmDownloadBuilder.create();
						confirmDownloadDialog.show();
					}
				}
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private boolean checkInitialized() {
		if (!OpenTenureApplication.getInstance().isInitialized()) {
			Toast toast;
			String toastMessage = String.format(OpenTenureApplication.getContext().getString(R.string.message_app_not_yet_initialized));

			toast = Toast.makeText(OpenTenureApplication.getContext(), toastMessage, Toast.LENGTH_LONG);
			toast.show();

			return false;
		}
		return true;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == MapLayersActivity.REQUEST_CODE && resultCode == MapLayersActivity.RESPONSE_CODE){
			try {
				mapType = MapType.valueOf(Configuration.getConfigurationValue(MainMapFragment.MAIN_MAP_TYPE));
			} catch (Exception e) {
				mapType = MapType.map_provider_google_normal;
			}
			loadLayers();
		}
	}

	private void downloadClaims() {
		ProgressBar bar = (ProgressBar) mapView.findViewById(R.id.progress_bar);

		bar.setVisibility(View.VISIBLE);
		bar.setProgress(0);

		TextView label = (TextView) mapView.findViewById(R.id.download_claim_label);
		label.setVisibility(View.VISIBLE);

		OpenTenureApplication.setMapFragment(this);

		LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;

		GetAllClaimsTask task = new GetAllClaimsTask();
		task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, bounds, mapView);
	}

	private void downloadTiles() {
		float currentZoomLevel = map.getCameraPosition().zoom;
		float maxSupportedZoomLevel = map.getMaxZoomLevel();

		if (currentZoomLevel >= (maxSupportedZoomLevel - MAX_ZOOM_LEVELS_TO_DOWNLOAD)) {

			int tilesToDownload = Tile.getTilesToDownload();

			LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;

			List<Tile> tiles = null;
			OfflineTilesProvider provider = null;

			String currentTilesProvider = "geoserver";
			if(OpenTenureApplication.getInstance().getProject() != null && !StringUtility.isEmpty(OpenTenureApplication.getInstance().getProject().getTilesServerType())){
				currentTilesProvider = OpenTenureApplication.getInstance().getProject().getTilesServerType().toLowerCase();
			}

			if (currentTilesProvider.equalsIgnoreCase(TilesProviderType.geoserver.toString())) {
				provider = new OfflineWmsMapTileProvider(OfflineTilesProvider.TILE_WIDTH, OfflineTilesProvider.TILE_HEIGHT);
			} else if (currentTilesProvider.equalsIgnoreCase(TilesProviderType.tms.toString())) {
				provider = new OfflineTmsMapTilesProvider(OfflineTilesProvider.TILE_WIDTH, OfflineTilesProvider.TILE_HEIGHT);
			} else {
				provider = new OfflineWtmsMapTilesProvider(OfflineTilesProvider.TILE_WIDTH, OfflineTilesProvider.TILE_HEIGHT);
			}

			tiles = provider.getTilesForLatLngBounds(bounds, (int)currentZoomLevel, (int)maxSupportedZoomLevel);

			if ((tilesToDownload + tiles.size()) < MAX_TILES_IN_DOWNLOAD_QUEUE) {
				Tile.createTiles(tiles);
				Log.d(this.getClass().getName(), "Created " + tiles.size() + " tiles to download");
				tilesToDownload = Tile.getTilesToDownload();
				Toast.makeText(getActivity().getBaseContext(), String.format(getActivity().getBaseContext().getResources().getString(R.string.tiles_queued), tilesToDownload), Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getActivity().getBaseContext(), String.format(getActivity().getBaseContext().getResources().getString(R.string.too_many_tiles_queued), tilesToDownload), Toast.LENGTH_LONG).show();
			}

			Task task = Task.getTask(TileDownloadTask.TASK_ID);

			if (task != null && System.currentTimeMillis() - task.getStarted().getTime() > 900000) {
				// Cancel tasks older than 15 minutes
				task.delete();
				task = null;
			}

			if (task == null) {
				TileDownloadTask downloadTask = new TileDownloadTask();
				downloadTask.setContext(getActivity().getBaseContext());
				downloadTask.setMap(map);
				downloadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}

		} else {
			Toast.makeText(getActivity().getBaseContext(), R.string.zoom_level_too_low, Toast.LENGTH_LONG).show();
		}
	}

	private void storeCameraPosition(CameraPosition cameraPosition) {

		Configuration zoom = Configuration.getConfigurationByName(MAIN_MAP_ZOOM);

		if (zoom != null) {
			zoom.setValue("" + cameraPosition.zoom);
			zoom.update();
		} else {
			zoom = new Configuration();
			zoom.setName(MAIN_MAP_ZOOM);
			zoom.setValue("" + cameraPosition.zoom);
			zoom.create();
		}

		Configuration latitude = Configuration.getConfigurationByName(MAIN_MAP_LATITUDE);

		if (latitude != null) {
			latitude.setValue("" + cameraPosition.target.latitude);
			latitude.update();
		} else {
			latitude = new Configuration();
			latitude.setName(MAIN_MAP_LATITUDE);
			latitude.setValue("" + cameraPosition.target.latitude);
			latitude.create();
		}

		Configuration longitude = Configuration.getConfigurationByName(MAIN_MAP_LONGITUDE);

		if (longitude != null) {
			longitude.setValue("" + cameraPosition.target.longitude);
			longitude.update();
		} else {
			longitude = new Configuration();
			longitude.setName(MAIN_MAP_LONGITUDE);
			longitude.setValue("" + cameraPosition.target.longitude);
			longitude.create();
		}
	}

	private void reloadVisibleProperties() {

		hideVisibleProperties();

		LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
		Polygon boundsPoly = getPolygon(bounds);

		if (allClaims == null) {
			allClaims = Claim.getSimplifiedClaimsForMap();
		}
		visibleProperties = new ArrayList<BasePropertyBoundary>();
		for (Claim claim : allClaims) {
			BasePropertyBoundary bpb = new BasePropertyBoundary(mapView.getContext(), map, claim.getClaimId(), false);
			Polygon claimPoly = bpb.getPolygon();
			if (claimPoly != null && claimPoly.intersects(boundsPoly)) {
				visibleProperties.add(bpb);
			}
		}

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

		showVisibleProperties();
		drawAreaOfInterest();
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

	public void refreshMap() {
		refreshMap(false);
	}

	public void refreshMap(boolean zoomToAreaOfInterest) {
		if(map != null) {
			hideVisibleProperties();

			allClaims = Claim.getSimplifiedClaimsForMap();
			visibleProperties = new ArrayList<BasePropertyBoundary>();
			for (Claim claim : allClaims) {
				visibleProperties.add(new BasePropertyBoundary(mapView.getContext(), map, claim.getClaimId(), false));
			}
			showVisibleProperties();
			drawAreaOfInterest();
			if (zoomToAreaOfInterest) {
				boundCameraToInterestArea();
			}
		}
	}

	private void drawAreaOfInterest() {
		if(communityArea != null && communityArea.getPolylines() != null){
			communityArea.removeFromMap();
		} else {
			communityArea = new CommunityArea(map);
		}
		communityArea.drawInterestArea();
	}

	public void boundCameraToInterestArea() {

		if (OpenTenureApplication.getInstance().isInitialized()) {
			drawAreaOfInterest();
			LatLngBounds.Builder bounds;
			// setup map
			bounds = new LatLngBounds.Builder();
			// get all cars from the datbase with getter method
			List<LatLng> K = CommunityArea.getPoints();

			// loop through cars in the database
			for (LatLng cn : K) {
				// use .include to put add each point to be included in the
				// bounds
				bounds.include(cn);
			}

			// set bounds with all the map points
			map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 400, 400, 10));
		}
	}
}
