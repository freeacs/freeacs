<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
	<head>
		<title>FreeACS Web | error</title>
		<#include "/meta/basic.ftl">
	</head>
	<body>
		<center>
			<div class="center_box" id="bodylogon">
				<form action="login" method="post" name="loginform" class="unit">
                    <fieldset id="logondiv">
						<legend>Illegal access</legend>
						<font color="red"><b><#if message??>${message}</#if></b></font>
					</fieldset>
				</form>
			</div>
		</center>
	</body>
</html>