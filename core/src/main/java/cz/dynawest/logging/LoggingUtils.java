
package cz.dynawest.logging;


import java.io.*;
import java.util.logging.*;


/**
 *
 * @author Ondrej Zizka
 */
public class LoggingUtils
{
  private static final Logger log = Logger.getLogger( LoggingUtils.class.getName() );

  
  /** Sets up logging. Uses "#/logging.properties" as default path. */
  public static void initLogging() {
    initLogging("#/logging.properties");
  }


  /** Sets up logging. */
  public static void initLogging( String filePath ) {
    String logConfigFile = System.getProperty("java.util.logging.config.file", filePath);
    try {
      InputStream is;
      if( logConfigFile.startsWith("#") )
        is = LoggingUtils.class.getResourceAsStream( logConfigFile.substring(1) );
        // "Use getClass().getClassLoader().findResource("path") instead."
      else
        is = new FileInputStream(logConfigFile);

			log.info("Loading logging conf from: "+logConfigFile+" (set in sys var java.util.logging.config.file)");
      if( is == null ){
        log.severe("Log config file not found: "+logConfigFile);
      }
      else{
        LogManager.getLogManager().readConfiguration( is );
      }
    }catch(IOException ex){
      System.err.println("Error loading logging conf from ["+logConfigFile+"]. Using default.");
    }
  }


}// class LoggingUtils
