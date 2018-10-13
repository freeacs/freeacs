			<table style="width:100%;margin-top:10px">
				<tr>
					<th align="left" class="tableheader">Module</th>
					<th align="left" class="tableheader">Status</th>
					<th align="left" class="tableheader">URL</th>
				</tr>
				<#if events??>
					<#list events as event>
					<tr>
						<td style="background:white">${event.module}</td>
						<td style="background:white" onmouseover="Tip('${event.errorMessage!}')">${event.status!}</td>
						<td style="background:white">${event.url!}</td>
					</tr>
					</#list>
				</#if>
			</table>
			<br />
			<div align="left" style="width:700px" id="error"></div>
