package com.github.freeacs.web.app.page;

import com.github.freeacs.common.util.NaturalComparator;
import com.github.freeacs.dbi.*;
import com.github.freeacs.web.Page;
import com.github.freeacs.web.app.menu.MenuItem;
import com.github.freeacs.web.app.security.AllowedUnittype;
import com.github.freeacs.web.app.table.TableColor;
import com.github.freeacs.web.app.util.*;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;


/**
 * The simple {@link WebPage} implementation.<br />
 * 
 * The only required method to implement when extending this class is the process method in {@link WebPage}<br />
 * 
 * Contains convenient methods that is used by (mostly) all pages.
 * 
 * @author Jarl Andre Hubenthal
 *
 */
public abstract class AbstractWebPage implements WebPage {
	
	/** The page processed. */
	private boolean pageProcessed = false;

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.WebPage#isPageProcessed()
	 */
	@Override
	public boolean isPageProcessed() {
		return pageProcessed;
	}
	
	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.WebPage#getTitle(java.lang.String)
	 */
	@Override
    public String getTitle(String page){
    	return ResourceHandler.getProperties().getString("TITLE_DESCRIPTION")+(page!=null?" | "+Page.getTitle(page):"");
    }
	
	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.WebPage#getShortcutItems(com.owera.xaps.web.app.util.SessionData)
	 */
	@Override
	public List<MenuItem> getShortcutItems(SessionData sessionData){
		List<MenuItem> list = new ArrayList<MenuItem>();
		if(StringUtils.isNotEmpty(sessionData.getUnittypeName())){
			list.add(new MenuItem("Go to Unit Type",Page.UNITTYPE)
				.addParameter("unittype", sessionData.getUnittypeName())
			);
			if(StringUtils.isNotEmpty(sessionData.getProfileName())){
				list.add(new MenuItem("Go to Profile",Page.PROFILE)
					.addParameter("unittype", sessionData.getUnittypeName())
					.addParameter("profile", sessionData.getProfileName())
				);
				if(StringUtils.isNotEmpty(sessionData.getUnitId())){
					list.add(new MenuItem("Go to Unit",Page.UNITSTATUS)
						.addParameter("unittype", sessionData.getUnittypeName())
						.addParameter("profile", sessionData.getProfileName())
						.addParameter("unit", sessionData.getUnitId())
					);
				}
			}
		}
		return list;
	}
	
	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.WebPage#useWrapping()
	 */
	@Override
	public boolean useWrapping(){
		return false;
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.WebPage#requiresNoCache()
	 */
	@Override
    public boolean requiresNoCache() {
        return false;
    }

	/**
	 * The Class NotLoggedInException.
	 */
	@SuppressWarnings("serial")
	@ResponseStatus(reason="You are not logged in",value=HttpStatus.UNAUTHORIZED)
	public static class NotLoggedInException extends IllegalAccessException{}
	
	/**
	 * The Class UnitNotFoundException.
	 */
	@SuppressWarnings("serial")
	@ResponseStatus(reason="The requested unitId was not found",value=HttpStatus.NOT_FOUND)
	public static class UnitNotFoundException extends IllegalAccessException {}
	
	/**
	 * The Class UnitTypeNotAllowedException.
	 */
	@SuppressWarnings("serial")
	@ResponseStatus(reason="The requested Unit Type is not allowed",value=HttpStatus.NOT_FOUND)
	public static class UnitTypeNotAllowedException extends IllegalAccessException {}
	
	/**
	 * Checks if is profiles limited.
	 *
	 * @param unittype the unittype
	 * @param sessionId the session id
	 * @param xapsDataSource
	 * @param syslogDataSource
	 * @return true, if is profiles limited
	 *  the no available connection exception
	 * @throws SQLException the sQL exception
	 */
	public boolean isProfilesLimited(Unittype unittype, String sessionId, DataSource xapsDataSource, DataSource syslogDataSource) throws SQLException {
		if(unittype==null)
			return false;
		List<Profile> list = getAllowedProfiles(sessionId,unittype, xapsDataSource, syslogDataSource);
		return list.size() != unittype.getProfiles().getProfiles().length;
	}
	
	/**
	 * Removes the from start.
	 *
	 * @param string the string
	 * @param match the match
	 * @return the string
	 */
	public String removeFromStart(String string,char match){
		char[] charArr = string.toCharArray();
		int matchCount = 0;
		for(char c: charArr){
			if(c==match)
				matchCount++;
			else
				break;
		}
		return string.substring(matchCount);
	}
	
	/**
	 * Gets the time elapsed.
	 *
	 * @param start the start
	 * @param msg the msg
	 * @return the time elapsed
	 */
	public static String getTimeElapsed(long start,String msg){
		if(msg==null)
			return null;
		long estimatedTime = System.nanoTime() - start;
		long ms = estimatedTime / 1000000l;
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb, Locale.US);
		return formatter.format("%s in %d ms", msg,ms).toString();
	}
	
	/**
	 * Log time elapsed.
	 *
	 * @param start the start
	 * @param message the message
	 * @param logger the logger
	 */
	public static void logTimeElapsed(long start,String message,Logger logger){
		String toLog = getTimeElapsed(start, message);
		if(toLog!=null){
			logger.debug(toLog);
		}
	}

	/**
	 * Gets the display names from map.
	 *
	 * @param map the map
	 * @return the display names from map
	 */
	public Map<String, UnittypeParameter> getDisplayNamesFromMap(Map<Integer, UnittypeParameter> map) {
		Map<String, UnittypeParameter> resultMap = new TreeMap<String, UnittypeParameter>(new NaturalComparator());
		for (Entry<Integer, UnittypeParameter> outerEntry : map.entrySet()) {
			int counter = 0;
			String[] utpNameArr = outerEntry.getValue().getName().split("\\.");
			String utpNamePart = "";
			for (int i = utpNameArr.length - 1; i >= 0; i--) {
				counter = 0;
				utpNamePart = "." + utpNameArr[i] + utpNamePart;
				for (Entry<Integer, UnittypeParameter> innerEntry : map.entrySet()) {
					String utpName = "."+innerEntry.getValue().getName();
					if (utpName.endsWith(utpNamePart))
						counter++;
				}
				if (counter == 1) {
					resultMap.put(utpNamePart.substring(1), outerEntry.getValue());
					break;
				}
			}
		}
		return resultMap;
	}

	/**
	 * Gets the display names from array.
	 *
	 * @param map the map
	 * @return the display names from array
	 */
	public Map<String, UnittypeParameter> getDisplayNamesFromArray(UnittypeParameter[] map) {
		Map<String, UnittypeParameter> resultMap = new TreeMap<String, UnittypeParameter>(new NaturalComparator());
		for (UnittypeParameter outerEntry : map) {
			int counter = 0;
			String[] utpNameArr = outerEntry.getName().split("\\.");
			String utpNamePart = "";
			for (int i = utpNameArr.length - 1; i >= 0; i--) {
				counter = 0;
				utpNamePart = "." + utpNameArr[i] + utpNamePart;
				for (UnittypeParameter innerEntry : map) {
					String utpName = "."+innerEntry.getName();
					if (utpName.endsWith(utpNamePart))
						counter++;
				}
				if (counter == 1) {
					resultMap.put(utpNamePart.substring(1), outerEntry);
					break;
				}
			}
		}
		return resultMap;
	}

	/**
	 * This class is used by some pages. It is used like this:
	 * 
	 * rootMap.put("indexof",new LastIndefOfMethod());
	 * 
	 * And is called from the template like this:
	 * 
	 * ${indexof(text,tofind)}
	 * 
	 * @author Jarl Andre Hubenthal
	 * 
	 */
	public static class LastIndexOfMethod implements TemplateMethodModel {

		/* (non-Javadoc)
		 * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
		 */
		@SuppressWarnings("rawtypes")
		public TemplateModel exec(List args) throws TemplateModelException {
			if (args.size() != 2) {
				throw new TemplateModelException("Wrong arguments");
			}

			String text = (String) args.get(0);
			String toFind = (String) args.get(1);

			if(toFind.equals("."))
				toFind="\\.";
			
			String[] arr = text.split(toFind);

			String result = (arr.length > 1 ? arr[arr.length-1] : arr[0]);

			return new SimpleScalar(result);
		}
	}


	/**
	 * The Class GetParameterValue.
	 */
	public static class GetParameterValue implements TemplateMethodModel {
		
		/** The xaps unit. */
		private ACSUnit acsUnit;

		/**
		 * Instantiates a new gets the parameter value.
		 *
		 * @param ACSUnit the xaps unit
		 */
		public GetParameterValue(ACSUnit acsUnit){
			this.acsUnit = acsUnit;
		}
		
		/** The units. */
		Map<String, Unit> units = new HashMap<String, Unit>();

		/* (non-Javadoc)
		 * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
		 */
		@SuppressWarnings("rawtypes")
		public TemplateModel exec(List args) throws TemplateModelException {
			if (args.size() < 2)
				throw new TemplateModelException("Wrong number of arguments");
			String id = (String) args.get(0);
			String name = (String) args.get(1);
			Unit unit = units.get(id);
			if (unit == null) {
				try {
					unit = acsUnit.getUnitById(id);
					units.put(id, unit);
				} catch (SQLException e) {
					throw new TemplateModelException("Error: " + e.getLocalizedMessage());
				}
			}
			String up = unit.getParameters().get(name);
			if(up!=null && up.trim().length()==0){
				if(unit.getUnitParameters().get(name)!=null)
					return new SimpleScalar("<span class=\"requiresTitlePopup\" title=\"The unit has overridden the profile value with a blank string\">[blank&nbsp;unit&nbsp;parameter]</span>");
			}
			return new SimpleScalar(up);
		}
	}

	/**
	 * The Class FirstIndexOfMethod.
	 */
	public static class FirstIndexOfMethod implements TemplateMethodModel {

		/* (non-Javadoc)
		 * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
		 */
		@SuppressWarnings("rawtypes")
		public TemplateModel exec(List args) throws TemplateModelException {
			if (args.size() != 2) {
				throw new TemplateModelException("Wrong arguments");
			}

			String text = (String) args.get(0);
			String toFind = (String) args.get(1);

			return new SimpleScalar(text.split(toFind)[0]);
		}
	}
	
	/**
	 * The Class RowBackgroundColorMethod.
	 */
	public static class RowBackgroundColorMethod implements TemplateMethodModel {
		
		/** The GOOD. */
		private String GOOD = TableColor.GREEN.toString();
		
		/** The MEDIUM. */
		private String MEDIUM = TableColor.ORANGE_LIGHT.toString();
		
		/** The CRITICAL. */
		private String CRITICAL = TableColor.RED.toString();
		
		/* (non-Javadoc)
		 * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
		 */
		@SuppressWarnings("rawtypes")
		public String exec(List arg0) throws TemplateModelException {
			return getStyle(arg0,"background-color:#%s;");
		}
		
		/**
		 * Gets the font color.
		 *
		 * @param totalScore the total score
		 * @return the font color
		 * @throws TemplateModelException the template model exception
		 */
		public String getFontColor(Float totalScore) throws TemplateModelException{
			GOOD = "green";
			MEDIUM = "orange";
			CRITICAL = "red";
			return getStyle(Arrays.asList(totalScore!=null?totalScore.toString():""),"color:%s;");
		}
		
		/**
		 * Gets the style.
		 *
		 * @param arg0 the arg0
		 * @param toFormat the to format
		 * @return the style
		 * @throws TemplateModelException the template model exception
		 */
		private String getStyle(List<?> arg0,String toFormat) throws TemplateModelException{
			if (arg0.size() < 1)
				throw new TemplateModelException("Specify total");
			String totalString = (String) arg0.get(0);
			Float total = Float.parseFloat(totalString);
			if(total>80)
				toFormat = String.format(toFormat, GOOD);
			else if(total>=70)
				toFormat = String.format(toFormat, MEDIUM);
			else if(total<70)
				toFormat = String.format(toFormat, CRITICAL);
			return toFormat;
		}
	}
	
	/**
	 * The Class DivideBy.
	 */
	public static class DivideBy implements TemplateMethodModel {
		
		/* (non-Javadoc)
		 * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
		 */
		@SuppressWarnings("rawtypes")
		public Float exec(List arg) throws TemplateModelException {
			if (arg.size() < 2)
				throw new TemplateModelException("Specify the number and the dividend");
			String number = (String) arg.get(0);
			String dividend = (String) arg.get(1);
			if(isNumber(number) && isNumber(dividend)){
				return Float.parseFloat(number) / Float.parseFloat(dividend);
			}
			return (float)-1;
		}
	}
	
	/**
	 * The Class FriendlyTimeRepresentationMethod.
	 */
	public static class FriendlyTimeRepresentationMethod implements TemplateMethodModel {
		
		/* (non-Javadoc)
		 * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
		 */
		@SuppressWarnings("rawtypes")
		public String exec(List arg) throws TemplateModelException{
			if(arg.size()<1)
				throw new TemplateModelException("Wrong number of arguments given. Seconds needed.");
			if(isNumber((String) arg.get(0))){
				int milliseconds = Integer.parseInt((String) arg.get(0)) * 1000;
				return TimeFormatter.convertMs2HourMinSecString(milliseconds);
			}else{
				throw new TemplateModelException(arg.get(0)+" is not a number");
			}
		}
	}

	/**
	 * Checks if is valid string.
	 *
	 * @param s the s
	 * @return true, if is valid string
	 */
	public boolean isValidString(String s) {
		return s != null && (s = s.trim()).length() > 0 && !s.equals(WebConstants.ALL_ITEMS_OR_DEFAULT);
	}

	/**
	 * Checks if is number.
	 *
	 * @param string the string
	 * @return true, if is number
	 */
	public static boolean isNumber(String string) {
		if(string==null)
			return false;
		return Pattern.matches("[0-9]+", string);
	}

	/**
	 * Strip spaces replace with under score.
	 *
	 * @param name the name
	 * @return the string
	 */
	public String stripSpacesReplaceWithUnderScore(String name) {
		return name.replaceAll(" ", "_");
	}

	/**
	 * Checks if is unittypes limited.
	 *
	 * @param sessionId the session id
	 * @param xapsDataSource
	 * @param syslogDataSource
	 * @return true, if is unittypes limited
	 *  the no available connection exception
	 * @throws SQLException the sQL exception
	 */
	public static boolean isUnittypesLimited(String sessionId, DataSource xapsDataSource, DataSource syslogDataSource) throws SQLException {
		List<Unittype> list = getAllowedUnittypes(sessionId, xapsDataSource, syslogDataSource);
		ACS acs = XAPSLoader.getXAPS(sessionId, xapsDataSource, syslogDataSource);
		return list.size() != acs.getUnittypes().getUnittypes().length;
	}

	/**
	 * Gets the allowed unittypes.
	 *
	 * @param sessionId the session id
	 * @param xapsDataSource
	 * @param syslogDataSource
	 * @return the allowed unittypes
	 *  the no available connection exception
	 * @throws SQLException the sQL exception
	 */
	public static List<Unittype> getAllowedUnittypes(String sessionId, DataSource xapsDataSource, DataSource syslogDataSource) throws SQLException {
		ACS acs = XAPSLoader.getXAPS(sessionId, xapsDataSource, syslogDataSource);
		SessionData sessionData = SessionCache.getSessionData(sessionId);
		List<Unittype> unittypesList = null;
		if (sessionData.getFilteredUnittypes() != null) {
			List<Unittype> unittypes = new ArrayList<Unittype>();
			Unittype[] xAPSUnittypes = acs.getUnittypes().getUnittypes();
			
			if(sessionData.getFilteredUnittypes().length==1 && sessionData.getFilteredUnittypes()[0].getName()!=null && sessionData.getFilteredUnittypes()[0].getName().equals("*"))
				return (unittypesList=Arrays.asList(xAPSUnittypes));
			
			for (int i = 0; i < xAPSUnittypes.length; i++) {
				for (AllowedUnittype ut : sessionData.getFilteredUnittypes()) {
					if (ut.getName()!=null && ut.getName().trim().equals(xAPSUnittypes[i].getName())) {
						unittypes.add(xAPSUnittypes[i]);
					}else if(ut.getId()!=null){
						Unittype unittype = acs.getUnittype(ut.getId());
						if(unittype!=null && unittype.getName().equals(xAPSUnittypes[i].getName()) && !unittypes.contains(xAPSUnittypes[i]))
							unittypes.add(xAPSUnittypes[i]);
					}
				}
			}
			unittypesList = unittypes;
		}
		return unittypesList;
	}
	
	/**
	 * Gets the allowed profiles.
	 *
	 * @param sessionId the session id
	 * @param unittype the unittype
	 * @param xapsDataSource
	 * @param syslogDataSource
	 * @return the allowed profiles
	 *  the no available connection exception
	 * @throws SQLException the sQL exception
	 */
	public static List<Profile> getAllowedProfiles(String sessionId, Unittype unittype, DataSource xapsDataSource, DataSource syslogDataSource) throws SQLException {
		if(unittype==null)
			return getAllAllowedProfiles(sessionId, xapsDataSource, syslogDataSource);
		
		SessionData sessionData = SessionCache.getSessionData(sessionId);
		
		Profile[] allProfiles = unittype.getProfiles().getProfiles();
		
		List<Profile> profilesList = null;
		if (sessionData.getFilteredUnittypes() != null) {
			List<Profile> profiles = new ArrayList<Profile>();
			
			for(AllowedUnittype ut: sessionData.getFilteredUnittypes()){
				if(ut.getId()!=null && ut.getId().intValue()!=unittype.getId().intValue())
					continue;
				else if(ut.getName()!=null && !ut.getName().trim().equals(unittype.getName()))
					continue;
				else{
					if(ut.getProfile()==null)
						continue;
					for(Profile profile: allProfiles){
						if(ut.getProfile().getId()!=null && ut.getProfile().getId().intValue()==profile.getId().intValue() && !profiles.contains(profile)){
							profiles.add(profile);
							break;
						}else if(ut.getProfile().getName()!=null && ut.getProfile().getName().trim().equals(profile.getName()) && !profiles.contains(profile)){
							profiles.add(profile);
							break;
						}
					}
				}
			}
			profilesList = profiles;
		}else{
			profilesList = Arrays.asList(unittype.getProfiles().getProfiles());
		}
		
		if(profilesList.size()==0)
			return Arrays.asList(allProfiles);
		return profilesList;
	}
	
	/**
	 * Gets the all allowed profiles.
	 *
	 * @param sessionId the session id
	 * @param xapsDataSource
	 * @param syslogDataSource
	 * @return the all allowed profiles
	 *  the no available connection exception
	 * @throws SQLException the sQL exception
	 */
	protected static List<Profile> getAllAllowedProfiles(String sessionId, DataSource xapsDataSource, DataSource syslogDataSource) throws SQLException{
		List<Unittype> unittypes = getAllowedUnittypes(sessionId, xapsDataSource, syslogDataSource);
 		List<Profile> filteredProfiles = new ArrayList<Profile>();
		for(Unittype unittype: unittypes){
			List<Profile> profiles = getAllowedProfiles(sessionId,unittype, xapsDataSource, syslogDataSource);
			if(profiles!=null)
				filteredProfiles.addAll(profiles);
		}
		return filteredProfiles;
	}
}
