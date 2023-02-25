
package cz.dynawest.jtexy.parsers;

import cz.dynawest.jtexy.JTexy;
import cz.dynawest.jtexy.RegexpInfo;
import cz.dynawest.jtexy.TexyException;
import cz.dynawest.jtexy.dom4j.io.ProtectedHTMLWriter;
import cz.dynawest.jtexy.util.MatchWithOffset;
import cz.dynawest.openjdkregex.Matcher;
import cz.dynawest.openjdkregex.Pattern;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Node;
import org.dom4j.dom.DOMElement;
import org.dom4j.dom.DOMText;

/**
 *  Parser which parses the content of the blocks, using LINE patterns.
 *
 * @author Ondrej Zizka
 */
public class TexyLineParser extends TexyParser
{
    private static final Logger log = Logger.getLogger( TexyLineParser.class.getName() );

    public boolean again;


    public TexyLineParser(JTexy texy, DOMElement element) {
        super(texy, element);
    }


    @Override
    protected List<RegexpInfo> getPatterns() {
        return this.getTexy().getLinePatterns();
    }



	/**
	 * Tries to match all LINE patterns against the text.
	 * The one which starts at the earliest position is applied -
	 * it's handler is called, and it's match is replaced with
	 * the response of the handler (either DOMText, DOMElement or null).
	 */
	@Override public void parse( String text ) throws TexyException
	{
        final boolean finest = log.isLoggable( Level.FINEST );

        if( this.getPatterns().isEmpty() ){
            // Nothing to do.
            ((DOMElement)this.element).add( new DOMText(text) );
            return;
        }

        int offset = 0;

        Map<RegexpInfo, ParserMatchInfo> allMatches = new HashMap( this.getPatterns().size() );

        // Store current offset for each line pattern.
        Map<RegexpInfo, Integer> patternOffsets = new HashMap( this.getPatterns().size() );
        for( RegexpInfo ri : this.getPatterns() ){
            patternOffsets.put( ri, -1 );
        }


        // Parse loop.
        do{
            RegexpInfo minPattern = null;
            int minOffset = text.length();


            // For each line pattern...
            for( RegexpInfo ri : this.getPatterns() )
            {
				if(finest) log.log(Level.FINEST, "  Parsing with pattern {0} - {1}...", new Object[]{ri.name, ri.getRegexp()});

                if( patternOffsets.get(ri) < offset )
                {
                    int delta  =  (patternOffsets.get(ri) == -2) ? 1 : 0;

                    // Regexp match
                    //Pattern pat = Pattern.compile( ri.getRegexp() );
                    Pattern pat = ri.getPattern();
                    Matcher mat = pat.matcher( text );
                    if( mat.find( offset + delta ) ){
                        // Store match info.
                        List<MatchWithOffset> groups = MatchWithOffset.fromMatcherState(mat);
                        ParserMatchInfo curMatchInfo = new ParserMatchInfo(ri, groups, mat.start());
                        allMatches.put(ri, curMatchInfo);
                        if( groups.get(0).match.length() == 0 )
                            continue;
                        // Store offset for this pattern.
                        patternOffsets.put(ri, mat.start());
                    }
                    else{
                        // Try next time.
                        continue;
                    }
                }// if( patternOffsets.get(ri) < offset )

                int curOffset = patternOffsets.get(ri);
                if( curOffset < minOffset ){
                    minOffset = curOffset;
                    minPattern = ri;
                }
            }// for each line pattern ( RegexpInfo ri : this.getPatterns() )


            // Nothing matched?
            if( minPattern == null ){
                if(finest) log.finest("No more matches.");
                break;
            }



            int start = offset = minOffset;

            // Call the handler of the minimal match.
            this.again = false;
            log.log(Level.FINER, "Using handler: {0}", minPattern.handler.getName());
            Node resNode = minPattern.handler.handle( this, allMatches.get(minPattern).groups, minPattern );

            String resString = "Invalid response from handler of "+minPattern.name+": "+resNode;

            if( resNode instanceof DOMElement ){
                //resString = resNode.asXML();
                resString = ProtectedHTMLWriter.fromElement( (DOMElement) resNode, getTexy().getProtector() );
            }
            else if( null == resNode ){
                // Store that this was rejected.
                patternOffsets.put( minPattern, -2 );
                continue;
            }
            else if( resNode instanceof DOMText ){
                resString = ((DOMText)resNode).getText();
            }
            if(finest) log.log(Level.FINEST, "Result: {0}", resNode);
            if(finest) log.log(Level.FINEST, "Result string: {0}", resString);


            int matchStart = minOffset; //patternOffsets.get(minPattern);
            int matchLen = allMatches.get(minPattern).groups.get(0).match.length();
            // Replace the matched part of the text with the result.
            text = StringUtils.overlay(text, resString, start, matchStart + matchLen );

            int delta = resString.length() - matchLen;

            // Adjust all patterns' offset.
            for( Entry<RegexpInfo, Integer> entry : patternOffsets.entrySet() ) {
                // If this pattern's offset is before the left-most match, reset it back to start.
                if( entry.getValue() < matchStart + matchLen ){ // TODO: add match end to MatchWithOffset.
                    entry.setValue( -1 );
                }
                // Otherwise, set it after the matched region.
                else {
                    entry.setValue( entry.getValue() + delta );
                }
            }

            if( this.again ){
                patternOffsets.put( minPattern, -2 );
            }
            else {
                patternOffsets.put( minPattern, -1 );
                offset += resString.length();
            }

            // TODO


		}while( true );

        log.log(Level.FINE, "Resulting string after parsing: {0}", text);

        // $this->element->insert(NULL, $text);
        //((DOMElement)this.element).addCDATA( text );
        this.element.addText( text );
		

	}// parse()

	


}



