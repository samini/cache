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

public class LocationLatLongRange implements Location {
	private LatLong minLatLong; // contains the minimum for both the latitude and the longitude
	private LatLong maxLatLong; // contains the maximum for both the latitude and the longitude
	
	public LocationLatLongRange(LatLong minLatLong, LatLong maxLatLong) {
		this.minLatLong = minLatLong;
		this.maxLatLong = maxLatLong;
	}
	
	public LatLong center() {
		LatLong val = new LatLong(
				(minLatLong.getLatitude() + maxLatLong.getLatitude()) / 2.0,
				(minLatLong.getLongitude() + maxLatLong.getLongitude()) / 2.0);
		
		return val;
	}
	
	public LatLong[] verticesNoOrder() {
		LatLong[] val = new LatLong[4];
		
		val[0] = minLatLong;
		val[1] = maxLatLong;
		val[2] = new LatLong(minLatLong.getLatitude(), maxLatLong.getLongitude());
		val[3] = new LatLong(maxLatLong.getLatitude(), minLatLong.getLongitude());
		
		return val;
	}

	public LatLong getMinLatLong() {
		return minLatLong;
	}

	public void setMinLatLong(LatLong minLatLong) {
		this.minLatLong = minLatLong;
	}

	public LatLong getMaxLatLong() {
		return maxLatLong;
	}

	public void setMaxLatLong(LatLong maxLatLong) {
		this.maxLatLong = maxLatLong;
	}
}
