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
import org.energy_home.jemma.osgi.dal.utils.IDConverters;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.dal.Function;
import org.osgi.service.dal.FunctionData;
import org.osgi.service.dal.functions.Meter;

public class EnergyMeterSimpleMeteringFactory implements ClusterFunctionFactory {

	Map<String,String> propertiesMapping;
	
	public EnergyMeterSimpleMeteringFactory()
	{
		propertiesMapping=new HashMap<String,String>();
		//for consumption meter
		propertiesMapping.put("CurrentSummationDelivered", Meter.PROPERTY_TOTAL);
		//for production meter
		propertiesMapping.put("CurrentSummationReceived", Meter.PROPERTY_TOTAL);
		propertiesMapping.put("IstantaneousDemand", Meter.PROPERTY_CURRENT);
	}
	
	public ServiceRegistration createFunctionService(IAppliance appliance, Integer endPointId, IAppliancesProxy appliancesProxy) {
		Dictionary d=new Hashtable();

		d.put(Function.SERVICE_DEVICE_UID, IDConverters.getDeviceUid(appliance.getPid(), appliance.getConfiguration()));
		d.put(Function.SERVICE_UID, getFunctionUID(appliance));
		
		d.put(Function.SERVICE_OPERATION_NAMES, new String[0]);
		d.put(Function.SERVICE_PROPERTY_NAMES, new String[]{Meter.PROPERTY_CURRENT,Meter.PROPERTY_TOTAL});
		
		switch(getCategory(appliance))
		{
			case 14: //it's a PV meter
				d.put(Meter.SERVICE_FLOW, Meter.FLOW_OUT);
				break;
			default:
				d.put(Meter.SERVICE_FLOW, Meter.FLOW_IN);
		
		}
		
		
		EnergyMeterDALAdapter adapter=new EnergyMeterDALAdapter(appliance.getPid(), endPointId, appliancesProxy);
		adapter.setServiceProperties(d);
		
		return FrameworkUtil.getBundle(this.getClass()).getBundleContext().registerService(
				new String[]{Function.class.getName(),Meter.class.getName()}, 
				adapter, 
				d);		
	}

	private int getCategory(IAppliance app)
	{
		String category=(String)app.getConfiguration().get("ah.category.pid");
		Integer cat=Integer.parseInt(category);
		return cat;
	}
	
	public String getMatchingCluster() {
		return "org.energy_home.jemma.ah.cluster.zigbee.metering.SimpleMeteringServer";
	}

	public String getFunctionUID(IAppliance appliance) {
		return IDConverters.getFunctionUid(appliance.getPid(), appliance.getConfiguration(),"EnergyMeter");
	}

	public String getMatchingPropertyName(String attributeName,IAppliance appliance) {
		return propertiesMapping.get(attributeName);
	}

}
