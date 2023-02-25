
package cz.dynawest.jtexy.dtd;

/**
 *  Attribute DTD. Simply a name.
 * 
 *  @author Ondrej Zizka
 */
public final class DtdAttr {

    private final String name;

    public DtdAttr(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
}
