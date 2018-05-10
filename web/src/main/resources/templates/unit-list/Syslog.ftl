<fieldset>
	<legend>Unit summary</legend>
	<fieldset>
		<legend>Filters</legend>
		<table>
			<thead>
				<tr>
					<th align="left">Severity:</th>
					<td>
						 <@macros.dropdown list=severity default="All severities" callMethodForKey="" />
					</td>
				</tr>
				<tr>
					<th align="left">Facility:</th>
					<td>
						 <@macros.dropdown list=facility default="All facilities" callMethodForKey="" />
					</td>
				</tr>
				<tr>
					<th align="left">EventId:</th>
					<td>
						 <@macros.dropdown list=eventid default="All event ids" callMethodForKey="" />
					</td>
				</tr>
				<tr>
					<th align="left">Message count:</th>
					<td>
						<input type="text" size="4" name="filter_msg_count_low" value="${filter_msg_count_low!}" /> - <input type="text" size="4" name="filter_msg_count_high" value="${filter_msg_count_high!}" />
					</td>
				</tr>
				<tr>
					<th align="left">Limit result:</th>
					<td>
						<input type="text" size="4" name="filter_rows_count" value="${filter_rows_count!}" />
					</td>
				</tr>
			</thead>
		</table>
	</fieldset>
	<table style="width:500px;" class="syslog tablesorter {sortlist: [[1,1]]}">
		<thead>
			<tr>
				<th>Unit Id</th>
				<#if severity.selected??>
				<th style="padding-right:20px;">${severity.selected}</th>
				<#else>
				<th style="padding-right:20px;">Count</th>
				</#if>
			</tr>
		</thead>
		<tbody>
			<#list reports as record>
				<tr>
					<td style="${record.rowBackgroundStyle}">
						<a href="javascript:goToUrlAndWait('${URL_MAP.UNITSTATUS}&unit=${record.unit.id?url}<#if record.unit.profile??>&profile=${record.unit.profile.name?url}&unittype=${record.unit.profile.unittype.name?url}<#else>&profile=.&unittype=.</#if>&start=${start}&end=${end}&selectedTab=2&current=false&history=true','Loading unit ...')">
							${record.unit.id}
						</a>
					</td>
					<#if severity.selected??>
					<#if severity.selected="Warning">
					<td style="${record.rowBackgroundStyle}">${record.warnings}</td>
					<#else>
					<#if severity.selected="Error">
					<td style="${record.rowBackgroundStyle}">${record.errors}</td>
					<#else>
					<td style="${record.rowBackgroundStyle}">${record.total}</td>
					</#if>
					</#if>
					<#else>
					<td style="${record.rowBackgroundStyle}">${record.total}</td>
					</#if>
				</tr>
			</#list>
		</tbody>
	</table>
</fieldset>