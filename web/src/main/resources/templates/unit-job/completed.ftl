					<@macros.form>	
						<input type="hidden" name="page" value="job" />
						<input type="hidden" name="job" value="${job.name}" />
						<input type="hidden" name="group" value="${job.group.name}" />
						<input type="hidden" name="unittype" value="${job.group.unittype.name}" />
						<input type="hidden" name="cmd" value="getcompletedunitjobs" />
						<#if async??><input type="hidden" name="async" value="true" /></#if>
						<fieldset>
						<legend>Completed unit jobs (<a href="?page=job&job=${job.name?url}&cmd=exportcompletedjobs">export</a>)</legend>
						<#if completedUnitJobs??>
						Limit result: <input type="text" <#if limit??>value="${limit}"</#if> name="limit" size="5" /><input type="submit" name="formsubmit" value="Refresh" />
						<table class="unitjobs">
							<tr>
								<th class="sortable favour-reverse">UnitId</th>
								<#list jobparameters as param>
								<th class="sortable favour-reverse">${lastindexof(param.parameter.unittypeParameter.name,".")}</th>
								</#list>
								<th class="sortable favour-reverse">Messages</th>
							</tr>
							<#list completedUnitJobs as unitJob>
							<tr>
								<td><a href="?page=unit&amp;unit=${unitJob.id?url}">${unitJob.id}</a></td>
								<#list jobparameters as param>
								<td>${getparamvalue(unitJob.id,param.parameter.unittypeParameter.name)}</td>
								</#list>
								<td><a href="${URL_MAP.SYSLOG}&unittype=${job.group.unittype.name?url}&amp;unit=${unitJob.id?url}&amp;cmd=auto">Syslog</a></td>
							</tr>
							</#list>
						</table>
						<#else>
						<p>There are no completed unit jobs</p>
						</#if>
						</fieldset>
					</@macros.form>