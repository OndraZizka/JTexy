
package cz.dynawest.jtexy.util;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import junit.framework.TestCase;

/**
 *
 * @author Ondrej Zizka
 */
public class OrderedMapBackedPropertiesTest extends TestCase {

	public void testIt() throws IOException{
		
		Properties props = PropertiesLoader.loadProperties("#/cz/dynawest/jtexy/modules/PhraseModulePatterns.properties");

		Enumeration keys = props.propertyNames();
		while( keys.hasMoreElements() ){
			Object key = keys.nextElement();
			String value = props.getProperty((String) key);
			System.out.println(""+key+": "+value);
		}



	}

}// class
