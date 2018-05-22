<script src="javascript/acs.module.group.js"></script>
<@macros.form>
    <@macros.hidden list=groups />
    <@macros.hidden list=unittypes />
<table cellspacing="0" cellpadding="0">
    <tr>
        <td>
            <div>
                <fieldset>
                    <legend>Group details</legend>
                    <table id="input">
                        <tr>
                            <th align="right">Description:</th>
                            <td>
                                <input name="description" type="text" value="${groups.selected.description!}"
                                       size="30"/>
                            </td>
                        </tr>
                        <tr>
                            <th align="right">Parent group:</th>
                            <td>
                                <@macros.dropdown list=parents default="No parent group" onchange="" />
                                <#if parents.selected??>
                                    <a href='?page=group&amp;group=${parents.selected.name?url}'>
                                        <img src='images/shortcut.gif' style='height:15px' border='0'
                                             title='Go to parent group' alt='shortcut'/>
                                    </a>
                                </#if>
                            </td>
                        </tr>
                        <tr>
                            <th align="right">Profile:</th>
                            <td>
                                <#if (profiles.items?size>0)>
                                    <@macros.dropdown list=profiles default="All profiles" onchange="" />
                                <#else>
                                    <#if profiles.selected??>
                                    ${profiles.selected.name}
                                    <#else>
                                        All profiles
                                    </#if>
                                </#if>
                            </td>
                        </tr>
                        <tr>
                            <th align="right">Current size:</th>
                            <td>${count}</td>
                        </tr>
                        <tr>
                            <td/>
                            <td align="right">
                                <input name="formsubmit" value="Delete" type="submit"
                                       onclick="return processDelete('delete the group');"/>
                                <input name="formsubmit" value="Update" type="submit"
                                       onclick="return validateFields(['description']);"/>
                            </td>
                        </tr>
                    </table>
                </fieldset>
            </div>
        </td>
        <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
        <td valign="top">
            <div>
                <#if (groups.selected.children?size>0) || (groupjobs?size>0)>
                    <fieldset style="margin-bottom:10px;">
                        <legend>Referenced by:</legend>
                        <#if (groups.selected.children?size>0)>
                            <select onchange="goToGroup(this.value)" style="width:200px">
                                <option>Select group child:</option>
                                <#list groups.selected.children as child>
                                    <option value="${child.name}:@:${child.unittype.name}">${child.name}</option>
                                </#list>
                            </select>
                        </#if>
                        <#if (groupjobs?size>0)>
                            <br/>
                            <select onchange="goToJob(this.value)" style="width:200px">
                                <option>Select active job:</option>
                                <#list groupjobs as job>
                                    <option value="${job.name}:@:${job.group.unittype.name}">${job.name}</option>
                                </#list>
                            </select>
                        </#if>
                    </fieldset>
                </#if>
                <#if groups.selected.timeParameter??>
                    <fieldset>
                        <legend>Time rolling</legend>
                        <div><input name="timerollingparameter" value="${groups.selected.timeParameter.name?string}"/>
                        </div>
                        <div><@macros.dropdown list=formats default="Select format" callMethodForKey="" onchange="" /></div>
                        <div><@macros.dropdown list=offsets default="No offset" callMethodForKey="" onchange="" /></div>
                    </fieldset>
                </#if>
            </div>
        </td>
    </tr>
</table>
</@macros.form>
<@macros.form name="parameters">
    <@macros.hidden list=groups />
    <@macros.hidden list=unittypes />
<fieldset id="parameters">
    <legend>Parameters</legend>
    <table id="actions" class="center" style="width:100%">
        <tr>
            <td style="width:190px">
                Name: <@macros.text input=filterstring size=10 onkeyup="TABLETREE.filterParameters()" />
            </td>
            <td style="width:180px">
                Flag: <@macros.dropdown list=filterflags onchange="TABLETREE.filterParameters();" default="" callMethodForKey="" width="120px"/>
            </td>
            <td style="width:200px">
                Status: <@macros.dropdown list=filtertypes onchange="TABLETREE.filterParameters();" default="" callMethodForKey="" width="120px" />
            </td>
            <td align="right">
                <input name="formsubmit" value="Update parameters" type="submit"/>
            </td>
        </tr>
    </table>
    <table class="parameter" id="results">
        <tr>
            <th align="left">
                <a href='#' onclick='collapse();'><img src='images/minus.gif' id='collapseimage' alt='minus'
                                                       title="Click here to collapse/expand all" class="tiptip"
                                                       border='0'/></a>Name
            </th>
            <th>Flags</th>
            <th>Data&nbsp;type</th>
            <th>Operator</th>
            <th>Value</th>
            <th style="text-align:center !important;">Create</th>
            <th style="text-align:center !important;">
                <span title="Click here to check all" class="tiptip" onclick="check('delete')">Delete</span>
            </th>
        </tr>
        <#list params as param>
            <#if param.unittypeParameter??>
                <#if param.groupParameter??> <!-- test if this is configured-->
                    <!-- do we have a list of group parameters? -->
                    <#list param.groupParameter as groupParameter> <!-- lets loop the group parameters -->
                        <tr id="${groupParameter.name}">
                            <td class="configured">
                                <span style="margin-left:${param.tab}px"><a
                                        onclick="return showModal('Unit Type Parameter','${URL_MAP.UNITTYPEPARAMETERS}&unittype=${groups.selected.unittype.name?url}&utp=${param.unittypeParameter.id}', 700, 500);"
                                        href="">${param.shortName}</a></span>
                            </td>
                            <td>${param.unittypeParameter.flag.flag}</td>
                            <td>
                                <select name="datatype::${groupParameter.name}">
                                    <#list datatypes as datatype>
                                        <option
                                            <#if groupParameter?? && groupParameter.parameter.type=datatype>selected="selected"</#if>
                                            value="${datatype.type}">${datatype.type}</option>
                                    </#list>
                                </select>
                            </td>
                            <td>
                                <select name="operator::${groupParameter.name}">
                                    <#list operators as operator>
                                        <option
                                            <#if groupParameter?? && groupParameter.parameter.op=operator>selected="selected"</#if>
                                            value="${operator.operatorSign}">${operator.operatorSign}</option>
                                    </#list>
                                </select>
                            </td>
                            <td style="white-space:nowrap">
                                <#assign utpValues= param.unittypeParameter.values?? && (param.unittypeParameter.values.values?size>0)>
                                <#if utpValues>
                                    <select name="update::${groupParameter.name}" style="width:100%">
                                        <#assign selected=false>
                                        <#list param.unittypeParameter.values.values as value>
                                            <option value="${value}"
                                                    <#if groupParameter.parameter.value=value><#assign selected=true>selected="selected"</#if>>${value}</option>
                                        </#list>
                                        <#if !selected>
                                            <option selected="selected"
                                                    value="${groupParameter.parameter.value}">${groupParameter.parameter.value}
                                                (custom)
                                            </option>
                                        </#if>
                                    </select>
                                <#else>
                                    <input name="update::${groupParameter.name}" type="text"
                                           value="<#if groupParameter.parameter.valueWasNull()>NULL<#else>${groupParameter.parameter.value}</#if>"
                                           class="parameterinput"/>
                                </#if>
                            </td>
                            <#-- if param.unittypeParameter.flag.inspection=false -->
                                <td>
                                    &nbsp;
                                </td>
                                <td align="center">
                                    <input name="delete::${groupParameter.name}"
                                           class="deleteParameter {parameter: '${groupParameter.name}'}"
                                           type="checkbox"/>
                                </td>
                            <#-- else>
                                <td>&nbsp;</td>
                                <td>&nbsp;</td>
                            </#if -->
                        </tr>
                    </#list>
                </#if>
                <tr id="${param.name}">
                    <td class="unconfigured">
                        <span style="margin-left:${param.tab}px"><a
                                onclick="return showModal('Unit Type Parameter','${URL_MAP.UNITTYPEPARAMETERS}&unittype=${groups.selected.unittype.name?url}&utp=${param.unittypeParameter.id}', 700, 500);"
                                href="">${param.shortName}</a></span>
                    </td>
                    <td>${param.unittypeParameter.flag.flag}</td>
                    <td>
                        <select name="datatype::${param.name}" style="display:none;">
                            <#list datatypes as datatype>
                                <option value="${datatype.type}">${datatype.type}</option>
                            </#list>
                        </select>
                    </td>
                    <td>
                        <select name="operator::${param.name}" style="display:none;">
                            <#list operators as operator>
                                <option value="${operator.operatorSign}">${operator.operatorSign}</option>
                            </#list>
                        </select>
                    </td>
                    <td style="white-space:nowrap">
                        <#assign utpValues= param.unittypeParameter.values?? && (param.unittypeParameter.values.values?size>0)>
                        <#if utpValues>
                            <select name="update::${param.name}" style="width:100%;display:none">
                                <#list param.unittypeParameter.values.values as value>
                                    <option value="${value}">${value}</option>
                                </#list>
                            </select>
                        <#else>
                            <input name="update::${param.name}" type="text" class="parameterinput"
                                   style="display:none;"/>
                        </#if>
                    </td>
                    <#-- if param.unittypeParameter.flag.inspection=false -->
                        <td>
                            <input name="create::${param.name}" class="configureParameter {parameter:'${param.name}'}"
                                   type="checkbox"/>
                        </td>
                        <td align="center">
                            &nbsp;
                        </td>
                    <#-- else>
                        <td>&nbsp;</td>
                        <td>&nbsp;</td>
                    </#if -->
                </tr>
            <#else>
                <tr id="${param.name}">
                    <td>
                        <span style="margin-left:${param.tab-4}px"><img src="images/minus.gif" alt="minus"
                                                                        onclick="javascript:TABLETREE.collapse('${param.name}')"/>${param.shortName}</span>
                    </td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                    <td>&nbsp;</td>
                </tr>
            </#if>
        </#list>
        <tr>
            <td colspan="7">&nbsp;</td>
        </tr>
        <tr>
            <td colspan="7" align="right">
                <input name="formsubmit" value="Update parameters" type="submit"/>
            </td>
        </tr>
    </table>
</fieldset>
</@macros.form>