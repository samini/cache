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

package edu.cmu.ece.cache.framework.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.os.Environment;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	

	private final static String TAG = DatabaseHelper.class.getName();
	
	//The Android's default system path of your application database.
	//We need this path to copy the database to the SD card
	//Also need the databaseName and tableName set as well.
    private final static String DB_PATH = "/data/data/edu.cmu.ece.cache.framework/databases/";
    private final static String DB_SD_PATH = "Databases/";
	
	private String databaseName;
	private String tableName;
	private final Context context;
	
	// the columns and types to be used, key is the column name
	// value is its type
	private HashMap<String, String> tableColumns;
	
	// variables to contain the status of the external storage which is used
	// to copy the database out to the sd card
	private boolean mExternalStorageAvailable = false;
	private boolean mExternalStorageWriteable = false;
	private String mExternalStorageState =  null;
	
	// assign the column
	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version, String databaseName, String tableName, HashMap<String, String> tableColumns) {
		this(context, name, factory, version);
		this.databaseName = databaseName;
		this.tableName = tableName;
		this.tableColumns = tableColumns;
	}
	
	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		this.context = context;
	}

	// the create function only creates if the table does not exists
	@Override
	public void onCreate(SQLiteDatabase db) {
		createTableIfNotExists(db, tableName, tableColumns);
	}

	// the upgrade will drop the previous table and create a new one
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// on upgrades drop the old table
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
		
		String sqlStatement = "DROP TABLE IF EXISTS " + tableName +";";
		db.execSQL(sqlStatement);
		onCreate(db);
	}
	
	// create new table, most of this code is similar to the onCreateCode
	public void createTableIfNotExists(SQLiteDatabase db, String tableName, HashMap<String, String> tableColumns)
	{
		// db really shouldn't be null
		if (db == null) {
			return;
		}
		
		// if there is no columns set for creation
		if (tableColumns == null)
		{
			return;
		}
		
		if (tableName == null || tableName.equals(""))
		{
			return;
		}
		
		String columns = "";
		
		Set s = tableColumns.entrySet();
		
		if (s == null) return;
		
		Iterator i = s.iterator();
		
		if (i == null) return;
		
		while(i.hasNext()) {
			//Map.Entry<String, BluetoothDevice> me = (Map.Entry<String, BluetoothDevice>) i.next();
			Map.Entry<String, String> me = (Map.Entry<String, String>) i.next();
			
			if (me.getKey() == null || me.getKey().equals("")) continue;
			if (me.getValue() == null || me.getValue().equals("")) continue;
		
			// there will be an extra ", " at the end
			columns += me.getKey() + ' ' + me.getValue() + ", ";	
		}
		
		String sqlStatement = "CREATE TABLE IF NOT EXISTS " 
            + tableName
            + " ("
            + columns.substring(0, columns.length()-2)
            + ");";
		
		Log.d(TAG, sqlStatement);
		db.execSQL(sqlStatement);
	}
	
	public void dropTable(SQLiteDatabase db, String tableName)
	{
		String sqlStatement = new StringBuilder().append("DROP TABLE ").append(tableName).append(';').toString();
		db.execSQL(sqlStatement);
	}
	
	// function to check if the database exists
	public boolean checkDatabase(String databaseName){
		 
    	SQLiteDatabase checkDB = null;
 
    	try{
    		String myPath = DB_PATH + databaseName;
    		checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);
    	}catch(SQLiteException e){
    		//database does't exist yet.
    	}
 
    	if(checkDB != null){
    		checkDB.close();
    	}
    	
    	return checkDB != null ? true : false;
    }
	
	public boolean checkExternalStorageWritable()
	{
		boolean val = false;
		
		this.mExternalStorageState = Environment.getExternalStorageState(); 
		if (mExternalStorageState.equals(Environment.MEDIA_MOUNTED)) {
			this.mExternalStorageAvailable = this.mExternalStorageWriteable = true;
			val = true;
		} else if (mExternalStorageState.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			this.mExternalStorageAvailable = true;
			this.mExternalStorageWriteable = false;
		} else {
			this.mExternalStorageAvailable = this.mExternalStorageWriteable = false;
		}
		
		return val;
	}
	
	public boolean checkExternalStorageReadable()
	{
		boolean val = false;
		
		this.mExternalStorageState = Environment.getExternalStorageState(); 
		if (mExternalStorageState.equals(Environment.MEDIA_MOUNTED)) {
			this.mExternalStorageAvailable = this.mExternalStorageWriteable = true;
			val = true;
		} else if (mExternalStorageState.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
			this.mExternalStorageAvailable = true;
			this.mExternalStorageWriteable = false;
			val = true;
		} else {
			this.mExternalStorageAvailable = this.mExternalStorageWriteable = false;
		}
		
		return val;
	}
	
	// checks to see if there is a Databases/ directory under the sdcard
	// if there is not, it creates one
	private boolean hasExternalStoragePublicDatabasesWritable() {
	    // Create a path where we will place our picture in the user's
	    // public pictures directory and check if the file exists.  If
	    // external storage is not currently mounted this will think the
	    // picture doesn't exist.
		if (!checkExternalStorageWritable())
			return false;
		
	    File path = Environment.getExternalStorageDirectory();
	    
	    if (path == null || path.equals("") || !path.canWrite())
	    {
	    	return false;
	    }
	    
	    File databases = new File(path, DatabaseHelper.DB_SD_PATH);
	    
	    if (databases != null)
	    {
	    	if (databases.exists())
	    	{
	    		if (databases.isDirectory())
	    		{
	    			return true;
	    		}
	    		else
	    		{
	    			// this might be a bit risky 
	    			// if it is a file and we are deleting it
	    			databases.delete();
	    			databases.mkdirs();
	    			return true;
	    		}
	    	}
	    	else 
	    	{
	    		databases.mkdirs();
	    		return true;
	    	}
	    }
	    
	    return false;
	}
	
	// checks to see if there is a Databases/ directory under the sdcard
	// if there is not, it creates one
	private boolean hasExternalStoragePublicDatabasesReadable() {
	    // Create a path where we will place our picture in the user's
	    // public pictures directory and check if the file exists.  If
	    // external storage is not currently mounted this will think the
	    // picture doesn't exist.
		if (!checkExternalStorageReadable())
			return false;
		
	    File path = Environment.getExternalStorageDirectory();
	    
	    if (path == null || path.equals("") || !path.canRead())
	    {
	    	return false;
	    }
	    
	    File databases = new File(path, DatabaseHelper.DB_SD_PATH);
	    
	    if (databases != null)
	    {
	    	if (databases.exists())
	    	{
	    		if (databases.isDirectory())
	    		{
	    			return true;
	    		}
	    	}
	    }
	    
	    return false;
	}
	
	
	public boolean copyDatabaseToSDCard()
	{
		// check that external storage is writable
		if (!this.checkExternalStorageWritable())
			return false;
		
		Log.d(TAG, "External storage is writable");
		
		// creates the directory if it does not exist
		if (!this.hasExternalStoragePublicDatabasesWritable())
			return false;
		
		Log.d(TAG, "Directory for databases on external storage exists");
		
		// check that database to be written exists
		if (!this.checkDatabase(databaseName))
			return false;
		
		Log.d("TAG", "Database to be copied exists");
		
		
		
		// Path to the just created empty db
    	String outFileName = Environment.getExternalStorageDirectory() + "/" +
    						DB_SD_PATH + this.databaseName;
    	
    	String inFileName = DB_PATH + this.databaseName;
    	
    	OutputStream myOutput = null;
    	InputStream myInput = null;
    	
    	//Open the empty db as the output stream
    	try {
			myOutput = new FileOutputStream(outFileName);
			myInput = new FileInputStream(inFileName);
			
			byte[] inBuffer = new byte[1024];
			int length;
			
			while ((length = myInput.read(inBuffer)) > 0)
			{
				myOutput.write(inBuffer, 0, length);
			}
			
			myOutput.flush();
			myOutput.close();
			return true;
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally 
		{
			if (myOutput != null)
			{
				try {
					myOutput.close();
					myOutput = null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (myInput != null)
			{
				try {
					myInput.close();
					myInput = null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return false;
	}
	
	/*
	 * Copies the database over from the Databases folder on the external storage
	 * This should be used carefully as if there are any existing databases of
	 * the same name, they will be overwritten.
	 */
	public boolean copyDatabaseFromSDCard()
	{
		// check that external storage is writable
		if (!this.checkExternalStorageReadable())
			return false;
		
		Log.d(TAG, "External storage is readable");
		
		// creates the directory if it does not exist
		if (!this.hasExternalStoragePublicDatabasesReadable())
			return false;
		
		Log.d(TAG, "Directory for databases on external storage exists");
		
		// first do a dummy call to get a readable database
    	// this way if the db does not exist it is created and we copy over it
    	SQLiteDatabase dummy = this.getReadableDatabase();
    	dummy.close();

		// Path to the just created empty db
    	String outFileName = DB_PATH + this.databaseName;
    	
    	String inFileName = Environment.getExternalStorageDirectory() + "/" +
							DB_SD_PATH + this.databaseName;
    	
    	OutputStream myOutput = null;
    	InputStream myInput = null;
    	
    	//Open the empty db as the output stream
    	try {
			myOutput = new FileOutputStream(outFileName);
			myInput = new FileInputStream(inFileName);
			
			byte[] inBuffer = new byte[1024];
			int length;
			
			while ((length = myInput.read(inBuffer)) > 0)
			{
				myOutput.write(inBuffer, 0, length);
			}
			
			myOutput.flush();
			myOutput.close();
			return true;
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally 
		{
			if (myOutput != null)
			{
				try {
					myOutput.close();
					myOutput = null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if (myInput != null)
			{
				try {
					myInput.close();
					myInput = null;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		return false;
	}
	
	/*
	 * Copies the database over from the assets folder
	 * This should be used carefully as if there are any existing databases of
	 * the same name, they will be overwritten.
	 */
	public boolean copyDataBaseFromAssets(){
    	
		if (databaseName == null || databaseName.equals(""))
		{
			return false;
		}
		
    	Log.d(TAG, "Creating input and output streams...");
    	
    	// first do a dummy call to get a readable database
    	// this way if the db does not exist it is created and we copy over it
    	SQLiteDatabase dummy = this.getReadableDatabase();
    	dummy.close();
 
    	InputStream myInput = null;
    	OutputStream myOutput = null;
    	
    	try
    	{
    		//Open your local db as the input stream
        	myInput = context.getAssets().open(databaseName);
     
        	// Path to the just created empty db
        	String outFileName = DB_PATH + databaseName;
     
        	//Open the empty db as the output stream
        	myOutput = new FileOutputStream(outFileName);
     
        	Log.d(TAG, "Copying database over...");
        	
        	//transfer bytes from the inputfile to the outputfile
        	byte[] buffer = new byte[1024];
        	int length;
        	while ((length = myInput.read(buffer))>0){
        		myOutput.write(buffer, 0, length);
        	}
     
        	Log.d(TAG, "Almost done! closing streams...");
        	
        	//Close the streams
        	myOutput.flush();
        	myOutput.close();
        	myInput.close();
        	
        	Log.d(TAG, "Copy successful!");
        	
        	return true;
    	} catch (IOException e) {
    		e.printStackTrace();
    		
    		if (myOutput != null)
    		{
    			try {
					myOutput.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
    			myOutput = null;
    			
    			try 
    			{
    				myInput.close();
    			} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
    			myInput = null;
    		}
    	}

    	return false;
    }

	public String getDatabaseName() {
		return databaseName;
	}

	public String getTableName() {
		return tableName;
	}

	public HashMap<String, String> getTableColumns() {
		return tableColumns;
	}
	
}
