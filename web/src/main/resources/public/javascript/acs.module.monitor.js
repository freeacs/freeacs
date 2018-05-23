/**
 * XAPS MONITOR SERVER PAGE JAVASCRIPT
 * 
 * Two methods that is used by the monitor server html.
 * 
 * Since the monitor server html is loaded and displayed directly in xAPS Web
 * these javascript methods must also lie here.
 * 
 * Does not rely on anything else than core javascript.
 * 
 * Should either way be converted to a module ... :)
 * 
 * TEMPLATES
 * /WebContent/modal.ftl // the xAPS Monitor Server's GET response is wrapped in this template
 * 
 * @author Jarl Andre Hubenthal
 */

/**
 * Shows the error for a given system id
 * 
 * @param id the system id
 */
function showError(id){
	try{
		var currentLink = document.getElementById(id+"message").innerHTML;
		hideAllBut(currentLink);
		var error = document.getElementById("error");
		if(currentLink=="Show"){
			var msg = document.getElementById(id);
			error.innerHTML=msg.id+" last event message (<a href=\"#statusHeader\">back to top</a>):<br />"+msg.value;
			document.getElementById(id+"message").innerHTML="Hide";
		}else if(currentLink=="Hide"){
			error.innerHTML="";
			document.getElementById(id+"message").innerHTML="Show";
		}
	}catch(e){
		alert(e);
	}
}

/**
 * Hides all errors but the current id
 * 
 * @param current the current id
 */
function hideAllBut(current){
	var links = document.getElementsByName("messagelink");
	for(var link in links){
		if(links[link].innerHTML!=current)
			links[link].innerHTML="Show";
	}
}