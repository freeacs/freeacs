package com.github.freeacs.shell.command;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ContextElement implements Substitute {
  public static String TYPE_UNITTYPE = "ut";
  public static String TYPE_PROFILE = "pr";
  public static String TYPE_UNIT = "un";
  public static String TYPE_GROUP = "gr";
  public static String TYPE_JOB = "jo";
  public static String TYPE_UNITTYPE_PARAMS = "up";
  public static String TYPE_ROOT = "ro";
  public static String TYPE_BACK = "ba";

  public static Set<String> types = new HashSet<>();

  static {
    types.add(TYPE_UNITTYPE);
    types.add(TYPE_PROFILE);
    types.add(TYPE_UNIT);
    types.add(TYPE_GROUP);
    types.add(TYPE_JOB);
    types.add(TYPE_UNITTYPE_PARAMS);
  }

  private String name;
  private String type;
  private String substitute;

  public ContextElement(String type, String name) {
    this.type = type;
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  @Override
  public void resetToOriginalState() {
    substitute = name;
  }

  @Override
  public String getStringToSubstitute() {
    if (substitute != null) {
      return substitute;
    }
    return name;
  }

  @Override
  public void setSubstitutedString(String s) {
    substitute = s;
  }

  public String toString() {
    return type + ":" + getStringToSubstitute() + "/";
  }

  public static ContextContainer parseContextElements(String s) {
    ContextContainer cc = new ContextContainer();
    if (s == null || s.trim().isEmpty()) {
      return cc;
    }
    s = s.trim();
    if (s.startsWith("/")) {
      s = s.substring(1);
      cc.overwriteOrAppend(new ContextElement(TYPE_ROOT, "Root"));
    }
    String[] slashSplitArr = s.split("/");
    for (String contextStr : slashSplitArr) {
      if (contextStr.indexOf(':') == 2) { // matches context-pattern
        String type = contextStr.substring(0, 2);
        if (types.contains(type)) {
          cc.overwriteOrAppend(new ContextElement(type, contextStr.substring(3)));
        } else {
          throw new IllegalArgumentException("The context type " + type + " was not valid");
        }
      } else if ("..".equals(contextStr)) {
        cc.overwriteOrAppend(new ContextElement(TYPE_BACK, ".."));
      }
    }
    if (cc.getContextElement(TYPE_ROOT) != null) {
      cc.removeContextElement(TYPE_ROOT);
    }
    return cc;
  }

  /**
   * Accepted context elements string / /<type>:<name> /<type>:<name>/ /<type>:<name>/<type:name>
   * etc.. <type>:<name> <type>:<name>/ <type>:<name>/<type>:<name> etc..
   */
  public static Map<String, ContextElement> parseContextElementsOld(String s) {
    Map<String, ContextElement> contextMap = new HashMap<>();
    if (s == null || s.trim().isEmpty()) {
      return contextMap;
    }
    s = s.trim();
    // treat ROOT-context first
    if (s.startsWith("/")) {
      s = s.substring(1);
      contextMap.put(TYPE_ROOT, new ContextElement(TYPE_ROOT, "Root"));
    }
    String[] slashSplitArr = s.split("/");
    for (String contextStr : slashSplitArr) {
      if (contextStr.indexOf(':') == 2) { // matches context-pattern
        String type = contextStr.substring(0, 2);
        if (types.contains(type)) {
          contextMap.put(type, new ContextElement(type, contextStr.substring(3)));
        } else {
          throw new IllegalArgumentException("The context type " + type + " was not valid");
        }
      } else if ("..".equals(contextStr)) {
        if (contextMap.get(TYPE_UNIT) != null) {
          contextMap.remove(TYPE_UNIT);
        } else if (contextMap.get(TYPE_UNITTYPE_PARAMS) != null) {
          contextMap.remove(TYPE_UNITTYPE_PARAMS);
        } else if (contextMap.get(TYPE_GROUP) != null) {
          contextMap.remove(TYPE_GROUP);
        } else if (contextMap.get(TYPE_JOB) != null) {
          contextMap.remove(TYPE_JOB);
        } else if (contextMap.get(TYPE_PROFILE) != null) {
          contextMap.remove(TYPE_PROFILE);
        } else if (contextMap.get(TYPE_UNITTYPE) != null) {
          contextMap.remove(TYPE_UNITTYPE);
        }
      }
    }
    if (contextMap.size() > 1 && contextMap.get(TYPE_ROOT) != null) {
      contextMap.remove(TYPE_ROOT);
    }
    return contextMap;
  }
}
