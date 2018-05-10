							<@macros.form upload=true>
								<input type="hidden" name="page" value="return" />
								<fieldset>
									<legend>Return units</legend>
									<table id="input">
										<tr>
											<th align="right">${STAGING_PROD}:</th>
											<td>
												<select style="width:200px" name="unittype" onchange="if(document.getElementsByName('profile')[0]!=null){document.getElementsByName('profile')[0].selectedIndex=0;}processForm('form1');">
													<option value=".">${STAGING_PROD_SELECT}</option>
													<#list unittypes.items as unittype>
													<option <#if unittypes.selected?? && unittype.name=unittypes.selected.name>selected="selected"</#if>>${unittype.name}</option>
													</#list>
												</select>
											</td>
										</tr>
										<#if unittypes.selected??>
										<tr>
											<th align="right">${STAGING_PROV}:</th>
											<td>
												<select style="width:200px" name="profile" onchange="if(document.getElementsByName('shipment')[0]!=null){document.getElementsByName('shipment')[0].selectedIndex=0;}processForm('form1');">
													<option value=".">${STAGING_PROV_SELECT}</option>
													<#list profiles.items as profile>
													<option <#if profiles.selected?? && profile.name=profiles.selected.name>selected="selected"</#if> value="${profile.name}">${profile.name}</option>
													</#list>
												</select>
											</td>
										</tr>
										<#if profiles.selected??>
										<#if !confirmshipment??>
											<tr>
												<th align="right">File w/MAC:</th>
												<td><input type="file" name="unitlist" /></td>
											</tr>
											<tr>
												<td></td>
												<td><b>or</b></td>
											</tr>
											<tr>
												<th align="right">MAC:</th>
												<td><input type="text" name="mac"></td>
											</tr>
											<tr>
												<td colspan="2" align="right">
													<input style="margin-top:20px" type="submit" name="formsubmit" value="Return units" />
												</td>
											</tr>
										<#else>
											<tr>
												<td colspan="2" align="right">
													<input style="margin-top:20px" type="submit" name="cancelshipment" value="Cancel return" /><#if errors?? && (errors?size=0)><button style="margin-top:20px" type="submit" name="formsubmit" value="Confirm return">Confirm return</button></#if>
												</td>
											</tr>
										</#if>
										</#if>
										</#if>
										<#if response??>
										<tr>
											<td colspan="2" align="right" <#if response?starts_with("Success")>style="color:green"<#else>style="color:red"</#if>>
												${response}
											</td>
										</tr>
										</#if>
									</table>
								</fieldset>
								<#include "/staging/errorstable.ftl">
								<#if confirmshipment??>
								<fieldset>
									<legend>Found ${confirmshipment.units?size} units</legend>
									<#list confirmshipment.found as unit>
									<p>${unit.mac}: ${unit.unit.id}</p>
									</#list>
								</fieldset>
								</#if>
							</@macros.form>