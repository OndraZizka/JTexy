package cz.dynawest.jtexy.modules

import cz.dynawest.jtexy.*
import cz.dynawest.jtexy.dom4j.io.ProtectedHTMLWriter
import cz.dynawest.jtexy.events.BeforeBlockEvent
import cz.dynawest.jtexy.events.BeforeBlockEventListener
import cz.dynawest.jtexy.events.TexyEvent
import cz.dynawest.jtexy.events.TexyEventListener
import cz.dynawest.jtexy.parsers.TexyBlockParser
import cz.dynawest.jtexy.parsers.TexyLineParser
import cz.dynawest.jtexy.parsers.TexyParser
import cz.dynawest.jtexy.util.JTexyStringUtils
import cz.dynawest.jtexy.util.MatchWithOffset
import org.apache.commons.lang.StringUtils
import org.dom4j.Node
import org.dom4j.dom.DOMElement
import org.dom4j.dom.DOMText
import java.lang.Exception

/**
 *
 * @author Ondrej Zizka
 */
class BlockModule : TexyModule() {
    // --- Module meta-info --- //
    override fun getPatternHandlerByName(name: String): PatternHandler? {
        return if ("blocks" == name) blocksPatternHandler else null
    }

    override val eventListeners: Array<out TexyEventListener<in TexyEvent>>
        get() = arrayOf(beforeBlockListener, blockListener)

    /**
     * blocksPatternHandler
     */
    private val blocksPatternHandler: PatternHandler = object : PatternHandler {
        override val name: String
            get() = "blocks"

        @Throws(TexyException::class)
        override fun handle(parser: TexyParser, groups: List<MatchWithOffset>, pattern: RegexpInfo): Node? {
            //    [1] => code | text | ...
            //    [2] => ... additional parameters
            //    [3] => .(title)[class]{style}<>
            //    [4] => ... content
            var param = groups[1].match
            val mod = TexyModifier(groups[2].match!!)
            val content = groups[3].match!!
            val parts = param!!.split("(?u)\\s+".toRegex(), limit = 2).toTypedArray()
            val blockType = if (parts.size == 0) "block/default" else "block/" + parts[0]
            param = if (parts.size >= 2) parts[1] else null
            val event = BlockEvent(parser, content, mod, blockType, param)
            return texy.invokeAroundHandlers(event)
        }
    }

    /**
     * Finish invocation.
     * @return TexyHtml|string|FALSE
     */
    val blockListener: BlockEventListener = object : BlockEventListener {
        override val eventClass: Class<*>
            get() = BlockEvent::class.java

        @Throws(TexyException::class)
        override fun onEvent(event: BlockEvent): Node? {
            var event = event
            var str = event.text ?: throw Exception("Expected BlockEvent to have a text.")
            var blockType = event.blockType
            val texy = event.parser.texy
            if ("block/texy" == blockType) {
                /*$el = TexyHtml::el();
				$el->parseBlock(texy, str, $parser->isIndented());
				return $el;
				 */
                val elm = DOMElement(Constants.HOLDER_ELEMENT_NAME)
                TexyBlockParser(texy, elm, event.parser.isIndented).parse(str)
                return elm
            }
            if (!texy.isAllowed(blockType)) return null
            if ("block/texysource" == blockType) {
                str = JTexyStringUtils.outdent(str)
                if ("" == str) return DOMText("\n")
                val elm = DOMElement(Constants.HOLDER_ELEMENT_NAME)
                if ("line" == event.param) TexyLineParser(texy, elm).parse(str) else TexyBlockParser(texy, elm, false).parse(str)
                str = JTexyStringUtils.elementToHTML(elm, texy) // $s = $el->toHtml($tx);
                blockType = "block/code"
                event = BlockEvent(event, blockType, "html") // To be continued (as block/code).
            }
            if ("block/code" == blockType) {
                str = JTexyStringUtils.outdent(str)
                if ("" == str) return DOMText("\n")
                str = JTexyStringUtils.escapeHtml(str)
                str = texy.protect(str, ContentType.BLOCK)
                val elm = DOMElement("pre")
                event.modifier!!.classes.add(event.param) // Code language.
                event.modifier!!.decorate(texy, elm)
                elm.addElement("code").addText(str)
                return elm
            }
            if ("block/default" == blockType) {
                str = JTexyStringUtils.outdent(str)
                if ("" == str) return DOMText("\n")
                val elm = DOMElement("pre")
                event.modifier!!.classes.add(event.param) // Code language.
                event.modifier!!.decorate(texy, elm)
                str = JTexyStringUtils.escapeHtml(str)
                str = texy.protect(str, ContentType.BLOCK)
                elm.text = str
                return elm
            }
            if ("block/pre" == blockType) {
                str = JTexyStringUtils.outdent(str)
                if ("" == str) return DOMText("\n")
                val elm = DOMElement("pre")
                event.modifier!!.decorate(texy, elm)
                val lp = TexyLineParser(texy, elm)
                // Special mode - parse only HTML tags and comments.
                /* TODO
				 $tmp = $lineParser->patterns;
				$lineParser->patterns = array();
				if (isset($tmp['html/tag'])) $lineParser->patterns['html/tag'] = $tmp['html/tag'];
				if (isset($tmp['html/comment'])) $lineParser->patterns['html/comment'] = $tmp['html/comment'];
				unset($tmp);
				 */lp.parse(str)
                //str = elm.asXML(); //$el->getText();
                //str = Dom4jUtils.getInnerCode( elm );
                str = ProtectedHTMLWriter.Companion.fromElement(elm, texy.protector)
                str = JTexyStringUtils.unescapeHtml(str) // Get rid of HTML entities.
                str = JTexyStringUtils.escapeHtml(str) // Escape HTML spec chars.
                str = texy.unprotect(str) // Expand protected sub-strings.
                str = texy.protect(str, ContentType.BLOCK) // Protect the whole string.
                elm.text = str
                return elm
            }
            if ("block/html" == blockType) {
                str = StringUtils.strip(str, "\n")
                if ("" == str) return DOMText("\n")
                val elm = DOMElement(Constants.HOLDER_ELEMENT_NAME)
                val lp = TexyLineParser(texy, elm)
                // special mode - parse only html tags
                /* TODO
				$tmp = $lineParser->patterns;
				$lineParser->patterns = array();
				if (isset($tmp['html/tag'])) $lineParser->patterns['html/tag'] = $tmp['html/tag'];
				if (isset($tmp['html/comment'])) $lineParser->patterns['html/comment'] = $tmp['html/comment'];
				unset($tmp);
				 */lp.parse(str)
                //str = Dom4jUtils.getInnerCode( elm ); //$el.getText();
                str = ProtectedHTMLWriter.Companion.fromElement(elm, texy.protector)
                str = JTexyStringUtils.unescapeHtml(str) // Get rid of HTML entities.
                str = JTexyStringUtils.escapeHtml(str)
                str = texy.unprotect(str)
                return DOMText(
                    """
    ${texy.protect(str, ContentType.BLOCK)}
    
    """.trimIndent()
                )
            }
            if ("block/text" == blockType) {
                str = StringUtils.strip(str, "\n")
                if ("" == str) return DOMText("\n")
                str = JTexyStringUtils.escapeHtml(str)
                //str = str_replace("\n", TexyHtml::el('br')->startTag() , str); // nl2br
                str = str.replace("\n", "<br />")
                return DOMText(
                    """
    ${texy.protect(str, ContentType.BLOCK)}
    
    """.trimIndent()
                )
            }
            if ("block/comment" == blockType) {
                return DOMText("\n")
            }
            if ("block/div" == blockType) {
                str = JTexyStringUtils.outdent(str)
                if ("" == str) return DOMText("\n")
                val elm = DOMElement("div")
                event.modifier!!.decorate(texy, elm)
                //$el->parseBlock(texy, str, $parser->isIndented()); // TODO: INDENT or NORMAL ?
                TexyBlockParser(texy, elm, event.parser.isIndented).parse(str)
                return elm
            }
            return null
        }
    }

    /**
     * Single block pre-processing.
     */
    val beforeBlockListener: BeforeBlockEventListener<BeforeBlockEvent> = object : BeforeBlockEventListener<BeforeBlockEvent> {
        override val eventClass: Class<*>
            get() = BeforeBlockEvent::class.java

        override fun onEvent(event: BeforeBlockEvent): Node? {
            event.text ?: throw Exception("Expected BeforeBlockEvent to have a text.")

            // Autoclose exclusive blocks.
            /*$text = preg_replace(
				'#^(/--++ *+(?!div|texysource).*)$((?:\n.*+)*?)(?:\n\\\\--.*$|(?=(\n/--.*$)))#mi',
				"\$1\$2\n\\--", 	$text 	); */
            val text = event.text!!.replace(
                /*  /--<something> - not div or texysource
                 *  followed by 0+ lines reluctantly
                 *  and then (takes first) either
                 *  \--... or /--...
                 */
                ("(?:mi)^(/--++ *+(?!div|texysource).*)$" +
                        "((?:\\n.*+)*?)" +
                        "(?:\\n\\\\--.*$|(?=(\\n/--.*$)))").toRegex(),  // Normalize to /--... <content> \--
                "$1$2\n\\--"
            )
            event.text = text
            return DOMText(text)
        }
    }
}
