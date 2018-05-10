/* *****************************************************
    jHelp - jQuery Plug-in
    Copyright Serexx Ltd. 2010
    Created by Greg Burman
    Licensing - you are licensed to use and distrubute this software as you wish provided that all
        attributions and notices in this and all acompanying files remain in place and un-altered.
    No Warranty - this software is not warranted to suitable fit or merchantable for any purpose whatsoever.
        It is provided to you on as-is where-is basis.
**********************************************************/
(function ($) {  //closure for privacy
	jQuery.fn.exists = function(){return this.length>0;};

    $.jHelp = function (definitionUrl, options) {
    	
        var settings = $.extend({}, $.jHelp.defaults, options);     // extend with defaults and any override values
        settings.PanelTemplate = settings.PanelTemplate.replace("{PanelHeading}", settings.PanelHeading);
        $.jHelp.settings = settings;    // available publicly for jHelp event handlers
        document.write(settings.PanelTemplate);

        var standarSelectorTemplate = 
        $(document).ready(function ($) {
            $('#jHelp').draggable();

            //setup current values in public object
            $.jHelp.publicMethods.HelpDiv = $("#jHelp");
            $.jHelp.publicMethods.ContentDiv = $("#jHelpContent");
            $.jHelp.publicMethods.IconURL = $.jHelp.settings.IconURL;
            var current = $.jHelp.publicMethods;  //having a local copy is faster here, but we need global as well

            function setOnFocusAndOnBlur(selector){
                $(selector).focus(function () { 
                    $.jHelp.publicMethods.ToggleFieldStatus(true, this);
                }).blur(function () {
                    $.jHelp.publicMethods.ToggleFieldStatus(false, this);
                });
            }
            
            //Trap F1 key in IE if specified
            if (settings.TrapF1) {
                document.onhelp = function () {
                    event.cancelBubble = true;
                    event.returnValue = false;
                    $.jHelp.publicMethods.OnIndexClick(event.srcElement.Id);
                };
            };

            //read help definitions for me
            var me = document.location.pathname;
            //me = me.substr(me.lastIndexOf('#'));   //in case of anchor in URL
            $.ajax({
                type: "GET",
                url: definitionUrl,
                dataType: ($.browser.msie) ? "xml" : "text/xml",
                async: false,
                error: function (XMLHttpRequest, textStatus, errorThrown) {
                    if(typeof console !== "undefined"){
                      console.error("Could not retreive help xml",XMLHttpRequest,textStatus, errorThrown);
                    }
                },
                success: function (xml) {
                    var filter = "Page[Path='" + me + "']";
                    var helpIndex = settings.IndexHeadingTemplate;
                    var icon = settings.IconTemplate.replace("{iconURL}", current.IconURL);
                    $(xml).find(filter).each(function () {
                    	setOnFocusAndOnBlur("form input[type='text']:visible,form select");
                        $(this).find("Field").each(function () {
                        	var id = $(this).attr("ID");
                        	var selector = "form a#"+id+', form #'+id+':input:visible, form [name="' + id + '"]:input:visible';
                        	if($(selector).exists()){
	                            var t = $(this).find('Heading').text();
	                            var h = $(this).find('Help').text();
	                            helper[helper.length] = new help(id, t, h);
	                            helpIndex += '<a class="jHelpIndexItem" href="javascript:$.jHelp.publicMethods.OnIndexClick(\'' + id + '\');"><span class="jHelpIndexItem">' + t + '</span></a><br/>';
	                            $(selector).after(icon);
                        	}
                        });
                        setOnFocusAndOnBlur("form tr a");
                        $(this).find("Parameter").each(function () {
                        	var id = $(this).find('Name').text();
                        	var params = "form tr[id='"+id+"'] a";
                        	if($(params).exists()){
	                            var t = $(this).find('Heading').text();
	                            var h = $(this).find('Help').text();
	                            helper[helper.length] = new help(id, t, h);
	                            helpIndex += '<a class="jHelpIndexItem" href="javascript:$.jHelp.publicMethods.OnIndexClick(\'' + id + '\');"><span class="jHelpIndexItem">' + t + '</span></a><br/>';
	                            $(params).attr("name",id);
	                            $(params).after(icon);
                        	}
                        });
                    });
                    current.HelpIndexText = helpIndex;
                }
            });
        });
    };

    //global public value/method repository
    $.jHelp.publicMethods = {
        HelpOn: false,
        CurrentField: null,
        CurrentHelpID: "",
        HelpDiv: null,
        ContentDiv: null,
        HelpIndexText: "",
        IconURL: "",
        CloseHelp: function () { $(this.HelpDiv).css("display", "none"); this.HelpOn = false; },
        OnIconClick: function (e) { this.HelpOn = true; $($(e).prev(':input, a')).focus(); },
        ToggleFieldStatus: function (on, oField) {
            if (on) {
                this.CurrentField = oField; 
                this.CurrentHelpID = oField.id; 
                if(this.CurrentHelpID==="") { 
                	this.CurrentHelpID = $(oField).attr("name");
                }
                if (this.HelpOn) { 
                	showHelp(); 
                }
            }
            else {
                $(oField).removeClass("jHelpFieldActive");
                this.CurrentField = null;
            }
        },
        OnIndexClick: function (helpId) {
            $(this.CurrentField).removeClass("jHelpFieldActive");
            if (helpId === null || helpId === "") {
                this.CurrentField = null; 
                showHelp();
                return;
            }
            var selector = '#'+helpId+', [name="' + helpId + '"], '+"tr[id='"+helpId+"'] a";
            $(selector).focus();
        }
    };


    /* **********************************
    Private vars & methods
    ************************************/
    var settings;
    var helper = [];   //help specs
    //help item constructor
    function help(sFieldID, sDisplayName, sMsg) {
        this.Msg = sMsg;
        this.DisplayName = sDisplayName;
        this.ID = sFieldID;
    };

    //called by OnIndexClick or OnIconClick
    function showHelp() {
        //show help for current field or index
        var text;
        var current = $.jHelp.publicMethods;        //local reference
        if (current.CurrentField === null) {
            text = current.HelpIndexText;
        }
        else {
            //get help text - sorry or the field text
            text = $.jHelp.settings.SorryTemplate + current.HelpIndexText;
            for (var i = 0; i < helper.length; i+=1) {
                if (helper[i].ID === current.CurrentHelpID) {
                    text = "<div class='jHelpItemHeading'>" + helper[i].DisplayName + "</div><div class='jHelpItemText'>" + helper[i].Msg + "</div>";
                    break;
                }
            }
            if($(current.CurrentField).is(":visible"))
            	$(current.CurrentField).addClass("jHelpFieldActive");
        }
        //show it
        $(current.ContentDiv).html(text);
        $(current.HelpDiv).css("display", "inline-block");
        if($(current.CurrentField).is(":visible")){
	        $(current.HelpDiv).position({
	    	  my: "left top",
	    	  at: "left top",
	    	  offset: ($(current.CurrentField).width()+100)+" 0",
	    	  of: $(current.CurrentField)
	    	});
        }
        current.HelpOn = true;
    };

    //Establish Default Values for Settings
    var html = "<div class='jHelpPanel' id='jHelp' style='display:none;' >";
    html += "<div  class='jHelpHeading' id='jHelpHeading'>{PanelHeading}</div><hr class='jHelpPanel'><div id='jHelpContent'></div>";
    html += "<div id='jHelpFooter'><hr class='jHelpPanel'><a class='jHelpPanel' href='javascript:$.jHelp.publicMethods.CloseHelp();'>Close</a>&nbsp;&nbsp;<a class='jHelpPanel' href='javascript:$.jHelp.publicMethods.OnIndexClick(null);'>Index</a></div><br></div>";
    $.jHelp.defaults = {                    //public default values for options
        TrapF1: true,
        IconURL: "Images/jHelpIcon.gif",
        PanelHeading: "Form Help",
        PanelTemplate: html,
        IconTemplate: "&nbsp;&nbsp;<img onClick='javascript:$.jHelp.publicMethods.OnIconClick(this);' style='cursor:pointer;' alt='' class='jHelpIcon' src='{iconURL}' />",
        IndexHeadingTemplate: "<div id='jHelpIndexHeading'>Help Index</div>",
        SorryTemplate: "<span class='jHelpHeading' id='jHelpSorryHeading'>Sorry,</span> <span id='jHelpSorry'>there is no help text defined for this item.</span><hr/>"
    };
}(jQuery));