<fieldset>
	<legend>Unit summary</legend>
	<table class="syslog tablesorter {sortlist: [[1,1]]}" style="width:300px;">
		<thead>
			<tr>
				<th>Unit Id</th>
				<th>Output</th>
				<th>Prov. Ok Count</th>
				<th>Prov. Rescheduled Count</th>
				<th>Prov. Error Count</th>
				<th>Prov. Missing Count</th>
			</tr>
		</thead>
		<tbody>
			<#list records as record>
			<tr>
				<td><a href="${URL_MAP.UNITSTATUS}&unit=${record.getUnit().id}&history=true&current=false&selectedTab=2&start=${start}&end=${end}">${record.getUnit().getId()}</a></td>
				<td>${record.output}</td>
				<td>${record.okCount}</td>
				<td>${record.rescheduledCount}</td>
				<td>${record.errorCount}</td>
				<td>${record.missingCount}</td>
			</tr>
			</#list>
		</tbody>
	</table>
</fieldset>
