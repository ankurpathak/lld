package org.githhub.ankurpathak.lld.designpatterns.chainofresponsibility.logger;

import lombok.Setter;

enum LoggerLevel {
    INFO, DEBUG, ERROR
}


abstract class AbstractLogger {
    protected LoggerLevel level;

    @Setter
    private AbstractLogger nextLogger;


    public void logMessage(LoggerLevel level, String message) {
        if (this.level.ordinal() <= level.ordinal()) {
            write(message);
        }else if (nextLogger != null) {
            nextLogger.logMessage(level, message);
        }
    }

    abstract protected void write(String message);
}

class InfoLogger extends AbstractLogger {
    public InfoLogger() {
        this.level = LoggerLevel.INFO;
    }

    @Override
    protected void write(String message) {
        System.out.println("Info: " + message);
    }
}

class ErrorLogger extends AbstractLogger {
    public ErrorLogger() {
        this.level = LoggerLevel.ERROR;
    }

    @Override
    protected void write(String message) {
        System.out.println("Error: " + message);
    }
}

class DebugLogger extends AbstractLogger {
    public DebugLogger() {
        this.level = LoggerLevel.DEBUG;
    }

    @Override
    protected void write(String message) {
        System.out.println("Debug: " + message);
    }
}

class Logger extends AbstractLogger {
    private final AbstractLogger logger;

    public Logger(){
        logger = buildChain();
    }
    public void logMessage(LoggerLevel level, String message) {
        logger.logMessage(level, message);
    }

    @Override
    protected void write(String message) {
        logger.write(message);
    }

    private AbstractLogger buildChain(){
        AbstractLogger errorLogger = new ErrorLogger();
        AbstractLogger debugAbstractLogger = new DebugLogger();
        errorLogger.setNextLogger(debugAbstractLogger);
        AbstractLogger infoLogger = new InfoLogger();
        debugAbstractLogger.setNextLogger(infoLogger);
        return errorLogger;
    }
}

class LoggerClient {
    public static void main(String[] args) {
        Logger logger = new Logger();
        logger.logMessage(LoggerLevel.INFO, "This is an information.");
        logger.logMessage(LoggerLevel.DEBUG, "This is a debug level information.");
        logger.logMessage(LoggerLevel.ERROR, "This is an error information.");

        InfoLogger infoLogger = new InfoLogger();

        infoLogger.logMessage(LoggerLevel.INFO, "a This is an information.");
        infoLogger.logMessage(LoggerLevel.DEBUG, "a This is a debug level information.");
        infoLogger.logMessage(LoggerLevel.ERROR, "a This is an error information.");

    }
}

