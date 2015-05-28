package org.energy_home.jemma.osgi.dal;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlClient;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.HacException;
import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IApplianceDescriptor;
import org.energy_home.jemma.ah.hac.IApplicationEndPoint;
import org.energy_home.jemma.ah.hac.IApplicationService;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.IAttributeValuesListener;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IEndPointRequestContext;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.lib.Appliance;
import org.energy_home.jemma.ah.hac.lib.ApplianceDescriptor;
import org.energy_home.jemma.ah.hac.lib.EndPoint;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.energy_home.jemma.ah.hac.lib.ext.IConnectionAdminService;
import org.energy_home.jemma.osgi.dal.factories.BooleanControlOnOffFactory;
import org.energy_home.jemma.osgi.dal.factories.ColorControlFactory;
import org.energy_home.jemma.osgi.dal.factories.DoorLockFactory;
import org.energy_home.jemma.osgi.dal.factories.EnergyMeterSimpleMeteringFactory;
import org.energy_home.jemma.osgi.dal.factories.LevelControlFactory;
import org.energy_home.jemma.osgi.dal.factories.PowerProfileFactory;
import org.energy_home.jemma.osgi.dal.factories.TemperatureMeterThermostatFactory;
import org.energy_home.jemma.osgi.dal.factories.WhiteGoodApplianceControlFactory;
import org.energy_home.jemma.osgi.dal.factories.WindowCoveringFactory;
import org.energy_home.jemma.osgi.dal.utils.IDConverters;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.dal.Device;
import org.osgi.service.dal.Function;
import org.osgi.service.dal.FunctionData;
import org.osgi.service.dal.FunctionEvent;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.util.logging.resources.logging;

/**
 * This class is the main interface between Device Abstraction Layer and JEMMA.
 * It registers itself as OSGi Declarative Service as:
 * <ul>
 * 	<li>IApplicationService to get notifications from JEMMA of configured or deleted appliances </li>
 * 	<li>IAttributeValueListener to receive Attributes reportings from a device and make them become OSGi DAL events</li>
 *  <li>IManagedAppliance to be a virtual appliance exposing Client Clusters which are needed to receive commands from appliances</li>
 * </ul>
 * 
 * @author Ivan Grimaldi (ivan.grimaldi@telecomitalia.it)
 *
 */
public class ZigBeeDalAdapter extends Appliance implements IApplicationService,IAttributeValuesListener,ApplianceControlClient{

	private IAppliancesProxy appliancesProxy;
	private EventAdmin eventAdmin;
	
	private static final Logger LOG = LoggerFactory.getLogger( ZigBeeDalAdapter.class );
	
	private ComponentContext componentContext;
	
	private ConcurrentHashMap<String,List<ServiceRegistration>> functions;
	private ConcurrentHashMap<String,ServiceRegistration> devices;
	
	private Map<String,ClusterFunctionFactory> factories;
	
	//Parameters to configure this class as appliance with exposed endpoints. Usefult to get notification from Server Clusters
	private static Dictionary applianceConfiguration=new Hashtable();
	protected static final String FRIENDLY_NAME = "DAL_Appliance";
	protected static final String TYPE = "org.energy_home.jemma.ah.appliance.dal";
	protected static final String PID="ah.app.dal.notification.receiver";
	protected static final String END_POINT_TYPE = "org.energy_home.jemma.ah.appliance.dal.ep";
	
	private static ApplianceDescriptor descriptor;
	protected static final String DEVICE_TYPE = null;
	private EndPoint dalNotificationReceiverEndPoint;
	
	
	//I need connection admin service to perform bind on appliances
	private IConnectionAdminService connectionAdmin;
	
	
	static{
		applianceConfiguration.put(IAppliance.APPLIANCE_NAME_PROPERTY, FRIENDLY_NAME);
		descriptor = new ApplianceDescriptor(TYPE, DEVICE_TYPE, FRIENDLY_NAME);
	}
	
	public ZigBeeDalAdapter() throws ApplianceException{
		
		//prepare virtual appliance that will receive notifications commands
		super(PID,applianceConfiguration);
		dalNotificationReceiverEndPoint=addEndPoint(new EndPoint(END_POINT_TYPE));
		dalNotificationReceiverEndPoint.registerCluster(ApplianceControlClient.class.getName(), this);	
		setAvailability(true);
	
		
		//TODO: evaluate dynamic discovery of factories using Declarative services
		//add factories
		factories=new HashMap<String,ClusterFunctionFactory>();
		addClusterFunctionFactory(new BooleanControlOnOffFactory());
		addClusterFunctionFactory(new EnergyMeterSimpleMeteringFactory());
		addClusterFunctionFactory(new TemperatureMeterThermostatFactory());
		addClusterFunctionFactory(new WhiteGoodApplianceControlFactory());
		addClusterFunctionFactory(new ColorControlFactory());
		addClusterFunctionFactory(new DoorLockFactory());
		addClusterFunctionFactory(new WindowCoveringFactory());
		addClusterFunctionFactory(new PowerProfileFactory());
		addClusterFunctionFactory(new LevelControlFactory());
		
		functions=new ConcurrentHashMap<String,List<ServiceRegistration>>();
		devices=new ConcurrentHashMap<String,ServiceRegistration>();
	}
	
	
	public void activate(ComponentContext context)
	{
		this.componentContext=context;
		//try to bind this appliance with all appliances
		//required to get command must be received by client clusters
		try {
			this.connectionAdmin.addBindRule("(&(pid1="+PID+")(pid2=*))");
		} catch (InvalidSyntaxException e) {
			LOG.error("ERROR binding dal notification listener appliance");
		}
	}
	
	private void addClusterFunctionFactory(ClusterFunctionFactory factory)
	{
		this.factories.put(factory.getMatchingCluster(), factory);
	}
	
	public IServiceCluster[] getServiceClusters() {
		// nothing to be done
		return null;
	}

	/**
	 * Method called by JEMMA when a new appliance is configured in the framework
	 * @param endPoint
	 * @param appliance
	 */
	public void notifyApplianceAdded(IApplicationEndPoint endPoint, IAppliance appliance) {
		if(!appliance.isDriver())
		{
			//It's a virtual appliance: ignore!
			return;
		}
		IApplianceDescriptor desc=appliance.getDescriptor();
		LOG.info("###### APPLIANCE Descriptor:"+desc.getDeviceType()+"-"+desc.getType()+"-"+desc.getFriendlyName());
		LOG.info("###### APPLIANCE INFO:"+appliance.getPid()+" "+appliance.toString());
		LOG.info("###### APPPLIANCE CONFIGURATION");
		for(Enumeration<Object> keys=appliance.getConfiguration().keys();keys.hasMoreElements();)		{
			Object key=keys.nextElement();
			Object value=appliance.getConfiguration().get(key);
			LOG.info("\t"+key+": "+value);
		}
		LOG.info("###### END APPLIANCE CONFIGURATION");
		//Register OSGi DAL services
		for(IEndPoint ep:appliance.getEndPoints())
		{
			for(IServiceCluster cluster:ep.getServiceClusters())
			{
				if(factories.containsKey(cluster.getName()))
				{
					//register function service
					addFunctionRegistration(appliance.getPid(),factories.get(cluster.getName()).createFunctionService(appliance,
							ep.getId(), appliancesProxy));
					
				}
			}
		}

		//register device service
		Dictionary d=new Hashtable();
		
		d.put(Device.SERVICE_DRIVER, "ZigBee");
		d.put(Device.SERVICE_UID, IDConverters.getDeviceUid(appliance.getPid(), appliance.getConfiguration()));
		//the service status must be initially set STATUS_PROCESSING
		d.put(Device.SERVICE_STATUS, Device.STATUS_PROCESSING);
		d.put(Device.SERVICE_NAME,IDConverters.getDeviceName(appliance.getPid(), appliance.getConfiguration()));
		
		//added category from JEMMA in device's properties
		String category=(String)appliance.getConfiguration().get("ah.category.pid");
		d.put("ah.category.pid", category);
		devices.put(appliance.getPid(),FrameworkUtil.getBundle(this.getClass()).getBundleContext().registerService(
				Device.class.getName(),
				new JemmaDevice(),
				d));
		
		//update device properties according to appliance status
		updateDeviceServiceProperties(appliance);
		
	}

	/**
	 * Internal method storing service registrations for future use (e.g. service de-registration when appliance is removed)
	 * @param appliancePid
	 * @param registration
	 */
	private void addFunctionRegistration(String appliancePid,ServiceRegistration registration) {
		if(!functions.containsKey(appliancePid))
		{
			functions.put(appliancePid, new LinkedList<ServiceRegistration>());
		}
		functions.get(appliancePid).add(registration);
	}

	/**
	 * Method called by JEMMA when an appliance is removed
	 */
	public void notifyApplianceRemoved(IAppliance appliance) {
		unregisterApplianceServices(appliance.getPid());
	}

	/**
	 * Internal method to unregister all services related with an appliance
	 * @param appliancePid
	 */
	private void unregisterApplianceServices(String appliancePid) {
		//unregister Device service
		if(devices.containsKey(appliancePid))
		{
			ServiceRegistration reg=devices.get(appliancePid);
			if(reg!=null)
				synchronized (reg) {
					reg.unregister();
				}
		}
		
		//unregister function services
		for(ServiceRegistration reg:functions.get(appliancePid))
		{
			if(reg!=null){
				synchronized(reg)
				{
					reg.unregister();
				}
			}
		}
	}

	/**
	 * Method invoked by JEMMA when the availability of an appliance has changed
	 */
	public void notifyApplianceAvailabilityUpdated(IAppliance appliance) {
		LOG.info("Appliance availability updated");
		if(!appliance.isDriver())
		{
			//this is not a real appliance, nothing to do
			return;
		}
		if(devices.containsKey(appliance.getPid()))
		{
			updateDeviceServiceProperties(appliance);
		}
	}

	/**
	 * Updates device services properties (e.g. Device status) according to the content of the IAppliance class
	 * @param appliance
	 */
	private void updateDeviceServiceProperties(IAppliance appliance) {
		
		Dictionary d=new Hashtable();
		
		d.put(Device.SERVICE_DRIVER, "ZigBee");
		d.put(Device.SERVICE_UID, IDConverters.getDeviceUid(appliance.getPid(), appliance.getConfiguration()));
		d.put(Device.SERVICE_NAME,IDConverters.getDeviceName(appliance.getPid(), appliance.getConfiguration()));
		
		String category=(String)appliance.getConfiguration().get("ah.category.pid");
		d.put("ah.category.pid", category);
		
		//added category from JEMMA in device's properties
		if(appliance.isAvailable())
		{
			d.put(Device.SERVICE_STATUS, Device.STATUS_ONLINE);
		}else{
			d.put(Device.SERVICE_STATUS, Device.STATUS_OFFLINE);
		}
	
		//update service properties if availability changed
		ServiceRegistration reg=devices.get(appliance.getPid());
		if(reg==null){
			return;
		}
		synchronized(reg)
		{
		
			if(reg==null)
			{
				LOG.error("No service reference for appliance: "+appliance.getPid());
				return;
			}
			ServiceReference ref=reg.getReference();
			if(ref==null)
			{
				LOG.error("Error getting service reference for appliance PID"+appliance.getPid());
				return;
			}
			Dictionary props = null;
			synchronized(ref)
			{
				if(!(ref.getProperty(Device.SERVICE_STATUS).equals(d.get(Device.SERVICE_STATUS))))
				{					
					reg.setProperties(d);
				}
				
			}
			//release service reference
			FrameworkUtil.getBundle(this.getClass()).getBundleContext().ungetService(ref);
		}
		
	}

	/**
	 * Method invoked by JEMMA when an attribute is received from an appliance through ZigBee AttributeNotification
	 * services
	 */
	public void notifyAttributeValue(String appliancePid, Integer endPointId, String clusterName, String attributeName,
			IAttributeValue attributeValue) {
		
		LOG.info("Received an attribute {} from {}",
				attributeName,
				clusterName);
		if(!(this.factories.containsKey(clusterName)))
		{
			LOG.error("No DAL adapter is defined for cluster "+endPointId);
			return;
		}
		IAppliance appliance=appliancesProxy.getAppliance(appliancePid);
		if(appliance==null)
		{
			LOG.error("The notified value arrives from a non-existing appliance");
			return;
		}
		
		//update device service properties status if they are changed: JEMMA is lazy notifying availability changes 
		updateDeviceServiceProperties(appliancesProxy.getAppliance(appliancePid));
		
		//resolve function service that related to that attribute
		String functionUid=this.factories.get(clusterName).getFunctionUID(appliance);
		
		String filterString = "("+Function.SERVICE_UID+"="+functionUid+")";
		
		//Filter filter=FrameworkUtil.getBundle(this.getClass()).getBundleContext().createFilter(filterString);
		BundleContext ctx;
		ctx=FrameworkUtil.getBundle(this.getClass()).getBundleContext();
		ServiceReference[] functionRefs;
		try {
			functionRefs = (ServiceReference[]) ctx.getServiceReferences(
				    Function.class.getName(),
				    filterString);
	
		
			if (null == functionRefs)
			{
				LOG.error("No function reference found");
			    return; // no such services
			}
			
			if(functionRefs.length!=1)
			{
				LOG.error("Invalid size ("+functionRefs.length+") for list of service references to function with UID:"+functionUid);
				return;
			}
			
			//get the DAL property name related to this attribute from the factory related to the cluster which generated the attribute 
			String matchingPropertyName = this.factories.get(clusterName).getMatchingPropertyName(attributeName,appliancesProxy.getAppliance(appliancePid));
			
			if(matchingPropertyName==null)
			{
				LOG.error("Unhandled property, cannot find a name matching {}",attributeName);
				return;
			}
			
			ClusterDALAdapter adapter= (ClusterDALAdapter) ctx.getService(functionRefs[0]);
			
			//now convert the attribute to a FunctionData using the resolved Function service
			FunctionData newValue=adapter.getMatchingPropertyValue(attributeName, attributeValue);
			
			//and post the event through OSGi eventAdmin service
			if(newValue!=null)
			{
				try{
					Dictionary properties=new Hashtable();
					properties.put(FunctionEvent.PROPERTY_FUNCTION_UID, functionUid);
					properties.put(FunctionEvent.PROPERTY_FUNCTION_PROPERTY_NAME, matchingPropertyName);
					properties.put(FunctionEvent.PROPERTY_FUNCTION_PROPERTY_VALUE, newValue);
					
					Event evt=new Event(FunctionEvent.TOPIC_PROPERTY_CHANGED,properties);
		
					this.eventAdmin.postEvent(evt);
				}catch(Exception e){
					LOG.error("Error creating event for attribute "+attributeName);
				}
			}
			ctx.ungetService(functionRefs[0]);
			
		} catch (InvalidSyntaxException e) {
			LOG.error("Error creating event for AttributeNotification,{}",e);
		}
		
	}
	
	public void deactivate()
	{
		//unregister all appliances services
		for(String pid:this.devices.keySet())
		{
			unregisterApplianceServices(pid);
		}
		try {
			connectionAdmin.removeBindRule(PID);
		} catch (HacException e) {
			LOG.error("Error removing bind rules in DAL adapter");
		}
	}
	
	
	public void bindAppliancesProxy(IAppliancesProxy appliancesProxy)
	{
		this.appliancesProxy=appliancesProxy;
	}
	
	public void unbindAppliancesProxy(IAppliancesProxy appliancesProxy)
	{
		this.appliancesProxy=null;
	}

	public void bindEventAdmin(EventAdmin eventAdmin)
	{
		this.eventAdmin=eventAdmin;
	}
	
	public void unbindEventAdmin(EventAdmin eventAdmin)
	{
		this.eventAdmin=eventAdmin;
	}

	
	public void bindIConnectionAdminService(IConnectionAdminService connectionAdminService)
	{
		this.connectionAdmin=connectionAdminService;
	}
	
	public void unbindIConnectionAdminService(IConnectionAdminService connectionAdminService)
	{
		this.connectionAdmin=null;
	}
	
	
	//TODO evaluate how to re-implement this putting logic for client clusters in factories or functions
	//Functions for implemented client clusters
	public void execSignalStateNotification(short ApplianceStatus, short RemoteEnableFlags, int ApplianceStatus2,
			IEndPointRequestContext context) throws ApplianceException, ServiceClusterException {
		try {
			IAppliance appliance=context.getPeerEndPoint().getAppliance();
			
			//the WG ApplianceControlServer calls the GW applianceControlClient
			//so a Function implementing interactions with ApplianceControlServer
			//must be resolved
			String clusterName=ApplianceControlServer.class.getName();
			
			//now get the function implementing service
			String functionUid=this.factories.get(clusterName).getFunctionUID(appliance);
			String filterString = "("+Function.SERVICE_UID+"="+functionUid+")";
			ServiceReference[] functionRefs;
		
			functionRefs = (ServiceReference[]) componentContext.getBundleContext().getServiceReferences(
				    Function.class.getName(),
				    filterString);
	
		
			if (null == functionRefs)
			{
				LOG.error("No function reference found");
			    return; // no such services
			}
			
			if(functionRefs.length!=1)
			{
				LOG.error("Invalid size ("+functionRefs.length+") for list of service references to function with UID:"+functionUid);
				return;
			}
			//get the service
			ClusterDALAdapter adapter= (ClusterDALAdapter) componentContext.getBundleContext().getService(functionRefs[0]);
			
			//invoke method that create FunctionData according to received property and post the event
			FunctionData applianceStatusValue=adapter.getDataFromClusterNotification("ApplianceStatus", new Short(ApplianceStatus));
			if(applianceStatusValue!=null)
			{
				postEvent(functionUid, "ApplianceStatus", applianceStatusValue );
			}
			
			FunctionData remoteControlFlagValue=adapter.getDataFromClusterNotification("RemoteControl", new Short(RemoteEnableFlags));
			if(applianceStatusValue!=null)
			{
				postEvent(functionUid, "RemoteControl", remoteControlFlagValue );
			}
						
			componentContext.getBundleContext().ungetService(functionRefs[0]);
			
		} catch (Throwable e) {
			LOG.error("Error receivign appliance status and remote control flags, {}",e);
		}
		
		
	}
	
	private void postEvent(String functionUid,String propertyName,FunctionData propertyValue)
	{
		if(propertyValue!=null)
		{
			try{
				Dictionary properties=new Hashtable();
				properties.put(FunctionEvent.PROPERTY_FUNCTION_UID, functionUid);
				properties.put(FunctionEvent.PROPERTY_FUNCTION_PROPERTY_NAME, propertyName);
				properties.put(FunctionEvent.PROPERTY_FUNCTION_PROPERTY_VALUE, propertyValue);
				
				Event evt=new Event(FunctionEvent.TOPIC_PROPERTY_CHANGED,properties);
	
				this.eventAdmin.postEvent(evt);
			}catch(Exception e){
				LOG.error("Error creating event for property "+propertyName);
			}
		}
	}
	
	
	public IApplianceDescriptor getDescriptor() {
		return descriptor;
	}

}
