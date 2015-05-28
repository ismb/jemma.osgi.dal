package org.energy_home.jemma.osgi.dal;

import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.service.dal.Device;
import org.osgi.service.dal.DeviceException;

public class JemmaDevice implements Device{
	Dictionary serviceProperties=new Hashtable();
	
	public JemmaDevice(){}
	
	
	public JemmaDevice(Dictionary serviceProperties)
	{
		this.serviceProperties=serviceProperties;
	}
	
	public Object getServiceProperty(String propName) {
		return serviceProperties.get(propName);
	}

	public void remove() throws DeviceException, UnsupportedOperationException,
			SecurityException, IllegalStateException {
		// TODO Auto-generated method stub
		
	}

}
