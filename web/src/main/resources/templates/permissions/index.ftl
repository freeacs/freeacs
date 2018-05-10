	<div id="permissionsTab">
	    <ul>
	        <li><a href="#Users" title="User overview">Users</a></li>
	        <li><a hreF="#Groups" title="Group overview">Groups</a></li>                        
	    </ul>
        <div id="Users">
        	<#include "/permissions/user_tab.ftl">
        </div>
        <div id="Groups">
        	<#include "/permissions/group_tab.ftl">
        </div>
	</div>
	
	<script>
		jQuery(document).ready(function($){
			$("#permissionsTab").tabs();
		});
	</script>