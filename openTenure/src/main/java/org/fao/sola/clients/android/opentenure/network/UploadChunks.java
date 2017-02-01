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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

import org.fao.sola.clients.android.opentenure.model.Attachment;

import org.fao.sola.clients.android.opentenure.model.MD5;
import org.fao.sola.clients.android.opentenure.network.API.CommunityServerAPI;
import org.fao.sola.clients.android.opentenure.network.response.ApiResponse;
import org.fao.sola.clients.android.opentenure.network.response.UploadChunkPayload;
import org.fao.sola.clients.android.opentenure.network.response.UploadChunksResponse;
import org.h2.util.New;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * task that uploads the attachment file in chunks
 * 
 * */
public class UploadChunks {

	public UploadChunksResponse execute(String attachmentId) {

		boolean success = false;
		DataInputStream dis = null;

		UploadChunksResponse upResponse = new UploadChunksResponse();

		try {

			Attachment attachment = Attachment.getAttachment(attachmentId);

			File toTransfer = new File(attachment.getPath());

			FileInputStream fis = new FileInputStream(toTransfer);
			MessageDigest digest = MessageDigest.getInstance("MD5");

			dis = new DataInputStream(fis);

			Integer startPosition = 0;

			for (;;) {

				byte[] chunk = new byte[500000];

				int rsz = dis.read(chunk, 0, chunk.length);
				if (rsz > 0) {
					UploadChunkPayload payload = new UploadChunkPayload();

					if (rsz < 500000)
						chunk = Arrays.copyOfRange(chunk, 0, rsz);

					payload.setMd5(MD5.calculateMD5(chunk));

					payload.setAttachmentId(attachmentId);
					payload.setClaimId(attachment.getClaimId());
					payload.setId(UUID.randomUUID().toString());
					payload.setSize((long) rsz);
					payload.setStartPosition(startPosition);

					startPosition = startPosition + rsz;

					Gson gson = new GsonBuilder()
							.setPrettyPrinting()
							.serializeNulls()
							.setFieldNamingPolicy(
									FieldNamingPolicy.UPPER_CAMEL_CASE)
							.create();
					String json = gson.toJson(payload);

					/***
					 * Calling the server.....
					 * ***/

					ApiResponse res = CommunityServerAPI.uploadChunk(json,
							chunk);

					if (res.getHttpStatusCode() == 200) {

						attachment.updateUploadedBytes(startPosition);
						success = true;

					}
					if (res.getHttpStatusCode() == 100) {
						success = false;
						break;
					}
					if (res.getHttpStatusCode() == 105) {
						success = false;
						break;
					}

				} else
					break;
			}

			upResponse.setAttachmentId(attachmentId);
			upResponse.setSuccess(success);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				dis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return upResponse;
	}

}
