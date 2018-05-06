<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
	<head>
		<title>Help</title>
		<meta http-equiv="content-type" content="text/html; charset=UTF-8"/>
		<!--link rel="shortcut icon" href="images/fusion.ico" type="image/x-icon" /-->
		<link rel="stylesheet" type="text/css" href="css/xapsweb.css" />
		<link rel="stylesheet" type="text/css" href="css/colors/default.css" />
		<script>
			var HELP = (function(){
				var appendToTitle = function(extraTitle){
					document.title=(document.title+" | "+extraTitle);
				};
				return {
					addTitle: appendToTitle
				};
			}());
		</script>
		<style>
			// Style here
		</style>
	</head>
	<body id="helpcontents" onload="HELP.addTitle('${title}')">
		<form class="unit">
			<fieldset>
				<legend>${title}</legend>
				<#if content??>
					${content}
				<#else>
					No help text available
				</#if>
			</fieldset>
		</form>
		<!--<p>Successfully generated at <b>${time?string("yyyy-MM-dd HH:mm")}</b></p>-->
	</body>
</html>