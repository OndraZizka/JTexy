package cz.dynawest.logging

import java.io.PrintWriter
import java.io.StringWriter
import java.text.DateFormat
import java.text.MessageFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Formatter
import java.util.logging.Level
import java.util.logging.LogRecord

/**
 * Print a brief summary of the LogRecord in a human readable format. The summary will typically be 1 or 2 lines.
 */
class SimplestFormatter : Formatter() {
    //Date dat = new Date();
    //private final static String format = "{0,date} {0,time}";
    //private MessageFormat formatter;
    //private Object args[] = new Object[1];
    // Line separator string.  This is the value of the line.separator
    // property at the moment that the SimpleFormatter was created.
    //private String lineSeparator = (String) java.security.AccessController.doPrivileged(
    //        new sun.security.action.GetPropertyAction("line.separator"));
    private val lineSeparator = "\n"

    /**
     * Format the given LogRecord.
     *
     * @param record the log record to be formatted.
     * @return a formatted log record
     */
    @Synchronized
    override fun format(record: LogRecord): String {
        val sb = StringBuilder()

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
        val level = record.level
        if (level === Level.WARNING) {
            sb.append("Warning:  ")
        } else if (level === Level.SEVERE) {
            sb.append("Error!    ")
        } else {
            sb.append("          ")
        }


        // The message itself.
        //String message = formatMessage( record );
        //sb.append( message );

        // Date time
        val date = Date(record.millis)
        var dateStr: String?
        synchronized(DF) { dateStr = DF.format(date) }
        sb.append(dateStr).append(" ")

        // Class name.
        if (record.sourceClassName != null) {
            sb.append(record.sourceClassName)
        } else {
            sb.append(record.loggerName)
        }

        // Method name.
        if (record.sourceMethodName != null) sb.append(" ").append(record.sourceMethodName)
        sb.append(" ")
        if (record.parameters != null) {
            //new MessageFormat( record.getMessage() ).format( , null );
            sb.append(MessageFormat.format(record.message, *record.parameters))
        } else {
            sb.append(record.message)
        }

        // New line.
        sb.append(lineSeparator)

        // Exception.
        if (record.thrown != null) {
            try {
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                record.thrown.printStackTrace(pw)
                pw.close()
                sb.append(sw.toString())
            } catch (ex: Exception) {
            }
        }
        return sb.toString()
    }

    companion object {
        private val DF: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    }
}
