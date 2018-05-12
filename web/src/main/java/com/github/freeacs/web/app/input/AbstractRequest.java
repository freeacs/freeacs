package com.github.freeacs.web.app.input;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.ArrayList;
import java.util.List;


/**
 * This class enables ignoring specific request parameters.
 * 
 * Since this class extends an HttpServletRequestWrapper it has all the same methods.
 * 
 * The only overridden method is getParameter().
 * 
 * @author Jarl Andre Hubenthal
 *
 */
public class AbstractRequest extends HttpServletRequestWrapper {

	/**
	 * Instantiates a new abstract request.
	 *
	 * @param request the request
	 */
	public AbstractRequest(HttpServletRequest request) {
		super(request);
	}
	
	/** The ignored parameters. */
	private final List<String> ignoredParameters = new ArrayList<String>();
	
	/**
	 * Ignore parameter.
	 *
	 * @param param the param
	 */
	public void ignoreParameter(String param){
		ignoredParameters.add(param);
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
	 */
	@Override
	public String getParameter(String key){
		if(key!=null && !ignoredParameters.contains(key))
			return super.getParameter(key);
		return null;
	}
}