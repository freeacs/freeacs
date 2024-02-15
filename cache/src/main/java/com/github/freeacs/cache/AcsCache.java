package com.github.freeacs.cache;

import com.github.freeacs.dbi.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.List;

@Component
public class AcsCache {

    @Autowired
    private ACSDao acsDao;

    @Cacheable(value = "files", key ="{#unittype.getId(), #fileType, #version}")
    public File getFile(FileType fileType, Unittype unittype, String version) throws SQLException {
        return acsDao.getFileByUnitTypeIdAndFileTypeAndVersion(unittype, fileType, version);
    }

    @CacheEvict(value = "files", allEntries = true)
    public void clearFilesCache() {}

    @Cacheable(value = "unit-types", key = "{#unitTypeName}")
    public Unittype getUnitType(String unitTypeName) {
        return acsDao.getUnitTypeByName(unitTypeName);
    }

    @Cacheable(value = "unit-types", key = "{#unitTypeId}")
    public Unittype getUnitTypeById(Integer unitTypeId) {
        return acsDao.getUnitTypeById(unitTypeId, true);
    }

    @Cacheable(value = "unit-types")
    public List<Unittype> getUnitTypes() {
        return acsDao.getUnitTypes();
    }

    @Cacheable(value = "unit-type-parameter", key = "{#unitTypeParamId}")
    public UnittypeParameter getUnitTypeParamById(Integer unitTypeParamId) {
        return acsDao.getUnittypeParameterByUnitTypeParameId(unitTypeParamId);
    }

    @Cacheable(value = "unit-type-parameter", key = "{#unitTypeParamId}")
    public UnittypeParameter getUnitTypeParamByName(Integer unitTypeId, String unitTypeParamName) {
        return acsDao.getUnittypeParameterByUnitTypeParameName(unitTypeId, unitTypeParamName);
    }

    @Cacheable(value = "profiles", key = "{#profileId}")
    public Profile getProfileById(Integer profileId) {
        return acsDao.getProfileById(profileId, true);
    }

    @CacheEvict(value = "unit-types", allEntries = true)
    public void clearUnitTypesCache() {}

    @Cacheable(value = "file-bytes", key = "{#fileId}")
    public byte[] getFileContents(int fileId) throws SQLException {
        return acsDao.getFileContents(fileId);
    }

    @CacheEvict(value = "file-bytes", allEntries = true)
    public void clearFileBytesCache() {}

    @CacheEvict(value = "units", key = "{#unit.getId()}")
    public void addOrChangeQueuedUnitParameters(Unit unit) throws SQLException {
        acsDao.addOrChangeQueuedUnitParameters(unit);
    }

    @Cacheable(value = "units", key = "{#unitId}")
    public Unit getUnitById(String unitId) throws SQLException {
        return acsDao.getUnitById(unitId, this::getUnitTypeById, this::getUnitTypes, this::getUnitTypeParamById, this::getProfileById);
    }

    @CacheEvict(value = "units", key = "{#unitId}")
    public void addOrChangeUnitParameters(String unitId, List<UnitParameter> upList) throws SQLException {
        acsDao.addOrChangeUnitParameters(upList);
    }

    public void addUnits(List<String> unitIds, Profile pr) throws SQLException {
        acsDao.addUnits(unitIds, pr);
    }

    @CacheEvict(value = "units", key = "{#unitId}")
    public void addOrChangeSessionUnitParameters(String unitId, List<UnitParameter> unitSessionParameters) throws SQLException {
        acsDao.addOrChangeSessionUnitParameters(unitSessionParameters);
    }

    @Cacheable(value = "jobs", key = "{#jobId}")
    public Job getJobById(Integer jobId) {
        return acsDao.getJobById(jobId);
    }

    @Cacheable(value = "jobs", key = "{#unitTypeId}")
    public List<Job> getJobsByUnitTypeId(Integer unitTypeId) {
        return acsDao.getJobsByUnitTypeId(unitTypeId);
    }

    public void addOrChangeUnittype(Unittype ut) {
        acsDao.addOrChangeUnittype(ut);
    }
}
