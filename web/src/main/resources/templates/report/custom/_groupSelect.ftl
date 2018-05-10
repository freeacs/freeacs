<#if advancedView.boolean && groupList??>
	<tr>
		<th align="left">Group</th>
		<td>
			<@macros.dropdown list=groupList default="Choose group" disabled=realtime onchange="" class="submitonchange" width="180px" />
		</td>
	</tr>
</#if>