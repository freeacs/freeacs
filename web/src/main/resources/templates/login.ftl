<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
	<head>
		<title>FreeACS Web | login</title>
		<#include "meta/basic.ftl">
	</head>
	<body>
		<center>
			<div class="center_box" id="bodylogon">
				<form action="" id="loginForm" method="post" class="unit">
                    <input type="hidden" name="csrf" value="${csrf}"/>
                    <fieldset id="logondiv">
						<legend>FreeACS</legend>
						<table>
							<tr>
								<td align="right">
									User:
								</td>
								<td>
									<input name="username" type="text" maxlength="50" style="width:120px;" />
								</td>
							</tr>
							<tr>
								<td align="right">
									Password:
								</td>
								<td>
									<input name="password" type="password" maxlength="50" autocomplete="off" style="width:120px;" />
								</td>
							</tr>
							<tr>
								<td align="right" colspan="2">
									<input name="login" type="submit" value="Login" />
								</td>
							</tr>
						</table>
					</fieldset>
					<#if error??>
						<span style="color: red; font-weight: bold">${error}</span>
					</#if>
				</form>
			</div>
		</center>
	</body>
</html>
