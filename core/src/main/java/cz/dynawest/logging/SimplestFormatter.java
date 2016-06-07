package cz.dynawest.logging;

import java.util.logging.*;
import java.io.*;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Print a brief summary of the LogRecord in a human readable format. The summary will typically be 1 or 2 lines.
 */
public class SimplestFormatter extends Formatter {

    //Date dat = new Date();
    //private final static String format = "{0,date} {0,time}";
    //private MessageFormat formatter;
    //private Object args[] = new Object[1];
    // Line separator string.  This is the value of the line.separator
    // property at the moment that the SimpleFormatter was created.
    //private String lineSeparator = (String) java.security.AccessController.doPrivileged(
    //        new sun.security.action.GetPropertyAction("line.separator"));
    private String lineSeparator = "\n";

    private final static DateFormat DF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    
    /**
     * Format the given LogRecord.
     *
     * @param record the log record to be formatted.
     * @return a formatted log record
     */
    public synchronized String format( LogRecord record ) {

        StringBuilder sb = new StringBuilder();

        /*
        // Minimize memory allocations here.
        dat.setTime( record.getMillis() );
        args[0] = dat;

        // Date and time
        StringBuffer text = new StringBuffer();
        if( formatter == null ) {
            formatter = new MessageFormat( format );
        }
        formatter.format( args, text, null );
        sb.append( text );
        sb.append( " " );


        // Class name
        if( record.getSourceClassName() != null ) {
            sb.append( record.getSourceClassName() );
        } else {
            sb.append( record.getLoggerName() );
        }

        // Method name
        if( record.getSourceMethodName() != null ) {
            sb.append( " " );
            sb.append( record.getSourceMethodName() );
        }
        sb.append(" - "); // lineSeparator
         */



        // Level.
        //sb.append(record.getLevel().getLocalizedName());
        Level level = record.getLevel();
        if( level == Level.WARNING ) {
            sb.append( "Warning:  " );
        } else if( level == Level.SEVERE ) {
            sb.append( "Error!    " );
        } else {
            sb.append( "          " );
        }


        // The message itself.
        //String message = formatMessage( record );
        //sb.append( message );

        // Date time
        Date date = new Date(record.getMillis());
        String dateStr;
        synchronized(DF) {
            dateStr = DF.format( date );
        }
        sb.append( dateStr ).append(" ");

        // Class name.
        if( record.getSourceClassName() != null ) {
            sb.append( record.getSourceClassName() );
        } else {
            sb.append( record.getLoggerName() );
        }

        // Method name.
        if( record.getSourceMethodName() != null )
            sb.append(" ").append( record.getSourceMethodName() );

        sb.append(" ");

        if( record.getParameters() != null ){
            //new MessageFormat( record.getMessage() ).format( , null );
            sb.append( MessageFormat.format( record.getMessage(), record.getParameters() ) );
        }
        else {
            sb.append( record.getMessage() );
        }

        // New line.
        sb.append( lineSeparator );

        // Exception.
        if( record.getThrown() != null ) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter( sw );
                record.getThrown().printStackTrace( pw );
                pw.close();
                sb.append( sw.toString() );
            } catch( Exception ex ) {
            }
        }

        return sb.toString();
    }
}
