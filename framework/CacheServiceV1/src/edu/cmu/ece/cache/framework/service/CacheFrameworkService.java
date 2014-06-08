package edu.cmu.ece.cache.framework.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;
import edu.cmu.ece.cache.framework.CacheFramework;
import edu.cmu.ece.cache.framework.R;
import edu.cmu.ece.cache.framework.constants.Constants;
import edu.cmu.ece.cache.framework.database.AppTable;
import edu.cmu.ece.cache.framework.database.AppTableHelper;
import edu.cmu.ece.cache.framework.database.DatabaseHelper;
import edu.cmu.ece.cache.framework.database.DatabaseTableColumns;
import edu.cmu.ece.cache.framework.database.MasterTableHelper;
import edu.cmu.ece.cache.framework.download.DownloadManager;
import edu.cmu.ece.cache.framework.location.Cell;
import edu.cmu.ece.cache.framework.location.Grid;
import edu.cmu.ece.cache.framework.location.LatLong;
import edu.cmu.ece.cache.framework.location.definition.LocationBoundingBox;
import edu.cmu.ece.cache.framework.location.definition.LocationLatLong;
import edu.cmu.ece.cache.framework.request.ContentManager;
import edu.cmu.ece.cache.framework.serialization.FileNameGenerator;
import edu.cmu.ece.cache.framework.serialization.Serializer;
import edu.cmu.ece.cache.framework.service.IServiceBasic.Stub;
import edu.cmu.ece.cache.framework.url.Decoder;
import edu.cmu.ece.cache.framework.utility.MathUtils;

public class CacheFrameworkService extends Service {

	private final static String TAG = CacheFrameworkService.class.getName();
	private final static boolean DEBUG = true;
	
	// make this a forground service such that it is not killed
	private static final Class[] START_FOREGROUND_SIGNATURE = new Class[] {
	    int.class, Notification.class};
	private static final Class[] STOP_FOREGROUND_SIGNATURE = new Class[] {
	    boolean.class};
	
	// notification manager variables
	private static NotificationManager sNM;
	private Method mStartForeground;
	private Method mStopForeground;
	private Object[] mStartForegroundArgs = new Object[2];
	private Object[] mStopForegroundArgs = new Object[1];
	
	// there is only one lock for the entire system
	// any previous instance of the lock is nulled out before a new one is acquired
	private static PowerManager sPowerManager;
	private static PowerManager.WakeLock sWakeLock;
	
	// all checks on battery and wifi should have initial values set
	// the receivers are only monitoring if something changes
	
	// battery information
	// the purpose of sBatteryOkay is to make sure device is plugged
	// and has some amount of battery power before any downloading begings
	private static BroadcastReceiver sBatteryInfoReceiver;
	private static boolean sBatteryOkay;
	
	private static WifiManager sWifiManager;
	private static WifiManager.WifiLock sWifiLock;
	
	private static ConnectivityManager sConnectivityManager;
	
	// the alarm manager to start downloading
	private static AlarmManager sAlarmManager;
	
	// wifi connected action
	private static BroadcastReceiver sWifiConnReceiver;
	private static boolean sWifiOkay;
	
	// connectivity receiver
	private static BroadcastReceiver sConnectivityReceiver;
	private static boolean sConnOkay;
	
	private Notification cacheFrameworkNotification;
	
	// databases for location and for programs
	private static DatabaseHelper sMasterDBHelper;
	private static DatabaseHelper sAppDBHelper;
	private static SQLiteDatabase sMasterDB;
	private static SQLiteDatabase sAppDB;
	
	// download manager
	private static DownloadManager sDownloadManager;
	
	// request content
	private static ContentManager sContentManager;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		// put the cache service in foreground
		// so it is not accidentally killed by android.
		// this is not 100% necessary but allows fore easier proto-type
		makeForegroundService();
		
		// get the power manager for locks
		sPowerManager = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
		
		// the wifi manager
		sWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		
		// the connectivity manager
		sConnectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		// alarm manager
		sAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		
		checkBattery();
		
		// set the receiver for battery information
		createBatteryInfoReceiver();
		
		// register to get battery information
		registerReceiver(sBatteryInfoReceiver, 
			    new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		
		checkWifi();
		
		// set the receiver for wifi connectivity
		createWifiConnReceiver();
		
		registerReceiver(sWifiConnReceiver, 
			    new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
		
		checkConnectivity();
		
		// set the receiver for connectivity
		createConnectivityReceiver();
		
		registerReceiver(sConnectivityReceiver, 
			    new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		
		
		// create databases
		createDatabases();
		
		// create alarms, use to see if downloading should be kicked off
		createAlarms();
		
		// get an instance of download manager that will run in background
		sDownloadManager = DownloadManager.getInstance(sMasterDB, sAppDB);
		
		// get an instance of content manager used for content requests
		sContentManager = ContentManager.getInstance(this, sMasterDB, sAppDB);
		
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		String action = arg0.getAction();
		
		// 
		
		if (action != null) {
			
			Log.d(TAG, "action: " + action);
			
			if (action.equals("edu.cmu.ece.cache.framework.service.CacheFrameworkService")) {
				return mBinder;
			} else if (action.equals("edu.cmu.ece.cache.framework.service.CacheFrameworkServiceBasic")) {
				return mBinderBasic;
			}
		}
		
		return mBinder;
	}
	
	private final IService.Stub mBinder = new IService.Stub(){

		// all the developer has to do is call the following function to create
		// an entry for an application to be added to the cache service
		
		// registration happens with a check when called by a program
		public int remoteRegisterApplication(String appName, String url,
				String api, double cellWidth,
				double cellHeight, int priority, int rate, boolean multiLevel,
				boolean overlay) throws RemoteException {
			
			// checked so that app doesn't keep creating new tables
			return registerApplicationWithCheck( appName,  url,
					 api,  cellWidth,
					 cellHeight,  priority,  rate,  multiLevel,
					 overlay);
		}

		public void remoteStopSelf() throws RemoteException {
			stopSelf();
		}

		public List remoteListTables() throws RemoteException {
			Log.d(TAG, "remoteListTables called");
			return listTables();
		}

		public void remoteDropAppTable(int tableId) throws RemoteException {
			dropAppTable(tableId);
		}

		public void remoteCopyDBToSD(int tableId) throws RemoteException {
			copyDBToSD(tableId);
		}

		public int remoteNewInstance(int tableId) throws RemoteException {
			return newInstance(tableId);
		}

		public AppTable remoteGetMasterTableEntry(int tableId)
				throws RemoteException {
			return MasterTableHelper.selectEntry(sMasterDB, tableId);
		}

		public boolean remoteIsConnected() throws RemoteException {
			return sConnOkay;
		}

		public boolean remoteIsPluggedIn() throws RemoteException {
			return sBatteryOkay;
		}

		public boolean remoteIsWifiEnabled() throws RemoteException {
			return sWifiOkay;
		}

		public boolean remoteCreateCache(int initialTableId, String address,
				double centerLat, double CenterLon, double radius)
				throws RemoteException {
			return createCache(initialTableId, address,
					centerLat, CenterLon, radius);
		}

		public void remoteDownloadRequirementsCheck() throws RemoteException {
			dlReqsCheck();
		}

		public String remoteRequestContent(String appName, String schema, String request)
				throws RemoteException {
			Log.d(TAG, "remoteRequestContent called");
			return sContentManager.requestContent(appName, schema, request);
		}

		public String remoteHelloWorld(String name) throws RemoteException {
			return new StringBuilder().append("Hello ").append(name).append('!').toString();
		}
		
		

	};
	
	private final IServiceBasic.Stub mBinderBasic = new IServiceBasic.Stub() {
		
		public String remoteRequestContent(String appName, String schema,
				String request) throws RemoteException {
			Log.d(TAG, "remoteRequestContent called");
			return sContentManager.requestContent(appName, schema, request);
		}
		
		public int remoteRegisterApplication(String appName, String url,
				String api, double cellWidth, double cellHeight, int priority,
				int rate, boolean multiLevel, boolean overlay)
				throws RemoteException {
			// checked so that app doesn't keep creating new tables
			return registerApplicationWithCheck( appName,  url,
					 api,  cellWidth,
					 cellHeight,  priority,  rate,  multiLevel,
					 overlay);
		}
		
		public String remoteHelloWorld(String name) throws RemoteException {
			return new StringBuilder().append("Hello ").append(name).append('!').toString();
		}

		public void remoteStopSelf() throws RemoteException {
			stopSelf();
		}
		
		
	};
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (DEBUG) Log.d(TAG, "onDestory called!");
		
		// unregister the battery info receiver
		unregisterReceiver(sBatteryInfoReceiver);
		
		unregisterReceiver(sWifiConnReceiver);
		
		unregisterReceiver(sConnectivityReceiver);
		
		// stop the download manager
		if (sDownloadManager != null)
			sDownloadManager.stop();
		
		// close any open databases
		closeDatabases();
		
		// cancel all alarms
		cancelAlarms();
		
		releaseWifiLock();
		
		releaseLock();
	}
	
	// the broadcast receiver to check when requirements for download
	// are met and to kick off
	public static class DownloadBroadcastReceiver extends BroadcastReceiver {
		
		// this is really an assurance, in case the right conditions are met
		// however the original receivers do not detect and start downloading
		
		@Override
		public void onReceive(Context context, Intent intent) {
			dlReqsCheck();
		}
	}
	
	// Download Requirements Check
	private synchronized static void dlReqsCheck() {
		if (sBatteryOkay && sWifiOkay && sConnOkay) {
			acquireLockIfNot();
			enableWifiIfNot();
			acquireWifiLockIfNot();
			
			// download kick off
			Thread t = new Thread(sDownloadManager.getInstance(sMasterDB, sAppDB));
			t.start();
			
		} else {
			// stop downloading
			if (sDownloadManager != null)
				sDownloadManager.stop();
			
			// if the device is no longer plugged in release the wifi and processor locks
			if (!sBatteryOkay) {
				releaseWifiLock();
				releaseLock();
			}
		}
	}
	
	private void makeForegroundService()
	{
		// put in foreground so service is not killed
		sNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
	    try {
	        mStartForeground = getClass().getMethod("startForeground",
	                START_FOREGROUND_SIGNATURE);
	        mStopForeground = getClass().getMethod("stopForeground",
	                STOP_FOREGROUND_SIGNATURE);
	    } catch (NoSuchMethodException e) {
	        // Running on an older platform.
	        mStartForeground = mStopForeground = null;
	    }
	    
	    // link to http://lbaumann.com/
	    int icon = R.drawable.icon_grayscale;
	    CharSequence tickerText = "CMU Cache Framework Running.";
	    
	    long when = System.currentTimeMillis();
	    
	    cacheFrameworkNotification = new Notification(icon, tickerText, when);
	    cacheFrameworkNotification.defaults |= Notification.FLAG_AUTO_CANCEL;
	    ComponentName comp = new ComponentName(getApplicationContext().getPackageName(), 
	    		CacheFramework.class.getName());
	    Intent intent = new Intent().setComponent(comp);
	    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 
	    		PendingIntent.FLAG_UPDATE_CURRENT);
	    cacheFrameworkNotification.setLatestEventInfo(getApplicationContext(), "Carnegie Mellon Cache Framework", "Currently Running.", pendingIntent);
	    
	    startForegroundCompat(R.string.foreground_service_started, cacheFrameworkNotification);
	}
	
	/**
	 * This is a wrapper around the new startForeground method, using the older
	 * APIs if it is not available.
	 */
	private void startForegroundCompat(int id, Notification notification) {
	    // If we have the new startForeground API, then use it.
	    if (mStartForeground != null) {
	        mStartForegroundArgs[0] = Integer.valueOf(id);
	        mStartForegroundArgs[1] = notification;
	        try {
	            mStartForeground.invoke(this, mStartForegroundArgs);
	        } catch (InvocationTargetException e) {
	            // Should not happen.
	            Log.w("ApiDemos", "Unable to invoke startForeground", e);
	        } catch (IllegalAccessException e) {
	            // Should not happen.
	            Log.w("ApiDemos", "Unable to invoke startForeground", e);
	        }
	        return;
	    }

	    // Fall back on the old API.
	    setForeground(true);
	    sNM.notify(id, notification);
	}

	/**
	 * This is a wrapper around the new stopForeground method, using the older
	 * APIs if it is not available.
	 */
	private void stopForegroundCompat(int id) {
	    // If we have the new stopForeground API, then use it.
	    if (mStopForeground != null) {
	        mStopForegroundArgs[0] = Boolean.TRUE;
	        try {
	            mStopForeground.invoke(this, mStopForegroundArgs);
	        } catch (InvocationTargetException e) {
	            // Should not happen.
	            Log.w("ApiDemos", "Unable to invoke stopForeground", e);
	        } catch (IllegalAccessException e) {
	            // Should not happen.
	            Log.w("ApiDemos", "Unable to invoke stopForeground", e);
	        }
	        return;
	    }

	    // Fall back on the old API.  Note to cancel BEFORE changing the
	    // foreground state, since we could be killed at that point.
	    sNM.cancel(id);
	    setForeground(false);
	}
	
	// locks
	private static void acquireLock() {
		if (DEBUG) Log.d(TAG, "Acquiring wake lock.");
		
		// just to make sure there is no outstanding lock
		releaseLock();
		
		// used to prevent cpu from sleeping when uploading values
		sWakeLock = sPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		
		sWakeLock.acquire();
	}
	
	private static void acquireLockIfNot() {
		if (sWakeLock != null && sWakeLock.isHeld())
			return;
		
		acquireLock();
	}
	
	private static void releaseLock() {
		if (sWakeLock != null) {
			if (sWakeLock.isHeld()) {
				if (DEBUG) Log.d(TAG, "Releasing wake lock.");
				sWakeLock.release();
			}
			sWakeLock = null;
		}
	}
	
	// wifi lock
	private static void acquireWifiLock() {
		if (DEBUG) Log.d(TAG, "Acquiring Wifi lock.");
		
		// just to make sure there is no outstanding lock
		releaseWifiLock();
		
		if (sWifiManager != null) {
			// used to prevent cpu from sleeping when uploading values
			sWifiLock = sWifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, TAG);
			sWifiLock.acquire();
		}
	}
	
	private static void acquireWifiLockIfNot() {
		if (sWifiLock != null && sWifiLock.isHeld()) {
			return;
		}
		
		acquireWifiLock();
	}
	
	private static void releaseWifiLock() {
		if (sWifiLock != null) {
			if (sWifiLock.isHeld()) {
				if (DEBUG) Log.d(TAG, "Releasing Wifi lock.");
				sWifiLock.release();
			}
			sWifiLock = null;
		}
	}
	
	// dummy function that sets battery false
	// once the battery info is registered hopefully the true
	// value is taken
	private static void checkBattery() {
		sBatteryOkay = false;
	}
	
	// battery information
	private static void createBatteryInfoReceiver()
	{
		sBatteryInfoReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent intent) {
				String action = intent.getAction();
				
				if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
	                int level = intent.getIntExtra("level", -1);
	                int scale = intent.getIntExtra("scale", -1);
	                int plugged = intent.getIntExtra("plugged", -1);
	                
	                if (BatteryManager.BATTERY_PLUGGED_AC == plugged ||
	                		BatteryManager.BATTERY_PLUGGED_USB == plugged) {
	                	
	                	// acquire a lock to do all processing
	                	// we are plugged in so not an issue to keep processor alive
	                	acquireLockIfNot();
	                	
	                	// when we know the device is plugged in
	                	// to be able to download we need to make sure the wifi is turned on
	                	// if it is not, and that a wifi lock is acquired
	                	// TODO: we are under the assumption that the wifi connects to an AP
	                	enableWifiIfNot();
	                	
	                	// acquire a lock on the WiFi
	                	// since the battery information may change rapidly
	                	// we don't want to acquire a new lock everytime if it is already
	                	// established and held
	                	acquireWifiLockIfNot();
	                	
	                	// TODO: something is wrong if either one is -1
	                	// for now set battery okay for testing purposes
	                	if (level == -1 || scale == -1) {
	                		sBatteryOkay = true;
	                	} else {
	                	
		                	double batteryPercentage = (double)level / (double)scale;
		                	
		                	if (batteryPercentage > Constants.BATTERY_OKAY_THRESHOLD) {
		                		sBatteryOkay = true;
		                	} else {
		                		sBatteryOkay = false;
		                	}
	                	}
	                	
	                	dlReqsCheck();
	                	
	                } else {
	                	// if device is not plugged, battery is not okay
	                	sBatteryOkay = false;
	                	
	                	dlReqsCheck();
	                }

				}

			}
		};
	}
	
	private static void enableWifiIfNot() {
		if (sWifiManager != null) {
			switch (sWifiManager.getWifiState()) {
				case WifiManager.WIFI_STATE_ENABLED:
				case WifiManager.WIFI_STATE_ENABLING:
					break;
				default:
					// enable Wifi
					sWifiManager.setWifiEnabled(true);
			}
		}
	}
	
	private static void checkWifi()
	{
		sWifiOkay = sWifiManager.isWifiEnabled();
	}
	
	private static void createWifiConnReceiver() {
		sWifiConnReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent intent) {
				String action = intent.getAction();
				
				if(WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)){
					int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1);
					
					if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
						sWifiOkay = true;
					} else {
						sWifiOkay = false;
					}
					
					dlReqsCheck();
					
				}
			}
			
		};
		
	}
	
	private static void checkConnectivity() {
		NetworkInfo networkInfo = sConnectivityManager.getActiveNetworkInfo();
		
		if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			if (networkInfo.isAvailable() && networkInfo.isConnected()) {
				sConnOkay = true;
			} else {
				sConnOkay = false;
			}
		} else {
			sConnOkay = false;
		}
		
	}
	
	// TODO: ACTION_BACKGROUND_DATA_SETTING_CHANGED
	// currently our service always runs in foreground
	private static void createConnectivityReceiver() {
		sConnectivityReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent intent) {
				String action = intent.getAction();
				
				if(ConnectivityManager.CONNECTIVITY_ACTION.equals(action)){
					
					// get the network information
					NetworkInfo networkInfo = intent.getParcelableExtra(
							ConnectivityManager.EXTRA_NETWORK_INFO);
					
					// TODO: check to make sure we don't need the other info
					//NetworkInfo otherNetworkInfo = intent.getParcelableExtra(
						//	ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);
					
					if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
					{
						if (networkInfo.isAvailable() && networkInfo.isConnected())
						{
							sConnOkay = true;
						} else {
							sConnOkay = false;
						}
					}
					
					dlReqsCheck();
					
				}
			}
			
		};
	}
	
	// code to create the databases
	private void createDatabases()
	{
		createMasterDatabase();
		createAppDatabase();
	}
	
	// creates the masterDB if it does not exist
	// if it does exist, it just grabs a writable db to it
	private void createMasterDatabase()
	{
		String databaseName = Constants.CACHE_FRAMEWORK_MASTER_DB;
		String tableName = Constants.CACHE_FRAMEWORK_MASTER_TABLE;
		int version = Constants.CACHE_FRAMEWORK_VERSION;
		
		sMasterDBHelper = new DatabaseHelper(
				getApplicationContext(), databaseName, null, // no cursor factory assigned
				version, databaseName, tableName, 
				DatabaseTableColumns.getInstance().getMasterTableColumns());
		
		// unless getWritableDatabase or getReadableDatabase is called
		// the table is not created
		sMasterDB = sMasterDBHelper.getWritableDatabase();
	}
	
	private void createAppDatabase()
	{
		// do not create any tables for app databases
		// tables are created when various applications are registered
		String databaseName = Constants.CACHE_FRAMEWORK_APP_DB;
		int version = Constants.CACHE_FRAMEWORK_VERSION;
		
		sAppDBHelper = new DatabaseHelper(
				getApplicationContext(), databaseName, null, // no cursor factory assigned
				version, databaseName, null, 
				null);
		
		// unless getWritableDatabase or getReadableDatabase is called
		// the table is not created
		sAppDB = sAppDBHelper.getWritableDatabase();
	}
	
	// code to close the databases
	private void closeDatabases()
	{
		closeMasterDatabase();
		closeAppDatabase();
	}
	
	private void closeMasterDatabase()
	{
		if (sMasterDB != null) sMasterDB.close();
		if (sMasterDBHelper != null) sMasterDBHelper.close();
	}
	
	private void closeAppDatabase()
	{
		if (sAppDB != null) sAppDB.close();
		if (sAppDBHelper != null) sAppDBHelper.close();
	}
	
	private void createAlarms() {
		// runs every half hour
		// The DownloadBroadcastReceiver is set to check periodically
		// whether the requirements are met and can start downloading
		Intent intent = new Intent(this, DownloadBroadcastReceiver.class);
		PendingIntent sender0 = PendingIntent.getBroadcast(this, 
				Constants.DOWNLOAD_REQUEST_CODE, intent, 0);
		sAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, 
				System.currentTimeMillis() + Constants.INTERVAL_ONE_MINUTE, 
				AlarmManager.INTERVAL_FIFTEEN_MINUTES, sender0);
	}
	
	private void cancelAlarms() {
		// cancel DownloadBroadcastReceiver
		Intent intent = new Intent(this, DownloadBroadcastReceiver.class);
		PendingIntent sender0 = PendingIntent.getBroadcast(this, 
				Constants.DOWNLOAD_REQUEST_CODE, intent, 0);
		sAlarmManager.cancel(sender0);
	}
	
	/*
	 * Below are the functions that are called by remote functions through mBinder
	 */

	// if app is already registered just returns true
	// does not reinsert into the master table
	private int registerApplicationWithCheck(String appName, String url,
			String api, double cellWidth,
			double cellHeight, int priority, int rate, boolean multiLevel,
			boolean overlay)
	{
		if (sMasterDBHelper == null || sMasterDB == null || !sMasterDB.isOpen())
			return Constants.INVALID_ROW_ID;
		
		if (sAppDBHelper == null | sAppDB == null || !sAppDB.isOpen())
			return Constants.INVALID_ROW_ID;
		
		// if the app already existing it has a valid row id assigned
		int rowId = MasterTableHelper.appExists(sMasterDB, appName);
		
		if (Constants.INVALID_ROW_ID != rowId) {
			return rowId;
		}
		
		return registerApplication(appName, url,  
				api, cellWidth, cellHeight, priority, rate, multiLevel, overlay);
	}
	
	// insert a new entry into the master table
	// and create a table in the app database
	private int registerApplication(String appName, String url,
			String api, double cellWidth,
			double cellHeight, int priority, int rate, boolean multiLevel,
			boolean overlay)
	{
		if (sMasterDBHelper == null || sMasterDB == null)
			return Constants.INVALID_ROW_ID;
		
		if (sAppDBHelper == null | sAppDB == null)
			return Constants.INVALID_ROW_ID;
		
		
		int rowId = (int)MasterTableHelper.insertEntry(sMasterDB, appName, url, 
				api, cellWidth, cellHeight, priority, rate, multiLevel, overlay);
		
		// if unable to insert the row, just return
		// do not create a new table
		if (rowId == Constants.INVALID_ROW_ID)
		{
			return Constants.INVALID_ROW_ID;
		}
		
		// create a new table for the application
		sAppDBHelper.createTableIfNotExists(sAppDB, 
				AppTableHelper.tableName(appName, rowId), 
				DatabaseTableColumns.getInstance().getAppTableColumns());
		
		return rowId;
	}
	
	private List listTables()
	{
		if (sMasterDBHelper == null || sMasterDB == null)
		{
			return null;
		}
		
		List tables = new ArrayList<String>();
		
		StringBuilder sb = new StringBuilder();
		sb.append(Constants.MASTER_TABLE_ID).append(Constants.SPACE_SEPARATOR).append("Master Table");
		
		tables.add(sb.toString());
		
		String[] columns = {DatabaseTableColumns.MasterTableColumns.ID, 
				DatabaseTableColumns.MasterTableColumns.APP_NAME,
				DatabaseTableColumns.MasterTableColumns.ADDRESS};
		String orderBy = "ID";
		Cursor c = sMasterDB.query(sMasterDBHelper.getTableName(), 
				columns, null, null, null, null, orderBy);
		
		if (c != null)
		{
			int idColumnIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.ID);
			int appNameColumnIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.APP_NAME);
			int addressColumnIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.ADDRESS);
			
			if (c.moveToFirst())
			{
				do {
					sb = new StringBuilder();
					sb.append(c.getInt(idColumnIndex));
					sb.append(Constants.SPACE_SEPARATOR);
					sb.append(c.getString(appNameColumnIndex));
					sb.append(Constants.SPACE_SEPARATOR);
					sb.append(c.getString(addressColumnIndex));
					tables.add(sb.toString());
				} while(c.moveToNext());
			}
			
			c.close();
		}
		
		return tables;
	}
	
	private void copyDBToSD(int tableId)
	{
		if (Constants.MASTER_TABLE_ID == tableId)
		{
			sMasterDBHelper.copyDatabaseToSDCard();
		} else {// application table
			sAppDBHelper.copyDatabaseToSDCard();
		}
	}
	
	private void dropAppTable(int tableId)
	{
		// the tables that can be delete are only in the app database
		// although the entry from the master database also needs to be removed
		
		// extra caution
		if (Constants.MASTER_TABLE_ID == tableId)
		{
			return;
		} else if (0 >= tableId) {
			return;
		}
		
		MasterTableHelper.deleteEntry(sMasterDB, tableId);
		
		// get the table name
		String tableName = MasterTableHelper.selectTableName(sMasterDB, tableId);
		
		// drop the old table
		if (tableName != null)
		{
			sAppDBHelper.dropTable(sAppDB, tableName);
		}

	}
	
	private int newInstance(int tableId)
	{
		// extra caution
		if (Constants.MASTER_TABLE_ID == tableId)
		{
			return Constants.INVALID_ROW_ID;
		} else if (tableId <= 0) {
			return Constants.INVALID_ROW_ID;
		}
		
		String selection = new 
		StringBuilder().append(DatabaseTableColumns.MasterTableColumns.ID).append("=?").toString();
		String[] selectionArgs = {new StringBuilder().append(tableId).toString()};
		// grab all columns
		Cursor c = sMasterDB.query(Constants.CACHE_FRAMEWORK_MASTER_TABLE, 
				null, selection, selectionArgs, null, null, null);
		
		if (c != null)
		{
			int retVal = Constants.INVALID_ROW_ID;
			
			if (c.moveToFirst())
			{
				int appNameIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.APP_NAME);
				int urlIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.URL);
				int apiIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.API);
				int cellWidthIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.CELL_WIDTH);
				int cellHeightIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.CELL_HEIGHT);
				int priorityIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.PRIORITY);
				int rateIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.RATE);
				int multiLevelIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.MULTI_LEVEL);
				int overlayIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.OVERLAY);
				
				// the rest is an application registration
				retVal = registerApplication(
						c.getString(appNameIndex), 
						c.getString(urlIndex),
						c.getString(apiIndex), 
						c.getDouble(cellWidthIndex),
						c.getDouble(cellHeightIndex), 
						c.getInt(priorityIndex), 
						c.getInt(rateIndex), 
						c.getInt(multiLevelIndex) == 0 ? false : true,
						c.getInt(overlayIndex) == 0 ? false : true
						);
			}
			
			c.close();			
			return retVal;
		}
		
		return Constants.INVALID_ROW_ID;
		
	}
	
	// this can be a pretty intensive function
	private boolean createCache(int initialTableId, String address,
			double centerLat, double centerLon, double radius)
	{
		//Log.d(TAG, "Inside createCache function");
		
		if (sMasterDBHelper == null || sMasterDB == null)
			return false;
		
		LatLong center = new LatLong(centerLat, centerLon);
		
		double radiusMeters = radius * Constants.MILE_TO_METERS;
		
		double overlappingSquareHalfDiagonal = MathUtils.overlappingSquareHalfDiagonal(radiusMeters);
		double side = 2 * radiusMeters; // radius is in miles
		
		//Log.d(TAG, "Side (m): " + side);
		
		// find the North West point of the grid for all grids, regardless of overlap
		LatLong nw = center.adjacentLatLong(overlappingSquareHalfDiagonal, LatLong.NORTH_WEST_BEARING);
		
		// get this tables overlay and multi-level specifications
		String selection = new 
		StringBuilder().append(DatabaseTableColumns.MasterTableColumns.ID).append("=?").toString();
		String[] selectionArgs = {new StringBuilder().append(initialTableId).toString()};
		String[] columns = {
				DatabaseTableColumns.MasterTableColumns.APP_NAME,
				DatabaseTableColumns.MasterTableColumns.CELL_WIDTH,
				DatabaseTableColumns.MasterTableColumns.CELL_HEIGHT,
				DatabaseTableColumns.MasterTableColumns.MULTI_LEVEL, 
				DatabaseTableColumns.MasterTableColumns.OVERLAY};
		Cursor c = sMasterDB.query(Constants.CACHE_FRAMEWORK_MASTER_TABLE, 
				columns, selection, selectionArgs, null, null, null);
		
		// guess that there is no multilevel and no overlay
		int multiLevel = 0;
		int overlay = 0;
		double cellWidth = 0, cellHeight = 0; // these are in meters
		String appName = null;
		
		if (c != null) {
			if (c.moveToFirst()) {
				int appNameIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.APP_NAME);
				int multiLevelIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.MULTI_LEVEL);
				int overlayIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.OVERLAY);
				int cellWidthIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.CELL_WIDTH);
				int cellHeightIndex = c.getColumnIndex(DatabaseTableColumns.MasterTableColumns.CELL_HEIGHT);
				
				appName = c.getString(appNameIndex);
				multiLevel = c.getInt(multiLevelIndex);
				overlay = c.getInt(overlayIndex);
				cellWidth = c.getDouble(cellWidthIndex);
				cellHeight = c.getDouble(cellHeightIndex);
			}
			
			c.close();
		}
		
		/*
		Log.d(TAG, appName);
		Log.d(TAG, "" + multiLevel);
		Log.d(TAG, "" + overlay);
		Log.d(TAG, "" + cellWidth);
		Log.d(TAG, "" + cellHeight);
		*/
		
		// if either the cell width and height are 0 there is not much we can do
		if (cellWidth == 0 || cellHeight == 0) {
			return false;
		}
		
		boolean initialTable = true;
		
		int tableId = Constants.INVALID_ROW_ID;
		
		int gridSize = -1;
		int level = 0;
		
		// serializer to serialize the grid files
		Serializer serializer = new Serializer(this);
		
		// the key point here is to end up with one cell that covers the entire region
		do {
			int numRows = (int)Math.ceil(side / (double)cellHeight);
			int numCols = (int)Math.ceil(side / (double)cellWidth);
			
			Log.d(TAG, "numRows: " + numRows);
			Log.d(TAG, "numCols: " + numCols);
			
			// create a grid for itself and one for the overlay if there is an overlay
			// note if there is no overlay then overlay is 0
			for (int i = 0; i <= overlay; i++) {
				LatLong initial = null;
				if (0 == i) {
					initial = nw;
				} else {
					// introduce a shift to the south east
					initial = nw.adjacentLatLong(cellWidth / 2.0, LatLong.EAST_BEARING).
						adjacentLatLong(cellHeight / 2.0, LatLong.SOUTH_BEARING);
				}
				
				Grid g = new Grid(initial, numRows, numCols, cellWidth, cellHeight);
				// next step is a rather data intensive one
				g.createGrid();
				
				// this is the same regardless of overlay
				gridSize = g.getGridSize();
				
				//Log.d(TAG, "gridSize: " + gridSize);
				
				if (initialTable) {
					tableId = initialTableId;
				} else {
					// create a new instance of the table only once
					// no reason to also create for the overlay
					// as both values get inserted into the same table
					if (0 == i) { // just the original
						// create a new table and a new entry in the master table
						tableId = newInstance(initialTableId);
					}
				}
				
				// update the geography in the master table
				// only for the original table, not for overlay
				if (0 == i) {
					Cell cell = g.overarchingCell();
					MasterTableHelper.updateGeography(sMasterDB, tableId, address, 
							centerLat, centerLon, radius, 
							cell.getVertices()[Constants.VERTEX_NW_INDEX].getLatitude(), 
							cell.getVertices()[Constants.VERTEX_NW_INDEX].getLongitude(), 
							cell.getVertices()[Constants.VERTEX_NE_INDEX].getLatitude(), 
							cell.getVertices()[Constants.VERTEX_NE_INDEX].getLongitude(), 
							cell.getVertices()[Constants.VERTEX_SE_INDEX].getLatitude(), 
							cell.getVertices()[Constants.VERTEX_SE_INDEX].getLongitude(), 
							cell.getVertices()[Constants.VERTEX_SW_INDEX].getLatitude(), 
							cell.getVertices()[Constants.VERTEX_SW_INDEX].getLongitude(), 
							gridSize,
							level);
				}
				
				// insert the grid into its according table
				AppTableHelper.insertGrid(sAppDB, appName, tableId, g, i == 0 ? false: true);
				
				// serialize the grid
				// i is whether it is the original table or the overlay
				serializer.serialize(
						FileNameGenerator.fileName(appName, tableId, level, i), g);
				
			}
			
			// increase the level number
			level++;
			
			// double the cell width and height at each level
			cellWidth = cellWidth * 2;
			cellHeight = cellHeight * 2;
			
			// past first run, no longer the initial table
			if (initialTable) {
				initialTable = false;
			}
		} while (multiLevel > 0 &&
				gridSize != 1); // multi level check takes into effect only to not create
									// more than one grid for non-level caches
		
		return true;
	}
}
