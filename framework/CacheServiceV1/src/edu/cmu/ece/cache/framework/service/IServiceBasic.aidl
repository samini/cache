package edu.cmu.ece.cache.framework.service;

interface IServiceBasic {
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
	
	String remoteRequestContent(in String appName, in String schema, in String request);
	
	String remoteHelloWorld(in String name);
}