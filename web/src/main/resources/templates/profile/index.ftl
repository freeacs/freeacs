<@macros.form fieldset="Profile overview">
	<#if unittypes.selected??>
	<table>
		<tr>
			<th align="left" style="min-width:100px;">Name</th>
			<th>Parameters</th>
		</tr>
		
			<#list profiles.items as profile>
				<tr>
					<td><a href="${URL_MAP.PROFILE}&profile=${profile.name?url}&unittype=${profile.unittype.name?url}">${profile.name}</a></td>
					<td>${profile.profileParameters.profileParameters?size?string}</td>
				</tr>	
			</#list>
	</table>
	<#else>
	No Unit Type selected
	</#if>
</@macros.form>