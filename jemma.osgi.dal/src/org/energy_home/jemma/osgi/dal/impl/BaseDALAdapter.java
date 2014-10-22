package org.energy_home.jemma.osgi.dal.impl;

import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.energy_home.jemma.ah.hac.lib.ext.TextConverter;
import org.energy_home.jemma.osgi.dal.ClusterDALAdapter;

public abstract class BaseDALAdapter implements ClusterDALAdapter{

	protected IAppliancesProxy appliancesProxy;
	protected Integer endPointId;
	protected String appliancePid;
	
	
	public BaseDALAdapter(String appliancePid, Integer endPointId, IAppliancesProxy appliancesProxy) {
		this.appliancePid=appliancePid;
		this.endPointId=endPointId;
		this.appliancesProxy=appliancesProxy;
	}

	protected Object[] createParams(String clusterName,String methodName,String[] args)
	{
		Object[] objectParams=null;
		try {
			objectParams = TextConverter.getObjectParameters(Class.forName( clusterName)
					, methodName,
					args,//empty argument array, for testing
					appliancesProxy.getRequestContext(true));
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return objectParams;
	}
}
