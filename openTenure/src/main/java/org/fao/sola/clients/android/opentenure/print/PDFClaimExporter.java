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
package org.fao.sola.clients.android.opentenure.print;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.fao.sola.clients.android.opentenure.DisplayNameLocalizer;
import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.form.FieldPayload;
import org.fao.sola.clients.android.opentenure.form.FormPayload;
import org.fao.sola.clients.android.opentenure.form.SectionElementPayload;
import org.fao.sola.clients.android.opentenure.form.SectionPayload;
import org.fao.sola.clients.android.opentenure.maps.EditablePropertyBoundary;
import org.fao.sola.clients.android.opentenure.model.AdjacenciesNotes;
import org.fao.sola.clients.android.opentenure.model.Adjacency;
import org.fao.sola.clients.android.opentenure.model.Attachment;
import org.fao.sola.clients.android.opentenure.model.Claim;
import org.fao.sola.clients.android.opentenure.model.ClaimType;
import org.fao.sola.clients.android.opentenure.model.DocumentType;
import org.fao.sola.clients.android.opentenure.model.IdType;
import org.fao.sola.clients.android.opentenure.model.LandUse;
import org.fao.sola.clients.android.opentenure.model.MD5;
import org.fao.sola.clients.android.opentenure.model.Owner;
import org.fao.sola.clients.android.opentenure.model.Person;
import org.fao.sola.clients.android.opentenure.model.ShareProperty;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfDocument.Page;
import android.graphics.pdf.PdfDocument.PageInfo;
import android.util.Log;

@SuppressLint("NewApi")
// Suppressions needed to allow compiling for API level 17
public class PDFClaimExporter {

	public static final int A4_PAGE_WIDTH = 595;
	public static final int A4_PAGE_HEIGHT = 842;
	public static final int LETTER_PAGE_WIDTH = 612;
	public static final int LETTER_PAGE_HEIGHT = 792;
	public static final int DEFAULT_HORIZONTAL_MARGIN = 40;
	public static final int DEFAULT_VERTICAL_MARGIN = 40;
	public static final int DEFAULT_VERTICAL_SPACE = 5;
	public static final int DEFAULT_HORIZONTAL_SPACE = 5;
	public static final String FONT_SANS_SERIF = "sans-serif";
	public static final String DEFAULT_CERTIFICATE_MIME_TYPE = "application/pdf";
	public static final String DEFAULT_CERTIFICATE_DOCUMENT_TYPE = "claimSummary";

	static PdfDocument document;
	private String filePath;
	private String fileName;
	private String mapFileName;
	private int horizontalMargin = DEFAULT_HORIZONTAL_MARGIN;
	private int verticalMargin = DEFAULT_VERTICAL_MARGIN;
	private int horizontalSpace = DEFAULT_HORIZONTAL_SPACE;
	private int verticalSpace = DEFAULT_VERTICAL_SPACE;
	private int currentPageIndex = 1;
	private Page currentPage = null;
	private Paint typeface = null;
	private int currentX = 0;
	private int currentLineHeight = 0;
	private int currentY = 0;
	private int pageWidth = A4_PAGE_WIDTH;
	private int pageHeight = A4_PAGE_HEIGHT;

	DisplayNameLocalizer dnl = new DisplayNameLocalizer(OpenTenureApplication
			.getInstance().getLocalization());

	public PDFClaimExporter(Context context, Claim claim, boolean asAttachment) {

		try {
			String baseDir = null;
			
			if(asAttachment){
				baseDir = FileSystemUtilities.getAttachmentFolder(claim.getClaimId()).toString();
			}else{
				baseDir = FileSystemUtilities.getCertificatesFolder().toString();
			}

			if (claim.getClaimNumber() != null && !"".equalsIgnoreCase(claim.getClaimNumber())){
				fileName = claim.getClaimNumber() + ".pdf";
			}
			else{
				fileName = claim.getName() + ".pdf";
			}
			
			filePath = baseDir + File.separator + fileName;

			mapFileName = FileSystemUtilities.getAttachmentFolder(claim.getClaimId())
					+ File.separator
					+ EditablePropertyBoundary.DEFAULT_MAP_FILE_NAME;
			document = new PdfDocument();

			addPage(document, context, claim.getClaimId());

			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:MM", Locale.US);

			/*------------------------------------------- HEADER -------------------------------------*/
			if (!OpenTenureApplication.getInstance().getLocale().toString()
					.startsWith("ar")) {
				drawBitmap(bitmapFromResource(context,
						R.drawable.open_tenure_logo,

						128, 85));

				moveX(15);
				moveY(15);

				if (claim.getClaimNumber() != null) {
					writeBoldText(
							context.getResources().getString(R.string.claim)
									+ " #" + claim.getClaimNumber(), 25);
				} else {
					writeBoldText(
							context.getResources().getString(R.string.claim)
									+ ": " + claim.getName(), 25);
				}

				moveX(45);
				moveY(30);

				String dataStr = sdf.format(new Date());

				int x = currentX;
				setX(450);
				writeText(context.getResources().getString(
						R.string.generated_on)
						+ " :");
				currentX = x;
				moveY(20);
				setX(430);
				writeText(dataStr);
				moveY(-60);
			} else {

				setFont(FONT_SANS_SERIF, Typeface.NORMAL, 15);
				moveY(60);

				writeText(context.getResources().getString(
						R.string.generated_on)
						+ " :");

				setX(40);
				moveY(25);

				String dataStr = sdf.format(new Date());
				writeText(dataStr);

				moveX(15);
				moveY(-80);

				if (claim.getClaimNumber() != null) {
					writeBoldText(
							context.getResources().getString(R.string.claim)
									+ " #" + claim.getClaimNumber(), 25);
				} else {
					writeBoldText(
							context.getResources().getString(R.string.claim)
									+ ": " + claim.getName(), 25);
				}

				setX(420);
				moveY(40);

				drawBitmap(bitmapFromResource(context,
						R.drawable.open_tenure_logo,

						128, 85));

			}

			/*
			 * ----------------------------------------- CLAIMANT
			 * ----------------
			 * --------------------------------------------------
			 * ----------------
			 */
			newLine();
			drawHorizontalLine();
			newLine();

			if (!OpenTenureApplication.getInstance().getLocale().toString()
					.startsWith("ar")) {

				writeBoldText(OpenTenureApplication.getContext().getResources()
						.getString(R.string.claimant_no_star), 18);

				moveY(5);

				Bitmap picture = Person.getPersonPictureForPdf(context, claim
						.getPerson().getPersonId(), 200);

				if (claim.getPerson().getPersonType().equals(Person._PHYSICAL)) {
					if (picture == null) {
						newLine();
						writeBoldText(
								context.getResources().getString(
										R.string.first_name)
										+ " :", 16);
						setX(200);
						writeBoldText(
								context.getResources().getString(
										R.string.last_name)
										+ " :", 16);
						setX(400);
						writeBoldText(
								context.getResources().getString(
										R.string.date_of_birth_simple)
										+ ": ", 16);
					} else {

						setX(330);
						drawBitmap(picture);

						newLine();

						moveY(-125);
						writeBoldText(
								context.getResources().getString(
										R.string.first_name)
										+ " :", 16);

						newLine();

						newLine();
						writeText(claim.getPerson().getFirstName());

						newLine();
						newLine();
						writeBoldText(
								context.getResources().getString(
										R.string.last_name)
										+ " :", 16);

						newLine();
						newLine();
						writeText(claim.getPerson().getLastName());

						newLine();
						newLine();
						writeBoldText(
								context.getResources().getString(
										R.string.date_of_birth_simple)
										+ ": ", 16);
						newLine();
						newLine();
						if (claim.getPerson().getDateOfBirth() != null)
							writeText(sdf.format(claim.getPerson()
									.getDateOfBirth()));

					}

				} else {
					if (picture == null) {

						newLine();
						writeBoldText(
								context.getResources().getString(
										R.string.group_name)
										+ " :", 16);
						setX(300);
						writeBoldText(
								context.getResources().getString(
										R.string.date_of_establishment_label)
										+ " :", 16);

					} else {

						setX(330);
						drawBitmap(picture);

						newLine();
						moveY(-120);
						writeBoldText(
								context.getResources().getString(
										R.string.group_name)
										+ " :", 16);

						newLine();
						newLine();
						newLine();

						writeText(claim.getPerson().getFirstName());

						newLine();
						newLine();
						writeBoldText(
								context.getResources().getString(
										R.string.date_of_establishment_label)
										+ " :", 16);
						newLine();
						newLine();
						newLine();

						if (claim.getPerson().getDateOfBirth() != null)
							writeText(sdf.format(claim.getPerson()
									.getDateOfBirth()));
						newLine();
						newLine();
						newLine();
						newLine();
						newLine();
					}
				}

				newLine();
				newLine();

				if (claim.getPerson().getPersonType().equals(Person._GROUP)) {
					if (picture == null) {
						writeText(claim.getPerson().getFirstName());
						setX(300);
						if (claim.getPerson().getDateOfBirth() != null)
							writeText(sdf.format(claim.getPerson()
									.getDateOfBirth()));
					}
				} else {
					if (picture == null) {
						writeText(claim.getPerson().getFirstName());
						setX(200);
						writeText(claim.getPerson().getLastName());
						setX(400);
						if (claim.getPerson().getDateOfBirth() != null)
							writeText(sdf.format(claim.getPerson()
									.getDateOfBirth()));
					}
				}
				newLine();
				newLine();
				writeBoldText(
						context.getResources().getString(
								R.string.postal_address)
								+ ": ", 16);
				setX(300);
				writeBoldText(
						context.getResources().getString(
								R.string.contact_phone_number)
								+ ": ", 16);
				newLine();
				if (claim.getPerson().getPostalAddress() != null)
					writeText(claim.getPerson().getPostalAddress());
				setX(300);
				if (claim.getPerson().getContactPhoneNumber() != null)
					writeText(" " + claim.getPerson().getContactPhoneNumber());
				newLine();
				newLine();

				writeBoldText(context.getResources()
						.getString(R.string.id_type) + ": ", 16);
				setX(300);

				writeBoldText(
						context.getResources().getString(R.string.id_number)
								+ ": ", 16);
				newLine();
				if (claim.getPerson().getIdType() != null)
					writeText(dnl.getLocalizedDisplayName(new IdType()
							.getDisplayValueByType(claim.getPerson()
									.getIdType())));
				setX(300);
				if (claim.getPerson().getIdNumber() != null)
					writeText(claim.getPerson().getIdNumber());
				newLine();
				newLine();
				drawHorizontalLine();
				newLine();

			} else {

				Bitmap picture = Person.getPersonPictureForPdf(context, claim
						.getPerson().getPersonId(), 200);

				setX(430);
				writeBoldText(OpenTenureApplication.getContext().getResources()
						.getString(R.string.claimant_no_star), 18);

				newLine();

				if (claim.getPerson().getPersonType().equals(Person._PHYSICAL)) {
					if (picture == null) {

						newLine();
						setX(430);
						writeBoldText(
								context.getResources().getString(
										R.string.first_name)
										+ " :", 16);
						setX(250);
						writeBoldText(
								context.getResources().getString(
										R.string.last_name)
										+ " :", 16);
						setX(70);
						writeBoldText(
								context.getResources().getString(
										R.string.date_of_birth_simple)
										+ ": ", 16);
					} else {
						setX(40);
						drawBitmap(picture);

						newLine();
						setX(430);
						moveY(-135);
						writeBoldText(
								context.getResources().getString(
										R.string.first_name)
										+ " :", 16);

						newLine();
						newLine();
						setX(430);
						writeText(claim.getPerson().getFirstName());

						newLine();
						newLine();
						setX(430);
						writeBoldText(
								context.getResources().getString(
										R.string.last_name)
										+ " :", 16);

						newLine();
						newLine();
						setX(430);
						writeText(claim.getPerson().getLastName());

						newLine();
						newLine();
						setX(430);
						writeBoldText(
								context.getResources().getString(
										R.string.date_of_birth_simple)
										+ ": ", 16);
						newLine();
						newLine();
						setX(430);
						if (claim.getPerson().getDateOfBirth() != null)
							writeText(sdf.format(claim.getPerson()
									.getDateOfBirth()));

					}
				} else {
					if (picture == null) {
						newLine();
						setX(430);
						writeBoldText(
								context.getResources().getString(
										R.string.group_name)
										+ " :", 16);
						setX(130);
						writeBoldText(
								context.getResources().getString(
										R.string.date_of_establishment_label)
										+ " :", 16);
					} else {

						setX(40);
						drawBitmap(picture);

						newLine();
						setX(430);
						moveY(-120);
						writeBoldText(
								context.getResources().getString(
										R.string.group_name)
										+ " :", 16);

						newLine();
						newLine();
						newLine();
						setX(430);

						writeText(claim.getPerson().getFirstName());

						newLine();
						newLine();
						setX(430);
						writeBoldText(
								context.getResources().getString(
										R.string.date_of_establishment_label)
										+ " :", 16);
						newLine();
						newLine();
						newLine();
						setX(430);

						if (claim.getPerson().getDateOfBirth() != null)
							writeText(sdf.format(claim.getPerson()
									.getDateOfBirth()));
						newLine();
						newLine();
						newLine();
						newLine();
						newLine();

					}
				}

				newLine();

				if (picture == null) {
					if (claim.getPerson().getPersonType().equals(Person._GROUP)) {
						setX(430);
						writeText(claim.getPerson().getFirstName());
						setX(130);
						if (claim.getPerson().getDateOfBirth() != null)
							writeText(sdf.format(claim.getPerson()
									.getDateOfBirth()));
					} else {

						setX(430);
						writeText(claim.getPerson().getFirstName());
						setX(250);
						writeText(claim.getPerson().getLastName());
						setX(70);
						if (claim.getPerson().getDateOfBirth() != null)
							writeText(sdf.format(claim.getPerson()
									.getDateOfBirth()));

					}
				}

				newLine();
				newLine();
				setX(430);
				writeBoldText(
						context.getResources().getString(
								R.string.postal_address)
								+ ": ", 16);
				setX(130);
				writeBoldText(
						context.getResources().getString(
								R.string.contact_phone_number)
								+ ": ", 16);
				newLine();
				newLine();
				setX(430);
				if (claim.getPerson().getPostalAddress() != null)
					writeText(claim.getPerson().getPostalAddress());
				setX(130);
				if (claim.getPerson().getContactPhoneNumber() != null)
					writeText(" " + claim.getPerson().getContactPhoneNumber());
				newLine();
				newLine();
				setX(430);
				writeBoldText(context.getResources()
						.getString(R.string.id_type) + ": ", 16);
				setX(130);

				writeBoldText(
						context.getResources().getString(R.string.id_number)
								+ ": ", 16);
				newLine();
				newLine();
				setX(430);
				if (claim.getPerson().getIdType() != null)
					writeText(dnl.getLocalizedDisplayName(new IdType()
							.getDisplayValueByType(claim.getPerson()
									.getIdType())));
				setX(130);
				if (claim.getPerson().getIdNumber() != null)
					writeText(claim.getPerson().getIdNumber());
				newLine();
				newLine();
				drawHorizontalLine();
				newLine();

			}
			/*---------------------------------------------- OWNERS ------------------------------------------------------ */

			if (OpenTenureApplication.getInstance().getLocale().toString()
					.startsWith("ar"))
				setX(430);
			writeBoldText(context.getResources().getString(R.string.owners), 16);

			List<ShareProperty> shares = claim.getShares();
			int i = 0;
			for (Iterator<ShareProperty> iterator = shares.iterator(); iterator.hasNext();) {
				if (isPageEnding())
					addPage(document, context, claim.getClaimId());

				++i;
				newLine();
				newLine();
				newLine();
				newLine();
				drawHorizontalLine();
				newLine();
				if (!OpenTenureApplication.getInstance().getLocale().toString()
						.startsWith("ar")) {
					ShareProperty shareProperty = (ShareProperty) iterator
							.next();

					writeBoldText(
							context.getResources().getString(
									R.string.title_share)
									+ " "
									+ i
									+ " :"
									+ shareProperty.getShares() + " %", 16);
					newLine();
					List<Owner> owners = Owner.getOwners(shareProperty.getId());
					for (Iterator<Owner> iterator2 = owners.iterator(); iterator2
							.hasNext();) {

						if (isPageEnding())
							addPage(document, context, claim.getClaimId());

						Owner owner = (Owner) iterator2.next();
						Person person = Person.getPerson(owner.getPersonId());

						if (person.getPersonType().equals(Person._PHYSICAL)) {
							newLine();
							writeBoldText(
									context.getResources().getString(
											R.string.first_name)
											+ " :", 16);
							setX(200);
							writeBoldText(
									context.getResources().getString(
											R.string.last_name)
											+ " :", 16);
							setX(400);
							writeBoldText(
									context.getResources().getString(
											R.string.date_of_birth_simple)
											+ ": ", 16);
						} else {
							newLine();
							writeBoldText(
									context.getResources().getString(
											R.string.group_name)
											+ " :", 16);
							setX(300);
							writeBoldText(
									context.getResources()
											.getString(
													R.string.date_of_establishment_label)
											+ " :", 16);
						}

						newLine();

						if (person.getPersonType().equals(Person._GROUP)) {

							writeText(person.getFirstName());
							setX(300);
							if (person.getDateOfBirth() != null)
								writeText(sdf.format(claim.getPerson()
										.getDateOfBirth()));
						} else {
							writeText(person.getFirstName());
							setX(200);
							writeText(person.getLastName());
							setX(400);
							if (person.getDateOfBirth() != null)
								writeText(sdf.format(claim.getPerson()
										.getDateOfBirth()));

						}
						newLine();
						newLine();
						writeBoldText(
								context.getResources().getString(
										R.string.postal_address)
										+ ": ", 16);
						setX(300);
						writeBoldText(
								context.getResources().getString(
										R.string.contact_phone_number)
										+ ": ", 16);
						newLine();
						if (person.getPostalAddress() != null)
							writeText(claim.getPerson().getPostalAddress());
						setX(300);

						if (person.getContactPhoneNumber() != null)
							writeText(" "
									+ claim.getPerson().getContactPhoneNumber());
						newLine();
						newLine();

						writeBoldText(
								context.getResources().getString(
										R.string.id_type)
										+ ": ", 16);
						setX(300);

						writeBoldText(
								context.getResources().getString(
										R.string.id_number)
										+ ": ", 16);
						newLine();
						if (person.getIdType() != null)
							writeText(dnl.getLocalizedDisplayName(new IdType()
									.getDisplayValueByType(claim.getPerson()
											.getIdType())));

						setX(300);
						if (person.getIdNumber() != null)
							writeText(claim.getPerson().getIdNumber());
						newLine();
						newLine();

					}
				} else {

					ShareProperty shareProperty = (ShareProperty) iterator
							.next();
					setX(430);
					writeBoldText(
							context.getResources().getString(
									R.string.title_share)
									+ " "
									+ i
									+ " :"
									+ shareProperty.getShares() + " %", 16);
					newLine();
					setX(430);
					List<Owner> owners = Owner.getOwners(shareProperty.getId());
					for (Iterator<Owner> iterator2 = owners.iterator(); iterator2
							.hasNext();) {

						if (isPageEnding())
							addPage(document, context, claim.getClaimId());

						Owner owner = (Owner) iterator2.next();
						Person person = Person.getPerson(owner.getPersonId());

						if (person.getPersonType().equals(Person._PHYSICAL)) {
							newLine();
							setX(430);
							writeBoldText(
									context.getResources().getString(
											R.string.first_name)
											+ " :", 16);
							setX(250);
							writeBoldText(
									context.getResources().getString(
											R.string.last_name)
											+ " :", 16);
							setX(70);
							writeBoldText(
									context.getResources().getString(
											R.string.date_of_birth_simple)
											+ ": ", 16);
						} else {
							newLine();
							setX(430);
							writeBoldText(
									context.getResources().getString(
											R.string.group_name)
											+ " :", 16);
							setX(130);
							writeBoldText(
									context.getResources()
											.getString(
													R.string.date_of_establishment_label)
											+ " :", 16);
						}

						newLine();
						setX(430);
						if (person.getPersonType().equals(Person._GROUP)) {

							writeText(person.getFirstName());
							setX(130);
							if (person.getDateOfBirth() != null)
								writeText(sdf.format(claim.getPerson()
										.getDateOfBirth()));
						} else {
							writeText(person.getFirstName());
							setX(250);
							writeText(person.getLastName());
							setX(70);
							if (person.getDateOfBirth() != null)
								writeText(sdf.format(claim.getPerson()
										.getDateOfBirth()));

						}
						newLine();
						newLine();
						setX(430);
						writeBoldText(
								context.getResources().getString(
										R.string.postal_address)
										+ ": ", 16);
						setX(130);
						writeBoldText(
								context.getResources().getString(
										R.string.contact_phone_number)
										+ ": ", 16);
						newLine();
						setX(430);
						if (person.getPostalAddress() != null)
							writeText(claim.getPerson().getPostalAddress());
						setX(130);

						if (person.getContactPhoneNumber() != null)
							writeText(" "
									+ claim.getPerson().getContactPhoneNumber());
						newLine();
						newLine();
						setX(430);

						writeBoldText(
								context.getResources().getString(
										R.string.id_type)
										+ ": ", 16);
						setX(130);

						writeBoldText(
								context.getResources().getString(
										R.string.id_number)
										+ ": ", 16);
						newLine();
						setX(430);
						if (person.getIdType() != null)
							writeText(dnl.getLocalizedDisplayName(new IdType()
									.getDisplayValueByType(claim.getPerson()
											.getIdType())));

						setX(130);
						if (person.getIdNumber() != null)
							writeText(claim.getPerson().getIdNumber());
						newLine();
						newLine();

					}
				}
			}

			/*------------------     DOCUMENTS -------------------------------------*/
			if (!OpenTenureApplication.getInstance().getLocale().toString()
					.startsWith("ar")) {
				newLine();
				if (isPageEnding())
					addPage(document, context, claim.getClaimId());
				drawHorizontalLine();
				newLine();
				newLine();
				writeBoldText(
						context.getResources().getString(
								R.string.title_claim_documents), 18);
				newLine();
				writeBoldText(context.getResources().getString(R.string.type),
						16);
				setX(300);
				writeBoldText(
						context.getResources().getString(R.string.description),
						16);
				newLine();
				List<Attachment> attachments = claim.getAttachments();

				for (Iterator<Attachment> iterator = attachments.iterator(); iterator
						.hasNext();) {
					Attachment attachment = (Attachment) iterator.next();

					writeText(dnl.getLocalizedDisplayName((new DocumentType())
							.getDisplayVauebyType(attachment.getFileType())));

					setX(300);
					writeText(attachment.getDescription());
					newLine();
					newLine();
				}
			} else {

				newLine();

				if (isPageEnding())
					addPage(document, context, claim.getClaimId());
				drawHorizontalLine();
				newLine();
				newLine();
				setX(430);
				writeBoldText(
						context.getResources().getString(
								R.string.title_claim_documents), 18);
				newLine();
				newLine();
				setX(430);
				writeBoldText(context.getResources().getString(R.string.type),
						16);
				setX(130);
				writeBoldText(
						context.getResources().getString(R.string.description),
						16);
				newLine();
				setX(430);
				List<Attachment> attachments = claim.getAttachments();

				for (Iterator<Attachment> iterator = attachments.iterator(); iterator
						.hasNext();) {
					Attachment attachment = (Attachment) iterator.next();

					writeText(dnl.getLocalizedDisplayName((new DocumentType())
							.getDisplayVauebyType(attachment.getFileType())));

					setX(130);
					writeText(attachment.getDescription());
					newLine();
					newLine();
					setX(430);

				}

			}

			/*------------------ ADDITIONAL INFO -------------------------------------*/
			newLine();
			drawHorizontalLine();
			if (isPageEnding())
				addPage(document, context, claim.getClaimId());
			if (!OpenTenureApplication.getInstance().getLocale().toString()
					.startsWith("ar")) {

				newLine();
				newLine();
				newLine();
				writeBoldText(
						context.getResources().getString(R.string.claim_notes),
						18);
				newLine();
				newLine();
				newLine();
				writeText(claim.getNotes());
				if (isPageEnding())
					addPage(document, context, claim.getClaimId());
			} else {

				newLine();
				newLine();
				newLine();
				setX(430);
				writeBoldText(
						context.getResources().getString(R.string.claim_notes),
						18);
				newLine();
				newLine();
				newLine();
				setX(430);
				writeText(claim.getNotes());
				if (isPageEnding())
					addPage(document, context, claim.getClaimId());
			}
			/*------------------ PARCEL -------------------------------------*/
			newLine();
			drawHorizontalLine();

			if (isPageEnding())
				addPage(document, context, claim.getClaimId());

			if (!OpenTenureApplication.getInstance().getLocale().toString()
					.startsWith("ar")) {
				newLine();
				newLine();
				newLine();
				writeBoldText(
						context.getResources().getString(R.string.parcel), 18);
				newLine();
				newLine();
				writeBoldText(
						context.getResources().getString(
								R.string.claim_area_label), 16);
				setX(300);
				writeBoldText(
						context.getResources().getString(
								R.string.claim_type_no_star), 16);
				newLine();
				writeText(claim.getClaimArea()
						+ " "
						+ context.getResources().getString(
								R.string.square_meters));
				setX(300);
				writeText(dnl.getLocalizedDisplayName(new ClaimType()
						.getDisplayValueByType(claim.getType())));
				newLine();
				writeBoldText(
						context.getResources().getString(R.string.land_use), 16);
				setX(300);
				writeBoldText(
						context.getResources().getString(
								R.string.date_of_start_label_print), 16);

				newLine();
				writeText(dnl.getLocalizedDisplayName(new LandUse()
						.getDisplayValueByType(claim.getLandUse())));
				setX(300);
				sdf.applyPattern("dd/MM/yyyy");
				if (claim.getDateOfStart() != null)
					writeText(sdf.format(claim.getDateOfStart()));
			} else {

				newLine();
				newLine();
				newLine();
				setX(430);
				writeBoldText(
						context.getResources().getString(R.string.parcel), 18);
				newLine();
				newLine();
				setX(430);
				writeBoldText(
						context.getResources().getString(
								R.string.claim_area_label), 16);
				setX(130);
				writeBoldText(
						context.getResources().getString(
								R.string.claim_type_no_star), 16);
				newLine();
				setX(430);
				writeText(claim.getClaimArea()
						+ " "
						+ context.getResources().getString(
								R.string.square_meters));
				setX(130);
				writeText(dnl.getLocalizedDisplayName(new ClaimType()
						.getDisplayValueByType(claim.getType())));
				newLine();
				setX(430);
				writeBoldText(
						context.getResources().getString(R.string.land_use), 16);
				setX(130);
				writeBoldText(
						context.getResources().getString(
								R.string.date_of_start_label_print), 16);

				newLine();
				setX(430);
				writeText(dnl.getLocalizedDisplayName(new LandUse()
						.getDisplayValueByType(claim.getLandUse())));
				setX(130);
				sdf.applyPattern("dd/MM/yyyy");
				if (claim.getDateOfStart() != null)
					writeText(sdf.format(claim.getDateOfStart()));

			}
			newLine();
			newLine();
			drawHorizontalLine();
			// ---------------------------------------------------- Adjacent
			// claims section -------------------------//

			newLine();
			newLine();
			newLine();

			if (isPageEnding())
				addPage(document, context, claim.getClaimId());

			if (!OpenTenureApplication.getInstance().getLocale().toString()
					.startsWith("ar")) {
				writeBoldText(
						context.getResources().getString(
								R.string.adjacent_claims), 18);
				newLine();
				List<Adjacency> adjList = Adjacency.getAdjacencies(claim.getClaimId());
				for (Adjacency adj : adjList) {
					Claim adjacentClaim;
					String direction;
					if (adj.getSourceClaimId().equalsIgnoreCase(claim.getClaimId())) {
						adjacentClaim = Claim.getClaim(adj.getDestClaimId());
						direction = Adjacency.getCardinalDirection(context,
								adj.getCardinalDirection());
					} else {
						adjacentClaim = Claim.getClaim(adj.getSourceClaimId());
						direction = Adjacency.getCardinalDirection(context,
								Adjacency.getReverseCardinalDirection(adj
										.getCardinalDirection()));
					}
					newLine();
					newLine();
					writeText(context.getResources().getString(
							R.string.cardinal_direction)
							+ ": "
							+ direction
							+ ", "
							+ context.getResources().getString(
									R.string.property)
							+ ": "
							+ adjacentClaim.getName()
							+ ", "
							+ context.getResources().getString(R.string.by)
							+ ": "
							+ adjacentClaim.getPerson().getFirstName()
							+ " " + adjacentClaim.getPerson().getLastName());
				}
				newLine();
				newLine();
				newLine();
				drawHorizontalLine();
				if (isPageEnding())
					addPage(document, context, claim.getClaimId());
				newLine();
				newLine();
				newLine();
				newLine();
				writeBoldText(
						context.getResources().getString(
								R.string.adjacent_properties), 18);
				newLine();
				newLine();
				newLine();
				writeBoldText(context.getResources().getString(R.string.north),
						16);
				setX(300);
				writeBoldText(context.getResources().getString(R.string.south),
						16);
				newLine();
				newLine();

				AdjacenciesNotes adNotes = AdjacenciesNotes
						.getAdjacenciesNotes(claim.getClaimId());

				if (adNotes != null) {
					writeText(adNotes.getNorthAdjacency());
					setX(300);
					writeText(adNotes.getSouthAdjacency());
				}
				newLine();
				newLine();
				newLine();

				writeBoldText(context.getResources().getString(R.string.east),
						16);
				setX(300);
				writeBoldText(context.getResources().getString(R.string.west),
						16);
				newLine();
				newLine();
				if (adNotes != null) {
					writeText(adNotes.getEastAdjacency());
					setX(300);
					writeText(adNotes.getWestAdjacency());
				}
			} else {

				setX(430);
				writeBoldText(
						context.getResources().getString(
								R.string.adjacent_claims), 18);
				newLine();
				setX(430);

				List<Adjacency> adjList = Adjacency.getAdjacencies(claim.getClaimId());
				for (Adjacency adj : adjList) {
					Claim adjacentClaim;
					String direction;
					if (adj.getSourceClaimId().equalsIgnoreCase(claim.getClaimId())) {
						adjacentClaim = Claim.getClaim(adj.getDestClaimId());
						direction = Adjacency.getCardinalDirection(context,
								adj.getCardinalDirection());
					} else {
						adjacentClaim = Claim.getClaim(adj.getSourceClaimId());
						direction = Adjacency.getCardinalDirection(context,
								Adjacency.getReverseCardinalDirection(adj
										.getCardinalDirection()));
					}
					newLine();
					newLine();
					setX(130);
					writeText(context.getResources().getString(
							R.string.cardinal_direction)
							+ ": "
							+ direction
							+ ", "
							+ context.getResources().getString(
									R.string.property)
							+ ": "
							+ adjacentClaim.getName()
							+ ", "
							+ context.getResources().getString(R.string.by)
							+ ": "
							+ adjacentClaim.getPerson().getFirstName()
							+ " " + adjacentClaim.getPerson().getLastName());
				}
				newLine();
				newLine();
				newLine();

				drawHorizontalLine();
				if (isPageEnding())
					addPage(document, context, claim.getClaimId());
				newLine();
				newLine();
				newLine();
				newLine();
				setX(430);
				writeBoldText(
						context.getResources().getString(
								R.string.adjacent_properties), 18);
				newLine();
				newLine();
				newLine();
				setX(430);
				writeBoldText(context.getResources().getString(R.string.north),
						16);
				setX(130);
				writeBoldText(context.getResources().getString(R.string.south),
						16);
				newLine();
				newLine();
				setX(430);

				AdjacenciesNotes adNotes = AdjacenciesNotes
						.getAdjacenciesNotes(claim.getClaimId());

				if (adNotes != null) {
					writeText(adNotes.getNorthAdjacency());
					setX(130);
					writeText(adNotes.getSouthAdjacency());
				}
				newLine();
				newLine();
				newLine();
				setX(430);

				writeBoldText(context.getResources().getString(R.string.east),
						16);
				setX(130);
				writeBoldText(context.getResources().getString(R.string.west),
						16);
				newLine();
				newLine();
				setX(430);
				if (adNotes != null) {
					writeText(adNotes.getEastAdjacency());
					setX(130);
					writeText(adNotes.getWestAdjacency());
				}

			}

			// ----------------------------------Dynamic Data
			// ----------------------------------------------- //

			if (!OpenTenureApplication.getInstance().getLocale().toString()
					.startsWith("ar")) {
				FormPayload formPayload = claim.getDynamicForm();

				List<SectionPayload> list = formPayload.getSectionPayloadList();

				for (Iterator<SectionPayload> iterator = list.iterator(); iterator.hasNext();) {
					SectionPayload sectionPayload = (SectionPayload) iterator
							.next();

					addPage(document, context, claim.getClaimId());
					newLine();
					drawHorizontalLine();
					newLine();
					newLine();

					writeBoldText(
							context.getResources().getString(
									R.string.title_section), 18);
					writeBoldText(
							" : "
									+ dnl.getLocalizedDisplayName(sectionPayload
											.getDisplayName()), 18);

					newLine();
					newLine();
					newLine();
					newLine();

					List<SectionElementPayload> sePayload = sectionPayload
							.getSectionElementPayloadList();

					for (Iterator<SectionElementPayload> iterator2 = sePayload.iterator(); iterator2
							.hasNext();) {
						SectionElementPayload sectionElementPayload = (SectionElementPayload) iterator2
								.next();
						List<FieldPayload> fieldPayloadList = sectionElementPayload
								.getFieldPayloadList();

						int x = horizontalMargin;

						for (Iterator<FieldPayload> iterator3 = fieldPayloadList.iterator(); iterator3
								.hasNext();) {
							FieldPayload fieldPayload = (FieldPayload) iterator3
									.next();

							String load = null;

							switch (fieldPayload.getFieldType()) {
							case DATE:

								setX(x);
								writeBoldText(
										dnl.getLocalizedDisplayName(fieldPayload
												.getDisplayName())
												+ " :", 16);
								load = fieldPayload.getStringPayload();
								newLine();
								if (load != null) {
									setX(x);
									writeText(dnl.getLocalizedDisplayName(load));

								} else
									newLine();
								if (x == 300) {

									newLine();
									newLine();
									newLine();
									newLine();

								} else {

									if (fieldPayload.getStringPayload() != null)
										moveY(-20);
									else
										moveY(-25);
								}

								break;
							case TIME:

								setX(x);
								writeBoldText(
										dnl.getLocalizedDisplayName(fieldPayload
												.getDisplayName())
												+ " :", 16);
								load = fieldPayload.getStringPayload();
								newLine();
								if (load != null) {
									setX(x);
									writeText(dnl.getLocalizedDisplayName(load));
								} else
									newLine();
								if (x == 300) {

									newLine();
									newLine();
									newLine();
									newLine();
								} else {
									if (fieldPayload.getStringPayload() != null)
										moveY(-20);
									else
										moveY(-25);
								}
								break;
							case SNAPSHOT:
							case DOCUMENT:
							case GEOMETRY:
							case TEXT:

								setX(x);
								writeBoldText(
										dnl.getLocalizedDisplayName(fieldPayload
												.getDisplayName())
												+ " :", 16);
								load = fieldPayload.getStringPayload();
								newLine();
								if (load != null) {
									setX(x);
									writeText(dnl.getLocalizedDisplayName(load));
								} else
									newLine();
								if (x == 300) {

									newLine();
									newLine();
									newLine();
									newLine();
								} else {
									if (fieldPayload.getStringPayload() != null)
										moveY(-20);
									else
										moveY(-25);
								}
								break;
							case DECIMAL:

								setX(x);
								writeBoldText(
										dnl.getLocalizedDisplayName(fieldPayload
												.getDisplayName())
												+ " :", 16);

								newLine();
								if (fieldPayload.getBigDecimalPayload() != null) {
									setX(x);
									writeText(fieldPayload
											.getBigDecimalPayload() + "");
								} else
									newLine();
								if (x == 300) {

									newLine();
									newLine();
									newLine();
									newLine();
								} else {
									if (fieldPayload.getBigDecimalPayload() != null)
										moveY(-20);
									else
										moveY(-25);
								}
								break;
							case INTEGER:

								setX(x);
								writeBoldText(
										dnl.getLocalizedDisplayName(fieldPayload
												.getDisplayName())
												+ " :", 16);
								;
								newLine();
								if (fieldPayload.getBigDecimalPayload() != null) {
									setX(x);
									writeText(fieldPayload
											.getBigDecimalPayload() + "");
								} else
									newLine();
								if (x == 300) {

									newLine();
									newLine();
									newLine();
									newLine();
								} else {
									if (fieldPayload.getBigDecimalPayload() != null)
										moveY(-20);
									else
										moveY(-25);
								}
								break;
							case BOOL:

								writeBoldText(
										dnl.getLocalizedDisplayName(fieldPayload
												.getDisplayName())
												+ " :", 16);

								newLine();
								if (fieldPayload.getBooleanPayload() != null) {
									setX(x);
									writeText(fieldPayload.getBooleanPayload()
											+ "");
								} else
									newLine();
								if (x == 300) {

									newLine();
									newLine();
									newLine();
									newLine();
								} else {
									if (fieldPayload.getBooleanPayload() != null)
										moveY(-20);
									else
										moveY(-25);
								}
								break;
							default:
								break;
							}

							if (x == horizontalMargin)
								x = 300;
							else
								x = horizontalMargin;

						}

						newLine();
						newLine();
						newLine();
						newLine();
						newLine();
						newLine();
						newLine();
						newLine();
						drawHorizontalLine();

						newLine();
						newLine();

						if (isPageEnding())
							addPage(document, context, claim.getClaimId());

						newLine();
						newLine();
					}

				}

			} else {

				FormPayload payLoad = claim.getDynamicForm();
				List<SectionPayload> list = payLoad.getSectionPayloadList();

				for (Iterator<SectionPayload> iterator = list.iterator(); iterator.hasNext();) {
					SectionPayload sectionPayload = (SectionPayload) iterator
							.next();

					addPage(document, context, claim.getClaimId());

					newLine();
					drawHorizontalLine();
					newLine();
					newLine();

					writeBoldText(
							context.getResources().getString(
									R.string.title_section), 18);
					writeBoldText(
							" : "
									+ dnl.getLocalizedDisplayName(sectionPayload
											.getDisplayName()), 18);

					newLine();
					newLine();
					newLine();
					newLine();

					List<SectionElementPayload> fuffa = sectionPayload
							.getSectionElementPayloadList();

					for (Iterator<SectionElementPayload> iterator2 = fuffa.iterator(); iterator2
							.hasNext();) {
						SectionElementPayload sectionElementPayload = (SectionElementPayload) iterator2
								.next();
						List<FieldPayload> pizzaq = sectionElementPayload
								.getFieldPayloadList();

						int x = horizontalMargin;

						for (Iterator<FieldPayload> iterator3 = pizzaq.iterator(); iterator3
								.hasNext();) {
							FieldPayload fieldPayload = (FieldPayload) iterator3
									.next();

							String load = null;

							switch (fieldPayload.getFieldType()) {
							case DATE:

								setX(x);
								writeBoldText(
										dnl.getLocalizedDisplayName(fieldPayload
												.getDisplayName())
												+ " :", 16);
								load = fieldPayload.getStringPayload();
								newLine();
								if (load != null) {
									setX(x);
									writeText(dnl.getLocalizedDisplayName(load));

								} else
									newLine();
								if (x == 300) {

									newLine();
									newLine();
									newLine();
									newLine();

								} else {

									if (fieldPayload.getStringPayload() != null)
										moveY(-20);
									else
										moveY(-25);
								}

								break;
							case TIME:

								setX(x);
								writeBoldText(
										dnl.getLocalizedDisplayName(fieldPayload
												.getDisplayName())
												+ " :", 16);
								load = fieldPayload.getStringPayload();
								newLine();
								if (load != null) {
									setX(x);
									writeText(dnl.getLocalizedDisplayName(load));
								} else
									newLine();
								if (x == 300) {
									newLine();
									newLine();
									newLine();
									newLine();
								} else {
									if (fieldPayload.getStringPayload() != null)
										moveY(-20);
									else
										moveY(-25);
								}
								break;
							case SNAPSHOT:
							case DOCUMENT:
							case GEOMETRY:
							case TEXT:

								setX(x);
								writeBoldText(
										dnl.getLocalizedDisplayName(fieldPayload
												.getDisplayName())
												+ " :", 16);
								load = fieldPayload.getStringPayload();
								newLine();
								if (load != null) {
									setX(x);
									writeText(dnl.getLocalizedDisplayName(load));
								} else
									newLine();
								if (x == 300) {

									newLine();
									newLine();
									newLine();
									newLine();
								} else {
									if (fieldPayload.getStringPayload() != null)
										moveY(-20);
									else
										moveY(-25);
								}
								break;
							case DECIMAL:

								setX(x);
								writeBoldText(
										dnl.getLocalizedDisplayName(fieldPayload
												.getDisplayName())
												+ " :", 16);

								newLine();
								if (fieldPayload.getBigDecimalPayload() != null) {
									setX(x);
									writeText(fieldPayload
											.getBigDecimalPayload() + "");
								} else
									newLine();
								if (x == 300) {

									newLine();
									newLine();
									newLine();
									newLine();
								} else {
									if (fieldPayload.getBigDecimalPayload() != null)
										moveY(-20);
									else
										moveY(-25);
								}
								break;
							case INTEGER:

								setX(x);
								writeBoldText(
										dnl.getLocalizedDisplayName(fieldPayload
												.getDisplayName())
												+ " :", 16);
								;
								newLine();
								if (fieldPayload.getBigDecimalPayload() != null) {
									setX(x);
									writeText(fieldPayload
											.getBigDecimalPayload() + "");
								} else
									newLine();
								if (x == 300) {

									newLine();
									newLine();
									newLine();
									newLine();
								} else {
									if (fieldPayload.getBigDecimalPayload() != null)
										moveY(-20);
									else
										moveY(-25);
								}
								break;
							case BOOL:

								writeBoldText(
										dnl.getLocalizedDisplayName(fieldPayload
												.getDisplayName())
												+ " :", 16);

								newLine();
								if (fieldPayload.getBooleanPayload() != null) {
									setX(x);
									writeText(fieldPayload.getBooleanPayload()
											+ "");
								} else
									newLine();
								if (x == 300) {

									newLine();
									newLine();
									newLine();
									newLine();
								} else {
									if (fieldPayload.getBooleanPayload() != null)
										moveY(-20);
									else
										moveY(-25);
								}
								break;
							default:
								break;
							}

							if (x == horizontalMargin)
								x = 300;
							else
								x = horizontalMargin;

						}

						newLine();
						newLine();
						newLine();
						newLine();
						newLine();
						newLine();
						newLine();
						newLine();
						drawHorizontalLine();

						newLine();
						newLine();

						if (isPageEnding())
							addPage(document, context, claim.getClaimId());

						newLine();
						newLine();
					}
				}

			}

			// ---------------------------------------------------------------------------------------------
			// MAP screenshot section
			addPage(document, context, claim.getClaimId());
			newLine();
			drawBitmap(getMapPicture(mapFileName, 515));

			/*------------------ SIGNATURE -------------------------------------*/
			if (!OpenTenureApplication.getInstance().getLocale().toString()
					.startsWith("ar")) {
				drawHorizontalLine(pageWidth / 2);
				newLine();
				moveY(80);
				writeBoldText(context.getResources().getString(R.string.date),
						18);
				drawHorizontalLine(pageWidth / 2);
				writeBoldText(
						context.getResources().getString(R.string.signature),
						18);
				drawHorizontalLine(pageWidth - horizontalMargin);
			} else {
				newLine();
				newLine();
				newLine();
				drawHorizontalLine(pageWidth - horizontalMargin);
				moveY(100);
				newLine();
				setX(500);
				writeBoldText(
						context.getResources().getString(R.string.signature),
						18);
				setX(500);
				drawHorizontalLine(380);
				setX(pageWidth / 2);
				writeBoldText(context.getResources().getString(R.string.date),
						18);
				setX(40);
				// drawHorizontalLine(pageWidth - horizontalMargin);

				drawHorizontalLine(pageWidth / 2);

			}
			if (currentPage != null) {
				document.finishPage(currentPage);
			}
			document.writeTo(new FileOutputStream(filePath));
			document.close();

		} catch (Exception e) {
			e.printStackTrace();
			Log.e(this.getClass().getName(),"Error " + e.getMessage() + " writing claim summary to " + filePath);
		}
	}

	public String getFileName() {
		return fileName;
	}

	public String getFilePath() {
		return filePath;
	}

	private void setFont(String fontName, int style, int size) {
		Typeface tf = Typeface.create(fontName, style);
		typeface = new Paint();
		typeface.setTypeface(tf);
		typeface.setAntiAlias(true);
		typeface.setTextSize(size);

	}

	private void writeBoldText(String text, int size) {

		setFont(FONT_SANS_SERIF, Typeface.BOLD, size);
		writeText(text);
		setFont(FONT_SANS_SERIF, Typeface.NORMAL, 15);
	}

	private void writeText(String text) {
		if (text != null) {
			Rect bounds = new Rect();

			typeface.getTextBounds(text, 0, text.length(), bounds);
			currentPage.getCanvas().drawText(text, currentX,
					currentY + (bounds.height() - bounds.bottom), typeface);
			currentX += bounds.width() + horizontalSpace;
			currentLineHeight = Math.max(currentLineHeight, bounds.height());
		}
	}

	private void newLine() {
		currentX = horizontalMargin;
		currentY += currentLineHeight + verticalSpace;
		currentLineHeight = 0;
	}

	private void drawBitmap(Bitmap bmp) {
		currentPage.getCanvas().drawBitmap(bmp, currentX, currentY, null);
		currentX += bmp.getWidth() + horizontalSpace;
		currentLineHeight = Math.max(currentLineHeight, bmp.getHeight());
	}

	private void drawHorizontalLine() {
		currentPage.getCanvas().drawLine(currentX,
				currentY + currentLineHeight, pageWidth - horizontalMargin,
				currentY + currentLineHeight, typeface);
		currentX = pageWidth - horizontalMargin;

	}

	private void drawHorizontalLine(int to) {
		currentPage.getCanvas().drawLine(currentX,
				currentY + currentLineHeight, to, currentY + currentLineHeight,
				typeface);
		currentX = to + horizontalSpace;

	}

	private void moveX(int space) {

		currentX += space;

	}

	private void setX(int x) {

		currentX = x;

	}

	private void moveY(int space) {

		currentY += space;

	}

	private void addPage(PdfDocument document, Context context, String claimId) {

		if (currentPage != null) {
			document.finishPage(currentPage);
		}
		// crate a page description
		PageInfo pageInfo = new PageInfo.Builder(pageWidth, pageHeight,
				currentPageIndex++).create();

		// start a page
		currentPage = document.startPage(pageInfo);
		currentX = horizontalMargin;
		currentY = verticalMargin;

	}

	public boolean isPageEnding() {

		if ((pageHeight - currentY) < 150)
			return true;

		return false;
	}

	private Bitmap bitmapFromResource(Context context, int resId, int width,
			int height) {
		try {
			Bitmap resource = BitmapFactory.decodeResource(
					context.getResources(), resId);
			return Bitmap.createScaledBitmap(resource, width, height, false);
		} catch (Exception e) {
		}
		return null;
	}

	private Bitmap getMapPicture(String fileName, int size) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;

		Bitmap bitmap = BitmapFactory.decodeFile(fileName);

		int height = bitmap.getHeight();
		int width = bitmap.getWidth();
		int startOffset = 0;

		Bitmap croppedBitmap = null;

		if (height > width) {
			// Portrait
			startOffset = (height - width) / 2;
			croppedBitmap = Bitmap.createBitmap(bitmap, 0, startOffset, width,
					width);
		} else {
			// Landscape
			startOffset = (width - height) / 2;
			croppedBitmap = Bitmap.createBitmap(bitmap, startOffset, 0, height,
					height);
		}
		return Bitmap.createScaledBitmap(croppedBitmap, size, size, true);
	}

	public void addAsAttachment(Context context, String claimId){
		try {
			Claim claim = Claim.getClaim(claimId);
			for (Attachment att : claim.getAttachments()) {
				if (att.getFileName().equals(fileName)) {
					att.delete();
				}
			}
			Attachment att = new Attachment();
			att.setClaimId(claimId);
			att.setDescription(claim.getName());
			att.setFileName(fileName);
			att.setFileType(DEFAULT_CERTIFICATE_DOCUMENT_TYPE);						
			att.setMimeType(DEFAULT_CERTIFICATE_MIME_TYPE);
			att.setMD5Sum(MD5.calculateMD5(new File(filePath)));
			att.setPath(filePath);
			att.setSize(new File(filePath).length());
			att.create();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}