														<tr>
															<td><b>Unit Id:</b></td>
															<td>
																<input name="unit" class="submitonchange<#if !unit??> searchField</#if>" id="units" type="text" value="<#if unit??>${unit!}<#else>search for unit</#if>" autocomplete="off" style="width:300px" />
															</td>
														</tr>
														<#if unit??>
														<tr>
															<td><b>Software:</b></td>
															<td>
																<@macros.dropdown list=softwares callMethodForKey="version" default="Select software" class="submitonchange" width="300px"/>
															</td>
														</tr>
														<#if softwares.selected??>
														<tr>
															<td colspan="2" align="right">
																<input name="formsubmit" value="Upgrade" type="submit" onclick="return validateFields(['unittype','type','firmware','unit']);" />
															</td>
														</tr>
														<tr>
															<td colspan="2" align="right" />
														</tr>
														</#if>
														</#if>
														<script>
															$(document).ready(function(){
																var field = $("#units");
																field.autocomplete({
																	source: "${URL_MAP.SEARCH}&unittype=${unittypes.selected.name}",
																	minLength: 3,
																	select: function(event,ui){
																		field.closest("form").submit();
																	}
																});
															});
														</script>