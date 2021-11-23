package main.java.com.cisco.josouthe.iseries;

import com.appdynamics.agent.api.Transaction;
import main.java.com.cisco.josouthe.iseries.exceptions.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class Agent {
    private static final Logger logger = LogManager.getFormatterLogger();
    private Database database;
    private ConcurrentHashMap<String,Object> exitCallStash = null;
    private ConcurrentHashMap<String, Transaction> transactionMap = null;

    public Agent( String configFileName ) throws ConfigurationException {
        LoggerContext loggerContext = Configurator.initialize("PropertiesConfig", configFileName);
        exitCallStash = new ConcurrentHashMap<>();
        transactionMap = new ConcurrentHashMap<>();
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
    }

    public static void main( String ... args ) throws Exception {
        String configFileName = "main/resources/config.properties";
        if (args.length > 0) configFileName = args[0];
        Agent agent = new Agent(configFileName);
    }
}
