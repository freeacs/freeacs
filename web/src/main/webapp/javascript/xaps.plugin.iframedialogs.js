/**
 * XAPS JAVASCRIPT IFRAME DIALOGS PLUGIN
 * 
 * Used to display jQuery dialogs with an IFrame
 * that automatically adjusts its width and height according to the dialog size.
 * Easily enables showing a different view on the same page without moving away.
 * A trick for returning a value to the parent view 
 * is to save the return value in session or cookie.
 * But this trick has never been used in xAPS Web and is only mentioned 
 * for the sake of completeness.
 * 
 * The interesting part of this script, is the last part, the return command,
 * that eventually returns an object with six methods.
 * 
 * @author Jarl Andre Hubenthal
 */
var IFRAMEDIALOGS = (function($){
	function showDialog(dialogTitle,url,dialogWidth,dialogHeight,dialogPosition,openAtOnce){
		return $('<div></div>')
			.html('<iframe id="'+dialogTitle+'" style="width:100%;height:100%;border:0px" src="'+url+'" />')
			.dialog({
				autoOpen: openAtOnce,
				title: dialogTitle,
				position:dialogPosition,
				height:dialogHeight,
				width:dialogWidth,
				autoResize: true,
				resizable: true,
				close: function(){ getResponse("help?cmd=setactivedialog");getResponse("help?cmd=setactivetool");dialogs[dialogTitle]=null; },
				open: function(){ $(dialogTitle).src=url; }
			}).width(dialogWidth - 30).height(dialogHeight - 30);
	}

	var dialogs = new Object();

	function getResponse(url){
		var toReturn = null;
		$(document).ready(function($){
			$.ajax({
		         url:     url,
		         success: function(data) {
							toReturn=data;
		                  },
		         async:   false
		    });
		});
		return toReturn;
	}

	function reloadHelpDialog(title){
		showHelpDialog(title,"help?page="+title);
	}

	function showHelpDialog(dialogTitle,url){
		if(dialogs[dialogTitle]==null){
			var $dialog = showDialog(dialogTitle,url,600,500,['right','top'],true);
			dialogs[dialogTitle]=$dialog;
			getResponse("help?cmd=setactivedialog&title="+dialogTitle);
		}else{
			dialogs[dialogTitle].dialog("open");
		}
	}

	function showToolDialog(dialogTitle,url){
		showToolDialogWithDimensions(dialogTitle,url,1024,400);
	}

	function showToolDialogWithDimensions(dialogTitle,url,width,height){
		if(dialogs[dialogTitle]==null){
			var $dialog = showDialog(dialogTitle,url,width,height,'center',true);
			dialogs[dialogTitle]=$dialog;
			getResponse("help?cmd=setactivetool&title="+dialogTitle);
		}else{
			dialogs[dialogTitle].dialog("open");
		}
	}
	
	return {
		init : function(){
			$(document).ready(function($){
				$.ajax({
					 async:   true,
			         url:     "help?cmd=getactivedialog",
			         success: function(data) {
						if(data){
							reloadHelpDialog(data);
						}
			         }
			    });
			});
		},
		dialogs: dialogs,
		showDialog: showDialog,
		showHelpDialog: showHelpDialog,
		showToolDialog: showToolDialog,
		showToolDialogWithDimensions: showToolDialogWithDimensions
	};
}(jQuery));