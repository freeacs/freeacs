	<script src="javascript/acs.module.user.js" type="text/javascript"></script>
	
	<form action="" method="get">
		<fieldset>
			<legend>Shortcut</legend>
			<input type="button" value="Create new user" id="button-create-user" />
			<input type="button" value="Back to overview" id="button-list-users" style="display:none" />
		</fieldset>
		<fieldset>
			<legend><label id="permissions-title"></label></legend>
			<div id="permissions-contents"></div>
		</fieldset>
	</form>
	
	<script type="text/html" id="user-table-template">
		<![CDATA[
		<form sction="" method="get" class="unit" name="unit">
			<table>
				<tr>
					<th align="left"><span style="margin-right:20px">Username</span></th>
					<th align="left"><span style="margin-right:20px">Full name</span></th>
					<th align="left"><span style="margin-right:20px">Group name</span></th>
				</tr>
				<% for(var i = 0;i<this.users.length;i++){ 
					var user = this.users[i]; %>
					<tr class="">
						<td>
							<a href="#" class="username"><%=user.username %></a>
						</td>
						<td><%=user.fullname %></td>
						<td><%=user.group.name %></td>
					</tr>
				<% } %>
			</table>
		</form>
		]]>
	</script>
	
	<script type="text/html" id="user-details-template">
		<![CDATA[
		<form sction="" method="put" class="unit" name="unit">
			<div class="username">
				<label for="username">Username:</label>
				&nbsp;
				<input value="<%=this.user.username %>" type="text" name="username" id="username" />
			</div>
			<div class="fullname">
				<label for="username">Fullname:</label>
				&nbsp;
				<input value="<%=this.user.fullname %>" type="text" name="fullname" id="fullname" />
			</div>
			<div class="password">
				<label for="access">Access:</label>
				&nbsp;
				<input value="<%=this.user.access %>" type="text" name="access" id="access" />
			</div>
			<div class="password">
				<label for="username">Password:</label>
				&nbsp;
				<input value="<%=this.user.password %>" type="password" name="password" id="password" />
			</div>
			<div class="group">
				<label for="groupname">Group:</label>
				&nbsp;
				<select name="groupname" id="groupname">
					<option>Select group</option>
					<% for(var i=0;i<this.groups.length;i++){ 
						var group = this.groups[i];
					%>
					<option value="<%=group.name %>" <% if(this.user.group.name===group.name){ %>selected="selected"<% } %>><%=group.name %></option>
					<% } %>
				</select>
			</div>

			<input type="submit" id="button-update-user" value="Update user" />
		</form>
		]]>
	</script>
	
	<script type="text/html" id="user-create-template">
		<![CDATA[
		<form sction="" method="post" class="unit" name="unit">
			<div class="username">
				<label for="username">Username:</label>
				&nbsp;
				<input type="text" name="username" id="username" />
			</div>
			<div class="fullname">
				<label for="username">Fullname:</label>
				&nbsp;
				<input type="text" name="fullname" id="fullname" />
			</div>
			<div class="password">
				<label for="access">Access:</label>
				&nbsp;
				<input type="text" name="access" id="access" />
			</div>
			<div class="password">
				<label for="username">Password:</label>
				&nbsp;
				<input type="password" name="password" id="password" />
			</div>
			<div class="group">
				<label for="groupname">Group:</label>
				&nbsp;
				<select name="groupname" id="groupname">
					<option>Select group</option>
					<% for(var i=0;i<this.groups.length;i++){ 
						var group = this.groups[i];
					%>
					<option value="<%=group.name %>"><%=group.name %></option>
					<% } %>
				</select>
			</div>

			<input type="submit" id="button-save-user" value="Save user" />
		</form>
		]]>
	</script>