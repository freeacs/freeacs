<div id="hoverpopup_failurerule" class="hoverpopup" style="visibility:hidden;">
	<table cellspacing="0">
		<tr>
			<td nowrap="nowrap" align="left">
				<input name="closeButton" type="button" value="X" style="width:20px" onclick="HideRuleBox('hoverpopup_failurerule');" />
				<b>Stop Rules - Helper</b>
			</td>
		</tr>
		<tr>
			<td nowrap="nowrap" align="left" style="padding-bottom:10px;padding-left:10px;padding-top:10px">
				<div id="status">
					<input id="statusnumber" type="text" style="width:170px" />
				</div>
				<select id="statustype" style="width:175px">
					<option value="a">Failed (any type)</option>
					<option value="u">Unconfirmed failed</option>
					<option value="c">Confirmed failed</option>
				</select>
				<input name="amounttype" type="hidden" value="#" id="amounttype" /><br />
				out of:
				<div id="amount">
					<select id="amountnumber" style="width:175px">
						<option>n/a</option>
						<option>10</option>
						<option>100</option>
						<option>1000</option>
						<option>10000</option>
					</select>
				</div>
				<input name="submitButton" type="button" value="Add failure rule" style="width:175px" onclick="addFailureRule(['amount','status']);" />
			</td>
		</tr>
	</table>
	<table cellspacing="0" cellpadding="10" style="margin-top:3px;">
		<tr>
			<td nowrap="nowrap" align="left">
				Number of units:<br />
				<input name="unitamount" type="text" style="width:170px" />
				<br />
				<input name="addUnitRule" type="button" value="Add count rule" style="width:170px" onclick="addRule('unit','n');" />
			</td>
		</tr>
	</table>
</div>
