package com.saucelabs.grid;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @author Ross Rowe
 */
public class LogFormatter extends Formatter {

    private static final String DATE_FORMAT = "yyyy/MM/dd hh:mm:ss.SSSS";
    @Override
    public String format(LogRecord record) {
        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        StringBuilder builder = new StringBuilder();
        builder.append("[Thread-").append(record.getThreadID()).append("] ");
        builder.append(dateFormat.format(new Date(record.getMillis()))).append(' ');
        builder.append('[').append(record.getSourceClassName()).append(".");
        builder.append(record.getSourceMethodName()).append("] -");
        builder.append('[').append(record.getLevel()).append("] - ");
        builder.append(formatMessage(record));
        builder.append("\n");
        if(record.getThrown() != null) {
        	record.getThrown().printStackTrace();
        }
        return builder.toString();
    }
}
