package com.github.freeacs.web;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;

public class ValueInsertHelper {

  public static void insert(DataSource ds) throws SQLException {
    Connection connection = ds.getConnection();
    connection.setAutoCommit(false);
    Statement stmt =
        connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
    List<String> inserts =
        Arrays.asList(
            "INSERT INTO unit_type (unit_type_id, matcher_id, unit_type_name, vendor_name, description, protocol) VALUES (1, null, 'Test', null, 'Test', 'TR069');              ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (1, 1, 'System.X_FREEACS-COM.Comment', 'X');                                    ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (2, 1, 'System.X_FREEACS-COM.Debug', 'X');                                      ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (3, 1, 'System.X_FREEACS-COM.DesiredSoftwareVersion', 'X');                     ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (4, 1, 'System.X_FREEACS-COM.Device.GUIURL', 'X');                              ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (5, 1, 'System.X_FREEACS-COM.Device.PeriodicInterval', 'X');                    ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (6, 1, 'System.X_FREEACS-COM.Device.PublicIPAddress', 'X');                     ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (7, 1, 'System.X_FREEACS-COM.Device.SerialNumber', 'X');                        ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (8, 1, 'System.X_FREEACS-COM.Device.SoftwareVersion', 'X');                     ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (9, 1, 'System.X_FREEACS-COM.Discover', 'X');                                   ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (10, 1, 'System.X_FREEACS-COM.FirstConnectTms', 'X');                           ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (11, 1, 'System.X_FREEACS-COM.IM.Message', 'X');                                ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (12, 1, 'System.X_FREEACS-COM.Job.Current', 'X');                               ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (13, 1, 'System.X_FREEACS-COM.Job.CurrentKey', 'X');                            ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (14, 1, 'System.X_FREEACS-COM.Job.Disruptive', 'X');                            ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (15, 1, 'System.X_FREEACS-COM.Job.History', 'X');                               ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (16, 1, 'System.X_FREEACS-COM.LastConnectTms', 'X');                            ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (17, 1, 'System.X_FREEACS-COM.ProvisioningMode', 'X');                          ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (18, 1, 'System.X_FREEACS-COM.Reset', 'X');                                     ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (19, 1, 'System.X_FREEACS-COM.Restart', 'X');                                   ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (20, 1, 'System.X_FREEACS-COM.Secret', 'XC');                                   ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (21, 1, 'System.X_FREEACS-COM.SecretScheme', 'X');                              ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (22, 1, 'System.X_FREEACS-COM.ServiceWindow.Disruptive', 'X');                  ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (23, 1, 'System.X_FREEACS-COM.ServiceWindow.Enable', 'X');                      ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (24, 1, 'System.X_FREEACS-COM.ServiceWindow.Frequency', 'X');                   ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (25, 1, 'System.X_FREEACS-COM.ServiceWindow.Regular', 'X');                     ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (26, 1, 'System.X_FREEACS-COM.ServiceWindow.Spread', 'X');                      ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (27, 1, 'System.X_FREEACS-COM.SoftwareURL', 'X');                               ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (28, 1, 'System.X_FREEACS-COM.TR069Test.Enable', 'X');                          ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (29, 1, 'System.X_FREEACS-COM.TR069Test.FactoryResetOnStartup', 'X');           ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (30, 1, 'System.X_FREEACS-COM.TR069Test.Method', 'X');                          ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (31, 1, 'System.X_FREEACS-COM.TR069Test.ParamFilter', 'X');                     ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (32, 1, 'System.X_FREEACS-COM.TR069Test.Steps', 'X');                           ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (33, 1, 'System.X_FREEACS-COM.TR069Test.TagFilter', 'X');                       ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (34, 1, 'System.X_FREEACS-COM.Telnet.DesiredScriptVersion', 'X');               ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (35, 1, 'System.X_FREEACS-COM.Telnet.IPAddress', 'X');                          ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (36, 1, 'System.X_FREEACS-COM.Telnet.Password', 'XC');                          ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (37, 1, 'System.X_FREEACS-COM.Telnet.Port', 'X');                               ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (38, 1, 'System.X_FREEACS-COM.Telnet.Username', 'X');                           ",
            "INSERT INTO unit_type_param (unit_type_param_id, unit_type_id, name, flags) VALUES (39, 1, 'InternetGatewayDevice.ManagementServer.PeriodicInformInterval', 'RW'); ",
            "INSERT INTO profile (profile_id, unit_type_id, profile_name) VALUES (1, 1, 'Default');                                                                             ",
            "INSERT INTO unit (unit_id, unit_type_id, profile_id) VALUES ('test123', 1, 1);                                                                                     ",
            "INSERT INTO unit_param (unit_id, unit_type_param_id, value) VALUES ('test123', 20, 'password');                                                                    ");
    for (String insert : inserts) {
      stmt.addBatch(insert);
    }
    stmt.executeBatch();
    connection.commit();
    connection.close();
  }
}
