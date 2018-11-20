package cn.edu.whu.openmi.event;

import org.openmi.standard.IEvent;
import org.openmi.standard.IEvent.EventType;
import org.openmi.standard.IListener;
import org.openmi.standard.ITimeStamp;

import cn.edu.whu.openmi.util.OpenMIUtilities;

public class EventListener implements IListener {

	@Override
	public EventType getAcceptedEventType(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getAcceptedEventTypeCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void onEvent(IEvent event) {
		// TODO Auto-generated method stub
		String description = event.getDescription();
		EventType eventType = event.getType();
		ITimeStamp timeStamp = event.getSimulationTime();
		String senderID = event.getSender().getModelID();
		String print = "";
		if (!OpenMIUtilities.isEmpty(senderID)) {
			print+=senderID +" sends an event ";
		}
		if (eventType!=null) {
			print += "typed "+eventType.toString();
		}
		if (timeStamp!=null) {
			print+= " in "+timeStamp.toString();
		}
		if (!OpenMIUtilities.isEmpty(description)) {
			print+=",the description is "+description;
		}
		System.out.println(print);
	}

}
