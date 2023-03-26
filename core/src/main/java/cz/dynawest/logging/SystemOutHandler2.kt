/*
 * @(#)SystemOutHandler.java	1.13 05/11/17
 *
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package cz.dynawest.logging

import java.util.logging.*

/**
 * This <tt>Handler</tt> publishes log records to <tt>System.err</tt>.
 * By default the <tt>SimpleFormatter</tt> is used to generate brief summaries.
 *
 *
 * **Configuration:**
 * By default each <tt>ConsoleHandler</tt> is initialized using the following
 * <tt>LogManager</tt> configuration properties.  If properties are not defined
 * (or have invalid values) then the specified default values are used.
 *
 *  *    java.util.logging.ConsoleHandler.level
 * specifies the default level for the <tt>Handler</tt>
 * (defaults to <tt>Level.INFO</tt>).
 *  *    java.util.logging.ConsoleHandler.filter
 * specifies the name of a <tt>Filter</tt> class to use
 * (defaults to no <tt>Filter</tt>).
 *  *    java.util.logging.ConsoleHandler.formatter
 * specifies the name of a <tt>Formatter</tt> class to use
 * (defaults to <tt>java.util.logging.SimpleFormatter</tt>).
 *  *    java.util.logging.ConsoleHandler.encoding
 * the name of the character set encoding to use (defaults to
 * the default platform encoding).
 *
 *
 *
 * @version 1.13, 11/17/05
 * @since 1.4
 */
class SystemOutHandler2 : StreamHandler() {
    // Private method to configure a ConsoleHandler from LogManager
    // properties and/or default values as specified in the class
    // javadoc.
    private fun configure() {
        val manager = LogManager.getLogManager()
        val cname = javaClass.name
        val level: Level?
        level = try {
            Level.parse(manager.getProperty("$cname.level"))
        } catch (ex: Exception) {
            Level.INFO
        }
        setLevel(level)

        //setFilter(manager.getFilterProperty(cname + ".filter", null));
        formatter = getFormatterProperty("$cname.formatter", SimpleFormatter())
        try {
            encoding = manager.getProperty("$cname.encoding")
        } catch (ex: Exception) {
            try {
                encoding = null
            } catch (ex2: Exception) {
                // doing a setEncoding with null should always work.
                // assert false;
            }
        }
    }

    private fun getFormatterProperty(name: String?, defaultValue: Formatter): Formatter {
        val manager = LogManager.getLogManager()
        val `val` = manager.getProperty(name)
        try {
            if (`val` != null) {
                val clz = ClassLoader.getSystemClassLoader().loadClass(`val`)
                return clz.newInstance() as Formatter
            }
        } catch (ex: Exception) {
            // We got one of a variety of exceptions in creating the
            // class or creating an instance.
            // Drop through.
        }
        // We got an exception.  Return the defaultValue.
        return defaultValue
    }

    /**
     * Create a <tt>ConsoleHandler</tt> for <tt>System.err</tt>.
     *
     *
     * The <tt>ConsoleHandler</tt> is configured based on
     * <tt>LogManager</tt> properties (or their default values).
     *
     */
    init {
        //  sealed = false;
        configure()
        setOutputStream(System.err)
        //sealed = true;
    }

    /**
     * Publish a <tt>LogRecord</tt>.
     *
     *
     * The logging request was made initially to a <tt>Logger</tt> object,
     * which initialized the <tt>LogRecord</tt> and forwarded it here.
     *
     *
     * @param  record  description of the log event. A null record is
     * silently ignored and is not published
     */
    override fun publish(record: LogRecord) {
        super.publish(record)
        flush()
    }

    /**
     * Override <tt>StreamHandler.close</tt> to do a flush but not
     * to close the output stream.  That is, we do **not**
     * close <tt>System.err</tt>.
     */
    override fun close() {
        flush()
    }
}
