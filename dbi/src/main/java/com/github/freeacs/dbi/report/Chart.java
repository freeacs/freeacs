package com.github.freeacs.dbi.report;

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
import org.jfree.data.time.*;
import org.jfree.ui.RectangleEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


@SuppressWarnings({ "rawtypes" })
public class Chart<R extends Record> {

	public static final String SET_TYPE_ALL = "ALL";
	public static final String SET_TYPE_SHOW = "SHOW";
	public static final String SET_TYPE_HIDE = "HIDE";

	/* Add to set to show in strategy mode STRATEGY_HIDE_SELECTED */
	public static final String STRATEGY_HIDE_SELECTED = "HIDE";
	/* Add to set to show in strategy mode STRATEGY_SHOW_SELECTED */
	public static final String STRATEGY_SHOW_SELECTED = "SHOW";

	private static Logger logger = LoggerFactory.getLogger(Chart.class);

	private long NINTY_DAYS = 90l * 24l * 3600l * 1000l;
	private long TWO_DAYS = 2l * 24l * 3600l * 1000l;
	private long TWO_MINUTES = 2l * 60l * 1000l;

	/* Set to true to use showMap-logic, set to false to use hideMap-logic */
	private String strategy = STRATEGY_HIDE_SELECTED;
	/* The map which contains all the sets */
	private Map<String, Set<String>> setMap = new HashMap<String, Set<String>>();

	private Report<R> report;
	private PeriodType periodType;
	private String method;
	private String[] keyNames;
	private JFreeChart chart;
	private Map<Key, R> recordMap;
	private String title;
	private boolean displayFrame = true;
	private long startTms;
	private long endTms;

	public Chart(Report<R> report, String method, boolean displayFrame, String title, String... keyNames) throws Exception {
		this.report = report;
		this.periodType = report.getPeriodType();
		this.method = method;
		this.keyNames = keyNames;
		this.title = title;
		this.recordMap = report.getMapAggregatedOn(keyNames);
		this.displayFrame = displayFrame;
	}

	/**
	 * Use the SET_TYPE-constants
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
			set = new HashSet<String>();
			setMap.put(mapKey, set);
		}
		return set;
	}

	@SuppressWarnings("unchecked")
	private boolean show(String keyStr) {
		boolean show = true;
		if (keyStr.startsWith("Total"))
			show = true;
		else {
			Set allSet = getSet(SET_TYPE_ALL);
			allSet.add(keyStr);
			if (strategy == null || strategy.equals(STRATEGY_HIDE_SELECTED)) {
				show = true;
				Set hideSet = getSet(SET_TYPE_HIDE);
				if (hideSet.contains(keyStr)) {
					show = false;
					//					System.out.println(keyStr + " was not shown since it's in the hide-set");
				}

			} else if (strategy.equals(STRATEGY_SHOW_SELECTED)) {
				show = false;
				Set showSet = getSet(SET_TYPE_SHOW);
				if (showSet.contains(keyStr))
					show = true;
				//				else
//					System.out.println(keyStr + " was not shown since it's not in the show-set");
			}
		}
		return show;
	}

	private Map<String, TimeSeries> makeTimeSeriesMap(String method, Map<Key, R> recordMap, String... keyNames) throws Exception {
		
		logger.debug("Will create a time series map using a record map with " + recordMap.size() + " entries");
		Map<String, TimeSeries> timeSeriesMap = new HashMap<String, TimeSeries>();
		for (Entry<Key, R> entry : recordMap.entrySet()) {
			Key key = entry.getKey();
			if (key.getTms().getTime() < startTms)
				startTms = key.getTms().getTime();
			if (key.getTms().getTime() > endTms)
				endTms = key.getTms().getTime();
			Record record = entry.getValue();
			String keyStr = key.getKeyStringFallbackOnMethodName(false,method, keyNames);
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
						if (dividend != 1)
							numberD = numberD / dividend.doubleValue();
						//						System.out.println(numberD + "\t" + keyStr + "\t" + key.getTms());
						if (periodType == PeriodType.HOUR)
							timeseries.add(new Hour(key.getTms()), numberD);
						else if (periodType == PeriodType.DAY)
							timeseries.add(new Day(key.getTms()), numberD);
						else if (periodType == PeriodType.MONTH)
							timeseries.add(new Month(key.getTms()), numberD);
						else if (periodType == PeriodType.MINUTE)
							timeseries.add(new Minute(key.getTms()), numberD);
						else if (periodType == PeriodType.SECOND)
							timeseries.add(new Second(key.getTms()), numberD);
					}
				}
			}
		}
		logger.debug("Have created a time series map with " + timeSeriesMap.size() + " entries");
		return timeSeriesMap;
	}

	public JFreeChart makeTimeChart() throws Exception {
		return makeTimeChart(null, null, null, null);
	}

	/**
	 * All params can be null.
	 * @param min - the minimum number on the left range axis (must be set if max is set)
	 * @param max - the maximum number on the left range axis (must be set if min is set)
	 * @param method2 - the method to run out of Record, and populates a bar series (right range axis)
	 * @param highLightIndex - the index to highlight
	 * @return 
	 * @throws Exception
	 */
	public JFreeChart makeTimeChart(Double min, Double max, String method2, Integer highLightIndex) throws Exception {
		startTms = System.currentTimeMillis();
		endTms = 0;
		XYBarRenderer.setDefaultShadowsVisible(false);
		TimeSeriesCollection data = new TimeSeriesCollection();
		Map<String, TimeSeries> timeSeriesMap = makeTimeSeriesMap(method, recordMap, keyNames);
		for (TimeSeries timeSeries : timeSeriesMap.values())
			data.addSeries(timeSeries);
		String yAxisLabel = method;
		String denominator = Record.getDenominator(report.getRecordClass(), method.toLowerCase());
		if (denominator != null)
			yAxisLabel += " (" + denominator + ")";
		chart = ChartFactory.createTimeSeriesChart(title, "Time", yAxisLabel, data, true, true, true);
		XYPlot plot = (XYPlot) chart.getPlot();
		if (method2 != null) {
			Map<Key, R> recordMap2 = recordMap;
			TimeSeriesCollection data2 = new TimeSeriesCollection();
			if (keyNames.length > 0)
				recordMap2 = report.getMapAggregatedOn();
			Map<String, TimeSeries> timeSeriesMap2 = makeTimeSeriesMap(method2, recordMap2);
			if (timeSeriesMap2.get("Total ("+method2+")") != null)
				data2.addSeries(timeSeriesMap2.get("Total ("+method2+")"));
			String y2AxisLabel = method2;
			String demoninator2 = Record.getDenominator(report.getRecordClass(), method2.toLowerCase());
			if(demoninator2!=null)
				y2AxisLabel += " ("+ demoninator2 + ")";
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
		if (min != null && max != null)
			plot.getRangeAxis(0).setRange(min, max);

		if (highLightIndex != null)
			chart.getXYPlot().getRenderer().setSeriesStroke(highLightIndex, new BasicStroke(5f));

		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
		renderer.setBaseShapesVisible(true);
		renderer.setUseFillPaint(true);
		renderer.setBaseFillPaint(Color.white);

		long diff = endTms - startTms;
		String format = "HH:mm";
		if (diff > NINTY_DAYS)
			format = "MMM-yyyy";
		else if (diff > TWO_DAYS)
			format = "dd-MMM";
		else if (diff > TWO_MINUTES)
			format = "HH:mm";
		else
			format = "HH:mm:ss";
		DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setDateFormatOverride(new SimpleDateFormat(format));

		//		final DateAxis axis = new DateAxis("Date");
		//		axis.setVerticalTickLabels(true);
		//		axis.setTickUnit(new DateTickUnit(DateTickUnitType.HOUR, 1));
		//		axis.setDateFormatOverride(new SimpleDateFormat("hh:mm"));
		//		axis.setLowerMargin(0.01);
		//		axis.setUpperMargin(0.01);
		//		plot.setDomainAxis(axis);

		LegendTitle lt = chart.getLegend(0);
		lt.setPosition(RectangleEdge.RIGHT);

		if (displayFrame) {
			ChartFrame frame = new ChartFrame(title, chart);
			frame.pack();
			frame.setVisible(true);
		}

		return chart;
	}

	public String getStrategy() {
		return strategy;
	}

	public void setStrategy(String strategy) {
		this.strategy = strategy;
	}

	public JFreeChart getChart() {
		return chart;
	}
}
