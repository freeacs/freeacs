<#import "/macros/macros.ftl" as macros>
<#if records?? && (records?size>0)>
	Last 100 entries listed below (<a <a style="color:#103D74" href="${URL_MAP.SYSLOG}&tmsstart=${start}&tmsend=${end}&unit=${info.unit.id}&cmd=auto&advancedView=true">get more syslog entries</a>):
	<table class="syslog tablesorter {sortlist: [[1,1],[2,1]]}" style="width:400px !important;" name="status-table" id="hardware-table">
		<thead>
			<tr>
				<th>Time</th>
				<th style="padding-right:20px;text-align:center !important;" title="On-Chip&nbsp;Memory" class="tiptip">OCM</th>
				<th style="padding-right:20px;text-align:center !important;" title="Double&nbsp;Data&nbsp;Rate" class="tiptip">DDR</th>
				<th style="padding-right:20px;text-align:center !important;">Uptime</th>
			</tr>
		</thead>
		<tbody>
			<#list records as record>
			<tr title="${record.tableOfMemoryUnused}" class="tiptip">
				<td style="${record.rowBackgroundStyle}width:140px;">${record.tmsAsString}</td>
				<#if record.bootMessage??>
				<td style="${record.rowBackgroundStyle}" colspan="5">${record.bootMessage}</td>
				<#else>
				<td style="${record.rowBackgroundStyle}" align="right">${record.getMemoryHeapOcmUsagePercentAsString()} %</td>
				<td style="${record.rowBackgroundStyle}" align="right">${record.getMemoryHeapDdrUsagePercentAsString()} %</td>
				<td style="${record.rowBackgroundStyle}" align="right">${record.upTimeAvgAsString!}</td>
				</#if>
			</tr>
			</#list>
		</tbody>
	</table>
	<script>
		jQuery(document).ready(function(){
			$("#hardware-table").tablesorter();
			$(ACS.settings.titlePopupClass).tipTip();
		});
	</script>
<#else>
	<p>no messages to display for this period</p>
</#if>