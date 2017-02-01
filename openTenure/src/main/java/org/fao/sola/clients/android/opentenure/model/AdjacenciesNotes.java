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

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;

public class AdjacenciesNotes {

	static Database db = OpenTenureApplication.getInstance().getDatabase();

	private String northAdjacency;
	private String southAjacency;
	private String eastAdjacency;
	private String westAdjacency;
	private String claimId;

	public String getNorthAdjacency() {
		return northAdjacency;
	}

	public void setNorthAdjacency(String northAdjacency) {
		this.northAdjacency = northAdjacency;
	}

	public String getSouthAdjacency() {
		return southAjacency;
	}

	public void setSouthAdjacency(String southAdjacency) {
		this.southAjacency = southAdjacency;
	}

	public String getEastAdjacency() {
		return eastAdjacency;
	}

	public void setEastAdjacency(String eastAdjacency) {
		this.eastAdjacency = eastAdjacency;
	}

	public String getWestAdjacency() {
		return westAdjacency;
	}

	public void setWestAdjacency(String westAdjacency) {
		this.westAdjacency = westAdjacency;
	}

	public String getClaimId() {
		return claimId;
	}

	public void setClaimId(String claimId) {
		this.claimId = claimId;
	}

	@Override
	public String toString() {
		return "AdjacenciesNotes [" + "northAdjacency=" + northAdjacency
				+ ", southAdjacency=" + southAjacency + ", eastAdjacency="
				+ eastAdjacency + ", westAdjacency=" + westAdjacency + "]";
	}

	public static int createAdjacenciesNotes(AdjacenciesNotes adjacenciesNotes) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement(" INSERT INTO ADJACENCIES_NOTES ( CLAIM_ID, NORTH_ADJACENCY, SOUTH_ADJACENCY, EAST_ADJACENCY,WEST_ADJACENCY) VALUES(?,?,?,?,?)");
			statement.setString(1, adjacenciesNotes.getClaimId());
			statement.setString(2, adjacenciesNotes.getNorthAdjacency());
			statement.setString(3, adjacenciesNotes.getSouthAdjacency());
			statement.setString(4, adjacenciesNotes.getEastAdjacency());
			statement.setString(5, adjacenciesNotes.getWestAdjacency());

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

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement(" INSERT INTO ADJACENCIES_NOTES ( CLAIM_ID, NORTH_ADJACENCY, SOUTH_ADJACENCY, EAST_ADJACENCY,WEST_ADJACENCY) VALUES(?,?,?,?,?)");
			statement.setString(1, getClaimId());
			statement.setString(2, getNorthAdjacency());
			statement.setString(3, getSouthAdjacency());
			statement.setString(4, getEastAdjacency());
			statement.setString(5, getWestAdjacency());

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
					.prepareStatement("DELETE FROM ADJACENCIES_NOTES WHERE CLAIM_ID=?");
			statement.setString(1, getClaimId());
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

	public static int deleteAdjacenciesNotes(AdjacenciesNotes adjacenciesNotes) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("DELETE FROM ADJACENCIES_NOTES WHERE CLAIM_ID=?");
			statement.setString(1, adjacenciesNotes.getClaimId());
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

	public static int deleteAdjacenciesNotes(String claimId, Connection connection) {
		int result = 0;
		PreparedStatement statement = null;

		try {

			statement = connection
					.prepareStatement("DELETE FROM ADJACENCIES_NOTES WHERE CLAIM_ID=?");
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

	public static int updateAdjacenciesNotes(AdjacenciesNotes adjacenciesNotes) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement(" UPDATE ADJACENCIES_NOTES SET NORTH_ADJACENCY=?, SOUTH_ADJACENCY=?, EAST_ADJACENCY=?, WEST_ADJACENCY=? WHERE CLAIM_ID=? ");

			statement.setString(5, adjacenciesNotes.getClaimId());
			statement.setString(1, adjacenciesNotes.getNorthAdjacency());
			statement.setString(2, adjacenciesNotes.getSouthAdjacency());
			statement.setString(3, adjacenciesNotes.getEastAdjacency());
			statement.setString(4, adjacenciesNotes.getWestAdjacency());

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

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE ADJACENCIES_NOTES SET NORTH_ADJACENCY=?, SOUTH_ADJACENCY=?, EAST_ADJACENCY=?, WEST_ADJACENCY=? WHERE CLAIM_ID=?");
			statement.setString(5, getClaimId());
			statement.setString(1, getNorthAdjacency());
			statement.setString(2, getSouthAdjacency());
			statement.setString(3, getEastAdjacency());
			statement.setString(4, getWestAdjacency());

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

	public static AdjacenciesNotes getAdjacenciesNotes(String claimId) {
		AdjacenciesNotes adjacenciesNotes = null;
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT NORTH_ADJACENCY, SOUTH_ADJACENCY, EAST_ADJACENCY ,WEST_ADJACENCY FROM ADJACENCIES_NOTES WHERE CLAIM_ID=?");
			statement.setString(1, claimId);
			rs = statement.executeQuery();
			while (rs.next()) {
				adjacenciesNotes = new AdjacenciesNotes();
				adjacenciesNotes.setClaimId(claimId);
				adjacenciesNotes.setNorthAdjacency(rs.getString(1));
				adjacenciesNotes.setSouthAdjacency(rs.getString(2));
				adjacenciesNotes.setEastAdjacency(rs.getString(3));
				adjacenciesNotes.setWestAdjacency(rs.getString(4));
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
		return adjacenciesNotes;
	}

}
