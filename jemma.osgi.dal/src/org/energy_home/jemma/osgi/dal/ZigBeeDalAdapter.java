package org.energy_home.jemma.osgi.dal;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

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
import org.energy_home.jemma.osgi.dal.factories.MeterSimpleMeteringFactory;
import org.energy_home.jemma.osgi.dal.utils.IDConverters;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.dal.Device;
import org.osgi.service.dal.Function;
import org.osgi.service.dal.FunctionEvent;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO: handle deactivate: unregister all dal services

public class ZigBeeDalAdapter implements IApplicationService,IAttributeValuesListener{

	private IAppliancesProxy appliancesProxy;
	private EventAdmin eventAdmin;
	private static final Logger LOG = LoggerFactory.getLogger( ZigBeeDalAdapter.class );
	
	List<ServiceRegistration> functions;
	List<ServiceRegistration> devices;
	
	List<ClusterFunctionFactory> factories;
	
	public ZigBeeDalAdapter(){
		factories=new LinkedList<ClusterFunctionFactory>();
		factories.add(new BooleanControlOnOffFactory());
		factories.add(new MeterSimpleMeteringFactory());
		functions=new LinkedList<ServiceRegistration>();
		devices=new LinkedList<ServiceRegistration>();
	}
	
	@Override
	public IServiceCluster[] getServiceClusters() {
		// nothing to be done
		return null;
	}

	/**
	 * Method called byt JEMMA when a new appliance is configured in the framework
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
				for(ClusterFunctionFactory factory:this.factories)
				{
					if(factory.isClusterMatching(cluster.getName()))
					{
						//register function service
						functions.add(factory.createFunctionService(appliance,
								ep.getId(), appliancesProxy));
					}
				}
			}
		}

		
		//register device service
		Dictionary d=new Hashtable();
		//FIXME: change device ID
		d.put(Device.SERVICE_DRIVER, "ZigBee");
		d.put(Device.SERVICE_UID, IDConverters.getDeviceUid(appliance.getPid(), appliance.getConfiguration()));
		d.put(Device.SERVICE_STATUS, "2");
		devices.add(FrameworkUtil.getBundle(this.getClass()).getBundleContext().registerService(
				Device.class,
				new JemmaDevice(),
				d));
		
	}

	@Override
	public void notifyApplianceRemoved(IAppliance appliance) {
		// an appliance have been removed, unregister Device and Functions

	}

	@Override
	public void notifyApplianceAvailabilityUpdated(IAppliance appliance) {
		// Update appliances references
		
	}

	@Override
	public void notifyAttributeValue(String appliancePid, Integer endPointId, String clusterName, String attributeName,
			IAttributeValue attributeValue) {
		LOG.info(attributeValue.toString());
		
		Hashtable properties=new Hashtable();

		/*
		properties.put(FunctionEvent.PROPERTY_FUNCTION_UID, context.getProperties().get(Function.SERVICE_UID));
		properties.put(FunctionEvent.PROPERTY_FUNCTION_PROPERTY_NAME, "data");
		properties.put(FunctionEvent.PROPERTY_FUNCTION_PROPERTY_VALUE, attributeValue.getValue());
		
		Event evt=new Event(FunctionEvent.TOPIC_PROPERTY_CHANGED,(Dictionary)properties);

		this.eventAdmin.postEvent(evt);*/
		
	}
	
	public void bindAppliancesProxy(IAppliancesProxy appliancesProxy)
	{
		this.appliancesProxy=appliancesProxy;
	}
	
	public void unbindAppliancesProxy(IAppliancesProxy appliancesProxy)
	{
		this.appliancesProxy=null;
	}

}
