package com.github.freeacs.security;

import com.github.freeacs.base.db.DBAccess;
import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.util.SystemParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class AcsUserDetailsService implements UserDetailsService {

    private final DBAccess dbAccess;

    @Autowired
    public AcsUserDetailsService(DBAccess dbAccess) {
        this.dbAccess = dbAccess;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            ACSUnit acsUnit = new ACSUnit(dbAccess.getDataSource(), dbAccess.getDbi().getAcs(), dbAccess.getDbi().getAcs().getSyslog());
            Unit unit = acsUnit.getUnitById(username);
            if (unit != null) {
                String secret = unit.getUnitParameters().get(SystemParameters.SECRET).getValue();
                return new AcsUser(username, secret);
            }
        } catch (SQLException e) {
            throw new UsernameNotFoundException("Failed to retrieve user " + username, e);
        }
        return null;
    }
}
