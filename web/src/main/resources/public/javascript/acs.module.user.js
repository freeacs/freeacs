/*global xAPS */
/**
 * A user module for the Permissions page.
 * 
 * @deprecated was originally meant to replace the permission page, but failed to convince.
 * 
 * @author Jarl Andre Hubenthal
 */
ACS.createModule("user",function($this, $super, $) {
		$this.settings = {
			//Hooks
			contents : 				"#permissions-contents",
			title:					"#permissions-title",
			//User
			userTableTemplate : 	"#user-table-template",
			userDetailsTemplate: 	"#user-details-template",
			createUserButtonId: 	"#button-create-user",
			listUsersButtonId: 		"#button-list-users",
			createUserTemplate: 	"#user-create-template",
			saveUserButtonId: 		"#button-save-user",
			updateUserButtonId: 	"#button-update-user",
			//Titles
			listTitle: 				"Overview",
			createUserTitle: 		"Create new user",
			editUserTitle: 			"Edit user"
		};
		
		// Override
		$this._initForm = function() {			
			$this.initUserList();
			$this.hookUserCreateButton();
			$this.hookUserListButton();
			$($this.settings.title).html($this.settings.listTitle);
		};
		
		$this.editUser = function(){
			$($this.settings.contents).html("Loading ...");
			$($this.settings.listUsersButtonId).show();
			$($this.settings.createUserButtonId).hide();
			$($this.settings.title).html($this.settings.editUserTitle);
		};
		
		$this.showUserList = function(){
			$($this.settings.contents).html("Loading ...");
			$($this.settings.listUsersButtonId).hide();
			$($this.settings.createUserButtonId).show();
			$($this.settings.title).html($this.settings.listTitle);
		};
		
		$this.createUser = function(){
			$($this.settings.contents).html("Loading ...");
			$($this.settings.listUsersButtonId).show();
			$($this.settings.createUserButtonId).hide();
			$($this.settings.title).html($this.settings.createUserTitle);
		};
		
		$this.getUser = function(){
			return {
				"username":		$("#username").val(),
				"fullname":		$("#fullname").val(),
				"password":		$("#password").val(),
				"access":		$("#access").val(),
				"group":		$("#groupname").val()
			};
		};
		
		$this.hookUserListButton = function(){
			$($this.settings.listUsersButtonId).click(function(e){
				$this.showUserList();
				e.preventDefault();
				$this.initUserList($this.cache_users);
			});
		};
		
		$this.hookUserCreateButton = function(){
			$($this.settings.createUserButtonId).click(function(e){
				$this.createUser();
				e.preventDefault();
				$($this.settings.contents).html(
					$($this.settings.createUserTemplate).jqote({"groups":$this.parent.group.page.cache_groups})
				);
				$($this.settings.saveUserButtonId).click(function(e){
					e.preventDefault();
					$.ajax({
			            async: true,
			            type: 'post',
			            url : 'app/user',
			            data: JSON.stringify($this.getUser()),
			            dataType : 'json',
			            contentType: 'application/json',
			            success : function(data) {
			            	$this.initUserList();
			            }
					});
				});
			});
		};
		
		$this.initUserLinks = function(){
			$("a.username").click(function(){
				var username = $(this).text();
				$this.editUser();
				$.ajax({
		            async: true,
		            type: 'get',
		            url : 'app/user/'+username,
		            dataType : 'json',
		            contentType: 'application/json',
		            success : function(data) {
		            	$($this.settings.contents).html(
	    					$($this.settings.userDetailsTemplate).jqote(
	    							{
	    								"user":		data,
	    								"groups":	$this.parent.group.page.cache_groups
	    							}
	    					)
		    			);
		            	$($this.settings.updateUserButtonId).click(function(e){
		            		e.preventDefault();
		            		$.ajax({
					            async: true,
					            type: 'put',
					            url: 'app/user',
					            data: JSON.stringify($this.getUser()),
					            dataType : 'json',
					            contentType: 'application/json',
					            success : function(data) {
					            	$this.initUserList();
					            },
					            error: function(e,f,g){
					            	alert("COuld not save user");
					            }
							});
		            	});
		            }
				});
			});
		};
		
		$this.initUserList = function() {
			$this.showUserList();
			$.ajax({
	            async: true,
	            type: 'get',
	            url : 'app/user/list',
	            dataType : 'json',
	            contentType: 'application/json',
	            success : function(data) {
	            	$this.cache_users = data.users;
	            	$($this.settings.contents).html(
    					$($this.settings.userTableTemplate).jqote(
    						{
    							"users":	data.users
    						}
    					)
	    			);
	            	$this.initUserLinks();
	            }
	        });
		};
});