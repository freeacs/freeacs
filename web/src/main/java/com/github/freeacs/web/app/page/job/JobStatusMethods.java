/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.freeacs.web.app.page.job;

import com.github.freeacs.dbi.JobStatus;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;

public class JobStatusMethods {
  private final GetNextAvailableStatusCodesMethod allowedStatusVerificator =
      new GetNextAvailableStatusCodesMethod();
  private final IsStatusFinishedMethod statusFinishedVerificator = new IsStatusFinishedMethod();
  private final IsStatusReadyMethod statusReadyVerificator = new IsStatusReadyMethod();
  private final GetStatusFromAcronymMethod statusFromAcronymConverter =
      new GetStatusFromAcronymMethod();
  private final ConvertStatusToAcronymMethod statusToAcronymConverter =
      new ConvertStatusToAcronymMethod();

  public TemplateMethodModel getNextAvailableStatusCodesMethod() {
    return allowedStatusVerificator;
  }

  public TemplateMethodModel getIsStatusFinishedMethod() {
    return statusFinishedVerificator;
  }

  public TemplateMethodModel getIsStatusReadyMethod() {
    return statusReadyVerificator;
  }

  public TemplateMethodModel getStatusFromAcronymMethod() {
    return statusFromAcronymConverter;
  }

  public TemplateMethodModel getStatusToAcronymMethod() {
    return statusToAcronymConverter;
  }

  private final class GetNextAvailableStatusCodesMethod implements TemplateMethodModel {
    public List<?> exec(@SuppressWarnings("rawtypes") List args) throws TemplateModelException {
      if (args.size() != 1) {
        throw new TemplateModelException("No status in argument list");
      }
      String status = (String) args.get(0);
      String[] statusArray = getNextAvailableStatusCodes(status);
      return Arrays.asList(statusArray);
    }
  }

  public String[] getNextAvailableStatusCodes(String status) {
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

  private final class IsStatusFinishedMethod implements TemplateMethodModel {
    public Boolean exec(@SuppressWarnings("rawtypes") List args) throws TemplateModelException {
      if (args.size() != 1) {
        throw new TemplateModelException("No job status in argument list");
      }
      return JobStatus.COMPLETED == JobStatus.valueOf((String) args.get(0));
    }
  }

  private final class IsStatusReadyMethod implements TemplateMethodModel {
    public Boolean exec(@SuppressWarnings("rawtypes") List args) throws TemplateModelException {
      if (args.size() != 1) {
        throw new TemplateModelException("No job status in argument list");
      }
      return JobStatus.READY == JobStatus.valueOf((String) args.get(0));
    }
  }

  private final class ConvertStatusToAcronymMethod implements TemplateMethodModel {
    public String exec(@SuppressWarnings("rawtypes") List args) throws TemplateModelException {
      if (args.size() != 1) {
        throw new TemplateModelException("No job status in argument list");
      }
      String jobStatus = (String) args.get(0);
      return getJobStatusToAcronym(jobStatus);
    }
  }

  public String getJobStatusToAcronym(String jobStatus) {
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

  private final class GetStatusFromAcronymMethod implements TemplateMethodModel {
    public String exec(@SuppressWarnings("rawtypes") List args) throws TemplateModelException {
      if (args.size() != 1) {
        throw new TemplateModelException("No acronym supplied in argument list");
      }
      String acronym = (String) args.get(0);
      return getJobStatusFromAcronym(acronym).toString();
    }
  }

  public JobStatus getJobStatusFromAcronym(String acronym) {
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
