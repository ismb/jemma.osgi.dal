package org.energy_home.jemma.osgi.dal.factories;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.energy_home.dal.functions.Fridge;
import org.energy_home.dal.functions.Oven;
import org.energy_home.dal.functions.WashingMachine;
import org.energy_home.dal.functions.type.TimeAttribute;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlServer;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.energy_home.jemma.osgi.dal.ClusterFunctionFactory;
import org.energy_home.jemma.osgi.dal.impl.FridgeDALApplianceControlAdapter;
import org.energy_home.jemma.osgi.dal.impl.OvenDALApplianceControlAdapter;
import org.energy_home.jemma.osgi.dal.impl.TemperatureMeterDALAdapter;
import org.energy_home.jemma.osgi.dal.impl.WashingMachineDALApplianceControlAdapter;
import org.energy_home.jemma.osgi.dal.utils.IDConverters;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.dal.Function;
import org.osgi.service.dal.functions.MultiLevelSensor;

public class WhiteGoodApplianceControlFactory implements ClusterFunctionFactory{

	private static String APPLIANCE_IDENTIFICATION_CLUSTER="org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceIdentificationServer";
	
	Map<String,String> washingMachineAttributesMap;
	Map<String,String> fridgeAttributesMap;
	Map<String,String> ovenAttributesMap;
	
	@Override
	public ServiceRegistration createFunctionService(IAppliance appliance, Integer endPointId,
			IAppliancesProxy appliancesProxy) {

		washingMachineAttributesMap=new HashMap<String, String>();
		fridgeAttributesMap=new HashMap<String, String>();
		ovenAttributesMap=new HashMap<String, String>();
		
		washingMachineAttributesMap.put(ApplianceControlServer.ATTR_TemperatureTarget0_NAME, WashingMachine.PROPERTY_TEMPERATURE);
		washingMachineAttributesMap.put(ApplianceControlServer.ATTR_CycleTarget0_NAME, WashingMachine.PROPERTY_CYCLE);
		washingMachineAttributesMap.put(ApplianceControlServer.ATTR_Spin_NAME, WashingMachine.PROPERTY_SPIN);
		washingMachineAttributesMap.put(ApplianceControlServer.ATTR_StartTime_NAME,WashingMachine.PROPERTY_STARTTIME);
		washingMachineAttributesMap.put(ApplianceControlServer.ATTR_RemainingTime_NAME,WashingMachine.PROPERTY_REMAININGTIME);
		washingMachineAttributesMap.put(ApplianceControlServer.ATTR_FinishTime_NAME,WashingMachine.PROPERTY_FINISHTIME);
		
		fridgeAttributesMap.put(ApplianceControlServer.ATTR_TemperatureTarget0_NAME, Fridge.PROPERTY_FRIDGETEMPERATURE);
		fridgeAttributesMap.put(ApplianceControlServer.ATTR_TemperatureTarget1_NAME, Fridge.PROPERTY_FREEZERTEMPERATURE);
		fridgeAttributesMap.put(ApplianceControlServer.ATTR_EcoMode_NAME,Fridge.PROPERTY_ECOMODE);
		fridgeAttributesMap.put(ApplianceControlServer.ATTR_NormalMode_NAME,Fridge.PROPERTY_NORMALMODE);
		fridgeAttributesMap.put(ApplianceControlServer.ATTR_HolidayMode_NAME,Fridge.PROPERTY_HOLIDAYMODE);
		fridgeAttributesMap.put(ApplianceControlServer.ATTR_IceParty_NAME,Fridge.PROPERTY_ICEPARTY);
		fridgeAttributesMap.put(ApplianceControlServer.ATTR_SuperCoolMode_NAME,Fridge.PROPERTY_SUPERCOOLMODE);
		fridgeAttributesMap.put(ApplianceControlServer.ATTR_SuperFreezeMode_NAME,Fridge.PROPERTY_SUPERFREEZE);
		
		ovenAttributesMap.put(ApplianceControlServer.ATTR_TemperatureTarget0_NAME, Oven.PROPERTY_TEMPERATURE);
		ovenAttributesMap.put(ApplianceControlServer.ATTR_CycleTarget0_NAME, Oven.PROPERTY_CYCLE);
		ovenAttributesMap.put(ApplianceControlServer.ATTR_StartTime_NAME, Oven.PROPERTY_STARTTIME);
		ovenAttributesMap.put(ApplianceControlServer.ATTR_FinishTime_NAME, Oven.PROPERTY_FINISHTIME);
		ovenAttributesMap.put(ApplianceControlServer.ATTR_RemainingTime_NAME, Oven.PROPERTY_REMAININGTIME);
		
		ServiceRegistration reg=null;
		Dictionary d=new Hashtable();
		d.put(Function.SERVICE_DEVICE_UID, IDConverters.getDeviceUid(appliance.getPid(), appliance.getConfiguration()));
		d.put(Function.SERVICE_UID, getFunctionUID(appliance));
		//WARNING: the category depends on Configuration, make sure you use EmptyConfig.xml file is loaded
		switch(getCategory(appliance))
		{
			case 37: //It's a Washing machine
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
						WashingMachine.PROPERTY_REMAININGTIME,
						WashingMachine.PROPERTY_REMOTECONTROL
						});
				
				reg=FrameworkUtil.getBundle(this.getClass()).getBundleContext().registerService(
						new String[]{Function.class.getName(),WashingMachine.class.getName()}, 
						new WashingMachineDALApplianceControlAdapter(appliance.getPid(), endPointId, appliancesProxy), 
						d);	
				break;
			case 38: //It's the oven
				d.put(Function.SERVICE_OPERATION_NAMES, new String[]{
						"execStartCycle",
						"execStopCycle",
						"execOverloadPauseResume",
						"execOverloadPause"
				});
				d.put(Function.SERVICE_PROPERTY_NAMES, new String[]{
						Oven.PROPERTY_CYCLE,
						Oven.PROPERTY_TEMPERATURE,
						Oven.PROPERTY_STARTTIME,
						Oven.PROPERTY_FINISHTIME,
						Oven.PROPERTY_REMAININGTIME,
						Oven.PROPERTY_REMOTECONTROL
						});
				
				reg=FrameworkUtil.getBundle(this.getClass()).getBundleContext().registerService(
						new String[]{Function.class.getName(),Oven.class.getName()}, 
						new OvenDALApplianceControlAdapter(appliance.getPid(), endPointId, appliancesProxy), 
						d);	
				break;
			case 39: //It's the Fridge
				d.put(Function.SERVICE_OPERATION_NAMES, new String[]{});
				d.put(Function.SERVICE_PROPERTY_NAMES, new String[]{
						Fridge.PROPERTY_FRIDGETEMPERATURE,
						Fridge.PROPERTY_FREEZERTEMPERATURE,
						Fridge.PROPERTY_ECOMODE,
						Fridge.PROPERTY_HOLIDAYMODE,
						Fridge.PROPERTY_ICEPARTY,
						Fridge.PROPERTY_SUPERCOOLMODE,
						Fridge.PROPERTY_SUPERFREEZE
						});
				
				reg=FrameworkUtil.getBundle(this.getClass()).getBundleContext().registerService(
						new String[]{Function.class.getName(),Fridge.class.getName()}, 
						new FridgeDALApplianceControlAdapter(appliance.getPid(), endPointId, appliancesProxy), 
						d);	
				break;

			default: 
				//unmanaged appliance
				return null;

		}
		
		return reg;
	}

	@Override
	public String getFunctionUID(IAppliance appliance) {
		
		switch(getCategory(appliance))
		{
			case 37: //Washing machine
				return IDConverters.getFunctionUid(appliance.getPid(),appliance.getConfiguration(), "WashingMachine");
			case 38: //Oven
				return IDConverters.getFunctionUid(appliance.getPid(),appliance.getConfiguration(), "Oven");
			case 39: //Fridge
				return IDConverters.getFunctionUid(appliance.getPid(),appliance.getConfiguration(), "Fridge");
			default: 
				//unmanaged appliance
				return null;
		}
		
	}

	private int getCategory(IAppliance app)
	{
		String category=(String)app.getConfiguration().get("ah.category.pid");
		Integer cat=Integer.parseInt(category);
		return cat;
	}
	
	@Override
	public String getMatchingPropertyName(String attributeName,IAppliance appliance) {
		switch(getCategory(appliance))
		{
			case 37: //Washing machine
				return washingMachineAttributesMap.get(attributeName);
			case 38: //Oven
				return ovenAttributesMap.get(attributeName);
			case 39: //Fridge
				return fridgeAttributesMap.get(attributeName);
			default:
				return null;
		}
		
	}

	@Override
	public String getMatchingCluster() {
		return "org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlServer";
	}
	
}
