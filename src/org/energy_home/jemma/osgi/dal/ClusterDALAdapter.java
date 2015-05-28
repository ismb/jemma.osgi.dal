package org.energy_home.jemma.osgi.dal;

import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.lib.AttributeValue;
import org.osgi.service.dal.FunctionData;
import org.osgi.service.dal.functions.data.BooleanData;

public interface ClusterDALAdapter {

	//function to be implemented to translate an AttributeNotification having its value and the name provided
	// by the factory
	public FunctionData getMatchingPropertyValue(String attributeName, IAttributeValue attributeValue);

	//function to be implemented to translate a notification received from a client cluster
	// returns the FunctionData if the notification is supported by the adapter, null otherwise 
	public FunctionData getDataFromClusterNotification(String notificationPropertyName,Object value);
}
