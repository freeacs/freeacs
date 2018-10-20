package com.github.freeacs.dbi;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class UnittypeTestUtils {
    public static class Param {
        final String name;
        final String flag;

        public Param(String name, String flag) {
            this.name = name;
            this.flag = flag;
        }
    }

    public static Unittype createUnittype(List<Param> params, ACS acs) throws SQLException {
        Unittype unittype = new Unittype(
                "Name",
                "Vendor",
                "Desc",
                Unittype.ProvisioningProtocol.TR069
        );
        Unittypes unittypes = new Unittypes(new HashMap<>(), new HashMap<>());
        unittypes.addOrChangeUnittype(unittype, acs);
        UnittypeParameters unittypeParameters = new UnittypeParameters(new HashMap<>(), new HashMap<>(), unittype);
        List<UnittypeParameter> parametersToCreate = params.stream().map(param ->
                new UnittypeParameter(unittype, param.name, new UnittypeParameterFlag(param.flag))).collect(Collectors.toList());
        unittypeParameters.addOrChangeUnittypeParameters(parametersToCreate, acs);
        unittype.setUnittypeParameters(unittypeParameters);
        return unittype;
    }
}
