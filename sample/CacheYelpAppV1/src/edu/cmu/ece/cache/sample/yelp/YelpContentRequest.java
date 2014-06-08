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

package edu.cmu.ece.cache.sample.yelp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

import edu.cmu.ece.cache.framework.service.ServiceHelper;

public class YelpContentRequest extends Thread implements Runnable  {
	public static final String TAG = YelpContentRequest.class.getName();
	
	// based on Yelp API v1: http://www.yelp.com/developers/documentation/technical_overview
	public static final String YELP_URL = "http://api.yelp.com/business_review_search?term=%s&lat=%f&long=%f&ywsid=%s&limit=20&radius=3";
	
	// FIXME: replace the following with your YELP API id
	public static final String YELP_YWSID = "";
	public static final String YELP_SEARCH_TERM = "restaurant";
	
	private double lat, lon;
	private CustomItemizedOverlay itemizedOverlay;
	private Handler handler;
	
	// this class takes a handler instantiated in the applications main thread
	// to be able to influence UI elements
	public YelpContentRequest(Handler handler, CustomItemizedOverlay itemizedOverlay, double lat, double lon)
	{
		super();
		this.handler = handler;
		this.lat = lat;
		this.lon = lon;
		this.itemizedOverlay = itemizedOverlay;
	}
	
	// Code to Enable Cache Framework Support
	private ServiceHelper mServiceHelper;
	
	public YelpContentRequest(Handler handler, CustomItemizedOverlay itemizedOverlay, double lat, double lon, ServiceHelper serviceHelper)
	{
		super();
		this.handler = handler;
		this.lat = lat;
		this.lon = lon;
		this.itemizedOverlay = itemizedOverlay;
		this.mServiceHelper = serviceHelper;
	}
	/////////

	@Override
	public void run()
	{
		String url = YELP_URL;
        url = String.format(url, YELP_SEARCH_TERM, lat, lon, YELP_YWSID);
        
        Log.d(TAG, url);
        
        try {
        	
        	/*
            HttpGet httpGet = new HttpGet(url);
            
            HttpClient httpClient = new DefaultHttpClient();
            ResponseHandler responseHandler = new BasicResponseHandler();
			String response = httpClient.execute(httpGet, responseHandler);
			*/
        	
        	// Code replacing HTTP GET request for Cache Request
        	String response = null;
        	
        	Log.d(TAG, Main.URL);
        	
        	//if (mServiceHelper != null && mServiceHelper.isServiceBound() && mServiceHelper.getService() != null) {
        		
        	//response = mServiceHelper.getService().remoteRequestContent("CacheYelp", Main.URL, url);
        	//response = mServiceHelper.getService().remoteListTables().get(0).toString(); // works
        	response = mServiceHelper.getService().remoteRequestContent("CacheYelp", Main.URL, url);
        		
        		Log.d(TAG, "reponse:" + response);
        	//}
        	
			if (response != null)
			{
				JSONObject json = new JSONObject(response);
				
				// Log.d(TAG, response);
				
				parse (json);
			}
        } catch (RemoteException e) {
			Log.d(TAG, e.getMessage());
		} catch (Exception e) {
			Log.d(TAG, e.getMessage());
        }
	}
	
	public void parse(JSONObject json)
	{
		if (itemizedOverlay == null)
		{
			return;
		}
		
		// clear any prior results
		handler.post(new Runnable() {
			public void run()
			{
				itemizedOverlay.clear();
			}
		});
		
		
		try {
			JSONArray businesses = json.getJSONArray("businesses");
			
			int count = businesses.length();
			
			for (int i = 0; i < count; i++)
			{
				JSONObject obj = businesses.getJSONObject(i);
				
				String name = obj.getString("name");
				String address1 = obj.getString("address1");
				Double lat = obj.getDouble("latitude");
				Double lon = obj.getDouble("longitude");
				
				Log.d(TAG, name);
				
				// create an overlay item for each result
				GeoPoint point = new GeoPoint((int)(lat * 1E6), (int)(lon * 1E6));
				final OverlayItem item = new OverlayItem(point, name, address1);
				
				handler.post(new Runnable() {
					public void run()
					{
						itemizedOverlay.addOverlay(item);
					}
				});
			}
			
			Log.d(TAG, businesses.toString());
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
