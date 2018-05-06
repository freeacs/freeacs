<#macro unitreportgraph type methods>
    <@macros.form id="${type}form1" name="${type}form1">
	<input type="hidden" name="${type}selectedTab" id="${type}selectedTab" value="0" />
	<input type="hidden" name="type" value="${type}" />
	<input type="hidden" name="current" value="false" />
	<input type="hidden" name="history" value="true" />
	<div id="${type}ControlsDiv" style="float:right;width:300px;margin-left:20px;">
    	<table cellpadding="0" cellspacing="0">
	    	<tr>
    	    	<th align="left">From:</th>
    	    	<td>
    	    		<input value="${start}" id="${type}start_original" type="hidden" />
    	    		<input value="${start}" name="start" id="${type}start" type="text" />&nbsp;<img src="images/dateIMG.jpg" alt="date" id="${type}start_img" />
				</td>
			</tr>
			<tr>
				<th align="left">To:</th>
				<td>
					<input value="${end}" id="${type}end_original" type="hidden" />
					<input value="${end}" name="end" id="${type}end" type="text" />&nbsp;<img src="images/dateIMG.jpg" alt="date" id="${type}end_img" />
				</td>
			</tr>
			<tr>
				<th align="left">Metric:</th>
				<td>
					<@macros.dropdown class="needsgraphreload" list=methods namePrefixForId="${type}" default="" callMethodForKey="" onchange="" width="180px" />
				</td>
			</tr>
			<tr>
				<th align="left">Period:&nbsp;&nbsp;</th>
				<td>
					<@macros.dropdown class="needsgraphreload" list=periodType namePrefixForId="${type}" default="" callMethodForKey="typeStr" onchange="" width="180px" />
				</td>
			</tr>
			<#if type="Syslog">
			<tr>
				<th align="left">Message:</th>
				<td>
					<input type="text" name="syslogFilter" id="syslogFilter" value="${syslogFilter!}" />
				</td>
			</tr>
			<tr>
				<th align="left">Aggregate:</th>
				<td>
					<@macros.checkbox class="needsgraphreload" namePrefixForId="${type}" list=Sysaggregation onclick="" />
				</td>
			</tr>
			</#if>
			<tr>
				<td colspan="2">
					&nbsp;
				</td>
			</tr>
			<tr>
				<td colspan="2" align="right">
					<input type="button" id="${type}Button" value="update" />
				</td>
			</tr>
		</table>
	</div>
	</@macros.form>
	<img id="${type}Image" src="images/spinner.gif" alt="graph" />
	<div style="clear:both;"></div>
	<div id="${type}status-table" style="margin-top:20px;"><img src="images/spinner.gif" alt="spinner" /></div>
</#macro>