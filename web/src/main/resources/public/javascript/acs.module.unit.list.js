/*global xAPS */
/**
 * A module for the Unit List page.
 * 
 * Closely related to the Report page, from where it is being redirected from in the first place, because of zooming.
 * 
 * Can also be accessed directly.
 * 
 * Does not need to be initialized.
 * 
 * TEMPLATES
 * /WebContent/templates/unit-list/index.ftl // Includes this script
 * 
 * @author Jarl Andre Hubenthal
 */
ACS.createModule("unit.list", function($this, $super, $) {
	/**
	 * The configuration.
	 */
	$this.settings = {
        submit : {
            onchange : ".submitonchange",
            button : "#updateButton"
        }
	};

	/**
	 * The constructor.
	 * 
	 * Configures calendars, and change/onclick event handlers.
	 */
	$this._initForm = function() {
		$super.setupFromCalendar();
		$super.setupToCalendar();
		$($this.settings.submit.onchange).change(function() {
			return $this.submitUnitListForm();
		});
		$($this.settings.submit.button).click(function() {
			return $this.submitUnitListForm();
		});
	};

	/**
	 * Submits the form if the end date is higher than the start date.
	 * Else it pops up an alert.
	 */
	$this.submitUnitListForm = function() {
		if($super.parseDate($("#start").val())>$super.parseDate($("#end").val())){
			alert("Start date is not before end date!");
			return false;
		}else{
			// In earlier times the following was used to display a please wait dialog
			//$super.pleaseWait();
			// Submits the default form
			$super.submitForm();
		}
	};
});
