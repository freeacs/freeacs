/*global xAPS */
/**
 * The generic xAPS module for all types of Reports.
 * 
 * Configures the basic functionality and binds functions to button clicks.
 * 
 * Especially interesting is the prePostback() method call in the updateReport() method definition.
 * It checks if the module has a prePostback() method defined, and if it does, 
 * it is then being called, supplying the url and a reference to jQuery ($)-
 * 
 * There is no normal form submit on the Report page. At the end of the updateReport() method, 
 * it accesses <code>$super.goTo(url);</code> that is actually a <code>window.location=url</code>. 
 * By using the mainModules goTo method, we ensure that the mainModule is allowed to hack the url before it is being redirected.
 *
 * Notice that in the updateReport() method we use a safe and simple way of appending values to an array 
 * (and we also do not declare it as an array because that is not good practice.. we use the short form [])
 * that enables use to move parts around without the need to take care about indexes.
 * 
 * NOTICE
 * There is also a special thing to notice, in the updateReport() method, there is a special section for adding context info to the url.
 * This is actually not very well designed, as it will pass on the context information from the context bar,
 * and not the context information that is added to the enclosing form by changes in the context bar selects.
 * So each time the update report is called context info is updated on the server side, potentially causing a slower response.
 * 
 * TEMPLATES
 * /WebContent/templates/report/ 			// Contains the dashboard, index and reportgraph file.
 * /WebContent/templates/report/reportgraph.ftl 	// Includes and initializes the module script.
 * /WebContent/templates/report/custom/ 		// Contains the templates for different custom report types.
 * /WebContent/templates/report/custom/Group.ftl 	// A custom report template example. Defines the prePostback callback on the module.
 * 
 * @author Jarl Andre Hubenthal
 */
ACS.createModule("report", function($this, $super, $){
	/**
	 * The configuration.
	 */
	$this.settings = {
		submit : {
			onchange : ".submitonchange",
			button : "#updateButton"
		},
		reset : {
			button : "#resetButton"
		},
		switchMode : {
			button : "#advancedView"
		},
		realtime: {
			radio : "input[name='realtime']:radio"
		}
	};
	
	/**
	 * The constructor
	 */
	$this._initForm = function() {
		$super.setupFromCalendar($this.reloadReport);
		$super.setupToCalendar($this.reloadReport);
		$($this.settings.submit.onchange).change(function() {
			return $this.reloadReport();
		});
		$($this.settings.submit.button).click(function() {
			return $this.reloadReport();
		});
		$($this.settings.reset.button).click(function() {
			return $this.resetReport();
		});
		$($this.settings.switchMode.button).click(function() {
			return $this.switchMode();
		});
		$($this.settings.realtime.radio).change(function(){
			return $this.reloadReport();
		});
		if($this.settings.realtime.enabled){
			setTimeout(function(){$this.reloadReport();},5000);
		}
	};
	
	/**
	 * Collects all necessary details about the report and then redirects to a generated url.
	 */
	$this.updateReport = function(highLightLegend) {
		var url = [];
		url[url.length] = "type=" + $this.settings.type;
		if($super.parseDate($("#start").val())>$super.parseDate($("#end").val())){
			alert("Start date is not before end date!");
			return false;
		}else{
			url[url.length] = "start=" + $("#start").val();
			url[url.length] = "end=" + $("#end").val();
		}
		if($($this.settings.realtime.radio+":checked").val()){
			url[url.length] = "realtime=" + $($this.settings.realtime.radio+":checked").val();
		}
		if (highLightLegend !== "undefined") {
			url[url.length] = "legendIndex=" + highLightLegend;
		} else {
			if($("#legend").val()){
				url[url.length] = "legendIndex=" + $("#legend").val();
			}
		}
		url[url.length] = "period=" + $("#period").val();
		if($("#groupselect").val())
			url[url.length] = "groupselect=" + $("#groupselect").val();
		if($("#swversion").val())
			url[url.length] = "swversion=" + $("#swversion").val();
		if($("#method").val()){
			url[url.length] = "method=" + $("#method").val();
		}
		if($("#optionalmethod").val()){
			url[url.length] = "optionalmethod=" + $("#optionalmethod").val();
		}
		$("input[name='aggregate']:checked").each(function(){
			if($(this).val()){
				url[url.length] = "aggregate=" + $(this).val();
			}
		});
		
		url[url.length] = "advancedView=" + $("#advancedView_setting").val();
		
		// IMPORTANT
		// Append context bar information to the url
		if($("input[name='contextunittype']").val()){
			url[url.length] = "contextunittype="+ $("input[name='contextunittype']").val();
			if($("input[name='contextprofile']").val()){
				url[url.length] = "contextprofile="+ $("input[name='contextprofile']").val();
			}
		}
		
		url = $super.makeUrl("report", url.join("&"));
		
		// IMPORTANT
		// This is VERY important for being able to "inject" custom url attributes for some reports
		// One example is the Group report that needs to add the group name to the url
        if(typeof $this.prePostback === "function"){
            url = $this.prePostback(url,$);
        }
        url[url.length] = "advancedView=" + $("#advancedView_setting").val();
		$super.goTo(url);
		
		return false;
	};
	
	/**
	 * Actually the same as hitting the Update button, and it could safely be removed,
	 * it is being called when the realtime radio changes. It could be that we needed some special logic for this.
	 */
	$this.reloadReport = function() {
		return $this.updateReport();
	};
	
	/**
	 * It resets all settings in the report by calling a url that only contains the report type.
	 */
	$this.resetReport = function() {
		var urlToRedirectTo = $super.url("report", "type=" + $this.settings.type, false);
		$super.redirect(urlToRedirectTo);
		return false;
	};
	
	/**
	 * As the name implies, it switches the view from simple to advanced and vice versa.
	 */
	$this.switchMode = function() {
		var advancedView = $("#advancedView_setting");
		if (advancedView.val() === 'true'){
			advancedView.val('false');
			$("#groupselect").val(".");
			$("#swversion").val(".");
		}else{
			advancedView.val('true');
		}
		this.reloadReport();
		return false;// Disable submit behaviour
	};
});
