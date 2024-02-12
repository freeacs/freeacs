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

    @CacheEvict(value = "unit-types", allEntries = true)
    public void clearUnitTypesCache() {}

    @Cacheable(value = "file-bytes", key = "{#fileId}")
    public byte[] getFileContents(int fileId) throws SQLException {
        return acsDao.getFileContents(fileId);
    }

    @CacheEvict(value = "file-bytes", allEntries = true)
    public void clearFileBytesCache() {}

    public void addOrChangeQueuedUnitParameters(Unit unit) throws SQLException {

    }

    public Unit getUnitById(String username) throws SQLException {
        return null;
    }

    public void addOrChangeUnitParameters(List<UnitParameter> upList) throws SQLException {

    }

    public void addUnits(List<String> unitIds, Profile pr) throws SQLException {

    }

    public void addOrChangeSessionUnitParameters(List<UnitParameter> unitSessionParameters, Profile profile) throws SQLException {

    }
}
