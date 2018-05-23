<#if records?? && (records?size>0)>
	<table class="syslog tablesorter {sortlist: [[7,1],[1,1],[2,1]]}" style="width:600px;" name="status-table" id="voip-calls-table">
		<thead>
			<tr>
				<th>Time</th>
				<th style="padding-right:20px;">MOS&nbsp;(1-5)</th>
				<th style="padding-right:20px;">Jitter&nbsp;(average)</th>
				<th style="padding-right:20px;">Jitter&nbsp;(max)</th>
				<th style="padding-right:20px;">Loss&nbsp;(%)</th>
				<th style="padding-right:20px;">Length</th>
				<th style="padding-right:20px;">Line</th>
				<th style="padding-right:20px;">Service&nbsp;quality&nbsp;(0-100)</th>
			</tr>
		</thead>
		<tbody>
			<#list records as record>
			<#if record.telephoneCall>
			<tr>
				<td style="${record.rowBackgroundStyle};text-decoration:underline;"><a href="javascript:IFRAMEDIALOGS.showToolDialogWithDimensions('Real Time Mos','${URL_MAP.REALTIMEMOS}&unit=${info.unit.id}&async=true&reload=true&start=${record.callStartAsString}&end=${record.tmsAsString}&channel=${record.line}',650,300);">${record.tmsAsStringNonBreaking!}</a></td>
				<td style="${record.rowBackgroundStyle}" align="right">${record.mosAvgAsString!}</td>
				<td style="${record.rowBackgroundStyle}" align="right">${record.jitterAvgAsLong!}</td>
				<td style="${record.rowBackgroundStyle}" align="right">${record.jitterMaxAsLong!}</td>
				<td style="${record.rowBackgroundStyle}" align="right" tablesorter_customkey="${record.percentLossAvgAsLong!}">${record.percentLossAvgAsString!}%</td>
				<td style="${record.rowBackgroundStyle}" align="right" tablesorter_customkey="${record.callLengthTotalAsLong!}">${record.callLengthTotalAsString!}</td>
				<td style="${record.rowBackgroundStyle}" align="right">${record.line!}</td>
				<td style="${record.rowBackgroundStyle}" align="right">${record.totalScoreAsString!}</td>
			</tr>
			</#if>
			</#list>
		</tbody>
	</table>
	<script>
		jQuery(document).ready(function(){
			$("#voip-calls-table").tablesorter();
			$(ACS.settings.titlePopupClass).tipTip();
		});
	</script>
<#else>
	<p>no calls in this period</p>
</#if>