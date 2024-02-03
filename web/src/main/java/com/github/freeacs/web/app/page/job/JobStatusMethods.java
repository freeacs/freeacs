/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.freeacs.web.app.page.job;

import com.github.freeacs.dbi.JobStatus;
import com.github.freeacs.common.freemarker.AbstractTemplateMethodModel;
import freemarker.template.TemplateModelException;
import java.util.Arrays;
import java.util.List;

import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;

@Getter
public class JobStatusMethods {
  private final GetNextAvailableStatusCodesMethod allowedStatusVerificator =
      new GetNextAvailableStatusCodesMethod();
  private final IsStatusFinishedMethod statusFinishedVerificator = new IsStatusFinishedMethod();
  private final IsStatusReadyMethod statusReadyVerificator = new IsStatusReadyMethod();
  private final GetStatusFromAcronymMethod statusFromAcronymConverter =
      new GetStatusFromAcronymMethod();
  private final ConvertStatusToAcronymMethod statusToAcronymConverter =
      new ConvertStatusToAcronymMethod();

  private static final class GetNextAvailableStatusCodesMethod implements AbstractTemplateMethodModel {
    public List<?> exec(List args) throws TemplateModelException {
      if (args.size() != 1) {
        throw new TemplateModelException("No status in argument list");
      }
      String status = (String) args.get(0);
      String[] statusArray = getNextAvailableStatusCodes(status);
      return Arrays.asList(statusArray);
    }
  }

  public static String[] getNextAvailableStatusCodes(String status) {
    if (JobStatus.READY.toString().equalsIgnoreCase(status)) {
      return new String[] {JobStatus.STARTED.toString()};
    } else if (JobStatus.PAUSED.toString().equalsIgnoreCase(status)) {
      return new String[] {JobStatus.STARTED.toString(), JobStatus.COMPLETED.toString()};
    } else if (JobStatus.STARTED.toString().equalsIgnoreCase(status)) {
      return new String[] {JobStatus.PAUSED.toString()};
    } else {
      return ArrayUtils.EMPTY_STRING_ARRAY;
    }
  }

  private static final class IsStatusFinishedMethod implements AbstractTemplateMethodModel {
    public Boolean exec(List args) throws TemplateModelException {
      if (args.size() != 1) {
        throw new TemplateModelException("No job status in argument list");
      }
      return JobStatus.COMPLETED == JobStatus.valueOf((String) args.get(0));
    }
  }

  private static final class IsStatusReadyMethod implements AbstractTemplateMethodModel {
    public Boolean exec(List args) throws TemplateModelException {
      if (args.size() != 1) {
        throw new TemplateModelException("No job status in argument list");
      }
      return JobStatus.READY == JobStatus.valueOf((String) args.get(0));
    }
  }

  private static final class ConvertStatusToAcronymMethod implements AbstractTemplateMethodModel {
    public String exec(List args) throws TemplateModelException {
      if (args.size() != 1) {
        throw new TemplateModelException("No job status in argument list");
      }
      String jobStatus = (String) args.get(0);
      return getJobStatusToAcronym(jobStatus);
    }
  }

  public static String getJobStatusToAcronym(String jobStatus) {
    if (JobStatus.STARTED.toString().equalsIgnoreCase(jobStatus)) {
      return "START";
    } else if (JobStatus.PAUSED.toString().equalsIgnoreCase(jobStatus)) {
      return "PAUSE";
    } else if (JobStatus.COMPLETED.toString().equalsIgnoreCase(jobStatus)) {
      return "FINISH";
    } else {
      return jobStatus;
    }
  }

  private static final class GetStatusFromAcronymMethod implements AbstractTemplateMethodModel {
    public String exec(List args) throws TemplateModelException {
      if (args.size() != 1) {
        throw new TemplateModelException("No acronym supplied in argument list");
      }
      String acronym = (String) args.get(0);
      return getJobStatusFromAcronym(acronym).toString();
    }
  }

  public static JobStatus getJobStatusFromAcronym(String acronym) {
    if ("START".equalsIgnoreCase(acronym)) {
      return JobStatus.STARTED;
    } else if ("PAUSE".equalsIgnoreCase(acronym)) {
      return JobStatus.PAUSED;
    } else if ("FINISH".equalsIgnoreCase(acronym)) {
      return JobStatus.COMPLETED;
    } else {
      return null;
    }
  }
}
