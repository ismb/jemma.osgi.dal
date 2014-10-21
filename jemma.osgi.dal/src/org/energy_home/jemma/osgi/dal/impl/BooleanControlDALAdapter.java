package org.energy_home.jemma.osgi.dal.impl;

import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.osgi.service.dal.DeviceException;
import org.osgi.service.dal.OperationMetadata;
import org.osgi.service.dal.PropertyMetadata;
import org.osgi.service.dal.functions.BooleanControl;
import org.osgi.service.dal.functions.data.BooleanData;

/**
 * DAL function implementation for ZigBee OnOffServer
 * @author Ivan Grimaldi (grimaldi@ismb.it)
 *
 */
public class BooleanControlDALAdapter extends BaseDALAdapter implements BooleanControl{

	private static String ONOFFCLUSTER="org.energy_home.jemma.ah.cluster.zigbee.general.OnOffServer";
	
	public BooleanControlDALAdapter(String appliancePid,Integer endPointId,IAppliancesProxy appliancesProxy)
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
	public BooleanData getData() {
		Boolean data=null;
		try {
			data=(Boolean)this.appliancesProxy.invokeClusterMethod(appliancePid, endPointId, ONOFFCLUSTER, "getOnOff", 
						createParams(ONOFFCLUSTER, "getOnOff", new String[0]));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(data==null)
			return null;
		return new BooleanData(System.currentTimeMillis(),null,data);
	}

	@Override
	public void setData(boolean data) throws UnsupportedOperationException, IllegalStateException, DeviceException,
			IllegalArgumentException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reverse() throws UnsupportedOperationException, IllegalStateException, DeviceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTrue() throws UnsupportedOperationException, IllegalStateException, DeviceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFalse() throws UnsupportedOperationException, IllegalStateException, DeviceException {
		// TODO Auto-generated method stub
		
	}

	
}
