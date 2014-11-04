package org.energy_home.jemma.osgi.dal.impl;

import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlServer;
import org.energy_home.jemma.ah.cluster.zigbee.eh.WriteAttributeRecord;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.osgi.service.dal.DeviceException;
import org.osgi.service.dal.FunctionData;

public abstract class BaseApplianceControlDalAdapter extends BaseDALAdapter {

	protected static String APPLIANCECONTROLCLUSTER = "org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlServer";
	
	public BaseApplianceControlDalAdapter(String appliancePid, Integer endPointId, IAppliancesProxy appliancesProxy) {
		super(appliancePid, endPointId, appliancesProxy);
	}

	protected void execCommand(short commandId) throws DeviceException
	{
		try{
			getCluster().execCommandExecution(commandId, appliancesProxy.getRequestContext(true));
		}catch(Exception e)
		{
			throw new DeviceException(e.getMessage(),e.getCause());
		}
	}
	
	protected void execSingleWriteFunction(String name,Object value) throws DeviceException
	{
		WriteAttributeRecord record=new WriteAttributeRecord();
		record.name=name;
		record.value=value;
		
		ApplianceControlServer cluster=getCluster();
		try {
			cluster.execWriteFunctions(new WriteAttributeRecord[]{record}, appliancesProxy.getRequestContext(true));
		} catch (ApplianceException | ServiceClusterException e) {
			throw new DeviceException(e.getMessage(),e.getCause());
		}
	}
	
	protected ApplianceControlServer getCluster()
	{
		return (ApplianceControlServer) appliancesProxy.getAppliance(appliancePid).getEndPoint(endPointId).getServiceCluster(APPLIANCECONTROLCLUSTER);
	}

}
