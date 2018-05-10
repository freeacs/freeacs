<#macro dropdown list class="" default="" defaultValue="." not=[] disabled=false encodeValue=false namePrefixForId="" size="1" multiple=false visible=true callMethodForKey="name" callMethodForDisplay="" width="200px" onchange="processForm('form1')">
	<select class="${class}" name="${list.input.key}" <#if disabled>disabled="disabled"</#if> id="${namePrefixForId!}${list.input.key}" onchange="${onchange}" size="${size}" <#if multiple=true && (size!="0")>multiple</#if> <#if width!="">style="width:${width};</#if><#if !visible>display:none;</#if>">
		<#if callMethodForDisplay="" && callMethodForKey??>
			<#assign displayMethod=callMethodForKey />
		<#else>
			<#assign displayMethod=callMethodForDisplay />
		</#if>
		<#assign excludedItems=not />
		<#if default!="">
			<option value="${defaultValue}">${default}</option>
		</#if>
		<#list list.items as item>
			<@option item list excludedItems callMethodForKey displayMethod multiple encodeValue />
		</#list>
	</select>
</#macro>

<#macro option item list excludeList callMethodForKey callMethodForDisplay multiple encodeValue>
	<#if callMethodForKey != "">
		<#if is_option_excluded(excludeList,item,callMethodForKey) = false>
			<#if multiple>
				<option <#if list.selected?? && is_option_selected(list.selected,item,callMethodForKey)>selected="selected"</#if> value="<#if encodeValue>${item[callMethodForKey]}<#else>${item[callMethodForKey]}</#if>">${item[callMethodForDisplay]}</option>
			<#else>
				<option <#if list.selected?? && list.selected[callMethodForKey]=item[callMethodForKey]>selected="selected"</#if> value="<#if encodeValue>${item[callMethodForKey]}<#else>${item[callMethodForKey]}</#if>">${item[callMethodForDisplay]}</option>
			</#if>
		</#if>
	<#else>
		<#if is_option_excluded(excludeList,item,"") = false>
			<#if multiple>
				<option <#if list.selected?? && is_option_selected(list.selected,item,"")>selected="selected"</#if> value="<#if encodeValue>${item?string}<#else>${item?string}</#if>">${item?string}</option>
			<#else>
				<option <#if list.selected?? && item?string=list.selected?string>selected="selected"</#if> value="<#if encodeValue>${item?string}<#else>${item?string}</#if>">${item?string}</option>
			</#if>
		</#if>
	</#if>
</#macro>

<#function is_option_excluded excludedArray itemToCompareWith callMethodForKey>
	<#list excludedArray as excludedArrayItem>
		<#if callMethodForKey!="" && itemToCompareWith[callMethodForKey]=excludedArrayItem>
			<#return true />
		<#else>
			<#if itemToCompareWith?string=excludedArrayItem>
				<#return true />
			</#if>
		</#if>
	</#list>
	<#return false />
</#function>

<#function is_option_selected selectedArray itemToCompareWith callMethodForKey>
	<#list selectedArray as selectedArrayItem>
		<#if callMethodForKey!="" && selectedArrayItem[callMethodForKey]=itemToCompareWith[callMethodForKey]>
			<#return true />
		<#else>
			<#if selectedArrayItem?string=itemToCompareWith?string>
				<#return true />
			</#if>
		</#if>
	</#list>
	<#return false />
</#function>

<#macro checkbox list class="" callMethod="" suffix="<br />" namePrefixForId="" onclick="processForm('form1');">
	<#list list.items as item>
	<#if callMethod!="">
	${item[callMethod]}&nbsp;<input type="checkbox" <#if onclick!="">onclick="${onclick}"</#if> class="${class}" name="${namePrefixForId!}${list.input.key}" value="${item[callMethod]}" <#list list.selected as selectedItem><#if item = selectedItem>checked="checked"</#if></#list> />${suffix!}
	<#else>
	${item?string}&nbsp;<input type="checkbox" <#if onclick!="">onclick="${onclick}"</#if> class="${class}" name="${namePrefixForId!}${list.input.key}" value="${item?string}" <#list list.selected as selectedItem><#if item = selectedItem>checked="checked"</#if></#list> />${suffix!}
	</#if>
	</#list>
</#macro>

<#macro radio list class="" callMethod="" suffix="<br />" namePrefixForId="" onclick="processForm('form1');">
	<#list list.items as item>
		<#if callMethod!="">
			${item[callMethod]}&nbsp;<input type="radio" <#if onclick!="">onclick="${onclick}"</#if> class="${class}" name="${namePrefixForId!}${list.input.key}" value="${item[callMethod]}" <#if list.selected?? && item[callMethod] = list.selected[callMethod]>checked="checked"</#if> />${suffix!}
		<#else>
			${item?string}&nbsp;<input type="radio" <#if onclick!="">onclick="${onclick}"</#if> class="${class}" name="${namePrefixForId!}${list.input.key}" value="${item?string}" <#if list.selected?? && item?string = list.selected?string>checked="checked"</#if> />${suffix!}
		</#if>
	</#list>
</#macro>

<#macro statusimage bool=true>
	<#if !bool><img src="images/err.png" alt="Bad" height="16px" title="Bad" /><#else>&nbsp;</#if>
</#macro>

<#macro fieldsetWithTitle title>
	<fieldset>
		<legend>${title}</legend>
		<#nested>
	</fieldset>
</#macro>

<#macro form onsubmit="" name="form1" autocomplete="off" id="" upload=false class="unit" method="POST" action="" charset="ISO-8859-1" fieldset="">
	<form <#if onsubmit!="">onsubmit="${onsubmit}"</#if> <#if autocomplete!="">autocomplete="${autocomplete}"</#if> action="<#if action!="">${action}<#else><#if CURRENT_PAGE??>${CURRENT_PAGE.url}</#if></#if>" <#if upload>enctype="multipart/form-data"</#if> method="${method}" <#if id!="">id="${id}"</#if> name="${name}" class="${class}" accept-charset="${charset}">
		<#if action==""><input type="hidden" name="page" value="${CURRENT_PAGE.id}" /></#if>
		<#if fieldset!="">
			<@fieldsetWithTitle title=fieldset>
				<#nested>
			</@fieldsetWithTitle>
		<#else>
			<#nested>
		</#if>
	</form>
</#macro>

<#macro text input size="" onkeyup="" style="">
	<input type="text" name="${input.key}" id="${input.key}" value="${input.value!}" <#if size?is_number || size!="">size="${size}"</#if> <#if onkeyup!="">onkeyup="${onkeyup}"</#if> <#if style!="">style="${style}"</#if> />
</#macro>

<#macro hidden list callMethodForSelected="name">
	<input type="hidden" name="${list.input.key}" value="<#if callMethodForSelected!="" && list.selected??>${list.selected[callMethodForSelected]}<#else>${list.selected!}</#if>" />
</#macro>