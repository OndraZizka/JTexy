
package cz.dynawest.jtexy.events;

/**
 * Root event class.
 * 
 * @author Ondrej Zizka
 */
public class TexyEvent {
    
	protected String text;
	public final String getText() {		return text;	}
	public final void setText(String text) {		this.text = text;	}

    public TexyEvent(String text) {
        this.text = text;
    }

}
