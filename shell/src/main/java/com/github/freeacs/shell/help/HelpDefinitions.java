package com.github.freeacs.shell.help;

import com.github.freeacs.dbi.util.ACSVersionCheck;
import java.util.HashMap;
import java.util.Map;

public class HelpDefinitions {
  public static String CK_GENERIC = "generic";
  public static String CK_ROOT = "root";
  public static String CK_UNITTYPE = "unittype";
  public static String CK_PROFILE = "profile";
  public static String CK_UNIT = "unit";
  public static String CK_UNITTYPEPARAMETER = "unittypeparameter";
  public static String CK_GROUP = "group";
  public static String CK_JOB = "job";

  private Map<String, HelpGroup> helpDef = new HashMap<>();

  private static String expressionSyntax =
      "\tword\tmatches messages which contain 'word'\n"
          + "\t*\tmatches 0 or more characters\n"
          + "\t_\tmatches 1 character\n"
          + "\t^\tif used at beginning (allowed after !) of expression, matches expression only from start of message\n"
          + "\t$\tif used at end of expression, matches expression only at end of message\n"
          + "\t!\tif used at beginning of expression, will negate the matching\n"
          + "\t|\tsplit the search, so 'A|B' translates to 'A OR B', and '!A|B' translates to 'NOT A AND NOT B'\n";

  public HelpDefinitions() {
    helpDef.put(CK_GENERIC, genericHelpBuilder());
    helpDef.put(CK_ROOT, rootHelpBuilder());
    helpDef.put(CK_UNITTYPE, unittypeHelpBuilder());
    helpDef.put(CK_PROFILE, profileHelpBuilder());
    helpDef.put(CK_UNIT, unitHelpBuilder());
    helpDef.put(CK_UNITTYPEPARAMETER, unittypeParameterHelpBuilder());
    helpDef.put(CK_GROUP, groupHelpBuilder());
    helpDef.put(CK_JOB, jobHelpBuilder());
  }

  public HelpGroup getHelpGroup(String contextKey) {
    return helpDef.get(contextKey);
  }

  private String getPatternComment(String argName) {
    String argCom =
        "Optional. Any string which will be used to match the list of " + argName + "s. ";
    argCom += "The string will be interpreted as a regular expression ";
    argCom += "which is a very powerful matching language. If you want ";
    argCom += "to know how to take full advantage of regular expressions ";
    argCom +=
        "you need to consult internet resources. Special feature: Prefix with '!' to negate the search.";
    return argCom;
  }

  private HelpGroup jobHelpBuilder() {
    HelpGroup hg = new HelpGroup("Job");
    Help help;

    help = new Help("listdetails");
    help.addComment("List details about this job");
    hg.addHelp(help);

    help = new Help("listparams [DEFAULT|<unitid>]");
    help.addComment("List job parameters for this job.");
    help.addOption(getAllInfoHelpOption());
    help.addOption(getUseContextHelpOption());
    help.addOption(getListContextHelpOption());
    help.addOption(getOrderHelpOption());
    help.addArgument(
        "[DEFAULT|<unitid>]",
        "If set to DEFAULT the command will list all job parameters that are common for all units run by this job. "
            + "If set to a particular unit-id, the command will list only those job parameters set for a particular unit. The job parameters transferred "
            + "to the unit are the set of DEFAULT job parameters + the unit specific job parameters. If argument is skipped, all job parameters will be "
            + "listed.");
    help.addExamples("listparams", "listparams DEFAULT", "listparams 123456-Router-123456789AB");
    hg.addHelp(help);

    help = new Help("listfailedunits");
    help.addComment(
        "Will list all units that has had a failure of sorts when running the job. It might be that the unit in the end has managed to execute the job.");
    help.addOption(getAllInfoHelpOption());
    help.addOption(getUseContextHelpOption());
    help.addOption(getListContextHelpOption());
    help.addOption(getOrderHelpOption());
    hg.addHelp(help);

    help = new Help("delfailedunits");
    help.addComment(
        "Will delete all records of units that failed when running the job. This command is mostly used for export/delete of the entire unittype, "
            + "but it's also necessary to run this command if you try to delete a job");
    help.addOption(getUseContextHelpOption());
    hg.addHelp(help);

    help = new Help("status");
    help.addComment("Will list status of the job");
    help.addOption(getUseContextHelpOption());
    hg.addHelp(help);

    help = new Help("start");
    help.addComment("Starts the job.");
    help.addOption(getUseContextHelpOption());
    hg.addHelp(help);

    help = new Help("pause");
    help.addComment("Pause the job. You may start the job again later.");
    help.addOption(getUseContextHelpOption());
    hg.addHelp(help);

    help = new Help("finish");
    help.addComment(
        "Finishes the job, it cannot be started after this. You cannot finish a job before you have stopped it.");
    help.addOption(getUseContextHelpOption());
    hg.addHelp(help);

    help = new Help("setparam DEFAULT|<unitid> <unittype-parameter-name> <value>");
    help.addComment("Add/change a job parameter");
    help.addOption(getUseContextHelpOption());
    help.addArgument(
        "DEFAULT|<unitid>",
        "Set to DEFAULT to create a job parameter that will apply to all units executing this job. Set to <unitid> to create a job parameter specific to a unit.");
    help.addArgument(
        "<unittype-parameter-name>",
        "Adding: A valid unittype parameter. Changing: A valid job parameter.");
    help.addArgument(
        "<value>",
        "Any string. Max 255 characters. The value will be set as a unit parameter on the unit executing the job.");
    help.addExamples("setparam DEFAULT Device.ManagementServer.PeriodicInformInterval 1800");
    hg.addHelp(help);

    help = new Help("delparam DEFAULT|<unitid> <unittype-parameter-name>");
    help.addComment("Delete a job parameter");
    help.addOption(getUseContextHelpOption());
    help.addArgument(
        "DEFAULT|<unitid>",
        "Set to DEFAULT to delete a job parameter that will apply to all units executing this job. Set to <unitid> to delete a job parameter specific to a unit.");
    help.addArgument("<unittype-parameter-name>", "A valid job parameter.");
    help.addExamples("delparam DEFAULT Device.ManagementServer.PeriodicInformInterval");
    hg.addHelp(help);

    help = new Help("refresh");
    help.addComment(
        "Refresh status about a job. Otherwise counters seen in 'listjobdetails' are not updated in this session.");
    help.addOption(getUseContextHelpOption());
    hg.addHelp(help);

    return hg;
  }

  private HelpOption getUseContextHelpOption() {
    return new HelpOption(
        "-u",
        "Will use the context available from another command (piping) or input-file. The context will be changed before the command is run.");
  }

  private HelpOption getAllInfoHelpOption() {
    return new HelpOption(
        "-a",
        "Will list all available information about this object to the output. This is useful for export of data.");
  }

  private HelpOption getListContextHelpOption() {
    return new HelpOption(
        "-c",
        "Will list the current context to the output. This is useful when using this output as input to another command using the -u option.");
  }

  private HelpOption getOrderHelpOption() {
    return new HelpOption(
        "-o",
        "Will order the listing of the output. Syntax of the option argument is -o(<columnnumber><a|n><a|d>)+ An example might be "
            + "-o3aa4nd, which is to be read like this: Sort the (3)rd column (a)lphabetically in an (a)scending order, next sort on the (4)th column "
            + "(n)umericallay in an (d)escending order. Parts of the argument may be ignored if incorrectly specified.");
  }

  private HelpGroup groupHelpBuilder() {
    HelpGroup hg = new HelpGroup("Group");
    Help help;

    help = new Help("listparams [<parameter-name-pattern>]");
    help.addComment("List group parameters");
    help.addOption(getAllInfoHelpOption());
    help.addOption(getUseContextHelpOption());
    help.addOption(getListContextHelpOption());
    help.addOption(getOrderHelpOption());
    help.addArgument("[[!]<parameter-name-pattern>]", getPatternComment("parameter-name"));
    help.addExamples("listparams", "listparams DeviceInfo", "listparams ^Device.Device.*");
    hg.addHelp(help);

    help = new Help("setparam <unittype-parameter-name> <operator> <value> [<datatype>]");
    help.addComment(
        "Add/change a group parameter. A group parameter is acutally a search criteria, hence the equal/negation character.");
    help.addOption(getUseContextHelpOption());
    help.addArgument(
        "<unittype-parameter-name>",
        "Adding: A valid unittype parameter name. Changing: A valid group parameter with the #<number> at the end.");
    help.addArgument(
        "<operator>",
        "Valid operators are EQ (equals), NE (not equals), LT (less), LE (less or equal), "
            + "GE (greater or equal), GT (greater).  ");
    help.addArgument(
        "<value>",
        "Any string. Max 255 characters. Long values will take more time to process in the event of a group search, so try to avoid if possible. "
            + "The rules for matching are the same as for the 'listunits' commands in the unittype/profile context. In other words, use '%' in the value string to match 0 "
            + "or more arbitrary characters and use '_' in the value string to match 1 arbitrary character. Avoid % and _ at the start of the value. To search "
            + "for NULL-values, use the string NULL.");
    help.addArgument(
        "[<dataype>]",
        "Optional. Default datatype is VARCHAR (which is the same as 'text' or 'string'). You can also "
            + "use SIGNED for an integer which might be negative, or UNSIGNED for integers which are always positive.");
    help.addExample("setparam Device.DeviceInfo.SerialNumber EQ AABBCCDDEEFF00");
    help.addExample("setparam Device.DeviceInfo.SerialNumber NE AAB%BCC_");
    help.addExample("setparam Device.DeviceInfo.SerialNumber GE 123 UNSIGNED");
    hg.addHelp(help);

    help = new Help("delparam <group-parameter-name>");
    help.addComment(
        "Delete a group parameter. This will change the search criteria for this group, and all child groups of this group.");
    help.addOption(getUseContextHelpOption());
    help.addArgument("<group-parameter-name>", "A valid group parameter");
    help.addExamples("delparam Device.DeviceInfo.SerialNumber");
    hg.addHelp(help);

    help = new Help("delallparams");
    help.addComment(
        "Delete all group parameters. This will change the search criteria for this group, and all child groups of this group.");
    help.addOption(getUseContextHelpOption());
    hg.addHelp(help);

    help = new Help("count");
    help.addComment("Will count the number of units that matches this group's search criteria");
    help.addOption(getUseContextHelpOption());
    help.addOption(getListContextHelpOption());
    hg.addHelp(help);

    help = new Help("listunits");
    help.addComment("Will list all units that matches this group's search critera");
    help.addOption(getAllInfoHelpOption());
    help.addOption(getUseContextHelpOption());
    help.addOption(getListContextHelpOption());
    help.addOption(getOrderHelpOption());
    hg.addHelp(help);

    help = new Help("listdetails");
    help.addComment("Will list detailed information about this group.");
    hg.addHelp(help);

    return hg;
  }

  private Help getListTestHistoryHelp() {
    Help help = new Help("listtesthistory [filter]...");
    help.addComment(
        "List the test history of this unit. Takes a number of optional filter parameters on the form 'filtername=value'.");
    help.addArgument(
        "startTms=<timestamp>",
        "Optional. If specified, only shows tests that started after the specified date.");
    help.addArgument(
        "endTms=<timestamp>",
        "Optional. If specified, only shows tests that finished before the specified date.");
    help.addArgument(
        "result=<expression>",
        "Optional. If specified, only shows tests whose results matches the given expression."
            + " In the expression, '_' means 'exactly one wildcard' and '%' means 'zero or more wildcards'.");
    help.addArgument(
        "expectError=<boolean>",
        "Optional. If specified, only shows tests that either expects errors or not.");
    help.addArgument(
        "failed=<boolean>", "Optional. If specified, only shows tests that failed or not.");
    help.addExamples(
        "listtesthistory",
        "listtesthistory startTms=3d1h",
        "listtesthistory startTms=20120701 endTms=20140705-1300",
        "listtesthistory failed=true",
        "listtesthistory expectError=false",
        "listtesthistory result=%Time%",
        "listtesthistory result=GETVALUE%");
    return help;
  }

  private HelpGroup unittypeParameterHelpBuilder() {
    HelpGroup hg = new HelpGroup("UnittypeParameter");
    Help help;

    help = new Help("listvalues");
    help.addComment("List all enumerated values for this unittype parameter.");
    help.addOption(getUseContextHelpOption());
    help.addOption(getListContextHelpOption());
    help.addOption(getOrderHelpOption());
    hg.addHelp(help);

    help = new Help("setvalues enum|regexp <value>+");
    help.addComment("Add/change the set of accepted values for this unittype parameter.");
    help.addOption(getUseContextHelpOption());
    help.addArgument(
        "enum|regexp",
        "The only supported 'mode' in Fusion Web is 'enum'. Later it might be possible to specify a regexp with which a parameter value must match.");
    help.addArgument(
        "<value>+",
        "One or more values. If a value contains spaces enclose value with double quotes.");
    help.addExamples("setvalues enum 1 2 3", "setvalues enum On Off");
    hg.addHelp(help);

    help = new Help("delvalues");
    help.addComment("Will delete all enumerations for this unittype parameter");
    help.addOption(getUseContextHelpOption());
    hg.addHelp(help);

    help = new Help("generateenum");
    help.addComment(
        "Generates enumeration values based on TR098-1.4, TR104-1.1 and TR181-1.2.0. Be advised that the enumerations may "
            + "not be supported by the device.");

    hg.addHelp(help);

    return hg;
  }

  private HelpGroup unitHelpBuilder() {
    HelpGroup hg = new HelpGroup("Unit");
    Help help;

    help = new Help("listallparams [<parameter-name-pattern>]");
    help.addComment(
        "List all parameters, both profile and unit parameter. Last column shows which are unit parameters (U) and which are profile parameters (P).");
    help.addOption(getAllInfoHelpOption());
    help.addOption(getUseContextHelpOption());
    help.addOption(getListContextHelpOption());
    help.addOption(getOrderHelpOption());
    help.addArgument("[[!]<parameter-name-pattern>]", getPatternComment("parameter-name"));
    help.addExamples(
        "listallparams", "listallparams DeviceInfo", "listallparams ^Device.DeviceInfo.*");
    hg.addHelp(help);

    help = new Help("listunitparams [<parameter-name-pattern>]");
    help.addComment("List unit parameters.");
    help.addOption(getAllInfoHelpOption());
    help.addOption(getUseContextHelpOption());
    help.addOption(getListContextHelpOption());
    help.addOption(getOrderHelpOption());
    help.addArgument("[[!]<parameter-name-pattern>]", getPatternComment("parameter-name"));
    help.addExamples(
        "listunitparams", "listunitparams DeviceInfo", "listunitparams ^Device.DeviceInfo.*");
    hg.addHelp(help);

    help = new Help("setparam <parameter-name> <value>");
    help.addComment("Add/change a unit parameter.");
    help.addOption(getUseContextHelpOption());
    help.addArgument(
        "<parameter-name>",
        "Adding: A valid unittype parameter name. Changing: A valid unit parameter name.");
    help.addArgument("<value>", "Any String, maximum 512 character.");
    help.addExamples(
        "setparam Device.DeviceInfo.PeriodicInformInterval 100",
        "setparam System.X_FREEACS-COM.Comment \"Service is unavailable\"");
    hg.addHelp(help);

    help = new Help("delparam <parameter-name>");
    help.addComment("Delete a unit parameter");
    help.addOption(getUseContextHelpOption());
    help.addArgument("<parameter-name>", "A valid unit parameter");
    help.addExamples("delparam Device.DeviceInfo.PeriodicInformInterval");
    hg.addHelp(help);

    help = new Help("provision [async]");
    String argCom =
        "The device will connect ASAP and complete a provisioning (both reading/writing from/to the device). ";
    argCom +=
        "This will only work if the ConnectionRequestURL or UDPConnectionRequestAddress is pointing to the correct address.";
    help.addComment("");
    help.addOption(getUseContextHelpOption());
    help.addArgument(
        "async",
        "If this argument is used, the kick will be initiated and then the command will return. "
            + "If not used, the command will wait up til 30 sec to see if the kick is completed.");
    help.addExamples("kick", "kick async");
    hg.addHelp(help);

    help = new Help("readall [async]");
    argCom =
        "The device will connect ASAP and all parameters from the CPE/device will be read and listed with the 'listallparameters' command. ";
    argCom +=
        "This will only work if the ConnectionRequestURL or UDPConnectionRequestAddress is pointing to the correct address.";
    help.addComment(argCom);
    help.addOption(getUseContextHelpOption());
    help.addArgument(
        "async",
        "If this argument is used, the kick will be initiated and then the command will return. "
            + "If not used, the command will wait up til 30 sec to see if the kick is completed.");
    help.addExamples("readall", "readall async");
    hg.addHelp(help);

    help = new Help("regular");
    argCom =
        "The device will return to REGULAR mode without issuing any kick to the device. The next time the device connects to the server, it will be a normal provisioning session";
    help.addComment(argCom);
    help.addExamples("regular");
    hg.addHelp(help);

    help = new Help("refresh");
    help.addComment(
        "Refresh the unit parameter cache in shell. This may be necessary to run also if you've used the setparam command, since setparam do not"
            + " refresh the unit context (in order to be efficient when upload large numbers of parameters).");
    help.addOption(getUseContextHelpOption());
    hg.addHelp(help);

    help = new Help("generatetc [test-case-id|VALUE|ATTRIBUTE]");
    help.addComment(
        "If the optional arugment is omitted, it will generate Test Cases for the TR-069 test based on the Unit type parameters available and TR-181 (v1.9), TR-104 (v1.1) and TR-098 (v1.4)");
    help.addArgument(
        "test-case-id",
        "Specify a \"master\" Test Case with many N SET parameters. The generate command will produce one TC for each SET parameter, from 1-N params");
    help.addArgument("VALUE", "Generate only VALUE test cases");
    help.addArgument("ATTRIBUTE", "Generate only ATTRIBUTE test cases");
    help.addExamples("generatetc", "generatetc 4049", "generatetc VALUE");
    hg.addHelp(help);

    help = new Help("deltcduplicates");
    help.addComment("Delete duplicate Test Cases");
    hg.addHelp(help);

    help = new Help("listtc [<method>|NULL [<parameter-filter>|NULL [<tag-filter>|NULL]]]");
    help.addComment("List all or a subset of Test Cases");
    help.addArgument(
        "<method>|NULL",
        "Optional. Could be set to VALUE, ATTRIBUTE or FILE. Setting it to NULL will match all (methods)");
    help.addArgument(
        "<parameter-filter>|NULL",
        "Optional if the previous argument is specified. Could be set to a part of a parameter name. Setting it to NULL will match all (parameters)");
    help.addArgument(
        "<paratagmeter-filter>|NULL",
        "Optional if the previous argument is specified. Could be set to a series of strings enclosed in square brackets [ ] to match tags. Setting it to NULL will match all (tags)");
    help.addExample("listtc NULL NULL NULL");
    help.addExample("listtc VALUE WANConnection \"[READONLY][GENERATED]\"");
    hg.addHelp(help);

    help = new Help("listtctags [<method>|NULL [<parameter-filter>|NULL [<tag-filter>|NULL]]]");
    String comment =
        "List all tags for these test cases, and count the number of test case occurrences for each tag. ";
    comment +=
        "The tags used are (according to the format of a test case) entirely freely chosen values - hence the meaning ";
    comment +=
        "of a tag is up to the creator of a test case. However, using the generatetc command, a number of tags will be ";
    comment += "created, and these have a specific meaning:\n";
    comment += "\tCOMPLEX     - Contains multiple parameters\n";
    comment += "\tFALSE       - Tests only boolean parameter with all values set to 'false'\n";
    comment += "\tGENERATED   - Test case generated by generatetc command\n";
    comment += "\tLARGE       - Test contains many parameters\n";
    comment +=
        "\tLAYERx      - Testing all parameters on layer x in the unittype. Layer 0 is ALL parameters, Layer 1 could be all params within DeviceInfo object, etc\n";
    comment += "\tREADONLY    - Contains only read-only parameters - no use running SET method\n";
    comment += "\tREADWRITE   - Contains only read-write parameters\n";
    comment += "\tSIMPLE      - Contains one single parameter\n";
    comment += "\tTEMPLATE    - Considered a template for you to modify\n";
    comment += "\tTRUE        - Tests only boolean parameter with all values set to 'true'\n";
    comment += "\txsd:<type>  - Tests only parameter of this xsd type\n";
    help.addComment(comment);
    help.addOption(getOrderHelpOption());
    help.addArgument(
        "<method>|NULL",
        "Optional. Could be set to VALUE, ATTRIBUTE or FILE. Setting it to NULL will match all (methods)");
    help.addArgument(
        "<parameter-filter>|NULL",
        "Optional if the previous argument is specified. Could be set to a part of a parameter name. Setting it to NULL will match all (parameters)");
    help.addArgument(
        "<paratagmeter-filter>|NULL",
        "Optional if the previous argument is specified. Could be set to a series of strings enclosed in square brackets [ ] to match tags. Setting it to NULL will match all (tags)");
    help.addExample("listtc NULL NULL NULL");
    help.addExample("listtc VALUE WANConnection \"[READONLY][GENERATED]\"");
    hg.addHelp(help);

    help = new Help("showtc <id>");
    help.addComment("Show the Test Case for the given id, as it would be if exported to file");
    help.addArgument("<id>", "A valid Test Case id (get from listtc command)");
    help.addExample("showtc 123");
    hg.addHelp(help);

    help = new Help("deltc [<method>|NULL [<parameter-filter>|NULL [<tag-filter>|NULL]]]");
    help.addComment("Delete all or a subset of Test Cases");
    help.addArgument(
        "<method>|NULL",
        "Optional. Could be set to VALUE or ATTRIBUTE. Setting it to NULL will match all (methods)");
    help.addArgument(
        "<parameter-filter>|NULL",
        "Optional if the previous argument is specified. Could be set to a part of a parameter name. Setting it to NULL will match all (parameters)");
    help.addArgument(
        "<paratagmeter-filter>|NULL",
        "Optional if the previous argument is specified. Could be set to a series of strings enclosed in square brackets [ ] to match tags. Setting it to NULL will match all (tags)");
    help.addExample("deltc NULL NULL NULL");
    help.addExample("deltc VALUE WANConnection \"[READONLY][GENERATED]\"");
    hg.addHelp(help);

    help = new Help("exporttcfile <directory> <id>");
    help.addComment("Export one single Test Cases");
    help.addArgument("<directory>", "A directory to where the Test Case file will be exported");
    help.addArgument("<id>", "A valid Test Case id (get from listtc command)");
    help.addExample("exporttc tr069test 123");
    hg.addHelp(help);

    help =
        new Help(
            "exporttcdir <directory> [<method>|NULL [<parameter-filter>|NULL [<tag-filter>|NULL]]]");
    help.addComment("Export all or a subset of Test Cases");
    help.addArgument("<directory>", "A directory to where the Test Case files will be exported");
    help.addArgument(
        "<method>|NULL",
        "Optional. Could be set to VALUE or ATTRIBUTE. Setting it to NULL will match all (methods)");
    help.addArgument(
        "<parameter-filter>|NULL",
        "Optional if the previous argument is specified. Could be set to a part of a parameter name. Setting it to NULL will match all (parameters)");
    help.addArgument(
        "<paratagmeter-filter>|NULL",
        "Optional if the previous argument is specified. Could be set to a series of strings enclosed in square brackets [ ] to match tags. Setting it to NULL will match all (tags)");
    help.addExample("exporttc NULL NULL NULL");
    help.addExample("exporttc VALUE WANConnection \"[READONLY][GENERATED]\"");
    hg.addHelp(help);

    help = new Help("importtcfile <filename>");
    help.addComment("Import a Test Case");
    help.addArgument("<filename>", "A Test Case file to be imported");
    help.addExample("importtcfile tr069test/2930.tc");
    hg.addHelp(help);

    help = new Help("importtcdir <directory>");
    help.addComment("Import all Test Cases in a directory");
    help.addArgument("<directory>", "A directory from where all Test Case files will be imported");
    help.addExample("importtcdir tr069test");
    hg.addHelp(help);

    help =
        new Help(
            "testsetup [steps=<steplist>] [reset=true|false] "
                + "[method=VALUE|ATTRIBUTE|FILE] [parameter-filter=<paramname>] [tag-filter=<taglist>]");
    comment =
        "Setup the TR-069 parameter test. We suggest to start with the first example, assuming you've run the "
            + "generatetc command. Then continue with the following examples in the list.";
    help.addComment(comment);
    help.addArgument(
        "<steps>",
        "A comma-separated list of the following allowed string: "
            + "GET, SET, RESET or REBOOT. This is the order in which the test will be executed. "
            + "It is not allowed to repeat a step, nor applicable at all if the method is set "
            + "to FILE, se below. If not set, defaults to simply GET.");
    help.addArgument(
        "<reset>",
        "True or False, 0 or 1. Will initiate a Factory Reset "
            + "before the test starts if set to True/1. False/0 by default.");
    help.addArgument(
        "<method>",
        "Can be either VALUE, ATTRIBUTE or FILE. If set to VALUE, "
            + "Get/SetParameterValues method will be run with the test cases. If set to ATTRIBUTE, "
            + "Get/SetParameterAttributes will be run similarly. If set to FILE however, the TR-069 "
            + "server will instead send the raw contents in the file specified by the INPUT-field "
            + "in the test case, and possibly expect the reply to match that of the file specified "
            + "by the OUTPUT field. The default method is VALUE.");
    help.addArgument(
        "<parameter-filter>",
        "A part of a parameter name which will narrow the set "
            + "of test-cases to be run in the test.");
    help.addArgument(
        "<tag-filter>",
        "A set of tags, each enclosed is square brackets, which will "
            + "narrow the set of test-cases to be run in the test.");
    help.addExample("Will test to read all parameters, one by one:");
    help.addExample("> testsetup tag-filter=[SIMPLE]");
    help.addExample("Will test to read the value of a single parameter: (DeviceInfo.ProductClass)");
    help.addExample("> testsetup parameter-filter=ProductClass tag-filter=[SIMPLE]");
    help.addExample("testsetup param-filter=WANConnection tag-filter=\"[READONLY] [GENERATED]\"");
    help.addExample(
        "testsetup steps=SET,REBOOT,GET param-filter=WANConnection tag-filter=\"[READWRITE] [GENERATED]\"");

    hg.addHelp(help);

    help = new Help("enabletest");
    help.addComment(
        "Enable TR-069 parameter test mode, which performs a series of tests based on test-cases added to the Unit Type. As long as the device is in test-mode, it will not "
            + "provision normally, so make sure you disable the test when you're satisfied. Check up on the test-case commands in the Unit Type menu");
    hg.addHelp(help);

    help = new Help("disabletest");
    help.addComment("Disable TR-069 parameter test mode.");
    hg.addHelp(help);

    hg.addHelp(getListTestHistoryHelp());

    help = new Help("deltesthistory [startTms|NULL [endTms|NULL]]");
    help.addComment("Delete the test history of this unit.");
    help.addArgument(
        "startTms",
        "Optional, can also be set to NULL (to delete from beginning). Otherwise specify date using syntax from syslog-command");
    help.addArgument(
        "endTms",
        "Optional, can also be set to NULL (to delete until end). Otherwise specify date using syntax from syslog-command");
    help.addExamples(
        "deltesthistory", "deltesthistory 3d 1h", "deltesthistory 20120701 20120705-1300");
    hg.addHelp(help);

    return hg;
  }

  private HelpGroup profileHelpBuilder() {
    HelpGroup hg = new HelpGroup("Profile");
    Help help;

    help = new Help("listparams [<profile-parameter-name-pattern>]");
    help.addComment("List profile parameters.");
    help.addOption(getAllInfoHelpOption());
    help.addOption(getUseContextHelpOption());
    help.addOption(getListContextHelpOption());
    help.addOption(getOrderHelpOption());
    help.addArgument(
        "[[!]<profile-parameter-name-pattern>]", getPatternComment("profile-parameter"));
    help.addExamples("listparams", "listparams DeviceInfo");
    hg.addHelp(help);

    help = new Help("setparam <parameter-name> <value>");
    help.addComment("Add/change a profile parameter.");
    help.addOption(getUseContextHelpOption());
    help.addArgument(
        "<parameter-name>",
        "Adding: A valid unittype parameter name. Changing: A valid profile parameter name.");
    help.addArgument("<value>", "Any String, maximum 255 character.");
    help.addExamples("setparam System.X_COMPANY-COM.Test \"Hello world\"");
    hg.addHelp(help);

    help = new Help("delparam <profile-parameter-name>");
    help.addComment("Delete a profile parameter.");
    help.addOption(getUseContextHelpOption());
    help.addArgument("<profile-parameter-name>", "A valid profile parameter name.");
    help.addExamples("delparam Device.DeviceInfo.PeriodicInformInterval");
    hg.addHelp(help);

    help =
        new Help(
            "listunits [(<search-value>) | (<unittype-parameter-name> <operator> <value> [<datatype>])*]");
    help.addComment("List units within this unittype.");
    help.addOption(getAllInfoHelpOption());
    help.addOption(getUseContextHelpOption());
    help.addOption(getListContextHelpOption());
    help.addOption(getOrderHelpOption());
    help.addArgument(
        "[<search-value>]", "Optional. It will search among all parameter values and unit-ids");
    help.addArgument(
        "<operator>",
        "Valid operators are EQ (equals), NE (not equals), LT (less), LE (less or equal), "
            + "GE (greater or equal), GT (greater).  ");
    help.addArgument(
        "<value>",
        "Any string. Max 255 characters. Long values will take more time to process in the event of a group search, so try to avoid if possible. "
            + "The rules for matching are the same as for the 'listunits' commands in the unittype/profile context. In other words, use '%' in the value string to match 0 "
            + "or more arbitrary characters and use '_' in the value string to match 1 arbitrary character. Avoid % and _ at the start of the value. To search "
            + "for NULL-values, use the string NULL.");
    help.addArgument(
        "[<dataype>]",
        "Optional. Default datatype is VARCHAR (which is the same as 'text' or 'string'). You can also "
            + "use SIGNED for an integer which might be negative, or UNSIGNED for integers which are always positive.");
    help.addExamples("listunits", "listunits John");
    help.addExample("listunits Device.DeviceInfo.SerialNumber EQ AABBCCDDEEFF00");
    help.addExample("listunits Device.DeviceInfo.SerialNumber NE %AABBCC%");
    hg.addHelp(help);

    help = new Help("setunit <unitid>");
    help.addComment("Add a unit to this profile");
    help.addOption(getUseContextHelpOption());
    help.addArgument(
        "<unitid>",
        "A TR-069 unit id must adhere to the standard which is\n\t<OUI>[-<ProductClass>]<-SerialNumber>.\n"
            + "The OUI is a 6-digit hexadecimal number representing the vendor. Usually this is the same as the 6 first digits of the "
            + "MAC address of the unit. You can also find the appropriate OUI on the internet. ProductClass is optional, but is usually "
            + "the name of the product. SerialNumber is in many cases the MAC address, but could be any string. For non TR-069 units you "
            + "may set <unitid> to any string. The unittype's protocol decides which rule applies.");
    help.addExamples("setunit A", "setunit 123456-VeryFastRouter-123456789AB");
    hg.addHelp(help);

    help = new Help("delunit <unitid>");
    help.addComment("Delete a unit.");
    help.addOption(getUseContextHelpOption());
    help.addArgument("<unitid>", "A valid unit id.");
    help.addExamples("delunit 123456-VeryFastRouter-123456789AB");
    hg.addHelp(help);

    help = new Help("delallunits");
    help.addComment(
        "Deletes all units within this profile. This command is a combined/wizard command in the sense that you could "
            + "achieve the same as this command does by running 2 other commands (listunits > FILE, delunit < FILE).");
    help.addOption(getUseContextHelpOption());
    hg.addHelp(help);

    help = new Help("moveunit <unitid> <profilename>");
    help.addComment(
        "Move a unit from this profile to another profile within the same unittype. The unit will keep all unit parameters.");
    help.addOption(getUseContextHelpOption());
    help.addArgument("<unitid>", "A valid unit id.");
    help.addArgument("<profilename>", "A valid profile name within this unittype.");
    help.addExamples("moveunit 123456-VeryFastRouter-123456789AB new-improved-profile");
    hg.addHelp(help);

    return hg;
  }

  private HelpGroup unittypeHelpBuilder() {
    HelpGroup hg = new HelpGroup("Unittype");
    Help help;
    String argCom;
    help = new Help("listparams [<unittype-parameter-name-pattern>]");
    help.addComment("List unittype parameters.");
    help.addOption(getAllInfoHelpOption());
    help.addOption(getUseContextHelpOption());
    help.addOption(getListContextHelpOption());
    help.addOption(getOrderHelpOption());
    argCom = "Optional. Any string which will be used to match the list of unittype names. ";
    argCom += "The string will be interpreted as a regular expression ";
    argCom += "which is a very powerful matching language. If you want ";
    argCom += "to know how to take full advantage of regular expressions ";
    argCom += "you need to consult internet resources.";
    help.addArgument(
        "[[!]<unittype-parameter-name-pattern>]", getPatternComment("unittype-parameter"));
    help.addExamples("listparams", "listparams DeviceInfo", "listparams ^Device.Device.*");
    hg.addHelp(help);

    help = new Help("setparam <unittype-parameter-name> [D][A][C][S][I][M](R|RW|X)");
    help.addComment("Add/change a unittype parameter.");
    help.addOption(getUseContextHelpOption());
    argCom = "If adding: A name without spaces. It is a good idea to follow the ";
    argCom += "TR-069 dotted pattern for parameter names. If the device is a TR-069 ";
    argCom += "device, make sure that parameter names are equal to those registered ";
    argCom +=
        "in the device. Max length: 255 characters. Tip: Fusion TR-069 Server, discovery mode. ";
    argCom += "If changing: A valid unittype parameter name.";
    help.addArgument("<unittype-parameter-name>", argCom);
    argCom =
        "The flags D,A,C,S,I,M are optional, R, RW or X is mandatory. The order of the flags are not important.\n";
    argCom +=
        "\t[A]lwaysRead   - The value will always be read from the device. Cannot be combined with I, RW or X.\n";
    argCom +=
        "\t[B]ootRequired - If such a parameter is sent/set to the device, an Reboot will follow immediately after\n";
    argCom +=
        "\t[C]onfidential - The value will probably be sent from the device as \"\", usually used for passwords. This flag will ";
    argCom +=
        "suppress a warning that is logged when database and device differ while it was expected to be equal. The logs will obscure the value, to minimize exposure.\n";
    argCom +=
        "\t[D]isplayable  - The value will be shown as a column in the search page of Fusion Web. Cannot be combined with I and C.\n";
    argCom +=
        "\t[S]earchable   - Possible to search for this particular parameter in Fusion Web, using the Advanced Search.\n";
    argCom +=
        "Web in the Unit Parameter page. After inspection mode is finished, the inspection parameters read will be deleted. Useful for short-lived parameters.\n";
    argCom +=
        "\t[M]onitor      - The value will be sent to Fusion Syslog Server, and can be processed into a TR-report (given the appropriate ";
    argCom +=
        "parameters are set to M). The parameters are stored to database only if combined with A flag.\n";
    argCom +=
        "\t(R)eadOnly     - The parameter can only be read from the device, never written to the device.\n";
    argCom +=
        "\t(R)ead(W)rite  - The parameter can be written to the device. Only read from device if combined with I.\n";
    argCom +=
        "\t(X) system     - The parameter is only for internal use, never sent to the device. You can create as many X parameters as you ";
    argCom +=
        "like. Please adhere to standard name convention for such parameters:\n\t\tSystem.X_COMPAMY-COM.NameOfParameter<.SubName>";
    help.addArgument("<flags>", argCom);
    help.addExample("setparam Device.DeviceInfo.PeriodicInformInterval RW");
    help.addExample("setparam System.X_FREEACS-COM.Comment SDX");
    help.addExample("setparam Device.DeviceInfo.SerialNumber AR");
    hg.addHelp(help);

    help = new Help("delparam <unittype-parameter-name>");
    help.addComment(
        "Delete a unitttype parameter. Cannot be done unless the parameter is not used in any unit, profile, job, group.");
    help.addArgument("<unittype-parameter-name>", "A valid unittype parameter name");
    help.addExamples("delparam Device.DeviceInfo.PeriodicInformInterval");
    hg.addHelp(help);
    help.addArgument("<unittype-parameter-name>", "A valid unittype parameter name.");
    help.addArgument(
        "<operator>",
        "Valid operators are EQ (equals), NE (not equals), LT (less), LE (less or equal), "
            + "GE (greater or equal), GT (greater).  ");
    help.addArgument(
        "<value>",
        "Any string. Max 255 characters. Long values will take more time to process in the event of a group search, so try to avoid if possible. "
            + "The rules for matching are the same as for the 'listunits' commands in the unittype/profile context. In other words, use '%' in the value string to match 0 "
            + "or more arbitrary characters and use '_' in the value string to match 1 arbitrary character. Avoid % and _ at the start of the value. To search "
            + "for NULL-values, use the string NULL.");
    help.addArgument(
        "[<dataype>]",
        "Optional. Default datatype is VARCHAR (which is the same as 'text' or 'string'). You can also "
            + "use SIGNED for an integer which might be negative, or UNSIGNED for integers which are always positive.");

    help =
        new Help(
            "listunits [(<search-value>) | (<unittype-parameter-name> <operator> <value> [<datatype>])*]");
    help.addComment("List units within this unittype.");
    help.addOption(getAllInfoHelpOption());
    help.addOption(getUseContextHelpOption());
    help.addOption(getListContextHelpOption());
    help.addOption(getOrderHelpOption());
    help.addArgument(
        "[<search-value>]", "Optional. It will search among all parameter values and unit-ids");
    help.addArgument(
        "<operator>",
        "Valid operators are EQ (equals), NE (not equals), LT (less), LE (less or equal), "
            + "GE (greater or equal), GT (greater).  ");
    help.addArgument(
        "<value>",
        "Any string. Max 255 characters. Long values will take more time to process in the event of a group search, so try to avoid if possible. "
            + "The rules for matching are the same as for the 'listunits' commands in the unittype/profile context. In other words, use '%' in the value string to match 0 "
            + "or more arbitrary characters and use '_' in the value string to match 1 arbitrary character. Avoid % and _ at the start of the value. To search "
            + "for NULL-values, use the string NULL.");
    help.addArgument(
        "[<dataype>]",
        "Optional. Default datatype is VARCHAR (which is the same as 'text' or 'string'). You can also "
            + "use SIGNED for an integer which might be negative, or UNSIGNED for integers which are always positive.");
    help.addExamples("listunits", "listunits John");
    help.addExample("listunits Device.DeviceInfo.SerialNumber EQ AABBCCDDEEFF00");
    help.addExample("listunits Device.DeviceInfo.SerialNumber NE %AABBCC%");
    hg.addHelp(help);

    help = new Help("moveunit <unitid> <unittypename> <profilename>");
    help.addComment(
        "Move a unit to another unittype. Requires that the target unittype has defined all unittype parameters found for this unit's parameters and that the profile exists. ");
    help.addOption(getUseContextHelpOption());
    help.addArgument("<unitid>", "A valid unit-id in this unittype.");
    help.addArgument("<unittypename>", "Target unittype name, must exist.");
    help.addArgument("<profilename>", "Target profile name, must exist.");
    help.addExamples("moveunit 123456-ProductClass-123456789012 NPA201E-Test Default");
    hg.addHelp(help);

    help = new Help("systemparameterscleanup");
    help.addComment(
        "Cleanup of old and unused system parameters. Will never delete a system parameter that is in use.");
    help.addOption(getUseContextHelpOption());
    hg.addHelp(help);

    help = new Help("listfiles [<file-name-pattern>]");
    help.addComment("List files stored in Fusion.");
    help.addOption(getAllInfoHelpOption());
    help.addOption(getUseContextHelpOption());
    help.addOption(getListContextHelpOption());
    help.addOption(getOrderHelpOption());
    help.addArgument("[[!]<file-name-pattern>]", getPatternComment("file-name"));
    help.addExamples("listfiles", "listfiles 5.4.1");
    hg.addHelp(help);

    help =
        new Help(
            "importfile <filename> <filetype> <version> <timestamp> <targetname> <description>");
    help.addComment("Add/import file to Fusion filestore. ");
    help.addOption(getUseContextHelpOption());
    help.addArgument(
        "<filename>",
        "Filename of the file (found in the filesystem). Use / as directory path delimiter.");
    argCom = "SOFTWARE      - CPE image files, used in software upgrade\n";
    argCom +=
        "TR069_SCRIPT  - A CPE script/config file, used in TR-069 Script Upgrade. A Target filename must be specified\n";
    argCom +=
        "SHELL_SCRIPT  - A Fusion script file, used in Fusion Shell, Shell Jobs, Triggers and Syslog Events\n";
    argCom += "TELNET_SCRIPT - A Telnet script file, used in Telnet Job\n";
    argCom +=
        "UNITS         - A unit file, first column in file must be a valid unit-id, used in Trigger Script execution\n";
    argCom += "MISC          - A file, no particular use-case\n";
    help.addArgument("<filetype>", argCom);
    argCom =
        "Extremely important if importing a SOFTWARE or a TR069_SCRIPT. It MUST be the version number reported from the software/script when installed in ";
    argCom +=
        "the device. Otherwise it will create a download loop! For other file types this is not as important.";
    help.addArgument("<version>", argCom);
    help.addArgument(
        "timestamp",
        "Specify a timestamp (format is : yyyyMMdd-HH:mm:ss) or set to NULL (which will translate into current timestamp)");
    help.addArgument(
        "targetname",
        "Optional (set to NULL) for all file types except TR069_SCRIPT, in which case the targetname is used to distinguish between multiple Script Upgrade.");
    help.addArgument(
        "ower",
        "Owner of file. Can only be set if you're admin, if not it will automatically (and silently) be set to your username");
    help.addArgument("<description>", "Non-essential (to Fusion) description of the file.");
    help.addExamples("importfile test.fss SHELL_SCRIPT 1 NULL NULL NULL \"A Shell Script\"");
    help.addExamples(
        "importfile firmware.bin SOFTWARE 5.0.3-alfa NULL NULL myuser \"A firmware for the NPA201E\"");
    help.addExamples(
        "importfile voip.zip TR069_SCRIPT 1 NULL /opt/voip.zip NULL \"A Zip-file download to the CPE\"");
    hg.addHelp(help);

    help = new Help("exportfile <filename>");
    help.addComment("Export file from Fusion filestore. ");
    help.addOption(getUseContextHelpOption());
    argCom =
        "A filename from Fusion filestore. If the file already exists it will create a directory named ";
    argCom +=
        "after the unittype, and place the file there. The import command will always search for such a location first ";
    argCom += "when trying to import a file.";
    help.addArgument("<filename>", argCom);
    help.addExamples("exportfile test.xss");
    hg.addHelp(help);

    help = new Help("delfile <filename>");
    help.addComment("Delete a file from Fusion filestore");
    help.addOption(getUseContextHelpOption());
    help.addArgument("<filename>", "A valid filename in the Fusion filestore");
    help.addExamples("delfile test.xss");
    hg.addHelp(help);

    help = new Help("listprofiles [<profile-name-pattern>]");
    help.addComment("List profiles within this unittype");
    help.addOption(getUseContextHelpOption());
    help.addOption(getListContextHelpOption());
    help.addOption(getOrderHelpOption());
    help.addArgument("[[!]<profile-name-pattern>]", getPatternComment("profile-name"));
    help.addExamples("listprofiles", "listprofiles Def");
    hg.addHelp(help);

    help = new Help("setprofile <profilename>");
    help.addComment("Add/change a profile. Does not add/change any profile parameters.");
    help.addOption(getUseContextHelpOption());
    help.addArgument(
        "<profilename>", "Any string, enclose arguments with spaces using double quotes.");
    help.addExamples("setprofile Default");
    hg.addHelp(help);

    help = new Help("delprofile <profilename>");
    help.addComment(
        "Delete a profile. Not allowed if there are units within this profile or if groups are connected to this profile.");
    help.addOption(getUseContextHelpOption());
    help.addArgument("<profilename>", "A valid profile within this unittype.");
    help.addExamples("delprofile Default");
    hg.addHelp(help);

    help = new Help("listgroups [<group-name-pattern>] [PARENT-FIRST|PARENT-LAST]");
    help.addComment("List groups within this unittype.");
    help.addOption(getAllInfoHelpOption());
    help.addOption(getUseContextHelpOption());
    help.addOption(getListContextHelpOption());
    help.addOption(getOrderHelpOption());
    help.addArgument("[[!]<group-name-pattern>]", getPatternComment("group-name"));
    help.addArgument(
        "[PARENT-FIRST|PARENT-LAST]",
        "Defines the ordering of the list. This is usually not interesting - mostly used for -export/import options of this shell");
    help.addExamples("listgroups", "listgroups \"All units\"");
    hg.addHelp(help);

    help =
        new Help("setgroup <groupname> <parent-groupname>|NULL <description> <profile-name>|NULL");
    help.addComment("Add/change a group.");
    help.addOption(getUseContextHelpOption());
    help.addArgument(
        "<groupname>",
        "Adding: Any string. Enclose with double quotes if it contains spaces. Changing: Valid group name.");
    argCom =
        "A group can be a child of another group. In that case it will inherit the group parameters from the parent group. ";
    argCom +=
        "The group parameters will then be ANDed together to form one or more criteria for the group search.";
    help.addArgument("<parent-groupname>|NULL", argCom);
    help.addArgument("<description>", "A non-essential (for Fusion) description of the group.");
    argCom = "Will restrict the search performed by this group till this profile only. ";
    argCom +=
        "If a parent group is specified, the profile setting of that group will override this.";
    help.addArgument("<profile-name>|NULL", argCom);
    //		argCom = "If you like to create a time-rolling group (other terms might be 'time' and
    // 'monitor' groups), you must specify a time parameter. ";
    //		argCom += "The effect of this is that Fusion Core Server will change this group's time
    // parameter criteria on a periodic interval, ";
    //		argCom += "which in turn is extremely useful for a special Syslog Event copying group
    // parameters to a unit. The whole idea is to be able  ";
    //		argCom += "to monitor a certain event as it varies over time. In that case, specify a valid
    // unit type parameter. Please specify a System parameter name, ";
    //		argCom += "run 'help setparam' and read about the X flag and the name convention. Otherwise
    // set it to NULL";
    //		help.addArgument("<time-param-name>|NULL", argCom);
    //		argCom = "The time-format can be any string, but some characters (and group of characters)
    // take on a special meaning. A format like ";
    //		argCom += "'yyyyMMddHH' will populate (through Fusion Core Server) the time parameter with
    // something like '2010111814'. The formatting ";
    //		argCom += "rules are described in SimleDateFormat (search on the internet). If time
    // parameter is set and this format set to NULL, the ";
    //		argCom += "default value will be 'yyyyMMdd'.";
    //		help.addArgument("<time-format>|NULL", argCom);
    //		argCom = "When Fusion Core Server sets this value, it will subtract the offset (seconds)
    // from current time. ";
    //		argCom += "If timeparameter is set and this offset set to NULL, the default value will be
    // 0.";
    //		help.addArgument("<time-offset>|NULL", argCom);
    help.addExamples("setgroup Test NULL Test NULL");
    help.addExamples("setgroup SubTest Test SubTest NULL");
    hg.addHelp(help);

    help = new Help("delgroup <groupname>");
    help.addComment("Delete a group");
    help.addOption(getUseContextHelpOption());
    help.addArgument("<groupname>", "A valid groupname");
    help.addExamples("delgroup Test");
    hg.addHelp(help);

    help = new Help("listjobs [<job-name-pattern>] [DEP-FIRST|DEP-LAST]");
    help.addComment("List jobs within this unittype.");
    help.addOption(getAllInfoHelpOption());
    help.addOption(getUseContextHelpOption());
    help.addOption(getListContextHelpOption());
    help.addOption(getOrderHelpOption());
    help.addArgument("[[!]<job-name-pattern>]", getPatternComment("job-name"));
    help.addArgument(
        "[DEP-FIRST|DEP-LAST]",
        "Defines the ordering of the list. This is usually not interesting - mostly used for -export/import options of this shell");
    help.addExamples("listjobs", "listjobs Upgrade");
    hg.addHelp(help);

    String fileArg = "<file-version>|NULL";
    help =
        new Help(
            "setjob <jobname> <jobtype> REGULAR|DISRUPTIVE <groupname> <parent-jobname>|NULL <description> "
                + fileArg
                + " <unconfirmed-timeout> <stop-rules>|NULL [<repeat-count> <repeat-interval-sec>]");
    help.addComment(
        "Make a job. There is a variety of jobtypes to be made. Using job to perform provisioning is to leverage the full force of Fusion, instead"
            + "of the simple Profile/Unit provisioning.");
    help.addOption(getUseContextHelpOption());
    help.addArgument(
        "<jobname>", "Adding: Any string. Enclose names with double quotes if it contain spaces");
    argCom = "CONFIG       -  Change parameters/configuration/settings on CPE\n";
    argCom += "SOFTWARE     -  Upgrade/downgrade software/firmware on CPE\n";
    argCom +=
        "TR069_SCRIPT -  Upload a proprietary script/config-file/etc to the CPE - will be executed on CPE\n";
    argCom += "RESTART      -  Reboot the CPE\n";
    argCom += "RESET        -  Factory Reset on CPE\n";
    argCom += "SHELL        -  Execute a Fusion shell script upon provisioning of CPE\n";
    argCom +=
        "KICK         -  Will issue a 'kick' to the CPE, which will trigger a regular provisioning cycle\n";
    argCom +=
        "TELNET       -  Will execute a Telnet-script on the CPE (make sure Telnet-parameters are defined on unit/profile)\n";
    help.addArgument("<jobtype>", argCom);
    help.addArgument(
        "REGULAR|DISRUPTIVE",
        "Type of service window to work within. If set to REGULAR the job will obey the regular service window (allow the execution of the job within this window). Otherwise set to DISRUPTIVE (usually a more restricted time window).");
    help.addArgument(
        "<groupname>",
        "A valid group name, only units that matches this group parameters will be able to run the job");
    help.addArgument(
        "<parent-jobname>|NULL",
        "If the job depends upon another job, set the parent job here. Otherwise set to NULL");
    help.addArgument("<description>", "A non-essenital (for Fusion) description.");
    argCom =
        "The file to be used in the job (if the job type require a file). The version specified will be used to retrieve the file of the correct type:\n";
    argCom += "Job Type SOFTWARE     -> File Type SOFTWARE\n";
    argCom += "Job Type TR069_SCRIPT -> File Type TR069_SCRIPT\n";
    argCom += "Job Type SHELL        -> File Type SHELL_SCRIPT\n";
    argCom += "Job Type TELNET       -> File Type TELNET_SCRIPT\n";
    argCom += "For other job types no file can be specified\n";
    help.addArgument(fileArg, argCom);
    help.addArgument(
        "<unconfirmed-timeout>",
        "If a device doesn't reconnect after a job within this specified number of seconds, the job for this unit is considered 'unconfirmed failed'");
    argCom =
        "Stop rules tells a job to 'pause' or 'stop' when a certain condition occur. A stop rule can be specified on this ";
    argCom += "(regexp) format (a|u|c\\d+(/\\d+)?)|n\\d+. A more detailed explanation:\n";
    argCom += "\ta : any failure\n";
    argCom += "\tu : unconfirmed failure (the device does not respond)\n";
    argCom += "\tc : confirmed failure (the device responds with an error message)\n";
    argCom += "\tn : counter\n";
    argCom +=
        "\tRule example 1: a10/1000 : Will stop the job if 10 out of the last 1000 unit jobs has any failure\n";
    argCom +=
        "\tRule example 2: u5,n1000 : Will stop the job if 5 unconfirmed failure occurs or if 1000 unit jobs has been executed.\n";
    help.addArgument("<stop-rules>|NULL", argCom);
    help.addArgument(
        "[<repeat-count>]",
        "Optional. Set to a positive number to repeat a job. Normal behavious is to never repeat a job. This is useful for monitoring jobs.");
    help.addArgument(
        "[<repeat-interval-sec>]",
        "Optional. Set to a positive number to decide the interval of repeating jobs. This interval will shorten the periodic inform interval if necessary. Default is 86400 (24h).");
    help.addExamples(
        "setjob Upgrade SOFTWARE DISRUPTIVE \"All units\" NULL \"An upgrade job\" 0.9-alfa 600 \"u5,a100\" NULL NULL");
    help.addExamples(
        "setjob Test CONFIG REGULAR \"All units\" NULL \"A config job\" 1.2.3-alfa 600 a100 NULL NULL");
    hg.addHelp(help);

    help = new Help("deljob <jobname>");
    help.addComment("Deletes job.");
    help.addOption(getUseContextHelpOption());
    help.addArgument("<jobname>", "A valid jobname");
    help.addExamples("deljob Test");
    hg.addHelp(help);

    help = new Help("listsyslogevents");
    help.addComment("List syslog events within this unittype.");
    help.addOption(getAllInfoHelpOption());
    help.addOption(getUseContextHelpOption());
    help.addOption(getListContextHelpOption());
    help.addOption(getOrderHelpOption());
    hg.addHelp(help);

    help =
        new Help(
            "setsyslogevent <id> <name> <group-name>|NULL <expression> <store-policy> <script-name>|NULL <delete-limit>|NULL <description>");
    help.addComment(
        "Add/change a syslog event. A syslog event is an event which is trapped on the syslog server if a certain syslog message arrive.");
    help.addOption(getUseContextHelpOption());
    help.addArgument(
        "<id>",
        "Adding: The id must be a number larger or equal to 1000. Changing: A valid syslog event id.");
    help.addArgument("<name>", "A non-essential name (for Fusion) of the syslog event.");
    help.addArgument(
        "<group-name>|NULL",
        "If specified, the syslog-event will only match the units within the group");
    help.addArgument(
        "<expression>",
        "This is the expression with which syslog messages will be matched. Syntax:\n "
            + expressionSyntax
            + "The matching is partial - you don't need to match the entire syslog message with the "
            + "expression. A message will perform/execute the first event it matches, so avoid setting up events that match on the same "
            + "syslog message."
            + "Check out the matching using the command 'syslog sm=<expression>'");
    argCom =
        "Storepolicy defines what this syslog event will store the message. You may choose one out of these policies:\n";
    argCom +=
        "\tSTORE - This is the default task for all messages that arrive to the syslog server\n";
    argCom += "\tDISCARD - Discards the message, a useful event to throw away garbage messages.\n";
    argCom +=
        "\tDUPLICATE - Duplicate messages will only be printed once every 60 minutes (count will be updated)\n";
    help.addArgument("<store-policy>", argCom);
    help.addArgument("<script-name>", "If specified a script will be run upon the Syslog Event");
    help.addArgument(
        "<delete-limit>",
        "If specified, deletion limit will override the default severity limit specified in the Fusion Core Server configuration. It may delay or hasten "
            + "the deletion of specific type of syslog message. Default value is 0 (which is to say 'use the default severity limit')");
    help.addArgument(
        "<description>", "A non-essential (to Fusion) description of the syslog event.");
    help.addExample(
        "setsyslogevent 1000 SipRegFailed-monitor NULL \"reg failed\" STORE NULL NULL \"A monitor over sip registrations that failed\"");
    help.addExample(
        "setsyslogevent 2000 BlockedMsg-drop NULL \"gw: Blocked message\" DISCARD NULL 0 \"Will drop unneccessary messages from the device\"");
    help.addExample(
        "setsyslogevent 3777 Script-runner MyGroup \"DNS Error\" DUPLICATE setdebugflagonunit.xss 15 \"Will force a system-debug flag to 1 using a script\"");
    hg.addHelp(help);

    help = new Help("delsyslogevent <id>");
    help.addComment("Delete a syslog event");
    help.addOption(getUseContextHelpOption());
    help.addArgument("<id>", "A valid syslog event id");
    help.addExamples("delsyslogevent 5000");
    hg.addHelp(help);

    if (ACSVersionCheck.scriptExecutionSupported) {
      help = new Help("listexecutions [<match-expression>|NULL]");
      help.addComment(
          "List all script executions by ShellDaemon, usually from Trigger or SyslogEvent");
      help.addOption(getUseContextHelpOption());
      help.addOption(getListContextHelpOption());
      help.addOption(getOrderHelpOption());
      help.addArgument(
          "match-expression",
          "A regular expression string which will be matched against script-name, script-arguments and error-message");
      help.addExamples("listexecutions", "listtriggers Error");
      hg.addHelp(help);
    }

    if (ACSVersionCheck.heartbeatSupported) {
      help = new Help("listheartbeats [<match-expression>]|NULL]");
      help.addComment("List all \"Missing Heartbeat Detection\" definitions within the Unit Type");
      help.addOption(getUseContextHelpOption());
      help.addOption(getListContextHelpOption());
      help.addOption(getOrderHelpOption());
      help.addArgument(
          "match-expression",
          "A regular expression string which will be matched against name, group-name and expression");
      help.addExamples("listheartbeats", "listheartbeats Test");
      hg.addHelp(help);

      help = new Help("setheartbeat <name> <expression> <group> <timeout>");
      help.addComment(
          "Add/Change a \"Missing Heartbeat Detection\". By defining a heartbeat, Fusion Core server will generate "
              + "missing syslog messages and insert them into the syslog. The report system of Fusion can take advantage of these "
              + "messages to further improve the reports - now also detecting missing services. It can also be smart to build "
              + "syslog events and triggers to further act on the missing heartbeats syslog messages.");
      help.addOption(getUseContextHelpOption());
      help.addArgument("<name>", "The heartbeat name");
      help.addArgument(
          "<expression>",
          "An expression used to match against the syslog message. If no units in the group have "
              + "sent messages that matches this expression within the timeout period, then the heartbeat is considered \"missing\" "
              + "and a syslog message will be generated. Syntax:\n "
              + expressionSyntax
              + "To improve Fusion reports, the following expressions can be used - and they will be caught by the report system:\n"
              + "\t^ProvMsg\tMatches the ProvisioningHeartbeat\n"
              + "To test the expression and if it works, test it with the 'syslog sm=<expression>'\n");
      help.addArgument(
          "<group>",
          "The group name - the group should match on those units which emits \"heartbeat\" messages");
      help.addArgument(
          "<timeout>", "The timeout in number of hours. Must be a number between 1 and 48.");
      help.addExample("setheartbeat Device ^REP-DEVREP AllUnits 2");
      help.addExample("setheartbeat VoIP ^REP-VOIPREP VoIPUnits 2");
      hg.addHelp(help);

      help = new Help("delheartbeat <name>");
      help.addComment("Delete a heartbeat");
      help.addOption(getUseContextHelpOption());
      help.addArgument("<name>", "A heartbeat");
      help.addExamples("delheartbeat Device");
      hg.addHelp(help);
    }
    if (ACSVersionCheck.triggerSupported) {
      help = new Help("listtriggers [<match-expression>|NULL] [PARENT-FIRST|PARENT-LAST]");
      help.addComment("List all triggers within the Unit Type");
      help.addOption(getUseContextHelpOption());
      help.addOption(getListContextHelpOption());
      help.addOption(getOrderHelpOption());
      help.addArgument(
          "match-expression",
          "A regular expression string which will be matched against trigger-name, description, profile-name and group-name");
      help.addArgument(
          "[PARENT-FIRST|PARENT-LAST]",
          "Defines the ordering of the list. This is usually not interesting - mostly used for -export/import options of this shell");
      help.addExamples("listtriggers", "listtriggers DNS");
      hg.addHelp(help);

      help =
          new Help(
              "settrigger <name> <description> <type> <action> <active> <group-name> <evaluation-period-minutes> <notify-interval-hours> <script> <parent-triggername> <to-list> <syslog-event-id> <no-events-total> <no-events-pr-unit> <no-units> ");
      help.addComment("Add/Change a trigger");
      help.addOption(getUseContextHelpOption());
      help.addArgument("<name>", "The trigger name");
      help.addArgument("<description>", "A description of the trigger. May be set to NULL");
      help.addArgument("<type>", "The trigger type. Can be either BASIC or COMPOSITE");
      help.addArgument("<action>", "The type may be ALARM, REPORT, SCRIPT or SILENT.");
      help.addArgument(
          "<active>", "Activate or de-activate triger. Can be either \"true\" or \"false\"");
      //			help.addArgument("<group-name>", "A group name which the trigger should be based upon.
      // May be set to NULL");
      help.addArgument(
          "<evaluation-period-minutes>",
          "A value from 15 to 120. This is the time period for which the trigger may be fulfilled.");
      help.addArgument(
          "<notify-interval-hours>",
          "A value from 1 to 168. This is the minimum interval between each ALARM or REPORT. May be set to NULL.");
      help.addArgument(
          "<script>", "A script filename has to be set if <action> is SCRIPT. Otherwise NULL.");
      help.addArgument("<parent-triggername>", "A parent trigger. May be set to NULL.");
      help.addArgument(
          "<to-list>", "A list of comma separated email-addresses. May be set to NULL");
      help.addArgument(
          "<syslog-event-id>",
          "Set only if <type> is BASIC, otherwise NULL or skipped. A syslog event id which the trigger should be based upon.");
      help.addArgument(
          "<no-events-total>",
          "Set only if <type> is BASIC, otherwise NULL or skipped. A value larger than 0. The number of events which is needed to fulfill the trigger.");
      help.addArgument(
          "<no-events-pr-unit>",
          "Set only if <type> is BASIC, otherwise NULL or skipped. A value larger than 0. The number of events pr unit which is needed to fulfill the trigger");
      help.addArgument(
          "<no-units>",
          "Set only if <type> is BASIC, otherwise NULL or skipped. A value larger than 0. The number of units needed to fulfill the trigger");
      help.addExample(
          "settrigger CompDNS \"ALARM on DNS troubles\" COMPOSITE REPORT true 15 1 NULL NULL senior@lab.com NULL NULL NULL NULL");
      help.addExample(
          "settrigger BasicDNS \"ALARM on DNS troubles\" BASIC ALARM true 15 1 NULL CompDNS junior@lab.com 3100 1 1 1");
      hg.addHelp(help);

      help = new Help("deltrigger <name>");
      help.addComment("Delete a trigger");
      help.addOption(getUseContextHelpOption());
      help.addArgument("<name>", "A triggername");
      help.addExamples("deltrigger DNS");
      hg.addHelp(help);
    }

    //		help = new Help("makegroupmonitor <syslog-event-id> <syslog-expression> <group-name>
    // <time-parameter> <time-rolling-format>|NULL");
    //		help.addComment("This command is a combined/wizard command in the sense that you could
    // achieve the same as this command does by running 2 or 3 other commands. "
    //				+ "The purpose of the command it to create a monitoring of a syslog message and to set up
    // the correct syslog event and the groups needed to monitor. "
    //				+ "It will actually also create the unit type parameter if necessary. The group that is
    // created is time-rolling/monitor group, and the syslog event "
    //				+ "is has a GROUPSYNC task. Together with Fusion Core Server (which must produce Group
    // report) and Fusion Web (which shows the results) this will enable a "
    //				+ "powerful feature to monitor any syslog message expression that the device might send.
    // However, creating a lot of syslog events will reduce the performance " + "of the syslog
    // server.");
    //		help.addOption(getUseContextHelpOption());
    //		help.addArgument("<syslog-event-id>", "The id must be a number larger or equal to 1000.");
    //		help.addArgument("<syslog-expression>", "This is the expression with which syslog messages
    // will be matched. The mathing is performed by a regular expression matcher "
    //				+ "(check internet resources on this topic). The matching is partial - you don't need to
    // match the entire syslog message with the "
    //				+ "expression. As of today, a message will perform the first event it matches, so avoid
    // setting up events that match on the same " + "syslog message.");
    //		help.addArgument("<group-name>", "Any string. Enclose with double quotes if it contains
    // spaces.");
    //		argCom = "If you like to create a time-rolling group (other terms might be 'time' and
    // 'monitor' groups), you must specify a time parameter. ";
    //		argCom += "The effect of this is that Fusion Core Server will change this group's time
    // parameter criteria on a periodic interval, ";
    //		argCom += "which in turn is extremely useful for a special Syslog Event copying group
    // parameters to a unit. The whole idea is to be able  ";
    //		argCom += "to monitor a certain event as it varies over time. In that case, specify a valid
    // unit type parameter. Please specify a System parameter name, ";
    //		argCom += "run 'help setparam' and read about the X flag and the name convention.";
    //		help.addArgument("<time-param-name>", argCom);
    //		argCom = "The time-format can be any string, but some characters (and group of characters)
    // take on a special meaning. A format like ";
    //		argCom += "'yyyyMMddHH' will populate (through Fusion Core Server) the time parameter with
    // something like '2010111814'. The formatting ";
    //		argCom += "rules are described in SimleDateFormat (search on the internet). If time
    // parameter is set and this format set to NULL, the ";
    //		argCom += "default value will be 'yyyyMMdd'.";
    //		help.addArgument("<time-rolling-format>|NULL", argCom);
    //		help.addExamples("makegroupmonitor 1000 \"reg failed\" \"All units\"
    // System.X_COMPANY-COM.Monitor.SipRegFailed NULL");
    //		help.addExamples("makegroupmonitor 1000 \"reg failed\" \"All units\"
    // System.X_COMPANY-COM.Monitor.SipRegFailed yyyyMMddHH");
    //		hg.addHelp(help);

    //		help = new Help("enabletr069report <reporttype> <group-name> <interval> [<uploadURL>|NULL
    // <downloadURL>|NULL <ping-host>|NULL]");
    //		help.addComment("This command is a combined/wizard command in the sense that you could
    // achieve the same as this command does by running "
    //				+ "2-8 other commands. The purpose is to enable a TR-069 report to get information from
    // the devices regarding a number of interesting metrics. "
    //				+ "Make sure you enable the report in Fusion Core Server configuration in addition to this
    // operation. Each monitoring operation "
    //				+ "may require up till 4 jobs running (GatewayTR requires 4 jobs to run, the others 1
    // job). The GatewayTR monitoring is also "
    //				+ "demanding on the network, since they can run upload-test and download-test. All in all,
    // monitoring using TR-069 metrices "
    //				+ "cost resources of your system. It's therefore a good advice to not set up all units to
    // do monitoring, and not to do it all the time.");
    //		help.addOption(getUseContextHelpOption());
    //		help.addArgument("<reporttype>", "The reporttype can be one out of three:\n" + "\tHardwareTR
    //   - A report for temperature, cpu-usage, memory, etc\n"
    //				+ "\tGatewayTR    - A report for upload/download speed, ping-time, WAN-uptime\n" +
    // "\tVoipTR       - A report for voip-quality, call-length, loss, etc\n"
    //				+ "Note that most devices probably wont support these metrices, but some will support some
    // of it. A very few might support all of them.");
    //		help.addArgument("<group-name>", "A valid group name within this unittype. One piece of
    // advice: Do not make the group too large (test with 100, "
    //				+ "then 1000 units), it will put a huge strain on the system.");
    //		help.addArgument("<interval>", "An interval in minutes. Do not set it too low (test with
    // 1440, then 60 minutes), it will put a huge strain on the system.");
    //		help.addArgument("<downloadURL>|NULL", "If you want to run GatewayTR monitoring you can
    // offer a downloadURL. This URL will be executed from the device "
    //				+ "to test the download-speed. You may skip it (setting to NULL).");
    //		help.addArgument("<uploadURL>|NULL", "If you want to run GatewayTR monitoring you can offer
    // a uploadURL. This URL will be executed from the device "
    //				+ "to test the upload-speed. You may skip it (setting to NULL).");
    //		help.addArgument("<pingHost>|NULL", "If you want to run GatewayTR monitoring you can offer a
    // ping host. This hostname will be used from the device "
    //				+ "to test the ping times. You may skip it (setting to NULL).");
    //		help.addExample("enabletr069report HardwareTR monitor-group 60");
    //		help.addExample("enabletr069report GatewayTR monitor-group 60
    // http://xaps.comp.com/xapstr069/file/uptest http://xaps.comp.com/xapstr069/file/downtest
    // xaps.com.com");
    //		hg.addHelp(help);

    help = new Help("listexecutions [<match-expression>]|NULL]");
    help.addComment("List all script executions run by Core Script Executor");
    help.addOption(getUseContextHelpOption());
    help.addOption(getListContextHelpOption());
    help.addOption(getOrderHelpOption());
    help.addArgument(
        "match-expression",
        "A regular expression string which will be matched against script, args and request-Id");
    help.addExamples("listexecutions", "listexecutions Test");
    hg.addHelp(help);

    help = new Help("setexecution <fusion-script-file> <args>|NULL <request-id>|NULL");
    help.addComment("Add/Change a shell script execution in the Core server.");
    help.addOption(getUseContextHelpOption());
    help.addArgument("<script>", "A file of type SHELL_SCRIPT and optionally arguments");
    help.addArgument(
        "<request-id>", "A request id, to identify the source/requestor of the script execution");
    help.addExample("setexecution test.fss NULL");
    help.addExample("setexecution \"test.fss -uut:NPA201E-Pingcom\" MyRequest-1234");
    hg.addHelp(help);

    help = new Help("generatetc [test-case-id|VALUE|ATTRIBUTE]");
    help.addComment(
        "If the optional arugment is omitted, it will generate Test Cases for the TR-069 test based on the Unit type parameters available and TR-181 (v1.9), TR-104 (v1.1) and TR-098 (v1.4)");
    help.addArgument(
        "test-case-id",
        "Specify a \"master\" Test Case with many N SET parameters. The generate command will produce one TC for each SET parameter, from 1-N params");
    help.addArgument("VALUE", "Generate only VALUE test cases");
    help.addArgument("ATTRIBUTE", "Generate only ATTRIBUTE test cases");
    help.addExamples("generatetc", "generatetc 4049", "generatetc VALUE");
    hg.addHelp(help);

    help = new Help("deltcduplicates");
    help.addComment("Delete duplicate Test Cases");
    hg.addHelp(help);

    help = new Help("listtc [<method>|NULL [<parameter-filter>|NULL [<tag-filter>|NULL]]]");
    help.addComment("List all or a subset of Test Cases");
    help.addArgument(
        "<method>|NULL",
        "Optional. Could be set to VALUE, ATTRIBUTE or FILE. Setting it to NULL will match all (methods)");
    help.addArgument(
        "<parameter-filter>|NULL",
        "Optional if the previous argument is specified. Could be set to a part of a parameter name. Setting it to NULL will match all (parameters)");
    help.addArgument(
        "<tag-filter>|NULL",
        "Optional if the previous argument is specified. Could be set to a series of strings enclosed in square brackets [ ] to match tags. Setting it to NULL will match all (tags)");
    help.addExample("listtc NULL NULL NULL");
    help.addExample("listtc VALUE WANConnection \"[READONLY][GENERATED]\"");
    hg.addHelp(help);

    help = new Help("listtctags [<method>|NULL [<parameter-filter>|NULL [<tag-filter>|NULL]]]");
    String comment =
        "List all tags for these test cases, and count the number of test case occurrences for each tag. ";
    comment +=
        "The tags used are (according to the format of a test case) entirely freely chosen values - hence the meaning ";
    comment +=
        "of a tag is up to the creator of a test case. However, using the generatetc command, a number of tags will be ";
    comment += "created, and these have a specific meaning:\n";
    comment += "\tCOMPLEX     - Contains multiple parameters\n";
    comment += "\tFALSE       - Tests only boolean parameter with all values set to 'false'\n";
    comment += "\tGENERATED   - Test case generated by generatetc command\n";
    comment += "\tLARGE       - Test contains many parameters\n";
    comment +=
        "\tLAYERx      - Testing all parameters on layer x in the unittype. Layer 0 is ALL parameters, Layer 1 could be all params within DeviceInfo object, etc\n";
    comment += "\tREADONLY    - Contains only read-only parameters - no use running SET method\n";
    comment += "\tREADWRITE   - Contains only read-write parameters\n";
    comment += "\tSIMPLE      - Contains one single parameter\n";
    comment += "\tTEMPLATE    - Considered a template for you to modify\n";
    comment += "\tTRUE        - Tests only boolean parameter with all values set to 'true'\n";
    comment += "\txsd:<type>  - Tests only parameter of this xsd type\n";
    help.addComment(comment);
    help.addOption(getOrderHelpOption());
    help.addArgument(
        "<method>|NULL",
        "Optional. Could be set to VALUE, ATTRIBUTE or FILE. Setting it to NULL will match all (methods)");
    help.addArgument(
        "<parameter-filter>|NULL",
        "Optional if the previous argument is specified. Could be set to a part of a parameter name. Setting it to NULL will match all (parameters)");
    help.addArgument(
        "<tag-filter>|NULL",
        "Optional if the previous argument is specified. Could be set to a series of strings enclosed in square brackets [ ] to match tags. Setting it to NULL will match all (tags)");
    help.addExample("listtc NULL NULL NULL");
    help.addExample("listtc VALUE WANConnection \"[READONLY][GENERATED]\"");
    hg.addHelp(help);

    help = new Help("showtc <id>");
    help.addComment("Show the Test Case for the given id, as it would be if exported to file");
    help.addArgument("<id>", "A valid Test Case id (get from listtc command)");
    help.addExample("showtc 123");
    hg.addHelp(help);

    help = new Help("deltc [<method>|NULL [<parameter-filter>|NULL [<tag-filter>|NULL]]]");
    help.addComment("Delete all or a subset of Test Cases");
    help.addArgument(
        "<method>|NULL",
        "Optional. Could be set to VALUE or ATTRIBUTE. Setting it to NULL will match all (methods)");
    help.addArgument(
        "<parameter-filter>|NULL",
        "Optional if the previous argument is specified. Could be set to a part of a parameter name. Setting it to NULL will match all (parameters)");
    help.addArgument(
        "<paratagmeter-filter>|NULL",
        "Optional if the previous argument is specified. Could be set to a series of strings enclosed in square brackets [ ] to match tags. Setting it to NULL will match all (tags)");
    help.addExample("deltc NULL NULL NULL");
    help.addExample("deltc VALUE WANConnection \"[READONLY][GENERATED]\"");
    hg.addHelp(help);

    help = new Help("exporttcfile <directory> <id>");
    help.addComment("Export one single Test Cases");
    help.addArgument("<directory>", "A directory to where the Test Case file will be exported");
    help.addArgument("<id>", "A valid Test Case id (get from listtc command)");
    help.addExample("exporttc tr069test 123");
    hg.addHelp(help);

    help =
        new Help(
            "exporttcdir <directory> [<method>|NULL [<parameter-filter>|NULL [<tag-filter>|NULL]]]");
    help.addComment("Export all or a subset of Test Cases");
    help.addArgument("<directory>", "A directory to where the Test Case files will be exported");
    help.addArgument(
        "<method>|NULL",
        "Optional. Could be set to VALUE or ATTRIBUTE. Setting it to NULL will match all (methods)");
    help.addArgument(
        "<parameter-filter>|NULL",
        "Optional if the previous argument is specified. Could be set to a part of a parameter name. Setting it to NULL will match all (parameters)");
    help.addArgument(
        "<paratagmeter-filter>|NULL",
        "Optional if the previous argument is specified. Could be set to a series of strings enclosed in square brackets [ ] to match tags. Setting it to NULL will match all (tags)");
    help.addExample("exporttc NULL NULL NULL");
    help.addExample("exporttc VALUE WANConnection \"[READONLY][GENERATED]\"");
    hg.addHelp(help);

    help = new Help("importtcfile <filename>");
    help.addComment("Import a Test Case");
    help.addArgument("<filename>", "A Test Case file to be imported");
    help.addExample("importtcfile tr069test/2930.tc");
    hg.addHelp(help);

    help = new Help("importtcdir <directory>");
    help.addComment("Import all Test Cases in a directory");
    help.addArgument("<directory>", "A directory from where all Test Case files will be imported");
    help.addExample("importtcdir tr069test");
    hg.addHelp(help);

    hg.addHelp(getListTestHistoryHelp());

    help = new Help("deltesthistory [startTms|NULL [endTms|NULL]]");
    help.addComment("Delete the test history of this unit.");
    help.addArgument(
        "startTms",
        "Optional, can also be set to NULL (to delete from beginning). Otherwise specify date using syntax from syslog-command");
    help.addArgument(
        "endTms",
        "Optional, can also be set to NULL (to delete until end). Otherwise specify date using syntax from syslog-command");
    help.addExamples(
        "deltesthistory", "deltesthistory 3d 1h", "deltesthistory 20120701 20120705-1300");
    hg.addHelp(help);

    help = new Help("validateflags");
    help.addComment(
        "Validate the unittype parameter flags according to the TR-069 datamodel (TR-098, TR-104 and TR-181). It will only list those entries which violates the datamodel");
    help.addExamples("validateflags");
    hg.addHelp(help);

    //		help = new Help("listtesthistory");
    //		help.addComment("List the test history of this Unit Type.");
    //		hg.addHelp(help);
    //		help = new Help("deltesthistory");
    //		help.addComment("Delete the test history of this Unit Type.");
    //		hg.addHelp(help);

    return hg;
  }

  private HelpGroup rootHelpBuilder() {
    HelpGroup hg = new HelpGroup("Root");
    Help help;

    help = new Help("listunittypes [<unittype-name-pattern>]");
    help.addComment("Lists the unittypes available in Fusion");
    help.addOption(getAllInfoHelpOption());
    help.addOption(getUseContextHelpOption());
    help.addOption(getOrderHelpOption());
    String argCom;
    argCom = "Optional. Any string which will be used to match the list of unittype names. ";
    argCom += "The string will be interpreted as a regular expression ";
    argCom += "which is a very powerful matching language. If you want ";
    argCom += "to know how to take full advantage of regular expressions ";
    argCom += "you need to consult internet resources.";
    help.addArgument("[<unittype-name-pattern>]", argCom);
    help.addExamples("list", "list NPA", "list ^NPA", "list NPA\\d01");
    hg.addHelp(help);

    help = new Help("listunits [<search-value>]");
    help.addComment("List/search for units in Fusion");
    help.addOption(getAllInfoHelpOption());
    help.addOption(getUseContextHelpOption());
    help.addOption(getListContextHelpOption());
    help.addOption(getOrderHelpOption());
    help.addArgument(
        "[<search-value>]",
        "Optional. If this is the only argument, it will search among all unit parameters and unit-ids. ");
    help.addExamples("listunits AABBCC0123", "listunits \"John Doe\"");
    hg.addHelp(help);

    help =
        new Help(
            "setunittype <unittype-name> TR-069|N/A (<vendor> <description>)|<unittype-file> ");
    help.addComment("Add/change a unittype. No parameters are added.");
    help.addOption(getUseContextHelpOption());
    help.addArgument(
        "<unittype-name>", "Any string if adding. If changing it must be a valid unittype name.");
    help.addArgument("TR-069|N/A", "Set to N/A for all other protocols (HTTP/TFTP) than TR-069.");
    help.addArgument("<vendor>", "Non-essential (to Fusion) information");
    help.addArgument("<description>", "Non-essential (to Fusion) information");
    argCom =
        "A file in the filesystem (cannot retrieve from Fusion filestore). Use / as directory path delimiter. The file must ";
    argCom += "be a specially made xml-file with information on how to build a complete unittype.";
    help.addArgument("<unittype-file>", argCom);
    help.addExample("setunittype NPA201E TR-069 \"Ping Communication\" \"Two port ATA\"");
    help.addExample("setunittype NPA201E TR-069 unittypedef/npa201e/npa201e-34707-master.xml");
    hg.addHelp(help);

    help = new Help("delunittype <unittype-name>");
    argCom =
        "Delete a unittype. The command will not succeed if profiles/groups/jobs/software/files exists in this unittype. ";
    argCom +=
        "For a complete delete procedure, use the -delete option when executing xapsshell.jar";
    help.addComment(argCom);
    help.addOption(getUseContextHelpOption());
    help.addArgument("<unittype-name>", "Must be a valid unittype name.");
    help.addExample("delunittype NPA201E");
    hg.addHelp(help);

    help = new Help("listparams [staging]");
    help.addComment("Lists system parameters used in all unittypes.");
    help.addOption(getUseContextHelpOption());
    help.addOption(getOrderHelpOption());
    help.addArgument(
        "[staging]",
        "Optional. If set to 'staging' all system parameters regarding staging will be shown. Staging is a special concept, usually only for CPE vendors");
    help.addExamples("listparams", "listparams staging");
    hg.addHelp(help);

    help = new Help("listusers [<username-pattern>]");
    help.addComment("List users in Fusion");
    help.addOption(getUseContextHelpOption());
    help.addOption(getOrderHelpOption());
    argCom = "Optional. Any string which will be used to match the list of users. ";
    argCom += "The string will be interpreted as a regular expression ";
    argCom += "which is a very powerful matching language. If you want ";
    argCom += "to know how to take full advantage of regular expressions ";
    argCom += "you need to consult internet resources.";
    help.addArgument("[<username-pattern>]", argCom);
    help.addExamples("listusers", "listusers admin", "listusers comp.*");
    hg.addHelp(help);

    help = new Help("setuser <username> <fullname> <secret> <accesslist>|NULL <admin>");
    help.addComment("Add/change user.");
    help.addOption(getUseContextHelpOption());
    help.addArgument(
        "<username>", "Any string if adding. If changing it must be a valid user name.");
    help.addArgument("<fullname>", "The full name of the user");
    argCom = "The password of the user. Will be stored in the database as a hash of the password. ";
    argCom +=
        "Thus if the password is forgotten, it cannot be retrieved. However, using this command it can be ovewritten ";
    argCom +=
        "without any problem. The user/password information is needed to login to Fusion Web and Fusion Web Services.";
    help.addArgument("<secret>", argCom);
    argCom =
        "A comma-separated list of pages that the user can access in Fusion Web. The valid pages are\n";
    argCom += "\tsupport              - Support-related pages\n";
    argCom += "\tlimited-provisioning - Some of the provisioning pages\n";
    argCom += "\tfull-provisioning    - All pages except staging\n";
    argCom += "\treport               - Reports\n";
    argCom += "\tstaging              - Staging pages \n";
    argCom += "\tmonitor              - Monitor page\n";
    argCom += "Setting access to NULL  will allow all pages!";
    help.addArgument("<accesslist>|NULL", argCom);
    argCom =
        "Can be set to \"true\" or \"false\". If set to \"true\" the user will have access to all unittypes and profiles ";
    argCom +=
        "regardless of any permissions added to this user later. If set to \"false\" the user will have no permissions ";
    argCom +=
        "unless explicitely given permissions (using setperm). The flag cannot be set to true unless the user running the command ";
    argCom += "is itself an admin.";
    help.addArgument("<admin>", argCom);
    help.addExample("setuser admin Administrator supersecret NULL true");
    help.addExample("setuser support Support supersecret full-provisioning,staging false");
    hg.addHelp(help);

    help = new Help("deluser <username>");
    help.addComment("Delete a user, but requires that no permissions are attached to this user.");
    help.addOption(getUseContextHelpOption());
    argCom =
        "Must be a valid username. If 'admin' user is deleted, a default 'admin' user will still ";
    argCom += "exist in Fusion. Then it will have the password 'xaps'";
    help.addArgument("<username>", argCom);
    help.addExample("deluser support");
    hg.addHelp(help);

    help = new Help("listperms [<unittype-name-pattern>]");
    argCom =
        "List permissions in Fusion. Each permission is attached to a user and controls which unittypes/profiles are allowed for that user. ";
    argCom +=
        "If a permission for a unittype is created with NULL as profile value, no other permissions for that unittype can be created.";
    help.addComment(argCom);
    help.addOption(getUseContextHelpOption());
    help.addOption(getOrderHelpOption());
    argCom = "Optional. Any string which will be used to match the unittype of the permissions. ";
    argCom += "The string will be interpreted as a regular expression ";
    argCom += "which is a very powerful matching language. If you want ";
    argCom += "to know how to take full advantage of regular expressions ";
    argCom += "you need to consult internet resources.";
    help.addArgument("[<unittype-name-pattern>]", argCom);
    help.addExamples("listperms", "listperms NPA201E");
    hg.addHelp(help);

    help = new Help("setperm <username> <unittype-name> <profile-name>|NULL");
    help.addComment("Add a permission. Change not possible (have to delete first).");
    help.addOption(getUseContextHelpOption());
    help.addArgument("<username>", "A valid user name.");
    help.addArgument("<unittype-name>", "A valid unittype name");
    help.addArgument(
        "<profile-name>|NULL",
        "If NULL, then all profiles within the unittype is permitted. Else only the specified profile is permitted.");
    help.addExamples("setperm support NPA201E NULL", "setperm support NPA201E Default");
    hg.addHelp(help);

    help = new Help("delperm <username> <unittype-name> <profile-name>|NULL");
    help.addComment("Delete a permission.");
    help.addOption(getUseContextHelpOption());
    help.addArgument("<username>", "A permission user name.");
    help.addArgument("<unittype-name>", "A valid permission unittype name");
    help.addArgument("<profile-name>|NULL", "A valid permission profile name or NULL");
    help.addExamples("delperm support NPA201E NULL", "delperm support NPA201E Default");
    hg.addHelp(help);

    help = new Help("listcertificates");
    help.addComment(
        "List certificates in Fusion. Shows the complete certificate string in both encrypted version and clear-text");
    help.addOption(getUseContextHelpOption());
    help.addOption(getOrderHelpOption());
    hg.addHelp(help);

    help = new Help("setcertificate <name> <certificate>");
    help.addComment(
        "Add/change a certificate. Will not allow all kinds of changes (going from production to test certificate is not allowed)");
    help.addOption(getUseContextHelpOption());
    help.addArgument(
        "<name>",
        "The name of the certificate is not important. Fusion will recognize the type no matter the name");
    argCom =
        "A hexadecimal 256-char string which is the certificate.Certificates can only be retrieved from Ping Communication. ";
    argCom += "Currently there are two certificates supported in Fusion: Report and Provisioning.";
    help.addArgument("<certificate>", argCom);
    help.addExample(
        "setcertificate Test E9D66AABBC012345E9D66AABBC012345E9D66AABBC012345E9D66AABBC012345E9D66AABBC012345E9D66AABBC012345E9D66AABBC012345E9D66AABBC012345E9D66AABBC012345E9D66AABBC012345E9D66AABBC012345E9D66AABBC012345E9D66AABBC012345E9D66AABBC012345E9D66AABBC012345E9D66AABBC012345");
    hg.addHelp(help);
    help = new Help("delcertificate <name>");
    help.addComment("Delete a certificate. Make sure to have backed up the certificate string.");
    help.addArgument("<name>", "A valid certificate name");
    help.addExample("delcertificate Test");
    hg.addHelp(help);

    return hg;
  }

  private HelpGroup genericHelpBuilder() {
    HelpGroup hg = new HelpGroup("Generic");

    Help help = new Help("cc <context>");
    help.addComment(
        "Change to another context. The context change is fairly robust, so you can make context changes directly from, say a unit context  to "
            + "a group context, if only the group specified is within the same unit type.");
    String context =
        "Specify context to change to.\n"
            + "Syntax: .. or /(<type>:<contextname>/)*\n"
            + "Use .. to go one step back in the context hiearchy.. Types are:\n\t'ut' (Unit Type)\n\t'pr' (Profile)\n\t'un' (Unit)\n\t'gr' (Group)\n\t'jo' (Job)\n\t"
            + "'up' (Unit Type Parameter)\nUse a single '/' to specify root. If context name contains "
            + "whitespace, enclose the entire context with quotes: \"/ut:Test UT/\".";
    help.addArgument("<context>", context);
    help.addExamples(
        "cc ..",
        "cc /",
        "cc /ut:TestUnittype/pr:TestProfile",
        "cc \"/ut:Test Unit Type/\"",
        "cc /gr:MyGroup");
    hg.addHelp(help);

    help = new Help("unit [<search-value>]");
    help.addComment(
        "Switch context to a particular unit. If more than 1 unit is found, all the units are "
            + "listed. If within a unit type context, all parameters with the D(isplayeble) flag are shown.");
    String argCom = "Optional. Searches among all unit parameters values and unit-ids. ";
    argCom += "Possible to use control characters to specify search more closely:\n";
    argCom += "* (asterix)      - 0 or more wildchars\n";
    argCom += "_ (underscore)   - 1 wildchar\n";
    argCom += "^ (circumflex)   - start of string\n";
    argCom += "$ (dollar)       - end of string\n";
    argCom += "! (exclamation)  - negation of search\n";
    help.addArgument("[<search-value>]", argCom);

    help.addExamples("unit", "unit AABBCC0123", "unit \"John Doe\"");
    hg.addHelp(help);

    help = new Help("syslog [<search-args>] [<list-args>]");
    help.addComment("Search syslog and list the results in various ways");
    help.addOption(getListContextHelpOption());
    help.addOption(getUseContextHelpOption());
    help.addOption(getOrderHelpOption());
    argCom =
        "Optional. Default search is for everything the last 24h, but only return the 100 most recent entries. If search argument contains whitespace, enclose with double quotes. The search argument syntax is:\n"
            + "s(<option>)(,<option>)* Each option is described below: \n";
    argCom += "ts=<time>  - The start time. There are two ways to specify the <time> option:\n";
    argCom += "             \\d+(m|h|d) - specify as a number of (m)in, (h)our or (d)ays ago.\n";
    argCom += "             yyyyMMdd[-HH[mm]] - specify as a timestamp. Optional defaults to 0\n";
    argCom += "te=<time>  - The end time. There are two ways to specify the <time> option:\n";
    argCom += "             \\d+(m|h|d) - specify as a number of (m)in, (h)our or (d)ays ago.\n";
    argCom += "             yyyyMMdd[-HH[mm]] - specify as a timestamp. Optional defaults to 0\n";
    argCom += "             If ts is not specified, it will be set to 1d before ts.\n";
    argCom += "ev=\\d+     - Event-id\n";
    argCom +=
        "fn=.+      - Facility-name (ex: Device-Local<number>, Fusion TR069, Fusion Web, etc)\n";
    argCom += "fv=.+      - Facility-version (expr*)\n";
    argCom += "ip=.+      - An ip-address or part of an ip-address (expr*)\n";
    argCom += "m=.+       - Part of the syslog message. May use % or _ as wildchar. (expr*)\n";
    argCom += "s=(\\d)+   - One or more severities. 7=DEBUG, 6=INFO, 5=NOTICE, 4=WARN, 3=ERROR\n";
    argCom += "us=.+      - A user id (expr*)\n";
    argCom += "r=\\d+      - Max number of rows in the result. Default is 100\n";
    argCom +=
        "Some fields are marked with (expr*). These fields can be specifed using the following syntax:\n"
            + expressionSyntax;
    argCom +=
        "To search within a specific unittype, profile or unit, change to the appropriate context and search.\n";
    help.addArgument("[<search-args>]", argCom);

    argCom =
        "Optional. Default listing is timestamp, severity, facility, message, ipaddress, unitid, profile and unittype. The list argument syntax is:\n";
    argCom += "l(<column-type>)(,<column-type>)* Each column-type is described below: \n";
    argCom += "t     - Timestamp column\n";
    argCom += "ev    - Event id\n";
    argCom += "fn    - Facility name\n";
    argCom += "fv    - Facility version\n";
    argCom += "ip    - Ip address\n";
    argCom += "m     - Syslog message\n";
    argCom += "s     - Severity level\n";
    argCom += "us    - User name\n";
    argCom += "un    - Unit id\n";
    argCom += "pr    - Profile name\n";
    argCom += "ut    - Unit Type name\n";
    help.addArgument("[<list-args>]", argCom);

    help.addExample("syslog \"sts=1d,m=Hello World,s=5,6,7,r=120\"");
    help.addExample("syslog sts=20110515-12,ip=192,fv=4.2");
    help.addExample("syslog lun,t,fv,m");
    hg.addHelp(help);

    help = new Help("call <scriptname> ");
    help.addComment(
        "A call to another script. Possible to pass context-information and variables. ");
    context =
        "Specify context to run the script within. Default is to run in the same context as the callee.\n"
            + "Syntax: -c/(<type>:<contextname>/)*\n"
            + "Types are: 'ut' (Unit Type), 'pr' (Profile), 'un' (Unit), 'gr' (Group), 'jo' (Job) or "
            + "'up' (Unit Type Parameter). Use a single '/' to specify root. If context name contains "
            + "whitespace, enclose the entire option with quotes: \"-c/ut:Test UT/\".";
    help.addOption("-u", context);
    String optCom =
        "Specify variables to pass to the script. The variables are passed using the "
            + "variable name or a value\n"
            + "Syntax: -v(<var1>)(,<var2>)*\n"
            + "The variable names must be defined in the callee's context, if not the variable is passed as a value "
            + " and is then referred to using ${_1} for the first value, and ${_2} for the second value etc..";
    help.addOption("-v", optCom);
    argCom = "Can refer to scripts found in the filesystem or in Fusion. For referring to ";
    argCom +=
        "Fusion script files, the context cannot be root. In case a script has the same name ";
    argCom +=
        "in the filesystem and in Fusion, the Fusion script file is used. For filesystem scripts ";
    argCom += "use / as directory path delimiter.";
    help.addArgument("<scriptname>", argCom);
    help.addExample("call test.xss");
    help.addExample("call scripts/test.xss");
    help.addExample("call -c/ut:TestUnittype/pr:TestProfile/un:TestUnit/ test.xss ");
    help.addExample("call -vFoo,Bar test.xss");
    hg.addHelp(help);

    help = new Help("setvar <name> <expression>");
    argCom =
        "Setting a variable which may be read in this session. The variables will not be passed ";
    argCom +=
        "to a new script when using call, then you need to use -v option in the call command. ";
    argCom += "To reference a variable use ${name}. ";
    help.addComment(argCom);
    help.addArgument(
        "<name>",
        "Any name, but cannot start with a number, or an undescore or contain whitespace.");
    help.addArgument(
        "<expression>", "A Javascript expression. Use single quotes to specify a string value.");
    help.addExample("setvar Foo 'Hello'");
    help.addExample("setvar Bar 'Hello World'");
    help.addExample("setvar '${Bar}'+Test");
    help.addExample("setvar Sum 1 + 2");
    hg.addHelp(help);

    help = new Help("delvar <name>");
    help.addComment("Delete a variable, the variable can no longer be accessed in this session.");
    help.addArgument("<name>", "The variable name");
    help.addExample("delvar Foo");
    hg.addHelp(help);

    help = new Help("listvars");
    help.addComment("List all variables defined in this session");
    hg.addHelp(help);

    help = new Help("cat <filename>");
    help.addComment("Prints the content of the filename to the screen.");
    argCom = "Can refer to files found in the filesystem or in Fusion. For referring to ";
    argCom +=
        "Fusion script files, the context cannot be root. In case a script has the same name ";
    argCom +=
        "in the filesystem and in Fusion, the Fusion script file is used. For filesystem files ";
    argCom += "use / as directory path delimiter.";
    help.addArgument("<filename>", argCom);
    help.addExamples("cat test.xss", "cat scrips/test.xss");
    hg.addHelp(help);

    help = new Help("delosfile <filename>");
    help.addComment("Deletes a file found in the os filesystem.");
    help.addOption(getUseContextHelpOption());
    argCom = "Specify filename to delete. Use / as directory path delimiter.";
    help.addArgument("<filename>", "Specify filename to delete. Use / as directory path delimiter");
    help.addExamples("delosfile test.log");
    hg.addHelp(help);

    help = new Help("unittypeimport <unittypename>|ALL");
    help.addComment(
        "Import a unittype from a set of files previously exported using exportunittype command");
    help.addArgument(
        "<unittypename>|ALL",
        "Specify the unittype name or ALL for all unittypes. In both cases the "
            + "command expects a directory with this name to exist in working directory and it must contain "
            + "the files exported from exportunittype command");
    help.addExamples("unittypeimport ALL", "unittypeimport NPA201E");
    hg.addHelp(help);

    help = new Help("unittypeexport <unittypename>|ALL");
    help.addComment(
        "Export a unittype to a set of files in a directory with the same name as the unittype");
    help.addArgument(
        "<unittypename>|ALL",
        "Specify the unittype name or ALL for all unittypes. In both cases the "
            + "command will create a directory with this name and store all the exported files there.");
    help.addExamples("unittypeexport ALL", "unittypeexport NPA201E");
    hg.addHelp(help);

    help = new Help("unittypecompletedelete <unittypename>|ALL");
    help.addComment("Delete a unittype or all unittypes.");
    help.addArgument("<unittypename>|ALL", "Specify the unittype name or ALL for all unittypes. ");
    help.addExamples("unittypecompletedelete ALL", "unittypecompletedelete NPA201E");
    hg.addHelp(help);

    help = new Help("logout");
    help.addComment("Logout of the shell, will prompt a new login");
    hg.addHelp(help);

    help = new Help("dbinfo");
    help.addComment("Shows which db-user is used which database the shell is connected to");
    hg.addHelp(help);

    help = new Help("userinfo");
    help.addComment("Shows which fusion user is logged in");
    hg.addHelp(help);

    help = new Help("echo <text>");
    help.addComment(
        "Echos the <text> argument to screen. Will evaluate any references to variables");
    help.addArgument("<text>", "Any text");
    help.addExamples("echo \"Hello World\"", "echo ${VariableName}");
    hg.addHelp(help);

    help = new Help("exit");
    help.addComment("Exits Fusion Shell");
    hg.addHelp(help);

    help = new Help("help [<command>|generic]");
    help.addComment("Helptext for the various commands.");
    argCom = "Optional. If skipped help will show a list of commands available in this context. ";
    argCom +=
        "Use 'generic' to get list of generic commands. To get detailed help, specify a command ";
    argCom +=
        "name. There is no need to specify the whole command name, just specify enough to make ";
    argCom += "the argument non-ambigious.";
    help.addArgument("[<command>|generic]", argCom);
    help.addExamples("help", "help generic", "help cal", "help <unittype-name>");
    hg.addHelp(help);

    help = new Help("ls [<directory>]");
    help.addComment("Lists the content of the files in a directory");
    help.addArgument(
        "[<directory>]",
        "Optional. Default is the working directory. Use / as directory path delimiter.");
    help.addExamples("ls", "ls lib/.svn");

    hg.addHelp(help);

    help = new Help("sleep [<seconds>]");
    help.addComment("Sleeps/halts the shell for a number of seconds.");
    help.addArgument("[<seconds>]", "Optional. Default is 60 seconds.");
    hg.addHelp(help);

    help = new Help("pausescript");
    help.addComment("Pause the script execution until user hits RETURN");
    hg.addHelp(help);

    help = new Help("error <error-message>");
    help.addComment("Exit a script with an error message");
    hg.addHelp(help);
    return hg;
  }
}
