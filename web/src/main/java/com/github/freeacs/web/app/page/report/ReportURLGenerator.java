package com.github.freeacs.web.app.page.report;

import com.github.freeacs.web.app.util.WebConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYDataset;

/**
 * Used to generate links for url postback (zooming requests).
 *
 * @author Jarl Andre Hubenthal
 */
final class ReportURLGenerator extends org.jfree.chart.urls.CustomXYURLGenerator {
  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /** The chart. */
  JFreeChart chart;

  /** The format. */
  private String format;

  /** The aggregation. */
  private List<String> aggregation;

  /**
   * The constructor.
   *
   * <p>Initializes and updates the url format based on the aggregation keys given.
   *
   * @param format the format to use in the url, must adhere to String.format().
   * @param chart the report diagram
   * @param aggregation a list of strings that represent the currently selected aggregation keys
   */
  public ReportURLGenerator(String format, JFreeChart chart, List<String> aggregation) {
    this.format = format;
    this.chart = chart;
    this.aggregation = aggregation;
    if (aggregation == null) {
      throw new IllegalArgumentException("Aggregation key list is null");
    }
    StringBuilder aggregateKeysAndValuesPointers = new StringBuilder();
    for (String aggr : aggregation) {
      aggregateKeysAndValuesPointers.append("&").append(aggr.toLowerCase()).append("=%s");
    }
    this.format = this.format.replace("%AGGREGATION%", aggregateKeysAndValuesPointers.toString());
  }

  /**
   * Should not be used directly, is used by JFreeChart.
   *
   * @param dataset the dataset
   * @param series the series
   * @param item the item
   * @return the string
   */
  public String generateURL(XYDataset dataset, int series, int item) {
    Object[] os = getObjects(series, item);
    return String.format(format, os);
  }

  /**
   * A method for retrieving the object array to be used in formatting the url.
   *
   * @param series the time series index
   * @param item the time series item index
   * @return an object array where the two first items is series and item, and the latter items is
   *     calculated.
   */
  private Object[] getObjects(int series, int item) {
    String name = chart.getXYPlot().getDataset().getSeriesKey(series).toString();

    // Ensure that the currently selected aggregation is represented in the series key.
    // If so we have an aggregated diagram that needs the object array we get by splitting the
    // series key added to the returned object array.
    if (this.aggregation.size() == name.split("\\|").length) {
      List<Object> objects = new ArrayList<>();
      objects.add(series);
      objects.add(item);
      objects.addAll(Arrays.asList(name.split("\\|")));
      return objects.toArray();
    }

    // Check if this is instead a simple diagram with unittype only in the series key.
    // If so we just add the series key to the returned object array.
    if (!"Total".equals(name) && !name.contains("|")) {
      return new Object[] {series, item, name};
    }

    // Return a default object array with a dot (.) as the third value, representing null
    return new Object[] {series, item, WebConstants.ALL_ITEMS_OR_DEFAULT};
  }
}
