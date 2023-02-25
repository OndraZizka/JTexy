
package cz.dynawest.jtexy;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 *
 * @author Ondrej Zizka
 */
public class Options {


    // -- Hard-coded options -- //

    public boolean removeSoftHyphens = false;
    public int tabWidth = 4;
    public boolean mergeLines = true;
    public String nontextParagraph = "div";
    public boolean makeAutoLinksShorter = true;
    public boolean obfuscateEmails = true;
    public boolean useLinkDefinitions = true;
    public String linkRootUrl = "";
    public String imageRootUrl = "";
    public String imageRootDir = "";
    public boolean forceNofollow = false;
    public String popupOnclick = "";



    // -- Set-event listeners -- //

    private Set<SetPropertyListener> setListeners = new LinkedHashSet();

    public void addSetPropertyListener( SetPropertyListener lis ){
        this.setListeners.add(lis);
    }

    public int setProperty( String name, Object value ){
        int accepted = 0;
        this.propsHistory.add( new SetPropertyEvent(name, value) );
        for( SetPropertyListener lis : this.setListeners ){
            if( lis.onSetProperty( name, value ) )
                accepted++;
        }
        return accepted;
    }


    // Set-event listener interface.
    public static interface SetPropertyListener {
        /** Returns true if this listener accepted the property (processed it somehow). */
        public boolean onSetProperty( String name, Object value );
    }

    // Set-event.
    public static class SetPropertyEvent {
        String name;
        Object value;

        public SetPropertyEvent(String name, Object value) {
            this.name = name;
            this.value = value;
        }
    }



    // -- Keep history of set properties for debug purposes. -- //

    private List<SetPropertyEvent> propsHistory = new LinkedList();

    public Object getPropertiesHistory(){ return Collections.unmodifiableList(propsHistory); }

    public Object getPropertyLastValue( String name ){
        if( name == null) throw new NullPointerException("Property name can't be null.");
        // Not likely to be used much, no need for hashmap.
        ListIterator<SetPropertyEvent> it = propsHistory.listIterator( propsHistory.size() );
        while( it.hasPrevious() ){
            SetPropertyEvent ev = it.previous();
            if( name.equals( ev.name ) )
                return ev.value;
        }
        return null;
    }



}
