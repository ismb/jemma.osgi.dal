package org.energy_home.jemma.osgi.dal.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.energy_home.dal.functions.data.PowerProfileConstraintsData;
import org.energy_home.dal.functions.data.PowerProfileData;
import org.energy_home.dal.functions.data.PowerProfilePhasesData;
import org.energy_home.dal.functions.type.PowerProfileAttribute;
import org.energy_home.dal.functions.type.ScheduledPhaseAttribute;
import org.energy_home.dal.functions.type.TimeAttribute;
import org.energy_home.dal.functions.type.TransferredPhaseAttribute;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileScheduleConstraintsResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.PowerProfileStateResponse;
import org.energy_home.jemma.ah.cluster.zigbee.eh.ScheduledPhase;

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

		public static PowerProfileConstraintsData toPowerProfileConstraintsData(PowerProfileScheduleConstraintsResponse resp) {
			PowerProfileConstraintsData data=new PowerProfileConstraintsData();
			data.PowerProfileID=resp.PowerProfileID;
			data.StartAfter=resp.StartAfter;
			data.StopBefore=resp.StopBefore;
			return data;
			
		}

		public static PowerProfileData toPowerProfileData(PowerProfileStateResponse resp) {
			List<PowerProfileAttribute> powerProfiles=new ArrayList<PowerProfileAttribute>();
			
			for(int i=0;i<resp.PowerProfiles.length;i++)
			{
				PowerProfileAttribute attr=new PowerProfileAttribute();
				attr.EnergyPhaseID=resp.PowerProfiles[i].EnergyPhaseID;
				attr.PowerProfileID=resp.PowerProfiles[i].PowerProfileID;
				attr.PowerProfileRemoteControl=resp.PowerProfiles[i].PowerProfileRemoteControl;
				attr.PowerProfileState=resp.PowerProfiles[i].PowerProfileState;
				powerProfiles.add(attr);
			}
			
			return new PowerProfileData(System.currentTimeMillis(), null,powerProfiles);
		}

		public static PowerProfilePhasesData toPowerProfilePhasesData(PowerProfileResponse resp) {
			List<TransferredPhaseAttribute> phases=new ArrayList<TransferredPhaseAttribute>();
			for(int i=0;i<resp.PowerProfileTransferredPhases.length;i++)
			{
				TransferredPhaseAttribute phase=new TransferredPhaseAttribute();
				phase.Energy=resp.PowerProfileTransferredPhases[i].Energy;
				phase.EnergyPhaseID=resp.PowerProfileTransferredPhases[i].EnergyPhaseID;
				phase.ExpectedDuration=resp.PowerProfileTransferredPhases[i].ExpectedDuration;
				phase.MacroPhaseID=resp.PowerProfileTransferredPhases[i].MacroPhaseID;
				phase.MaxActivationDelay=resp.PowerProfileTransferredPhases[i].MaxActivationDelay;
				phases.add(phase);
			}
			return new PowerProfilePhasesData(System.currentTimeMillis(), null,resp.PowerProfileID,resp.TotalProfileNum,phases);
		}

		public static ScheduledPhase[] toScheduledPhases(ScheduledPhaseAttribute[] phases) {
			ScheduledPhase[] s_phases=new ScheduledPhase[phases.length];
			for(int i=0;i<phases.length;i++)
			{
				s_phases[i]=new ScheduledPhase(phases[i].EnergyPhaseID,phases[i].ScheduledTime);
			}
			return s_phases;
		}
}
