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
import org.fao.sola.clients.android.opentenure.network.response.BoundaryTypeResponse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BoundaryType extends RefDataModel {
	private static String tableName = "BOUNDARY_TYPE";
	int level;

	@Override
	public String toString() {
		return "BoundaryType [code=" + code + ", description=" + description + ", displayValue=" + displayValue + ", active=" + active + "]";
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	@Override
	public int insert() {
		try {
			PreparedStatement statement = prepareStatement("INSERT INTO " + tableName + "(CODE, LEVEL, DESCRIPTION, DISPLAY_VALUE, ACTIVE) VALUES (?,?,?,?,?)");
			statement.setString(1, getCode());
			statement.setInt(2, getLevel());
			statement.setString(3, getDescription());
			statement.setString(4, getDisplayValue());
			statement.setBoolean(5, getActive());

			return executeStatement(statement);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return 0;
	}

	public static Map<String,String> getKeyValueMap(boolean onlyActive) {
		return getKeyValueMap(tableName, BoundaryType.class, onlyActive);
	}

	public static BoundaryType getItem(String code) {
		ResultSet rs = null;
		PreparedStatement statement = null;

		try {
			statement = prepareStatement("SELECT LEVEL, DESCRIPTION, DISPLAY_VALUE, ACTIVE FROM " + tableName + " WHERE CODE=?");
			statement.setString(1, code);
			rs = executeSelect(statement);

			if(rs != null) {
				if (rs.next()) {
					BoundaryType boundaryType = new BoundaryType();
					boundaryType.setCode(code);
					boundaryType.setLevel(rs.getInt(1));
					boundaryType.setDescription(rs.getString(2));
					boundaryType.setDisplayValue(rs.getString(3));
					boundaryType.setActive(rs.getBoolean(4));
					return boundaryType;
				}
			}
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
					if(statement.getConnection() != null && !statement.getConnection().isClosed()) {
						statement.getConnection().close();
					}
					statement.close();
				} catch (SQLException e) {
				}
			}
		}
		return null;
	}

	@Override
	public int update() {
		try {
			PreparedStatement statement = prepareStatement("UPDATE " + tableName + " SET CODE=?, LEVEL=?, DESCRIPTION=?, DISPLAY_VALUE=?, ACTIVE=? WHERE CODE = ?");
			statement.setString(1, getCode());
			statement.setInt(2, getLevel());
			statement.setString(3, getDescription());
			statement.setString(4, getDisplayValue());
			statement.setBoolean(5, getActive());
			statement.setString(6, getCode());

			return executeStatement(statement);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return 0;
	}

	public static void update(List<BoundaryTypeResponse> types) {
		if (types != null && (types.size() > 0) && tableName != null) {
			PreparedStatement statement = prepareStatement("UPDATE " + tableName + " SET ACTIVE='false' WHERE ACTIVE= 'true'");
			executeStatement(statement);

			for (Iterator<BoundaryTypeResponse> iterator = types.iterator(); iterator.hasNext();) {
				BoundaryTypeResponse response = iterator.next();

				try {
					BoundaryType item = new BoundaryType();

					item.setDescription(response.getDescription());
					item.setCode(response.getCode());
					item.setDisplayValue(response.getDisplayValue());
					item.setLevel(response.getLevel());
					item.setActive(response.getStatus().equalsIgnoreCase("c"));
					if (item.getItem(response.getCode()) == null) {
						item.insert();
					}
					else {
						item.update();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
