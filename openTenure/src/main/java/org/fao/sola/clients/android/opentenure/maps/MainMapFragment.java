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
import org.fao.sola.clients.android.opentenure.network.GetAllClaimsTask;
import org.fao.sola.clients.android.opentenure.network.LoginActivity;
import org.fao.sola.clients.android.opentenure.network.LogoutTask;

import com.androidmapsextensions.ClusteringSettings;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.GoogleMap.OnCameraChangeListener;
import com.androidmapsextensions.GoogleMap.OnMapLongClickListener;
import com.androidmapsextensions.GoogleMap.OnMarkerClickListener;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.androidmapsextensions.SupportMapFragment;
import com.androidmapsextensions.TileOverlay;
import com.androidmapsextensions.TileOverlayOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainMapFragment extends SupportMapFragment implements OnCameraChangeListener {

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
		map_provider_google_normal, map_provider_google_satellite, map_provider_google_hybrid, map_provider_google_terrain, map_provider_osm_mapnik, map_provider_osm_mapquest, map_provider_local_tiles, map_provider_geoserver
	};

	private MapType mapType = MapType.map_provider_google_normal;
	private View mapView;
	private MapLabel label;
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
			Log.d(this.getClass().getName(), "onStatusChanged");
		}

		public void onProviderEnabled(String provider) {
			Log.d(this.getClass().getName(), "onProviderEnabled");
		}

		public void onProviderDisabled(String provider) {
			Log.d(this.getClass().getName(), "onProviderDisabled");
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
		remove.setClusterGroup(Constants.MARKER_EDIT_REMOVE_GROUP);
		cancel = map.addMarker(new MarkerOptions()
		.position(projection.fromScreenLocation(getControlCancelPosition(markerScreenPosition, markerWidth, markerHeight)))
		.anchor(0.5f, 0.5f)
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_menu_block)));
		cancel.setClusterGroup(Constants.MARKER_EDIT_CANCEL_GROUP);
	}

	private Point getControlRemovePosition(Point markerScreenPosition, int markerWidth, int markerHeight){
		return new Point(markerScreenPosition.x - markerWidth, markerScreenPosition.y + markerHeight);
	}

	private Point getControlCancelPosition(Point markerScreenPosition, int markerWidth, int markerHeight){
		return new Point(markerScreenPosition.x + markerWidth, markerScreenPosition.y + markerHeight);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		MenuItem itemIn;
		MenuItem itemOut;

		try {
			Thread.sleep(400);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		Log.d(this.getClass().getName(), "Is the user logged in ? : " + OpenTenureApplication.isLoggedin());

		if (OpenTenureApplication.isLoggedin()) {
			itemIn = menu.findItem(R.id.action_login);
			itemIn.setVisible(false);
			itemOut = menu.findItem(R.id.action_logout);
			itemOut.setVisible(true);

		} else {

			itemIn = menu.findItem(R.id.action_login);
			itemIn.setVisible(true);
			itemOut = menu.findItem(R.id.action_logout);
			itemOut.setVisible(false);
		}
		if (map != null) {
			float currentZoomLevel = map.getCameraPosition().zoom;
			float maxSupportedZoomLevel = map.getMaxZoomLevel();

			if (currentZoomLevel >= (maxSupportedZoomLevel - MAX_ZOOM_LEVELS_TO_DOWNLOAD)) {
				MenuItem item = menu.findItem(R.id.action_download_tiles);
				item.setVisible(true);
			} else {
				MenuItem item = menu.findItem(R.id.action_download_tiles);
				item.setVisible(false);
			}
		}

		super.onPrepareOptionsMenu(menu);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		super.onCreateView(inflater, container, savedInstanceState);
		mapView = inflater.inflate(R.layout.main_map, container, false);
		setHasOptionsMenu(true);
		label = (MapLabel) getChildFragmentManager().findFragmentById(R.id.main_map_provider_label);
//		label = (MapLabel) getActivity().getSupportFragmentManager().findFragmentById(R.id.main_map_provider_label);
		label.changeTextProperties(MAP_LABEL_FONT_SIZE,
				getActivity().getResources().getString(R.string.map_provider_google_normal));
//		map = ((SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.main_map_fragment))
//				.getExtendedMap();
		map = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.main_map_fragment))
				.getExtendedMap();
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
		reloadVisibleProperties();

		lh = new LocationHelper(
				(LocationManager) getActivity().getBaseContext().getSystemService(Context.LOCATION_SERVICE));
		lh.start();
		MapsInitializer.initialize(this.getActivity());
		map.setInfoWindowAdapter(new PopupAdapter(inflater));
		map.setOnCameraChangeListener(this);

		if (savedInstanceState != null && savedInstanceState.getString(MAIN_MAP_TYPE) != null) {
			// probably an orientation change don't move the view but
			// restore the current type of the map
			mapType = MapType.valueOf(savedInstanceState.getString(MAIN_MAP_TYPE));
		} else {
			// restore the latest map type used on the main map
			try {
				mapType = MapType.valueOf(Configuration.getConfigurationValue(MainMapFragment.MAIN_MAP_TYPE));
			} catch (Exception e) {
				mapType = MapType.map_provider_google_normal;
			}
		}
		setMapType();

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
			Configuration cfg = Configuration.getConfigurationByName("isInitialized");
			if (cfg != null && Boolean.parseBoolean(cfg.getValue())) {

				LatLngBounds.Builder bounds;
				// setup map

				bounds = new LatLngBounds.Builder();
				// Get vertices of the community area
				List<LatLng> K = CommunityArea.getPolyline().getPoints();

				for (LatLng cn : K) {
					// Make sure that the vertices are in the displayed area
					bounds.include(cn);

				}
				// set bounds with all the map points
				map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 400, 400, 10));

			}

		}

		OpenTenureApplication.setMapFragment(this);

		redrawVisibleProperties();

		this.map.setOnMapLongClickListener(new OnMapLongClickListener() {

			@Override
			public void onMapLongClick(final LatLng position) {
				addMapBookmark(position);
			}
		});

		this.map.setOnMarkerClickListener(new OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(final Marker mark) {
				return handleMarkerClick(mark);
			}
		});
		
		List<Bookmark> allBookmarks = Bookmark.getAllBookmarks();
		
		for(Bookmark bm : allBookmarks){
			mapBookmarksMap.put(createMapBookmarkMarker(new LatLng(bm.getLat(), bm.getLon()), bm.getName()),bm);
		}

		OpenTenureApplication.setActivity(getActivity());

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
				.clusterGroup(Constants.BASE_MAP_BOOKMARK_MARKERS_GROUP + mapBookmarksMap.size())
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.map_bookmark)));
		return marker;
	}

	private void showVisibleProperties() {

		if (visibleProperties != null) {
			for (BasePropertyBoundary visibleProperty : visibleProperties) {
				visibleProperty.showBoundary();
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
				visibleProperty.hideBoundary();
			}
		}
	}

	@Override
	public void onStart() {

//		map = ((SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.main_map_fragment))
//				.getExtendedMap();
		map = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.main_map_fragment))
				.getExtendedMap();
		ClusteringSettings settings = new ClusteringSettings();
		settings.clusterOptionsProvider(new OpenTenureClusterOptionsProvider(getResources()));
		settings.addMarkersDynamically(true);
		map.setClustering(settings);
		super.onStart();

	}

	@Override
	public void onDestroyView() {

		lh.stop();
		Fragment map = getFragmentManager().findFragmentById(R.id.main_map_fragment);
		Fragment label = getFragmentManager().findFragmentById(R.id.main_map_provider_label);
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

	@Override
	public void onResume() {
		super.onResume();
		refreshMap();
		lh.hurryUp();
	}

	@Override
	public void onPause() {
		super.onPause();
		lh.slowDown();
	}

	@Override
	public void onStop() {
		super.onStop();
		lh.stop();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.map, menu);

		super.onCreateOptionsMenu(menu, inflater);
	}

	private void setMapLabel() {

		switch (mapType) {
		case map_provider_google_normal:
			label.changeTextProperties(MAP_LABEL_FONT_SIZE,
					getResources().getString(R.string.map_provider_google_normal));
			break;
		case map_provider_google_satellite:
			label.changeTextProperties(MAP_LABEL_FONT_SIZE,
					getResources().getString(R.string.map_provider_google_satellite));
			break;
		case map_provider_google_hybrid:
			label.changeTextProperties(MAP_LABEL_FONT_SIZE,
					getResources().getString(R.string.map_provider_google_hybrid));
			break;
		case map_provider_google_terrain:
			label.changeTextProperties(MAP_LABEL_FONT_SIZE,
					getResources().getString(R.string.map_provider_google_terrain));
			break;
		case map_provider_osm_mapnik:
			label.changeTextProperties(MAP_LABEL_FONT_SIZE, getResources().getString(R.string.map_provider_osm_mapnik));
			break;
		case map_provider_osm_mapquest:
			label.changeTextProperties(MAP_LABEL_FONT_SIZE,
					getResources().getString(R.string.map_provider_osm_mapquest));
			break;
		case map_provider_local_tiles:
			label.changeTextProperties(MAP_LABEL_FONT_SIZE,
					getResources().getString(R.string.map_provider_local_tiles));
			break;
		case map_provider_geoserver:
			label.changeTextProperties(MAP_LABEL_FONT_SIZE, getResources().getString(R.string.map_provider_geoserver));
			break;
		default:
			break;
		}
	}

	public void setMapType() {

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
			map.addTileOverlay(new TileOverlayOptions().tileProvider(mapNikTileProvider));
			redrawVisibleProperties();
			break;
		case map_provider_osm_mapquest:
			OsmTileProvider mapQuestTileProvider = new OsmTileProvider(256, 256, OSM_MAPQUEST_BASE_URL);
			map.setMapType(GoogleMap.MAP_TYPE_NONE);
			map.addTileOverlay(new TileOverlayOptions().tileProvider(mapQuestTileProvider));
			redrawVisibleProperties();
			break;
		case map_provider_local_tiles:
			map.setMapType(GoogleMap.MAP_TYPE_NONE);
			map.addTileOverlay(new TileOverlayOptions().tileProvider(new LocalMapTileProvider()));
			redrawVisibleProperties();
			break;
		case map_provider_geoserver:
			map.setMapType(GoogleMap.MAP_TYPE_NONE);
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mapView.getContext());
			map.addTileOverlay(new TileOverlayOptions().tileProvider(new WmsMapTileProvider(256, 256, preferences)));
			redrawVisibleProperties();
			break;
		default:
			break;
		}
		setMapLabel();

		Configuration provider = Configuration.getConfigurationByName(MAIN_MAP_TYPE);

		if (provider != null) {
			provider.setValue(mapType.toString());
			provider.update();
		} else {
			provider = new Configuration();
			provider.setName(MAIN_MAP_TYPE);
			provider.setValue(mapType.toString());
			provider.create();
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
		case R.id.action_settings:
			return true;
		case R.id.map_provider_google_normal:
			mapType = MapType.map_provider_google_normal;
			setMapType();
			return true;
		case R.id.map_provider_google_satellite:
			mapType = MapType.map_provider_google_satellite;
			setMapType();
			return true;
		case R.id.map_provider_google_hybrid:
			mapType = MapType.map_provider_google_hybrid;
			setMapType();
			return true;
		case R.id.map_provider_google_terrain:
			mapType = MapType.map_provider_google_terrain;
			setMapType();
			return true;
		case R.id.map_provider_osm_mapnik:
			mapType = MapType.map_provider_osm_mapnik;
			setMapType();
			return true;
		case R.id.map_provider_osm_mapquest:
			mapType = MapType.map_provider_osm_mapquest;
			setMapType();
			return true;
		case R.id.map_provider_local_tiles:
			mapType = MapType.map_provider_local_tiles;
			setMapType();
			return true;
		case R.id.map_provider_geoserver:
			mapType = MapType.map_provider_geoserver;
			setMapType();
			return true;
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

			if (!Boolean.parseBoolean(Configuration.getConfigurationByName("isInitialized").getValue())) {
				Toast toast;
				String toastMessage = String
						.format(OpenTenureApplication.getContext().getString(R.string.message_app_not_yet_initialized));

				toast = Toast.makeText(OpenTenureApplication.getContext(), toastMessage, Toast.LENGTH_LONG);
				toast.show();

				return true;
			}

			MainMapFragment mapFrag = OpenTenureApplication.getMapFragment();

			mapFrag.boundCameraToInterestArea();
			return true;

		case R.id.action_download_claims:

			if (!Boolean.parseBoolean(Configuration.getConfigurationByName("isInitialized").getValue())) {
				Toast toast;
				String toastMessage = String
						.format(OpenTenureApplication.getContext().getString(R.string.message_app_not_yet_initialized));

				toast = Toast.makeText(OpenTenureApplication.getContext(), toastMessage, Toast.LENGTH_LONG);
				toast.show();

				return true;
			}

			if (!OpenTenureApplication.isLoggedin()) {
				Toast toast = Toast.makeText(OpenTenureApplication.getContext(), R.string.message_login_before,
						Toast.LENGTH_LONG);
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
		case R.id.action_login:

			if (!Boolean.parseBoolean(Configuration.getConfigurationByName("isInitialized").getValue())) {
				Toast toast;
				String toastMessage = String
						.format(OpenTenureApplication.getContext().getString(R.string.message_app_not_yet_initialized));

				toast = Toast.makeText(OpenTenureApplication.getContext(), toastMessage, Toast.LENGTH_LONG);
				toast.show();
				return true;
			}

			OpenTenureApplication.setActivity(getActivity());

			Context context = getActivity().getApplicationContext();
			Intent intent2 = new Intent(context, LoginActivity.class);
			startActivity(intent2);

			OpenTenureApplication.setActivity(getActivity());

			return false;

		case R.id.action_logout:

			try {

				LogoutTask logoutTask = new LogoutTask();

				logoutTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getActivity());

			} catch (Exception e) {
				Log.d("Details", "An error ");

				e.printStackTrace();
			}

			return true;
		case R.id.action_download_tiles:

			if (!Boolean.parseBoolean(Configuration.getConfigurationByName("isInitialized").getValue())) {
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

			SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mapView.getContext());

			List<Tile> tiles = null;
			OfflineTilesProvider provider = null;

			String currentTilesProvider = sharedPrefs.getString(OpenTenure.tiles_provider,
					OfflineTilesProvider.TilesProviderType.GeoServer.toString());

			if (currentTilesProvider.equalsIgnoreCase(TilesProviderType.GeoServer.toString())) {
				provider = new OfflineWmsMapTileProvider(OfflineTilesProvider.TILE_WIDTH,
						OfflineTilesProvider.TILE_HEIGHT, sharedPrefs);
			} else if (currentTilesProvider.equalsIgnoreCase(TilesProviderType.TMS.toString())) {
				provider = new OfflineTmsMapTilesProvider(OfflineTilesProvider.TILE_WIDTH,
						OfflineTilesProvider.TILE_HEIGHT, sharedPrefs);
			} else {
				provider = new OfflineWtmsMapTilesProvider(OfflineTilesProvider.TILE_WIDTH,
						OfflineTilesProvider.TILE_HEIGHT, sharedPrefs);
			}
			tiles = provider.getTilesForLatLngBounds(bounds, (int)currentZoomLevel, (int)maxSupportedZoomLevel);

			if ((tilesToDownload + tiles.size()) < MAX_TILES_IN_DOWNLOAD_QUEUE) {

				Tile.createTiles(tiles);
				Log.d(this.getClass().getName(), "Created " + tiles.size() + " tiles to download");
				tilesToDownload = Tile.getTilesToDownload();
				Toast.makeText(getActivity().getBaseContext(),
						String.format(getActivity().getBaseContext().getResources().getString(R.string.tiles_queued),
								tilesToDownload),
						Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(getActivity().getBaseContext(),
						String.format(
								getActivity().getBaseContext().getResources().getString(R.string.too_many_tiles_queued),
								tilesToDownload),
						Toast.LENGTH_LONG).show();
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
			BasePropertyBoundary bpb = new BasePropertyBoundary(mapView.getContext(), map, claim, false);
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

	@Override
	public void onCameraChange(CameraPosition cameraPosition) {

		getActivity().invalidateOptionsMenu();
		// Store camera position to allow drawing on the claim map where we
		// left off the main map
		storeCameraPosition(cameraPosition);

		reloadVisibleProperties();

	}

	public void refreshMap() {

		hideVisibleProperties();

		allClaims = Claim.getSimplifiedClaimsForMap();
		visibleProperties = new ArrayList<BasePropertyBoundary>();
		for (Claim claim : allClaims) {
			visibleProperties.add(new BasePropertyBoundary(mapView.getContext(), map, claim, false));
		}

		showVisibleProperties();
		drawAreaOfInterest();
		// redrawVisibleProperties();

		return;
	}

	private void drawAreaOfInterest() {
		CommunityArea area = new CommunityArea(map);
		area.drawInterestArea();

	}

	public void boundCameraToInterestArea() {

		if (Boolean.parseBoolean(Configuration.getConfigurationByName("isInitialized").getValue())) {

			drawAreaOfInterest();

			LatLngBounds.Builder bounds;
			// setup map

			bounds = new LatLngBounds.Builder();
			// get all cars from the datbase with getter method
			List<LatLng> K = CommunityArea.getPolyline().getPoints();

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
