package com.github.freeacs.web.app.page.scriptexecution;

import com.github.freeacs.dbi.File;
import java.util.List;

public class Script {
  private List<ScriptArg> scriptArgs;
  private String title;
  private String description;
  private File script;

  public List<ScriptArg> getScriptArgs() {
    return scriptArgs;
  }

  public void setScriptArgs(List<ScriptArg> scriptArgs) {
    this.scriptArgs = scriptArgs;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public File getScript() {
    return script;
  }

  public void setScript(File script) {
    this.script = script;
  }
}
