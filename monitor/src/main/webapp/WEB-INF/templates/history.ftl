								<script type="text/javascript" src="javascript/monitor.js"></script>
								Currently displaying ${events?size} events. Enter limit: 
								<input type="text" value="<#if limit??>${limit}<#else>100</#if>" size="10" name="limit" />
								<select name="system" size="1" onchange="processForm('form1')">
									<option>All modules</option>
									<#if systems??>
									<#list systems as name>
									<option<#if selectedsystem?? && selectedsystem==name> selected</#if>>${name}</option>
									</#list>
									</#if>
								</select>
								<br/>
								<table style="width:100%;margin-top:10px;">
									<tr>
										<th align="left" class="tableheader">Module</th>
										<th align="left" class="tableheader">Instance</th>
										<th align="left" class="tableheader">Status</th>
										<th align="left" class="tableheader">Logged</th>
										<th align="left" class="tableheader">Response time</th>
										<th align="left" class="tableheader">Message</th>
									</tr>
									<#if events??>
									<#list events as event>
									<tr>
										<td style="background:white" onmouseover="Tip('${event.config}')">${event.realName}</td>
										<td style="background:white">${event.uniqueName}</td>
										<td style="background:white"><#if event.state == 1><span style="color:green">OK</span><#else><span style="color:red">ERROR</span></#if></td>
										<td style="background:white">${event.endTime}</td>
										<td style="background:white">${event.timeSpent}ms</td>
										<td style="background:white"><a href="#" onclick="showError('${event.uniqueName}-${event.endTime}')"><span name="messagelink" id="${event.uniqueName}-${event.endTime}message">Show</span></a></td>
										<input type="hidden" id="${event.uniqueName}-${event.endTime}" value="${event.msg}" />
									</tr>
									</#list>
									</#if>
								</table>
								<br/>
								<div align="left" style="width:700px" id="error"></div>