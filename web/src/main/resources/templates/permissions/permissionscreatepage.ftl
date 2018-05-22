									<script src="javascript/acs.module.permissions.js"></script>
									<form autocomplete="off" action="${URL_MAP.PERMISSIONS}&cmd=create<#if async??>&async=true</#if>" method="post" name="details" class="unit" accept-charset="ISO-8859-1">
										<#if submitted?? && submitted><input type="hidden" id="submitted" value="true" /></#if>
										<input type="hidden" value="permissions" name="page" />
										<input type="hidden" value="create" name="cmd" />
										<#if async??>
										<input type="hidden" name="async" value="true" />
										<input type="hidden" name="header" value="true" />
										</#if>
										<fieldset>
											<legend>Links</legend>
											<a href="${URL_MAP.PERMISSIONS}<#if async??>&async=true&header=true</#if>">List users</a>
										</fieldset>
										<fieldset>
											<legend>Add new user</legend>
											<table>
												<tr>
													<th align="right">Username:</th>
													<td><input type="text" name="user_name" <#if username??>value="${username}"</#if> /></td>
												</tr>
												<tr>
													<th align="right">Full name:</th>
													<td><input type="text" name="user_fullname" <#if fullname??>value="${fullname}"</#if> /></td>
												</tr>
												<tr>
													<th align="right">Password:</th>
													<td><input type="password" name="user_pass" <#if password??>value="${password}"</#if> /></td>
												</tr>
												<tr>
													<th align="right">Admin:</th>
													<td><input type="checkbox" name="user_admin" <#if admin??>value="${admin}"</#if> /></td>
												</tr>
												<tr>
													<th align="right" valign="top">Modules:</th>
													<td>
														<a href="#" id="showtable" onclick="var table = Dom.id('module_table');table.style.display='';Dom.disable(table,'INPUT');this.style.display='none';Dom.id('hidetable').style.display='';return false;"><u>Configure settings</u></a><a href="#" id="hidetable" onclick="var table = Dom.id('module_table');table.style.display='none';Dom.disable(table,'INPUT');this.style.display='none';Dom.id('showtable').style.display='';return false;" style="display:none"><u>Keep old settings</u></a>
														<table style="display:none" id="module_table" cellpadding="0" cellspacing="0">
															<input type="hidden" name="configure" value="true" />
															<tr>
																<td colspan="2"><hr style="width:100px"></td>
															</tr>
															<#list all_pages as page>
															<tr><td>${page.key}</td><td><input style="display:none" type="checkbox" <#if usr_pages?? && usr_pages?seq_contains(page.value)>checked="checked"</#if> value="${page.value}" name="web_access" /></td></tr>
															</#list>
															<tr>
																<td colspan="2"><hr style="width:100px"></td>
															</tr>
														</table>
													</td>
												</tr>
												<tr>
													<th align="right" valign="top">Permissions:</th>
													<td>
														<#include "/permissions/permissionstable.ftl">
													</td>
												</tr>
												<tr>
													<td colspan="2">&nbsp;</td>
												</tr>
												<tr>
													<td colspan="2" align="right"><input type="submit" value="Create new user" name="detailsubmit" /></td>
												</tr>
												<#if message??>
												<tr>
													<td colspan="2" align="left">${message}</td>
												</tr>
												</#if>
											</table>
										</fieldset>
									</form>