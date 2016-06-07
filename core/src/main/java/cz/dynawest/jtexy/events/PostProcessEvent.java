
package cz.dynawest.jtexy.events;

import cz.dynawest.jtexy.JTexy;

/**
 *  At the end of processing; currently used by HtmlOutputModule.
 *
 *  @author Ondrej Zizka
 */
public class PostProcessEvent extends TexyEvent {
    
    public PostProcessEvent(JTexy texy, String text) {
        super(text);
    }
    
}
