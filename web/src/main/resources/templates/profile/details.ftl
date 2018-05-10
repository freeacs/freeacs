								<@macros.form>
									<!--<script type='text/javascript'>document.onkeypress=KeyPressed;</script>-->
									<input name="page" type="hidden" value="profile" />
									<table cellspacing="0" cellpadding="0">
										<tr>
											<td valign="top">
												<div>
													<fieldset id="details">
														<legend>Profile configuration</legend>
														<table style="height:60px;" id="input">
															<tr>
																<th align="right">Unit Type:</th>
																<td>
																	<@macros.dropdown list=unittypes default="Select unittype" width="200px" />
																</td>
															</tr>
															<tr>
																<th align="right">
																	<div title="Scroll down the drop-down menu to find profiles already created" class="tiptip">
																		Profile:
																	</div>
																</th>
																<td>
																	<@macros.dropdown list=profiles default="Select profile" width="200px" />
																</td>
																<td>
																	<input name="formsubmit" value="Delete" type="submit" onclick="return processDelete('delete the profile');" />
																</td>
															</tr>
														</table>
													</fieldset>
												</div>
											</td>
										</tr>
									</table>
									<fieldset id="parameters">
										<legend>Parameters</legend>
										<table id="actions" class="center" style="width:100%">
											<tr>
												<td style="width:180px">
													Name:
													<input name="${string.key}" type="text" onkeyup="TABLETREE.filterParameters()" size="9" value="${string.string!}"/>
												</td>
												<td style="width:180px">
													Flag:
													<@macros.dropdown list=flags callMethodForKey="" width="120px" onchange="TABLETREE.filterParameters()" />
												</td>
												<td style="width:210px">
													Status:
													<@macros.dropdown list=types callMethodForKey="" width="120px" onchange="TABLETREE.filterParameters()" />
												</td>
												<td align="right"><input name="formsubmit" value="Update parameters" type="submit" /></td>
											</tr>
										</table>
										<table class="parameter" id="results">
											<tr>
												<th align="left">
													<a href='#' onclick='collapse();'><img src='images/minus.gif' id='collapseimage' alt='minus' title="Click here to collapse/expand all" class="tiptip" border='0'/></a>Name
												</th>
												<th>Flags</th>
												<th align="left">Profile value</th>
												<th>Create</th>
												<th>
													<span title="Click here to check all" onclick="check('delete')" class="tiptip">Delete</span>
												</th>
											</tr>
											<#list params as param>
											<#if param.unittypeParameter??>
											<tr id="${param.name}">
												<#if param.profileParameter??>
												<td class="configured">
												<#else>
												<td class="unconfigured">
												</#if>
													<span style="margin-left:${param.tab}px"><a onclick="return showModal('Unit Type Parameter','${URL_MAP.UNITTYPEPARAMETERS}&unittype=${profile.unittype.name?url}&utp=${param.unittypeParameter.id}', 700, 500);" href="">${param.shortName}</a></span>
												</td>
												<td>${param.unittypeParameter.flag.flag}</td>
												<td>
													<#assign utpValues= param.unittypeParameter.values?? && (param.unittypeParameter.values.values?size>0)>
													<#if param.profileParameter??>
													<#if utpValues>
													<select name="update::${param.name}" style="width:100%">
														<#assign selected=false>
														<#list param.unittypeParameter.values.values as value>
														<option <#if param.profileParameter.value=value><#assign selected=true>selected="selected"</#if>>${value}</option>
														</#list>
														<#if !selected>
															<option value="${param.profileParameter.value}" selected="selected">${param.profileParameter.value} (custom)</option>
														</#if>
													</select>
													<#else>
													<input name="update::${param.name}" type="text" value="${param.profileParameter.value}" class="parameterinput"<#if param.unittypeParameter.flag.readOnly=true> style="background-color:silver" readonly="readonly"</#if>/>
													</#if>
													<#else>
													<#if utpValues>
													<select name="update::${param.name}" style="width:100%;display:none">
														<#list param.unittypeParameter.values.values as value>
														<option>${value}</option>
														</#list>
													</select>
													<#else>
													<input name="update::${param.name}" type="text" class="parameterinput"<#if param.unittypeParameter.flag.readOnly=true> style="background-color:silver;display:none;" readonly="readonly"<#else>style="display:none;"</#if> />
													</#if>
													</#if>
												</td>
												<td><#if !param.profileParameter??><input name="create::${param.name}" onclick="toggle('update::${param.name}',this);" type="checkbox" /></#if></td>
												<td align="center"><#if param.profileParameter??><input name="delete::${param.name}" type="checkbox" onclick="toggle('update::${param.name}',this);" /></#if></td>
											</tr>
											<#else>
											<tr id="${param.name}">
												<td>
													<span style="margin-left:${param.tab-4}px"><img src="images/minus.gif" alt="minus" onclick="TABLETREE.collapse('${param.name}')" />${param.shortName}</span>
												</td>
												<td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td>
											</tr>
											</#if>
											</#list>
											<tr><td colspan="5">&nbsp;</td></tr>
											<tr>
												<td colspan="5" align="right">
													<input name="formsubmit" value="Update parameters" type="submit" />
	
												</td>
											</tr>
										</table>
									</fieldset>
								</@macros.form>