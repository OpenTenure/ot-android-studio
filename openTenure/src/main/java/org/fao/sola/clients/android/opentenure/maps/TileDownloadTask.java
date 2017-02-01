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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.model.Task;
import org.fao.sola.clients.android.opentenure.model.Tile;

import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class TileDownloadTask extends AsyncTask<Void, Integer, Integer> {

	public static final String TASK_ID = "TileDownloadTask";
	private static final int TILES_PER_BATCH = 20;
	public static final long TILE_REFRESH_TIME = 21 * 24 * 60 * 60 * 1000;
	private static final int TIMEOUT = 8000;
	private static final int MAX_RETRY = 2;

	private static final int ATTEMPTED_INDEX = 0;
	private static final int TOTAL_INDEX = 1;
	private static final int SUCCEEDED_INDEX = 2;
	private static final int FAILED_INDEX = 3;

	private Context context;
	private Marker downloadStatusMarker;
	private GoogleMap map;
	private Integer[] downloadStatus = new Integer[4];

	public void setContext(Context context) {
		this.context = context;
	}

	public void setMap(GoogleMap map) {
		this.map = map;
	}

	public TileDownloadTask() {
	}

	protected void onPreExecute() {
		Task.createTask(new Task(TASK_ID));
		downloadStatusMarker = createDownloadStatusMarker();
	}

	private boolean needsDownloading(Tile tile) {

		File tileFile = new File(tile.getFileName());

		if (!tileFile.exists()) {
			
			// If it doesn't exist request a new download

			return true;
		}

		if ((tileFile.lastModified() < (System.currentTimeMillis() - TILE_REFRESH_TIME))
				|| (BitmapFactory.decodeFile(tile.getFileName()) == null)) {

			// If it does not comply with our caching policy
			// or can't be decoded as an image delete it and request
			// a new download
			
			tileFile.delete();
			return true;
		}

		return false;
	}

	private void resetDownloadStatus() {
		downloadStatus[ATTEMPTED_INDEX] = Integer.valueOf(0);
		downloadStatus[SUCCEEDED_INDEX] = Integer.valueOf(0);
		downloadStatus[FAILED_INDEX] = Integer.valueOf(0);

		int tilesToDownload = Tile.getTilesToDownload();
		downloadStatus[TOTAL_INDEX] = tilesToDownload;
	}

	private void updateDownloadStatus() {
		int tilesToDownload = Tile.getTilesToDownload();

		if (!(tilesToDownload == (downloadStatus[TOTAL_INDEX] - downloadStatus[ATTEMPTED_INDEX]))) {
			// Someone added more tiles to the download queue so we reset
			// counters
			downloadStatus[ATTEMPTED_INDEX] = Integer.valueOf(0);
			downloadStatus[SUCCEEDED_INDEX] = Integer.valueOf(0);
			downloadStatus[FAILED_INDEX] = Integer.valueOf(0);
			downloadStatus[TOTAL_INDEX] = tilesToDownload;
		}
	}

	protected Integer doInBackground(Void... params) {

		List<Tile> tiles = Tile.getTilesToDownload(TILES_PER_BATCH);

		resetDownloadStatus();
		publishProgress(downloadStatus);
		int failures = 0;

		Log.d(this.getClass().getName(), "loaded a batch of " + tiles.size()
				+ " tiles out of " + downloadStatus[TOTAL_INDEX]);

		while (tiles != null && tiles.size() >= 1) {

			for (Tile tile : tiles) {

				boolean failed = false;

				for (int i = 0; i <= MAX_RETRY; i++) {

					File outputFile = new File(tile.getFileName());
					File dir = new File(outputFile.getParent());
					dir.mkdirs();
					InputStream is = null;
					FileOutputStream fos = null;

					if (needsDownloading(tile)) {

						Log.d(this.getClass().getName(), "Trying to download tile " + tile.getUrl()
								+ " to file " + tile.getFileName());
						
						try {

							URL url = new URL(tile.getUrl());
							HttpURLConnection c = (HttpURLConnection) url
									.openConnection();
							c.setRequestMethod("GET");
							c.setDoOutput(true);
							c.setConnectTimeout(TIMEOUT);
							c.setReadTimeout(TIMEOUT);
							c.connect();
							fos = new FileOutputStream(outputFile);
							is = c.getInputStream();
							byte[] buffer = new byte[1024];
							int len1 = 0;
							while ((len1 = is.read(buffer)) != -1) {
								fos.write(buffer, 0, len1);
							}
							fos.close();
							is.close();
							failed = false;
							break;

						} catch (FileNotFoundException e) {
							failed = true;
							e.printStackTrace();
							outputFile.delete();
							break;
						} catch (Exception e) {
							failed = true;
							e.printStackTrace();
							outputFile.delete();
						} finally {
							if (is != null) {
								try {
									is.close();
								} catch (IOException ignore) {
								}
							}
							if (fos != null) {
								try {
									fos.close();
								} catch (IOException ignore) {
								}
							}
						}
					} else {
						failed = false;
						break;
					}
				}
				if (failed) {
					failures++;
					downloadStatus[FAILED_INDEX]++;
				} else {
					downloadStatus[SUCCEEDED_INDEX]++;
				}
				tile.delete();
				downloadStatus[ATTEMPTED_INDEX]++;
				publishProgress(downloadStatus);
			}

			updateDownloadStatus();
			tiles = Tile.getTilesToDownload(TILES_PER_BATCH);
			Log.d(this.getClass().getName(),
					"loaded a batch of " + tiles.size() + " tiles out of "
							+ downloadStatus[TOTAL_INDEX]);
		}

		return failures;
	}

	protected void onProgressUpdate(Integer... downloadStatus) {
		String message = String.format(
				context.getResources()
						.getString(R.string.tiles_download_status),
				downloadStatus[ATTEMPTED_INDEX], downloadStatus[TOTAL_INDEX],
				downloadStatus[SUCCEEDED_INDEX], downloadStatus[FAILED_INDEX]);
		downloadStatusMarker.setPosition(getMarkerPosition());
		downloadStatusMarker.setTitle(message);
		downloadStatusMarker.showInfoWindow();
	}
	private LatLng getMarkerPosition(){
		LatLngBounds latLngBounds = map.getProjection().getVisibleRegion().latLngBounds;
		LatLng position = new LatLng(latLngBounds.southwest.latitude,
				latLngBounds.southwest.longitude + (latLngBounds.northeast.longitude - latLngBounds.southwest.longitude)/2.0);
		return position;
	}
	private Marker createDownloadStatusMarker(){
		Marker downloadStatusMarker = map
		.addMarker(new MarkerOptions()
				.position(getMarkerPosition())
				.anchor(0.5f, 1.0f));
		downloadStatusMarker.setAlpha(0.0f);
		downloadStatusMarker.setInfoWindowAnchor(.5f,1.0f);
		downloadStatusMarker
		.setClusterGroup(Constants.MARKER_DOWNLOAD_STATUS_GROUP);
		return downloadStatusMarker;
	}

	protected void onPostExecute(Integer failures) {

		Task.deleteTask(new Task(TASK_ID));
		downloadStatusMarker.hideInfoWindow();
		downloadStatusMarker.remove();
		// If many tasks like this were running only the one serving the last
		if (failures > 0) {
			Toast.makeText(
					context,
					String.format(
							context.getResources().getString(
									R.string.not_all_tiles_downloaded),
							failures), Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(
					context,
					context.getResources().getString(
							R.string.all_tiles_downloaded), Toast.LENGTH_LONG)
					.show();
		}
		int deletedTiles = Tile.deleteAllTiles();
		Log.d(this.getClass().getName(), "Deleted " + deletedTiles
				+ " tiles still in the queue at the end of the download task");
	}
}
