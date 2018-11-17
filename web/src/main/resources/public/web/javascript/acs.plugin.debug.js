/*global window,jQuery*/
/**
 * XAPS JAVASCRIPT DEBUG PLUGIN
 * 
 * This "sexy" little snippet cleanly overrides the default window.onerror
 * so that it can display the errors in a div on the middle of the page,
 * while at the same time allowing the error to be passed on to the original error handler.
 * 
 * Uses the proxy pattern for error handling.
 * 
 * @author Jarl Andre Hubenthal
 */
(function(window,$){
   $.fn.center = function () {
        this.css("position","absolute");
        this.css("top", ( $(window).height() - this.height() ) / 2+$(window).scrollTop() + "px");
        this.css("left", ( $(window).width() - this.width() ) / 2+$(window).scrollLeft() + "px");
        return this;
   };
   $(window.document).ready(function($){
   		$("#JavaScriptsErrorsDiv").width(600).height(500).center().css("backgroundColor","white").css("border","1px solid black").css("padding",20).hide();
   });
   var originalOnError = window.onerror;
   window.onerror = function(msg, url, linenumber){
      $(document).ready(function($){
        $("#JavaScriptsErrorsDiv").append("Error message: "+msg+"<br />URL: "+url+"<br />Line Number: "+linenumber+"<br />").show();
      });
      if(originalOnError){
         var args = []; // empty array
         // copy all other arguments we want to "pass through"
         for(var i = 2; i < arguments.length; i+=1)
         {
             args.push(arguments[i]);
         }
         originalOnError.apply(window, args);
      }
   };
}(window,jQuery));