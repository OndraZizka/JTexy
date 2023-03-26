package cz.dynawest.jtexy.modules

import cz.dynawest.jtexy.JTexy
import cz.dynawest.jtexy.RegexpInfo
import cz.dynawest.jtexy.TexyException
import cz.dynawest.jtexy.events.AroundEvent
import cz.dynawest.jtexy.events.AroundEventListener
import cz.dynawest.jtexy.events.TexyEvent
import cz.dynawest.jtexy.events.TexyEventListener
import cz.dynawest.jtexy.util.PropertiesLoader
import org.apache.commons.lang.StringUtils
import java.io.IOException
import java.util.*
import java.util.logging.*

/**
 * Module base class - implements regexp map, a jtexy property,
 * and init() which reads the .properties file.
 *
 * @author Ondrej Zizka
 */
abstract class TexyModule {
    protected open val propsFilePath: String?
        get() = String.format(DEFAULT_PROPS_PATH, this.javaClass.simpleName)

    /** JTexy backreference.  */
    lateinit var texy: JTexy

    /* --- Event listeners. --- */
    /** Override: return all module's parser event listeners.  */
    protected abstract val eventListeners: List<TexyEventListener<in TexyEvent>>

    // TBD: Make unmodifiable after initialization.
    /* --- Regexp infos. --- */
    var regexpInfos = LinkedHashMap<String, RegexpInfo>()
        private set

    /** @returns  RegexpInfo by name. Ex.: "phrase/span"
     */
    fun getRegexpInfo(name: String): RegexpInfo {
        return regexpInfos[name] ?: throw Exception("RegexpInfo not found by name: $name")
    }

    fun addRegexpInfo(ri: RegexpInfo) {
        regexpInfos[ri.name] = ri
    }

    protected fun clearRegexpInfos() {
        regexpInfos = LinkedHashMap<String, RegexpInfo>()
    }

    /**
     * Override this: Return the pattern handler named in the .properties file.
     */
    protected abstract fun getPatternHandlerByName(name: String): PatternHandler?

    /**
     * Default behavior: Register all listeners.
     */
    @Throws(TexyException::class)
    fun onRegister() {
        // Initialize.
        // When it was in a constructor, there was an "unreported exception in constructor".
        log.finer("Intializing module " + this.javaClass.name + "...")
        init()


        // -- Add all module's patterns in the order it gave them. TBD: Move to onRegister(). -- //
        log.finest("  " + regexpInfos.size + " regexpInfos.")

        //for( RegexpInfo ri : module.getRegexpInfos(). ){
        for ((_, value) in regexpInfos) {
            texy.addPattern(value)
        }


        // -- Register all listeners. -- //
        val listeners = eventListeners
        log.finer("Registering " + listeners.size + " listeners for " + this.javaClass.simpleName + ".")
        for (lis in listeners) {
            log.finer("  " + lis.eventClass.name)
            if (lis is AroundEventListener<AroundEvent>) texy.aroundHandlers.addHandler(lis)
            else texy.normalHandlers.addHandler(lis)
        }
        // PatternHandlers / RegexpInfos are registered in JTexy#registerModule().
    } // onRegister()
    /**
     * Constructor - initializes the module (calls init()).
     */
    /*public TexyModule() throws TexyException {
        this.init();
	}*/
    /**
     * Like init( String propertiesFilePath ),
     * only the default path is
     * "#/cz/dynawest/jtexy/modules/" + {ModuleClassName} + "Patterns.properties".
     */
    @Throws(TexyException::class)
    protected fun init() {
        if (null != propsFilePath) loadRegexFromPropertiesFile(propsFilePath)
    }

    /**
     * Init - loads regular expressions and their metainfo
     * like mode of processing, bound html element etc.
     *
     * Synchronized due to clearRegexpInfos() and addRegexpInfo().
     */
    @Synchronized
    @Throws(TexyException::class)
    protected open fun loadRegexFromPropertiesFile(propsFilePath: String?) {
        try {
            // Reset regex infos.
            clearRegexpInfos()

            // Map for lookups, List to keep the order.
            val reMap: MutableMap<String, RegexpInfo> = LinkedHashMap()


            // Load properties file (and get default values etc).
            val props = PropertiesLoader.loadProperties(propsFilePath, this.javaClass)
            var defaultReType = RegexpInfo.Type.LINE

            // First check for "global" settings.
            val typeVal = props.getProperty("default.type")
            if (typeVal != null) {
                defaultReType = getReTypeByName(typeVal, "default.type")
            }
            val initErrors: MutableList<TexyException> = ArrayList()

            // For each key in properties file...
            for (key in props.keys) {
                processProperty(key, props, defaultReType, reMap, initErrors)
            }

            TexyException.throwIfErrors(
                "Errors when initializing module: " + this.javaClass.name, initErrors
            )


            // Fill up the module's RE map/list.
            log.finer("Patterns added for '" + this.javaClass.simpleName + "':" + reMap.size)
            val defaultHandler = getPatternHandlerByName("default")
            for (ri in reMap.values) {
                // Default .htmlelement is the part of pattern name after '/' .
                // I.e.  phrase/sub =>  default element is "sub".
                if (StringUtils.isEmpty(ri.htmlElement)) ri.htmlElement = StringUtils.substringAfterLast(
                    ri.name, "/"
                )

                // If no handler yet, bind the default one.
                if (null == ri.handler) ri.handler = defaultHandler
                addRegexpInfo(ri)
                log.finest("    Pattern " + ri.name + ": " + ri.type + " " + ri.perlRegexp + " => " + ri.regexp)
            }
        } catch (ex: IOException) {
            throw TexyException(ex)
        } catch (ex: TexyException) {
            throw TexyException("Error when processing $propsFilePath:\n${ex.message}")
        }
    }

    /**
     *
     */
    @Throws(TexyException::class)
    private fun processProperty(
        key: Any, props: Properties?, defaultReType: RegexpInfo.Type,
        reMap: MutableMap<String, RegexpInfo>, initErrors: MutableList<TexyException>
    ) {
        val propName = key as String
        var name = propName
        val value = props!!.getProperty(propName)
        var metaName: String? = null

        // When using java.util.Properties, props are not processed
        // in order of appearance in the .properties file (Map).
        if (StringUtils.startsWith(propName, "default")) return
        if (StringUtils.startsWith(propName, "_")) return

        // abc = xy      If the property name does not contain a dot, it's a name of a pattern.
        // abc.ef = 15   If it contains a dot, it's a sub-property of some pattern.
        val dotPos = propName.indexOf('.')
        if (dotPos != -1) {
            name = propName.substring(0, dotPos)
            metaName = propName.substring(dotPos + 1)
        }

        // Get or create the temporary regexp info.
        var ri = reMap[name]
        if (null == ri) {
            // TODO: How do we know whether it's LINE or BLOCK?  Need new property in the file.
            ri = RegexpInfo(name, defaultReType)
            reMap[name] = ri
        }

        // Handle the value.
        if (dotPos == -1) {
            // Regular expression of this pattern.
            try {
                ri.parseRegexp(value)
            } catch (ex: TexyException) {
                throw TexyException("Error parsing pattern '$name'.", ex)
            }
        } else {
            // Pattern metadata.
            if ("handler" == metaName) {
                ri.handler = getPatternHandlerByName(value)
                if (ri.handler == null) initErrors.add(TexyException("Unknown handler for pattern '$name': $value"))
            } else if ("type" == metaName) {
                ri.type = getReTypeByName(value, name)
                //if( ri.type == null )
                //	initErrors.add( new TexyException("Unknown type of pattern '"+name+"': "+metaName) );
            } else if ("htmlelement" == metaName) {
                ri.htmlElement = value
            } else {
                log.warning("Unknown regexp metadata: $metaName")
            }
        }
    } // processProperty

    /** Not much clean code, but I'm lazy to introduce some parser context class for this.  */
    @Throws(TexyException::class)
    private fun getReTypeByName(typeName: String, metaName: String): RegexpInfo.Type {
        return try {
            RegexpInfo.Type.valueOf(typeName.uppercase(Locale.getDefault()))
        } catch (ex: IllegalArgumentException) {
            throw TexyException("Pattern type must be LINE or BLOCK, was '$typeName' for '$metaName'.")
        }
    }

    companion object {
        private val log = Logger.getLogger(TexyModule::class.java.name)
        private const val DEFAULT_PROPS_PATH = "#/cz/dynawest/jtexy/modules/%sPatterns.properties"
    }
}
