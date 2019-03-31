package com.github.freeacs.service;

import com.github.freeacs.dao.PermissionDao;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {
    private final PermissionDao permissionDao;
    private final UnitTypeService unitTypeService;
    private final ProfileService profileService;

    @Autowired
    public PermissionService(PermissionDao permissionDao, UnitTypeService unitTypeService, ProfileService profileService) {
        this.permissionDao = permissionDao;
        this.unitTypeService = unitTypeService;
        this.profileService = profileService;
    }

    public List<PermissionDto> getByUser(UserDto userDto) {
        return permissionDao.getByUserId(userDto.getId())
                .flatMap(permission -> unitTypeService.getUnitTypeById(permission.getUnitTypeId())
                        .flatMap(unitTypeDto -> {
                            if (permission.getProfileId() != null) {
                                return profileService.getProfileById(permission.getProfileId())
                                        .map(profileDto -> new PermissionDto(
                                                permission.getId(),
                                                userDto,
                                                unitTypeDto,
                                                profileDto
                                        ));
                            }
                            return Option.of(new PermissionDto(
                                    permission.getId(),
                                    userDto,
                                    unitTypeDto,
                                    null
                            ));
                        }));
    }
}
