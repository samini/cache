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

package edu.cmu.ece.cache.framework.url;

import android.util.Log;
import edu.cmu.ece.cache.framework.constants.Constants;
import edu.cmu.ece.cache.framework.location.Cell;
import edu.cmu.ece.cache.framework.location.CellConversion;
import edu.cmu.ece.cache.framework.location.LatLong;
import edu.cmu.ece.cache.framework.location.definition.LocationBoundingBox;
import edu.cmu.ece.cache.framework.location.definition.LocationCircle;
import edu.cmu.ece.cache.framework.location.definition.LocationLatLong;
import edu.cmu.ece.cache.framework.location.definition.LocationLatLongRange;
import edu.cmu.ece.cache.framework.location.definition.LocationLatLongSpan;

public class Encoder {
	
	public static final String TAG = Encoder.class.getName();
	
	// o is a location description update
	public static String encode(String api, String s, Cell c) {
		
		Log.d(TAG, "encode call");
		
		if (api == null || s == null || c == null) {
			return null;
		}
		
		if (api.equals(Constants.API_SLL)) {
			return encodeSLL(s, CellConversion.CellToShallowLocationLatLong(c));
		} else if (api.equals(Constants.API_SLLR)) {
			return encodeSLLR(s, CellConversion.CellToLocationCircle(c));
		} else if (api.equals(Constants.API_BB)) {
			return encodeBB(s, CellConversion.CellToLocationBoundingBox(c));
		} else if (api.equals(Constants.API_LLR)) {
			return encodeLLR(s, CellConversion.CellToLocationLatLongRange(c));
		} else if (api.equals(Constants.API_SLLS)) {
			return encodeSLLS(s, CellConversion.CellToLocationLatLongSpan(c));
		}
		
		return null;
	}
	
	public static String encodeSLL(String s, LocationLatLong l)
	{
		if (s == null)
			return null;
		
		if (l == null || l.getLatLong() == null)
			return null;
		
		String retVal = null;
		
		retVal = s.replace(Constants.API_LAT_DEG, 
				new StringBuilder().append(l.getLatLong().getLatitude()).toString());
		
		retVal = retVal.replace(Constants.API_LON_DEG, 
				new StringBuilder().append(l.getLatLong().getLongitude()).toString());
		
		return retVal;
	}
	
	public static String encodeSLLR(String s, LocationCircle c)
	{
		if (s == null)
			return null;
		
		if (c == null || c.getCenter() == null)
			return null;
		
		String retVal = null;
		
		retVal = s.replace(Constants.API_LAT_DEG, 
				new StringBuilder().append(c.getCenter().getLatitude()).toString());
		
		retVal = retVal.replace(Constants.API_LON_DEG, 
				new StringBuilder().append(c.getCenter().getLongitude()).toString());
		
		// place in the radius
		
		retVal = encodeRadius(retVal, c.getRadius());
		
		return retVal;
	}
	
	public static String encodeBB(String s, LocationBoundingBox b)
	{
		if (s == null)
			return null;
		
		if (b == null || b.getVertices() == null)
			return null;
		
		String retVal = null;
		
		LatLong[] vertices = b.getVertices();
		LatLong nw = vertices[Constants.VERTEX_NW_INDEX];
		LatLong ne = vertices[Constants.VERTEX_NE_INDEX];
		LatLong se = vertices[Constants.VERTEX_SE_INDEX];
		LatLong sw = vertices[Constants.VERTEX_SW_INDEX];
		
		retVal = s.replace(Constants.API_NW_LAT, 
				new StringBuilder().append(nw.getLatitude()).toString());
		
		retVal = retVal.replace(Constants.API_NW_LON, 
				new StringBuilder().append(nw.getLongitude()).toString());
		
		retVal = retVal.replace(Constants.API_NE_LAT, 
				new StringBuilder().append(ne.getLatitude()).toString());
		
		retVal = retVal.replace(Constants.API_NE_LON, 
				new StringBuilder().append(ne.getLongitude()).toString());
		
		retVal = retVal.replace(Constants.API_SE_LAT, 
				new StringBuilder().append(se.getLatitude()).toString());
		
		retVal = retVal.replace(Constants.API_SE_LON, 
				new StringBuilder().append(se.getLongitude()).toString());
		
		retVal = retVal.replace(Constants.API_SW_LAT, 
				new StringBuilder().append(sw.getLatitude()).toString());
		
		retVal = retVal.replace(Constants.API_SW_LON, 
				new StringBuilder().append(sw.getLongitude()).toString());
		
		return retVal;
	}
	
	public static String encodeLLR(String s, LocationLatLongRange r)
	{
		if (s == null)
			return null;
		
		if (r == null || r.getMinLatLong() == null || r.getMaxLatLong() == null)
			return null;
		
		String retVal = null;
		
		retVal = s.replace(Constants.API_LAT_MIN_DEG, 
				new StringBuilder().append(r.getMinLatLong().getLatitude()).toString());
		
		retVal = retVal.replace(Constants.API_LON_MIN_DEG, 
				new StringBuilder().append(r.getMinLatLong().getLongitude()).toString());
		
		retVal = retVal.replace(Constants.API_LAT_MAX_DEG, 
				new StringBuilder().append(r.getMaxLatLong().getLatitude()).toString());
		
		retVal = retVal.replace(Constants.API_LON_MAX_DEG, 
				new StringBuilder().append(r.getMaxLatLong().getLongitude()).toString());
		
		return retVal;
	}
	
	public static String encodeSLLS(String s, LocationLatLongSpan l)
	{
		if (s == null)
			return null;
		
		if (l == null || l.getCenter() == null)
			return null;
		
		String retVal = null;
		
		retVal = s.replace(Constants.API_LAT_DEG, 
				new StringBuilder().append(l.getCenter().getLatitude()).toString());
		
		retVal = retVal.replace(Constants.API_LON_DEG, 
				new StringBuilder().append(l.getCenter().getLongitude()).toString());
		
		retVal = retVal.replace(Constants.API_SPAN_LAT, 
				new StringBuilder().append(l.getLatSpan()).toString());
		
		retVal = retVal.replace(Constants.API_SPAN_LON, 
				new StringBuilder().append(l.getLongSpan()).toString());
		
		return retVal;
	}

	// incoming radius is in miles
	public static String encodeRadius(String s, double radius)
	{
		// the assumption here is that string s
		// only contains the radius marker in one unit
		
		if (s == null)
			return null;
		
		String retVal = null;
		
		// the idea is if a particular substring is not in s, then it simply does not replace
		// anything and spits out the original string
		retVal = s.replace(Constants.API_RADIUS_KM, 
				new StringBuilder().append(radius * Constants.MILE_TO_KM).toString());
		
		retVal = retVal.replace(Constants.API_RADIUS_METERS, 
				new StringBuilder().append(radius * Constants.MILE_TO_METERS).toString());
		
		retVal = retVal.replace(Constants.API_RADIUS_MILES, 
				new StringBuilder().append(radius).toString());
		
		// at this point regardless of unit whatever marker was used is filled with the correct radius
		
		return retVal;
		
	}

}
