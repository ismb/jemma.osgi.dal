package org.energy_home.jemma.osgi.dal.factories;

import java.util.Dictionary;
import java.util.Hashtable;

import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.energy_home.jemma.osgi.dal.ClusterFunctionFactory;
import org.energy_home.jemma.osgi.dal.impl.BooleanControlDALAdapter;
import org.energy_home.jemma.osgi.dal.impl.MeterDALAdapter;
import org.energy_home.jemma.osgi.dal.utils.IDConverters;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.dal.Function;
import org.osgi.service.dal.functions.BooleanControl;
import org.osgi.service.dal.functions.Meter;

public class MeterSimpleMeteringFactory implements ClusterFunctionFactory {

	
	
	@Override
	public boolean isClusterMatching(String clusterName) {
		if("org.energy_home.jemma.ah.cluster.zigbee.metering.SimpleMeteringServer".equals(clusterName))
		{
			return true;
		}
		return false;
	}

	@Override
	public ServiceRegistration createFunctionService(IAppliance appliance, Integer endPointId, IAppliancesProxy appliancesProxy) {
		Dictionary d=new Hashtable();

		d.put(Function.SERVICE_DEVICE_UID, IDConverters.getDeviceUid(appliance.getPid(), appliance.getConfiguration()));
		d.put(Function.SERVICE_UID, IDConverters.getFunctionUid(appliance.getPid(), appliance.getConfiguration(),"Meter"));
		
		d.put(Function.SERVICE_OPERATION_NAMES, new String[0]);
		d.put(Function.SERVICE_PROPERTY_NAMES, new String[]{Meter.PROPERTY_CURRENT,Meter.PROPERTY_TOTAL});
		d.put(Meter.SERVICE_FLOW, Meter.FLOW_IN);
		return FrameworkUtil.getBundle(this.getClass()).getBundleContext().registerService(
				new String[]{Function.class.getName(),Meter.class.getName()}, 
				new MeterDALAdapter(appliance.getPid(), endPointId, appliancesProxy), 
				d);		
	}

}
