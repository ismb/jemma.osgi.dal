package org.energy_home.jemma.osgi.dal.impl;

import java.util.List;

import org.energy_home.dal.functions.ColorControl;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlServer;
import org.energy_home.jemma.ah.cluster.zigbee.zll.ColorControlServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.osgi.service.dal.DeviceException;
import org.osgi.service.dal.FunctionData;
import org.osgi.service.dal.OperationMetadata;
import org.osgi.service.dal.PropertyMetadata;

public class ColorControlDALAdapter extends BaseDALAdapter implements ColorControl {

	private static final String COLORCONTROLCLUSTER = "org.energy_home.jemma.ah.cluster.zigbee.zll.ColorControlServer";

	public ColorControlDALAdapter(String appliancePid, Integer endPointId, IAppliancesProxy appliancesProxy) {
		super(appliancePid, endPointId, appliancesProxy);
	}

	
	public PropertyMetadata getPropertyMetadata(String propertyName) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public OperationMetadata getOperationMetadata(String operationName) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Object getServiceProperty(String propName) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setHS(Short hue,Short saturation) throws DeviceException
	{
		try {
			getCluster().execMoveToHueAndSaturation(hue, saturation, 10, appliancesProxy.getRequestContext(true));
			//getCluster().execStepColor((int)(xy[0]*254), (int)(xy[1]*254), 10, appliancesProxy.getRequestContext(true));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(),e.getCause());
		}
	}
	
	public Short[] getHS() throws DeviceException
	{
		try
		{
			Short hue=getCluster().getCurrentHue(appliancesProxy.getRequestContext(true));
			Short sat=getCluster().getCurrentSaturation(appliancesProxy.getRequestContext(true));
			return new Short[]{hue,sat};
		}catch(Exception e)
		{
			throw new DeviceException(e.getMessage(),e.getCause());
		}
	}
	
	private ColorControlServer getCluster()
	{
		return (ColorControlServer) appliancesProxy.getAppliance(appliancePid).getEndPoint(endPointId).getServiceCluster(COLORCONTROLCLUSTER);
	}

	
	public FunctionData getMatchingPropertyValue(String attributeName, IAttributeValue attributeValue) {
		// TODO Auto-generated method stub
		return null;
	}

}
