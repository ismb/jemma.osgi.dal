package org.energy_home.jemma.osgi.dal.impl;

import java.util.Map;

import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.lib.AttributeValue;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.energy_home.jemma.osgi.dal.ClusterFunctionFactory;
import org.osgi.service.dal.DeviceException;
import org.osgi.service.dal.FunctionData;
import org.osgi.service.dal.OperationMetadata;
import org.osgi.service.dal.PropertyMetadata;
import org.osgi.service.dal.functions.BooleanControl;
import org.osgi.service.dal.functions.data.BooleanData;

/**
 * DAL function implementation for ZigBee OnOffServer
 * 
 * @author Ivan Grimaldi (grimaldi@ismb.it)
 *
 */
public class BooleanControlDALAdapter extends BaseDALAdapter implements BooleanControl {

	private static String ONOFFCLUSTER = "org.energy_home.jemma.ah.cluster.zigbee.general.OnOffServer";

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
	public BooleanData getData() throws DeviceException {
		Boolean data = null;
		try {
			data = (Boolean) this.appliancesProxy.invokeClusterMethod(appliancePid, endPointId, ONOFFCLUSTER,
					"getOnOff", createParams(ONOFFCLUSTER, "getOnOff", new String[0]));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(), e.getCause());
		}

		return new BooleanData(System.currentTimeMillis(), null, data);
	}

	@Override
	public void setData(boolean data) throws UnsupportedOperationException, IllegalStateException, DeviceException,
			IllegalArgumentException {
		throw new UnsupportedOperationException("Unimplemented method");
	}

	@Override
	public void reverse() throws UnsupportedOperationException, IllegalStateException, DeviceException {
		try {
			this.appliancesProxy.invokeClusterMethod(appliancePid, endPointId, ONOFFCLUSTER, "execToggle",
					createParams(ONOFFCLUSTER, "execToggle", new String[0]));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(), e.getCause());
		}

	}

	@Override
	public void setTrue() throws UnsupportedOperationException, IllegalStateException, DeviceException {
		try {
			this.appliancesProxy.invokeClusterMethod(appliancePid, endPointId, ONOFFCLUSTER, "execOn",
					createParams(ONOFFCLUSTER, "execOn", new String[0]));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(), e.getCause());
		}

	}

	@Override
	public void setFalse() throws UnsupportedOperationException, IllegalStateException, DeviceException {
		try {
			this.appliancesProxy.invokeClusterMethod(appliancePid, endPointId, ONOFFCLUSTER, "execOff",
					createParams(ONOFFCLUSTER, "execOff", new String[0]));
		} catch (Exception e) {
			throw new DeviceException(e.getMessage(), e.getCause());
		}

	}

	@Override
	public FunctionData getMatchingPropertyValue(String attributeName, IAttributeValue value) {
		return new BooleanData(value.getTimestamp(), null, (boolean) value.getValue());
	}

}
