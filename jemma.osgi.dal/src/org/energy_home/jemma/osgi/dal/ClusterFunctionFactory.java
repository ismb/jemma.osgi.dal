package org.energy_home.jemma.osgi.dal;

import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.osgi.framework.ServiceRegistration;

public interface ClusterFunctionFactory {

	//this creates function instance and registers service
	public ServiceRegistration createFunctionService(IAppliance appliance,Integer endPointId,IAppliancesProxy appliancesProxy);
	
	public boolean isClusterMatching(String clusterName);
}
