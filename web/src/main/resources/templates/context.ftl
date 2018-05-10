<#macro contextDropdown list default="Select" defaultOption="All" visible=false displayAll=true icon="">	
	<dl class="contextSelect dropdown" <#if !visible>style="display:none;"</#if>>
		<dt>
			<a href="#">
				<#if icon!=""><img width="35px" style="float:left;vertical-align:middle;" src="${icon}" /></#if><span>${default}</span>
			</a>
		</dt>
		<dd>
			<ul style="display:none">
				<#if displayAll>
				<li>
					<#if !list.selected??><b>${defaultOption}</b><#else><a href="#">${defaultOption}</a></#if>
				</li>
				</#if>
				<#list list.items as item>
				<li>
					<#if list.selected?? && list.selected=item><b>${item.name}</b><#else><a href="#">${item.name}</a></#if>
				</li>
				</#list>
			</ul>
		</dd>
	</dl>
</#macro>

<#macro contextItemSimple display icon>
	<a>
		<img width="20px" border="0" src="${icon}" style="vertical-align:middle" />
		${display}
	</a>
</#macro>

<#macro contextSearchField>
	<td style="padding-left:30px;">
		<img width="20px" border="0" src="images/icon_unit.png" style="vertical-align:top" />
		<input type="text" class="searchField" id="contextsearch" value="context search" name="contextunit" />
		<script>
		$(document).ready(function(){
			var field = $("#contextsearch");
			field.autocomplete({
				source: "${URL_MAP.SEARCH}<#if !UNITTYPE_DROPDOWN.selected??>&unittype=.&profile=.<#else>&unittype=${UNITTYPE_DROPDOWN.selected.name}<#if PROFILE_DROPDOWN.selected??>&profile=${PROFILE_DROPDOWN.selected.name}<#else>&profile=.</#if></#if>",
				minLength: 3,
				select: function(event,ui){
					field.closest("form").submit();
				}
			});
		});
		</script>
	</td>
</#macro>

<form class="unit" action="" method="post">
	<fieldset id="CONTEXTFIELDSET">
		<legend>Context navigation</legend>
			<div>
				<#if CONTEXT_ITEM??>
					<#assign item=CONTEXT_ITEM />
				</#if>
				<table cellpadding="0" cellspacing="0" style="float:left;height:30px">
					<tr>
						<#if item?? && item!="" && CONTEXT_SPECIFIC>
							<#if item.unitId??>
								<td style="padding-right:30px">
									<@contextItemSimple display=item.unitTypeName?default("Unknown") icon="images/icon_settings.png" />
								</td>
								<td style="padding-right:30px">
									<@contextItemSimple display=item.profileName?default("Unknown") icon="images/icon_profile.png" />
								</td>
								<td>
									<@contextItemSimple display=item.unitId icon="images/icon_unit.png" />
									<span class="contextSelect" style="display:none;">
										<img width="20px" border="0" src="images/icon_unit.png" style="vertical-align:top;" />
										<input type="text" class="searchField" id="contextunitspecific" name="contextunit" style="background-color:transparent" value="search for other unit" />
									</span>
									<script>
										$(document).ready(function(){
											$( "#contextunitspecific" ).autocomplete({
												source: "${URL_MAP.SEARCH}<#if !UNITTYPE_DROPDOWN.selected??>&unittype=.&profile=.<#else>&unittype=${UNITTYPE_DROPDOWN.selected.name}<#if PROFILE_DROPDOWN.selected??>&profile=${PROFILE_DROPDOWN.selected.name}<#else>&profile=.</#if></#if>",
												minLength: 3
											});
										});
									</script>
									<img title="Search for other units" class="contextSwitcher unit" border="0" src="images/arrow.png"  style="vertical-align:middle" />
								</td>
							<#else>
								<#if item.groupName??>
									<td style="padding-right:30px">
										<@contextItemSimple display=item.unitTypeName?default("Unknown") icon="images/icon_settings.png" />
									</td>
									<td>
										<@contextItemSimple display=item.groupName icon="images/icon_group.png" />
										<@contextDropdown list=GROUP_DROPDOWN icon="images/icon_group.png" default="Change Group:" displayAll=false />
										<input type="hidden" name="contextgroup" value="${item.groupName}" />
										<img title="Select another group" class="contextSwitcher group" border="0" src="images/arrow.png"  style="vertical-align:middle" />
									</td>
								<#else>
									<#if item.jobName??>
										<td style="padding-right:30px">
											<@contextItemSimple display=item.unitTypeName?default("Unknown") icon="images/icon_settings.png" />
										</td>
										<td>
											<@contextItemSimple display=item.jobName icon="images/icon_job.png" />
											<@contextDropdown list=JOB_DROPDOWN icon="images/icon_job.png" default="Change Job:" displayAll=false />
											<input type="hidden" name="contextjob" value="${item.jobName}" />
											<img title="Select another job" class="contextSwitcher job" border="0" src="images/arrow.png"  style="vertical-align:middle" />
										</td>
									<#else>
										<#if item.unitTypeName?? && item.profileName??>
											<td style="padding-right:30px">
												<@contextItemSimple display=item.unitTypeName?default("Unknown") icon="images/icon_settings.png" />
											</td>
											<td>
												<@contextItemSimple display=item.profileName icon="images/icon_profile.png" />
												<@contextDropdown list=PROFILE_DROPDOWN icon="images/icon_profile.png" default="Select Profile:" displayAll=false />
												<input type="hidden" name="contextprofile" value="${item.profileName}" />
												<img title="Select another profile" class="contextSwitcher profile" border="0" src="images/arrow.png"  style="vertical-align:middle" />
											</td>
										<#else>
											<#if item.unitTypeName??>
												<td>
													<@contextItemSimple display=item.unitTypeName icon="images/icon_settings.png" />
													<@contextDropdown list=UNITTYPE_DROPDOWN icon="images/icon_settings.png" default="Select Unit Type:" displayAll=false />
													<input type="hidden" name="contextunittype" value="${item.unitTypeName}" />
													<img title="Select another unittype" class="contextSwitcher unittype" border="0" src="images/arrow.png"  style="vertical-align:middle" />
												</td>
											</#if>
										</#if>
										<@contextSearchField />
									</#if>
								</#if>
							</#if>
						<#else>
							<#if item?? && item!="">
								<#if !item.unitTypeName??>
									<#if item.displayUnitType>
										<td>
											<@contextItemSimple display="All" icon="images/icon_settings.png" />
											<#if item.contextUnitTypeEditable>
												<@contextDropdown list=UNITTYPE_DROPDOWN icon="images/icon_settings.png" default="Select Unit Type:" displayAll=item.displayAllOptionUnitType />
												<input type="hidden" name="contextunittype" value="All" />
												<img title="Select another unittype" class="contextSwitcher unittype" border="0" src="images/arrow.png"  style="vertical-align:middle" />
											</#if>
										</td>
										<@contextSearchField />
									</#if>
								<#else>
									<#if item.unitTypeName?? && item.displayUnitType && !item.profileName??>
										<td style="padding-right:30px">
											<@contextItemSimple display=item.unitTypeName icon="images/icon_settings.png" />
											<@contextDropdown list=UNITTYPE_DROPDOWN icon="images/icon_settings.png" default="Select Unit Type:" displayAll=item.displayAllOptionUnitType />
											<input type="hidden" name="contextunittype" value="${item.unitTypeName}" />
											<img title="Select another unittype" class="contextSwitcher unittype" border="0" src="images/arrow.png"  style="vertical-align:middle" />
										</td>
										<#if item.displayProfile>
										<td>
											<@contextItemSimple display="All" icon="images/icon_profile.png" />
											<#if item.contextProfileEditable>
												<@contextDropdown list=PROFILE_DROPDOWN icon="images/icon_profile.png" default="Select Profile:" displayAll=item.displayAllOptionProfile />
												<input type="hidden" name="contextprofile" value="All" />
												<img title="Select another profile" class="contextSwitcher profile" border="0" src="images/arrow.png"  style="vertical-align:middle" />
											</#if>
										</td>
										</#if>
										<@contextSearchField />
									<#else>
										<#if item.unitTypeName?? && item.displayUnitType && item.profileName??>
											<td style="padding-right:30px">
												<@contextItemSimple display=item.unitTypeName icon="images/icon_settings.png" />
												<@contextDropdown list=UNITTYPE_DROPDOWN icon="images/icon_settings.png" default="Select Unit Type:" displayAll=item.displayAllOptionUnitType />
												<input type="hidden" name="contextunittype" value="${item.unitTypeName}" />
												<img title="Select another unittype" class="contextSwitcher unittype" border="0" src="images/arrow.png"  style="vertical-align:middle" />
											</td>
											<td>
												<@contextItemSimple display=item.profileName icon="images/icon_profile.png" />
												<@contextDropdown list=PROFILE_DROPDOWN icon="images/icon_profile.png" default="Select Profile:" displayAll=item.displayAllOptionProfile />
												<input type="hidden" name="contextprofile" value="${item.profileName}" />
												<img title="Select another profile" class="contextSwitcher profile" border="0" src="images/arrow.png"  style="vertical-align:middle" />
											</td>
											<@contextSearchField />
										</#if>
									</#if>
								</#if>
							</#if>
						</#if>
					</tr>
				</table>
			</div>
			<#if SHORTCUTS?? && (SHORTCUTS?size>0)>
			<dl class="dropdown shortcuts" style="margin-left:50px;margin-top:5px;min-width:50px !important;">
				<dt>
					<a href="#">
						<img style="float:left;vertical-align:middle;" border="0" src="images/shortcut.png" />
						<span>Select action:</span>
					</a>
				</dt>
				<dd>
					<ul style="display:none;top:-1px !important;left:0px !important;z-index:100;">
						<#list SHORTCUTS as item>
						<li>
							<a href="${item.url}">${item.display}</a>
						</li>
						</#list>
					</ul>
				</dd>
			</dl>
			</#if>
	</fieldset>
	<input type="submit" name="submitContext" style="display:none;" value="Submit" />
</form>