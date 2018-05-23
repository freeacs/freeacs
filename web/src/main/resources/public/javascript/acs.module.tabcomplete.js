/*global xAPS */
/**
 * A tabcomplete module that retrieves its complete data from a hidden field on the page.
 * 
 * A page that needs to use this module, must put the complete data, space separated, in a hidden field on the page 
 * (by default with id tabcompleteData).
 * 
 * It can easily be extended to retrieve its data from a url, its just a question of time and energy.
 * 
 * Used by create Group page, in the time rolling settings.
 * 
 * It is much easier to use jQuery UI autocomplete. All search fields in xAPS Web use it.
 * 
 * TEMPLATES
 * /WebContent/templates/group/create.ftl // Executes initCompletion() on the module
 * 
 * @author Jarl Andre Hubenthal
 */
ACS.createModule("tabcomplete", function($this, $super, $, window) {
	/**
	 * The configuration.
	 */
    $this.settings = {
        inputSelector : "#tabcompleteInput",
        dataSelector : "#tabcompleteData",
        dataSeparator : " ",
        characterWidth : 10
    };
    
    /**
     * The constructor.
     */
    $this._initForm = function(){
    	// do nothing
    };

    /**
     * Merge settings and register a keydown handler on the input field
     */
    $this.initCompletion = function(config) {
        config = $.extend({}, $this.settings, config);
        $(config.inputSelector).keydown(
            function(e) {
                if (e.keyCode === 9 && $this.autocomplete(this, $(config.dataSelector).html().split( config.dataSeparator), config)) {
                    e.preventDefault();
                }
            }
        );
    };

    /**
     * The auto complete logic
     */
    $this.autocomplete = function(input, data, config) {
        if (input.value.length === input.selectionStart && input.value.length === input.selectionEnd) {
            var candidates = [];
            // filter data to find only strings that start with existing value
            for ( var i = 0; i < data.length; (i=i+1)) {
                if (data[i].indexOf(input.value) === 0 && data[i].length > input.value.length) {
                    candidates.push(data[i]);
                }
            }

            if (candidates.length > 0) {
                // some candidates for autocompletion are found
                if (candidates.length === 1) {
                    input.value = candidates[0];
                } else {
                    input.value = _longestInCommon(candidates, input.value.length);
                }
                $(input).caret($(input).val().length);
                $(input).width($(input).val().length * config.characterWidth);
                return true;
            }
        }
        return false;
    };

    /**
     * Private utility method
     */
    var _longestInCommon = function(candidates, index) {
        var i, ch, memo;
        do {
            memo = null;
            for (i = 0; i < candidates.length; (i=i+1)) {
                ch = candidates[i].charAt(index);
                if (!ch) {
                    break;
                }
                if (!memo) {
                    memo = ch;
                } else if (ch !== memo) {
                    break;
                }
            }
        } while (i === candidates.length && (index=index+1));
        return candidates[0].slice(0, index);
    };

    /**
     * Extend jQuery with caret functionality
     */
    $.extend($.fn, {
        caret : function(start, end) {
            var elem = this[0], val, range;

            if (elem) {
                // get caret range
                if (typeof start === "undefined") {
                    if (elem.selectionStart) {
                        start = elem.selectionStart;
                        end = elem.selectionEnd;
                    } else if (window.document.selection) {
                        val = this.val();
                        range = window.document.selection.createRange()
                        .duplicate();
                        range.moveEnd("character", val.length);
                        start = (range.text === "" ? val.length : val.lastIndexOf(range.text));
                        range = window.document.selection.createRange().duplicate();
                        range.moveStart("character", -val.length);
                        end = range.text.length;
                    }
                } else {
                    val = this.val();

                    if (typeof start !== "number"){
                        start = -1;
                    }
                    if (typeof end !== "number"){
                        end = -1;
                    }
                    if (start < 0){
                        start = 0;
                    }
                    if (end > val.length){
                        end = val.length;
                    }
                    if (end < start){
                        end = start;
                    }
                    if (start > end){
                        start = end;
                    }
                    
                    elem.focus();

                    if (elem.selectionStart) {
                        elem.selectionStart = start;
                        elem.selectionEnd = end;
                    } else if (window.document.selection) {
                        range = elem.createTextRange();
                        range.collapse(true);
                        range.moveStart("character", start);
                        range.moveEnd("character", end - start);
                        range.select();
                    }
                }
                return {
                    start : start,
                    end : end
                };
            }
            return {};
        }
    });
});