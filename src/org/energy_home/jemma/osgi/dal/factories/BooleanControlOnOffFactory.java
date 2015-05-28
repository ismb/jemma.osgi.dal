package org.energy_home.jemma.osgi.dal.factories;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.lib.AttributeValue;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.energy_home.jemma.osgi.dal.ClusterFunctionFactory;
import org.energy_home.jemma.osgi.dal.impl.BooleanControlDALAdapter;
import org.energy_home.jemma.osgi.dal.utils.IDConverters;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.dal.Function;
import org.osgi.service.dal.FunctionData;
import org.osgi.service.dal.functions.BooleanControl;
import org.osgi.service.dal.functions.data.BooleanData;

public class BooleanControlOnOffFactory implements ClusterFunctionFactory {

	Map<String,String> propertiesMapping;
	
	public BooleanControlOnOffFactory()
	{
		propertiesMapping=new HashMap<String, String>();
		propertiesMapping.put("OnOff",BooleanControl.PROPERTY_DATA);
	}
	
	
	public ServiceRegistration createFunctionService(IAppliance appliance, Integer endPointId, IAppliancesProxy appliancesProxy) {
		Dictionary d=new Hashtable();
		
		d.put(Function.SERVICE_DEVICE_UID, IDConverters.getDeviceUid(appliance.getPid(), appliance.getConfiguration()));
		d.put(Function.SERVICE_UID, getFunctionUID(appliance));
		
		d.put(Function.SERVICE_OPERATION_NAMES, new String[]{
				BooleanControl.OPERATION_REVERSE,
				BooleanControl.OPERATION_SET_TRUE,
				BooleanControl.OPERATION_SET_FALSE});
		d.put(Function.SERVICE_PROPERTY_NAMES, new String[]{BooleanControl.PROPERTY_DATA});
		return FrameworkUtil.getBundle(this.getClass()).getBundleContext().registerService(
				new String[]{Function.class.getName(),BooleanControl.class.getName()}, 
				new BooleanControlDALAdapter(appliance.getPid(), endPointId, appliancesProxy), 
				d);		
	}

	
	public String getMatchingCluster() {
		return "org.energy_home.jemma.ah.cluster.zigbee.general.OnOffServer";
	}

	
	public String getFunctionUID(IAppliance appliance) {
		return IDConverters.getFunctionUid(appliance.getPid(), appliance.getConfiguration(),"OnOff");
	}

	
	public String getMatchingPropertyName(String attributeName,IAppliance appliance) {
		return propertiesMapping.get(attributeName);
	}

}
