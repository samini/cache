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

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import edu.cmu.ece.cache.framework.constants.Constants;
import edu.cmu.ece.cache.framework.service.CacheFrameworkService;
import edu.cmu.ece.cache.framework.service.IService;
import edu.cmu.ece.cache.framework.service.ServiceHelper;

public class ApplicationTables extends ListActivity {
	
	static ArrayList<String> arrayList;
	static ArrayAdapter arrayAdapter;
	
	private MyServiceHelper serviceHelper;
	
	// menu options
	private static final int EDIT_TABLE = Menu.FIRST;
	private static final int NEW_INSTANCE = Menu.FIRST + 1;
	private static final int COPY_TABLE = Menu.FIRST + 2;
	private static final int DELETE_TABLE = Menu.FIRST + 3;
	
	// progress dialog occurs when new instance created
	// or table is being copied or deleted
	private ProgressDialog dialog;
	
	private Handler handler;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_application_tables);
		
		// create the service connection
		serviceHelper = new MyServiceHelper();
		
		// to allow context menu for each application
		registerForContextMenu(this.getListView());
		
		handler = new Handler();
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
    
    @Override
    public void onListItemClick (ListView l, View v, int position, long id)
    {
    	String s = arrayList.get((int) id);
    	final int tableId = Integer.parseInt(s.split("" + Constants.SPACE_SEPARATOR)[0]);
    	
    	if (Constants.MASTER_TABLE_ID == tableId)
    	{
	    	Toast.makeText( getApplicationContext(), 
					"Action not allowed for master table.", 
					Toast.LENGTH_SHORT ).show();
    	} else { // application tables
    		editApplicationTable(tableId);
    	}
    }
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		menu.add(0, EDIT_TABLE, 0, R.string.edit_table);
		menu.add(0, NEW_INSTANCE, 0, R.string.new_table_instance);
		menu.add(0, COPY_TABLE, 0, R.string.copy_table);
		menu.add(0, DELETE_TABLE, 0, R.string.delete_table);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	
    	int id = (int) info.id;
    	
    	String s = arrayList.get(id);
    	final int tableId = Integer.parseInt(s.split("" + Constants.SPACE_SEPARATOR)[0]);
    	
    	// worker thread
    	Thread t;
    	
    	if (Constants.MASTER_TABLE_ID == tableId)
    	{
    		switch(item.getItemId())
        	{
        	case EDIT_TABLE:
        	case NEW_INSTANCE:
        	case DELETE_TABLE:
        		Toast.makeText( getApplicationContext(), 
        				"Action not allowed for master table.", 
        				Toast.LENGTH_SHORT ).show();
        		return true;
        	case COPY_TABLE:
        		// copy the master table to SD
        		
        		showProgressDialog();
        		
        		// put in a new thread
        		t = new Thread()
        		{
        			public void run()
        			{
        				try {
        					serviceHelper.getService().remoteCopyDBToSD(Constants.MASTER_TABLE_ID);
        				} catch (RemoteException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				}
        				
        				hideProgressDialog();
        			}
        		};
        		
        		t.start();
        		
        		return true;
        	}
    		
    	} else { // application table selected
    		switch(item.getItemId())
        	{
        	case EDIT_TABLE:
        		// goes to a new activity
        		editApplicationTable(tableId);
        		break;
        	case NEW_INSTANCE:
        		showProgressDialog();
        		
        		// put in a new thread
        		t = new Thread()
        		{
        			public void run()
        			{
        				try {
        					serviceHelper.getService().remoteNewInstance(tableId);
        				} catch (RemoteException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				}
        				
        				listTables();
        				refreshListViaHandler();
        				hideProgressDialog();
        			}
        		};
        		
        		t.start();
        		return true;
        	case COPY_TABLE:
        		showProgressDialog();
        		
        		// put in a new thread
        		t = new Thread()
        		{
        			public void run()
        			{
        				try {
        					serviceHelper.getService().remoteCopyDBToSD(tableId);
        				} catch (RemoteException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				}
        				
        				hideProgressDialog();
        			}
        		};
        		
        		t.start();
        		return true;
        	case DELETE_TABLE:
        		showProgressDialog();
        		
        		// this is a dangerous action
        		// have to make sure the table is not already in use!
        		
        		// put in a new thread
        		t = new Thread()
        		{
        			public void run()
        			{
        				try {
        					serviceHelper.getService().remoteDropAppTable(tableId);
        				} catch (RemoteException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				}
        				
        				listTables();
        				refreshListViaHandler();
        				hideProgressDialog();
        			}
        		};
        		
        		t.start();
        		return true;
        	}
    	}

    	return super.onContextItemSelected(item);
	}
	
	private void refreshListViaHandler()
	{
		if (handler != null)
		{
			handler.post(new Runnable() {
				public void run() {
					refreshList();
				}
			});
		}
	}
	
	private void refreshList() {
    	arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);
		setListAdapter(arrayAdapter);
    }
	
	private void listTables()
	{
		try {
			List l = serviceHelper.getService().remoteListTables();
			
			if (l != null && l instanceof ArrayList<?>)
			{
				// remove clear the old values in list
				if (arrayList != null)
				{
					arrayList.clear();
					arrayList = null;
				}
				
				arrayList = (ArrayList<String>) l;
			}
			
		} catch (RemoteException e) {
			return;
		}
	}
	
	private class MyServiceHelper extends ServiceHelper
	{
		// the constructor is automatically set up to use the context
		// of this activity and the CacheFrameworkService
		public MyServiceHelper()
		{
			super(ApplicationTables.this, CacheFrameworkService.class);
		}
		
		@Override
	    public void bindService() {
	    	ServiceConnection tmp = new ServiceConnection() {

				public void onServiceConnected(ComponentName name, IBinder service) {
					MyServiceHelper.this.setService(IService.Stub.asInterface(service));
					
					// add additional instructions
					
					listTables();
					refreshList();
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
	
	private void editApplicationTable(int tableId)
	{
		Intent intent = new Intent(this, EditApplicationTable.class);
		intent.putExtra(Constants.APP_TABLE_ID_EXTRA, tableId);
		startActivity(intent);
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

}
