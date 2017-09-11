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
import java.util.Arrays;
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

	public enum PageType{A4,LETTER};
	private static final int A4_PAGE_WIDTH = 595;
	private static final int A4_PAGE_HEIGHT = 842;
	private static final int LETTER_PAGE_WIDTH = 612;
	private static final int LETTER_PAGE_HEIGHT = 792;
	private static final int APPROX_ONE_INCH = 75;
	private static final int DEFAULT_HORIZONTAL_MARGIN = 40;
	private static final int DEFAULT_VERTICAL_MARGIN = 40;
	private static final int DEFAULT_VERTICAL_SPACE = 5;
	private static final int DEFAULT_HORIZONTAL_SPACE = 5;
    private static final int NORMAL_TYPEFACE_SIZE = 15;
    private static final int ARABIC_LINE_START = 430;
    private static final int ARABIC_MID_PAGE = 130;
	private static final String FONT_SANS_SERIF = "sans-serif";
	private static final String DEFAULT_CERTIFICATE_MIME_TYPE = "application/pdf";
	public static final String DEFAULT_CERTIFICATE_DOCUMENT_TYPE = "claimSummary";

	private String filePath;
	private String fileName;
	private int horizontalMargin = DEFAULT_HORIZONTAL_MARGIN;
	private int horizontalSpace = DEFAULT_HORIZONTAL_SPACE;
	private int currentPageIndex = 1;
	private Page currentPage = null;
	private Paint typeface = null;
	private int currentX = 0;
	private int currentLineHeight = 0;
	private int currentY = 0;
	private int pageWidth;
	private int pageHeight;

	public PDFClaimExporter(Context context, Claim claim, boolean asAttachment) {
		this(context, claim, asAttachment, PageType.A4);
	}

	public PDFClaimExporter(Context context, Claim claim, boolean asAttachment, PageType pageType) {


		if(pageType == PageType.LETTER){
			pageWidth = LETTER_PAGE_WIDTH;
			pageHeight = LETTER_PAGE_HEIGHT;
		}else{
			pageWidth = A4_PAGE_WIDTH;
			pageHeight = A4_PAGE_HEIGHT;
		}

        int midPage = pageWidth/2;
		PdfDocument document;
		String mapFileName;
		DisplayNameLocalizer dnl = new DisplayNameLocalizer(OpenTenureApplication
				.getInstance().getLocalization());
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
				sdf.applyPattern("dd/MM/yyyy HH:MM");
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

				sdf.applyPattern("dd/MM/yyyy HH:MM");
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

						newLine(2);
						writeText(claim.getPerson().getFirstName());

						newLine(2);
						writeBoldText(
								context.getResources().getString(
										R.string.last_name)
										+ " :", 16);

						newLine(2);
						writeText(claim.getPerson().getLastName());

						newLine(2);
						writeBoldText(
								context.getResources().getString(
										R.string.date_of_birth_simple)
										+ ": ", 16);
						newLine(2);
						if (claim.getPerson().getDateOfBirth() != null){
							sdf.applyPattern("dd/MM/yyyy");
							writeText(sdf.format(claim.getPerson()
									.getDateOfBirth()));
						}

					}

				} else {
					if (picture == null) {

						newLine();
						writeBoldText(
								context.getResources().getString(
										R.string.group_name)
										+ " :", 16);
						setX(midPage);
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

						newLine(3);
						writeText(claim.getPerson().getFirstName());

						newLine(2);
						writeBoldText(
								context.getResources().getString(
										R.string.date_of_establishment_label)
										+ " :", 16);
						newLine(3);

						if (claim.getPerson().getDateOfBirth() != null){
							sdf.applyPattern("dd/MM/yyyy");
							writeText(sdf.format(claim.getPerson()
									.getDateOfBirth()));
						}
						newLine(5);
					}
				}

				newLine(2);

				if (claim.getPerson().getPersonType().equals(Person._GROUP)) {
					if (picture == null) {
						writeText(claim.getPerson().getFirstName());
						setX(midPage);
						if (claim.getPerson().getDateOfBirth() != null){
							sdf.applyPattern("dd/MM/yyyy");
							writeText(sdf.format(claim.getPerson()
									.getDateOfBirth()));
						}
					}
				} else {
					if (picture == null) {
						writeText(claim.getPerson().getFirstName());
						setX(200);
						writeText(claim.getPerson().getLastName());
						setX(400);
						if (claim.getPerson().getDateOfBirth() != null){
							sdf.applyPattern("dd/MM/yyyy");
							writeText(sdf.format(claim.getPerson()
									.getDateOfBirth()));
						}
					}
				}
				newLine(2);
				writeBoldText(
						context.getResources().getString(
								R.string.postal_address)
								+ ": ", 16);
				setX(midPage);
				writeBoldText(
						context.getResources().getString(
								R.string.contact_phone_number)
								+ ": ", 16);
				newLine();
				if (claim.getPerson().getPostalAddress() != null)
					writeText(claim.getPerson().getPostalAddress());
				setX(midPage);
				if (claim.getPerson().getContactPhoneNumber() != null)
					writeText(" " + claim.getPerson().getContactPhoneNumber());
				newLine(2);

				writeBoldText(context.getResources()
						.getString(R.string.id_type) + ": ", 16);
				setX(midPage);

				writeBoldText(
						context.getResources().getString(R.string.id_number)
								+ ": ", 16);
				newLine();
				if (claim.getPerson().getIdType() != null)
					writeText(dnl.getLocalizedDisplayName(new IdType()
							.getDisplayValueByType(claim.getPerson()
									.getIdType())));
				setX(midPage);
				if (claim.getPerson().getIdNumber() != null)
					writeText(claim.getPerson().getIdNumber());
				newLine(2);
				drawHorizontalLine();
				newLine();

			} else {

				Bitmap picture = Person.getPersonPictureForPdf(context, claim
						.getPerson().getPersonId(), 200);

				setX(ARABIC_LINE_START);
				writeBoldText(OpenTenureApplication.getContext().getResources()
						.getString(R.string.claimant_no_star), 18);

				newLine();

				if (claim.getPerson().getPersonType().equals(Person._PHYSICAL)) {
					if (picture == null) {

                        newLine();
						writeBoldText(
								context.getResources().getString(
										R.string.first_name)
										+ " :", 16);
						setX(ARABIC_MID_PAGE);
						writeBoldText(
								context.getResources().getString(
										R.string.last_name)
										+ " :", 16);
                        newLine();
						writeBoldText(
								context.getResources().getString(
										R.string.date_of_birth_simple)
										+ ": ", 16);
					} else {
						setX(40);
						drawBitmap(picture);

                        newLine();
						moveY(-135);
						writeBoldText(
								context.getResources().getString(
										R.string.first_name)
										+ " :", 16);

                        newLine(2);
						writeText(claim.getPerson().getFirstName());

                        newLine(2);
						writeBoldText(
								context.getResources().getString(
										R.string.last_name)
										+ " :", 16);

                        newLine(2);
						writeText(claim.getPerson().getLastName());

                        newLine(2);
						writeBoldText(
								context.getResources().getString(
										R.string.date_of_birth_simple)
										+ ": ", 16);
                        newLine(2);
						if (claim.getPerson().getDateOfBirth() != null){
							sdf.applyPattern("dd/MM/yyyy");
							writeText(sdf.format(claim.getPerson()
									.getDateOfBirth()));
						}
					}
				} else {
					if (picture == null) {
                        newLine();
						writeBoldText(
								context.getResources().getString(
										R.string.group_name)
										+ " :", 16);
						setX(ARABIC_MID_PAGE);
						writeBoldText(
								context.getResources().getString(
										R.string.date_of_establishment_label)
										+ " :", 16);
					} else {

						setX(40);
						drawBitmap(picture);

                        newLine();
						moveY(-120);
						writeBoldText(
								context.getResources().getString(
										R.string.group_name)
										+ " :", 16);

                        newLine(3);

						writeText(claim.getPerson().getFirstName());

                        newLine(2);
						writeBoldText(
								context.getResources().getString(
										R.string.date_of_establishment_label)
										+ " :", 16);
                        newLine(3);

						if (claim.getPerson().getDateOfBirth() != null){
							sdf.applyPattern("dd/MM/yyyy");
							writeText(sdf.format(claim.getPerson()
									.getDateOfBirth()));
						}
						newLine(5);
					}
				}

				newLine();

				if (picture == null) {
					if (claim.getPerson().getPersonType().equals(Person._GROUP)) {
						setX(ARABIC_LINE_START);
						writeText(claim.getPerson().getFirstName());
						setX(ARABIC_MID_PAGE);
						if (claim.getPerson().getDateOfBirth() != null){
							sdf.applyPattern("dd/MM/yyyy");
							writeText(sdf.format(claim.getPerson()
									.getDateOfBirth()));
						}
					} else {

						setX(ARABIC_LINE_START);
						writeText(claim.getPerson().getFirstName());
						setX(ARABIC_MID_PAGE);
						writeText(claim.getPerson().getLastName());
                        newLine();
						if (claim.getPerson().getDateOfBirth() != null){
							sdf.applyPattern("dd/MM/yyyy");
							writeText(sdf.format(claim.getPerson()
									.getDateOfBirth()));
						}
					}
				}

                newLine(2);
				writeBoldText(
						context.getResources().getString(
								R.string.postal_address)
								+ ": ", 16);
				setX(ARABIC_MID_PAGE);
				writeBoldText(
						context.getResources().getString(
								R.string.contact_phone_number)
								+ ": ", 16);
                newLine(2);
				if (claim.getPerson().getPostalAddress() != null)
					writeText(claim.getPerson().getPostalAddress());
				setX(ARABIC_MID_PAGE);
				if (claim.getPerson().getContactPhoneNumber() != null)
					writeText(" " + claim.getPerson().getContactPhoneNumber());
                newLine(2);
				writeBoldText(context.getResources()
						.getString(R.string.id_type) + ": ", 16);
				setX(ARABIC_MID_PAGE);

				writeBoldText(
						context.getResources().getString(R.string.id_number)
								+ ": ", 16);
                newLine(2);
				if (claim.getPerson().getIdType() != null)
					writeText(dnl.getLocalizedDisplayName(new IdType()
							.getDisplayValueByType(claim.getPerson()
									.getIdType())));
				setX(ARABIC_MID_PAGE);
				if (claim.getPerson().getIdNumber() != null)
					writeText(claim.getPerson().getIdNumber());
				newLine(2);
				drawHorizontalLine();
				newLine();

			}
			/*---------------------------------------------- OWNERS ------------------------------------------------------ */

			if (OpenTenureApplication.getInstance().getLocale().toString()
					.startsWith("ar"))
				setX(ARABIC_LINE_START);
			writeBoldText(context.getResources().getString(R.string.owners), 16);

			List<ShareProperty> shares = claim.getShares();
			int i = 0;
			for (Iterator<ShareProperty> iterator = shares.iterator(); iterator.hasNext();) {
				if (isPageEnding())
					addPage(document, context, claim.getClaimId());

				++i;
				newLine(4);
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
							setX(midPage);
							writeBoldText(
									context.getResources().getString(
											R.string.last_name)
											+ " :", 16);
							newLine();
							writeText(person.getFirstName());
							setX(midPage);
							writeText(person.getLastName());
							if (person.getDateOfBirth() != null) {
								newLine(2);
								writeBoldText(
										context.getResources().getString(
												R.string.date_of_birth_simple)
												+ ": ", 16);
								newLine();
								sdf.applyPattern("dd/MM/yyyy");
								writeText(sdf.format(claim.getPerson()
										.getDateOfBirth()));
							}
						} else {
							newLine();
							writeBoldText(
									context.getResources().getString(
											R.string.group_name)
											+ " :", 16);
							setX(midPage);
							writeBoldText(
									context.getResources()
											.getString(
													R.string.date_of_establishment_label)
											+ " :", 16);
							newLine();
							writeText(person.getFirstName());
							if (person.getDateOfBirth() != null){
								setX(midPage);
								sdf.applyPattern("dd/MM/yyyy");
								writeText(sdf.format(claim.getPerson()
										.getDateOfBirth()));
							}
						}

						newLine(2);
						writeBoldText(
								context.getResources().getString(
										R.string.postal_address)
										+ ": ", 16);
						setX(midPage);
						writeBoldText(
								context.getResources().getString(
										R.string.contact_phone_number)
										+ ": ", 16);
						newLine();
						if (person.getPostalAddress() != null)
							writeText(claim.getPerson().getPostalAddress());
						setX(midPage);

						if (person.getContactPhoneNumber() != null)
							writeText(" "
									+ claim.getPerson().getContactPhoneNumber());
						newLine(2);

						writeBoldText(
								context.getResources().getString(
										R.string.id_type)
										+ ": ", 16);
						setX(midPage);

						writeBoldText(
								context.getResources().getString(
										R.string.id_number)
										+ ": ", 16);
						newLine();
						if (person.getIdType() != null)
							writeText(dnl.getLocalizedDisplayName(new IdType()
									.getDisplayValueByType(claim.getPerson()
											.getIdType())));

						setX(midPage);
						if (person.getIdNumber() != null)
							writeText(claim.getPerson().getIdNumber());
						newLine(2);

					}
				} else {

					ShareProperty shareProperty = (ShareProperty) iterator
							.next();
					setX(ARABIC_LINE_START);
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
							setX(ARABIC_MID_PAGE);
							writeBoldText(
									context.getResources().getString(
											R.string.last_name)
											+ " :", 16);
                            newLine();
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
							setX(ARABIC_MID_PAGE);
							writeBoldText(
									context.getResources()
											.getString(
													R.string.date_of_establishment_label)
											+ " :", 16);
						}

                        newLine();
						if (person.getPersonType().equals(Person._GROUP)) {

							writeText(person.getFirstName());
							setX(ARABIC_MID_PAGE);
							if (person.getDateOfBirth() != null){
								sdf.applyPattern("dd/MM/yyyy");
								writeText(sdf.format(claim.getPerson()
										.getDateOfBirth()));
							}
						} else {
							writeText(person.getFirstName());
							setX(ARABIC_MID_PAGE);
							writeText(person.getLastName());
                            newLine();
							if (person.getDateOfBirth() != null){
								sdf.applyPattern("dd/MM/yyyy");
								writeText(sdf.format(claim.getPerson()
										.getDateOfBirth()));
							}
						}
                        newLine(2);
						writeBoldText(
								context.getResources().getString(
										R.string.postal_address)
										+ ": ", 16);
						setX(ARABIC_MID_PAGE);
						writeBoldText(
								context.getResources().getString(
										R.string.contact_phone_number)
										+ ": ", 16);
                        newLine();
						if (person.getPostalAddress() != null)
							writeText(claim.getPerson().getPostalAddress());
						setX(ARABIC_MID_PAGE);

						if (person.getContactPhoneNumber() != null)
							writeText(" "
									+ claim.getPerson().getContactPhoneNumber());
                        newLine(2);

						writeBoldText(
								context.getResources().getString(
										R.string.id_type)
										+ ": ", 16);
						setX(ARABIC_MID_PAGE);

						writeBoldText(
								context.getResources().getString(
										R.string.id_number)
										+ ": ", 16);
                        newLine();
						if (person.getIdType() != null)
							writeText(dnl.getLocalizedDisplayName(new IdType()
									.getDisplayValueByType(claim.getPerson()
											.getIdType())));

						setX(ARABIC_MID_PAGE);
						if (person.getIdNumber() != null)
							writeText(claim.getPerson().getIdNumber());
						newLine(2);

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
				newLine(2);
				writeBoldText(
						context.getResources().getString(
								R.string.title_claim_documents), 18);
				newLine();
				writeBoldText(context.getResources().getString(R.string.type),
						16);
				setX(midPage);
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

					setX(midPage);
					writeText(attachment.getDescription());
					newLine(2);
				}
			} else {

				newLine();

				if (isPageEnding())
					addPage(document, context, claim.getClaimId());
				drawHorizontalLine();
                newLine(2);
				writeBoldText(
						context.getResources().getString(
								R.string.title_claim_documents), 18);
                newLine(2);
				writeBoldText(context.getResources().getString(R.string.type),
						16);
				setX(ARABIC_MID_PAGE);
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

					setX(ARABIC_MID_PAGE);
					writeText(attachment.getDescription());
                    newLine(2);

				}

			}

			/*------------------ ADDITIONAL INFO -------------------------------------*/
			newLine();
			drawHorizontalLine();
			if (isPageEnding())
				addPage(document, context, claim.getClaimId());
			if (!OpenTenureApplication.getInstance().getLocale().toString()
					.startsWith("ar")) {

				newLine(3);
				writeBoldText(
						context.getResources().getString(R.string.claim_notes),
						18);
				newLine(3);
				writeText(claim.getNotes());
				if (isPageEnding())
					addPage(document, context, claim.getClaimId());
			} else {

                newLine(3);
				writeBoldText(
						context.getResources().getString(R.string.claim_notes),
						18);
                newLine(3);
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
				newLine(3);
				writeBoldText(
						context.getResources().getString(R.string.parcel), 18);
				newLine(2);
				writeBoldText(
						context.getResources().getString(
								R.string.claim_area_label), 16);
				setX(midPage);
				writeBoldText(
						context.getResources().getString(
								R.string.claim_type_no_star), 16);
				newLine();
				writeText(claim.getClaimArea()
						+ " "
						+ context.getResources().getString(
								R.string.square_meters));
				setX(midPage);
				writeText(dnl.getLocalizedDisplayName(new ClaimType()
						.getDisplayValueByType(claim.getType())));
				newLine();
				writeBoldText(
						context.getResources().getString(R.string.land_use), 16);
				setX(midPage);
				writeBoldText(
						context.getResources().getString(
								R.string.date_of_start_label_print), 16);

				newLine();
				writeText(dnl.getLocalizedDisplayName(new LandUse()
						.getDisplayValueByType(claim.getLandUse())));
				setX(midPage);
				if (claim.getDateOfStart() != null){
					sdf.applyPattern("dd/MM/yyyy");
					writeText(sdf.format(claim.getDateOfStart()));
				}
			} else {

                newLine(3);
				writeBoldText(
						context.getResources().getString(R.string.parcel), 18);
                newLine(2);
				writeBoldText(
						context.getResources().getString(
								R.string.claim_area_label), 16);
				setX(ARABIC_MID_PAGE);
				writeBoldText(
						context.getResources().getString(
								R.string.claim_type_no_star), 16);
                newLine();
				writeText(claim.getClaimArea()
						+ " "
						+ context.getResources().getString(
								R.string.square_meters));
				setX(ARABIC_MID_PAGE);
				writeText(dnl.getLocalizedDisplayName(new ClaimType()
						.getDisplayValueByType(claim.getType())));
                newLine();
				writeBoldText(
						context.getResources().getString(R.string.land_use), 16);
				setX(ARABIC_MID_PAGE);
				writeBoldText(
						context.getResources().getString(
								R.string.date_of_start_label_print), 16);

                newLine();
				writeText(dnl.getLocalizedDisplayName(new LandUse()
						.getDisplayValueByType(claim.getLandUse())));
				setX(ARABIC_MID_PAGE);
				if (claim.getDateOfStart() != null){
					sdf.applyPattern("dd/MM/yyyy");
					writeText(sdf.format(claim.getDateOfStart()));
				}

			}
			newLine(2);
			drawHorizontalLine();
			// ---------------------------------------------------- Adjacent
			// claims section -------------------------//

			newLine(3);

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
					newLine(2);
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
				newLine(3);
				drawHorizontalLine();
				if (isPageEnding())
					addPage(document, context, claim.getClaimId());
				newLine(4);
				writeBoldText(
						context.getResources().getString(
								R.string.adjacent_properties), 18);
				newLine(3);
				writeBoldText(context.getResources().getString(R.string.north),
						16);
				setX(midPage);
				writeBoldText(context.getResources().getString(R.string.south),
						16);
				newLine(2);

				AdjacenciesNotes adNotes = AdjacenciesNotes
						.getAdjacenciesNotes(claim.getClaimId());

				if (adNotes != null) {
					writeText(adNotes.getNorthAdjacency());
					setX(midPage);
					writeText(adNotes.getSouthAdjacency());
				}
				newLine(3);

				writeBoldText(context.getResources().getString(R.string.east),
						16);
				setX(midPage);
				writeBoldText(context.getResources().getString(R.string.west),
						16);
				newLine(2);
				if (adNotes != null) {
					writeText(adNotes.getEastAdjacency());
					setX(midPage);
					writeText(adNotes.getWestAdjacency());
				}
			} else {

				setX(ARABIC_LINE_START);
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
					newLine(2);
					setX(ARABIC_MID_PAGE);
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
				newLine(3);

				drawHorizontalLine();
				if (isPageEnding())
					addPage(document, context, claim.getClaimId());
                newLine(4);
				writeBoldText(
						context.getResources().getString(
								R.string.adjacent_properties), 18);
                newLine(3);
				writeBoldText(context.getResources().getString(R.string.north),
						16);
				setX(ARABIC_MID_PAGE);
				writeBoldText(context.getResources().getString(R.string.south),
						16);
                newLine(2);

				AdjacenciesNotes adNotes = AdjacenciesNotes
						.getAdjacenciesNotes(claim.getClaimId());

				if (adNotes != null) {
					writeText(adNotes.getNorthAdjacency());
					setX(ARABIC_MID_PAGE);
					writeText(adNotes.getSouthAdjacency());
				}
                newLine(3);

				writeBoldText(context.getResources().getString(R.string.east),
						16);
				setX(ARABIC_MID_PAGE);
				writeBoldText(context.getResources().getString(R.string.west),
						16);
                newLine(2);
				if (adNotes != null) {
					writeText(adNotes.getEastAdjacency());
					setX(ARABIC_MID_PAGE);
					writeText(adNotes.getWestAdjacency());
				}

			}

			// ----------------------------------Dynamic Data
			// ----------------------------------------------- //

			if (!OpenTenureApplication.getInstance().getLocale().toString()
					.startsWith("ar")) {
				FormPayload df = claim.getDynamicForm();

				List<SectionPayload> spl = df.getSectionPayloadList();

				for (Iterator<SectionPayload> spli = spl.iterator(); spli.hasNext();) {
					SectionPayload sp = (SectionPayload) spli
							.next();

					addPage(document, context, claim.getClaimId());
					newLine();
					drawHorizontalLine();
					newLine(2);

					writeBoldText(
							context.getResources().getString(
									R.string.title_section), 18);
					writeBoldText(
							" : "
									+ dnl.getLocalizedDisplayName(sp
											.getDisplayName()), 18);

					newLine(4);

					List<SectionElementPayload> sepl = sp
							.getSectionElementPayloadList();

					for (Iterator<SectionElementPayload> sepli = sepl.iterator(); sepli
							.hasNext();) {
						SectionElementPayload sep = (SectionElementPayload) sepli
								.next();
						List<FieldPayload> fpl = sep
								.getFieldPayloadList();

                        int columnStart = horizontalMargin;
                        int columnEnd = midPage;

						for (Iterator<FieldPayload> fpli = fpl.iterator(); fpli
								.hasNext();) {
							FieldPayload fp = (FieldPayload) fpli
									.next();

							String load = null;

							switch (fp.getFieldType()) {
							case DATE:

								setX(columnStart);
								writeLongBoldTextInColumn(
										dnl.getLocalizedDisplayName(fp
												.getDisplayName())
												+ " :", 16, columnStart, columnEnd);
								load = fp.getStringPayload();
								newLine();
								if (load != null) {
									setX(columnStart);
									writeText(dnl.getLocalizedDisplayName(load));

								} else
									newLine();
								if (columnStart == midPage) {

									newLine(4);

								} else {

									if (fp.getStringPayload() != null)
										moveY(-20);
									else
										moveY(-25);
								}

								break;
							case TIME:

								setX(columnStart);
								writeLongBoldTextInColumn(
										dnl.getLocalizedDisplayName(fp
												.getDisplayName())
												+ " :", 16, columnStart, columnEnd);
								load = fp.getStringPayload();
								newLine();
								if (load != null) {
									setX(columnStart);
									writeText(dnl.getLocalizedDisplayName(load));
								} else
									newLine();
								if (columnStart == midPage) {

									newLine(4);
								} else {
									if (fp.getStringPayload() != null)
										moveY(-20);
									else
										moveY(-25);
								}
								break;
							case SNAPSHOT:
							case DOCUMENT:
							case GEOMETRY:
							case TEXT:

								setX(columnStart);
								writeLongBoldTextInColumn(
										dnl.getLocalizedDisplayName(fp
												.getDisplayName())
												+ " :", 16, columnStart, columnEnd);
								load = fp.getStringPayload();
								newLine();
								if (load != null) {
									setX(columnStart);
									writeLongTextInColumn(dnl.getLocalizedDisplayName(load), columnStart, columnEnd);
								} else
									newLine();
								if (columnStart == midPage) {

									newLine(4);
								} else {
									if (fp.getStringPayload() != null)
										moveY(-20);
									else
										moveY(-25);
								}
								break;
							case DECIMAL:

								setX(columnStart);
								writeLongBoldTextInColumn(
										dnl.getLocalizedDisplayName(fp
												.getDisplayName())
												+ " :", 16, columnStart, columnEnd);

								newLine();
								if (fp.getBigDecimalPayload() != null) {
									setX(columnStart);
									writeText(fp
											.getBigDecimalPayload() + "");
								} else
									newLine();
								if (columnStart == midPage) {

									newLine(4);
								} else {
									if (fp.getBigDecimalPayload() != null)
										moveY(-20);
									else
										moveY(-25);
								}
								break;
							case INTEGER:

								setX(columnStart);
								writeLongBoldTextInColumn(
										dnl.getLocalizedDisplayName(fp
												.getDisplayName())
												+ " :", 16, columnStart, columnEnd);
								;
								newLine();
								if (fp.getBigDecimalPayload() != null) {
									setX(columnStart);
									writeText(fp
											.getBigDecimalPayload() + "");
								} else
									newLine();
								if (columnStart == midPage) {

									newLine(4);
								} else {
									if (fp.getBigDecimalPayload() != null)
										moveY(-20);
									else
										moveY(-25);
								}
								break;
							case BOOL:

								setX(columnStart);
								writeLongBoldTextInColumn(
										dnl.getLocalizedDisplayName(fp
												.getDisplayName())
												+ " :", 16, columnStart, columnEnd);

								newLine();
								if (fp.getBooleanPayload() != null) {
									setX(columnStart);
									writeText(fp.getBooleanPayload()
											+ "");
								} else
									newLine();
								if (columnStart == midPage) {

									newLine(4);
								} else {
									if (fp.getBooleanPayload() != null)
										moveY(-20);
									else
										moveY(-25);
								}
								break;
							default:
								break;
							}

							if (columnStart == horizontalMargin){
                                columnStart = midPage;
                                columnEnd = pageWidth - horizontalMargin;
                            }
							else{
                                columnStart = horizontalMargin;
                                columnEnd = midPage;
                            }

						}

						newLine(8);
						drawHorizontalLine();

						newLine(2);

						if (isPageEnding())
							addPage(document, context, claim.getClaimId());

						newLine(2);
					}

				}

			} else {

				FormPayload df = claim.getDynamicForm();
				List<SectionPayload> spl = df.getSectionPayloadList();

				for (Iterator<SectionPayload> spli = spl.iterator(); spli.hasNext();) {
					SectionPayload sp = (SectionPayload) spli
							.next();

					addPage(document, context, claim.getClaimId());

					newLine();
					drawHorizontalLine();
					newLine(2);

					writeBoldText(
							context.getResources().getString(
									R.string.title_section), 18);
					writeBoldText(
							" : "
									+ dnl.getLocalizedDisplayName(sp
											.getDisplayName()), 18);

					newLine(4);

					List<SectionElementPayload> sepl = sp
							.getSectionElementPayloadList();

					for (Iterator<SectionElementPayload> sepli = sepl.iterator(); sepli
							.hasNext();) {
						SectionElementPayload sep = (SectionElementPayload) sepli
								.next();
						List<FieldPayload> fpl = sep
								.getFieldPayloadList();

						int columnStart = horizontalMargin;
                        int columnEnd = midPage;

						for (Iterator<FieldPayload> fpli = fpl.iterator(); fpli
								.hasNext();) {
							FieldPayload fp = (FieldPayload) fpli
									.next();

							String load = null;

							switch (fp.getFieldType()) {
							case DATE:

								setX(columnStart);
								writeLongBoldTextInColumn(
										dnl.getLocalizedDisplayName(fp
												.getDisplayName())
												+ " :", 16, columnStart, columnEnd);
								load = fp.getStringPayload();
								newLine();
								if (load != null) {
									setX(columnStart);
									writeText(dnl.getLocalizedDisplayName(load));

								} else
									newLine();
								if (columnStart == midPage) {

									newLine(4);

								} else {

									if (fp.getStringPayload() != null)
										moveY(-20);
									else
										moveY(-25);
								}

								break;
							case TIME:

								setX(columnStart);
								writeLongBoldTextInColumn(
										dnl.getLocalizedDisplayName(fp
												.getDisplayName())
												+ " :", 16, columnStart, columnEnd);
								load = fp.getStringPayload();
								newLine();
								if (load != null) {
									setX(columnStart);
									writeText(dnl.getLocalizedDisplayName(load));
								} else
									newLine();
								if (columnStart == midPage) {
									newLine(4);
								} else {
									if (fp.getStringPayload() != null)
										moveY(-20);
									else
										moveY(-25);
								}
								break;
							case SNAPSHOT:
							case DOCUMENT:
							case GEOMETRY:
							case TEXT:

								setX(columnStart);
								writeLongBoldTextInColumn(
										dnl.getLocalizedDisplayName(fp
												.getDisplayName())
												+ " :", 16, columnStart, columnEnd);
								load = fp.getStringPayload();
								newLine();
								if (load != null) {
									setX(columnStart);
									writeLongTextInColumn(dnl.getLocalizedDisplayName(load), columnStart, columnEnd);
								} else
									newLine();
								if (columnStart == midPage) {

									newLine(4);
								} else {
									if (fp.getStringPayload() != null)
										moveY(-20);
									else
										moveY(-25);
								}
								break;
							case DECIMAL:

								setX(columnStart);
								writeBoldText(
										dnl.getLocalizedDisplayName(fp
												.getDisplayName())
												+ " :", 16);

								newLine();
								if (fp.getBigDecimalPayload() != null) {
									setX(columnStart);
									writeText(fp
											.getBigDecimalPayload() + "");
								} else
									newLine();
								if (columnStart == midPage) {

									newLine(4);
								} else {
									if (fp.getBigDecimalPayload() != null)
										moveY(-20);
									else
										moveY(-25);
								}
								break;
							case INTEGER:

								setX(columnStart);
								writeLongBoldTextInColumn(
										dnl.getLocalizedDisplayName(fp
												.getDisplayName())
												+ " :", 16, columnStart, columnEnd);
								;
								newLine();
								if (fp.getBigDecimalPayload() != null) {
									setX(columnStart);
									writeText(fp
											.getBigDecimalPayload() + "");
								} else
									newLine();
								if (columnStart == midPage) {

									newLine(4);
								} else {
									if (fp.getBigDecimalPayload() != null)
										moveY(-20);
									else
										moveY(-25);
								}
								break;
							case BOOL:

								setX(columnStart);
								writeLongBoldTextInColumn(
										dnl.getLocalizedDisplayName(fp
												.getDisplayName())
												+ " :", 16, columnStart, columnEnd);

								newLine();
								if (fp.getBooleanPayload() != null) {
									setX(columnStart);
									writeText(fp.getBooleanPayload()
											+ "");
								} else
									newLine();
								if (columnStart == midPage) {

									newLine(4);
								} else {
									if (fp.getBooleanPayload() != null)
										moveY(-20);
									else
										moveY(-25);
								}
								break;
							default:
								break;
							}

                            if (columnStart == horizontalMargin){
                                columnStart = midPage;
                                columnEnd = pageWidth - horizontalMargin;
                            }
                            else{
                                columnStart = horizontalMargin;
                                columnEnd = midPage;
                            }

						}

						newLine(8);
						drawHorizontalLine();

						newLine(2);

						if (isPageEnding())
							addPage(document, context, claim.getClaimId());

						newLine(2);
					}
				}

			}

			// ---------------------------------------------------------------------------------------------
			// MAP screenshot section
			addPage(document, context, claim.getClaimId());
			newLine();
			setX(horizontalMargin); // the map covers the entire width regardless of arabic orientation
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
				newLine(3);
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
		setFont(FONT_SANS_SERIF, Typeface.NORMAL, NORMAL_TYPEFACE_SIZE);
	}

    private void writeLongBoldTextInColumn(String text, int size, int columStart, int columnEnd) {

        setFont(FONT_SANS_SERIF, Typeface.BOLD, size);
        writeLongTextInColumn(text, columStart, columnEnd);
        setFont(FONT_SANS_SERIF, Typeface.NORMAL, NORMAL_TYPEFACE_SIZE);
    }

    private void writeLongTextInColumn(String text, int columnStart, int columnEnd) {
        String[] tokens = text.split("\\s+");
        Iterator<String> iter = Arrays.asList(tokens).iterator();
        while (iter.hasNext()) {
            String token = iter.next();
            if(iter.hasNext()){
                writeText(token);
                if(isLineEnding(columnEnd)){
                    newColumnLine(columnStart);
                }else{
                    writeText(" ");
                }
            }else{
                writeText(token);
            }
        }
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

    private void newColumnLine(int howMany, int columnStart) {
        if(howMany < 1){
            return;
        }
        for(int i = 0; i < howMany; i++){
            newColumnLine(columnStart);
        }
    }

    private void newColumnLine(int columnStart) {
        currentX = columnStart;
        currentY += currentLineHeight + DEFAULT_VERTICAL_SPACE;
        currentLineHeight = 0;
    }

    private void newLine() {
        if (!OpenTenureApplication.getInstance().getLocale().toString()
                .startsWith("ar")) {
            currentX = horizontalMargin;
        }else{
            currentX = ARABIC_LINE_START;
        }
        currentY += currentLineHeight + DEFAULT_VERTICAL_SPACE;
        currentLineHeight = 0;
    }

    private void newLine(int howMany) {
		if(howMany < 1){
			return;
		}
		for(int i = 0; i < howMany; i++){
			newLine();
		}
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
		currentY = DEFAULT_VERTICAL_MARGIN;

	}

	private boolean isPageEnding() {

		return (pageHeight - currentY) < 2*APPROX_ONE_INCH;

	}

	private boolean isLineEnding(int columnEnd) {

		return (columnEnd - currentX) < APPROX_ONE_INCH;

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