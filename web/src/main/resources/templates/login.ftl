<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
	<head>
		<title>Freeacs Web | login</title>
		<#include "meta/basic.ftl">
        <script src="javascript/jquery-1.4.4.js"></script>
	</head>
	<body>
		<center>
			<div class="center_box" id="bodylogon">
				<form action="/login" id="loginForm" method="post" class="unit">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
					<fieldset id="logondiv">
						<legend>Freeacs</legend>
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
									<input name="password" type="password" maxlength="50" style="width:120px;" />
								</td>
							</tr>
							<tr>
								<td align="right" colspan="2">
									<input name="login" type="submit" value="Login" />
								</td>
							</tr>
						</table>
						<#if error??>
						    ${error}
						</#if>
					</fieldset>
				</form>
			</div>
		</center>
	</body>
</html>