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

package edu.cmu.ece.cache.framework.location.geocode;

import android.net.Uri;
import edu.cmu.ece.cache.framework.http.HttpRequest;
import edu.cmu.ece.cache.framework.location.LatLong;

public class GeoCode {
	
	// FIXME: Support for the Geocoding API has been discountinued. Geocode API should be upgrade to Google Geocoding API v3.
	// http://maps.google.com/maps/geo
	
	private static final String URL = "maps.google.com";
	private static final String PATH = "maps/geo";
	private static final String SCHEME = "http";
	private static final int RETRY = 2;
	
	// call in a thread or handler
	// user puts in space separated address in s
	public static LatLong geoCode(String s)
	{
		LatLong retVal = null;
		
		Uri.Builder b = new Uri.Builder();
		b.scheme(SCHEME);
		b.authority(URL);
		b.appendEncodedPath(PATH);
		b.appendQueryParameter("q", s);
		b.appendQueryParameter("output", "csv");
		b.appendQueryParameter("key", "abcdefg");
		
		String url = b.build().toString();
		
		String response = HttpRequest.HttpRequestURL(url, RETRY);
		
		if (response == null)
			return null;
		
		String[] responseSplit = response.split(",");
		
		double lat = Double.parseDouble(responseSplit[2]);
		double lon = Double.parseDouble(responseSplit[3]);
		
		retVal = new LatLong(lat, lon);
		
		return retVal;
	}

}
