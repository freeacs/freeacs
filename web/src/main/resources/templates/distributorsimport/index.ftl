							<@macros.form upload=true>
								<input type="hidden" name="page" value="distributors" />
								<fieldset>
									<legend>Update ${STAGING_PROD}</legend>
									<table id="input">
										<tr>
											<th align="right">${STAGING_PROD}:</th>
											<td>
												<@macros.dropdown list=unittypes default="${STAGING_PROD_MODEL_SELECT}" />
											</td>
										</tr>
										<#if unittypes.selected??>
										<#if (softwares?size>0)>
										<tr>
											<th align="right">${SOFTWARE_VERSION}:</th>
											<td>
												<select name="versionnumber">
													<option value=".">${SOFTWARE_CHOOSE}</option>
													<#list softwares as software>
													<option <#if version?? && version=software.version>selected="selected"</#if> value="${software.version}">${software.version} (${software.name})</option>
													</#list>
												</select>
											</td>
										</tr>
										<tr>
											<th align="right">${STAGING_TAIWAN_FILE}:</th>
											<td>
												<input name="taiwan" type="file" />
											</td>
										</tr>
										</#if>
										
										<tr>
											<td colspan="2" align="right">
												<#if (softwares?size>0)>
												<button style="margin-top:20px" name="formsubmit" type="submit" onclick="return validateFields(['modelname','distributorname','versionnumber','taiwan']);" value="Add new product">Update ${STAGING_PROD}</button>
												<#else>
												No ${SOFTWARES} is defined. Do it <a href="${URL_MAP.SOFTWARE}&unittype=${unittype?url}-${distributors.selected?url}">here</a>.
												</#if>
											</td>
										</tr>
										<#if response??>
										<tr>
											<td colspan="2" align="right" <#if response?starts_with("ERROR")>style="color:red"<#else>style="color:green"</#if>>
												${response}
											</td>
										</tr>
										</#if>
										</#if>
									</table>
								</fieldset>
								<#include "/staging/errorstable.ftl">
							</@macros.form>