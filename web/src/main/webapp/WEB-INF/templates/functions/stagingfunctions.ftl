<#function contains_model unittype>
	<#if unittype?contains("-") && models?seq_contains(lastindex(unittype,"-"))>
		<#return true>
	<#else>
		<#return false>
	</#if>
</#function>

<#function notvalue map value>
	<#if !map.selected??>
		<#return true>
	<#else>
		<#if (map.selected?? && map.selected!=value)>
			<#return true>
		<#else>
			<#return false>
		</#if>
	</#if>
</#function>

<#function notdefault map list=["Default","."]>
	<#if !map?is_hash || !map.selected??>
		<#return false>
	<#else>
		<#if !list?seq_contains(map.selected)>
			<#return true>
		<#else>
			<#return false>
		</#if>
	</#if>
</#function>
