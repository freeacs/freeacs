package com.github.freeacs.shell.output;

import com.github.freeacs.dbi.Unit;
import com.github.freeacs.shell.Context;
import com.github.freeacs.shell.command.Command;
import com.github.freeacs.shell.command.Option;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Listing {
  public static String HEADER_UNITTYPE = "Unit Type";
  public static String HEADER_UNITTYPE_PARAMETER = "Unit Type Parameter Name";
  public static String HEADER_PROFILE = "Profile";
  public static String HEADER_UNIT = "Unit Id";
  public static String HEADER_GROUP = "Group";
  public static String HEADER_JOB = "Job";

  private Heading heading;
  private List<Line> lines = new ArrayList<>();
  private List<Column> columns = new ArrayList<>();
  private boolean printed;
  private Context context;
  private Command command;

  private static Pattern orderOptionArgPattern = Pattern.compile("(\\d+)(a|n)(a|d)");

  public Listing(Context context, Command command) {
    this.context = context;
    this.command = command;
  }

  public void setHeading(String... headings) {
    this.heading = new Heading(new Line(headings));
    addContextToHeading(this.heading, false);
    updateColumns(heading.getLine());
  }

  public void setHeading(Heading heading) {
    this.heading = heading;
    addContextToHeading(this.heading, false);
    updateColumns(heading.getLine());
  }

  public void setHeading(Heading heading, boolean unitList) {
    this.heading = heading;
    addContextToHeading(this.heading, unitList);
    updateColumns(heading.getLine());
  }

  private void insertIntoList(String strToInsert, Line line) {
    if (!line.getValues().isEmpty()) {
      String firstElement = line.getValues().get(0);
      if (!strToInsert.equals(firstElement)) {
        line.insertValue(0, strToInsert);
      }
    } else {
      line.addValue(strToInsert);
    }
  }

  private void addContextToHeading(Heading heading, boolean unitList) {
    if (command
        .getOptions()
        .containsKey(
            Option.OPTION_LIST_CONTEXT)) { // insert context-headings, make sure to avoid duplicate
      // headings
      if (context.getUnittypeParameter() != null) {
        insertIntoList(HEADER_UNITTYPE_PARAMETER, heading.getLine());
      }
      if (context.getJob() != null) {
        insertIntoList(HEADER_JOB, heading.getLine());
      }
      if (context.getGroup() != null && !unitList && context.getProfile() == null) {
        insertIntoList(HEADER_GROUP, heading.getLine());
      }
      if (context.getUnit() != null) {
        insertIntoList(HEADER_UNIT, heading.getLine());
      }
      if (unitList) {
        insertIntoList(HEADER_PROFILE, heading.getLine());
        insertIntoList(HEADER_UNITTYPE, heading.getLine());
      } else {
        if (context.getProfile() != null) {
          insertIntoList(HEADER_PROFILE, heading.getLine());
        }
        if (context.getUnittype() != null) {
          insertIntoList(HEADER_UNITTYPE, heading.getLine());
        }
      }
    }
  }

  private void addContextToLine(Line line, Unit unit) {
    if (command.getOptions().containsKey(Option.OPTION_LIST_CONTEXT)) { // insert context-values
      if (context.getUnittypeParameter() != null) {
        insertIntoList("up:" + context.getUnittypeParameter().getName() + "/", line);
      }
      if (context.getJob() != null) {
        insertIntoList("jo:" + context.getJob().getName() + "/", line);
      }
      if (context.getGroup() != null && unit == null && context.getProfile() == null) {
        insertIntoList("gr:" + context.getGroup().getName() + "/", line);
      }
      if (context.getUnit() != null) {
        insertIntoList("un:" + context.getUnit().getId() + "/", line);
      }
      if (unit != null) {
        insertIntoList("pr:" + unit.getProfile().getName() + "/", line);
        insertIntoList("/ut:" + unit.getUnittype().getName() + "/", line);
      } else {
        if (context.getProfile() != null) {
          insertIntoList("pr:" + context.getProfile().getName() + "/", line);
        }
        if (context.getUnittype() != null) {
          insertIntoList("/ut:" + context.getUnittype().getName() + "/", line);
        }
      }
    }
  }

  public void addLineRaw(String s) {
    Line line = new Line();
    line.addValueRaw(s);
    lines.add(line);
  }

  public void addLine(String... values) {
    Line line = new Line(values);
    addContextToLine(line, null);
    lines.add(line);
    updateColumns(line);
  }

  public void addLine(Line line) {
    addContextToLine(line, null);
    lines.add(line);
    updateColumns(line);
  }

  public void addLine(Line line, Unit unit) {
    addContextToLine(line, unit);
    lines.add(line);
    updateColumns(line);
  }

  /**
   * Public void addLine(Line line, Unit unit) { addContextToLine(line, unit); lines.add(line);
   * updateColumns(line); }
   */
  private void updateColumns(Line line) {
    for (int i = 0; i < line.getValues().size(); i++) {
      String value = line.getValues().get(i);
      if (columns.size() < i + 1) {
        columns.add(new Column());
      }
      Column column = columns.get(i);
      column.incWidthIfNecessary(value.length());
    }
  }

  public void printListing(OutputHandler oh) throws IOException {
    if (!printed) {
      printed = true;

      if (heading != null) {
        StringBuilder headingSb = new StringBuilder();
        for (int i = 0; i < heading.getLine().getValues().size(); i++) {
          String headingCol = heading.getLine().getValues().get(i);
          Column column = columns.get(i);
          headingSb.append(String.format("%-" + column.getMaxWidth() + "s ", headingCol));
        }
        oh.setHeading(headingSb + "\n");
      }
      Option orderOption = command.getOptions().get(Option.OPTION_ORDER);
      if (orderOption != null && orderOption.getOptionArgs() != null) {
        List<LineComparatorColumn> lineComparatorColumns = new ArrayList<>();
        Matcher m = orderOptionArgPattern.matcher(orderOption.getOptionArgs());
        int lastEnd = 0;
        while (m.find()) {
          int columnIndex = Integer.valueOf(m.group(1)) - 1;
          if (columnIndex >= 0 && columnIndex < heading.getLine().getValues().size()) {
            LineComparatorColumn lcc =
                new LineComparatorColumn(columnIndex, m.group(2), m.group(3));
            lineComparatorColumns.add(lcc);
            lastEnd = m.end();
          }
        }
        if (lastEnd + 1 < orderOption.getOptionArgs().length()) {
          context.println(
              "WARN: The o-option arguments contain errors, ordering may not be applied");
        }
        if (!lineComparatorColumns.isEmpty()) {
          lines.sort(new LineComparator(lineComparatorColumns));
        }
      }
      for (Line line : lines) {
        StringBuilder lineSb = new StringBuilder();
        for (int j = 0; j < line.getValues().size(); j++) {
          String value = line.getValues().get(j);
          if (columns.size() > j) {
            Column column = columns.get(j);
            lineSb.append(String.format("%-" + column.getMaxWidth() + "s ", value));
          } else { // raw/unformatted listing
            lineSb.append(value);
          }
        }
        oh.print(lineSb.toString());
        oh.print("\n");
      }
      if (!oh.toFile()
          && !command.getCommandAndArguments().get(0).getCommandAndArgument().contains("echo")) {
        oh.print(lines.size() + " entries printed\n");
      }
    }
  }

  public List<Line> getLines() {
    return lines;
  }
}
