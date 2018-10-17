package com.github.freeacs.web.app.page.report.uidata;

public class RecordUIDataGroupSumFromReport {
  /*
  private final List<RecordUIDataGroupFromReport> records = new ArrayList<RecordUIDataGroupFromReport>();
  private final List<String> groupNames = new ArrayList<String>();
  private int unitCount = 0;
  private final Unit unit;

  public RecordUIDataGroupSumFromReport(Unit unit) {
  	this.unit = unit;
  }

  public String getSyslogEventExpression(){
  	if(groupNames.size()>1)
  		throw new IllegalArgumentException("Group report sum is aggregated on multiple groups. Extracting syslog event pattern is only supported when the group report sum only contains 1 group.");
  	String gName = groupNames.get(0);
  	Unittype unittype = unit.getUnittype();
  	Group group = unittype.getGroups().getByName(gName);
  	for(SyslogEvent se: unittype.getSyslogEvents().getSyslogEvents()){
  		if(se.getTask().startsWith(SyslogEvent.TASK_GROUP_SYNC)){
  			int commandLength = SyslogEvent.TASK_GROUP_SYNC.length();
  			if (se.getTask().length() > commandLength) {
  				String groupName = se.getTask().substring(commandLength).trim();
  				Group g = unittype.getGroups().getByName(groupName);
  				if(g==group)
  					return se.getExpression().pattern();
  			}
  		}
  	}
  	return null;
  }

  public void addRecord(RecordUIDataGroupFromReport rec) {
  	this.groupNames.add(rec.getRecord().getGroupName());
  	this.unitCount += (rec.getRecord().getUnitCount().get()/rec.getRecord().getUnitCount().getDividend());
  	this.records.add(rec);
  }

  public Unit getUnit(){
  	return unit;
  }

  public int getUnitCount() {
  	return unitCount;
  }

  public List<String> getGroupNames() {
  	return groupNames;
  }
  */
}
