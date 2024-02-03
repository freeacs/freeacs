package de.javawi.jstun.attribute;

import de.javawi.jstun.attribute.MessageAttributeInterface.MessageAttributeType;

import java.io.Serial;

public class UnknownMessageAttributeException extends MessageAttributeParsingException {
  @Serial
  private static final long serialVersionUID = 5375193544145543299L;

  private final MessageAttributeType type;

  public UnknownMessageAttributeException(String mesg, MessageAttributeType type) {
    super(mesg);
    this.type = type;
  }

  public MessageAttributeType getType() {
    return type;
  }
}
