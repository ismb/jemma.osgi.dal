package org.energy_home.jemma.osgi.dal.factories;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.energy_home.dal.functions.ColorControl;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.energy_home.jemma.osgi.dal.ClusterFunctionFactory;
import org.energy_home.jemma.osgi.dal.impl.BooleanControlDALAdapter;
import org.energy_home.jemma.osgi.dal.impl.ColorControlDALAdapter;
import org.energy_home.jemma.osgi.dal.utils.IDConverters;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.dal.Function;
import org.osgi.service.dal.functions.BooleanControl;

public class ColorControlFactory implements ClusterFunctionFactory {

	Map<String,String> propertiesMapping;
	
	public ColorControlFactory()
	{
		propertiesMapping=new HashMap<String, String>();
	}
	
	@Override
	public ServiceRegistration createFunctionService(IAppliance appliance, Integer endPointId, IAppliancesProxy appliancesProxy) {
		Dictionary d=new Hashtable();
		
		d.put(Function.SERVICE_DEVICE_UID, IDConverters.getDeviceUid(appliance.getPid(), appliance.getConfiguration()));
		d.put(Function.SERVICE_UID, getFunctionUID(appliance));
		
		d.put(Function.SERVICE_OPERATION_NAMES, new String[]{
				ColorControl.OPERATION_SETCOLOR});
		d.put(Function.SERVICE_PROPERTY_NAMES, new String[]{});
		return FrameworkUtil.getBundle(this.getClass()).getBundleContext().registerService(
				new String[]{Function.class.getName(),ColorControl.class.getName()}, 
				new ColorControlDALAdapter(appliance.getPid(), endPointId, appliancesProxy), 
				d);		
	}

	@Override
	public String getMatchingCluster() {
		return "org.energy_home.jemma.ah.cluster.zigbee.zll.ColorControlServer";
	}

	@Override
	public String getFunctionUID(IAppliance appliance) {
		return IDConverters.getFunctionUid(appliance.getPid(), appliance.getConfiguration(),"ColorControl");
	}

	@Override
	public String getMatchingPropertyName(String attributeName) {
		return propertiesMapping.get(attributeName);
	}

}
