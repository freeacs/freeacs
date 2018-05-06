<#if advancedView.boolean>
	<#if swList??>
		<tr>
			<th align="left">Software version</th>
			<td>
				<@macros.dropdown list=swList callMethodForKey="" default="Choose software" onchange="" class="submitonchange" width="180px" /> 
			</td>
		</tr>
	</#if>
</#if>