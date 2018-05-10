							<@macros.form onsubmit="return validateFields(['profilename']);">
								<!--<script type='text/javascript'>document.onkeypress=KeyPressed;</script>-->
								<input name="cmd" type="hidden" value="create" />
								<table cellspacing="0" cellpadding="0">
									<tr>
										<td valign="top">
											<div>
												<fieldset>
													<legend>Create new profile</legend>
													<table id="input">
														<tr>
															<th align="right">Unit Type:</th>
															<td>
																<@macros.dropdown list=unittypes default="Select unittype" width="200px" />
															</td>
														</tr>
														<#if unittypes.selected??>
														<tr>
															<th align="right">Profile name:</th>
															<td><input name="profilename" type="text" size="20" /></td>
														</tr>
														<#if profilestocopyfrom.items?? && (profilestocopyfrom.items?size>0)>
														<tr>
															<th align="right">Copy parameters from:</th>
															<td>
																<@macros.dropdown list=profilestocopyfrom default="Dont copy any parameters" onchange="" width="200px" />
															</td>
															<td>
																<input name="formsubmit" value="Create profile" type="submit" />
															</td>
														</tr>
														<#else>
														<tr>
															<td colspan="2">&nbsp;</td>
															<td align="right" width="100px">
																<input name="formsubmit" value="Create profile" type="submit"  />
															</td>
														</tr>
														</#if>
														</#if>
														<#if error??>
														<tr>
															<td colspan="3">
																<center><font color="red">${error}</font></center>
															</td>
														</tr>
														</#if>
													</table>
												</fieldset>
											</div>
										</td>
									</tr>
								</table>
							</@macros.form>