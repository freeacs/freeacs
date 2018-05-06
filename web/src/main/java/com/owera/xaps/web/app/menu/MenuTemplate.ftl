<#macro menu items>
	<#if (items?size>0)>
	<ul>
		<#list items as item>
		<li<#if item.selected> class="selected"</#if>>
			<a href="${item.url}"<#list item.attributes as attr> ${attr.key}="${attr.value}"</#list>>
				${item.display}
			</a>
			<@menu item.subMenuItems />
		</li>
		</#list>
	</ul>
	</#if>
</#macro>
<@menu list />
<br style="clear:left" />