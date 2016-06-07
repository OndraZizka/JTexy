
package cz.dynawest.jtexy.modules;

import cz.dynawest.jtexy.JTexy;
import cz.dynawest.jtexy.RegexpInfo;
import cz.dynawest.jtexy.TexyException;
import cz.dynawest.jtexy.events.PostProcessEvent;
import cz.dynawest.jtexy.parsers.AroundEventListener;
import cz.dynawest.jtexy.parsers.BeforeAfterEventListener;
import cz.dynawest.jtexy.parsers.TexyEventListener;
import cz.dynawest.jtexy.util.PropertiesLoader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;



/**
 * Module base class - implements regexp map, a jtexy property,
 * and init() which reads the .properties file.
 *
 * @author Ondrej Zizka
 */
public abstract class TexyModule
{
    private static final Logger log = Logger.getLogger( TexyModule.class.getName() );

    private static final String DEFAULT_PROPS_PATH = "#/cz/dynawest/jtexy/modules/%sPatterns.properties";
	protected String getPropsFilePath(){ return String.format( DEFAULT_PROPS_PATH, this.getClass().getSimpleName() ); }

	/** JTexy backreference. */
	protected JTexy texy = null;
	/** JTexy backreference. */
	public JTexy getTexy() {		return texy;	}
	public void setTexy(JTexy texy) {		this.texy = texy;	}




	/* --- Event listeners. --- */

	/** Override: return all module's parser event listeners. */
	public abstract TexyEventListener[] getEventListeners();

    

	/* --- Regexp infos. --- */
	
	private LinkedHashMap<String, RegexpInfo> regexpInfos	= new LinkedHashMap<String, RegexpInfo>();

	public LinkedHashMap<String, RegexpInfo> getRegexpInfos() {
		return this.regexpInfos; // TBD: Make unmodifiable after initialization.
	}

    /** @returns  RegexpInfo by name. Ex.: "phrase/span" */
	public RegexpInfo getRegexpInfo( String name ){
		return (RegexpInfo) this.regexpInfos.get( name );
	}

	public void addRegexpInfo( RegexpInfo ri) {
		this.regexpInfos.put( ri.name, ri );
	}

	protected void clearRegexpInfos() {
		this.regexpInfos = (LinkedHashMap<String, RegexpInfo>) new LinkedHashMap();
	}




	/**
	 * Override this: Return the pattern handler named in the .properties file.
	 */
	protected abstract PatternHandler getPatternHandlerByName( String name );


	/**
	 * Default behavior: Register all listeners.
	 */
	public void onRegister() throws TexyException
	{
		// Initialize.
		// When it was in a constructor, there was an "unreported exception in constructor".
		log.finer("Intializing module "+this.getClass().getName() + "...");
		init();


		// -- Add all module's patterns in the order it gave them. TBD: Move to onRegister(). -- //
		log.finest("  "+this.getRegexpInfos().size()+" regexpInfos.");

		//for( RegexpInfo ri : module.getRegexpInfos(). ){
		for (Entry<String, RegexpInfo> e: this.getRegexpInfos().entrySet()) {
			getTexy().addPattern(e.getValue());
		}
		

		// -- Register all listeners. -- //
		TexyEventListener[] listeners = this.getEventListeners();

		log.finer("Registering "+listeners.length+" listeners for "+this.getClass().getSimpleName()+".");

		for( TexyEventListener lis : listeners ){
			log.finer("  "+lis.getEventClass().getName());
			if( lis instanceof AroundEventListener )
				getTexy().getAroundHandlers().addHandler( (AroundEventListener) lis);
            else 
				getTexy().getNormalHandlers().addHandler( lis );
		}
		// PatternHandlers / RegexpInfos are registered in JTexy#registerModule().

	}// onRegister()

	
	
	
	/**
	 *   Constructor - initializes the module (calls init()).
	 */
	/*public TexyModule() throws TexyException {
        this.init();
	}*/





	/**
	 * Like init( String propertiesFilePath ),
	 * only the default path is
	 * "#/cz/dynawest/jtexy/modules/" + {ModuleClassName} + "Patterns.properties".
	 */
	protected void init() throws TexyException {
        if( null != this.getPropsFilePath() )
            this.loadRegexFromPropertiesFile( this.getPropsFilePath() );
	}

    
	/**
	 *  Init - loads regular expressions and their metainfo
	 *  like mode of processing, bound html element etc.
     * 
     *  Synchronized due to clearRegexpInfos() and addRegexpInfo().
	 */
	protected synchronized void loadRegexFromPropertiesFile( String propsFilePath ) throws TexyException {

		try {
			// Reset regex infos.
			clearRegexpInfos();

			// Map for lookups, List to keep the order.
			Map<String, RegexpInfo> reMap = new LinkedHashMap();


			// Load properties file (and get default values etc).
			Properties props = PropertiesLoader.loadProperties( propsFilePath, this.getClass() );

			RegexpInfo.Type defaultReType = RegexpInfo.Type.LINE;
			
			// First check for "global" settings.
			String typeVal = props.getProperty("default.type");
			if( typeVal != null ){
				defaultReType = getReTypeByName(typeVal, "default.type");
			}



			List<TexyException> initErrors = new ArrayList();

			// For each key in properties file...
			for( Object key : props.keySet() ){

                processProperty( key, props, defaultReType, reMap, initErrors );

			}// for each property.

			TexyException.throwIfErrors(
                "Errors when initializing module: "+this.getClass().getName(), initErrors);


			// Fill up the module's RE map/list.
			log.finer("Patterns added for '" + this.getClass().getSimpleName() + "':" + reMap.size());
			final PatternHandler defaultHandler = this.getPatternHandlerByName("default");
			for( RegexpInfo ri : reMap.values() )
			{
				// Default .htmlelement is the part of pattern name after '/' .
				// I.e.  phrase/sub =>  default element is "sub".
				if( StringUtils.isEmpty( ri.htmlElement ) )
					ri.htmlElement = StringUtils.substringAfterLast( ri.name, "/" );

				// If no handler yet, bind the default one.
				if( null == ri.handler )
					ri.handler = defaultHandler;

				this.addRegexpInfo( ri );
				log.finest( "    Pattern " + ri.name + ": "+ri.type+" "+ri.getPerlRegexp() + " => " + ri.getRegexp() );
			}
		}
		catch( IOException ex ){
			throw new TexyException(ex);
		}
        catch (TexyException ex){
            throw new TexyException("Error when processing " + propsFilePath + ":\n" + ex.getMessage());
        }


    }// init(){}



    /**
     * 
     */
    private void processProperty(
            Object key, Properties props, RegexpInfo.Type defaultReType, 
            Map<String, RegexpInfo> reMap, List<TexyException> initErrors)
    throws TexyException 
    {

        String propName = (String)key;
        String name = propName;
        String value = props.getProperty( propName );
        String metaName = null;

        // When using java.util.Properties, props are not processed
        // in order of appearance in the .properties file (Map).
        if( StringUtils.startsWith(propName, "default") )  return;
        if( StringUtils.startsWith(propName, "_") )        return;

        // abc = xy      If the property name does not contain a dot, it's a name of a pattern.
        // abc.ef = 15   If it contains a dot, it's a sub-property of some pattern.
        int dotPos = propName.indexOf('.');
        if( dotPos != -1 ){
            name = propName.substring( 0, dotPos );
            metaName = propName.substring( dotPos+1 );
        }

        // Get or create the temporary regexp info.
        RegexpInfo ri = reMap.get( name );
        if( null == ri ){
            // TODO: How do we know whether it's LINE or BLOCK?  Need new property in the file.
            ri = new RegexpInfo( name, defaultReType );
            reMap.put( name, ri );
        }

        // Handle the value.
        if( dotPos == -1 ){
            // Regular expression of this pattern.
            try {
                ri.parseRegexp( value );
            } catch ( TexyException ex ){
                throw new TexyException("Error parsing pattern '"+name+"'.", ex);
            }
        }else{
            // Pattern metadata.
            if( "handler".equals(metaName) ){
                ri.handler = getPatternHandlerByName(value);
                if( ri.handler == null )
                    initErrors.add( new TexyException("Unknown handler for pattern '"+name+"': "+value) );
            }else if( "type".equals(metaName) ){
                ri.type = getReTypeByName(value, name);
                //if( ri.type == null )
                //	initErrors.add( new TexyException("Unknown type of pattern '"+name+"': "+metaName) );
            }else if( "htmlelement".equals(metaName) ){
                ri.htmlElement = value;
            }else{
                log.warning("Unknown regexp metadata: "+metaName);
            }
        }
    }// processProperty


    
	/** Not much clean code, but I'm lazy to introduce some parser context class for this. */
	private RegexpInfo.Type getReTypeByName( String typeName, String metaName ) throws TexyException {
		try {
			return RegexpInfo.Type.valueOf( typeName.toUpperCase() );
		} catch( IllegalArgumentException ex ){
			throw new TexyException("Pattern type must be LINE or BLOCK, was '"+typeName+"' for '"+metaName+"'.");
		}
	}



}// class TexyModule
