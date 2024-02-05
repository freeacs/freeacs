DROP TABLE IF EXISTS unit_type;
CREATE TABLE unit_type
(
  unit_type_id   SERIAL PRIMARY KEY,
  matcher_id     VARCHAR(32),
  unit_type_name VARCHAR(64) NOT NULL,
  vendor_name    VARCHAR(64),
  description    VARCHAR(2000),
  protocol       VARCHAR(16) NOT NULL,
  UNIQUE (unit_type_name)
);

DROP TABLE IF EXISTS unit_type_param;
CREATE TABLE unit_type_param
(
  unit_type_param_id SERIAL PRIMARY KEY,
  unit_type_id       INTEGER NOT NULL,
  name               VARCHAR(255) NOT NULL,
  flags              VARCHAR(32) NOT NULL
);
CREATE UNIQUE INDEX idx_u_t_p_unit_type_id_name ON unit_type_param (unit_type_id, name);
ALTER TABLE unit_type_param ADD CONSTRAINT fk_u_t_p_unit_type_id FOREIGN KEY (unit_type_id)
    REFERENCES unit_type (unit_type_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;

DROP TABLE IF EXISTS unit_type_param_value;
CREATE TABLE unit_type_param_value
(
  unit_type_param_id INTEGER NOT NULL,
  value VARCHAR(255) NOT NULL,
  priority INTEGER NOT NULL,
  type VARCHAR(32) NOT NULL DEFAULT 'enum',
  PRIMARY KEY (unit_type_param_id, value),
  CONSTRAINT fk_unit_param_value_utpid FOREIGN KEY (unit_type_param_id)
    REFERENCES unit_type_param (unit_type_param_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

DROP TABLE IF EXISTS profile;
CREATE TABLE profile
(
  profile_id SERIAL PRIMARY KEY,
  unit_type_id INTEGER NOT NULL,
  profile_name VARCHAR(64) NOT NULL,
  UNIQUE (unit_type_id, profile_name),
  CONSTRAINT fk_profile_unit_type_id FOREIGN KEY (unit_type_id)
    REFERENCES unit_type (unit_type_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

DROP TABLE IF EXISTS profile_param;
CREATE TABLE profile_param
(
  profile_id         INTEGER NOT NULL,
  unit_type_param_id INTEGER NOT NULL,
  value              VARCHAR(255) NULL,
  PRIMARY KEY (profile_id, unit_type_param_id),
  CONSTRAINT fk_profile_param_profile_id FOREIGN KEY (profile_id)
    REFERENCES profile (profile_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_profile_param_u_t_p_id FOREIGN KEY (unit_type_param_id)
    REFERENCES unit_type_param (unit_type_param_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

DROP TABLE IF EXISTS unit;
CREATE TABLE unit
(
  unit_id      VARCHAR(64) NOT NULL,
  unit_type_id INTEGER     NOT NULL,
  profile_id   INTEGER     NOT NULL,
  PRIMARY KEY (unit_id),
  FOREIGN KEY (unit_type_id)
    REFERENCES unit_type (unit_type_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  FOREIGN KEY (profile_id)
    REFERENCES profile (profile_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);
CREATE INDEX idx_unit_unit_type_profile ON unit (unit_type_id, profile_id, unit_id);
CREATE INDEX idx_unit_profile_unit_type ON unit (profile_id, unit_type_id, unit_id);

DROP TABLE IF EXISTS unit_param;
CREATE TABLE unit_param
(
  unit_id            VARCHAR(64) NOT NULL,
  unit_type_param_id INTEGER     NOT NULL,
  value              VARCHAR(512) NULL,
  PRIMARY KEY (unit_id, unit_type_param_id),
  FOREIGN KEY (unit_id)
    REFERENCES unit (unit_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  FOREIGN KEY (unit_type_param_id)
    REFERENCES unit_type_param (unit_type_param_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);
CREATE INDEX idx_unit_param_type_id2 ON unit_param (unit_type_param_id, value);
CREATE INDEX idx_unit_param_value ON unit_param (value);

DROP TABLE IF EXISTS unit_param_session;
CREATE TABLE unit_param_session
(
  unit_id            VARCHAR(64) NOT NULL,
  unit_type_param_id INTEGER     NOT NULL,
  value              VARCHAR(512) NULL,
  PRIMARY KEY (unit_id, unit_type_param_id),
  FOREIGN KEY (unit_id)
    REFERENCES unit (unit_id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  FOREIGN KEY (unit_type_param_id)
    REFERENCES unit_type_param (unit_type_param_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

DROP TABLE IF EXISTS group_;
CREATE TABLE group_
(
  group_id          SERIAL PRIMARY KEY,
  unit_type_id      INTEGER NOT NULL,
  group_name        VARCHAR(64) NOT NULL,
  description       VARCHAR(2000) NULL,
  parent_group_id   INTEGER NULL,
  profile_id        INTEGER NULL,
  count             INTEGER NULL,
  time_param_id     INTEGER NULL,
  time_rolling_rule VARCHAR(32) NULL,
  CONSTRAINT fk_group__unit_type_id FOREIGN KEY (unit_type_id)
    REFERENCES unit_type (unit_type_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_group__group_id FOREIGN KEY (parent_group_id)
    REFERENCES group_ (group_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_group__profile_id FOREIGN KEY (profile_id)
    REFERENCES profile (profile_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_time_param_u_t_p_id FOREIGN KEY (time_param_id)
    REFERENCES unit_type_param (unit_type_param_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

DROP TABLE IF EXISTS group_param;
CREATE TABLE group_param
(
  id                 SERIAL PRIMARY KEY,
  group_id           INTEGER     NOT NULL,
  unit_type_param_id INTEGER     NOT NULL,
  operator           VARCHAR(2)  NOT NULL DEFAULT '=',
  data_type          VARCHAR(32) NOT NULL DEFAULT 'TEXT',
  value              VARCHAR(255) NULL,
  CONSTRAINT fk_group_param_group_id FOREIGN KEY (group_id)
    REFERENCES group_ (group_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_group_param_u_t_p_id FOREIGN KEY (unit_type_param_id)
    REFERENCES unit_type_param (unit_type_param_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

DROP TABLE IF EXISTS user_;
CREATE TABLE user_
(
  id         SERIAL PRIMARY KEY,
  username   VARCHAR(64) NOT NULL,
  secret     VARCHAR(64) NOT NULL,
  fullname   VARCHAR(64) NOT NULL,
  accesslist VARCHAR(256) NOT NULL,
  is_admin   INTEGER NOT NULL DEFAULT 0,
  UNIQUE (username)
);

DROP TABLE IF EXISTS permission_;
CREATE TABLE permission_
(
  id           SERIAL PRIMARY KEY,
  user_id      INTEGER NOT NULL,
  unit_type_id INTEGER NOT NULL,
  profile_id   INTEGER NULL,
  FOREIGN KEY (user_id)
    REFERENCES user_ (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  FOREIGN KEY (unit_type_id)
    REFERENCES unit_type (unit_type_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

DROP TABLE IF EXISTS filestore;
CREATE TABLE filestore
(
  id           SERIAL PRIMARY KEY,
  name         VARCHAR(64) NOT NULL,
  unit_type_id INTEGER NOT NULL,
  type         VARCHAR(64) NOT NULL DEFAULT 'SOFTWARE',
  description  VARCHAR(2000) NULL,
  version      VARCHAR(64) NOT NULL,
  content      BYTEA NOT NULL,
  timestamp_   TIMESTAMP NOT NULL,
  target_name  VARCHAR(128) NULL,
  owner        INTEGER NULL,
  FOREIGN KEY (unit_type_id)
    REFERENCES unit_type (unit_type_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  FOREIGN KEY (owner)
    REFERENCES user_ (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

DROP TABLE IF EXISTS syslog_event;
CREATE TABLE syslog_event
(
  id                SERIAL PRIMARY KEY,
  syslog_event_id   INTEGER NOT NULL,
  syslog_event_name VARCHAR(64) NOT NULL,
  unit_type_id      INTEGER NOT NULL,
  group_id          INTEGER NULL,
  expression        VARCHAR(64) NOT NULL DEFAULT 'Specify an expression',
  store_policy      VARCHAR(16) NOT NULL DEFAULT 'STORE',
  filestore_id      INTEGER NULL,
  description       VARCHAR(1024) NULL,
  delete_limit      INTEGER NULL,
  CONSTRAINT fk_syslog_event_unit_type_id FOREIGN KEY (unit_type_id)
    REFERENCES unit_type (unit_type_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_syslog_event_group_id FOREIGN KEY (group_id)
    REFERENCES group_ (group_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_syslog_event_filestore_id FOREIGN KEY (filestore_id)
    REFERENCES filestore (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  UNIQUE (syslog_event_id, unit_type_id)
);

DROP TABLE IF EXISTS job;
CREATE TABLE job
(
  job_id                SERIAL PRIMARY KEY,
  job_name              VARCHAR(64) NOT NULL,
  job_type              VARCHAR(32) NOT NULL,
  description           VARCHAR(2000) NULL,
  group_id              INTEGER NOT NULL,
  unconfirmed_timeout   INTEGER NOT NULL,
  stop_rules            VARCHAR(255) NULL,
  status                VARCHAR(32) NOT NULL,
  completed_no_failure  INTEGER NOT NULL,
  completed_had_failure INTEGER NOT NULL,
  confirmed_failed      INTEGER NOT NULL,
  unconfirmed_failed    INTEGER NOT NULL,
  start_timestamp       TIMESTAMP NULL,
  end_timestamp         TIMESTAMP NULL,
  firmware_id           INTEGER NULL,
  job_id_dependency     INTEGER NULL,
  profile_id            INTEGER NULL,
  repeat_count          INTEGER NULL,
  repeat_interval       INTEGER NULL,
  FOREIGN KEY (group_id)
    REFERENCES group_ (group_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  FOREIGN KEY (firmware_id)
    REFERENCES filestore (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  FOREIGN KEY (job_id_dependency)
    REFERENCES job (job_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  FOREIGN KEY (profile_id)
    REFERENCES profile (profile_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

DROP TABLE IF EXISTS job_param;
CREATE TABLE job_param
(
  job_id             INTEGER NOT NULL,
  unit_id            VARCHAR(64) NOT NULL,
  unit_type_param_id INTEGER NOT NULL,
  value              VARCHAR(255) NULL,
  PRIMARY KEY (job_id, unit_id, unit_type_param_id),
  CONSTRAINT fk_job_param_job_id FOREIGN KEY (job_id)
    REFERENCES job (job_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_job_param_u_t_p_id FOREIGN KEY (unit_type_param_id)
    REFERENCES unit_type_param (unit_type_param_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

DROP TABLE IF EXISTS unit_job;
CREATE TABLE unit_job
(
  unit_id         VARCHAR(64) NOT NULL,
  job_id          INTEGER     NOT NULL,
  start_timestamp TIMESTAMP    NOT NULL,
  end_timestamp   TIMESTAMP NULL,
  status          VARCHAR(32) NOT NULL,
  processed       INTEGER NULL DEFAULT 0,
  confirmed       INTEGER NULL DEFAULT 0,
  unconfirmed     INTEGER NULL DEFAULT 0,
  PRIMARY KEY (unit_id, job_id)
);
ALTER TABLE unit_job ADD CONSTRAINT fk_unit_job_unit_id FOREIGN KEY (unit_id)
    REFERENCES unit (unit_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;
ALTER TABLE unit_job ADD CONSTRAINT fk_unit_job_job_id FOREIGN KEY (job_id)
    REFERENCES job (job_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;
CREATE INDEX idx_unit_job_1 ON unit_job (status, start_timestamp);
CREATE INDEX idx_unit_job_2 ON unit_job (processed);

DROP TABLE IF EXISTS heartbeat;
CREATE TABLE heartbeat
(
  id                     SERIAL PRIMARY KEY,
  name                   VARCHAR(64) NOT NULL,
  unit_type_id           INTEGER NOT NULL,
  heartbeat_expression   VARCHAR(64) NOT NULL,
  heartbeat_group_id     INTEGER NOT NULL,
  heartbeat_timeout_hour INTEGER NOT NULL DEFAULT 1,
  CONSTRAINT fk_hb_group_id FOREIGN KEY (heartbeat_group_id)
    REFERENCES group_ (group_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT fk_hb_unit_type_id FOREIGN KEY (unit_type_id)
    REFERENCES unit_type (unit_type_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

DROP TABLE IF EXISTS trigger_;
CREATE TABLE trigger_
(
  id                    SERIAL PRIMARY KEY,
  name                  VARCHAR(255) NOT NULL,
  description           VARCHAR(1024),
  trigger_type          INTEGER      NOT NULL DEFAULT 0,
  notify_type           INTEGER      NOT NULL DEFAULT 0,
  active                INTEGER      NOT NULL DEFAULT 0,
  unit_type_id          INTEGER      NOT NULL,
  group_id              INTEGER,
  eval_period_minutes   INTEGER      NOT NULL,
  notify_interval_hours INTEGER NULL,
  filestore_id          INTEGER,
  parent_trigger_id     INTEGER,
  to_list               VARCHAR(512),
  syslog_event_id       INTEGER,
  no_events             INTEGER,
  no_events_pr_unit     INTEGER,
  no_units              INTEGER
);
CREATE UNIQUE INDEX idx_trigger_unit_type_id_name ON trigger_ (unit_type_id, name);
ALTER TABLE trigger_ ADD CONSTRAINT fk_trigger_unit_type_id FOREIGN KEY (unit_type_id)
    REFERENCES unit_type (unit_type_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;
ALTER TABLE trigger_ ADD CONSTRAINT fk_trigger_group_id FOREIGN KEY (group_id)
    REFERENCES group_ (group_id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;
ALTER TABLE trigger_ ADD CONSTRAINT fk_trigger_filestore_id FOREIGN KEY (filestore_id)
    REFERENCES filestore (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;
ALTER TABLE trigger_ ADD CONSTRAINT fk_trigger_parent_id FOREIGN KEY (parent_trigger_id)
    REFERENCES trigger_ (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;
ALTER TABLE trigger_ ADD CONSTRAINT fk_trigger_syslog_event_id FOREIGN KEY (syslog_event_id)
    REFERENCES syslog_event (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;

DROP TABLE IF EXISTS trigger_event;
CREATE TABLE trigger_event
(
  id         SERIAL PRIMARY KEY,
  timestamp_ TIMESTAMP    NOT NULL,
  trigger_id INTEGER     NOT NULL,
  unit_id    VARCHAR(64) NOT NULL, -- We skip foreign key referenec on unit -> increase performance
  CONSTRAINT fk_trigger_event_trigger_id FOREIGN KEY (trigger_id)
    REFERENCES trigger_ (id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
);

DROP TABLE IF EXISTS trigger_release;
CREATE TABLE trigger_release
(
  id                    SERIAL PRIMARY KEY,
  trigger_id            INTEGER  NOT NULL,
  no_events             INTEGER NULL,
  no_events_pr_unit     INTEGER NULL,
  no_units              INTEGER NULL,
  first_event_timestamp TIMESTAMP NOT NULL,
  release_timestamp     TIMESTAMP NOT NULL,
  sent_timestamp        TIMESTAMP,
  CONSTRAINT fk_trigger_release_trigger_id FOREIGN KEY (trigger_id)
    REFERENCES trigger_ (id)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);

-- Tables with no or few foreign keys
DROP TABLE IF EXISTS certificate;
CREATE TABLE certificate
(
  id          SERIAL PRIMARY KEY,
  name        VARCHAR(64)  NOT NULL UNIQUE,
  certificate VARCHAR(256) NOT NULL
);

DROP TABLE IF EXISTS message;
CREATE TABLE message
(
  id          SERIAL PRIMARY KEY,
  type        VARCHAR(64) NOT NULL,
  sender      VARCHAR(64) NOT NULL,
  receiver    VARCHAR(64),
  object_type VARCHAR(64),
  object_id   VARCHAR(64),
  timestamp_  TIMESTAMP    NOT NULL,
  content     VARCHAR(2000) NULL
);

DROP TABLE IF EXISTS monitor_event;
CREATE TABLE monitor_event
(
  event_id     SERIAL PRIMARY KEY,
  module_name  VARCHAR(32) NOT NULL,
  module_key   VARCHAR(32) NOT NULL,
  module_state INTEGER     NOT NULL,
  message      VARCHAR(2000) NULL,
  starttime    TIMESTAMP   NOT NULL,
  endtime      TIMESTAMP   NOT NULL,
  lastchecked  TIMESTAMP   NOT NULL,
  url          VARCHAR(255),
  CONSTRAINT NameAndKey UNIQUE (module_name, module_key)
);

DROP TABLE IF EXISTS script_execution;
CREATE TABLE script_execution
(
  id                SERIAL PRIMARY KEY,
  unit_type_id      INTEGER  NOT NULL, -- SET BY REQUEST-CLIENT
  filestore_id      INTEGER  NOT NULL, -- SET BY REQUEST-CLIENT
  arguments         VARCHAR(1024), -- SET BY REQUEST-CLIENT
  request_timestamp TIMESTAMP NOT NULL, -- SET BY REQUEST-CLIENT
  request_id        VARCHAR(32), -- SET BY REQUEST-CLIENT
  start_timestamp   TIMESTAMP, -- SET BY SSD
  end_timestamp     TIMESTAMP, -- SET BY SSD
  exit_status       INTEGER, -- SET BY SSD (0=SUCCESS, 1=ERROR)
  error_message     VARCHAR(1024) -- SET BY SSD IF NECESSARY
);

DROP TABLE IF EXISTS syslog;
CREATE TABLE syslog
(
  syslog_id           SERIAL PRIMARY KEY,
  collector_timestamp TIMESTAMP                                                 NOT NULL,
  syslog_event_id     INTEGER                                                  NOT NULL,
  facility            INTEGER                                                  NOT NULL,
  facility_version    VARCHAR(48) NULL,
  severity            INTEGER                                                  NOT NULL,
  device_timestamp    VARCHAR(32) NULL,
  hostname            VARCHAR(32) NULL,
  tag                 VARCHAR(32) NULL,
  content             VARCHAR(1024) NOT NULL,
  flags               VARCHAR(32) NULL,
  ipaddress           VARCHAR(32) NULL,
  unit_id             VARCHAR(64) NULL,
  profile_name        VARCHAR(64) NULL,
  unit_type_name      VARCHAR(64) NULL,
  user_id             VARCHAR(32) NULL
);

CREATE INDEX idx_syslog_coll_tms ON syslog (collector_timestamp ASC, severity ASC, syslog_event_id ASC);
CREATE INDEX idx_syslog_unit_id_coll_tms ON syslog (unit_id ASC, collector_timestamp ASC);

DROP TABLE IF EXISTS report_unit;
CREATE TABLE report_unit
(
  timestamp_       TIMESTAMP    NOT NULL,
  period_type      INTEGER     NOT NULL,
  unit_type_name   VARCHAR(64) NOT NULL,
  profile_name     VARCHAR(64) NOT NULL,
  software_version VARCHAR(64) NOT NULL,
  status           VARCHAR(32) NOT NULL,
  unit_count       INTEGER     NOT NULL,
  PRIMARY KEY (timestamp_, period_type, unit_type_name, profile_name, software_version, status)
);

DROP TABLE IF EXISTS report_group;
CREATE TABLE report_group
(
  timestamp_     TIMESTAMP    NOT NULL,
  period_type    INTEGER     NOT NULL,
  unit_type_name VARCHAR(64) NOT NULL,
  group_name     VARCHAR(64) NOT NULL,
  unit_count     INTEGER     NOT NULL,
  PRIMARY KEY (timestamp_, period_type, unit_type_name, group_name)
);

DROP TABLE IF EXISTS report_job;
CREATE TABLE report_job
(
  timestamp_         TIMESTAMP    NOT NULL,
  period_type        INTEGER     NOT NULL,
  unit_type_name     VARCHAR(64) NOT NULL,
  job_name           VARCHAR(64) NOT NULL,
  group_name         VARCHAR(64) NOT NULL,
  group_size         INTEGER     NOT NULL,
  completed          INTEGER     NOT NULL,
  confirmed_failed   INTEGER     NOT NULL,
  unconfirmed_failed INTEGER     NOT NULL,
  PRIMARY KEY (timestamp_, period_type, unit_type_name, job_name)
);

DROP TABLE IF EXISTS report_syslog;
CREATE TABLE report_syslog
(
  timestamp_      TIMESTAMP    NOT NULL,
  period_type     INTEGER     NOT NULL,
  unit_type_name  VARCHAR(64) NOT NULL,
  profile_name    VARCHAR(64) NOT NULL,
  severity        VARCHAR(16) NOT NULL,
  syslog_event_id INTEGER     NOT NULL,
  facility        VARCHAR(32) NOT NULL,
  unit_count      INTEGER     NOT NULL,
  PRIMARY KEY (timestamp_, period_type, unit_type_name, profile_name, severity, syslog_event_id, facility)
);

DROP TABLE IF EXISTS report_prov;
CREATE TABLE report_prov
(
  timestamp_         TIMESTAMP    NOT NULL,
  period_type        INTEGER     NOT NULL,
  unit_type_name     VARCHAR(64) NOT NULL,
  profile_name       VARCHAR(64) NOT NULL,
  software_version   VARCHAR(64) NOT NULL,
  prov_output        VARCHAR(16) NOT NULL,
  ok_count           INTEGER,
  rescheduled_count  INTEGER,
  error_count        INTEGER,
  missing_count      INTEGER,
  session_length_avg INTEGER,
  PRIMARY KEY (timestamp_, period_type, unit_type_name, profile_name, software_version, prov_output)
);

DROP TABLE IF EXISTS report_voip;
CREATE TABLE report_voip
(
  timestamp_                 TIMESTAMP    NOT NULL,
  period_type                INTEGER     NOT NULL,
  unit_type_name             VARCHAR(64) NOT NULL,
  profile_name               VARCHAR(64) NOT NULL,
  software_version           VARCHAR(64) NOT NULL,
  line                       INTEGER     NOT NULL,
  mos_avg                    INTEGER,
  jitter_avg                 INTEGER,
  jitter_max                 INTEGER,
  percent_loss_avg           INTEGER,
  call_length_avg            INTEGER,
  call_length_total          INTEGER     NOT NULL,
  incoming_call_count        INTEGER     NOT NULL,
  outgoing_call_count        INTEGER     NOT NULL,
  outgoing_call_failed_count INTEGER     NOT NULL,
  aborted_call_count         INTEGER     NOT NULL,
  no_sip_service_time        INTEGER     NOT NULL,
  PRIMARY KEY (timestamp_, period_type, unit_type_name, profile_name, software_version, line)
);

DROP TABLE IF EXISTS report_voip_tr;
CREATE TABLE report_voip_tr
(
  timestamp_                 TIMESTAMP    NOT NULL,
  period_type                INTEGER     NOT NULL,
  unit_type_name             VARCHAR(64) NOT NULL,
  profile_name               VARCHAR(64) NOT NULL,
  software_version           VARCHAR(64) NOT NULL,
  line                       VARCHAR(16) NOT NULL,
  line_status                VARCHAR(64) NOT NULL,
  overruns_count             INTEGER     NOT NULL,
  underruns_count            INTEGER     NOT NULL,
  percent_loss_avg           INTEGER,
  call_length_avg            INTEGER,
  call_length_total          INTEGER     NOT NULL,
  incoming_call_count        INTEGER     NOT NULL,
  outgoing_call_count        INTEGER     NOT NULL,
  outgoing_call_failed_count INTEGER     NOT NULL,
  aborted_call_count         INTEGER     NOT NULL,
  no_sip_service_time        INTEGER     NOT NULL,
  PRIMARY KEY (timestamp_, period_type, unit_type_name, profile_name, software_version, line, line_status)
);

DROP TABLE IF EXISTS report_hw;
CREATE TABLE report_hw
(
  timestamp_               TIMESTAMP    NOT NULL,
  period_type              INTEGER     NOT NULL,
  unit_type_name           VARCHAR(64) NOT NULL,
  profile_name             VARCHAR(64) NOT NULL,
  software_version         VARCHAR(64) NOT NULL,
  boot_count               INTEGER     NOT NULL,
  boot_watchdog_count      INTEGER     NOT NULL,
  boot_misc_count          INTEGER     NOT NULL,
  boot_power_count         INTEGER     NOT NULL,
  boot_reset_count         INTEGER     NOT NULL,
  boot_prov_count          INTEGER     NOT NULL,
  boot_prov_sw_count       INTEGER     NOT NULL,
  boot_prov_conf_count     INTEGER     NOT NULL,
  boot_prov_boot_count     INTEGER     NOT NULL,
  boot_user_count          INTEGER     NOT NULL,
  mem_heap_ddr_pool_avg    INTEGER,
  mem_heap_ddr_current_avg INTEGER,
  mem_heap_ddr_low_avg     INTEGER,
  mem_heap_ocm_pool_avg    INTEGER,
  mem_heap_ocm_current_avg INTEGER,
  mem_heap_ocm_low_avg     INTEGER,
  mem_np_ddr_pool_avg      INTEGER,
  mem_np_ddr_current_avg   INTEGER,
  mem_np_ddr_low_avg       INTEGER,
  mem_np_ocm_pool_avg      INTEGER,
  mem_np_ocm_current_avg   INTEGER,
  mem_np_ocm_low_avg       INTEGER,
  cpe_uptime_avg           INTEGER,
  PRIMARY KEY (timestamp_, period_type, unit_type_name, profile_name, software_version)
);

DROP TABLE IF EXISTS report_hw_tr;
CREATE TABLE report_hw_tr
(
  timestamp_          TIMESTAMP    NOT NULL,
  period_type         INTEGER     NOT NULL,
  unit_type_name      VARCHAR(64) NOT NULL,
  profile_name        VARCHAR(64) NOT NULL,
  software_version    VARCHAR(64) NOT NULL,
  cpe_uptime_avg      INTEGER,
  memory_total_avg    INTEGER,
  memory_free_avg     INTEGER,
  cpu_usage_avg       INTEGER,
  process_count_avg   INTEGER,
  temperature_now_avg INTEGER,
  temperature_max_avg INTEGER,
  PRIMARY KEY (timestamp_, period_type, unit_type_name, profile_name, software_version)
);

DROP TABLE IF EXISTS report_gateway_tr;
CREATE TABLE report_gateway_tr
(
  timestamp_             TIMESTAMP    NOT NULL,
  period_type            INTEGER     NOT NULL,
  unit_type_name         VARCHAR(64) NOT NULL,
  profile_name           VARCHAR(64) NOT NULL,
  software_version       VARCHAR(64) NOT NULL,
  ping_success_count_avg INTEGER,
  ping_failure_count_avg INTEGER,
  ping_response_time_avg INTEGER,
  download_speed_avg     INTEGER,
  upload_speed_avg       INTEGER,
  wan_uptime_avg         INTEGER,
  PRIMARY KEY (timestamp_, period_type, unit_type_name, profile_name, software_version)
);

-- Setup initial admin user with default password "freeacs"
INSERT INTO user_ (id, username, secret, fullname, accesslist, is_admin)
VALUES (1, 'admin', '4B53D26ABECDC0802008328DCAF28B1D71B5250BB228ED784FEA56801FE102E0', 'Admin user', 'Admin', 1);
