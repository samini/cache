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

import android.app.AlarmManager;

import edu.cmu.ece.cache.framework.database.AppTable;

public class Prioritize {
	
	// returns a chain of chains. in each sub-chain
	// all same priority values are placed
	// this way they can easily be interleaved to 
	// reduce pressure on single service provider
	public static LinkedList<LinkedList<AppTable>> prioritize(PriorityQueue<AppTable> tables) {
		
		if (tables == null || tables.isEmpty()) {
			return null;
		}
		
		LinkedList<LinkedList<AppTable>> retVal = new LinkedList<LinkedList<AppTable>>();
		
		AppTable prevTable = null;
		AppTable currTable = null;
		
		do {
			currTable = tables.poll();
			
			// if the current table does not have it's center set, pass it
			// cannot download for a table without location
			if (currTable.getCenter() == null ||
					currTable.getCenter().isInvalid()) {
				continue;
			}
			
			// if the current table is not due for re-download, pass it
			// we first check if there was a complete pass based on the last
			// downloaded cell being the last cell in the grid
			if (currTable.getOverlay() == 0 && currTable.getLastCell() == currTable.getNumCells() - 1) {
				// update complete is kept in terms of seconds
				if (currTable.getUpdateCompleteTs() * 1000 + 
						currTable.getRate() * AlarmManager.INTERVAL_DAY >
						System.currentTimeMillis()) {
					continue;
				}
			} else if (currTable.getOverlay() == 1 && 
					currTable.getLastCell() == 2 * currTable.getNumCells() - 1 ) {
				if (currTable.getUpdateCompleteTs() * 1000 + 
						currTable.getRate() * AlarmManager.INTERVAL_DAY >
						System.currentTimeMillis()) {
					continue;
				}
			}
			
			if (prevTable == null || currTable.compareTo(prevTable) > 0) {
				// adds at the end of the linked list
				retVal.add(new LinkedList<AppTable>());
			}
			
			retVal.getLast().add(currTable);
			
			prevTable = currTable;
			
		} while (tables != null && !tables.isEmpty());
		
		return retVal;
	}
}
