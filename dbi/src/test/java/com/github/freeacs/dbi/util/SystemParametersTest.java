package com.github.freeacs.dbi.util;

import com.github.freeacs.common.util.DataSourceHelper;
import com.github.freeacs.dbi.*;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SystemParametersTest {

    private ACS acs;
    private DataSource dataSource;

    @Before
    public void init() {
        acs = mock(ACS.class);
        User user = mock(User.class);
        when(acs.getUser()).thenReturn(user);
        when(user.isUnittypeAdmin(any())).thenReturn(true);
        when(user.isProfileAdmin(any(), any())).thenReturn(true);
        when(user.isAdmin()).thenReturn(true);
        dataSource = DataSourceHelper.inMemoryDataSource();
        when(acs.getDataSource()).thenReturn(dataSource);
        DBI dbi = mock(DBI.class);
        when(acs.getDbi()).thenReturn(dbi);
        doNothing().when(dbi).publishChange(any(Unittype.class), any(Unittype.class));
    }

    @After
    public void tearDown() throws SQLException {
        dataSource.unwrap(HikariDataSource.class).close();
    }

    @Test
    public void getTR069ScriptParameterForTargetFilenameWhenScriptParameterExists() throws SQLException {
        // Given:
        String name = "Hallo";
        SystemParameters.TR069ScriptType type = SystemParameters.TR069ScriptType.TargetFileName;
        Unittype unittype = UnittypeTestUtils.createUnittype(Arrays.asList(
                new UnittypeTestUtils.Param("System.X_FREEACS-COM.TR069Script.Hei.TargetFileName", "X"),
                new UnittypeTestUtils.Param("System.X_FREEACS-COM.TR069Script.Hallo.TargetFileName", "X")),
                acs);

        // When:
        UnittypeParameter unittypeParameter = SystemParameters.getTR069ScriptParameter(name, type, acs, unittype);

        // Then:
        assertNotNull(unittypeParameter);
        assertEquals("System.X_FREEACS-COM.TR069Script.Hallo.TargetFileName", unittypeParameter.getName());
    }

    @Test
    public void getTR069ScriptParameterForTargetFilenameWhenScriptParameterNotExists() throws SQLException {
        // Given:
        String name = "Hallo";
        SystemParameters.TR069ScriptType type = SystemParameters.TR069ScriptType.TargetFileName;
        Unittype unittype = UnittypeTestUtils.createUnittype(Collections.emptyList(), acs);

        // When:
        UnittypeParameter unittypeParameter = SystemParameters.getTR069ScriptParameter(name, type, acs, unittype);

        // Then:
        assertNotNull(unittypeParameter);
        assertEquals("System.X_FREEACS-COM.TR069Script.Hallo.TargetFileName", unittypeParameter.getName());
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

    @Test(expected = IllegalArgumentException.class)
    public void getTR069ScriptParameterNameWhenNameIsNull() {
        // When:
        SystemParameters.getTR069ScriptParameterName(null, SystemParameters.TR069ScriptType.URL);
    }
}