package main.java.com.cisco.josouthe.iseries.data;

public enum SyslogFacility {
    USER        (1),
    SECURITY    (4),
    UNKNOWN     (-1);

    private final int level;

    SyslogFacility( int i ) {
        this.level=i;
    }

    public int getLevel() { return level; }
    public static SyslogFacility valueOf(int i) {
        switch (i) {
            case 1: return USER;
            case 4: return SECURITY;
            default: return UNKNOWN;
        }
    }
}
