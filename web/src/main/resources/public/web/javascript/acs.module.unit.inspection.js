/*global xAPS */
/**
 * A module for the Unit Configuration page, Inspection mode.
 * 
 * Starts the monitoring of the mode and state parameters (Inspection mode) and reloads the page if something has changed.
 * 
 * TEMPLATES
 * /WebContent/templates/unit/details.ftl // Includes this script and initializes the module
 * 
 * @author Jarl Andre Hubenthal
 */
ACS.createModule("unit.inspection",function($this, $super, $){
	/**
	 * The configuration.
	 */
    $this.settings = {
    	modeSelector: "input[name='update::System.X_FREEACS-COM.ProvisioningMode.Cache']",
    	stateSelector: "input[name='update::System.X_FREEACS-COM.ProvisioningState.Cache']",
    	messageSelector: "#unitkickmessage",
    	inspectionPageId: null, // REQUIRED the page id for the background callback to the inspection page
    	unitPageId: null, // REQUIRED the page id for reloading (or redirecting to) the unit page
    	unitId: null // REQUIRED
    };
    
    /**
     * The constructor.
     * 
     * First we check for the presence of inspectionPageId, unitPageId and unitId, and alerts if they are not present.
     * 
     * Then executes executeModeAndStateMonitoring() method.
     */
	$this._initForm = function(){
		if($this.settings.inspectionPageId==null){
    		alert("The Unit Inspection module has not been configured properly. Missing the inspectionPageId configuration setting.");
    	}else if($this.settings.unitPageId==null){
    		alert("The Unit Inspection module has not been configured properly. Missing the unitPageId configuration setting.");
    	}else if($this.settings.unitId==null){
    		alert("The Unit Inspection module has not been configured properly. Missing the unitId configuration setting.");
    	}else{
    		executeModeAndStateMonitoring();
    	}
	};
	
	/**
	 * Inspection; checking mode and state continuously.
	 * Can be understand as a background loop, because it fires off it self.
	 */
	function executeModeAndStateMonitoring() {
		var messageLabel = $($this.settings.messageSelector);
		try {
			var mode = $($this.settings.modeSelector).val();
			var state = $($this.settings.stateSelector).val();

			if (mode && state) {
				var url = "?page="+$this.settings.inspectionPageId+"&unit=" + $this.settings.unitId + "&mode=" + mode + "&state=" + state + "&timestamp="+new Date().getMilliseconds();
				
				var method = function(data) {
	                var txt = data;
	                
	                var cmd = "";
	                if (txt.length > 3)
	                	cmd = txt.substr(0, 4);

	                var value = null;
	                if (txt.length > 4)
	                    value = txt.substr(4);

	                messageString = messageLabel.html().trim();

	                if (cmd == "relo") {
	                    url = "?page="+$this.settings.unitPageId+"&unit=" + $this.settings.unitId;
	                    window.location.href = unescape(url);
	                } else if (cmd == "wait" && value != null && value.trim().length>0) {
	                    messageLabel.html(value);
	                }
	                
					window.setTimeout(executeModeAndStateMonitoring, 3000);
				};
				
				if (mode == "EXTRACTION" || mode == "INSPECTION" || mode == "KICK")
	                $.get(url,method);
			}
		} catch (e) {
			alert("Error occured with inspection mode: "+e);
			
			if(messageLabel)
				messageLabel.html("<b color='red'>" + e + "</b>");
			
			setTimeout(executeModeAndStateMonitoring, 3000);
		}
	}
});