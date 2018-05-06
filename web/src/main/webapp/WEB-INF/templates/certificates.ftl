<form name="form1" method="post" action="${URL_MAP.CERTIFICATES}" class="unit" accept-charset="ISO-8859-1" enctype="multipart/form-data">
	<input type="hidden" name="async" value="true" />
	<input type="hidden" name="header" value="true" />
	<#if async??><input type="hidden" name="async" value="${async?string}" /><input type="hidden" name="header" value="true" /></#if>
	<fieldset>
		<legend>Manage Certificates</legend>
		<table>
			<tr>
				<th align="left">Certificate:</th>
				<td>
					<input name="certificate" type="file" size="40" class="file" style="width:500px;" />
				</td>
				<td>
					<span style="color:red;margin-left:5px;"><#if errors??>${errors.certificate!}</#if></span>
				</td>
			</tr>
			<tr>
				<td colspan="2">&nbsp;</td>
			</tr>
			<tr>
				<td align="right" colspan="2">
					<input type="submit" name="formsubmit" value="Add certificate" />
				</td>
			</tr>
		</table>
		<span style="color:green;font-weight:bold;">${info!}</span>
		<span style="color:red;font-weight:bold;">${error!}</span>
	</fieldset>
	<fieldset>
		<legend>Certificates</legend>
		<#if certificates?? && (certificates?size>0)>
		<table class="syslog tablesorter">
			<thead>
				<tr>
					<th align="left">Functionality</th>
					<th align="left">Type</th>
					<th align="left">Comment</th>
	                <th align="left">Issued to</th>
					<th align="left">Status</th>
					<th align="left">Delete</th>
				</tr>
			</thead>
			<tbody>
				<#list certificates as cert>
				<tr>
					<td>
						<#if cert.certType??>
							${cert.certType}
						<#else>
							N/A
						</#if>
					</td>
					<td>
						<#if cert.trial>
							Trial
						<#else>
							Production
						</#if>
					</td>
					<td>
						<#if cert.trialType??>
							<#if cert.trialType=="Count" && cert.maxCount??>
								Limited to provision ${cert.maxCount?string} units only.
							</#if>
							<#if cert.trialType=="Days" && cert.dateLimit??>
								Valid until ${cert.dateLimit?string("yyyy-MM-dd")}
							</#if>
						<#else>
							&nbsp;
						</#if>
					</td>
	                <td>${cert.issuedTo?default("&nbsp;")}</td>
					<td>
						<#if ismodulevalid(cert.id)>
							<img src="images/ok.png" title="Valid" height="30px" alt="ok" />
						<#else>
							<img src="images/err.png" title="Invalid" height="30px" alt="error" />
						</#if>
					</td>
					<td>
						<a href="${URL_MAP.CERTIFICATES}&id=${cert.id}&action=delete<#if async??>&async=true&header=true</#if>" onclick="return processDelete('delete certificate ${cert.name}');">Delete</a>
					</td>
				</tr>
				</#list>
			</tbody>
		</table>
		<#else>
		There is no certificates
		</#if>
	</fieldset>
</form>