package org.energy_home.jemma.osgi.dal.utils;

import java.util.Calendar;

import org.energy_home.dal.functions.type.TimeAttribute;

public class DataConverters {

	
	//constructor to be used to parse ZigBee StartTime/FinishTime/RemainingTime attributes
		public static TimeAttribute toTimeAttribute(int time) {
			TimeAttribute attr=new TimeAttribute();
			boolean relative = (time & 0xC0) == 0; 
			attr.setAbsolute(!relative);
			
			attr.setHours( (time & 0xFF00) >> 8 );
			attr.setMinutes( time & 0x003F);
			
			return attr;
		}
		
		public static int toApplianceTime(TimeAttribute attr)
		{
			
			int relativeMinutes=attr.getHours()*60+attr.getMinutes();
			
			int t_hours, t_minutes, mask, result;
			
			Calendar cal=Calendar.getInstance();
			
			if (attr.getAbsolute()) {
				cal.setTimeInMillis(System.currentTimeMillis());
				cal.add(Calendar.MINUTE, relativeMinutes);
				t_hours = cal.get(Calendar.HOUR_OF_DAY);
				t_minutes = cal.get(Calendar.MINUTE);
				mask = 0x40;
			} else {
				t_hours = relativeMinutes / 60;
				t_minutes = relativeMinutes % 60;
				cal.setTimeInMillis(System.currentTimeMillis());
				mask = 0x00;
			}
			result = (t_hours << 8) | mask | (t_minutes & 0x3F); 
			return result;
		}
}
