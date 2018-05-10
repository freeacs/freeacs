<div id="permissions">
														<#if !permissions?? || (permissions?size==0)>
														No permission is defined.<br />
														<#else>
														<#list permissions as permission>
														<#assign unittypeName = getunittypename(permission.unittypeId)>
														<div>${unittypeName}<#if permission.profileId??><#assign profileName=getprofilename(permission.profileId,permission.unittypeId)> <b>></b> ${profileName}</#if><input value="${unittypeName}<#if profileName??>\${profileName}</#if>" name="permission" type="hidden" style="display:none">&nbsp;<img tabindex="" -1="" title="Remove permission" src="images/trash.gif" alt="trash" onclick='Dom.remove(this.parentNode); var permissions = Dom.id("permissions"); if(Dom.name("permission")==null) permissions.innerHTML ="No permissions is defined";'></div>
														</#list>
														</#if>
														</div>
														<br />
														<select name="unittype" onchange="processForm('details')">
														<option value=".">Choose Unit Type</option>
														<#list unittypes as ut>
														<option <#if unittype?? && unittype=ut.name>selected="selected"</#if>>${ut.name}</option>
														</#list>
														</select>
														<#if profiles??>
														<select name="profile">
														<option value=".">All profiles</option>
														<#list profiles as p>
														<option <#if profile?? && profile=p.name>selected="selected"</#if>>${p.name}</option>
														</#list>
														</select>
														<input type="button" value="Add new permission" onclick="addPermission()" />
														</#if>