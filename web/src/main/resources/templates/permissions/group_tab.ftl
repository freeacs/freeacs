	<script src="javascript/acs.module.user.group.js" type="text/javascript"></script>
	
	<form action="" method="get">
		<fieldset>
			<legend>Shortcut</legend>
			<input type="button" value="Create new group" id="button-create-group" />
			<input type="button" value="Back to overview" id="button-list-groups" style="display:none"/>
		</fieldset>
		<fieldset>
			<legend><label id="groups-title"></label></legend>
			<div id="groups-contents"></div>
		</fieldset>
	</form>
	
	<script type="text/html" id="group-table-template">
		<![CDATA[
		<form sction="" method="get" class="unit" name="unit">
			<table>
				<tr>
					<th align="left"><span style="margin-right:20px">Group name</span></th>
				</tr>
				<% for(var i = 0;i<this.groups.length;i++){ 
					var group = this.groups[i]; %>
					<tr class="">
						<td>
							<a href="#" class="groupname"><%=group.name %></a>
						</td>
					</tr>
				<% } %>
			</table>
		</form>
		]]>
	</script>
	
	<script type="text/html" id="group-details-template">
		<![CDATA[
		<form sction="" method="put" class="unit" name="unit">
			<div class="groupname">
				<label for="groupname">Group name:</label>
				&nbsp;
				<input value="<%=this.name %>" type="text" name="groupname" id="username" />
			</div>

			<input type="submit" id="button-update-group" value="Update group" />
		</form>
		]]>
	</script>
	
	<script type="text/html" id="group-create-template">
		<![CDATA[
		<form sction="" method="post" class="unit" name="unit">
			<div class="groupname">
				<label for="groupname">Name:</label>
				&nbsp;
				<input type="text" name="groupname" id="groupname" />
			</div>

			<input type="submit" id="button-save-group" value="Save group" />
		</form>
		]]>
	</script>