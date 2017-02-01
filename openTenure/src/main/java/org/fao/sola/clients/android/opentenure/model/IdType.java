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
import java.util.Map;

import org.fao.sola.clients.android.opentenure.DisplayNameLocalizer;
import org.fao.sola.clients.android.opentenure.OpenTenureApplication;

public class IdType {

	Database db = OpenTenureApplication.getInstance().getDatabase();

	String type;
	String displayValue;
	String description;
	String status;
	Boolean active;

	@Override
	public String toString() {
		return "DocumentType [code=" + type + ", description=" + description
				+ ", displayValue=" + displayValue + ", status=" + status + ", active=" + active + "]";
	}



	public Database getDb() {
		return db;
	}

	public void setDb(Database db) {
		this.db = db;
	}
	
	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDisplayValue() {
		return displayValue;
	}

	public void setDisplayValue(String displayValue) {
		this.displayValue = displayValue;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int add() {

		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("INSERT INTO ID_TYPE(TYPE, DESCRIPTION, DISPLAY_VALUE, ACTIVE) VALUES (?,?,?,'true')");

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

	public int addType(IdType idType) {

		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("INSERT INTO ID_TYPE(TYPE, DESCRIPTION, DISPLAY_VALUE,ACTIVE) VALUES (?,?,?,'true')");

			statement.setString(1, idType.getType());
			statement.setString(2, idType.getDescription());
			statement.setString(3, idType.getDisplayValue());

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

	public List<IdType> getIdTypesActive() {

		List<IdType> types = new ArrayList<IdType>();
		ResultSet rs = null;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("SELECT TYPE, DESCRIPTION, DISPLAY_VALUE FROM ID_TYPE IT where ACTIVE = 'true'");
			rs = statement.executeQuery();

			while (rs.next()) {
				IdType idType = new IdType();
				idType.setType(rs.getString(1));
				idType.setDescription(rs.getString(2));
				idType.setDisplayValue(rs.getString(3));

				types.add(idType);

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
	
	public List<IdType> getIdTypes() {

		List<IdType> types = new ArrayList<IdType>();
		ResultSet rs = null;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("SELECT TYPE, DESCRIPTION, DISPLAY_VALUE FROM ID_TYPE IT");
			rs = statement.executeQuery();

			while (rs.next()) {
				IdType idType = new IdType();
				idType.setType(rs.getString(1));
				idType.setDescription(rs.getString(2));
				idType.setDisplayValue(rs.getString(3));

				types.add(idType);

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

	public List<String> getDisplayValues(String localization,boolean onlyActive) {

		List<org.fao.sola.clients.android.opentenure.model.IdType> list;
		
		if(!onlyActive)
			list = getIdTypes();
		else
			list = getIdTypesActive();
		
		
		DisplayNameLocalizer dnl = new DisplayNameLocalizer(OpenTenureApplication.getInstance().getLocalization());
		List<String> displayList = new ArrayList<String>();

		for (Iterator<org.fao.sola.clients.android.opentenure.model.IdType> iterator = list.iterator(); iterator.hasNext();) {
			org.fao.sola.clients.android.opentenure.model.IdType idType = (org.fao.sola.clients.android.opentenure.model.IdType) iterator
					.next();

			displayList.add(dnl.getLocalizedDisplayName(idType.getDisplayValue()));
		}
		return displayList;
	}
	
	public Map<String,String> getKeyValueMap(String localization,boolean onlyActive) {

		List<org.fao.sola.clients.android.opentenure.model.IdType> list;
		
		if(!onlyActive)
			list = getIdTypes();
		else
			list = getIdTypesActive();

		DisplayNameLocalizer dnl = new DisplayNameLocalizer(OpenTenureApplication.getInstance().getLocalization());
		Map<String,String> keyValueMap = new HashMap<String,String>();

		for (Iterator<IdType> iterator = list.iterator(); iterator.hasNext();) {
			
			IdType idType = (IdType) iterator
					.next();
			
			keyValueMap.put(idType.getType().toLowerCase(),dnl.getLocalizedDisplayName(idType.getDisplayValue()));
		}
		return keyValueMap;
	}
	
	public Map<String,String> getValueKeyMap(String localization,boolean onlyActive) {

		List<org.fao.sola.clients.android.opentenure.model.IdType> list;
		
		if(!onlyActive)
			list = getIdTypes();
		else
			list = getIdTypesActive();

		DisplayNameLocalizer dnl = new DisplayNameLocalizer(OpenTenureApplication.getInstance().getLocalization());
		Map<String,String> keyValueMap = new HashMap<String,String>();

		for (Iterator<IdType> iterator = list.iterator(); iterator.hasNext();) {
			
			IdType idType = (IdType) iterator
					.next();

			keyValueMap.put(dnl.getLocalizedDisplayName(idType.getDisplayValue()),idType.getType());
		}
		return keyValueMap;
	}

	public int getIndexByCodeType(String code,boolean onlyActive) {

		List<org.fao.sola.clients.android.opentenure.model.IdType> list;
		
		if(!onlyActive)
			list = getIdTypes();
		else
			list = getIdTypesActive();


		int i = 0;

		for (Iterator<org.fao.sola.clients.android.opentenure.model.IdType> iterator = list.iterator(); iterator.hasNext();) {
			org.fao.sola.clients.android.opentenure.model.IdType idType = (org.fao.sola.clients.android.opentenure.model.IdType) iterator
					.next();

			if (idType.getType().equals(code)) {

				return i;

			}

			i++;
		}
		return 0;

	}

	public String getTypebyDisplayValue(String value) {

		ResultSet rs = null;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("SELECT TYPE FROM ID_TYPE CT WHERE DISPLAY_VALUE LIKE  '%' || ? || '%' ");
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

	public String getDisplayValueByType(String value) {

		ResultSet rs = null;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("SELECT DISPLAY_VALUE FROM ID_TYPE CT WHERE TYPE = ?");
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
	
	public static IdType getIdType(String type) {
		ResultSet result = null;
		Connection localConnection = null;
		PreparedStatement statement = null;
		IdType idType = new IdType();
		try {
			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT TYPE, DESCRIPTION, DISPLAY_VALUE FROM ID_TYPE WHERE TYPE=?");
			statement.setString(1, type);

			result = statement.executeQuery();

			if (result.next()) {

				idType.setType(result.getString(1));
				idType.setDescription(result.getString(2));
				idType.setDisplayValue(result.getString(3));
				
				return idType;
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
	
	public int updadateIdType() {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {
			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE ID_TYPE SET TYPE=?, DESCRIPTION=?, DISPLAY_VALUE=?, ACTIVE='true' WHERE TYPE = ?");
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
	
	
	public static int setAllIdTypeNoActive() {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {
			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE ID_TYPE ID SET ACTIVE='false' WHERE  ID.ACTIVE= 'true'");

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
