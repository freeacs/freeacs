<fieldset>
	<legend>Current selection</legend>
	Group:
	<@macros.dropdown list=groups default="" />
	<a href="${URL_MAP.GROUP}&group=${groups.selected.name}">Go to group</a>
</fieldset>
<fieldset>
	<legend>Unit summary</legend>
	<table class="syslog tablesorter {sortlist: [[1,1]]}" style="width:300px;">
		<thead>
			<tr>
				<th>Unit Id</th>
				<th>Count</th>
			</tr>
		</thead>
		<tbody>
			<#list reports as report>
			<tr>
				<td><a href="${URL_MAP.UNITSTATUS}&unit=${report.getUnit().id}&history=true&current=false&selectedTab=2&start=${start}&end=${end}&syslogFilter=${report.getSyslogEventExpression()!}">${report.getUnit().getId()}</a></td>
				<td>${report.getUnitCount()}</td>
			</tr>
			</#list>
		</tbody>
	</table>
</fieldset>
<input type="hidden" name="group" value="${groups.selected.name}" />