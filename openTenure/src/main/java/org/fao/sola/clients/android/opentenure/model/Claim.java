/**
 * ******************************************************************************************
 * Copyright (C) 2014 - Food and Agriculture Organization of the United Nations (FAO).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,281
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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.filesystem.json.JsonUtilities;
import org.fao.sola.clients.android.opentenure.form.FormPayload;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class Claim {

	public enum Status {
		unmoderated, moderated, challenged, created, uploading, updating, upload_incomplete, update_incomplete, upload_error, update_error, withdrawn, reviewed
	};

	public static final int MAX_SHARES_PER_CLAIM = 100;

	public FormPayload getDynamicForm() {
		return dynamicForm;
	}

	public void setDynamicForm(FormPayload dynamicForm) {
		this.dynamicForm = dynamicForm;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<AdditionalInfo> getAdditionalInfo() {
		// No longer used
		return new ArrayList<AdditionalInfo>();
	}

	public void setAdditionalInfo(List<AdditionalInfo> additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	Database db = OpenTenureApplication.getInstance().getDatabase();

	public Claim() {
		this.claimId = UUID.randomUUID().toString();
		this.status = ClaimStatus._CREATED;
		this.availableShares = MAX_SHARES_PER_CLAIM;
	}

	public int getAvailableShares() {
		// return availableShares;
		int total = 0;
		List<ShareProperty> list = this.getShares();
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			ShareProperty shareProperty = (ShareProperty) iterator.next();
			total = total + shareProperty.getShares();
		}
		
		return MAX_SHARES_PER_CLAIM - total;

	}

	public void setAvailableShares(int availableShares) {
		this.availableShares = availableShares;
	}

	@Override
	public String toString() {
		return "Claim [" + "claimId=" + claimId + ", status=" + status
				+ ", claimNumber=" + claimNumber + ", type=" + type + ", name="
				+ name + ", person=" + person + ", propertyLocations="
				+ Arrays.toString(propertyLocations.toArray()) + ", vertices="
				+ Arrays.toString(vertices.toArray()) + ", additionalInfo="
				+ Arrays.toString(additionalInfo.toArray())
				+ ", challengedClaim=" + challengedClaim + ", notes=" + notes
				+ ", challengeExpiryDate=" + challengeExpiryDate
				+ ", dateOfStart=" + dateOfStart + ", version=" + version
				+ ", claimArea=" + claimArea
				+ ", challengingClaims="
				// + Arrays.toString(challengingClaims.toArray())
				+ ", attachments=" + Arrays.toString(attachments.toArray())
				+ ", shares=" + Arrays.toString(shares.toArray())
				+ ", availableShares=" + availableShares + "]";
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getClaimId() {
		return claimId;
	}

	public void setClaimId(String claimId) {
		this.claimId = claimId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public void setChallengedClaimId(String challengedClaimId) {
		this.challengedClaimId = challengedClaimId;
	}

	public Person getPerson() {
		if (personId != null && person == null) {
			person = Person.getPerson(personId);
		}
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public List<Vertex> getVertices() {
		if (claimId != null && vertices == null) {
			vertices = Vertex.getVertices(claimId);
		}
		return vertices;
	}

	public void setVertices(List<Vertex> vertices) {
		this.vertices = vertices;
	}

	public List<PropertyLocation> getPropertyLocations() {
		if (claimId != null && propertyLocations == null) {
			propertyLocations = PropertyLocation.getPropertyLocations(claimId);
		}
		return propertyLocations;
	}

	public void setPropertyLocations(List<PropertyLocation> propertyLocations) {
		this.propertyLocations = propertyLocations;
	}

	public Claim getChallengedClaim() {
		if (challengedClaimId != null && challengedClaim == null) {
			challengedClaim = Claim.getClaim(challengedClaimId);
		}
		return challengedClaim;
	}

	public void setChallengedClaim(Claim challengedClaim) {
		this.challengedClaim = challengedClaim;
	}

	public List<Claim> getChallengingClaims() {
		return challengingClaims;
	}

	public void setChallengingClaims(List<Claim> challengingClaims) {
		this.challengingClaims = challengingClaims;
	}

	public List<Attachment> getAttachments() {
		if (claimId != null && attachments == null) {
			attachments = Attachment.getAttachments(claimId);
		}
		return attachments;
	}

	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}

	public List<ShareProperty> getShares() {
		if (claimId != null && shares == null) {
			shares = ShareProperty.getShares(claimId);
		}
		return shares;
	}

	public void setShares(List<ShareProperty> shares) {
		availableShares = MAX_SHARES_PER_CLAIM;
		for (ShareProperty share : shares) {
			availableShares -= share.getShares();
		}
		this.shares = shares;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getChallengeExpiryDate() {
		return challengeExpiryDate;
	}

	public void setChallengeExpiryDate(Date challengeExpiryDate) {
		this.challengeExpiryDate = challengeExpiryDate;
	}

	public String getLandUse() {
		return landUse;
	}

	public void setLandUse(String landUse) {
		this.landUse = landUse;
	}

	public java.sql.Date getDateOfStart() {
		return dateOfStart;
	}

	public void setDateOfStart(java.sql.Date dateOfStart) {
		this.dateOfStart = dateOfStart;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getClaimNumber() {
		return claimNumber;
	}

	public void setClaimNumber(String claimNumber) {
		this.claimNumber = claimNumber;
	}

	public AdjacenciesNotes getAdjacenciesNotes() {
		return adjacenciesNotes;
	}

	public void setAdjacenciesNotes(AdjacenciesNotes adjacenciesNotes) {
		this.adjacenciesNotes = adjacenciesNotes;
	}

	public String getRecorderName() {
		return recorderName;
	}

	public void setRecorderName(String recorderName) {
		this.recorderName = recorderName;
	}

	public long getClaimArea() {
		return claimArea;
	}

	public void setClaimArea(long claimArea) {
		this.claimArea = claimArea;
	}

	public static int createClaim(Claim claim) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("INSERT INTO CLAIM(CLAIM_ID, STATUS, CLAIM_NUMBER, NAME, TYPE, PERSON_ID, CHALLENGED_CLAIM_ID, CHALLANGE_EXPIRY_DATE,DATE_OF_START, LAND_USE, NOTES, RECORDER_NAME, VERSION, SURVEY_FORM) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setString(1, claim.getClaimId());
			statement.setString(2, claim.getStatus());
			statement.setString(3, claim.getClaimNumber());
			statement.setString(4, claim.getName());
			statement.setString(5, claim.getType());
			statement.setString(6, claim.getPerson().getPersonId());
			if (claim.getChallengedClaim() != null) {
				statement.setString(7, claim.getChallengedClaim().getClaimId());

			} else {
				statement.setString(7, null);
			}
			statement.setDate(8, claim.getChallengeExpiryDate());
			statement.setDate(9, claim.getDateOfStart());
			statement.setString(10, claim.getLandUse());
			statement.setString(11, claim.getNotes());
			statement.setString(12, claim.getRecorderName());
			statement.setString(13, claim.getVersion());
			if (claim.getDynamicForm() != null) {
				statement.setCharacterStream(14, new StringReader(claim
						.getDynamicForm().toJson()));

			} else {
				statement.setCharacterStream(14, null);
			}

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
					.prepareStatement("INSERT INTO CLAIM(CLAIM_ID, STATUS, CLAIM_NUMBER, NAME, TYPE, PERSON_ID, CHALLENGED_CLAIM_ID, CHALLANGE_EXPIRY_DATE,DATE_OF_START, LAND_USE, NOTES,RECORDER_NAME,VERSION, SURVEY_FORM) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			statement.setString(1, getClaimId());
			statement.setString(2, getStatus());
			statement.setString(3, getClaimNumber());
			statement.setString(4, getName());
			statement.setString(5, getType());
			statement.setString(6, getPerson().getPersonId());
			if (getChallengedClaim() != null) {
				statement.setString(7, getChallengedClaim().getClaimId());
			} else {
				statement.setString(7, null);
			}
			statement.setDate(8, getChallengeExpiryDate());
			statement.setDate(9, getDateOfStart());
			statement.setString(10, getLandUse());
			statement.setString(11, getNotes());
			statement.setString(12, getRecorderName());
			statement.setString(13, getVersion());
			if (getDynamicForm() != null) {
				statement.setCharacterStream(14, new StringReader(
						getDynamicForm().toJson()));

			} else {
				statement.setCharacterStream(14, null);
			}
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

	public static int updateClaim(Claim claim) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE CLAIM SET STATUS=?, CLAIM_NUMBER=?, NAME=?, PERSON_ID=?, TYPE=?,CHALLENGED_CLAIM_ID=?, CHALLANGE_EXPIRY_DATE=?, DATE_OF_START=?, LAND_USE=?, NOTES=?, RECORDER_NAME=?, VERSION=? , SURVEY_FORM=? WHERE CLAIM_ID=?");
			statement.setString(1, claim.getStatus());
			statement.setString(2, claim.getClaimNumber());
			statement.setString(3, claim.getName());
			statement.setString(4, claim.getPerson().getPersonId());
			statement.setString(5, claim.getType());
			if (claim.getChallengedClaim() != null) {
				statement.setString(6, claim.getChallengedClaim().getClaimId());
			} else {
				statement.setString(6, null);
			}
			statement.setDate(7, claim.getChallengeExpiryDate());
			statement.setDate(8, claim.getDateOfStart());
			statement.setString(9, claim.getLandUse());
			statement.setString(10, claim.getNotes());
			statement.setString(11, claim.getRecorderName());
			statement.setString(12, claim.getVersion());
			if (claim.getDynamicForm() != null) {
				statement.setCharacterStream(13, new StringReader(claim
						.getDynamicForm().toJson()));

			} else {
				statement.setCharacterStream(13, null);
			}
			statement.setString(14, claim.getClaimId());
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
					.prepareStatement("UPDATE CLAIM SET STATUS=?, CLAIM_NUMBER=?,NAME=?, PERSON_ID=?, TYPE=?, CHALLENGED_CLAIM_ID=?, CHALLANGE_EXPIRY_DATE=?, DATE_OF_START=?, LAND_USE=?, NOTES=?, RECORDER_NAME=?, VERSION=?, SURVEY_FORM=?  WHERE CLAIM_ID=?");
			statement.setString(1, getStatus());
			statement.setString(2, getClaimNumber());
			statement.setString(3, getName());
			statement.setString(4, getPerson().getPersonId());
			statement.setString(5, getType());
			if (getChallengedClaim() != null) {
				statement.setString(6, getChallengedClaim().getClaimId());

			} else {
				statement.setString(6, null);
			}
			statement.setDate(7, getChallengeExpiryDate());
			statement.setDate(8, getDateOfStart());
			statement.setString(9, getLandUse());
			statement.setString(10, getNotes());
			statement.setString(11, getRecorderName());
			statement.setString(12, getVersion());
			if (getDynamicForm() != null) {
				statement.setCharacterStream(13, new StringReader(
						getDynamicForm().toJson()));

			} else {
				statement.setCharacterStream(13, null);
			}
			statement.setString(14, getClaimId());
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

	public static Claim getClaim(String claimId) {
		Claim claim = null;
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT STATUS, CLAIM_NUMBER, NAME, PERSON_ID, TYPE, CHALLENGED_CLAIM_ID, CHALLANGE_EXPIRY_DATE, DATE_OF_START, LAND_USE, NOTES, RECORDER_NAME, VERSION, CLAIM_AREA, SURVEY_FORM FROM CLAIM WHERE CLAIM_ID=?");
			statement.setString(1, claimId);
			rs = statement.executeQuery();
			while (rs.next()) {
				claim = new Claim();
				claim.setClaimId(claimId);
				claim.setStatus(rs.getString(1));
				claim.setClaimNumber(rs.getString(2));
				claim.setName(rs.getString(3));
				claim.setPersonId(rs.getString(4));
				claim.setType((rs.getString(5)));
				claim.setChallengedClaimId(rs.getString(6));
				claim.setChallengeExpiryDate(rs.getDate(7));
				claim.setDateOfStart(rs.getDate(8));
				claim.setLandUse(rs.getString(9));
				claim.setNotes(rs.getString(10));
				claim.setRecorderName(rs.getString(11));
				claim.setVersion(rs.getString(12));
				claim.setClaimArea(rs.getInt(13));
				Clob clob = rs.getClob(14);
				if (clob != null) {
					claim.setDynamicForm(FormPayload.fromJson(clob
							.getSubString(1L, (int) clob.length())));
				} else {
					claim.setDynamicForm(new FormPayload());
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
		return claim;
	}

	public static Claim getClaim(String claimId, Connection externalConnection) {
		Claim claim = null;
		PreparedStatement statement = null;
		try {

			statement = externalConnection
					.prepareStatement("SELECT STATUS, CLAIM_NUMBER, NAME, PERSON_ID, TYPE, CHALLENGED_CLAIM_ID, CHALLANGE_EXPIRY_DATE, DATE_OF_START, LAND_USE, NOTES, RECORDER_NAME, VERSION, CLAIM_AREA, SURVEY_FORM FROM CLAIM WHERE CLAIM_ID=?");
			statement.setString(1, claimId);
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				claim = new Claim();
				claim.setClaimId(claimId);
				claim.setStatus(rs.getString(1));
				claim.setClaimNumber(rs.getString(2));
				claim.setName(rs.getString(3));
				claim.setPersonId(rs.getString(4));
				claim.setType((rs.getString(5)));
				claim.setChallengedClaimId(rs.getString(6));
				claim.setChallengeExpiryDate(rs.getDate(7));
				claim.setDateOfStart(rs.getDate(8));
				claim.setLandUse(rs.getString(9));
				claim.setNotes(rs.getString(10));
				claim.setRecorderName(rs.getString(11));
				claim.setVersion(rs.getString(12));
				claim.setClaimArea(rs.getInt(13));
				Clob clob = rs.getClob(14);
				if (clob != null) {
					claim.setDynamicForm(FormPayload.fromJson(clob
							.getSubString(1L, (int) clob.length())));
				} else {
					claim.setDynamicForm(new FormPayload());
				}
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					// also closes current result set if any
					statement.close();
				} catch (SQLException e) {
				}
			}
		}
		return claim;
	}

	public static List<Claim> getAllClaims(Connection externalConnection) {
		List<Claim> allClaims = new ArrayList<Claim>();
		PreparedStatement statement = null;
		try {

			statement = externalConnection
					.prepareStatement("SELECT CLAIM_ID, STATUS, CLAIM_NUMBER, NAME, PERSON_ID, TYPE, CHALLENGED_CLAIM_ID, CHALLANGE_EXPIRY_DATE, DATE_OF_START, LAND_USE, NOTES, RECORDER_NAME, VERSION, CLAIM_AREA, SURVEY_FORM FROM CLAIM");
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				String claimId = rs.getString(1);
				Claim claim = new Claim();
				claim.setClaimId(claimId);
				claim.setStatus(rs.getString(2));
				claim.setClaimNumber(rs.getString(3));
				claim.setName(rs.getString(4));
				claim.setPerson(Person.getPerson(rs.getString(5)));
				claim.setType((rs.getString(6)));
				claim.setChallengedClaim(Claim.getClaim(rs.getString(7)));
				claim.setChallengeExpiryDate(rs.getDate(8));
				claim.setDateOfStart(rs.getDate(9));
				claim.setLandUse(rs.getString(10));
				claim.setNotes(rs.getString(11));
				claim.setRecorderName(rs.getString(12));
				claim.setVersion(rs.getString(13));
				claim.setClaimArea(rs.getInt(14));
				Clob clob = rs.getClob(15);
				if (clob != null) {
					claim.setDynamicForm(FormPayload.fromJson(clob
							.getSubString(1L, (int) clob.length())));
				} else {
					claim.setDynamicForm(new FormPayload());
				}
				claim.setVertices(Vertex.getVertices(claimId,
						externalConnection));
				claim.setPropertyLocations(PropertyLocation
						.getPropertyLocations(claimId, externalConnection));
				claim.setAttachments(Attachment.getAttachments(claimId,
						externalConnection));
				claim.setShares(ShareProperty.getShares(claimId,
						externalConnection));
				claim.setAdditionalInfo(new ArrayList<AdditionalInfo>()); // No
																			// longer
																			// used
				allClaims.add(claim);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					// also closes current result set if any
					statement.close();
				} catch (SQLException e) {
				}
			}
		}
		return allClaims;
	}

	public static List<Claim> getSimplifiedClaims(Connection externalConnection) {
		// Only loads what doesn't need subqueries on other tables
		List<Claim> allClaims = new ArrayList<Claim>();
		PreparedStatement statement = null;
		try {

			statement = externalConnection
					.prepareStatement("SELECT CLAIM_ID, STATUS, CLAIM_NUMBER, NAME, TYPE, CHALLANGE_EXPIRY_DATE, DATE_OF_START, LAND_USE, NOTES, RECORDER_NAME, VERSION, CLAIM_AREA FROM CLAIM");
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				String claimId = rs.getString(1);
				Claim claim = new Claim();
				claim.setClaimId(claimId);
				claim.setStatus(rs.getString(2));
				claim.setClaimNumber(rs.getString(3));
				claim.setName(rs.getString(4));
				claim.setType((rs.getString(5)));
				claim.setChallengeExpiryDate(rs.getDate(6));
				claim.setDateOfStart(rs.getDate(7));
				claim.setLandUse(rs.getString(8));
				claim.setNotes(rs.getString(9));
				claim.setRecorderName(rs.getString(10));
				claim.setVersion(rs.getString(11));
				claim.setClaimArea(rs.getInt(12));
				claim.setAdditionalInfo(new ArrayList<AdditionalInfo>()); // No
																			// longer
																			// used
				allClaims.add(claim);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					// also closes current result set if any
					statement.close();
				} catch (SQLException e) {
				}
			}
		}
		return allClaims;
	}

	public static Map<String, Claim> getSimplifiedClaimsForDownload(
			Connection externalConnection) {
		HashMap<String, Claim> allClaims = new HashMap<String, Claim>();
		PreparedStatement statement = null;
		try {

			statement = externalConnection
					.prepareStatement("SELECT CLAIM_ID, VERSION FROM CLAIM");
			ResultSet rs = statement.executeQuery();
			while (rs.next()) {
				String claimId = rs.getString(1);
				Claim claim = new Claim();
				claim.setClaimId(claimId);
				claim.setVersion(rs.getString(2));
				allClaims.put(claimId, claim);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					// also closes current result set if any
					statement.close();
				} catch (SQLException e) {
				}
			}
		}
		return allClaims;
	}

	public static List<Claim> getSimplifiedClaimsForMap(
			Connection externalConnection) {
		// Only loads what's needed to draw properties on the maps
		List<Claim> allClaims = new ArrayList<Claim>();
		PreparedStatement statement = null;
		String lastClaimId = null;
		try {

			statement = externalConnection.prepareStatement("SELECT "
					+ "CLAIM.CLAIM_ID, " + "CLAIM.STATUS, " + "CLAIM.NAME, "
					+ "CLAIM.TYPE, " + "PERSON.PERSON_ID, "
					+ "PERSON.FIRST_NAME, " + "PERSON.LAST_NAME, "
					+ "VERTEX.VERTEX_ID, " + "VERTEX.SEQUENCE_NUMBER, "
					+ "VERTEX.GPS_LAT, " + "VERTEX.GPS_LON, "
					+ "VERTEX.MAP_LAT, " + "VERTEX.MAP_LON "
					+ "FROM CLAIM, PERSON, VERTEX "
					+ "WHERE CLAIM.PERSON_ID=PERSON.PERSON_ID "
					+ "AND CLAIM.CLAIM_ID=VERTEX.CLAIM_ID "
					+ "ORDER BY CLAIM_ID, VERTEX.SEQUENCE_NUMBER");
			ResultSet rs = statement.executeQuery();
			List<Vertex> vertices = null;
			Claim claim = null;
			int nClaims = 0;
			int nVertices = 0;
			while (rs.next()) {
				String claimId = rs.getString(1);
				if (lastClaimId == null
						|| !lastClaimId.equalsIgnoreCase(claimId)) {
					// It's a new claim so we add the previous one, if any, to
					// the list
					if (claim != null) {
						if (vertices != null) {
							claim.setVertices(vertices);
						} else {
							claim.setVertices(new ArrayList<Vertex>());
						}
						allClaims.add(claim);
						nClaims++;
					}
					vertices = new ArrayList<Vertex>();
					claim = new Claim();
					claim.setClaimId(claimId);
					claim.setStatus(rs.getString(2));
					claim.setName(rs.getString(3));
					claim.setType((rs.getString(4)));
					String personId = rs.getString(5);
					String firstName = rs.getString(6);
					String lastName = rs.getString(7);
					Person person = new Person();
					person.setPersonId(personId);
					person.setFirstName(firstName);
					person.setLastName(lastName);
					claim.setPerson(person);
				}
				// It's a new vertex for the same claim
				Vertex vertex = new Vertex();
				vertex.setVertexId(rs.getString(8));
				vertex.setSequenceNumber(rs.getInt(9));
				vertex.setGPSPosition(new LatLng(rs.getBigDecimal(10)
						.doubleValue(), rs.getBigDecimal(11).doubleValue()));
				vertex.setMapPosition(new LatLng(rs.getBigDecimal(12)
						.doubleValue(), rs.getBigDecimal(13).doubleValue()));
				vertices.add(vertex);
				nVertices++;
				lastClaimId = claimId;
			}
			if (claim != null) {
				if (vertices != null) {
					claim.setVertices(vertices);
				} else {
					claim.setVertices(new ArrayList<Vertex>());
				}
				allClaims.add(claim);
				nClaims++;
			}
			Log.d(Claim.class.getName(), "Retrieved " + nVertices
					+ " vertices for " + nClaims + " claims");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					// also closes current result set if any
					statement.close();
				} catch (SQLException e) {
				}
			}
		}
		return allClaims;
	}

	public static List<Claim> getSimplifiedClaimsForList(
			Connection externalConnection) {
		// Only loads what's needed to fill the list of locally stored claims
		List<Claim> allClaims = new ArrayList<Claim>();
		PreparedStatement statement = null;
		String lastClaimId = null;
		try {

			// Join Claim, Person and Attachment to get everything we need at
			// once

			statement = externalConnection
					.prepareStatement("SELECT "
							+ "CP.CLAIM_ID, "
							+ "CP.STATUS, "
							+ "CP.NAME, "
							+ "CP.TYPE, "
							+ "CP.CLAIM_NUMBER, "
							+ "CP.CHALLANGE_EXPIRY_DATE, "
							+ "CP.RECORDER_NAME, "
							+ "CP.PERSON_ID, "
							+ "CP.FIRST_NAME, "
							+ "CP.LAST_NAME, "
							+ "ATTACHMENT.ATTACHMENT_ID, "
							+ "ATTACHMENT.STATUS, "
							+ "ATTACHMENT.SIZE "
							+ "FROM (SELECT "
							+ "CLAIM.CLAIM_ID, "
							+ "CLAIM.STATUS, "
							+ "CLAIM.NAME, "
							+ "CLAIM.TYPE, "
							+ "CLAIM.CLAIM_NUMBER, "
							+ "CLAIM.CHALLANGE_EXPIRY_DATE, "
							+ "CLAIM.RECORDER_NAME, "
							+ "PERSON.PERSON_ID, "
							+ "PERSON.FIRST_NAME, "
							+ "PERSON.LAST_NAME "
							+ "FROM CLAIM, PERSON "
							+ "WHERE CLAIM.PERSON_ID=PERSON.PERSON_ID) AS CP LEFT JOIN ATTACHMENT ON (CP.CLAIM_ID=ATTACHMENT.CLAIM_ID) "
							+ "ORDER BY CP.CLAIM_ID");
			ResultSet rs = statement.executeQuery();
			List<Attachment> attachments = null;
			Claim claim = null;
			int nClaims = 0;
			int nAttachments = 0;
			while (rs.next()) {
				String claimId = rs.getString(1);
				if (lastClaimId == null
						|| !lastClaimId.equalsIgnoreCase(claimId)) {
					// It's a new claim so we add the previous one, if any, to
					// the list
					if (claim != null) {
						if (attachments != null) {
							claim.setAttachments(attachments);
						} else {
							claim.setAttachments(new ArrayList<Attachment>());
						}
						allClaims.add(claim);
						nClaims++;
					}
					attachments = new ArrayList<Attachment>();
					claim = new Claim();
					claim.setClaimId(claimId);
					claim.setStatus(rs.getString(2));
					claim.setName(rs.getString(3));
					claim.setType((rs.getString(4)));
					claim.setClaimNumber((rs.getString(5)));
					claim.setChallengeExpiryDate((rs.getDate(6)));
					claim.setRecorderName((rs.getString(7)));
					String personId = rs.getString(8);
					Person person = new Person();
					person.setPersonId(personId);
					person.setFirstName(rs.getString(9));
					person.setLastName(rs.getString(10));
					claim.setPerson(person);
				}
				String attachmentId = rs.getString(11);
				if (attachmentId != null) {
					// It's a new attachment for the same claim
					Attachment attachment = new Attachment();
					attachment.setAttachmentId(attachmentId);
					attachment.setStatus(rs.getString(12));
					attachment.setSize(rs.getLong(13));
					attachments.add(attachment);
					nAttachments++;
				}
				lastClaimId = claimId;
			}
			if (claim != null) {
				if (attachments != null) {
					claim.setAttachments(attachments);
				} else {
					claim.setAttachments(new ArrayList<Attachment>());
				}
				allClaims.add(claim);
				nClaims++;
			}
			Log.d(Claim.class.getName(), "Retrieved " + nAttachments
					+ " attachments for " + nClaims + " claims");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (statement != null) {
				try {
					// also closes current result set if any
					statement.close();
				} catch (SQLException e) {
				}
			}
		}
		return allClaims;
	}

	public static List<Claim> getChallengingClaims(String claimId) {
		List<Claim> challengingClaims = new ArrayList<Claim>();
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;

		try {
			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT CLAIM_ID FROM CLAIM WHERE CHALLENGED_CLAIM_ID=?");
			statement.setString(1, claimId);
			rs = statement.executeQuery();
			while (rs.next()) {

				Claim challengingClaim = Claim.getClaim(rs.getString(1));
				challengingClaims.add(challengingClaim);
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
		return challengingClaims;
	}

	public static int addOwner(String claimId, String personId, int shares) {
		return Claim.getClaim(claimId).addOwner(personId, shares);
	}

	public int addOwner(String personId, int shares) {

		ShareProperty share = new ShareProperty();
		share.setClaimId(claimId);
		share.setShares(shares);

		int result = share.create();

		if (result == 1) {
			availableShares -= shares;
		}
		return result;
	}

	public int removeShare(String shareId) {

		ShareProperty share = ShareProperty.getShare(shareId);
		int shares = share.getShares();

		int result = share.deleteShare();

		if (result == 1) {
			availableShares += shares;
		}
		return result;
	}

	public static List<Claim> getAllClaims() {
		Connection localConnection = null;
		List<Claim> allClaims = null;
		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			allClaims = getAllClaims(localConnection);
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
		return allClaims;
	}

	public static List<Claim> getSimplifiedClaimsForMap() {
		Connection localConnection = null;
		List<Claim> allClaims = null;
		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			allClaims = getSimplifiedClaimsForMap(localConnection);
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
		return allClaims;
	}

	public static List<Claim> getSimplifiedClaims() {
		Connection localConnection = null;
		List<Claim> allClaims = null;
		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			allClaims = getSimplifiedClaims(localConnection);
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
		return allClaims;
	}

	public static List<Claim> getSimplifiedClaimsForList() {
		Connection localConnection = null;
		List<Claim> allClaims = null;
		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			allClaims = getSimplifiedClaimsForList(localConnection);
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
		return allClaims;
	}

	public static Map<String, Claim> getSimplifiedClaimsForDownload() {
		Connection localConnection = null;
		Map<String, Claim> allClaims = null;
		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			allClaims = getSimplifiedClaimsForDownload(localConnection);
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
		return allClaims;
	}

	public static int getNumberOfClaims() {
		Connection localConnection = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		int result = 0;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("SELECT COUNT (*) FROM CLAIM");
			rs = statement.executeQuery();
			while (rs.next()) {
				result = rs.getInt(1);
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
		return result;
	}

	public static int deleteCascade(String claimId) {
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();

			ShareProperty.deleteShares(claimId, localConnection);
			Vertex.deleteVertices(claimId, localConnection);
			Attachment.deleteAttachments(claimId, localConnection);
			PropertyLocation.deletePropertyLocations(claimId, localConnection);
			Adjacency.deleteAdjacencies(claimId, localConnection);
			AdjacenciesNotes.deleteAdjacenciesNotes(claimId, localConnection);

			statement = localConnection
					.prepareStatement("DELETE CLAIM WHERE CLAIM_ID=?");
			statement.setString(1, claimId);

			result = statement.executeUpdate();
			FileSystemUtilities.deleteClaim(claimId);
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

	public String getSlogan(Context context) {
		String claimName = getName().equalsIgnoreCase("") ? context
				.getString(R.string.default_claim_name) : getName();
		return claimName + ", " + context.getString(R.string.by) + ": "
				+ getPerson().getFirstName() + " " + getPerson().getLastName();
	}

	public static String getSlogan(String name, String firstName,
			String lastName, Context context) {
		String claimName = name.equalsIgnoreCase("") ? context
				.getString(R.string.default_claim_name) : name;
		return claimName + ", " + context.getString(R.string.by) + ": "
				+ firstName + " " + lastName;
	}

	public boolean isUploadable() {

		if (getChallengeExpiryDate() == null)
			return true;

		if (!(getStatus().equals(ClaimStatus._WITHDRAWN))
				&& (JsonUtilities.remainingDays(getChallengeExpiryDate()) >= 1))
			return true;
		else
			return false;
	}

	public boolean isModifiable() {

		if (getChallengeExpiryDate() == null)
			return true;
		else
			return false;
	}
	
	
	public static int resetClaimUploading(){
		
		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE CLAIM SET STATUS= 'created' WHERE status= 'uploading' ");
			
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

	public int updateArea(long area) {

		int result = 0;
		Connection localConnection = null;
		PreparedStatement statement = null;

		try {

			localConnection = OpenTenureApplication.getInstance().getDatabase()
					.getConnection();
			statement = localConnection
					.prepareStatement("UPDATE CLAIM SET CLAIM_AREA=? WHERE CLAIM_ID=?");
			statement.setLong(1, area);
			statement.setString(2, getClaimId());
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

	private String claimId;
	private String name;
	private String type;
	private String status;
	private Person person;
	private String personId;
	private Date dateOfStart;
	private Claim challengedClaim;
	private String challengedClaimId;
	private AdjacenciesNotes adjacenciesNotes;
	private List<Vertex> vertices;
	private List<PropertyLocation> propertyLocations;
	private List<AdditionalInfo> additionalInfo;
	private List<Claim> challengingClaims;
	private List<Attachment> attachments;
	private List<ShareProperty> shares;
	private Date challengeExpiryDate;
	private int availableShares;
	private String landUse;
	private String notes;
	private String claimNumber;
	private String recorderName;
	private String version;
	private long claimArea;
	private FormPayload dynamicForm;

}
