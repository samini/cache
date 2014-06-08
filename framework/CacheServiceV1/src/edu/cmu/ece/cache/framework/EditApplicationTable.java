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

package edu.cmu.ece.cache.framework;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import edu.cmu.ece.cache.framework.constants.Constants;
import edu.cmu.ece.cache.framework.database.AppTable;
import edu.cmu.ece.cache.framework.location.LatLong;
import edu.cmu.ece.cache.framework.location.geocode.GeoCode;
import edu.cmu.ece.cache.framework.service.CacheFrameworkService;
import edu.cmu.ece.cache.framework.service.IService;
import edu.cmu.ece.cache.framework.service.ServiceHelper;

public class EditApplicationTable extends Activity {

	private static final String TAG = EditApplicationTable.class.getName();
	
	private MyServiceHelper serviceHelper;
	private int tableId;
	private AppTable appTable;
	
	private ProgressDialog dialog;
	private Handler handler;
	
	private static final int SAVE_ID = Menu.FIRST;
	private static final int BACK_ID = Menu.FIRST + 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_application_table);
		
		handler = new Handler();
		
		tableId = this.getIntent().getExtras().getInt(Constants.APP_TABLE_ID_EXTRA);

		// create the service connection
		serviceHelper = new MyServiceHelper();
		
		Button button;
		
		button = (Button) findViewById(R.id.geoCodeButton);
		button.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				
				showProgressDialog();
				
				final TextView t;
				t = (TextView) findViewById(R.id.addressCoordinates);
				
				final EditText e;
				e = (EditText) findViewById(R.id.addressEditText);
				
				if (e == null || e.getText().equals(""))
				{
					return;
				}
				
				Thread t0 = new Thread()
				{
					public void run()
					{
						final LatLong l = GeoCode.geoCode(e.getText().toString());
						
						handler.post(new Runnable() {
							public void run() {
								if (l != null)
									t.setText(l.toString());
							}
						});
						
						hideProgressDialog();
						
					}
				};
				
				t0.start();
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		serviceHelper.unbindService();
	}

	@Override
	protected void onPause() {
		super.onPause();
		serviceHelper.unbindService();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		serviceHelper.startService();
    	serviceHelper.bindService();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
	    menu.add(0, SAVE_ID, 0, R.string.menu_save);
	    menu.add(0, BACK_ID, 0, R.string.menu_back);
        return true;
    }
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        
	        case SAVE_ID:
	        	// check to see if the table already has a center set
	        	// if it does, this table cannot be saved. it is already in use
	        	if (appTable != null &&
	        			appTable.getCenter() != null &&
	        			!appTable.getCenter().isInvalid()) {
	        		Toast.makeText( getApplicationContext(), 
	        				"Table previously initialized", 
	        				Toast.LENGTH_SHORT ).show();
	        		return true;
	        	}
	        	
	        	final String address;
	        	final double centerLat, centerLon, radius;
	        	
	        	EditText e;
	        	e = (EditText) findViewById(R.id.addressEditText);
	        	
	        	if (e == null || e.getText() == null || e.getText().toString().trim().equals("")) {
	        		Toast.makeText( getApplicationContext(), 
	        				"No address for cache", 
	        				Toast.LENGTH_SHORT ).show();
	        		
	        		return true;
	        	} else {
	        		address = e.getText().toString().trim();
	        	}
	        	
	        	TextView t;
	        	t = (TextView) findViewById(R.id.addressCoordinates);
				
	        	if (t == null || t.getText() == null || t.getText().toString().trim().equals("")) {
	        		Toast.makeText( getApplicationContext(), 
	        				"No coordinates for cache", 
	        				Toast.LENGTH_SHORT ).show();
	        		
	        		return true;
	        	} else {
	        		String coordinates =  t.getText().toString().trim();
	        		String[] coordinatesSplit = coordinates.split(
	        				new StringBuilder().append(Constants.COMMA_SEPARATOR).toString());
	        		centerLat = Double.parseDouble(coordinatesSplit[0].trim());
	        		centerLon = Double.parseDouble(coordinatesSplit[1].trim());
	        	}
	        	
	        	
	        	e = (EditText) findViewById(R.id.radiusEditText);
	        	
	        	if (e == null || e.getText() == null || e.getText().toString().trim().equals("")) {
	        		Toast.makeText( getApplicationContext(), 
	        				"No radius for cache", 
	        				Toast.LENGTH_SHORT ).show();
	        		
	        		return true;
	        	} else {
	        		radius = Double.parseDouble(e.getText().toString().trim());
	        	}
	        	
	        	showProgressDialog();
	        	
	        	Thread t0 = new Thread() {
	        		@Override
	        		public void run() {
	        			
	        			try {
							serviceHelper.getService().remoteCreateCache(tableId, 
									address, centerLat, centerLon, radius);
						} catch (RemoteException e) {
							e.printStackTrace();
						}
	        			
	        			hideProgressDialogAndFinish();
	        		}
	        	};
	        	
	        	t0.start();
	        	
	        	return true;
	        	
	        case BACK_ID:
        		// close this activity
	        	finish();
        		return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }
	
	private void populateContent()
	{
		if (appTable == null)
		{
			return;
		}
		
		Log.d(TAG, appTable.getAppName());
		Log.d(TAG, appTable.getUrl());
		
		TextView t;
		EditText e;
		
		t = (TextView) findViewById(R.id.tableIdTextView);
		t.setText(new StringBuilder().append(appTable.getId()).toString());
		
		t = (TextView) findViewById(R.id.appNameTextView);
		t.setText(appTable.getAppName());
		
		t = (TextView) findViewById(R.id.regTsTextView);
		t.setText(new StringBuilder().append(appTable.getRegTs()).toString());
		

		t = (TextView) findViewById(R.id.urlTextView);
		t.setText(appTable.getUrl());
		
		t = (TextView) findViewById(R.id.urlParamsTextView);
		t.setText(appTable.getUrlParams());
		

		t = (TextView) findViewById(R.id.apiTextView);
		t.setText(appTable.getApi());
		

		t = (TextView) findViewById(R.id.cellWidthTextView);
		t.setText(new StringBuilder().append(appTable.getCellWidth()).toString());
		

		t = (TextView) findViewById(R.id.cellHeightTextView);
		t.setText(new StringBuilder().append(appTable.getCellHeight()).toString());
		
		t = (TextView) findViewById(R.id.upComTsTextView);
		t.setText(new StringBuilder().append(appTable.getUpdateCompleteTs()).toString());
		

		t = (TextView) findViewById(R.id.upInitTsTextView);
		t.setText(new StringBuilder().append(appTable.getUpdateInitTs()).toString());
		

		t = (TextView) findViewById(R.id.priorityTextView);
		t.setText(new StringBuilder().append(appTable.getPriority()).toString());
		

		t = (TextView) findViewById(R.id.rateTextView);
		t.setText(new StringBuilder().append(appTable.getRadius()).toString());
		
		
		e = (EditText) findViewById(R.id.addressEditText);
		e.setText(appTable.getAddress());

		t = (TextView) findViewById(R.id.centerTextView);
		t.setText(appTable.getCenter().toString());

		e = (EditText) findViewById(R.id.radiusEditText);
		e.setText(new StringBuilder().append(appTable.getRadius()).toString());

		t = (TextView) findViewById(R.id.nwTextView);
		t.setText(appTable.getNw().toString());

		t = (TextView) findViewById(R.id.neTextView);
		t.setText(appTable.getNe().toString());

		t = (TextView) findViewById(R.id.seTextView);
		t.setText(appTable.getSe().toString());

		t = (TextView) findViewById(R.id.swTextView);
		t.setText(appTable.getSw().toString());
		
		t = (TextView) findViewById(R.id.numCellsTextView);
		t.setText(new StringBuilder().append(appTable.getNumCells()).toString());

		t = (TextView) findViewById(R.id.lastCellTextView);
		t.setText(new StringBuilder().append(appTable.getLastCell()).toString());

		t = (TextView) findViewById(R.id.multiLevelTextView);
		t.setText(new StringBuilder().append(appTable.getMultiLevel()).toString());

		t = (TextView) findViewById(R.id.levelTextView);
		t.setText(new StringBuilder().append(appTable.getLevel()).toString());
		
		t = (TextView) findViewById(R.id.overlayTextView);
		t.setText(new StringBuilder().append(appTable.getOverlay()).toString());
	}
	
	private void showProgressDialog() {
		dialog = ProgressDialog.show(this, 
				"", "Performing action...", 
				true);
	}
	
	// this is inside a handler as it gets called by a worker thread
	private void hideProgressDialog() {
		handler.post(new Runnable() {
			public void run() {
				if (dialog != null) {
					dialog.dismiss();
				} 
			}
		});
	}
	
	private void hideProgressDialogAndFinish() {
		handler.post(new Runnable() {
			public void run() {
				if (dialog != null) {
					dialog.dismiss();
				} 
				
				finish();
			}
		});
	}
	
	private class MyServiceHelper extends ServiceHelper
	{
		// the constructor is automatically set up to use the context
		// of this activity and the CacheFrameworkService
		public MyServiceHelper()
		{
			super(EditApplicationTable.this, CacheFrameworkService.class);
		}
		
		@Override
	    public void bindService() {
	    	ServiceConnection tmp = new ServiceConnection() {

				public void onServiceConnected(ComponentName name, IBinder service) {
					MyServiceHelper.this.setService(IService.Stub.asInterface(service));
					
					// add additional instructions
					try {
						appTable = serviceHelper.getService().remoteGetMasterTableEntry(tableId);
						populateContent();
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				public void onServiceDisconnected(ComponentName name) {
					MyServiceHelper.this.setService(null);
					MyServiceHelper.this.setServiceBound(false);
				}
	    		
	    	};
	    	
	    	this.setServiceConnection(tmp);
	    	
	    	Intent serviceBindIntent = new Intent(this.getContext(), this.getServiceClass());
	    	this.setServiceBound(this.getContext().bindService(serviceBindIntent, 
	    			this.getServiceConnection(), Context.BIND_AUTO_CREATE));
	    }
	}

}
