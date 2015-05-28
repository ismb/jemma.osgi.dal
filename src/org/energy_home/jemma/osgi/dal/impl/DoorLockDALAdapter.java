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

	private static String DOORLOCKSERVER = "org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer";

	public DoorLockDALAdapter(String appliancePid,Integer endPointId,IAppliancesProxy appliancesProxy)
	{
		super(appliancePid,endPointId,appliancesProxy);
	}

	
	public PropertyMetadata getPropertyMetadata(String propertyName) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public OperationMetadata getOperationMetadata(String operationName) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Object getServiceProperty(String propName) {
		// TODO Auto-generated method stub
		return null;
	}
	

	
	public FunctionData getMatchingPropertyValue(String attributeName, IAttributeValue value) {
		Short state=(Short)value.getValue();
		return new DoorLockData(System.currentTimeMillis(), null, state.equals(Short.valueOf((short) 1))?DoorLockData.STATUS_CLOSED:DoorLockData.STATUS_OPEN);
	}

	
	
	private DoorLockServer getCluster()
	{
		return (DoorLockServer)this.appliancesProxy.getAppliance(appliancePid).getEndPoint(endPointId).getServiceCluster(DOORLOCKSERVER);
	}

	
	public void open() throws DeviceException {
		try {
			getCluster().execUnlockDoor("0",appliancesProxy.getRequestContext(true));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(), e.getCause());
		}

		
	}

	
	public void close() throws DeviceException {
		try {
			getCluster().execLockDoor("1",appliancesProxy.getRequestContext(true));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(), e.getCause());
		}

		
	}

	
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
