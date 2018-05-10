								<#if (errors?size>0) || (warnings?size>0)>
								<table cellspacing="0" cellpadding="0" width="100%">
									<tr>
										<td>
											<#if (errors?size>0)>
											<fieldset>
											<legend><font color="red">Errors</font></legend>
											<#list errors as error><p>${error}</p></#list>
											</fieldset>
											</#if>
										</td>
									</tr>
									<tr>
										<td>
											<#if (warnings?size>0)>
											<fieldset>
											<legend><font color="orange">Warnings</font></legend>
											<#list warnings as warning><p>${warning}</p></#list>
											</fieldset>
											</#if>
										</td>
									</tr>
								</table>										
								</#if>