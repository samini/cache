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
import edu.cmu.ece.cache.framework.utility.Utils;

//serializble so we can write grid to a file
public class Grid implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4042470456219793520L;

	public static final int INVALID = -1;
	
	private int numRows, numCols;
	private int gridSize;
	
	// hstep increases towards west
	// vstep increases towards south
	// use meters
	private double hStep, vStep;
	
	// the NW point of the grid
	private LatLong nw;
	
	// NW most cell at index 0
	private Cell[] cells;
	
	public Grid()
	{
		numRows = INVALID;
		numCols = INVALID;
		hStep = 0;
		vStep = 0;
	}
	
	public Grid(int numRows, int numCols)
	{
		this.numRows = numRows;
		this.numCols = numCols;
	}
	
	public Grid(LatLong nw, int numRows, int numCols)
	{
		this(numRows, numCols);
		this.nw = nw;
	}
	
	public Grid(LatLong nw, int numRows, int numCols, double hStep, double vStep)
	{
		this(nw, numRows, numCols);
		this.hStep = hStep;
		this.vStep = vStep;
	}
	
	// returns the maximum of the horizontal and vertical step size in meters
	public double maxStep()
	{
		return Math.max(hStep, vStep);
	}
	
	// TODO: in next version only use the adjacentLatLong for each
	// column and row only once. this leads to a flawless grid
	
	// in order to create a grid one needs to know the following
	// the starting point, i.e. nw needs to be set
	// the hStep and vStep
	// numRows and numColumns
	// uses a precision of 6 decimal points to make the grid tight
	public void createGrid()
	{
		if (this.getNw() == null) return;
		if (this.getNumCols() < 1 || this.getNumRows() < 1) return;
		if (hStep <= 0 || vStep <= 0) return;
		
		int precision = 6;
		
		gridSize = numRows * numCols;
		
		cells = new Cell[gridSize];
		
		// each cell has vertices that have to be filled
		
		// create the first cell
		cells[0] = new Cell(0); // id = 0;
		LatLong[] vertices = new LatLong[4];
		vertices[Constants.VERTEX_NW_INDEX] = this.getNw();
		vertices[Constants.VERTEX_NE_INDEX] = vertices[Constants.VERTEX_NW_INDEX].adjacentLatLong(hStep, LatLong.EAST_BEARING, precision);
		vertices[Constants.VERTEX_SE_INDEX] = vertices[Constants.VERTEX_NE_INDEX].adjacentLatLong(vStep, LatLong.SOUTH_BEARING, precision);
		vertices[Constants.VERTEX_SW_INDEX] = vertices[Constants.VERTEX_NW_INDEX].adjacentLatLong(vStep, LatLong.SOUTH_BEARING, precision);
		
		cells[0].setVertices(vertices);
		
		// create the first row
		for (int i = 1; i < numCols; i++)
		{
			cells[i] = new Cell(i);
			vertices = new LatLong[4];
			vertices[Constants.VERTEX_NW_INDEX] = cells[i-1].getVertices()[Constants.VERTEX_NE_INDEX];
			vertices[Constants.VERTEX_SW_INDEX] = cells[i-1].getVertices()[Constants.VERTEX_SE_INDEX];
			vertices[Constants.VERTEX_NE_INDEX] = vertices[Constants.VERTEX_NW_INDEX].adjacentLatLong(hStep, LatLong.EAST_BEARING, precision);
			vertices[Constants.VERTEX_SE_INDEX] = vertices[Constants.VERTEX_SW_INDEX].adjacentLatLong(hStep, LatLong.EAST_BEARING, precision);
			cells[i].setVertices(vertices);
		}
		
		// after first row is created one can easily copy the calculations
		// for the vertices 0 and 1 from the last row
		// this is also extremely important so that the cells are tight
		// using the adjacent function would leave holes between cells!
		for (int i = 1; i < numRows; i++)
		{
			int id, northernId;
			
			for (int j = 0; j < numCols; j++)
			{
				// create the first cell in the row
				id = this.getIdByIndices(i, j);
				northernId = this.getIdByIndices(i-1, j);
				Cell northernCell = this.getCellById(northernId);
				LatLong[] northernCellVertices = northernCell.getVertices();
				cells[id] = new Cell(id);
				vertices = new LatLong[4];
				vertices[Constants.VERTEX_NW_INDEX] = northernCellVertices[Constants.VERTEX_SW_INDEX];
				vertices[Constants.VERTEX_NE_INDEX] = northernCellVertices[Constants.VERTEX_SE_INDEX];
				vertices[Constants.VERTEX_SE_INDEX] = vertices[Constants.VERTEX_NE_INDEX].adjacentLatLong(vStep, LatLong.SOUTH_BEARING, precision);
				vertices[Constants.VERTEX_SW_INDEX] = vertices[Constants.VERTEX_NW_INDEX].adjacentLatLong(vStep, LatLong.SOUTH_BEARING, precision);
				cells[id].setVertices(vertices);
			}
		}
	}
	
	// returns an overarching Cell for the entire grid
	// this cell can be used as a simple check to see if the grid has coverage or not
	public Cell overarchingCell()
	{
		Cell c = new Cell();
		c.setId(0);
		
		LatLong[] vertices = new LatLong[4];
		vertices[Constants.VERTEX_NW_INDEX] = this.cells[0].getVertices()[Constants.VERTEX_NW_INDEX];
		vertices[Constants.VERTEX_NE_INDEX] = this.cells[numCols - 1].getVertices()[Constants.VERTEX_NE_INDEX];
		vertices[Constants.VERTEX_SE_INDEX] = this.cells[gridSize - 1].getVertices()[Constants.VERTEX_SE_INDEX];
		vertices[Constants.VERTEX_SW_INDEX] = this.cells[this.getIdByIndices(numRows - 1, 0)].getVertices()[Constants.VERTEX_SW_INDEX];
		
		c.setVertices(vertices);
		
		return c;
	}
	
	// README: this works before the longitude switches signs!
	// to find if the point is in the grid and return it
	// we return a cell id. the work is done by binary search and inPoly function in LatLong
	// NOTE: for this type of search, need a tight grid! no space between cells!
	// which is why the adjacent cells function with percision is used to create the cells
	public int getIdByLatLong(LatLong l)
	{
		if (cells == null) return Cell.INVALID_CELL;
		if (l == null) return Cell.INVALID_CELL;
		
		// if the latlong is the NW point of the cell, it is considered to be 
		// in that cell. Once it becomes the NE point of the cell, it is in the next cell
		// similarly if the point becomes the SW point of the cell, it is in the cell below
		// if it becomes the SE point, it is in the cell to the right of the cell below
		
		// create a double array for the latitude and the longitudes of NW point
		// it is necessary to pad it with one extra value at the end
		// to include points that are in the last cell
		
		// a few checks to see if the point is outside of the grid
		// if it is equal to NW point it is still in grid
		if (l.getLatitude() > cells[0].getVertices()[Constants.VERTEX_NW_INDEX].getLatitude())
		{
			return Cell.INVALID_CELL;
		}
		
		if (l.getLongitude() < cells[0].getVertices()[Constants.VERTEX_NW_INDEX].getLongitude())
		{
			return Cell.INVALID_CELL;
		}
		
		// if it is equal to SE point, it is not in the grid
		if (l.getLatitude() <= cells[gridSize - 1].getVertices()[Constants.VERTEX_SE_INDEX].getLatitude())
		{
			return Cell.INVALID_CELL;
		}
		
		if (l.getLongitude() >= cells[gridSize -1].getVertices()[Constants.VERTEX_SE_INDEX].getLongitude())
		{
			return Cell.INVALID_CELL;
		}
		
		double[] latitudes, longitudes;
		latitudes = new double[numRows + 1];
		longitudes = new double[numCols + 1];
		
		// determine the column
		for (int i = 0; i < numCols; i++)
		{
			longitudes[i] = cells[i].getVertices()[Constants.VERTEX_NW_INDEX].getLongitude();
		}
		
		longitudes[longitudes.length - 1] = cells[numCols - 1].getVertices()[Constants.VERTEX_NE_INDEX].getLongitude();
		
		int col = Utils.binarySearchInBetween(longitudes, l.getLongitude(), 0, longitudes.length - 1);
		
		// did not find a proper column
		if (col <= -1)
		{
			return Cell.INVALID_CELL;
		}
		
		// determine the row, longitude values decrease in grid, 
		// they have to be loaded in reverse
		for (int i = 1; i < numRows + 1; i++)
		{
			
			// set the column to be 0, this is the west edge
			int id = getIdByIndices(numRows - i, 0);
			//System.out.println("" + i + ' ' + (numRows - i) + ' ' + id);
			
			latitudes[i] = cells[id].getVertices()[Constants.VERTEX_NW_INDEX].getLatitude();
		}
		
		int id = getIdByIndices(numRows - 1, 0);
		latitudes[0] = cells[id].getVertices()[Constants.VERTEX_SW_INDEX].getLatitude();
		
		int row = Utils.binarySearchInBetween(latitudes, l.getLatitude(), 0, latitudes.length - 1);
		
		// did not find a proper row
		if (row <= -1)
		{
			return Cell.INVALID_CELL;
		}
		
		// the rows are now flipped because of longitude signs
		row = numRows - row;
		
		return getIdByIndices(row, col);
	}
	
	public Cell getCellById(int id)
	{
		return cells[id];
	}
	
	public int getIdByIndices(int row, int col)
	{
		return (row * numCols + col);
	}
	
	public Cell getCellByIndices(int row, int col)
	{
		return cells[getIdByIndices(row, col)];
	}
	
	public int getRowById(int id)
	{
		return (id / numCols);
	}
	
	public int getColById(int id)
	{
		return (id % numCols);
	}

	public int getNumRows() {
		return numRows;
	}

	public void setNumRows(int numRows) {
		this.numRows = numRows;
	}

	public int getNumCols() {
		return numCols;
	}

	public void setNumCols(int numCols) {
		this.numCols = numCols;
	}

	public int getGridSize() {
		return gridSize;
	}

	public void setGridSize(int gridSize) {
		this.gridSize = gridSize;
	}

	public LatLong getNw() {
		return nw;
	}

	public void setNw(LatLong nw) {
		this.nw = nw;
	}

	public Cell[] getCells() {
		return cells;
	}

	public void setCells(Cell[] cells) {
		this.cells = cells;
	}

	public double getHStep() {
		return hStep;
	}

	public void setHStep(double hStep) {
		this.hStep = hStep;
	}

	public double getVStep() {
		return vStep;
	}

	public void setVStep(double vStep) {
		this.vStep = vStep;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0 ; i < numRows; i++)
		{
			for (int j = 0 ; j < numCols; j++)
			{
				int id = this.getIdByIndices(i, j);
				//System.out.println(id);
				sb.append(cells[id].getVertices()[Constants.VERTEX_NW_INDEX]);
				sb.append('|');
			}
			
			sb.append('\n');
		}
		
		return sb.toString();
	}
	
}
