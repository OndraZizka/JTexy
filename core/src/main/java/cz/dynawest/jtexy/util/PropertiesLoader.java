package cz.dynawest.jtexy.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Helper functions to load Properties.
 * 
 * @author Ondrej Zizka
 */
public class PropertiesLoader {

    private static final Logger log = Logger.getLogger(PropertiesLoader.class.getName());
    

    /** Loads properties. */
    public static Properties loadProperties(String propsPath) throws IOException {
        return loadProperties(propsPath, PropertiesLoader.class);
    }

    /** Loads properties from given class'es classloader. */
    public static Properties loadProperties(String propsPath, Class clazz) throws IOException {
        return loadPropertiesOrdered(propsPath, clazz);
    }

    /** Loads properties. Counts the propsPath from the class'es path. */
    public static Properties loadPropertiesUnordered(String propsPath, Class clazz) throws IOException {

        InputStream is = getInputStream(propsPath, clazz);

        Properties props = new Properties();
        props.load(is);
        return props;
    }

    /**
     *  Get the input stream from the props file on the given path.
     *  If the propsPath begins with #, then it's loaded via clazz's classloader.
     *  Otherwise it's read from the filesystem from the current path.
     */
    private static InputStream getInputStream(String propsPath, Class clazz) throws IOException {
        //ClassLoader.getSystemResource( propsPath ).openStream() );

        InputStream is;
        if (propsPath.startsWith("#")) {
            is = clazz.getResourceAsStream(propsPath.substring(1));
        } // "Use getClass().getClassLoader().findResource("path") instead."
        else {
            is = new FileInputStream(propsPath);
        }

        if (is == null) {
            throw new IOException("Properties file not found: " + propsPath + "  For class: " + clazz.getName());
        }
        return is;
    }

    /**
     *  Loads properties file into an ordered map.
     */
    private static Properties loadPropertiesOrdered(String propsPath, Class clazz) throws IOException {
        InputStream is = getInputStream(propsPath, clazz);

        OrderedProperties osp = new OrderedProperties();
        osp.load(is);

        return osp;
    }
}