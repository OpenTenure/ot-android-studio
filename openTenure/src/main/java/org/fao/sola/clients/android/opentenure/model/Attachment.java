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
package org.fao.sola.clients.android.opentenure.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;

public class Attachment {

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	Database db = OpenTenureApplication.getInstance().getDatabase();

	public String getClaimId() {
		return claimId;
	}

	public void setClaimId(String claimId) {
		this.claimId = claimId;
	}

	public Attachment() {
		this.attachmentId = UUID.randomUUID().toString();
		this.status = AttachmentStatus._CREATED;
	}

	@Override
	public String toString() {
		return "Attachment [attachmentId=" + attachmentId + ", claimId="
				+ claimId + ", status=" + status + ", description="
				+ description + ", fileName=" + fileName + ", fileType="
				+ fileType + ", mimeType=" + mimeType + ", MD5Sum=" + MD5Sum
				+ ", path=" + path + ", size=" + size + ", uploadedBytes=" + uploadedBytes + ", downloadedBytes=" + downloadedBytes + "]";
	}

	public String getAttachmentId() {
		return attachmentId;
	}

	public void setAttachmentId(String attachmentId) {
		this.attachmentId = attachmentId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getMD5Sum() {
		return MD5Sum;
	}

	public void setMD5Sum(String mD5Sum) {
		MD5Sum = mD5Sum;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public static int createAttachment(Attachment attachment) {

		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("INSERT INTO ATTACHMENT(ATTACHMENT_ID, STATUS, CLAIM_ID, DESCRIPTION, FILE_NAME, FILE_TYPE, MIME_TYPE, MD5SUM, PATH, SIZE, DOWNLOADED_BYTES, UPLOADED_BYTES) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setString(1, attachment.getAttachmentId());
			statement.setString(2, attachment.getStatus());
			statement.setString(3, attachment.getClaimId());
			statement.setString(4, attachment.getDescription());
			statement.setString(5, attachment.getFileName());
			statement.setString(6, attachment.getFileType());
			statement.setString(7, attachment.getMimeType());
			statement.setString(8, attachment.getMD5Sum());
			statement.setString(9, attachment.getPath());
			statement.setLong(10, attachment.getSize());
			statement.setLong(11, 0);
			statement.setLong(12, 0);
			result = statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	public int create() {

		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("INSERT INTO ATTACHMENT(ATTACHMENT_ID, STATUS, CLAIM_ID, DESCRIPTION, FILE_NAME, FILE_TYPE, MIME_TYPE, MD5SUM, PATH, SIZE, DOWNLOADED_BYTES, UPLOADED_BYTES) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setString(1, getAttachmentId());
			statement.setString(2, getStatus());
			statement.setString(3, getClaimId());
			statement.setString(4, getDescription());
			statement.setString(5, getFileName());
			statement.setString(6, getFileType());
			statement.setString(7, getMimeType());
			statement.setString(8, getMD5Sum());
			statement.setString(9, getPath());
			statement.setLong(10, getSize());
			statement.setLong(11, 0);
			statement.setLong(12, 0);

			result = statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	public static int updateAttachment(Attachment attachment) {

		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE ATTACHMENT SET STATUS=?, CLAIM_ID=?, DESCRIPTION=?, FILE_NAME=?, FILE_TYPE=?, MIME_TYPE=?, MD5SUM=?, PATH=?, SIZE=? WHERE ATTACHMENT_ID=?");
			statement.setString(1, attachment.getStatus());
			statement.setString(2, attachment.getClaimId());
			statement.setString(3, attachment.getDescription());
			statement.setString(4, attachment.getFileName());
			statement.setString(5, attachment.getFileType());
			statement.setString(6, attachment.getMimeType());
			statement.setString(7, attachment.getMD5Sum());
			statement.setString(8, attachment.getPath());
			statement.setLong(9, attachment.getSize());
			statement.setString(10, attachment.getAttachmentId());

			result = statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	public int update() {

		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE ATTACHMENT SET STATUS=?, CLAIM_ID=?, DESCRIPTION=?, FILE_NAME=?, FILE_TYPE=?, MIME_TYPE=?, MD5SUM=?, PATH=?, SIZE=? WHERE ATTACHMENT_ID=?");
			statement.setString(1, getStatus());
			statement.setString(2, getClaimId());
			statement.setString(3, getDescription());
			statement.setString(4, getFileName());
			statement.setString(5, getFileType());
			statement.setString(6, getMimeType());
			statement.setString(7, getMD5Sum());
			statement.setString(8, getPath());
			statement.setLong(9, getSize());
			statement.setString(10, getAttachmentId());

			result = statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	public static int deleteAttachment(Attachment attachment) {

		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("DELETE ATTACHMENT WHERE ATTACHMENT_ID=?");
			statement.setString(1, attachment.getAttachmentId());

			result = statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	public int delete() {

		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("DELETE ATTACHMENT WHERE ATTACHMENT_ID=?");
			statement.setString(1, getAttachmentId());

			result = statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	public static int deleteAttachments(String claimId, Connection connection) {

		int result = 0;
		PreparedStatement statement = null;

		try {

			statement = connection
					.prepareStatement("DELETE ATTACHMENT WHERE CLAIM_ID=?");
			statement.setString(1, claimId);

			result = statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	public static Attachment getAttachment(String attachmentId) {

		Attachment attachment = null;
		ResultSet rs = null;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT STATUS, CLAIM_ID, DESCRIPTION, FILE_NAME, FILE_TYPE, MIME_TYPE, MD5SUM, PATH, SIZE, UPLOADED_BYTES, DOWNLOADED_BYTES FROM ATTACHMENT DOC WHERE DOC.ATTACHMENT_ID=?");
			statement.setString(1, attachmentId);
			rs = statement.executeQuery();
			while (rs.next()) {
				attachment = new Attachment();
				attachment.setAttachmentId(attachmentId);
				attachment.setStatus(rs.getString(1));
				attachment.setClaimId(rs.getString(2));
				attachment.setDescription(rs.getString(3));
				attachment.setFileName(rs.getString(4));
				attachment.setFileType(rs.getString(5));
				attachment.setMimeType(rs.getString(6));
				attachment.setMD5Sum(rs.getString(7));
				attachment.setPath(rs.getString(8));
				attachment.setSize(rs.getLong(9));
				attachment.setUploadedBytes(rs.getLong(10));
				attachment.setDownloadedBytes(rs.getLong(11));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return attachment;
	}

	public static List<Attachment> getAttachments(String claimId) {

		List<Attachment> attachments = new ArrayList<Attachment>();
		ResultSet rs = null;
		Connection localConnection = null;
		PreparedStatement statement = null;
		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT ATTACHMENT_ID, STATUS, DESCRIPTION, FILE_NAME, FILE_TYPE, MIME_TYPE, MD5SUM, PATH, SIZE FROM ATTACHMENT ATT WHERE ATT.CLAIM_ID=?");
			statement.setString(1, claimId);
			rs = statement.executeQuery();
			while (rs.next()) {
				Attachment attachment = new Attachment();
				attachment.setAttachmentId(rs.getString(1));
				attachment.setStatus(rs.getString(2));
				attachment.setClaimId(claimId);
				attachment.setDescription(rs.getString(3));
				attachment.setFileName(rs.getString(4));
				attachment.setFileType(rs.getString(5));
				attachment.setMimeType(rs.getString(6));
				attachment.setMD5Sum(rs.getString(7));
				attachment.setPath(rs.getString(8));
				attachment.setSize(rs.getLong(9));
				attachments.add(attachment);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return attachments;
	}

	public static List<Attachment> getAttachments(String claimId, Connection externalConnection) {

		List<Attachment> attachments = new ArrayList<Attachment>();
		PreparedStatement statement = null;
		try {

			statement = externalConnection
					.prepareStatement("SELECT ATTACHMENT_ID, STATUS, DESCRIPTION, FILE_NAME, FILE_TYPE, MIME_TYPE, MD5SUM, PATH, SIZE FROM ATTACHMENT ATT WHERE ATT.CLAIM_ID=?");
			statement.setString(1, claimId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				Attachment attachment = new Attachment();
				attachment.setAttachmentId(rs.getString(1));
				attachment.setStatus(rs.getString(2));
				attachment.setClaimId(claimId);
				attachment.setDescription(rs.getString(3));
				attachment.setFileName(rs.getString(4));
				attachment.setFileType(rs.getString(5));
				attachment.setMimeType(rs.getString(6));
				attachment.setMD5Sum(rs.getString(7));
				attachment.setPath(rs.getString(8));
				attachment.setSize(rs.getLong(9));
				attachments.add(attachment);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
		}
		return attachments;
	}
	
	public int updateDownloadedBytes(long bytes) {

		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE ATTACHMENT SET DOWNLOADED_BYTES=? WHERE ATTACHMENT_ID=?");
			statement.setLong(1, bytes);			
			statement.setString(2, getAttachmentId());

			result = statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}
	
	public int updateUploadedBytes(long bytes) {

		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {
			
			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE ATTACHMENT SET UPLOADED_BYTES=? WHERE ATTACHMENT_ID=?");
			statement.setLong(1, bytes);			
			statement.setString(2, getAttachmentId());

			result = statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}
	
	public long getUploadedBytes() {

		
		ResultSet rs = null;
		Connection localConnection = null;
		PreparedStatement statement = null;
		long bytes = 0 ;
		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT UPLOADED_BYTES FROM ATTACHMENT DOC WHERE DOC.ATTACHMENT_ID=?");
			statement.setString(1, attachmentId);
			rs = statement.executeQuery();
			while (rs.next()) {
				
			bytes = rs.getLong(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		
		return bytes;
	}
	
	public long getDownloadedBytes() {
		
		ResultSet rs = null;
		Connection localConnection = null;
		PreparedStatement statement = null;
		long bytes = 0 ;
		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT DOWNLOADED_BYTES FROM ATTACHMENT DOC WHERE DOC.ATTACHMENT_ID=?");
			statement.setString(1, attachmentId);
			rs = statement.executeQuery();
			while (rs.next()) {
			bytes = rs.getLong(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (SQLException e) {
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
				}
			}
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		
		return bytes;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public void setUploadedBytes(Long uploadedBytes) {
		this.uploadedBytes = uploadedBytes;
	}

	public void setDownloadedBytes(Long downloadedBytes) {
		this.downloadedBytes = downloadedBytes;
	}

	String attachmentId;
	String claimId;
	String description;
	String fileName;
	String fileType;
	String mimeType;
	String MD5Sum;
	String path;
	String status;
	Long size;
	Long uploadedBytes;
	Long downloadedBytes;

}
