package com.github.freeacs.tr069.xml;

import java.util.ArrayList;
import java.util.List;

public class EventList {
  public static final String ID = "EventList";

  private List<EventStruct> events;

  public EventList() {
    events = new ArrayList<>();
  }

  public void addEvent(EventStruct event) {
    this.events.add(event);
  }

  public List<EventStruct> getEventList() {
    return this.events;
  }

  public EventList clone() {
    EventList clonedEventList = new EventList();
    for (EventStruct event : events) {
      EventStruct clonedEvent = new EventStruct(event.getEventCode(), event.getCommandKey());
      clonedEventList.addEvent(clonedEvent);
    }
    return clonedEventList;
  }
}
