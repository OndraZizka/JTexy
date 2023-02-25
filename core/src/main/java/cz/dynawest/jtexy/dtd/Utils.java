
package cz.dynawest.jtexy.dtd;

import java.util.List;

/**
 * DTD util methods.
 * 
 * @author Ondrej Zizka
 * @deprecated  Moved to Dtd.
 */
public class Utils {

    private static List<DtdElement> createElements( Dtd dtd, String nameStr ){
        return dtd.getOrCreateElements(nameStr);
    }
    
}
