<script src="javascript/acs.module.unit.dashboard.js" tyoe="text/javascript"></script>
<td valign="top" class="dashboard">
	<fieldset>
		<legend>${UNIT_DASHBOARD}</legend>
		<fieldset>
		<legend>${BASIC_INFO}</legend>
		<table style="width:100%"><tbody>
			<tr>
				<th>${UNIT_TYPE}:&nbsp;</th>
				<td>
					<a href="${URL_MAP.UNITTYPE}&unittype=${info.unit.unittype.name?url}">${info.unit.unittype.name}</a>
				</td>
			</tr>
			<tr>
				<th>${PROFILE}:&nbsp;</th>
				<td>
					<a href="${URL_MAP.PROFILE}&profile=${info.unit.profile.name?url}">${info.unit.profile.name}</a>
				</td>
			</tr>
			<tr>
				<th>${SERIAL_NUMBER}:&nbsp;</th>
				<td>
					<a href="${URL_MAP.UNIT}&unit=${info.unit.id?url}" title="Go to Unit configuration" class="tiptip"><#if info.serialNumber??>${info.serialNumber!}<#else>${info.unit.id}</#if></a>
				</td>
			</tr>
			<tr>
				<th>${SOFTWARE_VERSION}:&nbsp;</th>
				<td>
					${info.softwareVersion!}<#if info.desiredSoftwareVersion?? && (!info.softwareVersion?? || info.desiredSoftwareVersion!=info.softwareVersion)>&nbsp;<img src="images/Exclamation.gif" height="12px;" alt="pending upgrade" title="An upgrade is pending to ${info.desiredSoftwareVersion!}" class="tiptip" /></#if>
				</td>
			</tr>
			<tr>
				<th>${IP_ADDRESS}:</th>
				<td>
					<#if info.ipAddress??>
						${info.ipAddress!}
					<#else>
						${format(NOT_DEFINED_FOR_THIS,UNIT)}
					</#if>
				</td>
			</tr>
			<tr>
				<th>Behind Gateway/NAT:</th>
				<td>
					<#if info.isBehindNat()>
						Yes
					<#else>
						No
					</#if>
				</td>
			</tr>
			<tr>
				<th>Supports TR-111:</th>
				<td style="font-weight:bold;<#if info.supportsTr111()>color:green<#else>color:red</#if>;">
					<#if info.supportsTr111()>
						Yes
					<#else>
						No
					</#if>
				</td>
			</tr>
			<tr>
				<td colspan="2" align="right">&nbsp;</td>
			</tr>
			<tr>
				<td colspan="2" align="right"><a href="${URL_MAP.UNIT}&unit=${info.unit.id}">Go to Unit configuration</a></td>
			</tr>
		</tbody></table>
		</fieldset>
		<fieldset>
		<legend>${UNIT_DASHBOARD_CURRENT}</legend>
		<table style="width:100%"><tbody>
			<tr>
				<th>${FIRST_MANAGEMENT}:&nbsp;</th>
				<td>
					<#if info.firstConnectTimestamp??>
					${info.firstConnectTimestamp!}
					<#else>
					${format(NOT_DEFINED_FOR_THIS,UNIT)}
					</#if>
				</td>
			</tr>
			<tr>
				<th>${LAST_MANAGEMENT}:&nbsp;</th>
				<td style="font-weight:bold;<#if info.withinServiceWindow>color:green<#else>color:red</#if>;">
					<#if info.lastConnectTimestamp??>
					${info.lastConnectTimestamp!}
					<#else>
					${format(NOT_DEFINED_FOR_THIS,UNIT)}
					</#if>
				</td>
				<#if (info.overallStatus.serviceWindowEffect>0)>
				<td style="padding-left:10px;padding-right:10px;color:red" align="right">
					( -${info.overallStatus.serviceWindowEffect?string("0.0")} )
				</td>
				</#if>
			</tr>
			<tr>
				<th>${NEXT_MANAGEMENT}:&nbsp;</th>
				<td>
					<#if info.nextConnectTimestamp??>
					${info.nextConnectTimestamp!}
					<#else>
					${format(NOT_DEFINED_FOR_THIS,UNIT)}
					</#if>
				</td>
			</tr>
			<#if info.line1Applicable>
			<tr>
				<th>${format(LINE_STATUS,1)}:</th>
				<td>
					<#if info.line1Configured && !info.line1Registered>
					<font color="#FFC62D"><b>Configured, not registered</b></font>
					<#else>
					<#if info.line1Configured && info.line1Registered>
					<font color="green"><a id="line1Status" style="color:green;font-weight: bold;" mos_registered="Registered" mos_inacall="Registered, in a call" mos_href="javascript:IFRAMEDIALOGS.showToolDialogWithDimensions('Real Time Mos: Line 1','${URL_MAP.REALTIMEMOS}&unit=${info.unit.id}&async=true&reload=true&channel=0',650,300);">Registered</a></font>
					<#else>
					<#if info.line1ConfiguredError>
					<font color="red">Configure error</font>
					<#else>
					<#if info.line1ConfiguredNotEnabled>
					${CONFIGURED_BUT_NOT_ENABLED}
					<#else>
					${NOT_CONFIGURED}
					</#if>
					</#if>
					</#if>
					</#if>
				</td>
				<#if (info.overallStatus.voipLineEffect>0)>
				<td rowspan="2" style="padding-left:10px;padding-right:10px;color:red" align="right">
					( -${info.overallStatus.voipLineEffect?string("0.0")} )
				</td>
				</#if>
			</tr>
		    <tr>
				<th>${format(LINE_STATUS,2)}:</th>
				<td>
					<#if info.line2Configured && !info.line2Registered>
					<font color="#FFC62D"><b>Configured, not registered</b></font>
					<#else>
					<#if info.line2Configured && info.line2Registered>
					<font color="green"><a id="line2Status" style="color:green;font-weight: bold;" mos_registered="Registered" mos_inacall="Registered, in a call" mos_href="javascript:IFRAMEDIALOGS.showToolDialogWithDimensions('Real Time Mos: Line 2','${URL_MAP.REALTIMEMOS}&unit=${info.unit.id}&async=true&reload=true&channel=1',650,300);">Registered</a></font>
					<#else>
					<#if info.line2ConfiguredError>
					<font color="red">Configure error</font>
					<#else>
					<#if info.line2ConfiguredNotEnabled>
					${CONFIGURED_BUT_NOT_ENABLED}
					<#else>
					${NOT_CONFIGURED}
					</#if>
					</#if>
					</#if>
					</#if>
				</td>
			</tr>
			</#if>
		</tbody></table>
		</fieldset>
		<fieldset>
		<legend>History from <span style="font-weight:bold;">${startSimple}</span> to <span style="font-weight:bold;">${endSimple}</span></legend>
		<table style="width:100%"><tbody>
			<#if showVoip>
				<tr>
					<th title="This gives an hint to the average call quality of both VoIP lines." class="tiptip">VoIP quality:</th>
					<td>
						<div id="unitTotalScore"><img src="images/spinner.gif" alt="wait" /></div>
					</td>
					<td id="unitTotalScoreEffect" style="display:none;padding-left:10px;padding-right:10px;color:red" align="right"></td>
				</tr>
			</#if>
			<tr>
				<th>Syslog status:</th>
				<td>
					<#if (info.overallStatus.syslogEffect>0)>
					<font color="red">Errors have been logged</font>
					<#else>
					${NO_ERRORS_LOGGED}
					</#if>
				</td>
				<#if (info.overallStatus.syslogEffect>0)>
				<td style="padding-left:10px;padding-right:10px;color:red" align="right">
					( -${info.overallStatus.syslogEffect?string("0.0")} )
				</td>
				</#if>
			</tr>
			<#if showHardware>
				<tr>
					<th>Hardware status:</th>
					<td>
						<#if (info.overallStatus.hardwareEffect>0)>
						<font color="red">Errors have been logged</font>
						<#else>
						${NO_ERRORS_LOGGED}
						</#if>
					</td>
					<#if (info.overallStatus.hardwareEffect>0)>
					<td style="padding-left:10px;padding-right:10px;color:red" align="right">
						( -${info.overallStatus.hardwareEffect?string("0.0")} )
					</td>
					</#if>
				</tr>
			</#if>
			<tr>
				<td colspan="3" align="right">&nbsp;</td>
			</tr>
			<tr>
				<td colspan="3" align="right"><a href="${URL_MAP.UNITSTATUS}&history=true&current=false&unit=${info.unit.id}">Go to Unit history</a></td>
			</tr>
		</tbody></table>
		</fieldset>
		<#if shortCutParams?has_content>
		<fieldset>
		<legend>Custom parameters</legend>
		<table style="width:100%"><tbody>
			<#list shortCutParams.entrySet() as entry>
				<tr>
                	<th>${entry.key}:</th>
					<td>
						<#if entry.value?matches("^https://.*|^http://.*")>
							<a href="${entry.value}" target="_new">${entry.value}</a>
						<#else>
							${entry.value}
						</#if>
					</td>
				</tr>
			</#list>
		</tbody></table>
		</fieldset>
		</#if>
	</fieldset>
</td>
<td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
<td>
	<img src="images/spinner.gif" id="overallstatus-speedometer" alt="Status pie" /><br />
</td>
<script>
    ACS.unit.dashboard.initSettings({
       start: '${start}',
       end: '${end}',
       unitId: '${info.unit.id}',
       pageId: '${URL_MAP.UNITSTATUS.id}',
       showHardware: ${showHardware?string},
       showVoip: ${showVoip?string}
    });
</script>
