							<form action="" name="form1" method="get" class="unit">
							<fieldset>
							<legend>Monitor</legend>
								<input type="hidden" name="page" value="status" />
								<#if async??><input type="hidden" name="async" value="${async}" /></#if>
								<#include "status.ftl">
							</fieldset>
							</form>
