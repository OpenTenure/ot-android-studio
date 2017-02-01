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

public class LandUse {

	Database db = OpenTenureApplication.getInstance().getDatabase();

	String type;
	String displayValue;
	String description;
	String status;
	Boolean active;

	@Override
	public String toString() {
		return "DocumentType [type=" + type + ", description=" + description
				+ ", displayValue=" + displayValue + ", status=" + status + ", active=" + active + "]";
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
	
	

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
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
					.prepareStatement("INSERT INTO LAND_USE(TYPE, DESCRIPTION, DISPLAY_VALUE, ACTIVE) VALUES (?,?,?,'true')");

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

	public int addType(LandUse use) {

		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("INSERT INTO LAND_USE(TYPE, DESCRIPTION, DISPLAY_VALUE, ACTIVE) VALUES (?,?,?,'true')");

			statement.setString(1, use.getType());
			statement.setString(2, use.getDescription());
			statement.setString(3, use.getDisplayValue());

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

	public List<LandUse> getLandUsesActive() {

		List<LandUse> uses = new ArrayList<LandUse>();
		ResultSet rs = null;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("SELECT TYPE, DESCRIPTION, DISPLAY_VALUE FROM LAND_USE LU where ACTIVE = 'true'");
			rs = statement.executeQuery();

			while (rs.next()) {
				LandUse landUse = new LandUse();
				landUse.setType(rs.getString(1));
				landUse.setDescription(rs.getString(2));
				landUse.setDisplayValue(rs.getString(3));

				uses.add(landUse);

			}
			return uses;

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
		return uses;

	}
	
	public List<LandUse> getLandUses() {

		List<LandUse> uses = new ArrayList<LandUse>();
		ResultSet rs = null;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("SELECT TYPE, DESCRIPTION, DISPLAY_VALUE FROM LAND_USE LU");
			rs = statement.executeQuery();

			while (rs.next()) {
				LandUse landUse = new LandUse();
				landUse.setType(rs.getString(1));
				landUse.setDescription(rs.getString(2));
				landUse.setDisplayValue(rs.getString(3));

				uses.add(landUse);

			}
			return uses;

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
		return uses;

	}

	public Map<String,String> getKeyValueMap(String localization, boolean onlyActive) {
		
		List<org.fao.sola.clients.android.opentenure.model.LandUse> list;
		if(!onlyActive)
			list = getLandUses();
		else
			list = getLandUsesActive();


		DisplayNameLocalizer dnl = new DisplayNameLocalizer(OpenTenureApplication.getInstance().getLocalization());
		Map<String,String> keyValueMap = new HashMap<String,String>();

		for (Iterator<org.fao.sola.clients.android.opentenure.model.LandUse> iterator = list.iterator(); iterator.hasNext();) {
			org.fao.sola.clients.android.opentenure.model.LandUse landUse = (org.fao.sola.clients.android.opentenure.model.LandUse) iterator
					.next();

			keyValueMap.put(landUse.getType().toLowerCase(),dnl.getLocalizedDisplayName(landUse.getDisplayValue()));

		}
		return keyValueMap;
	}
	
	public Map<String,String> getValueKeyMap(String localization, boolean onlyActive) {

		List<org.fao.sola.clients.android.opentenure.model.LandUse> list;
		if(!onlyActive)
			list = getLandUses();
		else
			list = getLandUsesActive();
		
		DisplayNameLocalizer dnl = new DisplayNameLocalizer(OpenTenureApplication.getInstance().getLocalization());
		Map<String,String> keyValueMap = new HashMap<String,String>();

		for (Iterator<org.fao.sola.clients.android.opentenure.model.LandUse> iterator = list.iterator(); iterator.hasNext();) {
			org.fao.sola.clients.android.opentenure.model.LandUse landUse = (org.fao.sola.clients.android.opentenure.model.LandUse) iterator
					.next();

			keyValueMap.put(dnl.getLocalizedDisplayName(landUse.getDisplayValue()),landUse.getType());

		}
		return keyValueMap;
	}

	public int getIndexByCodeType(String code, boolean onlyActive) {

		List<org.fao.sola.clients.android.opentenure.model.LandUse> list;
		if(!onlyActive)
			list = getLandUses();
		else
			list = getLandUsesActive();

		int i = 0;

		for (Iterator<org.fao.sola.clients.android.opentenure.model.LandUse> iterator = list.iterator(); iterator.hasNext();) {
			org.fao.sola.clients.android.opentenure.model.LandUse landUse = (org.fao.sola.clients.android.opentenure.model.LandUse) iterator
					.next();

			if (landUse.getType().equals(code)) {

				return i;

			}

			i++;
		}
		return 0;

	}

	public String getDisplayValueByType(String value) {

		ResultSet rs = null;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("SELECT DISPLAY_VALUE FROM LAND_USE LU WHERE TYPE = ?");
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
	
	public static LandUse getLandUse(String type) {
		ResultSet result = null;
		Connection localConnection = null;
		PreparedStatement statement = null;
		LandUse landUse = new LandUse();
		try {
			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT TYPE, DESCRIPTION, DISPLAY_VALUE FROM LAND_USE WHERE TYPE=?");
			statement.setString(1, type);

			result = statement.executeQuery();

			if (result.next()) {

				landUse.setType(result.getString(1));
				landUse.setDescription(result.getString(2));
				landUse.setDisplayValue(result.getString(3));
				
				return landUse;
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
	
	public int updadateLandUse() {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {
			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE LAND_USE SET TYPE=?, DESCRIPTION=?, DISPLAY_VALUE=?, ACTIVE='true' WHERE TYPE = ?");
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
	
	public static int setAllLandUseNoActive() {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {
			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE LAND_USE LU SET ACTIVE='false' WHERE  LU.ACTIVE= 'true'");
			

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
