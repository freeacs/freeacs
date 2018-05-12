package com.github.freeacs.common.counter;

/**
 * @author ME3
 * 
 * This class contains an Id and an array of X BasicMeasurement objects. These
 * objects represents the last X periods of measurements. The class offers
 * methods to calculate an aggregated BasicMeasurement object for a certain
 * number of periods from now on and back in time.
 * 
 * A typical use case would be to have 60 BasicMeasurements in this object and
 * then ask to aggregate 10 and 60 periods. The BasicMeasurement object you get
 * out of that method can then be used to print the avg. response-time, total
 * hits and the number of hits for each of the MeasurementTypes.
 */
public class BMCollection {
	private String id;
	private MeasurementTypes types;
	private int numberOfPeriods;
	private long periodeLength;

	private BasicMeasurement[] basicMeasurements = null;

	public BMCollection(MeasurementTypes types, String id, int numberOfPeriods, long periodeLength) {
		this.types = types;
		this.id = id;
		this.numberOfPeriods = numberOfPeriods;
		this.periodeLength = periodeLength;
		this.basicMeasurements = new BasicMeasurement[numberOfPeriods];
	}

	/**
	 * Denne metoden er laget for at hver request skal kunne legge inn en
	 * oppdatert versjon av bm. Her legger man ogs� til i hovedBM for denne
	 * TjenesteMaalingCollection den endringen man tilf�rer.
	 */
	public void insert(BasicMeasurement bm) {
		int index = CalculationUtil.getTimeperiodsHashcode(bm, periodeLength, numberOfPeriods);
		BasicMeasurement gjeldendeBm = get(index);
		if (gjeldendeBm != null)
			gjeldendeBm.add(bm);
		else
			basicMeasurements[index] = bm;
	}

	/**
	 * Denne metoden gir bm for den angitte index. Dersom bm-objektet som ligger
	 * her er for gammelt, s� fjernes det. Dermed trekkes ogs� dette gamle
	 * bm-objektet fra hovedBM for denne TjenesteMaalingCollection.
	 */
	public BasicMeasurement get(int index) {
		BasicMeasurement bm = basicMeasurements[index];
		if (bm != null) {
			if (CalculationUtil.getAgeInTimeperiods(bm, periodeLength, numberOfPeriods) >= numberOfPeriods) {
				basicMeasurements[index] = null;
				return null; // vi kan ikke bruke denne maalingen lenger, den
								// gjelder for forrige time
			} else {
				return bm;
			}
		} else {
			return null;
		}
	}

	/**
	 * Denne metoden skal aggregerer X antallTidsperioder av BasisMaalinger som
	 * vi har liggende. Typisk tilfelle vil v�re � sette sisteXTidsperioder til
	 * 1, 10 og 60 (ref. eksempelet i begynnelsen av denne klassen). Sette
	 */
	public BasicMeasurement getAggregert(MeasurementTypes typer, int sisteXTidsperioder) {
		if (sisteXTidsperioder > numberOfPeriods)
			throw new IllegalArgumentException("Du kan ikke beregne for mer enn " + numberOfPeriods
					+ " tidsperioder. Du satte sisteXTidsperioder til " + sisteXTidsperioder);
		int antallTidsperioderBeregnet = 0;
		BasicMeasurement nyBm = new BasicMeasurement(typer, id);
		int index = CalculationUtil.getTimeperiodsHashcode(nyBm, periodeLength, numberOfPeriods);
		while (true) {
			BasicMeasurement bm = get(index);
			if (bm != null)
				nyBm.add(bm);
			antallTidsperioderBeregnet++;
			index--;
			if (index < 0)
				index = numberOfPeriods - 1;
			if (antallTidsperioderBeregnet == sisteXTidsperioder)
				break;
		}
		return nyBm;
	}

	public int getAntallPerioder() {
		return numberOfPeriods;
	}

	public void setAntallPerioder(int antallPerioder) {
		this.numberOfPeriods = antallPerioder;
	}

	public long getTidsperiode() {
		return periodeLength;
	}

	public void setTidsperiode(long tidsperiode) {
		this.periodeLength = tidsperiode;
	}

	public MeasurementTypes getTyper() {
		return types;
	}

	public void setTyper(MeasurementTypes typer) {
		this.types = typer;
	}

	public String getId() {
		return id;
	}

	public String toString() {
		String ret = "";
		ret += "BasisMaalingCollection: " + id + "    AntallPerioder: " + numberOfPeriods + "    PeriodeLengde:" + periodeLength + "\n";
		for (int i = 0; i < basicMeasurements.length; i++) {
			BasicMeasurement bm = get(i);
			if (bm != null) {
				int hash = CalculationUtil.getTimeperiodsHashcode(bm, periodeLength, numberOfPeriods);
				int alder = CalculationUtil.getAgeInTimeperiods(bm, periodeLength, numberOfPeriods);
				ret += "\tAlder:" + alder + " Index:" + hash + "  " + bm + "\n";
			}
		}
		return ret;
	}

}
