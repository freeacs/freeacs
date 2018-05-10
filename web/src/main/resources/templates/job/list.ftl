<@macros.form>
	<!--<script type='text/javascript'>document.onkeypress=KeyPressed;</script>-->
	<input name="page" type="hidden" value="jobs" />
	<#if params?? && (params?size>0)>
	<fieldset id="parameters">
		<legend>Jobs</legend>
		<table class="parameter" id="results">
			<tr>
				<th align="left" colspan="9">Name: <input id="filterstring" name="filterstring" onkeyup="TABLETREE.filterJobs();" /></th>
			</tr>
			<tr>
				<th align="left">
					<a href='#' onclick='collapse();'><img src='images/minus.gif' id='collapseimage' alt='minus' title="Click here to collapse/expand all" class="tiptip" border='0'/></a>Name
				</th>
				<th align="left">Id</th>
				<th align="left">Type</th>
				<th align="left">Group</th>
				<th align="center">Status</th>
				<th align="center">Started</th>
				<th align="center" title="Unconfirmed failed" class="tiptip">UF</th>
				<th align="center" title="Confirmed failed" class="tiptip">CF</th>
				<th align="center">OK</th>
			</tr>
			<#list params as param> 
			<#if param.jobParent=false>
			<tr id="${param.name}">
				<td>
					<span style="margin-left:${param.tab}px"><a href="?page=job&amp;job=${param.job.name?url}">${param.job.name}</a></span>
				</td>
				<td>${param.job.id}</td>
				<td>${param.job.flags.type}</td>
				<td><a href="?page=group&amp;group=${param.job.group.name}">${param.job.group.name}</a></td>
				<td>${param.job.status}</td>
				<td><#if param.job.startTimestamp??>${param.job.startTimestamp?string('MMM.dd HH:mm')}<#else>N/A</#if></td>
				<td>${param.job.unconfirmedFailed}</td>
				<td>${param.job.confirmedFailed}</td>
				<td>${param.job.completedHadFailures+param.job.completedNoFailures}</td>
			</tr>
			<#else>
			<tr id="${param.name}">
				<td>
					<span style="margin-left:${param.tab-4}px"><img alt="minus.gif" src="images/minus.gif" onclick="javascript:TABLETREE.collapse('${param.name}')"><a href="?page=job&amp;job=${param.job.name?url}">${param.job.name}</a>
				</td>
				<td>${param.job.id}</td>
				<td>${param.job.flags.type}</td>
				<td><a href="?page=group&amp;group=${param.job.group.name}">${param.job.group.name}</a></td>
				<td>${param.job.status}</td>
				<td><#if param.job.startTimestamp??>${param.job.startTimestamp?string('MMM.dd HH:mm')}<#else>N/A</#if></td>
				<td>${param.job.unconfirmedFailed}</td>
				<td>${param.job.confirmedFailed}</td>
				<td>${param.job.completedHadFailures+param.job.completedNoFailures}</td>
			</tr>
			</#if>
			</#list>
		</table>
	</fieldset>
	<#else>
	<#if params??>
	<fieldset id="parameters">
		<legend>Jobs</legend>
		No jobs found for this selection
	</fieldset>
	</#if>
	</#if>
</@macros.form>