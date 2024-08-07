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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.List;

import org.fao.sola.clients.android.opentenure.BuildConfig;
import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.model.Attachment;
import org.fao.sola.clients.android.opentenure.model.AttachmentStatus;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.network.API.CommunityServerAPIUtilities;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;

import androidx.core.content.ContextCompat;

public class FileSystemUtilities {

	private static String _CLAIMS_FOLDER = "claims";
	private static String _CLAIMANTS_FOLDER = "claimants";
	private static String _CLAIM_PREFIX = "claim_";
	private static String _CLAIMANT_PREFIX = "claimant_";
	private static String _ATTACHMENT_FOLDER = "attachments";
	private static String _OPEN_TENURE_FOLDER = "Open Tenure";

	private static String _MBTILES_FOLDER = "mbtiles";
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
			File ot = getOpentenureFolder();
			if (ot.mkdirs()) {
				Log.d("FileSystemUtilities", "Created Open Tenure Folder");
				return true;
			}
			return false;
		} else
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
			File certificatesFolder = getCertificatesFolder();
			if (certificatesFolder.mkdirs()) {
				Log.d("FileSystemUtilities", "Created Certificates Folder");
				return true;
			}
			return false;
		} else
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

	public static File getMbTilesFolder() {
		if (isExternalStorageWritable()) {
			try {
				Context context = OpenTenureApplication.getContext();
				File appFolder = context.getExternalFilesDir(null);
				File folder = new File(appFolder.getAbsoluteFile() + File.separator + _MBTILES_FOLDER);

				if(!folder.exists()) {
					folder.mkdir();
				}
				return folder;
			} catch (Exception e) {
				return null;
			}
		} else {
			return null;
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

	public static String getClaimAttachmentContentPath(String claimID, String fileName) {
		return "content://"+ BuildConfig.APPLICATION_ID+"/app-files/" + _CLAIMS_FOLDER + "/" + _CLAIM_PREFIX + claimID + "/" + _ATTACHMENT_FOLDER + "/" + fileName;
	}

	public static boolean createMutipageFolder(String claimID) {

		File multiFolder = null;
		try {
			new File(getAttachmentFolder(claimID), _MULTIPAGE).mkdir();
			multiFolder = new File(getAttachmentFolder(claimID) + File.separator + _MULTIPAGE);

		} catch (Exception e) {
			// TODO: handle exception
			Log.e("opentenure.filesystem", e.getMessage());
		}

		if (multiFolder.exists() && multiFolder.isDirectory()) {
			return true;
		} else {

			return false;

		}
	}

	public static File getOpentenureFolder() {
		File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
		return new File(path, _OPEN_TENURE_FOLDER);
	}

	public static File getCertificatesFolder() {
		return new File(getOpentenureFolder(), _CERTIFICATES);
	}

	public static File copyStreamInAttachFolder(String claimID, InputStream reader, String fileName) {
		File dest = null;

		try {

			dest = new File(getAttachmentFolder(claimID), fileName);
			dest.createNewFile();

			Log.d("FileSystemUtilities", dest.getAbsolutePath());
			byte[] buffer = new byte[1024];


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
	public static File copyFileInAttachFolder(String claimID, File source) {
		try (FileInputStream reader = new FileInputStream(source)) {
			return copyStreamInAttachFolder(claimID, reader, source.getName());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
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

	public static String getJsonClaim(String claimId) {
		try {
			File claimFile = new File(getClaimFolder(claimId)+ File.separator + "claim.json");
			boolean isFileExists = claimFile.exists();
			FileInputStream fis = new FileInputStream(claimFile);
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

	public static void rotateAndCompressImage(Context context, Uri imageUri) {
		try {
			int MAX_HEIGHT = 1024;
			int MAX_WIDTH = 1024;

			// First decode with inJustDecodeBounds=true to check dimensions
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			InputStream imageStream = context.getContentResolver().openInputStream(imageUri);
			BitmapFactory.decodeStream(imageStream, null, options);
			imageStream.close();

			// Calculate inSampleSize
			options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;
			imageStream = context.getContentResolver().openInputStream(imageUri);
			Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);
			imageStream.close();

			imageStream = context.getContentResolver().openInputStream(imageUri);
			ExifInterface ei;
			if (Build.VERSION.SDK_INT > 23)
				ei = new ExifInterface(imageStream);
			else
				ei = new ExifInterface(imageUri.getPath());

			imageStream.close();

			int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			int rotationDegree = 0;

			switch (orientation) {
				case ExifInterface.ORIENTATION_ROTATE_90:
					rotationDegree = 90;
					break;
				case ExifInterface.ORIENTATION_ROTATE_180:
					rotationDegree = 180;
					break;
				case ExifInterface.ORIENTATION_ROTATE_270:
					rotationDegree = 270;
					break;
			}

			// Rotation is required
			if(rotationDegree > 0){
				Matrix matrix = new Matrix();
				matrix.postRotate(rotationDegree);
				Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
				img.recycle();
				img = rotatedImg;
			}

			// Save
			try (OutputStream out = context.getContentResolver().openOutputStream(imageUri)){
				img.compress(CompressFormat.JPEG, 80, out);
				out.flush();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		} catch (IOException ex) {
			Log.e(FileSystemUtilities.class.getName(), "Failed to rotate image: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			// Calculate ratios of height and width to requested height and width
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will guarantee a final image
			// with both dimensions larger than or equal to the requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

			// This offers some additional logic in case the image has a strange
			// aspect ratio. For example, a panorama may have a much larger
			// width than height. In these cases the total pixels might still
			// end up being too large to fit comfortably in memory, so we should
			// be more aggressive with sample down the image (=larger inSampleSize).
			final float totalPixels = width * height;

			// Anything more than 2x the requested pixels we'll sample down further
			final float totalReqPixelsCap = reqWidth * reqHeight * 2;

			while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
				inSampleSize++;
			}
		}
		return inSampleSize;
	}

	public static int getFileSizeFromUri(Context ctx, Uri uri){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len;

		// read bytes from input stream to buffer
		try (InputStream is = ctx.getContentResolver().openInputStream(uri)) {
			while ((len = is.read(buffer)) != -1) {
				baos.write(buffer, 0, len);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.size();
	}

	/**
	* @deprecated
	 * Compress JPEG image
	*/
	public static void reduceJpeg(Uri imageUri, Context context) {
		ByteArrayOutputStream original = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len;

		// read bytes from input stream to buffer
		try (InputStream is = context.getContentResolver().openInputStream(imageUri)) {
			while ((len = is.read(buffer)) != -1) {
				original.write(buffer, 0, len);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		/* 100 = max quality, 0 = max compression */
		int quality = 0;
		int size = original.size();

		if (size < 1000000) {
			quality = 80;
		} else if (size >= 1000000 && size < 2000000) {
			quality = 60;
		} else if (size >= 2000000) {
			quality = 40;
		}

		try (ByteArrayInputStream in = new ByteArrayInputStream(original.toByteArray())) {
			Bitmap bitmap = BitmapFactory.decodeStream(in);
			in.close();

			try (OutputStream out = context.getContentResolver().openOutputStream(imageUri)) {
				if (bitmap.compress(CompressFormat.JPEG, quality, out)) {
					out.flush();
				} else {
					throw new Exception("Failed to save the image as a JPEG");
				}
			} catch (Exception e) {
				Log.e(FileSystemUtilities.class.getName(), "Failed to compress image :" + e.getMessage());
				e.printStackTrace();
			}
		} catch (Exception e) {
			Log.e(FileSystemUtilities.class.getName(), "Failed to compress image :" + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * @deprecated
	 * Compress JPEG file
	 */
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
