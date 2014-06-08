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

import edu.cmu.ece.cache.framework.location.LatLong;

// location latlong to be used as center points of grid cells
public class LocationLatLong implements Location {
	private LatLong latLong;
	private double distance; // distance in between points
	
	
	public LocationLatLong(LatLong latLong)
	{
		this.latLong = latLong;
	}
	
	public LocationLatLong(LatLong latLong, double distance) {
		this(latLong);
		this.distance = distance;
	}
	
	public LatLong center() {
		return latLong;
	}
	
	public LatLong getLatLong() {
		return latLong;
	}
	public void setLatLong(LatLong latLong) {
		this.latLong = latLong;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	
}
