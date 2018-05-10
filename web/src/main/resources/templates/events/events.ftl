<@macros.form>
	<fieldset>
		<legend>Syslog Event Configuration</legend>
		<table>
			<#if unittypes.selected??>
			<tr>
				<th align="left">Unit Type:</th>
				<td>
					<a href="${URL_MAP.UNITTYPE}&unittype=${unittypes.selected.name?url}">${unittypes.selected.name}</a>
				</td>
			</tr>
			<tr>
				<th align="left">Event Id:</th>
				<td>
					<input type="text" name="eventid" <#if event??>readonly="readonly" style="background-color:#C0C0C0;" value="${event.eventId}"</#if> />
				</td>
				<td>
					<span style="color:red;margin-left:5px;"><#if errors??>${errors.eventid!}</#if></span>
				</td>
			</tr>
			<tr>
				<th align="left">Name:</th>
				<td>
					<input type="text" name="name" <#if event??>value="${event.name!}"</#if> />
				</td>
				<td>
					<span style="color:red;margin-left:5px;"><#if errors??>${errors.name!}</#if></span>
				</td>
			</tr>
			<tr>
				<th align="left">Description:</th>
				<td>
					<input type="text" name="description" <#if event??>value="${event.description!}"</#if> />
				</td>
			</tr>
			<tr>
				<th align="left">Group:</th>
				<td>
					<@macros.dropdown callMethodForKey="id" callMethodForDisplay="name" list=groups default="No group selected" onchange="" />
				</td>
			</tr>
			<tr>
				<th align="left">Expression:</th>
				<td>
					<input type="text" name="expression" <#if event??>value="${event.expression!}"</#if> />
				</td>
				<td>
					<span style="color:red;margin-left:5px;"><#if errors??>${errors.expression!}</#if></span>
				</td>
			</tr>
			<tr>
				<th align="left">StorePolicy:</th>
				<td>
                    <@macros.dropdown list=storepolicies callMethodForKey="" onchange="" />
				</td>
			</tr>
			<tr>
				<th align="left">Script:</th>
				<td>
                    <@macros.dropdown list=scripts callMethodForKey="id" callMethodForDisplay="name" default="No script selected" onchange="" />
				</td>
			</tr>
			<tr>
				<th align="left">Delete Limit:</th>
				<td>
					<input type="text" name="limit" <#if event??>value="${event.deleteLimit!}"</#if> />
				</td>
				<td>
					<span style="color:red;margin-left:5px;"><#if errors??>${errors.limit!}</#if></span>
				</td>
			</tr>
			<tr>
				<td colspan="2">&nbsp;</td>
			</tr>
			<tr>
				<td align="right" colspan="2">
					<#if event??><input type="button" onclick="window.location='${URL_MAP.SYSLOGEVENTS}'" value="Clear" />&nbsp;</#if><input type="submit" name="formsubmit" value="<#if event??>Update<#else>Add</#if>" />
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
		<legend>Syslog Events</legend>
		<#if events?? && (events?size>0)>
		<table class="syslog tablesorter">
			<thead>
				<tr>
					<th align="left">Id</th>
					<th align="left">Name</th>
					<th align="left">Description</th>
					<th align="left">Group</th>
					<th align="left">Expression</th>
					<th align="left">StorePolicy</th>
					<th align="left">Script</th>
					<th align="left">DeleteLimit</th>
					<th align="left">Delete</th>
				</tr>
			</thead>
			<tbody>
				<#list events as event>
				<tr>
					<td align="left"><a href="${URL_MAP.SYSLOGEVENTS}&eventid=${event.eventId}&action=edit">${event.eventId!}</a></td>
					<td><a href="${URL_MAP.SYSLOGEVENTS}&eventid=${event.eventId}&action=edit">${event.name!}</a></td>
					<td>${event.description!}</td>
					<td><#if event.group??><a href="${URL_MAP.GROUP}&group=${event.group.name?url}	">${event.group.name}</a></#if></td>
					<td><a href="${URL_MAP.SYSLOG}&message=${event.expression!}&advancedView=true&cmd=auto">${event.expression!}</a></td>
					<td>${event.storePolicy}</td>
					<td><#if event.script??><a href="${URL_MAP.FILES}&id=${event.script.id}&unittype=${event.unittype.name}">${event.script.name!}</a></#if></td>
					<td>${event.deleteLimit!}</td>
					<td><#if event.id??><a href="${URL_MAP.SYSLOGEVENTS}&eventid=${event.eventId}&action=delete" onclick="return processDelete('delete Syslog Event ${event.eventId}');">Delete</a><#else>n/a</#if></td>
				</tr>
				</#list>
			</tbody>
		</table>
		<#else>
		There are no syslog events
		</#if>
	</fieldset>
	</#if>
</@macros.form>