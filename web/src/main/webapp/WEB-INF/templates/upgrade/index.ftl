							<@macros.form>
								<#if async??>
								<input type="hidden" name="async" value="true" />
								<input type="hidden" name="header" value="true" />
								</#if>
								<table>
									<tr>
										<td>
											<div>
												<fieldset>
													<legend>Software upgrade</legend>
													<table id="input">
														<#if unittypes.selected??>
														<tr>
															<td><b>Type:</b></td>
															<td>
																<@macros.dropdown list=types default="Select type" callMethodForKey="" class="submitonchange" width="300px"/>
															</td>
														</tr>
														<#if types.selected??>
															<#if types.selected="Profile">
																<#include "/upgrade/profile.ftl">
															<#else><#if types.selected="Unit">
																<#include "/upgrade/unit.ftl">
															</#if></#if>
														</#if>
														<#else>
														<tr><td colspan="2">No Unit Type selected</td></tr>
														</#if>
														<#if error??>
														<tr>
															<td colspan="2" align="right"><font color="red"><b>${error}</b></font></td>
														</tr>
														<#else>
														<#if message??>
														<tr>
															<td colspan="2" align="right"><font color="green"><b>${message}</b></font></td>
														</tr>
														</#if>
														</#if>
													</table>
												</fieldset>
											</div>
										</td>
									</tr>
								</table>
							</@macros.form>