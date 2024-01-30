package com.github.freeacs.dbi;

import java.sql.SQLException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FilesTest extends BaseDBITest {

  @Test
  public void addOrChangeFile() throws SQLException {
    // Given:
    String unittypeName = "Test unittype 1";
    Unittype unittype = TestUtils.createUnittype(unittypeName, acs);
    String fileName = "File name";
    byte[] bytes = "Hei, dette er en test".getBytes();
    String version = "v1.0";
    FileType fileType = FileType.MISC;

    // When:
    TestUtils.createFileAndVerify(unittype, fileName, bytes, fileType, version, acs);

    // Then:
    File file = unittype.getFiles().getByName(fileName);
    Assertions.assertNotNull(file);
    Assertions.assertEquals(bytes, file.getContent());
  }

  @Test
  public void deleteFile() throws SQLException {
    // Given:
    String unittypeName = "Test unittype 2";
    Unittype unittype = TestUtils.createUnittype(unittypeName, acs);
    String fileName = "File name";
    byte[] bytes = "Hei, dette er en test".getBytes();
    String version = "v1.0";
    FileType fileType = FileType.MISC;
    TestUtils.createFileAndVerify(unittype, fileName, bytes, fileType, version, acs);

    // When:
    unittype.getFiles().deleteFile(unittype.getFiles().getByName(fileName), acs);
    Assertions.assertEquals(0, unittype.getFiles().getFiles().length);
  }
}
