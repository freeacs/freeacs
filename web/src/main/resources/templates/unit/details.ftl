							<script src="javascript/acs.module.unit.inspection.js"></script>
							<script>
								ACS.unit.inspection.initSettings({
									unitId: '${unit.id}',
									inspectionPageId: '${URL_MAP.INSPECTION.id}',
									unitPageId: '${URL_MAP.UNIT.id}'
								});
								function rewriteURLToAvoidDuplicateUpdate() {
        							history.replaceState({},'Dummy','?page=unit-configuration&init_refreshpage=Refresh');
        							activateFirstInput();
    							}
    							function interceptSubmit(event) {
    								if (!event)
    									event = window.event;
    								TABLETREE.filterParameters();
    								event.preventDefault();
    								event.stopPropagation();
    								return false;
    							}
    							function activateFirstInput() {
    								var firstinput = document.getElementById("firstinput");
    								firstinput.addEventListener('focus', function() {
    									document.getElementById("main-content-form").addEventListener('submit', interceptSubmit, false);
    								}, false);
    								firstinput.addEventListener('blur', function() {
    									document.getElementById("main-content-form").removeEventListener('submit', interceptSubmit, false);
    								}, false);
    								firstinput.disabled = false;
    								document.getElementById("filterBtn").disabled = false;
    								firstinput.focus();
    							}
    							window.onload = rewriteURLToAvoidDuplicateUpdate;
							</script>
							<@macros.form id="main-content-form">
								<input name="unit" type="hidden" value="${unit.id}" />
								<input name="unittype" type="hidden" value="${unittype}" />
								<table width="100%" cellspacing="0" cellpadding="0">
									<tr>
										<td width="400px" style="padding-right: 10px">
											<div>
												<fieldset>
													<legend>Unit configuration</legend>
													<table style="height:160px" id="unit_config">
														<tr>
															<th style="text-align:right;white-space:nowrap;">Profile:</th>
															<td><@macros.dropdown list=profiles onchange="" /></td>
															<td><input name="unitmove" value="Move to profile" type="submit" class="tiptip" title="Immediately move unit to another profile, only unit parameter settings will be kept. Next provisioning may lead to changes in the device, since new profile parameters are applied." /></td>
														</tr>
														<tr>
															<th style="text-align:right;white-space:nowrap;">Software:</th>
															<td>
																<#if !currentsoftware?? && files.items.size() == 1>
																	<@macros.dropdown callMethodForDisplay="version" callMethodForKey="version" list=files onchange="" disabled=true/>
																<#else>
																	<@macros.dropdown callMethodForDisplay="version" callMethodForKey="version" list=files onchange="" />
																</#if>
															</td>
															<td>
																<#if !desiredsoftware?? && !currentsoftware??>
																	<input name="unitupgrade" value="Upgrade" type="submit" onclick="return processDelete('upgrade the software');" disabled="disabled" class="tiptip" title="Will upgrade/downgrade the software on the unit on next provisioning. Make sure to select a valid software file."/>
																<#else>
																	<input name="unitupgrade" value="Upgrade" type="submit" onclick="return processDelete('upgrade the software');" class="tiptip" title="Will upgrade/downgrade the software on the unit on next provisioning. Make sure to select a valid software file."/>
																</#if>
																<#if desiredsoftware?? && currentsoftware?? && currentsoftware != desiredsoftware>
																	<img src="images/Exclamation.gif" height="12px;" alt="Pending upgrade" title="Software upgrade to ${desiredsoftware} is pending" class="tiptip" />
																</#if>
															</td>
														</tr>
														<tr>
															<th style="text-align:right;white-space:nowrap;">Unit Id:</th>
															<td>${unit.id}</td>
															<td><input name="unitdelete" value="Delete" type="submit" onclick="return processDelete('delete the unit');" class="tiptip" title="The unit will be deleted from the system, all unit parameter settings will be lost. The device may continue to connect to the system and may be recovered through Discovery or manual creation of Unit in the system" /></td>
														</tr>
														<tr>
															<th style="text-align:right;white-space:nowrap;" class="tiptip" title="Link to Web GUI of device. You may specify the System.X_FREEACS-COM.Device.GUIURL as a profile/unit parameter to change/set this URL. Reference other parameters to build the URL by using ${r'${parameter-name}'}">Device GUI:</th>
															<td colspan="2">
																<#if guiurl??>
																	<a target="_blank" href="${guiurl}">${guiurl}</a><#if natdetected??> (Behind NAT)</#if>
																<#else>
																	Not defined
																</#if>
															</td>
														</tr>
													</table>
												</fieldset>
											</div>
										</td>
										
										<td width="350px" style="padding-right: 10px">
											<fieldset>
												<legend>Provisioning</legend>
												<table style="height:160px">
													<tr>
														<td colspan="2"><table width=100%><tr>
														<td>
															<!--a href="${URL_MAP.UNIT}&init_provisioning=true"><img src="images/initiateprov.png" class="tiptip" title="Initiates provisioning immediately - if ConnectionRequestURL or UDPConnectionRequestUDP is defined. The device should provision within 30 seconds max."></a -->
															<input name="init_provisioning" value="Provision" type="submit" class="tiptip" title="Initiates provisioning immediately - if ConnectionRequestURL or UDPConnectionRequestUDP is defined. The device should provision within 30 seconds max."/>
														</td>
														<td>
															<input name="init_readall" value="Read all" type="submit" class="tiptip" title="Initiates provisioning immdiately - if ConnectionRequestURL or UDPConnectionRequestUDP is defined. The device will return ALL parameters to read/view within approx. 60 seconds. The device will return to regular provisioning mode after 15 minutes."/>
														</td>
														<td>
															<input name="init_restart" value="Reboot" type="submit" onclick="return processDelete('reboot the device');"  class="tiptip" title="Initiates reboot immdiately - if ConnectionRequestURL or UDPConnectionRequestUDP is defined. The device should provision within 30 seconds max - at which point at reboot command will be issued"/>
														</td>
														<td>
															<input name="init_reset" value="Reset" type="submit" onclick="return processDelete('factory reset the device');"  class="tiptip" title="Initiates factory reset immdiately - if ConnectionRequestURL or UDPConnectionRequestUDP is defined. The device should provision within 30 seconds max - at which point at factory reset command will be issued"/>
														</td>
														<!-- td><input name="init_refreshpage" value="Refresh page" type="submit"/></td -->
														</tr></table></td>
													</tr>
													<tr>
														<td colspan="2" align="center">
														<#if kick_message??>
															<#if kick_message?contains("Reboot")>
																<div class="tiptip" title="${kick_mouseover}" style="font-weight:bold;color:red">${kick_message}</div>
															<#else>
																<div class="tiptip" title="${kick_mouseover}" style="font-weight:bold;color:green">${kick_message}</div>
															</#if>
														</#if>
														</td>
													</tr>
													<tr>
														<th align="right">
															Freq/Spread: 
														</th>
														<td style="white-space:nowrap">
															<input name="frequency" value=${frequency} type="text" size="3" class="tiptip" title="Set provisioning frequency (provisionings pr week), default is 7. Update the parameter System.X_FREEACS-COM.ServiceWindow.Frequency in the profile if you want to make changes to all devices in the profile."/>/
															<input name="spread" value=${spread} type="text" size="2" class="tiptip" title="Set provisioning spread (0-100% from fixed interval), default is 20. Update the parameter System.X_FREEACS-COM.ServiceWindow.Spread in the profile if you want to make changes to all devices in the profile."/>
															<input name="change_freqspread" value="Change" type="submit" class="tiptip" title="Initiates provisioning interval change immediately - if ConnectionRequestURL or UDPConnectionRequestUDP is defined. The device should provision within 30 seconds max - at which point at frequency change will be issued"/>
														</td>
													</tr>
													<tr class="tiptip" title="Shows the provisioning interval in seconds. If +/- factor is greater than 0, it means that every provisioning will change the interval to a random number within this range.">
														<th align="right" >
															Interval: 
														</th>
														<td>
															${frequency_interval}
														</td>
													</tr>
													<tr>
														<th align="right">Last:</th>
														<td style="white-space:nowrap">
															<#if lastconnecttimestamp??>
																${lastconnecttimestamp}
																<#if lastconnectdiff??>
																	${lastconnectdiff}
																</#if>
															<#else>
																Unknown
															</#if>
														</td>
													</tr>
													
													<tr>
														<th align="right">Next:</th>
														<td style="<#if lateconnect>font-weight:bold;color:red;</#if>white-space:nowrap">
															<#if nextconnecttimestamp??>
																${nextconnecttimestamp}
																<#if nextconnectdiff??>
																	${nextconnectdiff}
																</#if>
															<#else>
																Unknown
															</#if>
														</td>
													</tr>
													
												</table>
											</fieldset>
										</td>
										
										<td>
											<fieldset>
												<legend>
													<a href="?page=syslog&cmd=auto&unittype=${unittype}&profile=${unit.profile.name}&unit=%5E${unit.id}%24&advancedView=True&message=%5EProvMsg:&tmsstart=${historystarttms}">
														Provisioning history (last 48 hours)
													</a>
												</legend>
												<div style="overflow-y:scroll;overflow-x:none;min-height:160px;max-height:160px">
												<table class="syslog tablesorter {sortlist:[[0,1]]}">
													<thead>
														<tr>
															<th style="white-space:nowrap;">Timestamp</th>
															<th style="white-space:nowrap;">Status</th>
															<th style="white-space:nowrap;">Type</th>
															<th style="white-space:nowrap;">Eventcode(s)</th>
														</tr>
													</thead>
													<#list history as entry>
													<#if entry.status! = "ERROR">
														<#assign historycolor="#E49400">
													<#else>
														<#assign historycolor="#ccdcb7">
													</#if>
													<tr>
														<td style="white-space:nowrap;background-color:${historycolor}" tablesorter_customkey="${entry.timestamp!}">${entry.timestamp!}</td>
														<td style="white-space:nowrap;background-color:${historycolor}" tablesorter_customkey="${entry.status!}"<#if entry.status! = "ERROR">class="tiptip" title="${entry.errorMessage!}"</#if>>${entry.status!}</td>
														<#if entry.output! = "CONFIG">
														<td style="white-space:nowrap;background-color:${historycolor}" class="tiptip" title="${entry.paramsWritten!} parameters written">
														<#elseif entry.output! = "SOFTWARE">
														<td style="white-space:nowrap;background-color:${historycolor}" class="tiptip" title="Upgraded to version ${entry.fileVersion!}">
														<#else>
														<td style="white-space:nowrap;background-color:${historycolor}">
														</#if>
														${entry.output!}</td>
														<td style="white-space:nowrap;background-color:${historycolor}" tablesorter_customkey="${entry.eventCodes!}">
														${entry.eventCodes!}
														</td>
													</tr>
													</#list>
													<#if history.size() = 0>
													<tr>
														<td colspan="4">No recent provisionings</td>
													</tr>
													</#if>
												</table>
												</div>
											</fieldset>
										</td>
										
									</tr>
								</table>
								<fieldset id="parameters">
									<legend>Parameters</legend>
									
									<table class="parameter" id="results">

										<tr>
											<th align="left">
												<a href='#' onclick='collapse();'><img src='images/minus.gif' id='collapseimage' alt='minus' title="Click here to collapse/expand all" class="tiptip" border='0'/></a>Name
											</th>
											<th>Flags</th>
											<th align="left">Profile value</th>
											<th align="left">Unit value</th>
											<th>Create</th>
											<th>
												<span title="Click here to check all" class="tiptip" onclick="check('delete')">Delete</span>
											</th>
											<#list params as param>
												<#if param.name == "System.X_FREEACS-COM.ProvisioningMode" && param.unitParameter?? && param.unitParameter.value == "READALL">
													<th align="left">CPE (current) value</th>
													<#assign mode_readall=true>
													<#break>
												</#if>
											</#list>
										</tr>

										<tr>
											<td>
												<input name="filterstring" id="firstinput" <#if autofilter>onkeyup="TABLETREE.filterParameters();"</#if> disabled type="text" size="30" value="${string}" tabindex="1" class="tiptip" title="Filter parameter names. Regular expression are allowed" />
												<#if !autofilter>
													<button name="filterParamButton" id="filterBtn" disabled type="button" value="Filter" onclick="TABLETREE.filterParameters();">Filter</button>		
												</#if>
											</td>
											<td style="width: 1px; white-space: nowrap;" colspan="2">
												<select name="filterflag" size="1" onchange="TABLETREE.filterParameters();" tabindex="2">
													<#list flags.items as flag>
														<option<#if flags.selected==flag> selected="selected"</#if>>${flag}</option>
													</#list>
												</select>
											</td>
											<!--<td></td>-->
											<td>
												<select name="filtertype" size="1" onchange="TABLETREE.filterParameters();" tabindex="3">
													<#list types.items as type>
														<option<#if types.selected=type> selected="selected"</#if>>${type}</option>
													</#list>
												</select>
											</td>
											<td colspan="2" style="width: 1px;">
												<input name="formsubmit" value="Update parameters" type="submit"/>
											</td>
											
										</tr>

										
										<#assign tabindex = 3> <!--current tabindex -->
										<#list params as param>
											<#assign tabindex = tabindex + 1>
											<#if param.unittypeParameter??>
												<tr id="${param.name}" style="display: none;">
													<#if param.unitParameter?? || param.profileParameter?? || param.unitSessionParameter??>
														<td class="configured">
													<#else>
														<td class="unconfigured">
													</#if>
														<span style="margin-left:${param.tab}px"><a onclick="return showModal('Unit Type Parameter','${URL_MAP.UNITTYPEPARAMETERS}&unittype=${unittype?url}&utp=${param.unittypeParameter.id}', 700, 500);" href="">${param.shortName}</a></span>
													</td>
													<td>${param.unittypeParameter.flag.flag}</td>
													<td>
														<#if param.profileParameter??>
															<#if !showconfidential?? && confidentialsrestricted && param.unittypeParameter.flag.flag.contains("C")>
																<div class="tiptip" title="Parameter value is confidential. Click 'Show confidentials' in Action Menu to show.">
																	<em>Confidential</em>
																</div>
															<#else>
																<input name="dummy" type="text" value="${param.profileParameter.value}" class="parameterinput" style="background-color:silver" readonly="readonly" />
															</#if>
														</#if>
													</td>
													<#assign utpValues=(param.unittypeParameter.values?? && (param.unittypeParameter.values.values?size>0))>
													<#--if mode_session=false-->
													<#if !(mode_readall??)>
														<#if param.unitParameter??>
															<td>
																<#if !showconfidential?? && confidentialsrestricted && param.unittypeParameter.flag.flag.contains("C")>
																	<div class="tiptip" title="Parameter value is confidential. Click 'Show confidentials' in Action Menu to show.">
																		<em>Confidential</em>
																	</div>
																<#else>
																	<input name="update::${param.name}.Cache" type="hidden" value="${param.unitParameter.value}"/>
																	<#if utpValues>
																		<select name="update::${param.name}" style="width:100%" tabindex="${tabindex}">
																		<#assign selected=false>
																		<#list param.unittypeParameter.values.values as value>
																			<option <#if param.unitParameter.value=value><#assign selected=true>selected="selected"</#if>>${value}</option>
																		</#list>
																		<#if !selected>
																			<option selected="selected" value="${param.unitParameter.value}">${param.unitParameter.value} (custom)</option>
																		</#if>
																	</select>
																	<#else>
																		<input name="update::${param.name}" type="text" value="${param.unitParameter.value}" class="parameterinput" tabindex="${tabindex}" <#if param.unittypeParameter.flag.readOnly=true> style="background-color:silver" readonly="readonly"</#if>/>
																	</#if>
																</#if>
															</td>
															<td></td> <!-- Empty create-column-->
															<td align="center"><input name="delete::${param.name}" type="checkbox" onclick="toggle('update::${param.name}',this);" /></td>
														<#else>
															<td>
																<#if utpValues>
																	<select name="update::${param.name}" style="width:100%;display:none" tabindex="${tabindex}">
																	<#list param.unittypeParameter.values.values as value>
																		<option>${value}</option>
																	</#list>
																	</select>
																<#else>
																	<input name="update::${param.name}" type="text" class="parameterinput" tabindex="${tabindex}" <#if param.unittypeParameter.flag.readOnly=true> style="background-color:silver;display:none;" readonly="readonly"<#else> style="display:none;"</#if> />
																</#if>
															</td>
															<td align="center"><input name="create::${param.name}" onclick="toggle('update::${param.name}',this);" type="checkbox" /></td>														
															<td></td>
														</#if>
													<#else>
														<#if param.unitParameter?? && param.unitParameter.value!="">
															<td>
																<input name="update::${param.name}.Cache" type="hidden" value="${param.unitParameter.value}"/>
																<#if utpValues>
																	<select name="update::${param.name}" style="width:100%" tabindex="${tabindex}">
																	<#assign selected=false>
																	<#list param.unittypeParameter.values.values as value>
																		<option <#if param.unitParameter.value=value><#assign selected=true>selected="selected"</#if>>${value}</option>
																	</#list>
																	<#if !selected>
																		<option selected="selected" value="${param.unitParameter.value}">${param.unitParameter.value} (custom)</option>
																	</#if>
																	</select>
																<#else>
																	<input name="update::${param.name}" type="text" value="${param.unitParameter.value}" class="parameterinput" tabindex="${tabindex}" <#if param.unittypeParameter.flag.readOnly=true> style="background-color:silver" readonly="readonly"</#if>/>
																</#if>
															</td>
															<td></td>															
															<td align="center"><input name="delete::${param.name}" type="checkbox" onclick="toggle('update::${param.name}',this);" /></td>
														<#else>
															<td>
																<#if utpValues>
																	<select name="update::${param.name}" style="width:100%;display:none" tabindex="${tabindex}">
																	<#list param.unittypeParameter.values.values as value>
																		<option>${value}</option>
																	</#list>
																	</select>
																<#else>
																	<input name="update::${param.name}" type="text" class="parameterinput" tabindex="${tabindex}" <#if param.unittypeParameter.flag.readOnly=true> style="background-color:silver;display:none;" readonly="readonly"<#else> style="display:none;"</#if> />
																</#if>
															</td>
															<td align="center"><input name="create::${param.name}" onclick="toggle('update::${param.name}',this);" type="checkbox" /></td>
															<td></td>
														</#if>
													</#if>
													<#if mode_readall?? && param.unitSessionParameter?? && !param.name?starts_with("System")>
														<td>
															<input name="dummy" type="text" value="${param.unitSessionParameter.value!}" class="parameterinput" style="background-color:silver" readonly="readonly" />
														</td>
													</#if>					
												</tr>
											<#else>
												<tr id="${param.name}" style="display: none;">
													<td>
														<span style="margin-left:${param.tab-4}px"><img src="images/minus.gif" alt="minus" onclick="TABLETREE.collapse('${param.name}')"/>${param.shortName}</span>
													</td>
													<td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td>
												</tr>
											</#if>
										</#list>
										<tr><td colspan="6">&nbsp;</td></tr>
									</table>
								</fieldset>
							</@macros.form>