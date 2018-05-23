<#if records?? && (records?size>0)>
	<p>Last ${records?size-1} entries listed below (<a style="color:#103D74" href="${URL_MAP.SYSLOG}&tmsstart=${start}&tmsend=${end}&unit=${info.unit.id}&cmd=auto&advancedView=true">get more syslog entries</a>):
	<table class="syslog tablesorter" style="width:100%" name="status-table" id="syslog-table">
		<thead>
			<tr>
				<th style="padding-right:20px;">Timestamp</th>
				<th style="padding-right:20px;">Severity</th>
				<th style="padding-right:20px;">Facility</th>
				<th style="padding-right:20px;">Event Id</th>
				<th style="padding-right:20px;">Message</th>
				<th style="padding-right:20px;">Ip Address
			</tr>
		</thead>
		<tbody>
			<#list records as record>
			<tr>
				<td style="${record.rowBackgroundStyle}">${record.tmsAsString!}</td>
				<td style="${record.rowBackgroundStyle}">${record.severity!}</td>
				<td style="${record.rowBackgroundStyle}">${record.facility!}</td>
				<td style="${record.rowBackgroundStyle}">${record.eventIdAsString!}</td>
				<td style="${record.rowBackgroundStyle}" title="${record.message}" class="tiptip">${record.messageExcerpt!}</td>
				<td style="${record.rowBackgroundStyle}">${record.ipAddress!}</td>
			</tr>
			</#list>
		</tbody>
	</table>
	<script>
		jQuery(document).ready(function(){
			$("#syslog-table").tablesorter();
			$(ACS.settings.titlePopupClass).tipTip();
		});
	</script>
<#else>
	<p>no data in this period</p>
</#if>
