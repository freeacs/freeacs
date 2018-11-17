/*global xAPS */
/**
 * The xAPS module for the Search page.
 * 
 * Doesn't really do that much magical. 
 * 
 * Only thing that could make it hard to read is the references to $this.settings
 * It makes it easier to change settings without the need to skim the code.
 * 
 * This module is initialized runtime and depends on a supplied url for the "add new parameter" on advanced mode.
 * 
 * TEMPLATES
 * /WebContent/templates/search/ // Contains the index.ftl file and some other files that is included in index.ftl.
 * 
 * @author Jarl Andre Hubenthal
 */
ACS.createModule("search",function($this, $super, $){
	/**
	 * The configuration.
	 */
	$this.settings = {
        submit : {
            onchange : ".submitonchange"
        },
        loading: {
            selector : "#loading_message",
            message : "Loading ..."
        },
        addparameter: {
        	input: "#addparameter",
        	template: "#parametertemplate",
        	url: null // REQUIRED, only supplied in advanced mode
        },
        enable: {
        	selector: ".enableOrDisable"
        }
	};

	/**
	 * The constructor.
	 */
	$this._initForm = function() {
        $($this.settings.defaultFormId).submit(function(){
        	$this.loading();
        });
        
        $($this.settings.submit.onchange).change(function(){
            $super.submitForm();
        });
        var enableDisableCheckboxFunction = function(){
        	var isEnabled = $(this).data("enabled");
        	if(isEnabled){
        		if($(this).val()==""){
        			$(this).data("enabled",false);
        			var radio = $(this).closest("tr").find("td input:checkbox[value='true']");
        			radio.attr("checked",false);
        		}
        	}else{
        		if($(this).val()!=""){
        			$(this).data("enabled",true);
        			var radio = $(this).closest("tr").find("td input:checkbox[value='true']");
        			radio.attr("checked",true);
        		}
        	}
        };
        $($this.settings.enable.selector).keyup(enableDisableCheckboxFunction);
        if($this.settings.addparameter.url){
        	$($this.settings.addparameter.input).autocomplete({
				source: $this.settings.addparameter.url,
				minLength: 1,
				select: function(event,ui){
					var selectedParameter = ui.item.value;
					var processed = $.jqote($this.settings.addparameter.template, {param: selectedParameter});
					processed = $(processed);
					var tr = $($this.settings.addparameter.input).closest("tr");
					tr.before(processed);
					ui.item.value = "";
					processed.find("input:text").keyup(enableDisableCheckboxFunction);
				}
			});
        }
	};
	
	/**
	 * Places the loading message in the loading selector.
	 */
	$this.loading = function() {
		$($this.settings.loading.selector).html($this.settings.loading.message);
	};
});
