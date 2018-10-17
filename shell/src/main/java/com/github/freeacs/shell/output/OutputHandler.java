package com.github.freeacs.shell.output;

import com.github.freeacs.shell.Context;
import com.github.freeacs.shell.Properties;
import com.github.freeacs.shell.command.Command;
import com.github.freeacs.shell.util.FileUtil;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class OutputHandler {
  private FileWriter fw;
  private boolean printedHeading;
  private Context context;
  private String heading;
  private Listing listing;
  private Command command;

  public void setHeading(String heading) {
    this.heading = heading;
  }

  public OutputHandler(Command command, Context context) throws IOException {
    this.context = context;
    this.command = command;
    this.listing = new Listing(context, command);
    if (command.getOutputFilename() != null) {
      if (Properties.isRestricted()) {
        File f = new File(command.getOutputFilename());
        if (!FileUtil.allowed("Write to " + command.getOutputFilename(), f)) {
          throw new IllegalArgumentException(
              "Abort command execution due to access restriction violations");
        }
      }
      fw = new FileWriter(command.getOutputFilename(), command.appendToOutput());
    }
  }

  public void print(String s) throws IOException {
    if (fw == null) {
      if (!printedHeading && heading != null) {
        context.print(heading);
        printedHeading = true;
      }
      context.print(s);
    } else {
      fw.write(s);
    }
  }

  public void close() throws IOException {
    if (fw != null) {
      fw.close();
    }
  }

  public Listing getListing() {
    return listing;
  }

  public Command getCommand() {
    return command;
  }

  public boolean toFile() {
    return fw != null;
  }
}
