<@macros.form>
	<!--<script type='text/javascript'>document.onkeypress=KeyPressed;</script>-->
	<table cellspacing="0" cellpadding="0">
		<tr>
			<td valign="top">
				<div>
					<fieldset>
						<legend>Trigger release</legend>
						<table>
							<#if unittypes.selected??>
							<tr>
								<th align="right">Trigger name:</th>
								<td width="250" align="left" valign="bottom">
									<input name="filterstring" type="text" onkeyup="TABLETREE.filterJobs();" id="filterstring" />
								</td>
								<th align="right">Showing last trigger releases in timeperiod: </th>
								<td valign="bottom">
									<input name="twohoursbeforetms" type="text" value="${twohoursbeforetms}" id="twohoursbeforetms" style="background-color:silver" readonly="readonly" disabled="true" />
									<b>&nbsp;-&nbsp;</b>
									<input name="tms" type="text" value="${tms}" id="tms" />
									<img height="15" src="images/dateIMG.jpg" alt="date" id="fromDate_img" />
								</td>
								<td align="right">
									<input name="formsubmit" value="Update" type="submit" />
								</td>
							</tr>
							<#else>
							<tr>
								<td>No Unit Type selected</td>
							</tr>
							</#if>
						</table>
					</fieldset>
				</div>
			</td>
		</tr>
	</table>
	<#if triggertablelist?? && (triggertablelist?size>0)>
	<fieldset id="parameters">
		<legend>Triggers</legend>
		<table class="parameter" id="results">
			<tr>
				<th colspan="3"></th>
				<th colspan="2" style="text-align: center;">Number of</th>
				<th colspan="2"></th>
			</tr>
			<tr>
				<th align="left">
					<a href='#' onclick='collapse();'><img src='images/minus.gif' id='collapseimage' alt='minus' title="Click here to collapse/expand all" class="tiptip" border='0'/></a>Trigger name
				</th>
				<th align="left">Released</th>
				<th align="left">Trigger Events</th>
				<th align="left" width="1px" style="text-align: center;">Units</th>
				<th align="left" width="1px" style="text-align: center;">Events</th>
				<th align="left">Script Execution</th>
				<th align="left">Notification</th>
			</tr>
			<#list triggertablelist as triggerelement>
			<#assign rt=triggerelement.releaseTrigger>
			<#if triggerelement.name??>
			<tr id="${triggerelement.name}">
				<td>
					<#if triggerelement.triggerParent=false>
						<span style="margin-left:${triggerelement.tab}px">
							<a href="${URL_MAP.TRIGGEROVERVIEW}&action=edit&triggerId=${triggerelement.trigger.id}">${triggerelement.trigger.name}</a>
						</span>
					<#else>
						<span style="margin-left:${triggerelement.tab-4}px"><img src="images/minus.gif" alt="minus" onclick="javascript:TABLETREE.collapse('${triggerelement.name}')" />
							<a href="${URL_MAP.TRIGGEROVERVIEW}&action=edit&triggerId=${triggerelement.trigger.id}">${triggerelement.trigger.name}</a>
						</span>
					</#if>
				</td>
				<td>
					<#if rt.releasedTms??>
						<img height=18 width=20 src="images/ok.png">&nbsp;${rt.displayReleased}
					<#else>
						No release
					</#if>
				</td>
				<td>
					<#if rt.syslogPageQueryString??>
						<a href="/xapsweb/web${rt.syslogPageQueryString}">Events (${rt.displayFirstEvent}-${rt.displayReleased})</a>
					</#if>
				</td>
				<td>
				${rt.noUnits}
				</td>
				<td>
				${rt.noEvents}
				</td>
				<td>
					<#if rt.scriptExecution??>
						<#if rt.scriptExecution.exitStatus??>
							<#if rt.scriptExecution.exitStatus?string == 'false'>
								<img height=18 width=20 src="images/ok.png">&nbsp;${rt.displayScriptFinished}
							<#else>
								<img height=18 width=20 src="images/err.png">&nbsp;${rt.displayScriptFinished}
							</#if>
						<#else>
							<#if rt.scriptExecution.startTms??>
								<img height=18 width=20 src="images/Exclamation.gif">&nbsp;${rt.displayScriptStarted} (started)
							<#else>
								<img height=18 width=20 src="images/Exclamation.gif">&nbsp;${rt.displayScriptRequested} (queued)
							</#if>
						</#if>
					</#if>
				</td>
				<td>
					<#if rt.notifiedTms??>
						<img height=18 width=20 src="images/ok.png">&nbsp;${rt.displayNotified}	
					</#if>
				</td>
				</tr>
			</#if>
			</#list>
		</table>
	</fieldset>
	<#else><#if triggertablelist??>
	<fieldset id="triggerelementeters">
		<legend>Trigger</legend>
		No triggers found for this selection
	</fieldset>
	</#if></#if>
	<script type='text/javascript'>
		if (document.getElementById('tms') != null) {
			ACS.setupFromCalendar(null,{
				field: "tms",
				button: "fromDate_img"
			});
		}
	</script>						
</@macros.form>