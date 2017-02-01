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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.model.Attachment;
import org.fao.sola.clients.android.opentenure.model.AttachmentStatus;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.network.API.CommunityServerAPIUtilities;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class FileSystemUtilities {

	private static String _CLAIMS_FOLDER = "claims";
	private static String _CLAIMANTS_FOLDER = "claimants";
	private static String _CLAIM_PREFIX = "claim_";
	private static String _CLAIMANT_PREFIX = "claimant_";
	private static String _ATTACHMENT_FOLDER = "attachments";
	private static String _OPEN_TENURE_FOLDER = "Open Tenure";
	private static String _CERTIFICATES = "Certificates";
	private static String _IMPORT = "Import";
	private static String _EXPORT = "Export";
	private static String _MULTIPAGE = "multipage";
	public static String _CSV_FIELD_SEPARATOR = ",";
	public static String _CSV_REC_TERMINATOR = "\n";

	/**
	 * 
	 * Create the folder that contains all the claims under the application file
	 * system
	 * 
	 */

	public static boolean createClaimsFolder() {

		if (isExternalStorageWritable()) {

			try {
				Context context = OpenTenureApplication.getContext();
				File appFolder = context.getExternalFilesDir(null);
				new File(appFolder, _CLAIMS_FOLDER).mkdir();
				File claimsFolder = new File(appFolder.getAbsoluteFile() + File.separator + _CLAIMS_FOLDER);

				if (claimsFolder.exists() && claimsFolder.isDirectory())
					return true;
				else
					return false;

			} catch (Exception e) {
				return false;
			}
		} else {
			return false;
		}

	}

	/**
	 * 
	 * Create the OpenTenure folder under the the public file system Here will
	 * be exported the compressed claim
	 * 
	 **/

	public static boolean createOpenTenureFolder() {

		if (isExternalStorageWritable()) {

			File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
			File ot = new File(path.getParentFile(), _OPEN_TENURE_FOLDER);

			if (ot.mkdir() && ot.isDirectory()) {

				Log.d("FileSystemUtilities", "Created Open Tenure Folder");
				return true;
			}
			return false;
		}

		else
			return false;

	}

	/**
	 * 
	 * Create the Certificates folder under the the public file system Here will
	 * be exported the compressed claim
	 * 
	 **/

	public static boolean createCertificatesFolder() {

		if (isExternalStorageWritable()) {

			File ot = new File(getOpentenureFolder(), _CERTIFICATES);

			if (ot.mkdir() && ot.isDirectory()) {

				Log.d("FileSystemUtilities", "Created Certificates Folder");
				return true;
			}
			return false;
		}

		else
			return false;

	}

	/**
	 * 
	 * Create the Import folder under the the Open Tenure file system. Here will
	 * be unzipped the zipped claim
	 * 
	 **/

	public static boolean createImportFolder() {

		if (isExternalStorageWritable()) {

			File ot = new File(getOpentenureFolder(), _IMPORT);

			if (ot.mkdir() && ot.isDirectory()) {

				Log.d("FileSystemUtilities", "Created Import Folder");
				return true;
			}
			return false;
		}

		else
			return false;

	}

	/**
	 * 
	 * Create the Export folder under the Open Tenure file system. Here will be
	 * stored the zipped claims
	 * 
	 **/

	public static boolean createExportFolder() {

		if (isExternalStorageWritable()) {

			File ot = new File(getOpentenureFolder(), _EXPORT);

			if (ot.mkdir() && ot.isDirectory()) {

				Log.d("FileSystemUtilities", "Created Export Folder");
				return true;
			}
			return false;
		}

		else
			return false;

	}

	public static boolean createClaimantsFolder() {

		if (isExternalStorageWritable()) {

			try {
				Context context = OpenTenureApplication.getContext();
				File appFolder = context.getExternalFilesDir(null);
				new File(appFolder, _CLAIMANTS_FOLDER).mkdir();
				File claimantsFolder = new File(appFolder.getAbsoluteFile() + File.separator + _CLAIMANTS_FOLDER);

				if (claimantsFolder.exists() && claimantsFolder.isDirectory())
					return true;
				else
					return false;

			} catch (Exception e) {
				return false;
			}
		} else {
			return false;
		}

	}

	public static boolean createClaimFileSystem(String claimID) {

		File claimFolder = null;
		File claimsFolder = null;

		try {

			claimsFolder = getClaimsFolder();

			new File(claimsFolder, _CLAIM_PREFIX + claimID).mkdir();

			claimFolder = new File(claimsFolder, _CLAIM_PREFIX + claimID);

			new File(claimFolder, _ATTACHMENT_FOLDER).mkdir();

			Log.d("FileSystemUtilities", "Claim File System created " + claimFolder.getAbsolutePath());

		} catch (Exception e) {
			Log.d("FileSystemUtilities", "Error creating the file system of the claim!!!");
			return false;
		}

		return (new File(claimFolder, _ATTACHMENT_FOLDER).exists());
	}

	public static boolean createClaimantFolder(String personId) {

		try {
			new File(getClaimantsFolder(), _CLAIMANT_PREFIX + personId).mkdir();

		} catch (Exception e) {
			Log.d("FileSystemUtilities", "Error creating the file system of the claim: " + e.getMessage());
			return false;
		}

		return new File(getClaimantsFolder(), _CLAIMANT_PREFIX + personId).exists();
	}

	public static void deleteFolder(File file) throws IOException {

		if (file.isDirectory()) {

			// directory is empty, then delete it
			if (file.list().length == 0) {

				file.delete();
				Log.d("FileSystemUtilities", "Directory is deleted : " + file.getAbsolutePath());

			} else {

				// list all the directory contents
				String files[] = file.list();

				for (String temp : files) {
					// construct the file structure
					File fileDelete = new File(file, temp);

					// recursive delete
					deleteFolder(fileDelete);
				}

				// check the directory again, if empty then delete it
				if (file.list().length == 0) {
					file.delete();
					Log.d("FileSystemUtilities", "Directory is deleted : " + file.getAbsolutePath());
				}
			}

		} else {
			// if file, then delete it
			file.delete();
			Log.d("FileSystemUtilities", "File is deleted : " + file.getAbsolutePath());
		}
	}

	public static void deleteFilesInFolder(File folder) throws IOException {

		if (folder.exists() && folder.isDirectory()) {

			// directory is empty, then delete it
			if (folder.list().length == 0) {

				Log.d("FileSystemUtilities", "Folder is empty : " + folder.getAbsolutePath());

			} else {

				// list all the directory contents
				String files[] = folder.list();

				for (String temp : files) {
					// construct the file structure
					File fileDelete = new File(folder, temp);

					// recursive delete
					deleteFolder(fileDelete);
				}

				// check the directory again, if empty then delete it
				if (folder.list().length == 0) {
					Log.d("FileSystemUtilities", "Folder is empty : " + folder.getAbsolutePath());
				}
			}

		} else {
			// if file, then delete it

			Log.d("FileSystemUtilities", "is not a folder : " + folder.getAbsolutePath());
		}
	}

	public static void deleteCompressedClaim(String claimID) throws IOException {

		File oldZip = new File(FileSystemUtilities.getOpentenureFolder().getAbsolutePath() + File.separator + "Claim_"
				+ claimID + ".zip");
		deleteFolder(oldZip);
	}

	public static boolean removeClaimantFolder(String personId) {

		try {
			deleteFolder(new File(getClaimantsFolder(), _CLAIMANT_PREFIX + personId));
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static File getClaimsFolder() {

		Context context = OpenTenureApplication.getContext();
		File appFolder = context.getExternalFilesDir(null);
		return new File(appFolder, _CLAIMS_FOLDER);

	}

	public static File getClaimantsFolder() {

		Context context = OpenTenureApplication.getContext();
		File appFolder = context.getExternalFilesDir(null);
		return new File(appFolder, _CLAIMANTS_FOLDER);

	}

	public static File getClaimFolder(String claimID) {
		return new File(getClaimsFolder(), _CLAIM_PREFIX + claimID);
	}

	public static File getExportFolder() {
		return new File(getOpentenureFolder(), _EXPORT);
	}

	public static File getImportFolder() {
		return new File(getOpentenureFolder(), _IMPORT);
	}

	public static File getClaimantFolder(String personId) {
		return new File(getClaimantsFolder(), _CLAIMANT_PREFIX + personId);
	}

	public static File getAttachmentFolder(String claimID) {
		return new File(getClaimFolder(claimID), _ATTACHMENT_FOLDER);
	}

	public static boolean createMutipageFolder(String claimID) {

		File multiFolder = null;
		try {
			new File(getAttachmentFolder(claimID), _MULTIPAGE).mkdir();
			multiFolder = new File(getAttachmentFolder(claimID) + File.separator + _MULTIPAGE);

		} catch (Exception e) {
			// TODO: handle exception
			Log.e("org.fao.sola.clients.android.opentenure.filesystem", e.getMessage());
		}

		if (multiFolder.exists() && multiFolder.isDirectory()) {
			return true;
		} else {

			return false;

		}
	}

	public static File getOpentenureFolder() {
		File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		return new File(path.getParentFile(), _OPEN_TENURE_FOLDER);
	}

	public static File getCertificatesFolder() {

		return new File(getOpentenureFolder(), _CERTIFICATES);
	}

	public static File copyFileInAttachFolder(String claimID, File source) {

		File dest = null;

		try {

			dest = new File(getAttachmentFolder(claimID), source.getName());
			dest.createNewFile();

			Log.d("FileSystemUtilities", dest.getAbsolutePath());
			byte[] buffer = new byte[1024];

			FileInputStream reader = new FileInputStream(source);
			FileOutputStream writer = new FileOutputStream(dest);

			BufferedInputStream br = new BufferedInputStream(reader);

			int i = 0;
			while ((i = br.read(buffer)) != -1) {
				writer.write(buffer, 0, i);
			}

			reader.close();
			writer.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return dest;
	}

	public static File copyFileInClaimantFolder(String personId, File source) {

		File dest = null;

		try {

			dest = new File(getClaimantFolder(personId), personId + ".jpg");
			dest.createNewFile();

			Log.d("FileSystemUtilities", dest.getAbsolutePath());
			byte[] buffer = new byte[1024];

			FileInputStream reader = new FileInputStream(source);
			FileOutputStream writer = new FileOutputStream(dest);

			BufferedInputStream br = new BufferedInputStream(reader);

			int i = 0;
			while ((i = br.read(buffer)) != -1) {
				writer.write(buffer, 0, i);
			}

			reader.close();
			writer.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return dest;
	}

	public static File copyFileInImportFolder(File source) {

		File importFolder = null;
		File dest = null;

		try {

			importFolder = new File(getOpentenureFolder(), _IMPORT);
			dest = new File(importFolder, source.getName());
			dest.createNewFile();

			Log.d("FileSystemUtilities", dest.getAbsolutePath());
			byte[] buffer = new byte[1024];

			FileInputStream reader = new FileInputStream(source.getAbsoluteFile());
			FileOutputStream writer = new FileOutputStream(dest);

			BufferedInputStream br = new BufferedInputStream(reader);

			int i = 0;
			while ((i = br.read(buffer)) != -1) {
				writer.write(buffer, 0, i);
			}

			reader.close();
			writer.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return dest;
	}

	public static String getJsonClaim(String claimId) {
		try {

			File folder = getClaimFolder(claimId);
			FileInputStream fis = new FileInputStream(folder + File.separator + "claim.json");
			return CommunityServerAPIUtilities.Slurp(fis, 100);

		} catch (Exception e) {
			Log.d("FileSystemUtilities", "Error reading claim.json :" + e.getMessage());
			e.printStackTrace();
			return null;
		}

	}

	public static String getJsonAttachment(String attachmentId) {
		try {

			Attachment attach = Attachment.getAttachment(attachmentId);
			org.fao.sola.clients.android.opentenure.filesystem.json.model.Attachment attachment = new org.fao.sola.clients.android.opentenure.filesystem.json.model.Attachment();

			String extension = "";

			int i = attach.getPath().lastIndexOf('.');
			if (i > 0) {
				extension = attach.getPath().substring(i + 1);
			}

			attachment.setDescription(attach.getDescription());

			/*
			 * 
			 * Temporary solution for typeCode
			 */
			attachment.setTypeCode(attach.getFileType());

			attachment.setFileName(attach.getFileName());
			attachment.setId(attachmentId);
			attachment.setMd5(attach.getMD5Sum());
			attachment.setMimeType(attach.getMimeType());
			attachment.setSize(attach.getSize());
			attachment.setFileExtension(extension);

			Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls()
					.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
			return gson.toJson(attachment);

		} catch (Exception e) {
			Log.d("FileSystemUtilities", "Error reading creating Attachment json :" + e.getMessage());
			e.printStackTrace();
			return null;
		}

	}

	protected static boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	protected static String matchTypeCode(String original) {

		if (original.equals("pdf"))
			return "pdf";
		else if (original.equals("jpg") || original.equals("jpeg"))
			return "jpg";
		else if (original.equals("tiff") || original.equals("tif"))
			return original;
		else if (original.equals("mpeg") || original.equals("avi"))
			return "standardDocument";
		else if (original.equals("doc") || original.equals("docx") || original.equals("xlsb")
				|| original.equals("xlsb"))
			return "standardDocument";

		return "standardDocument";

	}

	public static boolean deleteClaim(String claimId) {

		File attachFold = getAttachmentFolder(claimId);

		File claimFold = getClaimFolder(claimId);

		File[] files;

		if (attachFold.exists()) {
			files = attachFold.listFiles();
			for (int i = 0; i < files.length; i++) {

				files[i].delete();
			}
		}

		if (claimFold.exists()) {
			files = claimFold.listFiles();
			for (int i = 0; i < files.length; i++) {
				files[i].delete();

			}
			return claimFold.delete();
		}

		return true;
	}

	public static boolean deleteCLaimant(String personId) {

		File claimantFold = getClaimantFolder(personId);

		File[] files;

		if (claimantFold.exists()) {
			files = claimantFold.listFiles();
			for (int i = 0; i < files.length; i++) {

				files[i].delete();
			}
			return claimantFold.delete();
		}

		return true;
	}

	/**
	 * Replace special characters in a string with '-'
	 * 
	 * @param original
	 *            String
	 * @return
	 */
	public static String cleanBySpecial(String original) {
		char[] reservedChars = { '?', ':', '\\', ':', '\'', '*', '|', '/', '<', '>' };

		for (int i = 0; i < reservedChars.length; i++) {
			original = original.replace(reservedChars[i], '-');
		}
		return original;
	}

	public static int getUploadProgress(String claimId, String status) {

		int progress = 0;
		List<Attachment> attachments = Claim.getClaim(claimId).getAttachments();

		if (attachments.size() == 0)
			progress = 100;
		else {
			long totalSize = 0;
			long uploadedSize = 0;

			File claimfolder = getClaimFolder(claimId);
			File json = new File(claimfolder, "claim.json");
			totalSize = totalSize + json.length();

			if (status.equals(ClaimStatus._UPLOADING) || status.equals(ClaimStatus._UPLOAD_INCOMPLETE)
					|| status.equals(ClaimStatus._UPDATING) || status.equals(ClaimStatus._UPDATE_INCOMPLETE))
				uploadedSize = uploadedSize + json.length();

			for (Iterator<Attachment> iterator = attachments.iterator(); iterator.hasNext();) {
				Attachment attachment = (Attachment) iterator.next();
				totalSize = totalSize + attachment.getSize();

				if (!attachment.getStatus().equals(AttachmentStatus._UPLOAD_ERROR)) {

					uploadedSize = uploadedSize + attachment.getUploadedBytes();

				}

			}

			float factor = (float) uploadedSize / totalSize;

			progress = (int) (factor * 100);

		}

		return progress;

	}

	public static File reduceJpeg(File file) {

		InputStream in = null;
		OutputStream out = null;
		File dir = file.getParentFile();
		String cmpFileName = file.getName().replace(".jpg", "_cmp.jpg");

		/* 100 = max quality, 0 = max compression */

		int quality = 0;

		if (file.length() < 1000000)
			quality = 80;

		if (file.length() >= 1000000 && file.length() < 2000000)
			quality = 60;

		if (file.length() >= 2000000)
			quality = 40;

		try {
			Log.d(FileSystemUtilities.class.getName(), "Compressing " + file.getName() + " to " + cmpFileName+ " with " + quality + " quality hint");
			in = new FileInputStream(file);

			Bitmap bitmap = BitmapFactory.decodeStream(in);
			File cmpFile = new File(dir, cmpFileName);
			out = new FileOutputStream(cmpFile);
			if (bitmap.compress(CompressFormat.JPEG, quality, out)) {
				out.flush();
				out.close();
				in.close();
				return cmpFile;

			} else {
				throw new Exception("Failed to save the image as a JPEG");
			}
		} catch (Exception e) {
			Log.e(FileSystemUtilities.class.getName(), "Failed to compress image :" + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				Log.e(FileSystemUtilities.class.getName(), "Failed to release streams :" + e.getMessage());
				e.printStackTrace();
			}
		}
		return null;
	}
}
