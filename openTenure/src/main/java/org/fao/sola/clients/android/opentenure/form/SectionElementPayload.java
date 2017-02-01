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

public class SectionElementPayload {
	
	@JsonIgnore
	private transient SectionPayload sectionPayload;
	private String sectionPayloadId;
	private String id;
	private List<FieldPayload> fieldPayloadList;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSectionPayloadId() {
		return sectionPayloadId;
	}

	public void setSectionPayloadId(String sectionPayloadId) {
		this.sectionPayloadId = sectionPayloadId;
	}

	public SectionElementPayload(){
		this.id = UUID.randomUUID().toString();
		this.fieldPayloadList = new ArrayList<FieldPayload>();
	}

	public SectionElementPayload(SectionElementPayload se){
		this.id = se.getId();
		this.sectionPayloadId = se.getSectionPayloadId();
		this.fieldPayloadList = new ArrayList<FieldPayload>();
		List<FieldPayload> fieldPayloadList = se.getFieldPayloadList();
		if(fieldPayloadList != null){
			for(FieldPayload fieldPayload:fieldPayloadList){
				this.fieldPayloadList.add(new FieldPayload(fieldPayload));
			}
		}
	}

	public SectionElementPayload(SectionTemplate st) {
		this.id = UUID.randomUUID().toString();
		this.fieldPayloadList = new ArrayList<FieldPayload>();
		List<FieldTemplate> fieldTemplateList = st.getFieldTemplateList();
		if(fieldTemplateList != null){
			for(FieldTemplate ft:fieldTemplateList){
				FieldPayload newField = new FieldPayload(ft);
				newField.setSectionElementPayloadId(id);
				this.fieldPayloadList.add(newField);
			}
		}
	}

	@Override
	public String toString() {
		return "SectionElementPayload ["
				+ "id=" + id
				+ ", fieldPayloadList=" + Arrays.toString(fieldPayloadList.toArray())
				+ "]";
	}

	public List<FieldPayload> getFieldPayloadList() {
		return fieldPayloadList;
	}

	public void setFieldPayloadList(List<FieldPayload> fieldPayloadList) {
		if(fieldPayloadList != null){
			for(FieldPayload fieldPayload:fieldPayloadList){
				fieldPayload.setSectionElementPayloadId(id);
			}
		}
		this.fieldPayloadList = fieldPayloadList;
	}

	public void addField(FieldPayload field) {
		field.setSectionElementPayloadId(id);
		fieldPayloadList.add(field);
	}

	public SectionPayload getSectionPayload() {
		return sectionPayload;
	}

	public void setSectionPayload(SectionPayload sectionPayload) {
		this.sectionPayload = sectionPayload;
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
	
	public static SectionElementPayload fromJson(String json) {
		ObjectMapper mapper = new ObjectMapper();
		SectionElementPayload section;
		try {
			section = mapper.readValue(json, SectionElementPayload.class);
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
