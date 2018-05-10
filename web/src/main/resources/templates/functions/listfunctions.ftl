<#function isMapSelected dropdown method>
	<#if dropdown.items??>
		<#list dropdown.items as item>
			<#if dropdown.selected?? && dropdown.selected!="." && (((method?length>0) && item[method]=dropdown.selected) || item=dropdown.selected)>
				<#return true>
			</#if>
		</#list>
	</#if>
	<#return false>
</#function>

<#function isListSelected dropdownlist value method>
	<#list dropdownlist as item>
		<#if value!="." && (((method?length>0) && item[method]=value) || item=value)>
			<#return true>
		</#if>
	</#list>
	<#return false>
</#function>