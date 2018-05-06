package com.owera.xaps.web.app.page.certificates;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import com.owera.common.log.Logger;
import com.owera.xaps.dbi.Certificate;
import com.owera.xaps.dbi.XAPS;
import com.owera.xaps.web.Page;
import com.owera.xaps.web.app.Output;
import com.owera.xaps.web.app.input.InputDataRetriever;
import com.owera.xaps.web.app.input.ParameterParser;
import com.owera.xaps.web.app.page.AbstractWebPage;
import com.owera.xaps.web.app.util.WebConstants;
import com.owera.xaps.web.app.util.XAPSLoader;

import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModelException;


/**
 * The Certificates page class.
 *  
 * @author Jarl Andre Hubenthal
 */
public class CryptoPage extends AbstractWebPage {

	/** The input data. */
	private CryptoData inputData;
	
	/** The xaps. */
	private XAPS xaps;
	
	/** The logger. */
	private Logger logger = new Logger();

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.page.WebPage#process(com.owera.xaps.web.app.input.ParameterParser, com.owera.xaps.web.app.output.ResponseHandler)
	 */
	public void process(ParameterParser params, Output outputHandler) throws Exception {
		inputData = (CryptoData) InputDataRetriever.parseInto(new CryptoData(), params);

		HttpSession session = params.getSession();
		
		xaps = XAPSLoader.getXAPS(session.getId());
		if (xaps == null) {
			outputHandler.setRedirectTarget(WebConstants.DB_LOGIN_URL);
			return;
		}
		
		Map<String,Object> root = outputHandler.getTemplateMap();
		
		if(inputData.getAction().notNullNorValue("") && inputData.getId().getInteger()!=null){
			Certificate certToEdit = xaps.getCertificates().getById(inputData.getId().getInteger());
			if(certToEdit!=null){
				if(inputData.getAction().isValue("delete")){
					try{
						xaps.getCertificates().deleteCertificate(certToEdit, xaps);
						session.setAttribute("info","Successfully deleted Certificate ["+certToEdit.getId().toString()+"]");
						outputHandler.setDirectToPage(Page.CERTIFICATES); // Avoid postback warning
						return;
					}catch(Throwable ex){
						root.put("error", "Could not delete certificate ["+certToEdit.getName()+"]: "+ex.getLocalizedMessage());
					}
				}
			}
		}else if(inputData.getFormSubmit().notNullNorValue("")){
			if(inputData.bindAndValidate(root)){
				Certificate cert = null;
				try{
					 cert = new Certificate("Uploaded "+new Date().toString(), inputData.getCertificate().getFileAsString());
					 try{
						xaps.getCertificates().addOrChangeCertificate(cert, xaps);
						params.getSession().setAttribute("info","Successfully added Certificate ["+cert.getId()+"]");
						outputHandler.setDirectToPage(Page.CERTIFICATES); // Avoid postback warning
						return;
					 }catch(IllegalArgumentException certificateExists){ // Thrown by addOrChange if cert type exists
						boolean valid = cert.isValid(0);
						logger.debug("Certificate is "+(valid?"valid":"invalid"));
						if(valid){
							logger.debug("Deleting current "+cert.getCertType()+" certificate");
							Certificate certToDelete = xaps.getCertificates().getCertificate(cert.getCertType());
							if(certToDelete.isTrial()){
								xaps.getCertificates().deleteCertificate(certToDelete, xaps);
								logger.debug("Upgrading "+cert.getCertType()+" certificate");
								xaps.getCertificates().addOrChangeCertificate(cert, xaps);
								params.getSession().setAttribute("info","Successfully updated Certificate ["+cert.getId()+"]");
								outputHandler.setDirectToPage(Page.CERTIFICATES); // Avoid postback warning
								return;
							}
						}
					 }
				}catch(IllegalArgumentException errorInCertificate){ // Thrown by new Certificate()
					root.put("error","Invalid certificate.");
				}catch(Exception e){
					root.put("error","Could not add certificate: "+e.getLocalizedMessage());
				}

			}else{
				root.put("errors", inputData.getErrors());
			}
		}
		
		root.put("ismodulevalid",new IsModuleValidMethod());
		
		if(xaps.getCertificates()!=null)
			root.put("certificates", xaps.getCertificates().getCertificates());
		
		outputHandler.setTemplatePath("/certificates.ftl");
	}
	
	/**
	 * The Class IsModuleValidMethod.
	 */
	public class IsModuleValidMethod implements TemplateMethodModel {

		/* (non-Javadoc)
		 * @see freemarker.template.TemplateMethodModel#exec(java.util.List)
		 */
		@SuppressWarnings("rawtypes")
		public Boolean exec(List args) throws TemplateModelException {
			if (args.size() != 1) {
				throw new TemplateModelException("Wrong number of arguments");
			}

			Integer id = Integer.parseInt((String) args.get(0));

			Certificate cert = xaps.getCertificates().getById(id);
			
			if(cert==null)
				throw new IllegalArgumentException("Certificate "+id+" was not found");

			return cert.isValid(0);
		}
	}
}
