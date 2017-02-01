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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.fao.sola.clients.android.opentenure.OpenTenure;
import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.maps.OfflineTilesProvider.TilesProviderType;

import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

public class LocalMapTileProvider implements TileProvider {
	public static final int TILE_WIDTH = 256;
	public static final int TILE_HEIGHT = 256;
	private static final int BUFFER_SIZE = 32 * 1024;

	private OfflineTilesProvider tilesProvider;
	public LocalMapTileProvider() {
		
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(OpenTenureApplication.getContext());
		
		String currentTilesProvider = prefs.getString(OpenTenure.tiles_provider, TilesProviderType.GeoServer.toString());
		
		if(currentTilesProvider.equals(TilesProviderType.GeoServer.toString())){
			tilesProvider = new OfflineWmsMapTileProvider(TILE_WIDTH, TILE_HEIGHT, prefs);
		}else if(currentTilesProvider.equals(TilesProviderType.TMS.toString())){
			tilesProvider = new OfflineTmsMapTilesProvider(TILE_WIDTH, TILE_HEIGHT, prefs);
		}else{
			tilesProvider = new OfflineWtmsMapTilesProvider(TILE_WIDTH, TILE_HEIGHT, prefs);
		}
	}

	@Override
	public Tile getTile(int x, int y, int zoom) {
		byte[] image = readTileImage(x, y, zoom);
		return image == null ? null : new Tile(TILE_WIDTH, TILE_HEIGHT, image);
	}

	private byte[] readTileImage(int x, int y, int zoom) {
		InputStream in = null;
		ByteArrayOutputStream buffer = null;
		String tileFileName = tilesProvider.getBaseStorageDir() + zoom + "/" + x + "/" + y + tilesProvider.getTilesSuffix();

		if (BitmapFactory.decodeFile(tileFileName) == null) {
			File checkFile = new File(tileFileName);
			checkFile.delete();
		}

		try {
			File tileFile = new File(tileFileName);
			if(tileFile.exists()){
				if(tileFile.lastModified() < (System
						.currentTimeMillis() - TileDownloadTask.TILE_REFRESH_TIME)){
					// The tile does not comply our caching policy
					// so we delete it and return null
					tileFile.delete();
				}else{
					in = new FileInputStream(tileFileName);
					buffer = new ByteArrayOutputStream();

					int nRead;
					byte[] data = new byte[BUFFER_SIZE];

					while ((nRead = in.read(data, 0, BUFFER_SIZE)) != -1) {
						buffer.write(data, 0, nRead);
					}
					buffer.flush();

					return buffer.toByteArray();
				}
			}
		} catch (IOException e) {
			Log.d(this.getClass().getName(), "Can't read local tile: " + tileFileName);
		} catch (OutOfMemoryError e) {

		} finally {
			if (in != null)
				try {
					in.close();
				} catch (Exception ignored) {
				}
			if (buffer != null)
				try {
					buffer.close();
				} catch (Exception ignored) {
				}
		}
		return null;
	}
}
