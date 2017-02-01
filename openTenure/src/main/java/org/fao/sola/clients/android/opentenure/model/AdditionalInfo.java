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

public class AdditionalInfo {

	Database db = OpenTenureApplication.getInstance().getDatabase();

	public AdditionalInfo() {
		this.additionalInfoId = UUID.randomUUID().toString();
	}

	public String getClaimId() {
		return claimId;
	}

	public void setClaimId(String claimId) {
		this.claimId = claimId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "AdditionalInfo [" + "additionalInfoId=" + additionalInfoId
				+ ", claimId=" + claimId + ", name="
				+ name + ", value=" + value + "]";
	}

	public static int createAdditionalInfo(AdditionalInfo additionalInfo) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("INSERT INTO ADDITIONAL_INFO (ADDITIONAL_INFO_ID, CLAIM_ID, NAME, VALUE) VALUES(?,?,?,?)");
			statement.setString(1, additionalInfo.getAdditionalInfoId());
			statement.setString(2, additionalInfo.getClaimId());
			statement.setString(3, additionalInfo.getName());
			statement.setString(4, additionalInfo.getValue());
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
					.prepareStatement("INSERT INTO ADDITIONAL_INFO (ADDITIONAL_INFO_ID, CLAIM_ID, NAME, VALUE) VALUES(?,?,?,?)");
			statement.setString(1, getAdditionalInfoId());
			statement.setString(2, getClaimId());
			statement.setString(3, getName());
			statement.setString(4, getValue());
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
					.prepareStatement("DELETE FROM ADDITIONAL_INFO WHERE ADDITIONAL_INFO_ID=?");
			statement.setString(1, getAdditionalInfoId());
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

	public static int deleteAdditionalInfo(AdditionalInfo additionalInfo) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("DELETE FROM ADDITIONAL_INFO WHERE ADDITIONAL_INFO_ID=?");
			statement.setString(1, additionalInfo.getAdditionalInfoId());
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

	public static int updateAdditionalInfo(AdditionalInfo additionalInfo) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE ADDITIONAL_INFO SET CLAIM_ID=?, NAME=?, VALUE=? WHERE ADDITIONAL_INFO_ID=?");
			statement.setString(1, additionalInfo.getClaimId());
			statement.setString(2, additionalInfo.getName());
			statement.setString(3, additionalInfo.getValue());
			statement.setString(4, additionalInfo.getAdditionalInfoId());
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
					.prepareStatement("UPDATE ADDITIONAL_INFO SET CLAIM_ID=?, NAME=?, VALUE=? WHERE ADDITIONAL_INFO_ID=?");
			statement.setString(1, getClaimId());
			statement.setString(2, getName());
			statement.setString(3, getValue());
			statement.setString(4, getAdditionalInfoId());
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

	public static AdditionalInfo getAdditionalInfo(String additionalInfoId) {
		AdditionalInfo additionalInfo = null;
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT CLAIM_ID, NAME, VALUE FROM ADDITIONAL_INFO WHERE ADDITIONAL_INFO_ID=?");
			statement.setString(1, additionalInfoId);
			rs = statement.executeQuery();
			while (rs.next()) {
				additionalInfo = new AdditionalInfo();
				additionalInfo.setAdditionalInfoId(additionalInfoId);
				additionalInfo.setClaimId(rs.getString(1));
				additionalInfo.setName(rs.getString(2));
				additionalInfo.setValue(rs.getString(3));
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
		return additionalInfo;
	}

	public String getAdditionalInfo(String claimId, String name) {
		String value = null;
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("SELECT VALUE FROM ADDITIONAL_INFO META WHERE META.CLAIM_ID=? AND META.NAME=?");
			statement.setString(1, claimId);
			statement.setString(2, name);
			rs = statement.executeQuery();
			while (rs.next()) {
				value = rs.getString(1);
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
		return value;
	}

	public String getAdditionalInfoId() {
		return additionalInfoId;
	}

	public void setAdditionalInfoId(String additionalInfoId) {
		this.additionalInfoId = additionalInfoId;
	}

	public static List<AdditionalInfo> getClaimAdditionalInfo(String claimId) {
		List<AdditionalInfo> additionalInfo = new ArrayList<AdditionalInfo>();
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT ADDITIONAL_INFO_ID, NAME, VALUE FROM ADDITIONAL_INFO META WHERE META.CLAIM_ID=?");
			statement.setString(1, claimId);
			rs = statement.executeQuery();
			while (rs.next()) {
				AdditionalInfo item = new AdditionalInfo();
				item.setClaimId(claimId);
				item.setAdditionalInfoId(rs.getString(1));
				item.setName(rs.getString(2));
				item.setValue(rs.getString(3));
				additionalInfo.add(item);
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
		return additionalInfo;
	}

	String additionalInfoId;
	String claimId;
	String name;
	String value;

}
