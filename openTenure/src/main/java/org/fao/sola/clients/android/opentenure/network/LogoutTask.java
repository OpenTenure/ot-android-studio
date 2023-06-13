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
package org.fao.sola.clients.android.opentenure.network;


import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.network.API.CommunityServerAPI;

import android.os.AsyncTask;
import androidx.fragment.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;


/**
 * Task which performs the logout request and update the status 
 * 
 * */
public class LogoutTask extends AsyncTask<Object, Void, Integer > {
	
	FragmentActivity fa;


	@Override
	protected Integer doInBackground(Object... params) {
		try {
			
			   fa = (FragmentActivity) params[0];
			   return CommunityServerAPI.logout();
			   
				
			}
		catch (Throwable ex) {
				
				Log.d("LogoutTask","An error has occurred during logout:" + ex.getMessage());
				ex.printStackTrace();
				return 0;
			}
	}
	
	
	@Override
	protected void onPostExecute(final Integer status) {
		
		Toast toast;
		
		switch (status) {
		
		case 200:
			OpenTenureApplication.setLoggedin(false);

			toast = Toast
					.makeText(OpenTenureApplication.getContext(),
							R.string.message_logout_ok,
							Toast.LENGTH_LONG);
			toast.show();		
			
			fa.invalidateOptionsMenu();
			
			break;
			
		case 401:
			toast = Toast
			.makeText(OpenTenureApplication.getContext(),
					R.string.message_logout_not_ok,
					Toast.LENGTH_LONG);
			toast.show();			
			break;
			
		case 80:
			toast = Toast
			.makeText(OpenTenureApplication.getContext(),
					R.string.message_timeout_exception,
					Toast.LENGTH_LONG);
			toast.show();
			break;
			
		case 1:
			toast = Toast
			.makeText(OpenTenureApplication.getContext(),
					R.string.message_unknowhost_error,
					Toast.LENGTH_LONG);
			toast.show();
			break;	

			
		case 0:
			toast = Toast
			.makeText(OpenTenureApplication.getContext(),
					R.string.message_logout_error,
					Toast.LENGTH_LONG);
			toast.show();
			break;		

		default:			
			
			break;
		}
		
	}
	
	
	
	
	
	

}
