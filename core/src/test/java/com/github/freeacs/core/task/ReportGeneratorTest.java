package com.github.freeacs.core.task;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.freeacs.common.scheduler.ScheduleType;
import com.github.freeacs.dbi.report.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import javax.sql.DataSource;
import org.junit.Test;

public class ReportGeneratorTest {

  @Test
  public void populateReportHWTable() throws SQLException {
    // Given:
    final Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, 2018);
    calendar.set(Calendar.MONTH, 10);
    calendar.set(Calendar.DAY_OF_MONTH, 15);
    final ReportGenerator reportGenerator =
        new ReportGenerator("Test", ScheduleType.DAILY, null, null);
    final DataSource fakeDataSource = mock(DataSource.class);
    final Connection fakeConnection = mock(Connection.class);
    final PreparedStatement fakePreparedStatement = mock(PreparedStatement.class);
    when(fakeConnection.prepareStatement(anyString())).thenReturn(fakePreparedStatement);
    when(fakeDataSource.getConnection()).thenReturn(fakeConnection);
    final Report<RecordHardware> hardwareReport =
        new Report<>(RecordHardware.class, PeriodType.ETERNITY);
    final Date tms = new Date();
    tms.setYear(2018 - 1900);
    tms.setMonth(11 - 1);
    tms.setDate(1);
    final Key key =
        RecordHardware.keyFactory.makeKey(
            tms, PeriodType.DAY, "testunittype", "testprofile", "v1.0");
    final RecordHardware recordHardware =
        new RecordHardware(tms, PeriodType.DAY, "testunittype", "testprofile", "v1.0");
    hardwareReport.getMap().put(key, recordHardware);

    // When:
    reportGenerator.populateReportHWTable(fakeDataSource, hardwareReport, calendar);

    // Then:
    verify(fakeConnection)
        .prepareStatement(
            "insert into report_hw VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
    verify(fakePreparedStatement).executeUpdate();
    final long expectedTime = new TmsConverter(calendar).convert(tms, PeriodType.DAY).getTime();
    final Timestamp expectedTimestamp = new Timestamp(expectedTime);
    verify(fakePreparedStatement).setTimestamp(1, expectedTimestamp);
    verify(fakePreparedStatement).setInt(2, PeriodType.DAY.getTypeInt());
    verify(fakePreparedStatement).setString(3, "testunittype");
    verify(fakePreparedStatement).setString(4, "testprofile");
    verify(fakePreparedStatement).setString(5, "v1.0");
    verify(fakePreparedStatement).setInt(6, 0);
    verify(fakePreparedStatement).setInt(7, 0);
    verify(fakePreparedStatement).setInt(8, 0);
    verify(fakePreparedStatement).setInt(9, 0);
    verify(fakePreparedStatement).setInt(10, 0);
    verify(fakePreparedStatement).setInt(11, 0);
    verify(fakePreparedStatement).setInt(12, 0);
    verify(fakePreparedStatement).setInt(13, 0);
    verify(fakePreparedStatement).setInt(14, 0);
    verify(fakePreparedStatement).setInt(15, 0);
    verify(fakePreparedStatement).setString(16, null);
    verify(fakePreparedStatement).setString(17, null);
    verify(fakePreparedStatement).setString(18, null);
    verify(fakePreparedStatement).setString(19, null);
    verify(fakePreparedStatement).setString(20, null);
    verify(fakePreparedStatement).setString(21, null);
    verify(fakePreparedStatement).setString(22, null);
    verify(fakePreparedStatement).setString(23, null);
    verify(fakePreparedStatement).setString(24, null);
    verify(fakePreparedStatement).setString(25, null);
    verify(fakePreparedStatement).setString(26, null);
    verify(fakePreparedStatement).setString(27, null);
    verify(fakePreparedStatement).setString(28, null);
    verify(fakeConnection).close();
  }
}
