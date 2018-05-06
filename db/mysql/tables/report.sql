DROP TABLE IF EXISTS `report_unit`;
CREATE TABLE `report_unit` (
  `timestamp_` DATETIME NOT NULL,
  `period_type` INTEGER NOT NULL,
  `unit_type_name` VARCHAR(64) NOT NULL,
  `profile_name` VARCHAR(64) NOT NULL,
  `software_version` VARCHAR(64) NOT NULL,
  `status` VARCHAR(32) NOT NULL,
  `unit_count` INTEGER NOT NULL,
  PRIMARY KEY (`timestamp_`, `period_type`, `unit_type_name`, `profile_name`, `software_version`, `status`)
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;

DROP TABLE IF EXISTS `report_group`;
CREATE TABLE `report_group` (
  `timestamp_` DATETIME NOT NULL,
  `period_type` INTEGER NOT NULL,
  `unit_type_name` VARCHAR(64) NOT NULL,
  `group_name` VARCHAR(64) NOT NULL,
  `unit_count` INTEGER NOT NULL,
  PRIMARY KEY (`timestamp_`, `period_type`, `unit_type_name`, `group_name`)
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;

DROP TABLE IF EXISTS `report_job`;
CREATE TABLE `report_job` (
  `timestamp_` DATETIME NOT NULL,
  `period_type` INTEGER NOT NULL,
  `unit_type_name` VARCHAR(64) NOT NULL,
  `job_name` VARCHAR(64) NOT NULL,
  `group_name` VARCHAR(64) NOT NULL,
  `group_size` INTEGER NOT NULL,
  `completed` INTEGER NOT NULL,
  `confirmed_failed` INTEGER NOT NULL,
  `unconfirmed_failed` INTEGER NOT NULL,
  PRIMARY KEY (`timestamp_`, `period_type`, `unit_type_name`, `job_name`)
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;

DROP TABLE IF EXISTS `report_syslog`;
CREATE TABLE `report_syslog` (
  `timestamp_` DATETIME NOT NULL,
  `period_type` INTEGER NOT NULL,
  `unit_type_name` VARCHAR(64) NOT NULL,
  `profile_name` VARCHAR(64) NOT NULL,
  `severity` VARCHAR(16) NOT NULL,
  `syslog_event_id` INTEGER NOT NULL,
  `facility` VARCHAR(32) NOT NULL, 
  `unit_count` INTEGER NOT NULL,
  PRIMARY KEY (`timestamp_`, `period_type`, `unit_type_name`, `profile_name`, `severity`, `syslog_event_id`, `facility`)
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;

DROP TABLE IF EXISTS `report_prov`;
CREATE TABLE `report_prov` (
  `timestamp_` DATETIME NOT NULL,
  `period_type` INTEGER NOT NULL,
  `unit_type_name` VARCHAR(64) NOT NULL,
  `profile_name` VARCHAR(64) NOT NULL,
  `software_version` VARCHAR(64) NOT NULL,
  `prov_output` VARCHAR(16) NOT NULL,
  `ok_count` INTEGER,
  `rescheduled_count` INTEGER,
  `error_count` INTEGER,
  `missing_count` INTEGER,
  `session_length_avg` INTEGER,
  PRIMARY KEY (`timestamp_`, `period_type`, `unit_type_name`, `profile_name`, `software_version`, `prov_output`)
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;


DROP TABLE IF EXISTS `report_voip`;
CREATE TABLE `report_voip` (
  `timestamp_` DATETIME NOT NULL,
  `period_type` INTEGER NOT NULL,
  `unit_type_name` VARCHAR(64) NOT NULL,
  `profile_name` VARCHAR(64) NOT NULL,
  `software_version` VARCHAR(64) NOT NULL,
  `line` INTEGER NOT NULL,
  `mos_avg` INTEGER,
  `jitter_avg` INTEGER,
  `jitter_max` INTEGER,
  `percent_loss_avg` INTEGER,
  `call_length_avg` INTEGER,
  `call_length_total` INTEGER NOT NULL,
  `incoming_call_count` INTEGER NOT NULL,
  `outgoing_call_count` INTEGER NOT NULL,
  `outgoing_call_failed_count` INTEGER NOT NULL,
  `aborted_call_count` INTEGER NOT NULL,
  `no_sip_service_time` INTEGER NOT NULL,
  PRIMARY KEY (`timestamp_`, `period_type`, `unit_type_name`, `profile_name`, `software_version`, `line`)
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;

DROP TABLE IF EXISTS `report_voip_tr`;
CREATE TABLE `report_voip_tr` (
  `timestamp_` DATETIME NOT NULL,
  `period_type` INTEGER NOT NULL,
  `unit_type_name` VARCHAR(64) NOT NULL,
  `profile_name` VARCHAR(64) NOT NULL,
  `software_version` VARCHAR(64) NOT NULL,
  `line` VARCHAR(16) NOT NULL,
  `line_status` VARCHAR(64) NOT NULL, 
  `overruns_count` INTEGER NOT NULL,
  `underruns_count` INTEGER NOT NULL,
  `percent_loss_avg` INTEGER,
  `call_length_avg` INTEGER,
  `call_length_total` INTEGER NOT NULL,
  `incoming_call_count` INTEGER NOT NULL,
  `outgoing_call_count` INTEGER NOT NULL,
  `outgoing_call_failed_count` INTEGER NOT NULL,
  `aborted_call_count` INTEGER NOT NULL,
  `no_sip_service_time` INTEGER NOT NULL,
  PRIMARY KEY (`timestamp_`, `period_type`, `unit_type_name`, `profile_name`, `software_version`, `line`, `line_status`)
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;

DROP TABLE IF EXISTS `report_hw`;
CREATE TABLE `report_hw` (
  `timestamp_` DATETIME NOT NULL,
  `period_type` INTEGER NOT NULL,
  `unit_type_name` VARCHAR(64) NOT NULL,
  `profile_name` VARCHAR(64) NOT NULL,
  `software_version` VARCHAR(64) NOT NULL,
  `boot_count` INTEGER NOT NULL,
  `boot_watchdog_count` INTEGER NOT NULL,
  `boot_misc_count` INTEGER NOT NULL,
  `boot_power_count` INTEGER NOT NULL,
  `boot_reset_count` INTEGER NOT NULL,
  `boot_prov_count` INTEGER NOT NULL,
  `boot_prov_sw_count` INTEGER NOT NULL,
  `boot_prov_conf_count` INTEGER NOT NULL,
  `boot_prov_boot_count` INTEGER NOT NULL,
  `boot_user_count` INTEGER NOT NULL,
  `mem_heap_ddr_pool_avg` INTEGER,
  `mem_heap_ddr_current_avg` INTEGER,
  `mem_heap_ddr_low_avg` INTEGER,
  `mem_heap_ocm_pool_avg` INTEGER,
  `mem_heap_ocm_current_avg` INTEGER,
  `mem_heap_ocm_low_avg` INTEGER,
  `mem_np_ddr_pool_avg` INTEGER,
  `mem_np_ddr_current_avg` INTEGER,
  `mem_np_ddr_low_avg` INTEGER,
  `mem_np_ocm_pool_avg` INTEGER,
  `mem_np_ocm_current_avg` INTEGER,
  `mem_np_ocm_low_avg` INTEGER,
  `cpe_uptime_avg` INTEGER,
  PRIMARY KEY (`timestamp_`, `period_type`, `unit_type_name`, `profile_name`, `software_version`)
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;

DROP TABLE IF EXISTS `report_hw_tr`;
CREATE TABLE `report_hw_tr` (
  `timestamp_` DATETIME NOT NULL,
  `period_type` INTEGER NOT NULL,
  `unit_type_name` VARCHAR(64) NOT NULL,
  `profile_name` VARCHAR(64) NOT NULL,
  `software_version` VARCHAR(64) NOT NULL,
  `cpe_uptime_avg` INTEGER,
  `memory_total_avg` INTEGER,
  `memory_free_avg` INTEGER,
  `cpu_usage_avg` INTEGER,
  `process_count_avg` INTEGER,
  `temperature_now_avg` INTEGER,
  `temperature_max_avg` INTEGER,
  PRIMARY KEY (`timestamp_`, `period_type`, `unit_type_name`, `profile_name`, `software_version`)
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;

DROP TABLE IF EXISTS `report_gateway_tr`;
CREATE TABLE `report_gateway_tr` (
  `timestamp_` DATETIME NOT NULL,
  `period_type` INTEGER NOT NULL,
  `unit_type_name` VARCHAR(64) NOT NULL,
  `profile_name` VARCHAR(64) NOT NULL,
  `software_version` VARCHAR(64) NOT NULL,
  `ping_success_count_avg` INTEGER,
  `ping_failure_count_avg` INTEGER,
  `ping_response_time_avg` INTEGER,
  `download_speed_avg` INTEGER,
  `upload_speed_avg` INTEGER,
  `wan_uptime_avg` INTEGER,
  PRIMARY KEY (`timestamp_`, `period_type`, `unit_type_name`, `profile_name`, `software_version`)
)
ENGINE = innodb CHARACTER SET = latin1 COLLATE = latin1_general_ci;
