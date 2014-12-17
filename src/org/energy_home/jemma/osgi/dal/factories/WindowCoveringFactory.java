package org.energy_home.jemma.osgi.dal.factories;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.energy_home.dal.functions.DoorLock;
import org.energy_home.dal.functions.WindowCovering;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.energy_home.jemma.osgi.dal.ClusterFunctionFactory;
import org.energy_home.jemma.osgi.dal.impl.DoorLockDALAdapter;
import org.energy_home.jemma.osgi.dal.impl.WindowCoveringDALAdapter;
import org.energy_home.jemma.osgi.dal.utils.IDConverters;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.dal.Function;

public class WindowCoveringFactory implements ClusterFunctionFactory {

	Map<String,String> propertiesMapping;
	
	public WindowCoveringFactory()
	{
		propertiesMapping=new HashMap<String, String>();
		propertiesMapping.put("CurrentPositionLiftPercentage",WindowCovering.PROPERTY_STATUS);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public ServiceRegistration createFunctionService(IAppliance appliance, Integer endPointId, IAppliancesProxy appliancesProxy) {
		Dictionary d=new Hashtable();
		
		d.put(Function.SERVICE_DEVICE_UID, IDConverters.getDeviceUid(appliance.getPid(), appliance.getConfiguration()));
		d.put(Function.SERVICE_UID, getFunctionUID(appliance));
		
		d.put(Function.SERVICE_OPERATION_NAMES, new String[]{
				WindowCovering.OPERATION_OPEN,
				WindowCovering.OPERATION_CLOSE});
		d.put(Function.SERVICE_PROPERTY_NAMES, new String[]{WindowCovering.PROPERTY_STATUS});
		return FrameworkUtil.getBundle(this.getClass()).getBundleContext().registerService(
				new String[]{Function.class.getName(),WindowCovering.class.getName()}, 
				new WindowCoveringDALAdapter(appliance.getPid(), endPointId, appliancesProxy), 
				d);		
	}

	@Override
	public String getMatchingCluster() {
		return "org.energy_home.jemma.ah.cluster.zigbee.closures.WindowCoveringServer";
	}

	@Override
	public String getFunctionUID(IAppliance appliance) {
		return IDConverters.getFunctionUid(appliance.getPid(), appliance.getConfiguration(),"WindowCovering");
	}

	@Override
	public String getMatchingPropertyName(String attributeName,IAppliance appliance) {
		return propertiesMapping.get(attributeName);
	}

}
