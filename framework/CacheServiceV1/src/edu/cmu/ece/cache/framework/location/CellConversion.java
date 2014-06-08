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

package edu.cmu.ece.cache.framework.location;

import edu.cmu.ece.cache.framework.constants.Constants;
import edu.cmu.ece.cache.framework.location.definition.LocationBoundingBox;
import edu.cmu.ece.cache.framework.location.definition.LocationCircle;
import edu.cmu.ece.cache.framework.location.definition.LocationLatLong;
import edu.cmu.ece.cache.framework.location.definition.LocationLatLongRange;
import edu.cmu.ece.cache.framework.location.definition.LocationLatLongSpan;

// a few of these functions are more efficient as they
// take pre-computed values as arguments instead of computing themselves
public class CellConversion {
	
	// the CellTo* function created so that when
	// a query has to be made using the API,
	// the cell is taken from the db, the parameters of the
	// query are generated for that cell
	// and the content is retrieved
	
	public static LocationBoundingBox CellToLocationBoundingBox(Cell c)
	{
		return new LocationBoundingBox(c.getVertices());
	}
	
	// this function is more expensive since it tries to find the
	// radius itself. you don't want a very thin rectangle for this
	public static LocationCircle CellToLocationCircle(Cell c)
	{
		return new LocationCircle(c.center(), c.radiusMeters());
	}
	
	// creates a location circle based on center of a cell
	// radius in meters
	public static LocationCircle CellToLocationCircle(Cell c, double radius)
	{
		return new LocationCircle(c.center(), radius); 
	}
	
	public static LocationLatLong CellToShallowLocationLatLong(Cell c)
	{
		return new LocationLatLong(c.center());
	}
	
	// under the assumption that the cells vertical and horizontal distances are equal
	public static LocationLatLong CellToDeepLocationLatLong(Cell c)
	{
		return new LocationLatLong(c.center(), c.widthMeters());
	}
	
	// assuming that the developer already knows what the distances are
	public static LocationLatLong CellToDeepLocationLatLong(Cell c, double distance)
	{
		return new LocationLatLong(c.center(), distance);
	}
	
	// this function does not work past the Meridian
	public static LocationLatLongRange CellToLocationLatLongRange(Cell c)
	{
		// ne point has the most positive latitude and long
		// sw point has the most negative latitude and long
		
		LatLong[] v = c.getVertices();
		LatLong ne = v[Constants.VERTEX_NE_INDEX];
		LatLong sw = v[Constants.VERTEX_SW_INDEX];
		
		return new LocationLatLongRange(sw, ne);
	}
	
	public static LocationLatLongSpan CellToLocationLatLongSpan(Cell c)
	{
		double width = c.widthMeters();
		double height = c.heightMeters();
		
		double latSpan = LatLong.metersToLatSpan(height);
		double longSpan = LatLong.metersToLonSpan(width, 
				c.getVertices()[Constants.VERTEX_NW_INDEX].getLatitude());
		
		return new LocationLatLongSpan(c.center(), latSpan, longSpan);
	}
	
	// in case dev already knows the latSpan and longSpan
	public static LocationLatLongSpan CellToLocationLatLongSpan(Cell c, double latSpan, double longSpan)
	{
		return new LocationLatLongSpan(c.center(), latSpan, longSpan);
	}

}
