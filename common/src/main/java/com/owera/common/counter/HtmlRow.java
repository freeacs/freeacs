package com.owera.common.counter;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is only used in the process of making HTML out of
 * the Map of maps (=BMCCollection) containing all the measurement.
 * 
 * For those who are really interested: This class purpose is
 * to contain som HTML, but most of all to add up the number of
 * rows, so that we can specify correctly the "rowspan" attribute.
 * To set this attribute right we have to recursively walk through
 * the tree-structure, but when that walkthrough is done, it's to
 * late to write "rowspan" to the HTML. We therefore store it
 * here together with its corresponding HTML-row-content.
 * 
 * @author me3
 *
 */
public class HtmlRow {
	private int rowSpan = 1;
	private List<HtmlRow> subRows = new ArrayList<HtmlRow>();
	private String html;

	public HtmlRow(int rowSpan, String html) {
		this.rowSpan = rowSpan;
		this.html = html;
	}

	public void addHtmlRow(HtmlRow row) {
		subRows.add(row);
		this.rowSpan += row.getRowSpan();
	}

	public int getRowSpan() {
		return rowSpan;
	}

	public void setRowSpan(int rowSpan) {
		this.rowSpan = rowSpan;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public List<HtmlRow> getSubRows() {
		return subRows;
	}

	public void setSubRows(List<HtmlRow> subRows) {
		this.subRows = subRows;
	}

}
