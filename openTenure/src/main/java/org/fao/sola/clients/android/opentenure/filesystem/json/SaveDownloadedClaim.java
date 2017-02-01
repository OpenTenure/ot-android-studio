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
package org.fao.sola.clients.android.opentenure.filesystem.json;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.filesystem.json.model.Attachment;
import org.fao.sola.clients.android.opentenure.filesystem.json.model.Claim;
import org.fao.sola.clients.android.opentenure.filesystem.json.model.Claimant;
import org.fao.sola.clients.android.opentenure.filesystem.json.model.Location;
import org.fao.sola.clients.android.opentenure.filesystem.json.model.Share;
import org.fao.sola.clients.android.opentenure.model.AdjacenciesNotes;
import org.fao.sola.clients.android.opentenure.model.AttachmentStatus;
import org.fao.sola.clients.android.opentenure.model.Owner;
import org.fao.sola.clients.android.opentenure.model.ShareProperty;
import org.fao.sola.clients.android.opentenure.model.Person;
import org.fao.sola.clients.android.opentenure.model.PropertyLocation;
import org.fao.sola.clients.android.opentenure.model.Vertex;
import org.fao.sola.clients.android.opentenure.network.GetClaimantPhotoTask;
import org.fao.sola.clients.android.opentenure.network.API.CommunityServerAPI;

import android.util.Log;

public class SaveDownloadedClaim {

	/**
	 * 
	 * Parsing the downloaded Claim and saving it to DB
	 **/

	public static boolean save(Claim downloadedClaim) {

		TimeZone tz = TimeZone.getTimeZone("UTC");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		sdf.setTimeZone(tz);

		List<org.fao.sola.clients.android.opentenure.model.Attachment> attachmentsDB = new ArrayList<org.fao.sola.clients.android.opentenure.model.Attachment>();
		List<org.fao.sola.clients.android.opentenure.model.AdditionalInfo> additionalInfoDBList = new ArrayList<org.fao.sola.clients.android.opentenure.model.AdditionalInfo>();

		org.fao.sola.clients.android.opentenure.model.Claim claimDB = new org.fao.sola.clients.android.opentenure.model.Claim();
		/*
		 * Temporary disable
		 */
		// List<AdditionalInfo> metadataList;
		// if ((metadataList = claim.getAdditionaInfo()) != null) {
		//
		// for (Iterator iterator = metadataList.iterator(); iterator
		// .hasNext();) {
		// AdditionalInfo additionalInfo = (AdditionalInfo) iterator
		// .next();
		//
		// org.fao.sola.clients.android.opentenure.model.Metadata metadataDB
		// =
		// new Metadata();
		//
		// metadataDB.setClaimId(claim.getId());
		// metadataDB.setMetadataId(additionalInfo.getMetadataId());
		// metadataDB.setName(additionalInfo.getName());
		// metadataDB.setValue(additionalInfo.getValue());
		//
		// metadataDBList.add(metadataDB);
		//
		// }
		// }

		/*
		 * First of all cheks if claim is challenging another claim. In case of
		 * challenge, need to download ad save the claim challenged
		 * 
		 * 
		 * We should set the challenged claim but if is not in the right order
		 * it will be there a problem
		 */

		if (downloadedClaim.getChallengedClaimId() != null
				&& !downloadedClaim.getChallengedClaimId().equals("")) {

			/*
			 * The downloaded claim got a challenging . Check if the challenged
			 * is already present locally
			 */
			org.fao.sola.clients.android.opentenure.model.Claim challenged = org.fao.sola.clients.android.opentenure.model.Claim
					.getClaim(downloadedClaim.getChallengedClaimId());
			if (challenged == null) {
				/*
				 * here the case in which the claim challenged is not already
				 * present locally. Making a call to GetClaimsTask to retrieve
				 * the challenged claim
				 */

				try {

					/*
					 * Here the task will download the necessary challenged
					 * claim and the thread will wait for the result before to
					 * go forward
					 */

					org.fao.sola.clients.android.opentenure.filesystem.json.model.Claim challengingClaim = CommunityServerAPI
							.getClaim(downloadedClaim.getChallengedClaimId());

					if (challengingClaim == null) {
						Log.d("CommunityServerAPI",
								"ERROR SAVING CHALLENGED CLAIM OF DOWNLOADED  CLAIM "
										+ downloadedClaim.getId());
						return false;

					} else {
						SaveDownloadedClaim.save(challengingClaim);

					}

				} catch (Exception e) {

					Log.d("CommunityServerAPI",
							"ERROR SAVING CHALLENGED CLAIM OF DOWNLOADED  CLAIM "
									+ downloadedClaim.getId());
					e.printStackTrace();
				}

				claimDB.setChallengedClaim(org.fao.sola.clients.android.opentenure.model.Claim
						.getClaim(downloadedClaim.getChallengedClaimId()));

			} else {

				claimDB.setChallengedClaim(org.fao.sola.clients.android.opentenure.model.Claim
						.getClaim(downloadedClaim.getChallengedClaimId()));

			}

		}

		Claimant claimant = downloadedClaim.getClaimant();

		Person person = new Person();
		person.setContactPhoneNumber(claimant.getPhone());

		Date birth = null;
		try {
			// birth = df.parse(claimant.getBirthDate());

			String aDate = claimant.getBirthDate();
			if (aDate != null) {

				Calendar cal = JsonUtilities.toCalendar(aDate);
				birth = cal.getTime();

				if (birth != null)
					person.setDateOfBirth(new java.sql.Date(birth.getTime()));
				else
					person.setDateOfBirth(new java.sql.Date(2000, 2, 3));

			}

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

			Log.d("CommunityServerAPI", "ERROR SAVING  CLAIM "
					+ downloadedClaim.getId());

			return false;
		}

		try {
			Date date;

			person.setEmailAddress(claimant.getEmail());
			person.setFirstName(claimant.getName());
			person.setGender(claimant.getGenderCode());
			person.setLastName(claimant.getLastName());
			person.setMobilePhoneNumber(claimant.getMobilePhone());
			person.setPersonId(claimant.getId());
			person.setIdNumber(claimant.getIdNumber());
			person.setIdType(claimant.getIdTypeCode());
			// person.setPlaceOfBirth(claimant.getPlaceOfBirth());
			person.setPostalAddress(claimant.getAddress());
			if (claimant.isPhysicalPerson())
				person.setPersonType(Person._PHYSICAL);
			else
				person.setPersonType(Person._GROUP);

			claimDB.setAttachments(attachmentsDB);

			claimDB.setClaimId(downloadedClaim.getId());
			claimDB.setAdditionalInfo(additionalInfoDBList);
			claimDB.setName(downloadedClaim.getDescription());
			claimDB.setLandUse(downloadedClaim.getLandUseCode());
			claimDB.setNotes(downloadedClaim.getNotes());
			claimDB.setRecorderName(downloadedClaim.getRecorderName());
			claimDB.setVersion(downloadedClaim.getVersion());

			if (downloadedClaim.getStartDate() != null) {
				date = sdf.parse(downloadedClaim.getStartDate());
				claimDB.setDateOfStart(new java.sql.Date(date.getTime()));
			}
			if (downloadedClaim.getChallengeExpiryDate() != null) {
				date = sdf.parse(downloadedClaim.getChallengeExpiryDate());
				claimDB.setChallengeExpiryDate(new java.sql.Date(date.getTime()));
			} 
			claimDB.setPerson(person);
			claimDB.setStatus(downloadedClaim.getStatusCode());
			claimDB.setClaimNumber(downloadedClaim.getNr());
			claimDB.setType(downloadedClaim.getTypeCode());
			claimDB.setDynamicForm(downloadedClaim.getDynamicForm());

			if (Person.getPerson(claimant.getId()) == null)
				Person.createPerson(person);
			else
				Person.updatePerson(person);

			// Here the creation of the Claim
			if (org.fao.sola.clients.android.opentenure.model.Claim
					.getClaim(downloadedClaim.getId()) == null)
				org.fao.sola.clients.android.opentenure.model.Claim
						.createClaim(claimDB);
			else
				org.fao.sola.clients.android.opentenure.model.Claim
						.updateClaim(claimDB);

			AdjacenciesNotes adjacenciesNotes = new AdjacenciesNotes();
			adjacenciesNotes.setClaimId(downloadedClaim.getId());
			adjacenciesNotes.setNorthAdjacency(downloadedClaim
					.getNorthAdjacency());
			adjacenciesNotes.setSouthAdjacency(downloadedClaim
					.getSouthAdjacency());
			adjacenciesNotes.setEastAdjacency(downloadedClaim
					.getEastAdjacency());
			adjacenciesNotes.setWestAdjacency(downloadedClaim
					.getWestAdjacency());

			if (AdjacenciesNotes.getAdjacenciesNotes(downloadedClaim.getId()) == null)
				adjacenciesNotes.create();
			else
				AdjacenciesNotes.updateAdjacenciesNotes(adjacenciesNotes);

			if (downloadedClaim.getGpsGeometry() == null
					|| downloadedClaim.getGpsGeometry().startsWith("POINT"))
				Vertex.storeWKT(claimDB.getClaimId(),
						downloadedClaim.getMappedGeometry(),
						downloadedClaim.getMappedGeometry());
			else
				Vertex.storeWKT(claimDB.getClaimId(),
						downloadedClaim.getMappedGeometry(),
						downloadedClaim.getGpsGeometry());

			/*
			 * Here the creation of Folder for the claim
			 */

			FileSystemUtilities.createClaimantFolder(claimant.getId());
			FileSystemUtilities.createClaimFileSystem(downloadedClaim.getId());

			org.fao.sola.clients.android.opentenure.model.Attachment alreadyPresent;
			List<Attachment> attachments = downloadedClaim.getAttachments();
			for (Iterator<Attachment> iterator = attachments.iterator(); iterator
					.hasNext();) {

				org.fao.sola.clients.android.opentenure.model.Attachment attachmentDB = new org.fao.sola.clients.android.opentenure.model.Attachment();
				Attachment attachment = (Attachment) iterator.next();
				// Here the Claimant Photo handling
				if (attachment.getId().equals(claimant.getId())) {

					attachmentDB.setAttachmentId(attachment.getId());
					attachmentDB.setClaimId(downloadedClaim.getId());
					attachmentDB.setDescription(attachment.getDescription());
					attachmentDB.setFileName(attachment.getFileName());
					attachmentDB.setFileType(attachment.getTypeCode());
					attachmentDB.setMD5Sum(attachment.getMd5());
					attachmentDB.setMimeType(attachment.getMimeType());
					attachmentDB.setStatus(AttachmentStatus._UPLOADED);
					attachmentDB.setSize(attachment.getSize());
					attachmentDB.setPath(FileSystemUtilities.getClaimantFolder(
							claimant.getId()).getAbsolutePath());

					alreadyPresent = org.fao.sola.clients.android.opentenure.model.Attachment
							.getAttachment(attachment.getId());

					GetClaimantPhotoTask task = new GetClaimantPhotoTask();
					task.execute(attachmentDB);

				}

				else {
					attachmentDB.setAttachmentId(attachment.getId());
					attachmentDB.setClaimId(downloadedClaim.getId());
					attachmentDB.setDescription(attachment.getDescription());
					attachmentDB.setFileName(attachment.getFileName());
					attachmentDB.setFileType(attachment.getTypeCode());
					attachmentDB.setMD5Sum(attachment.getMd5());
					attachmentDB.setMimeType(attachment.getMimeType());
					attachmentDB.setStatus(AttachmentStatus._UPLOADED);
					attachmentDB.setSize(attachment.getSize());

					alreadyPresent = org.fao.sola.clients.android.opentenure.model.Attachment
							.getAttachment(attachment.getId());
				}
				if (alreadyPresent == null) {

					attachmentDB.setPath("");

					org.fao.sola.clients.android.opentenure.model.Attachment
							.createAttachment(attachmentDB);
				} else {

					attachmentDB.setPath(alreadyPresent.getPath());

					org.fao.sola.clients.android.opentenure.model.Attachment
							.updateAttachment(attachmentDB);
				}
			}

			List<Location> locations = downloadedClaim.getLocations();
			for (Iterator<Location> iterator = locations.iterator(); iterator
					.hasNext();) {

				Location location = (Location) iterator.next();

				org.fao.sola.clients.android.opentenure.model.PropertyLocation propertyLocation;
				propertyLocation = PropertyLocation
						.propertyLocationFromWKT(location.getMappedLocation(),
								location.getGpsLocation());

				if (propertyLocation != null) {
					propertyLocation.setClaimId(location.getClaimId());
					propertyLocation.setDescription(location.getDescription());
					propertyLocation.setPropertyLocationId(location.getId());

					int i = PropertyLocation
							.createPropertyLocation(propertyLocation);
				}
			}

			List<Share> shares = downloadedClaim.getShares();

			List<ShareProperty> localShares = ShareProperty
					.getShares(downloadedClaim.getId());
			for (Iterator iterator = localShares.iterator(); iterator.hasNext();) {
				ShareProperty shareProperty = (ShareProperty) iterator.next();

				if (shares.indexOf(shareProperty) == -1) {
					/*
					 * In this case the share shall be removed togheter with his
					 * owners
					 */

					List<Owner> owners = Owner.getOwners(shareProperty.getId());
					for (Iterator iterator2 = owners.iterator(); iterator2
							.hasNext();) {
						Owner owner = (Owner) iterator2.next();
						Person personTD = Person.getPerson(owner.getPersonId());
						owner.delete();
						personTD.delete();
					}

					shareProperty.deleteShare();

				}

			}

			for (Iterator iterator = shares.iterator(); iterator.hasNext();) {
				Share share = (Share) iterator.next();

				ShareProperty shareDB = new ShareProperty();

				shareDB.setClaimId(downloadedClaim.getId());
				shareDB.setId(share.getId());
				shareDB.setShares(share.getPercentage());

				if (ShareProperty.getShare(share.getId()) == null)
					shareDB.create();
				else
					shareDB.updateShare();

				List<org.fao.sola.clients.android.opentenure.filesystem.json.model.Person> sharePersons = share
						.getOwners();

				if (ShareProperty.getShare(share.getId()) != null) {
					List<Owner> localOwners = Owner.getOwners(share.getId());

					for (Iterator iteratorT = localOwners.iterator(); iteratorT
							.hasNext();) {
						Owner ownerT = (Owner) iteratorT.next();

						if (sharePersons.indexOf(ownerT) == -1) {
							Person personTBD = Person.getPerson(ownerT
									.getPersonId());

							ownerT.delete();
							personTBD.delete();

						}

					}

				}

				for (Iterator iterator2 = sharePersons.iterator(); iterator2
						.hasNext();) {
					org.fao.sola.clients.android.opentenure.filesystem.json.model.Person person2 = (org.fao.sola.clients.android.opentenure.filesystem.json.model.Person) iterator2
							.next();

					Person personDB2 = new Person();

					personDB2.setContactPhoneNumber(person2.getPhone());

					if (claimant.getBirthDate() != null) {
						Calendar cal = JsonUtilities.toCalendar(claimant
								.getBirthDate());
						birth = cal.getTime();
					}
					if (birth != null)
						personDB2.setDateOfBirth(new java.sql.Date(birth
								.getTime()));

					personDB2.setEmailAddress(person2.getEmail());
					personDB2.setFirstName(person2.getName());
					personDB2.setGender(person2.getGenderCode());
					personDB2.setLastName(person2.getLastName());
					personDB2.setMobilePhoneNumber(person2.getMobilePhone());
					personDB2.setPersonId(person2.getId());
					// personDB2.setPlaceOfBirth(person2.get);

					if (claimant.isPhysicalPerson())
						personDB2.setPersonType(Person._PHYSICAL);
					else
						personDB2.setPersonType(Person._GROUP);

					personDB2.setPostalAddress(person2.getAddress());

					if (Person.getPerson(person2.getId()) == null)
						Person.createPerson(personDB2);
					else
						Person.updatePerson(personDB2);

					Owner ownerDB = new Owner();
					ownerDB.setPersonId(person2.getId());
					ownerDB.setShareId(share.getId());
					ownerDB.create();

				}

			}

		}

		catch (Exception e) {
			Log.d("CommunityServerAPI", "ERROR SAVING DOWNLOADED  CLAIM "
					+ downloadedClaim.getId());
			e.printStackTrace();

			return false;
		}

		return true;
	}

}
