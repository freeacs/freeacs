package com.github.freeacs.web.app.page.scriptexecution;

import com.github.freeacs.dbi.File;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Script {
  private List<ScriptArg> scriptArgs;
  private String title;
  private String description;
  private File script;
}
