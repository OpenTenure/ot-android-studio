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
package org.fao.sola.clients.android.opentenure;

import android.content.Context;

import java.io.Serializable;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;

import org.fao.sola.clients.android.opentenure.model.Attachment;
import org.fao.sola.clients.android.opentenure.tools.StringUtility;

public class ClaimListTO implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = -7028473419982625766L;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSlogan() {
		return slogan;
	}

	public void setSlogan(String slogan) {
		this.slogan = slogan;
	}

	public String getRemaingDays() {
		return remaingDays;
	}

	public void setRemaingDays(String remaingDays) {
		this.remaingDays = remaingDays;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public boolean isModifiable() {
		return isModifiable;
	}

	public void setModifiable(boolean isModifiable) {
		this.isModifiable = isModifiable;
	}

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public Date getDateOfStart() {
		return dateOfStart;
	}

	public void setDateOfStart(Date dateOfStart) {
		this.dateOfStart = dateOfStart;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	private String number;
	private String id;
	private String slogan;
	private String status;
	private String remaingDays;
	private String personId;
	private String name;
	private boolean deleted;
	private List<Attachment> attachments;
	private boolean isModifiable;
	private Date dateOfStart;
	private Date creationDate;

	public static Comparator<ClaimListTO> startDateAsc = new Comparator<ClaimListTO>() {
		@Override
		public int compare(ClaimListTO claim1, ClaimListTO claim2) {
			if (claim1.getDateOfStart() == null && claim2.getDateOfStart() == null) {
				return 0;
			}
			if (claim1.getDateOfStart() != null && claim2.getDateOfStart() == null) {
				return 1;
			}
			if (claim1.getDateOfStart() == null && claim2.getDateOfStart() != null) {
				return -1;
			}
			return claim1.getDateOfStart().compareTo(claim2.getDateOfStart());
		}
	};

	public static Comparator<ClaimListTO> claimNumberAsc = new Comparator<ClaimListTO>() {
		@Override
		public int compare(ClaimListTO claim1, ClaimListTO claim2) {
			return StringUtility.empty(claim1.getNumber()).compareTo(claim2.getNumber());
		}
	};

	public static Comparator<ClaimListTO> claimNameAsc = new Comparator<ClaimListTO>() {
		@Override
		public int compare(ClaimListTO claim1, ClaimListTO claim2) {
			String name1 = StringUtility.empty(claim1.getName());
			String name2 = StringUtility.empty(claim2.getName());

			if (StringUtility.isEmpty(name1)) {
				name1 = OpenTenureApplication.getInstance().getString(R.string.default_claim_name);
				if (claim1.getCreationDate() != null) {
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					name1 += " (" + df.format(claim1.getCreationDate()) + ")";
				}
			}

			if (StringUtility.isEmpty(name2)) {
				name2 = OpenTenureApplication.getInstance().getString(R.string.default_claim_name);
				if (claim2.getCreationDate() != null) {
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					name2 += " [" + df.format(claim1.getCreationDate()) + "]";
				}
			}

			return name1.compareTo(name2);
		}
	};

	public static Comparator<ClaimListTO> startDateDesc = new Comparator<ClaimListTO>() {
		@Override
		public int compare(ClaimListTO claim1, ClaimListTO claim2) {
			return startDateAsc.compare(claim2, claim1);
		}
	};

	public static Comparator<ClaimListTO> claimNumberDesc = new Comparator<ClaimListTO>() {
		@Override
		public int compare(ClaimListTO claim1, ClaimListTO claim2) {
			return claimNumberAsc.compare(claim2, claim1);
		}
	};

	public static Comparator<ClaimListTO> claimNameDesc = new Comparator<ClaimListTO>() {
		@Override
		public int compare(ClaimListTO claim1, ClaimListTO claim2) {
			return claimNameAsc.compare(claim2, claim1);
		}
	};

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
}