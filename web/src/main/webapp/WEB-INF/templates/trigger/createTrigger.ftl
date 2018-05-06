<#if updateMessage??>
    <div align="left"><span style="color: green; margin-bottom: 10px; margin-top: 10px; margin-left: 10px; font-weight: bold">${updateMessage}</span></div>
</#if>
<fieldset>
    <legend>${createUpdateHeaderName}</legend>
    <table>
     	<tr>
    		<td valign=top>
    			<table>
    				<tr>
    					<th valign=top height=30 colspan=2>Trigger Setup</th>
    				</tr>
			        <tr>
			            <td align="right" style="font-weight: bold;">Active:</td>
			            <td align="left"><input name="active" type="checkbox" id="active" checked="checked" value="true"/></td>
			        </tr>
			        <tr>
			            <td align="right" style="font-weight: bold;">Name:</td>
			            <td><input name="name" type="text" id="name" value="" style="width:14em;"/></td>
			        </tr>
			        <tr>
			            <td align="right" style="font-weight: bold;">Description:</td>
			            <td><textarea name="description" rows="3" id="description" cols="23"></textarea></td>
			        </tr>
			        <tr>
			            <td align="right" style="font-weight: bold;">Type:</td>
			            <td><@macros.dropdown list=typeTrigger default="" callMethodForKey="id" callMethodForDisplay="name" onchange="toggleNumberOfFields(this)" width="14em"/></td>
			        </tr>
			        <tr>
			            <td align="right" style="font-weight: bold;">Evaluation Period:</td>
			            <td><@macros.dropdown list=evalPeriodMinutes default="" callMethodForKey="" onchange="" width="14em"/></td>
			        </tr>
			        <tr>
			            <td align="right" style="font-weight: bold;">Parent Trigger:</td>
			            <td><@macros.dropdown list=parentTrigger default="[none]" callMethodForKey="id" callMethodForDisplay="name" onchange="" width="14em" /></td>
			        </tr>
       
	        	</table>
        	</td>
 	   		<td valign=top>
    			<table>
    				<tr>
    					<th valign=top height=30 colspan=2>BASIC Trigger Fields</th>
    				</tr>
			        <tr>
			            <td align="right" style="font-weight: bold;">Syslog Event:</td>
			            <td><@macros.dropdown list=syslogEvents default="[choose event]" callMethodForKey="id" callMethodForDisplay="name" onchange="" width="14em"/></td>
			        </tr>
			        <tr>
			            <td align="right" style="font-weight: bold;">Number of total events:</td>
			            <td><input name="noTotal" type="text" id="noTotal" value="" style="width:14em;"/></td>
			        </tr>
			        <tr>
			            <td align="right" style="font-weight: bold;">Number of events per unit:</td>
			            <td><input name="noPrUnit" type="text" id="noPrUnit" value="" style="width:14em;"/></td>
			        </tr>
			        <tr>
			            <td align="right" style="font-weight: bold;">Number of units:</td>
			            <td><input name="noUnits" type="text" id="noUnits" value="" style="width:14em;"/></td>
			        </tr>
	        	</table>
        	</td>
 	   		<td valign=top>
    			<table>
    				<tr>
    					<th valign=top height=30 colspan=2>Trigger Action Fields</th>
    				</tr>
			        <tr>
			            <td align="right" style="font-weight: bold;">Notify Type:</td>
			            <td><@macros.dropdown list=notifyType callMethodForKey="id" callMethodForDisplay="name" onchange="toggleNotifyIntHours(this)" width="14em" /></td>
			        </tr>
			        <tr>
			            <td align="right" style="font-weight: bold;">Notify Interval:</td>
			            <td><@macros.dropdown list=notifyIntervalHours default=" " callMethodForKey="id" callMethodForDisplay="name" onchange="" width="14em"/></td>
			        </tr>
			        <tr>
			            <td align="right" style="font-weight: bold;">To List:</td>
			            <td><input name="toList" type="text" id="toList" value="" style="width:14em;"/></td>
			        </tr>
			        <tr>
			            <td align="right" style="font-weight: bold;">Script file:</td>
			            <td><@macros.dropdown list=scriptFiles default="[none]" callMethodForKey="id" callMethodForDisplay="name" onchange="" width="14em"/></td>
			        </tr>
			        <tr>
			            <td align="right" colspan="2"><input name="formsubmit" type="submit" id="submitbutton" value="${buttonname}"/></td>
			        </tr>
        
        		</table>
        	</td>
        </tr>
    </table>
</fieldset>
