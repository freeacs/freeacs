<script>
 /* Change the text of the option in the unittype select that has the value "." to "Choose Unit Type"  */
 jQuery(document).ready(function($){
 	$("select[name='unittype'] option[value='.']").text("Choose Unit Type");
 	<#if !(unittype.selected?? && groups.selected??)>
 	$("#imagemapGroup").html("<fieldset style='margin-left:20px;'><legend>Could not display report</legend>Please select a Unit Type and a Group</fieldset>");
 	$("#aggregationRow").hide();
 	</#if>
 });

</script>
<#if unittype.selected??>
<script>
 ACS.report.prePostback = function(url, $){
    url += "&${groups.input.key}=" + $("#${groups.input.key}").val();
    return url;
 };
</script>
<#-- <tr>
	<th align="left">Group type:</th>
	<td>
		<@macros.radio class="submitonchange" list=types onclick="" suffix=" " callMethod="" />
	</td>
</tr>
-->
<tr>
	<th align="left">Group:</th>
	<td>
		<@macros.dropdown list=groups default="Select group" width="180px" onchange="" class="submitonchange" encodeValue=false />
	</td>
</tr>
</#if>