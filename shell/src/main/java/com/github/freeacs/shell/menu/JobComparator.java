package com.github.freeacs.shell.menu;

import com.github.freeacs.dbi.Job;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class JobComparator implements Comparator<Job> {
  private boolean dependencyFirst = true;

  public JobComparator(boolean dependencyFirst) {
    this.dependencyFirst = dependencyFirst;
  }

  public int compare(Job j1, Job j2) {
    if (dependencyFirst) {
      return -compareImpl(j1, j2);
    } else {
      return compareImpl(j1, j2);
    }
  }

  private int compareImpl(Job j1, Job j2) {
    /* Find the top-most job, and create a list of jobs from j.parent() -> top (for both j1 and j2)*/
    Job tmp1 = j1;
    List<Integer> jobIdList1 = new ArrayList<>();
    jobIdList1.add(tmp1.getId());
    while (tmp1.getDependency() != null) {
      tmp1 = tmp1.getDependency();
      jobIdList1.add(tmp1.getId());
    }
    Job tmp2 = j2;
    List<Integer> jobIdList2 = new ArrayList<>();
    jobIdList2.add(tmp2.getId());
    while (tmp2.getDependency() != null) {
      tmp2 = tmp2.getDependency();
      jobIdList2.add(tmp2.getId());
    }

    /* If the top-most job has different names, then make string compare on names.
     * Else continue to further investigation.
     */
    int result = tmp1.getName().compareTo(tmp2.getName());
    if (result > 0 || result < 0) {
      //			System.out.println("TOP  DOWN : " + j1.getName() + " compared to " + j2.getName());
      return result;
    }

    /* Find out, if any job in the two lists match */
    Integer matchId = null;
    for (Integer x : jobIdList1) {
      for (Integer y : jobIdList2) {
        if (x == y) {
          matchId = x;
        }
      }
    }

    if (matchId != null) {
      /* If match, then check length of job lists (length up to the top). If length differs, then
       * compare based on length, the shortest list first. Else continue investigation.
       */
      if (jobIdList1.size() > jobIdList2.size()) {
        //				System.out.println("DEP  DOWN : " + j1.getName() + " compared to " + j2.getName());
        return 1;
      } else if (jobIdList2.size() > jobIdList1.size()) {
        //				System.out.println("DEP  UP   : " + j1.getName() + " compared to " + j2.getName());
        return -1;
      }
    } else /* No match, the jobs are totally unrelated. The order assigned is therefore rather arbitrary */ if (jobIdList1
        .isEmpty()) {
      //				System.out.println("NULL DOWN : " + j1.getName() + " compared to " + j2.getName());
      return 1;
    } else {
      //				System.out.println("NULL UP   : " + j1.getName() + " compared to " + j2.getName());
      return -1;
    }

    return j1.getName().compareTo(j2.getName());
  }
}
