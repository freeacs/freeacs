package com.github.freeacs.shell.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ContextContainer will store the context as parsed from the command line. Previously the
 * parsed context has been stored in a HashMap, but it turned out that the we needed both the
 * map-quality and the list-quality. The LinkedHashMap was considered, but still not useful, since
 * some keys could occur more than once. The end-result was to create this class, to try to cover
 * both needs.
 *
 * <p>A context from the command line is parsed and a ContextElement is generated for each part of
 * the context, usually separated with a slash (/). These ContextElements are then placed into this
 * container.
 *
 * <p>The following rules apply for adding ContextElements to this container:
 *
 * <p>1. Adding a ContextElement with equal type to an already existing ContextElement will result
 * in overwriting the old type. 2. Exception for rule 1 if type is "ba" = BACK (expressed as ".." on
 * the command line) 3. If BACK ContextElement is preceded by a ContextElement of another type than
 * BACK, both ContextElements will be removed from the container, since they cancel each other.
 *
 * @author morten
 */
public class ContextContainer {
  private List<ContextElement> contextList = new ArrayList<>();
  private Map<String, Integer> lookupMap = new HashMap<>();
  private static Logger logger = LoggerFactory.getLogger(ContextContainer.class);

  public ContextElement getContextElement(String type) {
    Integer index = lookupMap.get(type);
    if (index != null) {
      return contextList.get(index);
    } else {
      return null;
    }
  }

  public void removeContextElement(String type) {
    Integer index = lookupMap.get(type);
    if (index != null) {
      contextList.remove(index);
      lookupMap.remove(type);
    }
  }

  public int size() {
    return contextList.size();
  }

  //	public void addContextContainer(ContextContainer cc) {
  //		if (cc != null && cc.getContextList().size() > 0) {
  //			for (ContextElement ce : cc.getContextList())
  //				addContextElement(ce);
  //		}
  //	}

  /**
   * This method is supposed to be used by context containers with absolute path/context. We
   * anticipate such contexts from -u option of a command (either read directly from command or from
   * a file). Thus, all ../ are ignored.
   *
   * @param cc
   */
  public void overwriteOrInsert(ContextContainer cc) {
    String thisCCStr = toString();
    String newCCStr = cc.toString();
    if (cc != null && !cc.getContextList().isEmpty()) {
      for (int i = cc.getContextList().size() - 1; i >= 0; i--) {
        overwriteOrInsert(cc.getContextList().get(i));
      }
    }
    logger.debug("OverwriteOrInsert " + thisCCStr + "  +  " + newCCStr + "  =  " + this);
  }

  public void skipOrAppend(ContextContainer cc) {
    String thisCCStr = toString();
    String newCCStr = cc.toString();
    if (cc != null && !cc.getContextList().isEmpty()) {
      for (ContextElement ce : cc.getContextList()) {
        skipOrAppend(ce);
      }
    }
    if (!"".equals(thisCCStr.trim()) && !"".equals(newCCStr.trim())) {
      logger.debug("Adding two contexts: " + thisCCStr + "  +  " + newCCStr + "  =  " + this);
    }
  }

  private void overwriteOrInsert(ContextElement ce) {
    if (ce != null && ce.getType() != null) {
      Integer index = lookupMap.get(ce.getType());
      if (index != null) {
        contextList.remove((int) index);
        contextList.add(index, ce);
      } else {
        // Updated lookupMap before insert new element in context-list
        for (int i = contextList.size() - 1; i >= 0; i--) {
          ContextElement tmp = contextList.get(i);
          Integer tmpIndex = lookupMap.get(tmp.getType());
          lookupMap.put(tmp.getType(), tmpIndex + 1);
        }
        contextList.add(0, ce);
        lookupMap.put(ce.getType(), 0);
      }
    }
  }

  private void skipOrAppend(ContextElement ce) {
    if (ce != null && ce.getType() != null) {
      if (ce.getType().equals(ContextElement.TYPE_BACK)) {
        if (!contextList.isEmpty()) {
          ContextElement prevElem = contextList.get(contextList.size() - 1);
          if (prevElem.getType().equals(ContextElement.TYPE_BACK)) {
            contextList.add(ce);
          } else {
            contextList.remove(contextList.size() - 1);
            lookupMap.remove(prevElem.getType());
          }
        } else {
          contextList.add(ce);
        }
      } else {
        Integer index = lookupMap.get(ce.getType());
        if (index == null) {
          contextList.add(ce);
          lookupMap.put(ce.getType(), contextList.size() - 1);
        }
      }
    }
  }

  /**
   * Useful when parsing a context-string which could be relative (and include ../)
   *
   * @param ce
   */
  protected void overwriteOrAppend(ContextElement ce) {
    if (ce != null && ce.getType() != null) {
      if (ce.getType().equals(ContextElement.TYPE_BACK)) {
        if (!contextList.isEmpty()) {
          ContextElement prevElem = contextList.get(contextList.size() - 1);
          if (prevElem.getType().equals(ContextElement.TYPE_BACK)) {
            contextList.add(ce);
          } else {
            contextList.remove(contextList.size() - 1);
            lookupMap.remove(prevElem.getType());
          }
        } else {
          contextList.add(ce);
        }
      } else {
        Integer index = lookupMap.get(ce.getType());
        if (index != null) {
          contextList.remove((int) index);
          contextList.add(index, ce);
        } else {
          contextList.add(ce);
          lookupMap.put(ce.getType(), contextList.size() - 1);
        }
      }
    }
  }

  public List<ContextElement> getContextList() {
    return contextList;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (ContextElement ce : contextList) {
      sb.append(ce);
    }
    return sb.toString();
  }
}
