package org.energy_home.jemma.osgi.dal.impl;

import java.math.BigDecimal;

import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.osgi.service.dal.DeviceException;
import org.osgi.service.dal.OperationMetadata;
import org.osgi.service.dal.PropertyMetadata;
import org.osgi.service.dal.Units;
import org.osgi.service.dal.functions.Meter;
import org.osgi.service.dal.functions.data.LevelData;

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

		int instantaneousDemand;
		try {
			instantaneousDemand=(int)this.appliancesProxy.invokeClusterMethod(appliancePid, endPointId, SIMPLEMETERINGCLUSTER,
					"getIstantaneousDemand", 
					createParams(SIMPLEMETERINGCLUSTER, "getIstantaneousDemand", new String[0]));
			updateDivisor();
			updateMultiplier();
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(),e.getCause());
		}
		BigDecimal result=new BigDecimal(instantaneousDemand);
		//level in KW
		BigDecimal level= result.multiply(new BigDecimal(multiplier)).divide(new BigDecimal(divisor));
		
		//i need the value in Watt
		level=level.multiply(new BigDecimal(1000));
		
		LevelData data=new LevelData(System.currentTimeMillis(), null, Units.WATT, level);
		return data;
	}

	@Override
	public LevelData getTotal() throws UnsupportedOperationException, IllegalStateException, DeviceException {
		long total;
		try {
			total=(long)this.appliancesProxy.invokeClusterMethod(appliancePid, endPointId, SIMPLEMETERINGCLUSTER,
					"getCurrentSummationDelivered", 
					createParams(SIMPLEMETERINGCLUSTER, "getCurrentSummationDelivered", new String[0]));
			updateDivisor();
			updateMultiplier();
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(),e.getCause());
		}
		BigDecimal result=new BigDecimal(total);
		//level in KW*h
		BigDecimal level= result.multiply(new BigDecimal(multiplier)).divide(new BigDecimal(divisor));
		
		//i need the value in Watt*per hour
		level=level.multiply(new BigDecimal(1000));
		LevelData data=new LevelData(System.currentTimeMillis(), null, Units.WATT_PER_HOUR, level);
		return data;
	}

	@Override
	public void resetTotal() throws UnsupportedOperationException, IllegalStateException, DeviceException {
		throw new UnsupportedOperationException("Unsupported operation");
		
	} 

}
