<@macros.form onsubmit="ok('description',document.getElementsByName('description')[0].value,1900);return validateFields(['name','versionnumber','softwaredate']);" upload=true>
	<input type="hidden" name="unittype" <#if unittypes.selected??>value="${unittypes.selected.name}"</#if> />								
	<input type="hidden" name="page" value="${CURRENT_PAGE.id}" />
	<#if fileobj??><input type="hidden" name="id" value="${fileobj.id}" /></#if>
	<table cellspacing="0" cellpadding="0">
		<tr>
			<td>
				<div>
					<#if unittypes.selected??>
					<fieldset>
						<legend>File configuration</legend>
						<table id="input">
							<tr>
								<th align="right">Type:</th>
								<td><@macros.dropdown list=types callMethodForKey="" onchange=""/></td>
								<th align="right" valign="top" rowspan="5">Content:</th>
								<#if filecontent?? && filecontent?contains("Software is assumed to be binary content")>
								<td rowspan="5"><textarea cols="100" rows="15" name="content" readonly style="background:silver" >${filecontent!}</textarea></td>
								<#else>
								<td rowspan="5"><textarea wrap="off" cols="100" rows="15" name="content">${filecontent!}</textarea></td>
								</#if>
							</tr>														
							<tr>
								<th align="right">Name:</th>
								<td><input name="name" type="text" size="20" <#if fileobj??>value="${fileobj.name}"</#if>/></td>
							</tr>
							<tr>
								<th align="right">Description:</th>
								<td><textarea onkeyup="ok(this.name,this.value,1900);" cols="26" rows="8" name="description"><#if fileobj??>${fileobj.description!}</#if></textarea></td>
							</tr>
							<tr>
								<th align="right">Version:</th>
								<td><input name="versionnumber" type="text" size="10" <#if fileobj??>value="${fileobj.version}"</#if>/></td>
							</tr>
							<tr>
								<th align="right">Date:</th>
								<td><input type="text" id="create_timestamp" name="softwaredate" size="20" <#if fileobj??>value="${fileobj.timestamp?string("yyyy-MM-dd")}"</#if> /> <img src="images/dateIMG.jpg" id="date_create" alt="date" /></td>
								<script type="text/javascript">
									jQuery(document).ready(function($){
										ACS.setupFromCalendar(null,{
								            field: "create_timestamp",
								            button: "date_create",
								            showsTime: false,
								            dateFormat: ACS.settings.calendar.dateFormatNoTime
								        });
									});
								</script>
							</tr>
							<tr>
								<th align="right">Target Name:</th>
								<td><input name="targetname" type="text" size="10" <#if fileobj??>value="${fileobj.targetName!}"</#if>/></td>
								<th align="right">File:</th>
								<td><input name="filename" type="file" size="40" class="file" /></td>
							<tr>
								<td></td>
								<td></td>
								<td colspan="2" align="right">
									<#if fileobj??>
										<#if fileobj.type == 'SHELL_SCRIPT'>
											<input type="button" value="Execute Script" onClick="window.location.href='${URL_MAP.SCRIPTEXECUTIONS}&fileId=${fileobj.id}&unittype=${fileobj.unittype.name?url}'">
										</#if>
										<input name="formsubmit" value="Clear" type="submit" />
										<input name="formsubmit" value="Update file" type="submit" />
									<#else>
										<input name="formsubmit" value="Upload file" type="submit" />
									</#if>
								</td>
							</tr>
						</table>
					</fieldset>
					<#else>
					<fieldset>
						<legend>Files</legend>
						<table id="input">
							<tr><td>No Unit Type selected</td></tr>
						</table>
					</fieldset>
					</#if>
				</div>
			</td>
		</tr>
	</table>
</@macros.form>
<#if unittypes.selected??>
<@macros.form name="params">
	<input type="hidden" name="unittype" <#if unittypes.selected??>value="${unittypes.selected.name}"</#if> />
	<fieldset id="parameters">
		<legend>Found ${num} file(s)</legend>
		<script>
			var evaluateDeleteChecked = function(checkbox){
				var checkbox = jQuery(checkbox);
				var tableRow = checkbox.closest("tr");
				if(tableRow.attr("checked")){
					tableRow.removeAttr("checked");
					tableRow.css("background-color","transparent");
				}else{
					tableRow.attr("checked",true);
					tableRow.css("background-color","orange");
				}
			}
		</script>
		<table id="results" style="border-collapse: collapse;background:white;width:100%">
			<tr>
              <td style="white-space:nowrap;">
              	File type: <@macros.dropdown list=filetypes default="All" callMethodForKey="" width="120px" class="submitonchange" onchange="processForm('params')"/>
              </td>
			  <td colspan="8" align="right">
			  	<input name="formsubmit" value="Delete selected files" type="submit" />
			  </td>
			</tr>
			<tr>
				<td colspan="7">&nbsp;</td>
			</tr>
			<tr>
				<th align="left" style="padding: 5px;border: 1px solid #f9f9f9;">Name</th>
				<th align="left" style="padding: 5px;border: 1px solid #f9f9f9;">Version</th>
				<th align="left" style="padding: 5px;border: 1px solid #f9f9f9;">Type</th>
				<!-- th align="left" style="padding: 5px;border: 1px solid #f9f9f9;">Description<th -->
				<th align="left" style="padding: 5px;border: 1px solid #f9f9f9;">Date</th>
				<th align="left" style="padding: 5px;border: 1px solid #f9f9f9;">Owner</th>
				<!-- th align="left" style="padding: 5px;border: 1px solid #f9f9f9;">TargetName</th -->
				<th align="left" style="padding: 5px;border: 1px solid #f9f9f9;">Export</th>
				<th align="left" style="padding: 5px;border: 1px solid #f9f9f9;">Size</th>
				<th align="left" style="padding: 5px;border: 1px solid #f9f9f9;">Delete</th>
			</tr>
			<#list files as file>
			<tr id="${file.version?replace(".","::")}">
				<td align="left" style="padding: 5px;border: 1px solid #f9f9f9;"><a href="${URL_MAP.FILES}&id=${file.id}&unittype=${file.unittype.name?url}">${file.name}</a></td>
				<td class="${file.type}" align="left" style="padding: 5px;border: 1px solid #f9f9f9;width:250px">${file.version}</td>
				<!--td align="left" style="padding: 5px;border: 1px solid #f9f9f9;"><input type="text" value="${file.name}" name="update::${file.id}::name" style="width:100%" /></td-->
				<td align="left" style="padding: 5px;border: 1px solid #f9f9f9;">${file.type}</td>
				<!-- td align="left" style="padding: 5px;border: 1px solid #f9f9f9;">${file.description!}</td -->
				<td align="left" style="padding: 5px;border: 1px solid #f9f9f9;width:120px">${file.timestamp?string(DATE_FORMAT_NOTIME)}</td>
				<td align="left" style="padding: 5px;border: 1px solid #f9f9f9;"><#if file.owner??>${file.owner.username}</#if></td>
				<!-- td align="left" style="padding: 5px;border: 1px solid #f9f9f9;">${file.targetName!}</td -->
				<td align="left" style="padding: 5px;border: 1px solid #f9f9f9;"><a href="${URL_MAP.SOFTWARE}&name=${file.name?url}&unittype=${file.unittype.name?url}&cmd=export">Binary</a></td>
				<td align="left" style="padding: 5px;border: 1px solid #f9f9f9;">${file.length}</td>
				<td align="left" style="padding: 5px;border: 1px solid #f9f9f9;"><input name="delete::${file.name}" type="checkbox" onclick="evaluateDeleteChecked(this);" /></td>
			</tr>
			</#list>
			<tr>
				<td colspan="7">&nbsp;</td>
			</tr>
		</table>
	</fieldset>
</@macros.form>
</#if>


