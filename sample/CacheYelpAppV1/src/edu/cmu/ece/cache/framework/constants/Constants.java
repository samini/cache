/*
   Copyright 2010 Shahriyar Amini

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package edu.cmu.ece.cache.framework.constants;

import android.app.AlarmManager;

public class Constants {

	public static int CELL_DOWNLOAD_RETRY = 14; // attemps download 15 times
	public static long DOWNLOAD_SLEEP_TIME_MS = 3000; // 3 seconds
	
	public static int INT_TRUE = 1;
	public static int INT_FALSE = 0;
	
	public static final double SQRT_TWO = Math.sqrt(2.0);
	
	public static final double MILE_TO_KM = 1.609344;
	public static final double MILE_TO_METERS = 1609.344;
	public static final double KM_TO_METERS = 1000;
	
	public static final String CACHE_FRAMEWORK_MASTER_DB = "cache_framework_master.db";
	
	public static final String CACHE_FRAMEWORK_APP_DB = "cache_framework_app.db";
	
	public static final String CACHE_FRAMEWORK_MASTER_TABLE = "master";
	
	public static final int MASTER_TABLE_ID = -1;
	
	public static final int INVALID_ROW_ID = -2;
	
	public static final int INVALID_OVERLAY = -1;
	
	public static final String APP_TABLE_ID_EXTRA = "APP_TABLE_ID_EXTRA";
	
	public static final char SPACE_SEPARATOR = ' ';
	public static final char POUND_SEPARATOR = '#';
	public static final char COMMA_SEPARATOR = ',';
	public static final char UNDERSCORE_SEPARATOR = '_';
	
	// the application table names will be based on application
	// the application id assigned in master table
	// each application has a table per location assigned for caching
	// all levels are put in the same table
	// overlays are put in the same table with the appropriate table name
	
	public static final int CACHE_FRAMEWORK_VERSION = 1;
	
	// if battery threshold is lower than below, don't download anything
	public static final double BATTERY_OKAY_THRESHOLD = 0.2;
	
	// API Types
	public static final String API_SLL = "SLL";
	public static final String API_SLLR = "SLLR"; // w/ radius
	public static final String API_BB = "BB";
	public static final String API_LLR = "LLR";
	public static final String API_SLLS = "SLLS"; // w/ span
	
	// use # so it is known to be not part of URL
	// API markers
	public static final char API_MARKER = POUND_SEPARATOR;
	public static final String API_LAT_DEG = "#lat_d#";
	public static final String API_LON_DEG = "#lon_d#";
	public static final String API_LAT_RAD = "#lat_r#";
	public static final String API_LON_RAD = "#lon_r#";
	// note some apis may use top-left for NW and bottom-right for SE and so forth
	public static final String API_NW_LAT = "#nw_lat_d#";
	public static final String API_NW_LON = "#nw_lon_d#";
	public static final String API_NE_LAT = "#ne_lat_d#";
	public static final String API_NE_LON = "#ne_lon_d#";
	public static final String API_SE_LAT = "#se_lat_d#";
	public static final String API_SE_LON = "#se_lon_d#";
	public static final String API_SW_LAT = "#sw_lat_d#";
	public static final String API_SW_LON = "#sw_lon_d#";
	public static final String API_RADIUS_METERS = "#radius_meters";
	public static final String API_RADIUS_MILES = "#radius_miles";
	public static final String API_RADIUS_KM = "#radius_km";
	public static final String API_LAT_MIN_DEG = "#lat_min_d#";
	public static final String API_LAT_MAX_DEG = "#lat_max_d#";
	public static final String API_LON_MIN_DEG = "#lon_min_d#";
	public static final String API_LON_MAX_DEG = "#lon_max_d#";
	public static final String API_SPAN_LAT = "#span_lat_d#";
	public static final String API_SPAN_LON = "#span_lot_d#";
	
	public static final int VERTEX_NW_INDEX = 0;
	public static final int VERTEX_NE_INDEX = 1;
	public static final int VERTEX_SE_INDEX = 2;
	public static final int VERTEX_SW_INDEX = 3;
	
	// request code for DownloadBroadcastReceiver
	public static int DOWNLOAD_REQUEST_CODE = 0x0000FF01;
	
	// units of time
	public static final long INTERVAL_ONE_MINUTE = 60 * 1000;
	public static final long INTERVAL_PRIORITIZATION = AlarmManager.INTERVAL_DAY;
	
	// lower is better
	public static final int MIN_PRIORITY = 0;
	public static final int MAX_PRIORITY = 9;

}
