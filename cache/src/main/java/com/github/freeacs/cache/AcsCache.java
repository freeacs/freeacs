package com.github.freeacs.cache;

import com.github.freeacs.dbi.ACSDao;
import com.github.freeacs.dbi.File;
import com.github.freeacs.dbi.FileType;
import com.github.freeacs.dbi.Unittype;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.sql.SQLException;

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
}
