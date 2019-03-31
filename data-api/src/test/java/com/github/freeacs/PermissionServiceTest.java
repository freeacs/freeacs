package com.github.freeacs;

import com.github.freeacs.service.PermissionDto;
import com.github.freeacs.service.PermissionService;
import com.github.freeacs.service.UserService;
import io.vavr.collection.List;
import org.jdbi.v3.core.Jdbi;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PermissionServiceTest extends BaseTest {

    @Autowired
    private Jdbi jdbi;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private UserService userService;

    @Before
    public void init() {
        super.init();
        jdbi.withHandle(handle ->
                handle.createUpdate("insert into unit_type(unit_type_name, protocol) values('Test', 'TR069');").execute());
        jdbi.withHandle(handle ->
                handle.createUpdate("insert into profile(unit_type_id, profile_name) values(1, 'Default');").execute());
        jdbi.withHandle(handle ->
                handle.createUpdate("insert into permission_(user_id, unit_type_id, profile_id) values(1, 1, 1);").execute());

        jdbi.withHandle(handle ->
                handle.createUpdate("insert into user_(username, secret, fullname, accesslist, is_admin) " +
                        "values('test', 'A33E0694639DA19CF58FA1130B2D767F6F4531019FDD45D73D178CED', 'Test', 'WEB[bla]', 0);").execute());
        jdbi.withHandle(handle ->
                handle.createUpdate("insert into permission_(user_id, unit_type_id) values(2, 1);").execute());
    }

    @Test
    public void getByUserIdWithBothUnitTypeAndProfilePermission() {
        List<PermissionDto> permissionDtos = permissionService.getByUser(userService.getByUserName("admin").get());
        assertEquals(1, permissionDtos.size());
        assertEquals(1, permissionDtos.get(0).getId().longValue());
        assertEquals(1, permissionDtos.get(0).getProfile().getId().longValue());
        assertEquals(1, permissionDtos.get(0).getUnitType().getId().longValue());
        assertEquals(1, permissionDtos.get(0).getUser().getId().longValue());
    }

    @Test
    public void getByUserIdWithOnlyUnitTypePermission() {
        List<PermissionDto> permissionDtos = permissionService.getByUser(userService.getByUserName("test").get());
        assertEquals(1, permissionDtos.size());
        assertEquals(2, permissionDtos.get(0).getId().longValue());
        assertNull(permissionDtos.get(0).getProfile());
        assertEquals(1, permissionDtos.get(0).getUnitType().getId().longValue());
        assertEquals(2, permissionDtos.get(0).getUser().getId().longValue());
    }
}
