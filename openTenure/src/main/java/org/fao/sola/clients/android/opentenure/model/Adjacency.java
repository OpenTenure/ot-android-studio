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

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;

import android.content.Context;

public class Adjacency {
	
	@Override
	public String toString() {
		return "Adjacency [sourceClaimId=" + sourceClaimId + ", destClaimId="
				+ destClaimId + ", cardinalDirection=" + cardinalDirection
				+ "]";
	}

	public String getSourceClaimId() {
		return sourceClaimId;
	}

	public void setSourceClaimId(String sourceClaimId) {
		this.sourceClaimId = sourceClaimId;
	}

	public String getDestClaimId() {
		return destClaimId;
	}

	public void setDestClaimId(String destClaimId) {
		this.destClaimId = destClaimId;
	}

	public CardinalDirection getCardinalDirection() {
		return cardinalDirection;
	}

	public static CardinalDirection getReverseCardinalDirection(CardinalDirection cardinalDirection) {
		switch (cardinalDirection) {
		case NORTH:
			return CardinalDirection.SOUTH;
		case SOUTH:
			return CardinalDirection.NORTH;
		case EAST:
			return CardinalDirection.WEST;
		case WEST:
			return CardinalDirection.EAST;
		case NORTHEAST:
			return CardinalDirection.SOUTHWEST;
		case NORTHWEST:
			return CardinalDirection.SOUTHEAST;
		case SOUTHEAST:
			return CardinalDirection.NORTHWEST;
		case SOUTHWEST:
			return CardinalDirection.NORTHEAST;

		default:
			return CardinalDirection.NONE;
		}
	}

	public void setCardinalDirection(CardinalDirection cardinalDirection) {
		this.cardinalDirection = cardinalDirection;
	}

	public enum CardinalDirection{NORTH, SOUTH, EAST, WEST, NORTHEAST, NORTHWEST, SOUTHEAST, SOUTHWEST, NONE};

	String sourceClaimId;
	String destClaimId;
	CardinalDirection cardinalDirection;

	Database db = OpenTenureApplication.getInstance().getDatabase();

	public static int createAdjacency(Adjacency adj) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("INSERT INTO ADJACENCY(SOURCE_CLAIM_ID, DEST_CLAIM_ID, CARDINAL_DIRECTION) VALUES (?,?,?)");
			statement.setString(1, adj.getSourceClaimId());
			statement.setString(2, adj.getDestClaimId());
			statement.setString(3, adj.getCardinalDirection().name());
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
					.prepareStatement("INSERT INTO ADJACENCY(SOURCE_CLAIM_ID, DEST_CLAIM_ID, CARDINAL_DIRECTION) VALUES (?,?,?)");
			statement.setString(1, getSourceClaimId());
			statement.setString(2, getDestClaimId());
			statement.setString(3, getCardinalDirection().name());
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

	public static int deleteAdjacency(Adjacency adj) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("DELETE ADJACENCY WHERE SOURCE_CLAIM_ID=? AND DEST_CLAIM_ID=?");
			statement.setString(1, adj.getSourceClaimId());
			statement.setString(2, adj.getDestClaimId());
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

	public static int deleteAdjacencies(String claimId) {
		int result = 0;
		Connection localConnection = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			result = deleteAdjacencies(claimId, localConnection);
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

	public static int deleteAdjacencies(String claimId, Connection connection) {
		int result = 0;
		PreparedStatement statement = null;

		try {

			statement = connection
					.prepareStatement("DELETE ADJACENCY WHERE SOURCE_CLAIM_ID=? OR DEST_CLAIM_ID=?");
			statement.setString(1, claimId);
			statement.setString(2, claimId);
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

	public int delete() {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("DELETE ADJACENCY WHERE SOURCE_CLAIM_ID=? AND DEST_CLAIM_ID=?");
			statement.setString(1, getSourceClaimId());
			statement.setString(2, getDestClaimId());
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

	public static List<Adjacency> getAdjacencies(String claimId) {
		List<Adjacency> adjList = new ArrayList<Adjacency>();
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT ADJ.SOURCE_CLAIM_ID, ADJ.DEST_CLAIM_ID, ADJ.CARDINAL_DIRECTION FROM ADJACENCY ADJ WHERE SOURCE_CLAIM_ID=? OR DEST_CLAIM_ID=?");
			statement.setString(1, claimId);
			statement.setString(2, claimId);
			rs = statement.executeQuery();
			while (rs.next()) {
				Adjacency adj = new Adjacency();
				adj.setSourceClaimId(rs.getString(1));
				adj.setDestClaimId(rs.getString(2));
				adj.setCardinalDirection(CardinalDirection.valueOf(rs.getString(3)));
				adjList.add(adj);
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
		return adjList;
	}
	public static String getCardinalDirection(Context context, CardinalDirection cardinalDirection){
		switch(cardinalDirection){
		case NORTH:
			return context.getResources().getString(R.string.north);
		case SOUTH:
			return context.getResources().getString(R.string.south);
		case EAST:
			return context.getResources().getString(R.string.east);
		case WEST:
			return context.getResources().getString(R.string.west);
		case NORTHEAST:
			return context.getResources().getString(R.string.north_east);
		case NORTHWEST:
			return context.getResources().getString(R.string.north_west);
		case SOUTHEAST:
			return context.getResources().getString(R.string.south_east);
		case SOUTHWEST:
			return context.getResources().getString(R.string.south_west);
		default:
			return "";
		}
	}

}
