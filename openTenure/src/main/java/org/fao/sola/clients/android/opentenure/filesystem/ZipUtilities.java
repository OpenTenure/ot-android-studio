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
package org.fao.sola.clients.android.opentenure.filesystem;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.model.Claim;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import android.content.Context;
import android.media.MediaScannerConnection;

public class ZipUtilities {

	public static boolean AddFilesWithAESEncryption(String password,
			String claimId) {

		try {

			String claimName = Claim.getClaim(claimId).getName();			
			
			/*Removing special characters from claim name */
			claimName = FileSystemUtilities.cleanBySpecial(claimName);

			SimpleDateFormat sdf = new SimpleDateFormat();

			sdf.applyPattern("yyyy-MM-dd");

			Context context = OpenTenureApplication.getContext();

			ZipFile zipFile = new ZipFile(FileSystemUtilities.getExportFolder()
					.getAbsolutePath()
					+ File.separator
					+ "Claim_"
					+ claimName
					+ "_" + sdf.format(new Date()) + ".zip");

			// Build the list of files to be added in the array list
			// Objects of type File have to be added to the ArrayList
			// ArrayList filesToAdd = new ArrayList();
			// filesToAdd.add(new File("c:\\ZipTest\\sample.txt"));
			// filesToAdd.add(files[0]);
			// filesToAdd.add(new File("c:\\ZipTest\\pippo.pdf"));
			// filesToAdd.add(new File("c:\\ZipTest\\setup.docx"));

			// Initiate Zip Parameters which define various properties such
			// as compression method, etc. More parameters are explained in
			// other
			// examples
			ZipParameters parameters = new ZipParameters();
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set
																			// compression
																			// method
																			// to
																			// deflate
																			// compression

			// Set the compression level. This value has to be in between 0 to 9
			// Several predefined compression levels are available
			// DEFLATE_LEVEL_FASTEST - Lowest compression level but higher speed
			// of compression
			// DEFLATE_LEVEL_FAST - Low compression level but higher speed of
			// compression
			// DEFLATE_LEVEL_NORMAL - Optimal balance between compression
			// level/speed
			// DEFLATE_LEVEL_MAXIMUM - High compression level with a compromise
			// of speed
			// DEFLATE_LEVEL_ULTRA - Highest compression level but low speed
			parameters
					.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_FASTEST);

			// Set the encryption flag to true
			// If this is set to false, then the rest of encryption properties
			// are ignored
			if (password != null && !password.equals(""))
				parameters.setEncryptFiles(true);
			else
				parameters.setEncryptFiles(false);

			// Set the encryption method to AES Zip Encryption
			parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);

			// Set AES Key strength. Key strengths available for AES encryption
			// are:
			// AES_STRENGTH_128 - For both encryption and decryption
			// AES_STRENGTH_192 - For decryption only
			// AES_STRENGTH_256 - For both encryption and decryption
			// Key strength 192 cannot be used for encryption. But if a zip file
			// already has a
			// file encrypted with key strength of 192, then Zip4j can decrypt
			// this file
			parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);

			// Set password
			if (password != null && !password.equals(""))
				parameters.setPassword(password);

			// Now add files to the zip file
			// Note: To add a single file, the method addFile can be used
			// Note: If the zip file already exists and if this zip file is a
			// split file
			// then this method throws an exception as Zip Format Specification
			// does not
			// allow updating split zip files

			// zipFile.addFiles(filesToAdd, parameters);
			zipFile.addFolder(FileSystemUtilities.getClaimFolder(claimId),
					parameters);

			MediaScannerConnection.scanFile(context, new String[] { zipFile
					.getFile().getAbsolutePath() }, null, null);

			return true;
		} catch (Throwable e) {
			e.printStackTrace();
			return false;

		}
	}

	public static int UnzipFilesWithAESEncryption(String password, File zip) {

		try {
			ZipFile zipFile = new ZipFile(zip);

			if (zipFile.isEncrypted()) {

				System.out.println("Is a crypted zip, try password :"
						+ password);
				zipFile.setPassword(password.toCharArray());
			}

			@SuppressWarnings("unchecked")
			List<FileHeader> fileHeaders = zipFile.getFileHeaders();

			for (FileHeader fileHeader : fileHeaders) {

				if (fileHeader.getFileName().contains("claim.json")) {

					zipFile.extractAll(zip.getParent());

					return 1;
				}

			}

			return 3;

		} catch (ZipException e) {

			if (e.getMessage().contains("Wrong Password for file")) {

				return 2;

			}

			if (e.getMessage().contains("empty or null password provided")) {

				return 2;

			}

			e.printStackTrace();
			return 0;
		}

	}

}
