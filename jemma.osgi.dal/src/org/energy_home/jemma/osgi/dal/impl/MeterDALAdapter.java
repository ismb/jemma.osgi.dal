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
import org.osgi.service.dal.functions.data.LevelData;

import sun.util.LocaleServiceProviderPool.LocalizedObjectGetter;

public class MeterDALAdapter extends BaseDALAdapter implements Meter{

	private Integer divisor=null;
	private Integer multiplier=null;
	
	private static String SIMPLEMETERINGCLUSTER="org.energy_home.jemma.ah.cluster.zigbee.metering.SimpleMeteringServer";
	
	public MeterDALAdapter(String appliancePid, Integer endPointId, IAppliancesProxy appliancesProxy) {
		super(appliancePid, endPointId, appliancesProxy);
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

	public Integer getDivisor() throws Exception {
		updateDivisor();
		return divisor;
	}

	public Integer getMultiplier() throws Exception {
		updateMultiplier();
		return multiplier;
	}
	
	private void updateDivisor() throws Exception
	{
		if(divisor==null)
		{
			divisor=(Integer)this.appliancesProxy.invokeClusterMethod(appliancePid,
				endPointId, 
				SIMPLEMETERINGCLUSTER, "getDivisor", 
				createParams(SIMPLEMETERINGCLUSTER, "getDivisor", new String[0]));
		}
	}
	
	private void updateMultiplier() throws Exception
	{
		if(multiplier==null)
		{
			multiplier=(Integer)this.appliancesProxy.invokeClusterMethod(appliancePid,
				endPointId,
				SIMPLEMETERINGCLUSTER, 
				"getMultiplier", 
				createParams(SIMPLEMETERINGCLUSTER, "gedMultiplier", new String[0]));
		}
	}
	
	@Override
	public LevelData getCurrent() throws UnsupportedOperationException, IllegalStateException, DeviceException {
		BigDecimal result=null;
		int instantaneousDemand;
		try {
			instantaneousDemand=(int)this.appliancesProxy.invokeClusterMethod(appliancePid, endPointId, SIMPLEMETERINGCLUSTER,
					"getIstantaneousDemand", 
					createParams(SIMPLEMETERINGCLUSTER, "getIstantaneousDemand", new String[0]));
			result=this.scaleValues(new BigDecimal(instantaneousDemand));
			
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(),e.getCause());
		}
		
		
		LevelData data=new LevelData(System.currentTimeMillis(), null, Units.WATT, result);
		return data;
	}

	@Override
	public LevelData getTotal() throws UnsupportedOperationException, IllegalStateException, DeviceException {
		BigDecimal result=null;
		long total;
		try {
			total=(long)this.appliancesProxy.invokeClusterMethod(appliancePid, endPointId, SIMPLEMETERINGCLUSTER,
					"getCurrentSummationDelivered", 
					createParams(SIMPLEMETERINGCLUSTER, "getCurrentSummationDelivered", new String[0]));
			result=this.scaleValues(new BigDecimal(total));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(),e.getCause());
		}
		
		
		LevelData data=new LevelData(System.currentTimeMillis(), null, Units.WATT_PER_HOUR, result);
		return data;
	}
	
	private BigDecimal scaleValues(BigDecimal value) throws Exception
	{
		BigDecimal factor=new BigDecimal(getMultiplier())
			.divide(new BigDecimal(getDivisor())) //now I have the factor for conversion in KW*h
			//I need the value in Watt (or Watt*Hour)
			.multiply(new BigDecimal(1000));
		
		BigDecimal level=value.multiply(factor);
		return level;
	}

	@Override
	public void resetTotal() throws UnsupportedOperationException, IllegalStateException, DeviceException {
		throw new UnsupportedOperationException("Unsupported operation");
		
	}

	@Override
	public FunctionData getMatchingPropertyValue(String attributeName, IAttributeValue attributeValue) {
		LevelData levelData=null;
		try {
		
			if(attributeName=="CurrentSummationDelivered")
			{
				long value=(long)(attributeValue.getValue());
				
					levelData=new LevelData(attributeValue.getTimestamp(), null, Units.WATT_PER_HOUR, scaleValues(new BigDecimal(value)));
			}else if(attributeName=="IstantaneousDemand")
			{
				int value=(int)(attributeValue.getValue());
				levelData=new LevelData(attributeValue.getTimestamp(), null, Units.WATT, scaleValues(new BigDecimal(value)));
			}
		} catch (Exception e) {
			
		}
		return levelData;
	} 

}
