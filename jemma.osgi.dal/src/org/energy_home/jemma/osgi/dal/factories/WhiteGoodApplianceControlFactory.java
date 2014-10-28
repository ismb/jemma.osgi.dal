package org.energy_home.jemma.osgi.dal.factories;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.energy_home.dal.functions.WashingMachine;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlServer;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.energy_home.jemma.osgi.dal.ClusterFunctionFactory;
import org.energy_home.jemma.osgi.dal.impl.TemperatureMeterDALAdapter;
import org.energy_home.jemma.osgi.dal.impl.WashingMachineDALApplianceControlAdapter;
import org.energy_home.jemma.osgi.dal.utils.IDConverters;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.dal.Function;
import org.osgi.service.dal.functions.MultiLevelSensor;

public class WhiteGoodApplianceControlFactory implements ClusterFunctionFactory{

	private static String APPLIANCE_IDENTIFICATION_CLUSTER="org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceIdentificationServer";
	
	Map<String,String> attributesMap;
	
	@Override
	public ServiceRegistration createFunctionService(IAppliance appliance, Integer endPointId,
			IAppliancesProxy appliancesProxy) {

		attributesMap=new HashMap<String, String>();
		
		attributesMap.put(ApplianceControlServer.ATTR_TemperatureTarget0_NAME, WashingMachine.PROPERTY_TEMPERATURE);
		attributesMap.put(ApplianceControlServer.ATTR_CycleTarget0_NAME, WashingMachine.PROPERTY_CYCLE);
		attributesMap.put(ApplianceControlServer.ATTR_Spin_NAME, WashingMachine.PROPERTY_SPIN);
		attributesMap.put(ApplianceControlServer.ATTR_StartTime_NAME,WashingMachine.PROPERTY_STARTTIME);
		attributesMap.put(ApplianceControlServer.ATTR_RemainingTime_NAME,WashingMachine.PROPERTY_REMAININGTIME);
		attributesMap.put(ApplianceControlServer.ATTR_FinishTime_NAME,WashingMachine.PROPERTY_FINISHTIME);
		
		String category=(String)appliance.getConfiguration().get("ah.category.pid");
		ServiceRegistration reg=null;
		Dictionary d=new Hashtable();
		d.put(Function.SERVICE_DEVICE_UID, IDConverters.getDeviceUid(appliance.getPid(), appliance.getConfiguration()));
		d.put(Function.SERVICE_UID, getFunctionUID(appliance));
		//WARNING: the category depends on Configuration, make sure you use EmptyConfig.xml file is loaded
		switch(category)
		{
			case "37": //It's a Washing machine
				d.put(Function.SERVICE_OPERATION_NAMES, new String[]{ 
						"execStartCycle",
						"execStopCycle",
						"execPauseCycle",
						"execOverloadPauseResume",
						"execOverloadPause",
						"execOverloadWarning"});
				d.put(Function.SERVICE_PROPERTY_NAMES, new String[]{
						WashingMachine.PROPERTY_CYCLE,
						WashingMachine.PROPERTY_TEMPERATURE,
						WashingMachine.PROPERTY_SPIN,
						WashingMachine.PROPERTY_STARTTIME,
						WashingMachine.PROPERTY_FINISHTIME,
						WashingMachine.PROPERTY_REMAININGTIME
						});
				
				reg=FrameworkUtil.getBundle(this.getClass()).getBundleContext().registerService(
						new String[]{Function.class.getName(),WashingMachine.class.getName()}, 
						new WashingMachineDALApplianceControlAdapter(appliance.getPid(), endPointId, appliancesProxy), 
						d);	
				break;
			case "38": //It's the oven
				break;
			case "39": //It's the Fridge
				break;

			default: 
				//unmanaged appliance
				return null;

		}
		
		return reg;
	}

	@Override
	public String getFunctionUID(IAppliance appliance) {
		String category=(String)appliance.getConfiguration().get("ah.category.pid");
		switch(category)
		{
			case "37": //Washing machine
				return IDConverters.getFunctionUid(appliance.getPid(),appliance.getConfiguration(), "WashingMachine");
			case "38": //Oven
				return IDConverters.getFunctionUid(appliance.getPid(),appliance.getConfiguration(), "Oven");
			case "39": //Fridge
				return IDConverters.getFunctionUid(appliance.getPid(),appliance.getConfiguration(), "Fridge");
			default: 
				//unmanaged appliance
				return null;
		}
		
	}

	@Override
	public String getMatchingPropertyName(String attributeName) {
		// TODO Auto-generated method stub
		return attributesMap.get(attributeName);
	}

	@Override
	public String getMatchingCluster() {
		return "org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlServer";
	}

}
