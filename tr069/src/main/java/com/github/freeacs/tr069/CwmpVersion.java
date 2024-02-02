package com.github.freeacs.tr069;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public enum CwmpVersion {
    VER_1_0("1-0"),
    VER_1_1("1-1"),
    VER_1_2("1-2"),
    VER_1_3("1-3"),
    VER_1_4("1-4");

    private final String id;

    CwmpVersion(String id) {
        this.id = id;
    }

    public static String extractVersionFrom(String xml) {
        if (StringUtils.contains(xml,"xmlns:cwmp=\"urn:dslforum-org:cwmp-" + VER_1_0.id)) {
            return VER_1_0.id;
        }
        if (StringUtils.contains(xml,"xmlns:cwmp=\"urn:dslforum-org:cwmp-" + VER_1_1.id)) {
            return VER_1_1.id;
        }
        if (StringUtils.contains(xml,"xmlns:cwmp=\"urn:dslforum-org:cwmp-" + VER_1_2.id)) {
            return VER_1_2.id;
        }
        if (StringUtils.contains(xml,"xmlns:cwmp=\"urn:dslforum-org:cwmp-" + VER_1_3.id)) {
            return VER_1_3.id;
        }
        if (StringUtils.contains(xml,"xmlns:cwmp=\"urn:dslforum-org:cwmp-" + VER_1_4.id)) {
            return VER_1_4.id;
        }
        return VER_1_2.id;
    }
}
