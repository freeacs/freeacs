package com.github.freeacs.shell.menu;

import com.github.freeacs.dbi.Group;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GroupComparator implements Comparator<Group> {
  private boolean parentFirst;

  public GroupComparator(boolean parentFirst) {
    this.parentFirst = parentFirst;
  }

  public int compare(Group g1, Group g2) {
    if (parentFirst) {
      return -compareImpl(g1, g2);
    } else {
      return compareImpl(g1, g2);
    }
  }

  private int compareImpl(Group g1, Group g2) {
    /* Find the top-most Group, and create a list of Groups from j.parent() -> top (for both j1 and j2)*/
    Group tmp1 = g1;
    List<Integer> groupIdList1 = new ArrayList<>();
    while (tmp1.getParent() != null) {
      tmp1 = tmp1.getParent();
      groupIdList1.add(tmp1.getId());
    }
    Group tmp2 = g2;
    List<Integer> groupIdList2 = new ArrayList<>();
    while (tmp2.getParent() != null) {
      tmp2 = tmp2.getParent();
      groupIdList2.add(tmp2.getId());
    }

    /* If the top-most Group has different names, then make string compare on names.
     * Else continue to further investigation.
     */
    int result = tmp1.getName().compareTo(tmp2.getName());
    if (result > 0 || result < 0) {
      //			System.out.println("TOP  DOWN : " + g1.getName() + " compared to " + g2.getName());
      return result;
    }

    /* Find out, if any Group in the two lists match */
    Integer matchId = null;
    for (Integer x : groupIdList1) {
      for (Integer y : groupIdList2) {
        if (x == y) {
          matchId = x;
        }
      }
    }

    if (matchId != null) {
      /* If match, then check length of Group lists (length up to the top). If length differs, then
       * compare based on length, the shortest list first. Else continue investigation.
       */
      if (groupIdList1.size() < groupIdList2.size()) {
        //				System.out.println("DEP  DOWN : " + g1.getName() + " compared to " + g2.getName());
        return 1;
      } else if (groupIdList2.size() > groupIdList1.size()) {
        //				System.out.println("DEP  UP   : " + g1.getName() + " compared to " + g2.getName());
        return -1;
      }
    } else /* No match, the Groups are totally unrelated. The order assigned is therefore rather arbitrary */ if (groupIdList1
        .isEmpty()) {
      //				System.out.println("NULL DOWN : " + g1.getName() + " compared to " + g2.getName());
      return 1;
    } else {
      //				System.out.println("NULL UP   : " + g1.getName() + " compared to " + g2.getName());
      return -1;
    }

    return g1.getName().compareTo(g2.getName());
  }
}
