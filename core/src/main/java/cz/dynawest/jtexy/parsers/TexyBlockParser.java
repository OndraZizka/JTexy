
package cz.dynawest.jtexy.parsers;

import cz.dynawest.jtexy.JTexy;
import cz.dynawest.jtexy.RegexpInfo;
import cz.dynawest.jtexy.RegexpPatterns;
import cz.dynawest.jtexy.TexyException;
import cz.dynawest.jtexy.modules.BeforeBlockEvent;
import cz.dynawest.jtexy.modules.ParagraphEvent;
import cz.dynawest.jtexy.modules.TexyModifier;
import cz.dynawest.jtexy.util.MatchWithOffset;
import cz.dynawest.openjdkregex.Matcher;
import cz.dynawest.openjdkregex.Pattern;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Node;
import org.dom4j.dom.DOMElement;
import org.dom4j.dom.DOMText;

/**
 *
 * @author Ondrej Zizka
 */
public class TexyBlockParser extends TexyParser
{
    private static final Logger log = Logger.getLogger( TexyBlockParser.class.getName() );


    private String text = null;

    private int offset = 0;



    /** Const */
    public TexyBlockParser( JTexy texy, DOMElement element, boolean indented)
    {
        super( texy, element );
        this.indented = indented;
    }



    @Override
    protected List<RegexpInfo> getPatterns() {
        return this.getTexy().getBlockPatterns();
    }



    /**
     * Match current line against the given RE.
     * If successful, increments current position to the end of the match.
     * @returns the list of matches.
     */
    public List<MatchWithOffset> next( String pattern ){
        //pattern = "^"+pattern; // Instead of $pattern . 'A' - anchored
        return this.next( Pattern.compile( pattern, Pattern.MULTILINE ) );
    }

    /**
     * Match current line against the given Pattern.
     * If successful, increments current position to the end of the match.
     * @returns the list of matches.
     */
    public List<MatchWithOffset> next( Pattern pattern )
    {
        if( this.offset >= this.text.length() ){
            if( this.offset != this.text.length() )
                log.warning("Offset overflow! Length: "+this.text.length()+" Off: "+this.offset);
            return null;
        }

        /*
        Matcher mat = pattern.matcher( this.text );
        if( ! mat.find( this.offset ) )
            return null;
         */

        // Instead of (?A) - anchored. Otherwise, "^Ahoj" also matched "\n\n|Ahoj".
        String substr = this.text.substring( this.offset );
        Matcher mat = pattern.matcher( substr );
        if( ! mat.find() )
            return null;
        /**/


        //TODO: Replace this.offset with substr througout this method.
        //      Or, is \A enough?  No, it's not.
        // DONE.


        // !! Added to this.offset because the regex was applied on a substring!
        this.offset = this.offset + mat.end() + 1;  // 1 = "\n"

        // Get the groups of the match.
        List<MatchWithOffset> matches = new ArrayList( mat.groupCount()+1 );
        for( int i = 0; i < mat.groupCount()+1; i++ ) {
            matches.add( new MatchWithOffset( mat.group(i), this.offset + mat.start(i) ));
        }
        return matches;

    }// next()





    /** Moves the current position after the previous newline. */
    public void moveBackward(){ moveBackward(1); }

    /**
     * Moves the current position after the previous newline.
     *
     * If at the beginning of a line,
     *  moves the current position of this parser to the beginning of the previous line.
     * If in the middle of a line,
     *  moves at the beginning of this line.
     */
    public void moveBackward( int linesCount ){
        // We're at the start of a line - skip before the last \n.
        if( this.offset != 0 )
            this.offset--;

        do {
            this.offset = this.text.lastIndexOf('\n', this.offset - 1);
            //if( this.offset == -1 ){ this.offset = 0; break; }
            this.offset++; // Go after the \n; also handles the -1 case.
            linesCount--;
            if( linesCount < 1 )  break;
        }
        while ( true );
    }

    /** @deprecated  TODO: Dump. */
    public void moveBackwardUgly( int linesCount )
    {
        char[] chars = new char[this.offset];
        this.text.getChars(0, this.offset, chars, 0);

        while( --this.offset > 0 ){
            //if( '\n' == this.text.charAt( this.offset - 1 ) ){
            if( '\n' == chars[ this.offset - 1 ] ){
                linesCount--;
                if( linesCount < 1)  break;
            }
        }
        this.offset = Math.max( this.offset, 0 );
    }



    /**
     * Parses the given text.
     * 1) Fires a BeforeBlockEvent.
     * 2) Calls {@link #parseLoop(java.lang.String, java.util.List)} .
     * 3) Calls {@link #processLoop(java.lang.String, java.util.List)} .
     */
    @Override
    public void parse( String text ) throws TexyException {

        log.fine("===================================================================");
        log.fine("Parsing: " + StringUtils.abbreviate(text, 45).replace("\n", "\\n") + " ("+text.length()+")");


        // BeforeBlockEventListener's can modify the text before parsing.
        BeforeBlockEvent ev = new BeforeBlockEvent(this, text);
        this.getTexy().invokeNormalHandlers( ev );
        text = ev.getText();

        // --- Parse loop. --- //
        List<ParserMatchInfo> allMatches = parseLoop( text, this.getPatterns() );

        // TODO: Handle the case with no matches.

        // --- Process Loop. ---
        processLoop( text, allMatches );

    }// parse( String text )



    /**
     *  Scans the text for the matches of the given list of patterns.
     *  @returns a list of matches, ordered by the offset of the start of the match.
     * // TBD: Rename MatchWithOffset to MatchData or similar?
     */
    private static List<ParserMatchInfo> parseLoop( String text, List<RegexpInfo> patterns ){
        final boolean finer = JTexy.debug && log.isLoggable(Level.FINER);
        final boolean finest = JTexy.debug && log.isLoggable(Level.FINEST);

        // $matches[] = array($offset, $name, $m, $priority);
        List<ParserMatchInfo> allMatches = new ArrayList();
        int priority = 0;


        /*  For each pattern... */
        for( RegexpInfo pattern : patterns )
        {
            if(finest) log.finest("Applying pattern: "+pattern.name);

            Pattern pat = pattern.getPattern();
            Matcher mat = pat.matcher( text );

            // All matches of this pattern throughout the text.
            List<List<MatchWithOffset>> matches = MatchWithOffset.fromMatcherAll(mat);

            // For each match, store its start offset and all match groups.
            for( List<MatchWithOffset> groups : matches ){
                allMatches.add( new ParserMatchInfo( pattern, groups, groups.get(0).offset, priority ) );
            }
            priority++;
        }// for each patterns;

        // Sort the matches by offset, then priority.
        Collections.sort(allMatches);


        log.fine("Matches: " + allMatches.size());
        if( finer )
            for( ParserMatchInfo pmi : allMatches )
                log.finer("    Match: " + pmi.offset +": "+pmi.groups );

        return allMatches;
    }



    /**
     * Processes the text using all given matches -
     * passes matches to their handlers and replaces 
     * the matched part of the text with the handler's result.
     * Also processes text between matches.
     */
    private void processLoop( String text, List<ParserMatchInfo> allMatches ) throws TexyException {

        // Terminal cap.
        ParserMatchInfo pmi = new ParserMatchInfo(null, null, text.length());
        allMatches.add(pmi);

        ListIterator<ParserMatchInfo> it = allMatches.listIterator();
        this.offset = 0;
        this.text = text;

        do{
            // Set the initial match info to the end of the whole content to handle the case with no matches.
            // Consider: Perhaps this should be appended to the PMI list?

            while( it.hasNext() ){
                pmi = it.next();
                if( pmi.offset >= this.offset )
                    break;
            }

            // Between-matches content.
            if( pmi.offset > this.offset ){
                String textBetween = text.substring( this.offset,  pmi.offset ).trim();
                if( 0 != textBetween.length() ){
                    processTextBetweenMatches( textBetween );
                }
            }

            // Terminal cap - Finito.
            if( null == pmi.groups )
                break;

            // Advance to the place after the matched string (block).
            this.offset = pmi.offset + pmi.groups.get(0).match.length() + 1; // 1 = \n

            // Call the handler.
            Node result = pmi.pattern.handler.handle(this, pmi.groups, pmi.pattern);

            // this.offset <= pmi.offset should never happen, see this.offset = ... above
            if( null == result || this.offset <= pmi.offset ){
                // Prevent generic block split.
                this.offset = pmi.offset; // Set the offset back.
                continue;
            }
            else if( result instanceof DOMElement ){
                ((DOMElement)this.element).add(result);
            }
            else if( result instanceof DOMText ){
                ((DOMElement)this.element).add(result);
            }

        }while( true );
    }



    /**
     *  Processes text between block patterns matches
     * (line patterns are used inside this and inside block handlers).
     *  Usually this is a text spanned over multiple paragraphs.
     *  Separates the text into paragraphs and fires a ParagraphEvent for each.
     */
    void processTextBetweenMatches( String textBetween ) throws TexyException {

        String[] parts;
        // Split paragraphs - by 2 or more lines.
        if( this.isIndented() ){
            // (?! )  ==  zero-width negative lookahead  ==  no spaces after a newline.
            //$parts = preg_split('#(\n(?! )|\n{2,})#', $content, -1, PREG_SPLIT_NO_EMPTY);
            parts = textBetween.split("(\\n(?! )|\\n{2,})");
        } else {
            //$parts = preg_split('#(\n{2,})#', $content, -1, PREG_SPLIT_NO_EMPTY);
            parts = textBetween.split( "(\\n{2,})" );
        }

        // For each paragraph...
        for (int i = 0; i < parts.length; i++) {
            String parText = parts[i];

            parText = parText.trim();
            if( 0 == parText.length() )
                continue;


            // Try to find a modifier.
            TexyModifier modifier = null;

            Pattern pat = PAR_MODIFIER_PATTERN;
            Matcher mat = pat.matcher( parText );
            if( mat.matches() ) {
                // list(, $mC1, $mMod, $mC2) = $mx;
                String mC1 = mat.group(1);
                String mMod = mat.group(2);
                String mC2 = StringUtils.defaultString( mat.group(3) );

                parText = (mC1 + mC2).trim();
                if( "".equals(parText) ) continue;
                modifier = new TexyModifier( mMod );
            }

            // Fire a ParagraphEvent.
            ParagraphEvent ev = new ParagraphEvent( this, parText, modifier );
            try{
                Node parNode = getTexy().invokeAroundHandlers( ev );
                log.finer("ParagraphEvent invocation returned: "+parNode);
                //String res = ev.getText(); // TODO
                //if( null != res ) this.element.addCDATA(res);
                if( null != parNode )
                    this.element.add(parNode);
            }
            catch( TexyException ex ){
                throw new TexyException(
                    "Error when parsing paragraph starting with '"+
                    StringUtils.abbreviate(parText, 20) +"':\n    " + ex.toString(),  ex);
            }
        }
    }// processTextBetweenMatches()



    //preg_match('#\A(.*)(?<=\A|\S)'.TEXY_MODIFIER_H.'(\n.*)?()\z#sUm', $s, $mx);
    private static final String PAR_MOD_REGEX = "(?sUm)\\A(.*)(?<=\\A|\\S)"+RegexpPatterns.TEXY_MODIFIER_H+"(\\n.*)?()\\z";
    private static final Pattern PAR_MODIFIER_PATTERN = Pattern.compile(PAR_MOD_REGEX);



    /** Debug function - displays position info in the form of:  "...|..." */
    public String getPosAsString() {
        return getPosAsString(13);
    }

    /** Debug function - displays position info in the form of:  "...|..." */
    public String getPosAsString( int bounds ) {

        int min = Math.max( offset-bounds, 0 );
        int max = Math.min( offset+bounds, text.length() );
        if( min == max ) return "(empty text)";

        int delimPos = Math.max( offset, min ); // Lower bound.
        delimPos = Math.min( delimPos, max );   // Upper bound.

        String strBefore = this.text.substring(	min, delimPos );
        String strAfter = this.text.substring( delimPos, max );
        
        // Add ... around
        if( min != 0 )  strBefore = "…" + strBefore;

        return StringUtils.replaceEach(
                    strBefore + "◆" + strAfter,
                    new String[]{"\r","\n"}, new String[]{"\\r","\\n"}
        ) + "…";
    }


    /** @returns "@{offset}: "+getPosAsString(); */
    public String toString(){
        if( this.text == null )  return "[no text yet]";
        return "@"+this.offset+": "+getPosAsString();
    }

}// class
