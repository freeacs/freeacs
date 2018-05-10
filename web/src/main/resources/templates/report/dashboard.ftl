<@macros.form name="form1" fieldset="Shortcuts">
	<ul style="list-style: none;">
		<li><img width="20px" src="images/icon_report.png" border="0" />FreeACS reports
			<ul>
				<li><a href="${URL_MAP.REPORT}&type=Unit">Unit report</a></li>
				<li><a href="${URL_MAP.REPORT}&type=Group">Group report</a></li>
				<li><a href="${URL_MAP.REPORT}&type=Job">Job report</a></li>
				<li><a href="${URL_MAP.REPORT}&type=Prov">Provisioning report</a></li>
				<li><a href="${URL_MAP.REPORT}&type=Syslog">Syslog report</a></li>
			</ul>
		</li>
		<li><img width="20px" src="images/icon_report.png" border="0" />Pingcom Device Reports
			<ul>
				<li><a href="${URL_MAP.REPORT}&type=Voip">Voip report </a></li>
				<li><a href="${URL_MAP.REPORT}&type=Hardware">Hardware report</a></li>
			</ul>
		</li>
		<li><img width="20px" src="images/icon_report.png" border="0" />TR069 Device Reports
			<ul>
				<li><a href="${URL_MAP.REPORT}&type=HardwareTR">Hardware report</a></li>
				<li><a href="${URL_MAP.REPORT}&type=GatewayTR">Gateway report</a></li>
			</ul>
		</li>
	</ul>
</@macros.form>