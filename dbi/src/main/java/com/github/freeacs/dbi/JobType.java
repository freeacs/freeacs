package com.github.freeacs.dbi;

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
        JobType jt = null;
        try {
            jt = valueOf(typeStr);
        } catch (Throwable t) { // Convert from old jobtype
            if ("SCRIPT".equals(typeStr)) {
                jt = TR069_SCRIPT;
            }
        }
        return jt;
    }
}