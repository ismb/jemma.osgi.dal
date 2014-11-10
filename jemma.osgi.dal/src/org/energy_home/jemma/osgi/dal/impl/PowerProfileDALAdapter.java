package org.energy_home.jemma.osgi.dal.impl;

import java.util.Map;

import org.energy_home.dal.functions.PowerProfileFunction;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileScheduleConstraintsResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.lib.AttributeValue;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.energy_home.jemma.osgi.dal.ClusterFunctionFactory;
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
public class PowerProfileDALAdapter extends BaseDALAdapter implements PowerProfileFunction {

	private static String POWERPROFILECLUISTER = "org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileServer";

	public PowerProfileDALAdapter(String appliancePid,Integer endPointId,IAppliancesProxy appliancesProxy)
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
		boolean v=(boolean) value.getValue();
		BooleanData data=new BooleanData(value.getTimestamp(), null, v);
		return data;
	}

	@Override
	public void updateApplianceSubscriptions() {
		// TODO Auto-generated method stub
		
	}
	
	private PowerProfileServer getCluster()
	{
		return (PowerProfileServer) appliancesProxy.getAppliance(appliancePid).getEndPoint(endPointId).getServiceCluster(POWERPROFILECLUISTER);
	}

	@Override
	public Short getTotalProfileNum() throws DeviceException {
		Short val=null;
		try{
			val=getCluster().getTotalProfileNum(appliancesProxy.getRequestContext(true));
		}catch(Exception e)
		{
			throw new DeviceException(e.getMessage(),e.getCause());
		}
		return val;
	}

	@Override
	public Boolean getMultipleScheduling() throws DeviceException {
		Boolean val=null;
		try{
			val=getCluster().getMultipleScheduling(appliancesProxy.getRequestContext(true));
		}catch(Exception e)
		{
			throw new DeviceException(e.getMessage(),e.getCause());
		}
		return val;
	}

	@Override
	public Boolean getEnergyRemote() throws DeviceException {
		Boolean val=null;
		try{
			val=getCluster().getEnergyRemote(appliancesProxy.getRequestContext(true));
		}catch(Exception e)
		{
			throw new DeviceException(e.getMessage(),e.getCause());
		}
		return val;
	}

	@Override
	public Short getScheduleMode() throws DeviceException {
		Short val=null;
		try{
			val=getCluster().getScheduleMode(appliancesProxy.getRequestContext(true));
		}catch(Exception e)
		{
			throw new DeviceException(e.getMessage(),e.getCause());
		}
		return val;	}

	@Override
	public void setScheduleMode(Short scheduleMode) throws DeviceException {

		try{
			getCluster().setScheduleMode(scheduleMode, appliancesProxy.getRequestContext(true));
		}catch(Exception e)
		{
			throw new DeviceException(e.getMessage(),e.getCause());
		}
		
	}

	public void getConstraints(Short profileId) throws DeviceException {
		PowerProfileScheduleConstraintsResponse resp=null;
		try {
			resp=getCluster().execPowerProfileScheduleConstraintsRequest(profileId, appliancesProxy.getRequestContext(true));
		} catch(Exception e)
		{
			throw new DeviceException(e.getMessage(),e.getCause());
		}
		
	}

	@Override
	public void getConstraints() {
		// TODO Auto-generated method stub
		
	}

}
