package cz.dynawest.jtexy.modules

import cz.dynawest.jtexy.*
import cz.dynawest.jtexy.dom4j.Dom4jUtils
import cz.dynawest.jtexy.events.TexyEventListener
import cz.dynawest.jtexy.parsers.TexyLineParser
import cz.dynawest.openjdkregex.Pattern
import org.apache.commons.lang.StringUtils
import org.dom4j.Node
import org.dom4j.dom.DOMElement
import java.util.logging.*

/**
 *
 * @author Ondrej Zizka
 */
class ParagraphModule : TexyModule() {
    override val eventListeners: Array<TexyEventListener<*>>
        // -- Module metainfo -- //
        get() = arrayOf(parListener)

    /*
	@Override protected void onRegister() {
		getTexy().getAroundHandlers().addHandler( parListener );
	}*/
    override fun getPatternHandlerByName(name: String): PatternHandler? {
        return null // No patterns in this module.
    }

    // -- EventListener stuff -- //
    var parListener: ParagraphEventListener = object : ParagraphEventListener {
        override val eventClass: Class<*>
            get() = ParagraphEvent::class.java

        @Throws(TexyException::class)
        override fun onEvent(event: ParagraphEvent): Node? {
            return solve(null, event.text!!, event.modifier)
        }
    }

    /**
     * Creates a DOM node for this paragraph.
     */
    @Throws(TexyException::class)
    fun solve(invocation: Invocation?, content: String, modifier: TexyModifier?): DOMElement {

        // Find hard linebreaks.
        var content = content
        content = if (texy.options.mergeLines) {
            // ....
            //  ...  => \r means break line
            content.replace("\\n +(?=\\S)".toRegex(), "\r")
        } else {
            content.replace("\n", "\r")
        }
        val elm = DOMElement("p")
        //$el->parseLine($tx, $content);
        //$content = $el->getText(); // string
        // TODO: Debug. Hapruje to.
        TexyLineParser(texy, elm).parse(content)

        // Get the code out of the element,
        // process it textually, and put it back.

        //content = Dom4jUtils.getInnerCode( elm );
        //content = ProtectedHTMLWriter.fromElement( elm, getTexy().getProtector() );
        // Something like "new ProtectedHTMLWriter( Protector ).fromElement( elm )" might be cleaner...
        content = Dom4jUtils.getInnerText(elm)
        log.finer("Paragraph content after parse: $content")


        // Check content type.

        // Block contains block tag.
        if (-1 != content.indexOf(ContentType.BLOCK.delim)) {
            elm.name = Constants.HOLDER_ELEMENT_NAME // ignores modifier!
        } else if (-1 != content.indexOf(ContentType.TEXTUAL.delim)) {
        } else if (TM_PAT.matcher(content).find()) {
        } else if (-1 != content.indexOf(
                ContentType.REPLACED.delim
            )
        ) {
            elm.name = texy.options.nontextParagraph
        } else {
            // if{ ignoreEmptyStuff } return FALSE;
            if (null == modifier) elm.name = Constants.HOLDER_ELEMENT_NAME
        }


        // If not holder element...
        if (Constants.HOLDER_ELEMENT_NAME != elm.name) {
            // Apply the modifier.
            modifier?.decorate(elm)

            // Add <br />.
            if (-1 != content.indexOf("\r")) {
                val key = texy.protect("<br />", ContentType.REPLACED)
                content = content.replace("\r", key)
            }
        }
        content = StringUtils.replaceChars(content, "\r\n", "  ")
        elm.text = content
        return elm
    }

    companion object {
        private val log = Logger.getLogger(ParagraphModule::class.java.name)
        private val TM_PAT: Pattern = Pattern.compile("(?u)[^\\s" + RegexpPatterns.TEXY_MARK + "]")
    }
}
