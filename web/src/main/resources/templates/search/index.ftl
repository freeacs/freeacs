<script src="javascript/acs.module.search.js" type="text/javascript"></script>
<@macros.form>
<table cellspacing="0" cellpadding="0">
    <tr>
        <td valign="top">
            <fieldset style="min-width:350px">
                <legend>Search for units</legend>
                <table id="searchtable">
                    <#if advanced>
                        <#include "/search/advanced.ftl">
                    <#else>
                        <#include "/search/simple.ftl">
                    </#if>
                    <tr>
                        <td align="left">
                            <input name="mode" type="hidden"/><input name="advancedView" type="hidden"
                                                                     value="${advanced?string}"/>
                            <input name="submitButtonJS" value="<#if advanced>Simple form<#else>Advanced form</#if>"
                                   type="button"
                                   onclick="document.form1.advancedView.value='${(!advanced)?string}';document.form1.submit();"/>
                        </td>
                        <#if !(advanced && !UNITTYPE_DROPDOWN.selected??)>
                            <td align="right" colspan="4">
                                <input name="formsubmit" id="submitSearchButton" value="Search" type="submit"/>
                            </td>
                        </#if>
                    </tr>
                    <tr>
                        <th>&nbsp;</th>
                        <td align="right" colspan="4">
                            <span id="loading_message"></span>
                            <#if cmd.string??>
                                <input type="button" name="goback" value="Go back" onclick="history.go(-2)"/>
                            </#if>
                        </td>
                    </tr>
                </table>
            </fieldset>
        </td>
    </tr>
    <tr>
        <td>
            <#if result??>
                <#include "/search/results.ftl" />
            </#if>
        </td>
    </tr>
</table>
</@macros.form>