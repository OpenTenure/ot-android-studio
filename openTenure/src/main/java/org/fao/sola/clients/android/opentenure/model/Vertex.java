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

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.maps.Constants;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.fao.sola.clients.android.opentenure.maps.WKTWriter;

public class Vertex {

	public static final LatLng INVALID_POSITION = new LatLng(400.0, 400.0);

	public int getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	Database db = OpenTenureApplication.getInstance().getDatabase();

	public Vertex() {
		this.vertexId = UUID.randomUUID().toString();
	}

	public Vertex(Vertex vertex) {
		this.vertexId = UUID.randomUUID().toString();
		this.GPSPosition = vertex.getGPSPosition();
		this.mapPosition = vertex.getMapPosition();
		this.claimId = vertex.getClaimId();
		this.sequenceNumber = vertex.getSequenceNumber();
	}

	public Vertex(LatLng mapPosition) {
		this.vertexId = UUID.randomUUID().toString();
		setMapPosition(mapPosition);
		setGPSPosition(INVALID_POSITION);
	}

	public Vertex(LatLng mapPosition, LatLng GPSPosition) {
		this.vertexId = UUID.randomUUID().toString();
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
		return "Vertex [" + "vertexId=" + vertexId + ", claimId=" + claimId
				+ ", sequenceNumber="
				+ sequenceNumber + ", GPSLat=" + GPSPosition.latitude
				+ ", GPSLon=" + GPSPosition.longitude + ", MapLat="
				+ mapPosition.latitude + ", MapLon=" + mapPosition.longitude
				+ "]";
	}

	public void setGPSPosition(LatLng GPSPosition) {
		this.GPSPosition = GPSPosition;
	}

	public void setMapPosition(LatLng mapPosition) {
		this.mapPosition = mapPosition;
	}

	public static int createVertices(List<Vertex> vertices) {
		int result = 0;
		for(Vertex vertex:vertices){
			result += createVertex(vertex);
		}
		return result;
	}

	public static int createVertex(Vertex vertex) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("INSERT INTO VERTEX(VERTEX_ID, CLAIM_ID, SEQUENCE_NUMBER, GPS_LAT, GPS_LON, MAP_LAT, MAP_LON) VALUES(?,?,?,?,?,?,?)");
			statement.setString(1, vertex.getVertexId());
			statement.setString(2, vertex.getClaimId());
			statement.setInt(3, vertex.getSequenceNumber());
			statement.setBigDecimal(4, new BigDecimal(
					vertex.getGPSPosition().latitude));
			statement.setBigDecimal(5, new BigDecimal(
					vertex.getGPSPosition().longitude));
			statement.setBigDecimal(6, new BigDecimal(
					vertex.getMapPosition().latitude));
			statement.setBigDecimal(7, new BigDecimal(
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
					.prepareStatement("INSERT INTO VERTEX(VERTEX_ID, CLAIM_ID, SEQUENCE_NUMBER, GPS_LAT, GPS_LON, MAP_LAT, MAP_LON) VALUES(?,?,?,?,?,?,?)");
			statement.setString(1, getVertexId());
			statement.setString(2, getClaimId());
			statement.setInt(3, getSequenceNumber());
			statement.setBigDecimal(4,
					new BigDecimal(getGPSPosition().latitude));
			statement.setBigDecimal(5, new BigDecimal(
					getGPSPosition().longitude));
			statement.setBigDecimal(6,
					new BigDecimal(getMapPosition().latitude));
			statement.setBigDecimal(7, new BigDecimal(
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

	public static int deleteVertex(Vertex vertex) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {
			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("DELETE FROM VERTEX WHERE VERTEX_ID=?");
			statement.setString(1, vertex.getVertexId());
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
					.prepareStatement("DELETE FROM VERTEX WHERE CLAIM_ID=?");
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
					.prepareStatement("DELETE FROM VERTEX WHERE VERTEX_ID=?");
			statement.setString(1, getVertexId());
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

	public static int updateVertex(Vertex vertex) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE VERTEX SET CLAIM_ID=?, SEQUENCE_NUMBER=?, GPS_LAT=?, GPS_LON=?, MAP_LAT=?, MAP_LON=? WHERE VERTEX_ID=?");
			statement.setString(1, vertex.getClaimId());
			statement.setInt(2, vertex.getSequenceNumber());
			statement.setBigDecimal(3, new BigDecimal(
					vertex.getGPSPosition().latitude));
			statement.setBigDecimal(4, new BigDecimal(
					vertex.getGPSPosition().longitude));
			statement.setBigDecimal(5, new BigDecimal(
					vertex.getMapPosition().latitude));
			statement.setBigDecimal(6, new BigDecimal(
					vertex.getMapPosition().longitude));
			statement.setString(7, vertex.getVertexId());
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
					.prepareStatement("UPDATE VERTEX SET CLAIM_ID=?, SEQUENCE_NUMBER=?, GPS_LAT=?, GPS_LON=?, MAP_LAT=?, MAP_LON=? WHERE VERTEX_ID=?");
			statement.setString(1, getClaimId());
			statement.setInt(2, getSequenceNumber());
			statement.setBigDecimal(3,
					new BigDecimal(getGPSPosition().latitude));
			statement.setBigDecimal(4, new BigDecimal(
					getGPSPosition().longitude));
			statement.setBigDecimal(5,
					new BigDecimal(getMapPosition().latitude));
			statement.setBigDecimal(6, new BigDecimal(
					getMapPosition().longitude));
			statement.setString(7, getVertexId());
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

	public static Vertex getVertex(String vertexId) {
		Vertex vertex = null;
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT CLAIM_ID, SEQUENCE_NUMBER, GPS_LAT, GPS_LON, MAP_LAT, MAP_LON FROM VERTEX VERT WHERE VERT.VERTEX_ID=?");
			statement.setString(1, vertexId);
			rs = statement.executeQuery();
			while (rs.next()) {
				vertex = new Vertex();
				vertex.setVertexId(vertexId);
				vertex.setClaimId(rs.getString(1));
				vertex.setSequenceNumber(rs.getInt(2));
				vertex.setGPSPosition(new LatLng(rs.getBigDecimal(3)
						.doubleValue(), rs.getBigDecimal(4).doubleValue()));
				vertex.setMapPosition(new LatLng(rs.getBigDecimal(5)
						.doubleValue(), rs.getBigDecimal(6).doubleValue()));
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

	public static void storeWKT(String claimId, String mapWKT, String gpsWKT) {
		deleteVertices(claimId);

		List<Vertex> vertices = verticesFromWKT(mapWKT, gpsWKT);
		int i = 0;

		for (Vertex vertex : vertices) {
			vertex.setClaimId(claimId);
			vertex.setSequenceNumber(i++);
			vertex.create();
		}
	}

	public static String mapWKTFromVertices(List<Vertex> vertices) {
		if (vertices == null || vertices.size() == 0) {
			return null;
		}
		GeometryFactory gf = new GeometryFactory();
		Coordinate[] coordinates;
		StringWriter writer = new StringWriter();
		WKTWriter wktWriter = new WKTWriter(2);
		int i = 0;
		
		switch(vertices.size()){
			case 1:
				Point point = gf.createPoint(new Coordinate(
						vertices.get(0).getMapPosition().longitude, vertices.get(0)
						.getMapPosition().latitude));
				point.setSRID(Constants.SRID);

				try {
					wktWriter.write(point, writer);
				} catch (IOException e) {
				}

				return writer.toString();
			case 2:
				coordinates = new Coordinate[vertices.size()];

				for (Vertex vertex : vertices) {
					coordinates[i] = new Coordinate(vertex.getMapPosition().longitude,
							vertex.getMapPosition().latitude);
					i++;
				}
				LineString linestring = gf.createLineString(coordinates);
				linestring.setSRID(Constants.SRID);


				try {
					wktWriter.write(linestring, writer);
				} catch (IOException e) {
				}

				return writer.toString();
			default:
				coordinates = new Coordinate[vertices.size() + 1];

				for (Vertex vertex : vertices) {
					coordinates[i] = new Coordinate(vertex.getMapPosition().longitude,
							vertex.getMapPosition().latitude);
					i++;
				}
				coordinates[i] = new Coordinate(
						vertices.get(0).getMapPosition().longitude, vertices.get(0)
								.getMapPosition().latitude);

				Polygon polygon = gf.createPolygon(coordinates);
				polygon.setSRID(Constants.SRID);

				try {
					wktWriter.write(polygon, writer);
				} catch (IOException e) {
				}

				return writer.toString();
		}
	}

	public static String gpsWKTFromVertices(List<Vertex> vertices) {
		if (vertices == null || vertices.size() == 0) {
			return null;
		}
		
		boolean noGPSData = true;
		StringWriter writer = new StringWriter();
		GeometryFactory gf = new GeometryFactory();
		WKTWriter wktWriter = new WKTWriter(2);
		

		for (Vertex vertex : vertices) {
			
			if(INVALID_POSITION.equals(vertex.getGPSPosition())){
			}else{
				noGPSData = false;
			}
		}

		if (noGPSData) {			
			
			
			Point point = gf.createPoint(new Coordinate(
					vertices.get(0).getMapPosition().longitude, vertices.get(0)
					.getMapPosition().latitude));
			point.setSRID(Constants.SRID);


			try {
				wktWriter.write(point, writer);
			} catch (IOException e) {
			}
			return writer.toString();
		}
		
		Coordinate[] coordinates;
		int i = 0;
		
		switch(vertices.size()){
			case 1:
				Point point;
				
				if(INVALID_POSITION.equals(vertices.get(0).getGPSPosition())){
					point = gf.createPoint(new Coordinate(vertices.get(0).getMapPosition().longitude, vertices.get(0).getMapPosition().latitude));
				}else{
					point = gf.createPoint(new Coordinate(vertices.get(0).getGPSPosition().longitude, vertices.get(0).getGPSPosition().latitude));
				}
				point.setSRID(Constants.SRID);

				try {
					wktWriter.write(point, writer);
				} catch (IOException e) {
				}

				return writer.toString();
			case 2:
				coordinates = new Coordinate[vertices.size()];

				for (Vertex vertex : vertices) {

					if(INVALID_POSITION.equals(vertex.getGPSPosition())){
						coordinates[i] = new Coordinate(vertex.getMapPosition().longitude, vertex.getMapPosition().latitude);
					}else{
						coordinates[i] = new Coordinate(vertex.getGPSPosition().longitude, vertex.getGPSPosition().latitude);
					}
					i++;
				}
				LineString linestring = gf.createLineString(coordinates);
				linestring.setSRID(Constants.SRID);

				try {
					wktWriter.write(linestring, writer);
				} catch (IOException e) {
				}

				return writer.toString();
			default:
				coordinates = new Coordinate[vertices.size() + 1];

				for (Vertex vertex : vertices) {
					if(INVALID_POSITION.equals(vertex.getGPSPosition())){
						coordinates[i] = new Coordinate(vertex.getMapPosition().longitude, vertex.getMapPosition().latitude);
					}else{
						coordinates[i] = new Coordinate(vertex.getGPSPosition().longitude, vertex.getGPSPosition().latitude);
					}
					i++;
				}
				if(INVALID_POSITION.equals(vertices.get(0).getGPSPosition())){
					coordinates[i] = new Coordinate(vertices.get(0).getMapPosition().longitude, vertices.get(0).getMapPosition().latitude);
				}else{
					coordinates[i] = new Coordinate(vertices.get(0).getGPSPosition().longitude, vertices.get(0).getGPSPosition().latitude);
				}

				Polygon polygon = gf.createPolygon(coordinates);
				polygon.setSRID(Constants.SRID);

				try {
					wktWriter.write(polygon, writer);
				} catch (IOException e) {
				}

				return writer.toString();
		}
	}

	public static List<Vertex> verticesFromWKT(String mapWKT, String gpsWKT) {
		List<Vertex> vertices = new ArrayList<Vertex>();
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

		if (gpsPolygon != null && mapPolygon.getNumPoints() != gpsPolygon.getNumPoints()) {
			Log.e(Vertex.class.getName(), mapWKT + " and " + gpsWKT
					+ " have a different number of vertices");
			return null;
		}

		for (int i = 0; i < mapPolygon.getNumPoints(); i++) {
			Coordinate mapCoordinate = mapPolygon.getCoordinates()[i];
			Vertex vertex = new Vertex(new LatLng(
					mapCoordinate.getOrdinate(Coordinate.Y),
					mapCoordinate.getOrdinate(Coordinate.X)));
			if (gpsPolygon != null) {
				Coordinate gpsCoordinate = gpsPolygon.getCoordinates()[i];
				vertex.setGPSPosition(new LatLng(gpsCoordinate
						.getOrdinate(Coordinate.Y), gpsCoordinate
						.getOrdinate(Coordinate.X)));
			}
			vertices.add(vertex);
		}

		return vertices;
	}

	public static List<Vertex> getVertices(String claimId) {
		List<Vertex> vertices = new ArrayList<Vertex>();
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT VERTEX_ID, SEQUENCE_NUMBER, GPS_LAT, GPS_LON, MAP_LAT, MAP_LON FROM VERTEX VERT WHERE VERT.CLAIM_ID=? ORDER BY SEQUENCE_NUMBER");
			statement.setString(1, claimId);
			rs = statement.executeQuery();
			while (rs.next()) {
				Vertex vertex = new Vertex();
				vertex.setVertexId(rs.getString(1));
				vertex.setClaimId(claimId);
				vertex.setSequenceNumber(rs.getInt(2));
				vertex.setGPSPosition(new LatLng(rs.getBigDecimal(3)
						.doubleValue(), rs.getBigDecimal(4).doubleValue()));
				vertex.setMapPosition(new LatLng(rs.getBigDecimal(5)
						.doubleValue(), rs.getBigDecimal(6).doubleValue()));
				vertices.add(vertex);
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
		return vertices;
	}

	public static List<Vertex> getVertices(String claimId, Connection externalConnection) {
		List<Vertex> vertices = new ArrayList<Vertex>();
		PreparedStatement statement = null;

		try {

			statement = externalConnection
					.prepareStatement("SELECT VERTEX_ID, SEQUENCE_NUMBER, GPS_LAT, GPS_LON, MAP_LAT, MAP_LON FROM VERTEX VERT WHERE VERT.CLAIM_ID=? ORDER BY SEQUENCE_NUMBER");
			statement.setString(1, claimId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				Vertex vertex = new Vertex();
				vertex.setVertexId(rs.getString(1));
				vertex.setClaimId(claimId);
				vertex.setSequenceNumber(rs.getInt(2));
				vertex.setGPSPosition(new LatLng(rs.getBigDecimal(3)
						.doubleValue(), rs.getBigDecimal(4).doubleValue()));
				vertex.setMapPosition(new LatLng(rs.getBigDecimal(5)
						.doubleValue(), rs.getBigDecimal(6).doubleValue()));
				vertices.add(vertex);
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
		return vertices;
	}

	public String getVertexId() {
		return vertexId;
	}

	public void setVertexId(String vertexId) {
		this.vertexId = vertexId;
	}

	public String getClaimId() {
		return claimId;
	}

	public void setClaimId(String claimId) {
		this.claimId = claimId;
	}

	String vertexId;
	String claimId;
	int sequenceNumber;
	LatLng GPSPosition;
	LatLng mapPosition;

}
