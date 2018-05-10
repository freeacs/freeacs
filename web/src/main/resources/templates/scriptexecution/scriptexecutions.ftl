<@macros.form>
	<fieldset>
		<legend>Script Execution Configuration</legend>
		<table>
			<#if unittypes.selected??>
			
			<div>
			</div>
			<tr>
				<th align="left">Script:</th>
				<td>
					<@macros.dropdown list=scripts callMethodForKey="id" callMethodForDisplay="name" default="Select script" width="300px"/>
				</td>
				<#if file?? && file.error??>
					<td><font color="red">${file.error}</font></td>
				</#if>
			</tr>
			<#if title??>
			<tr>
				<th align="left">Title:</th>
				<td style="max-width:300px">${title}</td>
			</tr>
			</#if>
			<#if description??>
			<tr>
				<th align="left" valign="top">Description:</th>
				<td style="max-width:300px">${description}</td>
			</tr>
			</#if>
			<#if scriptargs??>
				<#list scriptargs as si>
					<tr>
						<th align="left">${si.name}:</th>
						
						<#if si.fileDropDown??>
							<td><@macros.dropdown list=si.fileDropDown onchange="" callMethodForKey="id" callMethodForDisplay="name" default="Select a file" width="300px"/></td>
						<#elseif si.profileDropDown??>
							<td><@macros.dropdown list=si.profileDropDown onchange="" callMethodForKey="id" callMethodForDisplay="name" default="Select a profile" width="300px"/></td>
						<#elseif si.enumDropDown??>
							<td><@macros.dropdown list=si.enumDropDown onchange="" callMethodForKey="value" callMethodForDisplay="description" default="Select a file" width="300px"/></td>
							
						<#else>
							<td><input size=40 type="text" name="argument${si.index}" <#if si.value??>value="${si.value}"</#if>></td>
						</#if>
						<td>
							${si.comment}
							<#if si.validationRule?? || si.error??>
								<#if si.error??> 
									<font color="red">(${si.error})</font>
								<#else>
									(${si.validationRule})
								</#if>
							</#if>
						</td>
					</tr>
				</#list>
			<#else>
				<tr>
					<th align="left">Arguments:</th>
					<td>
						<input name="arguments" type="text" id="arguments" <#if arguments??>value="${arguments}"</#if> size="40" />
					</td>
				</tr>
			</#if>
			<tr>
				<th align="left">RequestId:</th>
				<td><input name="requestid" type="text" id="requestid" <#if requestid??>value="${requestid}"</#if> size="40" /></td>
			</tr>
			<tr>
				<td colspan="2">&nbsp;</td>
			</tr>
			<tr>
				<td align="left">
					<input type="submit" name="formsubmit" value="Refresh" />
				</td>
				<td align="right">
					<input type="submit" name="formsubmit" value="Execute" />
				</td>
			</tr>
			<#else>
				<tr><td>No Unit Type selected</td></tr>
			</#if>
		</table>
		<span style="color:green;font-weight:bold;">${info!}</span>
		<span style="color:red;font-weight:bold;">${error!}</span>
	</fieldset>
	<#if unittypes.selected??>
	<fieldset>
		<legend>Script Executions</legend>
		<#if scriptexecutions?? && (scriptexecutions?size>0)>
		<table class="syslog tablesorter">
			<thead>
				<tr>
					<th align="left">Requested</th>
					<th align="left">Started</th>
					<th align="left">Completed</th>
					<th align="left">Name</th>
					<th align="left">Arguments</th>
					<th align="left">Request id</th>
					<th align="left">End status</th>
					<th align="left">Message</th>
				</tr>
			</thead>
			<tbody>
				<#list scriptexecutions as scriptexecution>
				<tr>
					<td><a href="${URL_MAP.SCRIPTEXECUTIONS}&id=${scriptexecution.id}">${scriptexecution.requestTms?string("yyyy-MM-dd HH:mm:ss")}</a></td>
					<#if scriptexecution.startTms??>
						<td>${scriptexecution.startTms?string("HH:mm:ss")}</td>
					<#else>
						<td></td>
					</#if>
					<#if scriptexecution.endTms??>
						<td>${scriptexecution.endTms?string("HH:mm:ss")}</td>
					<#else>
						<td></td>
					</#if>
					
					<#if scriptexecution.scriptFile??>
						<td><a href="${URL_MAP.FILES}&id=${scriptexecution.scriptFile.id}">${scriptexecution.scriptFile.name!}</a></td>
					<#else>					
						<td>Script deleted</td>
					</#if>
					
					<td>${scriptexecution.arguments!}</td>
					<td>${scriptexecution.requestId!}</td>
					<td>
						<#if scriptexecution.exitStatus??>
							<#if scriptexecution.exitStatus?string == 'false'>
								<img height=18 width=20 src="images/ok.png">
							<#else>
								<img height=18 width=20 src="images/err.png">
							</#if>
						<#else>
							<#if scriptexecution.startTms??>
								<img height=18 width=20 src="images/Exclamation.gif"> (started)
							<#else>
								<img height=18 width=20 src="images/Exclamation.gif"> (queued)
							</#if>
						</#if>
					</td>
					<td>${scriptexecution.errorMessage!}</td>
				</tr>
				</#list>
			</tbody>
		</table>
		<#else>
		There are no scriptexecutions
		</#if>
	</fieldset>
	</#if>
</@macros.form>