package com.github.freeacs.web.app.page.scriptexecution;

import com.github.freeacs.dbi.ACS;
import com.github.freeacs.dbi.File;
import com.github.freeacs.dbi.FileType;
import com.github.freeacs.dbi.Profile;
import com.github.freeacs.dbi.ScriptExecution;
import com.github.freeacs.dbi.ScriptExecutions;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.web.Page;
import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.DropDownSingleSelect;
import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputDataIntegrity;
import com.github.freeacs.web.app.input.InputDataRetriever;
import com.github.freeacs.web.app.input.InputSelectionFactory;
import com.github.freeacs.web.app.input.ParameterParser;
import com.github.freeacs.web.app.menu.MenuItem;
import com.github.freeacs.web.app.page.AbstractWebPage;
import com.github.freeacs.web.app.util.ACSLoader;
import com.github.freeacs.web.app.util.SessionData;
import com.github.freeacs.web.app.util.WebConstants;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sql.DataSource;

public class ScriptExecutionsPage extends AbstractWebPage {
  private ScriptExecutionData inputData;

  private ACS acs;

  public void process(
      ParameterParser params,
      Output outputHandler,
      DataSource xapsDataSource,
      DataSource syslogDataSource)
      throws Exception {
    /* Parse input data to the servlet */
    inputData =
        (ScriptExecutionData) InputDataRetriever.parseInto(new ScriptExecutionData(), params);

    /* Retrieve the XAPS object from session */
    acs = ACSLoader.getXAPS(params.getSession().getId(), xapsDataSource, syslogDataSource);
    if (acs == null) {
      outputHandler.setRedirectTarget(WebConstants.LOGIN_URI);
      return;
    }

    /* Update (if necessary) the session state, so that unittype/profile context-menus are ok */
    InputDataIntegrity.loadAndStoreSession(
        params, outputHandler, inputData, inputData.getUnittype());

    //		System.out.println("Making unittype-dropdown.");
    /* Make the unittype-dropdown */
    DropDownSingleSelect<Unittype> unittypes =
        InputSelectionFactory.getUnittypeSelection(inputData.getUnittype(), acs);
    outputHandler.getTemplateMap().put("unittypes", unittypes);

    //		System.out.println("Calling action/output..");
    if (unittypes.getSelected() != null) {
      Script script = action(params, outputHandler, unittypes.getSelected());
      output(outputHandler, unittypes.getSelected(), script);
    }
    outputHandler.setTemplatePath("scriptexecution/scriptexecutions.ftl");
    //		System.out.println("Template set");
  }

  /**
   * Should output an scriptexecution, if an scriptexecution is chosen. If not a default "blank"
   * scriptexecution must be the output Will always output a list of all possible scriptexecutions
   * within this unittype.
   */
  private void output(Output outputHandler, Unittype unittype, Script script) throws Exception {
    Map<String, Object> fmMap = outputHandler.getTemplateMap();

    ScriptExecutions scriptExecutions = acs.getScriptExecutions();

    /* Output for the configuration */
    File selectedFile = null;
    if (script != null) {
      selectedFile = script.getScript();
    } else if (inputData.getId().getInteger()
        != null) { // Clicked on a script execution (in history-list)
      ScriptExecution se = scriptExecutions.getById(unittype, inputData.getId().getInteger());
      selectedFile = se.getScriptFile();
      script = parse(selectedFile, unittype);
      fmMap.put(
          "arguments",
          se.getArguments() != null ? se.getArguments().replaceAll("\"", "&quot;") : " ");
      if (se.getArguments() != null
          && !se.getArguments().isEmpty()) { // copy from arguments-array into ScriptArgs-values
        String[] args = StringUtil.split(se.getArguments());
        //				System.out.println(args);
        if (script != null && script.getScriptArgs() != null) {
          for (int i = 0; i < script.getScriptArgs().size() && i < args.length; i++) {
            ScriptArg scriptArg = script.getScriptArgs().get(i);
            //						System.out.println("Type: " + scriptArg.getType());
            if (scriptArg.getType() == ScriptArg.ArgType.FILE) {
              scriptArg.getFileDropDown().setSelected(unittype.getFiles().getByName(args[i]));
            }
            if (scriptArg.getType() == ScriptArg.ArgType.PROFILE) {
              scriptArg.getProfileDropDown().setSelected(unittype.getProfiles().getByName(args[i]));
            }
            if (scriptArg.getType() == ScriptArg.ArgType.ENUM) {
              Method m = ScriptExecutionData.class.getMethod("getArgument" + scriptArg.getIndex());
              Input in = (Input) m.invoke(inputData, (Object[]) null);
              //							System.out.println("Bork: " + in.getString());

              //							scriptArg.getEnumDropDown().getItems()
              //							scriptArg.getEnumDropDown().setSelected(new Enumeration(in.getString(),
              // in.getString()));
            }
            scriptArg.setValue(args[i]);
          }
        }
      }
      fmMap.put("requestid", se.getRequestId());
    } else if (inputData.getFormSubmit().getValue() == null
        && inputData.getFileId().getInteger() != null) { // Clicked on "Execute"-button i File page
      selectedFile = unittype.getFiles().getById(inputData.getFileId().getInteger());
      script = parse(selectedFile, unittype);
    }

    if (script != null && script.getScriptArgs() != null && !script.getScriptArgs().isEmpty()) {
      fmMap.put("scriptargs", script.getScriptArgs());
    }
    if (script != null && script.getTitle() != null) {
      fmMap.put("title", script.getTitle());
    }
    if (script != null && script.getDescription() != null) {
      fmMap.put("description", script.getDescription());
    }
    if (fmMap.get("info") == null && inputData.getInfo().getString() != null) {
      fmMap.put("info", inputData.getInfo().getString());
    }
    List<File> allScriptFiles = Arrays.asList(unittype.getFiles().getFiles(FileType.SHELL_SCRIPT));
    fmMap.put(
        "scripts",
        InputSelectionFactory.getDropDownSingleSelect(
            inputData.getFileId(), selectedFile, allScriptFiles));

    /* Output for the event list */
    List<ScriptExecution> execList =
        scriptExecutions.getExecutions(
            unittype, new Date(System.currentTimeMillis() - 24 * 3600 * 1000), null);
    Collections.reverse(execList);
    fmMap.put("scriptexecutions", execList);
  }

  private Script parse(File scriptFile, Unittype unittype) throws Exception {
    if (scriptFile == null) {
      return null;
    }

    Script script = new Script();
    script.setScript(scriptFile);
    String content = new String(scriptFile.getContent());
    String[] lines = content.split("\\n");
    List<ScriptArg> scriptArgList = new ArrayList<>();
    int index = 0;
    String title = null;
    String description = null;
    for (String line : lines) {
      if (line.startsWith("#:Argument")) {
        String[] fieldArray = line.substring(2).split(":");
        index++;
        String name = "Argument " + index;
        String type = "String";
        String comment = "";
        String value = "";
        if (fieldArray.length > 1) {
          name = fieldArray[1].trim();
        }
        if (fieldArray.length > 2) {
          type = fieldArray[2].trim();
        }
        if (fieldArray.length > 3) {
          value = fieldArray[3].trim();
          //					System.out.println("Parsed a value \"" + value + "\"");
        }
        if (fieldArray.length > 4) {
          comment = fieldArray[4].trim();
        }
        ScriptArg sif = new ScriptArg(index, name, type, comment, value, unittype, inputData);
        scriptArgList.add(sif);
      } else if (line.startsWith("#:Title")) {
        String[] array = line.substring(2).split(":");
        if (array.length > 1) {
          title = array[1];
        }
      } else if (line.startsWith("#:Description")) {
        String[] array = line.substring(2).split(":");
        if (array.length > 1 && description == null) {
          description = "";
        }
        description += array[1];
      }
    }
    script.setTitle(title);
    script.setDescription(description);
    script.setScriptArgs(scriptArgList);
    return script;
  }

  private static Pattern rangePattern = Pattern.compile("(\\d+)\\s*-\\s*(\\d+)?");

  private boolean validate(ScriptArg scriptArg, Unittype unittype) {
    if (scriptArg.getType() == ScriptArg.ArgType.INTEGER && scriptArg.getValidationRule() == null) {
      try {
        Integer.parseInt(scriptArg.getValue());
      } catch (NumberFormatException nfe) {
        scriptArg.setError("Not a number");
        return false;
      }
    } else if (scriptArg.getType() == ScriptArg.ArgType.INTEGER
        && scriptArg.getValidationRule() != null) {
      Matcher matcher = rangePattern.matcher(scriptArg.getValidationRule());
      if (matcher.matches()) {
        int from = Integer.parseInt(matcher.group(1));
        int to = Integer.MAX_VALUE;
        if (matcher.groupCount() == 2) {
          to = Integer.parseInt(matcher.group(2));
        }
        try {
          int number = Integer.parseInt(scriptArg.getValue());
          if (number < from || number > to) {
            scriptArg.setError("Number is outside range (" + to + "-" + from + ")");
            return false;
          }
        } catch (NumberFormatException nfe) {
          scriptArg.setError("Not a number");
          return false;
        }
      }
    } else if (scriptArg.getType() == ScriptArg.ArgType.PROFILE) {
      if (scriptArg.getValue() == null) {
        scriptArg.setError("No profile is chosen");
        return false;
      }
    } else if (scriptArg.getType() == ScriptArg.ArgType.FILE) {
      if (scriptArg.getValue() == null) {
        scriptArg.setError("No file is chosen");
        return false;
      }
    } else if (scriptArg.getType() == ScriptArg.ArgType.ENUM && scriptArg.getValue() == null) {
      scriptArg.setError("No option is chosen");
      return false;
    }
    return true;
  }

  /** Will execute a script. */
  private Script action(ParameterParser params, Output outputHandler, Unittype unittype)
      throws IllegalAccessException, InvocationTargetException {
    Map<String, Object> fmMap = outputHandler.getTemplateMap();
    ScriptExecutions scriptExecutions = acs.getScriptExecutions();
    if (inputData.getFormSubmit().isValue("Execute")) {
      if (inputData.getFileId().getInteger() != null) {
        try {
          File scriptFile = unittype.getFiles().getById(inputData.getFileId().getInteger());
          Script script = parse(scriptFile, unittype);
          int index = 1;
          String scriptArgs = "";
          boolean typeCheckOk = true;
          do {
            String argValue = params.getParameter("argument" + index);
            if (argValue == null) {
              break;
            }
            if (script != null
                && script.getScriptArgs() != null
                && script.getScriptArgs().size() >= index) {
              ScriptArg scriptArg = script.getScriptArgs().get(index - 1);
              try {
                if (scriptArg.getType() == ScriptArg.ArgType.PROFILE) {
                  Profile p = unittype.getProfiles().getById(Integer.valueOf(argValue));
                  scriptArg.getProfileDropDown().setSelected(p);
                  argValue = p.getName();
                } else if (scriptArg.getType() == ScriptArg.ArgType.FILE) {
                  File f = unittype.getFiles().getById(Integer.valueOf(argValue));
                  scriptArg.getFileDropDown().setSelected(f);
                  argValue = f.getName();
                } else if (scriptArg.getType() == ScriptArg.ArgType.ENUM) {
                  Method m =
                      ScriptExecutionData.class.getMethod("getArgument" + scriptArg.getIndex());
                  Input in = (Input) m.invoke(inputData, (Object[]) null);
                  scriptArg
                      .getEnumDropDown()
                      .setSelected(new Enumeration(in.getString(), in.getString()));
                  argValue = in.getString();
                }
              } catch (NumberFormatException nfe) {
                argValue = null;
              }
              scriptArg.setValue(argValue);
              if (!validate(scriptArg, unittype)) {
                typeCheckOk = false;
              }
            }
            if (argValue != null && argValue.contains(" ")) {
              argValue = "\"" + argValue + "\"";
            }
            scriptArgs += argValue + " ";
            index++;
          } while (true);
          if (scriptArgs.isEmpty()) {
            scriptArgs = inputData.getArguments().getString();
          }
          String requestId = inputData.getRequestid().getString();
          if (typeCheckOk) {
            scriptExecutions.requestExecution(scriptFile, scriptArgs, requestId);
            // To avoid Double-post of Execute-command:
            outputHandler
                .getServletResponse()
                .sendRedirect(
                    Page.SCRIPTEXECUTIONS.getUrl()
                        + "&info=Script execution initiated successfully");
          }
          return script;
        } catch (Throwable ex) {
          fmMap.put(
              "error",
              "Could not execute script (" + ex.getClass() + " : " + ex.getMessage() + ")");
        }
      } else {
        inputData.getFileId().setError("No script chosen");
        fmMap.put("file", inputData.getFileId());
      }
    }
    return null;
  }

  public List<MenuItem> getShortcutItems(SessionData sessionData) {
    List<MenuItem> list = new ArrayList<>(super.getShortcutItems(sessionData));
    list.add(new MenuItem("Unit type overview", Page.UNITTYPEOVERVIEW));
    list.add(new MenuItem("Files overview", Page.FILES));
    return list;
  }
}
