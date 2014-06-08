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

package edu.cmu.ece.cache.framework.location.definition;

import edu.cmu.ece.cache.framework.constants.Constants;
import edu.cmu.ece.cache.framework.location.LatLong;

public class LocationBoundingBox implements Location {

	// the vertices are in order NW, NE, SE, SW, i.e. clockwise

	private LatLong[] vertices;

	public LocationBoundingBox(LatLong[] vertices) {
		this.vertices = vertices.clone();
	}
	
	public LatLong center()
	{
		// center can be computed using all 4 points
		// the NW and SE
		// or the NE and SW
		
		LatLong retVal = null;
		
		// if all values are present use the average as the center
		if (allVerticesPresent()) {
			double latSum = 0, longSum = 0;
			
			for (int i = 0; i < vertices.length; i++)
			{
				latSum += vertices[i].getLatitude();
				longSum += vertices[i].getLongitude();
			}
			
			retVal = new LatLong(latSum / 4.0, longSum / 4.0);
			
			return retVal;
		}
		
		// at least one of the values is not present,
		// so only one of the two conditions below holds
		
		if (nwSePresent()) {
				
			double lat = (vertices[Constants.VERTEX_NW_INDEX].getLatitude() +
				vertices[Constants.VERTEX_SE_INDEX].getLatitude()) / 2.0;
			
			double lon = (vertices[Constants.VERTEX_NW_INDEX].getLongitude() +
					vertices[Constants.VERTEX_SE_INDEX].getLongitude()) / 2.0;
			
			retVal = new LatLong(lat, lon);
			return retVal;
		}
		
		if (neSwPresent()) {
				
			double lat = (vertices[Constants.VERTEX_NE_INDEX].getLatitude() +
				vertices[Constants.VERTEX_SW_INDEX].getLatitude()) / 2.0;
			
			double lon = (vertices[Constants.VERTEX_NE_INDEX].getLongitude() +
					vertices[Constants.VERTEX_SW_INDEX].getLongitude()) / 2.0;
			
			retVal = new LatLong(lat, lon);
			return retVal;
		}
		
		return null;
		
	}
	
	public boolean isValid() {
		if (allVerticesPresent()) {
			return true;
		} else if (nwSePresent()) {
			return true;
		} else if (neSwPresent()) {
			return true;
		}
		
		return false;
	}
	
	public boolean allVerticesPresent() {
		for (int i = 0; i < vertices.length; i++) {
			if (vertices[i] == null || vertices[i].isInvalid()) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean nwSePresent() {
		if (vertices[Constants.VERTEX_NW_INDEX] == null)
			return false;
		
		if (vertices[Constants.VERTEX_SE_INDEX] == null)
			return false;
		
		if (vertices[Constants.VERTEX_NW_INDEX].isInvalid())
			return false;
		
		if (vertices[Constants.VERTEX_SE_INDEX].isInvalid())
			return false;
		
		return true;
	}
	
	public boolean neSwPresent() {
		
		if (vertices[Constants.VERTEX_NE_INDEX] == null)
			return false;
		
		if (vertices[Constants.VERTEX_SW_INDEX] == null)
			return false;
		
		if (vertices[Constants.VERTEX_NE_INDEX].isInvalid())
			return false;
		
		if (vertices[Constants.VERTEX_SW_INDEX].isInvalid())
			return false;
		
		return true;
		
	}

	public LatLong[] getVertices() {
		return vertices;
	}

	public void setVertices(LatLong[] vertices) {
		this.vertices = vertices;
	}
}
