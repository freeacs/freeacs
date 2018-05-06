								<#if shipments??>
								<fieldset>
									<legend><#if profiles.selected="Default">(Canceled) </#if>Previous ${STAGING_SHIPS} for ${STAGING_PROV}: ${profiles.selected.name}</legend>
									<#if (shipments.items?size>0)>
									<table>
										<tr>
											<th align="left">
												Name
											</th>
										</tr>
										<#assign count=0>
										<#list shipments.items as shipment>
										<#if profiles.selected=="Default" || !shipment.canceled>
										<#assign count=count+1>
										<tr>
											<td>
												<a style="margin-right:30px" href="${URL_MAP.STAGINGSHIPMENTS}&shipment=${shipment.name?url}&unittype=${unittypes.selected.name?url}&profile=${profiles.selected.name?url}">${firstindex(shipment.name,":")}</a>
											</td>
										</tr>
										</#if>
										</#list>
										<#if count=0>
										<tr>
											<td>
												No valid ${STAGING_SHIPS} was found
											</td>
										</tr>
										</#if>
									</table>
									<#else>
									No ${STAGING_SHIPS} was found for this ${STAGING_PROV}
									</#if>
								</fieldset>
								</#if>