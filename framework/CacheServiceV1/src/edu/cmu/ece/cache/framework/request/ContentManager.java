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

package edu.cmu.ece.cache.framework.request;

import java.util.Iterator;
import java.util.LinkedList;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import edu.cmu.ece.cache.framework.database.AppTable;
import edu.cmu.ece.cache.framework.database.AppTableHelper;
import edu.cmu.ece.cache.framework.database.MasterTableHelper;
import edu.cmu.ece.cache.framework.location.Cell;
import edu.cmu.ece.cache.framework.location.CellCoverage;
import edu.cmu.ece.cache.framework.location.Grid;
import edu.cmu.ece.cache.framework.location.definition.Location;
import edu.cmu.ece.cache.framework.serialization.FileNameGenerator;
import edu.cmu.ece.cache.framework.serialization.Serializer;
import edu.cmu.ece.cache.framework.url.Decoder;

public class ContentManager {
	
	private static final String TAG = ContentManager.class.getName();
	
	private static ContentManager sInstance;
	private static SQLiteDatabase sMasterDB, sAppDB;
	private static Context sContext; // used for serialization
	private static Serializer sSerializer;
	
	private ContentManager(Context context, SQLiteDatabase masterDB, SQLiteDatabase appDB) {
		sContext = context;
		sMasterDB = masterDB;
		sAppDB = appDB;
		sSerializer = new Serializer(sContext);
	}
	
	public static ContentManager getInstance(Context context, SQLiteDatabase masterDB, SQLiteDatabase appDB) {
		if (sInstance == null) {
			sInstance = new ContentManager(context, masterDB, appDB);
		}
		
		return sInstance;
	}

	public String requestContent(String appName, String schema, String request) {
		
		Log.d(TAG, "requestContent called");
		//Log.d(TAG, schema);
		Log.d(TAG, request);
		
		if (sMasterDB == null || !sMasterDB.isOpen()) {
			return null;
		}
		
		if (sAppDB == null || !sAppDB.isOpen()) {
			return null;
		}
		
		Location l = Decoder.decodeToLocation(schema, request);
		
		if (l == null) {
			Log.d(TAG, "Decoded location is null!");
			return null;
		}
		
		Log.d(TAG, l.center().toString());
		
		// now that we have a location, get all related tables from the master database
		LinkedList<AppTable> tables = 
			MasterTableHelper.selectAppLooseLocationCoverage(sMasterDB, appName, l.center());
		
		if (tables == null) {
			return null;
		}
		
		// for each table figure out if it covers the cell
		// if it covers find the smallest cell that covers the region and returns its content
		Cell coveringCell = null;
		double coveringCellSize = -1; 
		AppTable selectedTable = null;
		String content = null;
		
		Iterator<AppTable> it = tables.iterator();
		
		while (it.hasNext()) {
			AppTable t = it.next();
			
			if (t == null) {
				continue;
			}
			
			//Log.d(TAG, "Considering table: " + t.getAppName());
			
			// TODO: is this more efficient that doing a large scale sql lookup?
			// get the grid for t
			Grid g = (Grid) sSerializer.deserialize(
					FileNameGenerator.fileName(t.getAppName(), 
							t.getId(), 
							t.getLevel(), 
							t.getOverlay()));
			
			if (g != null) {
				
				// a fast check on whether the grid covers l
				// if it doesn't we won't bother doing a lookup for the cell
				boolean gridCoverage = CellCoverage.LocationInCell(g.overarchingCell(), l);
				
				//Log.d(TAG, "Determined cell coverage ");
				//Log.d(TAG, "" + gridCoverage);
				
				if (gridCoverage) {
				
					// get the cell that l is in in the grid
					Cell c = g.getCellById(g.getIdByLatLong(l.center()));
					
					if (c != null) {
						
						if (coveringCell == null || coveringCellSize < c.areaMetersSquared()) {
							coveringCell = c;
							coveringCellSize = c.areaMetersSquared();
							selectedTable = t;
						}
					}
					
				}
				
			}
		}
		
		// at this point we should have the smallest cell that covers the location
		// make a call and grab the content. here have to be careful if content comes from
		// an overlay cell
		if (selectedTable.getOverlay() == 0) {
			content = AppTableHelper.selectContent(sAppDB, selectedTable.getAppName(), 
					selectedTable.getId(), coveringCell.getId());
		} else {
			int idOffset = selectedTable.getNumCells();
			content = AppTableHelper.selectContent(sAppDB, selectedTable.getAppName(), 
					selectedTable.getId(), coveringCell.getId() + idOffset);
		}
		
		
		return content;
	}
}
