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
			            <td><input name="active" type="checkbox" id="active" <#if trigger.active=true>checked="on"</#if> value="true" style="width:14em;"/></td>
			        </tr>
			        <tr>
			            <td align="right" style="font-weight: bold;">Name:</td>
			            <td><input name="name" type="text" id="name" value="${trigger.name}" style="width:14em;"/></td>
			        </tr>
			        <tr>
			            <td align="right" style="font-weight: bold;">Description:</td>
			            <td><textarea name="description" rows="3" id="description" cols="23">${trigger.description!}</textarea></td>
			        </tr>
			        <tr>
			            <td align="right" style="font-weight: bold;">Type:</td>
			            <td><@macros.dropdown list=typeTrigger default="" callMethodForKey="id" callMethodForDisplay="name" onchange="toggleNumberOfFields(this)" width="14em" class="submitonchange" /></td>
			        </tr>
			        <tr>
			            <td align="right" style="font-weight: bold;">Evaluation Period:</td>
			            <td><@macros.dropdown list=evalPeriodMinutes default="" callMethodForKey="" onchange="" width="14em" class="submitonchange" /></td>
			        </tr>
			        <tr>
			            <td align="right" style="font-weight: bold;">Parent Trigger:</td>
			            <td><@macros.dropdown list=parentTrigger default="[none]" callMethodForKey="id" callMethodForDisplay="name" onchange="" width="14em" class="submitonchange" /></td>
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
			            <td><@macros.dropdown list=syslogEvents default="[choose event]" callMethodForKey="id" callMethodForDisplay="name" onchange="" width="14em" class="submitonchange" /></td>
			        </tr>
			        <tr>
			            <td align="right" style="font-weight: bold;">Number of total events:</td>
			            <td><input name="noTotal" type="text" id="noTotal" value="${trigger.noEvents!}" style="width:14em;"/></td>
			        </tr>
			        <tr>
			            <td align="right" style="font-weight: bold;">Number of events per unit:</td>
			            <td><input name="noPrUnit" type="text" id="noPrUnit" value="${trigger.noEventsPrUnit!}" style="width:14em;"/></td>
			        </tr>
			        <tr>
			            <td align="right" style="font-weight: bold;">Number of units:</td>
			            <td><input name="noUnits" type="text" id="noUnits" value="${trigger.noUnits!}" style="width:14em;"/></td>
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
			            <td><@macros.dropdown list=notifyType default="" callMethodForKey="id" callMethodForDisplay="name" onchange="toggleNotifyIntHours(this)" width="14em" class="submitonchange" /></td>
			        </tr>
			        <tr>
			            <td align="right" style="font-weight: bold;">Notify Interval:</td>
			            <td><@macros.dropdown list=notifyIntervalHours callMethodForKey="id" callMethodForDisplay="name" onchange="" width="14em"/></td>
			        </tr>
			        <tr>
			            <td align="right" style="font-weight: bold;">To List:</td>
			            <td><input name="toList" type="text" id="toList" value="${trigger.toList!}" style="width:14em;"/></td>
			        </tr>
			        <tr>
			            <td align="right" style="font-weight: bold;">Script file:</td>
			            <td><@macros.dropdown list=scriptFiles default="[none]" callMethodForKey="id" callMethodForDisplay="name" onchange="" width="14em"/></td>
			        </tr>    				
			        <tr>
			            <td align="right" colspan="2"><input name="formsubmit" type="submit" id="submitbutton" value="${buttonname}"/></td>
			            <input type=hidden name="triggerId" id="triggerId" value="${trigger.id}" style="width:14em;"/>
			        </tr>
        		</table>
        	</td>
        </tr>
    </table>
</fieldset>
<script type="text/javascript">
    toggleFieldsInEditmode();
</script>