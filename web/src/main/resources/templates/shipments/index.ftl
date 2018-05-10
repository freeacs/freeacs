							<@macros.form upload=true>
								<fieldset>
									<legend>${STAGING_SHIP_ADD}</legend>
									<table id="input">
										<tr>
											<th align="right">${STAGING_PROD}:</th>
											<td>
												<@macros.dropdown list=unittypes default=STAGING_PROD_SELECT width="200px" onchange="if(document.getElementsByName('profile')[0]!=null){document.getElementsByName('profile')[0].selectedIndex=0;}processForm('form1');" />
											</td>
										</tr>
										<#if unittypes.selected??>
										<tr>
											<th align="right">${STAGING_PROV}:</th>
											<td>
												<@macros.dropdown list=profiles default=STAGING_PROV_SELECT onchange="if(document.getElementsByName('shipment')[0]!=null){document.getElementsByName('shipment')[0].selectedIndex=0;}processForm('form1');" />
											</td>
										</tr>
										<#if profiles.selected??>
										<tr>
											<th align="right">${STAGING_SHIP}:</th>
											<td>
												<select style="width:200px" name="shipment" onchange="processForm('form1');">
													<option value=".">${STAGING_SHIP_CREATE}</option>
													<#list shipments.items as shipment>
													<option <#if shipments.selected?? && (shipments.selected.name=shipment.name || lastindex(shipments.selected.name,"/")=shipment.name)>selected="selected"</#if> value="<#if shipment.canceled>canceled/</#if>${shipment.name}">${firstindex(shipment.name,":")}<#if shipment.canceled> (Cancelled)</#if></option>
													</#list>
												</select>
											</td>
										</tr>
										<#if !shipments.selected??>
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
													<input style="margin-top:20px" type="submit" name="formsubmit" value="Add new shipment" />
												</td>
											</tr>
										<#else>
											<tr>
												<td colspan="2" align="right">
													<input style="margin-top:20px" type="submit" name="cancelshipment" value="Cancel shipment" /><#if errors?? && (errors?size=0)><button style="margin-top:20px" type="submit" name="formsubmit" value="Confirm shipment">Confirm shipment</button></#if>
												</td>
											</tr>
										</#if>
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
								<#if units??>
								<fieldset>
									<legend>Details for ${STAGING_SHIP}: ${units.name} (${units.listsize} units)</legend>
									<#if (units.list?size>0)>
									<table>
										<tr>
											<th align="left">Unit Id</th>
											<th align="left">MAC</th>
											<th align="left">Status</th>
											<th align="left">Staged-Tms</th>
											<th align="left">Registered-Tms</th>
										</tr>
										<#list units.list as unit>
										<tr>
											<td style="padding-right:10px">
												<a href="${URL_MAP.UNIT}&unit=${unit.unitId?url}">${unit.unitId}</a>
											</td>
											<td style="padding-right:10px">${unit.serialNumber!}</td>
											<td style="padding-right:10px">${unit.status!}</td>
											<td style="padding-right:10px">${unit.stagedTms!}</td>
											<td style="padding-right:10px">${unit.registeredTms!}</td>
											<#if unit.status?contains("NOT CONNECTED")>
											<td style="padding-right:10px">
												<a href="${URL_MAP.STAGINGSHIPMENTS}&shipment=<#if shipment??>${shipment?url}</#if>&unittype=<#if unittype??>${unittype?url}</#if>&profile=<#if profile??>${profile?url}</#if>&unit=<#if unit.unitId??>${unit.unitId?url}</#if>&mac=${unit.serialNumber}&formsubmit=cancelunit">Cancel shipment (return unit to pool)</a>
											</td>
											</#if>
										</tr>
										</#list>
									</table>
									<#else>
									No units was found for this ${STAGING_SHIP}
									</#if>
								</fieldset>
								</#if>
								<#include "/staging/errorstable.ftl">
								<#if confirmshipment??>
								<fieldset>
									<legend>Found ${confirmshipment.units?size} units</legend>
									<#list confirmshipment.found as unit>
									<p>${unit.mac}: ${unit.unit.id}</p>
									</#list>
								</fieldset>
								</#if>
								<#include "/staging/shipmentstable.ftl">
							</@macros.form>