INSERT INTO unit_type (unit_type_id, matcher_id, unit_type_name, vendor_name, description, protocol) VALUES (1, null, 'Test', null, 'Test', 'TR069');
INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (1, 1, 'System.X_FREEACS-COM.Secret', 'XC');
INSERT INTO profile (profile_id, unit_type_id, profile_name) VALUES (1, 1, 'Default');
INSERT INTO unit (unit_id, unit_type_id, profile_id) VALUES ('test123', 1, 1);
INSERT INTO unit_param (unit_id, unit_type_param_id, value) VALUES ('test123', 1, 'password');