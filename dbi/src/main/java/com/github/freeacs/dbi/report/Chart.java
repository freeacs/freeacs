package com.github.freeacs.dbi.report;

import java.awt.*;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.time.Day;
import org.jfree.data.time.Hour;
import org.jfree.data.time.Minute;
import org.jfree.data.time.Month;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.chart.ui.RectangleEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"rawtypes"})
public class Chart<R extends Record> {

  public static final String SET_TYPE_ALL = "ALL";
  public static final String SET_TYPE_SHOW = "SHOW";
  public static final String SET_TYPE_HIDE = "HIDE";

  /** Add to set to show in strategy mode STRATEGY_HIDE_SELECTED. */
  public static final String STRATEGY_HIDE_SELECTED = "HIDE";
  /** Add to set to show in strategy mode STRATEGY_SHOW_SELECTED. */
  public static final String STRATEGY_SHOW_SELECTED = "SHOW";

  private static Logger logger = LoggerFactory.getLogger(Chart.class);

  private long NINTY_DAYS = 90L * 24L * 3600L * 1000L;
  private long TWO_DAYS = 2L * 24L * 3600L * 1000L;
  private long TWO_MINUTES = 2L * 60L * 1000L;

  /** Set to true to use showMap-logic, set to false to use hideMap-logic. */
  private String strategy = STRATEGY_HIDE_SELECTED;
  /** The map which contains all the sets. */
  private Map<String, Set<String>> setMap = new HashMap<>();

  private Report<R> report;
  private PeriodType periodType;
  private String method;
  private String[] keyNames;
  private JFreeChart chart;
  private Map<Key, R> recordMap;
  private String title;
  private boolean displayFrame;
  private long startTms;
  private long endTms;

  public Chart(
      Report<R> report, String method, boolean displayFrame, String title, String... keyNames) {
    this.report = report;
    this.periodType = report.getPeriodType();
    this.method = method;
    this.keyNames = keyNames;
    this.title = title;
    this.recordMap = report.getMapAggregatedOn(keyNames);
    this.displayFrame = displayFrame;
  }

  /**
   * Use the SET_TYPE-constants.
   *
   * @param setType
   * @return
   */
  @SuppressWarnings("unchecked")
  public Set<String> getSet(String setType) {
    String mapKey = setType + method;
    for (String keyName : keyNames) {
      mapKey += keyName;
    }
    Set set = setMap.get(mapKey);
    if (set == null) {
      set = new HashSet<>();
      setMap.put(mapKey, set);
    }
    return set;
  }

  @SuppressWarnings("unchecked")
  private boolean show(String keyStr) {
    boolean show = true;
    if (keyStr.startsWith("Total")) {
      show = true;
    } else {
      Set allSet = getSet(SET_TYPE_ALL);
      allSet.add(keyStr);
      if (strategy == null || STRATEGY_HIDE_SELECTED.equals(strategy)) {
        show = true;
        Set hideSet = getSet(SET_TYPE_HIDE);
        if (hideSet.contains(keyStr)) {
          show = false;
        }
      } else if (STRATEGY_SHOW_SELECTED.equals(strategy)) {
        show = false;
        Set showSet = getSet(SET_TYPE_SHOW);
        if (showSet.contains(keyStr)) {
          show = true;
        }
      }
    }
    return show;
  }

  private Map<String, TimeSeries> makeTimeSeriesMap(
      String method, Map<Key, R> recordMap, String... keyNames) throws Exception {
    logger.debug(
        "Will create a time series map using a record map with " + recordMap.size() + " entries");
    Map<String, TimeSeries> timeSeriesMap = new HashMap<>();
    for (Entry<Key, R> entry : recordMap.entrySet()) {
      Key key = entry.getKey();
      if (key.getTms().getTime() < startTms) {
        startTms = key.getTms().getTime();
      }
      if (key.getTms().getTime() > endTms) {
        endTms = key.getTms().getTime();
      }
      Record record = entry.getValue();
      String keyStr = key.getKeyStringFallbackOnMethodName(false, method, keyNames);
      if (show(keyStr)) {
        TimeSeries timeseries = timeSeriesMap.get(keyStr);
        if (timeseries == null) {
          timeseries = new TimeSeries(keyStr);
          timeSeriesMap.put(keyStr, timeseries);
        }
        Method m = report.getRecordClass().getMethod("get" + method, (Class[]) null); // Fix
        Object obj = m.invoke(record, (Object[]) null);
        if (obj != null) {
          m = obj.getClass().getMethod("get", (Class[]) null);
          Long number = (Long) m.invoke(obj, (Object[]) null);
          if (number != null) {
            double numberD = number.doubleValue();
            m = obj.getClass().getMethod("getDividend", (Class[]) null);
            Long dividend = (Long) m.invoke(obj, (Object[]) null);
            if (dividend != 1) {
              numberD = numberD / dividend.doubleValue();
            }
            //						System.out.println(numberD + "\t" + keyStr + "\t" + key.getTms());
            if (periodType == PeriodType.HOUR) {
              timeseries.add(new Hour(key.getTms()), numberD);
            } else if (periodType == PeriodType.DAY) {
              timeseries.add(new Day(key.getTms()), numberD);
            } else if (periodType == PeriodType.MONTH) {
              timeseries.add(new Month(key.getTms()), numberD);
            } else if (periodType == PeriodType.MINUTE) {
              timeseries.add(new Minute(key.getTms()), numberD);
            } else if (periodType == PeriodType.SECOND) {
              timeseries.add(new Second(key.getTms()), numberD);
            }
          }
        }
      }
    }
    logger.debug("Have created a time series map with " + timeSeriesMap.size() + " entries");
    return timeSeriesMap;
  }

  /**
   * All params can be null.
   *
   * @param min - the minimum number on the left range axis (must be set if max is set)
   * @param max - the maximum number on the left range axis (must be set if min is set)
   * @param method2 - the method to run out of Record, and populates a bar series (right range axis)
   * @param highLightIndex - the index to highlight
   * @return
   * @throws Exception
   */
  public JFreeChart makeTimeChart(Double min, Double max, String method2, Integer highLightIndex)
      throws Exception {
    startTms = System.currentTimeMillis();
    endTms = 0;
    XYBarRenderer.setDefaultShadowsVisible(false);
    TimeSeriesCollection data = new TimeSeriesCollection();
    Map<String, TimeSeries> timeSeriesMap = makeTimeSeriesMap(method, recordMap, keyNames);
    for (TimeSeries timeSeries : timeSeriesMap.values()) {
      data.addSeries(timeSeries);
    }
    String yAxisLabel = method;
    String denominator = Record.getDenominator(report.getRecordClass(), method.toLowerCase());
    if (denominator != null) {
      yAxisLabel += " (" + denominator + ")";
    }
    chart = ChartFactory.createTimeSeriesChart(title, "Time", yAxisLabel, data, true, true, true);
    XYPlot plot = (XYPlot) chart.getPlot();
    if (method2 != null) {
      Map<Key, R> recordMap2 = recordMap;
      TimeSeriesCollection data2 = new TimeSeriesCollection();
      if (keyNames.length > 0) {
        recordMap2 = report.getMapAggregatedOn();
      }
      Map<String, TimeSeries> timeSeriesMap2 = makeTimeSeriesMap(method2, recordMap2);
      if (timeSeriesMap2.get("Total (" + method2 + ")") != null) {
        data2.addSeries(timeSeriesMap2.get("Total (" + method2 + ")"));
      }
      String y2AxisLabel = method2;
      String demoninator2 = Record.getDenominator(report.getRecordClass(), method2.toLowerCase());
      if (demoninator2 != null) {
        y2AxisLabel += " (" + demoninator2 + ")";
      }
      NumberAxis axis2 = new NumberAxis(y2AxisLabel);
      XYBarRenderer renderer2 = new XYBarRenderer(0.20);
      plot.setRangeAxis(1, axis2);
      plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
      plot.setDataset(1, data2);
      plot.setRenderer(1, renderer2);
      plot.mapDatasetToRangeAxis(1, 1);
      renderer2.setBarPainter(new StandardXYBarPainter());
      plot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);
    }
    if (min != null && max != null) {
      plot.getRangeAxis(0).setRange(min, max);
    }

    if (highLightIndex != null) {
      chart.getXYPlot().getRenderer().setSeriesStroke(highLightIndex, new BasicStroke(5f));
    }

    XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
    renderer.setDefaultShapesVisible(true);
    renderer.setUseFillPaint(true);
    renderer.setDefaultFillPaint(Color.white);

    long diff = endTms - startTms;
    String format;
    if (diff > NINTY_DAYS) {
      format = "MMM-yyyy";
    } else if (diff > TWO_DAYS) {
      format = "dd-MMM";
    } else if (diff > TWO_MINUTES) {
      format = "HH:mm";
    } else {
      format = "HH:mm:ss";
    }
    DateAxis axis = (DateAxis) plot.getDomainAxis();
    axis.setDateFormatOverride(new SimpleDateFormat(format));

    LegendTitle lt = chart.getLegend(0);
    lt.setPosition(RectangleEdge.RIGHT);

    if (displayFrame) {
      ChartFrame frame = new ChartFrame(title, chart);
      frame.pack();
      frame.setVisible(true);
    }

    return chart;
  }

  public JFreeChart getChart() {
    return chart;
  }
}
