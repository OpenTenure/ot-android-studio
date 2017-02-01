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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;

public class ShareProperty {

	@Override
	public String toString() {
		return "ShareProperty [id=" + id + ", claimId=" + claimId + ", shares=" + shares + "]";
	}

	public String getClaimId() {
		return claimId;
	}

	public void setClaimId(String claimId) {
		this.claimId = claimId;
	}


	public int getShares() {
		return shares;
	}

	public void setShares(int shares) {
		this.shares = shares;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}


	String claimId;
	int shares;
	String id;

	Database db = OpenTenureApplication.getInstance().getDatabase();

	public ShareProperty() {

			this.id = UUID.randomUUID().toString();
	}

	public static int createShare(ShareProperty share) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("INSERT INTO SHARE(ID,CLAIM_ID, SHARES) VALUES (?,?,?)");
			statement.setString(1, share.getId());
			statement.setString(2, share.getClaimId());
			statement.setBigDecimal(3, new BigDecimal(share.getShares()));
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
					.prepareStatement("INSERT INTO SHARE(ID, CLAIM_ID, SHARES) VALUES (?,?,?)");
			statement.setString(1, getId());
			statement.setString(2, getClaimId());			
			statement.setBigDecimal(3, new BigDecimal(getShares()));
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

	public static int updateShare(ShareProperty share) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE SHARE SET SHARES=? WHERE ID=? ");
			statement.setBigDecimal(1, new BigDecimal(share.getShares()));
			statement.setString(2, share.getId());
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

	public int updateShare() {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE SHARE SET SHARES=? WHERE ID=?");
			statement.setBigDecimal(1, new BigDecimal(shares));
			statement.setString(2, id);
			
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

	public static int deleteShare(String shareId, Connection connection) {
		int result = 0;
		PreparedStatement statement = null;

		try {

			statement = connection
					.prepareStatement("DELETE SHARE WHERE ID=? ");
			statement.setString(1, shareId);
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

	public static int deleteShares(String claimId, Connection connection) {
		int result = 0;
		for (ShareProperty share : ShareProperty.getShares(claimId, connection)) {

			for (Owner owner: Owner.getOwners(share.getId(), connection)) {
				Owner.deleteOwner(share.getId(), owner.getPersonId(), connection);
				Person.deletePerson(owner.getPersonId(), connection);
			}
			result += ShareProperty.deleteShare(share.getId(), connection);
		}
		return result;
	}

	public static int deleteShare(String shareId) {
		int result = 0;
		Connection localConnection = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			result = deleteShare(shareId, localConnection);
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (localConnection != null) {
				try {
					localConnection.close();
				} catch (SQLException e) {
				}
			}
		}
		return result;
	}

	public int deleteShare() {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			
			statement = localConnection
					.prepareStatement("DELETE OWNER WHERE SHARE_ID=?");
			statement.setString(1, id);
			result = statement.executeUpdate();
			statement.close();
			
			statement = localConnection
					.prepareStatement("DELETE SHARE WHERE ID=?");
			statement.setString(1, id);
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

	public static List<ShareProperty> getShares(String claimId) {
		List<ShareProperty> shareList = new ArrayList<ShareProperty>();
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT SHARE.ID, SHARE.CLAIM_ID, SHARE.SHARES FROM SHARE WHERE SHARE.CLAIM_ID=? ORDER BY SHARE.ID");
			statement.setString(1, claimId);
			rs = statement.executeQuery();
			while (rs.next()) {
				ShareProperty share = new ShareProperty();
				share.setId(rs.getString(1));
				share.setClaimId(claimId);
				share.setShares(rs.getBigDecimal(3).intValue());
				shareList.add(share);
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
		return shareList;
	}

	public static List<ShareProperty> getShares(String claimId, Connection externalConnection) {
		List<ShareProperty> shareList = new ArrayList<ShareProperty>();
		PreparedStatement statement = null;

		try {

			statement = externalConnection
					.prepareStatement("SELECT SHARE.ID, SHARE.CLAIM_ID, SHARE.SHARES FROM SHARE WHERE SHARE.CLAIM_ID=? ORDER BY SHARE.ID");
			statement.setString(1, claimId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				ShareProperty share = new ShareProperty();
				share.setId(rs.getString(1));
				share.setClaimId(claimId);
				share.setShares(rs.getBigDecimal(3).intValue());
				shareList.add(share);
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
		return shareList;
	}

	public static ShareProperty getShare(String id) {

		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		ShareProperty share = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT SHARE.ID, SHARE.CLAIM_ID, SHARE.SHARES FROM SHARE WHERE SHARE.ID=?");
			statement.setString(1, id);
			
			rs = statement.executeQuery();
			while (rs.next()) {
				
				share = new ShareProperty();
				share.setId(rs.getString(1));
				share.setClaimId(rs.getString(2));
				share.setShares(rs.getBigDecimal(3).intValue());
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
		return share;
	}

	
}
