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

import org.fao.sola.clients.android.opentenure.DisplayNameLocalizer;
import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.tools.StringUtility;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Boundary {

	Database db = OpenTenureApplication.getInstance().getDatabase();
	private String id;
	private String name;
	private String typeCode;
	private String authorityName;
	private String parentId;
	private String recorderName;
	private String statusCode;
	private String geom;
	private boolean processed;
	private int version;
	private String displayName;
	private static final String SQL_SELECT = "SELECT ID, NAME, TYPE_CODE, AUTHORITY_NAME, PARENT_ID, RECORDER_NAME, GEOM, STATUS_CODE, PROCESSED, VERSION FROM BOUNDARY ";

	public Boundary(){
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getTypeCode() {
		return typeCode;
	}

	public void setTypeCode(String typeCode) {
		this.typeCode = typeCode;
	}

	public String getAuthorityName() {
		return authorityName;
	}

	public void setAuthorityName(String authorityName) {
		this.authorityName = authorityName;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getRecorderName() {
		return recorderName;
	}

	public void setRecorderName(String recorderName) {
		this.recorderName = recorderName;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public String getGeom() {
		return geom;
	}

	public void setGeom(String geom) {
		this.geom = geom;
	}

	public boolean isProcessed() {
		return processed;
	}

	public void setProcessed(boolean processed) {
		this.processed = processed;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	@Override
	public String toString() {
		if(getDisplayName() == null){
			if(getName() == null){
				return "";
			} else {
				return getName();
			}
		} else {
			return getDisplayName();
		}
	}

	public int insert() {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {
			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("INSERT INTO BOUNDARY(ID, NAME, TYPE_CODE, AUTHORITY_NAME, PARENT_ID, RECORDER_NAME, GEOM, STATUS_CODE, PROCESSED, VERSION) VALUES (?,?,?,?,?,?,?,?,?,?)");

			statement.setString(1, getId());
			statement.setString(2, getName());
			statement.setString(3, getTypeCode());
			statement.setString(4, getAuthorityName());
			statement.setString(5, getParentId());
			statement.setString(6, getRecorderName());
			statement.setString(7, getGeom());
			statement.setString(8, getStatusCode());
			statement.setBoolean(9, isProcessed());
			statement.setInt(10, getVersion());

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

	public static List<Boundary> getBoundariesByStatus(String statusCode) {
		List<Boundary> boundaries = new ArrayList<Boundary>();
		ResultSet rs = null;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {
			localConnection = OpenTenureApplication.getInstance().getDatabase().getConnection();
			String sql = SQL_SELECT + " ORDER BY PARENT_ID, NAME";
			if(statusCode != null && !statusCode.equals("")){
				sql = SQL_SELECT + " WHERE STATUS_CODE=? ORDER BY PARENT_ID, NAME";
			}
			statement = localConnection.prepareStatement(sql);
			if(statusCode != null && !statusCode.equals("")){
				statement.setString(1, statusCode);
			}
			rs = statement.executeQuery();

			while (rs.next()) {
				boundaries.add(makeBoundaryFromResultSet(rs));
			}
			return boundaries;
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
		return boundaries;
	}

	public static List<Boundary> getAllChildrenBoundaries(String parentId) {
		List<Boundary> childrenBoundaries = getChildrenBoundaries(parentId);
		List<Boundary> boundaries = new ArrayList<>();

		while (childrenBoundaries.size() > 0){
			List<Boundary> tmpBoundaries = new ArrayList<>();
			for(Boundary b : childrenBoundaries){
				tmpBoundaries.addAll(getChildrenBoundaries(b.getId()));
			}
			childrenBoundaries.clear();
			childrenBoundaries.addAll(tmpBoundaries);
			boundaries.addAll(childrenBoundaries);
		}

		return boundaries;
	}

	public static List<Boundary> getChildrenBoundaries(String parentId) {
		List<Boundary> boundaries = new ArrayList<Boundary>();
		ResultSet rs = null;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {
			localConnection = OpenTenureApplication.getInstance().getDatabase().getConnection();
			String sql = SQL_SELECT + " WHERE PARENT_ID=? ORDER BY NAME";
			statement = localConnection.prepareStatement(sql);
			statement.setString(1, parentId);
			rs = statement.executeQuery();

			while (rs.next()) {
				boundaries.add(makeBoundaryFromResultSet(rs));
			}
			return boundaries;
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
		return boundaries;
	}

	private static Boundary makeBoundaryFromResultSet(ResultSet rs) throws SQLException {
		Boundary boundary = new Boundary();
		boundary.setId(rs.getString(1));
		boundary.setName(rs.getString(2));
		boundary.setTypeCode(rs.getString(3));
		boundary.setAuthorityName(rs.getString(4));
		boundary.setParentId(rs.getString(5));
		boundary.setRecorderName(rs.getString(6));
		boundary.setGeom(rs.getString(7));
		boundary.setStatusCode(rs.getString(8));
		boundary.setProcessed(rs.getBoolean(9));
		boundary.setVersion(rs.getInt(10));
		return boundary;
	}

	public static List<Boundary> getFormattedBoundariesAll(boolean addDummy) {
		List<Boundary> boundaries = formatBoundaries(getBoundariesByStatus(null), null, 0);
		if(addDummy){
			Boundary dummy = new Boundary();
			dummy.setName("");
			boundaries.add(0, dummy);
		}
		return boundaries;
	}

	public static List<Boundary> getFormattedBoundariesApproved(boolean addDummy) {
		List<Boundary> boundaries = formatBoundaries(getBoundariesByStatus("approved"), null, 0);
		if(addDummy){
			Boundary dummy = new Boundary();
			dummy.setName("");
			boundaries.add(0, dummy);
		}
		return boundaries;
	}

	public static List<Boundary> getFormattedParentBoundaries(boolean addDummy) {
		List<Boundary> boundaries = new ArrayList<Boundary>();
		ResultSet rs = null;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {
			localConnection = OpenTenureApplication.getInstance().getDatabase().getConnection();
			String sql = SQL_SELECT + " WHERE TYPE_CODE NOT IN (SELECT CODE FROM BOUNDARY_TYPE ORDER BY LEVEL DESC LIMIT 1) ORDER BY PARENT_ID, NAME";
			statement = localConnection.prepareStatement(sql);
			rs = statement.executeQuery();

			while (rs.next()) {
				boundaries.add(makeBoundaryFromResultSet(rs));
			}

			boundaries = formatBoundaries(boundaries, null, 0);
			if(addDummy){
				Boundary dummy = new Boundary();
				dummy.setName("");
				boundaries.add(0, dummy);
			}

			return boundaries;
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
		return boundaries;
	}

	private static List<Boundary> formatBoundaries(List<Boundary> boundaries, String parentId, int level) {
		List<Boundary> result = new ArrayList<>();
		if(boundaries == null || boundaries.size() < 1){
			return result;
		}

		for(Boundary boundary : boundaries){
			if(StringUtility.empty(boundary.getParentId()).equals(StringUtility.empty(parentId))){
				if(level > 0){
					String space = "";
					for(int i = 0; i < level; i++){
						space += "   ";
					}
					boundary.setDisplayName(space + boundary.getName());
				} else {
					boundary.setDisplayName(boundary.getName());
				}
				result.add(boundary);
				result.addAll(formatBoundaries(boundaries, boundary.getId(), level + 1));
			}
		}
		return result;
	}

	public static Boundary getById(String id) {
		ResultSet result = null;
		Connection localConnection = null;
		PreparedStatement statement = null;
		Boundary boundary = new Boundary();
		try {
			localConnection = OpenTenureApplication.getInstance().getDatabase().getConnection();
			statement = localConnection.prepareStatement(SQL_SELECT + " WHERE ID=?");
			statement.setString(1, id);
			result = statement.executeQuery();

			if (result.next()) {
				return makeBoundaryFromResultSet(result);
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
	
	public int update() {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {
			localConnection = OpenTenureApplication.getInstance().getDatabase().getConnection();
			statement = localConnection
					.prepareStatement("UPDATE BOUNDARY SET NAME=?, TYPE_CODE=?, AUTHORITY_NAME=?, PARENT_ID=?, RECORDER_NAME=?, GEOM=?, STATUS_CODE=?, PROCESSED=?, VERSION=? WHERE ID = ?");

			statement.setString(1, getName());
			statement.setString(2, getTypeCode());
			statement.setString(3, getAuthorityName());
			statement.setString(4, getParentId());
			statement.setString(5, getRecorderName());
			statement.setString(6, getGeom());
			statement.setString(7, getStatusCode());
			statement.setBoolean(8, isProcessed());
			statement.setInt(9, getVersion());
			statement.setString(10, getId());

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
			localConnection = OpenTenureApplication.getInstance().getDatabase().getConnection();
			statement = localConnection.prepareStatement("DELETE BOUNDARY WHERE ID=?");
			statement.setString(1, getId());

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

	public int markProcessed(String boundaryServerId) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {
			localConnection = OpenTenureApplication.getInstance().getDatabase().getConnection();
			statement = localConnection
					.prepareStatement("UPDATE BOUNDARY SET ID=?, RECORDER_NAME=?, PROCESSED=? WHERE ID = ?");
			statement.setString(1, boundaryServerId);
			statement.setString(2, OpenTenureApplication.getUsername());
			statement.setBoolean(3, true);
			statement.setString(4, getId());

			result = statement.executeUpdate();
			setProcessed(true);
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

	public static void updateBoundariesFromResponse(List<org.fao.sola.clients.android.opentenure.network.response.Boundary> boundaries){
		if (boundaries != null && (boundaries.size() > 0)) {
			for (Iterator<org.fao.sola.clients.android.opentenure.network.response.Boundary> iterator = boundaries.iterator(); iterator.hasNext();) {
				org.fao.sola.clients.android.opentenure.network.response.Boundary boundary = iterator.next();
				Boundary dbBoundary = new Boundary();

				dbBoundary.setId(boundary.getId());
				dbBoundary.setName(boundary.getName());
				dbBoundary.setAuthorityName(boundary.getAuthorityName());
				dbBoundary.setTypeCode(boundary.getTypeCode());
				dbBoundary.setParentId(boundary.getParentId());

				dbBoundary.setGeom(boundary.getGeom());
				dbBoundary.setRecorderName(boundary.getRecorderName());
				dbBoundary.setStatusCode(boundary.getStatusCode());
				dbBoundary.setVersion(boundary.getRowVersion());
				dbBoundary.setProcessed(true);

				if (org.fao.sola.clients.android.opentenure.model.Boundary.getById(boundary.getId()) == null)
					dbBoundary.insert();
				else
					dbBoundary.update();
			}
		}
	}

	public org.fao.sola.clients.android.opentenure.network.response.Boundary convertToResponse(){
		org.fao.sola.clients.android.opentenure.network.response.Boundary responseBoundary = new org.fao.sola.clients.android.opentenure.network.response.Boundary();

		responseBoundary.setId(getId());
		responseBoundary.setName(getName());
		responseBoundary.setAuthorityName(getAuthorityName());
		responseBoundary.setTypeCode(getTypeCode());
		responseBoundary.setParentId(getParentId());

		responseBoundary.setGeom(getGeom());
		responseBoundary.setRecorderName(getRecorderName());
		responseBoundary.setStatusCode(getStatusCode());
		responseBoundary.setRowVersion(getVersion());
		return responseBoundary;
	}
}
