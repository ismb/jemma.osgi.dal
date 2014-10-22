package org.energy_home.jemma.osgi.dal;

import org.energy_home.jemma.ah.hac.IAppliance;
import org.energy_home.jemma.ah.hac.lib.AttributeValue;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.dal.FunctionData;

public interface ClusterFunctionFactory {

	//this creates function instance and registers service
	public ServiceRegistration createFunctionService(IAppliance appliance,Integer endPointId,IAppliancesProxy appliancesProxy);
	
	public String getFunctionUID(IAppliance appliance);
	
	public String getMatchingPropertyName(String attributeName);
	
	public String getMatchingCluster();

}
