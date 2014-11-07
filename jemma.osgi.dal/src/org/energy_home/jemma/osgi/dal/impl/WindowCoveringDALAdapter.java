package org.energy_home.jemma.osgi.dal.impl;

import org.energy_home.dal.functions.DoorLock;
import org.energy_home.dal.functions.WindowCovering;
import org.energy_home.dal.functions.data.DoorLockData;
import org.energy_home.dal.functions.data.WindowCoveringData;
import org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer;
import org.energy_home.jemma.ah.cluster.zigbee.closures.WindowCoveringServer;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.osgi.service.dal.DeviceException;
import org.osgi.service.dal.FunctionData;
import org.osgi.service.dal.OperationMetadata;
import org.osgi.service.dal.PropertyMetadata;
import org.osgi.service.dal.functions.BooleanControl;
import org.osgi.service.dal.functions.data.BooleanData;

/**
 * DAL function implementation for ZigBee OnOffServer
 * 
 * @author Ivan Grimaldi (grimaldi@ismb.it)
 *
 */
public class WindowCoveringDALAdapter extends BaseDALAdapter implements WindowCovering {

	private static String WINDOWCOVERINGFACTORY = "org.energy_home.jemma.ah.cluster.zigbee.closures.WindowCoveringServer";

	public WindowCoveringDALAdapter(String appliancePid,Integer endPointId,IAppliancesProxy appliancesProxy)
	{
		super(appliancePid,endPointId,appliancesProxy);
	}

	@Override
	public PropertyMetadata getPropertyMetadata(String propertyName) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OperationMetadata getOperationMetadata(String operationName) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getServiceProperty(String propName) {
		// TODO Auto-generated method stub
		return null;
	}
	

	@Override
	public FunctionData getMatchingPropertyValue(String attributeName, IAttributeValue value) {
		return new WindowCoveringData(System.currentTimeMillis(), null, (Short)value.getValue());
	}

	
	@Override
	public void updateApplianceSubscriptions() {
		// TODO Auto-generated method stub
		
	}
	
	private WindowCoveringServer getCluster()
	{
		return (WindowCoveringServer)this.appliancesProxy.getAppliance(appliancePid).getEndPoint(endPointId).getServiceCluster(WINDOWCOVERINGFACTORY);
	}


	@Override
	public void openUp() throws DeviceException {
		try{
			getCluster().execUpOpen(appliancesProxy.getRequestContext(true));
		}catch(Exception e)
		{
			throw new DeviceException(e.getMessage(),e);
		}
	}

	@Override
	public void closeDown() throws DeviceException {
		try{
			getCluster().execDownClose(appliancesProxy.getRequestContext(true));
		}catch(Exception e)
		{
			throw new DeviceException(e.getMessage(),e);
		}
		
	}

	@Override
	public WindowCoveringData getStatus() throws DeviceException {
		try{
			Short data = getCluster().getCurrentPositionLiftPercentage(appliancesProxy.getRequestContext(true));
			return new WindowCoveringData(System.currentTimeMillis(), null,data);
		}catch(Exception e)
		{
			throw new DeviceException(e.getMessage(),e);
		}
	}

}
