package com.github.freeacs.dao;

import com.github.freeacs.vo.UnittypeVO;
import org.jdbi.v3.sqlobject.config.RegisterColumnMapper;
import org.jdbi.v3.sqlobject.config.RegisterFieldMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface UnittypeDao {
    @SqlQuery("select unit_type_id, unit_type_name, matcher_id, vendor_name, description, protocol " +
            "from unit_type")
    @RegisterFieldMapper(UnittypeVO.class)
    @RegisterColumnMapper(UnittypeProvisioningProtocolMapper.class)
    List<UnittypeVO> get();

    default List<UnittypeVO> get(List<String> allowedUnitTypes) {
        List<UnittypeVO> unittypes = get();
        if (allowedUnitTypes.contains("*")) {
            return unittypes;
        }
        return unittypes.stream()
                .filter(unittype -> allowedUnitTypes.contains(unittype.getUnitTypeName()))
                .collect(Collectors.toList());
    }

    @SqlQuery("select unit_type_id, unit_type_name, matcher_id, vendor_name, description, protocol " +
            "from unit_type " +
            "where unit_type_id = :id")
    @RegisterFieldMapper(UnittypeVO.class)
    @RegisterColumnMapper(UnittypeProvisioningProtocolMapper.class)
    Optional<UnittypeVO> get(@Bind("id") Long id);

    @SqlUpdate("delete from unit_type where unit_type_id = :id")
    boolean delete(@Bind("id") Long id);

    @SqlUpdate("update unit_type set unit_type_name = :unitTypeName, matcher_id = :matcherId, " +
            "vendor_name = :vendorName, description = :description, protocol = :protocol " +
            "where unit_type_id = :unitTypeId")
    boolean update(@BindBean UnittypeVO object);

    @SqlUpdate("insert into unit_type(unit_type_name, matcher_id, vendor_name, description, protocol) " +
            "values(:unitTypeName, :matcherId, :vendorName, :description, :protocol)")
    @GetGeneratedKeys
    Long add(@BindBean UnittypeVO object);
}
