package cz.dynawest.jtexy.modules;

import cz.dynawest.jtexy.JTexy;
import cz.dynawest.jtexy.RegexpInfo;
import cz.dynawest.jtexy.RegexpPatterns;
import cz.dynawest.jtexy.TexyException;
import cz.dynawest.jtexy.parsers.TexyBlockParser;
import cz.dynawest.jtexy.parsers.TexyLineParser;
import cz.dynawest.jtexy.parsers.TexyParser;
import cz.dynawest.jtexy.parsers.TexyEventListener;
import cz.dynawest.jtexy.util.MatchWithOffset;
import cz.dynawest.openjdkregex.Matcher;
import cz.dynawest.openjdkregex.Pattern;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.dom4j.Node;
import org.dom4j.dom.DOMElement;
import org.w3c.dom.NodeList;

/**
 *
 * @author Ondrej Zizka, Martin Večeřa
 */
public class ListModule extends TexyModule
{
    private static final Logger log = Logger.getLogger(ListModule.class.getName());

    private PatternHandler listPatternHandler = new ListPatternHandler();
    private PatternHandler listDefPatternHandler = new DefListPatternHandler();



    // -- Module meta-info -- //

    @Override public TexyEventListener[] getEventListeners() {
        return new TexyEventListener[]{};
    }

    @Override	protected PatternHandler getPatternHandlerByName(String name) {
        if( listPatternHandler.getName().equals(name) ) {
            return listPatternHandler;
        } else if( listDefPatternHandler.getName().equals(name) ) {
            return listDefPatternHandler;
        } else {
            return null;
        }
    }



    /**
     * Override init() - don't read the properties file.
     * Instead, create the regex infos manually from the ListType enum.
     */
    @Override
    protected void loadRegexFromPropertiesFile( String propertiesFilePath ) throws TexyException {

        // List.
        StringBuilder sb1 = new StringBuilder();
        sb1.append("#^(?:").append(RegexpPatterns.TEXY_MODIFIER_H).append("\\n)?");
        sb1.append("(");
        for( ListType bullet : ListType.values() ) {
            sb1.append(bullet.getFirstRegExp()).append("|");
        }
        sb1.deleteCharAt( sb1.length() - 1 );
        sb1.append(")\\ *\\S.*$#mUu");

        // List definition.
        StringBuilder sb2 = new StringBuilder();
        sb2.append("#^(?:").append(RegexpPatterns.TEXY_MODIFIER_H).append("\\n)?");
        sb2.append("(\\S.*)\\:\\ *").append(RegexpPatterns.TEXY_MODIFIER_H).append("?\\n"); // Term
        sb2.append("(\\ ++)(");
        for( ListType bullet : ListType.values() ) {
            if( !bullet.isOrdered() ) {
                sb2.append(bullet.getFirstRegExp()).append("|");
            }
        }
        sb2.deleteCharAt( sb2.length() - 1 );
        sb2.append(")\\ *\\S.*$#mUu");


        log.finer("List regex: "    + sb1.toString());
        log.finer("List def regex: "+ sb2.toString());

        // List.
        RegexpInfo listRI = new RegexpInfo( "list", RegexpInfo.Type.BLOCK );
        listRI.parseRegexp( sb1.toString() );
        listRI.handler = listPatternHandler;
        this.addRegexpInfo( listRI );

        // List definition.
        RegexpInfo listDefRI = new RegexpInfo( "list/definition", RegexpInfo.Type.BLOCK );
        listDefRI.parseRegexp( sb2.toString() );
        listDefRI.handler = listDefPatternHandler;
        this.addRegexpInfo( listDefRI );

    }// init()






    // -- Handlers and listeners -- //



    /**
     * List pattern handler.
     */
    private class ListPatternHandler implements PatternHandler {

        @Override public String getName() { return "list"; } // Not used - init() overriden.

        @Override
        public Node handle( TexyParser parser, List<MatchWithOffset> groups, RegexpInfo pattern ) throws TexyException {
            /*
             *  Advances in two steps:
             *   1. Reads the first line of the list, to get its type.
             *   2. Then goes back one line and reads the whole list.
             */

            //    [1] => .(title)[class]{style}<>
            //    [2] => bullet * + - 1) a) A) IV)
            if( log.isLoggable(Level.FINEST) ) {
                for( MatchWithOffset match : groups ) {
                    log.finest("  " + match.toString());
                }
            }

            String modStr    = groups.get(1).match;
            String bulletStr = groups.get(2).match;


            DOMElement elm = new DOMElement( (String) null );

            // Determine the list type used.
            ListType listType = null;
            for( ListType curListType : ListType.values() ) {
                Matcher mat = curListType.getPattern().matcher( bulletStr );
                if( mat.matches() ) {
                    listType = curListType;
                    break;
                }
            }
            if( listType == null ) {
                return null; // None chosen??
            }

            String itemBulletRegex = listType.getNextOrFirstRegExp(); // StringUtils.defaultString( listType.getNextRegExp(), listType.getFirstRegExp() );
            int min = listType.getNextRegExp() == null ? 1 : 2; // List must have at least <min> items.

            // Prepare the list element.
            {
                elm.setName( listType.isOrdered() ? "ol" : "ul" );
                if( null != listType.getListStyleType() )
                    elm.setAttribute( "style", "list-style-type: " + listType.getListStyleType() );

                // Ordered list.
                if( listType.isOrdered() ) {
                    char firstTypeBulChar = listType.getType().charAt(0);
                    // 1) or 1.
                    if( listType == ListType.ARABPAR || listType == ListType.ARABDOT ) {
                        int bulletNum = NumberUtils.toInt( StringUtils.chop(bulletStr.trim()) );
                        if( bulletNum > 1 ) {
                            elm.setAttribute( "start", "" + bulletNum );
                        }
                    } else {
                        char firstBulChar = bulletStr.charAt(0);
                        if( firstTypeBulChar == 'a' && firstBulChar > 'a' ) {
                            elm.setAttribute( "start", Character.toString((char) ('1' + firstBulChar - 'a')) );
                        } else if( firstTypeBulChar == 'A' && firstBulChar > 'A' ) {
                            elm.setAttribute( "start", Character.toString((char) ('1' + firstBulChar - 'A')) );
                        }
                    }
                }// <ol>
            }

            TexyModifier mod = new TexyModifier(modStr);
            mod.decorate(elm);


            // Move backwards to have the first list item yet to be parsed.
            if( JTexy.debug ) log.finest("Before moveBackward(): " + ((TexyBlockParser)parser).getPosAsString());
            ((TexyBlockParser) parser).moveBackward(1);
            if( JTexy.debug ) log.finest("After  moveBackward(): " + ((TexyBlockParser)parser).getPosAsString());

            // And now parse all the items.
            DOMElement itemElm;
            while( null != (itemElm = parseItem((TexyBlockParser) parser, itemBulletRegex, false, "li")) ) {
                elm.add(itemElm);
            }
            if( ! elm.hasContent() ) {
                return null;
            }

            // TODO.
            //getTexy().invokeNormalHandlers( new AfterListEvent() );

            return elm;

        }// handle()
    }// PatternList





    /**
     * List definition pattern handler.
     */
    private class DefListPatternHandler implements PatternHandler {

        @Override public String getName() { return "defList"; } // Not used - init() overriden.

        @Override
        public Node handle(TexyParser parser, List<MatchWithOffset> groups, RegexpInfo pattern) throws TexyException {
            //   [1] => .(title)[class]{style}<>
            //   [2] => ...
            //   [3] => .(title)[class]{style}<>
            //   [4] => space
            //   [5] => - * +
            if( log.isLoggable(Level.FINEST) ) {
                for( MatchWithOffset match : groups ) {
                    log.finest("  " + match.toString());
                }
            }

            String modStr = groups.get(1).match;
            String bulletStr = groups.get(5).match;


            // Determine the list type used.
            ListType listType = null;
            for( ListType curListType : ListType.values() ) {
                Matcher mat = curListType.getPattern().matcher( bulletStr );
                if( mat.matches() ) {
                    listType = curListType;
                    break;
                }
            }
            if( listType == null ) {
                return null; // None chosen??
            }
            TexyBlockParser blockParser = (TexyBlockParser) parser;

            // DL element.
            DOMElement elm = new DOMElement("dl");
            TexyModifier mod = new TexyModifier( modStr );
            mod.decorate( elm );
            blockParser.moveBackward(2);

            // @ $desc[3] == nextRegex .
            String itemBulletRegex = StringUtils.defaultString(listType.getNextRegExp(), listType.getFirstRegExp());


            // Parse the def list items.
            while( true ) {

                // New DD - definition.
                DOMElement itemElm;
                itemElm = parseItem( (TexyBlockParser) parser, itemBulletRegex, false, "li" );
                if( itemElm != null ){
                    elm.add(itemElm);
                    continue;
                }

                // New DT - definition term.
                List<MatchWithOffset> dtGroups = blockParser.next( TERM_PATTERN );
                if( null == dtGroups )
                    break;

                //    [1] => ...
                //    [2] => .(title)[class]{style}<>

                DOMElement dtElm = new DOMElement("dt");
                new TexyModifier( dtGroups.get(2).match ).decorate(dtElm);

                new TexyLineParser( getTexy(), dtElm );
                elm.add(dtElm);

            }// parse loop while{}.


            // TODO.
            //getTexy().invokeNormalHandlers( new AfterDefListEvent(parser, elm, mod) );

            //return elm;

            throw new UnsupportedOperationException("Definition lists supported yet.");
        }
    }

    /** Used in PatternDefList. */
    private final static Pattern TERM_PATTERN = Pattern.compile(
                    "^\\n?(\\S.*)\\:\\ *"+RegexpPatterns.TEXY_MODIFIER_H+"?()$",
                    Pattern.MULTILINE | Pattern.UNGREEDY ); // (?mU)





    /**
     * Parses a single ongoing list item.
     *
     * TODO: Try to optimize this not to create Patterns all around.
     *       Perhaps move that `while` (from which this is called) to a new method.
     */
    private static DOMElement parseItem(
                    TexyBlockParser parser,
                    String itemBulletRegex,
                    boolean indented,
                    String tagName
    ) throws TexyException
    {

        String spaceBase = indented ? "\\ +" : "";
        //   \\A == The beginning of the input, instead of ^ - we don't have the (?A) flag.
        String itemPattern = "(?mUu)\\A\\n?(" + spaceBase + ")" + itemBulletRegex + "\\ *(\\S.*)?"
                        + RegexpPatterns.TEXY_MODIFIER_H + "?()$";

        if( JTexy.debug ) log.finest("Parser at: " + parser.getPosAsString());///


        // First line with a bullet.
        List<MatchWithOffset> firstMatchGroups = parser.next( itemPattern );
        if( null == firstMatchGroups )  return null;
        if( JTexy.debug ) log.finest("List item match: " + firstMatchGroups);///

        //    [1] => indent
        //    [2] => ...
        //    [3] => .(title)[class]{style}<>

        String indentStr = firstMatchGroups.get(1).match;
        DOMElement itemElm = new DOMElement( tagName );
        String contentStr = firstMatchGroups.get(2).match;
        TexyModifier mod = new TexyModifier( firstMatchGroups.get(3).match );
        mod.decorate( itemElm );


        // Successive lines. They are indented to the same depth or more as first line's content.
        // E.g.  1)  Hello
        //           world!

        // Spaces count: First line inside the list item defines how much spaces
        //               precede all successive lines for this item.
        // At the beginning, we don't know, so it's 0.
        String spacesCnt = "";

        //@ while ($parser->next('#^(\n*)'.$mIndent.'(\ {1,'.$spaces.'})(.*)()$#Am', $matches)) {
        StringBuilder contentSB = new StringBuilder(" " + contentStr); // Prej trick.
        do {
            String regex = "(?-m)^(\\n*)" + indentStr + "(\\ {1,"+spacesCnt+"})(.*)()(?m)$"; // (?-m)^ ensures it matches right after 1st line.
            List<MatchWithOffset> nextMatchGroups = parser.next( regex );
            if( null == nextMatchGroups )
                break; // No lines between list items ( => inside )

            //    [1] => blank line?
            //    [2] => spaces
            //    [3] => ...
            if( "".equals(spacesCnt) ){
                spacesCnt = "" + nextMatchGroups.get(2).match.length();
            }
            contentSB.append("\n").append( nextMatchGroups.get(1).match ).append( nextMatchGroups.get(3).match );
        } while( true );

        // Parse item content.
        new TexyBlockParser( parser.getTexy(), itemElm, true ).parse( contentSB.toString() );

        // TODO: @ WTF?  Maybe it's to prevent <p> inside <li>.
        /*if (isset($elItem[0]) && $elItem[0] instanceof TexyHtml) {
            $elItem[0]->setName(NULL);
        }*/
        // TODO: Use the special container element name, or move children up.
        /*if( itemElm.getFirstChild().getNodeType() == Node.ELEMENT_NODE ){
            NodeList nodes = itemElm.getFirstChild().getChildNodes();
            //itemElm.removeChild( itemElm.getFirstChild() );
            for( int i = 0; i < nodes.getLength(); i++ ) {
                //itemElm.appendChild( nodes.item(i) );
                // Cannot convert: org.dom4j.tree.DefaultText@275d595c [Text: "Komentáře bych nečetl."] into a W3C DOM Node
                //itemElm.add( DOMNodeHelper.asDOMNode( itemElm, nodes.item(i) ));
                DOMNodeHelper.appendChild( itemElm, nodes.item(i) ); // TODO: Could improve Dom4j.
            }
        }/**/

        return itemElm;

    }// parseItem()

}
