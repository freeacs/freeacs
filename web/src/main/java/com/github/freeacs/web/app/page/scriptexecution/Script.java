package com.github.freeacs.web.app.page.scriptexecution;

import com.github.freeacs.dbi.File;
import lombok.Getter;

import java.util.List;

@Getter
public class Script {
  private List<ScriptArg> scriptArgs;
  private String title;
  private String description;
  private File script;

  public void setScriptArgs(List<ScriptArg> scriptArgs) {
    this.scriptArgs = scriptArgs;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setScript(File script) {
    this.script = script;
  }
}
