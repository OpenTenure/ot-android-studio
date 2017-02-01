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
package org.fao.sola.clients.android.opentenure.maps;

public class Constants {
	
	
	public static final int SRID=3857;
	public static final int PROPERTY_LABEL_MARKERS_GROUP = 0;
	public static final int TARGET_MARKERS_GROUP = PROPERTY_LABEL_MARKERS_GROUP + 1;
	public static final int MY_LOCATION_MARKERS_GROUP = TARGET_MARKERS_GROUP + 1;
	public static final int MARKER_EDIT_RELATIVE_EDIT_GROUP = MY_LOCATION_MARKERS_GROUP + 1;
	public static final int MARKER_EDIT_CANCEL_GROUP = MARKER_EDIT_RELATIVE_EDIT_GROUP + 1;
	public static final int MARKER_EDIT_REMOVE_GROUP = MARKER_EDIT_CANCEL_GROUP + 1;
	public static final int MARKER_RELATIVE_EDIT_CANCEL_GROUP = MARKER_EDIT_REMOVE_GROUP + 1;
	public static final int MARKER_RELATIVE_EDIT_MOVE_TO_GROUP = MARKER_RELATIVE_EDIT_CANCEL_GROUP + 1;
	public static final int MARKER_RELATIVE_EDIT_ADD_GROUP = MARKER_RELATIVE_EDIT_MOVE_TO_GROUP + 1;
	public static final int MARKER_RELATIVE_EDIT_UP_GROUP = MARKER_RELATIVE_EDIT_ADD_GROUP + 1;
	public static final int MARKER_RELATIVE_EDIT_DOWN_GROUP = MARKER_RELATIVE_EDIT_UP_GROUP + 1;
	public static final int MARKER_RELATIVE_EDIT_RIGHT_GROUP = MARKER_RELATIVE_EDIT_DOWN_GROUP + 1;
	public static final int MARKER_RELATIVE_EDIT_LEFT_GROUP = MARKER_RELATIVE_EDIT_RIGHT_GROUP + 1;
	public static final int MARKER_DOWNLOAD_STATUS_GROUP = MARKER_RELATIVE_EDIT_LEFT_GROUP + 1;
	public static final int BASE_PROPERTY_LOCATION_MARKERS_GROUP = 1000;
	public static final int MAX_PROPERTY_LOCATION_MARKERS_GROUP = 1000;
	public static final int BASE_PROPERTY_BOUNDARY_MARKERS_GROUP = BASE_PROPERTY_LOCATION_MARKERS_GROUP + MAX_PROPERTY_LOCATION_MARKERS_GROUP;
	public static final int MAX_PROPERTY_BOUNDARY_MARKERS = 5000;
	public static final int BASE_MAP_BOOKMARK_MARKERS_GROUP = BASE_PROPERTY_BOUNDARY_MARKERS_GROUP + MAX_PROPERTY_BOUNDARY_MARKERS;
}
