<div style="float:left;">	
	<@macros.form onsubmit="return validateFields(['new_modelname','new_vendor','new_matcherid']);">
		<table cellspacing="0" cellpadding="0">
			<tr>
				<td>
					<div>
						<fieldset>
							<legend>Create Unit Type</legend>
							<table style="height:145px" id="input">
								<tr>
									<th align="right">Protocol:</th>
									<td>
										<select name="new_protocol" size="1" onchange="processForm('form1')">
											<#list protocols.items as pr><option <#if protocols.selected?? && protocols.selected=pr>selected="selected"</#if>>${pr}</option></#list>
										</select>
									</td>
								</tr>
								<#if (unittypesInProtocol.items?size>0)>
								<tr>
									<th align="right">
										<div>
											Copy parameters from:
										</div>
									</th>
									<td>
										<@macros.dropdown list=unittypesInProtocol default="Copy parameters from" />
									</td>
								</tr>
								</#if>
								<tr>
									<th align="right">Model name:</th>
									<td><input name="new_modelname" type="text" value="<#if modelname??>${modelname}</#if>" /></td>
								</tr>
								<#if protocols.selected?? && protocols.selected="OPP">
								<tr>
									<th align="right">Matcher Id:</th>
									<td><input name="new_matcherid" type="text" value="<#if matcherid??>${matcherid}</#if>"/></td>
								</tr>
								</#if>
								<tr>
									<th align="right">Vendor:</th>
									<td><input name="new_vendor" type="text" value="<#if vendor??>${vendor}</#if>"/></td>
								</tr>
								<tr>
									<th align="right">Description:</th>
									<td><input name="new_description" type="text" value="<#if description??>${description}</#if>" /></td>
								</tr>
								<tr>
									<td>&nbsp;</td>
									<td align="right">
										<input name="formsubmit" value="Create" type="submit" />
									</td>
								</tr>
							</table>
						</fieldset>
					</div>
				</td>
			</tr>
		</table>
	</@macros.form>
</div>