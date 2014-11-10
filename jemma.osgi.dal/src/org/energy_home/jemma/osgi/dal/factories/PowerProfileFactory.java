package org.energy_home.jemma.osgi.dal.factories;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.energy_home.dal.functions.PowerProfileFunction;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.lib.AttributeValue;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.energy_home.jemma.osgi.dal.ClusterFunctionFactory;
import org.energy_home.jemma.osgi.dal.impl.BooleanControlDALAdapter;
import org.energy_home.jemma.osgi.dal.impl.PowerProfileDALAdapter;
import org.energy_home.jemma.osgi.dal.utils.IDConverters;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.dal.Function;
import org.osgi.service.dal.FunctionData;
import org.osgi.service.dal.functions.BooleanControl;
import org.osgi.service.dal.functions.data.BooleanData;

public class PowerProfileFactory implements ClusterFunctionFactory {

	Map<String,String> propertiesMapping;
	
	public PowerProfileFactory()
	{
		propertiesMapping=new HashMap<String, String>();
	}
	
	@Override
	public ServiceRegistration createFunctionService(IAppliance appliance, Integer endPointId, IAppliancesProxy appliancesProxy) {
		Dictionary d=new Hashtable();
		
		d.put(Function.SERVICE_DEVICE_UID, IDConverters.getDeviceUid(appliance.getPid(), appliance.getConfiguration()));
		d.put(Function.SERVICE_UID, getFunctionUID(appliance));
		
		d.put(Function.SERVICE_OPERATION_NAMES, 
				new String[]{
				/*
				BooleanControl.OPERATION_REVERSE,
				BooleanControl.OPERATION_SET_TRUE,
				BooleanControl.OPERATION_SET_FALSE
				*/});
		d.put(Function.SERVICE_PROPERTY_NAMES, new String[]{
				PowerProfileFunction.PROPERTY_ENERGYREMOTE,
				PowerProfileFunction.PROPERTY_MULTIPLESCHEDULING,
				PowerProfileFunction.PROPERTY_SCHEDULEMODE,
				PowerProfileFunction.PROPERTY_TOTALPROFILENUM
				});
		return FrameworkUtil.getBundle(this.getClass()).getBundleContext().registerService(
				new String[]{Function.class.getName(),PowerProfileFunction.class.getName()}, 
				new PowerProfileDALAdapter(appliance.getPid(), endPointId, appliancesProxy), 
				d);		
	}

	@Override
	public String getMatchingCluster() {
		return "org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileServer";
	}

	@Override
	public String getFunctionUID(IAppliance appliance) {
		return IDConverters.getFunctionUid(appliance.getPid(), appliance.getConfiguration(),"PowerProfile");
	}

	@Override
	public String getMatchingPropertyName(String attributeName,IAppliance appliance) {
		return propertiesMapping.get(attributeName);
	}

}
