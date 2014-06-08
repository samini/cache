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

package edu.cmu.ece.cache.framework.database;

import edu.cmu.ece.cache.framework.constants.Constants;
import edu.cmu.ece.cache.framework.location.LatLong;
import edu.cmu.ece.cache.framework.utility.MathUtils;
import android.os.Parcel;
import android.os.Parcelable;

// this table also implements Comparable so that it can be used to prioritize downloads
public class AppTable implements Parcelable, Comparable {
	
	private int id;
	private String appName;
	private long regTs;
	private String url;
	private String urlParams;
	private String api;
	private double cellWidth;
	private double cellHeight;
	private long updateCompleteTs;
	private long updateInitTs;
	private int priority;
	private int rate;
	private String address;
	private LatLong center;
	private double radius;
	private LatLong nw, ne, se, sw;
	private int numCells;
	private int lastCell;
	private int multiLevel;
	private int level;
	private int overlay;

	public static final Parcelable.Creator<AppTable> CREATOR = new Parcelable.Creator<AppTable>() {
        public AppTable createFromParcel(Parcel in) {
            return new AppTable(in);
        }

        public AppTable[] newArray(int size) {
            return new AppTable[size];
        }
    };
    
    public AppTable(int id, String appName, long regTs, String url,
			String urlParams, String api, double cellWidth, double cellHeight,
			long updateCompleteTs, long updateInitTs, int priority, int rate,
			String address, LatLong center, double radius, LatLong nw,
			LatLong ne, LatLong se, LatLong sw, int numCells, int lastCell, int multiLevel,
			int levels, int overlay) {
		this.id = id;
		this.appName = appName;
		this.regTs = regTs;
		this.url = url;
		this.urlParams = urlParams;
		this.api = api;
		this.cellWidth = cellWidth;
		this.cellHeight = cellHeight;
		this.updateCompleteTs = updateCompleteTs;
		this.updateInitTs = updateInitTs;
		this.priority = priority;
		this.rate = rate;
		this.address = address;
		this.center = center;
		this.radius = radius;
		this.nw = nw;
		this.ne = ne;
		this.se = se;
		this.sw = sw;
		this.numCells = numCells;
		this.lastCell = lastCell;
		this.multiLevel = multiLevel;
		this.level = levels;
		this.overlay = overlay;
	}

	private AppTable(Parcel in) {
        readFromParcel(in);
    }
    
    public void writeToParcel(Parcel dest, int flags) {
    	dest.writeInt(id);
		dest.writeString(appName);
		dest.writeLong(regTs);
		dest.writeString(url);
		dest.writeString(urlParams);
		dest.writeString(api);
		dest.writeDouble(cellWidth);
		dest.writeDouble(cellHeight);
		dest.writeLong(updateCompleteTs);
		dest.writeLong(updateInitTs);
		dest.writeInt(priority);
		dest.writeInt(rate);
		dest.writeString(address);
		dest.writeDouble(center.getLatitude());
		dest.writeDouble(center.getLongitude());
		dest.writeDouble(radius);
		dest.writeDouble(nw.getLatitude());
		dest.writeDouble(nw.getLongitude());
		dest.writeDouble(ne.getLatitude());
		dest.writeDouble(ne.getLongitude());
		dest.writeDouble(se.getLatitude());
		dest.writeDouble(se.getLongitude());
		dest.writeDouble(sw.getLatitude());
		dest.writeDouble(sw.getLongitude());
		dest.writeInt(numCells);
		dest.writeInt(lastCell);
		dest.writeInt(multiLevel);
		dest.writeInt(level);
		dest.writeInt(overlay);
	}

	public void readFromParcel(Parcel in) {
		this.id = in.readInt();
		this.appName = in.readString();
		this.regTs = in.readLong();
		this.url = in.readString();
		this.urlParams = in.readString();
		this.api = in.readString();
		this.cellWidth = in.readDouble();
		this.cellHeight = in.readDouble();
		this.updateCompleteTs = in.readLong();
		this.updateInitTs = in.readLong();
		this.priority = in.readInt();
		this.rate = in.readInt();
		this.address = in.readString();
		this.center = new LatLong(in.readDouble(), in.readDouble());
		this.radius = in.readDouble();
		this.nw = new LatLong(in.readDouble(), in.readDouble());
		this.ne = new LatLong(in.readDouble(), in.readDouble());
		this.se = new LatLong(in.readDouble(), in.readDouble());
		this.sw = new LatLong(in.readDouble(), in.readDouble());
		this.numCells = in.readInt();
		this.lastCell = in.readInt();
		this.multiLevel = in.readInt();
		this.level = in.readInt();
		this.overlay = in.readInt();
	}

	public int describeContents() {
		return 0;
	}
	
	public int compareTo(Object arg0) {
		if (arg0 == null)
			return -1;
		
		AppTable a = null;
		
		if (arg0 instanceof AppTable) {
			a = (AppTable) arg0;
		} else {
			return -1;
		}
		
		if (score() < a.score()) {
			return -1;
		} else if (score() > a.score()) {
			return 1;
		}
		
		return 0;
	}
	
	// lower is better
	public double score() {
		return ((double)getRate() + (double)getPriority() / 10.0);
	}
	
	// check to see if a location is covered by the circle overlapping 
	// the grid of this table this is a loose check. if the location 
	// falls on the edge, the grid does not cover it. however, it is a
	// fast way of checking if the location is no where near the grid
	public boolean looseLocationCoverage(LatLong l) {
		boolean retVal = false;
		
		// FIXME: lat,lon = 0,0 needs to be fixed
		if (this.center == null) {
			return false;
		} else if (center.isInvalid()) {
			return false;
		}
		
		if (this.getRadius() == 0) {
			return false;
		}
		
		double radiusMeters = this.getRadius() * Constants.MILE_TO_METERS;
		
		double overlappingCircleRadiusMeters = MathUtils.isoscelesHypotenuse(radiusMeters);
		
		double distanceMeters = this.getCenter().distance(l);
		
		retVal = (distanceMeters < overlappingCircleRadiusMeters);
		
		return retVal;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public long getRegTs() {
		return regTs;
	}

	public void setRegTs(long regTs) {
		this.regTs = regTs;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrlParams() {
		return urlParams;
	}

	public void setUrlParams(String urlParams) {
		this.urlParams = urlParams;
	}

	public String getApi() {
		return api;
	}

	public void setApi(String api) {
		this.api = api;
	}

	public double getCellWidth() {
		return cellWidth;
	}

	public void setCellWidth(double cellWidth) {
		this.cellWidth = cellWidth;
	}

	public double getCellHeight() {
		return cellHeight;
	}

	public void setCellHeight(double cellHeight) {
		this.cellHeight = cellHeight;
	}

	public long getUpdateCompleteTs() {
		return updateCompleteTs;
	}

	public void setUpdateCompleteTs(long updateCompleteTs) {
		this.updateCompleteTs = updateCompleteTs;
	}

	public long getUpdateInitTs() {
		return updateInitTs;
	}

	public void setUpdateInitTs(long updateInitTs) {
		this.updateInitTs = updateInitTs;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getRate() {
		return rate;
	}

	public void setRate(int rate) {
		this.rate = rate;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public LatLong getCenter() {
		return center;
	}

	public void setCenter(LatLong center) {
		this.center = center;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public LatLong getNw() {
		return nw;
	}

	public void setNw(LatLong nw) {
		this.nw = nw;
	}

	public LatLong getNe() {
		return ne;
	}

	public void setNe(LatLong ne) {
		this.ne = ne;
	}

	public LatLong getSe() {
		return se;
	}

	public void setSe(LatLong se) {
		this.se = se;
	}

	public LatLong getSw() {
		return sw;
	}

	public void setSw(LatLong sw) {
		this.sw = sw;
	}

	public int getLastCell() {
		return lastCell;
	}

	public void setLastCell(int lastCell) {
		this.lastCell = lastCell;
	}

	public int getMultiLevel() {
		return multiLevel;
	}

	public void setMultiLevel(int multiLevel) {
		this.multiLevel = multiLevel;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getOverlay() {
		return overlay;
	}

	public void setOverlay(int overlay) {
		this.overlay = overlay;
	}

	public int getNumCells() {
		return numCells;
	}

	public void setNumCells(int numCells) {
		this.numCells = numCells;
	}

}
