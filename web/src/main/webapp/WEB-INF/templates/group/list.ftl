							<@macros.form>
								<!--<script type='text/javascript'>document.onkeypress=KeyPressed;</script>-->
								<table cellspacing="0" cellpadding="0">
									<tr>
										<td valign="top">
											<div>
												<fieldset>
													<legend>Group overview</legend>
													<table>
														<#if unittypes.selected??>
														<tr>
															<th align="right">Profile:</th>
															<td>
																<@macros.dropdown list=profiles default="Choose a profile" />
															</td>
														</tr>
														<tr>
															<th align="right">Group name:</th>
															<td>
																<input name="filterstring" type="text" onkeyup="TABLETREE.filterJobs();" id="filterstring" />
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
								<#if params?? && (params?size>0)>
								<fieldset id="parameters">
									<legend>Groups</legend>
									<table class="parameter" id="results">
										<tr>
											<th align="left">
												<a href='#' onclick='collapse();'><img src='images/minus.gif' id='collapseimage' alt='minus' title="Click here to collapse/expand all" class="tiptip" border='0'/></a>Group name
											</th>
											<th align="left">Last count</th>
											<th align="left">Profile</th>
											<th align="left">Description</th>
										</tr>
										<#list params as param>
										<#if param.name??>
										<tr id="${param.name}">
											<#if param.groupParent=false>
												<td>
													<span style="margin-left:${param.tab}px"><a href="?page=group&amp;group=${param.group.name?url}">${param.group.name}</a></span>
												</td>
											<#else>
												<td>
													<span style="margin-left:${param.tab-4}px"><img src="images/minus.gif" alt="minus" onclick="javascript:TABLETREE.collapse('${param.name}')" /><a href="?page=group&amp;group=${param.group.name?url}">${param.group.name}</a></span>
												</td>
											</#if>
											<td>${param.group.count!}</td>
											<td>${findprofile(param.group.name)!}</td>
											<td>${param.group.description!}</td>
										</tr>
										</#if>
										</#list>
									</table>
								</fieldset>
								<#else><#if params??>
								<fieldset id="parameters">
									<legend>Groups</legend>
									No groups found for this selection
								</fieldset>
								</#if></#if>
							</@macros.form>