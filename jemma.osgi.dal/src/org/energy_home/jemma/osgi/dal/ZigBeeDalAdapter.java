package org.energy_home.jemma.osgi.dal;

import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.IApplianceDescriptor;
import org.energy_home.jemma.ah.hac.IApplicationEndPoint;
import org.energy_home.jemma.ah.hac.IApplicationService;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.IAttributeValuesListener;
import org.energy_home.jemma.ah.hac.IServiceCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sun.util.logging.resources.logging;

public class ZigBeeDalAdapter implements IApplicationService,IAttributeValuesListener{

	private static final Logger LOG = LoggerFactory.getLogger( ZigBeeDalAdapter.class );
	
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
		IApplianceDescriptor desc=appliance.getDescriptor();
		LOG.info("###### APPLIANCE Descriptor:"+desc.getDeviceType()+"-"+desc.getType()+"-"+desc.getFriendlyName());
		LOG.info("###### APPLIANCE INFO:"+appliance.getPid()+" "+appliance.toString());
		//Register OSGi DAL services
		
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
		// A new value for the attribute, trasnform it into a DAL Event and publish it
		
	}

}
