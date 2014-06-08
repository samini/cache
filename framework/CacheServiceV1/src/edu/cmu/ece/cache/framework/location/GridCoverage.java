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

import edu.cmu.ece.cache.framework.location.definition.*;

// this is really just a wrapper around the CellCoverage class
public class GridCoverage {
	
	public static boolean LocationBoundingBoxInGrid(Grid g, LocationBoundingBox b)
	{
		Cell c = g.overarchingCell();
		return CellCoverage.LocationBoundingBoxInCell(c, b);
	}
	
	public static boolean LocationCircleInGrid(Grid g, LocationCircle l)
	{
		Cell c = g.overarchingCell();
		return CellCoverage.LocationCircleInCell(c, l);
	}
	
	public static boolean LocationLatLongInGrid(Grid g, LocationLatLong l)
	{
		Cell c = g.overarchingCell();
		return CellCoverage.LocationLatLongInCell(c, l);
	}
	
	public static boolean LocationLatLongRangeInGrid(Grid g, LocationLatLongRange r)
	{
		Cell c = g.overarchingCell();
		return CellCoverage.LocationLatLongRangeInCell(c, r);
	}
	
	public static boolean LocationLatLongSpanInGrid(Grid g, LocationLatLongSpan s)
	{
		Cell c = g.overarchingCell();
		return CellCoverage.LocationLatLongSpanInCell(c, s);
	}

}
