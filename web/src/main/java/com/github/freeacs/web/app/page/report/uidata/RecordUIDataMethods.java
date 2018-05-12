package com.github.freeacs.web.app.page.report.uidata;


/**
 * Contains static utility methods for use by the package.
 * 
 * Shared variables, like formatters, is placed in RecordUIDataConstants.
 * 
 * @author Jarl Andre Hubenthal
 *
 */
final class RecordUIDataMethods {
	
	/** The Constant BYTES_TO_KILOBYTES. */
	private static final int BYTES_TO_KILOBYTES = 1024;
	
	/** The Constant KILOBYTES_DIVIDEND. */
	private static final int KILOBYTES_DIVIDEND = 1024;
	
	/** The Constant NON_BREAKING. */
	private static final String NON_BREAKING = "&nbsp;";
	
	/**
	 * Gets the kilo byte presentation.
	 *
	 * @param bytes the bytes
	 * @return the kilo byte presentation
	 */
	protected static String getKiloBytePresentation(Long bytes){
		if(bytes==null)
			return "&nbsp;";
		if(bytes>BYTES_TO_KILOBYTES)
			return RecordUIDataConstants.TWO_DECIMALS_FORMAT.format(((double)bytes/(double)KILOBYTES_DIVIDEND))+"&nbsp;KB";
		return bytes.toString()+"&nbsp;B";
	}
	
	/**
	 * Gets the mega byte presentation.
	 *
	 * @param bytes the bytes
	 * @return the mega byte presentation
	 */
	protected static String getMegaBytePresentation(Long bytes){
		if(bytes==null)
			return "&nbsp;";
		if(bytes>BYTES_TO_KILOBYTES)
			return RecordUIDataConstants.TWO_DECIMALS_FORMAT.format(((double)bytes/(double)KILOBYTES_DIVIDEND))+"&nbsp;MB";
		return bytes.toString()+"&nbsp;KB";
	}
	
	/**
	 * Append string if not non breaking.
	 *
	 * @param value the value
	 * @param toAppend the to append
	 * @return the string
	 */
	protected static String appendStringIfNotNonBreaking(String value, String toAppend){
		if(value!=null && !value.equals(NON_BREAKING) && toAppend!=null)
			return value += NON_BREAKING+toAppend;
		return value;
	}
	
	/**
	 * Gets the to string or non breaking space.
	 *
	 * @param bytes the bytes
	 * @return the to string or non breaking space
	 */
	protected static String getToStringOrNonBreakingSpace(Long bytes){
		if(bytes==null)
			return NON_BREAKING;
		return bytes.toString();
	}
	
	/**
	 * Gets the percent.
	 *
	 * @param current the current
	 * @param pool the pool
	 * @return the percent
	 */
	protected static double getPercent(Long current,Long pool){
		if(current==null || pool==null)
			return 0;
		return (double)current/(double)pool*100d;
	}
}