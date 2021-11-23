package main.java.com.cisco.josouthe.iseries.data;

import java.sql.ResultSet;
import java.sql.SQLException;

//https://www.ibm.com/docs/en/i/7.4?topic=services-history-log-info-table-function
public class HistoryLogInfo {
    public int ordinalPosition, severity, syslogPriority;
    public String messageId, fromUser, fromJob, fromProgram,
            messageLibrary, messageFile,
            messageTokens, messageText, messageSecondLevelText, syslogEvent;
    public MessageType messageType;
    public MessageSubtype messageSubtype;
    public SyslogFacility syslogFacility;
    public SyslogSeverity syslogSeverity;
    public long messageTimestamp;

    public HistoryLogInfo(ResultSet resultSet) throws SQLException {
        ordinalPosition = resultSet.getInt("ORDINAL_POSITION");
        messageId = resultSet.getString("MESSAGE_ID");
        messageType = MessageType.valueOf(resultSet.getString("MESSAGE_TYPE"));
        messageSubtype = MessageSubtype.valueOf(resultSet.getString("MESSAGE_SUBTYPE").replace(" ", "_"));
        severity = resultSet.getInt("SEVERITY");
        messageTimestamp = resultSet.getTimestamp("MESSAGE_TIMESTAMP").getTime();
        fromUser = resultSet.getString("FROM_USER");
        fromProgram = resultSet.getString("FROM_PROGRAM");
        fromJob = resultSet.getString("FROM_JOB");
        messageLibrary = resultSet.getString("MESSAGE_LIBRARY");
        messageFile = resultSet.getString("MESSAGE_FILE");
        messageTokens = resultSet.getString("MESSAGE_TOKENS");
        messageText = resultSet.getString("MESSAGE_TEXT");
        messageSecondLevelText = resultSet.getString("MESSAGE_SECOND_LEVEL_TEXT");
        syslogEvent = resultSet.getString("SYSLOG_EVENT");
        syslogFacility = SyslogFacility.valueOf( resultSet.getInt("SYSLOG_FACILITY"));
        syslogSeverity = SyslogSeverity.valueOf( resultSet.getInt("SYSLOG_SEVERITY"));
        syslogPriority = resultSet.getInt("SYSLOG_PRIORITY");
    }
}
