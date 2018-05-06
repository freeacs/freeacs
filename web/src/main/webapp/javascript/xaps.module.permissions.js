/*global Dom */
/**
 * Used on Permissions page to add a Unit Type permission
 * 
 * Depends on xaps.legacy.js, escpecially the Dom object (a simple wrapper around document.getElementsByName("ddd")[0].. etc)
 * 
 * It uses javascript on the onclick attributes of the trash images, 
 * so this is a bit difficult to maintain without totally refactoring it.
 * 
 * Should therefore be converted to a module .... :) 
 * 
 * And should use jQuery for dom manipulation and avoid html concatenation.
 * 
 * TEMPLATES
 * /WebContent/templates/permissions/permissionscreatepage.ftl // Includes this script
 * /WebContent/templates/permissions/permissionsdetailspage.ftl // Includes this script
 * /WebContent/templates/permissions/permissionsoverviewpage.ftl // Does not include any script
 * 
 * @author Jarl Andre Hubenthal
 */
function addPermission(){
	var permissions = Dom.id("permissions");
	if(Dom.name('permission')==null)
		permissions.innerHTML="";
	var newPermission = Dom.create("div");
	var ut = Dom.name("unittype");
	if(ut!=null){
		var utS = ut[ut.selectedIndex].value;
		if(utS==".")
			return;
		utS = ut[ut.selectedIndex].text;
		var p = Dom.name("profile");
		if(p!=null){
			var pS = p[p.selectedIndex].value;
			if(pS!="."){
				pS = p[p.selectedIndex].text;
			}
		}
	}
	var value = utS+(pS!="."?" <b>&gt;</b> "+pS:"");
	newPermission.innerHTML=value;
	var hidden = Dom.create("input","hidden","permission",null,utS+(pS!="."?"\\"+pS:""));
	Dom.add(hidden,newPermission);
	newPermission.innerHTML+="&nbsp;<img tabindex'-1' title='Remove permission' src='images/trash.gif' alt='trash' onclick='Dom.remove(this.parentNode); var permissions = Dom.id(\"permissions\"); if(Dom.name(\"permission\")==null) permissions.innerHTML =\"No permissions is defined\";' />";
	Dom.add(newPermission,permissions);
}