package main.java.com.cisco.josouthe.iseries.exceptions;

public class ConfigurationException extends Exception{
    public ConfigurationException( String message, Object ... params ) {
        super(String.format(message,params));
    }
}
