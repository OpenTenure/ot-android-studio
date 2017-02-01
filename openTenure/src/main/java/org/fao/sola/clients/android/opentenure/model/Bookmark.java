/**
 * ******************************************************************************************
 * Copyright (C) 2015 - Food and Agriculture Organization of the United Nations (FAO).
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

public class Bookmark {

	@Override
	public String toString() {
		return "Bookmark [bookmarkId=" + bookmarkId + ", name=" + name
				+ ", lat=" + lat + ", lon=" + lon + "]";
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLon() {
		return lon;
	}

	public void setLon(double lon) {
		this.lon = lon;
	}

	public String getBookmarkId() {
		return bookmarkId;
	}

	public void setBookmarkId(String bookmarkId) {
		this.bookmarkId = bookmarkId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	String bookmarkId;
	String name;
	double lat;
	double lon;

	Database db = OpenTenureApplication.getInstance().getDatabase();

	public Bookmark() {
		this.bookmarkId = UUID.randomUUID().toString();
	}

	public static int createBookmark(Bookmark book) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("INSERT INTO MAP_BOOKMARK(MAP_BOOKMARK_ID, NAME, LAT, LON) VALUES(?,?,?,?)");
			statement.setString(1, book.getBookmarkId());
			statement.setString(2, book.getName());
			statement.setBigDecimal(3, new BigDecimal(book.getLat()));
			statement.setBigDecimal(4, new BigDecimal(book.getLon()));
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
					.prepareStatement("INSERT INTO MAP_BOOKMARK(MAP_BOOKMARK_ID, NAME, LAT, LON) VALUES(?,?,?,?)");
			statement.setString(1, getBookmarkId());
			statement.setString(2, getName());
			statement.setBigDecimal(3, new BigDecimal(getLat()));
			statement.setBigDecimal(4, new BigDecimal(getLon()));
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

	public static int deleteBookmark(Bookmark book) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("DELETE FROM MAP_BOOKMARK WHERE MAP_BOOKMARK_ID=?");
			statement.setString(1, book.getBookmarkId());
			result = statement.executeUpdate();
			statement.close();
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

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("DELETE FROM MAP_BOOKMARK WHERE MAP_BOOKMARK_ID=?");
			statement.setString(1, getBookmarkId());
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

	public static int updateBookmark(Bookmark book) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE MAP_BOOKMARK SET NAME=?, LAT=?, LON=? WHERE MAP_BOOKMARK_ID=?");
			statement.setString(1, book.getName());
			statement.setBigDecimal(2, new BigDecimal(book.getLat()));
			statement.setBigDecimal(3, new BigDecimal(book.getLon()));
			statement.setString(4, book.getBookmarkId());
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
					.prepareStatement("UPDATE MAP_BOOKMARK SET NAME=?, LAT=?, LON=? WHERE MAP_BOOKMARK_ID=?");
			statement.setString(1, getName());
			statement.setBigDecimal(2, new BigDecimal(getLat()));
			statement.setBigDecimal(3, new BigDecimal(getLon()));
			statement.setString(4, getBookmarkId());
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

	public static Bookmark getBookmarkByName(String name) {
		Bookmark book = null;
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT BOOK.MAP_BOOKMARK_ID, BOOK.LAT, BOOK.LON FROM MAP_BOOKMARK BOOK WHERE BOOK.NAME=?");
			statement.setString(1, name);
			rs = statement.executeQuery();
			while (rs.next()) {
				book = new Bookmark();
				book.setBookmarkId(rs.getString(1));
				book.setName(name);
				book.setLat(rs.getBigDecimal(2).doubleValue());
				book.setLon(rs.getBigDecimal(3).doubleValue());
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
		return book;
	}

	public static Bookmark getBookmark(String bookmarkId) {
		Bookmark book = null;
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT BOOK.NAME, BOOK.LAT, BOOK.LON FROM MAP_BOOKMARK BOOK WHERE BOOK.MAP_BOOKMARK_ID=?");
			statement.setString(1, bookmarkId);
			rs = statement.executeQuery();
			while (rs.next()) {
				book = new Bookmark();
				book.setBookmarkId(bookmarkId);
				book.setName(rs.getString(1));
				book.setLat(rs.getBigDecimal(2).doubleValue());
				book.setLon(rs.getBigDecimal(3).doubleValue());
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
		return book;
	}

	public static List<Bookmark> getAllBookmarks() {
		List<Bookmark> bookmarks = new ArrayList<Bookmark>();
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT BOOK.MAP_BOOKMARK_ID, BOOK.NAME, BOOK.LAT, BOOK.LON FROM MAP_BOOKMARK BOOK");
			rs = statement.executeQuery();
			while (rs.next()) {
				Bookmark book = new Bookmark();
				book.setBookmarkId(rs.getString(1));
				book.setName(rs.getString(2));
				book.setLat(rs.getBigDecimal(3).doubleValue());
				book.setLon(rs.getBigDecimal(4).doubleValue());
				bookmarks.add(book);
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
		return bookmarks;
	}
}
