package org.energy_home.jemma.osgi.dal.impl;

import java.math.BigDecimal;

import org.energy_home.dal.functions.WashingMachine;
import org.energy_home.dal.functions.data.TimeData;
import org.energy_home.dal.functions.type.TimeAttribute;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlServer;
import org.energy_home.jemma.ah.cluster.zigbee.eh.WriteAttributeRecord;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.osgi.service.dal.DeviceException;
import org.osgi.service.dal.FunctionData;
import org.osgi.service.dal.OperationMetadata;
import org.osgi.service.dal.PropertyMetadata;
import org.osgi.service.dal.Units;
import org.osgi.service.dal.functions.data.LevelData;

public class WashingMachineDALApplianceControlAdapter extends BaseDALAdapter implements WashingMachine{

	private static String APPLIANCECONTROLCLUSTER = "org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlServer";
	private static String ZIGBEETIMEUNIT="ZIGBEE_TIME";
	private static String SPINUNIT="SPIN";
	private static String CYCLEUNIT="CYCLE";
	
	public WashingMachineDALApplianceControlAdapter(String appliancePid, Integer endPointId,
			IAppliancesProxy appliancesProxy) {
		super(appliancePid, endPointId, appliancesProxy);
	}
	
	
	private void execSingleWriteFunction(String name,Object value) throws DeviceException
	{
		WriteAttributeRecord record=new WriteAttributeRecord();
		record.name=name;
		record.value=value;
		
		ApplianceControlServer cluster=getCluster();
		try {
			cluster.execWriteFunctions(new WriteAttributeRecord[]{record}, appliancesProxy.getRequestContext(true));
		} catch (ApplianceException | ServiceClusterException e) {
			throw new DeviceException("Error setting attribute "+record.name+" for device "+appliancePid,e);
		}
	}
	
	private void execCommand()
	{
		//getCluster().execCommandExecution(ApplianceControlServer.c, context)
	}
	
	private ApplianceControlServer getCluster()
	{
		return (ApplianceControlServer) appliancesProxy.getAppliance(appliancePid).getEndPoint(endPointId).getServiceCluster(APPLIANCECONTROLCLUSTER);
	}
	
	@Override
	public FunctionData getMatchingPropertyValue(String attributeName, IAttributeValue attributeValue) {
		
		LevelData levelData=null;
		switch(attributeName)
		{
			case ApplianceControlServer.ATTR_TemperatureTarget0_NAME:
				int value=(int)(attributeValue.getValue());
				levelData=new LevelData(attributeValue.getTimestamp(), null, Units.DEGREE_CELSIUS, new BigDecimal(value));
				break;
			case ApplianceControlServer.ATTR_CycleTarget0_NAME:
				Short v=(Short)(attributeValue.getValue());
				levelData=new LevelData(attributeValue.getTimestamp(), null, CYCLEUNIT, new BigDecimal(v));
				break;
			case ApplianceControlServer.ATTR_Spin_NAME:
				Short v2=(Short)(attributeValue.getValue());
				//divide by 100
				levelData=new LevelData(attributeValue.getTimestamp(), null, SPINUNIT, new BigDecimal(v2));
				break;
				//TODO: handle time-related attributes
		//	case ApplianceControlServer.ATTR_StartTime_NAME:
			default:
				return null;
		}
	
		
		
		return levelData;
	}

	@Override
	public void updateApplianceSubscriptions() {
		
	}

	@Override
	public PropertyMetadata getPropertyMetadata(String propertyName) throws IllegalArgumentException {

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
	public LevelData getCycle() throws DeviceException {
		LevelData cycle=null;
		Short result;
		try {
			result=(Short)this.appliancesProxy.invokeClusterMethod(appliancePid, endPointId, APPLIANCECONTROLCLUSTER,
					"getCycleTarget0", createParams(APPLIANCECONTROLCLUSTER, "getCycleTarget0", new String[0]));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(),e.getCause());
		}
		cycle=new LevelData(System.currentTimeMillis(), null, CYCLEUNIT, new BigDecimal(result));
		return cycle;
	}

	@Override
	public void setCycle(Short cycle)  throws DeviceException{
		execSingleWriteFunction("CycleTarget0",cycle);		
		
	}

	@Override
	public LevelData getTemperature() throws DeviceException {
		LevelData temperature=null;
		int result;
		try {
			result=(int)this.appliancesProxy.invokeClusterMethod(appliancePid, endPointId, APPLIANCECONTROLCLUSTER,
					"getTemperatureTarget0", createParams(APPLIANCECONTROLCLUSTER, "getTemperatureTarget0", new String[0]));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(),e.getCause());
		}
		temperature=new LevelData(System.currentTimeMillis(), null, Units.DEGREE_CELSIUS, new BigDecimal(result));
		return temperature;
	}

	@Override
	public void setTemperature(Integer temperature)  throws DeviceException {
		execSingleWriteFunction("TemperatureTarget0", temperature);
		
	}

	@Override
	public LevelData getSpin() throws DeviceException {
		LevelData spin=null;
		int result;
		try {
			result=(short)this.appliancesProxy.invokeClusterMethod(appliancePid, endPointId, APPLIANCECONTROLCLUSTER,
					"getSpin", createParams(APPLIANCECONTROLCLUSTER, "getSpin", new String[0]));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(),e.getCause());
		}
		spin=new LevelData(System.currentTimeMillis(), null, "SPIN", new BigDecimal(result));
		return spin;
	}

	@Override
	public void setSpin(Short spin)  throws DeviceException{
		execSingleWriteFunction("Spin", spin);
	}

	@Override
	public TimeData getStartTime()  throws DeviceException{
		TimeData time=null;
		int result;
		try {
			result=(int)this.appliancesProxy.invokeClusterMethod(appliancePid, endPointId, APPLIANCECONTROLCLUSTER,
					"getSpin", createParams(APPLIANCECONTROLCLUSTER, "getSpin", new String[0]));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(),e.getCause());
		}
		//time=new TimeData(System.currentTimeMillis(), null, result);
		TimeAttribute attr=new TimeAttribute(result);

		return time;
	}

	@Override
	public void setStartTime(TimeData data) throws DeviceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public TimeData getFinishTime()  throws DeviceException{
		TimeData time=null;
		int result;
		try {
			result=(int)this.appliancesProxy.invokeClusterMethod(appliancePid, endPointId, APPLIANCECONTROLCLUSTER,
					"getFinishTime", createParams(APPLIANCECONTROLCLUSTER, "getFinishTime", new String[0]));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(),e.getCause());
		}
		time=new TimeData(System.currentTimeMillis(), null, result);
		return time;
	}

	@Override
	public TimeData getRemainingTime() throws DeviceException {
		TimeData time=null;
		int result;
		try {
			result=(int)this.appliancesProxy.invokeClusterMethod(appliancePid, endPointId, APPLIANCECONTROLCLUSTER,
					"getRemainingTime", createParams(APPLIANCECONTROLCLUSTER, "getRemainingTime", new String[0]));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(),e.getCause());
		}
		time=new TimeData(System.currentTimeMillis(), null, result);
		return time;
	}

	@Override
	public void execStartCycle()  throws DeviceException{
		
	}

	@Override
	public void execStopCycle() throws DeviceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execPauseCycle() throws DeviceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execOverloadPauseResume()  throws DeviceException {
		try {
			getCluster().execOverloadPauseResume(appliancesProxy.getRequestContext(true));
		} catch (ApplianceException | ServiceClusterException e) {
			throw new DeviceException("Unable to exec the overload pause resume", e);
		}
		
	}

	@Override
	public void execOverloadPause()  throws DeviceException{
		try {
			getCluster().execOverloadPause(appliancesProxy.getRequestContext(true));
		} catch (ApplianceException | ServiceClusterException e) {
			throw new DeviceException("Unable to exec the overload pause", e);
		}
		
	}

	@Override
	public void execOverloadWarning() throws DeviceException {
		try {
			getCluster().execOverloadWarning(((short) 4),appliancesProxy.getRequestContext(true));
		} catch (ApplianceException | ServiceClusterException e) {
			throw new DeviceException("Unable to exec the overload pause", e);
		}
		
	}

}
