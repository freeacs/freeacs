								<fieldset id="parameters">
									<legend>Syslog entries: Found <#if (expectedrows<rowsreturned)>more than </#if><#if (expectedrows>rowsreturned)>${result?size}<#else>${expectedrows}</#if> entries in the syslog.</legend>
									<table class="syslog tablesorter {sortlist:[[0,1]]}">
										<thead>
											<tr>
												<th align="center">Timestamp</th>
												<th align="center" style="padding-right:20px;">Unit&nbsp;ID</th>
												<th align="center" style="padding-right:20px;">Severity</th>
												<th align="center" style="padding-right:20px;">Facility</th>
												<th align="center" style="padding-right:20px;">Facility version</th>
												<th align="center" style="padding-right:20px;">Event&nbsp;ID</th>
												<th align="center" style="padding-right:20px;">Message</th>
												<th align="center" style="padding-right:20px;">User</th>
												<!-- th align="center" style="padding-right:20px;">Host&nbsp;name</th -->
												<th align="center" style="padding-right:20px;">IP&nbsp;address</th>
												<!-- th align="center" style="padding-right:20px;">Profile</th -->
												<!-- th align="center" style="padding-right:20px;">Unit&nbsp;Type</th -->
											</tr>
										</thead>
										<tbody>
											<#list result as entry>
											<tr style="white-space:nowrap">
												<td style="background-color: #${background(entry.severity)} !important">
													<font color="#${fontcolor(entry.severity)}">
														${entry.collectorTimestamp?string(DATE_FORMAT_WITH_SECONDS_NON_BREAKING)}
													</font>
												</td>
												<td align="left" style="background-color: #${background(entry.severity)} !important">
													<font color="#${fontcolor(entry.severity)}">
														<#if entry.unitId??>
														<a href="${URL_MAP.UNIT}&amp;unit=${entry.unitId?url}&amp;profile=${getprofile(entry)!?url}&amp;unittype=${getunittype(entry)!?url}">${entry.unitId}</a>
														<a href="${URL_MAP.UNIT}&amp;unit=${entry.unitId?url}&amp;profile=${getprofile(entry)!?url}&amp;unittype=${getunittype(entry)!?url}"><img src="images/edit.png" height="15px;" border="0" alt="configuration" /></a>
														<a href="${URL_MAP.SYSLOG}&unit=%5E${entry.unitId?url}%24&cmd=auto&advancedView=true"><img src="images/list.png" height="15px;" border="0" alt="syslogentries" /></a>
														
														</#if>
													</font>
												</td>
												<td tablesorter_customkey="${entry.severity!}" align="center" style="background-color: #${background(entry.severity)} !important">
													<font color="#${fontcolor(entry.severity)}">
														<#if entry.severity?? && severitytext(entry.severity)??>${severitytext(entry.severity)}</#if>
													</font>
												</td>
												<td tablesorter_customkey="${entry.facility!}"align="center" style="background-color: #${background(entry.severity)} !important">
													<font color="#${fontcolor(entry.severity)}">
														<#if entry.facility?? && facilitytext(entry.facility)??>${facilitytext(entry.facility)}</#if>
													</font>
												</td>
												<td align="right" style="background-color: #${background(entry.severity)} !important">
													<font color="#${fontcolor(entry.severity)}">
														${entry.facilityVersion!}
													</font>
												</td>
												<td align="right" style="background-color: #${background(entry.severity)} !important"<#if entry.unittypeId?? && entry.eventId?? && eventdesc(entry.unittypeId,entry.eventId)??>title="${eventdesc(entry.unittypeId,entry.eventId)}"<#else>title="${eventdesc("",entry.eventId)}"</#if> class="tiptip">
													<font color="#${fontcolor(entry.severity)}">
														${entry.eventId}
													</font>
												</td>
												<td align="left" style="background-color: #${background(entry.severity)} !important">
													<font color="#${fontcolor(entry.severity)}">
														${entry.content}
													</font>
												</td>
												<td align="left" style="background-color: #${background(entry.severity)} !important">
													<font color="#${fontcolor(entry.severity)}">
														${entry.userId!}
													</font>
												</td>
												<td align="left" style="background-color: #${background(entry.severity)} !important">
													<font color="#${fontcolor(entry.severity)}">
														${entry.ipAddress!}
													</font>
												</td>
											</tr>
											</#list>
										</tbody>
									</table>
								</fieldset>