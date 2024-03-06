package com.github.freeacs.dbi.domain;

public enum JobType {
    CONFIG,
    KICK,
    RESET,
    RESTART,
    SHELL,
    SOFTWARE,
    TELNET,
    TR069_SCRIPT;

    public boolean requireFile() {
        return this == SHELL || this == SOFTWARE || this == TELNET || this == TR069_SCRIPT;
    }

    public FileType getCorrelatedFileType() {
        if (this == SHELL) {
            return FileType.SHELL_SCRIPT;
        } else if (this == SOFTWARE) {
            return FileType.SOFTWARE;
        } else if (this == TELNET) {
            return FileType.TELNET_SCRIPT;
        } else if (this == TR069_SCRIPT) {
            return FileType.TR069_SCRIPT;
        }
        return null;
    }

    public static JobType fromString(String typeStr) {
        if ("SCRIPT".equals(typeStr)) {
            return TR069_SCRIPT;
        }
        return valueOf(typeStr.toUpperCase());
    }
}