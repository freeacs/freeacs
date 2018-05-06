package com.owera.common.counter;

/**
 * BasicMeasurment contains 1 or more Measurements. Each Measurement
 * represents one type in MeasurementTypes.getTypes()
 * 
 * @author ME3
 */
public class BasicMeasurement {

	private String id; // en vilk�rlig id
	private Measurement[] measurements; // kan inneholde en eller flere maaleparametre
	private long tms; // ms for oppstart av denne maaleperioden
	private MeasurementTypes types; // typer av maalinger (f.eks. OK, FEIL, RESEND)

	public BasicMeasurement(MeasurementTypes types, String id) {
		this.id = id;
		this.tms = System.currentTimeMillis();
		this.types = types;
		int antallTyper = types.getTypes().length;
		measurements = new Measurement[antallTyper];
		for (int i = 0; i < antallTyper; i++) {
			measurements[i] = new Measurement(0, 0);
		}
	}

	public void add(BasicMeasurement bm) {
		if (bm.getTyper().getClass().equals(this.getTyper().getClass())) {
			for (int i = 0; i < measurements.length; i++) {
				this.measurements[i].add(bm.getMeasurements()[i]);
			}
		} else
			throw new IllegalArgumentException("Tries to add two BasicMeasurements with different MeasurementTypes");
	}

	public void subtract(BasicMeasurement bm) {
		if (bm.getTyper().getClass().equals(this.getTyper().getClass())) {
			for (int i = 0; i < measurements.length; i++) {
				this.measurements[i].subtract(bm.getMeasurements()[i]);
			}
		} else
			throw new IllegalArgumentException("Tries to subtract two BasicMeasurements with different MeasurementTypes");
	}

	/**
	 * Add a measurment for one specific MeasurementType. Hit-count is increased by 1.
	 * @param type
	 * @param utfortms
	 */
	public void add(int type, long utfortms) {
		measurements[type].add(1, utfortms);
	}

	/**
	 * Calculate the avg for all measurementTypes
	 */
	public long getAvg() {
		Measurement m = new Measurement(0, 0);
		for (int i = 0; i < measurements.length; i++) {
			m.add(measurements[i]);
		}
		return m.getAvg();
	}

	public String getAvgHTMLFormatted() {
		Measurement m = new Measurement(0, 0);
		for (int i = 0; i < measurements.length; i++) {
			m.add(measurements[i]);
		}
		long snitt = m.getAvg();
		if (snitt == 0 && m.getHits() == 0)
			return "&nbsp;";
		else
			return "" + snitt;
	}

	/**
	 * Returns a float number between 0 and 1, represents the relation between a
	 * single Measurement and all the Measurement in this object.
	 * 
	 * @param measurementType
	 * @return
	 */
	public float getIndex(int maalingType) {

		int totalHits = getHits();
		if (totalHits > 0 && measurements[maalingType].getHits() > 0)
			return (((float) measurements[maalingType].getHits()) / (float) totalHits) * 100;
		else {
			return -1f;
		}
	}

	public String getIndexFormatted(float f) {
		if (f == -1)
			return "&nbsp;";
		String str = String.format("%.2f", f);
		int dotpos = str.indexOf(".");
		if (dotpos + 1 == str.length()) // Hvis vi f�r et slikt tall : 85.1
			str += "0";
		str += "%";
		return str;
	}

	public int getHits() {
		Measurement m = new Measurement(0, 0);
		for (int i = 0; i < measurements.length; i++) {
			m.add(measurements[i]);
		}
		return m.getHits();
	}

	public String getHitsHTMLFormatted() {
		int i = getHits();
		if (i == 0)
			return "&nbsp;";
		else
			return "" + i;
	}

	public String getId() {
		return id;
	}

	public long getTimestamp() {
		return tms;
	}

	public Measurement[] getMeasurements() {
		return measurements;
	}

	public void setMeasurement(Measurement[] maalinger) {
		this.measurements = maalinger;
	}

	public MeasurementTypes getTyper() {
		return types;
	}

	public void setTyper(MeasurementTypes typer) {
		this.types = typer;
	}

	/**
	 * Simple print-out
	 * @see java.lang.Object#toString()
	 */
	/*
	 public String toString() {
	 String ret = "";
	 ret += String.format("%-20s",id);	
	 //ret += StringUtility.format(id, 20, ' ', false, true, false) + " ";
	 int[] typerArr = types.getTypes();
	 String[] typerTekst = types.getTypesText();

	 //ret += "Total: " + StringUtility.format("" + getHits(), 6, ' ', true, false, false) + " ";
	 ret += "Total: " + String.format("%6d", getHits());
	 //ret += StringUtility.format("" + getAvgHTMLFormatted(), 6, ' ', true, false, false) + " ";
	 ret += String.format("%6s", getAvgHTMLFormatted());
	 for (int i = 0; i < typerArr.length; i++) {
	 ret += String.format(typerTekst[i]) 
	 //ret += StringUtility.format(typerTekst[i], 6, ' ', true, true, false) + ": ";
	 ret += measurements[i] + "   ";
	 }
	 return ret;

	 }
	 */

}
