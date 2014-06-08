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
import java.math.BigDecimal;

public class LatLong implements Serializable {
	
	// the latitude and longitude exposed by this class are in degrees not radians
	
	private static final long serialVersionUID = -3520472849117995804L;
	
	public static final String TAG = LatLong.class.getName();
	
	private double latitudeDegrees, longitudeDegrees;
	private double latitudeRadians, longitudeRadians;
	
	public static final double INVALID = -360.0;
	
	public static final double LATITUDE_MIN = -90.0;
	public static final double LATITUDE_MAX = 90.0;
	public static final double LONGITUDE_MIN = -180.0;
	public static final double LONGITUDE_MAX = 180.0;
	
	public static final double NORTH_BEARING = 0.0;
	public static final double EAST_BEARING = 90.0;
	public static final double SOUTH_BEARING = 180.0;
	public static final double WEST_BEARING = 270.0;
	public static final double NORTH_WEST_BEARING = 315.0;

	public static final double EARTH_RADIUS_METERS = 6371009; // in meters 
	
	// landmarks
	public static final LatLong CMU = new LatLong(40.443599,-79.942274);
	public static final LatLong CATHEDRAL_OF_LEARNING = new LatLong(40.444362,-79.953174);
	
	public static final LatLong PITTSBURGH = new LatLong(40.440625,-79.995886);
	public static final LatLong PITTSBURGH_NORTH_WEST = new LatLong(40.4878, -80.0839);
	public static final LatLong PITTSBURGH_NORTH_EAST = new LatLong(40.4878, -79.8759);
	public static final LatLong PITTSBURGH_SOUTH_WEST = new LatLong(40.3678, -80.0839);
	public static final LatLong PITTSBURGH_SOUTH_EAST = new LatLong(40.3678, -79.8759);
	
	public static final LatLong SEATTLE = new LatLong(47.60621,-122.332071);
	public static final LatLong WASHINGTON_DC = new LatLong(38.895112,-77.036366);
	public static final LatLong NEW_YORK_CITY = new LatLong(40.714353,-74.005973);
	
	
	public LatLong()
	{
		this.latitudeDegrees = LatLong.INVALID;
		this.longitudeDegrees = LatLong.INVALID;
	}
	
	
	public LatLong(double latitudeDegrees, double longitudeDegrees)
    {
        this.latitudeDegrees = latitudeDegrees;
        this.longitudeDegrees = longitudeDegrees;
    }
	
    public double distance(LatLong latLong)
    {
        // Convert to radians
        double lat0, lon0;
        double lat1, lon1;
        double deltaLat, deltaLon;
        
        this.toRadians();
        lat0 = this.latitudeRadians;
        lon0 = this.longitudeRadians;

        latLong.toRadians();
        lat1 = latLong.latitudeRadians;
        lon1 = latLong.longitudeRadians;

        deltaLon = lon1 - lon0;
        deltaLat = lat1 - lat0;
        
        double sineHalfDeltaLat = Math.sin(deltaLat / 2);
        double sineHalfDeltaLon = Math.sin(deltaLon / 2);
        
        double a = sineHalfDeltaLat * sineHalfDeltaLat + 
        	Math.cos(lat0) * Math.cos(lat1) * sineHalfDeltaLon * sineHalfDeltaLon;
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        double retVal = c * EARTH_RADIUS_METERS;
        return retVal;
    }
    
    public LatLong adjacentLatLong(double distance, double bearing)
    {
        if (0 == distance) return new LatLong(this.latitudeDegrees, this.longitudeDegrees);
        else if (distance < 0) return null;
        
        double lat0, lon0;
        double lat1, lon1;

        distance = distance / EARTH_RADIUS_METERS;

        bearing = toRadians(bearing);

        this.toRadians();
        lat0 = this.latitudeRadians;
        lon0 = this.longitudeRadians;

        lat1 = Math.asin(Math.sin(lat0) * Math.cos(distance) + 
        		Math.cos(lat0) * Math.sin(distance) * Math.cos(bearing));

        lon1 = lon0 + Math.atan2(Math.sin(bearing) * Math.sin(distance) *
                            Math.cos(lat0),
                            Math.cos(distance) - Math.sin(lat0) *
                            Math.sin(lat1));

        if (Double.isNaN(lat1) || Double.isNaN(lon1)) return null;

        // the precision may be different, which may result in a grid that is not tight
        return new LatLong(toDegrees(lat1), toDegrees(lon1));
    }
    
    public LatLong adjacentLatLong(double distance, double bearing, int precision)
    {
        LatLong tmp = adjacentLatLong(distance, bearing);
        
        BigDecimal bd0 = new BigDecimal(Double.toString(tmp.getLatitude()));
        bd0 = bd0.setScale(precision, BigDecimal.ROUND_HALF_UP);
        
        BigDecimal bd1 = new BigDecimal(Double.toString(tmp.getLongitude()));
        bd1 = bd1.setScale(precision, BigDecimal.ROUND_HALF_UP);
        
        return new LatLong(bd0.doubleValue(), bd1.doubleValue());
    }
    
    public boolean inPolygon(LatLong[] latLongArray)
    {
        int j = latLongArray.length - 1;
        boolean inPolygon = false;

        for (int i = 0; i < latLongArray.length; i++)
        {
            if (latLongArray[i].longitudeDegrees < longitudeDegrees &&
                latLongArray[j].longitudeDegrees >= longitudeDegrees
             || latLongArray[j].longitudeDegrees < longitudeDegrees &&
                latLongArray[i].longitudeDegrees >= longitudeDegrees)
            {
                if (latLongArray[i].latitudeDegrees + (longitudeDegrees - latLongArray[i].longitudeDegrees) /
                  (latLongArray[j].longitudeDegrees - latLongArray[i].longitudeDegrees) * (latLongArray[j].latitudeDegrees
                    - latLongArray[i].latitudeDegrees) < latitudeDegrees)
                {
                    inPolygon = !inPolygon;
                }
            }
            j = i;
        }

        return inPolygon;
    }
    
    // verification:
    // 1 degree latitude is about 111km
    public static double metersToLatSpan(double meters)
    {
        return 180.0 * meters / (Math.PI * EARTH_RADIUS_METERS);
    }
    
    // 55km is about 1 degree at lat 60 degrees
    // lat is in degrees
    public static double metersToLonSpan(double meters, double lat)
    {
        double cosLat = Math.cos(Math.PI * lat / 180.0);
        return 180.0 * meters / (Math.PI * EARTH_RADIUS_METERS * cosLat);
    }
    
    // Convert from subtended degrees to distance
    public static double latSpanToMeters(double latSpan)
    {
        return Math.PI * EARTH_RADIUS_METERS * latSpan / 180.0;
    }

    // lat is in degrees
    public static double lonSpanToMeters(double lonSpan, double lat)
    {
        double cosLatitude = Math.cos(Math.PI * lat / 180.0);
        return Math.PI * EARTH_RADIUS_METERS * cosLatitude * lonSpan / 180.0;
    }
    
    // this function is not automatically called in the constructor
    // best to just run it prior to usage.
    public void toRadians()
    {
        latitudeRadians = Math.PI * this.latitudeDegrees / 180.0;
        longitudeRadians = Math.PI * this.longitudeDegrees / 180.0;
    }
    
    public static double toRadians(double degrees)
    {
        return degrees * Math.PI / 180.0;
    }

    public static double toDegrees(double radians)
    {
        return radians * 180.0 / Math.PI;
    }

    public boolean isZero() {
    	if (this.latitudeDegrees == 0 && this.longitudeDegrees == 0) {
    		return true;
    	}
    	
    	return false;
    }
    
    public boolean isInvalid() {
    	if (Double.isNaN(latitudeDegrees) || Double.isNaN(longitudeDegrees))
    		return true;
    	
    	if (latitudeDegrees < LatLong.LATITUDE_MIN)
    		return true;
    	
    	if (latitudeDegrees > LatLong.LATITUDE_MAX)
    		return true;
    	
    	if (longitudeDegrees < LatLong.LONGITUDE_MIN)
    		return true;
    	
    	if (longitudeDegrees > LatLong.LONGITUDE_MAX)
    		return true;
    	
    	return false;
    }
    
	public double getLatitude() {
		return latitudeDegrees;
	}

	public void setLatitude(double latitudeDegrees) {
		this.latitudeDegrees = latitudeDegrees;
	}

	public double getLongitude() {
		return longitudeDegrees;
	}

	public void setLongitude(double longitudeDegrees) {
		this.longitudeDegrees = longitudeDegrees;
	}

	@Override
	public String toString() {
		return "" + latitudeDegrees
				+ ",\t" + longitudeDegrees;
	}
	
}
