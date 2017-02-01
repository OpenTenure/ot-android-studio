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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.fao.sola.clients.android.opentenure.DisplayNameLocalizer;
import org.fao.sola.clients.android.opentenure.OpenTenureApplication;

public class DocumentType {

	Database db = OpenTenureApplication.getInstance().getDatabase();

	String type;
	String description;
	String displayValue;
	Boolean active;

	@Override
	public String toString() {
		return "DocumentType [code=" + type + ", description=" + description
				+ ", displayValue=" + displayValue + ", active=" + active + "]";
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDisplayValue() {
		return displayValue;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setDisplayValue(String displayValue) {
		this.displayValue = displayValue;
	}

	public int add() {

		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("INSERT INTO DOCUMENT_TYPE(CODE, DESCRIPTION, DISPLAY_VALUE,ACTIVE) VALUES (?,?,?,'true')");

			statement.setString(1, getType());
			statement.setString(2, getDescription());
			statement.setString(3, getDisplayValue());

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

	public int addType(DocumentType docType) {

		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("INSERT INTO DOCUMENT_TYPE(CODE, DESCRIPTION, DISPLAY_VALUE,ACTIVE) VALUES (?,?,?,'true')");

			statement.setString(1, docType.getType());
			statement.setString(2, docType.getDescription());
			statement.setString(3, docType.getDisplayValue());

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

	public static DocumentType getDocumentType(String code) {
		ResultSet result = null;
		Connection localConnection = null;
		PreparedStatement statement = null;
		DocumentType documentType = new DocumentType();
		try {
			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT CODE, DESCRIPTION, DISPLAY_VALUE, ACTIVE FROM DOCUMENT_TYPE WHERE CODE=?");
			statement.setString(1, code);

			result = statement.executeQuery();

			if (result.next()) {

				documentType.setType(result.getString(1));
				documentType.setDescription(result.getString(2));
				documentType.setDisplayValue(result.getString(3));
				documentType.setActive(result.getBoolean(4));

				return documentType;
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
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return null;
	}

	public int updadateDocumentType() {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {
			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE DOCUMENT_TYPE SET CODE=?, DESCRIPTION=?, DISPLAY_VALUE=?, ACTIVE='true' WHERE CODE = ?");
			statement.setString(1, getType());
			statement.setString(2, getDescription());
			statement.setString(3, getDisplayValue());
			statement.setString(4, getType());

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

	public List<DocumentType> getDocumentTypes() {

		List<DocumentType> types = new ArrayList<DocumentType>();
		ResultSet rs = null;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("SELECT CODE, DESCRIPTION, DISPLAY_VALUE FROM DOCUMENT_TYPE DT ");
			rs = statement.executeQuery();

			while (rs.next()) {
				DocumentType documentType = new DocumentType();
				documentType.setType(rs.getString(1));
				documentType.setDescription(rs.getString(2));
				documentType.setDisplayValue(rs.getString(3));

				types.add(documentType);

			}
			return types;

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
		return types;

	}
	
	
	public List<DocumentType> getDocumentTypesActive() {

		List<DocumentType> types = new ArrayList<DocumentType>();
		ResultSet rs = null;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("SELECT CODE, DESCRIPTION, DISPLAY_VALUE FROM DOCUMENT_TYPE DT where ACTIVE = 'true'");
			rs = statement.executeQuery();

			while (rs.next()) {
				DocumentType documentType = new DocumentType();
				documentType.setType(rs.getString(1));
				documentType.setDescription(rs.getString(2));
				documentType.setDisplayValue(rs.getString(3));

				types.add(documentType);

			}
			return types;

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
		return types;

	}

	public List<String> getDocumentTypesDisplayValues(String localization, boolean onlyActive) {

		List<org.fao.sola.clients.android.opentenure.model.DocumentType> list ;
		
		if(!onlyActive)
			list = getDocumentTypes();
		else
			list = getDocumentTypesActive();
		
		DisplayNameLocalizer dnl = new DisplayNameLocalizer(
				OpenTenureApplication.getInstance().getLocalization());
		List<String> displayList = new ArrayList<String>();

		for (Iterator<org.fao.sola.clients.android.opentenure.model.DocumentType> iterator = list
				.iterator(); iterator.hasNext();) {
			org.fao.sola.clients.android.opentenure.model.DocumentType docType = (org.fao.sola.clients.android.opentenure.model.DocumentType) iterator
					.next();

			displayList.add(dnl.getLocalizedDisplayName(docType
					.getDisplayValue()));
		}
		return displayList;
	}

	public int getIndexByCodeType(String code, boolean onlyActive) {

		List<org.fao.sola.clients.android.opentenure.model.DocumentType> list ;
		
		if(!onlyActive)
			list = getDocumentTypes();
		else
			list = getDocumentTypesActive();
		

		int i = 0;

		for (Iterator<org.fao.sola.clients.android.opentenure.model.DocumentType> iterator = list
				.iterator(); iterator.hasNext();) {
			org.fao.sola.clients.android.opentenure.model.DocumentType docType = (org.fao.sola.clients.android.opentenure.model.DocumentType) iterator
					.next();

			if (docType.getType().equals(code)) {
				return i;
			}

			i++;
		}
		return 0;

	}
	
	
	public Map<String,String> getKeyValueMap(String localization, boolean onlyActive) {

		List<org.fao.sola.clients.android.opentenure.model.DocumentType> list ;
		
		if(!onlyActive)
			list = getDocumentTypes();
		else
			list = getDocumentTypesActive();
		
		DisplayNameLocalizer dnl = new DisplayNameLocalizer(OpenTenureApplication.getInstance().getLocalization());
		Map<String,String> keyValueMap = new HashMap<String,String>();

		for (Iterator<DocumentType> iterator = list.iterator(); iterator.hasNext();) {
			
			DocumentType documentType = (DocumentType) iterator
					.next();
			
			keyValueMap.put(documentType.getType().toLowerCase(Locale.US),dnl.getLocalizedDisplayName(documentType.getDisplayValue()));
		}
		return keyValueMap;
	}
	
	public Map<String,String> getValueKeyMap(String localization, boolean onlyActive) {

		List<org.fao.sola.clients.android.opentenure.model.DocumentType> list ;
		
		if(!onlyActive)
			list = getDocumentTypes();
		else
			list = getDocumentTypesActive();
		
		DisplayNameLocalizer dnl = new DisplayNameLocalizer(OpenTenureApplication.getInstance().getLocalization());
		Map<String,String> keyValueMap = new HashMap<String,String>();

		for (Iterator<DocumentType> iterator = list.iterator(); iterator.hasNext();) {
			
			DocumentType documentType = (DocumentType) iterator
					.next();

			keyValueMap.put(dnl.getLocalizedDisplayName(documentType.getDisplayValue()),documentType.getType());
		}
		return keyValueMap;
	}

	public String getTypebyDisplayVaue(String value) {

		ResultSet rs = null;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("SELECT CODE FROM DOCUMENT_TYPE CT WHERE DISPLAY_VALUE LIKE  '%' || ? || '%' ");
			statement.setString(1, value);
			rs = statement.executeQuery();

			while (rs.next()) {
				return rs.getString(1);
			}
			return null;

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
		return null;

	}

	public String getDisplayVauebyType(String value) {

		ResultSet rs = null;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("SELECT DISPLAY_VALUE FROM DOCUMENT_TYPE CT WHERE CODE = ?");
			statement.setString(1, value);
			rs = statement.executeQuery();

			while (rs.next()) {
				return rs.getString(1);
			}
			return null;

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
		return null;

	}
		
	public static int setAllDocumentTypeNoActive() {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {
			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE DOCUMENT_TYPE DT SET ACTIVE='false' WHERE  DT.ACTIVE= 'true'");
			

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

}
