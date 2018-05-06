							<@macros.form id="input" autocomplete="off">
							  <fieldset> 
							  <legend>${STAGING_PROV_ADD}</legend>
							  <table>
							    <tbody>
							      <tr>
									<td valign="top">
										<fieldset style="height:110px;margin-left:10px;">
										<legend>Basic information</legend>
											<table>
											  <tbody>
											    <tr>
											      <th align="left">${STAGING_PROD}:</th>
											      <td>
												      <@macros.dropdown list=unittypes default="Select ${STAGING_PROD}" onchange="deselect('profile');processForm('form1')" />
											      </td>
											    </tr>
											    <#if unittypes.selected??>
											    <tr>
													<th align="left">${STAGING_PROV}:</option>
													<td>
														<@macros.dropdown list=profiles default=providerDefaultOption+STAGING_PROV onchange="deselect('fromsoftware');deselect('provider_protocol');clearinputs(['provider_wsurl','provider_wspass','provider_email','provider_unittype','provider_profile','provider_serialnumber']);processForm('form1');" />
													</td>
												</tr>
												<#if !profiles.selected??>
												<tr>
													<th align="right">${STAGING_PROV}&nbsp;name:</th>
													<td><input style="width:200px" type="text" name="providername" value="${providername!}" style="width:200px" /></td>
												</tr>
												</#if>
												</#if>
											  </tbody>
											</table>
										</fieldset>
										<#if unittypes.selected??>
										<fieldset style="margin-left:10px;">
											<legend>Upgrade job</legend>
												<table>
												<tr>
													<th align="left">From&nbsp;software:</th>
													<td>
														<@macros.dropdown list=fromsoftware callMethodForKey="version" default="Any software" />
													</td>
												</tr>
												<tr>
													<th align="left">To&nbsp;software:</th>
													<td>
														<#assign hiddenoption="">
														<#if fromsoftware.selected??>
															<#assign hiddenoption=fromsoftware.selected.version>
														</#if>
														<@macros.dropdown list=tosoftware not=[hiddenoption] callMethodForKey="version" default="Select software" onchange="" />
													</td>
												</tr>
												<#if profiles.selected?? && PROFILES_LIMITED=false>
												<tr>
													<td colspan="2">&nbsp;</td>
												</tr>
												<tr>
													<td colspan="2" align="right"><button onclick="return validateFields(['tosoftware','providername']);" type="submit" name="formsubmit" value="Add new upgrade job">Add new upgrade job for provider</button></td>
												</tr>
												</#if>
											</table>
										</fieldset>
										</#if>
									</td>
									<#if unittypes.selected??>
									<td valign="top">
										<fieldset style="height:220px;margin-left:10px;">
										<legend>Shipment registration</legend>
											<table>
											  <tbody>
											    <tr>
											      <th align="left">Web&nbsp;Service&nbsp;Url:</th>
											      <td><input name="provider_wsurl" value="${provider_wsurl!}" style="width: 200px;"></td>
											    </tr>
											    <tr>
											      <th align="left">Web&nbsp;Service&nbsp;User:</th>
											      <td><input name="provider_wsuser" value="${provider_wsuser!}" style="width: 200px;"></td>
											    </tr>
											    <tr>
											      <th align="left">Web&nbsp;Service&nbsp;Pass:</th>
											      <td><input name="provider_wspass" value="${provider_wspass!}" style="width: 200px;" type="password"></td>
											    </tr>
											    <tr>
											      <td colspan="2" align="left">and/or</td>
											    </tr>
											    <tr>
											      <th align="left">Email&nbsp;addresses:</th>
											      <td><input name="provider_email" value="${provider_email!}" style="width: 200px;"></td>
											    </tr>
											  </tbody>
											</table>
										</fieldset>
									</td>
									<td valign="top">
										<fieldset style="height:220px;margin-left:10px;">
										<legend>Target server information</legend>
											<table>
											  <tbody>
											    <tr>
											      <th align="left">Unit&nbsp;Type:</th>
											      <td><input name="provider_unittype" value="${provider_unittype!}" style="width: 200px;"></td>
											    </tr>
											    <tr>
											      <th align="left">Profile:</th>
											      <td><input name="provider_profile" value="${provider_profile!}" style="width: 200px;"></td>
											    </tr>
											    <tr>
											      <th align="left">S/N&nbsp;Parameter&nbsp;name:</th>
											      <td><input name="provider_serialnumber" value="${provider_serialnumber!}" style="width: 200px;"></td>
											    </tr>
											    <tr>
											      <th align="left">Secret&nbsp;Parameter&nbsp;name:</th>
											      <td><input name="provider_secret" value="${provider_secret!}" style="width: 200px;"></td>
											    </tr>
											    <tr>
											      <th align="left">Protocol:</th>
											      <td>
												      <select name="provider_protocol" style="width: 200px;">
												      	<option value="">Select protocol</option>
												      	<option <#if provider_protocol?? && provider_protocol="OPP">selected="selected"</#if>>OPP</option>
														<option <#if provider_protocol?? && provider_protocol="TR-069">selected="selected"</#if>>TR-069</option>
												      </select>
											      </td>
											    </tr>
											  </tbody>
											</table>
										</fieldset>
									</td>
									</#if>
							      </tr>
							      <tr>
							      	<td colspan=3" align="right">
						     		<#if profiles.selected?? && PROFILES_LIMITED=false>
											<input type="submit" name="formsubmit" value="Delete provider" onclick="return confirm('Do you want to delete the provider ${profiles.selected.name}?');" />
											<input type="submit" name="formsubmit" value="Update provider" />
									<#else><#if unittypes.selected??>
											<button style="margin-top:20px;" type="submit" name="formsubmit" value="Add new provider"><#if !profiles.selected??>Add new provider<#else>Add new upgrade job for provider</#if></button>
									</#if></#if>
							      	</td>
							   	  </tr>
							   	  <#if response??>
									<tr>
										<td colspan="3" align="left" <#if response?starts_with("Success")>style="color:green"<#else>style="color:red"</#if>>
											${response}
										</td>
									</tr>
								  </#if>
							    </tbody>
							  </table>
							  </fieldset>
							  
							  <#include "/staging/errorstable.ftl">
								
							  <#if profiles.selected??>
									<#if jobs??>
										<fieldset>
											<legend>Upgrade jobs for provider: ${profiles.selected.name}</legend>
											<#if profiles.selected.name!="Default">
											<#if (jobs?size>0)>
											<table>
												<tr>
													<th align="left">
														Name
													</th>
													<th align="left">
														From
													</th>
													<th align="left">
														To
													</th>
													<th align="center" colspan="2">
														Success
													</th>
													<th>&nbsp;&nbsp;</th>
													<th align="center" colspan="2">
														Failed
													</th>
													<th>&nbsp;&nbsp;</th>
													<th align="center">
														Total
													</th>
												</tr>
												<#list jobs as job>
												<#assign ok = (job.completedNoFailures+job.completedHadFailures)>
												<#assign processed = (job.unconfirmedFailed+ok)>
												<#assign failed = (job.confirmedFailed)>
												<#assign total = (groupsize(job.group.id)+ok)>
												<tr>
													<td style="padding-right:10px">
														<a href="${URL_MAP.JOB}&job=${job.name?url}">${job.name}</a>
													</td>
													<td style="padding-right:30px">
														<#if job.software??>${job.software.version}<#else>n/a</#if>
													</td>
													<td align="right">
														<#if (total>0)>${(processed/total*100)?string("0.00")}<#else>0.00</#if>%
													</td>
													<td align="right">
														${processed}
													</td>
													<td>&nbsp;&nbsp;</td>
													<td align="right">
														 <#if (total>0)>${(failed/total*100)?string("0.00")}<#else>0.00</#if>%
													</td>
													<td align="right">
														 ${failed}
													</td>
													<td>&nbsp;&nbsp;</td>
													<td align="right">
														 ${total}
													</td>
												</tr>
												</#list>
											</table>
											<#else>
											No jobs was found for this provider
											</#if>
											<#else>
											Jobs is not allowed
											</#if>
										</fieldset>
									</#if>
									<#include "/staging/shipmentstable.ftl" >
							  </#if>
							</@macros.form>