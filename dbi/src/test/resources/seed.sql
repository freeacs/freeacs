insert into unit_type(unit_type_id, unit_type_name, vendor_name, description, protocol)
values (1, 'Test unit type', 'Test vendor name', 'Test description', 'TR069');

insert into unit_type_param(unit_type_param_id, unit_type_id, name, flags)
values (1, 1, 'Test param name', 'RW');

insert into unit_type_param_value(unit_type_param_id, value, priority, type)
values (1, 'Test param value', 1, 'enum');

insert into profile(profile_id, unit_type_id, profile_name)
values (1, 1, 'Test profile name');

insert profile_param(profile_id, unit_type_param_id, value)
values (1, 1, 'Test value');

insert into group_ (group_id, unit_type_id, profile_id, group_name, description)
values (1, 1, 1, 'Test group name', 'Test description');

insert into group_ (group_id, unit_type_id, profile_id, parent_group_id, group_name, description)
values (2, 1, 1, 1, 'Test group name', 'Test description');

insert group_param(id, group_id, unit_type_param_id, operator, data_type)
values (1, 1, 1, '=', 'TEXT');

insert into job(job_id, job_name, job_type, description, group_id, unconfirmed_timeout, stop_rules, status, completed_no_failure, completed_had_failure, confirmed_failed, unconfirmed_failed)
values (1, 'Test job name', 'CONFIG|REGULAR', 'Test description', 1, 60, 'u100', 'READY', 0, 0, 0, 0);

insert job_param(job_id, unit_type_param_id, value)
values (1, 1, 'Test value');