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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import edu.cmu.ece.cache.framework.constants.Constants;
import edu.cmu.ece.cache.framework.service.CacheFrameworkService;
import edu.cmu.ece.cache.framework.service.IService;
import edu.cmu.ece.cache.framework.service.ServiceHelper;

public class DeviceStatus extends Activity {
	
	private MyServiceHelper serviceHelper;
	
	private Handler handler;
	
	private static final int DOWNLOAD_ID = Menu.FIRST;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_status);
        
        handler = new Handler();
        
        // create the service connection
		serviceHelper = new MyServiceHelper();
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
	    menu.add(0, DOWNLOAD_ID, 0, R.string.menu_dl);
        return true;
    }
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
        
	        case DOWNLOAD_ID:
	        	if (serviceHelper != null &&
	        			serviceHelper.getService() != null) {
	        		try {
						serviceHelper.getService().remoteDownloadRequirementsCheck();
					} catch (RemoteException e) {
						e.printStackTrace();
					}
	        	}
	        	return true;
        }
       
        return super.onMenuItemSelected(featureId, item);
    }
	
	private class MyServiceHelper extends ServiceHelper
	{
		// the constructor is automatically set up to use the context
		// of this activity and the CacheFrameworkService
		public MyServiceHelper()
		{
			super(DeviceStatus.this, CacheFrameworkService.class);
		}
		
		@Override
	    public void bindService() {
	    	ServiceConnection tmp = new ServiceConnection() {

				public void onServiceConnected(ComponentName name, IBinder service) {
					MyServiceHelper.this.setService(IService.Stub.asInterface(service));
					CheckBox c;
					
					c = (CheckBox) findViewById(R.id.pluggedInCheckBox);
					
					try {
						c.setChecked(serviceHelper.getService().remoteIsPluggedIn());
								
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					
					c = (CheckBox) findViewById(R.id.wifiEnabledCheckBox);
					
					try {
						c.setChecked(serviceHelper.getService().remoteIsWifiEnabled());
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					
					c = (CheckBox) findViewById(R.id.apConnectedCheckBox);
					
					try {
						c.setChecked(serviceHelper.getService().remoteIsConnected());
					} catch (RemoteException e) {
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
