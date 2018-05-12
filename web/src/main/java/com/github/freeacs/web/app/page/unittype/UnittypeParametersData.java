package com.github.freeacs.web.app.page.unittype;

import com.github.freeacs.web.app.input.Input;
import com.github.freeacs.web.app.input.InputData;

import java.util.Map;


/**
 * The Class UnittypeParametersData.
 */
public class UnittypeParametersData extends InputData {
	
	/** The form submit. */
	private Input formSubmit = Input.getStringInput("addparameters");

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.input.InputData#getFormSubmit()
	 */
	public Input getFormSubmit() {
		return formSubmit;
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.input.InputData#setFormSubmit(com.owera.xaps.web.app.input.Input)
	 */
	public void setFormSubmit(Input formSubmit) {
		this.formSubmit=formSubmit;
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.input.InputData#bindForm(java.util.Map)
	 */
	@Override
	public void bindForm(Map<String, Object> root) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see com.owera.xaps.web.app.input.InputData#validateForm()
	 */
	@Override
	public boolean validateForm() {
		// TODO Auto-generated method stub
		return false;
	}

}
