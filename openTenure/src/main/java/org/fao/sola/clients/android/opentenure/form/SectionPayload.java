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

public class SectionPayload {
	
	private String id;
	@JsonIgnore
	private transient FormPayload formPayload;
	private String formPayloadId;
	private String name;
	private int itemOrder;
	private String displayName;
	private String elementName;
	private String elementDisplayName;
	private int minOccurrences;
	private int maxOccurrences;
	private List<SectionElementPayload> sectionElementPayloadList;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public FormPayload getFormPayload() {
		return formPayload;
	}

	public void setFormPayload(FormPayload formPayload) {
		this.formPayload = formPayload;
	}

	public String getFormPayloadId() {
		return formPayloadId;
	}

	public void setFormPayloadId(String formPayloadId) {
		this.formPayloadId = formPayloadId;
	}
	
	public int getItemOrder() {
		return itemOrder;
	}

	public void setItemOrder(int itemOrder) {
		this.itemOrder = itemOrder;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getElementName() {
		return elementName;
	}

	public void setElementName(String elementName) {
		this.elementName = elementName;
	}

	public String getElementDisplayName() {
		return elementDisplayName;
	}

	public void setElementDisplayName(String elementDisplayName) {
		this.elementDisplayName = elementDisplayName;
	}

	public int getMinOccurrences() {
		return minOccurrences;
	}

	public void setMinOccurrences(int minOccurrences) {
		this.minOccurrences = minOccurrences;
	}

	public int getMaxOccurrences() {
		return maxOccurrences;
	}

	public void setMaxOccurrences(int maxOccurrences) {
		this.maxOccurrences = maxOccurrences;
	}

	public List<SectionElementPayload> getSectionElementPayloadList() {
		return sectionElementPayloadList;
	}

	public void setSectionElementPayloadList(List<SectionElementPayload> sectionElementPayloadList) {
		if(sectionElementPayloadList != null){
			for(SectionElementPayload sectionElementPayload:sectionElementPayloadList){
				sectionElementPayload.setSectionPayloadId(id);
			}
		}
		this.sectionElementPayloadList = sectionElementPayloadList;
	}
	
	@Override
	public String toString() {
		return "SectionPayload ["
				+ "id=" + id
				+ ", name=" + name
				+ ", displayName=" + displayName
				+ ", itemOrder=" + itemOrder
				+ ", elementName=" + elementName
				+ ", elementDisplayName=" + elementDisplayName
				+ ", minOccurrences=" + minOccurrences
				+ ", maxOccurrences=" + maxOccurrences
				+ ", sectionElementPayloadList=" + Arrays.toString(sectionElementPayloadList.toArray())
				+ "]";
	}

	public SectionPayload(String name, String displayName){
		this.id = UUID.randomUUID().toString();
		this.name = name;
		this.displayName = displayName;
		this.sectionElementPayloadList = new ArrayList<SectionElementPayload>();
	}

	public SectionPayload(){
		this.id = UUID.randomUUID().toString();
		this.sectionElementPayloadList = new ArrayList<SectionElementPayload>();
	}

	public SectionPayload(SectionPayload sp){
		this.id = sp.getId();
		this.formPayloadId = sp.getFormPayloadId();
		this.name = sp.getName();
		this.displayName = sp.getDisplayName();
		this.elementName = sp.getElementName();
		this.itemOrder = sp.getItemOrder();
		this.elementDisplayName = sp.getElementDisplayName();
		this.minOccurrences = sp.getMinOccurrences();
		this.maxOccurrences = sp.getMaxOccurrences();
		this.sectionElementPayloadList = new ArrayList<SectionElementPayload>();
		for(SectionElementPayload sep:sp.getSectionElementPayloadList()){
			this.sectionElementPayloadList.add(new SectionElementPayload(sep));
		}
	}

	public SectionPayload(SectionTemplate st){
		this.id = UUID.randomUUID().toString();
		this.name = st.getName();
		this.elementName = st.getElementName();
		this.elementDisplayName = st.getElementDisplayName();
		this.itemOrder = st.getItemOrder();
		this.minOccurrences = st.getMinOccurrences();
		this.maxOccurrences = st.getMaxOccurrences();
		this.displayName = st.getDisplayName();
		this.sectionElementPayloadList = new ArrayList<SectionElementPayload>();
	}

	public void addElement(SectionElementPayload element) {
		element.setSectionPayloadId(id);
		sectionElementPayloadList.add(element);
	}

	public void addElement(SectionTemplate sectionTemplate) {
		SectionElementPayload newElement = new SectionElementPayload(sectionTemplate);
		newElement.setSectionPayloadId(id);
		this.addElement(newElement);
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
	
	public static SectionPayload fromJson(String json) {
		ObjectMapper mapper = new ObjectMapper();
		SectionPayload section;
		try {
			section = mapper.readValue(json, SectionPayload.class);
			return section;
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
