package org.energy_home.jemma.osgi.dal.utils;

import java.util.Dictionary;

public class IDConverters {

	public static String getDeviceUid(String appliancePid,Dictionary configuration){
		return "ZigBee:"+appliancePid;
	}
	
	public static String getDeviceName(String appliancePid,Dictionary configuration)
	{
		return (String) configuration.get("ah.app.name");
	}
	
	public static String getFunctionUid(String appliancePid,Dictionary configuration,String suffix){
		return getDeviceUid(appliancePid, configuration)+":"+suffix;
	}
	
	
}
