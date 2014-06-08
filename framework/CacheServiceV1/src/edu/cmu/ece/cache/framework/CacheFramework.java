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
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.cmu.ece.cache.framework.service.CacheFrameworkService;
import edu.cmu.ece.cache.framework.service.IService;
import edu.cmu.ece.cache.framework.service.ServiceHelper;

public class CacheFramework extends Activity {
	public final static String TAG = CacheFramework.class.getName();
	
	private MyServiceHelper serviceHelper = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Button button;
        
        button = (Button) findViewById(R.id.viewTablesButton);
		button.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				Intent intent = new Intent(CacheFramework.this, ApplicationTables.class);
				startActivity(intent);
			}
		});
		
		button = (Button) findViewById(R.id.deviceStatusButton);
		button.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				Intent intent = new Intent(CacheFramework.this, DeviceStatus.class);
				startActivity(intent);
			}
		});
        
        // logic for the stop services button
        button = (Button) findViewById(R.id.stopServiceButton);
		button.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				
				try {
					AlertDialog.Builder builder = new AlertDialog.Builder(CacheFramework.this);
					//builder.setMessage("Are you sure you want to stop value upload?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					builder.setMessage("Are you sure you want to stop services? Note services will be started the next time you start this application or on phone restart.").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							serviceHelper.stopService();
						}
					}).setNegativeButton("No", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
					AlertDialog alert = builder.create();
					alert.show();

				} catch (Exception e) {
					e.printStackTrace();
				}				
			}
			
		});
		
		// logic for service restart
		button = (Button) findViewById(R.id.restartServiceButton);
		button.setOnClickListener(new OnClickListener() {
			
			public void onClick(View arg0) {

				try {
					AlertDialog.Builder builder = new AlertDialog.Builder(CacheFramework.this);
					builder.setMessage("Are you sure you want to restart services?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							Log.d(TAG, "Stopping services!");
							
							// no link to UI components
							// since there is a delay use another Thread
							
							Thread t = new Thread()
							{
								public void run()
								{
									// automatically unbinds
									serviceHelper.stopService();
									
									// sleep for a second
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										// ignore
									}
									
									Log.d(TAG, "Starting services!");
									
									serviceHelper.startService();
									
									if (!serviceHelper.isServiceBound())
									{
										serviceHelper.bindService();
									}
								}
								
							};
							
							t.start();
						}
					}).setNegativeButton("No", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
					AlertDialog alert = builder.create();
					alert.show();

				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}

		});
		
		// About button
		button = (Button) findViewById(R.id.aboutButton);
		button.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				showDialog(CacheFramework.this, R.string.about_dialog);
			}

		});
		
		serviceHelper = new MyServiceHelper();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	serviceHelper.startService();
    	serviceHelper.bindService();
    	
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	serviceHelper.unbindService();
    }
    
    @Override
    public void onDestroy() {
    	super.onDestroy();
    	serviceHelper.unbindService();
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
    
    public static void showDialog(Context context, CharSequence text) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(text).setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}

		});
		AlertDialog alert = builder.create();
		alert.show();
	}
    
    private class MyServiceHelper extends ServiceHelper
	{
		// the constructor is automatically set up to use the context
		// of this activity and the CacheFrameworkService
		public MyServiceHelper()
		{
			super(CacheFramework.this, CacheFrameworkService.class);
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