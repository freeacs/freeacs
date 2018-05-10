/*global xAPS */
/**
 * A user group module for the Permissions page.
 * 
 * @deprecated was originally meant to replace the permission page, but failed to convince.
 * 
 * @author Jarl Andre Hubenthal
 */
xAPS.createModule("user.group",function($this, $super, $) {
		$this.settings = {
			//Hooks
			contents : 				"#groups-contents",
			title:					"#groups-title",
			//Group
			groupTableTemplate : 	"#group-table-template",
			groupDetailsTemplate: 	"#group-details-template",
			createGroupButtonId: 	"#button-create-group",
			listGroupsButtonId: 	"#button-list-groups",
			createGroupTemplate: 	"#group-create-template",
			saveGroupButtonId: 		"#button-save-group",
			updateGroupButtonId: 	"#button-update-group",
			//Titles
			listTitle: 				"Group overview",
			createGroupTitle: 		"Create new group",
			editGroupTitle: 		"Edit group"
		};

		$this._initForm = function() {			
			$this.initGroupList();
			$this.hookGroupListButton();
			$this.hookGroupCreateButton();
		};
		
		$this.editGroup = function(){
			$($this.settings.contents).html("Loading ...");
			$($this.settings.listGroupsButtonId).show();
			$($this.settings.createGroupButtonId).hide();
			$($this.settings.title).html($this.settings.editGroupTitle);
		};
		
		$this.showGroupList = function(){
			$($this.settings.contents).html("Loading ...");
			$($this.settings.listGroupsButtonId).hide();
			$($this.settings.createGroupButtonId).show();
			$($this.settings.title).html($this.settings.listTitle);
		};
		
		$this.createGroup = function(){
			$($this.settings.contents).html("Loading ...");
			$($this.settings.listGroupsButtonId).show();
			$($this.settings.createGroupButtonId).hide();
			$($this.settings.title).html($this.settings.createGroupTitle);
		};
		
		$this.getGroup = function(){
			return {
				"name":$("#groupname").val()
			};
		};
		
		$this.hookGroupListButton = function(){
			$($this.settings.listGroupsButtonId).click(function(e){
				$this.showGroupList();
				e.preventDefault();
				$this.initGroupList($this.cache_groups);
			});
		};
		
		$this.hookGroupCreateButton = function(){
			$($this.settings.createGroupButtonId).click(function(e){
				e.preventDefault();
				$this.createGroup();
				$($this.settings.contents).html(
					$($this.settings.createGroupTemplate).jqote()
				);
				$($this.settings.saveGroupButtonId).click(function(e){
					e.preventDefault();
					$.ajax({
			            async: true,
			            type: 'post',
			            url : 'app/group',
			            data: JSON.stringify($this.getUser()),
			            dataType : 'json',
			            contentType: 'application/json',
			            success : function(data) {
			            	$this.initGroupList();
			            }
					});
				});
			});
		};
		
		$this.initGroupLinks = function(){
			$("a.groupname").click(function(e){
				e.preventDefault();
				$this.editGroup();
				var groupname = $(this).text();
				$.ajax({
		            async: true,
		            type: 'get',
		            url : 'app/group/'+groupname,
		            dataType : 'json',
		            contentType: 'application/json',
		            success : function(data) {
		            	$($this.settings.contents).html(
	    					$($this.settings.groupDetailsTemplate).jqote(
	    						data
	    					)
		    			);
		            	$($this.settings.updateGroupButtonId).click(function(e){
		            		e.preventDefault();
		            		$.ajax({
					            async: true,
					            type: 'put',
					            url: 'app/user',
					            data: JSON.stringify($this.getUser()),
					            dataType : 'json',
					            contentType: 'application/json',
					            success : function(data) {
					            	$this.initGroupList();
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
		
		$this.initGroupList = function() {
			$this.showGroupList();
			$.ajax({
	            async: true,
	            type: 'get',
	            url : 'app/group/list',
	            dataType : 'json',
	            contentType: 'application/json',
	            success : function(data) {
	            	$this.cache_groups = data.groups;
	            	$($this.settings.contents).html(
    					$($this.settings.groupTableTemplate).jqote(
    						{"groups":data.groups}
    					)
	    			);
	            	$this.initGroupLinks();
	            }
	        });
		};
});