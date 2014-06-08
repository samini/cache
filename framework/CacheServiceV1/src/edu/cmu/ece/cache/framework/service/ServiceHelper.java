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

package edu.cmu.ece.cache.framework.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class ServiceHelper {
	public static final String TAG = ServiceHelper.class.getName();
	
	private Context mContext;
	private Class<?> serviceClass;
	
	// service variables
	private ServiceConnection serviceConnection;
	private IService service;
	private boolean serviceBound = false;
	
	public ServiceHelper(Context c, Class<?> serviceClass)
	{
		mContext = c;
		this.serviceClass = serviceClass;
	}
	
	public void startService() {
    	Intent serviceStartIntent = new Intent(mContext, serviceClass);
    	mContext.startService(serviceStartIntent);
    }
    
	public void stopService() {
    	if (serviceBound && serviceConnection != null && service != null) {
    		try {
    			// this calls stop self
				service.remoteStopSelf();
			} catch (RemoteException e) {
				// another way to call stop on the service
				Intent serviceStartIntent = new Intent(mContext, serviceClass);
				mContext.stopService(serviceStartIntent);
			}
    	}
    	
    	unbindService();
    	
    	Log.d(TAG, "Stopped Services.");
    }
    
	// can always override this function to allow for more functionality
	// this should be overriden if some actions are required as soon as the service is started
    public void bindService() {
    	serviceConnection = new ServiceConnection() {

			public void onServiceConnected(ComponentName name, IBinder service) {
				ServiceHelper.this.service = IService.Stub.asInterface(service);
			}

			public void onServiceDisconnected(ComponentName name) {
				service = null;
				serviceBound = false;
			}
    		
    	};
    	
    	Intent serviceBindIntent = new Intent(mContext, serviceClass);
    	serviceBound = mContext.bindService(serviceBindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }
    
    // this should be setup such that can unbind multiple times without causing problems
    public void unbindService() {
    	try {
    		mContext.unbindService(serviceConnection);
    	} catch (IllegalArgumentException e) {
    		Log.d(TAG, "Service already unbound?");
    	}
    	
    	serviceBound = false;
    	serviceConnection = null;
    	service = null;
    	
		Log.d(TAG, "Successfully unbound services");
    }
    
	public ServiceConnection getServiceConnection() {
		return serviceConnection;
	}

	public Context getContext() {
		return mContext;
	}

	public Class<?> getServiceClass() {
		return serviceClass;
	}

	public void setServiceConnection(ServiceConnection serviceConnection) {
		this.serviceConnection = serviceConnection;
	}

	public IService getService() {
		return service;
	}

	public void setService(IService service) {
		this.service = service;
	}

	public boolean isServiceBound() {
		return serviceBound;
	}

	public void setServiceBound(boolean serviceBound) {
		this.serviceBound = serviceBound;
	}
    
}
