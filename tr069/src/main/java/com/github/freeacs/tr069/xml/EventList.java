package com.github.freeacs.tr069.xml;

import java.util.ArrayList;

public class EventList {
	public static final String ID = "EventList";

	private ArrayList<EventStruct> events;

	public EventList() {
		events = new ArrayList<EventStruct>();
	}

	public void addEvent(EventStruct event) {
		this.events.add(event);
	}

	public ArrayList<EventStruct> getEventList() {
		return this.events;
	}

	public EventList clone() {
		EventList clonedEventList = new EventList();
		for (int i = 0; i < events.size(); i++) {
			EventStruct event = events.get(i);
			EventStruct clonedEvent = new EventStruct(new String(event.getEventCode()), new String(event.getCommandKey()));
			clonedEventList.addEvent(clonedEvent);
		}
		return clonedEventList;
	}
}
