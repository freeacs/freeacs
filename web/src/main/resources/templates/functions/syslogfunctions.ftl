<#function getprofile entry>
	<#include "/functions/syslog_getunittypeprofile.ftl">
	<#if map??>
		<#return map.profile>
	</#if>
</#function>

<#function getunittype entry>
	<#include "/functions/syslog_getunittypeprofile.ftl">
	<#if map??>
		<#return map.unittype>
	</#if>
</#function>