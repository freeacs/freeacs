package com.owera.common.counter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This is the top-level class of this package. You may of course choose to used
 * each of the other classes (Measurement, BasicMeasurement and BMCollection) if
 * you want some kind of special control over your measurments, but this class
 * is made to make an effortless way to produce input to the system, as well as
 * make the output-print really simple.
 * 
 * The pattern of usage:
 * 
 * 1. Create an BMCCollection 2. Use add() to produce input 3. Use toString() if
 * you want a simple console-output // not supported right now 4. Use toHtml()
 * if you want a table-formatted html-output
 * 
 * That said, there's another key concept here: You may choose an arbitrary
 * number of "chained" keys when you add a measurement. Let's say you have a
 * measurement which depends on many keys. A table could maybe explain better:
 * 
 * Queue Environemt Service measurement (not our kind of Measurment) Q1 Prod S1
 * 2000 Q1 Test S1 1000 Q1 Prod S2 100
 * 
 * Both queue, environement and service are keys, but this class offers you a
 * possiblity to separate them (instead of concatenate them), thus making the
 * output much more readable.
 * 
 * Some notes: 1. This counter/measurement system is made to have a small memory
 * impact, there every measurement which falls out of the time periods measured,
 * is discarded. 2. The add()-method is synchronized. It has been measured:) to
 * return in 300 microseconds on my computer. 3. The maps used in this
 * implementation are TreeMap, which sorts everything.
 * 
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class BMCCollection {

	private Map collectionMap = Collections.synchronizedMap(new TreeMap());
	private MeasurementTypes types;
	private long periodeLength;
	private int numberOfPeriods;

	private static Logger logger = LoggerFactory.getLogger(BMCCollection.class);

	public BMCCollection(MeasurementTypes types, long periodeLength, int numberOfPeriods) {
		this.types = types;
		this.periodeLength = periodeLength;
		this.numberOfPeriods = numberOfPeriods;
	}

	/*
	 * Retrieve a map. The map can either contain another set of maps or a
	 * BMCollection String[] consists of the keys. You choose which level of map
	 * you will retrieve.
	 */
	public Map getMap(String[] keys) {
		Map map = collectionMap;
		try {
			if (keys != null) {
				for (int i = 0; i < keys.length; i++) {
					if (map.get(keys[i]) == null) {
						Map tmpMap = Collections.synchronizedMap(new TreeMap());
						map.put(keys[i], tmpMap);
						map = tmpMap;
					} else {
						map = (Map) map.get(keys[i]);
					}
				}
			}
		} catch (NullPointerException npe) {
			// do nothing. This catch-clause was added beacuse we got a
			// NullPointerException at one point:
			// at java.util.TreeMap.compare(Unknown Source)
			// at java.util.TreeMap.getEntry(Unknown Source)
			// at java.util.TreeMap.get(Unknown Source)
			// at java.util.Collections$SynchronizedMap.get(Unknown Source)
			// at com.owera.common.counter.BMCCollection.getMap(BMCCollection.java:61)
			// No exactly sure of why, but I have a feeling this is pretty
			// rare case. We'll print it so we know how often this
			// actually happens. 
			// 2009 - I think this bug is related to compare between null-strings..NaturalComparator fixes that
			logger.debug("Nullpointer happened when we tried to get retrieve a map in the BMCCollction.", npe);

		}
		return map;
	}

	/**
	 * Add a measurement to BMCCollection. Type refers to one type from
	 * MeasurementTypes.
	 * 
	 * @param keys
	 * @param type
	 * @param executeTime
	 */
	public synchronized void add(String[] keys, int type, long executeTime) {
		String[] additionalKeys = new String[keys.length - 1];
		for (int i = 0; i < keys.length - 1; i++) {
			if (keys[i] == null)
				throw new IllegalArgumentException("BMCCollection.add(): You cannot use a null-value in the String[] (keys).");
			additionalKeys[i] = keys[i];
		}
		String bmcId = keys[keys.length - 1];
		Map bmcMap = getMap(additionalKeys);
		BMCollection bmc = null;
		if (bmcMap.get(bmcId) == null) {
			bmc = new BMCollection(types, bmcId, numberOfPeriods, periodeLength);
			bmcMap.put(bmcId, bmc);
		} else {
			bmc = (BMCollection) bmcMap.get(bmcId);
		}
		BasicMeasurement bm = new BasicMeasurement(types, bmcId); // uses same
		// id as for
		// bmc
		bm.add(type, executeTime);
		bmc.insert(bm);
	}

	private String toString(Map map, String tab) {

		StringBuffer sb = new StringBuffer();
		Object[] keys = map.keySet().toArray();
		for (int i = 0; i < keys.length; i++) {
			if (map.get(keys[i]) instanceof BMCollection) {
				BMCollection bmc = (BMCollection) map.get(keys[i]);
				BasicMeasurement bm = bmc.getAggregert(types, 1);
				sb.append("1 periode  : " + bm + "\n");
				bm = bmc.getAggregert(types, 5);
				sb.append("5 perioder : " + bm + "\n");
				bm = bmc.getAggregert(types, 10);
				sb.append("10 perioder: " + bm + "\n");
			} else {
				if (tab.equals(""))
					sb.append("\n");
				sb.append(tab + keys[i] + "\n");
				sb.append(toString((Map) map.get(keys[i]), tab + "\t"));
			}
		}
		return sb.toString();
	}

	/**
	 * A really simple print. Doesn't print avg. Should be refactored to do so.
	 * Should also be refactored to print a heading.
	 * 
	 * @return
	 */
	public String toString() {
		return toString(collectionMap, "");
	}

	private String makeRows(HtmlRow row) {
		String s = "";
		s += "\t<tr>\n";
		if (row.getHtml() != null)
			s += row.getHtml();
		s += "\t</tr>\n";
		List<HtmlRow> subrows = row.getSubRows();
		for (int i = 0; i < subrows.size(); i++) {
			s += makeRows((HtmlRow) subrows.get(i));
		}
		return s;
	}

	/**
	 * Make a HTML-table containg the periods you want. Only supply the
	 * keyHeadings (example: new String[] {"Qeue","Environment","Service"}), and
	 * the periods you want to aggregate. If you have decided to measure 60
	 * periods, you may choose periods to contain this {1,10,60}, but not {61}.
	 * 
	 * @param keyHeadings
	 * @param periods
	 * @return
	 */
	public String toHtml(String[] keyHeadings, int[] periods) {
		StringBuffer sb = new StringBuffer();
		sb.append("<table border=1>\n");
		sb.append("\t<tr>\n");
		for (int i = 0; i < keyHeadings.length; i++)
			sb.append("\t\t<th rowspan=3>" + keyHeadings[i] + "</th>\n");
		sb.append("\t\t<th colspan=2>Total</th>\n");
		for (int i = 0; i < periods.length; i++) {
			int antall = (int) (periodeLength / getPeriodeDivdend()) * periods[i];
			sb.append("\t\t<th colspan=" + types.getTypes().length * 3 + ">Last " + antall + " " + getPeriodeText() + "</th>\n");
		}
		sb.append("\t</tr>\n");
		sb.append("\t<tr>\n");
		sb.append("\t\t<th rowspan=2>Avg</th>\n\t\t<th rowspan=2>Hits</th>\n");
		for (int i = 0; i < periods.length; i++) {
			for (int j = 0; j < types.getTypes().length; j++) {
				sb.append("\t\t<th colspan=3>" + types.getTypesText()[j] + "</th>\n");
			}
		}
		sb.append("\t</tr>\n");
		sb.append("\t<tr>\n");
		for (int i = 0; i < periods.length; i++) {
			for (int j = 0; j < types.getTypes().length; j++) {
				sb.append("\t\t<th>Index</th>\n\t\t<th>Avg</th>\n\t\t<th>Hits</th>\n");
			}
		}
		sb.append("\t</tr>\n");
		if (collectionMap.size() == 0) {
			int totalCol = keyHeadings.length + 2 + 3 * periods.length * types.getTypes().length;
			sb.append("\t<tr>\n\t\t<th colspan=" + totalCol + " align=center>No data collected yet</th>\n\t</tr>\n");
		} else {
			HtmlRow masterRow = toHtml(collectionMap, periods, null);
			String content = makeRows(masterRow);
			sb.append(content);
		}
		sb.append("</table>\n");
		return sb.toString();
	}

	private HtmlRow toHtml(Map map, int[] perioder, HtmlRow parentHtmlRow) {
		int[] typerArr = types.getTypes();
		Object[] keys = map.keySet().toArray();
		for (int i = 0; i < keys.length; i++) {
			HtmlRow newHtmlRow = null;
			if (map.get(keys[i]) instanceof BMCollection) {
				BMCollection bmc = (BMCollection) map.get(keys[i]);
				StringBuffer sb = new StringBuffer();
				sb.append("\t\t<td>" + keys[i] + "</td>\n");
				BasicMeasurement bm = bmc.getAggregert(types, perioder[perioder.length - 1]);
				sb.append("\t\t<td>" + bm.getAvgHTMLFormatted() + "</td>\n");
				sb.append("\t\t<td>" + bm.getHitsHTMLFormatted() + "</td>\n");
				for (int j = 0; j < perioder.length; j++) {
					bm = bmc.getAggregert(types, perioder[j]);
					for (int k = 0; k < typerArr.length; k++) {
						Measurement maaling = bm.getMeasurements()[k];
						float indeks = bm.getIndex(typerArr[k]);
						sb.append("\t\t<td>" + bm.getIndexFormatted(indeks) + "</td>\n");
						sb.append("\t\t<td>" + maaling.getAvgHTMLFormatted() + "</td>\n");
						sb.append("\t\t<td>" + maaling.getHitsHTMLFormatted() + "</td>\n");
					}
				}
				newHtmlRow = new HtmlRow(1, sb.toString());
			} else {
				Map tmpMap = (Map) map.get(keys[i]);
				newHtmlRow = toHtml(tmpMap, perioder, parentHtmlRow);
				String td = "\t\t<td rowspan=" + newHtmlRow.getRowSpan() + ">" + keys[i] + "</td>\n";
				newHtmlRow.setHtml(td + newHtmlRow.getHtml());
			}
			if (i == 0)
				parentHtmlRow = newHtmlRow;

			else
				parentHtmlRow.addHtmlRow(newHtmlRow);
		}
		return parentHtmlRow;
	}

	private String getPeriodeText() {
		if (periodeLength >= 60000 * 60 * 24)
			return "day(s)";
		if (periodeLength >= 60000 * 60)
			return "hour(s)";
		if (periodeLength >= 60000)
			return "minute(s)";
		if (periodeLength >= 1000)
			return "second(s)";
		else
			return "ms";
	}

	private long getPeriodeDivdend() {
		if (periodeLength >= 24 * 60 * 60000)
			return 24 * 60 * 60000;
		if (periodeLength >= 60000 * 60)
			return 60000 * 60;
		if (periodeLength >= 60000)
			return 60000;
		if (periodeLength >= 1000)
			return 1000;
		else
			return 1;
	}

}
