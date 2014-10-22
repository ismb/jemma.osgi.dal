package org.energy_home.jemma.osgi.dal;

import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.lib.AttributeValue;
import org.osgi.service.dal.FunctionData;
import org.osgi.service.dal.functions.data.BooleanData;

public interface ClusterDALAdapter {

	public FunctionData getMatchingPropertyValue(String attributeName, IAttributeValue attributeValue);

}
