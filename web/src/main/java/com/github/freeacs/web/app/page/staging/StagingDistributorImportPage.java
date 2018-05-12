package com.github.freeacs.web.app.page.staging;

import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.XAPS;
import com.github.freeacs.web.app.Output;
import com.github.freeacs.web.app.input.*;
import com.github.freeacs.web.app.util.WebConstants;
import com.github.freeacs.web.app.util.XAPSLoader;
import org.apache.commons.fileupload.FileItem;

import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;



/**
 * The Class StagingDistributorImportPage.
 */
public class StagingDistributorImportPage extends StagingActions {
	
	/** The input data. */
	private StagingDistributorData inputData;
	
	/** The session id. */
	private String sessionId;
	
	/** The xaps. */
	private XAPS xaps;
	
	/** The unittype. */
	private Unittype unittype;
        
	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.WebPage#process(com.owera.xaps.web.app.input.ParameterParser, com.owera.xaps.web.app.output.ResponseHandler)
	 */
	public void process(ParameterParser params, Output outputHandler) throws Exception {
		inputData = (StagingDistributorData) InputDataRetriever.parseInto(new StagingDistributorData(), params);

		sessionId = params.getSession().getId();
		
		xaps = XAPSLoader.getXAPS(sessionId);
		if (xaps == null) {
			outputHandler.setRedirectTarget(WebConstants.DB_LOGIN_URL);
			return;
		}
		
		InputDataIntegrity.loadAndStoreSession(params,outputHandler,inputData, inputData.getUnittype(), inputData.getProfile());
		
		DropDownSingleSelect<Unittype> distributors = InputSelectionFactory.getUnittypeSelection(inputData.getUnittype(), xaps);
		
		unittype = distributors.getSelected();
		
		Map<String, Object> root = outputHandler.getTemplateMap();
		
		String actionResponse = null;
		if (inputData.getFormSubmit().hasValue("Add new product")) {
			actionResponse = actionCreateDistributor(distributors.getSelected(), params,params.getSession());
		}
		
		root.put("unittypes",distributors);
		
		if(distributors.getSelected()!=null){
			root.put("softwares", unittype.getFiles().getFiles());
			root.put("version", params.getParameter("versionnumber"));
		}
		
		root.put("outputHandler", actionResponse);
		root.put("errors", generateErrorList());
		root.put("warnings", generateWarningList());
		
		outputHandler.setTemplatePathWithIndex("distributorsimport");
	}
	
	/**
	 * Action create distributor.
	 *
	 * @param unittype the unittype
	 * @param req the req
	 * @param session the session
	 * @return the string
	 * @throws Exception the exception
	 */
	private String actionCreateDistributor(Unittype unittype,ParameterParser req,HttpSession session) throws Exception {
		String version = inputData.getVersionNumber().getString();
		FileItem file = null;
		if (version != null) {
			file = req.getFileUpload("taiwan");
			if (file == null || file.get().length == 0) {
				return "ERROR: Warning: please upload taiwan file";
			}
		}

		try {
			if (unittype!= null) {
				if (version != null) {
					BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()));
					List<String> taiwanLines = new ArrayList<String>();
					while (br.ready()) {
						String line = br.readLine();
						if (line != null && (line = line.trim()).length() > 0)
							taiwanLines.add(line);
					}
					if (taiwanLines.size() > 0) {
						actionAddUnitsToDistributor(unittype, sessionId, taiwanLines, version);
						if (errors.size() == 0 && warnings.size() == 0)
							return "SUCCESS: Successfully updated " + unittype.getName();
						else if (errors.size() == 0)
							return "SUCCESS: Updated distributor: " + unittype.getName() + ", but some warnings occured.";
					} else
						return "ERROR: Warning: no units to add for distributor " + unittype.getName();
				}
			}
		} catch (Exception e) {
			return "ERROR: while updating distributor "+unittype.getName();
		}
		
		return null;
	}
}
