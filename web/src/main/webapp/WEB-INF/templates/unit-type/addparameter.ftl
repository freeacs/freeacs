							<@macros.form onsubmit="var bool=validateParameter();param = document.getElementsByName('param.name')[0];newparam = document.getElementsByName('name::1')[0];if(param!=null && newparam!=null && param.value!=newparam.value){ wantto=confirm('Do you want to change the Unit Type parameter name?');if(!wantto){newparam.value=param.value;} return wantto; }else{ return bool; }">
								<input type="hidden" name="unittype" value="${unittype.name}" />
								<#if async??>
								<input type="hidden" name="async" value="true" />
								<input type="hidden" name="header" value="true" />
								</#if>
								<#if param??>
									<input type="hidden" value="${param.name}" id="param.name" name="param.name" />
								</#if>
								<fieldset>
									<legend><#if !param??>Add<#else>Change</#if> Unit Type parameter</legend>
									<div id="parameter::1">
										<table style="width:95%">
											<tr>
												<th align="right" style="width:10%;"><span style="margin-right:10px">Name:</span></th>
												<td>
													<input type="text" name="name::1" value="<#if param??>${param.name}</#if>" style="width:80%;" />
													<input type="hidden" name="flag::1" value="<#if param??>${param.flag.flag}</#if>" />
												</td>
											</tr>
											<tr>
												<th align="right" style="width:10%;"><span style="margin-right:10px">Flags:</span></th>
												<td title="R-parameters are ReadOnly parameters and can never be written/provisioned to the device. RW-parameters can be both read/written to/from the device. System-parameters are System-Only parameters and not part of the provisioning cycle. You may create System-parameters for your own purposes.">
													Read (R):<input onchange="buildFlags()" <#if param?? && param.flag.readOnly=true>checked="checked"</#if> type="radio" name="param:main" value="R"/>
													Read/Write (RW):<input onchange="buildFlags()" <#if param?? && param.flag.readWrite=true>checked="checked"</#if> type="radio" name="param:main" value="RW" />
													System (X):<input onchange="buildFlags()" <#if param?? && param.flag.system=true>checked="checked"</#if> type="radio" name="param:main" value="X" />
												</td>
											</tr>
											<tr>
												<th align="right" style="width:10%;">&nbsp;</th>
												<td>
													<table cellspacing="0" cellpadding="0">
														<tr id="alwaysread" <#if !param?? || (param?? && param.flag.readOnly=false)>style="display:none"</#if> class="tiptip" title="The 'Always Read' flag guarantees that the system will always read this parameter from the device and store it. This is useful for some parameters of particular importance like UDPConnectionRequestAddress or ConnectionRequestURL.">
															<td align="left">Always-Read (A):</td>
															<td><input onchange="buildFlags(this)" <#if param?? && param.flag.alwaysRead=true>checked="checked"</#if> type="checkbox" name="param:always" value="A" /></td>
														</tr>
														<tr name="flag-attributes" <#if !param??>style="display:none"</#if> title="The 'Searchable' flag makes the parameter searchable in the search-page. If, for example, some unit has the value 'foo' on this parameter and you search for 'foo', this unit will appear in the search results.">
															<td align="left">Searchable (S):</td>
															<td><input onchange="buildFlags(this)" <#if param?? && param.flag.searchable=true>checked="checked"</#if> type="checkbox" name="param:searchable" value="S" /></td>
														</tr>
														<tr name="flag-attributes" <#if !param??>style="display:none"</#if> title="Some devices do not commit provisioned values correctly until rebooted. If you absolutely need a unit to reboot after having this parameter set, set the 'Boot-Required' flag. Such device behavior is against the TR-069 specification, and should be a temporary solution.">
															<td align="left">Boot-Required (B):</td>
															<td><input onchange="buildFlags(this)" <#if param?? && param.flag.bootRequired=true>checked="checked"</#if> type="checkbox" name="param:bootrequired" value="B" /></td>
														</tr>
														<tr name="flag-attributes" <#if !param??>style="display:none"</#if> title="Set the 'Confidential' flag for parameters that the unit considers confidential. In TR-069 provisioning, passwords are not allowed to be read from the device, which can cause the system to re-transmit the password on every provisioning. Setting this flag will avoid this.">
															<td align="left">Confidential (C):</td>
															<td><input onchange="buildFlags(this)" <#if param?? && param.flag.confidential=true>checked="checked"</#if> type="checkbox" name="param:confidential" value="C" /></td>
														</tr>
														<tr name="flag-attributes" <#if !param??>style="display:none"</#if> title="Sometimes it is desirable to see some parameter values directly from the unit search results. The 'Displayable' flag makes the parameter appear there.">
															<td align="left">Display (D):</td>
															<td><input onchange="buildFlags(this)" <#if param?? && param.flag.displayable=true>checked="checked"</#if> type="checkbox" name="param:displayable" value="D" /></td>
														</tr>
													</table>
												</td>
											</tr>
											<tr id="addparameters" <#if !param?? || param.flag.readOnly>style="display:none"</#if> title="To simplify daily use and minimize human errors, parameters can be given a 'Enumeration List', i.e. a list of 'allowable values' that the parameter can hold. For boolean values for example, it makes sense to only allow '1/0' values. If a parameter has a enumeration list, its text field is replaced with a drop-down list instead.">
												<th valign="top" align="right" style="width:10%;"><span style="margin-right:10px">Values:</span></th>
												<td>
													<span id="value::1">
														<#if param?? && param.values?? && !param.flag.readOnly>
														<#list param.values.values as value>
															<div><input type="text" style="width:300px" name="value::1::field" value="${value}" />&nbsp;<a href="#" onclick="return goUp(this);" title="Up"><img border="0" src="images/up.jpeg" alt="up" /></a>&nbsp;<a href="#" onclick="return goDown(this);" title="Down"><img border="0" src="images/down.jpeg" alt="down" /></a>&nbsp;&nbsp;<a href="#" onclick="removeMe(this)" title="Remove"><img src="images/trash.gif" alt="trash" title="Delete" border="0" /></a><br /></div>
														</#list>
														</#if>
													</span>
													<input style="margin-left:304px;" type="button" id="addparambutton" onclick="addParameterValue(this)" value="Add enumeration" />
												</td>
											</tr>
											<!--<tr>
												<th colspan="2" align="left">&nbsp;</th>
											</tr>-->
											<tr>
												<td colspan="2" align="right">
													<script type="text/javascript">var returnVal=false;</script>
													<input type="submit" name="addparameters" value="Save parameter" />&nbsp;<#if async??><input type="button" value="Close" onclick="window.top.document.getElementById('popCloseBox').onclick();" /></#if>
												</td>
											</tr>
											<tr>
												<td colspan="2">
													<#if added>
													<p align="left">The parameter was successfully updated</p>
													</#if>
													<#if error?? && (error?size>0)>
													<p align="left" color="red">${error}</p>
													</#if>
												</td>
											</tr>
										</table>
									</div>
								</fieldset>
							</@macros.form>