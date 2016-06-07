
package cz.dynawest.jtexy.dtd;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * DTD descriptor.
 *   $dtd[element][0] - allowed attributes (as array keys)
 *   $dtd[element][1] - allowed content for an element (content model) (as array keys)
 *                    - array of allowed elements (as keys)
 *                    - FALSE - empty element
 *                    - 0 - special case for ins & del
 * 
 * @author Ondrej Zizka
 * 
 * @ It would be great to work directly with DTD, if there was an library...
 */
public class DtdElement {
    

    private final String name;
    
    private final Map<String, DtdElement> elements = new HashMap();
    
    private final Set<DtdAttr> attrs = new HashSet();
    
    private Dtd dtd;

    
    /**
     *  Does not set DTD. Use for cases like set.contains( new DtdElement("foo") );
     */
    public DtdElement(String name) {
        this.name = name;
    }
    
    
    
    
    public DtdElement getElement(String name) {
        return elements.get(name);
    }
    
    
    public DtdElement add( DtdElement e ){
        this.elements.put(e.name, e);
        return this;
    }
    
    public DtdElement add( DtdAttr a ){
        this.attrs.add(a);
        return this;
    }
    
    public DtdElement addAll( Collection<DtdElement> c ){
        for (DtdElement dtdElement : c) {
            add(dtdElement);
        }
        return this;
    }
    
    public DtdElement addAllAttrs( Collection<DtdAttr> c ){
        this.attrs.addAll(c);
        return this;
    }
    
    public DtdElement addAll( String nameStr ){
        for (DtdElement dtdElement : dtd.getOrCreateElements(nameStr) ) {
            add(dtdElement);
        }
        return this;
    }
    
    public DtdElement removeAll( String nameStr ){
        for( String name : nameStr.split(" ") ) {
            this.elements.remove(name);
        }
        return this;
    }
    
    public DtdElement addAllAttrs( String nameStr ){
        this.attrs.addAll( Dtd.createAttributes(nameStr)); return this;
    }
    public DtdElement addAllAttrsIf( String nameStr, boolean cond ){
        if( cond )
            this.attrs.addAll( Dtd.createAttributes(nameStr));
        return this;
    }
    
    
    public boolean hasChildren(){
        return !this.elements.isEmpty();
    }
    public boolean hasNoChildren(){
        return this.elements.isEmpty();
    }

    
    
    
    //<editor-fold defaultstate="collapsed" desc="get/set">
    public String getName() {
        return name;
    }
    
    public Set<DtdElement> getElements() {
        return new HashSet(elements.values());
    }
    
    public Set<DtdAttr> getAttrs() {
        return attrs;
    }

    void setDtd(Dtd dtd) { this.dtd = dtd; }
    public Dtd getDtd() { return dtd; }
    //</editor-fold>

    
    //<editor-fold defaultstate="collapsed" desc="equals/hash">
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DtdElement other = (DtdElement) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override public String toString() {
        return "DtdElement{ " + name + ", " + elements.size() + " elms, " + attrs.size() + "attrs }";
    }
    //</editor-fold>

    
    
    
    
    // Unused. TODO - good for anything?
    private static final Set<DtdElement> ALL = new Set<DtdElement>() {
        @Override public int size() {
            throw new UnsupportedOperationException("ALL constant only supports contains().");
        }

        @Override public boolean isEmpty() { return false; }
        @Override public boolean contains(Object o) { return o instanceof DtdElement; }
        @Override public Iterator<DtdElement> iterator() { throw co(); }
        @Override public Object[] toArray() { throw co(); }
        @Override public <T> T[] toArray(T[] a) { throw co(); }
        @Override public boolean add(DtdElement e) { throw ro(); }
        @Override public boolean remove(Object o) { throw ro(); }
        @Override public boolean containsAll(Collection<?> c) { return true; }
        @Override public boolean addAll(Collection<? extends DtdElement> c) { throw ro(); }
        @Override public boolean retainAll(Collection<?> c) { throw ro(); }
        @Override public boolean removeAll(Collection<?> c) { throw ro(); }
        @Override public void clear() { ro(); }
        
        private final UnsupportedOperationException ro(){
            return new UnsupportedOperationException("Read only.");
        }
        
        private final UnsupportedOperationException co(){
            return new UnsupportedOperationException("ALL constant only supports contains().");
        }
        
    };// ALL
    
}// class
