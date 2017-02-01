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

import org.fao.sola.clients.android.opentenure.form.constraint.DateTimeFormatConstraint;
import org.fao.sola.clients.android.opentenure.form.constraint.DoubleRangeConstraint;
import org.fao.sola.clients.android.opentenure.form.constraint.IntegerConstraint;
import org.fao.sola.clients.android.opentenure.form.constraint.IntegerRangeConstraint;
import org.fao.sola.clients.android.opentenure.form.constraint.LengthConstraint;
import org.fao.sola.clients.android.opentenure.form.constraint.NotNullConstraint;
import org.fao.sola.clients.android.opentenure.form.constraint.OptionConstraint;
import org.fao.sola.clients.android.opentenure.form.constraint.RegexpFormatConstraint;
import org.fao.sola.clients.android.opentenure.form.exception.FormException;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "fieldType")
@JsonSubTypes({
		@JsonSubTypes.Type(value = org.fao.sola.clients.android.opentenure.form.field.BoolField.class, name = "BOOL"),
		@JsonSubTypes.Type(value = org.fao.sola.clients.android.opentenure.form.field.DateField.class, name = "DATE"),
		@JsonSubTypes.Type(value = org.fao.sola.clients.android.opentenure.form.field.DecimalField.class, name = "DECIMAL"),
		@JsonSubTypes.Type(value = org.fao.sola.clients.android.opentenure.form.field.DocumentField.class, name = "DOCUMENT"),
		@JsonSubTypes.Type(value = org.fao.sola.clients.android.opentenure.form.field.GeometryField.class, name = "GEOMETRY"),
		@JsonSubTypes.Type(value = org.fao.sola.clients.android.opentenure.form.field.IntegerField.class, name = "INTEGER"),
		@JsonSubTypes.Type(value = org.fao.sola.clients.android.opentenure.form.field.SnapshotField.class, name = "SNAPSHOT"),
		@JsonSubTypes.Type(value = org.fao.sola.clients.android.opentenure.form.field.TextField.class, name = "TEXT"),
		@JsonSubTypes.Type(value = org.fao.sola.clients.android.opentenure.form.field.TimeField.class, name = "TIME") })
public class FieldTemplate implements Comparable<FieldTemplate> {

	private String id;
	@JsonIgnore
	private SectionTemplate sectionTemplate;
	private String sectionTemplateId;
	protected String name;
	protected String displayName;
	protected int itemOrder;
	protected String hint;
	protected FieldType fieldType;
	protected List<FieldConstraint> fieldConstraintList;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getItemOrder() {
		return itemOrder;
	}

	public void setItemOrder(int itemOrder) {
		this.itemOrder = itemOrder;
	}

	public String getSectionTemplateId() {
		return sectionTemplateId;
	}

	public void setSectionTemplateId(String sectionTemplateId) {
		this.sectionTemplateId = sectionTemplateId;
	}

	public SectionTemplate getSectionTemplate() {
		return sectionTemplate;
	}

	public void setSectionTemplate(SectionTemplate sectionTemplate) {
		this.sectionTemplate = sectionTemplate;
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

	public String getHint() {
		return hint;
	}

	public void setHint(String hint) {
		this.hint = hint;
	}

	public FieldType getFieldType() {
		return fieldType;
	}

	public void setFieldType(FieldType fieldType) {
		this.fieldType = fieldType;
	}

	public List<FieldConstraint> getFieldConstraintList() {
		return fieldConstraintList;
	}

	public void setFieldConstraintList(List<FieldConstraint> fieldConstraintList) {
		this.fieldConstraintList = fieldConstraintList;
	}

	@Override
	public String toString() {
		return "FieldTemplate [" + "id=" + id + "itemOrder=" + itemOrder
				+ ", name=" + name + ", fieldType=" + fieldType + ", hint="
				+ hint + ", displayName=" + displayName
				+ ", fieldConstraintList="
				+ Arrays.toString(fieldConstraintList.toArray()) + "]";
	}

	public FieldTemplate() {
		this.id = UUID.randomUUID().toString();
		this.fieldConstraintList = new ArrayList<FieldConstraint>();
	}

	public FieldTemplate(FieldTemplate fieldTemplate) {
		this.id = UUID.randomUUID().toString();
		if (fieldTemplate.getName() != null) {
			this.name = new String(fieldTemplate.getName());
		}

		if (fieldTemplate.getDisplayName() != null) {
			this.displayName = new String(fieldTemplate.getDisplayName());
		}
		if (fieldTemplate.getHint() != null) {
			this.hint = new String(fieldTemplate.getHint());
		}
		this.fieldType = fieldTemplate.getFieldType();
		this.fieldConstraintList = new ArrayList<FieldConstraint>();
		if (fieldTemplate.getFieldConstraintList() != null) {
			for (FieldConstraint con : fieldTemplate.getFieldConstraintList()) {
				// Can't copy a constraint since it is an abstract class
				if (con instanceof DateTimeFormatConstraint) {
					this.fieldConstraintList.add(new DateTimeFormatConstraint(
							(DateTimeFormatConstraint) con));
				} else if (con instanceof IntegerConstraint) {
					this.fieldConstraintList.add(new IntegerConstraint(
							(IntegerConstraint) con));
				} else if (con instanceof LengthConstraint) {
					this.fieldConstraintList.add(new LengthConstraint(
							(LengthConstraint) con));
				} else if (con instanceof NotNullConstraint) {
					this.fieldConstraintList.add(new NotNullConstraint(
							(NotNullConstraint) con));
				} else if (con instanceof OptionConstraint) {
					this.fieldConstraintList.add(new OptionConstraint(
							(OptionConstraint) con));
				} else if (con instanceof IntegerRangeConstraint) {
					this.fieldConstraintList.add(new IntegerRangeConstraint(
							(IntegerRangeConstraint) con));
				} else if (con instanceof DoubleRangeConstraint) {
					this.fieldConstraintList.add(new DoubleRangeConstraint(
							(DoubleRangeConstraint) con));
				} else if (con instanceof RegexpFormatConstraint) {
					this.fieldConstraintList.add(new RegexpFormatConstraint(
							(RegexpFormatConstraint) con));
				} else {
					this.fieldConstraintList.add(con);
				}
			}
		}
	}

	public FieldTemplate(FieldPayload field) {
		this.id = UUID.randomUUID().toString();
		if (field.getName() != null) {
			this.name = new String(field.getName());
		}
		if (field.getDisplayName() != null) {
			this.displayName = new String(field.getDisplayName());
		}
		this.fieldType = field.getFieldType();
	}

	public void addConstraint(FieldConstraint fieldConstraint) throws Exception {
		if (fieldConstraint.appliesTo(this)) {
			fieldConstraintList.add(fieldConstraint);
		} else {
			throw new FormException("Constraint " + fieldConstraint.toString()
					+ " does not apply to field " + this.toString());
		}
	}

	public FieldConstraint getFailedConstraint(String externalDisplayName,
			FieldPayload payload) {
		for (FieldConstraint fieldConstraint : fieldConstraintList) {
			boolean check;
			if (externalDisplayName != null) {
				check = fieldConstraint.check(externalDisplayName + "/"
						+ displayName, payload);
			} else {
				check = fieldConstraint.check(externalDisplayName, payload);
			}
			if (!check) {
				return fieldConstraint;
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

	public static FieldTemplate fromJson(String json) {
		ObjectMapper mapper = new ObjectMapper();
		FieldTemplate field;
		try {
			field = mapper.readValue(json, FieldTemplate.class);
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

	@Override
	public int compareTo(FieldTemplate field) {
		// TODO Auto-generated method stub

		if (field.getItemOrder() == itemOrder)
			return 0;
		if (field.getItemOrder() > itemOrder)
			return -1;
		if (field.getItemOrder() < itemOrder)
			return 1;
		
		return 0;
		
	}

}
