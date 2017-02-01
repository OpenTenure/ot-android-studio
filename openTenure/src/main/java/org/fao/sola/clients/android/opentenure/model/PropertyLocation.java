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
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import org.fao.sola.clients.android.opentenure.maps.WKTWriter;

public class PropertyLocation {

	public static final LatLng INVALID_POSITION = new LatLng(400.0, 400.0);

	Database db = OpenTenureApplication.getInstance().getDatabase();

	public PropertyLocation() {
		this.propertyLocationId = UUID.randomUUID().toString();
	}

	public PropertyLocation(LatLng mapPosition) {
		this.propertyLocationId = UUID.randomUUID().toString();
		setMapPosition(mapPosition);
		setGPSPosition(INVALID_POSITION);
	}

	public PropertyLocation(LatLng mapPosition, LatLng GPSPosition) {
		this.propertyLocationId = UUID.randomUUID().toString();
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
		return "PropertyLocation ["
				+ "propertyLocationId=" + propertyLocationId
				+ ", claimId=" + claimId
				+ ", description=" + description
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

	public static int createPropertyLocation(PropertyLocation propertyLocation) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("INSERT INTO PROPERTY_LOCATION(PROPERTY_LOCATION_ID, CLAIM_ID, DESCRIPTION, GPS_LAT, GPS_LON, MAP_LAT, MAP_LON) VALUES(?,?,?,?,?,?,?)");
			statement.setString(1, propertyLocation.getPropertyLocationId());
			statement.setString(2, propertyLocation.getClaimId());
			statement.setString(3, propertyLocation.getDescription());
			statement.setBigDecimal(4, new BigDecimal(
					propertyLocation.getGPSPosition().latitude));
			statement.setBigDecimal(5, new BigDecimal(
					propertyLocation.getGPSPosition().longitude));
			statement.setBigDecimal(6, new BigDecimal(
					propertyLocation.getMapPosition().latitude));
			statement.setBigDecimal(7, new BigDecimal(
					propertyLocation.getMapPosition().longitude));
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
					.prepareStatement("INSERT INTO PROPERTY_LOCATION(PROPERTY_LOCATION_ID, CLAIM_ID, DESCRIPTION, GPS_LAT, GPS_LON, MAP_LAT, MAP_LON) VALUES(?,?,?,?,?,?,?)");
			statement.setString(1, getPropertyLocationId());
			statement.setString(2, getClaimId());
			statement.setString(3, getDescription());
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

	public static int deletePropertyLocation(PropertyLocation propertyLocation) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {
			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("DELETE FROM PROPERTY_LOCATION WHERE PROPERTY_LOCATION_ID=?");
			statement.setString(1, propertyLocation.getPropertyLocationId());
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

	public static int deletePropertyLocations(String claimId) {
		int result = 0;
		Connection localConnection = null;

		try {
			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			result = deletePropertyLocations(claimId, localConnection);
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

	public static int deletePropertyLocations(String claimId, Connection connection) {
		int result = 0;
		PreparedStatement statement = null;

		try {
			statement = connection
					.prepareStatement("DELETE FROM PROPERTY_LOCATION WHERE CLAIM_ID=?");
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
					.prepareStatement("DELETE FROM PROPERTY_LOCATION WHERE PROPERTY_LOCATION_ID=?");
			statement.setString(1, getPropertyLocationId());
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

	public static int updatePropertyLocation(PropertyLocation propertyLocation) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE PROPERTY_LOCATION SET CLAIM_ID=?, DESCRIPTION=?, GPS_LAT=?, GPS_LON=?, MAP_LAT=?, MAP_LON=? WHERE PROPERTY_LOCATION_ID=?");
			statement.setString(1, propertyLocation.getClaimId());
			statement.setString(2, propertyLocation.getDescription());
			statement.setBigDecimal(3, new BigDecimal(
					propertyLocation.getGPSPosition().latitude));
			statement.setBigDecimal(4, new BigDecimal(
					propertyLocation.getGPSPosition().longitude));
			statement.setBigDecimal(5, new BigDecimal(
					propertyLocation.getMapPosition().latitude));
			statement.setBigDecimal(6, new BigDecimal(
					propertyLocation.getMapPosition().longitude));
			statement.setString(7, propertyLocation.getPropertyLocationId());
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
					.prepareStatement("UPDATE PROPERTY_LOCATION SET CLAIM_ID=?, DESCRIPTION=?, GPS_LAT=?, GPS_LON=?, MAP_LAT=?, MAP_LON=? WHERE PROPERTY_LOCATION_ID=?");
			statement.setString(1, getClaimId());
			statement.setString(2, getDescription());
			statement.setBigDecimal(3,
					new BigDecimal(getGPSPosition().latitude));
			statement.setBigDecimal(4, new BigDecimal(
					getGPSPosition().longitude));
			statement.setBigDecimal(5,
					new BigDecimal(getMapPosition().latitude));
			statement.setBigDecimal(6, new BigDecimal(
					getMapPosition().longitude));
			statement.setString(7, getPropertyLocationId());
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

	public static PropertyLocation getPropertyLocation(String propertyLocationId) {
		PropertyLocation propertyLocation = null;
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT CLAIM_ID, DESCRIPTION, GPS_LAT, GPS_LON, MAP_LAT, MAP_LON FROM PROPERTY_LOCATION PROP WHERE PROP.PROPERTY_LOCATION_ID=?");
			statement.setString(1, propertyLocationId);
			rs = statement.executeQuery();
			while (rs.next()) {
				propertyLocation = new PropertyLocation();
				propertyLocation.setPropertyLocationId(propertyLocationId);
				propertyLocation.setClaimId(rs.getString(1));
				propertyLocation.setDescription(rs.getString(2));
				propertyLocation.setGPSPosition(new LatLng(rs.getBigDecimal(3)
						.doubleValue(), rs.getBigDecimal(4).doubleValue()));
				propertyLocation.setMapPosition(new LatLng(rs.getBigDecimal(5)
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
		return propertyLocation;
	}

	public static String mapWKTFromPropertyLocation(PropertyLocation propertyLocation) {
		if (propertyLocation == null) {
			return null;
		}
		GeometryFactory gf = new GeometryFactory();
		Coordinate coordinate = new Coordinate();

		coordinate = new Coordinate(propertyLocation.getMapPosition().longitude,
				propertyLocation.getMapPosition().latitude);

		Point point = gf.createPoint(coordinate);
		point.setSRID(Constants.SRID);

		StringWriter writer = new StringWriter();
		WKTWriter wktWriter = new WKTWriter(2);

		try {
			wktWriter.write(point, writer);
		} catch (IOException e) {
		}

		return writer.toString();
	}

	public static String gpsWKTFromPropertyLocation(PropertyLocation propertyLocation) {
		if (propertyLocation == null) {
			return null;
		}
		GeometryFactory gf = new GeometryFactory();
		Coordinate coordinate = new Coordinate();

		coordinate = new Coordinate(propertyLocation.getGPSPosition().longitude,
				propertyLocation.getGPSPosition().latitude);

		Point point = gf.createPoint(coordinate);
		point.setSRID(Constants.SRID);

		StringWriter writer = new StringWriter();
		WKTWriter wktWriter = new WKTWriter(2);

		try {
			wktWriter.write(point, writer);
		} catch (IOException e) {
		}

		return writer.toString();
	}

	public static PropertyLocation propertyLocationFromWKT(String mapWKT, String gpsWKT) {
		PropertyLocation propertyLocation;
		GeometryFactory geometryFactory = new GeometryFactory();

		WKTReader reader = new WKTReader(geometryFactory);
		Point mapPoint = null;
		Point gpsPoint = null;

		try {
			
			Geometry generic = reader.read(mapWKT);
			
			mapPoint = (Point) generic;
			mapPoint.setSRID(Constants.SRID);
			if (gpsWKT != null) {
				gpsPoint = (Point) reader.read(gpsWKT);
				gpsPoint.setSRID(Constants.SRID);
			}
		}catch(ClassCastException cce){
			
			try {
				Log.d("OpenTEnureApplication", "ClassCastException - is not a POINT : " + (reader.read(mapWKT)).getClass().getName());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
			return null;
		}
		
		catch (ParseException e) {
			return null;
		}

		Coordinate mapCoordinate = mapPoint.getCoordinate();
		propertyLocation = new PropertyLocation(new LatLng(mapCoordinate.getOrdinate(Coordinate.Y),
				mapCoordinate.getOrdinate(Coordinate.X)));
		if (gpsPoint != null) {
			Coordinate gpsCoordinate = gpsPoint.getCoordinate();
			propertyLocation.setGPSPosition(new LatLng(gpsCoordinate
					.getOrdinate(Coordinate.Y), gpsCoordinate
					.getOrdinate(Coordinate.X)));
		}

		return propertyLocation;
	}

	public static List<PropertyLocation> getPropertyLocations(String claimId) {
		List<PropertyLocation> propertyLocations = new ArrayList<PropertyLocation>();
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT PROPERTY_LOCATION_ID, DESCRIPTION, GPS_LAT, GPS_LON, MAP_LAT, MAP_LON FROM PROPERTY_LOCATION VERT WHERE VERT.CLAIM_ID=?");
			statement.setString(1, claimId);
			rs = statement.executeQuery();
			while (rs.next()) {
				PropertyLocation propertyLocation = new PropertyLocation();
				propertyLocation.setPropertyLocationId(rs.getString(1));
				propertyLocation.setClaimId(claimId);
				propertyLocation.setDescription(rs.getString(2));
				propertyLocation.setGPSPosition(new LatLng(rs.getBigDecimal(3)
						.doubleValue(), rs.getBigDecimal(4).doubleValue()));
				propertyLocation.setMapPosition(new LatLng(rs.getBigDecimal(5)
						.doubleValue(), rs.getBigDecimal(6).doubleValue()));
				propertyLocations.add(propertyLocation);
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
		return propertyLocations;
	}

	public static List<PropertyLocation> getPropertyLocations(String claimId, Connection externalConnection) {
		List<PropertyLocation> propertyLocations = new ArrayList<PropertyLocation>();
		PreparedStatement statement = null;

		try {

			statement = externalConnection
					.prepareStatement("SELECT PROPERTY_LOCATION_ID, DESCRIPTION, GPS_LAT, GPS_LON, MAP_LAT, MAP_LON FROM PROPERTY_LOCATION VERT WHERE VERT.CLAIM_ID=?");
			statement.setString(1, claimId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				PropertyLocation propertyLocation = new PropertyLocation();
				propertyLocation.setPropertyLocationId(rs.getString(1));
				propertyLocation.setClaimId(claimId);
				propertyLocation.setDescription(rs.getString(2));
				propertyLocation.setGPSPosition(new LatLng(rs.getBigDecimal(3)
						.doubleValue(), rs.getBigDecimal(4).doubleValue()));
				propertyLocation.setMapPosition(new LatLng(rs.getBigDecimal(5)
						.doubleValue(), rs.getBigDecimal(6).doubleValue()));
				propertyLocations.add(propertyLocation);
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
		return propertyLocations;
	}

	public String getPropertyLocationId() {
		return propertyLocationId;
	}

	public void setPropertyLocationId(String propertyLocationId) {
		this.propertyLocationId = propertyLocationId;
	}

	public String getClaimId() {
		return claimId;
	}

	public void setClaimId(String claimId) {
		this.claimId = claimId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	String propertyLocationId;
	String claimId;
	String description;
	LatLng GPSPosition;
	LatLng mapPosition;

}
