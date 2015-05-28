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
	
	public FridgeDALApplianceControlAdapter(String appliancePid, Integer endPointId,
			IAppliancesProxy appliancesProxy) {
		super(appliancePid, endPointId, appliancesProxy);
	}
	
	
	public FunctionData getMatchingPropertyValue(String attributeName, IAttributeValue attributeValue) {
		
		FunctionData data=null;
		if(ApplianceControlServer.ATTR_TemperatureTarget0_NAME.equals(attributeName))
		{
			int value=(Integer)(attributeValue.getValue());
			data=new LevelData(attributeValue.getTimestamp(), null, Units.DEGREE_CELSIUS, new BigDecimal(value));
		}else if(ApplianceControlServer.ATTR_TemperatureTarget1_NAME.equals(attributeName))
		{
			int value2=(Integer)(attributeValue.getValue());
			//value adjustment
			int realValue=value2-65536;
			data=new LevelData(attributeValue.getTimestamp(), null, Units.DEGREE_CELSIUS, new BigDecimal(realValue));
		}else if(ApplianceControlServer.ATTR_NormalMode_NAME.equals(attributeName))
		{
			Boolean normalMode=(Boolean) attributeValue.getValue();
			data=new BooleanData(attributeValue.getTimestamp(),null,normalMode.booleanValue());
		}else if(ApplianceControlServer.ATTR_EcoMode_NAME.equals(attributeName))
		{
			Boolean ecoMode=(Boolean) attributeValue.getValue();
			//boolean ecomode= (boolean) attributeValue.getValue();
			data=new BooleanData(attributeValue.getTimestamp(),null,ecoMode.booleanValue());
		}else if(ApplianceControlServer.ATTR_SuperCoolMode_NAME.equals(attributeName))
		{
			Boolean supercool=(Boolean) attributeValue.getValue();
			data=new BooleanData(attributeValue.getTimestamp(),null,supercool.booleanValue());
		}else if(ApplianceControlServer.ATTR_SuperFreezeMode_NAME.equals(attributeName))
		{
			Boolean superfreeze=(Boolean) attributeValue.getValue();
			data=new BooleanData(attributeValue.getTimestamp(),null,superfreeze.booleanValue());
		}else if(ApplianceControlServer.ATTR_HolidayMode_NAME.equals(attributeName))
		{
			Boolean holiday=(Boolean) attributeValue.getValue();
			data=new BooleanData(attributeValue.getTimestamp(),null,holiday.booleanValue());
		}else if(ApplianceControlServer.ATTR_IceParty_NAME.equals(attributeName))
		{
			Boolean iceparty=(Boolean) attributeValue.getValue();
			data=new BooleanData(attributeValue.getTimestamp(),null,iceparty.booleanValue());
		}else{
				return null;
		}
		return data;
	}
	
	public PropertyMetadata getPropertyMetadata(String propertyName) throws IllegalArgumentException {

		return null;
	}

	
	public OperationMetadata getOperationMetadata(String operationName) throws IllegalArgumentException {
		return null;
	}

	
	public Object getServiceProperty(String propName) {
		return null;
	}

	
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
	
	
	public LevelData getFreezerTemperature() throws DeviceException {
		LevelData temperature=null;
		int result;
		try {
			result=getCluster().getTemperatureTarget1(appliancesProxy.getRequestContext(true));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(),e.getCause());
		}
		//adjust temperature: the Fridge returns it with a gap of 65536
		int realValue=result-65536;
		temperature=new LevelData(System.currentTimeMillis(), null, Units.DEGREE_CELSIUS, new BigDecimal(realValue));
		return temperature;
	}

	
	public BooleanData getSuperCoolMode() throws DeviceException {
		BooleanData superCoolMode=null;
		Boolean result;
		try {
			result=getCluster().getSuperCoolMode(appliancesProxy.getRequestContext(true));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(),e.getCause());
		}
		
		superCoolMode=new BooleanData(System.currentTimeMillis(), null, result);
		return superCoolMode;
	}

	
	public void setSuperCoolMode(Boolean data) throws DeviceException {
		execSingleWriteFunction(Fridge.PROPERTY_SUPERCOOLMODE, data);
		return ;
	}

	
	public BooleanData getSuperFreezeMode() throws DeviceException {
		BooleanData superFreezeMode=null;
		Boolean result;
		try {
			result=getCluster().getSuperFreezeMode(appliancesProxy.getRequestContext(true));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(),e.getCause());
		}
		
		superFreezeMode=new BooleanData(System.currentTimeMillis(), null, result);
		return superFreezeMode;
	}

	
	public void setSuperFreezeMode(Boolean data) throws DeviceException {
		execSingleWriteFunction(Fridge.PROPERTY_SUPERFREEZE, data);
		return ;
	}

	
	public BooleanData getEcoMode() throws DeviceException {
		BooleanData ecomode=null;
		Boolean result;
		try {
			result=getCluster().getEcoMode(appliancesProxy.getRequestContext(true));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(),e.getCause());
		}
		
		ecomode=new BooleanData(System.currentTimeMillis(), null, result);
		return ecomode;
	}

	
	public void setEcoMode(Boolean data) throws DeviceException {
		execSingleWriteFunction(Fridge.PROPERTY_ECOMODE, data);
		return ;
	}

	
	public BooleanData getIceParty() throws DeviceException {
		BooleanData iceparty=null;
		Boolean result;
		try {
			result=getCluster().getIceParty(appliancesProxy.getRequestContext(true));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(),e.getCause());
		}
		
		iceparty=new BooleanData(System.currentTimeMillis(), null, result);
		return iceparty;
	}

	
	public void setIceParty(Boolean data) throws DeviceException {
		execSingleWriteFunction(Fridge.PROPERTY_ICEPARTY, data);
		return;
	}

	
	public BooleanData getHolidayMode() throws DeviceException {
		BooleanData holidayMode=null;
		Boolean result;
		try {
			result=getCluster().getHolidayMode(appliancesProxy.getRequestContext(true));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(),e.getCause());
		}
		
		holidayMode=new BooleanData(System.currentTimeMillis(), null, result);
		return holidayMode;
	}

	
	public void setHolidayMode(Boolean data) throws DeviceException {
		execSingleWriteFunction(Fridge.PROPERTY_HOLIDAYMODE, data);
		return ;
	}

}
