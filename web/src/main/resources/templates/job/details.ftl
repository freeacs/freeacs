<#include "/job/rules.ftl" />
<@macros.form>
	<input name="job" type="hidden" value="${job.name}" />
	<input name="unittype" type="hidden" value="${job.group.unittype.name}" />
	<table cellspacing="0" cellpadding="0"><tr>
		<td valign="top"><div><fieldset>
			<legend>Job details</legend>
			<table id="input">
				<tr>
					<th align="right">Id:</th>
					<td>${job.id}</td>
				</tr>
				<tr>
					<th align="right">Name:</th>
					<td><input name="name" type="hidden" value="${job.name}" />${job.name}</td>
					<td><span style="color:red;margin-left:5px;"><#if errors??>${errors.name!}</#if></span></td>
				</tr>
				<tr>
					<th align="right">Description:</th>
					<td><input name="description" type="text" value="${job.description!}" size="40" /></td>
				</tr>
				<tr>
					<th align="right">Type:</th>
					
					<td><input name="type" type="hidden" value="${job.flags.type}" /><a href="#" id="type">${job.flags.type}</a></td>
				</tr>
				<tr>
					<th align="right">Service Window:</th>
					<td><input name="servicewindow" type="hidden" value="${job.flags.serviceWindow}" /><a href="#" id="servicewindow">${job.flags.serviceWindow}</a></td>
				</tr>
				<tr>
					<th align="right">Group:</th>
					<td><input name="groupId" type="hidden" value="${job.group.id}" /><a href='?page=group&amp;group=${job.group.name?url}' id="groupId">${job.group.name}</a></td>
				</tr>
				<tr>
					<th align="right">Job dependency:</th>
					<td>
						<@macros.dropdown list=dependencies onchange="" default="No job dependency" />
						<#if dependencies.selected??>
							<a href='?page=job&amp;job=${dependencies.selected.name?url}&amp;unittype=${dependencies.selected.group.unittype.name?url}'>
								<img src='images/shortcut.gif' style='height:15px' border='0' title='Go to parent job' alt='shortcut' />
							</a>
						</#if>
					</td>
				</tr>
				<#if files??>
                    <tr>
                      	<#if requirefile>
	                      	<#if job.flags.type="SOFTWARE">
    	                    	<th align="right">Software:</th>
        	                	<#assign DEFAULT_SOFTWARE_OPTION = "Select software" />
            	          	<#else>
                	        	<th align="right">Script:</th>
                    	    	<#assign DEFAULT_SOFTWARE_OPTION = "Select script" />
                      		</#if>
                      		<td>
    	                    	<@macros.dropdown list=files callMethodForDisplay="nameAndVersion" callMethodForKey="id" default=DEFAULT_SOFTWARE_OPTION?default("Select script") defaultValue="Any" onchange="" />
	                      	</td>
	                      	<td><span style="color:red;margin-left:5px;"><#if errors??>${errors.fileId!}</#if></span></td>
                      	</#if>
                    </tr>
				</#if>
				<tr>
					<th align="right">Stop rules:</th>
					<td>
						<input name="stoprules" type="text" size="40" value="${job.stopRulesSerialized!}" />
						<a id="failurerule" href="#" onclick="ShowRuleBox(this,170,['amount','status']);return false;"><b>+</b></a>
					</td>
				</tr>
				<tr>
					<th align="right" title="Timeout in seconds" class="tiptip">Unconfirmed timeout:</th>
					<td><input name="unconfirmedtimeout" type="text" value="${job.unconfirmedTimeout!}" size="40" /></td>
					<td><span style="color:red;margin-left:5px;"><#if errors??>${errors.unconfirmedtimeout!}</#if></span></td>
				</tr>
				<tr>
					<th align="right">Repeat count:</th>
					<td><input name="repeatcount" type="text" value="${job.repeatCount!}" size="40" /></td>
					<td><span style="color:red;margin-left:5px;"><#if errors??>${errors.repeatcount!}</#if></span></td>
				</tr>
				<tr>
					<th align="right">Repeat interval:</th>
					<td><input name="repeatinterval" type="text" value="${job.repeatInterval!}" size="40" /></td>
					<td><span style="color:red;margin-left:5px;"><#if errors??>${errors.repeatinterval!}</#if></span></td>
				</tr>
				<tr>
					<td>&nbsp;</td>
					<td align="right">
						<#if haschildren=true>
						<span title="Delete childrens first" style="color:red;margin-right: 15px;">Cannot delete</span><#else>
						<#if ready(job.status) || finished(job.status)>
						<input name="formsubmit" value="Delete"  type="submit" onclick="return processDelete('delete the job');" <#if finished(job.status)>style="margin-right: 15px;"</#if>/>
						</#if>
						</#if>
						<#if !finished(job.status)>
						<input name="formsubmit" value="Update" type="submit"  style="margin-right: 15px;"/>
						</#if>
					</td>
				</tr>
				<tr>
					<td>&nbsp;</td>
					<td><hr / style="margin-right: 15px;"></td>
				</tr>
				<tr>
					<th align="right">Status:</th>
					<td>
						<input name="status" type="hidden" value="${job.status}" />
						<span <#if (allowed(job.status)?size>0)>style='color: green'<#else>style='color: red'</#if>>${job.status}</span>
						<#list allowed(job.status) as status>
						<input name="statuschange" type="submit" value="${acronym(status)}" <#if finished(status)>onclick="return processDelete('finish the job (it can not be restarted)')"</#if> />
						</#list>
					</td>
				</tr>
				<tr><th align="right">Completed:</th><td>${job.completedNoFailures+job.completedHadFailures}</td></tr>
				<tr><th align="right">Confirmed failed:</th><td>${job.confirmedFailed}</td></tr>
				<tr><th align="right">Unconfirmed failed:</th><td>${job.unconfirmedFailed}</td></tr>
				<#if job.endTimestamp??><tr><th align="right">Ended at:</th><td>${job.endTimestamp?string('MMM.dd HH:mm')}</td></tr></#if>
				<tr>
					<td>&nbsp;</td>
					<td align="right">
						<input name="formsubmit" value="refresh" type="button" onclick="window.location='${URL_MAP.JOB}&job=${job.name}&unittype=${job.group.unittype.name}';return false;" style="margin-right: 15px;" />
					</td>
				</tr>
			</table>
		</fieldset></div></td>
		<#if (job.allChildren?size>0)>
		<td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
		<td valign="top"><div><fieldset>
			<legend>Referenced by:</legend>
			<select onchange="goToJob(this.value)" style="width:200px">
				<option>Select child job:</option>
				<#list job.allChildren as child>
				<option value="${child.name}:@:${child.unittype.name}">${child.name}</option>
				</#list>
			</select>
		</fieldset></div></td>
		</#if>
	</tr></table>
</@macros.form>
<@macros.form name="parameters">
	<input name="job" type="hidden" value="${job.name}" />
	<input name="unittype" type="hidden" value="${job.group.unittype.name}" />
	<fieldset id="parameters">
		<legend>Parameters</legend>
		<table id="actions" class="center" style="width:100%">
			<tr>
				<td style="width:190px">
					Name: <@macros.text input=filterstring size=10 onkeyup="TABLETREE.filterParameters()" />
				</td>
				<td style="width:180px">
					Flag: <@macros.dropdown list=filterflags default="" onchange="TABLETREE.filterParameters();" width="120px" callMethodForKey=""/>
				</td>
				<td style="width:200px">
					Status: <@macros.dropdown list=filtertypes default="" onchange="TABLETREE.filterParameters();" width="120px" callMethodForKey=""/>
				</td>
				<td align="right"><input name="formsubmit" value="Update parameters" type="submit" /></td>
			</tr>
		</table>
		<table class="parameter" id="results">
			<tr>
				<th align="left">
					<a href='#' onclick='collapse();'><img src='images/minus.gif' id='collapseimage' alt='minus' title="Click here to collapse/expand all" class="tiptip" border='0'/></a>Name
				</th>
				<th>Flags</th>
				<th align="left">Value</th>
				<th>Create</th>
				<th>
					<span title="Click here to check all" class="tiptip" onclick="check('delete')">Delete</span>
				</th>
			</tr>
			<#list params as param>
			<#if param.unittypeParameter??>
			<tr id="${param.name}">
				<#if param.jobParameter??>
				<td class="configured">
				<#else>
				<td class="unconfigured">
				</#if>
					<span style="margin-left:${param.tab}px"><a onclick="return showModal('Unit Type Parameter','${URL_MAP.UNITTYPEPARAMETERS}&unittype=${job.group.unittype.name?url}&utp=${param.unittypeParameter.id}', 700, 500);" href="">${param.shortName}</a></span>
				</td>
				<td>${param.unittypeParameter.flag.flag}</td>
				<td>
					<#assign utpValues= param.unittypeParameter.values?? && (param.unittypeParameter.values.values?size>0)>
					<#if param.jobParameter??>
					<#if utpValues>
					<select name="update::${param.name}" style="width:100%">
						<#assign selected=false>
						<#list param.unittypeParameter.values.values as value>
						<option <#if param.jobParameter.parameter.value?? && param.jobParameter.parameter.value=value><#assign selected=true>selected="selected"</#if>>${value}</option>
						</#list>
						<#if !selected>
							<option selected="selected" value="${param.jobParameter.parameter.value}">${param.jobParameter.parameter.value} (custom)</option>
						</#if>
					</select>
					<#else>
					<input name="update::${param.name}" type="text" value="${param.jobParameter.parameter.value?default("n/a")}" class="parameterinput" <#if param.unittypeParameter.flag.readOnly=true> style="background-color:silver" readonly="readonly"</#if>/>
					</#if>
					<#else>	
					<#if utpValues>
					<select name="update::${param.name}" style="width:100%;display:none">
						<#list param.unittypeParameter.values.values as value>
						<option>${value}</option>
						</#list>
					</select>
					<#else>
					<input name="update::${param.name}" type="text" class="parameterinput" <#if param.unittypeParameter.flag.readOnly=true> style="background-color:silver;display:none;" readonly="readonly"<#else>style="display:none;"</#if> />
					</#if>
					</#if>
				</td>
				<td align="center"><#if !param.jobParameter??><input name="add::${param.name}" onclick="toggle('update::${param.name}',this);" type="checkbox" /></#if></td>
				<td align="center"><#if param.jobParameter??><input name="delete::${param.name}" type="checkbox" onclick="toggle('update::${param.name}',this);" /></#if></td>
			</tr>
			<#else>
			<tr id="${param.name}">
				<td>
					<span style="margin-left:${param.tab-4}px"><img src="images/minus.gif" alt="minus" onclick="javascript:TABLETREE.collapse('${param.name}')" />${param.shortName}</span>
				</td>
				<td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td>
			</tr>
			</#if>
			</#list>
			<tr><td colspan="5">&nbsp;</td></tr>
			<tr>
				<td colspan="5" align="right">
					<input name="formsubmit" value="Update parameters" type="submit" />
				</td>
			</tr>
		</table>
	</fieldset>
</@macros.form>
