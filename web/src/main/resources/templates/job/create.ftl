<#include "/job/rules.ftl" />
<@macros.form>
	<input name="cmd" type="hidden" value="create" />
	<table cellspacing="0" cellpadding="0"><tr><td valign="top"><div><fieldset>
		<legend>Create Job</legend>
		<table id="input">
			<#if unittypes.selected??>
				<tr>
					<th align="right">Name:</th>
					<td><input name="name" type="text" size="25" <#if name??>value="${name}"</#if> /></td>
					<td><span style="color:red;margin-left:5px;"><#if errors??>${errors.name!}</#if></span></td>
				</tr>
				<tr>
					<th align="right">Description:</th>
					<td><input name="description" type="text" size="25" <#if description??>value="${description}"</#if> /></td>
				</tr>
				<tr>
					<th align="right">Type:</th>
					<td><@macros.dropdown list=types default="" callMethodForKey="" /></td>
				</tr>
				<tr>
					<th align="right">Service Window:</th>
					<td><@macros.dropdown list=windows default="" callMethodForKey="" onchange=""/></td>
				</tr>
				<tr>
					<th align="right">Group:</th>
					<td><@macros.dropdown list=groups default="Choose a group" callMethodForKey="id" callMethodForDisplay="name" onchange=""/></td>
					<td><span style="color:red;margin-left:5px;"><#if errors??>${errors.groupId!}</#if></span></td>
				</tr>
				<tr>
					<th align="right">Job dependency:</th>
					<td><@macros.dropdown list=dependencies default="No job dependency" onchange="" /></td>
				</tr>
				<#if files??>
                    <tr>
                      	<#if requirefile>
	                      	<#if types.selected="SOFTWARE">
    	                    	<th align="right">Software:</th>
        	                	<#assign DEFAULT_SOFTWARE_OPTION = "Select software" />
            	          	<#else>
                	        	<th align="right">Script:</th>
                    	    	<#assign DEFAULT_SOFTWARE_OPTION = "Select script" />
                      		</#if>
                      		<td>
    	                    	<@macros.dropdown list=files callMethodForDisplay="nameAndVersion" callMethodForKey="id" default=DEFAULT_SOFTWARE_OPTION?default("Select script") defaultValue="Any" onchange=""/>
	                      	</td>
	                      	<td><span style="color:red;margin-left:5px;"><#if errors??>${errors.fileId!}</#if></span></td>
                      	</#if>
                    </tr>
				</#if>
				<tr>
					<th align="right">Stop rules:</th>
					<td>
						<input name="stoprules" type="text" size="25" <#if stoprules??>value="${stoprules}"</#if> />
						<a id='failurerule' href='#' onclick="ShowRuleBox(this,170,['amount','status']);return false;"><b>+</b></a>
					</td>
				</tr>
				<tr>
					<th align="right">Unconfirmed timeout:</th>
					<td><input name="unconfirmedtimeout" type="text" <#if unconfirmedtimeout??>value="${unconfirmedtimeout}"<#else>value="600"</#if> size="25" /></td>
					<td><span style="color:red;margin-left:5px;"><#if errors??>${errors.unconfirmedtimeout!}</#if></span></td>
				</tr>
				<tr>
					<th align="right">Repeat count:</th>
					<td><input name="repeatcount" type="text" <#if repeatcount??>value="${repeatcount!}"</#if> size="40" /></td>
					<td><span style="color:red;margin-left:5px;"><#if errors??>${errors.repeatcount!}</#if></span></td>
				</tr>
				<tr>
					<th align="right">Repeat interval:</th>
					<td><input name="repeatinterval" type="text" <#if repeatinterval??>value="${repeatinterval!}"</#if> size="40" /></td>
					<td><span style="color:red;margin-left:5px;"><#if errors??>${errors.repeatinterval!}</#if></span></td>
				</tr>
				<tr>
					<td align="right" colspan="2"><input name="formsubmit" value="Create new job" type="submit" /></td>
				</tr>
				<#if message??>
					<tr>
						<td colspan="2" style="color:red">${message}</td>
					</tr>
				</#if>
			<#else>
				<tr>
					<td>No Unit Type selected</td>
				</tr>
			</#if>
		</table>
	</fieldset></div></td></tr></table>
</@macros.form>