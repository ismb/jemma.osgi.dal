package org.energy_home.jemma.osgi.dal.impl;

import java.math.BigDecimal;

import javax.lang.model.element.ExecutableElement;

import org.energy_home.dal.functions.Fridge;
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
import org.osgi.service.dal.functions.data.BooleanData;
import org.osgi.service.dal.functions.data.LevelData;

public class FridgeDALApplianceControlAdapter extends BaseApplianceControlDalAdapter implements Fridge{

	private static String ZIGBEETIMEUNIT="ZIGBEE_TIME";
	private static String SPINUNIT="SPIN";
	private static String CYCLEUNIT="CYCLE";
	
	public FridgeDALApplianceControlAdapter(String appliancePid, Integer endPointId,
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
			case ApplianceControlServer.ATTR_TemperatureTarget1_NAME:
				int value2=(int)(attributeValue.getValue());
				data=new LevelData(attributeValue.getTimestamp(), null, Units.DEGREE_CELSIUS, new BigDecimal(value2));
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
	public LevelData getFridgeTemperature() throws DeviceException {
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
	public LevelData getFreezerTemperature() throws DeviceException {
		LevelData temperature=null;
		int result;
		try {
			result=getCluster().getTemperatureTarget1(appliancesProxy.getRequestContext(true));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(),e.getCause());
		}
		int max=Integer.MAX_VALUE;
		//adjust temperature: the Fridge returns it with a gap of 65536
		int realValue=result-65536;
		temperature=new LevelData(System.currentTimeMillis(), null, Units.DEGREE_CELSIUS, new BigDecimal(realValue));
		return temperature;
	}

	@Override
	public BooleanData getSuperCoolMode() throws DeviceException {

		return null;
	}

	@Override
	public void setSuperCoolMode(Boolean data) throws DeviceException {
		execSingleWriteFunction(Fridge.PROPERTY_SUPERCOOLMODE, data);
		return ;
	}

	@Override
	public BooleanData getSuperFreezeMode() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSuperFreezeMode(Boolean data) throws DeviceException {
		execSingleWriteFunction(Fridge.PROPERTY_SUPERFREEZE, data);
		return ;
	}

	@Override
	public BooleanData getEcoMode() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setEcoMode(Boolean data) throws DeviceException {
		execSingleWriteFunction(Fridge.PROPERTY_ECOMODE, data);
		return ;
	}

	@Override
	public BooleanData getIceParty() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setIceParty(Boolean data) throws DeviceException {
		execSingleWriteFunction(Fridge.PROPERTY_ICEPARTY, data);
		return;
	}

	@Override
	public BooleanData getHolidayMode() throws DeviceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHolidayMode(Boolean data) throws DeviceException {
		execSingleWriteFunction(Fridge.PROPERTY_HOLIDAYMODE, data);
		return ;
	}

}
