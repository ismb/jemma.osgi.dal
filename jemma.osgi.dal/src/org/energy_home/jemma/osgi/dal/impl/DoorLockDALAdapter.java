package org.energy_home.jemma.osgi.dal.impl;

import org.energy_home.dal.functions.DoorLock;
import org.energy_home.dal.functions.data.DoorLockData;
import org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer;
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
public class DoorLockDALAdapter extends BaseDALAdapter implements DoorLock {

	private static String WINDOWCOVERINGFACTORY = "org.energy_home.jemma.ah.zigbee.windowcovering";

	public DoorLockDALAdapter(String appliancePid,Integer endPointId,IAppliancesProxy appliancesProxy)
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
		return new DoorLockData(System.currentTimeMillis(), null, value.getValue().equals("1")?DoorLockData.STATUS_CLOSED:DoorLockData.STATUS_OPEN);
	}

	
	@Override
	public void updateApplianceSubscriptions() {
		// TODO Auto-generated method stub
		
	}
	
	private DoorLockServer getCluster()
	{
		return (DoorLockServer)this.appliancesProxy.getAppliance(appliancePid).getEndPoint(endPointId).getServiceCluster(WINDOWCOVERINGFACTORY);
	}

	@Override
	public void open() throws DeviceException {
		try {
			getCluster().execUnlockDoor("0",appliancesProxy.getRequestContext(true));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(), e.getCause());
		}

		
	}

	@Override
	public void close() throws DeviceException {
		try {
			getCluster().execLockDoor("1",appliancesProxy.getRequestContext(true));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(), e.getCause());
		}

		
	}

	@Override
	public DoorLockData getStatus() throws DeviceException {
		Short data = null;
		
		try {
			data=getCluster().getLockState(appliancesProxy.getRequestContext(true));
			
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(), e.getCause());
		}

		return new DoorLockData(System.currentTimeMillis(), null, data==1?DoorLockData.STATUS_CLOSED:DoorLockData.STATUS_OPEN);
		
	}

}
