
package cz.dynawest.jtexy.modules;

import cz.dynawest.jtexy.parsers.BeforeAfterEvent;
import cz.dynawest.jtexy.parsers.TexyParser;

/**
 *  Used for TypographyModule, LongWordsModule.
 * 
 *  TODO: extends TexyEvent, not BeforeAfterEvent. It's not a parsing event.
 *  TODO: Rename to PostLineEvent, afterall.
 * 
 *  @author Ondrej Zizka
 */
public class AfterLineEvent extends BeforeAfterEvent {

    public AfterLineEvent(TexyParser parser, String text) {
        super(parser, text);
    }


}// class
