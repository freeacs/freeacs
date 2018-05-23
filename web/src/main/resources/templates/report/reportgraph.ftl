<script type="text/javascript" src="javascript/acs.module.report.js"></script>
<script>
	ACS.report.initSettings({
    	type : '${pageType}',
    	realtime: {
    		enabled: <#if realtime>true<#else>false</#if>
    	}
    });
</script>
<@macros.form method="GET" onsubmit="$('#updateButton').click();return false;">
	<input type="hidden" name="type" value="${pageType}" />
	<input type="hidden" id="legend" name="legend" value="${legend!}" />
	<#if !advancedView.boolean && periodtype.selected??>
	<input type="hidden" id="period" name="period" value="${periodtype.selected.typeStr!}" />
	</#if>
	<input type="hidden" name="advancedView" id="advancedView_setting" value="${advancedView.boolean?string}" />
	<fieldset id="fieldset${pageType}">
		<legend>${pageType}</legend>
		<table>
			<tr>
				<th align="left">Mode</th>
				<td>Fixed:&nbsp;<input type="radio" name="realtime" value="false"<#if !realtime> checked="checked"</#if> />&nbsp;Real-time:&nbsp;<input type="radio" name="realtime" value="true"<#if realtime> checked="checked"</#if> /></td>
			</tr>
			<tr>
				<th align="left">From</th>
				<td>
					<input type="hidden" id="start_original" value="${start}" />
					<input value="${start}" name="start" id="start" type="text" <#if realtime>disabled="disabled"</#if> />
					<img src="images/dateIMG.jpg" alt="date" id="start_img" />
				</td>
				<th align="left">To</th>
				<td>
					<input type="hidden" id="end_original" value="${end}" />
					<input value="${end}" name="end" id="end" type="text" <#if realtime>disabled="disabled"</#if> />
					<img src="images/dateIMG.jpg" alt="date" id="end_img" />
				</td>
			</tr>
			<#include "/report/custom/"+pageType+".ftl">
			<tr>
				<th align="left">Metric</th>
				<td>
					<@macros.dropdown list=method default="" callMethodForKey="" onchange="" width="180px" class="submitonchange" />
				</td>
				<td>&nbsp;</td>
				<#if advancedView.boolean>
					<td rowspan="4" valign="top" id="aggregationRow">
						<@macros.checkbox list=aggregation onclick="" class="submitonchange" />
					</td>
				<#else>
					<td>&nbsp;</td>
				</#if>
			</tr>
			<#if advancedView.boolean>
				<#if optionalMethod.items??>
					<tr>
						<td>&nbsp;</td>
						<td>
							<@macros.dropdown list=optionalMethod default="Select additional metric" callMethodForKey="" onchange="" class="submitonchange" width="200px" />
						</td>
					</tr>
				</#if>
				<tr>
					<th align="left">Period type</th>
					<td>
						<@macros.dropdown list=periodtype default="" callMethodForKey="typeStr" disabled=realtime onchange="" class="submitonchange" width="180px" />
					</td>
				</tr>
			</#if>
			<tr>
				<td align="right">
					<input type="submit" id="${advancedView.key}" value="<#if advancedView.boolean>Simple form<#else>Advanced form</#if>" />
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td align="right">
					<input type="submit" name="reset" id="resetButton" value="Reset" />
					<input type="submit" name="update" id="updateButton" value="Update" />
				</td>
			</tr>
		</table>
	</fieldset>
</@macros.form>
<div style="margin-top:20px;width:600px;height:400px;" id="imagemap${pageType}">
	${imagemap}
</div>