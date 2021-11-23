package main.java.com.cisco.josouthe.iseries;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Util {
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    public static String getDateString() { return getDateString(now()); }
    public static String getDateString(long dateTime) {
        return ZonedDateTime.ofInstant(new Date(dateTime).toInstant(), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSS000"));
    }
    public static long now() { return new Date().getTime(); }
}
