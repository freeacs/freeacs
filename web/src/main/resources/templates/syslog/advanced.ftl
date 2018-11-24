								<table cellspacing="0" cellpadding="0">
									<tr>
										<td align="top">
											<fieldset>
												<legend>Syslog advanced filter</legend>
												<table>
													<#include "/syslog/basic.ftl">
													<#if events??>
													<tr>
														<th align="right">Event:</th>
														<td>
															<@macros.dropdown list=events callMethodForKey="key" callMethodForDisplay="value" default="All" width="200px" onchange="" />
														</td>
													</tr>
													</#if>
													<#if facilities??>
													<tr>
														<th align="right">Facility:</th>
														<td>
															<@macros.dropdown list=facilities callMethodForKey="key" callMethodForDisplay="value" default="All" width="200px" onchange="" />
														</td>
													</tr>
													</#if>
													<tr>
														<th align="right">Facility version:</th>
														<td>
															<input type="text" name="${facilityversion.key}" value="${facilityversion.string!}" />
														</td>
													</tr>
													<tr>
														<th align="right" title="Choose one, choose multiple or search for all" class="tiptip">
															Severity:
														</th>
														<td>
															<@macros.dropdown list=severities size="4" multiple=true callMethodForKey="" default="" width="200px" onchange="" />
														</td>
													</tr>
													<tr>
														<th align="right">User Id:</th>
														<td><input name="userid" type="text" <#if userid??>value="${userid}"</#if> /></td>
													</tr>
													<tr>
														<th align="right">Ip Address:</th>
														<td><input name="ipaddress" type="text" <#if ipaddress??>value="${ipaddress}"</#if> /></td>
													</tr>
													<tr>
														<th align="right">Message:</th>
														<td><input name="message" type="text" <#if message??>value="${message}"</#if>/></td>
													</tr>
													<tr>
														<th align="right">Max rows:</th>
														<td><input name="maxrows" type="text" value="${maxrows}" /></td>
													</tr>
													<#include "/syslog/footer.ftl">
												</table>
											</fieldset>
										</td>
									</tr>
								</table>
