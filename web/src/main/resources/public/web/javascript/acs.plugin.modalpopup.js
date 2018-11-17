/**
 * XAPS JAVASCRIPT MODAL POPUP PLUGIN
 * 
 * SUBMODAL v1.6 Used for displaying DHTML only popups 
 * instead of using buggy modal windows.
 * 
 * By Subimage LLC http://www.subimage.com
 * 
 * Contributions by: Eric Angel - tab index code Scott - hiding/showing selects
 * for IE users Todd Huss - inserting modal dynamically and anchor classes
 * 
 * Up to date code can be found at http://submodal.googlecode.com
 * 
 * Modified by Jarl Andr� H�benthal
 * 
 * @deprecated Should be discarded. Use xaps.plugin.iframedialogs.js instead.
 * 
 * @author Jarl Andre Hubenthal
 */
var MODALPOPUP = (function($this,$,window){
    var tabIndexes = [],
    tabTags = ["A","BUTTON","TEXTAREA","INPUT","IFRAME"],
    gPopupMask,
    gPopupContainer,
    gPopFrame,
    gHideSelects,
    gReturnVal,
    popHeight,
    popWidth,
    isVisible = false;
    
    $this.gReturnFunc = null;

    $this.settings = {
        html :
        "<div id='popupMask' />" +
	    '<div id="popupContainer">'+
	    '<div id="popupInner">' +
	    '<div id="popupTitleBar">' +
	    '<div id="popupTitle"></div>' +
	    '<div id="popupControls">' +
	    '<img src="images/close.gif" onclick="MODALPOPUP.hidePopWin(true);" id="popCloseBox" />' +
	    '</div>' +
	    '</div>' +
	    '<iframe src="loading.html" style="width:100%;height:100%;background-color:transparent;" scrolling="auto" frameborder="0" allowtransparency="true" id="popupFrame" name="popupFrame" width="100%" height="100%"></iframe>' +
	    '</div>'+
	    '</div>'
    };

    // For IE.  Go through predefined tags and disable tabbing into them.
    function disableTabIndexes() {
        if (document.all) {
            var i = 0;
            for (var j = 0; j < tabTags.length; j+=1) {
                var tagElements = document.getElementsByTagName(tabTags[j]);
                for (var k = 0 ; k < tagElements.length; k+=1) {
                    tabIndexes[i] = tagElements[k].tabIndex;
                    tagElements[k].tabIndex="-1";
                    i+=1;
                }
            }
        }
    }

    // For IE. Restore tab-indexes.
    function restoreTabIndexes() {
        if (document.all) {
            var i = 0;
            for (var j = 0; j < tabTags.length; j+=1) {
                var tagElements = document.getElementsByTagName(tabTags[j]);
                for (var k = 0 ; k < tagElements.length; k+=1) {
                    tagElements[k].tabIndex = tabIndexes[i];
                    tagElements[k].tabEnabled = true;
                    i+=1;
                }
            }
        }
    }

    var list = [];

    /**
     * Hides all drop down form select boxes on the screen so they do not appear
     * above the mask layer. IE has a problem with wanted select form tags to always
     * be the topmost z-index or layer
     *
     * Thanks for the code Scott!
     */
    function hideSelectBoxes() {
        var x = document.getElementsByTagName("SELECT");

        for (var i=0;x && i < x.length; i+=1) {
            if(!VisibilityHiddenSet(x[i])){
                list[x[i].id+"-"+x[i].name]="hidden";
                x[i].style.visibility = "hidden";
            }
        }
    }

    function VisibilityHiddenSet(element){
        while(element.parentNode){
            if(element.style.visibility==="hidden"){
                return true;
            }
            element=element.parentNode;
        }
        return false;
    }

    /**
     * Makes all drop down form select boxes on the screen visible so they
     * reappear after the dialog is closed.
     *
     * IE has a problem with wanting select form tags to always be the
     * topmost z-index or layer.
     */
    function displaySelectBoxes() {
        var x = document.getElementsByTagName("SELECT");

        for (var i=0;x && i < x.length; i+=1){
            if(list[x[i].id+"-"+x[i].name]==="hidden"){
                x[i].style.visibility = "visible";
            }
        }
    }

    function getViewportHeight() {
        if (window.innerHeight!==undefined){
                return window.innerHeight;
        }
        if (document.compatMode==='CSS1Compat'){
            return document.documentElement.clientHeight;
        }
        if (document.body){
            return document.body.clientHeight;
        }
        return undefined;
    }
    function getViewportWidth() {
        if (window.innerWidth!==undefined){
            return window.innerWidth;
        }
        if (document.compatMode==='CSS1Compat'){
            return document.documentElement.clientWidth;
        }
        if (document.body){
            return document.body.clientWidth;
        }
        return undefined;
    }

    /**
     * Gets the real scroll top
     */
    function getScrollTop() {
        if (window.self.pageYOffset) // all except Explorer
        {
            return window.self.pageYOffset;
        }
        else if (document.documentElement && document.documentElement.scrollTop)
        // Explorer 6 Strict
        {
            return document.documentElement.scrollTop;
        }
        else if (document.body) // all other Explorers
        {
            return document.body.scrollTop;
        }else{
            return null;
        }
    }
    
    function getScrollLeft() {
        if (window.self.pageXOffset) // all except Explorer
        {
            return window.self.pageXOffset;
        }
        else if (document.documentElement && document.documentElement.scrollLeft)
        // Explorer 6 Strict
        {
            return document.documentElement.scrollLeft;
        }
        else if (document.body) // all other Explorers
        {
            return document.body.scrollLeft;
        }else{
            return null;
        }
    }
    
    function reloadWindowLocation(){
    	window.location.reload();
    }

    $this.addContent = function( content ) {
        return $(content).appendTo("body");
    };
    
    $this.init = function(){
        $(window.document).keypress(function(e){
            if(isVisible && e.keyCode === 9)
                return false;
            return true;
        });
        
        $(window.document).ready(function($){
            $this.addContent($($this.settings.html));
            gPopupMask = document.getElementById("popupMask");
            gPopupContainer = document.getElementById("popupContainer");
            gPopFrame = document.getElementById("popupFrame");
            $("a[class^='submodal']").each(function(){
                $(this).click(function(){
                    var width = 400;
                    var height = 200;
                    var params = this.className.split('-');
                    if (params.length === 3) {
                        width = parseInt(params[1]);
                        height = parseInt(params[2]);
                    }
                    $this.showPopWin($(this).html(),this.href,width,height,reloadWindowLocation,true);
                    return false;
                });
            });
            var brsVersion = parseInt(window.navigator.appVersion.charAt(0), 10);
            if (brsVersion <= 6 && window.navigator.userAgent.indexOf("MSIE") > -1) {
                gHideSelects = true;
            }
        });
    };

    $this.showPopWin = function(title,url, width, height, returnFunc, showCloseBox) {
        $("#popupTitle").html(title);
	
        if (showCloseBox) {
            $("#popCloseBox").show();
        } else {
            $("#popCloseBox").hide();
        }
	        
        isVisible = true;
        disableTabIndexes();
        gPopupMask.style.display = "block";
        gPopupContainer.style.display = "block";
        // calculate where to place the window on screen
        $this.centerPopWin(width, height);
	
        var titleBarHeight = parseInt(document.getElementById("popupTitleBar").offsetHeight, 10);
	
	
        gPopupContainer.style.width = width + "px";
        gPopupContainer.style.height = (height+titleBarHeight) + "px";
	
        $this.setMaskSize();
	
        // need to set the width of the iframe to the title bar width because of the
        // dropshadow
        // some oddness was occuring and causing the frame to poke outside the
        // border in IE6
        gPopFrame.style.width = parseInt(document.getElementById("popupTitleBar").offsetWidth, 10) + "px";
        gPopFrame.style.height = (height) + "px";
	
        // set the url
        gPopFrame.src = window.location.protocol+"//"+window.location.hostname+":"+window.location.port+window.location.pathname+url.substring(url.indexOf("?"))+"&timestamp="+new Date().getMilliseconds();
        $this.gReturnFunc = returnFunc;
	
        // for IE
        if (gHideSelects == true) {
            hideSelectBoxes();
        }
    };
	
    $this.centerPopWin = function(width, height) {
        if (isVisible == true) {
            if (width == null || isNaN(width)) {
                width = gPopupContainer.offsetWidth;
            }
            if (height == null) {
                height = gPopupContainer.offsetHeight;
            }
	
            //var theBody = document.documentElement;
            var theBody = window.document.getElementsByTagName("BODY")[0];
            // theBody.style.overflow = "hidden";
            var scTop = parseInt(getScrollTop(),10);
            var scLeft = parseInt(theBody.scrollLeft,10);
	
            $this.setMaskSize();
	
            // window.status = gPopupMask.style.top + " " + gPopupMask.style.left +
            // " " + gi++;
	
            var titleBarHeight = parseInt(window.document.getElementById("popupTitleBar").offsetHeight, 10);
	
            var fullHeight = getViewportHeight();
            var fullWidth = getViewportWidth();
	
            gPopupContainer.style.top = (scTop + ((fullHeight - (height+titleBarHeight)) / 2)) + "px";
            gPopupContainer.style.left =  (scLeft + ((fullWidth - width) / 2)) + "px";
        }
    };

    $this.setMaskSize = function() {
        var theBody = window.document.getElementsByTagName("BODY")[0];
	
        var fullHeight = getViewportHeight();
        var fullWidth = getViewportWidth();
	
        // Determine what's bigger, scrollHeight or fullHeight / width
        if (fullHeight > theBody.scrollHeight) {
            popHeight = fullHeight;
        } else {
            popHeight = theBody.scrollHeight;
        }
	
        if (fullWidth > theBody.scrollWidth) {
            popWidth = fullWidth;
        } else {
            popWidth = theBody.scrollWidth;
        }
	
        gPopupMask.style.height = popHeight + "px";
        gPopupMask.style.width = popWidth + "px";
    };

    $this.hidePopWin = function(callReturnFunc) {
        try{
            isVisible = false;
            var theBody = document.getElementsByTagName("BODY")[0];
            theBody.style.overflow = "";
            restoreTabIndexes();
            if (gPopupMask == null) {
                return;
            }
            gPopupMask.style.display = "none";
            gPopupContainer.style.display = "none";
	
            var popupFrame = window.frames["popupFrame"];
            
            popupFrame.document.location.href="loading.html";
            
            if (callReturnFunc == true && $this.gReturnFunc != null) {
                var popupDoc = popupFrame.document;
                gReturnVal = popupDoc.getElementById("param.name");
                if(gReturnVal!=null && gReturnVal!=undefined){
                    gReturnVal = gReturnVal.value;
                    setTimeout(function(){
                        $this.gReturnFunc(gReturnVal);
                    },1);
                }else{
                    setTimeout(function(){
                        $this.gReturnFunc(false);
                    },1);
                }
            }
	
            if (gHideSelects == true) {
                displaySelectBoxes();
            }
        }catch(e){
            alert("Problem while hiding popup window: "+e);
        }
    };

    return $this;
}(MODALPOPUP || {},jQuery,window));

/**
 * X-browser event handler attachment and detachment
 * TH: Switched first true to false per http://www.onlinetools.org/articles/unobtrusivejavascript/chapter4.html
 *
 * @argument obj - the object to attach event to
 * @argument evType - name of the event - DONT ADD "on", pass only "mouseover", etc
 * @argument fn - function to call
 */
function addEvent(obj, evType, fn){
    if (obj.addEventListener){
        obj.addEventListener(evType, fn, false);
        return true;
    } else if (obj.attachEvent){
        var r = obj.attachEvent("on"+evType, fn);
        return r;
    } else {
        return false;
    }
}

function removeEvent(obj, evType, fn, useCapture){
    if (obj.removeEventListener){
        obj.removeEventListener(evType, fn, useCapture);
        return true;
    } else if (obj.detachEvent){
        var r = obj.detachEvent("on"+evType, fn);
        return r;
    } else {
        return false;
    }
}

addEvent(window, "load", function(){
    MODALPOPUP.init();
    addEvent(window, "resize", function(){
        MODALPOPUP.centerPopWin();
    });
    addEvent(window, "scroll", function(){
        MODALPOPUP.centerPopWin();
    });
    window.onscroll = function(){
        MODALPOPUP.centerPopWin();
    };
});

function returnRefresh(returnVal) {
    try{
        if(returnVal == false || returnVal == undefined){
            window.location=window.location.href;
            return;
        }else if(returnVal!=null && ModalUTP.ELEMENT_TO_UPDATE!=null){
            ModalUTP.ELEMENT_TO_UPDATE["name"]=returnVal;
        }
		
        if(ModalUTP.ELEMENT_TO_UPDATE==null || ModalUTP.ELEMENT_TO_UPDATE["name"]==null){
            window.location=window.location.href;
        }else{
            var name = ModalUTP.ELEMENT_TO_UPDATE["name"];
            var element = document.getElementsByName("update::"+name)[0];
            if(element==null){
                window.location.href="?page=unittype";
                return;
            }
            var profile = (ModalUTP.ELEMENT_TO_UPDATE["profile"]?"&profile="+encodeURIComponent(ModalUTP.ELEMENT_TO_UPDATE["profile"]):"");
            var unittype = (ModalUTP.ELEMENT_TO_UPDATE["unittype"]?"&unittype="+encodeURIComponent(ModalUTP.ELEMENT_TO_UPDATE["unittype"]):"");
            var url = "?page=getvalue&type="+ModalUTP.ELEMENT_TO_UPDATE["type"]+"&name="+name+unittype+profile+"&timestamp="+new Date().getMilliseconds();
            $.getJSON(url,function(data){
                if(data.flags && (typeof data.hasValues === "boolean")){
                    element.value=data.flags;
                    if($("#lock::"+name)){
                        if(data.hasValues){
                            $("#lock::"+name).show();
                        }else{
                            $("#lock::"+name).hide();
                        }
                    }
                }
            });
        }
    }catch(e){
        if(typeof console !== "undefined"){
            console.log(e);
        }
    }finally{
        ModalUTP.ELEMENT_TO_UPDATE=null;
    }
}

/**
 * Helper method for displaying the modal window on Unit Type page
 * 
 * @param element The parameter name
 * @param type Not used. Telling what type of callback object it is.
 * @param unittype The Unit Type name.
 * @param title The title of the modal window
 * @param url The url to load
 * @param width The width of the modal window
 * @param height The height of the modal window
 * 
 * @return Will always return false, to cancel a click on a link etc.
 */
/*global showModal */
var ModalUTP = function(){
    this.ELEMENT_TO_UPDATE = null;
	
    this.show = function(element,type,unittype,title,url,width,height){
        this.ELEMENT_TO_UPDATE = [];
        this.ELEMENT_TO_UPDATE.name=element;
        this.ELEMENT_TO_UPDATE.type=type;
        this.ELEMENT_TO_UPDATE.unittype=unittype;
        return showModal(title,url,width,height);
    };
	
    return this;
};

ModalUTP = new ModalUTP();

/**
 * Calls showPopWin
 * 
 * @param title
 * @param url
 * @param width
 * @param height
 * @return
 */
function showModal(title,url,width,height){
    MODALPOPUP.showPopWin(title,url+'&async=true&header=true', width, height, returnRefresh,true);
    return false;
}