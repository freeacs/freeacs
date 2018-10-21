package com.github.freeacs.dbi;

import static org.junit.Assert.*;

import java.sql.SQLException;
import org.junit.Test;

public class FilesTest extends BaseDBITest {

  @Test
  public void addOrChangeFile() throws SQLException {
    // Given:
    String unittypeName = "Test unittype";
    Unittype unittype = TestUtils.createUnittype(unittypeName, acs);
    String fileName = "File name";
    byte[] bytes = "Hei, dette er en test".getBytes();
    String version = "v1.0";
    FileType fileType = FileType.MISC;

    // When:
    TestUtils.createFileAndVerify(unittype, fileName, bytes, fileType, version, acs);

    // Then:
    File file = unittype.getFiles().getByName(fileName);
    assertNotNull(file);
    assertEquals(bytes, file.getContent());
  }

  @Test
  public void deleteFile() throws SQLException {
    // Given:
    String unittypeName = "Test unittype";
    Unittype unittype = TestUtils.createUnittype(unittypeName, acs);
    String fileName = "File name";
    byte[] bytes = "Hei, dette er en test".getBytes();
    String version = "v1.0";
    FileType fileType = FileType.MISC;
    TestUtils.createFileAndVerify(unittype, fileName, bytes, fileType, version, acs);

    // When:
    unittype.getFiles().deleteFile(unittype.getFiles().getByName(fileName), acs);
    assertEquals(0, unittype.getFiles().getFiles().length);
  }
}
