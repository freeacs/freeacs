<@macros.form>
	<fieldset>
		<legend>Heartbeat Configuration</legend>
		<table>
			<#if unittypes.selected??>
			<tr>
				<th align="left">Unit Type:</th>
				<td>
					<a href="${URL_MAP.UNITTYPE}&unittype=${unittypes.selected.name?url}">${unittypes.selected.name}</a>
				</td>
			</tr>
			<tr>
				<th align="left">Name:</th>
				<td>
					<input type="text" size=25 name="name" <#if heartbeat??> <#if heartbeat.id??>readonly="readonly" style="background-color:#C0C0C0;"</#if> value="${heartbeat.name!}"</#if> />
				</td>
				<td>
					<span style="color:red;margin-left:5px;"><#if errors??>${errors.name!}</#if></span>
				</td>
			</tr>
			<tr>
				<th align="left">Group:</th>
				<td>
					<@macros.dropdown callMethodForKey="id" callMethodForDisplay="name" list=groups default="No group selected" onchange="" />
				</td>
				<td>
					<span style="color:red;margin-left:5px;"><#if errors??>${errors.groupId!}</#if></span>
				</td>
			</tr>
			<tr>
				<th align="left">Expression:</th>
				<td>
					<input type="text" size=25 name="expression" <#if heartbeat??>value="${heartbeat.expression!}"</#if> />
				</td>
				<td>
					<span style="color:red;margin-left:5px;"><#if errors??>${errors.expression!}</#if></span>
				</td>
			</tr>
			<tr>
				<th align="left">Timeout:</th>
				<td>
					<input type="text" size=25 name="timeout" <#if heartbeat?? && heartbeat.timeoutHours != 0>value="${heartbeat.timeoutHours!}"</#if> />
				</td>
				<td>
					<span style="color:red;margin-left:5px;"><#if errors??>${errors.timeout!}</#if></span>
				</td>
			</tr>
			<tr>
				<td colspan="2">&nbsp;</td>
			</tr>
			<tr>
				<td align="right" colspan="2">
					<#if heartbeat??><input type="button" onclick="window.location='${URL_MAP.HEARTBEATS}'" value="Clear" />&nbsp;</#if><input type="submit" name="formsubmit" value="<#if heartbeat??>Update<#else>Add</#if>" />
				</td>
			</tr>
			<#else>
				<tr><td>No Unit Type selected</td></tr>
			</#if>
		</table>
		<span style="color:green;font-weight:bold;">${info!}</span>
		<span style="color:red;font-weight:bold;">${error!}</span>
	</fieldset>
	<#if unittypes.selected??>
	<fieldset>
		<legend>Heartbeats</legend>
		<#if heartbeats?? && (heartbeats?size > 0)>
		<table class="syslog tablesorter">
			<thead>
				<tr>
					<th align="left">Name</th>
					<th align="left">Group</th>
					<th align="left">Expression</th>
					<th align="left">Timeout</th>
					<th align="left">Delete</th>
				</tr>
			</thead>
			<tbody>
				<#list heartbeats as heartbeat>
				<tr>
					<td><a href="${URL_MAP.HEARTBEATS}&id=${heartbeat.id}&action=edit">${heartbeat.name!}</a></td>
					<td><#if heartbeat.group??><a href="${URL_MAP.GROUP}&group=${heartbeat.group.name?url}	">${heartbeat.group.name}</a></#if></td>
					<td><a href="${URL_MAP.SYSLOG}&message=${heartbeat.expression!}&advancedView=true&cmd=auto">${heartbeat.expression!}</a></td>
					<td>${heartbeat.timeoutHours!}</td>
					<td><#if heartbeat.id??><a href="${URL_MAP.HEARTBEATS}&id=${heartbeat.id}&action=delete" onclick="return processDelete('delete Syslog heartbeat ${heartbeat.name}');">Delete</a><#else>n/a</#if></td>
				</tr>
				</#list>
			</tbody>
		</table>
		<#else>
		There are no syslog heartbeats
		</#if>
	</fieldset>
	</#if>
</@macros.form>