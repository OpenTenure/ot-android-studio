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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.fao.sola.clients.android.opentenure.DisplayNameLocalizer;
import org.fao.sola.clients.android.opentenure.form.constraint.IntegerRangeConstraint;
import org.fao.sola.clients.android.opentenure.form.field.IntegerField;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SectionTemplate implements Comparable<SectionTemplate>{
	
	private String id;
	@JsonIgnore
	private FormTemplate formTemplate;
	private String formTemplateName;
	private String name;
	private int itemOrder;
	private String displayName;
	private String errorMsg = "{0} must contain between {2} and {3} elements";
	private int minOccurrences;
	private int maxOccurrences;
	private String elementName;
	private String elementDisplayName;
	private List<FieldTemplate> fieldTemplateList;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public FormTemplate getFormTemplate() {
		return formTemplate;
	}

	public void setFormTemplate(FormTemplate formTemplate) {
		this.formTemplate = formTemplate;
	}

	public String getFormTemplateName() {
		return formTemplateName;
	}

	public void setFormTemplateName(String formTemplateName) {
		this.formTemplateName = formTemplateName;
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

	public int getMinOccurrences() {
		return minOccurrences;
	}
	
	
	public int getItemOrder() {
		return itemOrder;
	}

	public void setItemOrder(int itemOrder) {
		this.itemOrder = itemOrder;
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

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public List<FieldTemplate> getFieldTemplateList() {
		return fieldTemplateList;
	}

	public void setFieldTemplateList(List<FieldTemplate> fieldTemplateList) {
		this.fieldTemplateList = fieldTemplateList;
	}

	@Override
	public String toString() {
		return "SectionTemplate ["
				+ "id=" + id
				+ ", name=" + name
				+ ", displayName=" + displayName
				+ ", itemOrder=" + itemOrder
				+ ", errorMsg=" + errorMsg
				+ ", minOccurrences=" + minOccurrences
				+ ", maxOccurrences=" + maxOccurrences
				+ ", elementName=" + elementName
				+ ", elementDisplayName=" + elementDisplayName
				+ ", fieldTemplateList=" + Arrays.toString(fieldTemplateList.toArray())
				+ "]";
	}

	public SectionTemplate(String name, String displayName, String elementName, String elementDisplayName, int minOccurrences, int maxOccurrences,int itemOrder){
		this.id = UUID.randomUUID().toString();
		this.name = name;
		this.displayName = displayName;
		this.elementName = elementName;
		this.itemOrder = itemOrder;
		this.elementDisplayName = elementDisplayName;
		this.minOccurrences = minOccurrences;
		this.maxOccurrences = maxOccurrences;
		this.fieldTemplateList = new ArrayList<FieldTemplate>();
	}

	public SectionTemplate(String name, String displayName, int itemOrder){
		this.id = UUID.randomUUID().toString();
		this.name = name;
		this.displayName = displayName;
		this.elementName = name;
		this.itemOrder = itemOrder;
		this.elementDisplayName = displayName;
		this.fieldTemplateList = new ArrayList<FieldTemplate>();
		this.minOccurrences = 1;
		this.maxOccurrences = 1;
	}

	public SectionTemplate(){
		this.id = UUID.randomUUID().toString();
		this.fieldTemplateList = new ArrayList<FieldTemplate>();
	}

	public SectionTemplate(SectionTemplate sec){
		this.id = UUID.randomUUID().toString();
		this.minOccurrences = sec.minOccurrences;
		this.maxOccurrences = sec.maxOccurrences;
		this.name = new String(sec.getName());
		this.itemOrder = sec.getItemOrder();
		this.displayName = new String(sec.getDisplayName());
		this.elementName = new String(sec.getElementName());
		this.elementDisplayName = new String(sec.getElementDisplayName());
		for(FieldTemplate fieldTemplate:sec.getFieldTemplateList()){
			this.fieldTemplateList.add(new FieldTemplate(fieldTemplate));
		}
	}

	public SectionTemplate(SectionPayload sp){
		this.id = UUID.randomUUID().toString();
		this.name = new String(sp.getName());
		this.elementName = new String(sp.getElementName());
		this.elementDisplayName = new String(sp.getElementDisplayName());
		this.itemOrder = sp.getItemOrder();
		this.minOccurrences = sp.getMinOccurrences();
		this.maxOccurrences = sp.getMaxOccurrences();
		this.displayName = new String(sp.getDisplayName());
		if(sp.getSectionElementPayloadList() != null && sp.getSectionElementPayloadList().size() > 0){
			this.fieldTemplateList = new ArrayList<FieldTemplate>();
			for(FieldPayload fieldPayload:sp.getSectionElementPayloadList().get(0).getFieldPayloadList()){
				this.fieldTemplateList.add(new FieldTemplate(fieldPayload));
			}
		}
	}

	public FieldConstraint getFailedConstraint(SectionPayload payload, DisplayNameLocalizer dnl) {
		
		List<SectionElementPayload> elements = payload.getSectionElementPayloadList();

		if ((elements.size() < minOccurrences) || (elements.size() > maxOccurrences)) {
			IntegerRangeConstraint constraint = new IntegerRangeConstraint();
			constraint.setMinValue(BigDecimal.valueOf(minOccurrences));
			constraint.setMaxValue(BigDecimal.valueOf(maxOccurrences));
			constraint.setName(name);
			constraint.setDisplayName(displayName);
			constraint.setErrorMsg(errorMsg);
			FieldTemplate fieldTemplate = new IntegerField();
			FieldPayload fieldValue = new FieldPayload();
			fieldValue.setName(name);
			fieldValue.setDisplayName(displayName);
			fieldValue.setItemOrder(itemOrder);
			fieldValue.setFieldValueType(FieldValueType.NUMBER);
			fieldValue.setBigDecimalPayload(new BigDecimal(elements.size()));
			fieldTemplate.setName(name);
			fieldTemplate.setDisplayName(displayName);
			try {
				fieldTemplate.addConstraint(constraint);
			} catch (Exception e) {
				e.printStackTrace();
			}
			constraint.check(displayName, fieldValue);
			return constraint;
		}
		int sectionElementIndex = 0;
		for(SectionElementPayload element:elements){
			FieldConstraint failedConstraint = getFailedConstraint(dnl.getLocalizedDisplayName(displayName) + "/" + sectionElementIndex, element);
			if(failedConstraint != null){
				return failedConstraint;
			}
			sectionElementIndex++;
		}
		return null;
	}
	public void addField(FieldTemplate fieldTemplate) {
		fieldTemplateList.add(fieldTemplate);
	}

	private FieldConstraint getFailedConstraint(String externalDisplayName, SectionElementPayload payload) {
		Map<String,FieldTemplate> templateMap = new HashMap<String,FieldTemplate>();

		if(payload.getFieldPayloadList() == null || payload.getFieldPayloadList().size() <= 0){
			return null;
		}

		if(fieldTemplateList != null && fieldTemplateList.size() > 0){
			for(FieldTemplate ft:fieldTemplateList){
				templateMap.put(ft.getName(),ft);
			}
		}else{
			return null;
		}
		
		for(FieldPayload fp:payload.getFieldPayloadList()){
			FieldTemplate ft = templateMap.get(fp.getName());
			if(ft != null){
				FieldConstraint fieldConstraint = ft.getFailedConstraint(externalDisplayName, fp);
				if(fieldConstraint != null){
					return fieldConstraint;
				}
			}
		}

		return null;
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
	
	public static SectionTemplate fromJson(String json) {
		ObjectMapper mapper = new ObjectMapper();
		SectionTemplate section;
		try {
			section = mapper.readValue(json, SectionTemplate.class);
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

	@Override
	public int compareTo(SectionTemplate section) {
		 
		
		if (section.getItemOrder() == itemOrder)
			return 0;
		if (section.getItemOrder() > itemOrder)
			return -1;
		if (section.getItemOrder() < itemOrder)
			return 1;
		
		return 0;

	}

}
