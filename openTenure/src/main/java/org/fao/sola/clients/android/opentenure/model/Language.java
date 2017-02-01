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

public class Language {

	String code;
	String displayValue;
	int itemOrder;
	boolean active;
	boolean isDefault;
	boolean ltr;

	static Database db = OpenTenureApplication.getInstance().getDatabase();

	@Override
	public String toString() {
		return "DocumentType [code=" + code + ", displayValue=" + displayValue
				+ ", itemOrder=" + itemOrder + ", active=" + active
				+ ", isDefault=" + isDefault + ", ltr=" + ltr + "]";

	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDisplayValue() {
		return displayValue;
	}

	public void setDisplayValue(String displayValue) {
		this.displayValue = displayValue;
	}

	public int getItemOrder() {
		return itemOrder;
	}

	public void setItemOrder(int itemOrder) {
		this.itemOrder = itemOrder;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isIsDefault() {
		return isDefault;
	}

	public void setIsDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	public boolean isLtr() {
		return ltr;
	}

	public void setLtr(boolean ltr) {
		this.ltr = ltr;
	}

	public static Language getLanguage(String code) {
		ResultSet result = null;
		Connection localConnection = null;
		PreparedStatement statement = null;
		Language language = null;
		try {
			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT DISPLAY_VALUE, ACTIVE, IS_DEFAULT, LTR, ITEM_ORDER FROM LANGUAGE WHERE CODE=?");
			statement.setString(1, code);

			result = statement.executeQuery();

			if (result.next()) {
				language = new Language();
				language.setCode(code);
				language.setDisplayValue(result.getString(1));
				language.setActive(result.getBoolean(2));
				language.setIsDefault(result.getBoolean(3));
				language.setLtr(result.getBoolean(4));
				language.setItemOrder(result.getInt(5));
				return language;
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

	public static Language getDefaultLanguage() {
		// Returns the default language or the first available if no default is set
		// or null if no language has been defined
		ResultSet result = null;
		Connection localConnection = null;
		PreparedStatement statement = null;
		Language language = null;
		try {
			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT CODE, DISPLAY_VALUE, ACTIVE, LTR, ITEM_ORDER FROM LANGUAGE WHERE IS_DEFAULT=TRUE");
			result = statement.executeQuery();

			if (result.next()) {
				language = new Language();
				language.setCode(result.getString(1));
				language.setDisplayValue(result.getString(2));
				language.setActive(result.getBoolean(3));
				language.setIsDefault(true);
				language.setLtr(result.getBoolean(4));
				language.setItemOrder(result.getInt(5));
				return language;
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

	public static int addLanguage(Language lang) {

		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("INSERT INTO LANGUAGE(CODE, DISPLAY_VALUE, ACTIVE, IS_DEFAULT, LTR, ITEM_ORDER) VALUES (?,?,?,?,?,?)");

			statement.setString(1, lang.getCode());
			statement.setString(2, lang.getDisplayValue());
			statement.setBoolean(3, lang.isActive());
			statement.setBoolean(4, lang.isIsDefault());
			statement.setBoolean(5, lang.isLtr());
			statement.setInt(6, lang.getItemOrder());

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

	public int add() {

		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("INSERT INTO LANGUAGE(CODE, DISPLAY_VALUE, ACTIVE, IS_DEFAULT, LTR, ITEM_ORDER) VALUES (?,?,?,?,?,?)");

			statement.setString(1, getCode());
			statement.setString(2, getDisplayValue());
			statement.setBoolean(3, isActive());
			statement.setBoolean(4, isIsDefault());
			statement.setBoolean(5, isLtr());
			statement.setInt(6, getItemOrder());

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

	public int updateLanguage() {

		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE LANGUAGE SET DISPLAY_VALUE=?, ACTIVE=?, IS_DEFAULT=?, LTR=?, ITEM_ORDER=? WHERE CODE = ?");

			statement.setString(1, getDisplayValue());
			statement.setBoolean(2, isActive());
			statement.setBoolean(3, isIsDefault());
			statement.setBoolean(4, isLtr());
			statement.setInt(5, getItemOrder());
			statement.setString(6, getCode());
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

	public static List<Language> getLanguages() {

		List<Language> languages = new ArrayList<Language>();
		ResultSet rs = null;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("SELECT CODE, DISPLAY_VALUE, ACTIVE, IS_DEFAULT, LTR, ITEM_ORDER FROM LANGUAGE LANG ");
			rs = statement.executeQuery();

			while (rs.next()) {
				Language language = new Language();
				language.setCode(rs.getString(1));
				language.setDisplayValue(rs.getString(2));
				language.setActive(rs.getBoolean(3));
				language.setIsDefault(rs.getBoolean(4));
				language.setLtr(rs.getBoolean(5));
				language.setItemOrder(rs.getInt(6));

				languages.add(language);

			}
			return languages;

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
		return languages;

	}
}
