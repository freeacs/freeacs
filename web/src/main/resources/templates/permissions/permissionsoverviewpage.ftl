									<form action="${URL_MAP.PERMISSIONS}" method="post" name="form1" class="unit" accept-charset="ISO-8859-1">
                                        <input type="hidden" value="permissions" name="page" />
										<#if async??>
										<input type="hidden" name="async" value="${async?string}" />
										<input type="hidden" name="header" value="true" />
										</#if>
										<fieldset>
											<legend>Links</legend>
											<a href="${URL_MAP.PERMISSIONS}&cmd=create<#if async??>&async=true&header=true</#if>">Create new user</a>
										</fieldset>
										<fieldset>
											<legend>Registered users</legend>
											<#if users?? && (users?size>0)>
											<table class="syslog tablesorter">
												<thead>
													<tr>
														<th align="left"><span style="margin-right:20px">Username</span></th>
														<th align="left"><span style="margin-right:20px">Full name</span></th>
														<th align="left"><span style="margin-right:20px">Web page access</span></th>
														<th align="left"><span style="margin-right:20px">Admin</span></th>
														<th align="left">Permissions</th>
													</tr>
												</thead>
												<tbody>
													<#list users as user>
													<tr>
														<td><a onclick="<#if user.permissions?? && (user.permissions.permissions?size>0)>alert('Cannot delete ${user.username}! Delete permissions first!');return false;<#else>return confirm('Do you want to delete ${user.username}?');</#if>" href="${URL_MAP.PERMISSIONS}&cmd=delete&user=${user.username}<#if async??>&async=true&header=true</#if>"><img border="0" src="images/trash.gif" title="Delete user" /></a>&nbsp;&nbsp;<a href="${URL_MAP.PERMISSIONS}&user=${user.username!}<#if async??>&async=true&header=true</#if>">${user.username}</a></td>
														<td>${user.fullname}</td>
														<td>${user.access}</td>
														<td>${user.admin?string}</td>
														<td align="left"><#if !user.admin && user.permissions??><#list user.permissions.permissions as permission>${getunittypename(permission.unittypeId)}[<#if permission.profileId??>${getprofilename(permission.profileId,permission.unittypeId)}<#else>All</#if>]; </#list></#if></td>
													</tr>
													</#list>
												</tbody>
											</table>
											<#else>
											No users is defined.
											</#if>
										</fieldset>
									</form>