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

import java.util.LinkedList;
import java.util.PriorityQueue;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import edu.cmu.ece.cache.framework.constants.Constants;
import edu.cmu.ece.cache.framework.location.LatLong;

public class MasterTableHelper {
	public static AppTable selectEntry(SQLiteDatabase db, int tableId)
	{
		if (db == null || !db.isOpen()) {
			return null;
		}
		
		if (Constants.MASTER_TABLE_ID == tableId)
		{
			return null;
		} else if (tableId <= 0) {
			return null;
		} else {
		
			AppTable retVal = null;
			
			String selection = new 
			StringBuilder().append(DatabaseTableColumns.MasterTableColumns.ID).append("=?").toString();
			String[] selectionArgs = {new StringBuilder().append(tableId).toString()};
			
			Cursor c = db.query(Constants.CACHE_FRAMEWORK_MASTER_TABLE, 
					null, selection, selectionArgs, null, null, null);
			
			if (c != null)
			{
				
				if (c.moveToFirst())
				{
					int idIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.ID);
					int appNameIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.APP_NAME);
					int regTsIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.REG_TS);
					int urlIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.URL);
					int urlParamsIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.URL_PARAMETERS);
					int apiIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.API);
					int cellWidthIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.CELL_WIDTH);
					int cellHeightIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.CELL_HEIGHT);
					int updateCompleteTsIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.UP_COM_TS);
					int updateInitTsIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.UP_INIT_TS);
					int priorityIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.PRIORITY);
					int rateIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.RATE);
					int addressIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.ADDRESS);
					int centerLatIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.CENTER_LAT);
					int centerLonIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.CENTER_LON);
					int radiusLonIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.RADIUS);
					int nwLatIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.NW_LAT);
					int nwLonIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.NW_LON);
					int neLatIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.NE_LAT);
					int neLonIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.NE_LON);
					int seLatIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.SE_LAT);
					int seLonIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.SE_LON);
					int swLatIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.SW_LAT);
					int swLonIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.SW_LON);
					int numCellsIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.NUM_CELLS);
					int lastCellIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.LAST_CELL);
					int multiLevelIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.MULTI_LEVEL);
					int levelIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.LEVEL);
					int overlayIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.OVERLAY);
					
					
					retVal = new AppTable(
							c.getInt(idIndex), 
							c.getString(appNameIndex), 
							c.getLong(regTsIndex), 
							c.getString(urlIndex),
							c.getString(urlParamsIndex), 
							c.getString(apiIndex), 
							c.getDouble(cellWidthIndex), 
							c.getDouble(cellHeightIndex),
							c.getLong(updateCompleteTsIndex), 
							c.getLong(updateInitTsIndex), 
							c.getInt(priorityIndex), 
							c.getInt(rateIndex),
							c.getString(addressIndex), 
							new LatLong(c.getDouble(centerLatIndex), c.getDouble(centerLonIndex)), 
							c.getDouble(radiusLonIndex), 
							new LatLong(c.getDouble(nwLatIndex), c.getDouble(nwLonIndex)),
							new LatLong(c.getDouble(neLatIndex), c.getDouble(neLonIndex)),
							new LatLong(c.getDouble(seLatIndex), c.getDouble(seLonIndex)),
							new LatLong(c.getDouble(swLatIndex), c.getDouble(swLonIndex)),
							c.getInt(numCellsIndex),
							c.getInt(lastCellIndex), 
							c.getInt(multiLevelIndex),
							c.getInt(levelIndex), 
							c.getInt(overlayIndex));
				}
				
				c.close();
			}
			
			return retVal;
		}
	}
	
	// since this is a priority queue implementation, it is ordered by the AppTable
	// comparator, which is based on the rate and the priority
	public static PriorityQueue<AppTable> selectAll(SQLiteDatabase db) {
		if (db == null || !db.isOpen()) {
			return null;
		}
		
		PriorityQueue<AppTable> retVal = new PriorityQueue<AppTable>();
		
		Cursor c = db.query(Constants.CACHE_FRAMEWORK_MASTER_TABLE, 
				null, null, null, null, null, null);
		
		AppTable tmp;
		
		if (c != null)
		{
			
			if (c.moveToFirst())
			{
				do {
					int idIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.ID);
					int appNameIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.APP_NAME);
					int regTsIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.REG_TS);
					int urlIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.URL);
					int urlParamsIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.URL_PARAMETERS);
					int apiIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.API);
					int cellWidthIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.CELL_WIDTH);
					int cellHeightIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.CELL_HEIGHT);
					int updateCompleteTsIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.UP_COM_TS);
					int updateInitTsIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.UP_INIT_TS);
					int priorityIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.PRIORITY);
					int rateIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.RATE);
					int addressIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.ADDRESS);
					int centerLatIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.CENTER_LAT);
					int centerLonIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.CENTER_LON);
					int radiusLonIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.RADIUS);
					int nwLatIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.NW_LAT);
					int nwLonIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.NW_LON);
					int neLatIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.NE_LAT);
					int neLonIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.NE_LON);
					int seLatIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.SE_LAT);
					int seLonIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.SE_LON);
					int swLatIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.SW_LAT);
					int swLonIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.SW_LON);
					int numCellsIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.NUM_CELLS);
					int lastCellIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.LAST_CELL);
					int multiLevelIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.MULTI_LEVEL);
					int levelIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.LEVEL);
					int overlayIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.OVERLAY);
					
					
					tmp = new AppTable(
							c.getInt(idIndex), 
							c.getString(appNameIndex), 
							c.getLong(regTsIndex), 
							c.getString(urlIndex),
							c.getString(urlParamsIndex), 
							c.getString(apiIndex), 
							c.getDouble(cellWidthIndex), 
							c.getDouble(cellHeightIndex),
							c.getLong(updateCompleteTsIndex), 
							c.getLong(updateInitTsIndex), 
							c.getInt(priorityIndex), 
							c.getInt(rateIndex),
							c.getString(addressIndex), 
							new LatLong(c.getDouble(centerLatIndex), c.getDouble(centerLonIndex)), 
							c.getDouble(radiusLonIndex), 
							new LatLong(c.getDouble(nwLatIndex), c.getDouble(nwLonIndex)),
							new LatLong(c.getDouble(neLatIndex), c.getDouble(neLonIndex)),
							new LatLong(c.getDouble(seLatIndex), c.getDouble(seLonIndex)),
							new LatLong(c.getDouble(swLatIndex), c.getDouble(swLonIndex)),
							c.getInt(numCellsIndex),
							c.getInt(lastCellIndex), 
							c.getInt(multiLevelIndex),
							c.getInt(levelIndex), 
							c.getInt(overlayIndex));
					
					retVal.add(tmp);
					
				} while (c.moveToNext());
			}
			
			c.close();
			
			return retVal;
		}
		
		return null; // if no cursor
	}
	
	// selects all tables that have to do with appName application
	public static AppTable[] selectApp(SQLiteDatabase db, String appName) {
		if (db == null || !db.isOpen()) {
			return null;
		}
		
		//LinkedList<AppTable> retVal = new LinkedList<AppTable>();
		AppTable[] retVal = null;
		
		String selection = new 
		StringBuilder().append(DatabaseTableColumns.MasterTableColumns.APP_NAME).append("=?").toString();
		String[] selectionArgs = {new StringBuilder().append(appName).toString()};
		
		Cursor c = db.query(Constants.CACHE_FRAMEWORK_MASTER_TABLE, 
				null, selection, selectionArgs, null, null, null);
		
		AppTable tmp;
		
		if (c != null)
		{
			
			if (c.moveToFirst())
			{
				int count = c.getCount();
				retVal = new AppTable[count];
				int i = 0;
				
				do {
					int idIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.ID);
					int appNameIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.APP_NAME);
					int regTsIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.REG_TS);
					int urlIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.URL);
					int urlParamsIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.URL_PARAMETERS);
					int apiIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.API);
					int cellWidthIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.CELL_WIDTH);
					int cellHeightIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.CELL_HEIGHT);
					int updateCompleteTsIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.UP_COM_TS);
					int updateInitTsIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.UP_INIT_TS);
					int priorityIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.PRIORITY);
					int rateIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.RATE);
					int addressIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.ADDRESS);
					int centerLatIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.CENTER_LAT);
					int centerLonIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.CENTER_LON);
					int radiusLonIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.RADIUS);
					int nwLatIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.NW_LAT);
					int nwLonIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.NW_LON);
					int neLatIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.NE_LAT);
					int neLonIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.NE_LON);
					int seLatIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.SE_LAT);
					int seLonIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.SE_LON);
					int swLatIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.SW_LAT);
					int swLonIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.SW_LON);
					int numCellsIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.NUM_CELLS);
					int lastCellIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.LAST_CELL);
					int multiLevelIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.MULTI_LEVEL);
					int levelIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.LEVEL);
					int overlayIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.OVERLAY);
					
					
					tmp = new AppTable(
							c.getInt(idIndex), 
							c.getString(appNameIndex), 
							c.getLong(regTsIndex), 
							c.getString(urlIndex),
							c.getString(urlParamsIndex), 
							c.getString(apiIndex), 
							c.getDouble(cellWidthIndex), 
							c.getDouble(cellHeightIndex),
							c.getLong(updateCompleteTsIndex), 
							c.getLong(updateInitTsIndex), 
							c.getInt(priorityIndex), 
							c.getInt(rateIndex),
							c.getString(addressIndex), 
							new LatLong(c.getDouble(centerLatIndex), c.getDouble(centerLonIndex)), 
							c.getDouble(radiusLonIndex), 
							new LatLong(c.getDouble(nwLatIndex), c.getDouble(nwLonIndex)),
							new LatLong(c.getDouble(neLatIndex), c.getDouble(neLonIndex)),
							new LatLong(c.getDouble(seLatIndex), c.getDouble(seLonIndex)),
							new LatLong(c.getDouble(swLatIndex), c.getDouble(swLonIndex)),
							c.getInt(numCellsIndex),
							c.getInt(lastCellIndex), 
							c.getInt(multiLevelIndex),
							c.getInt(levelIndex), 
							c.getInt(overlayIndex));
					
					retVal[i] = tmp;
					i++;
					
				} while (c.moveToNext());
			}
			
			c.close();
			
			return retVal;
		}
		
		return null; // if no cursor
	}
	
	// select tables that can field a possible location query
	public static LinkedList<AppTable> selectAppLooseLocationCoverage(SQLiteDatabase db, String appName, LatLong l) {
		
		if (db == null || !db.isOpen()) {
			return null;
		}
		
		LinkedList<AppTable> retVal = null;
		
		// first get a list of appTables for this appName
		AppTable[] tables = selectApp(db, appName);
		
		if (tables == null || tables.length <= 0) {
			return null;
		}
		
		retVal = new LinkedList<AppTable>();
		
		for (int i = 0; i < tables.length; i++) {
			if (tables[i] != null) {
				
				// if a loose location check does not the location of interest
				// we save quite a bit of computation time
				if (tables[i].looseLocationCoverage(l)) {
					retVal.add(tables[i]);
				}
				
			}
		}
		
		return retVal;
	}
	
	public static int insertEntry(SQLiteDatabase db, String appName, String url,
			String api, double cellWidth,
			double cellHeight, int priority, int rate, boolean multiLevel,
			boolean overlay) {
		
		if (db == null || !db.isOpen()) {
			return Constants.INVALID_ROW_ID;
		}
		
		// the column hack puts a null in a certain column so insert is not empty
		// this is part of android SQLiteDatabase API, I chose the URL params to be the
		// null column since we are not using it currently
		
		// create an entry in the master table
		// for this table
		
		ContentValues cv = new ContentValues();
		cv.put(DatabaseTableColumns.MasterTableColumns.API, api);
		cv.put(DatabaseTableColumns.MasterTableColumns.APP_NAME, appName);
		cv.put(DatabaseTableColumns.MasterTableColumns.CELL_HEIGHT, cellHeight);
		cv.put(DatabaseTableColumns.MasterTableColumns.CELL_WIDTH, cellWidth);
		cv.put(DatabaseTableColumns.MasterTableColumns.LAST_CELL, -1);
		cv.put(DatabaseTableColumns.MasterTableColumns.MULTI_LEVEL, multiLevel);
		cv.put(DatabaseTableColumns.MasterTableColumns.OVERLAY, overlay);
		cv.put(DatabaseTableColumns.MasterTableColumns.PRIORITY, priority);
		cv.put(DatabaseTableColumns.MasterTableColumns.RATE, rate);
		cv.put(DatabaseTableColumns.MasterTableColumns.REG_TS, System.currentTimeMillis() / 1000);
		cv.put(DatabaseTableColumns.MasterTableColumns.URL, url);
		
		long rowId = db.insert(Constants.CACHE_FRAMEWORK_MASTER_TABLE, 
				DatabaseTableColumns.MasterTableColumns.URL_PARAMETERS, cv);
		
		return (int) rowId;
	}
	
	public static boolean deleteEntry(SQLiteDatabase db, int tableId) {
		if (db == null || !db.isOpen()) {
			return false;
		}
		
		if (Constants.MASTER_TABLE_ID == tableId)
		{
			return false;
		} else if (0 >= tableId) {
			return false;
		}
		
		// delete entry from the master table
		// and delete table from the app db
		String whereClause = new StringBuilder().append(DatabaseTableColumns.MasterTableColumns.ID).append("=?").toString();
		String[] whereArgs = {new StringBuilder().append(tableId).toString()};
		
		// delete the entry from the master table
		db.delete(Constants.CACHE_FRAMEWORK_MASTER_TABLE, whereClause, whereArgs);
		
		return true;
		
	}
	
	// returns the name of a table based on its id
	// mainly used for app tables
	public static String selectTableName(SQLiteDatabase db, int tableId)
	{
		if (db == null || !db.isOpen()) {
			return null;
		}
		
		if (Constants.MASTER_TABLE_ID == tableId)
		{
			return Constants.CACHE_FRAMEWORK_MASTER_TABLE;
		} else if (tableId <= 0) {
			return null;
		} else {
			String selection = new 
			StringBuilder().append(DatabaseTableColumns.MasterTableColumns.ID).append("=?").toString();
			String[] selectionArgs = {new StringBuilder().append(tableId).toString()};
			String[] columns = {DatabaseTableColumns.MasterTableColumns.APP_NAME};
			Cursor c = db.query(Constants.CACHE_FRAMEWORK_MASTER_TABLE, 
					columns, selection, selectionArgs, null, null, null);
			
			if (c != null)
			{
				String tableName = null;
				
				if (c.moveToFirst())
				{
					int columnIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.APP_NAME);
					tableName = AppTableHelper.tableName(c.getString(columnIndex), tableId);
				}
				
				c.close();
				return tableName;
			}
		}
		
		return null;
	}
	
	public static int selectOverlay(SQLiteDatabase db, int tableId) {
		if (db == null || !db.isOpen()) {
			return Constants.INVALID_OVERLAY;
		}
		
		if (Constants.MASTER_TABLE_ID == tableId)
		{
			return Constants.INVALID_OVERLAY;
		} else if (tableId <= 0) {
			return Constants.INVALID_OVERLAY;
		} else {
			String selection = new 
			StringBuilder().append(DatabaseTableColumns.MasterTableColumns.ID).append("=?").toString();
			String[] selectionArgs = {new StringBuilder().append(tableId).toString()};
			String[] columns = {DatabaseTableColumns.MasterTableColumns.OVERLAY};
			Cursor c = db.query(Constants.CACHE_FRAMEWORK_MASTER_TABLE, 
					columns, selection, selectionArgs, null, null, null);
			
			if (c != null)
			{
				int overlay = Constants.INVALID_OVERLAY;
				
				if (c.moveToFirst())
				{
					int columnIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.OVERLAY);
					overlay = c.getInt(columnIndex);
				}
				
				c.close();
				return overlay;
			}
		}
		
		return Constants.INVALID_OVERLAY;
	}
	
	public static int appExists(SQLiteDatabase db, String appName) {
		if (db == null || !db.isOpen()) {
			return Constants.INVALID_ROW_ID;
		}
		
		// check to see if the app has already been registered,
		// if it is just return true
		// the dev can save this as a preference but make the check dev independent
		String selection = new 
		StringBuilder().append(DatabaseTableColumns.MasterTableColumns.APP_NAME).append("=?").toString();
		String[] selectionArgs = {appName};
		String[] columns = {DatabaseTableColumns.MasterTableColumns.ID};
		Cursor c = db.query(Constants.CACHE_FRAMEWORK_MASTER_TABLE, 
				columns, selection, selectionArgs, null, null, null);
		
		int rowId = Constants.INVALID_ROW_ID;
		
		if (c != null)
		{
			if (c.moveToFirst())
			{
				// a positive >= 1 rowId is returned if an app of such name already exists
				rowId = c.getInt(c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.ID));
				
				if (rowId <= 0) {
					rowId = Constants.INVALID_ROW_ID;
				}
			}
			
			c.close();
		}
		
		return Constants.INVALID_ROW_ID;
	}
	
	public static boolean updateGeography(SQLiteDatabase db, int tableId, String address, 
			double centerLat, double centerLon, double radius)
	{
		if (db == null || !db.isOpen()) {
			return false;
		}
		
		String whereClause = new StringBuilder().append(
				DatabaseTableColumns.MasterTableColumns.ID).append("=?").toString();
		
		String[] whereArgs = {
				new StringBuilder().append(tableId).toString()
		};
		
		ContentValues cv = new ContentValues();
		cv.put(DatabaseTableColumns.MasterTableColumns.ADDRESS, address);
		cv.put(DatabaseTableColumns.MasterTableColumns.CENTER_LAT, centerLat);
		cv.put(DatabaseTableColumns.MasterTableColumns.CENTER_LON, centerLon);
		cv.put(DatabaseTableColumns.MasterTableColumns.RADIUS, radius);
		
		int numAffectedRows = db.update(Constants.CACHE_FRAMEWORK_MASTER_TABLE, 
				cv, whereClause, whereArgs);
		
		if (numAffectedRows == 1) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean updateGeography(SQLiteDatabase db, int tableId, String address, 
			double centerLat, double centerLon, double radius,
			double nwLat, double nwLon,
			double neLat, double neLon,
			double seLat, double seLon,
			double swLat, double swLon,
			double numCells,
			int level)
	{
		if (db == null || !db.isOpen()) {
			return false;
		}
		
		String whereClause = new StringBuilder().append(
				DatabaseTableColumns.MasterTableColumns.ID).append("=?").toString();
		
		String[] whereArgs = {
				new StringBuilder().append(tableId).toString()
		};
		
		ContentValues cv = new ContentValues();
		cv.put(DatabaseTableColumns.MasterTableColumns.ADDRESS, address);
		cv.put(DatabaseTableColumns.MasterTableColumns.CENTER_LAT, centerLat);
		cv.put(DatabaseTableColumns.MasterTableColumns.CENTER_LON, centerLon);
		cv.put(DatabaseTableColumns.MasterTableColumns.RADIUS, radius);
		cv.put(DatabaseTableColumns.MasterTableColumns.NW_LAT, nwLat);
		cv.put(DatabaseTableColumns.MasterTableColumns.NW_LON, nwLon);
		cv.put(DatabaseTableColumns.MasterTableColumns.NE_LAT, neLat);
		cv.put(DatabaseTableColumns.MasterTableColumns.NE_LON, neLon);
		cv.put(DatabaseTableColumns.MasterTableColumns.SE_LAT, seLat);
		cv.put(DatabaseTableColumns.MasterTableColumns.SE_LON, seLon);
		cv.put(DatabaseTableColumns.MasterTableColumns.SW_LAT, swLat);
		cv.put(DatabaseTableColumns.MasterTableColumns.SW_LON, swLon);
		cv.put(DatabaseTableColumns.MasterTableColumns.NUM_CELLS, numCells);
		cv.put(DatabaseTableColumns.MasterTableColumns.LEVEL, level);
		
		int numAffectedRows = db.update(Constants.CACHE_FRAMEWORK_MASTER_TABLE, 
				cv, whereClause, whereArgs);
		
		if (numAffectedRows == 1) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean updateLastCell(SQLiteDatabase db, int tableId, int lastCell) {
		if (db == null || !db.isOpen()) {
			return false;
		}
		
		String whereClause = new StringBuilder().append(
				DatabaseTableColumns.MasterTableColumns.ID).append("=?").toString();
		
		String[] whereArgs = {
				new StringBuilder().append(tableId).toString()
		};
		
		ContentValues cv = new ContentValues();
		cv.put(DatabaseTableColumns.MasterTableColumns.LAST_CELL, lastCell);
		
		int numAffectedRows = db.update(Constants.CACHE_FRAMEWORK_MASTER_TABLE, 
				cv, whereClause, whereArgs);
		
		if (numAffectedRows == 1) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean updateUpdateCompletionTS(SQLiteDatabase db, int tableId) {
		if (db == null || !db.isOpen()) {
			return false;
		}
		
		String whereClause = new StringBuilder().append(
				DatabaseTableColumns.MasterTableColumns.ID).append("=?").toString();
		
		String[] whereArgs = {
				new StringBuilder().append(tableId).toString()
		};
		
		ContentValues cv = new ContentValues();
		cv.put(DatabaseTableColumns.MasterTableColumns.UP_COM_TS, 
				System.currentTimeMillis() / 1000);
		
		int numAffectedRows = db.update(Constants.CACHE_FRAMEWORK_MASTER_TABLE, 
				cv, whereClause, whereArgs);
		
		if (numAffectedRows == 1) {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean updateUpdateInitTS(SQLiteDatabase db, int tableId) {
		if (db == null || !db.isOpen()) {
			return false;
		}
		
		String whereClause = new StringBuilder().append(
				DatabaseTableColumns.MasterTableColumns.ID).append("=?").toString();
		
		String[] whereArgs = {
				new StringBuilder().append(tableId).toString()
		};
		
		ContentValues cv = new ContentValues();
		cv.put(DatabaseTableColumns.MasterTableColumns.UP_INIT_TS, 
				System.currentTimeMillis() / 1000);
		
		int numAffectedRows = db.update(Constants.CACHE_FRAMEWORK_MASTER_TABLE, 
				cv, whereClause, whereArgs);
		
		if (numAffectedRows == 1) {
			return true;
		} else {
			return false;
		}
	}
}
