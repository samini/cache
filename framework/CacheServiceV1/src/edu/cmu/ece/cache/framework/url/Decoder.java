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
import edu.cmu.ece.cache.framework.location.LatLong;
import edu.cmu.ece.cache.framework.location.definition.Location;
import edu.cmu.ece.cache.framework.location.definition.LocationBoundingBox;
import edu.cmu.ece.cache.framework.location.definition.LocationCircle;
import edu.cmu.ece.cache.framework.location.definition.LocationLatLong;
import edu.cmu.ece.cache.framework.location.definition.LocationLatLongRange;
import edu.cmu.ece.cache.framework.location.definition.LocationLatLongSpan;
import edu.cmu.ece.cache.framework.utility.Utils;

// decoder class takes a url in and strips the values that convert it back to one of 
// the location specifying objects
public class Decoder {
	
	public static final String TAG = Decoder.class.getName();
	
	// instead of taking advantage of the separate function below use on with all markers
	// this is faster than reading over the schema and request several times
	public static Location decodeToLocation(String schema, String request) {
		
		String[] markers = {
				// SLL, SLLR, SLLS
				Constants.API_LAT_DEG, Constants.API_LON_DEG, //0-1
				
				// circle 
				Constants.API_RADIUS_KM, //2-4
				Constants.API_RADIUS_MILES,
				Constants.API_RADIUS_METERS,
				
				// bb
				Constants.API_NW_LAT, Constants.API_NW_LON, //5-12
				Constants.API_NE_LAT, Constants.API_NE_LON,
				Constants.API_SE_LAT, Constants.API_SE_LON,
				Constants.API_SW_LAT, Constants.API_SW_LON,
				
				// llr
				Constants.API_LAT_MIN_DEG, //13-16
				Constants.API_LAT_MAX_DEG,
				Constants.API_LON_MIN_DEG,
				Constants.API_LON_MAX_DEG,
				
				// span
				Constants.API_SPAN_LAT, Constants.API_SPAN_LON //17-18
				
				};
		
		double[] values = requestParser(schema, request, markers);
		
		if (!Double.isNaN(values[0]) && !Double.isNaN(values[1])) {
			LatLong l = new LatLong(values[0], values[1]);
			
			// this could be either SLL, SLLR, SLLS
			
			// SLLR
			if (!Double.isNaN(values[2])) {
				// we have km
				return new LocationCircle(l, values[2] * Constants.KM_TO_METERS);
			}
			
			if (!Double.isNaN(values[3])) {
				return new LocationCircle(l, values[3] * Constants.MILE_TO_METERS);
			}
			
			if (!Double.isNaN(values[4])) {
				return new LocationCircle(l, values[4]);
			}
			
			// SLLS
			if (!Double.isNaN(values[17]) && !Double.isNaN(values[18])) {
				return new LocationLatLongSpan(l, values[17], values[18]);
			}
			
			// SLL
			return new LocationLatLong(l);
		}
		
		// assume llr
		boolean llr = true;
		
		// try to break the llr assumption
		for (int i = 13; i <= 16; i++) {
			if (Double.isNaN(values[i])) {
				llr = false;
				break;
			}
		}
		
		if (llr) {
			LatLong minLatLong = new LatLong(values[13], values[15]);
			LatLong maxLatLong = new LatLong(values[14], values[16]);
			return new LocationLatLongRange(minLatLong, maxLatLong);
		}
		
		// assume bb, construct a bb and see if it is valid
		LatLong[] vertices = new LatLong[4];
		vertices[Constants.VERTEX_NW_INDEX] = new LatLong(values[5], values[6]);
		vertices[Constants.VERTEX_NE_INDEX] = new LatLong(values[7], values[8]);
		vertices[Constants.VERTEX_SE_INDEX] = new LatLong(values[9], values[10]);
		vertices[Constants.VERTEX_SW_INDEX] = new LatLong(values[11], values[12]);
		
		LocationBoundingBox testLocationBoundingBox = new LocationBoundingBox(vertices);
		
		if (testLocationBoundingBox.isValid())
			return testLocationBoundingBox;
		
		return null;
	}

	public static LocationBoundingBox decodeToLocationBoundingBox(String schema, String request)
	{
		String[] markers = {Constants.API_NW_LAT, Constants.API_NW_LON,
				Constants.API_NE_LAT, Constants.API_NE_LON,
				Constants.API_SE_LAT, Constants.API_SE_LON,
				Constants.API_SW_LAT, Constants.API_SW_LON
				};
		
		double[] values = requestParser(schema, request, markers);
		
		LatLong[] vertices = new LatLong[4];
		vertices[Constants.VERTEX_NW_INDEX] = new LatLong(values[0], values[1]);
		vertices[Constants.VERTEX_NE_INDEX] = new LatLong(values[2], values[3]);
		vertices[Constants.VERTEX_SE_INDEX] = new LatLong(values[4], values[5]);
		vertices[Constants.VERTEX_SW_INDEX] = new LatLong(values[6], values[7]);
		
		return new LocationBoundingBox(vertices);
	
	}
	
	public static LocationCircle decodeToLocationCircle(String schema, String request)
	{
		String[] markers = {Constants.API_LAT_DEG, Constants.API_LON_DEG,
				Constants.API_RADIUS_KM,
				Constants.API_RADIUS_MILES,
				Constants.API_RADIUS_METERS,
				};
		
		double[] values = requestParser(schema, request, markers);
		
		LatLong center = new LatLong(values[0], values[1]);
		
		// the circle is defined with only one radius type
		// TODO: assume that one radius is set
		if (Double.isNaN(values[2])) {
			// we have km
			return new LocationCircle(center, values[2] * Constants.KM_TO_METERS);
		}
		
		if (Double.isNaN(values[3])) {
			return new LocationCircle(center, values[3] * Constants.MILE_TO_METERS);
		}
		
		if (Double.isNaN(values[4])) {
			return new LocationCircle(center, values[4]);
		}
		
		return null;
		
	}
	
	public static LocationLatLong decodeToLocationLatLong(String schema, String request)
	{
		String[] markers = {Constants.API_LAT_DEG, Constants.API_LON_DEG};
		
		double[] values = requestParser(schema, request, markers);
		
		for (int i = 0; i < values.length; i++)
			if (Double.isNaN(values[i]))
				return null;
		
		LatLong l = new LatLong(values[0], values[1]);
		
		return new LocationLatLong(l);
	}
	
	public static LocationLatLongRange decodeToLocationLatLongRange(String schema, String request)
	{
		//Log.d(TAG, "decodeToLocationLatLongRange call");
		String[] markers = {Constants.API_LAT_MIN_DEG, 
				Constants.API_LAT_MAX_DEG,
				Constants.API_LON_MIN_DEG,
				Constants.API_LON_MAX_DEG};
		
		double[] values = requestParser(schema, request, markers);
		
		for (int i = 0; i < values.length; i++)
			if (Double.isNaN(values[i]))
				return null;
		
		LatLong minLatLong = new LatLong(values[0], values[2]);
		LatLong maxLatLong = new LatLong(values[1], values[3]);
		
		return new LocationLatLongRange(minLatLong, maxLatLong);
	}
	
	public static LocationLatLongSpan decodeToLocationLatLongSpan(String schema, String request)
	{
		String[] markers = {Constants.API_LAT_DEG, Constants.API_LON_DEG,
				Constants.API_SPAN_LAT, Constants.API_SPAN_LON};
		
		double[] values = requestParser(schema, request, markers);
		
		for (int i = 0; i < values.length; i++)
			if (Double.isNaN(values[i]))
				return null;
		
		LatLong l = new LatLong(values[0], values[1]);
		
		return new LocationLatLongSpan(l, values[2], values[3]);
	}
	
	//FIXME: The parser only works with numeric types and not other type of
	// request parameters.
	public static double[] requestParser(String schema, String request, String[] markers)
	{
		//Log.d(TAG, "requestParser called");
		
		if (schema == null || request == null)
		{
			//Log.d(TAG, "Either schema or request is null.");
			return null;
		}
		
		// create a large enough array to hold all markers
		double[] retVal = new double[markers.length];
		
		for (int i = 0; i < retVal.length; i++)
			retVal[i] = Double.NaN;
		
		// keep a pointer into the schema and the request
		int schemaPointer = 0;
		int requestPointer = 0;
		
		while (requestPointer < request.length() && schemaPointer < schema.length())
		{
			// while the characters are equal, move along the pointers
			if (request.charAt(requestPointer) == schema.charAt(schemaPointer))
			{
				//Log.d(TAG, request.charAt(requestPointer));
				
				//Log.d(TAG, new StringBuilder().append(schema.charAt(schemaPointer)).toString());
				requestPointer++;
				schemaPointer++;
			} else if (schema.charAt(schemaPointer) == Constants.API_MARKER &&
					Utils.isNumeric(request.charAt(requestPointer)) ) {
				
				// a character is hit that is not equal in both cases, 
				// this character should be a '#'
				// figure out which marker it is and parse the value appropriately
				int markersIndex = 0;
				int exists = -1;
				
				do
				{
					// check to see if the current marker is where we have stopped in
					// the schema
					exists = schema.indexOf(markers[markersIndex], schemaPointer);
					
					// if the occurance is not the same place as the pointer,
					// then we obviously do not have the write marker
					if (exists == schemaPointer)
						break;
					
					markersIndex++;
					
				} while (markersIndex < markers.length);
					
				// if a marker exists, we know which marker it is based on the index
				// we also have to start reading extended numberic characters
				// until we reach something that is non-numberic
				// FIXME: if there are 2 dots after each other, it could be problematic
				// this should not be the case
				
				// if no marker was found something is wrong
				// FIXME: This could have better checks, maybe if we halted
				// and no marker exists, then something fishy is going on
				if (exists != schemaPointer)
				{
					Log.d(TAG, "Did not find a marker");
					return null;
				}
				
				// markersIndex holds the index to the appropirate marker
				// count number of numeric characters in request
				
				int numNumerics = Utils.numNumerics(request, requestPointer);
				
				// if there are no numerics something is wrong
				if (numNumerics <= 0)
				{
					return null;
				}
				
				char[] buffer = new char[numNumerics];
				int bufferIndex = 0;
				
				while (numNumerics > 0)
				{
					buffer[bufferIndex] = request.charAt(requestPointer);
					//Log.d(TAG, new StringBuilder().append(buffer).toString());
					bufferIndex++;
					numNumerics--;
					requestPointer++;
				}
				
				//Log.d(TAG, "" + request.charAt(requestPointer));
				
				// at this point the requestPointer should point at a non-numeric value
				// the bufferIndex should equal buffer.length()
				
				// place the obtained double value in the array
				retVal[markersIndex] = Double.parseDouble(
						new StringBuilder().append(buffer).toString());
				
				// the request pointer is already at the right location
				// move the schema pointer to the correct location as well
				schemaPointer += markers[markersIndex].length();
				
				//Log.d(TAG, "" + schema.charAt(schemaPointer));
			}
			
		}
		
		return retVal;
	}
	
}
