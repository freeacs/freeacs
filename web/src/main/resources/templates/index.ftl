<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
	<head>
		<title>${TITLE}</title>
		<!-- basic css -->
		<#include "/meta/basic.ftl">
		<!-- web css and js -->
		<#include "/meta/web.ftl">
		<!-- popup a debug window on window.error -->
		<#if JAVASCRIPT_DEBUG>
			<script src="javascript/acs.plugin.debug.js" type="text/javascript"></script>
		</#if>
		<!-- menu css and js -->
		<#include "/meta/menu.ftl">
		<!-- jhelp css and js -->
		<#include "/meta/jhelp.ftl">
		<#if IXEDIT_DEVELOPER>
			<!-- ixedit css and js -->
			<#include "/meta/ixedit.ftl">
		</#if>
		<!-- initialization -->
		<script type="text/javascript">
			ACS.initModule({
			     session: {
			     	timeout: ${SESSION_TIMEOUT}
			     },
			     requestedPage: '${REQUESTED_PAGE}',
			     confirmchanges: ${CONFIRMCHANGES?string}
			});
            // code to perform automatic logout after session_timeout is reached
            var idleMax = ${SESSION_TIMEOUT} - 1;
            var idleTime = 0;
            $(document).ready(function () {
                setInterval(timerIncrement, 60000);
                $(this).keypress(function (e) {idleTime = 0;});
            });
            function timerIncrement() {
                idleTime = idleTime + 1;
                if (idleTime > idleMax) {
                    window.location="${LOGOUT_URI}";
                }
            }
		</script>
		<#assign defaultMenuEnabled=true />
	</head>
	<body>
	    <noscript>
	      <div style="width: 22em; position: absolute; left: 50%; margin-left: -11em;margin-top:50px; color: red; background-color: white; border: 1px solid red; padding: 4px; font-family: sans-serif">
	        Your web browser must have JavaScript enabled
	        in order for this application to display correctly.
	      </div>
	    </noscript>
		<input type="hidden" id="browser_id" />
		<form id="searchForm" action="${URL_MAP.SEARCH}" method="POST">
            <input type="hidden" name="profile" value="." />
			<input type="hidden" name="unittype" value="." />
			<input type="hidden" name="cmd" value="follow-single-unit" />
			<input type="text" value="global search" class="searchField" style="width:320px;padding-left:5px;position:absolute;top:0;right:0;margin-right:15px;margin-top:4px;" autocomplete="off" id="unitparamvalue" name="unitparamvalue" />
			<script>
				jQuery(document).ready(function(){
					var field = $("#unitparamvalue");
					field.autocomplete({
						source: "${URL_MAP.SEARCH}&unittype=.&profile=.",
						minLength: 3,
						select: function(event,ui){
							field.closest("form").submit();
						}
					});
				});
			</script>
			<input type="submit" name="formsubmit" value="Search" style="display:none" />
		</form>
		<table width="100%" height="100%" cellspacing="0" cellpadding="0">
			<tr>
				<td id="sidebar">
					<div id="logo" align="center"></div>
					<div class="menu">
						<div id="smoothmenu2" class="ddsmoothmenu-v">
							<#if defaultMenuEnabled>
							${MAIN_MENU}
							</#if>
						</div>
					</div>
				</td>
				<td id="mainview">
					<div style="min-width:800px;padding-top:3px;padding-bottom:28px;">
						<div class='container' style='width:100px;float:left;clear:right;'>
							<b class="rtop"><b class="r1"></b> <b class="r2"></b> <b class="r3"></b> <b class="r4"></b></b>
								<center><div class="hidden">${TITLE_DESCRIPTION}</div><div id="title_logo"></div></center>
							<b class="rbottom"><b class="r4"></b> <b class="r3"></b> <b class="r2"></b> <b class="r1"></b></b>
						</div>
						<div class='container' style='min-width:150px;margin-left:2px;float:left;clear:right;'>
							<b class="rtop"><b class="r1"></b> <b class="r2"></b> <b class="r3"></b> <b class="r4"></b></b>
								<center style="padding-left:5px;padding-right:5px;">${STATUS_LOGGEDIN}</center>
							<b class="rbottom"><b class="r4"></b> <b class="r3"></b> <b class="r2"></b> <b class="r1"></b></b>
						</div>
						<div class='container' style='margin-left:2px;float:left;clear:right;'>
							<b class="rtop"><b class="r1"></b> <b class="r2"></b> <b class="r3"></b> <b class="r4"></b></b>
								<center style="padding-left: 10px; padding-right: 10px;">
									<#if TOOLS_MENU??>
										<#list TOOLS_MENU as tool>
											<a href="javascript:IFRAMEDIALOGS.showToolDialog('${tool.display}','${tool.url}&amp;async=true&amp;header=true');">${tool.display}</a> |
										</#list>
									</#if>
									<a href="javascript:IFRAMEDIALOGS.showHelpDialog('about','help?page=about');">About</a> |
									<a href="javascript:IFRAMEDIALOGS.showHelpDialog('${HELP_PAGE!}','help?page=${HELP_PAGE!}');">Help</a> |
										<a href="${LOGOUT_URI}">Logout</a> </center>
							<b class="rbottom"><b class="r4"></b> <b class="r3"></b> <b class="r2"></b> <b class="r1"></b></b>
						</div>
					</div>
					<div style="clear:both;"></div>
					<div id="bodycontent">
						<#include "context.ftl" />
						<div class="mainContent">
							<span style="margin-left:20px">${ERROR!}</span>
							${content!}
	                        <#if INCLUDED_TEMPLATE??>
	                        	<#include INCLUDED_TEMPLATE />
	                        </#if>
                        </div>
					</div>
				</td>
			</tr>
		</table>
        <div id="JavaScriptsErrorsDiv"></div>
	</body>
</html>
