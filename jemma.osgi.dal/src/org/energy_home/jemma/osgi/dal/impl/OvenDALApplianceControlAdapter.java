package org.energy_home.jemma.osgi.dal.impl;

import java.math.BigDecimal;

import javax.lang.model.element.ExecutableElement;

import org.energy_home.dal.functions.Oven;
import org.energy_home.dal.functions.WashingMachine;
import org.energy_home.dal.functions.data.TimeData;
import org.energy_home.dal.functions.type.TimeAttribute;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlServer;
import org.energy_home.jemma.ah.cluster.zigbee.eh.WriteAttributeRecord;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.energy_home.jemma.osgi.dal.utils.DataConverters;
import org.osgi.service.dal.DeviceException;
import org.osgi.service.dal.FunctionData;
import org.osgi.service.dal.OperationMetadata;
import org.osgi.service.dal.PropertyMetadata;
import org.osgi.service.dal.Units;
import org.osgi.service.dal.functions.data.LevelData;

public class OvenDALApplianceControlAdapter extends BaseApplianceControlDalAdapter implements Oven{

	private static String ZIGBEETIMEUNIT="ZIGBEE_TIME";
	private static String SPINUNIT="SPIN";
	private static String CYCLEUNIT="CYCLE";
	
	public OvenDALApplianceControlAdapter(String appliancePid, Integer endPointId,
			IAppliancesProxy appliancesProxy) {
		super(appliancePid, endPointId, appliancesProxy);
	}
	
	@Override
	public FunctionData getMatchingPropertyValue(String attributeName, IAttributeValue attributeValue) {
		
		FunctionData data=null;
		switch(attributeName)
		{
			case ApplianceControlServer.ATTR_TemperatureTarget0_NAME:
				int value=(int)(attributeValue.getValue());
				data=new LevelData(attributeValue.getTimestamp(), null, Units.DEGREE_CELSIUS, new BigDecimal(value));
				break;
			case ApplianceControlServer.ATTR_CycleTarget0_NAME:
				Short v=(Short)(attributeValue.getValue());
				data=new LevelData(attributeValue.getTimestamp(), null, CYCLEUNIT, new BigDecimal(v));
				break;
			case ApplianceControlServer.ATTR_Spin_NAME:
				Short v2=(Short)(attributeValue.getValue());
				data=new LevelData(attributeValue.getTimestamp(), null, SPINUNIT, new BigDecimal(v2));
				break;
			case ApplianceControlServer.ATTR_StartTime_NAME:
				TimeAttribute t=DataConverters.toTimeAttribute((int) attributeValue.getValue());
				data=new TimeData(attributeValue.getTimestamp(), null,t);
				break;
			case ApplianceControlServer.ATTR_FinishTime_NAME:
				TimeAttribute t2=DataConverters.toTimeAttribute((int) attributeValue.getValue());
				data=new TimeData(attributeValue.getTimestamp(), null,t2);
				break;
			case ApplianceControlServer.ATTR_RemainingTime_NAME:
				TimeAttribute t3=DataConverters.toTimeAttribute((int) attributeValue.getValue());
				data=new TimeData(attributeValue.getTimestamp(), null,t3);
				break;
			default:
				return null;
		}
	
		
		
		return data;
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
		return null;
	}

	@Override
	public Object getServiceProperty(String propName) {
		return null;
	}

	@Override
	public LevelData getCycle() throws DeviceException {
		LevelData cycle=null;
		Short result;
		try {
			result=getCluster().getCycleTarget0(appliancesProxy.getRequestContext(true));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(),e.getCause());
		}
		cycle=new LevelData(System.currentTimeMillis(), null, CYCLEUNIT, new BigDecimal(result));
		return cycle;
	}

	@Override
	public void setCycle(Short cycle)  throws DeviceException{
		execSingleWriteFunction(ApplianceControlServer.ATTR_CycleTarget0_NAME,cycle);		
		
	}

	@Override
	public LevelData getTemperature() throws DeviceException {
		LevelData temperature=null;
		int result;
		try {
			result=getCluster().getTemperatureTarget0(appliancesProxy.getRequestContext(true));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(),e.getCause());
		}
		temperature=new LevelData(System.currentTimeMillis(), null, Units.DEGREE_CELSIUS, new BigDecimal(result));
		return temperature;
	}

	@Override
	public void setTemperature(Integer temperature)  throws DeviceException {
		execSingleWriteFunction(ApplianceControlServer.ATTR_TemperatureTarget0_NAME, temperature);
	}

	@Override
	public TimeData getStartTime()  throws DeviceException{
		TimeData time=null;
		int result;
		try {
			result=getCluster().getStartTime(appliancesProxy.getRequestContext(true));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(),e.getCause());
		}
		TimeAttribute attr=DataConverters.toTimeAttribute(result);
		time=new TimeData(System.currentTimeMillis(), null, attr);
		
		return time;
	}

	@Override
	public void setStartTime(TimeData data) throws DeviceException {
		this.execSingleWriteFunction(ApplianceControlServer.ATTR_StartTime_NAME, DataConverters.toApplianceTime(data.getTimeAttribute()));
	}

	@Override
	public TimeData getFinishTime()  throws DeviceException{
		TimeData time=null;
		int result;
		try {
			result=getCluster().getFinishTime(appliancesProxy.getRequestContext(true));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(),e.getCause());
		}
		TimeAttribute attr=DataConverters.toTimeAttribute(result);
		time=new TimeData(System.currentTimeMillis(), null, attr);
		return time;
	}

	@Override
	public TimeData getRemainingTime() throws DeviceException {
		TimeData time=null;
		int result;
		try {
			result=getCluster().getRemainingTime(appliancesProxy.getRequestContext(true));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(),e.getCause());
		}
		TimeAttribute attr=DataConverters.toTimeAttribute(result);
		time=new TimeData(System.currentTimeMillis(), null, attr);
		return time;
	}

	@Override
	public void execStartCycle()  throws DeviceException{
		this.execCommand(ApplianceControlServer.CMD_Start_ID);
	}

	@Override
	public void execStopCycle() throws DeviceException {
		this.execCommand(ApplianceControlServer.CMD_Stop_ID);		
	}

	@Override
	public void execPauseCycle() throws DeviceException {
		this.execCommand(ApplianceControlServer.CMD_Pause_ID);
		
	}

	@Override
	public void execOverloadPauseResume()  throws DeviceException {
		try {
			getCluster().execOverloadPauseResume(appliancesProxy.getRequestContext(true));
		} catch (ApplianceException | ServiceClusterException e) {
			throw new DeviceException(e.getMessage(), e.getCause());
		}
		
	}

	@Override
	public void execOverloadPause()  throws DeviceException{
		try {
			getCluster().execOverloadPause(appliancesProxy.getRequestContext(true));
		} catch (ApplianceException | ServiceClusterException e) {
			throw new DeviceException(e.getMessage(), e.getCause());
		}
		
	}

	@Override
	public void execOverloadWarning() throws DeviceException {
		try {
			getCluster().execOverloadWarning(((short) 4),appliancesProxy.getRequestContext(true));
		} catch (ApplianceException | ServiceClusterException e) {
			throw new DeviceException(e.getMessage(), e.getCause());
		}
		
	}

}
