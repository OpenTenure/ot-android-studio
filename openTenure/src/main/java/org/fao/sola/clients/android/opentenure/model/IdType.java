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
package org.fao.sola.clients.android.opentenure.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.fao.sola.clients.android.opentenure.DisplayNameLocalizer;
import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.network.response.IdTypeResponse;

public class IdType extends RefDataModel {
	private static String tableName = "ID_TYPE";

	@Override
	public int insert() {
		return insert(tableName, this);
	}

	@Override
	public int update() {
		return update(tableName, this);
	}

	@Override
	public String toString() {
		return "DocumentType [code=" + code + ", description=" + description + ", displayValue=" + displayValue + ", active=" + active + "]";
	}

	public static List<IdType> getAll(boolean onlyActive, boolean addDummy){
		return RefDataModel.getItems(tableName, IdType.class, onlyActive, true, addDummy);
	}

	public static Map<String,String> getKeyValueMap(boolean onlyActive) {
		return getKeyValueMap(tableName, IdType.class, onlyActive);
	}

	public static Map<String,String> getValueKeyMap(boolean onlyActive) {
		return getValueKeyMap(tableName, IdType.class, onlyActive);
	}

	public int getIndexByCodeType(String code,boolean onlyActive) {
		return getIndexByCode(tableName, IdType.class, code, onlyActive);
	}

	public static String getDisplayValueByCode(String code) {
		return getDisplayValueByCode(tableName, code);
	}

	public static IdType getIdType(String code) {
		return (IdType) getItem(tableName, IdType.class, code);
	}

	public static void update (List<IdTypeResponse> types){
		update(types, tableName, IdType.class);
	}
}