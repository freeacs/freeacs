														<tr>
															<td align="right">
																<input name="mode" type="hidden" /><input name="advancedView" type="hidden" value="${advancedView?string}" />
																<input name="submitButtonJS" value="<#if advancedView>Simple form<#else>Advanced form</#if>" type="button" onclick="document.form1.advancedView.value='${(!advancedView)?string}';document.form1.submit();" />
															</td>
															<td>&nbsp;</td>
															<td>&nbsp;</td>
															<td align="right">
																<input name="formsubmit" value="Retrieve syslog" type="submit" onclick="return addMessage('Searching .....','syslog')" />
															</td>
														</tr>
														<tr>
															<td align="right" colspan="4">
																<#if cmd??>
																	<#if cmd="auto">
																		<input type="hidden" name="cmd" value="ready" />
																		<script type="text/javascript">document.form1.formsubmit.click();</script>
																	<#else>
																		<input type="button" name="goback" value="Go back" onclick="history.go(-2)" />
																	</#if>
																</#if>
																<div id="syslog_message"></div>
															</td>
														</tr>
														<script type='text/javascript'>
															ACS.setupFromCalendar(null,{
																field: "fromDate",
																button: "fromDate_img"
															});
															ACS.setupFromCalendar(null,{
																field: "toDate",
																button: "toDate_img"
															});
														</script>