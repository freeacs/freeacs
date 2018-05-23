								<#if UNITTYPE_DROPDOWN.selected??>
									<#if group?? && group.value??>
										<input name="${group.key}" type="hidden" value="${group.string}" />
									</#if>
									<tr>
										<th align="center">Parameter name</th>
										<th align="left">Data type</th>
										<th align="center">Operator</th>
										<th align="center">Value</th>
										<th align="center">Active</th>
									</tr>
									<#if searchables??>
										<#list searchables as searchable>
											<tr>
												<th align="right" title="${searchable.id}">
													${searchable.id}:
												</th>
												<td>
													<select name="datatype::${searchable.id}">
														<#list datatypes as datatype>
														<option <#if searchable.type=datatype>selected="selected"</#if> value="${datatype.type}">${datatype.type}</option>
														</#list>
													</select>
												</td>
												<td  align="center">
													<select name="operator::${searchable.id}">
														<#list operators as operator>
														<option <#if searchable.operator=operator>selected="selected"</#if> value="${operator.operatorSign}">${operator.operatorSign}</option>
														</#list>
													</select>
												</td>
												<td>
													<input name="${searchable.id}" type="text" value="${searchable.value!}" class="enableOrDisable" />
												</td>
												<td align="center">
													<input type="checkbox" value="true" <#if searchable.enabled>checked="checked"</#if> name="enabled::${searchable.id}" />
												</td>
											</tr>
										</#list>
									</#if>
									
									<#if groupparams??>
										<#list groupparams as param>
											<tr>
												<th align="right" title="${param.id}">
													${param.displayText}:
												</th>
												<td>
													<select name="datatype::${param.id}">
														<#list datatypes as datatype>
														<option <#if param.type=datatype>selected="selected"</#if> value="${datatype.type}">${datatype.type}</option>
														</#list>
													</select>
												</td>
												<td align="center">
													<select name="operator::${param.id}">
														<#list operators as operator>
														<option <#if param.operator=operator>selected="selected"</#if> value="${operator.operatorSign}">${operator.operatorSign}</option>
														</#list>
													</select>
												</td>
												<td>
													<input name="${param.id}" type="text" value="${param.value?default("NULL")}" class="enableOrDisable" />
												</td>
												<td align="center">
													<input type="checkbox" value="true" <#if param.enabled>checked="checked"</#if> name="enabled::${param.id}" />
												</td>
											</tr>
										</#list>
									</#if>
									
									<#if volatiles??>
										<#list volatiles as volatile>
											<tr>
												<th align="right" title="${volatile.id}">
													${volatile.displayText}:
												</th>
												<td>
													<select name="datatype::remember::${volatile.id}">
														<#list datatypes as datatype>
														<option <#if volatile.type=datatype>selected="selected"</#if> value="${datatype.type}">${datatype.type}</option>
														</#list>
													</select>
												</td>
												<td align="center">
													<select name="operator::remember::${volatile.id}">
														<#list operators as operator>
														<option <#if volatile.operator=operator>selected="selected"</#if> value="${operator.operatorSign}">${operator.operatorSign}</option>
														</#list>
													</select>
												</td>
												<td>
													<input name="remember::${volatile.id}" type="text" value="${volatile.value!}"  class="enableOrDisable" />
												</td>
												<td align="center">
													<input type="checkbox" value="true" <#if volatile.enabled>checked="checked"</#if> name="enabled::remember::${volatile.id}" />
												</td>
											</tr>
										</#list>
									</#if>
									
									<tr>
										<th align="right">
											Add new search parameter:
										</th>
										<td colspan="4">
											<input type="text" id="addparameter" value="" style="width:90%" />
										</td>
									</tr>
									
									<script type="text/x-jqote-template" id="parametertemplate">
										<![CDATA[
										<tr>
											<th align="right" title="<%= this.param %>">
												<%= this.param %>:
											</th>
											<td>
												<select name="datatype::remember::<%= this.param %>">
													<#list datatypes as datatype>
													<option value="${datatype.type}">${datatype.type}</option>
													</#list>
												</select>
											</td>
											<td align="center">
												<select name="operator::remember::<%= this.param %>">
													<#list operators as operator>
													<option value="${operator.operatorSign}">${operator.operatorSign}</option>
													</#list>
												</select>
											</td>
											<td>
												<input name="remember::<%= this.param %>" type="text" value="" class="enableOrDisable" />
											</td>
											<td align="center">
												<input type="checkbox" value="true" name="enabled::remember::<%= this.param %>" />
											</td>
										</tr>
										 ]]>
									</script>
									
									<tr id="limitrow">
										<th align="right">Limit results to:</th>
										<td colspan="4"><input name="${limit.key}" type="text" value="${limit.value}"  style="width:90%"/></td>
									</tr>
									
									<script>
										ACS.search.initSettings({
											addparameter: {
												url: "app/${URL_MAP.UNITTYPEPARAMETERS.id}/list?unittype=${UNITTYPE_DROPDOWN.selected.name}"
											}
										});
									</script>
								<#else>
									<tr>
										<td colspan="2">Please select a Unit Type to continue</td>
									</tr>
								</#if>