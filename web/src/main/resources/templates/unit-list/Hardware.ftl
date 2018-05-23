<table>
	<tr>
		<td valign="top">
			<fieldset>
				<legend>Memory and uptime</legend>
				<fieldset>
					<legend>Filters</legend>
					<table>
						<tr>
							<th align="right" valign="top" style="padding-top:7px;">Memory:&nbsp;</th>
							<td valign="top">
								DDR range:&nbsp;<input type="text" size="2" name="filter_ddr_low" value="${filter_ddr_low}" /> - <input type="text" size="2" name="filter_ddr_high" value="${filter_ddr_high}" />&nbsp;%
								<br />
								AND<input type="radio" onchange="ACS.submitForm()" name="filter_operand" <#if !filter_operand?? || (filter_operand?? && filter_operand="AND")>checked="checked"</#if>value="AND" /> OR<input type="radio" onchange="ACS.submitForm()" <#if filter_operand?? && filter_operand="OR">checked="checked"</#if>name="filter_operand" value="OR" />
								<br />
								OCM range:&nbsp;<input type="text" size="2" name="filter_ocm_low" value="${filter_ocm_low}" /> - <input type="text" size="2" name="filter_ocm_high" value="${filter_ocm_high}" />&nbsp;%
							</td>
						</tr>
						<tr>
							<th align="right" valign="top" style="padding-top:7px;">Uptime:&nbsp;</th>
							<td valign="top">
								<input type="text" size="5" name="filter_uptime_low" value="${filter_uptime_low}" /> - <input type="text" size="5" name="filter_uptime_high" value="${filter_uptime_high!}" />&nbsp;minutes
							</td>
						</tr>
					</table>
				</fieldset>
				<table class="memory tablesorter {sortlist: [[1,1],[2,1]]}">
					<thead>
						<tr>
							<th>Unit</th>
							<th style="padding-right:20px;">OCM</th>
							<th style="padding-right:20px;">DDR</th>
							<th style="padding-right:20px;">Uptime</th>
						</tr>
					</thead>
					<tbody>
						<#assign memEntries = 0 />
						<#list reports as record>
						<#if record.memoryRelevant && record.uptimeRelevant>
						<#assign memEntries = memEntries+1 />
						<tr title="${record.tableOfMemoryUnused}" class="tiptip">
							<td style="background-color:#ccdcb7"><a href="javascript:goToUrlAndWait('${URL_MAP.UNITSTATUS}&unit=${record.unit.id?url}<#if record.unit.profile??>&profile=${record.unit.profile.name?url}&unittype=${record.unit.profile.unittype.name?url}</#if>&start=${start}&end=${end}&selectedTab=1&current=false&history=true','Loading unit ...')">${record.unit.id}</a></td>
							<td style="background-color:#ccdcb7">${record.getHeapOcmMaxPercent()} %</td>
							<td style="background-color:#ccdcb7">${record.getHeapDdrMaxPercent()} %</td>
							<td style="background-color:#ccdcb7" align="right" class="tiptip" title="${record.getCpeUptimeAvg()!?string}" tablesorter_customkey="${record.getCpeUptimeAvg()!?string}">${record.getCpeUptimeAvgReadable()!}</td>
						</tr>
						</#if>
						</#list>
					</tbody>
				</table>
				<#if memEntries=0>
					There are no devices in memory-range
				<#else>
					<script>
						jQuery(document).ready(function(){
							$(".memory.tablesorter").tablesorter(); 
						});
					</script>
				</#if>
			</fieldset>
		</td>
		<td valign="top" style="padding-left:20px;">
			<fieldset>
				<legend>Boots</legend>
				<table class="boot tablesorter {sortlist: [[4,1]]}">
					<thead>
						<tr>
							<th>Unit</th>
							<th style="padding-right:20px;">Power</th>
							<th style="padding-right:20px;">Misc</th>
							<th style="padding-right:20px;">Prov</th>
							<th style="padding-right:20px;">Total</th>
						</tr>
					</thead>
					<tbody>
					<#assign numEntries = 0 />
					<#list reports as record>
						<#if record.bootRelevant>
						<#assign numEntries = numEntries+1 />
						<tr>
							<td style="background-color:#FFC62D"><a href="javascript:goToUrlAndWait('${URL_MAP.UNITSTATUS}&unit=${record.getUnit().getId()?url}<#if record.unit.profile??>&profile=${record.unit.profile.name?url}&unittype=${record.unit.profile.unittype.name?url}<#else>&profile=.&unittype=.</#if>&start=${start}&end=${end}&selectedTab=1&current=false&history=true','Loading unit ...')">${record.unit.id}</a></td>
							<td style="background-color:#FFC62D">${record.getBootPower()?string}</td>
							<td style="background-color:#FFC62D">${record.getBootMisc()?string}</td>
							<td style="background-color:#FFC62D">${record.getBootProv()?string}</td>
							<td style="background-color:#FFC62D">${record.getBoots()?string}</td>
						</tr>
						</#if>
					</#list>
					</tbody>
				</table>
				<#if numEntries=0>
					There are no boots
				<#else>
					<script>
						jQuery(document).ready(function(){
							$(".boot.tablesorter").tablesorter(); 
						});
					</script>
				</#if>
			</fieldset>
		</td>
	</tr>
</table>