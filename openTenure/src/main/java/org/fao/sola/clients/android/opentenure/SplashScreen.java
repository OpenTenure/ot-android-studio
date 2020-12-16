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
package org.fao.sola.clients.android.opentenure;



import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;


public class SplashScreen extends Activity {
	public static final int REQUEST_CODE_ALL=9;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_splash);
		new PrefetchData().execute();

	}

	private class PrefetchData extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				OpenTenureApplication.getInstance().getDatabase();
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (checkPermissions()) {
				Intent i = new Intent(SplashScreen.this, InitializationActivity.class);
				startActivity(i);
				finish();
			} else {
				requestPermissions();
			}
		}

		private void requestPermissions() {
			ActivityCompat.requestPermissions(SplashScreen.this, new String[] {
							Manifest.permission.ACCESS_FINE_LOCATION,
							Manifest.permission.CAMERA,
							Manifest.permission.WRITE_EXTERNAL_STORAGE
					}, REQUEST_CODE_ALL);

		}
		private boolean checkPermissions() {

			int FirstPermissionResult = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
			int SecondPermissionResult = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
			int ThirdPermissionResult = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

			return FirstPermissionResult == PackageManager.PERMISSION_GRANTED &&
					SecondPermissionResult == PackageManager.PERMISSION_GRANTED &&
					ThirdPermissionResult == PackageManager.PERMISSION_GRANTED;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case REQUEST_CODE_ALL:
				if (grantResults.length > 0) {

					boolean gpsPerm = grantResults[0] == PackageManager.PERMISSION_GRANTED;
					boolean cameraPerm = grantResults[1] == PackageManager.PERMISSION_GRANTED;
					boolean storagePerm = grantResults[2] == PackageManager.PERMISSION_GRANTED;

					if (gpsPerm && cameraPerm && storagePerm) {
						Intent i = new Intent(SplashScreen.this, InitializationActivity.class);
						startActivity(i);
						finish();
					}
					else {
						Toast.makeText(this,"Permissions are necessary",Toast.LENGTH_LONG).show();
						final Handler handler = new Handler();
						handler.postDelayed(new Runnable() {
							@Override
							public void run() {
								System.exit(0);
							}
						}, 4000);

					}
				}

				break;
		}
	}

}