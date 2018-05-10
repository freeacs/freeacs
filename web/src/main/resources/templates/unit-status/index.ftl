<@macros.form>
  	<table cellspacing="0" cellpadding="0">
  		<tr>
  			<#if currentEnabled>
	 			<#include "/unit-status/_current.ftl" />
  			<#else>
	  			<#include "/unit-status/_history.ftl" />
    		</#if>
  		</tr>
  	</table>
</@macros.form>