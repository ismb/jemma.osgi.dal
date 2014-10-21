package org.energy_home.jemma.osgi.dal.utils;

import java.util.Dictionary;

public class IDConverters {

	public static String getDeviceUid(String appliancePid,Dictionary configuration){
		return "ZigBee:"+configuration.get("ah.app.name")+":"+appliancePid;
	}
	
	public static String getFunctionUid(String appliancePid,Dictionary configuration,String suffix){
		return getDeviceUid(appliancePid, configuration)+":"+suffix;
	}
	
	
}
