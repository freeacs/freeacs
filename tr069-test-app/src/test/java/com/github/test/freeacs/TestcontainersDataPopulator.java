package com.github.test.freeacs;

import com.github.freeacs.common.util.AbstractMySqlIntegrationTest;
import com.github.freeacs.config.DBIConfig;
import com.github.freeacs.dbi.*;
import com.github.freeacs.dbi.util.SystemParameters;
import lombok.SneakyThrows;
import org.mariadb.jdbc.MariaDbDataSource;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

import java.sql.SQLException;
import java.util.Collections;

import static com.github.freeacs.dbi.Unittype.ProvisioningProtocol.TR069;

public class TestcontainersDataPopulator implements ApplicationListener<ApplicationEnvironmentPreparedEvent>  {
    @SneakyThrows
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        if (!AbstractMySqlIntegrationTest.mysql.isRunning()) {
            return;
        }

        MariaDbDataSource dataSource = new MariaDbDataSource();
        dataSource.setUrl(String.format("jdbc:mariadb://%s:%d/%s", AbstractMySqlIntegrationTest.mysql.getHost(), AbstractMySqlIntegrationTest.mysql.getFirstMappedPort(), AbstractMySqlIntegrationTest.mysql.getDatabaseName()));
        dataSource.setUser(AbstractMySqlIntegrationTest.mysql.getUsername());
        dataSource.setPassword(AbstractMySqlIntegrationTest.mysql.getPassword());

        DBI dbi = new DBIConfig().getDBI(dataSource);

        Unittype unittype = addUnittype(dbi, "TestUnittype");

        ACSUnit acsUnit = dbi.getACSUnit();

        acsUnit.addUnits(Collections.singletonList("test123"), unittype.getProfiles().getByName("Default"));
        Unit unit = acsUnit.getUnitById("test123");
        acsUnit.addOrChangeUnitParameter(unit, SystemParameters.SECRET, "password");

        dbi.setRunning(false);

        System.out.println("TestcontainersDataPopulator done");
    }

    private static Unittype addUnittype(DBI dbi, String unitTypeName) throws SQLException {
        Unittypes unittypes = dbi.getAcs().getUnittypes();
        unittypes.addOrChangeUnittype(new Unittype(unitTypeName, "","", TR069), dbi.getAcs());
        Unittype unittype = unittypes.getByName(unitTypeName);
        unittype.getUnittypeParameters().addOrChangeUnittypeParameter(new UnittypeParameter(unittype, "InternetGatewayDevice.ManagementServer.PeriodicInformInterval", new UnittypeParameterFlag("RW")), dbi.getAcs());
        return unittype;
    }
}
