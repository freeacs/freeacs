<script>
    $.jHelp("help/${windowPage}window.xml", {"IconURL":"/xapsweb/images/jHelpIcon.gif"});
</script>
<@macros.form>
    <#if async??>
    <input type="hidden" name="async" value="true"/>
    <input type="hidden" name="header" value="true"/>
    </#if>
    <#if unittype?? && profile??>
    <input name="unittype" type="hidden" value="${unittype.name}"/>
    <input name="profile" type="hidden" value="${profile.name}"/>
        <#assign id = profile.name>
    <#else>
        <#if unit??>
        <input name="unit" type="hidden" value="${unit.id}"/>
            <#assign id=unit.id>
        </#if>
    </#if>
<table>
    <tr>
        <td>
            <div>
                <fieldset>
                    <legend>Service Window</legend>
                    <table width="550px;">
                        <tr>
                            <th align="right" width="80px;">Disruptive:</th>
                            <td>
                                <div id="download-window" <#if !fromdayDownload??>style="display:none"</#if>>
                                    <select name="fromday::download" size="1">
                                        <option value=".">From day:</option>
                                        <#list window.days as day>
                                            <option
                                                <#if fromdayDownload?? && fromdayDownload=day!>selected="selected"</#if>>${day!}</option>
                                        </#list>
                                    </select>
                                    -
                                    <select name="today::download" size="1">
                                        <option value=".">To day:</option>
                                        <#list window.days as day>
                                            <option
                                                <#if todayDownload?? && todayDownload=day!>selected="selected"</#if>>${day!}</option>
                                        </#list>
                                    </select>
                                    :
                                    <select name="fromhour::download" size="1">
                                        <option value=".">From hour:</option>
                                        <#list window.hours as hour>
                                            <option
                                                <#if fromhourDownload?? && fromhourDownload=hour!>selected="selected"</#if>>${hour!}</option>
                                        </#list>
                                    </select>
                                    -
                                    <select name="tohour::download" size="1">
                                        <option value=".">To hour:</option>
                                        <#list window.hours as hour>
                                            <option
                                                <#if tohourDownload?? && tohourDownload=hour!>selected="selected"</#if>>${hour!}</option>
                                        </#list>
                                    </select>
                                    <img src="images/trash.gif" alt="trash" title="Remove configuration"
                                         onclick="toggleWindow('configure-download','download-window','download-window');"/>
                                </div>
                                <a id="configure-download" href="#" <#if fromdayDownload??>style="display:none"</#if>
                                   onclick="toggleWindow('download-window','configure-download','download-window');">Configure</a>
                            </td>
                        </tr>
                        <tr>
                            <th align="right" width="80px;">Regular:</th>
                            <td>
                                <div id="regular-window" <#if !fromdayRegular??>style="display:none"</#if>>
                                    <select name="fromday::regular" size="1">
                                        <option value=".">From day:</option>
                                        <#list window.days as day>
                                            <option
                                                <#if fromdayRegular?? && fromdayRegular=day!>selected="selected"</#if>>${day!}</option>
                                        </#list>
                                    </select>
                                    -
                                    <select name="today::regular" size="1">
                                        <option value=".">To day:</option>
                                        <#list window.days as day>
                                            <option
                                                <#if todayRegular?? && todayRegular=day!>selected="selected"</#if>>${day!}</option>
                                        </#list>
                                    </select>
                                    :
                                    <select name="fromhour::regular" size="1">
                                        <option value=".">From hour:</option>
                                        <#list window.hours as hour>
                                            <option
                                                <#if fromhourRegular?? && fromhourRegular=hour!>selected="selected"</#if>>${hour!}</option>
                                        </#list>
                                    </select>
                                    -
                                    <select name="tohour::regular" size="1">
                                        <option value=".">To hour:</option>
                                        <#list window.hours as hour>
                                            <option
                                                <#if tohourRegular?? && tohourRegular=hour!>selected="selected"</#if>>${hour!}</option>
                                        </#list>
                                    </select>
                                    <img src="images/trash.gif" alt="trash" title="Remove configuration"
                                         onclick="toggleWindow('configure-regular','regular-window','regular-window');"/>
                                </div>
                                <a id="configure-regular" href="#" <#if fromdayRegular??>style="display:none"</#if>
                                   onclick="toggleWindow('regular-window','configure-regular','regular-window');">Configure</a>
                            </td>
                        </tr>
                        <tr>
                            <th align="right" width="80px;">Frequency:</th>
                            <td>
                                <input id="frequency" type="text" <#if !frequency??>disabled="disabled"
                                       style="display:none"</#if> name="frequency" size="20"
                                       value="<#if frequency??>${frequency}<#else>7</#if>"/>
                                <img <#if !frequency??>style="display:none"</#if> src="images/trash.gif" alt="trash"
                                     title="Remove configuration"
                                     onclick="el =document.getElementById('frequency'); el.style.display='none'; el.value='';document.getElementById('configure-frequency').style.display='';this.style.display='none'"
                                     id="frequency-trash"/>
                                <a href="#" <#if frequency??>style="display:none"</#if>
                                   onclick="el = document.getElementById('frequency');el.style.display='';el.disabled=false;el.value='7';this.style.display='none';document.getElementById('frequency-trash').style.display='';"
                                   id="configure-frequency">Configure</a>
                            </td>
                        </tr>
                        <tr>
                            <th align="right" width="80px;">Spread:</th>
                            <td>
                                <input id="spread" type="text" <#if !spread??>disabled="disabled"
                                       style="display:none"</#if> name="spread" size="20"
                                       value="<#if spread??>${spread}<#else>50</#if>"/>
                                <img <#if !spread??>style="display:none"</#if> src="images/trash.gif" alt="trash"
                                     title="Remove configuration"
                                     onclick="el =document.getElementById('spread'); el.style.display='none'; el.value='';document.getElementById('configure-spread').style.display='';this.style.display='none'"
                                     id="spread-trash"/>
                                <a href="#" <#if spread??>style="display:none"</#if>
                                   onclick="el = document.getElementById('spread');el.style.display='';el.disabled=false;el.value='50';this.style.display='none';document.getElementById('spread-trash').style.display='';"
                                   id="configure-spread">Configure</a>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2" align="right">
                                <input name="formsubmit"
                                       onclick="if(IsNumber(document.getElementsByName('frequency')[0].value)){ return true; }else{ alert('Frequency must be a number larger than zero'); return false; }"
                                       value="${window.button}" type="submit"/><input type="button" value="Close"
                                                                                      onclick="window.top.document.getElementById('popCloseBox').onclick();"/>
                            </td>
                        </tr>
                        <tr>
                            <td colspan="2" align="right">
                                <#if message??><span style="color:green;font-weight:bold">${message}</span></#if>
                                <#if error??><span style="color:red;font-weight:bold">${error}</span></#if>
                            </td>
                        </tr>
                    </table>
                </fieldset>
            </div>
        </td>
    </tr>
</table>
</@macros.form>