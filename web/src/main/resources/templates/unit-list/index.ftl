<script src="javascript/acs.module.unit.list.js"></script>
<@macros.form>
 	<input type="hidden" name="type" value="${type}" />
	<fieldset>
		<legend>${type} report details</legend>
		<table>
			<tr>
				<th align="right">From:</th>
				<td>
					<input value="${start}" id="start_original" type="hidden" />
					<input value="${start}" name="start" id="start" type="text" />
					<img src="images/dateIMG.jpg" alt="date" id="start_img" />
				</td>
				<td>&nbsp;</td>
				<th align="right">To:</th>
				<td>
					<input value="${end}" id="end_original" type="hidden" />
					<input value="${end}" name="end" id="end" type="text" />
					<img src="images/dateIMG.jpg" alt="date" id="end_img" />
				</td>
			</tr>
			
			<#if type="Hardware">
			<#if swVersionList??>
			<tr>
				<th align="right" valign="top" style="padding-top:7px;">Software:&nbsp;</th>
				<td valign="top">
					<#if (swVersionList.items?size>0)>
						<@macros.dropdown list=swVersionList default="All softwares" callMethodForKey="" />
					<#else>
						<#if swVersionList.selected??>
							<@macros.hidden list=swVersionList callMethodForSelected="" />
							<p style="margin-top:6px;" class="tiptip" title="Narrow down by selecting a Unit&nbsp;Type">${swVersionList.selected}</p>
						<#else>
							<p style="margin-top:6px;" class="tiptip" title="Narrow down by selecting a Unit&nbsp;Type">
							All software versions
							</p>
						</#if>
					</#if>
				</td>
			</tr>
			</#if>
			</#if>
			
			<tr>
				<td colspan="5" align="right"><input type="submit" id="updateButton" value="Update" /></td>
			</tr>
		</table>
	</fieldset>
	<#include "/unit-list/"+type+".ftl" />
 </@macros.form>