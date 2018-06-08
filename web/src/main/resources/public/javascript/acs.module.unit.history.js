/*global xAPS */
/**
 * A module for the Unit History page.
 * 
 * It is a bit more complicated that necessary but all the logic is necessary for the page to work.
 * 
 * In future upgrades of this module some parts of this module should be moved up the main module (xaps.js).
 * 
 * Contains a jewel, the function datediff. It is used to calculate the difference between two dates.
 * Since it is only used here we keep it in this module.
 * 
 * TEMPLATES
 * /WebContent/templates/unit-status/_history.ftl // Includes this script and initializes the module
 * 
 * @author Jarl Andre Hubenthal
 */
ACS.createModule("unit.history",function($this, $super, $){
	/**
	 * The configuration.
	 */
    $this.settings = {
    	pageId: null, // REQUIRED
    	unitId: null, // REQUIRED
    	selectedTab: null,
    	showHardware: null,
    	showVoip: null
    };

    /**
     * The constructor.
     * 
     * First we check for the presence of pageId and unitId, and alerts if they are not present.
     * 
     * Initialize the tabs
     * Select the currently selected tab
     * Initialize each tabs content
     */
    $this._initForm = function() {
    	if($this.settings.pageId==null){
    		alert("The Unit Dashboard has not been configured properly. Missing the pageId configuration setting.");
    	}else if($this.settings.unitId==null){
    		alert("The Unit Dashboard has not been configured properly. Missing the unitId configuration setting.");
    	}else{
    		$("#tabs").tabs({
	    	   select: function(event, ui) {
	               if($this.activeCall)
	            	   $this.activeCall.abort();
	    		   if (ui.panel.id === 'voipDiv')
	    			   $("#VoipButton").click();
	    		   else if (ui.panel.id === 'hardwareDiv')
	    			   $("#HardwareButton").click();
	    		   else if (ui.panel.id === 'syslogDiv')
	    			   $("#SyslogButton").click();
	    	   }
	        });
	        if($this.settings.selectedTab){
	            $("#tabs").tabs("select",$this.settings.selectedTab);
	        }
	        if ($this.settings.showVoip)
	        	$this.initVoipTab();
	        if ($this.settings.showHardware)
	        	$this.initHardwareTab();
	        $this.initSyslogTab();
    	}
    };

    /**
     * JEWEL function (meaning worth to keep and reuse)
     * 
     * Calculates the difference between two dates. The third argument is a string that is either years, months, weeks, days, hours,minutes or seconds.
     */
    var datediff = function(date1,date2,interval) {
        var second=1000, minute=second*60, hour=minute*60, day=hour*24, week=day*7;
        var timediff = date2 - date1;
        if (isNaN(timediff)) return NaN;
        switch (interval) {
            case "years":
                return date2.getFullYear() - date1.getFullYear();
            case "months":
                return (
                ( date2.getFullYear() * 12 + date2.getMonth() ) -
                ( date1.getFullYear() * 12 + date1.getMonth() )
                );
            case "weeks"  :
                return Math.floor(timediff / week);
            case "days"   :
                return Math.floor(timediff / day);
            case "hours"  :
                return Math.floor(timediff / hour);
            case "minutes":
                return Math.floor(timediff / minute);
            case "seconds":
                return Math.floor(timediff / second);
            default:
                return undefined;
        }
    };

    /**
     * Initialize a tab
     * Configures calendars
     * Configures change and click events
     * And clicks the Update button
     */
    var _initTab = function(needsreloadonchange,updatebutton,type){
        $(needsreloadonchange).change(function(){
            return $this.updateReport(type);
        });
        $(updatebutton).click(function(){
            return $this.updateReport(type);
        });
        $super.setupFromCalendar(null,{
            field: type+"start",
            button: type+"start_img"
        });
        $super.setupToCalendar(null,{
            field: type+"end",
            button: type+"end_img"
        });
        $(updatebutton).click();
    };

    /**
     * Init the Voip tab
     * A wrapper around _initTab
     * setTimeout is delayed for 100ms
     */
    $this.initVoipTab = function(){
        setTimeout(function(){_initTab("#VoipControlsDiv .needsgraphreload","#VoipButton","Voip");},
        	100);
    };
    /**
     * Init the Hardware tab
     * A wrapper around _initTab
     * setTimeout is delayed for 300ms
     */
    $this.initHardwareTab = function(){
    	setTimeout(function(){_initTab("#HardwareControlsDiv .needsgraphreload","#HardwareButton","Hardware");},
    		300);
    };
    /**
     * Init the Syslog tab
     * A wrapper around _initTab
     * setTimeout is delayed for 600ms
     */
    $this.initSyslogTab = function(){
    	setTimeout(function(){_initTab("#SyslogControlsDiv .needsgraphreload","#SyslogButton","Syslog");},
    		600);
    };

    /**
     * Generates a url for use with async GET.
     * If using it with POST, this url will not produce the expected response from the backend server.
     */
    var _makeGetUrl = function(){
        var url = "?";
        for(var i=0;i<arguments.length;i++){
            if(arguments[i]!="")
                url+=arguments[i].trim()+"&";
        }
        if(url.length>4)
            return url.slice(0,url.length-1).trim();
        return url;
    };

    /**
     * First it places a spinning wheel where the image should be
     * Then it configures a timeout for setting the src to the supplied url, to enable the feeling that it actually changes.
     */
    var _updateReportImg = function(imageId,urlToGet){
    	$(imageId).attr("src","images/spinner.gif");
        setTimeout(function(){$(imageId).attr("src",urlToGet);},50);
    };
    
    /**
     * The main part of the page, where the image and table is loaded.
     * It retrieves the start and end date, and other inputs,
     * and downloads a table from the server,
     * and sets the src of the image to a spinning wheel to enable a loading effect,
     * before it sets the src of the image to retrieve the graph.
     */
    var refreshImageAndTable = function(id){
        var startCache = $("#"+id+"start_original").val();
        var startSelected = $("#"+id+"start").val();
        var endCache = $("#"+id+"end_original").val();
        var endSelected = $("#"+id+"end").val();
        
        var checkboxes = [];
        $("input[name='"+id+"aggregate']").each(function(){
            if(this.checked){
               checkboxes[checkboxes.length]="aggregate="+this.value;
            }
        });
        checkboxes = checkboxes.join("&");
    	
        var method = $("#"+id+"method").val();
        var period = $("#"+id+"period").val();
        
        var syslogFilter = $("#syslogFilter").val();
        
        var tableUrlArr = [];
        tableUrlArr[tableUrlArr.length] = "type="+id;
        tableUrlArr[tableUrlArr.length] = "method="+method;
        tableUrlArr[tableUrlArr.length] = "period="+period;
        tableUrlArr[tableUrlArr.length] = "start="+startSelected;
        tableUrlArr[tableUrlArr.length] = "end="+endSelected;
        tableUrlArr[tableUrlArr.length] = "unitId="+$this.settings.unitId;
        tableUrlArr[tableUrlArr.length] = "time="+new Date();
        if(syslogFilter)
        	tableUrlArr[tableUrlArr.length] = "syslogFilter="+syslogFilter;
        tableUrlArr[tableUrlArr.length] = checkboxes;
        
        tableUrl = ("app/"+$this.settings.pageId+"/charttable?")+tableUrlArr.join("&");
        $this.activeCall = $.ajax({
        	async:true,
            url: tableUrl,
            success: function(data) {
                $("#"+id+"status-table").html(data);
            }
        });
        
        imageUrl = ("app/"+$this.settings.pageId+"/chartimage?")+tableUrlArr.join("&");
        _updateReportImg("#"+id+"Image",imageUrl);
    };

    /**
     * This is called when changing selects or hitting the Update button.
     * If date is different from cache, it checks if the dates are compatible.
     */
    $this.updateReport = function(id){
        var startCache = $("#"+id+"start_original").val();
        var startSelected = $("#"+id+"start").val();
        var endCache = $("#"+id+"end_original").val();
        var endSelected = $("#"+id+"end").val();
        
        if(startCache==startSelected && endCache==endSelected){
            refreshImageAndTable(id);
            return false;
        }else{
            var MAX_DAYS_SPAN = 7;
            var startDate = Date.parseDate(startSelected,$this.settings.calendar.dateFormat);
            var endDate = Date.parseDate(endSelected,$this.settings.calendar.dateFormat);
            if(startDate>endDate){
            	alert("Start date is not before end date!");
            }else if(endDate.getFullYear()==startDate.getFullYear() && endDate.getMonth()==startDate.getMonth() && datediff(startDate,endDate,"days")>MAX_DAYS_SPAN){
                var msg = "The selection is more than "+MAX_DAYS_SPAN+" days!\nThis limit is configured to avoid slow loading speeds.";
                alert(msg);
            }else if(endDate.getFullYear()!=startDate.getFullYear() || endDate.getMonth()!=startDate.getMonth()){
            	var msg = "The selection must be maximum spanning "+MAX_DAYS_SPAN+" days and be within the same year!";
            	alert(msg);
            }else{
            	refreshImageAndTable(id);
            }
            return false;
        }
    };
});
