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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class MessengerServiceHelper {
	private static final String TAG = MessengerServiceHelper.class.getName();
	
	private Context mContext;
	
	private Messenger mService = null;
	private boolean mServiceBound = false;
	private ServiceConnection mServiceConnection = null;
	
	public MessengerServiceHelper(Context context) {
		mContext = context;
	}
	
	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mMessenger = new Messenger(new IncomingHandler());
	
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
	
	public void bindService() {
		mServiceConnection = new ServiceConnection() {

			public void onServiceConnected(ComponentName name, IBinder service) {
				mService = new Messenger(service);
			}

			public void onServiceDisconnected(ComponentName name) {
				mService = null;
				mServiceBound = false;
			}
    		
    	};
    	
    	Intent serviceBindIntent = new Intent(mContext, CacheFrameworkMessengerService.class);
    	mServiceBound = mContext.bindService(serviceBindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }
	
	public void unbindService() {
    	try {
    		mContext.unbindService(mServiceConnection);
    	} catch (IllegalArgumentException e) {
    		Log.d(TAG, "Service already unbound?");
    	}
    	
    	mServiceBound = false;
    	mServiceConnection = null;
    	
		Log.d(TAG, "Successfully unbound services");
    }
}
