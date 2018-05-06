<script src="javascript/jquery-1.4.4.js"></script>
<script>
	var url = "${URL_MAP.REALTIMEMOS}&reload=true&async=true<#if unit??>&unit=${unit.id}</#if><#if profile??>&profile=${profile.name}</#if><#if unittype??>&unittype=${unittype.name}</#if><#if start??>&start=${start}</#if><#if end??>&end=${end}</#if>&channel=${line!}";
	var imageUrl = url+"&display-chart=true";
	jQuery(document).ready(function() {
		$.ajax({
	         url:   imageUrl,
	         success: function(result) {
	         	$("#mosavgchart").html(result);
	         	<#if active>
	         	setTimeout("window.location='"+url+"';",5000);
	         	</#if>
	         },
	         error: function(result) {
	         	alert("Failed to load image: "+result);
	         },
	         async: false
	    });
    });
</script>
<div id="mosavgchart"><img src="images/spinner.gif" alt="Mos average graph" /></div>
