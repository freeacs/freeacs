package com.github.freeacs.dbi.repositories;

import com.github.freeacs.dbi.domain.Profile;
import com.github.freeacs.dbi.domain.UnitType;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

public interface ProfileRepository {
    @SqlQuery("""
        SELECT
            p.profile_id as p_id,
            p.profile_name as p_name,
            ut.unit_type_id as ut_id,
            ut.unit_type_name as ut_name,
            ut.vendor_name as ut_vendor,
            ut.description as ut_description,
            ut.protocol as ut_protocol
        FROM
            profile p
        INNER JOIN unit_type ut ON p.unit_type_id = ut.unit_type_id
        ORDER BY p.profile_id ASC
    """)
    @RegisterBeanMapper(value = Profile.class, prefix = "p")
    @RegisterBeanMapper(value = UnitType.class, prefix = "ut")
    List<Profile> listProfiles();

    @SqlUpdate("""
        INSERT INTO profile (profile_name, unit_type_id)
        VALUES (:name, :unitType.id)
    """)
    @GetGeneratedKeys
    Integer insertProfile(@BindBean Profile profile);

    @SqlUpdate("""
        UPDATE profile
        SET profile_name = :name
        WHERE profile_id = :id
    """)
    int updateProfile(@BindBean Profile profile);

    @SqlUpdate("""
        DELETE FROM profile WHERE profile_id = :id
    """)
    int deleteProfile(Integer id);
}
