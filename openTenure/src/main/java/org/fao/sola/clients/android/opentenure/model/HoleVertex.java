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

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.maps.Constants;
import org.fao.sola.clients.android.opentenure.maps.WKTWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HoleVertex {

	public static final LatLng INVALID_POSITION = new LatLng(400.0, 400.0);

	public int getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	Database db = OpenTenureApplication.getInstance().getDatabase();

	public HoleVertex() {
		this.holeVertexId = UUID.randomUUID().toString();
	}

	public HoleVertex(HoleVertex vertex) {
		this.holeVertexId = UUID.randomUUID().toString();
		this.GPSPosition = vertex.getGPSPosition();
		this.mapPosition = vertex.getMapPosition();
		this.claimId = vertex.getClaimId();
		this.holeNumber = vertex.getHoleNumber();
		this.sequenceNumber = vertex.getSequenceNumber();
	}

	public HoleVertex(LatLng mapPosition) {
		this.holeVertexId = UUID.randomUUID().toString();
		setMapPosition(mapPosition);
		setGPSPosition(INVALID_POSITION);
	}

	public HoleVertex(LatLng mapPosition, LatLng GPSPosition) {
		this.holeVertexId = UUID.randomUUID().toString();
		setMapPosition(mapPosition);
		setGPSPosition(GPSPosition);
	}

	public LatLng getMapPosition() {
		return mapPosition;
	}

	public LatLng getGPSPosition() {
		return GPSPosition;
	}

	@Override
	public String toString() {
		return "HoleVertex ["
				+ "holeVertexId=" + holeVertexId
				+ ", claimId=" + claimId
				+ ", holeNumber=" + holeNumber
				+ ", sequenceNumber=" + sequenceNumber
				+ ", GPSLat=" + GPSPosition.latitude
				+ ", GPSLon=" + GPSPosition.longitude
				+ ", MapLat=" + mapPosition.latitude
				+ ", MapLon=" + mapPosition.longitude
				+ "]";
	}

	public void setGPSPosition(LatLng GPSPosition) {
		this.GPSPosition = GPSPosition;
	}

	public void setMapPosition(LatLng mapPosition) {
		this.mapPosition = mapPosition;
	}

	public static int createVertices(List<List<HoleVertex>> holes) {
		int result = 0;
		for(List<HoleVertex> hole:holes){
			for(HoleVertex vertex:hole){
				result += createVertex(vertex);
			}
		}
		return result;
	}

	public static int createVertex(HoleVertex vertex) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("INSERT INTO HOLE_VERTEX(HOLE_VERTEX_ID, CLAIM_ID, HOLE_NUMBER, SEQUENCE_NUMBER, GPS_LAT, GPS_LON, MAP_LAT, MAP_LON) VALUES(?,?,?,?,?,?,?,?)");
			statement.setString(1, vertex.getHoleVertexId());
			statement.setString(2, vertex.getClaimId());
			statement.setInt(3, vertex.getHoleNumber());
			statement.setInt(4, vertex.getSequenceNumber());
			statement.setBigDecimal(5, new BigDecimal(
					vertex.getGPSPosition().latitude));
			statement.setBigDecimal(6, new BigDecimal(
					vertex.getGPSPosition().longitude));
			statement.setBigDecimal(7, new BigDecimal(
					vertex.getMapPosition().latitude));
			statement.setBigDecimal(8, new BigDecimal(
					vertex.getMapPosition().longitude));
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
					.prepareStatement("INSERT INTO HOLE_VERTEX(HOLE_VERTEX_ID, CLAIM_ID, HOLE_NUMBER, SEQUENCE_NUMBER, GPS_LAT, GPS_LON, MAP_LAT, MAP_LON) VALUES(?,?,?,?,?,?,?,?)");
			statement.setString(1, getHoleVertexId());
			statement.setString(2, getClaimId());
			statement.setInt(3, getHoleNumber());
			statement.setInt(4, getSequenceNumber());
			statement.setBigDecimal(5,
					new BigDecimal(getGPSPosition().latitude));
			statement.setBigDecimal(6, new BigDecimal(
					getGPSPosition().longitude));
			statement.setBigDecimal(7,
					new BigDecimal(getMapPosition().latitude));
			statement.setBigDecimal(8, new BigDecimal(
					getMapPosition().longitude));
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

	public static int deleteVertex(HoleVertex vertex) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {
			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("DELETE FROM HOLE_VERTEX WHERE HOLE_VERTEX_ID=?");
			statement.setString(1, vertex.getHoleVertexId());
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

	public static int deleteVertices(String claimId) {
		int result = 0;
		Connection localConnection = null;
		try {
			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			result = deleteVertices(claimId, localConnection);
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

	public static int deleteVertices(String claimId, Connection connection) {
		int result = 0;
		PreparedStatement statement = null;

		try {
			statement = connection
					.prepareStatement("DELETE FROM HOLE_VERTEX WHERE CLAIM_ID=?");
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

	public int delete() {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {
			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("DELETE FROM HOLE_VERTEX WHERE HOLE_VERTEX_ID=?");
			statement.setString(1, getHoleVertexId());
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

	public static int updateVertex(HoleVertex vertex) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE HOLE_VERTEX SET CLAIM_ID=?, HOLE_NUMBER=?, SEQUENCE_NUMBER=?, GPS_LAT=?, GPS_LON=?, MAP_LAT=?, MAP_LON=? WHERE HOLE_VERTEX_ID=?");
			statement.setString(1, vertex.getClaimId());
			statement.setInt(2, vertex.getHoleNumber());
			statement.setInt(3, vertex.getSequenceNumber());
			statement.setBigDecimal(4, new BigDecimal(
					vertex.getGPSPosition().latitude));
			statement.setBigDecimal(5, new BigDecimal(
					vertex.getGPSPosition().longitude));
			statement.setBigDecimal(6, new BigDecimal(
					vertex.getMapPosition().latitude));
			statement.setBigDecimal(7, new BigDecimal(
					vertex.getMapPosition().longitude));
			statement.setString(8, vertex.getHoleVertexId());
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
					.prepareStatement("UPDATE HOLE_VERTEX SET CLAIM_ID=?, HOLE_NUMBER=?, SEQUENCE_NUMBER=?, GPS_LAT=?, GPS_LON=?, MAP_LAT=?, MAP_LON=? WHERE HOLE_VERTEX_ID=?");
			statement.setString(1, getClaimId());
			statement.setInt(2, getHoleNumber());
			statement.setInt(3, getSequenceNumber());
			statement.setBigDecimal(4,
					new BigDecimal(getGPSPosition().latitude));
			statement.setBigDecimal(5, new BigDecimal(
					getGPSPosition().longitude));
			statement.setBigDecimal(6,
					new BigDecimal(getMapPosition().latitude));
			statement.setBigDecimal(7, new BigDecimal(
					getMapPosition().longitude));
			statement.setString(8, getHoleVertexId());
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

	public static HoleVertex getVertex(String holeVertexId) {
		HoleVertex vertex = null;
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT CLAIM_ID, HOLE_NUMBER, SEQUENCE_NUMBER, GPS_LAT, GPS_LON, MAP_LAT, MAP_LON FROM HOLE_VERTEX VERT WHERE VERT.HOLE_VERTEX_ID=?");
			statement.setString(1, holeVertexId);
			rs = statement.executeQuery();
			while (rs.next()) {
				vertex = new HoleVertex();
				vertex.setHoleVertexId(holeVertexId);
				vertex.setClaimId(rs.getString(1));
				vertex.setHoleNumber(rs.getInt(2));
				vertex.setSequenceNumber(rs.getInt(3));
				vertex.setGPSPosition(new LatLng(rs.getBigDecimal(4)
						.doubleValue(), rs.getBigDecimal(5).doubleValue()));
				vertex.setMapPosition(new LatLng(rs.getBigDecimal(6)
						.doubleValue(), rs.getBigDecimal(7).doubleValue()));
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
		return vertex;
	}

	public static long getArea(List<List<HoleVertex>> holesVertices){

		if (holesVertices == null || holesVertices.size() < 1) {
			return 0;
		}
		double area = 0;

		for(List<HoleVertex> holeVertices:holesVertices){

			if (holeVertices == null || holeVertices.size() <= 1) {
				continue;
			}

			List<LatLng> coordList = new ArrayList<LatLng>();

			int i = 0;

			for (HoleVertex vertex : holeVertices) {
				coordList.add(vertex.getMapPosition());
			}

			if (holeVertices.size() == 2) {
				// the source is a line segment so we replicate the second vertex to
				// create a three vertices polygon
				coordList.add(holeVertices.get(1).getMapPosition());
			}

			// then we close the polygon

			coordList.add(holeVertices.get(0).getMapPosition());

			area += SphericalUtil.computeArea(coordList);
		}

		return (long) Math.round(area);
	}

	public static void storeHolesFromWKT(String claimId, String mapWKT, String gpsWKT) {
		deleteVertices(claimId);

		List<List<HoleVertex>> holes = holesFromWKT(mapWKT, gpsWKT);
		int j = 0;
		for (List<HoleVertex> hole : holes) {
			int i = 0;
			for (HoleVertex vertex : hole) {
				vertex.setClaimId(claimId);
				vertex.setHoleNumber(j);
				vertex.setSequenceNumber(i++);
				vertex.create();
			}
			j++;
		}
	}

	public static LinearRing[] mapHoles(List<List<HoleVertex>> holesVertices) {

		GeometryFactory gf = new GeometryFactory();
		if (holesVertices == null){
			LinearRing[] holes = new LinearRing[1];
			holes[0]=gf.createLinearRing((Coordinate[]) null);
			holes[0].setSRID(Constants.SRID);
			return holes;

		}
		Coordinate[] coordinates;
		List<LinearRing> holesList = new ArrayList<LinearRing>();

		for(List<HoleVertex> holeVertices: holesVertices){
			int j = 0;

			// At least three vertices are needed to build a proper linear ring

			if(holeVertices != null && holeVertices.size() > 2){
				coordinates = new Coordinate[holeVertices.size() + 1];
				for (HoleVertex vertex : holeVertices) {
					coordinates[j] = new Coordinate(vertex.getMapPosition().longitude,
							vertex.getMapPosition().latitude);
					j++;
				}
				// Add one more vertex matching the first one to close the ring
				coordinates[j] = new Coordinate(
						holeVertices.get(0).getMapPosition().longitude, holeVertices.get(0)
						.getMapPosition().latitude);

				LinearRing hole =gf.createLinearRing(coordinates);
				hole.setSRID(Constants.SRID);
                holesList.add(hole);
			}
		}
        LinearRing[] holes = new LinearRing[holesList.size()];
        return (LinearRing[])holesList.toArray(holes);
	}

	public static LinearRing[]  gpsHoles(List<List<HoleVertex>> holesVertices) {
		boolean noGPSData = true;
		GeometryFactory gf = new GeometryFactory();

		if (holesVertices == null){
			LinearRing[] holes = new LinearRing[1];
			holes[0]=gf.createLinearRing((Coordinate[]) null);
			holes[0].setSRID(Constants.SRID);
			return holes;

		}

		for (List<HoleVertex> holeVertices : holesVertices) {
			if(holeVertices != null){
				for (HoleVertex vertex : holeVertices) {
					if(INVALID_POSITION.equals(vertex.getGPSPosition())){
					}else{
						noGPSData = false;
						break;
					}
				}
			}
		}

		if (noGPSData) {
			LinearRing[] holes = new LinearRing[1];
			holes[0]=gf.createLinearRing((Coordinate[]) null);
			holes[0].setSRID(Constants.SRID);
			return holes;
		}

		Coordinate[] coordinates;
        List<LinearRing> holesList = new ArrayList<LinearRing>();

		for(List<HoleVertex> holeVertices: holesVertices){

			// At least three vertices are needed to build a proper linear ring

			if(holeVertices != null && holeVertices.size() > 2){

				coordinates = new Coordinate[holeVertices.size() + 1];
				int j = 0;

				for (HoleVertex vertex : holeVertices) {
					// Use map coordinates for all points without GPS data
					if(INVALID_POSITION.equals(vertex.getGPSPosition())){
						coordinates[j] = new Coordinate(vertex.getMapPosition().longitude, vertex.getMapPosition().latitude);
					}else{
						coordinates[j] = new Coordinate(vertex.getGPSPosition().longitude, vertex.getGPSPosition().latitude);
					}
					j++;
				}
				// Add one more vertex matching the first one to close the ring
				if(INVALID_POSITION.equals(holeVertices.get(0).getGPSPosition())){
					coordinates[j] = new Coordinate(holeVertices.get(0).getMapPosition().longitude, holeVertices.get(0).getMapPosition().latitude);
				}else{
					coordinates[j] = new Coordinate(holeVertices.get(0).getGPSPosition().longitude, holeVertices.get(0).getGPSPosition().latitude);
				}

				LinearRing hole=gf.createLinearRing(coordinates);
				hole.setSRID(Constants.SRID);
                holesList.add(hole);
			}
		}
        LinearRing[] holes = new LinearRing[holesList.size()];
        return (LinearRing[])holesList.toArray(holes);
	}

	public static List<List<HoleVertex>> holesFromWKT(String mapWKT, String gpsWKT) {
		List<List<HoleVertex>> holes = new ArrayList<List<HoleVertex>>();
		GeometryFactory geometryFactory = new GeometryFactory();

		WKTReader reader = new WKTReader(geometryFactory);
		Polygon mapPolygon = null;
		Polygon gpsPolygon = null;

		try {
			mapPolygon = (Polygon) reader.read(mapWKT);
			mapPolygon.setSRID(Constants.SRID);
			if (gpsWKT != null) {
				gpsPolygon = (Polygon) reader.read(gpsWKT);
				gpsPolygon.setSRID(Constants.SRID);
			}
		} catch (ParseException e) {
			return null;
		}

		// Check that all linear rings in the polygon have the same number of vertices

		if (gpsPolygon != null) {
			if(mapPolygon.getNumInteriorRing() != gpsPolygon.getNumInteriorRing()){
				Log.e(HoleVertex.class.getName(), mapWKT + " and " + gpsWKT
						+ " have a different number of holes");
				return null;
			}else{
				for(int i = 0 ; i < mapPolygon.getNumInteriorRing();i++){
					if(mapPolygon.getInteriorRingN(i).getNumPoints() != gpsPolygon.getInteriorRingN(i).getNumPoints()){
						Log.e(HoleVertex.class.getName(), mapWKT + " and " + gpsWKT
								+ " have a different number of vertices in a hole");
						return null;
					}
				}
			}
		}

		for (int j = 0; j < mapPolygon.getNumInteriorRing(); j++) {

			List<HoleVertex> hole = new ArrayList<HoleVertex>();

			for (int i = 0; i < mapPolygon.getInteriorRingN(j).getNumPoints() - 1; i++) {
				Coordinate mapCoordinate = mapPolygon.getInteriorRingN(j).getCoordinates()[i];
				HoleVertex vertex = new HoleVertex(new LatLng(
						mapCoordinate.getOrdinate(Coordinate.Y),
						mapCoordinate.getOrdinate(Coordinate.X)));
				if (gpsPolygon != null) {
					Coordinate gpsCoordinate = gpsPolygon.getInteriorRingN(j).getCoordinates()[i];
					vertex.setGPSPosition(new LatLng(gpsCoordinate
							.getOrdinate(Coordinate.Y), gpsCoordinate
							.getOrdinate(Coordinate.X)));
				}
				hole.add(vertex);
			}
			holes.add(hole);
		}

		return holes;
	}

	public static List<List<HoleVertex>> getHoles(String claimId) {
		List<List<HoleVertex>> holes = new ArrayList<List<HoleVertex>>();
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT HOLE_VERTEX_ID, HOLE_NUMBER, SEQUENCE_NUMBER, GPS_LAT, GPS_LON, MAP_LAT, MAP_LON FROM HOLE_VERTEX VERT WHERE VERT.CLAIM_ID=? ORDER BY HOLE_NUMBER, SEQUENCE_NUMBER");
			statement.setString(1, claimId);
			rs = statement.executeQuery();
			List<HoleVertex> hole = null;
			int currentHole = -1; // first hole, if any, is 0
			while (rs.next()) {
				HoleVertex vertex = new HoleVertex();
				vertex.setHoleVertexId(rs.getString(1));
				vertex.setClaimId(claimId);
				int holeNumber = rs.getInt(2);
				vertex.setHoleNumber(holeNumber);
				vertex.setSequenceNumber(rs.getInt(3));
				vertex.setGPSPosition(new LatLng(rs.getBigDecimal(4)
						.doubleValue(), rs.getBigDecimal(5).doubleValue()));
				vertex.setMapPosition(new LatLng(rs.getBigDecimal(6)
						.doubleValue(), rs.getBigDecimal(7).doubleValue()));
				if(holeNumber != currentHole){
					hole = new ArrayList<HoleVertex>();
					holes.add(hole);
					currentHole = holeNumber;
				}
				hole.add(vertex);
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
		return holes;
	}

	public String getHoleVertexId() {
		return holeVertexId;
	}

	public void setHoleVertexId(String holeVertexId) {
		this.holeVertexId = holeVertexId;
	}

	public String getClaimId() {
		return claimId;
	}

	public void setClaimId(String claimId) {
		this.claimId = claimId;
	}

	String holeVertexId;
	String claimId;

	public int getHoleNumber() {
		return holeNumber;
	}

	public void setHoleNumber(int holeNumber) {
		this.holeNumber = holeNumber;
	}

	int holeNumber;
	int sequenceNumber;
	LatLng GPSPosition;
	LatLng mapPosition;

}
