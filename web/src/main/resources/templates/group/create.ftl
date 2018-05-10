							<@macros.form id="form1" onsubmit="validateFields(['groupname','description'],event);">
								<input name="page" type="hidden" value="group" />
								<input name="cmd" type="hidden" value="create" />
								<table cellspacing="0" cellpadding="0">
									<tr>
										<td valign="top">
											<div>
												<fieldset>
													<legend>Create Group</legend>
													<table id="input">
														<#if unittypes.selected??>
														<tr>
															<th align="right">Group name:</th>
															<td><input name="groupname" type="text" size="20" value="${name!}" /></td>
														</tr>
														<tr>
															<th align="right">Description:</th>
															<td><input name="description" type="text" size="20" value="${description!}" /></td>
														</tr>
														<tr>
															<th align="right">Parent group:</th>
															<td>
																<@macros.dropdown list=parents default="No parent group" />
															</td>
														</tr>
														<tr>
															<th align="right">Profile:</th>
															<td>
																<#if (profiles.items?size>0)>
																	<@macros.dropdown list=profiles default="All profiles" />
																<#else>
																	<#if profiles.selected??>
																		${profiles.selected.name}
																	<#else>
																		All profiles
																	</#if>
																</#if>
															</td>
														</tr>
														<tr>
															<td align="right" colspan="2">
																<input name="formsubmit" value="Create group" type="submit" />
															</td>
														</tr>
														<#else>
														<tr>
															<td>No Unit Type selected</td>
														</tr>
														</#if>
														<#if message??>
														<tr>
															<td colspan="2" style="color:red;">${message}</td>
														</tr>
														</#if>
													</table>
												</fieldset>
											</div>
										</td>
										<#-- <#if unittypes.selected??>
										<td valign="top" id="timerollingsettings">
											<div>
												<fieldset style="margin-left:10px;">
													<legend>Time rolling</legend>
													<script>
														function switchTimeRollingEnabled(elm){
															if(jQuery(elm).is(":checked")){
																jQuery("#timerollingparameter").removeAttr("disabled");
																jQuery("#timerollingformat").removeAttr("disabled");
																jQuery("#timerollingoffset").removeAttr("disabled");
															}else{
																jQuery("#timerollingparameter").attr("disabled","disabled");
																jQuery("#timerollingformat").attr("disabled","disabled");
																jQuery("#timerollingoffset").attr("disabled","disabled");
															}
														}
													</script>
													<table>
														<tr>
															<th align="right">Enable:</th>
															<td>
																<input type="checkbox" value="true" class="tiptip" title="Check this to enable the time rolling settings" id="timerollingenabled" name="timerollingenabled" onchange="switchTimeRollingEnabled(this)" />
															</td>
														</tr>
														<tr>
															<th align="right">Parameter:</th>
															<td>
																<input type="text" disabled="disabled" class="tiptip" title="Please enter the Unit Type Parameter" name="timerollingparameter" id="timerollingparameter" value="" />
															</td>
														</tr>
														<tr>
															<th align="right">Format:</th>
															<td>
																<@macros.dropdown disabled=true list=formats default="Select format" callMethodForKey="" onchange="" />
															</td>
														</tr>
														<tr>
															<th align="right">Offset:</th>
															<td>
																<@macros.dropdown disabled=true list=offsets default="No offset" callMethodForKey="" onchange="" />
															</td>
														</tr>
													</table>
												</fieldset>
											</div>
										</td>
										<div style="display:none;" id="timerollingutps"><#if unittypes.selected??><#list unittypes.selected.unittypeParameters.unittypeParameters as utp>${utp.name} </#list></#if></div>
										<script>
											xAPS.tabcomplete.initCompletion({
                                                inputSelector:"input[name='timerollingparameter']",
                                                dataSelector:"#timerollingutps"
                                            });
										</script>
										</#if>
										-->
									</tr>
								</table>
							</@macros.form>