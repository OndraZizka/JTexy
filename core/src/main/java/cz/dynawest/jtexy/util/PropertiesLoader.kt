package cz.dynawest.jtexy.util

import java.io.*
import java.util.*
import java.util.logging.*

/**
 * Helper functions to load Properties.
 *
 * @author Ondrej Zizka
 */
object PropertiesLoader {
    private val log = Logger.getLogger(PropertiesLoader::class.java.name)
    /** Loads properties from given class'es classloader.  */
    /** Loads properties.  */
    @JvmOverloads
    @Throws(IOException::class)
    fun loadProperties(propsPath: String?, clazz: Class<*> = PropertiesLoader::class.java): Properties {
        return loadPropertiesOrdered(propsPath, clazz)
    }

    /** Loads properties. Counts the propsPath from the class'es path.  */
    @Throws(IOException::class)
    fun loadPropertiesUnordered(propsPath: String?, clazz: Class<*>): Properties {
        val `is` = getInputStream(propsPath, clazz)
        val props = Properties()
        props.load(`is`)
        return props
    }

    /**
     * Get the input stream from the props file on the given path.
     * If the propsPath begins with #, then it's loaded via clazz's classloader.
     * Otherwise it's read from the filesystem from the current path.
     */
    @Throws(IOException::class)
    private fun getInputStream(propsPath: String?, clazz: Class<*>): InputStream {
        //ClassLoader.getSystemResource( propsPath ).openStream() );
        val `is`: InputStream?
        `is` = if (propsPath!!.startsWith("#")) {
            clazz.getResourceAsStream(propsPath.substring(1))
        } // "Use getClass().getClassLoader().findResource("path") instead."
        else {
            FileInputStream(propsPath)
        }
        if (`is` == null) {
            throw IOException("Properties file not found: " + propsPath + "  For class: " + clazz.name)
        }
        return `is`
    }

    /**
     * Loads properties file into an ordered map.
     */
    @Throws(IOException::class)
    private fun loadPropertiesOrdered(propsPath: String?, clazz: Class<*>): Properties {
        val `is` = getInputStream(propsPath, clazz)
        val osp = OrderedProperties()
        osp.load(`is`)
        return osp
    }
}
