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
import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FieldPayload {
	
	private String id;
	@JsonIgnore
	private transient SectionElementPayload sectionElementPayload;
	private String sectionElementPayloadId;
	protected String name;
	protected String displayName;
	protected FieldType fieldType;
	private FieldValueType fieldValueType;
	private String stringPayload;
	private BigDecimal bigDecimalPayload;
	private Boolean booleanPayload;
	protected int itemOrder;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public SectionElementPayload getSectionElementPayload() {
		return sectionElementPayload;
	}

	public void setSectionElementPayload(SectionElementPayload sectionElementPayload) {
		this.sectionElementPayload = sectionElementPayload;
	}

	public String getSectionElementPayloadId() {
		return sectionElementPayloadId;
	}

	public void setSectionElementPayloadId(String sectionElementPayloadId) {
		this.sectionElementPayloadId = sectionElementPayloadId;
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
	
	public FieldType getFieldType() {
		return fieldType;
	}

	public void setFieldType(FieldType type) {
		this.fieldType = type;
	}

	public FieldValueType getFieldValueType() {
		return fieldValueType;
	}
	public void setFieldValueType(FieldValueType fieldValueType) {
		this.fieldValueType = fieldValueType;
	}

	public String getStringPayload() {
		return stringPayload;
	}

	public void setStringPayload(String stringPayload) {
		this.stringPayload = stringPayload;
	}

	public BigDecimal getBigDecimalPayload() {
		return bigDecimalPayload;
	}

	public void setBigDecimalPayload(BigDecimal bigDecimalPayload) {
		this.bigDecimalPayload = bigDecimalPayload;
	}

	public Boolean getBooleanPayload() {
		return booleanPayload;
	}

	public void setBooleanPayload(Boolean booleanPayload) {
		this.booleanPayload = booleanPayload;
	}

	
	@Override
	public String toString() {
		return "FieldPayload ["
				+ "id=" + id
				+ ", name=" + name
				+ ", displayName=" + displayName
				+ ", fieldType=" + fieldType
				+ ", stringPayload=" + stringPayload
				+ ", bigDecimalPayload=" + bigDecimalPayload
				+ ", booleanPayload=" + booleanPayload
				+ ", fieldValueType=" + fieldValueType
				+ "]";
	}
	
	public FieldPayload(){
		this.id = UUID.randomUUID().toString();
	}

	public FieldPayload(FieldPayload field){
		this.id = field.getId();
		this.itemOrder = field.getItemOrder();
		this.sectionElementPayloadId = field.getSectionElementPayloadId();
		if(field.getName() != null){
			this.name = field.getName();
		}

		if(field.getDisplayName() != null){
			this.displayName = field.getDisplayName();
		}
		
		this.fieldType = field.getFieldType();
		if(field.getStringPayload() != null){
			this.stringPayload = field.getStringPayload();
		}
		if(field.getBigDecimalPayload() != null){
			this.bigDecimalPayload = new BigDecimal(field.getBigDecimalPayload().toString());
		}
		if(field.getBooleanPayload() != null){
			this.booleanPayload = Boolean.valueOf(field.getBooleanPayload());
		}

		this.fieldValueType = field.getFieldValueType();
	}

	public FieldPayload(FieldTemplate field){
		this.id = UUID.randomUUID().toString();
		if(field.getName() != null){
			this.name = new String(field.getName());
		}
		if(field.getDisplayName() != null){
			this.displayName = new String(field.getDisplayName());
		}
		switch(field.getFieldType()){
		case BOOL:
			this.fieldValueType = FieldValueType.BOOL;
			break;
		case DATE:
			this.fieldValueType = FieldValueType.TEXT;
			break;
		case TIME:
			this.fieldValueType = FieldValueType.TEXT;
			break;
		case DECIMAL:
			this.fieldValueType = FieldValueType.NUMBER;
			break;
		case DOCUMENT:
			this.fieldValueType = FieldValueType.TEXT;
			break;
		case GEOMETRY:
			this.fieldValueType = FieldValueType.TEXT;
			break;
		case INTEGER:
			this.fieldValueType = FieldValueType.NUMBER;
			break;
		case SNAPSHOT:
			this.fieldValueType = FieldValueType.TEXT;
			break;
		case TEXT:
			this.fieldValueType = FieldValueType.TEXT;
			break;
		}
		this.fieldType = field.getFieldType();
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
	
	public static FieldPayload fromJson(String json) {
		ObjectMapper mapper = new ObjectMapper();
		FieldPayload field;
		try {
			field = mapper.readValue(json, FieldPayload.class);
			return field;
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
