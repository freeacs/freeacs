package com.github.freeacs.dbi.repositories;

import com.github.freeacs.common.util.AbstractMySqlIntegrationTest;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class RepositoryTest implements AbstractMySqlIntegrationTest  {

    private static Jdbi jdbi;

    @BeforeAll
    public static void localBeforeAll() {
        jdbi = Jdbi.create(mysql.getJdbcUrl(), mysql.getUsername(), mysql.getPassword());
        jdbi.installPlugin(new SqlObjectPlugin());
    }

    @Test
    public void canListUnits() {
        jdbi.onDemand(UnitRepository.class).listUnits().forEach(System.out::println);
    }

    @Test
    public void canListUsers() {
        jdbi.onDemand(UserRepository.class).listUsers().forEach(System.out::println);
    }

    @Test
    public void canListUnitTypes() {
        jdbi.onDemand(UnitTypeRepository.class).listUnitTypes().forEach(System.out::println);
    }

    @Test
    public void canListProfiles() {
        jdbi.onDemand(ProfileRepository.class).listProfiles().forEach(System.out::println);
    }

}
