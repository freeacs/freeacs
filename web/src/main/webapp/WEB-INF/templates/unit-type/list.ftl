<div style="float:left;">
	<@macros.form name="list" fieldset="Unit Type Overview">
		<table>
			<tr>
				<th>Model</th>
				<th>Vendor</th>
				<th>Description</th>
				<th>Protocol</th>
			</tr>
			<#list unittypes as unittype>
			<tr>
				<td><a href="${urltodetails}&unittype=${unittype.name}">${unittype.name}</a></td>
				<td>${unittype.vendor!}</td>
				<td>${unittype.description!}</td>
				<td>${unittype.protocol}</td>
			</tr>
			</#list>
		</table>
	</@macros.form>
</div>