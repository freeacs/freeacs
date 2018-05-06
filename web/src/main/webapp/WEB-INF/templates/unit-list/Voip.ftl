<#if (filter_line)?? || (filter_softwareversion)??>
<fieldset>
	<legend>Current selection</legend>
	<#if filter_line??>Line: ${filter_line}&nbsp;</#if><#if filter_softwareversion??>SoftwareVersion: ${filter_softwareversion}</#if>
</fieldset>
</#if>
<table>
	<tr>
		<td valign="top">
			<fieldset>
				<legend>Units with traffic</legend>
				<#if (reports?size>0)>
				<table class="syslog tablesorter {sortlist: [[4,1]]}">
					<thead>
						<tr>
							<th class="sorttable_nosort">Unit Id</th>
							<th style="padding-right:20px;">MOS (1-5)&nbsp;&nbsp;&nbsp;&nbsp;</th>
							<th style="padding-right:20px;">Length&nbsp;&nbsp;&nbsp;&nbsp;</th>
							<th style="padding-right:20px;">No&nbsp;sip&nbsp;service&nbsp;(min)&nbsp;&nbsp;&nbsp;&nbsp;</th>
							<th style="padding-right:20px;">Total Score&nbsp;&nbsp;&nbsp;&nbsp;</th>
						</tr>
					</thead>
					<tbody>
						<#list reports as report>
						<tr>
							<#assign totalScore = divideby(report.record.voIPQuality,report.record.voIPQuality.dividend)?string("0")>
							<#assign style = backgroundcolor(totalScore)>
							<td style="${style}"><a href="javascript:goToUrlAndWait('${URL_MAP.UNITSTATUS}&unit=${report.unit.id?url}<#if report.unit.profile??>&profile=${report.unit.profile.name?url}&unittype=${report.unit.profile.unittype.name?url}<#else>&profile=.&unittype=.</#if>&start=${start}&end=${end}&history=true&current=false&selectedTab=0','Loading unit ...')">${report.unit.id}</a></td>
							<td style="${style}"><#if mosavgbad(report.unit.id)><img src="images/Exclamation.gif" alt="!" style="height:11px;" title="Mos Average is bad" /></#if>${divideby(report.record.mosAvg,report.record.mosAvg.dividend)?string("0.##")}</td>
							<td tablesorter_customkey="${report.record.callLengthTotal}" style="${style}" align="right">${friendlytime(report.record.callLengthTotal)}</td>
							<td style="${style}"><#if sipregbad(report.unit.id)><img src="images/Exclamation.gif" alt="!" style="height:11px;" title="Too many sip registration fails" /></#if>${divideby(report.record.noSipServiceTime,report.record.noSipServiceTime.dividend)?string("0")}</td>
							<td style="${style}">${totalScore}</td>
						</tr>
						</#list>
					</tbody>
				</table>
				<#else>
				No data available
				</#if>
			</fieldset>
			</td>
			<td valign="top">
			<fieldset>
				<legend>Units without service</legend>
				<#if (failed?size>0)>
				<table class="syslog tablesorter {sortlist: [[1,1]]}">
					<thead>
						<tr>
							<th>Unit Id</th>
							<th>No&nbsp;sip&nbsp;service&nbsp;(min)&nbsp;&nbsp;&nbsp;&nbsp;</th>
						</tr>
					</thead>
					<tbody>
						<#list failed as report>
						<tr>
							<td style="${backgroundcolor("0")}"><a href="javascript:goToUrlAndWait('${URL_MAP.UNITSTATUS}&unit=${report.unit.id?url}<#if report.unit.profile??>&profile=${report.unit.profile.name?url}&unittype=${report.unit.profile.unittype.name?url}</#if>&selectedTab=2&current=false&history=true&start=${start}&end=${end}','Loading unit ...')">${report.unit.id}</a></td>
							<td style="${backgroundcolor("0")}" style="padding-right:20px;">${divideby(report.record.noSipServiceTime,report.record.noSipServiceTime.dividend)?string("0")}</td>
						</tr>
						</#list>
					</tbody>
				</table>
				<#else>
				No data available
				</#if>
			</fieldset>
		</td>
	</tr>
</table>
