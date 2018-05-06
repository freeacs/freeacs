							<script type="text/javascript">document.onkeypress = null;</script>
							<@macros.form onsubmit="return validateFields(['new_unit','profile','unittype']);">
								<input name="cmd" type="hidden" value="create" />
								<table cellspacing="0" cellpadding="0">
									<tr>
										<td>
											<div>
												<fieldset>
													<legend>Create new unit</legend>
													<table style="height:90px" id="input">
														<#if isMapSelected(unittypes,"name")=true>
														<tr>
															<th align="right">Profile:</th>
															<td>
																<@macros.dropdown list=profiles default="Choose a profile" class="submitonchange" />
															</td>
														</tr>
														<#if isMapSelected(profiles,"name")=true>
														<tr>
															<th align="right">Unit Id:</th>
															<td><input name="new_unit" type="text" size="20" value="<#if new_unit??>${new_unit}</#if>" /></td>
															<td>
																<input name="formsubmit" value="Create unit" type="submit" />
															</td>
														</tr>
														</#if>
														<#else>
														<tr><td colspan="2">No Unit Type selected</td></tr>
														</#if>
														<tr><td colspan="2" style="color:red"><#if error??>${error}<#else>&nbsp;</#if></td></tr>
														<tr>
															<td colspan="2">
																If you want to search for an existing unit,<br /> go to <a href="${URL_MAP.SEARCH}">the search page</a>
															</td>
														</tr>
													</table>
												</fieldset>
											</div>
										</td>
									</tr>
								</table>
							</@macros.form>