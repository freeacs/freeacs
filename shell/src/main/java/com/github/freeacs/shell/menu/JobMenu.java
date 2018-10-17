package com.github.freeacs.shell.menu;

import com.github.freeacs.dbi.Job;
import com.github.freeacs.dbi.JobParameter;
import com.github.freeacs.dbi.JobStatus;
import com.github.freeacs.dbi.Parameter;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.UnitJob;
import com.github.freeacs.dbi.UnitJobs;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import com.github.freeacs.shell.Context;
import com.github.freeacs.shell.Session;
import com.github.freeacs.shell.output.Line;
import com.github.freeacs.shell.output.Listing;
import com.github.freeacs.shell.output.OutputHandler;
import com.github.freeacs.shell.util.Validation;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class JobMenu {
  private Session session;
  private Context context;
  private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");

  public JobMenu(Session session) {
    this.session = session;
    this.context = session.getContext();
  }

  /**
   * Returns true : has processed a cd-command Returns false : has processed another command
   * (everything else) help += "\tlistdetails\n"; help += "\tlistparams\n"; help += "\tsetprofile
   * <profilename>\n"; help += "\tstart\n"; help += "\tstop\n"; help += "\tabort\n"; help +=
   * "\tsetparam ALL|<unitid> <unittype-parameter-name> <value>\n"; help += "\tdelparam ALL|<unitid>
   * <unittype-parameter-name>\n";
   */
  public boolean execute(String[] inputArr, OutputHandler oh) throws Exception {
    if (inputArr[0].equals(JobStatus.COMPLETED.toString())) {
      changeStatus(JobStatus.STARTED);
      changeStatus(JobStatus.PAUSED);
      changeStatus(JobStatus.COMPLETED);
    } else if (inputArr[0].startsWith("dela")) {
      delallparams(inputArr);
    } else if (inputArr[0].startsWith("delf")) {
      delfailedunits(inputArr);
    } else if (inputArr[0].startsWith("delp")) {
      delparam(inputArr);
    } else if (inputArr[0].startsWith("fini")) {
      changeStatus(JobStatus.COMPLETED);
    } else if (inputArr[0].startsWith("listd")) {
      listdetails(inputArr, oh);
    } else if (inputArr[0].startsWith("listf")) {
      listfailedunits(inputArr, oh);
    } else if (inputArr[0].startsWith("listp")) {
      listparams(inputArr, oh);
    } else if (inputArr[0].equals(JobStatus.READY.toString())) {
      session.println(
          "Assume command ("
              + inputArr[0]
              + ") is run as part of import, job is already in READY state");
    } else if (inputArr[0].startsWith("ref")) {
      refresh();
    } else if (inputArr[0].startsWith("setf")) {
      setfailedunits(inputArr);
    } else if (inputArr[0].startsWith("setp")) {
      setparam(inputArr);
    } else if (inputArr[0].toLowerCase().startsWith("star")) {
      changeStatus(JobStatus.STARTED);
    } else if (inputArr[0].startsWith("stat")) {
      status(inputArr, oh);
    } else if ("stop".equals(inputArr[0]) || "pause".equals(inputArr[0])) {
      changeStatus(JobStatus.PAUSED);
    } else if (inputArr[0].equals(JobStatus.PAUSED.toString())) {
      changeStatus(JobStatus.STARTED);
      changeStatus(JobStatus.PAUSED);
    } else {
      throw new IllegalArgumentException("The command " + inputArr[0] + " was not recognized.");
    }
    return false;
  }

  private void refresh() throws Exception {
    Job[] jobs = context.getUnittype().getJobs().getJobs();
    Job newJob = null;
    for (Job j : jobs) {
      if (j.getId().equals(context.getJob().getId())) {
        newJob = j;
        break;
      }
    }
    context.setJob(newJob);
  }

  private void changeStatus(JobStatus status) throws Exception {
    refresh();
    context.getJob().setStatus(status);
    context.getUnittype().getJobs().changeStatus(context.getJob(), session.getAcs());
    session.println("Status changed to " + status);
    refresh();
  }

  private void listparams(String[] inputArr, OutputHandler oh) throws Exception {
    refresh();
    Map<String, JobParameter> params = null;
    Listing listing = oh.getListing();
    listing.setHeading("Unit-id", "Unit Type Parameter Name", "Value");
    if (inputArr.length > 1) {
      String unitId = inputArr[1];
      if ("DEFAULT".equals(unitId)) {
        params = context.getJob().getDefaultParameters();
      } else {
        params =
            context
                .getUnittype()
                .getJobs()
                .readJobParameters(
                    context.getJob(), new Unit(unitId, null, null), session.getAcs());
      }
    } else {
      params =
          context
              .getUnittype()
              .getJobs()
              .readJobParameters(context.getJob(), null, session.getAcs());
    }
    for (Entry<String, JobParameter> entry : params.entrySet()) {
      Line line = new Line();
      String unitId = entry.getValue().getUnitId();
      if (unitId.equals(Job.ANY_UNIT_IN_GROUP)) {
        line.addValue("DEFAULT");
      } else {
        line.addValue(unitId);
      }
      line.addValue(entry.getKey());
      line.addValue(entry.getValue().getParameter().getValue());
      listing.addLine(line);
    }
    //		listing.printListing(oh);
  }

  private void delparam(String[] inputArr) throws Exception {
    Validation.numberOfArgs(inputArr, 3);
    List<JobParameter> toBeDeleted = new ArrayList<>();

    String unitId = inputArr[1];
    if ("DEFAULT".equals(inputArr[1])) {
      unitId = Job.ANY_UNIT_IN_GROUP;
    }
    Job job = context.getJob();
    Unittype unittype = job.getGroup().getUnittype();
    UnittypeParameter utp = unittype.getUnittypeParameters().getByName(inputArr[2]);
    if (utp == null) {
      session.println(
          "["
              + session.getCounter()
              + "] Job parameter name "
              + inputArr[2]
              + " is not a valid unittype parameter name");
      return;
    }
    JobParameter jp = new JobParameter(job, unitId, new Parameter(utp, "dummy"));
    toBeDeleted.add(jp);
    int rowsDeleted =
        context.getUnittype().getJobs().deleteJobParameters(toBeDeleted, session.getAcs());
    if (rowsDeleted >= 1) {
      session.println(
          "[" + session.getCounter() + "] Job parameter " + inputArr[2] + " was deleted");
    } else {
      session.println(
          "[" + session.getCounter() + "] Job parameter " + inputArr[2] + " was not found");
    }
    session.incCounter();
  }

  private void setparam(String[] inputArr) throws Exception {
    Validation.numberOfArgs(inputArr, 4);
    Job job = context.getJob();
    Unittype unittype = job.getGroup().getUnittype();

    String unitId = inputArr[1];
    if ("DEFAULT".equals(unitId)) {
      unitId = Job.ANY_UNIT_IN_GROUP;
    }

    UnittypeParameter utp = unittype.getUnittypeParameters().getByName(inputArr[2]);
    if (utp != null) {
      JobParameter p = new JobParameter(job, unitId, new Parameter(utp, inputArr[3]));
      List<JobParameter> toBeAdded = new ArrayList<>();
      toBeAdded.add(p);
      context.getUnittype().getJobs().addOrChangeJobParameters(toBeAdded, session.getAcs());
      session.println(
          "[" + session.getCounter() + "] Job parameter " + inputArr[2] + " was added/changed");
    } else {
      session.println("[" + session.getCounter() + "] Wrong unittype parameter name");
    }
    session.incCounter();
  }

  private void delfailedunits(String[] inputArr) throws Exception {
    Job job = context.getJob();
    UnitJobs unitJobs = session.getUnitJobs();
    unitJobs.delete(job);
    session.println("[" + session.getCounter() + "] All failed unit jobs was deleted for this job");
    session.incCounter();
  }

  /**
   * Setfailedunits <unitid> <status> <start-tms>|NULL <end-tms>|NULL <unconfirmedfailed>
   * <confirmedfailed>.
   */
  private void setfailedunits(String[] inputArr) throws Exception {
    Job job = context.getJob();
    UnitJobs unitJobs = session.getUnitJobs();
    Validation.numberOfArgs(inputArr, 7);
    UnitJob uj = new UnitJob(inputArr[1], job.getId());
    uj.setProcessed(false);
    uj.setStatus(inputArr[2]);
    if (!"NULL".equals(inputArr[3])) {
      uj.setStartTimestamp(sdf.parse(inputArr[3]));
    }
    if (!"NULL".equals(inputArr[4])) {
      uj.setEndTimestamp(sdf.parse(inputArr[4]));
    }
    uj.setUnconfirmedFailed(Integer.parseInt(inputArr[5]));
    uj.setConfirmedFailed(Integer.parseInt(inputArr[6]));
    unitJobs.addOrChange(uj);
    session.println("[" + session.getCounter() + "] A failed unit-job was added/changed");
    session.incCounter();
  }

  private void listfailedunits(String[] inputArr, OutputHandler oh) throws Exception {
    Job job = context.getJob();
    List<UnitJob> unitJobs = session.getUnitJobs().readAllProcessed(job);
    Listing listing = oh.getListing();
    listing.setHeading("Unitid", "Status", "Start-Tms", "End-Tms", "UF", "CF");
    for (UnitJob uj : unitJobs) {
      Line line = new Line(uj.getUnitId(), uj.getStatus());
      if (uj.getStartTimestamp() != null) {
        line.addValue(sdf.format(uj.getStartTimestamp()));
      } else {
        line.addValue("NULL");
      }
      if (uj.getEndTimestamp() != null) {
        line.addValue(sdf.format(uj.getEndTimestamp()));
      } else {
        line.addValue("NULL");
      }
      line.addValue(String.valueOf(uj.getUnconfirmedFailed()));
      line.addValue(String.valueOf(uj.getConfirmedFailed()));
      listing.addLine(line);
    }
  }

  private void status(String[] inputArr, OutputHandler oh) throws Exception {
    refresh();
    Job job = context.getJob();
    Listing listing = oh.getListing();
    listing.setHeading("Status");
    listing.addLine(job.getStatus().toString());
  }

  private void listdetails(String[] inputArr, OutputHandler oh) throws Exception {
    refresh();
    Job job = context.getJob();
    oh.print("                  Id : " + job.getId() + "\n");
    oh.print("                Name : " + job.getName() + "\n");
    oh.print("         Description : " + job.getDescription() + "\n");
    oh.print("          Stop rules : " + job.getStopRulesSerialized() + "\n");
    oh.print("               Group : " + job.getGroup().getName() + "\n");
    if (job.getFile() != null) {
      oh.print("            Software : " + job.getFile().getNameAndVersion() + "\n");
    } else {
      oh.print("            Software : NULL\n");
    }
    if (job.getDependency() != null) {
      oh.print("          Dependency : " + job.getDependency().getName() + "\n");
    } else {
      oh.print("          Dependency : NULL\n");
    }
    if (!job.getChildren().isEmpty()) {
      oh.print("        Job children : ");
      for (Job child : job.getChildren()) {
        oh.print(child.getName() + ", ");
      }
      oh.print("\n");
    } else {
      oh.print("        Job children : No children\n");
    }

    oh.print(" Unconfirmed timeout : " + job.getUnconfirmedTimeout() + "\n");
    if (job.getTimeoutTms() < Long.MAX_VALUE) {
      oh.print("         Job timeout : " + new Date(job.getTimeoutTms()) + "\n");
    } else {
      oh.print("         Job timeout : Infinite\n");
    }
    oh.print("              Status : " + job.getStatus() + "\n");
    oh.print(
        "                  OK : "
            + (job.getCompletedNoFailures() + job.getCompletedHadFailures())
            + "\n");
    oh.print("  Unconfirmed failed : " + job.getUnconfirmedFailed() + "\n");
    oh.print("    Confirmed failed : " + job.getConfirmedFailed() + "\n");
    if (job.getStartTimestamp() != null) {
      oh.print("     Start-timestamp : " + job.getStartTimestamp() + "\n");
    } else {
      oh.print("     Start-timestamp : Not started\n");
    }
    if (job.getEndTimestamp() != null) {
      oh.print("       End-timestamp : " + job.getEndTimestamp() + "\n");
    } else {
      oh.print("       End-timestamp : Not aborted nor completed\n");
    }
    //		if (job.getMoveToProfile() != null)
    //			oh.print("     Move-to-profile : " + job.getMoveToProfile().getName() + "\n");
    //		else
    //			oh.print("     Move-to-profile : N/A\n");
    if (job.getRepeatCount() != null) {
      oh.print("              Repeat : " + job.getRepeatCount() + " times\n");
      oh.print("            Interval : " + job.getRepeatInterval() + " seconds\n");
    } else {
      oh.print("              Repeat : 0 times\n");
      oh.print("            Interval : N/A\n");
    }
    for (JobParameter jp : job.getDefaultParameters().values()) {
      oh.print("   Default parameter : ");
      oh.print(String.format("%-30s = ", jp.getParameter().getUnittypeParameter().getName()));
      oh.print(jp.getParameter().getValue() + "\n");
    }
  }

  private void delallparams(String[] inputArr) throws Exception {
    session.println("[" + session.getCounter() + "] Job parameters were deleted");
    context.getUnittype().getJobs().deleteJobParameters(context.getJob(), session.getAcs());
    session.incCounter();
  }
}
