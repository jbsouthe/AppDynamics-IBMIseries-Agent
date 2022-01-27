package com.cisco.josouthe.iseries;

import com.appdynamics.agent.api.AppdynamicsAgent;
import com.appdynamics.agent.api.EntryTypes;
import com.appdynamics.agent.api.Transaction;
import com.appdynamics.apm.appagent.api.DataScope;
import com.cisco.josouthe.iseries.data.HistoryLogInfo;
import com.cisco.josouthe.iseries.exceptions.ConfigurationException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.*;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Agent {
    private Logger logger = LogManager.getFormatterLogger();
    private Database database;
    private ConcurrentHashMap<String,Object> exitCallStash = null;
    private ConcurrentHashMap<String, Transaction> transactionMap = null;
    private Set<DataScope> snapshotDatascopeOnly = null;

    public Agent( String configFileName ) throws ConfigurationException {
        LoggerContext loggerContext = Configurator.initialize("Main", configFileName);
        Configurator.setAllLevels("", Level.ALL);
        logger = loggerContext.getRootLogger();
        exitCallStash = new ConcurrentHashMap<>();
        transactionMap = new ConcurrentHashMap<>();
        snapshotDatascopeOnly = new HashSet<DataScope>();
        snapshotDatascopeOnly.add(DataScope.SNAPSHOTS);
        logger.info("System starting with configuration: " + configFileName );

        Properties props = new Properties();
        File configFile = new File(configFileName);
        InputStream is = null;
        if( configFile.canRead() ) {
            try {
                is = new FileInputStream(configFile);
            } catch (FileNotFoundException e) {
                System.err.println("Config file not found! Exception: "+e);
            }
        } else {
            URL configFileURL = getClass().getClassLoader().getResource(configFileName);
            logger.info("Config file URL: " + configFileURL.toExternalForm());
            is = getClass().getClassLoader().getResourceAsStream(configFileName);
        }
        try {
            props.load(is);
        } catch (IOException e) {
            logger.error("Error loading configuration: "+ configFileName +" Exception: "+ e.getMessage());
            return;
        }
        if( props.getProperty("appenders","nothing").equals("nothing") ) {
            Configurator.shutdown(loggerContext);
            Configurator.initialize("Failsafe", "main/resources/default-log4j2.properties");
            logger.info("System starting with configuration: " + configFileName );
            logger.info("No logging configuration defined, using default log4j2 config in main/resources/default-log4j2.properties, if this isn't wanted behavior, update config");
        }

        this.database = new Database(props);
        long startTimestamp = Util.now();
        long endTimestamp = -1;
        while(true) {
            if( endTimestamp == -1 ) {
                endTimestamp = startTimestamp;
                startTimestamp -= 300000;
            } else {
                startTimestamp = endTimestamp;
                endTimestamp = Util.now();
            }
            HistoryLogInfo[] historyLogInfos = database.getHistoryLogInfo( startTimestamp, endTimestamp);
            if( historyLogInfos != null && historyLogInfos.length > 0 ) {
                logger.info("Processing %d History Log Messages", historyLogInfos.length);
                for( HistoryLogInfo historyLogInfo : historyLogInfos ) {
                    process(historyLogInfo);
                }
            }
            Util.sleep( Long.parseLong(props.getProperty("agent.sleepMilliseconds","30000")) );
        }
    }

    private void process( HistoryLogInfo historyLogInfo ) {
        logger.info("Processing: %s", historyLogInfo);
        if(historyLogInfo.isStarted()) { //this is a start message for a job, start a BT
            Transaction transaction = AppdynamicsAgent.startTransaction(historyLogInfo.getBtName(), null, EntryTypes.POJO, false);
            transaction.collectData("FROM_USER", historyLogInfo.fromUser, snapshotDatascopeOnly);
            transaction.collectData("FROM_PROGRAM", historyLogInfo.fromProgram, snapshotDatascopeOnly);
            transaction.collectData("FROM_JOB", historyLogInfo.fromJob, snapshotDatascopeOnly);
            transaction.collectData("MESSAGE_TEXT", historyLogInfo.messageText, snapshotDatascopeOnly);
            transactionMap.put(historyLogInfo.fromJob, transaction);
        } else if( historyLogInfo.isEnded() ) { //end an already started BT
            Transaction transaction = transactionMap.remove( historyLogInfo.fromJob );
            transaction.collectData("MESSAGE_TEXT", historyLogInfo.messageText, snapshotDatascopeOnly);
            transaction.collectData("MESSAGE_SECOND_LEVEL_TEXT", historyLogInfo.messageSecondLevelText, snapshotDatascopeOnly);
            if( historyLogInfo.endedWithError() ) {
                transaction.markAsError("Error: "+ historyLogInfo.messageText + historyLogInfo.messageSecondLevelText);
            }
            transaction.end();
        }
    }

    public static void main( String ... args ) throws Exception {
        String configFileName = "main/resources/config.properties";
        if (args.length > 0) {
            configFileName = args[0];
        } else {
            System.out.println("ERROR: the first and only argument should be a configuration file, this isn't going to work well without one!");
            return;
        }
        Agent agent = new Agent(configFileName);
    }
}
