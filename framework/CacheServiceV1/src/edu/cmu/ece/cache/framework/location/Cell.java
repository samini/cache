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

import java.io.Serializable;

import edu.cmu.ece.cache.framework.constants.Constants;
import edu.cmu.ece.cache.framework.utility.MathUtils;
import edu.cmu.ece.cache.framework.utility.Utils;

// serializble so we can write grid to a file
public class Cell implements Serializable {
	
	private static final long serialVersionUID = 524094999978389132L;

	public static final int INVALID_CELL = -1;
	
	private int id;
	
	// index 0 used for initial vertex, index clockwise
	private LatLong[] vertices; 
	
	public Cell()
	{
		id = INVALID_CELL;
	}
	
	public Cell(int id)
	{
		this();
		this.id = id;
	}
	
	public Cell(int id, LatLong[] vertices)
	{
		this(id);
		this.vertices = vertices;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public LatLong[] getVertices() {
		return vertices;
	}

	public void setVertices(LatLong[] vertices) {
		this.vertices = vertices;
	}
	
	// all 4 vertices taken into account
	// more accurate based on 4 points. 
	// under assumption that the size of the cell is small
	// otherwise the distance functions should be used instead.
	public LatLong center()
	{
		double latSum = 0, longSum = 0;
		
		for (int i = 0; i < vertices.length; i++)
		{
			latSum += vertices[i].getLatitude();
			longSum += vertices[i].getLongitude();
		}
		
		return new LatLong(latSum / 4.0, longSum / 4.0);
	}
	
	public double widthMeters()
	{
		return vertices[Constants.VERTEX_NW_INDEX].distance(vertices[Constants.VERTEX_NE_INDEX]);
	}
	
	public double heightMeters()
	{
		return vertices[Constants.VERTEX_NW_INDEX].distance(vertices[Constants.VERTEX_SW_INDEX]);
	}
	
	public double areaMetersSquared()
	{
		return (widthMeters() * heightMeters());
	}
	
	// return the radius of a circle that would overlap this cell
	// assuming same center
	// would not work for a very small cell
	// based on the longest side of the cell
	public double radiusMeters()
	{
		double width = this.widthMeters();
		double height = this.heightMeters();
		return MathUtils.overlappingCircleRadius(width, height);
	}
}
