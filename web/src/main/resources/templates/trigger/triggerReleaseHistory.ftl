<@macros.form>
	<!--<script type='text/javascript'>document.onkeypress=KeyPressed;</script>-->
	<table cellspacing="0" cellpadding="0">
		<tr>
			<td valign="top">
				<div>
					<fieldset>
						<legend>Trigger release history</legend>
						<table>
							<#if unittypes.selected??>
							<tr>
								<th align="right">Trigger name:</th>
				                <td width="200"><@macros.dropdown list=triggers default="All triggers" callMethodForKey="id" callMethodForDisplay="name" width="142px" class="submitonchange" /></td>
								<th align="right">Time period: </th>
								<td valign="bottom">
									<input name="tmsstart" type="text" value="${tmsStart}" id="tmsstart" />
									<img height="15" src="images/dateIMG.jpg" alt="date" id="startDate_img" />
									<b>&nbsp;-&nbsp;</b>
									<input name="tmsend" type="text" value="${tmsEnd}" id="tmsend" />
									<img height="15" src="images/dateIMG.jpg" alt="date" id="endDate_img" />
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
	
	<#if releasetriggers?? && (releasetriggers?size>0)>
	<fieldset id="parameters">
		<legend>Triggers</legend>
		<table class="parameter" id="results">
			<tr>
				<th colspan="3"></th>
				<th colspan="2" style="text-align: center;">Number of</th>
				<th colspan="2"></th>
			</tr>
			<tr>
				<th align="left">Trigger name</th>
				<th align="left">Released</th>
				<th align="left">Trigger Events</th>
				<th align="left" width="1px" style="text-align: center;">Units</th>
				<th align="left" width="1px" style="text-align: center;">Events</th>
				<th align="left">Script Execution</th>
				<th align="left">Notification</th>
			</tr>
			<#list releasetriggers as rt>
			<tr>
				<td>
					<a href="${URL_MAP.TRIGGEROVERVIEW}&action=edit&triggerId=${rt.trigger.id}">${rt.trigger.name}</a>
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
		if (document.getElementById('tmsstart') != null) {
			ACS.setupFromCalendar(null,{
				field: "tmsstart",
				button: "startDate_img"
			});
		}
		if (document.getElementById('tmsend') != null) {
			ACS.setupFromCalendar(null,{
				field: "tmsend",
				button: "endDate_img"
			});
		}
	</script>						
</@macros.form>