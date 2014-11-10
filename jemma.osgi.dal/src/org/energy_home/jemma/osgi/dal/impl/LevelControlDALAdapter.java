package org.energy_home.jemma.osgi.dal.impl;

import java.math.BigDecimal;

import org.energy_home.jemma.ah.cluster.zigbee.general.LevelControlServer;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.osgi.service.dal.DeviceException;
import org.osgi.service.dal.FunctionData;
import org.osgi.service.dal.OperationMetadata;
import org.osgi.service.dal.PropertyMetadata;
import org.osgi.service.dal.functions.MultiLevelControl;
import org.osgi.service.dal.functions.data.LevelData;

public class LevelControlDALAdapter extends BaseDALAdapter implements MultiLevelControl {

	private static final String LEVELCONTROLCLUSTER = "org.energy_home.jemma.ah.cluster.zigbee.general.LevelControlServer";

	public LevelControlDALAdapter(String appliancePid, Integer endPointId, IAppliancesProxy appliancesProxy) {
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

	
	private LevelControlServer getCluster()
	{
		return (LevelControlServer) appliancesProxy.getAppliance(appliancePid).getEndPoint(endPointId).getServiceCluster(LEVELCONTROLCLUSTER);
	}

	@Override
	public FunctionData getMatchingPropertyValue(String attributeName, IAttributeValue attributeValue) {
		FunctionData result=null;
		if(attributeName.equals("CurrentLevel"))
		{
			result=new LevelData(System.currentTimeMillis(), null, "", new BigDecimal((Short)(attributeValue.getValue())));
		}
		return result;
	}

	@Override
	public void updateApplianceSubscriptions() {
		// TODO Auto-generated method stub
		
	}
	

	@Override
	public LevelData getData() throws UnsupportedOperationException, IllegalStateException, DeviceException {
		LevelData data=null;
		try{
			Short level=getCluster().getCurrentLevel(appliancesProxy.getRequestContext(true));
			data=new LevelData(System.currentTimeMillis(), null, "", new BigDecimal(level));
		}catch(Exception e)
		{
			throw new DeviceException(e.getMessage(),e.getCause());
		}
		return data;
	}

	@Override
	public void setData(BigDecimal level) throws UnsupportedOperationException, IllegalStateException, DeviceException,
			IllegalArgumentException {
		try{
			getCluster().execMoveToLevel(level.shortValue(), 10, appliancesProxy.getRequestContext(true));
		}catch(Exception e)
		{
			throw new DeviceException(e.getMessage(),e.getCause());
		}
	}

	@Override
	public void setData(BigDecimal level, String unit) throws UnsupportedOperationException, IllegalStateException,
			DeviceException, IllegalArgumentException {
		throw new UnsupportedOperationException();
	}

}
