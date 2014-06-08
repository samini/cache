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

import edu.cmu.ece.cache.framework.location.definition.Location;
import edu.cmu.ece.cache.framework.location.definition.LocationBoundingBox;
import edu.cmu.ece.cache.framework.location.definition.LocationCircle;
import edu.cmu.ece.cache.framework.location.definition.LocationLatLong;
import edu.cmu.ece.cache.framework.location.definition.LocationLatLongRange;
import edu.cmu.ece.cache.framework.location.definition.LocationLatLongSpan;


// this class provides functions that check to see whether the cell
// contains a region of interest
// Assuming that the inpoly function works correctly
public class CellCoverage {
	
	public static final String TAG = CellCoverage.class.getName();
	
	public static boolean LocationInCell(Cell c, Location l) {
		if (c == null || l == null)
			return false;
		
		//Log.d(TAG, "LocationInCell call");
		
		if (l instanceof LocationBoundingBox) {
			return LocationBoundingBoxInCell(c, (LocationBoundingBox)l );
			
		} else if (l instanceof LocationCircle) {
			return LocationCircleInCell(c, (LocationCircle)l );
			
		} else if (l instanceof LocationLatLong) {
			return LocationLatLongInCell(c, (LocationLatLong)l );
			
		} else if (l instanceof LocationLatLongRange) {
			return LocationLatLongRangeInCell(c, (LocationLatLongRange)l );
			
		} else if (l instanceof LocationLatLongSpan) {
			return LocationLatLongSpanInCell(c, (LocationLatLongSpan)l );
		}
		
		return false;
	}
	
	public static boolean LocationBoundingBoxInCell(Cell c, LocationBoundingBox b)
	{
		//Log.d(TAG, "LocationBoundingBoxInCell call");
		LatLong[] vertices = c.getVertices();
		LatLong[] bVertices = b.getVertices();
		
		// checks to see if each vertex of the bounding box is inside the cell
		for (int i = 0; i < bVertices.length; i++)
		{
			if (bVertices[i] != null && !bVertices[i].inPolygon(vertices))
			{
				return false;
			}
		}
		
		return true;
		
	}
	
	public static boolean LocationCircleInCell(Cell c, LocationCircle l)
	{
		//Log.d(TAG, "LocationCircleInCell call");
		LatLong[] vertices = c.getVertices();
		
		// simple check is to see if center of the circle is inside
		if (!l.getCenter().inPolygon(vertices))
		{
			return false;
		}
		
		// now how to check if the furthest points on the perimeter of the circle are also in
		if (!l.getCenter().adjacentLatLong(l.getRadius(), LatLong.NORTH_BEARING).inPolygon(vertices))
		{
			return false;
		}
		
		if (!l.getCenter().adjacentLatLong(l.getRadius(), LatLong.EAST_BEARING).inPolygon(vertices))
		{
			return false;
		}
		
		if (!l.getCenter().adjacentLatLong(l.getRadius(), LatLong.SOUTH_BEARING).inPolygon(vertices))
		{
			return false;
		}
		
		if (!l.getCenter().adjacentLatLong(l.getRadius(), LatLong.WEST_BEARING).inPolygon(vertices))
		{
			return false;
		}
		
		return true;
		
	}
	
	public static boolean LocationLatLongInCell(Cell c, LocationLatLong l)
	{		
		LatLong[] vertices = c.getVertices();
		return l.getLatLong().inPolygon(vertices);
	}
	
	public static boolean LocationLatLongRangeInCell(Cell c, LocationLatLongRange r)
	{
		LatLong[] vertices = c.getVertices();
		LatLong[] rVerticesNoOrder = r.verticesNoOrder();
		
		// checks to see if each vertex of the range inside the cell
		for (int i = 0; i < rVerticesNoOrder.length; i++)
		{
			if (!rVerticesNoOrder[i].inPolygon(vertices))
			{
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean LocationLatLongSpanInCell(Cell c, LocationLatLongSpan s)
	{
		LatLong[] vertices = c.getVertices();
		LatLong[] sVertices = s.verticesNoOrder();
		
		// checks to see if each vertex of the span is inside the cell
		for (int i = 0; i < sVertices.length; i++)
		{
			if (!sVertices[i].inPolygon(vertices))
			{
				return false;
			}
		}
		
		return true;
	}

}
