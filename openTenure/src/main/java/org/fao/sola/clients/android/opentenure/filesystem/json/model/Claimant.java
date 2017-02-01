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
package org.fao.sola.clients.android.opentenure.filesystem.json.model;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class Claimant {

	@SerializedName("id")          private String id;
	@SerializedName("name")        private String name;
	@SerializedName("lastName")    private String lastName;
	@SerializedName("idTypeCode")  private String idTypeCode;
	@SerializedName("idNumber")    private String idNumber;
	//@SerializedName("placeOfBirth")private String placeOfBirth;
	@SerializedName("address")     private String address;
	@SerializedName("mobilePhone") private String mobilePhone;
	@SerializedName("phone")       private String phone;
	@SerializedName("email")       private String email;
	@SerializedName("birthDate")   private String birthDate;
	@SerializedName("genderCode")  private String genderCode;
	@SerializedName("person") 	   private boolean isPhysicalPerson;

	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getIdTypeCode() {
		return idTypeCode;
	}
	public void setIdTypeCode(String idTypeCode) {
		this.idTypeCode = idTypeCode;
	}
	public String getIdNumber() {
		return idNumber;
	}
	public void setIdNumber(String idNumber) {
		this.idNumber = idNumber;
	}
//	public String getPlaceOfBirth() {
//		return placeOfBirth;
//	}
//	public void setPlaceOfBirth(String placeOfBirth) {
//		this.placeOfBirth = placeOfBirth;
//	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getMobilePhone() {
		return mobilePhone;
	}
	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	public String getBirthDate() {
		return birthDate;
	}
	public void setBirthDate(String birthDate) {
		this.birthDate = birthDate;
	}
	public String getGenderCode() {
		if (genderCode == null)
			return null;
		if (genderCode.equals("male"))
				return "M";
		if (genderCode.equals("female"))
			return "F";
		return genderCode;
	}

	public void setGenderCode(String genderCode) {

		if (genderCode == null) {
			this.genderCode = null;
			return;
		}

		if (genderCode.equals("M"))
			this.genderCode = "male";
		if (genderCode.equals("F"))
			this.genderCode = "female";
	}	
	
	public boolean isPhysicalPerson() {
		return isPhysicalPerson;
	}
	public void setPhysicalPerson(boolean isPhysicalPerson) {
		this.isPhysicalPerson = isPhysicalPerson;
	}
	

}
