/*global jQuery,window,xAPS */
/**
 * XAPS JAVASCRIPT PARAMETER COOKIE COLLAPSER
 * 
 * The following CookieCollapser object is used on the Unit Type page
 * 
 * When a tree is collapsed/expanded, CookieUpdate.update() is called, 
 * and it updates a cookie for that parameter or parent name.
 * 
 * When page is loaded, the init() function in xapsweb.js calls CookieCollapse.execute()
 * and it recreates the state of the parameter list from the cookies.
 * 
 * @auhor Jarl Andre Hubenthal
 */
var CookieCollapser = (function(config,$){
	config = $.extend({timeout: 30},config);
	
	var timeout = config.timeout * 60 * 1000;
	
	var execute = function(){
		var cookies = document.cookie.split(';'), name, arr, img, tr,i;

		for(i=0;i<cookies.length;i+=1){
			arr = cookies[i].split("=");
			if(arr.length!==2 || arr[0].indexOf("collapser:")<0){
				continue;
			}
			name = arr[0].substring(11);
			tr = document.getElementById(name);
			if(tr===null){
				continue;
			}
			img = tr.getElementsByTagName("IMG")[0];
			if(img===null){
				continue;
			}
			if(arr[1]==="plus"){
				img.onclick();
			}
		}
	};
	
	var update = function(name){
		var expdate = new Date(),tr, img;
		
		expdate.setTime(expdate.getTime() + this.timeout);
		
		tr = document.getElementById(name);
		img = tr.getElementsByTagName("IMG")[0];
		
		if (img===null){
			return false;
		}
		
		if (img.src.indexOf("minus")>-1){
			setCookie("collapser:"+name,"minus",expdate);
		}else if (img.src.indexOf("plus")>-1){
			setCookie("collapser:"+name,"plus",expdate);
		}
		
		return true;
	};
	
	var setCookie = function(name, value, expires){
		if (!expires){
			expires = new Date(); 
		}
		document.cookie = name + "=" + window.escape(value) + "; expires=" + expires.toGMTString() + "; path=/";
	};
	
	return {
		update: update,
		execute: execute
	};
}({
    timeout: ACS.settings.session.timeout
},jQuery));