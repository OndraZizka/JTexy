/*
 * @(#)SimpleFormatter.java	1.15 05/11/17
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package cz.dynawest.logging

import java.io.PrintWriter
import java.io.StringWriter
import java.text.MessageFormat
import java.util.*
import java.util.logging.Formatter
import java.util.logging.LogRecord

/**
 * Print a brief summary of the LogRecord in a human readable
 * format.  The summary will typically be 1 or 2 lines.
 *
 * @version 1.15, 11/17/05
 * @since 1.4
 */
class SingleLineFormatter : Formatter() {
    var dat = Date()
    private var formatter: MessageFormat? = null
    private val args = arrayOfNulls<Any>(1)

    // Line separator string.  This is the value of the line.separator
    // property at the moment that the SimpleFormatter was created.
    //private String lineSeparator = (String) java.security.AccessController.doPrivileged(
    //        new sun.security.action.GetPropertyAction("line.separator"));
    private val lineSeparator = "\n"

    /**
     * Format the given LogRecord.
     * @param record the log record to be formatted.
     * @return a formatted log record
     */
    @Synchronized
    override fun format(record: LogRecord): String {
        val sb = StringBuffer()

        // Minimize memory allocations here.
        dat.time = record.millis
        args[0] = dat


        // Date and time 
        val text = StringBuffer()
        if (formatter == null) {
            formatter = MessageFormat(format)
        }
        formatter!!.format(args, text, null)
        sb.append(text)
        sb.append(" ")


        // Class name 
        if (record.sourceClassName != null) {
            sb.append(record.sourceClassName)
        } else {
            sb.append(record.loggerName)
        }

        // Method name 
        if (record.sourceMethodName != null) {
            sb.append(" ")
            sb.append(record.sourceMethodName)
        }
        sb.append(" - ") // lineSeparator
        val message = formatMessage(record)

        // Level
        sb.append(record.level.localizedName)
        sb.append(": ")

        // Indent - the more serious, the more indented.
        //sb.append( String.format("% ""s") );
        val iOffset = (1000 - record.level.intValue()) / 100
        for (i in 0 until iOffset) {
            sb.append(" ")
        }
        sb.append(message)
        sb.append(lineSeparator)
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
        private const val format = "{0,date} {0,time}"
    }
}
