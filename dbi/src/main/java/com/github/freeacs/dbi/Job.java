package com.github.freeacs.dbi;

import com.github.freeacs.dbi.JobFlag.JobServiceWindow;
import com.github.freeacs.dbi.util.MapWrapper;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class Job {
  private static final Pattern pattern = Pattern.compile("([ucant])(\\d+)/?(\\d*)");

  public static String ANY_UNIT_IN_GROUP = "ANY-UNIT-IN-GROUP";

  @Data
  public static class StopRule {
    public static final int ANY_FAILURE_TYPE = 0;
    public static final int CONFIRMED_FAILURE_TYPE = 1;
    public static final int UNCONFIRMED_FAILURE_TYPE = 2;
    public static final int COUNT_TYPE = 3;
    public static final int TIMEOUT_TYPE = 4;

    private Integer numberMax;

    private final long numberLimit;

    private int ruleType;

    private final String ruleStr;

    public StopRule(String ruleStr) {
      this.ruleStr = ruleStr;
      Matcher m = pattern.matcher(ruleStr);
      if (m.matches()) {
        String type = m.group(1);
        if ("u".equals(type)) {
          ruleType = UNCONFIRMED_FAILURE_TYPE;
        } else if ("c".equals(type)) {
          ruleType = CONFIRMED_FAILURE_TYPE;
        } else if ("a".equals(type)) {
          ruleType = ANY_FAILURE_TYPE;
        } else if ("n".equals(type)) {
          ruleType = COUNT_TYPE;
        } else if ("t".equals(type)) {
          ruleType = TIMEOUT_TYPE;
        }
        numberLimit = Integer.parseInt(m.group(2));
        String numberMaxStr = m.group(3);
        if (numberMaxStr != null && !numberMaxStr.trim().isEmpty()) {
          numberMax = Integer.parseInt(numberMaxStr);
          if (numberLimit >= numberMax) {
            throw new IllegalArgumentException(
                "The first number must be less than the second (rule: " + ruleStr + ")");
          }
          if (numberMax < 3 || numberMax > 10000) {
            throw new IllegalArgumentException(
                "The last number must be between 3 and 10000 (rule: " + ruleStr + ")");
          }
          if (numberLimit < 2) {
            throw new IllegalArgumentException(
                "The first number must be between 2 and 10000 (rule: " + ruleStr + ")");
          }
        } else if (numberLimit < 1) {
          throw new IllegalArgumentException(
              "The number must be greater than 0 (rule: " + ruleStr + ")");
        }
      } else {
        throw new IllegalArgumentException(
            "The rule " + ruleStr + " does not match the regexp pattern " + pattern);
      }
    }

    @Override
    public String toString() {
      return ruleStr;
    }
  }

  private Unittype unittype;
  private Integer id;
  private String name;
  private JobFlag flags;
  private String oldName;
  private String description;
  private Group group;
  private int unconfirmedTimeout;
  private String sRules;
  private List<StopRule> stopRules;
  private File file;
  private Job dependency;
  private Integer repeatCount;
  private Integer repeatInterval;

  /** The field is calculated based on other jobs. */
  private List<Job> children;

  /** Initial Job status. */
  private JobStatus status = JobStatus.READY;

  private Date startTimestamp;
  private Date endTimestamp;
  private Map<String, JobParameter> defaultParameters;
  private int completedNoFailures;
  private int completedHadFailures;
  private int confirmedFailed;
  private int unconfirmedFailed;

  /**
   * These counters are calculated each time rules are set, and they represents absolute stop rules
   * (not fractional rules).
   */
  private long timeoutTms = Long.MAX_VALUE;

  /**
   * -- GETTER --
   * Various GET/SET methods of non-persistent, run-time job-related fields.
   */
  private long maxFailureAny = Integer.MAX_VALUE;
  private long maxFailureConfirmed = Integer.MAX_VALUE;
  private long maxFailureUnconfirmed = Integer.MAX_VALUE;
  private long maxCount = Integer.MAX_VALUE;

  /**
   * The nextPII is calculated during JobLogic.checkNew to find which job to run first (or rather to
   * set the nextPII correctly to the CPE. This is not kept in the database, but set during the
   * session.
   */
  private Long nextPII;

  private boolean validateInput = true;

  public Job() {}

  public Job(
      Unittype unittype,
      String name,
      JobFlag flags,
      String description,
      Group group,
      int unconfirmedTimeout,
      String stopRules,
      File file,
      Job dependency,
      Integer repeatCount,
      Integer repeatInterval) {
    setUnittype(unittype);
    setName(name);
    setFlags(flags);
    setDescription(description);
    setGroup(group);
    setUnconfirmedTimeout(unconfirmedTimeout);
    setStopRules(stopRules);
    setFile(file);
    setDependency(dependency);
    setRepeatCount(repeatCount);
    setRepeatInterval(repeatInterval);
  }

  public void validate() {
    setUnittype(unittype);
    setName(name);
    setFlags(flags);
    setDescription(description);
    setGroup(group);
    setUnconfirmedTimeout(unconfirmedTimeout);
    setStopRules(stopRules);
    setFile(file);
    setDependency(dependency);
    setRepeatCount(repeatCount);
    setRepeatInterval(repeatInterval);
  }

  public List<StopRule> getStopRules() {
    if (stopRules == null) {
      stopRules = new ArrayList<>();
    }
    return stopRules;
  }

  public String getStopRulesSerialized() {
    return sRules;
  }

  public Integer getRepeatInterval() {
    if (repeatCount != null && repeatInterval == null) {
      return 86400;
    } // Make sure a default value is returned in case a repeat-counter exists
    return repeatInterval;
  }

  /** SET methods. */
  public void setUnittype(Unittype unittype) {
    if (unittype == null) {
      throw new IllegalArgumentException("Job unittype cannot be null");
    }
    this.unittype = unittype;
  }

  protected void setId(Integer id) {
    this.id = id;
  }

  public void setName(String n) {
    String name = n;
    if (name != null) {
      name = name.trim();
      this.oldName = this.name;
      this.name = name;
    }
  }

  public void setFlags(JobFlag flags) {
    if (validateInput && flags == null) {
      throw new IllegalArgumentException("Job Type/ServiceWindow cannot be null");
    }
    if (flags == null) {
      flags = new JobFlag(JobType.CONFIG, JobServiceWindow.REGULAR);
    }
    this.flags = flags;
  }

  public void setGroup(Group group) {
    if (validateInput && group == null) {
      throw new IllegalArgumentException("Job group cannot be null");
    }
    this.group = group;
  }

  public void setUnconfirmedTimeout(int unconfirmedTimeout) {
    if (validateInput && unconfirmedTimeout < 60) {
      throw new IllegalArgumentException("Cannot set unconfirmed timeout to less than 60 (sec)");
    }
    if (unconfirmedTimeout < 60) {
      unconfirmedTimeout = 60;
    }
    this.unconfirmedTimeout = unconfirmedTimeout;
  }

  private void setStopRules(List<StopRule> stopRules) {
    this.stopRules = stopRules;
  }

  public void setStopRules(String sRules) {
    List<StopRule> tmpList = new ArrayList<>();
    try {
      if (sRules != null) {
        String[] fRuleArr = sRules.split(",");
        long maxCount = Long.MAX_VALUE;
        long maxTimeoutTms = Long.MAX_VALUE;
        long maxFailureAny = Long.MAX_VALUE;
        long maxFailureConf = Long.MAX_VALUE;
        long maxFailureUnconf = Long.MAX_VALUE;

        for (String fRule : fRuleArr) {
          StopRule stopRule = new StopRule(fRule.trim());
          tmpList.add(stopRule);
          if (stopRule.getNumberMax() == null) {
            if (stopRule.getRuleType() == StopRule.COUNT_TYPE
                && stopRule.getNumberLimit() < maxCount) {
              maxCount = stopRule.getNumberLimit();
            } else if (stopRule.getRuleType() == StopRule.TIMEOUT_TYPE
                && stopRule.getNumberLimit() < maxTimeoutTms) {
              maxTimeoutTms = stopRule.getNumberLimit();
            } else if (stopRule.getRuleType() == StopRule.ANY_FAILURE_TYPE
                && stopRule.getNumberLimit() < maxFailureAny) {
              maxFailureAny = stopRule.getNumberLimit();
            } else if (stopRule.getRuleType() == StopRule.CONFIRMED_FAILURE_TYPE
                && stopRule.getNumberLimit() < maxFailureConf) {
              maxFailureConf = stopRule.getNumberLimit();
            } else if (stopRule.getRuleType() == StopRule.UNCONFIRMED_FAILURE_TYPE
                && stopRule.getNumberLimit() < maxFailureUnconf) {
              maxFailureUnconf = stopRule.getNumberLimit();
            }
          }
        }
        this.maxCount = maxCount;
        this.timeoutTms = maxTimeoutTms;
        this.maxFailureAny = maxFailureAny;
        this.maxFailureConfirmed = maxFailureConf;
        this.maxFailureUnconfirmed = maxFailureUnconf;
      }
    } catch (IllegalArgumentException iae) {
      if (validateInput) {
        throw iae;
      }
    }
    this.sRules = sRules;
    this.stopRules = tmpList;
  }

  public void setFile(File file) {
    if (validateInput) {
      if (getFlags().getType().requireFile() && file == null) {
        throw new IllegalArgumentException(
            "Job with jobtype " + getFlags().getType() + " requires a file (software/script)");
      } else if (!getFlags().getType().requireFile() && file != null) {
        throw new IllegalArgumentException(
            "Job with jobtype " + getFlags().getType() + " cannot specify a file");
      }
    }
    this.file = file;
  }

  public void setDependency(Job newdep) {
    if (dependency != null) {
      dependency.removeChild(this);
    }
    dependency = newdep;
    if (dependency != null) {
      dependency.addChild(this);
    }
  }

  public void setRepeatCount(Integer repeat) {
    if (validateInput && repeat != null && repeat < 0) {
      throw new IllegalArgumentException("Job Repeat Count cannot be less than 0");
    } else if (repeat != null && repeat < 0) {
      this.repeatCount = 0;
    } else {
      this.repeatCount = repeat;
    }
  }

  public void setRepeatInterval(Integer repeatInterval) {
    if (validateInput && repeatInterval != null && repeatInterval < 0) {
      throw new IllegalArgumentException("Job Repeat Interval cannot be less than 0");
    }
    if (repeatInterval != null && repeatInterval < 0) {
      repeatInterval = 86400;
    } // do not allow negative interval - set it default to 24h
    this.repeatInterval = repeatInterval;
  }

  public Map<String, JobParameter> getDefaultParameters() {
    if (defaultParameters == null) {
      MapWrapper<JobParameter> mw = new MapWrapper<>(ACS.isStrictOrder());
      defaultParameters = mw.getMap();
    }
    return defaultParameters;
  }

  protected void setDefaultParameters() {
    this.defaultParameters = null;
  }

  /** Job children manipulation and retrieval. */
  protected void addChild(Job child) {
    if (children == null) {
      children = new ArrayList<>();
    }
    if (!this.children.contains(child)) {
      this.children.add(child);
    }
  }

  protected void removeChild(Job child) {
    if (children != null) {
      children.remove(child);
    }
  }

  @SuppressWarnings("unused")
  public List<Job> getAllChildren() {
    return getAllChildrenRec(this);
  }

  private List<Job> getAllChildrenRec(Job j) {
    List<Job> groups = new ArrayList<>();
    for (Job childrenJob : j.getChildren()) {
      groups.add(childrenJob);
      groups.addAll(getAllChildrenRec(childrenJob));
    }
    return groups;
  }

  public List<Job> getChildren() {
    if (children == null) {
      children = new ArrayList<>();
    }
    return children;
  }

  protected void validateInput(boolean validateInput) {
    this.validateInput = validateInput;
  }
}
