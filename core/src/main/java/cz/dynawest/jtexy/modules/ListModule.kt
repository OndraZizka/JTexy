package cz.dynawest.jtexy.modules

import cz.dynawest.jtexy.JTexy
import cz.dynawest.jtexy.RegexpInfo
import cz.dynawest.jtexy.RegexpPatterns
import cz.dynawest.jtexy.TexyException
import cz.dynawest.jtexy.events.TexyEventListener
import cz.dynawest.jtexy.parsers.TexyBlockParser
import cz.dynawest.jtexy.parsers.TexyLineParser
import cz.dynawest.jtexy.parsers.TexyParser
import cz.dynawest.jtexy.util.MatchWithOffset
import cz.dynawest.openjdkregex.Pattern
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.math.NumberUtils
import org.dom4j.Node
import org.dom4j.dom.DOMElement
import java.util.logging.*

/**
 *
 * @author Ondrej Zizka, Martin Večeřa
 */
class ListModule : TexyModule() {
    private val listPatternHandler: PatternHandler = ListPatternHandler()
    private val listDefPatternHandler: PatternHandler = DefListPatternHandler()
    override val eventListeners: Array<TexyEventListener<*>>
        // -- Module meta-info -- //
        get() = arrayOf()

    override fun getPatternHandlerByName(name: String): PatternHandler? {
        return if (listPatternHandler.name == name) {
            listPatternHandler
        } else if (listDefPatternHandler.name == name) {
            listDefPatternHandler
        } else {
            null
        }
    }

    /**
     * Override init() - don't read the properties file.
     * Instead, create the regex infos manually from the ListType enum.
     */
    @Throws(TexyException::class)
    override fun loadRegexFromPropertiesFile(propertiesFilePath: String?) {

        // List.
        val sb1 = StringBuilder()
        sb1.append("#^(?:").append(RegexpPatterns.TEXY_MODIFIER_H).append("\\n)?")
        sb1.append("(")
        for (bullet in ListType.values()) {
            sb1.append(bullet.firstRegExp).append("|")
        }
        sb1.deleteCharAt(sb1.length - 1)
        sb1.append(")\\ *\\S.*$#mUu")

        // List definition.
        val sb2 = StringBuilder()
        sb2.append("#^(?:").append(RegexpPatterns.TEXY_MODIFIER_H).append("\\n)?")
        sb2.append("(\\S.*)\\:\\ *").append(RegexpPatterns.TEXY_MODIFIER_H).append("?\\n") // Term
        sb2.append("(\\ ++)(")
        for (bullet in ListType.values()) {
            if (!bullet.isOrdered) {
                sb2.append(bullet.firstRegExp).append("|")
            }
        }
        sb2.deleteCharAt(sb2.length - 1)
        sb2.append(")\\ *\\S.*$#mUu")
        log.finer("List regex: $sb1")
        log.finer("List def regex: $sb2")

        // List.
        val listRI = RegexpInfo("list", RegexpInfo.Type.BLOCK)
        listRI.parseRegexp(sb1.toString())
        listRI.handler = listPatternHandler
        addRegexpInfo(listRI)

        // List definition.
        val listDefRI = RegexpInfo("list/definition", RegexpInfo.Type.BLOCK)
        listDefRI.parseRegexp(sb2.toString())
        listDefRI.handler = listDefPatternHandler
        addRegexpInfo(listDefRI)
    } // init()
    // -- Handlers and listeners -- //
    /**
     * List pattern handler.
     */
    private inner class ListPatternHandler : PatternHandler {
        override val name: String
            get() = "list" // Not used - init() overriden.

        @Throws(TexyException::class)
        override fun handle(parser: TexyParser, groups: List<MatchWithOffset>, pattern: RegexpInfo): Node? {
            /*
             *  Advances in two steps:
             *   1. Reads the first line of the list, to get its type.
             *   2. Then goes back one line and reads the whole list.
             */

            //    [1] => .(title)[class]{style}<>
            //    [2] => bullet * + - 1) a) A) IV)
            if (log.isLoggable(Level.FINEST)) {
                for (match in groups!!) {
                    log.finest("  " + match.toString())
                }
            }
            val modStr = groups!![1]!!.match
            val bulletStr = groups[2]!!.match
            val elm = DOMElement(null as String?)

            // Determine the list type used.
            var listType: ListType? = null
            for (curListType in ListType.values()) {
                val mat = curListType.pattern.matcher(bulletStr)
                if (mat!!.matches()) {
                    listType = curListType
                    break
                }
            }
            if (listType == null) {
                return null // None chosen??
            }
            val itemBulletRegex =
                listType.nextOrFirstRegExp // StringUtils.defaultString( listType.getNextRegExp(), listType.getFirstRegExp() );
            val min = if (listType.nextRegExp == null) 1 else 2 // List must have at least <min> items.

            // Prepare the list element.
            run {
                elm.name = if (listType.isOrdered) "ol" else "ul"
                if (null != listType.listStyleType) elm.setAttribute("style", "list-style-type: " + listType.listStyleType)

                // Ordered list.
                if (listType.isOrdered) {
                    val firstTypeBulChar = listType.type[0]
                    // 1) or 1.
                    if (listType == ListType.ARABPAR || listType == ListType.ARABDOT) {
                        val bulletNum = NumberUtils.toInt(StringUtils.chop(bulletStr!!.trim { it <= ' ' }))
                        if (bulletNum > 1) {
                            elm.setAttribute("start", "" + bulletNum)
                        }
                    } else {
                        val firstBulChar = bulletStr!![0]
                        if (firstTypeBulChar == 'a' && firstBulChar > 'a') {
                            elm.setAttribute("start", Character.toString(('1'.code + firstBulChar.code - 'a'.code).toChar()))
                        } else if (firstTypeBulChar == 'A' && firstBulChar > 'A') {
                            elm.setAttribute("start", Character.toString(('1'.code + firstBulChar.code - 'A'.code).toChar()))
                        }
                    }
                } // <ol>
            }
            modStr ?.let { TexyModifier().decorate(elm) }


            // Move backwards to have the first list item yet to be parsed.
            if (JTexy.debug) log.finest("Before moveBackward(): " + (parser as TexyBlockParser).posAsString)
            (parser as TexyBlockParser).moveBackward(1)
            if (JTexy.debug) log.finest("After  moveBackward(): " + parser.posAsString)

            // And now parse all the items.
            var itemElm: DOMElement?
            while (null != parseItem(parser, itemBulletRegex, false, "li").also { itemElm = it }) {
                elm.add(itemElm)
            }
            return if (!elm.hasContent()) null else elm

            // TODO.
            //getTexy().invokeNormalHandlers( new AfterListEvent() );
        } // handle()
    } // PatternList

    /**
     * List definition pattern handler.
     */
    private inner class DefListPatternHandler : PatternHandler {
        override val name: String
            get() = "defList" // Not used - init() overriden.

        @Throws(TexyException::class)
        override fun handle(parser: TexyParser, groups: List<MatchWithOffset>, pattern: RegexpInfo): Node? {
            //   [1] => .(title)[class]{style}<>
            //   [2] => ...
            //   [3] => .(title)[class]{style}<>
            //   [4] => space
            //   [5] => - * +
            if (log.isLoggable(Level.FINEST)) {
                for (match in groups) {
                    log.finest("  " + match.toString())
                }
            }
            val modStr = groups[1].match
            val bulletStr = groups[5].match


            // Determine the list type used.
            var listType: ListType? = null
            for (curListType in ListType.values()) {
                val mat = curListType.pattern.matcher(bulletStr)
                if (mat!!.matches()) {
                    listType = curListType
                    break
                }
            }
            if (listType == null) {
                return null // None chosen??
            }
            val blockParser = parser as TexyBlockParser

            // DL element.
            val elm = DOMElement("dl")
            modStr ?.let { TexyModifier(it).decorate(elm) }
            blockParser.moveBackward(2)

            // @ $desc[3] == nextRegex .
            val itemBulletRegex = StringUtils.defaultString(listType.nextRegExp, listType.firstRegExp)


            // Parse the def list items.
            while (true) {

                // New DD - definition.
                var itemElm: DOMElement?
                itemElm = parseItem(parser, itemBulletRegex, false, "li")
                if (itemElm != null) {
                    elm.add(itemElm)
                    continue
                }

                // New DT - definition term.
                val dtGroups = blockParser.next(TERM_PATTERN) ?: break

                //    [1] => ...
                //    [2] => .(title)[class]{style}<>
                val dtElm = DOMElement("dt")
                TexyModifier(dtGroups[2].match!!).decorate(dtElm)
                TexyLineParser(texy, dtElm)
                elm.add(dtElm)
            } // parse loop while{}.
            throw UnsupportedOperationException("Definition lists supported yet.")
        }
    }

    companion object {
        private val log = Logger.getLogger(ListModule::class.java.name)

        /** Used in PatternDefList.  */
        private val TERM_PATTERN: Pattern = Pattern.compile(
            "^\\n?(\\S.*)\\:\\ *" + RegexpPatterns.TEXY_MODIFIER_H + "?()$",
            Pattern.MULTILINE or Pattern.UNGREEDY
        ) // (?mU)

        /**
         * Parses a single ongoing list item.
         *
         * TODO: Try to optimize this not to create Patterns all around.
         * Perhaps move that `while` (from which this is called) to a new method.
         */
        @Throws(TexyException::class)
        private fun parseItem(
            parser: TexyBlockParser,
            itemBulletRegex: String?,
            indented: Boolean,
            tagName: String
        ): DOMElement? {
            val spaceBase = if (indented) "\\ +" else ""
            //   \\A == The beginning of the input, instead of ^ - we don't have the (?A) flag.
            val itemPattern = ("(?mUu)\\A\\n?(" + spaceBase + ")" + itemBulletRegex + "\\ *(\\S.*)?"
                    + RegexpPatterns.TEXY_MODIFIER_H + "?()$")
            if (JTexy.debug) log.finest("Parser at: " + parser.posAsString) ///


            // First line with a bullet.
            val firstMatchGroups = parser.next(itemPattern) ?: return null
            if (JTexy.debug) log.finest("List item match: $firstMatchGroups") ///

            //    [1] => indent
            //    [2] => ...
            //    [3] => .(title)[class]{style}<>
            val indentStr = firstMatchGroups[1].match
            val itemElm = DOMElement(tagName)
            val contentStr = firstMatchGroups[2].match
            firstMatchGroups[3].match ?.let { TexyModifier(it).decorate(itemElm) }


            // Successive lines. They are indented to the same depth or more as first line's content.
            // E.g.  1)  Hello
            //           world!

            // Spaces count: First line inside the list item defines how much spaces
            //               precede all successive lines for this item.
            // At the beginning, we don't know, so it's 0.
            var spacesCnt = ""

            //@ while ($parser->next('#^(\n*)'.$mIndent.'(\ {1,'.$spaces.'})(.*)()$#Am', $matches)) {
            val contentSB = StringBuilder(" $contentStr") // Prej trick.
            do {
                val regex = "(?-m)^(\\n*)$indentStr(\\ {1,$spacesCnt})(.*)()(?m)$" // (?-m)^ ensures it matches right after 1st line.
                val nextMatchGroups = parser.next(regex) ?: break
                // No lines between list items ( => inside )

                //    [1] => blank line?
                //    [2] => spaces
                //    [3] => ...
                if ("" == spacesCnt) {
                    spacesCnt = "" + nextMatchGroups[2]!!.match!!.length
                }
                contentSB.append("\n").append(nextMatchGroups[1]!!.match).append(nextMatchGroups[3]!!.match)
            } while (true)

            // Parse item content.
            TexyBlockParser(parser.texy, itemElm, true).parse(contentSB.toString())

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
        }/ **/return itemElm
        } // parseItem()
    }
}
