package org.energy_home.jemma.osgi.dal.impl;

import org.energy_home.dal.functions.PowerProfileFunction;
import org.energy_home.dal.functions.data.PowerProfileConstraintsData;
import org.energy_home.dal.functions.data.PowerProfileData;
import org.energy_home.dal.functions.data.PowerProfilePhasesData;
import org.energy_home.dal.functions.type.ScheduledPhaseAttribute;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileScheduleConstraintsResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileServer;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileStateResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ScheduledPhase;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.energy_home.jemma.osgi.dal.utils.DataConverters;
import org.osgi.service.dal.DeviceException;
import org.osgi.service.dal.FunctionData;
import org.osgi.service.dal.OperationMetadata;
import org.osgi.service.dal.PropertyMetadata;
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
		boolean v=(Boolean) value.getValue();
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

	//FIXME: not working
	public PowerProfileConstraintsData getConstraints(Short profileId) throws DeviceException {
		
		PowerProfileScheduleConstraintsResponse resp=null;
		try {
			resp=getCluster().execPowerProfileScheduleConstraintsRequest(profileId, appliancesProxy.getRequestContext(true));
			return DataConverters.toPowerProfileConstraintsData(resp);
		} catch(Exception e)
		{
			throw new DeviceException(e.getMessage(),e.getCause());
		}
	}
	
	public PowerProfileData getPowerProfileState() throws DeviceException
	{
		try{
			PowerProfileStateResponse resp=getCluster().execPowerProfileStateRequest(this.appliancesProxy.getRequestContext(true));
			return DataConverters.toPowerProfileData(resp);
		} catch(Exception e)
		{
			throw new DeviceException(e.getMessage(),e.getCause());
		}
	}

	public PowerProfilePhasesData getPowerProfilePhases(Short PowerProfileID) throws DeviceException
	{

		try{
			PowerProfileResponse resp=getCluster().execPowerProfileRequest(PowerProfileID, appliancesProxy.getRequestContext(true));
			return DataConverters.toPowerProfilePhasesData(resp);
		} catch(Exception e)
		{
			throw new DeviceException(e.getMessage(),e.getCause());
		}
	}
	
	public void scheduleEnergyPhases(Short PowerProfileID,ScheduledPhaseAttribute[] phases) throws DeviceException
	{

		try{
			getCluster().execEnergyPhasesScheduleNotification(PowerProfileID, DataConverters.toScheduledPhases(phases), appliancesProxy.getRequestContext(true));
			
		} catch(Exception e)
		{
			throw new DeviceException(e.getMessage(),e.getCause());
		}
	}


}
