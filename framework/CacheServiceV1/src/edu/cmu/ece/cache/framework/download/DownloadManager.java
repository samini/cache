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

package edu.cmu.ece.cache.framework.download;

import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Random;

import android.app.AlarmManager;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import edu.cmu.ece.cache.framework.constants.Constants;
import edu.cmu.ece.cache.framework.database.AppTable;
import edu.cmu.ece.cache.framework.database.AppTableHelper;
import edu.cmu.ece.cache.framework.database.MasterTableHelper;
import edu.cmu.ece.cache.framework.http.HttpRequest;
import edu.cmu.ece.cache.framework.location.Cell;
import edu.cmu.ece.cache.framework.url.Encoder;

public class DownloadManager implements Runnable {

	private static final String TAG = DownloadManager.class.getName();
	
	private static DownloadManager sInstance;
	
	private static boolean stop = false;
	
	private static SQLiteDatabase sMasterDB, sAppDB;
	
	private static boolean running = false;;
	
	private DownloadManager(SQLiteDatabase masterDB, SQLiteDatabase appDB) {
		sMasterDB = masterDB;
		sAppDB = appDB;
	}
	
	public static DownloadManager getInstance(SQLiteDatabase masterDB, SQLiteDatabase appDB) {
		if (sInstance == null) {
			sInstance = new DownloadManager(masterDB, appDB);
		}
		
		return sInstance;
	}
	
	public void run() {
		// if something is already running
		// return
		if (isRunning()) {
			Log.d(TAG, "DownloadManager already running!");
			return;
		} else {
			setRunning(true);
		}
		
		stop = false;
		
		// get the tables in priorized order
		PriorityQueue<AppTable> tables = null; 
		LinkedList<LinkedList<AppTable>> priorityChain = null;
		
		while (!stop) {
			// always get a fresh list of tables
			tables = MasterTableHelper.selectAll(sMasterDB);
			priorityChain = Prioritize.prioritize(tables);
			
			if (priorityChain == null || priorityChain.isEmpty()) {
				Log.d(TAG, "PriorityChain is empty!");
				stop = true;
			} else {
				// at this point should have a chain with the 
				// with only appTables that are due for downloading
				
				LinkedList<AppTable> subChain = priorityChain.getFirst();
				
				// this chain finished, remove it, and iterate
				if (subChain == null || subChain.isEmpty()) {
					priorityChain.removeFirst();
					// nothing in this subchain, move to next subchain
					continue;
				}
				
				// process a single download over the subChain
				processSubChain(subChain);
				
			}
			
			// wait for one process of a chain so nothing is being throtteled
			randSleep(Constants.DOWNLOAD_SLEEP_TIME_MS);
		}
		
		Log.d(TAG, "DownloadManager stopping!");
		
		// this step is necessary so another starting thread
		// can operate
		setRunning(false);
	}
	
	private void randSleep(long ms) {
		sleep((long)(new Random().nextDouble() * ms));
	}
	
	private void sleep(long ms){
		try {
			Thread.sleep(ms);
		} catch (java.lang.InterruptedException ex) {

		}
	}
	
	// to start make a thread.start call
	public synchronized void stop() {
		stop = true;
	}

	public synchronized boolean isRunning() {
		return running;
	}

	public synchronized void setRunning(boolean running) {
		this.running = running;
	}
	
	public void processSubChain(LinkedList<AppTable> tables) {
		if (tables == null || tables.isEmpty()) {
			return;
		}
		
		Log.d(TAG, "Processing sub chain");
		
		// do a single download for each table in the chain
		// TODO: this would be better to be each app
		// or each service in the chain, using each table could be hitting
		// the same service provider and result in throttling
		for (int i = 0; i < tables.size(); i++) {
		
			try {
				// get a refreshed view of the table
				AppTable t = MasterTableHelper.selectEntry(sMasterDB, tables.get(i).getId());
				
				if (t == null) {
					tables.remove(i);
					i--;
					continue;
				}
				
				Log.d(TAG, t.getAppName());
				
				// remove the table if it has recently been completely updated
				if (t.getOverlay() == 0 && t.getLastCell() == t.getNumCells() - 1) {
					// update complete is kept in terms of seconds
					if (t.getUpdateCompleteTs() * 1000 + 
							t.getRate() * AlarmManager.INTERVAL_DAY >
							System.currentTimeMillis()) {
						// remove this from the list of tables for download
						Log.d(TAG, "Removing: " + t.getAppName());
						// here want to remove not t
						// but the table at location i
						tables.remove(i);
						i--;
						continue;
					} else {
						// table is refreshing
						singleDownload(t);
					}
				} else if (t.getOverlay() == 1 && 
						t.getLastCell() == 2 * t.getNumCells() - 1 ) {
					if (t.getUpdateCompleteTs() * 1000 + 
							t.getRate() * AlarmManager.INTERVAL_DAY >
							System.currentTimeMillis()) {
						Log.d(TAG, "Removing: " + t.getAppName());
						// here want to remove not t
						// but the table at location i
						tables.remove(i);
						i--;
						continue;
					} else {
						// table is refreshing
						singleDownload(t);
					}
				} else {
					singleDownload(t);
				}
			} catch (Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public void singleDownload(AppTable t) {
		if (t == null) {
			return;
		}
		
		Log.d(TAG, "Single Download for " + t.getAppName());
		
		// a download and update the cell number
		int cellToDownload = t.getLastCell() + 1;
		
		if ((t.getOverlay() == 0 && cellToDownload >= t.getNumCells()) ||
				(t.getOverlay() == 1 && cellToDownload >= 2 * t.getNumCells()) ) {
			cellToDownload = 0;
			
			// reset the tables initial update time
			MasterTableHelper.updateUpdateInitTS(sMasterDB, t.getId());
		}
		
		// get the cell from the appTable
		Cell c = AppTableHelper.selectCell(sAppDB, t.getAppName(), t.getId(), cellToDownload);
		
		// encode its url
		String url = Encoder.encode(t.getApi(), t.getUrl(), c);
		
		//Log.d(TAG, url);
		
		// keep attempting until we can get the content
		String content = HttpRequest.HttpRequestURL(url, Constants.CELL_DOWNLOAD_RETRY);
		
		// insert the content in the app table
		AppTableHelper.updateCellTextContent(sAppDB, t.getAppName(), 
				t.getId(), cellToDownload, content);
		
		// update the last cells number in the master db
		MasterTableHelper.updateLastCell(sMasterDB, t.getId(), cellToDownload);
		
		// if the table has completed its download
		// update the tables completion time
		if (t.getOverlay() == 0 && cellToDownload == t.getNumCells() - 1) {
			MasterTableHelper.updateUpdateCompletionTS(sMasterDB, t.getId());
		}
		
		if (t.getOverlay() == 1 && cellToDownload == 2 * t.getNumCells() - 1) {
			MasterTableHelper.updateUpdateCompletionTS(sMasterDB, t.getId());
		}
	}

}
