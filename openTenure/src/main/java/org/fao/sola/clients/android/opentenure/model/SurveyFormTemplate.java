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

import java.io.StringReader;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.form.FormTemplate;

public class SurveyFormTemplate {
	
	private static final String default_template_key = "dynamic_survey_form_template";

	public String getSurveyFormTemplateId() {
		return surveyFormTemplateId;
	}

	public void setSurveyFormTemplateId(String surveyFormTemplateId) {
		this.surveyFormTemplateId = surveyFormTemplateId;
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

	String surveyFormTemplateId;
	String name;
	String value;

	Database db = OpenTenureApplication.getInstance().getDatabase();

	public SurveyFormTemplate() {
		this.surveyFormTemplateId = UUID.randomUUID().toString();
	}

	private static int createSurveyFormTemplate(SurveyFormTemplate sft) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("INSERT INTO SURVEY_FORM_TEMPLATE(SURVEY_FORM_TEMPLATE_ID, NAME, VALUE) VALUES (?,?,?)");
			statement.setString(1, sft.getSurveyFormTemplateId());
			statement.setString(2, sft.getName());
			statement.setCharacterStream(3, new StringReader(sft.getValue()));
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

	private int create() {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("INSERT INTO SURVEY_FORM_TEMPLATE(SURVEY_FORM_TEMPLATE_ID, NAME, VALUE) VALUES (?,?,?)");
			statement.setString(1, getSurveyFormTemplateId());
			statement.setString(2, getName());
			statement.setCharacterStream(3, new StringReader(getValue()));
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

	private static int deleteSurveyFormTemplate(SurveyFormTemplate sft) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("DELETE SURVEY_FORM_TEMPLATE WHERE SURVEY_FORM_TEMPLATE_ID=?");
			statement.setString(1, sft.getSurveyFormTemplateId());
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

	private int delete() {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("DELETE SURVEY_FORM_TEMPLATE WHERE SURVEY_FORM_TEMPLATE_ID=?");
			statement.setString(1, getSurveyFormTemplateId());
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

	private static int updateSurveyFormTemplate(SurveyFormTemplate sft) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE SURVEY_FORM_TEMPLATE SET NAME=?, VALUE=? WHERE SURVEY_FORM_TEMPLATE_ID=?");
			statement.setString(1, sft.getName());
			statement.setCharacterStream(2, new StringReader(sft.getValue()));
			statement.setString(3, sft.getSurveyFormTemplateId());
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

	private int update() {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = db.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE SURVEY_FORM_TEMPLATE SET NAME=?, VALUE=? WHERE SURVEY_FORM_TEMPLATE_ID=?");
			statement.setString(1, getName());
			statement.setCharacterStream(2, new StringReader(getValue()));
			statement.setString(3, getSurveyFormTemplateId());
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

	private static String getSurveyFormTemplateValue(String name) {
		String val = null;
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT VALUE FROM SURVEY_FORM_TEMPLATE WHERE NAME=?");
			statement.setString(1, name);
			rs = statement.executeQuery();
			while (rs.next()) {
				Clob clob = rs.getClob(1);
				if(clob != null){
					val = clob.getSubString(1L, (int)clob.length());
				}
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
		return val;
	}
	
	public static FormTemplate getDefaultSurveyFormTemplate() {

		Configuration cfg = Configuration.getConfigurationByName(default_template_key);

		if(cfg != null){
			String templateName = cfg.getValue();
			SurveyFormTemplate surveyFormTemplate = getSurveyFormTemplateByName(templateName);
			if(surveyFormTemplate != null){
				return surveyFormTemplate.getFormTemplate();
			}else{
				return new FormTemplate();
			}
		}else{
			return new FormTemplate();
		}
	}


	private static SurveyFormTemplate getSurveyFormTemplateByName(String name) {
		SurveyFormTemplate cfg = null;
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT SURVEY_FORM_TEMPLATE_ID, VALUE FROM SURVEY_FORM_TEMPLATE WHERE NAME=?");
			statement.setString(1, name);
			rs = statement.executeQuery();
			while (rs.next()) {
				cfg = new SurveyFormTemplate();
				cfg.setSurveyFormTemplateId(rs.getString(1));
				cfg.setName(name);
				Clob clob = rs.getClob(2);
				if(clob != null){
					cfg.setValue(clob.getSubString(1L, (int)clob.length()));
				}
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
		return cfg;
	}

	private static Map<String, String> getSurveyFormTemplateValues() {
		Map<String, String> cfg = new HashMap<String, String>();
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT NAME, VALUE FROM SURVEY_FORM_TEMPLATE");
			rs = statement.executeQuery();
			while (rs.next()) {
				String key = rs.getString(1);
				Clob clob = rs.getClob(2);
				if(clob != null){
					cfg.put(key, clob.getSubString(1L, (int)clob.length()));
				}
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
		return cfg;
	}

	private static SurveyFormTemplate getSurveyFormTemplate(String surveyFormTemplateId) {
		SurveyFormTemplate cfg = null;
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT NAME, VALUE FROM SURVEY_FORM_TEMPLATE WHERE SURVEY_FORM_TEMPLATE_ID=?");
			statement.setString(1, surveyFormTemplateId);
			rs = statement.executeQuery();
			while (rs.next()) {
				cfg = new SurveyFormTemplate();
				cfg.setSurveyFormTemplateId(surveyFormTemplateId);
				cfg.setName(rs.getString(1));
				Clob clob = rs.getClob(2);
				if(clob != null){
					cfg.setValue(clob.getSubString(1L, (int)clob.length()));
				}
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
		return cfg;
	}
	
	private FormTemplate getFormTemplate(){
		return FormTemplate.fromJson(value);
	}

	public static int saveFormTemplate(FormTemplate surveyFormTemplate){
		String name = null;
		SurveyFormTemplate sft =  null;
		if(surveyFormTemplate != null){
			name = surveyFormTemplate.getName();
			sft =  SurveyFormTemplate.getSurveyFormTemplateByName(name);
			if(sft != null){
				sft.setValue(surveyFormTemplate.toJson());
				return sft.update();
			}else{
				sft = new SurveyFormTemplate();
				sft.setName(surveyFormTemplate.getName());
				sft.setValue(surveyFormTemplate.toJson());
				return sft.create();
			}
		}
		return 0;
	}

	public static int saveDefaultFormTemplate(FormTemplate surveyFormTemplate){

		int result = saveFormTemplate(surveyFormTemplate); 
		if(result != 0){
			Configuration cfg = Configuration.getConfigurationByName(default_template_key);
			if(cfg != null){
				cfg.setValue(surveyFormTemplate.getName());
				return cfg.update();
			}else{
				cfg = new Configuration();
				cfg.setName(default_template_key);
				cfg.setValue(surveyFormTemplate.getName());
				return cfg.create();
			}
		}
		return result;
	}

	public static FormTemplate getFormTemplate(String surveyFormTemplateId){
		SurveyFormTemplate sft = SurveyFormTemplate.getSurveyFormTemplate(surveyFormTemplateId);
		if(sft != null){
			return FormTemplate.fromJson(sft.getValue());
		}
		return null;
	}

	public static FormTemplate getFormTemplateByName(String name){
		SurveyFormTemplate sft = SurveyFormTemplate.getSurveyFormTemplateByName(name);
		if(sft != null && sft.getValue() != null){
			return FormTemplate.fromJson(sft.getValue());
		}else{
			return new FormTemplate();
		}
	}

}
