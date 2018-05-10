														<tr>
															<td><b>Profile:</b></td>
															<td>
																<@macros.dropdown list=profiles default="Select profile" class="submitonchange" width="300px"/>
															</td>
														</tr>
														<#if profiles.selected??>
														<tr>
															<td><b>Software:</b></td>
															<td>
																<@macros.dropdown list=softwares callMethodForKey="version" default="Select software" class="submitonchange" width="300px"/>
															</td>
														</tr>
														<#if softwares.selected??>
														<tr>
															<td colspan="2" align="right">
																<input name="formsubmit" value="Upgrade" type="submit" onclick="return validateFields(['unittype','type','firmware','profile']);" />
															</td>
														</tr>
														<tr>
															<td colspan="2" align="right" />
														</tr>
														</#if>
														</#if>