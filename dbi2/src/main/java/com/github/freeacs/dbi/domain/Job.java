package com.github.freeacs.dbi.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@With
@NoArgsConstructor
@AllArgsConstructor
public class Job {
    private UnitType unitType;
    private Integer id;
    private String name;
    private JobFlag flags;
    private String oldName;
    private String description;
    private Group group;
    private String sRules;
    private List<JobStopRule> stopRules;
    private File file;
    private Job dependency;
    private Integer repeatCount;
    private Integer repeatInterval;
    private List<Job> children;
    private JobStatus status = JobStatus.READY;
    private Date startTimestamp;
    private Date endTimestamp;
    private List<JobParameter> parameters;
    private int unconfirmedTimeout;
    private int completedNoFailures;
    private int completedHadFailures;
    private int confirmedFailed;
    private int unconfirmedFailed;
}
