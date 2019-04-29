package com.github.freeacs.security;

import com.github.freeacs.dbi.ACSUnit;
import com.github.freeacs.dbi.DBI;
import com.github.freeacs.dbi.Unit;
import com.github.freeacs.dbi.util.SystemParameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

@Component
public class AcsUnitDetailsService implements UserDetailsService {

    private final DBI dbi;

    @Autowired
    public AcsUnitDetailsService(DBI dbi) {
        this.dbi = dbi;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            ACSUnit acsUnit = dbi.getACSUnit();
            Unit unit = acsUnit.getUnitById(username);
            if (unit != null) {
                String secret = unit.getUnitParameters().get(SystemParameters.SECRET).getValue();
                if (secret == null) {
                    throw new UsernameNotFoundException("User " + username + " has no secret");
                }
                return new AcsUnit(username, secret);
            } else {
                throw new UsernameNotFoundException("User was not found: " + username);
            }
        } catch (SQLException e) {
            throw new UsernameNotFoundException("Failed to retrieve user: " + username, e);
        }
    }
}
