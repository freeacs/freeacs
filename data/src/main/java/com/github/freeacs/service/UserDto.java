package com.github.freeacs.service;

import io.vavr.collection.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

@Data
@Wither
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String secret;
    private String fullname;
    private String accessList;

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(username);
    }

    public boolean hasAdminPermission() {
        return "admin".equalsIgnoreCase(accessList);
    }

    public List<String> getAccessList() {
        if (accessList != null && accessList.startsWith("WEB")) {
            String[] modules = accessList.substring(1, accessList.length() - 1).split(",");
            return io.vavr.collection.List.of(modules);
        }
        if (accessList != null) {
            return List.of(accessList);
        }
        return List.empty();
    }
}
