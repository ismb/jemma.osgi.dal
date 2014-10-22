package org.energy_home.jemma.osgi.dal;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IApplianceDescriptor;
import org.energy_home.jemma.ah.hac.IApplicationEndPoint;
import org.energy_home.jemma.ah.hac.IApplicationService;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.IAttributeValuesListener;
import org.energy_home.jemma.ah.hac.IEndPoint;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.energy_home.jemma.osgi.dal.factories.BooleanControlOnOffFactory;
import org.energy_home.jemma.osgi.dal.factories.EnergyMeterSimpleMeteringFactory;
import org.energy_home.jemma.osgi.dal.factories.TemperatureMeterThermostatFactory;
import org.energy_home.jemma.osgi.dal.utils.IDConverters;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.dal.Device;
import org.osgi.service.dal.Function;
import org.osgi.service.dal.FunctionData;
import org.osgi.service.dal.FunctionEvent;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO: handle deactivate: unregister all dal services

public class ZigBeeDalAdapter implements IApplicationService,IAttributeValuesListener{

	private IAppliancesProxy appliancesProxy;
	private EventAdmin eventAdmin;
	
	private static final Logger LOG = LoggerFactory.getLogger( ZigBeeDalAdapter.class );
	
	private Map<String,List<ServiceRegistration>> functions;
	private Map<String,ServiceRegistration> devices;
	
	private Map<String,ClusterFunctionFactory> factories;
	
	public ZigBeeDalAdapter(){
		//add factories
		factories=new HashMap<String,ClusterFunctionFactory>();
		addClusterFunctionFactory(new BooleanControlOnOffFactory());
		addClusterFunctionFactory(new EnergyMeterSimpleMeteringFactory());
		addClusterFunctionFactory(new TemperatureMeterThermostatFactory());
		
		functions=new HashMap<String,List<ServiceRegistration>>();
		devices=new HashMap<String,ServiceRegistration>();
	}
	
	private void addClusterFunctionFactory(ClusterFunctionFactory factory)
	{
		this.factories.put(factory.getMatchingCluster(), factory);
	}
	
	@Override
	public IServiceCluster[] getServiceClusters() {
		// nothing to be done
		return null;
	}

	/**
	 * Method called by JEMMA when a new appliance is configured in the framework
	 * @param endPoint
	 * @param appliance
	 */
	@Override
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
		devices.put(appliance.getPid(),FrameworkUtil.getBundle(this.getClass()).getBundleContext().registerService(
				Device.class,
				new JemmaDevice(),
				d));
		
		//update device properties according to appliance status
		updateDeviceServiceProperties(appliance);
		
	}

	private void addFunctionRegistration(String appliancePid,ServiceRegistration registration) {
		if(!functions.containsKey(appliancePid))
		{
			functions.put(appliancePid, new LinkedList<ServiceRegistration>());
		}
		functions.get(appliancePid).add(registration);
	}

	@Override
	public void notifyApplianceRemoved(IAppliance appliance) {
		//unregister Device service
		if(devices.containsKey(appliance.getPid()))
		{
			devices.get(appliance.getPid()).unregister();
		}
		
		//unregister function services
		for(ServiceRegistration reg:functions.get(appliance.getPid()))
		{
			reg.unregister();
		}
	}

	@Override 
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

	private void updateDeviceServiceProperties(IAppliance appliance) {
		
		Dictionary d=new Hashtable();
		
		d.put(Device.SERVICE_DRIVER, "ZigBee");
		d.put(Device.SERVICE_UID, IDConverters.getDeviceUid(appliance.getPid(), appliance.getConfiguration()));
		
		//change the DAL Device Service status property accoring to Device avialability
		if(appliance.isAvailable())
		{
			d.put(Device.SERVICE_STATUS, Device.STATUS_ONLINE);
		}else{
			d.put(Device.SERVICE_STATUS, Device.STATUS_OFFLINE);
		}

		//update service properties
		ServiceRegistration reg=devices.get(appliance.getPid());
		reg.setProperties(d);
		
		//inform the framework that the service have been modified
		ServiceEvent serviceEvent=new ServiceEvent(ServiceEvent.MODIFIED, reg.getReference());
		Dictionary props = new Hashtable();
		props.put(EventConstants.EVENT, serviceEvent);
		props.put(EventConstants.SERVICE, serviceEvent.getServiceReference());
		props.put(EventConstants.SERVICE_PID, serviceEvent.getServiceReference().getProperty(Constants.SERVICE_PID));
		props.put(EventConstants.SERVICE_ID, serviceEvent.getServiceReference().getProperty(Constants.SERVICE_ID));
		props.put(EventConstants.SERVICE_OBJECTCLASS, serviceEvent.getServiceReference().getProperty(Constants.OBJECTCLASS)); 
		eventAdmin.postEvent(new Event("org/osgi/framework/ServiceEvent/MODIFIED",props)) ;
	}

	@Override
	public void notifyAttributeValue(String appliancePid, Integer endPointId, String clusterName, String attributeName,
			IAttributeValue attributeValue) {
		LOG.info(attributeValue.toString());
		if(!(this.factories.containsKey(clusterName)))
		{
			LOG.error("No DAL adapter is defined for cluster "+endPointId);
			return;
		}
		IAppliance appliance=appliancesProxy.getAppliance(appliancePid);
		if(appliance==null)
		{
			LOG.error("The notified value arrives from a non-existing appliance");
		}
		
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
			
			ClusterDALAdapter adapter= ctx.getService(functionRefs[0]);
			
			FunctionData newValue=adapter.getMatchingPropertyValue(attributeName, attributeValue);
			if(newValue!=null)
			{
				Dictionary properties=new Hashtable();
				properties.put(FunctionEvent.PROPERTY_FUNCTION_UID, functionUid);
				properties.put(FunctionEvent.PROPERTY_FUNCTION_PROPERTY_NAME, this.factories.get(clusterName).getMatchingPropertyName(attributeName));
				properties.put(FunctionEvent.PROPERTY_FUNCTION_PROPERTY_VALUE, newValue);
				
				Event evt=new Event(FunctionEvent.TOPIC_PROPERTY_CHANGED,properties);
	
				this.eventAdmin.postEvent(evt);
			}
			ctx.ungetService(functionRefs[0]);
			
		} catch (InvalidSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		/*
		*/
		
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
}
