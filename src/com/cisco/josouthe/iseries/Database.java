package com.cisco.josouthe.iseries;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.cisco.josouthe.iseries.data.HistoryLogInfo;
import com.cisco.josouthe.iseries.exceptions.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

public class Database {
    private static final Logger logger = LogManager.getFormatterLogger();
    private Properties properties = null;
    private HikariConfig hikariConfig;
    private HikariDataSource dataSource;

    public Database(Properties properties ) throws ConfigurationException {
        if( properties.getProperty("database.url","unset").equals("unset") || properties.getProperty("database.user","unset").equals("unset") || properties.getProperty("database.password","unset").equals("unset"))
            throw new ConfigurationException("Config properties must be present for database.url, database.user, and database.password");
        this.properties = properties;
        this.hikariConfig = new HikariConfig();
        this.hikariConfig.setJdbcUrl(properties.getProperty("database.url"));
        this.hikariConfig.setUsername(properties.getProperty("database.user"));
        this.hikariConfig.setPassword(properties.getProperty("database.password"));
        this.hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        this.hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        this.hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        this.hikariConfig.addDataSourceProperty("maximumPoolSize", properties.getProperty("scheduler.numberOfDatabaseThreads", "10"));
        this.dataSource = new HikariDataSource(this.hikariConfig);
    }

    private Connection getConnection() throws SQLException { return this.dataSource.getConnection(); }


    public HistoryLogInfo[] getHistoryLogInfo(long startTime, long endTime ) {
        ArrayList<HistoryLogInfo> data = new ArrayList<>();
        String query = String.format("SELECT * FROM TABLE( QSYS2.HISTORY_LOG_INFO( " +
                "START_TIME => '%s', END_TIME => '%s', GENERATE_SYSLOG => 'RFC3164' ) ) X", Util.getDateString(startTime), Util.getDateString(endTime));
        logger.debug("getHistoryLogInfo Query: '%s'",query);
        Statement statement = null;
        try {
            Connection connection = getConnection();
            statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while( resultSet.next() ) {
                data.add(new HistoryLogInfo(resultSet));
            }
        } catch (SQLException exception) {
            logger.warn("Query: '%s' SQL Exception: %s",query,exception.getMessage());
        } finally {
            if( statement != null ) {
                try {
                    statement.close();
                } catch (SQLException ignored) { }
            }
        }
        return data.toArray( new HistoryLogInfo[0] );
    }
}
