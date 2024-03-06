package com.github.freeacs.dbi.repositories;

import com.github.freeacs.common.util.AbstractMySqlIntegrationTest;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RepositoryTest implements AbstractMySqlIntegrationTest  {

    private static Jdbi jdbi;

    @BeforeAll
    public static void localBeforeAll() {
        jdbi = Jdbi.create(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
        jdbi.installPlugin(new SqlObjectPlugin());
    }

    @Test
    public void canListUnits() {
        var result = jdbi.onDemand(UnitRepository.class).listUnits();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void canListUsers() {
        var result = jdbi.onDemand(UserRepository.class).listUsers();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("admin", result.get(0).getUsername());
        assertTrue(result.get(0).getAdmin());
    }

    @Test
    public void canListUnitTypes() {
        var result = jdbi.onDemand(UnitTypeRepository.class).listUnitTypes();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void canListProfiles() {
        var result = jdbi.onDemand(ProfileRepository.class).listProfiles();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

}
