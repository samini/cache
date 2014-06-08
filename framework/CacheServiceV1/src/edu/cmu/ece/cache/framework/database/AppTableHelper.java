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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import edu.cmu.ece.cache.framework.constants.Constants;
import edu.cmu.ece.cache.framework.location.Cell;
import edu.cmu.ece.cache.framework.location.Grid;
import edu.cmu.ece.cache.framework.location.LatLong;

public class AppTableHelper {
	public static String tableName(String appName, int tableId) {
		return new StringBuilder().append(appName)
		.append(Constants.UNDERSCORE_SEPARATOR).append(tableId).toString();
	}
	
	public static Cell selectCell(SQLiteDatabase db, String appName, int tableId, int cellId)
	{
		if (db == null || appName == null || appName.equals("")) {
			return null;
		}
		
		if (Constants.MASTER_TABLE_ID == tableId)
		{
			return null;
		} else if (tableId <= 0) {
			return null;
		} else {
		
			Cell retVal = null;
			
			String tableName = tableName(appName, tableId);
			
			String selection = new 
			StringBuilder().append(DatabaseTableColumns.AppTableColumns.CELL_ID).append("=?").toString();
			String[] selectionArgs = {new StringBuilder().append(cellId).toString()};
			
			Cursor c = db.query(tableName, 
					null, selection, selectionArgs, null, null, null);
			
			if (c != null)
			{
				
				if (c.moveToFirst())
				{
					int nwLatIndex = c.getColumnIndex(DatabaseTableColumns.AppTableColumns.NW_LAT);
					int nwLonIndex = c.getColumnIndex(DatabaseTableColumns.AppTableColumns.NW_LON);
					int neLatIndex = c.getColumnIndex(DatabaseTableColumns.AppTableColumns.NE_LAT);
					int neLonIndex = c.getColumnIndex(DatabaseTableColumns.AppTableColumns.NE_LON);
					int seLatIndex = c.getColumnIndex(DatabaseTableColumns.AppTableColumns.SE_LAT);
					int seLonIndex = c.getColumnIndex(DatabaseTableColumns.AppTableColumns.SE_LON);
					int swLatIndex = c.getColumnIndex(DatabaseTableColumns.AppTableColumns.SW_LAT);
					int swLonIndex = c.getColumnIndex(DatabaseTableColumns.AppTableColumns.SW_LON);
					
					LatLong[] vertices = new LatLong[4];
					vertices[Constants.VERTEX_NW_INDEX] = new LatLong(c.getDouble(nwLatIndex),
							c.getDouble(nwLonIndex));
					vertices[Constants.VERTEX_NE_INDEX] = new LatLong(c.getDouble(neLatIndex),
							c.getDouble(neLonIndex));
					vertices[Constants.VERTEX_SE_INDEX] = new LatLong(c.getDouble(seLatIndex),
							c.getDouble(seLonIndex));
					vertices[Constants.VERTEX_SW_INDEX] = new LatLong(c.getDouble(swLatIndex),
							c.getDouble(swLonIndex));
					
					retVal = new Cell(cellId, vertices);
				}
				
				c.close();
			}
			
			return retVal;
		}
	}
	
	public static String selectContent(SQLiteDatabase db, String appName, int tableId, int cellId)
	{
		if (db == null || appName == null || appName.equals("")) {
			return null;
		}
		
		if (Constants.MASTER_TABLE_ID == tableId)
		{
			return null;
		} else if (tableId <= 0) {
			return null;
		} else {
		
			String retVal = null;
			
			String tableName = tableName(appName, tableId);
			
			String selection = new 
			StringBuilder().append(DatabaseTableColumns.AppTableColumns.CELL_ID).append("=?").toString();
			String[] selectionArgs = {new StringBuilder().append(cellId).toString()};
			
			Cursor c = db.query(tableName, 
					null, selection, selectionArgs, null, null, null);
			
			if (c != null)
			{
				
				if (c.moveToFirst())
				{
					int textIndex = 
						c.getColumnIndex(DatabaseTableColumns.AppTableColumns.TEXT_CONTENT);
					
					retVal = c.getString(textIndex);
				}
				
				c.close();
			}
			
			return retVal;
		}
	}
	
	// function that inserts a grid into the appropriate table
	public static boolean insertGrid(SQLiteDatabase db, String appName, int tableId, Grid g, boolean overlay) {
		if (db == null) {
			return false;
		}
		
		if (0 >= tableId) {
			return false;
		}
		
		if (g == null) {
			return false;
		}
		
		// get the name of the table to insert in
		String tableName = tableName(appName, tableId);
		
		int cellIdOffset = overlay ? g.getGridSize() : 0;
		
		db.beginTransaction();
		try {
			for (int i = 0; i < g.getGridSize(); i++) {
				Cell c = g.getCellById(i);
				LatLong[] vertices = c.getVertices();
				
				ContentValues cv = new ContentValues();
				
				cv.put(DatabaseTableColumns.AppTableColumns.CELL_ID, c.getId() + cellIdOffset);
				cv.put(DatabaseTableColumns.AppTableColumns.CENTER_LAT, c.center().getLatitude());
				cv.put(DatabaseTableColumns.AppTableColumns.CENTER_LON, c.center().getLongitude());
				cv.put(DatabaseTableColumns.AppTableColumns.NW_LAT, vertices[Constants.VERTEX_NW_INDEX].getLatitude());
				cv.put(DatabaseTableColumns.AppTableColumns.NW_LON, vertices[Constants.VERTEX_NW_INDEX].getLongitude());
				cv.put(DatabaseTableColumns.AppTableColumns.NE_LAT, vertices[Constants.VERTEX_NE_INDEX].getLatitude());
				cv.put(DatabaseTableColumns.AppTableColumns.NE_LON, vertices[Constants.VERTEX_NE_INDEX].getLongitude());
				cv.put(DatabaseTableColumns.AppTableColumns.SE_LAT, vertices[Constants.VERTEX_SE_INDEX].getLatitude());
				cv.put(DatabaseTableColumns.AppTableColumns.SE_LON, vertices[Constants.VERTEX_SE_INDEX].getLongitude());
				cv.put(DatabaseTableColumns.AppTableColumns.SW_LAT, vertices[Constants.VERTEX_SW_INDEX].getLatitude());
				cv.put(DatabaseTableColumns.AppTableColumns.SW_LON, vertices[Constants.VERTEX_SW_INDEX].getLongitude());
				
				db.insert(tableName, 
						DatabaseTableColumns.AppTableColumns.TEXT_CONTENT, cv);
				
			}
			
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
		
		return true;
	}
	
	// updates the text value of the cell
	// and the time at which this happens
	public static boolean updateCellTextContent(SQLiteDatabase db, String appName, int tableId, int cellId, String text) {
		if (db == null) {
			return false;
		}
		
		if (0 >= tableId) {
			return false;
		}
		
		// get the name of the table to insert in
		String tableName = tableName(appName, tableId);
		
		String whereClause = new StringBuilder().append(
				DatabaseTableColumns.AppTableColumns.CELL_ID).append("=?").toString();
		
		String[] whereArgs = {
				new StringBuilder().append(cellId).toString()
		};
		
		ContentValues cv = new ContentValues();
		cv.put(DatabaseTableColumns.AppTableColumns.TEXT_CONTENT, 
				text);
		cv.put(DatabaseTableColumns.AppTableColumns.TS, 
				System.currentTimeMillis() / 1000);
		
		int numAffectedRows = db.update(tableName, 
				cv, whereClause, whereArgs);
		
		if (numAffectedRows == 1) {
			return true;
		} else {
			return false;
		}
	}
}
