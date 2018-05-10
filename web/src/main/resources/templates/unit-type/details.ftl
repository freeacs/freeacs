<@macros.form>
	<table cellspacing="0" cellpadding="0">
		<tr>
			<td>
				<div>
					<fieldset>
						<legend>Unit Type configuration</legend>
						<table id="input">
							<tr>
								<th align="right">Protocol:</th>
								<td>
									<select name="protocol" size="1">
										<#list protocols.items as pr>
											<option <#if protocols.selected?? && protocols.selected=pr>selected="selected"</#if>>${pr}</option>
										</#list>
									</select>
								</td>
							</tr>
							<#if (protocols.selected?? && protocols.selected="OPP") || (!protocols.selected?? && unittype.protocol?? && unittype.protocol="OPP")>
							<tr>
								<th align="right">Matcher Id:</th>
								<td><input name="matcherid" type="text" value="${unittype.matcherId!}"/></td>
							</tr>
							</#if>
							<tr>
								<th align="right">Vendor:</th>
								<td><input name="vendor" type="text" value="${unittype.vendor!}" /></td>
							</tr>
							<tr>
								<th align="right">Description:</th>

								<td><input name="description" type="text" value="${unittype.description!}" /></td>
							</tr>
							<tr>
								<td>&nbsp;</td>
								<td align="right">
									<input name="formsubmit" value="Delete" type="submit" onclick="return processDelete('delete the unittype');" />
									<input name="formsubmit" value="Update" type="submit" onclick="return validateFields(['vendor']);" />
								</td>
							</tr>

						</table>
					</fieldset>
				</div>
			</td>
		</tr>
	</table>
	<fieldset id="parameters">
		<legend>Parameters</legend>
		<table id="actions" class="center" style="width:100%">
			<tr>
				<td style="width:180px">
					Name:
					<input name="filterstring" type="text" onkeyup="TABLETREE.filterParameters()" size="9" value="${string}"/>
				</td>
				<td style="width:180px">
					Flag:
					<select name="filterflag" size="1" onchange="TABLETREE.filterParameters();">
						<#list flags.items as flag>
						<option<#if flags.selected==flag> selected="selected"</#if>>${flag}</option>
						</#list>
					</select>
				</td>
				<td align="right" colspan="2">
					<input name="formsubmit" value="Update parameters" type="submit" />
				</td>
			</tr>
		</table>
		<table class="parameter" id="results">
			<tr>
				<th align="left">
					<a href='#' onclick='collapse();'><img src='images/minus.gif' id='collapseimage' alt='minus' title='Click here to collapse/expand all' class='tiptip' border='0'/></a>Name
				</th>
				<th>Flags</th>
				<th>
					<span title='Click here to check all' class='tiptip' onclick="check('delete')">Delete</span>
				</th>
			</tr>
			<#list params as param>
			<#if param.unittypeParameter??>
			<tr id="${param.name}">
				<td class="unittype">
					<span style="margin-left:${param.tab}px"><a onclick="return ModalUTP.show('${param.name}','unittype','${unittype.name}','Unit Type Parameter','${URL_MAP.UNITTYPEPARAMETERS}&unittype=${unittype.name?url}&utp=${param.unittypeParameter.id}', 700, 500);" href="javascript:void();">${param.shortName}</a><img <#if param.unittypeParameter.values?? && (param.unittypeParameter.values.values?size>0)>style="margin-left:10px"<#else>style="display:none;margin-left:10px"</#if> src="images/cantedit.gif" alt="locked" id="lock::${param.name}" title="This parameter has set values" /></span>
				</td>
				<td class="flagcell">
					<input name="update::${param.name}" type="text" value="${param.unittypeParameter.flag.flag}" onchange="validateFlag(this);" />
					<input name="update::${param.name}::Cache" type="hidden" value="${param.unittypeParameter.flag.flag}" />
				</td>
				<td align="center">
					<input name="delete::${param.name}" type="checkbox" onclick="toggle('update::${param.name}',this);" />
				</td>
			</tr>
			<#else>
			<tr id="${param.name}">
				<td>
					<span style="margin-left:${param.tab-4}px"><img src="images/minus.gif" alt="minus" onclick="TABLETREE.collapse('${param.name}')" />${param.shortName}</span>
				</td>
				<td>&nbsp;</td><td>&nbsp;</td>
			</tr>
			</#if>
			</#list>
			<tr><td colspan="3">&nbsp;</td></tr>
			<tr>
				<td colspan="3" align="right">

					<input name="formsubmit" value="Update parameters" type="submit" />
				</td>
			</tr>
		</table>
	</fieldset>
</@macros.form>