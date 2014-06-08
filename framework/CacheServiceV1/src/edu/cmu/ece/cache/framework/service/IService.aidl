package edu.cmu.ece.cache.framework.service;

import edu.cmu.ece.cache.framework.database.AppTable;

interface IService {
	int remoteRegisterApplication(
		in String appName,
		in String url,
		in String api,
		double cellWidth,
		double cellHeight,
		int priority,
		int rate, 
		boolean multiLevel,
		boolean overlay
		);
		
	void remoteStopSelf();
	
	List remoteListTables();
	
	void remoteDropAppTable(int tableId);
	
	void remoteCopyDBToSD(int tableId);
	
	int remoteNewInstance(int tableId);
	
	AppTable remoteGetMasterTableEntry(int tableId);
	
	boolean remoteIsWifiEnabled();
	
	boolean remoteIsPluggedIn();
	
	boolean remoteIsConnected();
	
	boolean remoteCreateCache(int initialTableId, in String address, double centerLat, double CenterLon, double radius);
	
	void remoteDownloadRequirementsCheck();
	
	String remoteRequestContent(in String appName, in String schema, in String request);
	
	String remoteHelloWorld(in String name);
}