package com.github.freeacs.tr069.xml;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The class is responsible for parsing the Event entity and populating the EventList which is a
 * list of EventStruct objects (Note that each EventStruct object has two parameters called the
 * EventCode and the CommandKey).
 */
public class EventHandler extends DefaultHandler {
  static final String EVENT_TAG = "Event";
  private static final String EVENT_STRUCT_TAG = "EventStruct";
  private static final String EVENT_CODE_TAG = "EventCode";
  private static final String COMMAND_KEY_TAG = "CommandKey";

  private Parser owner;
  private EventList events;
  private EventStruct currEvent;
  private StringBuilder currTextContent = new StringBuilder();

  EventHandler(EventList events, Parser owner) {
    this.events = events;
    this.owner = owner;
  }

  public void startElement(
      String namespaceURI, String localName, String qualifiedName, Attributes attributes) {
    currTextContent = new StringBuilder();
    if (EVENT_STRUCT_TAG.equals(localName)) {
      currEvent = new EventStruct();
    }
  }

  public void endElement(String namespaceURI, String localName, String qualifiedName) {
    if (EVENT_TAG.equals(localName)) {
      owner.getXmlReader().setContentHandler(owner);
    } else if (EVENT_STRUCT_TAG.equals(localName)) {
      if (currEvent != null) {
        events.addEvent(currEvent);
        currEvent = null;
      }
    } else if (EVENT_CODE_TAG.equals(localName)) {
      if (currEvent != null) {
        currEvent.setEventCode(new String(currTextContent));
      }
    } else if (COMMAND_KEY_TAG.equals(localName) && currEvent != null) {
      currEvent.setCommandKey(new String(currTextContent));
    }
  }

  public void characters(char[] ch, int start, int length) {
    String content = String.valueOf(ch).substring(start, start + length);
    currTextContent.append(content.trim());
  }
}
