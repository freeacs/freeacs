					<@macros.form>	
						<input type="hidden" name="page" value="job" />
						<input type="hidden" name="job" value="${job.name}" />
						<input type="hidden" name="group" value="${job.group.name}" />
						<input type="hidden" name="unittype" value="${job.group.unittype.name}" />
						<input type="hidden" name="cmd" value="getfailedunitjobs" />
						<#if async??><input type="hidden" name="async" value="true" /></#if>
						<fieldset>
						<legend>Failed unit jobs (<a href="?page=job&job=${job.name?url}&cmd=exportfailedjobs">export</a>)</legend>
						<#if unitJobs??>
						Limit result: <input type="text" <#if limit??>value="${limit}"</#if> name="limit" size="5" /><input type="submit" name="formsubmit" value="Refresh" />
						<table class="unitjobs">
							<tr>
								<th>UnitId</th>
								<th>Status</th>
								<th>Started</th>
								<th>Ended</th>
								<th>Unconfirmed</th>
								<th>Confirmed</th>
								<th>Messages</th>
							</tr>
							<#list unitJobs as unitJob>
							<tr>
								<td><a href="?page=unit&amp;unit=${unitJob.unitId?url}">${unitJob.unitId}</a></td>
								<td>${unitJob.status!}</td>
								<td>${unitJob.startTimestamp!}</td>
								<td>${unitJob.endTimestamp!}</td>
								<td align="center">${unitJob.unconfirmedFailed!}</td>
								<td align="center">${unitJob.confirmedFailed!}</td>
								<td><a href="${URL_MAP.SYSLOG}&unittype=${job.group.unittype.name?url}&amp;unit=${unitJob.unitId?url}&amp;cmd=auto">Syslog</a></td>
							</tr>
							</#list>
						</table>
						<#else>
						<p>There are no failed unit jobs</p>
						</#if>
						</fieldset>
					</@macros.form>