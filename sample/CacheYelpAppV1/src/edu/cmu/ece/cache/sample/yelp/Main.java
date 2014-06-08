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

import java.util.List;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import edu.cmu.ece.cache.framework.constants.Constants;
import edu.cmu.ece.cache.framework.service.IServiceBasic;
import edu.cmu.ece.cache.framework.service.ServiceHelper;

public class Main extends MapActivity {
	
	public static final double CMU_LAT = 40.443599;
	public static final double CMU_LONG = -79.942274;
	public static final int DEFAULT_ZOOM = 15;
	public static final int MIN_TIME = 60 * (int) 1E3; // in milliseconds
	public static final int MIN_DISTANCE = 50; // in meters
	
	private MapView mapView;
	private MyLocationOverlay me;
	private LocationManager locationManager;
	private Criteria locationCriteria;
	private String locationProvider;
	private LocationListener onLocationChange;
	private boolean firstLocationUpdate = true;
	
	private CustomItemizedOverlay currentLocationItemizedOverlay;
	private CustomItemizedOverlay restaurantItemizedOverlay;
	private OverlayItem currentLocationItem;
	private OverlayItem item;
	
	private Handler handler;
	
	private static final int REFRESH_CONTENT_ID = Menu.FIRST;
	private static final int ABOUT_ID = Menu.FIRST + 1;
	
	private static double currentLat, currentLon;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.yelp_map_activity);
	    
	    mapView = (MapView) findViewById(R.id.mapview);
	    mapView.setBuiltInZoomControls(true);
	    
	    // center map on CMU
	    GeoPoint point = new GeoPoint((int)(CMU_LAT * 1E6), (int)(CMU_LONG * 1E6));
	    mapView.getController().setCenter(point);
	    mapView.getController().setZoom(DEFAULT_ZOOM);
	    
	    me = new MyLocationOverlay(this, mapView); 
	    mapView.getOverlays().add(me);
	    
	    locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
	    // criteria for the type of location requested
	    locationCriteria = new Criteria();
	    locationCriteria.setAltitudeRequired(false);
	    locationCriteria.setBearingRequired(false);
	    locationCriteria.setAccuracy(locationCriteria.ACCURACY_COARSE);
	    locationCriteria.setCostAllowed(true);
	    
	    onLocationChange = new LocationListener() { 
	    	
	    	public void onLocationChanged(Location location) {
	    		updateLocation(location);
		    }
		    
		    public void onProviderDisabled(String provider) {
		    // required for interface, not used
		    }
		    
		    public void onProviderEnabled(String provider) { // required for interface, not used
		    }
		    
		    public void onStatusChanged(String provider, int status, Bundle extras) {
		    // required for interface, not used
		    } 

	    };
	    
	    currentLocationItem = null;
	    item = null;
	    
	    // Point back to author of target icon
	    // http://omercetin.deviantart.com/
	    Drawable drawable = this.getResources().getDrawable(R.drawable.target);
	    currentLocationItemizedOverlay = new CustomItemizedOverlay(drawable, this);
	    
	    // http://www.brightmix.com/
	    drawable = this.getResources().getDrawable(R.drawable.restaurant_wtfpl);
	    restaurantItemizedOverlay = new CustomItemizedOverlay(drawable, this);
	    
	    List<Overlay> mapOverlays = mapView.getOverlays();
	    mapOverlays.add(currentLocationItemizedOverlay);
	    mapOverlays.add(restaurantItemizedOverlay);
	    
	    handler = new Handler();
	    
	    // CODE ADDED added code
	    mServiceHelper = new MyServiceHelper();
	    mServiceHelper.startService();
	    
	}
	
	@Override
    public void onResume() {
    	super.onResume();
    	
    	firstLocationUpdate = true;
    	locationProvider = locationManager.getBestProvider(locationCriteria, false);
    	locationManager.requestLocationUpdates(locationProvider, MIN_TIME, MIN_DISTANCE, onLocationChange);
    	
    	// CODE ADDED added code
    	mServiceHelper.bindService();
    }
	
	@Override
	public void onPause() {
		super.onPause();
		
		// turn off location updates
		locationManager.removeUpdates(onLocationChange);
		
		// added code
		mServiceHelper.unbindService();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mServiceHelper.unbindService();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
     // menu items
	    menu.add(0, REFRESH_CONTENT_ID, 0, R.string.menu_refresh);
	    menu.add(0, ABOUT_ID, 0, R.string.menu_about);
        return true;
    }
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        
	        case REFRESH_CONTENT_ID:
	        	if (!firstLocationUpdate) {
	        		// CODE ADDED
	        		//Thread t = new YelpContentRequest(handler, restaurantItemizedOverlay, currentLat, currentLon);
	        		Thread t = new YelpContentRequest(handler, restaurantItemizedOverlay, currentLat, currentLon, mServiceHelper);
	    			t.start();
	        	}
	        	return true;
	        	
	        case ABOUT_ID:
        		this.showDialog(this, R.string.about_dialog);
        		return true;

        }
       
        return super.onMenuItemSelected(featureId, item);
    }
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	private void updateLocation(Location location) {

		if (currentLocationItemizedOverlay == null) return;
		
		//Log.d(TAG, "Location Updated!");
		if (currentLocationItem != null) {
			currentLocationItemizedOverlay.removeOverlay(currentLocationItem);
		}
		
		GeoPoint point = new GeoPoint((int)(location.getLatitude() * 1E6), (int)(location.getLongitude() * 1E6));
		currentLocationItem = new OverlayItem(point, "You", "You are here!");
		currentLocationItemizedOverlay.addOverlay(currentLocationItem);
		
		if (firstLocationUpdate) {
			mapView.getController().setCenter(point);
			mapView.getController().setZoom(DEFAULT_ZOOM);
			
			// CODE ADDED
			//Thread t = new YelpContentRequest(handler, restaurantItemizedOverlay, location.getLatitude(), location.getLongitude());
			Thread t = new YelpContentRequest(handler, restaurantItemizedOverlay, location.getLatitude(), location.getLongitude(), mServiceHelper);
			t.start();
		}
		
		// from this point on
		// update the tracker but do not center the map
		firstLocationUpdate = false;
		
		currentLat = location.getLatitude();
		currentLon = location.getLongitude();
		
		// from this point on user has to refresh content manually
	}
	
	public static void showDialog(Context context, int text) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(text).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}

		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	// Code enabling Cache support
	private MyServiceHelper mServiceHelper;
	
	// based on Yelp API v1: http://www.yelp.com/developers/documentation/technical_overview
	//FIXME: Replace the value for ywsid with your own Yelp API id
	public static final String URL = String.format("http://api.yelp.com/business_review_search?term=restaurant&lat=%s&long=%s&ywsid=YOUR_YELP_API_ID&limit=20&radius=3", 
			Constants.API_LAT_DEG, Constants.API_LON_DEG);
	private class MyServiceHelper extends ServiceHelper
	{
		public MyServiceHelper()
		{
			super(Main.this);
		}
		@Override
	    public void bindService() {
	    	ServiceConnection tmp = new ServiceConnection() {
				public void onServiceConnected(ComponentName name, IBinder service) {
					MyServiceHelper.this.setService(IServiceBasic.Stub.asInterface(service));
					registerApp();
				}
				public void onServiceDisconnected(ComponentName name) {
					MyServiceHelper.this.setService(null);
					MyServiceHelper.this.setServiceBound(false);
				}
	    	};
	    	this.setServiceConnection(tmp);
	    	Intent serviceBindIntent = new Intent(ServiceHelper.SERVICE_ACTION);
	    	this.setServiceBound(this.getContext().bindService(serviceBindIntent, 
	    			this.getServiceConnection(), Context.BIND_AUTO_CREATE));
	    }
	}
	// registration
	private void registerApp() {
		
		try {
			mServiceHelper.getService().remoteRegisterApplication("CacheYelp",
					URL, Constants.API_SLL, 5000, 5000, // 3 mile is 3/sqrt(2) * 2 * 1609 = 6826
					5, 1, false, false); // priority 5, rate 1
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
