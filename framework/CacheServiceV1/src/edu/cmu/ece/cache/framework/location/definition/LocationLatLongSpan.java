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

public class LocationLatLongSpan implements Location {
	private LatLong center;
	private double latSpan, longSpan;
	
	public LocationLatLongSpan(LatLong center, double latSpan, double longSpan) {
		this.center = center;
		this.latSpan = latSpan;
		this.longSpan = longSpan;
	}
	
	public LatLong[] verticesNoOrder()
	{
		double halfLatSpan = latSpan / 2.0;
		double halfLongSpan = longSpan / 2.0;
		
		LatLong[] val = new LatLong[4];
		
		val[0] = new LatLong(center.getLatitude() - halfLatSpan, center.getLongitude() - halfLongSpan);
		val[1] = new LatLong(center.getLatitude() - halfLatSpan, center.getLongitude() + halfLongSpan);
		val[2] = new LatLong(center.getLatitude() + halfLatSpan, center.getLongitude() - halfLongSpan);
		val[3] = new LatLong(center.getLatitude() + halfLatSpan, center.getLongitude() + halfLongSpan);
		
		return val;
 		
	}
	
	public LatLong center() {
		return getCenter();
	}

	public LatLong getCenter() {
		return center;
	}

	public void setCenter(LatLong center) {
		this.center = center;
	}

	public double getLatSpan() {
		return latSpan;
	}

	public void setLatSpan(double latSpan) {
		this.latSpan = latSpan;
	}

	public double getLongSpan() {
		return longSpan;
	}

	public void setLongSpan(double longSpan) {
		this.longSpan = longSpan;
	}
}
