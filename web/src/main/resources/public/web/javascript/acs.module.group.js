ACS.createModule("group",function($this, $super, $){
	/**
	 * The configuration.
	 */
	$this.settings = {
        createCheckboxSelector:"input[type='checkbox'][name^='create::']",
        deleteCheckboxSelector:"input[type='checkbox'][name^='delete::']",
        operatorSelect: function(parameterName){
        	return $("select[name='operator::"+parameterName+"']");
        },
		datatypeSelect: function(parameterName){
	    	return $("select[name='datatype::"+parameterName+"']");
	    },
	    inputField: function(parameterName){
	    	return $("input[name='update::"+parameterName+"']");
	    },
	    inputSelect: function(parameterName){
	    	return $("select[name='update::" +parameterName+"']");
	    }
	};

	/**
	 * The constructor.
	 */
	$this._initForm = function() {
		$($this.settings.createCheckboxSelector).click(function(){
			$this.configureOnclickForCreateCheckboxes(this);
		});
		$($this.settings.deleteCheckboxSelector).click(function(){
			$this.configureOnclickForDeleteCheckboxes(this);
		});
	};

	$this.configureOnclickForCreateCheckboxes = function(input) {
		var checkbox = $(input);
		var data = checkbox.metadata();
		var parameterName = data.parameter;
		if(checkbox.attr("toggled")){
			checkbox.removeAttr("toggled");
			fadeOutInputs(parameterName);
		}else{
			checkbox.attr("toggled",true);
			fadeInInputs(parameterName);
		}
	};
	
	$this.configureOnclickForDeleteCheckboxes = function(input) {
		var checkbox = $(input);
		var data = checkbox.metadata();
		var parameterName = data.parameter;
		if(checkbox.attr("toggled")){
			checkbox.removeAttr("toggled");
			fadeInInputs(parameterName);
		}else{
			checkbox.attr("toggled",true);
			fadeOutInputs(parameterName);
		}
	};
	
	var fadeInInputs = function(parameterName){
		$this.settings.operatorSelect(parameterName).fadeIn();
		$this.settings.datatypeSelect(parameterName).fadeIn();
		$this.settings.inputField(parameterName).fadeIn();
		$this.settings.inputSelect(parameterName).fadeIn();
	};
	
	var fadeOutInputs = function(parameterName){
		$this.settings.operatorSelect(parameterName).fadeOut();
		$this.settings.datatypeSelect(parameterName).fadeOut();
		$this.settings.inputField(parameterName).fadeOut();
		$this.settings.inputSelect(parameterName).fadeOut();
	};
});
