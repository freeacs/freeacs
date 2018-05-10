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

function hideAllBut(current){
	var links = document.getElementsByName("messagelink");
	for(var link in links){
		if(links[link].innerHTML!=current)
			links[link].innerHTML="Show";
	}
}