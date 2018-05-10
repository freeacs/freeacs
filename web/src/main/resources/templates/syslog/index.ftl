							<@macros.form>
								<#if async??>
									<input type="hidden" name="async" value="true" />
								</#if>
								<#if advancedView>
									<#include "/syslog/advanced.ftl">
								<#else>
									<#include "/syslog/simple.ftl">
								</#if>
								<#if result??>
									<#if (result?size>0)>
										<#include "/syslog/results.ftl" />
									<#else>
										<fieldset>
											<legend>Syslog entries</legend>
											No syslog entries found
										</fieldset>
									</#if>
								</#if>
							</@macros.form>