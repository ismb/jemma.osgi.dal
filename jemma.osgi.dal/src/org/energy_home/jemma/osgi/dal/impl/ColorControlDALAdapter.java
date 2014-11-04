package org.energy_home.jemma.osgi.dal.impl;

import org.energy_home.dal.functions.ColorControl;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ApplianceControlServer;
import org.energy_home.jemma.ah.cluster.zigbee.zll.ColorControlServer;
import org.energy_home.jemma.ah.hac.ApplianceException;
import org.energy_home.jemma.ah.hac.IAttributeValue;
import org.energy_home.jemma.ah.hac.ServiceClusterException;
import org.energy_home.jemma.ah.hac.lib.ext.IAppliancesProxy;
import org.osgi.service.dal.DeviceException;
import org.osgi.service.dal.FunctionData;
import org.osgi.service.dal.OperationMetadata;
import org.osgi.service.dal.PropertyMetadata;

public class ColorControlDALAdapter extends BaseDALAdapter implements ColorControl {

	private static final String COLORCONTROLCLUSTER = "org.energy_home.jemma.ah.cluster.zigbee.zll.ColorControlServer";

	public ColorControlDALAdapter(String appliancePid, Integer endPointId, IAppliancesProxy appliancesProxy) {
		super(appliancePid, endPointId, appliancesProxy);
	}

	@Override
	public PropertyMetadata getPropertyMetadata(String propertyName) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OperationMetadata getOperationMetadata(String operationName) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getServiceProperty(String propName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setColor(Short red, Short green, Short blue) throws DeviceException {
		Short[] hs=toHS(red, green, blue);
		
		try {
			getCluster().execMoveToHueAndSaturation(hs[0], hs[1], 10, appliancesProxy.getRequestContext(true));
		} catch (ApplianceException | ServiceClusterException e) {
			throw new DeviceException(e.getMessage(),e.getCause());
		}
	}
	
	
	
	public Short[] getColor() throws DeviceException
	{
		try
		{
			Short hue=getCluster().getCurrentHue(appliancesProxy.getRequestContext(true));
			Short sat=getCluster().getCurrentSaturation(appliancesProxy.getRequestContext(true));
			//scale values
			float hueV=hue/255;
			float satV=sat/255;

			return toRGB(hue, sat);
		}catch(Exception e)
		{
			throw new DeviceException(e.getMessage(),e.getCause());
		}
	}
	
	private ColorControlServer getCluster()
	{
		return (ColorControlServer) appliancesProxy.getAppliance(appliancePid).getEndPoint(endPointId).getServiceCluster(COLORCONTROLCLUSTER);
	}

	@Override
	public FunctionData getMatchingPropertyValue(String attributeName, IAttributeValue attributeValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateApplianceSubscriptions() {
		// TODO Auto-generated method stub
		
	}
	
	private Short[] toHS(Short red,Short green, Short blue)
	{
		double maxRGB = this.findMax(red, green, blue) / 255.0d;
		double minRGB = this.findMin(red, green, blue) / 255.0d;
		double delta = maxRGB - minRGB;
		
		double n_red = red / 255.0d;
		double n_green = green /255.0d;
		double n_blue = blue / 255.0d;

		double brightness = maxRGB;
		double saturation = maxRGB - minRGB / maxRGB;
		double hue = 65535;

		if (delta != 0)
		{
			if (maxRGB == n_red)
			{
				hue = 60 * ((n_green  -  n_blue ) /  delta) % 360;
			} else if (maxRGB == n_green)
			{
				hue = 60 * ((n_blue  - n_red ) /  delta) + 120;
			} else if (maxRGB == n_blue)
			{
				hue = 60 * ((n_red  - n_green) / delta) + 240;
			}
		}
		
		return new Short[]{ (short)((hue/360)*254),(short)(saturation*254)};
	}
	
	private Short[] toRGB(Short hue,Short saturation)
	{
		double h=hue/255.0d;
		double s=saturation/255.0d;
		double v=1;
		
		double r=0;
		double g=0;
		double b=0;
		
		if (s != 0)
		{
			double i = Math.floor(h / 60);
			double f = h - i;
			double p = v * (1 - s);
			double q = v * (1 - s * f);
			double t = v * (1 - s * (1 - f));

			switch ((int) i)
			{
			case 0:
			{
				r = v;
				g = t;
				b = p;
				break;
			}
			case 1:
			{
				r = q;
				g = v;
				b = p;
				break;
			}
			case 2:
			{
				r = p;
				g = v;
				b = t;
				break;
			}
			case 3:
			{
				r = p;
				g = q;
				b = v;
				break;
			}
			case 4:
			{
				r = t;
				g = p;
				b = v;
				break;
			}
			default:
			{
				r = v;
				g = p;
				b = q;
				break;
			}
			}

		}

		// denormalize
		return new Short[]{ (short)(r*255), (short)(g*255), (short)(g*255)};
	}
	
	private Short findMax(Short... values)
	{
		// init at the minum value
		Short max = Short.MIN_VALUE;

		// max search
		for (Short value : values)
			if (max < value)
				max = value;

		// return the maximum
		return max;
	}

	private Short findMin(Short... values)
	{
		// init at the minum value
		Short min = Short.MAX_VALUE;

		// max search
		for (Short value : values)
			if (min > value)
				min = value;

		// return the maximum
		return min;
	}

}
