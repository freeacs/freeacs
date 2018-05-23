<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
	<head>
		<title>FreeACS Web | Popup</title>
		<#include "/meta/basic.ftl">
		<script src="javascript/jquery-1.4.4.js">jQuery.noConflict();</script>
		<script src="javascript/jquery-ui-1.8.7.custom.min.js" type="text/javascript"></script>
		<#include "/meta/tablesorter.ftl" />
		<script src="javascript/jquery.jqote.js" type="text/javascript"></script>
		<script src="javascript/acs.js"></script>
		<script src="javascript/acs.legacy.js"></script>
		<script type="text/javascript">
			ACS.initModule({});
		</script>
		<#include "/meta/jhelp.ftl">
	</head>
	<body>
		<div id="bodycontent">
			${content}
		</div>
	</body>
</html>