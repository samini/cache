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

package edu.cmu.ece.cache.framework.database;

import java.util.HashMap;

public class DatabaseTableColumns {
	
	private static DatabaseTableColumns sInstance;
	
	// The master table columns
	private HashMap<String, String> masterTableColumns;
	
	// Application table columns
	private HashMap<String, String> appTableColumns;
	
	public static DatabaseTableColumns getInstance()
	{
		if (sInstance == null)
		{
			sInstance = new DatabaseTableColumns();
		}
		
		return sInstance;
	}
	
	// since private cannot have multiple instances
	private DatabaseTableColumns()
	{
		populateMasterTableColumns();
		populateAppTableColumns();
	}
	
	// just based on the master table, a check can be done to see if 
	// the overall regions cached contained the request or not
	private void populateMasterTableColumns()
	{
		masterTableColumns = new HashMap<String, String>();
		
		masterTableColumns.put(MasterTableColumns.ID, "INTEGER PRIMARY KEY");
		// application name
		masterTableColumns.put(MasterTableColumns.APP_NAME, "TEXT NOT NULL");
		// registration time stamp
		masterTableColumns.put(MasterTableColumns.REG_TS, "INTEGER NOT NULL");
		// service provider URL
		masterTableColumns.put(MasterTableColumns.URL, "TEXT NOT NULL");
		// other paramters in the URL that the dev wants to specify
		// these parameters should all be set as %opparam1%, %oppparam2%,... (optional parameter)
		// for now we won't implement this.
		// it would be like x={1,2,3};y={1};z={restaurant,pizza,...}
		masterTableColumns.put(MasterTableColumns.URL_PARAMETERS, "TEXT");
		// create the API type as a text so that it can be extended
		masterTableColumns.put(MasterTableColumns.API, "TEXT NOT NULL");
		// cell width
		masterTableColumns.put(MasterTableColumns.CELL_WIDTH, "INTEGER NOT NULL");
		// cell height
		masterTableColumns.put(MasterTableColumns.CELL_HEIGHT, "INTEGER NOT NULL");
		// last full update completion time stamp
		masterTableColumns.put(MasterTableColumns.UP_COM_TS, "INTEGER");
		// last update initial time stamp
		masterTableColumns.put(MasterTableColumns.UP_INIT_TS, "INTEGER");
		// content priority 1-10, default is at 5
		masterTableColumns.put(MasterTableColumns.PRIORITY, "INTEGER DEFAULT 5");
		// update rate in terms of days, default is once a week
		masterTableColumns.put(MasterTableColumns.RATE, "INTEGER DEFAULT 7");
		masterTableColumns.put(MasterTableColumns.ADDRESS, "TEXT");
		// the center lat/long
		masterTableColumns.put(MasterTableColumns.CENTER_LAT, "REAL DEFAULT -360.0");
		masterTableColumns.put(MasterTableColumns.CENTER_LON, "REAL DEFAULT -360.0");
		// radius in miles
		masterTableColumns.put(MasterTableColumns.RADIUS, "REAL DEFAULT 5"); 
		// the starting NW point, default is null
		masterTableColumns.put(MasterTableColumns.NW_LAT, "REAL DEFAULT -360.0");
		masterTableColumns.put(MasterTableColumns.NW_LON, "REAL DEFAULT -360.0");
		// decided to add the other points too, since they are easy to compute
		// and not worth computing for every check
		masterTableColumns.put(MasterTableColumns.NE_LAT, "REAL DEFAULT -360.0");
		masterTableColumns.put(MasterTableColumns.NE_LON, "REAL DEFAULT -360.0");
		masterTableColumns.put(MasterTableColumns.SE_LAT, "REAL DEFAULT -360.0");
		masterTableColumns.put(MasterTableColumns.SE_LON, "REAL DEFAULT -360.0");
		// the starting SE point, default is null
		masterTableColumns.put(MasterTableColumns.SW_LAT, "REAL DEFAULT -360.0");
		masterTableColumns.put(MasterTableColumns.SW_LON, "REAL DEFAULT -360.0");
		//masterTableColumns.put(MasterTableColumns.NUM_CELLS, "INTEGER DEFAULT -1");
		// last updated cell. it is assumed that all prior cells were updated
		masterTableColumns.put(MasterTableColumns.NUM_CELLS, "INTEGER DEFAULT -1");
		masterTableColumns.put(MasterTableColumns.LAST_CELL, "INTEGER DEFAULT -1");
		// whether multi level or not, default is false
		masterTableColumns.put(MasterTableColumns.MULTI_LEVEL, "BOOLEAN DEFAULT 0 NOT NULL");
		// number of levels
		//masterTableColumns.put(MasterTableColumns.LEVELS, "BOOLEAN DEFAULT 0 NOT NULL");
		masterTableColumns.put(MasterTableColumns.LEVEL, "INTEGER DEFAULT -1");
		// whether there is an overlay or not for each level, default false
		masterTableColumns.put(MasterTableColumns.OVERLAY, "BOOLEAN DEFAULT 0 NOT NULL");
	}
	
	private void populateAppTableColumns()
	{
		appTableColumns = new HashMap<String, String>();
		
		appTableColumns.put(AppTableColumns.ID, "INTEGER PRIMARY KEY");
		// time stamp of last update
		// since timestamps are done at download time, and not at
		// application table fill in time, set this value can be null
		//appTableColumns.put(AppTableColumns.TS, "INTEGER NOT NULL");
		appTableColumns.put(AppTableColumns.TS, "INTEGER");
		//appTableColumns.put(AppTableColumns.LEVEL, "INTEGER DEFAULT 0 NOT NULL");
		appTableColumns.put(AppTableColumns.CELL_ID, "INTEGER NOT NULL");
		// all the values below are known for each cell and have to be set
		appTableColumns.put(AppTableColumns.CENTER_LAT, "REAL NOT NULL");
		appTableColumns.put(AppTableColumns.CENTER_LON, "REAL NOT NULL");
		appTableColumns.put(AppTableColumns.NW_LAT, "REAL NOT NULL");
		appTableColumns.put(AppTableColumns.NW_LON, "REAL NOT NULL");
		appTableColumns.put(AppTableColumns.NE_LAT, "REAL NOT NULL");
		appTableColumns.put(AppTableColumns.NE_LON, "REAL NOT NULL");
		appTableColumns.put(AppTableColumns.SE_LAT, "REAL NOT NULL");
		appTableColumns.put(AppTableColumns.SE_LON, "REAL NOT NULL");
		appTableColumns.put(AppTableColumns.SW_LAT, "REAL NOT NULL");
		appTableColumns.put(AppTableColumns.SW_LON, "REAL NOT NULL");
		// the content may be null in case the request does not offer anything
		appTableColumns.put(AppTableColumns.TEXT_CONTENT, "TEXT");
		// content that may not be stored as text
		appTableColumns.put(AppTableColumns.BLOB_CONTENT, "BLOB");
	}

	// no setters
	public HashMap<String, String> getMasterTableColumns() {
		return masterTableColumns;
	}
	
	public HashMap<String, String> getAppTableColumns() {
		return appTableColumns;
	}
	
	public class MasterTableColumns
	{
		public static final String ID = "ID";
		public static final String APP_NAME = "APP_NAME";
		public static final String REG_TS = "REG_TS";
		public static final String URL = "URL";
		public static final String URL_PARAMETERS = "URL_PARAMETERS";
		public static final String API = "API";
		public static final String CELL_WIDTH = "CELL_WIDTH";
		public static final String CELL_HEIGHT = "CELL_HEIGHT";
		public static final String UP_COM_TS = "UP_COM_TS";
		public static final String UP_INIT_TS = "UP_INIT_TS";
		public static final String PRIORITY = "PRIORITY";
		public static final String RATE = "RATE";
		public static final String ADDRESS = "ADDRESS";
		public static final String CENTER_LAT = "CENTER_LAT";
		public static final String CENTER_LON = "CENTER_LON";
		public static final String RADIUS = "RADIUS";
		public static final String NW_LAT = "NW_LAT";
		public static final String NW_LON = "NW_LON";
		public static final String NE_LAT = "NE_LAT";
		public static final String NE_LON = "NE_LON";
		public static final String SE_LAT = "SE_LAT";
		public static final String SE_LON = "SE_LON";
		public static final String SW_LAT = "SW_LAT";
		public static final String SW_LON = "SW_LON";
		public static final String NUM_CELLS = "NUM_CELLS";
		public static final String LAST_CELL = "LAST_CELL";
		public static final String MULTI_LEVEL = "MULTI_LEVEL";
		public static final String LEVEL = "LEVEL";
		public static final String OVERLAY = "OVERLAY";
	}
	
	public class AppTableColumns
	{
		public static final String ID = "ID";
		public static final String TS = "TS";
		public static final String CELL_ID = "CELL_ID";
		public static final String CENTER_LAT = "CENTER_LAT";
		public static final String CENTER_LON = "CENTER_LON";
		public static final String NW_LAT = "NW_LAT";
		public static final String NW_LON = "NW_LON";
		public static final String NE_LAT = "NE_LAT";
		public static final String NE_LON = "NE_LON";
		public static final String SE_LAT = "SE_LAT";
		public static final String SE_LON = "SE_LON";
		public static final String SW_LAT = "SW_LAT";
		public static final String SW_LON = "SW_LON";
		public static final String TEXT_CONTENT = "TEXT_CONTENT";
		public static final String BLOB_CONTENT = "BLOB_CONTENT";
	}
}
