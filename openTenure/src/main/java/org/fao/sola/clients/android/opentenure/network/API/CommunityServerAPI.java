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
package org.fao.sola.clients.android.opentenure.network.API;

import java.io.IOException;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.UnknownHostException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.OpenTenurePreferencesActivity;
import org.fao.sola.clients.android.opentenure.filesystem.json.model.Claim;
import org.fao.sola.clients.android.opentenure.network.LoginActivity;
import org.fao.sola.clients.android.opentenure.network.response.ApiResponse;
import org.fao.sola.clients.android.opentenure.network.response.BoundaryResponse;
import org.fao.sola.clients.android.opentenure.network.response.BoundaryStatusResponse;
import org.fao.sola.clients.android.opentenure.network.response.BoundaryTypeResponse;
import org.fao.sola.clients.android.opentenure.network.response.ClaimTypeResponse;
import org.fao.sola.clients.android.opentenure.network.response.GetAttachmentResponse;
import org.fao.sola.clients.android.opentenure.network.response.GetCommunityAreaResponse;
import org.fao.sola.clients.android.opentenure.network.response.IdTypeResponse;
import org.fao.sola.clients.android.opentenure.network.response.LandUseResponse;
import org.fao.sola.clients.android.opentenure.network.response.LanguageResponse;
import org.fao.sola.clients.android.opentenure.network.response.ProjectResponse;
import org.fao.sola.clients.android.opentenure.network.response.RefDataResponse;
import org.fao.sola.clients.android.opentenure.network.response.ResultResponse;
import org.fao.sola.clients.android.opentenure.network.response.SaveAttachmentResponse;
import org.fao.sola.clients.android.opentenure.network.response.SaveClaimResponse;

import android.content.SharedPreferences;
import android.net.http.AndroidHttpClient;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class CommunityServerAPI {

	public static String SERVER_PROTO_VERSION_HEADER = "ProtoVersion";

	private static String serverProtoVersion;

	public static String getServerProtoVersion() {
		return serverProtoVersion;
	}


	private static void setServerProtoVersion(HttpResponse serverResponse) {
		Header header = serverResponse
				.getFirstHeader(SERVER_PROTO_VERSION_HEADER);
		if (header != null)
			setServerProtoVersion(header.getValue());
		else {
			Log.d(CommunityServerAPI.class.getName(), "Header is null");
		}
	}

	private static void setServerProtoVersion(String serverProtoVersion) {
		CommunityServerAPI.serverProtoVersion = serverProtoVersion;
	}

	/**
	 * 
	 * The login API
	 * 
	 * *
	 * 
	 * @return 200 in case of success, 401 in case of fail, 0 in case of generic
	 *         error, 80 in case of connection timed out error
	 * 
	 */
	public static int login(String username, String password) {

		try {
			/*
			 * Creating the url to call
			 */

			if (!OpenTenureApplication.getInstance().isOnline())
				return LoginActivity._NO_CONNECTION;

			SharedPreferences OpenTenurePreferences = PreferenceManager
					.getDefaultSharedPreferences(OpenTenureApplication
							.getContext());

			String csUrl = OpenTenureApplication.getInstance().getServerUrl();

			if (csUrl.trim().equals(""))
				csUrl = OpenTenureApplication._DEFAULT_COMMUNITY_SERVER;

			String url = String.format(CommunityServerAPIUtilities.HTTPS_LOGIN,
					csUrl, OpenTenureApplication.getInstance()
							.getLanguageCode(), username, password);

			HttpGet request = new HttpGet(url);

			/* Preparing to store coockies */
			CookieStore CS = OpenTenureApplication.getCoockieStore();
			HttpContext context = new BasicHttpContext();
			context.setAttribute(ClientContext.COOKIE_STORE, CS);

			AndroidHttpClient client = OpenTenureApplication.getHttpClient();

			/* Calling the Server.... */
			HttpResponse response = client.execute(request, context);

			Log.d("CommunityServerAPI",
					"Login Status line " + response.getStatusLine());

			// if (response.getStatusLine().getStatusCode() ==
			// (HttpStatus.SC_OK)) {
			//
			// String json = CommunityServerAPIUtilities.Slurp(response
			// .getEntity().getContent(), 1024);
			//
			// /* parsing the response in a Login object */
			// Gson gson = new Gson();
			// Login login = gson.fromJson(json, Login.class);

			setServerProtoVersion(response);

			switch (response.getStatusLine().getStatusCode()) {

			case 200:
				OpenTenureApplication.setCoockieStore(CS);
				Log.d("CommunityServerAPI", "Login status : 200");
				OpenTenureApplication.closeHttpClient();
				return 200;

			case 401:
				Log.d("CommunityServerAPI", "Login status : 401");
				OpenTenureApplication.closeHttpClient();
				return 401;

			default:
				Log.d("CommunityServerAPI", "Login status : default");
				OpenTenureApplication.closeHttpClient();
				return 0;
			}

		}

		catch (ConnectTimeoutException ct) {

			Log.d("CommunityServerAPI", ct.getMessage());
			ct.printStackTrace();
			return 80;

		} catch (Throwable ex) {

			Log.d("CommunityServerAPI", "An error has occurred during Login "
					+ ex.getMessage());
			ex.printStackTrace();
			return 0;
		}

	}

	/**
	 * 
	 * The logout API
	 * 
	 * *
	 * 
	 * @return 200 in case of success 401 in case of fail , 0 in case of generic
	 *         error 80 in case of connection timed out
	 */
	public static int logout() {

		try {

			SharedPreferences OpenTenurePreferences = PreferenceManager
					.getDefaultSharedPreferences(OpenTenureApplication
							.getContext());

			String csUrl = OpenTenureApplication.getInstance().getServerUrl();

			if (csUrl.trim().equals(""))
				csUrl = OpenTenureApplication._DEFAULT_COMMUNITY_SERVER;

			String url = String.format(
					CommunityServerAPIUtilities.HTTPS_LOGOUT, csUrl,
					OpenTenureApplication.getInstance().getLanguageCode());
			HttpGet request = new HttpGet(url);

			/* Preparing to store coockies */
			CookieStore CS = OpenTenureApplication.getCoockieStore();
			HttpContext context = new BasicHttpContext();
			context.setAttribute(ClientContext.COOKIE_STORE, CS);

			AndroidHttpClient client = OpenTenureApplication.getHttpClient();

			/* Calling the Server.... */
			HttpResponse response = client.execute(request, context);

			Log.d("CommunityServerAPI", response.getStatusLine().toString());

			setServerProtoVersion(response);

			switch (response.getStatusLine().getStatusCode()) {

			case 200:

				OpenTenureApplication.setCoockieStore(CS);

				return 200;

			case 401:

				return 401;

			default:

				return 0;
			}

		} catch (ConnectTimeoutException ct) {

			Log.d("CommunityServerAPI",
					"Logout ConnectTimeoutException" + ct.getMessage());
			ct.printStackTrace();
			return 80;

		}

		catch (UnknownHostException uhe) {

			System.out.println("UHE CAUSE " + uhe.getCause());
			Log.d("CommunityServerAPI",
					"Logout UnknownHostException" + uhe.getMessage());
			uhe.printStackTrace();
			return 1;
		} catch (Throwable ex) {

			Log.d("CommunityServerAPI", "Logout Exception " + ex.getMessage());
			ex.printStackTrace();
			return 0;
		}
	}

	public static String getCurrentUser() {
		String csUrl = OpenTenureApplication.getInstance().getServerUrl();

		String url = String.format(CommunityServerAPIUtilities.HTTPS_GET_CURRENT_USER, csUrl, OpenTenureApplication.getInstance().getLanguageCode());
		HttpGet request = new HttpGet(url);
		AndroidHttpClient client = OpenTenureApplication.getHttpClient();
		CookieStore coockieStore = OpenTenureApplication.getCoockieStore();
		HttpContext context = new BasicHttpContext();
		context.setAttribute(ClientContext.COOKIE_STORE, coockieStore);

		try {
			HttpResponse response = client.execute(request, context);
			setServerProtoVersion(response);

			if (response.getStatusLine().getStatusCode() == (HttpStatus.SC_OK)) {
				String userName = CommunityServerAPIUtilities.Slurp(response.getEntity().getContent(), 1024);
				return userName;
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static List<org.fao.sola.clients.android.opentenure.network.response.Claim> getAllClaims() {
		/*
		 * Creating the url to call
		 */
		String csUrl = OpenTenureApplication.getInstance().getServerUrl();

		String url = String.format(
				CommunityServerAPIUtilities.HTTPS_GETALLCLAIMS, csUrl,
				OpenTenureApplication.getInstance().getLanguageCode(),
				OpenTenureApplication.getInstance().getProject().getId());
		HttpGet request = new HttpGet(url);

		AndroidHttpClient client = OpenTenureApplication.getHttpClient();

		try {

			CookieStore CS = OpenTenureApplication.getCoockieStore();

			HttpContext context = new BasicHttpContext();
			context.setAttribute(ClientContext.COOKIE_STORE, CS);

			/* Calling the Server.... */
			HttpResponse response = client.execute(request, context);

			setServerProtoVersion(response);

			String json = CommunityServerAPIUtilities.Slurp(response
					.getEntity().getContent(), 1024);

			if (response.getStatusLine().getStatusCode() == (HttpStatus.SC_OK)) {

				Log.d("CommunityServerAPI", "GET ALL CLAIMS JSON RESPONSE "
						+ json);

				Type listType = new TypeToken<ArrayList<org.fao.sola.clients.android.opentenure.network.response.Claim>>() {
				}.getType();
				List<org.fao.sola.clients.android.opentenure.network.response.Claim> claimList = new Gson().fromJson(json, listType);

				return claimList;
			} else {
				Log.d("CommunityServerAPI", "GET ALL CLAIMS JSON RESPONSE " + json);
				return null;
			}

		} catch (Exception ex) {
			Log.d("CommunityServerAPI", "GET ALL CLAIMS error " + ex.getMessage());
			ex.printStackTrace();
			return null;
		}

	}

	public static List<org.fao.sola.clients.android.opentenure.network.response.Claim> getAllClaimsByBox(String[] coordinates) {

		/*
		 * Creating the url to call
		 */
		SharedPreferences OpenTenurePreferences = PreferenceManager.getDefaultSharedPreferences(OpenTenureApplication.getContext());

		String csUrl = OpenTenureApplication.getInstance().getServerUrl();

		String url = String.format(
				CommunityServerAPIUtilities.HTTPS_GETALLCLAIMSBYBOX, csUrl,
				OpenTenureApplication.getInstance().getLanguageCode(),
				coordinates[0], coordinates[1], coordinates[2], coordinates[3],
				OpenTenureApplication.getInstance().getProject().getId(),
				"100");
		HttpGet request = new HttpGet(url);

		AndroidHttpClient client = OpenTenureApplication.getHttpClient();

		try {

			Log.d("CommunityServerAPI", "GET ALL CLAIMS BY BOX JSON REQUEST "
					+ url);

			CookieStore CS = OpenTenureApplication.getCoockieStore();

			HttpContext context = new BasicHttpContext();
			context.setAttribute(ClientContext.COOKIE_STORE, CS);

			/* Calling the Server.... */
			HttpResponse response = client.execute(request, context);

			setServerProtoVersion(response);

			String json = CommunityServerAPIUtilities.Slurp(response
					.getEntity().getContent(), 1024);

			if (response.getStatusLine().getStatusCode() == (HttpStatus.SC_OK)) {

				Log.d("CommunityServerAPI",
						"GET ALL CLAIMS BY BOX JSON RESPONSE " + json);

				Type listType = new TypeToken<ArrayList<org.fao.sola.clients.android.opentenure.network.response.Claim>>() {
				}.getType();
				List<org.fao.sola.clients.android.opentenure.network.response.Claim> claimList = new Gson()
						.fromJson(json, listType);

				return claimList;

			} else {

				Log.d("CommunityServerAPI", "GET ALL CLAIMS JSON RESPONSE "
						+ json);
				return null;

			}

		} catch (Exception ex) {

			Log.d("CommunityServerAPI",
					"GET ALL CLAIMS error " + ex.getMessage());
			ex.printStackTrace();

			return null;

		}

	}

	public static ApiResponse withdrawClaim(String claimId) {

		/*
		 * Creating the url to call
		 */
		SharedPreferences OpenTenurePreferences = PreferenceManager
				.getDefaultSharedPreferences(OpenTenureApplication.getContext());

		String csUrl = OpenTenureApplication.getInstance().getServerUrl();

		if (csUrl.trim().equals(""))
			csUrl = OpenTenureApplication._DEFAULT_COMMUNITY_SERVER;

		String url = String.format(
				CommunityServerAPIUtilities.HTTPS_WITHDRAWCLAIM, csUrl,
				OpenTenureApplication.getInstance().getLanguageCode(), claimId);
		HttpGet request = new HttpGet(url);

		Log.d("CommunityServerAPI",
				"WITHDRAW request : " + request.getRequestLine());

		Log.d("CommunityServerAPI", " ");

		CookieStore CS = OpenTenureApplication.getCoockieStore();
		HttpContext context = new BasicHttpContext();
		context.setAttribute(ClientContext.COOKIE_STORE, CS);

		AndroidHttpClient client = OpenTenureApplication.getHttpClient();

		/* Calling the Server.... */
		try {
			HttpResponse response = client.execute(request, context);

			Log.d("CommunityServerAPI", "WITHDRAW Claim status line "
					+ response.getStatusLine());

			setServerProtoVersion(response);

			String json = CommunityServerAPIUtilities.Slurp(response
					.getEntity().getContent(), 1024);

			Log.d("CommunityServerAPI", "CLAIM JSON STRING " + json);

			Gson gson = new Gson();
			ApiResponse apiResponse = gson.fromJson(json, ApiResponse.class);
			apiResponse.setHttpStatusCode(response.getStatusLine()
					.getStatusCode());
			apiResponse.setClaimId(claimId);

			return apiResponse;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			ApiResponse apiResponse = new ApiResponse();

			apiResponse.setHttpStatusCode(100);
			apiResponse.setMessage(e.getMessage());
			apiResponse.setClaimId(claimId);

			return apiResponse;
		} catch (IllegalStateException ise) {

			// TODO Auto-generated catch block
			ise.printStackTrace();

			ApiResponse apiResponse = new ApiResponse();

			apiResponse.setHttpStatusCode(100);
			apiResponse.setMessage(ise.getMessage());
			apiResponse.setClaimId(claimId);
			return apiResponse;
		}

	}

	public static Claim getClaim(String claimId) {

		/*
		 * Creating the url to call
		 */
		SharedPreferences OpenTenurePreferences = PreferenceManager
				.getDefaultSharedPreferences(OpenTenureApplication.getContext());

		String csUrl = OpenTenureApplication.getInstance().getServerUrl();

		if (csUrl.trim().equals(""))
			csUrl = OpenTenureApplication._DEFAULT_COMMUNITY_SERVER;

		String url = String.format(CommunityServerAPIUtilities.HTTPS_GETCLAIM,
				csUrl, OpenTenureApplication.getInstance().getLanguageCode(),
				claimId);
		HttpGet request = new HttpGet(url);

		CookieStore CS = OpenTenureApplication.getCoockieStore();
		HttpContext context = new BasicHttpContext();
		context.setAttribute(ClientContext.COOKIE_STORE, CS);

		AndroidHttpClient client = OpenTenureApplication.getHttpClient();

		/* Calling the Server.... */
		try {
			HttpResponse response = client.execute(request, context);

			setServerProtoVersion(response);

			Log.d("CommunityServerAPI",
					"GET Claim status line " + response.getStatusLine());

			if (response.getStatusLine().getStatusCode() == (HttpStatus.SC_OK)) {

				String json = CommunityServerAPIUtilities.Slurp(response
						.getEntity().getContent(), 1024);

				Log.d("CommunityServerAPI", "CLAIM JSON STRING " + json);

				Gson gson = new Gson();
				Claim claim = gson.fromJson(json, Claim.class);

				Log.d("CommunityServerAPI",
						"CLAIM JSON GPSGEOMETRY " + claim.getGpsGeometry());

				return claim;

			} else {

				Log.d("CommunityServerAPI", "CLAIM not retrieved ");
				return null;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			return null;
		} catch (IllegalStateException ise) {

			ise.printStackTrace();
			return null;

		}

	}

	public static GetAttachmentResponse getAttachment(String attachmentId,
			long start, long offset) {

		GetAttachmentResponse methodResponse = new GetAttachmentResponse();

		/*
		 * Creating the url to call
		 */

		SharedPreferences OpenTenurePreferences = PreferenceManager
				.getDefaultSharedPreferences(OpenTenureApplication.getContext());

		String csUrl = OpenTenureApplication.getInstance().getServerUrl();

		if (csUrl.trim().equals(""))
			csUrl = OpenTenureApplication._DEFAULT_COMMUNITY_SERVER;

		String url = String.format(
				CommunityServerAPIUtilities.HTTPS_GETATTACHMENT, csUrl,
				attachmentId);
		HttpGet request = new HttpGet(url);

		/* Retrieve the attachment partially */
		if (offset > start)
			request.setHeader("Range", "bytes=" + start + "-" + offset);

		Log.d("CommunityServerAPI", "bytes=" + start + "-" + offset);

		AndroidHttpClient client = OpenTenureApplication.getHttpClient();

		/* Calling the Server.... */
		try {

			CookieStore CS = OpenTenureApplication.getCoockieStore();

			HttpContext context = new BasicHttpContext();
			context.setAttribute(ClientContext.COOKIE_STORE, CS);

			/* Calling the Server.... */
			HttpResponse response = client.execute(request, context);

			setServerProtoVersion(response);

			Log.d("CommunityServerAPI", "GET Attachment status line "
					+ response.getStatusLine());

			if (response.getStatusLine().getStatusCode() == (HttpStatus.SC_OK)) {

				byte[] byteArray = CommunityServerAPIUtilities.slurp(response
						.getEntity().getContent(), 1024);

				Log.d("CommunityServerAPI", "ATTACHMENT RETRIEVED SIZE"
						+ byteArray.length);

				methodResponse.setArray(byteArray);
				methodResponse.setHttpStatusCode(response.getStatusLine()
						.getStatusCode());
				methodResponse.setMessage(response.getStatusLine()
						.getReasonPhrase());

				org.apache.http.Header[] headers = response.getAllHeaders();
				for (int i = 0; i < headers.length; i++) {
					System.out.println("Head " + i + ": " + headers[i]);
				}

				if (response.getHeaders("ETag").length != 0)
					methodResponse.setMd5(response.getHeaders("ETag")[0]
							.getValue());
				else
					System.out.println("There's no ETag !!!!");

				return methodResponse;

			} else if (response.getStatusLine().getStatusCode() == (HttpStatus.SC_PARTIAL_CONTENT)) {

				org.apache.http.Header[] headers = response.getAllHeaders();

				for (int i = 0; i < headers.length; i++) {

					Log.d("CommunityServerAPI",
							"HEADER : " + headers[i].getName() + " "
									+ headers[i].getValue());
				}

				byte[] byteArray = CommunityServerAPIUtilities.slurp(response
						.getEntity().getContent(), 1024);

				Log.d("CommunityServerAPI",
						"ATTACHMENT partially retrieved. Size : "
								+ byteArray.length);

				methodResponse.setArray(byteArray);
				methodResponse.setHttpStatusCode(response.getStatusLine()
						.getStatusCode());
				methodResponse.setMessage(response.getStatusLine()
						.getReasonPhrase());
				methodResponse
						.setMd5(response.getHeaders("ETag")[0].getValue());
				return methodResponse;
			} else if (response.getStatusLine().getStatusCode() == (HttpStatus.SC_NOT_FOUND)) {

				Log.d("CommunityServerAPI", "ATTACHMENT NOT FOUND. Size ");

				methodResponse.setArray(null);
				methodResponse.setHttpStatusCode(response.getStatusLine()
						.getStatusCode());
				methodResponse.setMessage(response.getStatusLine()
						.getReasonPhrase());
				return methodResponse;

			} else {

				Log.d("CommunityServerAPI", "ATTACHMENT NOT RETRIEVED.");

				methodResponse.setArray(null);
				methodResponse.setHttpStatusCode(response.getStatusLine()
						.getStatusCode());
				methodResponse.setMessage(response.getStatusLine()
						.getReasonPhrase());
				return methodResponse;
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			methodResponse.setArray(null);
			methodResponse.setHttpStatusCode(400);
			methodResponse.setMessage("Error retrieving attachment");

			return methodResponse;
		} catch (IllegalStateException ise) {
			// TODO Auto-generated catch block
			ise.printStackTrace();

			methodResponse.setArray(null);
			methodResponse.setHttpStatusCode(400);
			methodResponse.setMessage("Error retrieving attachment");

			return methodResponse;
		}

	}

	public static List<LandUseResponse> getLandUses() {
		return getList(LandUseResponse[].class, false, CommunityServerAPIUtilities.HTTPS_GETLANDUSE);
	}

	public static List<ProjectResponse> getProjects() {
		return getList(ProjectResponse[].class, true, CommunityServerAPIUtilities.HTTPS_GET_PROJECTS);
	}

	public static <T> List<T> getList(final Class<T[]> clazz, boolean includeCookies, String serverMethod, String... args) {
		SharedPreferences OpenTenurePreferences = PreferenceManager
				.getDefaultSharedPreferences(OpenTenureApplication.getContext());

		String csUrl = OpenTenureApplication.getInstance().getServerUrl();
		List<String> stringArgs = new ArrayList<String>();
		stringArgs.add(csUrl);

		String url;

		if(args != null && args.length > 0) {
			for(String a: args) {
				stringArgs.add(a);
			}
		}

		url = String.format(serverMethod, stringArgs.toArray());
		HttpGet request = new HttpGet(url);
		AndroidHttpClient client = OpenTenureApplication.getHttpClient();

		try {
			HttpResponse response;

			if(includeCookies){
				CookieStore coockieStore = OpenTenureApplication.getCoockieStore();
				HttpContext context = new BasicHttpContext();
				context.setAttribute(ClientContext.COOKIE_STORE, coockieStore);
				response = client.execute(request, context);
			} else {
				response = client.execute(request);
			}

			setServerProtoVersion(response);
			String json = CommunityServerAPIUtilities.Slurp(response.getEntity().getContent(), 1024);

			if (response.getStatusLine().getStatusCode() == (HttpStatus.SC_OK)) {
				Log.d("CommunityServerAPI", "GET " + clazz.getName() + " JSON RESPONSE " + json);

				final T[] jsonToObject = new Gson().fromJson(json, clazz);
				List<T> types = Arrays.asList(jsonToObject);

				if (types != null)
					Log.d("CommunityServerAPI", "RETRIEVED " + clazz.getName() + " " + types.size());

				return types;
			} else {
				Log.d("CommunityServerAPI",
						"GET ALL " + clazz.getName() + " NOT SUCCEDED : HTTP STATUS "
								+ response.getStatusLine().getStatusCode()
								+ "  "
								+ response.getStatusLine().getReasonPhrase());
				return null;
			}
		} catch (java.net.SocketException se) {
			Log.d("CommunityServerAPI", "GET ALL " + clazz.getName() + " NETWORK ERROR SE " + se.getMessage());
			se.printStackTrace();
			OpenTenureApplication.getInstance().setNetworkError(true);
			return null;
		} catch (java.net.SocketTimeoutException stoe) {
			Log.d("CommunityServerAPI", "GET ALL " + clazz.getName() + " NETWORK ERROR STOE "	+ stoe.getMessage());
			stoe.printStackTrace();
			OpenTenureApplication.getInstance().setNetworkError(true);
			return null;
		} catch (javax.net.ssl.SSLException ssle) {
			Log.d("CommunityServerAPI", "GET ALL " + clazz.getName() + " NETWORK ERROR SSLE "	+ ssle.getMessage());
			ssle.printStackTrace();
			OpenTenureApplication.getInstance().setNetworkError(true);
			return null;
		}
		catch (Exception ex) {
			Log.d("CommunityServerAPI","GET ALL \" + typeDescription + \" ERROR " + ex.getMessage());
			ex.printStackTrace();
			return null;
		}
	}

	public static List<LanguageResponse> getLanguages() {
		return getList(LanguageResponse[].class, false, CommunityServerAPIUtilities.HTTPS_GETLANGUAGES);
	}

	public static List<IdTypeResponse> getIdTypes() {
		return getList(IdTypeResponse[].class, false, CommunityServerAPIUtilities.HTTPS_GETIDTYPES);
	}

	public static List<BoundaryStatusResponse> getBoundaryStatuses() {
		return getList(BoundaryStatusResponse[].class, false, CommunityServerAPIUtilities.HTTPS_GET_BOUNDARY_STATUSES);
	}

	public static List<BoundaryTypeResponse> getBoundaryTypes() {
		return getList(BoundaryTypeResponse[].class, false, CommunityServerAPIUtilities.HTTPS_GET_BOUNDARY_TYPES);
	}

	public static List<BoundaryResponse> getBoundaries() {
		return getList(BoundaryResponse[].class, true, CommunityServerAPIUtilities.HTTPS_GET_BOUNDARIES, OpenTenureApplication.getInstance().getLanguageCode());
	}

	public static List<ClaimTypeResponse> getClaimTypes() {

		SharedPreferences OpenTenurePreferences = PreferenceManager
				.getDefaultSharedPreferences(OpenTenureApplication.getContext());

		String csUrl = OpenTenureApplication.getInstance().getServerUrl();

		if (csUrl.trim().equals(""))
			csUrl = OpenTenureApplication._DEFAULT_COMMUNITY_SERVER;

		String url = String.format(
				CommunityServerAPIUtilities.HTTPS_GETCLAIMTYPES, csUrl);
		HttpGet request = new HttpGet(url);
		AndroidHttpClient client = OpenTenureApplication.getHttpClient();

		try {

			HttpResponse response = client.execute(request);

			setServerProtoVersion(response);

			String json = CommunityServerAPIUtilities.Slurp(response
					.getEntity().getContent(), 1024);

			if (response.getStatusLine().getStatusCode() == (HttpStatus.SC_OK)) {

				Log.d("CommunityServerAPI",
						"GET ALL CLAIM TYPES JSON RESPONSE " + json);

				Type listType = new TypeToken<ArrayList<ClaimTypeResponse>>() {
				}.getType();
				List<ClaimTypeResponse> claimTypeResponseList = new Gson()
						.fromJson(json, listType);

				if (claimTypeResponseList != null)
					Log.d("CommunityServerAPI", "RETRIEVED CLAIM TYPES LIST"
							+ claimTypeResponseList.size());

				return claimTypeResponseList;

			} else {

				Log.d("CommunityServerAPI",
						"GET ALL CLAIM TYPES NOT SUCCEDED : HTTP STATUS "
								+ response.getStatusLine().getStatusCode()
								+ "  "
								+ response.getStatusLine().getReasonPhrase());

				return null;

			}

		} catch (java.net.ConnectException ce) {
			Log.d("CommunityServerAPI", "GET ALL CLAIM TYPES NETWORK ERROR CE"
					+ ce.getMessage());
			ce.printStackTrace();
			OpenTenureApplication.getInstance().setNetworkError(true);
			return null;

		} catch (java.net.SocketException se) {

			Log.d("CommunityServerAPI", "GET ALL CLAIM TYPES NETWORK ERROR SE"
					+ se.getMessage());
			se.printStackTrace();
			OpenTenureApplication.getInstance().setNetworkError(true);
			return null;
		} catch (java.net.SocketTimeoutException stoe) {

			Log.d("CommunityServerAPI", "GET ALL CLAIM NETWORK ERROR STOE"
					+ stoe.getMessage());
			stoe.printStackTrace();
			OpenTenureApplication.getInstance().setNetworkError(true);
			return null;
		}

		catch (javax.net.ssl.SSLException ssle) {

			Log.d("CommunityServerAPI", "GET ALL CLAIM NETWORK ERROR SSLE"
					+ ssle.getMessage());
			ssle.printStackTrace();
			OpenTenureApplication.getInstance().setNetworkError(true);
			return null;
		}

		catch (Exception ex) {

			Log.d("CommunityServerAPI",
					"GET ALL CLAIM TYPES ERROR " + ex.getMessage());
			ex.printStackTrace();
			return null;

		}

	}

	public static List<RefDataResponse> getDocumentTypes() {
		return getList(RefDataResponse[].class, false, CommunityServerAPIUtilities.HTTPS_GETDOCUMENTYPES);
	}

	public static SaveClaimResponse saveClaim(String claim) {

		SharedPreferences OpenTenurePreferences = PreferenceManager
				.getDefaultSharedPreferences(OpenTenureApplication.getContext());

		String csUrl = OpenTenureApplication.getInstance().getServerUrl();

		if (csUrl.trim().equals(""))
			csUrl = OpenTenureApplication._DEFAULT_COMMUNITY_SERVER;

		String url = String.format(CommunityServerAPIUtilities.HTTPS_SAVECLAIM,
				csUrl, OpenTenureApplication.getInstance().getLanguageCode());

		HttpPost request = new HttpPost(url);

		StringEntity entity;
		try {
			entity = new StringEntity(claim, HTTP.UTF_8);
			entity.setContentType("application/json");
			request.setEntity(entity);

			AndroidHttpClient client = OpenTenureApplication.getHttpClient();

			CookieStore CS = OpenTenureApplication.getCoockieStore();

			HttpContext context = new BasicHttpContext();
			context.setAttribute(ClientContext.COOKIE_STORE, CS);

			/* Calling the Server.... */
			HttpResponse response = client.execute(request, context);

			setServerProtoVersion(response);

			Log.d("CommunityServerAPI",
					"saveClaim status line " + response.getStatusLine());

			String json = CommunityServerAPIUtilities.Slurp(response.getEntity().getContent(), 1024);

			Log.d("CommunityServerAPI", "SAVE CLAIM JSON RESPONSE " + json);

			Gson gson = new Gson();
			SaveClaimResponse saveResponse = gson.fromJson(json,
					SaveClaimResponse.class);

			Log.d("CommunityServerAPI", "SAVE CLAIM JSON RESPONSE "
					+ response.getStatusLine().getStatusCode());
			saveResponse.setHttpStatusCode(response.getStatusLine()
					.getStatusCode());

			return saveResponse;

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();

			SaveClaimResponse saveResponse = new SaveClaimResponse();
			saveResponse.setHttpStatusCode(110);
			saveResponse.setMessage(e.getMessage());

			return saveResponse;

		} catch (UnknownHostException uhe) {

			uhe.printStackTrace();

			SaveClaimResponse saveResponse = new SaveClaimResponse();
			saveResponse.setHttpStatusCode(100);
			saveResponse.setMessage("Unknown Host Exception : Network failure");

			return saveResponse;
		} catch (java.net.SocketException se) {

			se.printStackTrace();

			SaveClaimResponse saveResponse = new SaveClaimResponse();
			saveResponse.setHttpStatusCode(100);
			saveResponse.setMessage("Socket Exception : Network failure");

			return saveResponse;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			SaveClaimResponse saveResponse = new SaveClaimResponse();
			saveResponse.setHttpStatusCode(100);
			saveResponse.setMessage("IOException Exception : Network failure");

			return saveResponse;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			SaveClaimResponse saveResponse = new SaveClaimResponse();
			saveResponse.setHttpStatusCode(400);
			saveResponse.setMessage("Exception : " + e.getMessage());

			return saveResponse;
		}
	}

	public static ResultResponse saveBoundary(String boundaryJson) {

		SharedPreferences OpenTenurePreferences = PreferenceManager.getDefaultSharedPreferences(OpenTenureApplication.getContext());

		String csUrl = OpenTenureApplication.getInstance().getServerUrl();

		if (csUrl.trim().equals(""))
			csUrl = OpenTenureApplication._DEFAULT_COMMUNITY_SERVER;

		String url = String.format(CommunityServerAPIUtilities.HTTPS_SAVE_BOUNDARY,
				csUrl, OpenTenureApplication.getInstance().getLanguageCode());

		HttpPost request = new HttpPost(url);

		StringEntity entity;
		try {
			entity = new StringEntity(boundaryJson, HTTP.UTF_8);
			entity.setContentType("application/json");
			request.setEntity(entity);

			AndroidHttpClient client = OpenTenureApplication.getHttpClient();

			CookieStore CS = OpenTenureApplication.getCoockieStore();

			HttpContext context = new BasicHttpContext();
			context.setAttribute(ClientContext.COOKIE_STORE, CS);

			/* Calling the Server.... */
			HttpResponse response = client.execute(request, context);

			setServerProtoVersion(response);
			String json = CommunityServerAPIUtilities.Slurp(response.getEntity().getContent(), 1024);

			Gson gson = new Gson();
			ResultResponse saveResponse = gson.fromJson(json, ResultResponse.class);
			saveResponse.setHttpStatusCode(response.getStatusLine().getStatusCode());

			return saveResponse;

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();

			ResultResponse saveResponse = new ResultResponse();
			saveResponse.setHttpStatusCode(110);
			saveResponse.setMessage(e.getMessage());

			return saveResponse;

		} catch (UnknownHostException uhe) {

			uhe.printStackTrace();

			ResultResponse saveResponse = new ResultResponse();
			saveResponse.setHttpStatusCode(100);
			saveResponse.setMessage("Unknown Host Exception : Network failure");

			return saveResponse;
		} catch (java.net.SocketException se) {

			se.printStackTrace();

			ResultResponse saveResponse = new ResultResponse();
			saveResponse.setHttpStatusCode(100);
			saveResponse.setMessage("Socket Exception : Network failure");

			return saveResponse;
		} catch (IOException e) {
			e.printStackTrace();

			ResultResponse saveResponse = new ResultResponse();
			saveResponse.setHttpStatusCode(100);
			saveResponse.setMessage("IOException Exception : Network failure");

			return saveResponse;
		} catch (Exception e) {
			e.printStackTrace();

			ResultResponse saveResponse = new ResultResponse();
			saveResponse.setHttpStatusCode(400);
			saveResponse.setMessage("Exception : " + e.getMessage());

			return saveResponse;
		}
	}

	public static SaveAttachmentResponse saveAttachment(String attachment, String attachmentId) {
		SharedPreferences OpenTenurePreferences = PreferenceManager
				.getDefaultSharedPreferences(OpenTenureApplication.getContext());

		String csUrl = OpenTenureApplication.getInstance().getServerUrl();

		if (csUrl.trim().equals(""))
			csUrl = OpenTenureApplication._DEFAULT_COMMUNITY_SERVER;

		String url = String.format(
				CommunityServerAPIUtilities.HTTPS_SAVEATTACHMENT, csUrl,
				OpenTenureApplication.getInstance().getLanguageCode());

		HttpPost request = new HttpPost(url);
		SaveAttachmentResponse saveAttachmentResponse = null;
		StringEntity entity;
		try {
			Log.d("CommunityServerAPI", "saveAttachment payload " + attachment);

			entity = new StringEntity(attachment, HTTP.UTF_8);
			entity.setContentType("application/json");
			request.setEntity(entity);

			AndroidHttpClient client = OpenTenureApplication.getHttpClient();

			CookieStore CS = OpenTenureApplication.getCoockieStore();

			HttpContext context = new BasicHttpContext();
			context.setAttribute(ClientContext.COOKIE_STORE, CS);

			/* Calling the Server.... */

			HttpResponse response = client.execute(request, context);

			setServerProtoVersion(response);

			Log.d("CommunityServerAPI", "saveAttachment HTTP status line "
					+ response.getStatusLine());

			String json = CommunityServerAPIUtilities.Slurp(response
					.getEntity().getContent(), 1024);

			Log.d("CommunityServerAPI", "SAVE ATTACHMENT JSON RESPONSE " + json);

			Gson gson = new Gson();
			saveAttachmentResponse = gson.fromJson(json,
					SaveAttachmentResponse.class);

			saveAttachmentResponse.setHttpStatusCode(response.getStatusLine()
					.getStatusCode());

			saveAttachmentResponse.setAttachmentId(attachmentId);

		} catch (UnknownHostException ex) {

			SaveAttachmentResponse sar = new SaveAttachmentResponse();
			sar.setHttpStatusCode(100);
			sar.setAttachmentId(attachmentId);
			sar.setMessage(ex.getMessage());

			Log.d("CommunityServerAPI", "saveAttachment UnknownHostException "
					+ ex.getMessage());
			ex.printStackTrace();
			return sar;
		} catch (Throwable ex) {

			SaveAttachmentResponse sar = new SaveAttachmentResponse();
			sar.setHttpStatusCode(105);
			sar.setAttachmentId(attachmentId);
			sar.setMessage(ex.getMessage());

			Log.d("CommunityServerAPI",
					"saveAttachment Error " + ex.getMessage());
			ex.printStackTrace();
			return sar;
		}

		return saveAttachmentResponse;

	}

	public static ApiResponse uploadChunk(String payload, byte[] chunk) {

		Log.d("CommunityServerAPI", "chunk descriptor" + payload);

		String csUrl = OpenTenureApplication.getInstance().getServerUrl();

		String url = String.format(
				CommunityServerAPIUtilities.HTTPS_UPLOADCHUNK, csUrl,
				OpenTenureApplication.getInstance().getLanguageCode(),
				OpenTenureApplication.getInstance().getProject().getId());

		HttpPost request = new HttpPost(url);
		ApiResponse apiResponse = null;

		MultipartEntityBuilder entity = MultipartEntityBuilder.create();
		try {

			entity.addTextBody("descriptor", payload);
			entity.addBinaryBody("chunk", chunk);
			entity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			request.setEntity(entity.build());

			/* Preparing the client */
			AndroidHttpClient client = OpenTenureApplication.getHttpClient();
			CookieStore CS = OpenTenureApplication.getCoockieStore();
			HttpContext context = new BasicHttpContext();
			context.setAttribute(ClientContext.COOKIE_STORE, CS);

			/* Calling the Server.... */

			HttpResponse response = client.execute(request, context);

			setServerProtoVersion(response);

			String json = CommunityServerAPIUtilities.Slurp(response
					.getEntity().getContent(), 1024);

			Log.d("CommunityServerAPI", "UPLOAD CHUNK JSON RESPONSE " + json);

			Gson gson = new Gson();
			apiResponse = gson.fromJson(json, ApiResponse.class);
			apiResponse.setHttpStatusCode(response.getStatusLine()
					.getStatusCode());

		} catch (UnknownHostException uhe) {

			apiResponse = new ApiResponse();
			apiResponse.setHttpStatusCode(100);
			apiResponse.setMessage("uploadChunk error :" + uhe.getMessage());

			Log.d("CommunityServerAPI",
					"uploadChunk error : " + uhe.getMessage());
			uhe.printStackTrace();
			return apiResponse;
		} catch (Throwable ex) {
			apiResponse = new ApiResponse();
			apiResponse.setHttpStatusCode(105);
			apiResponse.setMessage("uploadChunk error :" + ex.getMessage());

			Log.d("CommunityServerAPI",
					"uploadChunk error : " + ex.getMessage());
			ex.printStackTrace();
			return apiResponse;
		}
		return apiResponse;

	}

	public static SaveAttachmentResponse addClaimantAttachment(String claimId,
			String attachmentId) {

		Log.d("CommunityServerAPI", "ADD CLAIMANT ATTACHMENT : " + attachmentId);

		SharedPreferences OpenTenurePreferences = PreferenceManager
				.getDefaultSharedPreferences(OpenTenureApplication.getContext());

		String csUrl = OpenTenureApplication.getInstance().getServerUrl();

		String url = String.format(
				CommunityServerAPIUtilities.HTTPS_ADDCLAIMATTACHMENT, csUrl,
				OpenTenureApplication.getInstance().getLanguageCode(), claimId,
				attachmentId);

		HttpGet request = new HttpGet(url);

		AndroidHttpClient client = OpenTenureApplication.getHttpClient();

		try {
			CookieStore CS = OpenTenureApplication.getCoockieStore();
			HttpContext context = new BasicHttpContext();
			context.setAttribute(ClientContext.COOKIE_STORE, CS);

			HttpResponse response = client.execute(request, context);

			setServerProtoVersion(response);

			String json = CommunityServerAPIUtilities.Slurp(response
					.getEntity().getContent(), 1024);

			SaveAttachmentResponse saveAttRes;

			Log.d("CommunityServerAPI", " ADD CLAIMANT ATTACHMENT RESPONSE : "
					+ json);

			Gson gson = new Gson();
			saveAttRes = gson.fromJson(json, SaveAttachmentResponse.class);
			saveAttRes.setHttpStatusCode(response.getStatusLine()
					.getStatusCode());
			saveAttRes.setClaimId(claimId);

			return saveAttRes;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			SaveAttachmentResponse res = new SaveAttachmentResponse();

			res.setHttpStatusCode(100);
			res.setMessage(e.getMessage());
			res.setClaimId(claimId);
			res.setAttachmentId(attachmentId);

			return res;
		}
	}

}
