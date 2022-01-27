package com.cisco.josouthe.iseries;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static Pattern patternProcessingTimeUsed = Pattern.compile("after it used (\\d*\\.?\\d+\\s+\\S+) processing unit time.");
    private static Pattern patternMaxTempSpaceUsed = Pattern.compile("The maximum temporary storage used was (\\d+\\s+\\S+)\\."); // The maximum temporary storage used was 24 megabytes.
    private static Pattern patternProcessInformation = Pattern.compile("Job (?<processId>\\d+)/(?<btName>\\S+)\\s+(?<processState>\\S+)");
    private static Pattern patternJobEndCode = Pattern.compile("end code (\\d+)");

    public static String getDateString() { return getDateString(now()); }
    public static String getDateString(long dateTime) {
        return ZonedDateTime.ofInstant(new Date(dateTime).toInstant(), ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSS000"));
    }
    public static long now() { return new Date().getTime(); }
    public static void sleep( long milliseconds ) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ignored) {
            //ignored
        }
    }

    public static String parseProcessingTime( String text ) {
        Matcher matcher = patternProcessingTimeUsed.matcher(text);
        if(matcher.find()) return matcher.group(0);
        return "";
    }

    public static String parseTempSpaceUsed( String text ) {
        Matcher matcher = patternMaxTempSpaceUsed.matcher(text);
        if(matcher.find()) return matcher.group(0);
        return "";
    }

    public static String parseProcessId(String text ) {// Job 370690/ICESYS/LEADUPLOAD ended on 12/01/21 at 08:43:48; .024 seconds used; end code 0 .
        Matcher matcher = patternProcessInformation.matcher(text);
        if(matcher.find()) return matcher.group("processId");
        return "";
    }

    public static String parseBtName(String text ) {
        Matcher matcher = patternProcessInformation.matcher(text);
        if(matcher.find()) return matcher.group("btName");
        return "";
    }

    public static String parseJobEndCode(String text ) {
        Matcher matcher = patternJobEndCode.matcher(text);
        if(matcher.find()) return matcher.group(0);
        return "";
    }

    public static String parseProcessState(String text ) {
        Matcher matcher = patternProcessInformation.matcher(text);
        if(matcher.find()) return matcher.group("processState");
        return "";
    }
}
