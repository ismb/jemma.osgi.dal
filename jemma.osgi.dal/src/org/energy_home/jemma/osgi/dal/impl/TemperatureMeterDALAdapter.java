package org.energy_home.jemma.osgi.dal.impl;

import java.math.BigDecimal;

import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.lib.AttributeValue;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.osgi.service.dal.DeviceException;
import org.osgi.service.dal.FunctionData;
import org.osgi.service.dal.OperationMetadata;
import org.osgi.service.dal.PropertyMetadata;
import org.osgi.service.dal.Units;
import org.osgi.service.dal.functions.Meter;
import org.osgi.service.dal.functions.MultiLevelSensor;
import org.osgi.service.dal.functions.data.LevelData;

import sun.util.LocaleServiceProviderPool.LocalizedObjectGetter;

public class TemperatureMeterDALAdapter extends BaseDALAdapter implements MultiLevelSensor{

	private static String THERMOSTATCLUSTER="org.energy_home.jemma.ah.cluster.zigbee.hvac.ThermostatServer";
	
	public TemperatureMeterDALAdapter(String appliancePid, Integer endPointId, IAppliancesProxy appliancesProxy) {
		super(appliancePid, endPointId, appliancesProxy);
	}

	@Override
	public FunctionData getMatchingPropertyValue(String attributeName, IAttributeValue attributeValue) {
		LevelData levelData=null;
		
		if("LocalTemperature".equals(attributeName))
		{
			int value=(int)(attributeValue.getValue());
			//divide by 100
			levelData=new LevelData(attributeValue.getTimestamp(), null, Units.DEGREE_CELSIUS, new BigDecimal(value).divide(new BigDecimal(100)));
		}
		return levelData;
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
	public LevelData getData() throws UnsupportedOperationException, IllegalStateException, DeviceException {
		BigDecimal result=null;
		int value;
		try {
			value=(int)this.appliancesProxy.invokeClusterMethod(appliancePid, endPointId, THERMOSTATCLUSTER,
					"getLocalTemperature", 
					createParams(THERMOSTATCLUSTER, "getLocalTemperature", new String[0]));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(),e.getCause());
		}
		result=new BigDecimal(value).divide(new BigDecimal(100));
		
		LevelData data=new LevelData(System.currentTimeMillis(), null, Units.DEGREE_CELSIUS, result);
		return data;
		
	} 

}
