package cz.dynawest.jtexy.dtd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

/**
 *  Simplified DTD. Contains map of elements String -> DtdElement, and a root element.
 * 
 *  @author Ondrej Zizka.
 */
public class Dtd {
    
    private final DtdElement root;
    private final Map<String, DtdElement> elementsMap = new HashMap();

    /** @see http://www.w3.org/TR/xhtml1/prohibitions.html */
    public Map<DtdElement, Set<DtdElement>> prohibits = new HashMap();
    
    
    public Dtd( String name ) {
        this.root = new DtdElement(name);
    }
    
    
    public Dtd put( DtdElement elm ){
        this.elementsMap.put(elm.getName(), elm);
        return this;
    }
    
    public DtdElement get( String name ){
        return this.elementsMap.get(name);
    }
    
    public boolean contains( String name ){ return this.elementsMap.containsKey(name); }
    
    public DtdElement getOrCreate( String name ){
        
        // Exists?
        DtdElement elm = this.elementsMap.get(name);
        if( null != elm )
            return elm;
        
        // New
        elm = new DtdElement(name);
        elm.setDtd( this );
        this.elementsMap.put(name, elm);
        return elm;
    }

    DtdElement getRootElement() {
        return root;
    }

    /**
     *  See e.g. @see http://www.w3.org/TR/xhtml1/prohibitions.html
     */
    void prohibit(String parent, String childrenStr) {
        this.prohibits.put( this.get(parent), new HashSet(this.getOrCreateElements(childrenStr)));
    }

    /**
     *  @returns  true if given child is prohibited in given parent.
     */
    public boolean isProhibited( DtdElement parent, DtdElement child ){
        
        Set<DtdElement> probibitedElms = this.prohibits.get( parent );
        if( probibitedElms == null )  return true;
        return probibitedElms.contains( child );
    }
    
    
    /**
     * @returns  List of prohibitions, or null.
     */
    public Set<DtdElement> getProbibitionsOf( DtdElement parent ){
        Set<DtdElement> prohibs = this.prohibits.get(parent);
        //if( prohibs == null )  return Collections.EMPTY_SET;
        return prohibs;
    }
    
    
    /**
     *  Convenience.
     * @returns  list of elements of names given in nameStr bound to this DTD.
     */
    public List<DtdElement> getOrCreateElements(String nameStr) {
        String[] names = StringUtils.split(nameStr, " ");
        
        List<DtdElement> list = new ArrayList(names.length);
        for( String name : names ){
            list.add( this.getOrCreate(name) );
        }
        return list;
    }

    
    /**
     *  Convenience.
     * @returns  list of attributes of names given in nameStr.
     */
    public static List<DtdAttr> createAttributes( String nameStr ){
        String[] names = StringUtils.split(nameStr, " ");
        
        List<DtdAttr> list = new ArrayList(names.length);
        for( String name : names ){
            list.add( new DtdAttr(name) );
        }
        return list;
    }
    
}
