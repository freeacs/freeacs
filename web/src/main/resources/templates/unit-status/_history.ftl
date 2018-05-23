<#import "/unit-status/chart.ftl" as unitmacros>
<script src="javascript/acs.module.unit.history.js" type="text/javascript"></script>
<td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
<td valign="top" style="width:800px;">
	<div id="tabs">
    	<ul>
    		<#if showVoip><li><a href="#voipDiv">Voip</a></li></#if>
    		<#if showHardware><li><a href="#hardwareDiv">Hardware</a></li></#if>
    		<li><a href="#syslogDiv">Syslog</a></li>
    	</ul>
    	<#if showVoip>
	    	<div id="voipDiv" style="width:1280px;min-height:400px;">
	    	    <@unitmacros.unitreportgraph type="Voip" methods=methodsForVoip />
	    	</div>
	    </#if>
	    <#if showHardware>
	    	<div id="hardwareDiv" style="width:1280px;min-height:400px">
	    		<@unitmacros.unitreportgraph type="Hardware" methods=methodsForHardware />
	    	</div>
	    </#if>
    	<div id="syslogDiv" style="width:1280px;min-height:400px">
    		<@unitmacros.unitreportgraph type="Syslog" methods=methodsForSyslog />
    	</div>
    </div>
    <script>
         ACS.unit.history.initSettings({
			selectedTab: ${selectedTab?default(0)},
			unitId: '${info.unit.id}',
			pageId: 'unit-dashboard',
			showHardware: ${showHardware?string},
			showVoip: ${showVoip?string}
         });
   </script>
</td>