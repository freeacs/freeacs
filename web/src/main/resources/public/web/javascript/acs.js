/*global jQuery */
/**
 * xAPS JavaScript v1.0-FINAL (first and final version)
 * 
 * 
 * INDEX:
 * - INTRODUCTION
 * - THE CORE FUNCTIONALITY OF XAPS JAVASCRIPT
 * - HOW MODULES RELATE TO THEIR PARENTS
 * - SOME THOUGHTS ABOUT THE LIBRARY
 * - PATTERN DESCRIPTION
 * - TEMPLATES
 * 
 * INTRODUCTION
 * 
 * The xAPS JavaScript library is a tool for creating private containers of logic for any given page as a whole or a parts of the page.
 * 
 * Every page module can have one or more parents, thus making it possible to create a specific order of inheritance. 
 * This enables module children to access methods and settings from its known parent(s).
 * 
 * 
 * THE CORE FUNCTIONALITY OF XAPS JAVASCRIPT
 * 
 * The methods that form the core of the xAPS object is: initModule, createModule, initSettings and _initForm. 
 * See section UTILITY METHODS for more methods on the xAPS object.
 * 
 * 1. The createModule method is the method that is used most in the xAPS object. 
 * Every new page should have its own "module", its own enclosed scope or private area where they can define private methods and events, 
 * and where they override default settings and implement their private _initForm method, that is called automatically by xAPS on page ready state in the xAPS._initForm method discussed later.
 * 
 * 2. The initSettings method is the entry point for changing the default and hard coded values of the settings in the module of interest, before the page has finished loading 
 * (and before the xAPS object calls the initModule, because we want the settings to be configured before initializing of the module starts).
 * It should be called right after including the module javascript file. An example is the Unit Dashboard, where the following two parts make the page work properly:
 * 
 * <code>
 *     <script src="javascript/xaps.module.unit.dashboard.js" tyoe="text/javascript"></script>
 *     // .....
 *     <script>
 *       xAPS.unit.dashboard.initSettings({
 *          start: '${start}',
 *          end: '${end}',
 *          unitId: '${info.unit.id}',
 *          pageId: '${URL_MAP.UNITSTATUS.id}'
 *       });
 *     </script>
 * </code>
 * 
 * As you can see the unit dashboard is passed a start and end timestamp, as well as a unit id, and also a page id that is going to be added to the url in asynchronous jQuery requests (as 'web?page=pageId&unit=...&start=...&end=...').
 * 
 * 3. The initModule method is called automatically by the xAPS object when the document is ready (http://api.jquery.com/ready/), 
 * but can also be called directly ahead of document ready state for any given module. The only problem with such pre-initialization is 
 * the fact that in such cases access to the javascript DOM is not possible before the page has finished loading. 
 * Any such direct calls to initModule must therefore either be wrapped in a $(document).ready( ... ) statement or 
 * contain logic that is not dependent on the DOM model, for example if it its sole purpose was to compute stuff needed by other modules. But this just theoretical.
 * 
 * 4. The _initForm method is kind of like the constructor for all modules in xAPS, even for the xAPS object itself. 
 * What this method does in the case of the xAPS object is to configure the basic layout, with menu functionality, help etc.
 * It also initializes all of its sub modules, so this is actually where the xAPS object calls initModule on each of the sub modules.
 * 
 * 
 * HOW MODULES RELATE TO THEIR PARENTS
 * 
 * An interesting thing to look at when discussing how the module pattern works is to look at another module, the search page module, 
 * because it is so dead simple and yet it describes the whole xAPS module system perfectly:
 * 
 * <code>
 * xAPS.createModule("search",function($this,$super,$){
 *	$this.settings = {
 *       submit : {
 *           onchange : ".submitonchange"
 *       },
 *       loading: {
 *           selector : "#loading_message",
 *           message : "Loading ..."
 *       }
 *	};
 *
 *	$this._initForm = function() {
 *       $($this.settings.defaultFormId).submit(function(){
 *               $this.loading();
 *       });
 *       $($this.settings.submit.onchange).change(function(){
 *               $super.submitForm();
 *       });
 *	};
 *	
 *	$this.loading = function() {
 *		$($this.settings.loading.selector).html($this.settings.loading.message);
 *	};
 *});
 * </code>
 * 
 * $this refers to the module being created, and as you see it augments (http://www.crockford.com/javascript/inheritance.html) itself with three properties:  settings, _initForm and loading. 
 * 
 * $super refers to the main module also known as xAPS, and is used as a shortcut for simple access to xAPS methods (instead of accessing them with $this.parent.parent.parent ... etc)
 * 
 * $ refers to the jQuery function. See http://jquery.com/ for more info about jQuery.
 * 
 * Look at the _initForm method. You can see it accesses a defaultFormId from $this.settings. But this variable is not defined in the module settings. 
 * It is defined as a default hard coded setting in the xAPS object and is merged with the module settings when running the createModule method. 
 * By doing this we ensure that the module can safely override the xAPS settings while still maintaining the original xAPS settings constant. And no modules is affected by another module overriding an xAPS setting.
 * By knowing that all xAPS settings is merged into each module, you can easily spot when the setting is a "global" setting, and when it is a "private" module setting,
 * and you can learn to love the idea that you do not have to type so much code to be able to use the settings from the xAPS object. For example if the settings were not merged
 * you would have to type <code>$super.settings</code> or <code>$this.parent.settings</code>, and it would be more difficult to read than accessing implicit settings in the modules own settings object.
 * It can of course be discussed.
 * 
 * In addition to accessing the xAPS default settings within the modules own settings object, it is also possible to execute methods defined on the xAPS object,
 * and you can see it is being done where you see <code>$super.submitForm();</code>. submitForm is not a method on the module, but a method definition on the xAPS object.
 * By saying <code>$super.submitForm();</code> you say "call the submitForm function on the xAPS object". You could also easily have said <code>$this.parent.submitForm();</code>,
 * but as you can see it takes more coding and is more error prone if you forget how many parents the module has. In this case the module has only one parent, the xAPS object,
 * but it could easily be created like this:
 * 
 * <code>
 * xAPS.createModule("search.page",function($this,$super,$){
 * 	......
 * });
 * </code>
 * 
 * In the above code you define a "page" module within the "search" module. 
 * And you would have to type <code>$this.parent.parent.submitForm</code> in the "page" module if using the parent technique (difficult to read) to access the submitForm in the xAPS object.
 * To access methods on the "search" module methods you would type <code>$this.parent.someMethod</code>.
 * 
 * By creating your page modules like this, you make it easy to create multiple different modules on one page.
 * If you for example wanted to have a common generic functionality you could first create the "search" module, 
 * then you would probably want to add a module for the submit behavior, "search.submitlogic", 
 * and maybe you also needed an auto complete behavior also, "search.autocomplete" ? 
 * The following represents the three files (or one file, if you decide) you would need to create.
 * 
 * <code>
 * xAPS.createModule("search",function($this,$super,$){
 * 	// Generic search page functionality here
 * });
 * </code>
 * 
 * <code>
 * xAPS.createModule("search.submitlogic",function($this,$super,$){
 * 	// submit logic here
 * });
 * </code>
 * 
 * <code>
 * xAPS.createModule("search.autocomplete",function($this,$super,$){
 * 	// auto complete behaviour here
 * });
 * </code>
 * 
 * You won't need to initialize the settings for all of these modules. You could be fine with initializing the parent module, the "search" module,
 * because the sub modules of "search" ("submitlogic" and "autocomplete") will then have access to "search" settings by <code>$this.parent.settings</code>.
 * 
 * Please note that a module is allowed to chain its way up to another module and its methods by accessing other known sub modules of its parent:
 * 
 * <code>
 * $this.parent.search.autocomplete.someMethod();
 * </code>
 * 
 * But it can only access "public" methods, variables (var's) are hidden within the module scope.
 *
 *
 * SOME THOUGHTS ABOUT THE LIBRARY
 * 
 * A good use of this library is based upon the pragmatic behavior of the maintainers. 
 * A must read for maintainers is "JavaScript: The Good Parts" and "JavaScript Patterns",
 * both is excellent books on their targeted topics. Its basically the same as reading "Effective Java",
 * which is the same kind of book for Java developers.
 * 
 * One thing to remember and use as a tool:
 * 
 * If you are creating a url, you can combine strings and be fine with the following:
 * 
 * "web?page=search"+"&unittype=..."+"&profile=...." etc
 * 
 * But it is extremely error prone. If you forget an amp in there, you have a bug.
 * It is therefore better to do it like this:
 * 
 * <code>
 * var url = [];
 * url[url.length] = "web?page=search";
 * url[url.length] = "unittype=....";
 * url[url.length] = "profile=.....";
 * url = url.join("&");
 * </code>
 * 
 * In addition to not having to think about amps, you also in this case don't have to think about array indexes, 
 * because I have used a clever trick to use the current size as the new index. making it easy to reorder and move parts of the function.
 * 
 * Another thing to remember:
 * 
 * You should avoid hard coded selectors and other settings within the _initForm method. Try to always reference the settings object,
 * as you saw in HOW MODULES RELATE TO THEIR PARENTS, where the search module accesses the defaultFormId from its own settings that has been merged into the default xAPS settings.
 * By always referencing the module settings (or a parents settings), you can easily change these setting without having to look into the modules logic.
 * 
 * Finally:
 * 
 * I have tried to use best practices, but in xaps.js the _initForm method is a bit overwhelmed.
 * This is because I have placed all shared page functionality in there, and have rapidly fixed it along the way.
 * Some of the parts of this _initForm method could be refactored into their own respective modules.
 * Think about this when you maintain it, if you should make a module of what you are fixing/upgrading.
 * 
 * PATTERN DESCRIPTION:
 * 
 * The xAPS JavaScript uses the commonly known module pattern, see more at http://www.adequatelygood.com/2010/3/JavaScript-Module-Pattern-In-Depth
 * 
 * This pattern makes it very easy to encapsulate and hide private methods and variables, while at the same time exposing the methods you want to be public.
 * 
 * The main idea is to avoid cluttering the global space with variables that does not have their own direct specific meaning.
 * 
 * Global objects is evil, and should therefore be avoided. 
 * 
 * By enclosing all xAPS javascript code within the xAPS object, you make sure that you know where to look when an error occurs.
 * 
 * Debugging is a lot more easier. Period.
 * 
 * TEMPLATES
 * /WebContent/WEB-INF/templates/meta/web.ftl // Included by index.ftl
 *  
 * @author Jarl Andre Hubenthal
 * 
 * @module xAPS
 * 
 * @requires jQuery, window
 */
var ACS = (function(mainModule, $, window) {
	/**
	 * FOR DEBUG PURPOSES ONLY (makes it easy to look at the modules in the xAPS object in for ex FireBug)
	 * 
	 * References to each sub module.
	 * 
	 * Could also be a variable (var), but since we want it to be visible in FireBug, it must be a property on the xAPS object (mainModule).
	 */
	mainModule.submodules = [];
	
    /**
     * @method createModule
     * 
	 * Adds a new submodule. A submodule can consist of several parent modules, dot separated. See documentation for the xAPS object.
	 * 
	 * @param {String} subModuleName
	 *            The name of the new sub module, can be dot separated.
	 * @param {Function} closure
	 * 			  A Function variable that will serve as the module
	 *            constructor. Will receive references to itself, the parent,
	 *            jQUery and the global window property.
	 */
    mainModule.createModule = function(subModuleName, closure) {
        var parts = subModuleName.split("."), module = mainModule, i;
        
        if(parts[0].toUpperCase() === "XAPS"){
            parts = parts.slice(1);
        }

        for(i=0;i<parts.length;i+=1){
            if(typeof module[parts[i]] === "undefined"){
                module[parts[i]] = {};
                module[parts[i]].parent = module;
            }
            module = module[parts[i]];
        }
        
        mainModule.submodules.push(module);

        closure(module, mainModule, $, window);

        module.name = subModuleName;
        
        module.initModule = mainModule.initModule;
        
        module.initSettings = mainModule.initSettings;
        
        // Make a deep copy of the main settings
        var copiedSettings = $.extend(true, {}, mainModule.settings);
        // Deeply override the copied main settings with the module settings
        // Replacing the current module settings with the result
        module.settings = $.extend(true,copiedSettings,module.settings);
    };
    
    /**
     * @method initSettings
     * 
     * Merges a provided settings object with the module settings, if not undefined.
     * 
	 * @param args a settings object that is merged with the hard coded module settings.
	 */
    mainModule.initSettings = function(args){
    	if(typeof args !== "undefined"){
    		// Deeply merge this.settings with args
    		this.settings = $.extend(true,this.settings,args);
    	}    	
    };
    
    /**
     * @method initModule
     * 
	 * This method will be copied to all submodules and serve as the constructor
	 * function for all xAPS modules. Relies on the presence of the _initForm
	 * method in the module to be initialized. If not present, it will not work.
	 * 
	 * If already initialized, this method will do nothing. This means that manually calling
	 * this method on the module will hinder the xAPS from calling calling it automatically.
	 * 
	 * If you call it directly before the page is completely loaded or before the xAPS object is initialized,
	 * make sure you wrap it in a $(document).ready closure. While doing this also make sure to execute this statement
	 * before you include xaps.js, to avoid xaps.js taking precedence in document.ready.
	 * 
	 * @param args a settings object that is merged with the hard coded module settings.
	 */
    mainModule.initModule = function(args) {
        if (!this.initiated) { // Not initiated, lets do it now
        	this.initSettings(args);
            if (typeof this._initForm === 'function') {
                // The module has implemented the _initForm method, lets execute
				// it
                this._initForm();
                // Remove the _initForm method, to avoid double runs
                delete this._initForm;
                // Signal that this module is initiated
                this.initiated = true;
            } else {
                mainModule.log.warn("Module " + this.name + " does not implement the _initForm method");
            }
        }
    };

    /**
     * @method _initForm
     * 
	 * Responsible for initializing the common page functionality shared by all pages, herein the logic for the context bar.
	 * 
	 * Initializes table filtering and collapse memory, for profile, unittype, unit, group and job.
	 * 
	 * Configuring table sorter for tables that is configured with the syslog class.
	 * 
	 * Initializing jquery iframe dialogs. Not used much, and should be fased out.
	 * 
	 * Initializing all children modules
	 * 
	 * Configuring the help system, a generic system that relies on the page ID.
	 * 
	 * Configuring the context bar functionality.
	 */
    mainModule._initForm = function() {
        $(document).ready(function($) {
        	mainModule.log.info("init() started");
        	
        	if(window.ddsmoothmenu){
	            window.ddsmoothmenu.init({
	                mainmenuid : "smoothmenu2",
	                orientation : 'v',
	                classname : 'ddsmoothmenu-v',
	                contentsource : "markup"
	            });
        	}
        	
        	if(window.TABLETREE){
        		var page = mainModule.settings.page;
        		window.TABLETREE.init();
        		if (page == 'job'){
        			window.TABLETREE.filterParameters();
        			//window.TABLETREE.filterJobs();
        		}else{
        			window.TABLETREE.filterParameters();
        		}
        	}

        	if(window.CookieCollapser){
        		window.CookieCollapser.execute();
        	}
        	
            if(mainModule.setupTablesorter){
	        	// Wrap this in try catch because some tables may be empty
	            try{
	                mainModule.setupTablesorter("table.syslog.tablesorter");
	            }catch(e){
	                mainModule.log.warn("Could not setup tablesorter",e);
	            }
        	}
            
            if(window.IFRAMEDIALOGS){
            	window.IFRAMEDIALOGS.init();
            }
            
            for(var i = 0; i<mainModule.submodules.length;i+=1){
            	var submodule = mainModule.submodules[i];
            	if(submodule && (typeof submodule.initModule === "function" && typeof submodule._initForm !== "undefined")){
            		submodule.initModule();
            	}
            }
            
            if($.fn.tipTip){
            	$(mainModule.settings.titlePopupClass).tipTip();
            }
            
            configureSearchFields();
            
			configureContextBar();
			
			if(mainModule.settings.confirmchanges === true){
		    	$("*[value='Update parameters']:submit").click(function(){
		    		var paramsCreated = [];
		    		$("input[name^='create::']:checked").each(function(i){
		    			var checkbox = $(this);
		    			var name = checkbox.attr("name");
		    			var parameter = name.substring("create::".length);
		    			paramsCreated[paramsCreated.length] = parameter;
		    		});
		    		var paramsDeleted = [];
		    		$("input[name^='delete::']:checked").each(function(i){
		    			var checkbox = $(this);
		    			var name = checkbox.attr("name");
		    			var parameter = name.substring("delete::".length);
		    			paramsDeleted[paramsDeleted.length] = parameter;
		    		});
		    		if(paramsCreated.length>0 || paramsDeleted.length>0){
		    			var createdText = paramsCreated.length>0?"You are creating the following parameters:\n"+paramsCreated.join("\n")+"\n":"";
		    			var deletedText = paramsDeleted.length>0?"You are deleting the following parameters:\n"+paramsDeleted.join("\n")+"\n":"";
		    			return confirm(createdText+deletedText);
		    		}
		    	});
			}
            
            mainModule.log.info("init() finished");
        });
        
        var helpXmlUrl = "help/"+mainModule.settings.requestedPage+".xml";
        
        var helpXmlPresent = false;
        
        $.ajax({
        	async:false,
            url: helpXmlUrl,
            success: function(data) {
            	helpXmlPresent = true;
            },
            error: function(){
            	// DO NOTHING
            }
        });
        
        if(helpXmlPresent){
        	try {
        		$.jHelp(helpXmlUrl,{"IconURL": "/xapsweb/images/jHelpIcon.gif"});
	    	} catch (e) {
	    		alert(data);
	    		mainModule.log.warn("Could not init help system",e);
	        }
        }
    	
    	/**
    	 * The following two private* methods is used above
    	 * *(they are only accessible within local scope)
    	 * 
    	 * @method configureSearchFields Configure all search fields (context bar, global search etc)
    	 * @method configureContextBar Configure the context bar functionality and css
    	 * 
    	 * They are placed below for the sake of readability.
    	 */ 
    	
    	/**
    	 * Configure the search fields to behave like we want them to.
    	 * Make the text gray when blurred, black when focused, and, if text field is empty, fill in the original value.
    	 * This is how search fields operate all over the web.
    	 * There is a also a listener for the ENTER key, to automatically submit the closest form.
    	 * And a listener for the ESCAPE key, to automatically focus on something else
    	 */
    	var configureSearchFields = function(){
        	$(".searchField").each(function(){
            	var oldValue = $(this).attr("value");
            	$(this).focus(function(){
            	 $(this).css('color','black');
            	 var currentValue = $(this).attr("value");
            	 if(currentValue==oldValue)
            		 $(this).val('');
            	});
            	$(this).blur(function(){
    				 $(this).css('color','gray');
    				 var currentValue = $(this).attr("value");
    				 if(currentValue=="")
    					 $(this).val(oldValue);
    			 });
            });
            
            $(".searchField").css("color","gray");
            
    		$("#searchForm input, #contextsearch").keyup(function(event){
    			 var keyCode = event.which ? event.which : event.keyCode;
    			 if(keyCode == 13 || keyCode == 10){
    				 $(this).closest('input:submit').submit();
    			 } else if(keyCode == 27){
    				 $(this).blur().closest('body').focus();
    			 }
    		 });
        };
        
        /**
         * This method configures the context bar
         * First it configures the arrows in the context items, that displays the context selects.
         * Then it configures contextSelect inputs (context search) to submit automatically on ENTER
         * Functional configuring of the special dropdowns (they are not selects and need some coding to work)
         * Binding on document.click to hide dropdowns
         */
        var configureContextBar = function(){
            $(".contextSwitcher").click(function(){
            	if(!$(this).attr("toggled")){
            		$(document.body).click();
            		$(this).attr("toggled",true);
            		$(this).hide();
            		$(this).parent().find("a").hide();
            		var dropdown = $(this).parent().find(".contextSelect");
            		dropdown.icon = $(this);
            		dropdown.find("dt a").show().click();
            		dropdown.show();
            		dropdown.insertBefore($(this));
            	}else{
            		$(this).removeAttr("toggled");
            		$(this).show();
            		$(this).parent().find("a").show();
            		var dropdown = $(this).parent().find(".contextSelect");
            		dropdown.hide();
            		dropdown.insertAfter($(this));
            	}
            });
            
            $("input.contextSelect").keyup(function(event){
    			 var keyCode = event.which ? event.which : event.keyCode;
    			 if(keyCode == 13 || keyCode == 10){
    				 var mainForm = $(this).closest('form').siblings('div').find('form');
    				 mainForm.append($(this));
    				 mainForm.submit();
    			 } else if(keyCode == 27){
    				 $(this).blur().closest('body').focus();
    			 }
    		 });
            
            $(".dropdown dt a").click(function() {
            	var parent = $(this).parent().parent();
                parent.find("dd ul").toggle();
                if(parent.hasClass("shortcuts")){
                	if(!$(this).attr("toggled")){
                		$(this).attr("toggled",true);
                	}else{
                		$(this).removeAttr("toggled");
                	}
                }
            });
                        
            $(".contextSelect dd ul li a").click(function() {
                var text = $(this).html();
                var dropdown = $(this).parent().parent().parent().parent();
                dropdown.find("dd ul").hide();
                var inputToUpdate = dropdown.parent().find("input:hidden");
                inputToUpdate.val(text);
                var mainForm = $(this).closest('form').siblings('div').find('form');
                mainForm.append(inputToUpdate);
                mainForm.submit();
            });

            $(document).bind('click', function(e) {
                var $clicked = $(e.target);
                if (! $clicked.hasClass("contextSelect") && ! $clicked.parents().hasClass("contextSelect") && ! $clicked.siblings().hasClass("contextSelect")){
                	$(".contextSelect").each(function(index){
                   		 $(this).siblings("img:first").each(function(){
                   			if($(this).attr("toggled")){
                   				$(this).click();
                   			} 
                   		 });
                   	});
                }
                
                if(!$clicked.parents().hasClass("shortcuts")){
                    $(".shortcuts").each(function(index){
                    	var parent = $(this);
                    	var link = $(this).find("dt a");
                		 if(link.attr("toggled")){
             				link.click();
             			 } 
                	 });
                }
            });
        };
    };
    
    /**
     * The xAPS logger map
     * 
     * Can be accessed from a sub module with 
     * <code>$super.log.error("ah something occured",exception);</code>
     * or directly on the xAPS object with 
     * <code>xAPS.log.error("ahh",exception)</code>
     * 
     * Only logs if console (Firebug console) is available to the browser
     */
    mainModule.log = function(){
    	return {
			info: function(msg,e){
		    	if(CONSOLE_AVAILABLE){
		    		if(isDefined(e))
		    			console.info(msg,e);
		    		else
		    			console.info(msg);
		    	}
			},
			error: function(msg,e){
				if(CONSOLE_AVAILABLE){
					if(isDefined(e))
						console.error(msg,e);
					else
						console.error(msg);
		    	}
			},
			warn: function(msg,e){
				if(CONSOLE_AVAILABLE){
					if(isDefined(e))
						console.warn(msg,e);
					else
						console.warn(msg);
		    	}
			},
			debug: function(msg,e){
				if(CONSOLE_AVAILABLE){
					if(isDefined(e))
						console.debug(msg,e);
					else
						console.debug(msg);
		    	}
			}
	    };
	    
        /**
         * Actually the same as calling isDefined(console)
         * Checks if the firebug console is available
         */
        var CONSOLE_AVAILABLE = typeof console !== "undefined";
        
        /**
         * Check if a variable is defined (not undefined)
         */
        var isDefined = function(e){
        	return typeof e !== "undefined";
        };
    }();

    /**
	 * The default settings for the xAPS main module and all sub modules.
	 */
    mainModule.settings = {
        defaultFormId : "form[name='form1']",
        session : {
            timeout : 30
        },
        debug: false,
        confirmchanges: true,
        titlePopupClass: ".requiresTitlePopup,.tiptip",
        pleaseWait : {
            message : "Please wait ...",
            border : "none",
            borderRadius : "10px",
            backgroundColor : "#000",
            fontColor : "#fff",
            padding : "15px",
            opacity : 0.5
        },
        calendar : {
            dateFormat : "%Y-%m-%d %H:%M",
            dateFormatNoTime: "%Y-%m-%d",
            singleClick : false,
            start : {
                input : "start",
                input_original : "start_original",
                button : "start_img"
            },
            end : {
                input : "end",
                input_original : "end_original",
                button : "end_img"
            },
            onClose : function() {
                this.hide();
            }
        }
    };
    
    
    /**
     * UTILITY METHODS
     * 
     * The following section of methods is helper methods auugmented to the xAPS object.
     * 
     * Some methods are private, eg defined as vars, because they do not represent a complete action, and therefore shouldn't be exposed.
     * 
     * Edit with care, since this can effect all pages.
     */
    
	mainModule.setupTablesorter = function(selector){
		$(selector).tablesorter();
	};
	
	mainModule.isNaN = function(toCheck) {
		return typeof toCheck === "undefined" || toCheck === null || toCheck === "";
	};

	mainModule.goTo = function(toUrl) {
		window.location = toUrl;
	};

	mainModule.parseDate = function(string, format) {
		format = mainModule.isNaN(format) ? mainModule.settings.calendar.dateFormat : format;
		return Date.parseDate(string, format);
	};

	mainModule.makeUrl = function(page, params, async) {
		async = mainModule.isNaN(async) ? false : async;
		params = mainModule.isNaN(params) ? "" : params;
		if (mainModule.isNaN(page)) {
			throw "makeUrl => Page variable is required but undefined.";
		}
		return [ "?page=" + page, "async=" + async, params ].join("&");
	};

	/**
	 * The default form submit function, used to submit a custom or default form.
	 * 
	 * @param formId A valid form Id or undefined (if undefined, it uses the defaultFormId)
	 */
	mainModule.submitForm = function(formId) {
		formId = (typeof formId === "undefined" ? mainModule.settings.defaultFormId : formId);
		$(formId).submit();
	};

     /**
	 * Function for checking if a date selector has changed.
	 */
	var _hasDateChanged = function(inputType) {
		return $("#" + inputType.input).val() !== $("#" + inputType.input_original).val();
	};

	/**
	 * Default calendar settings that relies on the type and callback function supplied
	 */
	var _defaultCalendarConfig = function(type, callbackOnChange) {
		return {
			field : type.input,
			button : type.button,
			onClose : function() {
				if (_hasDateChanged(type)) {
					if (typeof callbackOnChange === 'function') {
						callbackOnChange();
					}
				}
				return this.hide();
			},
			showsTime: true
		};
	};

	/**
	 * Function for setting up the calendar
	 */
	var _setupCalendar = function(config) {
		config = $.extend({},mainModule.settings.calendar, config);
		Calendar.setup({
			inputField : config.field,
			ifFormat : config.dateFormat,
			button : config.button,
			singleClick : config.singleClick,
			onClose : config.onClose,
			showsTime: config.showsTime
		});
	};
	
	/**
	 * Setup the "from" calendar selector
	 */
	mainModule.setupFromCalendar = function(callbackOnChange, config) {
		_setupCalendar($.extend(_defaultCalendarConfig(mainModule.settings.calendar.start,callbackOnChange), config));
	};

	/**
	 * Setup the "to" calendar selector
	 */
	mainModule.setupToCalendar = function(callbackOnChange, config) {
		_setupCalendar($.extend(_defaultCalendarConfig(mainModule.settings.calendar.end,callbackOnChange), config));
	};
	
	/**
	 * Blocking the screen and displaying a "Please wait ... " dialog.
	 */
	mainModule.pleaseWait = function(messageToDisplay) {
		messageToDisplay = (typeof messageToDisplay === "undefined" ? mainModule.settings.pleaseWait.message : messageToDisplay);
		$.blockUI({
			css : {
				border : mainModule.settings.pleaseWait.order,
				padding : mainModule.settings.pleaseWait.padding,
				backgroundColor : mainModule.settings.pleaseWait.backgroundColor,
				color : mainModule.settings.pleaseWait.fontColor,
				'-webkit-border-radius' : mainModule.settings.pleaseWait.borderRadius,
				'-moz-border-radius' : mainModule.settings.pleaseWait.borderRadius,
				opacity : mainModule.settings.pleaseWait.opacity
			},
			message : "<h1>" + messageToDisplay + "</h1>"
		});
	};

    return mainModule;
}(ACS || {}, jQuery, this)); // By referencing the window object with this we are absolutely sure to get the right object