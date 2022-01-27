package com.cisco.josouthe.iseries.data;

import com.cisco.josouthe.iseries.Util;

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
    private String processId, btName, processingUnitTime, maxTempStorageUsed, jobEndingCode, processState;

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
        setMessageText(resultSet.getString("MESSAGE_TEXT"));
        setMessageSecondLevelText( resultSet.getString("MESSAGE_SECOND_LEVEL_TEXT"));
        syslogEvent = resultSet.getString("SYSLOG_EVENT");
        syslogFacility = SyslogFacility.valueOf( resultSet.getInt("SYSLOG_FACILITY"));
        syslogSeverity = SyslogSeverity.valueOf( resultSet.getInt("SYSLOG_SEVERITY"));
        syslogPriority = resultSet.getInt("SYSLOG_PRIORITY");
    }

    private void setMessageSecondLevelText(String message_second_level_text) {
        //&N Cause . . . . . :   Job 370690/ICESYS/LEADUPLOAD completed on 12/01/21 at 08:43:48 after it used .024 seconds processing unit time. The maximum temporary storage used was 24 megabytes. The job had ending code 0. The job ended after 1 routing steps with a secondary ending code of 0.  The job ending codes and their meanings are as follows: &P  0 - The job completed normally. &P 10 - The job completed normally during controlled ending or controlled subsystem ending. &P 20 - The job exceeded end severity (ENDSEV job attribute). &P 30 - The job ended abnormally. &P 40 - The job ended before becoming active. &P 50 - The job ended while the job was active. &P 60 - The subsystem ended abnormally while the job was active. &P 70 - The system ended abnormally while the job was active. &P 80 - The job ended (ENDJOBABN command). &P 90 - The job was forced to end after the time limit ended (ENDJOBABN command). &N Recovery  . . . :   For more information, see the Work management topic collection in the Systems management category in the IBM i Information Center, http://www.ibm.com/systems/i/infocenter/.
        this.messageSecondLevelText = message_second_level_text;
        processingUnitTime = Util.parseProcessingTime(message_second_level_text);
        maxTempStorageUsed = Util.parseTempSpaceUsed(message_second_level_text);
    }

    public void setMessageText(String message_text) {
        this.messageText = message_text;
        /* Sample messages:
            Job 370690/ICESYS/LEADUPLOAD started on 12/01/21 at 08:43:48 in subsystem ICEBCH in ICESYSLIB. Job entered system on 12/01/21 at 08:43:48.
            Job 370690/ICESYS/LEADUPLOAD ended on 12/01/21 at 08:43:48; .024 seconds used; end code 0 .
         */
        processId = Util.parseProcessId( message_text );
        btName = Util.parseBtName( message_text );
        processState = Util.parseProcessState( message_text );
        jobEndingCode = Util.parseJobEndCode( message_text );
    }

    public boolean isStarted() { return "started".equalsIgnoreCase(processState); }
    public boolean isEnded() { return "ended".equalsIgnoreCase(processState); }
    public String getBtName() { return btName; }
    public String toString() {
        return String.format("HistoryLogInfo messageid: %s messageType: %s messageTimestamp: %d user: %s job: %s program: %s text: %s",
                messageId, messageType, messageTimestamp, fromUser, fromJob, fromProgram, messageText
        );
    }

    public boolean endedWithError() {
        if( isEnded() && !"0".equals(jobEndingCode) ) return true;
        return false;
    }
}
