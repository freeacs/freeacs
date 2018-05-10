<@macros.form id="trigger">
    <#if unittypes.selected??>
        <#if trigger??>
            <#include "updateTrigger.ftl">
        <#else>
            <#include "createTrigger.ftl">
        </#if>
        <fieldset id="parameters">
            <legend>Triggers</legend>
            <table class="parameter" id="results">
                <tr>
                    <th align="left">Trigger name</th>
                    <th align="left">Active</th>
                    <th align="left">Status</th>
                    <th align="left">Type</th>
                    <th align="left">Syslog Event</th>
                    <th align="left">Notify Type</th>
                    <th align="left">Script</th>
                    <th align="left">Delete</th>
                    <th align="left">History</th>
                </tr>
                <#list triggertablelist as triggerelement>
                <tr id="${triggerelement.name}">
                    <td>
                        <#if triggerelement.triggerParent=false>
                            <span style="margin-left:${triggerelement.tab}px">
                                <a href="${URL_MAP.TRIGGEROVERVIEW}&action=edit&triggerId=${triggerelement.trigger.id}">${triggerelement.trigger.name}</a>
                            </span>
                        <#else>
                            <span style="margin-left:${triggerelement.tab-4}px"><img src="images/minus.gif" alt="minus" onclick="javascript:TABLETREE.collapse('${triggerelement.trigger.name}')" />
                                <a href="${URL_MAP.TRIGGEROVERVIEW}&action=edit&triggerId=${triggerelement.trigger.id}">${triggerelement.trigger.name}</a>
                            </span>
                        </#if>
                    </td>
                    <td><#if triggerelement.trigger.active=true>Activated<#else>Deactivated</#if></td>
                    <td><a href="${URL_MAP.TRIGGERRELEASE}&triggerId=${triggerelement.trigger.id}">status</a></td>
                    <td>${triggerelement.trigger.triggerTypeStr}</td>
                    <td><#if triggerelement.trigger.syslogEvent??><a href="${URL_MAP.SYSLOGEVENTS}&eventid=${triggerelement.trigger.syslogEvent.eventId}">${triggerelement.trigger.syslogEvent.name}</a></#if></td>
                    <td>${triggerelement.trigger.notifyTypeAsStr}</td>
                    <td><#if triggerelement.trigger.script??><a href="${URL_MAP.FILES}&id=${triggerelement.trigger.script.id}&unittype=${triggerelement.trigger.unittype.name}">${triggerelement.trigger.script.name!}</a></#if></td>
                    <td><a href="${URL_MAP.TRIGGEROVERVIEW}&action=delete&triggerId=${triggerelement.trigger.id}"onclick="return processDelete('delete the trigger');">delete</a></td>
                    <td><a href="${URL_MAP.TRIGGERRELEASEHISTORY}&triggerId=${triggerelement.trigger.id}">history</a></td>
                </tr>
                </#list>
            </table>
        </fieldset>
    <#else>
        <div>
            <fieldset>
                <legend>Trigger overview</legend>
                No Unit Type selected
            </fieldset>
        </div>
    </#if>
</@macros.form>