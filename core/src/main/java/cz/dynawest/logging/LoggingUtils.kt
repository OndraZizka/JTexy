package cz.dynawest.logging

import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.util.logging.LogManager
import java.util.logging.Logger

/**
 *
 * @author Ondrej Zizka
 */
object LoggingUtils {
    private val log = Logger.getLogger(LoggingUtils::class.java.name)

    /** Sets up logging. Uses "#/logging.properties" as default path.  */
    @JvmStatic @JvmOverloads
    fun initLogging(filePath: String? = "#/logging.properties") {
        val logConfigFile = System.getProperty("java.util.logging.config.file", filePath)
        try {
            val `is`: InputStream?
            `is` =
                if (logConfigFile.startsWith("#")) LoggingUtils::class.java.getResourceAsStream(logConfigFile.substring(1)) else FileInputStream(
                    logConfigFile
                )
            log.info("Loading logging conf from: $logConfigFile (set in sys var java.util.logging.config.file)")
            if (`is` == null) {
                log.severe("Log config file not found: $logConfigFile")
            } else {
                LogManager.getLogManager().readConfiguration(`is`)
            }
        } catch (ex: IOException) {
            System.err.println("Error loading logging conf from [$logConfigFile]. Using default.")
        }
    }
}
