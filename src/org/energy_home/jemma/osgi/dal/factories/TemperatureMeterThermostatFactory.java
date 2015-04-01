package org.energy_home.jemma.osgi.dal.factories;

import java.net.URLDecoder;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.lib.AttributeValue;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.energy_home.jemma.osgi.dal.ClusterFunctionFactory;
import org.energy_home.jemma.osgi.dal.impl.EnergyMeterDALAdapter;
import org.energy_home.jemma.osgi.dal.impl.TemperatureMeterDALAdapter;
import org.energy_home.jemma.osgi.dal.utils.IDConverters;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.dal.Function;
import org.osgi.service.dal.FunctionData;
import org.osgi.service.dal.functions.Meter;
import org.osgi.service.dal.functions.MultiLevelSensor;

public class TemperatureMeterThermostatFactory implements ClusterFunctionFactory {

	Map<String,String> propertiesMapping;
	
	public TemperatureMeterThermostatFactory()
	{
		propertiesMapping=new HashMap<String,String>();
		propertiesMapping.put("LocalTemperature", MultiLevelSensor.PROPERTY_DATA);
	}
	
	public ServiceRegistration createFunctionService(IAppliance appliance, Integer endPointId, IAppliancesProxy appliancesProxy) {
		Dictionary d=new Hashtable();

		d.put(Function.SERVICE_DEVICE_UID, IDConverters.getDeviceUid(appliance.getPid(), appliance.getConfiguration()));
		d.put(Function.SERVICE_UID, getFunctionUID(appliance));

		d.put(Function.SERVICE_OPERATION_NAMES, new String[0]);
		d.put(Function.SERVICE_PROPERTY_NAMES, new String[]{MultiLevelSensor.PROPERTY_DATA});
		return FrameworkUtil.getBundle(this.getClass()).getBundleContext().registerService(
				new String[]{Function.class.getName(),MultiLevelSensor.class.getName()}, 
				new TemperatureMeterDALAdapter(appliance.getPid(), endPointId, appliancesProxy), 
				d);		
	}

	public String getMatchingCluster() {
		return "org.energy_home.jemma.ah.cluster.zigbee.hvac.ThermostatServer";
	}

	public String getFunctionUID(IAppliance appliance) {
		return IDConverters.getFunctionUid(appliance.getPid(), appliance.getConfiguration(),"Temperature");
	}

	public String getMatchingPropertyName(String attributeName,IAppliance appliance) {
		return propertiesMapping.get(attributeName);
	}

}
