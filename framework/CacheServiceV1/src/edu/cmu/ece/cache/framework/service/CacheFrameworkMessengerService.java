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

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

public class CacheFrameworkMessengerService extends Service {

	/**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    
    private MyServiceHelper serviceHelper;
	
    @Override
	public void onCreate()
	{
		super.onCreate();
		
		// create the service connection
		serviceHelper = new MyServiceHelper();
	}
    
	@Override
	public IBinder onBind(Intent arg0) {
		return mMessenger.getBinder();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		serviceHelper.unbindService();
	}
	
	// handle messages from remote processes here
	private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
        	switch (msg.what) {
        		default:
        			super.handleMessage(msg);
        	}
        }
	}
	
	// this simply connected to the aidl service
	private class MyServiceHelper extends ServiceHelper
	{
		// the constructor is automatically set up to use the context
		// of this activity and the CacheFrameworkService
		public MyServiceHelper()
		{
			super(CacheFrameworkMessengerService.this, CacheFrameworkService.class);
		}
		
		@Override
	    public void bindService() {
	    	ServiceConnection tmp = new ServiceConnection() {

				public void onServiceConnected(ComponentName name, IBinder service) {
					MyServiceHelper.this.setService(IService.Stub.asInterface(service));
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
