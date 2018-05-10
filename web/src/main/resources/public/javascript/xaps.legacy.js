/**
 * LEGACY XAPS JAVASCRIPT CODE
 * 
 * The following methods and variables are global and independent objects.
 * 
 * Code that lies in here are not meant to be updated and fixed.
 * 
 * If it needs to be fixed, 
 * it should be evaluated if the new code fits into an existing module
 * or even into the xaps.js.
 *
 * If a function is called globally and is not standard javascript,
 * then it is probably in this file.
 * 
 * Global objects is evil.
 * 
 * @author Jarl Andre Hubenthal
 */

/**
 * PROTOTYPE ADDONS
 * 
 * Augment String.prototype with useful functions
 */
String.prototype.trim = function() {
	return this.replace(/^\s*/, "").replace(/\s*$/, "");
};
String.prototype.startsWith = function(str) {
	return (this.match("^" + str) == str);
};
String.prototype.endsWith = function(str) {
	return (this.match(str + "$") == str);
};
String.prototype.bool = function() {
	return (/^true$/i).test(this);
};

/**
 * Setting selectedindex to the first item in the SELECT list, usually the
 * "Select .. "
 * 
 * @param name
 * @return
 */
function deselect(name) {
	selects = document.getElementsByName(name);
	if (selects.length > 0)
		selects[0].options.selectedIndex = 0;
}

/**
 * Clears the value of a given element with the specified name.
 * 
 * @param name
 * @return
 */
function clearinput(name) {
	inputs = document.getElementsByName(name);
	if (inputs.length > 0)
		inputs[0].value = "";
}

/**
 * Clears the values of the given elements with the specified name.
 * 
 * @param names
 * @return
 */
function clearinputs(names) {
	if (names instanceof Array) {
		for ( var i = 0; i < names.length; i++) {
			clearinput(names[i]);
		}
	}
}

/**
 * Used on firmware poage to check if description is within valid range (length)
 * 
 * @param name
 * @param txt
 * @param length
 * @return
 */
function ok(name, txt, length) {
	var field = document.getElementsByName(name)[0];
	if (txt == null || txt.length <= length)
		return;
	var txtChopped = txt.substring(0, length);
	field.value = txtChopped;
	alert("Text in " + name + " was too long (" + txt.length
			+ ")! Chopped down to " + length + " characters!");
}

/**
 * Focuses on the last input of type INPUT or SELECT
 * 
 * @return
 */
function focusOnTheFirstInput() {
	var f = document.forms.length;
	var i = 0;
	var pos = -1;
	while (pos == -1 && i < f) {
		var e = document.forms[i].elements.length;
		var j = 0;
		while (pos == -1 && j < e) {
			var curElm = document.forms[i].elements[j];
			var curTagName = curElm.tagName;
			if ((((curTagName == 'INPUT' && curElm.type != "hidden") || curTagName == 'SELECT'))
					&& curElm.style.display != 'none')
				pos = j;
			j++;
		}
		i++;
	}
	if (pos >= 0) {
		var toFocus = document.forms[i - 1].elements[pos];
		if (toFocus != null)
			toFocus.focus();
	}
}

/**
 * Used by /WEB-INF/templates/windowpage.ftl to show or hide service window
 * settings.
 * 
 * Calls toggleDisabled
 * 
 * @param toShow
 * @param toHide
 * @param toToggle
 * @return
 */
function toggleWindow(toShow, toHide, toToggle) {
	var el = document.getElementById(toShow);
	el.style.display = '';
	var elementToHide = document.getElementById(toHide);
	elementToHide.style.display = 'none';
	var toggle = document.getElementById(toToggle);
	toggleDisabled(toggle);
}
function toggleDisabled(el) {
	if (el.tagName != undefined && el.tagName == 'SELECT')
		el.selectedIndex = 0;
	else if (el.tagName != undefined && el.tagName == 'TR') {
		var curDisplay = el.style.display;
		if (curDisplay != null) {
			el.style.display = (curDisplay == 'none' ? '' : 'none');
		}
	}
	if (el.childNodes && el.childNodes.length > 0) {
		for ( var x = 0; x < el.childNodes.length; x++) {
			toggleDisabled(el.childNodes[x]);
		}
	}
}

/**
 * The following three methods are used by most pages to automatically click all
 * delete boxes in parameter table.
 */
var checkAllCheckBoxesState = "unchecked";
function check(prefix) {
	if (checkAllCheckBoxesState == "unchecked") {
		checkAll(prefix);
		checkAllCheckBoxesState = "checked";
	} else if (checkAllCheckBoxesState == "checked") {
		uncheckAll(prefix);
		checkAllCheckBoxesState = "unchecked";
	}
}
function checkAll(prefix) {
	var doc = document.documentElement.ownerDocument;
	var results = doc.getElementById("results");
	if (results == null)
		return false;
	var trs = results.getElementsByTagName("tr");
	for ( var a = 0; a < trs.length; a++) {
		var tr = trs[a];
		if (tr.style.display == 'none')
			continue;
		var fields = tr.getElementsByTagName("input");
		for ( var i = 0; i < fields.length; i++) {
			var field = fields[i];
			if (field.type == "checkbox" && field.name.startsWith(prefix))
				field.checked = true;
		}
	}
	return true;
}
function uncheckAll(prefix) {
	var doc = document.documentElement.ownerDocument;
	var results = doc.getElementById("results");
	if (results == null)
		return false;
	var trs = results.getElementsByTagName("tr");
	for ( var a = 0; a < trs.length; a++) {
		var tr = trs[a];
		if (tr.style.display == 'none')
			continue;
		var fields = tr.getElementsByTagName("input");
		for ( var i = 0; i < fields.length; i++) {
			var field = fields[i];
			if (field.type == "checkbox" && field.name.startsWith(prefix))
				field.checked = false;
		}
	}
	return true;
}

var collapse_state = 0;
/**
 * Full collapse of the parameter table and the status
 * 
 * @return
 */
function collapse() {
	var table = document.getElementById("results");
	var trs = table.getElementsByTagName("tr");
	var listImgs = new Array();
	var listImgsCount = 0;
	for ( var i = 0; i < trs.length; i++) {
		var imgs = trs[i].getElementsByTagName("img");
		if (imgs != null
				&& imgs.length > 0
				&& imgs[0].src
						.indexOf((collapse_state == 1) ? "plus" : "minus") > -1) {
			listImgs[listImgsCount] = imgs[0];
			listImgsCount++;
		}
	}
	for ( var x = listImgs.length - 1; x >= 0; x--) {
		try {
			listImgs[x].onclick();
		} catch (e) {
		}
	}
	collapse_state = ((collapse_state == 1) ? 0 : 1);
	var collapseimg = document.getElementById("collapseimage");
	if (collapseimg != null) {
		if (collapse_state == 0)
			collapseimg.src = "images/minus.gif";
		else
			collapseimg.src = "images/plus.gif";
	}
	return false;
}

/**
 * Not used. pleaseWait function is removed. Amd therefore the function will
 * only redirect.
 * 
 * Modules should use <code>xAPS.pleaseWait(message);</code>
 * 
 * @param url
 * @param messageToDisplayWhileWaiting
 */
function goToUrlAndWait(url, messageToDisplayWhileWaiting) {
	// pleaseWait(messageToDisplayWhileWaiting);
	window.location = url;
}

/**
 * Go back to url stored in element with id "history"
 * 
 * @return
 */
function goBack() {
	var history = document.getElementById("history");
	if (history != null) {
		var url = history.value;
		window.location = url;
	}
	return false;
}

/**
 * Go to the job with the specified name.
 * 
 * @param job
 * @return
 */
function goToJob(job) {
	var arr = job.split(":@:");
	var string = "?page=job&job=" + arr[0] + "&unittype=" + arr[1];
	window.location = string;
}

/**
 * Go to the group with the specified name
 * 
 * @param group
 * @return
 */
function goToGroup(group) {
	var arr = group.split(":@:");
	var string = "?page=group&group=" + arr[0] + "&unittype=" + arr[1];
	window.location = string;
}

/**
 * Adds the given text to a _message element with prefix
 * 
 * @param text
 * @param prefix
 * @return
 */
function addMessage(text, prefix) {
	var field = document.getElementById(prefix + "_message");
	if (field != null)
		field.innerHTML = text;
	return true;
}

/**
 * Usable Dom functions
 */
var Dom = {
	get : function(el) {
		if (typeof el === 'string') {
			return document.getElementById(el);
		} else {
			return el;
		}
	},
	getByName : function(el) {
		if (typeof el === 'string') {
			list = document.getElementsByName(el);
			if (list.length > 0)
				return list[0];
			return el;
		} else {
			return el;
		}
	},
	getChild : function(id, parent) {
		element = parent;
		children = element.childNames;
		for ( var child in children) {
			if (child.name == element.id)
				return getChild(id, child);
		}
		return null;
	},
	getChildThatStartsWith : function(id, parent) {
		children = parent.childNames;
		for ( var child in children) {
			alert(child.name + "-" + id);
			if (child.name.startsWith(id))
				return getByName(id);
			return getChildThatStartsWith(id, child);
		}
	},
	id : function(el) {
		if (typeof el === 'string') {
			return document.getElementById(el);
		} else {
			return el;
		}
	},
	name : function(el) {
		if (typeof el === 'string') {
			var nameList = this.names(el);
			if (nameList.length > 0)
				return nameList[0];
			return null;
		} else {
			return el;
		}
	},
	child : function(id, parent) {
		var element = parent;
		var children = element.childNames;
		for ( var child in children) {
			if (child.name == element.id)
				return this.child(id, child);
		}
		return null;
	},
	focus : function(elementName) {
		var inputFields = Dom.names(elementName);
		if (inputFields.length > 0)
			inputFields[inputFields.length - 1].focus();
	},
	add : function(el, dest) {
		el = this.id(el);
		dest = this.id(dest);
		dest.appendChild(el);
	},
	create : function(tag, type, name, id, value, width) {
		if (typeof tag == "undefined" || tag == null)
			throw ("Wrong usage! Elements must have a tagName.");
		var newElement = document.createElement(tag);
		if (typeof type != "undefined" && type != null)
			newElement.type = type;
		if (typeof name != "undefined" && name != null)
			newElement.name = name;
		if (typeof id != "undefined" && id != null)
			newElement.id = id;
		if (typeof value != "undefined" && value != null)
			newElement.value = value;
		if (typeof width != "undefined" && width != null)
			newElement.style.width = width;
		return newElement;
	},
	clone : function(el) {
		if (typeof el != "undefined" && el != null)
			return el.cloneNode;
		return el;
	},
	remove : function(el) {
		el = this.id(el);
		el.parentNode.removeChild(el);
	},
	names : function(name) {
		return document.getElementsByName(name);
	},
	disable : function(elToHide, tagNameToMatch) {
		if (tagNameToMatch == null || elToHide.tagName == tagNameToMatch)
			elToHide.style.display = elToHide.style.display == '' ? 'none' : '';
		if (elToHide.childNodes && elToHide.childNodes.length > 0) {
			for ( var x = 0; x < elToHide.childNodes.length; x++) {
				this.disable(elToHide.childNodes[x], tagNameToMatch);
			}
		}
	}
};

/**
 * Removes an element from the dom model.
 * 
 * @param element
 * @return
 */
function removeMe(element) {
	var input = element.parentNode.getElementsByTagName("INPUT")[0];
	if (input != null && input.type != null && input.type == "text") {
		Dom.remove(element.parentNode);
	}
}
/**
 * Gets next sibling.
 * 
 * @param el
 * @return
 */
function getNextSibling(el) {
	var n = el;
	do
		n = n.nextSibling;
	while (n && n.nodeType != 1);
	return n;
}

/**
 * Gets previous sibling
 * 
 * @param el
 * @return
 */
function getPreviousSibling(el) {
	var p = el;
	do
		p = p.previousSibling;
	while (p && p.nodeType != 1);
	return p;
}

/**
 * Moves an element up (before) its sibling
 * 
 * @param element
 * @return
 */
function goUp(element) {
	try {
		var parent = element.parentNode;
		var next = getPreviousSibling(parent);
		parent.parentNode.insertBefore(parent, next);
	} catch (e) {
	}
}

/**
 * Moves an element down (after) its sibling.
 * 
 * @param element
 * @return
 */
function goDown(element) {
	try {
		var parent = element.parentNode;
		var next = getNextSibling(parent);
		parent.parentNode.insertBefore(next, parent);
	} catch (e) {
	}
}

/**
 * The following functions used in Unit Type Parameter page.
 * 
 * @return
 */
function validateParameter() {
	var radios = document.getElementsByName("param:main");

	var error = "";

	var nameOk = false;
	var name = document.getElementsByName("name::1")[0];
	if (name != null && name.value != null && name.value.length > 0)
		nameOk = true;

	if (!nameOk) {
		error += "Please set the Name\n";
		name.focus();
	}

	var radiosOk = false;
	for ( var x = 0; x < radios.length; x++) {
		if (radios[x].checked) {
			radiosOk = true;
		}
	}

	if (!radiosOk)
		error += "Please set the Flags";

	if (error.length > 0) {
		alert(error);
		throw (error);
	}

	return radiosOk && nameOk;
}

/**
 * Validate and store flag in hidden element.
 * 
 * @param el
 * @return
 */
function buildFlags(el) {
	var hiddenFlag = document.getElementsByName("flag::1")[0];
	if (hiddenFlag == null)
		return;

	var radios = document.getElementsByName("param:main");
	for ( var x = 0; x < radios.length; x++) {
		if (radios[x].checked) {
			var mainRadio = radios[x].value;
			break;
		}
	}

	if (mainRadio != null)
		showAttributes();

	var b = Dom.getByName("param:bootrequired");
	var s = Dom.getByName("param:searchable");
	var c = Dom.getByName("param:confidential");
	var a = Dom.getByName("param:always");
	var d = Dom.getByName("param:displayable");

	var flag = mainRadio;
	if (flag == "R") {
		display(Dom.get("alwaysread"), true);
		if (a != null && a.value != null && a.checked)
			flag += a.value;
	} else
		display(Dom.get("alwaysread"), false);

	if (c != null && c.value != null && c.checked)
		flag += c.value;
	if (b != null && b.value != null && b.checked)
		flag += b.value;
	if (s != null && s.value != null && s.checked)
		flag += s.value;
	if (d != null && d.value != null && d.checked)
		flag += d.value;

	if (flag.indexOf("C") > -1 && flag.indexOf("D") > -1
			&& (el == c || el == d)) {
		alert("Cannot combine Confidential with Display");
		el.checked = false;
		flag = flag.replace(el.value, "");
	}
	
	if (flag.indexOf("B") > -1 && flag.indexOf("W") == -1
			&& el == b) {
		alert("Cannot combine BootRequired with not-ReadWrite parameter");
		el.checked = false;
		flag = flag.replace(el.value, "");
	}

	var flagReadOnly = window.flagReadOnly || null;
	var newFlagReadOnly = (flag.indexOf("R") > -1 && flag.indexOf("W") < 0);
	if (flagReadOnly !== newFlagReadOnly || (!flagReadOnly && !newFlagReadOnly)) {
		flagReadOnly = newFlagReadOnly;
		var elm = document.getElementById("addparameters");
		if (elm != null) {
			var isNotReadOnlyAndHidden = !newFlagReadOnly
					&& elm.style.display == "none";
			var isReadOnlyAndVisible = newFlagReadOnly
					&& elm.style.display == "";
			if (isNotReadOnlyAndHidden || isReadOnlyAndVisible)
				toggleDisabled(elm);
		}
	}

	hiddenFlag.value = flag;
}
/**
 * show flag attributes (displayable, searchable etc)
 * 
 * @return
 */
function showAttributes() {
	var attrs = document.getElementsByName("flag-attributes");
	for ( var i = 0; i < attrs.length; i++) {
		attrs[i].style.display = "";
	}
}

/**
 * sets display on an element based on a boolean.
 * 
 * @param elm
 * @param display
 *            boolean
 * @return
 */
function display(elm, display) {
	if (display)
		elm.style.display = "";
	else
		elm.style.display = "none";
}

/**
 * Disables an element.
 * 
 * @param elm
 * @param disable
 *            boolean
 * @return
 */
function disable(elm, disable) {
	if (disable)
		elm.disabled = true;
	else
		elm.disabled = false;
}

/**
 * Add parameter value and focus on the input field-
 * 
 * @param button
 * @return
 */
function addParameterValue(button) {
	tmp = "value::1";
	newDiv = document.createElement('div');
	newField = createInput("text", tmp + "::field", "");
	newDiv.appendChild(newField);
	Dom.add(newDiv, tmp);
	newDiv.innerHTML += "&nbsp;<a href='#' tabindex='-1' onclick='return goUp(this);' title='Up'><img border='0' src='images/up.jpeg' alt='up' /></a>&nbsp;<a href='#' tabindex='-1' onclick='return goDown(this);' title='Down'><img border='0' src='images/down.jpeg' alt='down' /></a>&nbsp;&nbsp;<a href='#' tabindex='-1' onclick='removeMe(this)' title='Remove'><img src='images/trash.gif' alt='trash' title='Delete' border='0' /></a><br />";
	button.blur();
	focusInput(tmp + "::field");
}

/**
 * Focus on the given input field name (the last one)
 * 
 * @param inputFieldName
 * @return
 */
function focusInput(inputFieldName) {
	var inputFields = document.getElementsByName(inputFieldName);
	if (inputFields.length > 0)
		inputFields[inputFields.length - 1].focus();
}

/**
 * Create input element with given name and value
 * 
 * @param type
 * @param name
 * @param value
 * @return
 */
function createInput(type, name, value) {
	var input = document.createElement('input');
	input.type = type;
	input.name = name;
	input.setAttribute("value", value);
	input.style.width = "300px";
	return input;
}

/**
 * USED IN: ADD UNIT TYPE PARAMETERS
 */
function addUnittypeParameterFlag(fieldName) {
	var field = document.getElementsByName(fieldName)[0];
	var select = document.getElementsByName("select::" + fieldName)[0];
	var value = select.value;
	var canBeInserted = true;
	var arrLength = value.length;
	var fieldValue = field.value;
	if (arrLength == 1) {
		if (fieldValue.indexOf(value) < 0)
			field.value += value;
		else
			alert("Duplicate flag: " + value);
	} else if (arrLength > 1) {
		for ( var i = 0; i < arrLength; i++) {
			for ( var h = 0; h < fieldValue.length; h++) {
				if (fieldValue.charAt(h) == value.charAt(i)) {
					canBeInserted = false;
					break;
				}
			}
			if (!canBeInserted)
				break;
		}
		if (canBeInserted)
			field.value += value;
		else
			alert("Duplicate flag: " + value);
	}
	select.selectedIndex = 0;
}

/**
 * USED IN: ADD UNIT TYPE PARAMETERS
 */
function delkey(e) {
	var kC = (window.event) ? event.keyCode : e.keyCode;
	var Esc = (window.event) ? 46 : e.DOM_VK_DELETE;
	if (kC == Esc)
		return true;
}
/**
 * USED IN: ADD UNIT TYPE PARAMETERS
 */
function tabkey(e) {
	return (e.keyCode || event.which) == 9;
}

/**
 * Usable functions for checking what key is typed in an event
 * 
 * @param e
 * @param code
 * @return
 */
function isCode(e, code) {
	e = e || window.event;
	var unicode = e.charCode ? e.charCode : e.keyCode ? e.keyCode : 0;
	return (unicode == code);
}

function isEscape(event) {
	return isCode(event, 27);
}

function isUp(event) {
	return isCode(event, 38);
}

function isDown(event) {
	return isCode(event, 40);
}

function isEnter(event) {
	return isCode(event, 13);
}

function isBackspace(event) {
	return isCode(event, 8);
}

function getEventCharacter(e) {
	e = e || window.event;
	var unicode = e.charCode ? e.charCode : e.keyCode ? e.keyCode : 0;
	var character = String.fromCharCode(unicode);
	return character;
}

function getCorrectLetter(evt) {
	var shiftPressed = evt.shiftKey;
	var altPressed = evt.altKey;
	var ctrlPressed = evt.ctrlKey;
	var unicode = evt.charCode ? evt.charCode : evt.keyCode ? evt.keyCode : 0;
	var character = String.fromCharCode(unicode);
	if (shiftPressed)
		return character;
	return character.toLowerCase();
}

/**
 * Used by upgrade page. Used in UpgradeUnit.java.
 * 
 * @param e
 * @return
 */
function KeyPressed(e) {
	if (isEnter(e)) {
		var targ;
		if (e.target)
			targ = e.target;
		else if (e.srcElement)
			targ = e.srcElement;
		var type = targ.getAttribute("type");
		if (targ.tagName == 'INPUT' && type != null && type == "submit")
			targ.click();
		if (targ.tagName == 'INPUT')
			return false;
	}
	return true;
}

/**
 * 
 * This method validates a table, with hard coded id: input
 * 
 * @param fieldsToBeValidated
 * @return Boolean
 * 
 */
function validateFields(fieldsToBeValidated, event) {
	var table = document.getElementById("input");

	var inputFields = table.getElementsByTagName("input");
	var selectFields = table.getElementsByTagName("select");

	var message = "";
	var validated = true;

	var borderRed = "border:2px solid red;";

	for ( var j = 0; j < inputFields.length; j++) {
		for ( var g = 0; g < fieldsToBeValidated.length; g++) {
			if (fieldsToBeValidated[g] == inputFields[j].name) {
				inputFields[j].style.cssText = "";
				var elm = document.getElementsByName(fieldsToBeValidated[g])[0];
				if (!elm.disabled) {
					if (inputFields[j].value.trim().length == 0) {
						message += inputFields[j].name + " is empty\n";
						inputFields[j].style.cssText = borderRed;
						validated = false;
					}
				}
			}
		}
	}

	for (j = 0; j < selectFields.length; j++) {
		for (g = 0; g < fieldsToBeValidated.length; g++) {
			if (fieldsToBeValidated[g] == selectFields[j].name) {
				selectFields[j].style.cssText = "";
				elm = document.getElementsByName(fieldsToBeValidated[g])[0];
				if (!elm.disabled) {
					if (selectFields[j].value == "."
							|| selectFields[j].value.startsWith("Select")
							|| selectFields[j].value.startsWith("Choose")
							|| selectFields[j].value.startsWith("Any")) {
						message += selectFields[j].name + " is not selected\n";
						selectFields[j].style.cssText = borderRed;
						validated = false;
					}
				}
			}
		}
	}

	if (!validated) {
		if (event) {
			event.preventDefault();
		}
	}

	return validated;
}

/**
 * return the value of the radio button that is checked return an empty string
 * if none are checked, or there are no radio buttons
 */
function getCheckedValue(radioObj) {
	if (!radioObj)
		return "";
	var radioLength = radioObj.length;
	if (radioLength == undefined)
		if (radioObj.checked)
			return radioObj.value;
		else
			return "";
	for ( var i = 0; i < radioLength; i++) {
		if (radioObj[i].checked) {
			return radioObj[i].value;
		}
	}
	return "";
}

/**
 * Validates a unit type parameter flags content
 * 
 * @param field
 * @return
 */
function validateFlag(field) {
	var flag = field.value;
	for ( var i = 0; i < flag.length; i++) {
		if (flag.charAt(i).toLowerCase() == "x")
			continue;
		else if (flag.charAt(i).toLowerCase() == "s")
			continue;
		else if (flag.charAt(i).toLowerCase() == "i")
			continue;
		else if (flag.charAt(i).toLowerCase() == "w")
			continue;
		else if (flag.charAt(i).toLowerCase() == "r")
			continue;
		else if (flag.charAt(i).toLowerCase() == "c")
			continue;
		else if (flag.charAt(i).toLowerCase() == "a")
			continue;
		else if (flag.charAt(i).toLowerCase() == "d")
			continue;
		else {
			alert("A flag can contain: SIXRWAC");
			field.value = document.getElementsByName(field.name + "::Cache")[0].value;
			break;
		}
	}
}

function setMatcherId() {
	try {
		var matcher = document.getElementsByName("matcherid")[0];
		var radio = getCheckedValue(document.form1.protocol);
		if (radio == "OPP") {
			// matcher.value="";
			matcher.disabled = false;
		} else if (radio == "TR-069") {
			// matcher.value="";
			matcher.disabled = true;
		}
	} catch (e) {
		// Do nothing
	}
}

/**
 * Was used in window page. Not used now, since dropdowns is used instead.
 * 
 * @param field
 * @return
 */
function checknumber(field) {
	var x = field.value;
	var anum = /(^\d+$)|(^\d+\.\d+$)/;
	var testresult;
	if (anum.test(x)) {
		var u = x.substr(1, 0);
		if (u == "0")
			x = x.substr(2);
		var y = parseInt(x);
		if (y > 2400) {
			alert(field.name + " must be a number between 0000 and 2400");
			field.value = "0000";
			testresult = false;
		} else
			testresult = true;
	} else if (x.trim() == "") {
		field.value = "0000";
		testresult = false;
	} else {
		alert(field.name + " can only contain numbers.");
		field.value = "0000";
		testresult = false;
	}
	return (testresult);
}

/**
 * used by parameter tables on all pages to display or hide input fields
 * 
 * @param id
 * @param checkbox
 * @return
 */
function toggle(id, checkbox) {
	try {
		var el = document.getElementsByName(id)[0];

		if (typeof el === 'undefined' || el === null)
			return;

		if (checkbox.name.startsWith("create") && !checkbox.checked
				&& el.style.display == "none")
			return;

		var display = el.style.display ? '' : 'none';
		el.style.display = display;

		if (display == '')
			el.focus();

	} catch (e) {
		alert(e);
	}
}

/**
 * If field value is nothing ("") uncheck checkbox, else check the checkbox.
 * 
 * @param field
 * @param prefix
 * @return
 */
function updateCheckbox(field, prefix) {
	try {
		if (field.value.trim() == "") {
			document.getElementsByName(prefix + "::" + field.name)[0].checked = false;
		} else {
			document.getElementsByName(prefix + "::" + field.name)[0].checked = true;
		}
	} catch (err) {
		// No action.. create:: checkbox does not exist
	}
}

/**
 * Confirms deletion
 * 
 * @param item
 * @return
 */
function processDelete(item) {
	return confirm("Do you want to " + item + "?");
}

/**
 * Could be called valueEquals effectively
 * 
 * @param name
 * @param toCompareAgainst
 * @return
 */
function processCompare(name, toCompareAgainst) {
	return document.getElementsByName(name)[0].value != toCompareAgainst;
}

/**
 * Almost the same as processDelete, but this one can be used for other use
 * cases.
 * 
 * @param question
 * @return
 */
function processUpdate(question) {
	return confirm(question);
}

/**
 * Processes form1
 * 
 * @return
 */
function processForm1() {
	document.form1.submit();
}

/**
 * Processes the given form
 * 
 * @param formId
 * @return
 */
function processForm(formId) {
	var list = document.getElementsByName(formId);
	if (list.length > 0 && list[0] != null)
		list[0].submit();
}

/**
 * Tests to see if a string is a number (including desimal)
 * 
 * @param sText
 * @return
 */
function IsNumeric(sText) {
	var ValidChars = "0123456789.";
	var IsNumber = true;
	var Char;

	for ( var i = 0; i < sText.length && IsNumber == true; i++) {
		Char = sText.charAt(i);
		if (ValidChars.indexOf(Char) == -1) {
			IsNumber = false;
		}
	}
	return IsNumber;
}

/**
 * Tests to see if a string is a normal integer
 * 
 * @param sText
 * @return
 */
function IsNumber(sText) {
	var ValidChars = "0123456789";
	var IsNumber = true;
	var Char;

	for ( var i = 0; i < sText.length && IsNumber == true; i++) {
		Char = sText.charAt(i);
		if (ValidChars.indexOf(Char) == -1) {
			IsNumber = false;
		}
	}
	return IsNumber;
}

/**
 * Used in Failure rule box
 * 
 * @param dropdown
 * @param width
 * @param field
 * @return
 */
function addInput(dropdown, width, field) {
	if (dropdown.id == field + "type") {
		var div = $(field);
		var input = $(field + "number");
		div.innerHTML = "<input type='text' style='width:" + input.style.width
				+ "' id='" + input.id + "' value='" + input.value
				+ "' size='10' />";
	}
}

/**
 * Used in Failure rule box
 * 
 * @param width
 * @param fields
 * @return
 */
function updateInput(width, fields) {
	var atype = document.getElementById(fields[0] + "type");
	if (atype != null) {
		var div = document.getElementById(fields[0]);
		if (atype.value != "Choose " + fields[0])
			div.innerHTML = "<input type='text' style='width:" + width
					+ "px' id='" + fields[0] + "number' size='10' />";
		else
			div.innerHTML = "";
	}

	var stype = document.getElementById(fields[1] + "type");
	if (stype != null) {
		var div = document.getElementById(fields[1]);
		if (stype.value != "Choose " + fields[1])
			div.innerHTML = "<input type='text' style='width:" + width
					+ "px' id='" + fields[1] + "number' size='10' />";
		else
			div.innerHTML = "";
	}
}

/**
 * Add Job stop rule
 * 
 * @param field
 * @param type
 * @return
 */
function addRule(field, type) {
	var elem = document.getElementsByName(field + "amount")[0];
	var number = elem.value;
	if (number != null && number.length > 0 && IsNumeric(number) == false) {
		alert(number + " is not a number");
		return;
	} else if (number == null || number.trim().length == 0) {
		alert(field + "amount is empty");
		return;
	}
	var text = type + number;
	var stopRulesName = "stoprules";
	var rule = document.getElementsByName(stopRulesName)[0];
	if (rule != null) {
		if (rule.value.trim() != "" && !rule.value.trim().endsWith(","))
			rule.value += ",";
		rule.value += text;
		elem.value = "";
	} else {
		alert("Placeholder for stop rules, [" + stopRulesName
				+ "], does not exist!");
	}
}

/**
 * Add Job failure rule
 * 
 * @param field
 * @param type
 * @return
 */
function addFailureRule(fields) {
	var atype;
	var anumber;
	try {
		atype = document.getElementById(fields[0] + "type").value;
		anumber = document.getElementById(fields[0] + "number").value;

		if (anumber.startsWith("n/a"))
			anumber = null;

		if (anumber != null && IsNumeric(anumber.trim()) == false) {
			alert("Stop rule: " + anumber + " is not a number");
			return;
		}
	} catch (e) {
	}

	var stype;
	var snumber;
	try {
		stype = document.getElementById(fields[1] + "type").value;
		snumber = document.getElementById(fields[1] + "number").value;

		if (snumber.startsWith("n/a"))
			snumber = null;

		if (snumber != null && IsNumeric(snumber.trim()) == false) {
			alert("Stop rule: " + snumber + " is not a number");
			return;
		}
	} catch (e) {
	}

	var text = null;
	try {
		if (snumber != null && snumber.trim() != "" && anumber != null
				&& anumber.trim() != "") {
			text = stype + snumber + '/' + anumber;
			document.getElementById(fields[0] + "number").value = "";
			document.getElementById(fields[1] + "number").selectedIndex = 0;
		} else if (snumber != null && snumber.trim() != "") {
			text = stype + snumber;
			document.getElementById(fields[0] + "number").value = "";
			document.getElementById(fields[1] + "number").selectedIndex = 0;
		}
	} catch (e) {
	}

	if (text != null) {
		var stopRulesName = "stoprules";
		var rule = null;
		rule = document.getElementsByName(stopRulesName)[0];
		if (rule != null) {
			if (rule.value.trim() != "" && !rule.value.trim().endsWith(","))
				rule.value += ",";
			rule.value += text;
		} else {
			alert("Placeholder for stop rules, [" + stopRulesName
					+ "], does not exist!");
		}
	} else {
		alert("Stop rule is not correctly set.");
	}
}

/**
 * Not used any more, Was originally used by Search page to display a list of
 * recently accessed pages.
 * 
 * @param hoveritem
 * @return
 */
function ShowHistoryDiv(hoveritem) {
	ShowPopup(hoveritem, document.getElementById("history_div"), 0);
}

/**
 * Show a hidden box (div element)
 * 
 * @param hoveritem
 * @param width
 *            Not used. But not removed to keep interface.
 * @param fields
 *            Not used. But not removed to keep interface.
 * @return
 */
function ShowRuleBox(hoveritem, width, fields) {
	ShowPopup(hoveritem, document.getElementById("hoverpopup_" + hoveritem.id),
			0);
}

/**
 * Hide a visible box (div element)
 * 
 * @param hoveritem
 * @return
 */
function HideRuleBox(hoveritem) {
	document.getElementById(hoveritem).style.visibility = "Hidden";
}

/**
 * Finds an objects left and top position.
 * 
 * @param obj
 * @return
 */
function FindPosition(obj) {
	var tmpObj = obj;
	var obj_left = tmpObj.offsetLeft;
	var obj_top = tmpObj.offsetTop;
	if (tmpObj.offsetParent) {
		while ((tmpObj = tmpObj.offsetParent)) {
			obj_left += tmpObj.offsetLeft;
			obj_top += tmpObj.offsetTop;
		}
	}
	return [ obj_left, obj_top ];
}

/**
 * The following methods are used by the menu system, to display and keep popups
 * and to hide them when blured.
 * 
 * @param hoveritem
 * @return
 */
function ShowSubMenu(hoveritem) {
	var hp = getElement(hoveritem);
	ShowPopup(hoveritem, hp, 70);
}
function ShowPopup(hoveritem, hp, shift) {
	if (!hp)
		return;

	var position = FindPosition(hoveritem);
	hp.style.left = (position[0] + shift) + "px";
	hp.style.top = position[1] + "px";

	if (hp.style.visibility != "visible") {
		hp.style.visibility = "visible";
	}
}
function KeepPopup(hoveritem) {
	var hp = document.getElementById(hoveritem.id);
	if (!hp)
		return;

	hp.style.visibility = "Visible";
}
function getElement(hoveritem) {
	var hp;
	if (hoveritem.id == 'nav_search_url')
		hp = document.getElementById("hoverpopup_nav_search");
	else if (hoveritem.id == 'nav_unit_url')
		hp = document.getElementById("hoverpopup_nav_unit");
	else if (hoveritem.id == 'nav_profile_url')
		hp = document.getElementById("hoverpopup_nav_profile");
	else if (hoveritem.id == 'nav_unittype_url')
		hp = document.getElementById("hoverpopup_nav_unittype");
	else if (hoveritem.id == 'nav_group_url')
		hp = document.getElementById("hoverpopup_nav_group");
	else if (hoveritem.id == 'nav_job_url')
		hp = document.getElementById("hoverpopup_nav_job");
	else if (hoveritem.id == 'nav_firmware_url')
		hp = document.getElementById("hoverpopup_nav_firmware");
	else if (hoveritem.id == 'nav_monitor_url')
		hp = document.getElementById("hoverpopup_nav_monitor");
	else if (hoveritem.id == 'nav_staging_url')
		hp = document.getElementById("hoverpopup_nav_staging");
	else if (hoveritem.id == 'nav_report_url')
		hp = document.getElementById("hoverpopup_nav_report");
	return hp;
}
function HidePopup(hoveritem) {
	var hp = getElement(hoveritem);
	if (!hp)
		return;

	hp.style.visibility = "Hidden";
}

function HidePopupWindow(hp) {
	if (!hp)
		return;

	hp.style.visibility = "Hidden";
}

/**
 * Search function used by search page and firmware upgrade page
 * 
 * @param e
 * @param type
 * @return
 */
function send(e, type) {
	if (isEscape(e)) {
		HideResultBox(type);
		return;
	} else if (isDown(e)) {
		goToList(type);
		return;
	}
	var searchValue = document.getElementById(type).value;
	var unittype = $("input[name='contextunittype']").val();
	var profile = ".";
	if (unittype != null && unittype != ".")
		profile = $("input[name='contextprofile']").val();
	if (searchValue != null && searchValue.trim() != "") {
		var urlString = '?page=' + type + 'search&string='
				+ encodeURIComponent(searchValue) + "&unittype="
				+ encodeURIComponent(unittype) + "&profile="
				+ encodeURIComponent(profile);
		jQuery.get(urlString, function(data) {
			updateSearchResult(data, type);
		});
	} else {
		HideResultBox(type);
	}
}

function updateUnitField(unit, units) {
	var field = document.getElementById(units);
	field.value = unit;
	field.focus();
	HideResultBox(units);
	document.form1.submit();
}

function updateUnitFieldOnClick(unit) {
	updateUnitField(unit, "units");
}

function updateUnitFieldKeyPressed(e, unit) {
	if (!isEnter(e))
		return;
	updateUnitField(unit, "units");
}

function updateSearchResult(txt, key) {
	if (txt != null && txt.trim() != "")
		ShowResultBox(txt, key);
	else
		HideResultBox(key);
}

function ShowResultBox(txt, fieldname) {
	var hoveritem = document.getElementById(fieldname);
	var popupid = "hoverpopup_" + fieldname;
	if (txt != null) {
		var popup = document.getElementById(popupid);
		popup.innerHTML = txt;
	}
	ShowResultPopup(hoveritem, popup, 0, 22);
}

function updateList(event, type, callback) {
	if (isEscape(event)) {
		HideResultBox(type);
		Dom.get(type).focus();
		return;
	} else if (isDown(event) || isUp(event))
		return;
	else if (isBackspace(event)) {
		deleteLastCharacter(type);
		send(event, type);
		return;
	}
	window[callback](event, type);
}

function addCharacterToUnitField(event, type) {
	character = getCorrectLetter(event);
	unit = document.getElementById(type);
	unit.value = unit.value + character;
	unit.focus();
	send(event, type);
}

function deleteLastCharacter(type) {
	var field = document.getElementById(type);
	var string = field.value;
	string = string.slice(0, -1);
	field.value = string;
	field.focus();
}

function goToList(type) {
	try {
		var popup = Dom.get("hoverpopup_" + type);
		if (popup.innerHTML != "") {
			ShowResultPopup(Dom.get(type), popup, 0, 22);
			document.getElementById(type + "searchselect").selectedIndex = 0;
			document.getElementById(type + "searchselect").focus();
		}
	} catch (e) {
	}
}

function ShowResultPopup(hoveritem, hp, shiftright, shiftdown) {
	if (!hp)
		return;

	var position = FindPosition(hoveritem);
	hp.style.left = (position[0] + shiftright) + "px";
	hp.style.top = (position[1] + shiftdown) + "px";

	if (hp.style.visibility != "visible") {
		hp.style.visibility = "visible";
	}
}

function HideResultBox(type) {
	document.getElementById("hoverpopup_" + type).style.visibility = "Hidden";
}

function toggleNotifyIntHours(element) {
	if (element.value == "0" || element.value == "1") {
		jQuery("#notifyIntervalHours").removeAttr("disabled");
		jQuery("#notifyIntervalHours").css({
			background : "white"
		});
		// jQuery("#notifyIntervalHours").val('1');
	}
	if (element.value == "2") {
		jQuery("#notifyIntervalHours").attr("disabled", true);
		jQuery("#notifyIntervalHours").css({
			background : "#EBEBE4"
		});
		// jQuery("#notifyIntervalHours").val('.');
	}
}

function toggleNumberOfFields(element) {
	if (element.value == "0") {
		jQuery("#syslogEventId").removeAttr("disabled");
		jQuery("#syslogEventId").css({
			background : "white"
		});
		jQuery("#noTotal").removeAttr("disabled");
		jQuery("#noTotal").css({
			background : "white"
		});
		jQuery("#noPrUnit").removeAttr("disabled");
		jQuery("#noPrUnit").css({
			background : "white"
		});
		jQuery("#noUnits").removeAttr("disabled");
		jQuery("#noUnits").css({
			background : "white"
		});
	}
	if (element.value == "1") {
		jQuery("#syslogEventId").attr("disabled", true);
		jQuery("#syslogEventId").css({
			background : "#EBEBE4"
		});
		jQuery("#noTotal").attr("disabled", true);
		jQuery("#noTotal").css({
			background : "#EBEBE4"
		});
		jQuery("#noPrUnit").attr("disabled", true);
		jQuery("#noPrUnit").css({
			background : "#EBEBE4"
		});
		jQuery("#noUnits").attr("disabled", true);
		jQuery("#noUnits").css({
			background : "#EBEBE4"
		});
	}
}

function toggleFieldsInEditmode() {
	toggleNumberOfFields(document.getElementById("typeTrigger"));
	toggleNotifyIntHours(document.getElementById("notifyType"));
}