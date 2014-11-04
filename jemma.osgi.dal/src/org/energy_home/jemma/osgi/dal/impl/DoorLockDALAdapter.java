package org.energy_home.jemma.osgi.dal.impl;

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
public class DoorLockDALAdapter extends BaseDALAdapter implements BooleanControl {

	private static String DOORLOCKCLUSTER = "org.energy_home.jemma.ah.cluster.zigbee.closures.DoorLockServer";

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
	public BooleanData getData() throws DeviceException {
		Short data = null;
		
		try {
			data=getCluster().getLockState(appliancesProxy.getRequestContext(true));
			
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(), e.getCause());
		}

		return new BooleanData(System.currentTimeMillis(), null, data==1?false:true);
	}

	@Override
	public void setData(boolean data) throws UnsupportedOperationException, IllegalStateException, DeviceException,
			IllegalArgumentException {
		throw new UnsupportedOperationException("Unimplemented method");
	}

	@Override
	public void reverse() throws UnsupportedOperationException, IllegalStateException, DeviceException {
		
		try {
			BooleanData state=getData();
			if(state.value==true)
			{
				setFalse();
			}else{
				setTrue();
			}
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(), e.getCause());
		}

	}

	@Override
	public void setTrue() throws UnsupportedOperationException, IllegalStateException, DeviceException {
		try {
			getCluster().execUnlockDoor("0",appliancesProxy.getRequestContext(true));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(), e.getCause());
		}

	}

	@Override
	public void setFalse() throws UnsupportedOperationException, IllegalStateException, DeviceException {
		try {
			getCluster().execLockDoor("1",appliancesProxy.getRequestContext(true));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(), e.getCause());
		}

	}

	@Override
	public FunctionData getMatchingPropertyValue(String attributeName, IAttributeValue value) {
		return new BooleanData(value.getTimestamp(), null, value.getValue().equals("1")?false:true);
	}

	
	@Override
	public void updateApplianceSubscriptions() {
		// TODO Auto-generated method stub
		
	}
	
	private DoorLockServer getCluster()
	{
		return (DoorLockServer)this.appliancesProxy.getAppliance(appliancePid).getEndPoint(endPointId).getServiceCluster(DOORLOCKCLUSTER);
	}

}
