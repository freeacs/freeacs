<#if !map??>
	<#if syslogSupported=true && entry.profileName?? && entry.unittypeName??>
		<#assign map = getprofilebyname(entry.unittypeName,entry.profileName)>
	<#else>
		<#if syslogSupported=true && entry.unittypeName??>
			<#assign map = getprofilebyname(entry.unittypeName,"")>
		<#else>
			<#if syslogSupported=false && entry.profileId?? && entry.unittypeId??>
				<#assign map = getprofilebyid(entry.unittypeId,entry.profileId)>
			<#else>
				<#if syslogSupported=false && entry.unittypeId??>
					<#assign map = getprofilebyid(entry.unittypeId,"")>
				</#if>
			</#if>
		</#if>
	</#if>
</#if>