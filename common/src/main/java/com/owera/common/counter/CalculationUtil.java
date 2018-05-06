package com.owera.common.counter;

/**
 * This class helps to do some calculation stuff which doesn't readily fit in BasicMeasurement,
 * nor into the BMCollection.
 * 
 * The central idea here is to be able to make a sort of "hashcode" out of the timestamps of the
 * measurements. If the "user" (a programmer really) specify that a timeperiod is 1000 ms and
 * that the number measured timeperiods is 60, the number of hashcodes will be 60. If a timestamp
 * is 60001 ms old, the measurement will receive a hashcode = 0, but the age will be set to
 * Integer.MAX_VALUE. The point is to compare measurements a to see which timeperiod they belong
 * to, regardless of what timeperiod (or the number of them) that has been chosen.
 * 
 * @author me3
 *
 */

public class CalculationUtil {

	/**
	 * Returns a number between 0 and maxHashcodeValue, based on the timestamp of the BasicMeasurement-object.
	 * 
	 * @param bm
	 * @param periodeLength
	 * @param maxHashcodeValue
	 * @return
	 */
	public static int getTimeperiodsHashcode(BasicMeasurement bm, long periodeLength, int maxHashcodeValue) {
		long index = bm.getTimestamp() % (periodeLength * maxHashcodeValue);
		float indexF = (float) index / (float) (periodeLength * maxHashcodeValue);
		float indexN = indexF * maxHashcodeValue;
		int hashCode = (int) Math.floor(indexN);
		return hashCode;
	}

	/**
	 * Returns the age (in periods) of a BasicMeasurement-object. If the object is older than 
	 * periodeLength*maxHashcodeValues it will return Integer.MAX_VALUE.
	 * 
	 * @param bm
	 * @param periodeLength
	 * @param maxHashcodeValues
	 * @return
	 */
	public static int getAgeInTimeperiods(BasicMeasurement bm, long periodeLength, int maxHashcodeValues) {
		long naa = System.currentTimeMillis();
		int hashCodeNaa = getHashcodeNow(naa, periodeLength, maxHashcodeValues);
		int hashCodeBm = getTimeperiodsHashcode(bm, periodeLength, maxHashcodeValues);
		long diffTms = naa - bm.getTimestamp();
		if (diffTms > periodeLength * maxHashcodeValues) {
			return Integer.MAX_VALUE;
		} else {
			if (hashCodeBm > hashCodeNaa)
				return hashCodeNaa + (maxHashcodeValues - hashCodeBm);
			if (hashCodeBm == hashCodeNaa) { // kan v�re tidsperiode*antallHashcodes ms i mellom
				if (diffTms > periodeLength)
					return maxHashcodeValues;
				else
					return 0;
			}
			return hashCodeNaa - hashCodeBm;
		}
	}

	/**
	 * Returns the hashcode of "right now". Essentially the exact same method as the first
	 * one in this class, except that it receives the timestamp directly. The first method
	 * should/could be refactored out, although it might be a bit clearer as it stands now.
	 * 
	 * @param now
	 * @param periodeLength
	 * @param maxHashcodeValues
	 * @return
	 */

	public static int getHashcodeNow(long now, long periodeLength, int maxHashcodeValues) {
		long index = now % (periodeLength * maxHashcodeValues);
		float indexF = (float) index / (float) (periodeLength * maxHashcodeValues);
		float indexN = indexF * maxHashcodeValues;
		return (int) Math.floor(indexN);
	}

}
