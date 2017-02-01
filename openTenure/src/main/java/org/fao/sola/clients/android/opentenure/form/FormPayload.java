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
package org.fao.sola.clients.android.opentenure.form;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FormPayload {
	@Override
	public String toString() {
		return "FormPayload [" + "sectionPayloadList="
				+ Arrays.toString(sectionPayloadList.toArray()) + ", claimId="
				+ claimId + ", id=" + id + "]";
	}

	private String id;
	@JsonIgnore
	private transient FormTemplate formTemplate;
	private String claimId;
	private String formTemplateName;
	private List<SectionPayload> sectionPayloadList;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFormTemplateName() {
		return formTemplateName;
	}

	public void setFormTemplateName(String formTemplateName) {
		this.formTemplateName = formTemplateName;
	}

	public FormTemplate getFormTemplate() {
		return formTemplate;
	}

	public void setFormTemplate(FormTemplate template) {
		this.formTemplate = template;
	}

	public FormPayload(String claimId) {
		this.claimId = claimId;
		this.sectionPayloadList = new ArrayList<SectionPayload>();
	}

	public FormPayload(FormTemplate template, String claimId) {
		this.id = UUID.randomUUID().toString();
		this.claimId = claimId;
		this.formTemplateName = template.getName();
		this.formTemplate = template;
		this.sectionPayloadList = new ArrayList<SectionPayload>();
		for (SectionTemplate sectionTemplate : template
				.getSectionTemplateList()) {
			SectionPayload newSection = new SectionPayload(sectionTemplate);
			newSection.setFormPayloadId(id);
			this.sectionPayloadList.add(newSection);
		}
	}

	public FormPayload(FormTemplate template) {
		this.id = UUID.randomUUID().toString();
		this.formTemplateName = template.getName();
		this.formTemplate = template;
		this.sectionPayloadList = new ArrayList<SectionPayload>();
		for (SectionTemplate sectionTemplate : template
				.getSectionTemplateList()) {
			SectionPayload newSection = new SectionPayload(sectionTemplate);
			newSection.setFormPayloadId(id);
			this.sectionPayloadList.add(newSection);
		}
	}

	public String getClaimId() {
		return claimId;
	}

	public void setClaimId(String claimId) {
		this.claimId = claimId;
	}

	public List<SectionPayload> getSectionPayloadList() {
		return sectionPayloadList;
	}

	public void setSectionPayloadList(List<SectionPayload> sectionPayloadList) {
		if (sectionPayloadList != null) {
			for (SectionPayload sectionPayload : sectionPayloadList) {
				sectionPayload.setFormPayloadId(id);
			}
		}
		this.sectionPayloadList = sectionPayloadList;
	}

	public FormPayload() {
		this.id = UUID.randomUUID().toString();
		this.formTemplate = new FormTemplate();
		this.sectionPayloadList = new ArrayList<SectionPayload>();
	}

	public FormPayload(FormPayload form) {
		this.id = form.getId();
		this.claimId = form.getClaimId();
		this.formTemplate = form.getFormTemplate();
		this.formTemplateName = form.getFormTemplateName();
		this.sectionPayloadList = new ArrayList<SectionPayload>();
		for (SectionPayload sectionTemplate : form.getSectionPayloadList()) {
			this.sectionPayloadList.add(new SectionPayload(sectionTemplate));
		}
	}

	public void addSection(SectionPayload sectionPayload) {
		sectionPayloadList.add(sectionPayload);
	}

	public String toJson() {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static FormPayload fromJson(String json) {
		ObjectMapper mapper = new ObjectMapper();
		FormPayload form;
		try {
			form = mapper.readValue(json, FormPayload.class);
			return form;
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
