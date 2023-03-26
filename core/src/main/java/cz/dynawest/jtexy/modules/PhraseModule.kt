package cz.dynawest.jtexy.modules

import cz.dynawest.jtexy.*
import cz.dynawest.jtexy.events.AroundEventListener
import cz.dynawest.jtexy.events.PhraseEvent
import cz.dynawest.jtexy.events.TexyEvent
import cz.dynawest.jtexy.events.TexyEventListener
import cz.dynawest.jtexy.parsers.LinkProcessEvent
import cz.dynawest.jtexy.parsers.TexyParser
import cz.dynawest.jtexy.util.JTexyStringUtils
import cz.dynawest.jtexy.util.MatchWithOffset
import org.apache.commons.lang.StringEscapeUtils
import org.dom4j.Node
import org.dom4j.dom.DOMElement
import org.dom4j.dom.DOMText
import java.util.logging.*

/**
 * Handles phrases, of which most can have
 * 1) Texy modifier
 * 2) a link attached following after ':'
 * as noted  below:
 *
 * <pre>
 * Mod?     Link?      Example
 * # ***strong+emphasis***     MOD      LINK
 * # **strong**                MOD      LINK
 * # //emphasis//              MOD      LINK
 * # *emphasisAlt*             MOD      LINK
 * # *emphasisAlt2*            MOD      LINK
 * # ++inserted++              MOD
 * # --deleted--               MOD
 * # ^^superscript^^           MOD
 * # m^2 alternative superscript
 * # __subscript__             MOD
 * # m_2 alternative subscript
 * # "span"                    MOD      LINK
 * # ~alternative span~        MOD      LINK
 * # ~~cite~~                  MOD      LINK
 * # >>quote&lt;&lt;           MOD      LINK
 * # acronym/abbr                                   "et al."((and others))
 * # acronym/abbr                                   NATO((North Atlantic Treaty Organisation))
 * # ''notexy''
 * # `code`                    MOD      LINK
 * # ....:LINK                          LINK
</pre> *
 *
 * @author Ondrej Zizka
 */
class PhraseModule : TexyModule() {
    override val eventListeners: List<TexyEventListener<TexyEvent>>
        get() = listOf(phraseListener as TexyEventListener<TexyEvent>)


    /** Return this module's pattern handler by name.  */
    override fun getPatternHandlerByName(name: String): PatternHandler? {
        return if ("patternPhrase" == name || "default" == name) {
            patternPhrase
        } else if ("patternNoTexy" == name) {
            PATTERN_NO_TEXY
        } else if ("patternSubSup" == name) {
            PATTERN_SUB_SUP
        } else {
            throw UnsupportedOperationException("Unknown pattern handler: $name")
        }
    }


    // --- Settings --- //

    /** Html links allowed?  */
    var isLinksAllowed = true


    /**
     * patternPhrase handler.
     */
    val patternPhrase: PatternHandler = object : PatternHandler {
        override val name: String
            get() = "patternPhrase"

        /**  TODO: Call solve().  */
        @Throws(TexyException::class)
        override fun handle(
            parser: TexyParser,
            groups: List<MatchWithOffset>,
            regexpInfo: RegexpInfo
        ): Node? {

            //    [1] => ** - probably means "enclosing string"; ignored in Texy.
            //    [2] => ...
            //    [3] => .(title)[class]{style}
            //    [4] => LINK
            for (match in groups) {
                log.finest("  " + match.toString()) ///
            }
            if (getRegexpInfo("phrase/span") != null) {
                log.finer(regexpInfo.perlRegexp)
                log.finer(regexpInfo.regexp)
            }
            val content = groups[1].match!!
            val modStr = groups[2].match
            val mod = modStr ?. let { TexyModifier(it)  } ?: TexyModifier()
            val linkStr = if (groups.size <= 3) null else groups[3].match
            var link: TexyLink? = TexyLink.fromString(linkStr)


            // TBD: What is this good for?
            //parser.again = "phrase/code".equals(pattern.name) || "phrase/quicklink".equals(pattern.name);
            if (regexpInfo.name.startsWith("phrase/span")) {
                if (linkStr == null) {
                    if (null == modStr) return null // Leave intact.
                } else {
                    // TBD: Why the hell is a link handled in a PhraseModule?!
                    // $link = $tx->linkModule->factoryLink($mLink, $mMod, $mContent);
                    val linkProcessEvent = LinkProcessEvent(
                        parser, mod, modStr, linkStr, content, null
                    )
                    texy.invokeAroundHandlers(linkProcessEvent)
                    link = linkProcessEvent.link!!
                }
            } else if (regexpInfo.name.startsWith("phrase/acronym")) {
                mod.title = JTexyStringUtils.unescapeHtml(linkStr!!.trim { it <= ' ' })
            } else if (regexpInfo.name.startsWith("phrase/quote")) {
                mod.cite = texy.linkModule ?.citeLink(linkStr)
            } else if (linkStr != null) {
                //link = texy.linkModule.factoryLink( linkStr );
                // @ Fire a processLinkEvent instead of calling LinkModule directly.
                val linkEvent = LinkProcessEvent(
                    parser, mod, modStr, linkStr, null, null
                )
                parser.texy.invokeAroundHandlers(linkEvent)
                link = linkEvent.link!!
            }


            //DOMElement elm = new DOMElement( pattern.htmlElement );
            //elm.addText( content );
            //Node node = solve( pattern.name, content, mod, link );
            val event = PhraseEvent(parser, content, mod, link, regexpInfo.name)
            return texy.invokeAroundHandlers(event)
        }
    }

    /**
     * Phrase Listener.
     */
    var phraseListener = object : PhraseEventListener {
        override val eventClass: Class<PhraseEvent>
            get() = PhraseEvent::class.java

        @Throws(TexyException::class)
        override fun onEvent(event: PhraseEvent): Node? {
            return solve(event.phraseName, event.text!!, event.modifier!!, event.link)
        }
    }

    /**
     * Solve.
     * @param phrase     Phrase ID.
     * @param content    Text content to process.
     * @param modifier   Texy content modifier .{...}[...](...)<>^ etc.
     * @param link       ???  Link is probably always null - see the patterns.
     * @return
     */
    private fun solve(phrase: String, content: String, modifier: TexyModifier, link: TexyLink?): Node? {
        var content = content
        val ri = getRegexpInfo(phrase)
        var elmName = ri?.htmlElement
        if ("a" == elmName) elmName = if (link != null && isLinksAllowed) null else "span"
        if ("phrase/code" == phrase) content = texy.protect(StringEscapeUtils.escapeHtml(content), ContentType.TEXTUAL)
        var nodeRet: Node? = null
        if ("phrase/strong+em" == phrase) {
            val elmRet = DOMElement(getRegexpInfo("phrase/strong").htmlElement)
            val eEm = DOMElement(getRegexpInfo("phrase/em").htmlElement)
            eEm.text = content
            elmRet.add(eEm)
            modifier.decorate(elmRet)
            nodeRet = elmRet
        } else if (null != elmName) {
            val elmRet = DOMElement(elmName)
            elmRet.text = content
            modifier.decorate(elmRet)
            if ("q" == elmName) elmRet.setAttribute("cite", modifier.cite)
            nodeRet = elmRet
        } else {
            // Trick - put's whole content
            nodeRet = DOMText(content)
        }

        //* TODO: Without this, links don't work!
        return if (link != null && isLinksAllowed) texy.linkModule!!.solveLinkReference(link, nodeRet) else nodeRet
    }

    companion object {
        private val log = Logger.getLogger(PhraseModule::class.java.name)

        /**
         * patternSubSup handler.  - __sub__  ^^sup^^  m^2  CO_2
         * TODO: Implement.
         */
        private val PATTERN_SUB_SUP: PatternHandler = object : PatternHandler {
            override val name: String
                get() = "patternSubSup"

            override fun handle(parser: TexyParser, groups: List<MatchWithOffset>, regexpInfo: RegexpInfo): Node? {
                for (match in groups!!) {
                    log.finer("  " + match.toString()) ///
                }
                throw UnsupportedOperationException("Not supported yet.")
            }
        }

        /**
         * patternNoTexy handler.
         * TODO: Implement.
         */
        private val PATTERN_NO_TEXY: PatternHandler = object : PatternHandler {
            override val name: String
                get() = "patternNoTexy"

            override fun handle(parser: TexyParser, groups: List<MatchWithOffset>, regexpInfo: RegexpInfo): Node? {
                for (match in groups!!) {
                    log.finer("  " + match.toString()) ///
                }
                throw UnsupportedOperationException("Not supported yet.")
            }
        }
    }
}

interface PhraseEventListener : AroundEventListener<PhraseEvent>
