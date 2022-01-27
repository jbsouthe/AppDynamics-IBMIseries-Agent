package com.cisco.josouthe.iseries.data;

public enum SyslogSeverity {
    ALERT       (1),
    CRITICAL    (2),
    ERROR       (3),
    WARNING     (4),
    NOTICE      (5),
    INFO        (6),
    DEBUG       (7),
    UNKNOWN     (-1);

    private final int level;

    SyslogSeverity( int i ) {
        this.level=i;
    }

    public int getLevel() { return level; }
    public static SyslogSeverity valueOf(int i) {
        switch (i) {
            case 1: return ALERT;
            case 2: return CRITICAL;
            case 3: return ERROR;
            case 4: return WARNING;
            case 5: return NOTICE;
            case 6: return INFO;
            case 7: return DEBUG;
            default: return UNKNOWN;
        }
    }
}
