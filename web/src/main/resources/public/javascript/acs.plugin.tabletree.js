/*
 * *************** ABOUT tabletree.js ****************
 *
 * The table tree is a concept for making a tree-structure, just like the tree
 * structure you would find in Windows Explorer or any other browser of
 * tree-structures.
 *
 * Making such a tree-structure is not complicated if you stick to using <ul>
 * and <li> tags, the browser supports this. If you want to add expand/collapse
 * capability to the structure, there are many javascripts available on net that
 * can help you. However, if you need a table to align columns then you need a
 * different solution. The first problem with the table is that it is not nested
 * (as the ul/li-solution). (Yes, you can nest tables within tables, but that
 * will go against the alignment critera which was the very reason for using
 * tables in the first place!)
 *
 * This problem doesn't concern the layout, because when you make the table you
 * can always make the correct indentation to make it look like a tree-structure
 * (e.g. using &nbsp;), but it concerns the tree-operations. Let's say you want
 * to add collapse/expand, how would you handle that?
 *
 * This javascript is about solving this. It's about making it possible to have
 * both the qualities of the table and the qualities of a tree-structure. In
 * addition to collapse/expand it will also support filtering, which sometimes
 * will act like a collapse/expand and sometimes just add/remove some rows
 * (depending on which rows are affected by the filter).
 *
 * Ok, let's get started. First you must make a table in HTML. There are two
 * requirements to this table: 1. Each tr-tag must also include an id attribute
 * 2. The first td-tag within a row must include an image if and only if there
 * are children (in the tree) to this node. The image name must be either
 * "minus.gif" or "plus.gif". 3. For each img-tag you must add an attribute
 * (onclick) to make sure that a click triggers collapseExpand. The id (from
 * rule 1) must be an argument into that method-call.
 *
 * Another rule is: You is responsible for making the tree- strucutre
 * (indentation) look correctly.
 *
 * Here is a small example of the HTML:
 *
 * <table> <tr id=2007><td><img src=minus.gif
 * onclick=javascript:collapseExpand('2007')>2007</td></tr> <tr id=2007.jan><td>&nbsp;<img
 * src=minus.gif onclick=javascript:collapseExpand('2007.jan')>January</td></tr>
 * <tr id=2007.jan.1><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;1st</td></tr>
 * <tr id=2007.jan.15><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;15th</td></tr>
 * <tr id=2007.apr><td><img src=minus.gif
 * onclick=javascript:collapseExpand('2007.apr')>April</td></tr>
 * <tr id=2007.apr.1><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;1st</td></tr>
 * <tr id=2007.apr.15><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;15th</td></tr>
 * <tr id=2007.jul><td><img src=minus.gif
 * onclick=javascript:collapseExpand('2007.jul')>July</td></tr>
 * <tr id=2007.jul.1><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;1st</td></tr>
 * <tr id=2007.jul.15><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;;15th</td></tr>
 * <tr id=2007.oct><td><img src=minus.gif
 * onclick=javascript:collapseExpand('2007.oct')>October</td></tr>
 * <tr id=2007.oct.1><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;1st</td></tr>
 * <tr id=2007.oct.15><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;15th</td></tr>
 * <tr id=2006><td><img src=minus.gif
 * onclick=javascript:collapseExpand('2006')>2006</td></tr> <tr id=2006.jan><td>&nbsp;<img
 * src=minus.gif onclick=javascript:collapseExpand('2006.jan')>January</td></tr>
 * <tr id=2006.jan.1><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;1st</td></tr>
 * <tr id=2006.jan.15><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;15th</td></tr>
 * <tr id=2006.jul><td><img src=minus.gif
 * onclick=javascript:collapseExpand('2006.jul')>July</td></tr>
 * <tr id=2006.jul.1><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;1st</td></tr>
 * <tr id=2006.jul.15><td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;15th</td></tr>
 * </table>
 *
 * This example isn't the best, because it only contains one column. In this
 * case you could just as well have you the more common ul/li solution. However,
 * if you wanted to, you could easily add more columns here.
 *
 * When the HTML is created, make sure that you will execute a call to
 * buildtreemap each time the page is loaded:
 *
 * <body onload=javascript:buildtreemap()>
 *
 * If you want to you can trigger the filtertreemap() method to filter based on
 * the content in a specific td-tag. Read more about this in the comment
 * attached to that method.
 * 
 * UPDATE: The tables does not use non breaking space (&nbsp;) any more, 
 * padding is used instead.
 */
/**
 * The interesting part is the last part, the return,
 * where the functions that is going to be public is made accessible by ids in an object.
 * 
 * @author Jarl Andre Hubenthal
 * @author Morten Simonsen
 */
var TABLETREE = (function($){
	var topLevelMap;
	/*
	 * Get image placed in the 1st td-tag within the tr-tag. Ths image is either
	 * plus.gif or minus.gif
	 */
	function getImg(tr) {
		var tds = tr.getElementsByTagName("td");
		var imgs = tds[0].getElementsByTagName("img");
		if (imgs != null && imgs.length > 0){
			if(imgs[0].id!=null && imgs[0].id.startsWith("lock::"))
				return null;
			return imgs[0];
		}else
			return null;
	}

	/*
	 * Change from minus.gif to plus.gif or visa versa.
	 */
	function changeToOppositeImg(entry) {
		var tr = entry['_ref'];
		var img = getImg(tr);
		if (img.src.indexOf("plus.gif") > -1) {
			img.src = img.src.substring(0, img.src.length - 8) + "minus.gif";
			entry['_imgplus'] = false;
		} else if (img.src.indexOf("minus.gif") > -1) {
			img.src = img.src.substring(0, img.src.length - 9) + "plus.gif";
			entry['_imgplus'] = true;
		}
	}

	/*
	 * Builds a treemap where each node looks like this: _filtered = <true|false>
	 * _collapsed = <true|false> _imgplus = <true|false> _ref = reference to the
	 * tr-tag _parent = reference to the parent-entry _children = <no_children>
	 * _children_filtered = <no_children_filtered> zero or more references to
	 * children (their index-name is part of the id)
	 */
	function buildtreemap() {
		var results = document.getElementById("results");
		if(!results)
			return;
		var trs = results.getElementsByTagName('tr');
		if(!trs)
			return;
		var trlength = trs.length;
		topLevelMap = new Object();
		for ( var i = 0; i < trlength; i++) {
			var id = trs[i].id;
			if (id == null || id == '')
				continue;
			var entry = new Object();
			entry['_collapsed'] = false;
			entry['_filtered'] = false;
			entry['_ref'] = trs[i];
			entry['_children'] = 0;
			entry['_children_filtered'] = 0;
			var img = getImg(trs[i]);
			if (img != null) {
				var gifname = img.src.substring(img.src.length - 8);
				if (gifname.indexOf('p') > -1) // gifname = plus.gif
					entry['_imgplus'] = true;
				else
					entry['_imgplus'] = false;
			}
			var idArr;
			var hashPos = id.indexOf('#');
			if (hashPos > -1)
				idArr = id.substring(hashPos + 1).split('.');
			else
				idArr = id.split('.');
			if (idArr.length == 1) {
				topLevelMap[idArr[0]] = entry;
			} else {
				var tmpMap = topLevelMap;
				for ( var j = 0; j < idArr.length - 1; j++) {
					var idPart = idArr[j];
					tmpMap = tmpMap[idPart];
				}
				var lastId = idArr[idArr.length - 1];
				entry['_parent'] = tmpMap;
				entry['_parent']['_children']++;
				tmpMap[lastId] = entry;
			}
		}
	}

	/*
	 * Retrieves an entry from the treemap-structure, using the id (from the
	 * tr-tag).
	 */
	function getEntry(id) {
		var hashPos = id.indexOf('#');
		if (hashPos > -1)
			id = id.substring(hashPos + 1);
		var tmpMap = topLevelMap;
		var idArr = id.split('.');
		var entry;
		var arrLength = idArr.length;
		if (arrLength == 1) {
			entry = tmpMap[id];
		} else {
			for ( var i = 0; i < arrLength - 1; i++) {
				tmpMap = tmpMap[idArr[i]];
			}
			entry = tmpMap[idArr[arrLength - 1]];
		}
		return entry;
	}

	function filterJobs() {
		var filterstringElem = document.getElementById('filterstring');
		if(!filterstringElem)
			return;
		var filterstring = filterstringElem.value;
		var args = new Array(3);
		args[0] = null;
		args[1] = null;
		args[2] = filterstring;
		for ( var childKey in topLevelMap) {
			filterTraverse(topLevelMap[childKey], args,filterOrShow);
		}
	}
	
	function filterFiles() {
		try {
			var filtertypeElem = document.getElementById('filtertype');
			var filtertype = 'All';
			if (filtertypeElem != null)
				filtertype = filtertypeElem.options[filtertypeElem.selectedIndex].innerHTML;

			var filterstringElem = document.getElementById('filterstring');
			var filterstring = filterstringElem.value;

			var args = new Array(3);
			args[0] = null;
			args[1] = filtertype;
			args[2] = filterstring;

			for ( var childKey in topLevelMap) {
				filterTraverse(topLevelMap[childKey], args, filterFilesImpl);
			}
		} catch (e) {}
	}
	
	function filterFilesImpl(entry, args) {
		var tds = entry['_ref'].getElementsByTagName('td');

		var filtertype = args[1];
		if (filtertype != null && filtertype != 'All') {
			var tdInput = tds[0];
			var tdInputId = tdInput.attributes['class'].value;
			var typestate = 'filtered';
			if (filtertype.toLowerCase() != tdInputId.toLowerCase()) {
				return true;
			}
		}

		var filterstring = args[2];
		if (filterstring != null && filterstring != '') {
			var trid = entry['_ref'].id;
			if (trid.toLowerCase().replace(/::/g,".").indexOf(filterstring.toLowerCase()) == -1) {
				return true;
			}
		}

		return false;
	}

	/*
	 * Preprocess method for the filter function. Reads certain elements from the
	 * document and put them into an args-array. Then call filter(args).
	 */
	function filterParameters() {
		try {
			var filterflagElem = document.getElementsByName('filterflag')[0];
			if (filterflagElem == null)
				return;
			var filterflag = filterflagElem.options[filterflagElem.selectedIndex].innerHTML;

			var filtertypeElem = document.getElementsByName('filtertype')[0];
			var filtertype = 'All';
			if (filtertypeElem != null)
				filtertype = filtertypeElem.options[filtertypeElem.selectedIndex].innerHTML;

			var filterstringElem = document.getElementsByName('filterstring')[0];
			var filterstring = filterstringElem.value;

			var args = new Array(3);
			args[0] = filterflag;
			args[1] = filtertype;
			args[2] = filterstring;

			for ( var childKey in topLevelMap) {
				filterTraverse(topLevelMap[childKey], args, filterOrShow);
			}
		} catch (e) {}
	}

	function filterTest() {
		for ( var childKey in topLevelMap) {
			childEntry = topLevelMap[childKey];
			var args = new Array(3);
			args[0] = 'All';
			args[1] = 'All';
			args[2] = 'Morten';
			filterTraverse(childEntry, args, filterOrShow);
		}
	}

	/*
	 * The function will traverse from the entry-point. The function should be
	 * called with topLevelMap as the first entry. It will change the state of
	 * _filtered to either true or false. It will check the children before the
	 * parent (depth-first), since the parent state depend upon the children.
	 *
	 * The states are then calculated like this: 1. If this is a bottom-most child
	 * (without any img), then set the state directly. 2. If the state is 'filtered'
	 * then change the display to 'none' 2. If the state is 'not filtered', then the
	 * parent of this element should also get 'not filtered' state. 4. If the state
	 * is neither collapsed nor filtered, set display to ''
	 */
	function filterTraverse(entry, args, filterFunc) {
		entry['_children_filtered'] = 0;
		for ( var childKey in entry) {
			if (childKey.substring(0, 1) != "_") {
				filterTraverse(entry[childKey], args, filterFunc);
			}
		}

		if (entry['_imgplus'] == null)
			entry['_filtered'] = filterFunc(entry, args);

		var parentEntry = entry['_parent'];
		if (entry['_filtered']) {
			entry['_ref'].style.display = 'none';
			if (parentEntry != null) {
				parentEntry['_children_filtered']++;
				if (parentEntry['_children'] == parentEntry['_children_filtered'])
					parentEntry['_filtered'] = true;
			}
		} else {
			if (!entry['_collapse'])
				entry['_ref'].style.display = '';
			if (parentEntry != null) {
				parentEntry['_filtered'] = entry['_filtered'];
			}
		}
		entry['_children_filtered'] = 0;
	}

	/*
	 * filterOrShow - change this method according to your needs. It must return
	 * either true (to filter away) or false (to let row stay) Only rows without any
	 * children-nodes is run through this method Arguments: Entry: Its an object
	 * with these properties: entry['_ref'] = the tr-element entry['_parent'] = the
	 * parent entry entry['_collapsed'] = <true|false> entry['_filtered'] =
	 * <true|false> Tds: the array of td-element within this row Args: the array of
	 * input args to the filter-method
	 */
	var filterOrShow = function(entry, args) {
		var tds = entry['_ref'].getElementsByTagName('td');
		var filterflag = args[0];
		if (filterflag != null && filterflag != 'All') {
			var tdFlag = tds[1].innerHTML;
			var inputArr = tds[1].getElementsByTagName("input");
			if (inputArr != null && inputArr.length > 0)
				tdFlag = inputArr[0].value;

			if (filterflag == 'Read-Write' && tdFlag.indexOf('R') > -1 && tdFlag.indexOf('W') > -1) {
			} else if (filterflag == 'Read-Only' && (tdFlag.indexOf('R') > -1 && tdFlag.indexOf('W') == -1)) {
			} else if (filterflag == 'System' && tdFlag.indexOf('X') > -1) {
			} else if (filterflag == 'Always-Read' && tdFlag.indexOf('A') > -1) {
			} else if (filterflag == 'Boot-Required' && tdFlag.indexOf('B') > -1) {
			} else if (filterflag == 'Confidential' && tdFlag.indexOf('C') > -1) {
			} else if (filterflag == 'Displayable' && tdFlag.indexOf('D') > -1) {
			} else if (filterflag == 'Searchable' && tdFlag.indexOf('S') > -1) {
			} else if (filterflag == 'Device' && tdFlag.indexOf('X') < 0) {
			} else {
				return true;
			}
		}

		var filtertype = args[1];
		if (filtertype != null && filtertype != 'All') {
			var tdInput = tds[0];
			var tdInputId = tdInput.attributes['class'].value;
			var typestate = 'filtered';
			if (filtertype.toLowerCase() != tdInputId.toLowerCase()) {
				return true;
			}
		}

		var filterstring = args[2];
		if (filterstring != null && filterstring != '') {
			var trid = entry['_ref'].id;
			if (trid.toLowerCase().match(filterstring.toLowerCase()) == null) {
				return true;
			}
		}

		return false;
	}

	/*
	 * Identifies the entry from which the click-event comes. Changes the image of
	 * this entry, then go through all the children of this entry and run the
	 * recursive function collapseExpandTraverse() to change the state on all the
	 * children (if necessary).
	 */
	function collapseExpand(id) {
		var entry = getEntry(id);
		changeToOppositeImg(entry);
		for ( var childKey in entry) {
			if (childKey.substring(0, 1) != "_") {
				collapseExpandTraverse(entry[childKey], entry['_imgplus']);
			}
		}
		CookieCollapser.update(id);
	}

	/*
	 * The function will traverse the map built in buildtreemap starting from the
	 * each one of the children of the entry which actually changed state (from plus
	 * to minus or visa versa). That is because the toplevel entry of the change
	 * must be dealt with differently that all the children.
	 *
	 * The logic is as follows: 1. Set state to the new collapse-state. 2. If
	 * collapse = true (a true collapse) then set display = 'none' 3. If not
	 * collapsed nor filtered, then set display = '' 4. If the entry is not a
	 * bottom-most entry (has a img) and that img is a minus-sign, then continue
	 * this recursive function on the children-entries.
	 */
	function collapseExpandTraverse(entry, collapse) {
		entry['_collapse'] = collapse;
		if (collapse)
			entry['_ref'].style.display = 'none';
		else if (!entry['_filtered'])
			entry['_ref'].style.display = '';
		if (entry['_imgplus'] != null && !entry['_imgplus']) {
			for ( var childKey in entry) {
				if (childKey.substring(0, 1) != "_") {
					collapseExpandTraverse(entry[childKey], collapse);
				}
			}
		}
	}
	
	return {
		init:buildtreemap,
		filterParameters:filterParameters,
		filterJobs:filterJobs,
		filterFiles:filterFiles,
		collapse: collapseExpand
	};
}(jQuery));