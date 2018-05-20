package com.github.freeacs.core;

import com.github.freeacs.core.util.FractionStopRuleCounter;
import com.github.freeacs.dbi.Job;
import com.github.freeacs.dbi.Job.StopRule;
import com.github.freeacs.dbi.JobFlag;
import com.github.freeacs.dbi.JobFlag.JobServiceWindow;
import com.github.freeacs.dbi.JobFlag.JobType;
import com.github.freeacs.dbi.UnitJob;

import java.util.Random;

public class FailureRuleCounterTest {

	private static Random random = new Random();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Job j = new Job(null, "test", new JobFlag(JobType.CONFIG, JobServiceWindow.REGULAR), "test", null, 100, "a3/10", null, null, null, null);
		StopRule fr = j.getStopRules().get(0);
		FractionStopRuleCounter frc = new FractionStopRuleCounter(fr);

		for (int i = 0; i < 50; i++) {
			UnitJob uj = new UnitJob(i + "", j.getId());
			int randomNumber = random.nextInt(10);
			System.out.print(i + ": ");
			if (randomNumber >= 7) {
				System.out.print("ERROR-");
				uj.setConfirmedFailed(1);
				uj.setUnconfirmedFailed(0);
			} else {
				System.out.print("OK   -");
				uj.setConfirmedFailed(0);
				uj.setUnconfirmedFailed(0);
			}
			frc.addResult(uj);
			if (frc.ruleMatch()) {
				System.out.print("Rule match");
			}
			System.out.println("");
		}
		System.out.println("Fin");

		// TODO Auto-generated method stub

	}

}
