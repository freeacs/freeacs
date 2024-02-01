package com.github.freeacs.dbi.util;

import com.github.freeacs.dbi.BaseDBITest;
import com.github.freeacs.dbi.TestUtils;
import com.github.freeacs.dbi.Unittype;
import com.github.freeacs.dbi.UnittypeParameter;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SystemParametersTest extends BaseDBITest {

  @Test
  public void getTR069ScriptParameterForTargetFilenameWhenScriptParameterExists()
      throws SQLException {
    // Given:
    String name = "Hallo";
    SystemParameters.TR069ScriptType type = SystemParameters.TR069ScriptType.TargetFileName;
    Unittype unittype =
        TestUtils.createUnittypeParameters(
            TestUtils.createUnittype("Name 1", acs),
            Arrays.asList(
                new TestUtils.Param("System.X_FREEACS-COM.TR069Script.Hei.TargetFileName", "X"),
                new TestUtils.Param("System.X_FREEACS-COM.TR069Script.Hallo.TargetFileName", "X")),
            acs);

    // When:
    UnittypeParameter unittypeParameter =
        SystemParameters.getTR069ScriptParameter(name, type, acs, unittype);

    // Then:
    assertNotNull(unittypeParameter);
    assertEquals(
        "System.X_FREEACS-COM.TR069Script.Hallo.TargetFileName", unittypeParameter.getName());
  }

  @Test
  public void getTR069ScriptParameterForTargetFilenameWhenScriptParameterNotExists()
      throws SQLException {
    // Given:
    String name = "Hallo";
    SystemParameters.TR069ScriptType type = SystemParameters.TR069ScriptType.TargetFileName;
    Unittype unittype =
        TestUtils.createUnittypeParameters(
            TestUtils.createUnittype("Name 2", acs), Collections.emptyList(), acs);

    // When:
    UnittypeParameter unittypeParameter =
        SystemParameters.getTR069ScriptParameter(name, type, acs, unittype);

    // Then:
    assertNotNull(unittypeParameter);
    assertEquals(
        "System.X_FREEACS-COM.TR069Script.Hallo.TargetFileName", unittypeParameter.getName());
  }

  @Test
  public void getTR069ScriptParameterNameForTargetFilename() {
    // Given:
    String name = "Hallo";
    SystemParameters.TR069ScriptType type = SystemParameters.TR069ScriptType.TargetFileName;

    // When:
    String result = SystemParameters.getTR069ScriptParameterName(name, type);

    // Then:
    assertEquals("System.X_FREEACS-COM.TR069Script.Hallo.TargetFileName", result);
  }

  @Test
  public void getTR069ScriptParameterNameForUrl() {
    // Given:
    String name = "Hallo";
    SystemParameters.TR069ScriptType type = SystemParameters.TR069ScriptType.URL;

    // When:
    String result = SystemParameters.getTR069ScriptParameterName(name, type);

    // Then:
    assertEquals("System.X_FREEACS-COM.TR069Script.Hallo.URL", result);
  }

  @Test
  public void getTR069ScriptParameterNameForVersion() {
    // Given:
    String name = "Hallo";
    SystemParameters.TR069ScriptType type = SystemParameters.TR069ScriptType.Version;

    // When:
    String result = SystemParameters.getTR069ScriptParameterName(name, type);

    // Then:
    assertEquals("System.X_FREEACS-COM.TR069Script.Hallo.Version", result);
  }

  @Test
  public void getTR069ScriptParameterNameWhenNameIsNull() {
    // When:
    assertThrows(
            IllegalArgumentException.class,
        () -> SystemParameters.getTR069ScriptParameterName(null, SystemParameters.TR069ScriptType.TargetFileName));
  }
}
