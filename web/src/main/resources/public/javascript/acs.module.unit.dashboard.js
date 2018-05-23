/*global xAPS */
/**
 * A module for the Unit Dashboard page.
 * 
 * Depends on the iframedialogs plugin (xaps.plugin.iframedialogs.js) in the updateLineStatus() method.
 * 
 * NOTICE
 * Accesses partial services in the UnitStatusPage within the _initForm() method, Spring 3.0 REST JSON services, 
 * through the "app" spring servlet dispatcher. For more information about this, see app servlet in web.xml and app.xml.
 * 
 * TEMPLATES
 * /WebContent/templates/unit-status/_current.ftl // Includes this script and initializes the module
 * 
 * @author Jarl Andre Hubenthal
 */
ACS.createModule("unit.dashboard", function($this, $super, $) {
	/**
	 * The configuration.
	 */
    $this.settings = {
        continueToUpdateLineStatus : true, // the continuous updateLineStatus process can be interrupted by changing this to false
        pageId: null, // REQUIRED
        unitId: null, // REQUIRED
        showHardware: null,
        showVoip: null
    };
    
    /**
     * Trying to gracefully "quit" all running processes,
     * and then redirect to the supplied url.
     * 
     * @param url any url
     */
    $this.abortLineUpdates = function(url){
    	window.stop();
    	$this.settings.continueToUpdateLineStatus = false;
    	$this['lineUpdateCheck'].abort();
    	if ($this.getTotalScoreNumber)
    		$this.getTotalScoreNumber.abort();
    	if($this.getTotalScoreEfect)
    		$this.getTotalScoreEffect.abort();
    	$("#overallstatus-speedometer").attr("src","");
    	window.clearTimeout($this["timeoutlinecheck"]);
    	window.location = url;
    };

    /**
     * Updates the visual representation of line statuses.
     * And also closes any real time mos dialogs if line goes down.
     * Runs continually, as long as <code>$this.settings.continueToUpdateLineStatus</code> is true
     */
    $this.updateLineStatus = function(timeout) {
        $this["lineUpdateCheck"] = $.ajax({
            async: true,
            url : "app/"+$this.settings.pageId+"/linesup?unitId="+$this.settings.unitId,
            dataType : 'json',/*
        	 * T
        	 */
            success : function(data) {
            	var link = $("#line1Status");
                if (data.line1 === true) {
                    link.html(link.attr("mos_inacall"));
                    if(!link.attr("old_href")){
                    	link.attr("old_href",link.attr("href"));
                    }
                    link.attr("href",link.attr("mos_href"));
                } else {
                    link.html(link.attr("mos_registered"));
                    link.removeAttr("href");
                    if(IFRAMEDIALOGS.dialogs["Real Time Mos: Line 1"]){
                    	IFRAMEDIALOGS.dialogs["Real Time Mos: Line 1"].dialog("close");
                    }
                }
                
                link = $("#line2Status");
                if (data.line2 === true) {
                    link.html(link.attr("mos_inacall"));
                    if(!link.attr("old_href")){
                    	link.attr("old_href",link.attr("href"));
                    }
                    link.attr("href",link.attr("mos_href"));
                } else {
                    link.html(link.attr("mos_registered"));
                    link.removeAttr("href");
                    if(IFRAMEDIALOGS.dialogs["Real Time Mos: Line 2"]){
                    	IFRAMEDIALOGS.dialogs["Real Time Mos: Line 2"].dialog("close");
                    }
                }
                
                if ($this.settings.continueToUpdateLineStatus) {
                    $this["timeoutlinecheck"] = setTimeout(function() {
                        $this.updateLineStatus(timeout);
                    }, timeout);
                }
            }
        });
    };
    
    /**
     * The constructor.
     * 
     * First we check for the presence of pageId and unitId, and alerts if they are not present.
     * 
     * Then retrieve the totalscore number
     * - if successful we retrieve the totalscore effect
     */
    $this._initForm = function() {
    	if($this.settings.pageId==null){
    		alert("The Unit Dashboard has not been configured properly. Missing the pageId configuration setting.");
    	}else if($this.settings.unitId==null){
    		alert("The Unit Dashboard has not been configured properly. Missing the unitId configuration setting.");
    	}else{
	    	if($this.settings.showVoip) {
	    		$this.getTotalScoreNumber = $.ajax({
		            async: true,
		            url : "app/"+$this.settings.pageId+"/totalscore-number?unitId="+$this.settings.unitId+"&start="+$this.settings.start+"&end="+$this.settings.end,
		            success : function(data) {
		                $('#unitTotalScore').html(data);
		                $this.getTotalScoreEffect = $.ajax({
		                    async: true,
		                    url : "app/"+$this.settings.pageId+"/totalscore-effect?unitId="+$this.settings.unitId+"&start="+$this.settings.start+"&end="+$this.settings.end,
		                    dataType : 'json',
		                    success : function(data) {
		                        if (data && data.score && data.color) {
		                            $('#unitTotalScoreEffect').show().html("( -<span style='color:" + data.color + "';>" + data.score + "</span> )");
		                        }
		                    }
		                });
		            }
		        });
	    	}
	
	        $("#overallstatus-speedometer").attr("src", "app/"+$this.settings.pageId+"/overallstatus?unitId="+$this.settings.unitId+"&start="+$this.settings.start+"&end="+$this.settings.end);
	
	        $this.updateLineStatus(5000);
    	}
    };
});
