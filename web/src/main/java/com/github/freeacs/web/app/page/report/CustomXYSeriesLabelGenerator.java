package com.github.freeacs.web.app.page.report;

import org.jfree.chart.labels.StandardXYSeriesLabelGenerator;
import org.jfree.data.xy.XYDataset;

/**
 * Used in graphs to display links on legend details.
 *
 * @author Jarl Andre Hubenthal
 */
@SuppressWarnings("serial")
final class CustomXYSeriesLabelGenerator extends StandardXYSeriesLabelGenerator {
  private final String format;

  public CustomXYSeriesLabelGenerator(String format) {
    this.format = format;
  }

  public String generateLabel(XYDataset dataset, int index) {
    return String.format(format, index);
  }
}
