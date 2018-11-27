<fieldset id="result">
	<legend>Search results: Found <#if (limit.value < result?size)>more than ${limit.value}<#else>${result?size}</#if> unit(s) <#if displayables?? && (displayables?size>0) && (result?size>1000)> (only displaying 1000 units)</#if> of ${unitcount}</legend>
	<table class="syslog tablesorter">
		<thead>
			<tr>
				<th align="left">Unit Id</th>
				<th align="left">Profile</th>
				<th align="left">Unit Type</th>
				<#if displayheaders??>
				<#list displayheaders as displayheader>
				<th align="left">${displayheader}</th>
				</#list>
				</#if>
			</tr>
		</thead>
		<tbody>
			<#assign count = 0>
			<#list wrappedresults as wrapped>
			<#if (count < limit.value)>
			<#if displayheaders?? && (displayheaders?size > 0) && count = 1000>
			<#assign getvalue=false>
			<#else>
			<#assign getvalue=true>
			</#if>
			<#assign count = count + 1>
			<tr style="font-family: Monospace;">
				<td align="left" style="min-width:270px;">
				<#if reportvalid>
					<a href="${URL_MAP.UNITSTATUS}&amp;unit=${wrapped.unit.id?url}&amp;profile=${wrapped.unit.profile.name?url}&amp;unittype=${wrapped.unit.unittype.name?url}">${wrapped.unit.id}</a>
					<a href="${URL_MAP.UNIT}&amp;unit=${wrapped.unit.id?url}&amp;profile=${wrapped.unit.profile.name?url}&amp;unittype=${wrapped.unit.unittype.name?url}"><img src="images/edit.png" height="15px;" border="0" alt="configuration" /></a>
					<a href="${URL_MAP.UNITSTATUS}&amp;unit=${wrapped.unit.id?url}&amp;profile=${wrapped.unit.profile.name?url}&amp;unittype=${wrapped.unit.unittype.name?url}&amp;current=false&amp;history=true"><img src="images/history.png" height="15px;" border="0" alt="history" /></a>
					<a href="${URL_MAP.SYSLOG}&unit=%5E${wrapped.unit.id?url}%24&cmd=auto&advancedView=true"><img src="images/list.png" height="15px;" border="0" alt="syslogentries" /></a>
				<#else>
					<a href="${URL_MAP.UNIT}&amp;unit=%5E${wrapped.unit.id?url}%24&amp;profile=${wrapped.unit.profile.name?url}&amp;unittype=${wrapped.unit.unittype.name?url}">${wrapped.unit.id}</a>
					<a href="${URL_MAP.SYSLOG}&unit=${wrapped.unit.id?url}&cmd=auto&advancedView=true"><img src="images/list.png" height="15px;" border="0" alt="syslogentries" /></a>
				</#if>
				</td>
				<td align="left">
					<div>
						<a href="${URL_MAP.PROFILE}&amp;profile=${wrapped.unit.profile.name?url}&amp;unittype=${wrapped.unit.unittype.name?url}">${wrapped.unit.profile.name}</a>
					</div>
				</td>
				<td align="left">
					<div>
						<a href="${URL_MAP.UNITTYPE}&amp;unittype=${wrapped.unit.unittype.name?url}">${wrapped.unit.unittype.name}</a>
					</div>
				</td>

				<#if displayheaders??>
				<#list wrapped.displayables as dispvalue>
				<td align="left">${dispvalue}</td>
				</#list>
				</#if>
			</tr>
			</#if>
			</#list>
		</tbody>
	</table>
</fieldset>
