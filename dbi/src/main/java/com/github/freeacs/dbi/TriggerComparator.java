package com.github.freeacs.dbi;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TriggerComparator implements Comparator<Trigger> {

	private boolean parentFirst = false;

	public TriggerComparator(boolean parentFirst) {
		this.parentFirst = parentFirst;
	}

	public int compare(Trigger t1, Trigger t2) {
		if (parentFirst)
			return -compareImpl(t1, t2);
		else
			return compareImpl(t1, t2);
	}

	private int compareImpl(Trigger t1, Trigger t2) {

		/* Find the top-most Trigger, and create a list of Triggers from j.parent() -> top (for both j1 and j2)*/
		Trigger tmp1 = t1;
		List<Integer> triggerIdList1 = new ArrayList<Integer>();
		while (tmp1.getParent() != null) {
			tmp1 = tmp1.getParent();
			triggerIdList1.add(tmp1.getId());
		}
		Trigger tmp2 = t2;
		List<Integer> triggerIdList2 = new ArrayList<Integer>();
		while (tmp2.getParent() != null) {
			tmp2 = tmp2.getParent();
			triggerIdList2.add(tmp2.getId());
		}

		/* If the top-most Trigger has different names, then make string compare on names.
		 * Else continue to further investigation.
		 */
		int result = tmp1.getName().compareTo(tmp2.getName());
		if (result > 0) {
//			System.out.println("TOP  DOWN : " + t1.getName() + " compared to " + t2.getName());
			return result;
		} else if (result < 0) {
//			System.out.println("TOP  UP   : " + t1.getName() + " compared to " + t2.getName());
			return result;
		}

		/* Find out, if any Trigger in the two lists match */
		Integer matchId = null;
		for (Integer x : triggerIdList1) {
			for (Integer y : triggerIdList2) {
				if (x == y)
					matchId = x;
			}
		}

		if (matchId != null) {
			/* If match, then check length of Trigger lists (length up to the top). If length differs, then
			 * compare based on length, the shortest list first. Else continue investigation.
			 */
			if (triggerIdList1.size() < triggerIdList2.size()) {
//				System.out.println("DEP  DOWN : " + t1.getName() + " compared to " + t2.getName());
				return 1;
			} else if (triggerIdList2.size() > triggerIdList1.size()) {
//				System.out.println("DEP  UP   : " + t1.getName() + " compared to " + t2.getName());
				return -1;
			}
		} else {
			/* No match, the Triggers are totally unrelated. The order assigned is therefore rather arbitrary */
			if (triggerIdList1.size() == 0) {
//				System.out.println("NULL DOWN : " + t1.getName() + " compared to " + t2.getName());
				return 1;
			} else {
//				System.out.println("NULL UP   : " + t1.getName() + " compared to " + t2.getName());
				return -1;
			}
		}

		/* The triggers are list of Triggers to the top are the same, possibly sibling Triggers. Regular
		 * string compare on the name decides the matter.
		 */
		result = t1.getName().compareTo(t2.getName());
		if (result >= 0) {
//			System.out.println("ALFA DOWN : " + t1.getName() + " compared to " + t2.getName());
			return result;
		} else {
//			System.out.println("ALFA UP   : " + t1.getName() + " compared to " + t2.getName());
			return result;
		}

	}
}
