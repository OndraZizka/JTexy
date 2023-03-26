package cz.dynawest.jtexy.modules

import cz.dynawest.jtexy.*
import cz.dynawest.jtexy.dom4j.io.ProtectedHTMLWriter
import cz.dynawest.jtexy.events.*
import cz.dynawest.jtexy.modules.TexyLink
import cz.dynawest.jtexy.parsers.*
import cz.dynawest.jtexy.util.JTexyStringUtils
import cz.dynawest.jtexy.util.MatchWithOffset
import cz.dynawest.jtexy.util.StringsReplaceCallback
import cz.dynawest.jtexy.util.UrlChecker
import cz.dynawest.openjdkregex.Pattern
import org.apache.commons.lang.StringUtils
import org.dom4j.Node
import org.dom4j.dom.DOMElement
import org.dom4j.dom.DOMText
import java.lang.Exception
import java.util.*
import java.util.logging.*

/**
 *
 * @author Ondrej Zizka
 */
class LinkModule : TexyModule() {
    override val eventListeners: Array<TexyEventListener<TexyEvent>> =
        arrayOf(
            linkListener,
            linkRefListener,
            linkProcessListener,
            NEW_REF_LISTENER,
            beforeParseListener
        )

    override fun getPatternHandlerByName(name: String): PatternHandler? {
        // [reference]
        if ("reference" == name) return referencePH
        // Direct url or email.
        return if ("urlOrEmail" == name) URL_OR_MAIL_PH else null
    }

    // References map - predefined links.
    private val refs: MutableMap<String, TexyLink> = HashMap()
    protected fun addRef(key: String, link: TexyLink) {
        refs[key.lowercase(Locale.getDefault())] = link
    }

    protected fun getRef(key: String): TexyLink? {
        val link = refs[key.lowercase(Locale.getDefault())] ?: return null
        return link.clone() // Cloning because we will change it.
    }
    // --- PatternHandler's --- //
    /**
     * Callback for: [ref].
     * Fires either a NewReference or a LinkReference event.
     */
    private val referencePH: PatternHandler = object : PatternHandler {
        override val name: String
            get() = "reference"

        @Throws(TexyException::class)
        override fun handle(parser: TexyParser, groups: List<MatchWithOffset>, pattern: RegexpInfo): Node? {
            //    [1] => [ref]
            val refName = StringUtils.substring(groups[0].match, 1, -1)
            val link = getRef(refName) ?: return texy.invokeAroundHandlers(NewReferenceEvent(parser, refName))
            // New reference encountered?
            link.type = TexyLink.Type.BRACKET
            var content: String

            // Label is not empty?
            if (!link.label.isNullOrEmpty() && !link.name.isNullOrEmpty()) {
                // Prevent circular references. TBD: Analyze.
                if (liveLock.contains(link.name)) content = link.label!!
                else {
                    liveLock.add(link.name!!)
                    val elm = DOMElement(Constants.HOLDER_ELEMENT_NAME)
                    TexyLineParser(texy, elm).parse(link.label!!)
                    content = ProtectedHTMLWriter.Companion.fromElement(elm, texy.protector)
                    liveLock.remove(link.name)
                }
            } else {
                content = link.asText(true, true)
                content = texy.protect(content, ContentType.TEXTUAL)
            }
            return texy.invokeAroundHandlers(LinkReferenceEvent(parser, content, link))
        }
    }

    /** Used by the referencePH. TBD: Analyze.  */
    private val liveLock: MutableSet<String> = HashSet()


    // --- EventListener's --- //
    /**
     * Before parse event listener, which resets module's internals
     * and parses and removes the link definitions.
     */
    private val beforeParseListener: BeforeAfterEventListener<BeforeParseEvent> = object : BeforeAfterEventListener<BeforeParseEvent> {
        override val eventClass: Class<*>
            get() = BeforeParseEvent::class.java

        @Throws(TexyException::class)
        override fun onEvent(event: BeforeParseEvent): Node? {

            // Reset this module's internals.
            liveLock.clear()

            // [la trine]: http://www.latrine.cz/ text odkazu .(title)[class]{style}
            if (texy.options.useLinkDefinitions) {
                // Parses the reference defs, add them to this, and removes them from the text.
                var text = event.text
                text = JTexyStringUtils.replaceWithCallback(text, PAT_LINK_DEFINITION, REF_DEF_CALLBACK)
                event.text = text
            }
            return null
        }
    }

    /** Reference definition callback for BeforeAfterEventListener.  */
    private val REF_DEF_CALLBACK = object: StringsReplaceCallback {
        override fun replace(groups: Array<String>): String {
            //    [1] => [ (reference) ]
            //    [2] => link
            //    [3] => ...
            //    [4] => .(title)[class]{style}
            val link = TexyLink(groups[2])
            link.label = groups[3]
            link.modifier = TexyModifier(groups[4])
            fixLink(link)
            addRef(groups[1], link)
            return ""
        }
    }

    /** New reference event.  */
    class NewReferenceEvent(parser: TexyParser, refName: String) : AroundEvent(parser, refName, null) {
        val refName: String?
            get() = text // Alias.
    }

    /** Link reference event.  */
    class LinkReferenceEvent(parser: TexyParser, content: String, var link: TexyLink) : AroundEvent(parser, content, null)

    /** Link reference event listener.   Texy: solve()  */
    private val linkRefListener: TexyEventListener<LinkReferenceEvent> = object : AroundEventListener<LinkReferenceEvent> {
        override val eventClass: Class<*>
            get() = LinkReferenceEvent::class.java

        @Throws(TexyException::class)
        override fun onEvent(event: LinkReferenceEvent): Node? {
            return this@LinkModule.solveLinkReference(event.link, event.text)
        }
    }

    /**
     * LinkEvent - for both URL and e-mail.
     */
    class LinkEvent(parser: TexyParser, var link: TexyLink, var isEmail: Boolean) : AroundEvent(parser, null, null)

    /** LinkEvent listener.  */
    private val linkListener: TexyEventListener<LinkEvent> = object : AroundEventListener<LinkEvent> {
        override val eventClass: Class<*>
            get() = LinkEvent::class.java

        @Throws(TexyException::class)
        override fun onEvent(event: LinkEvent): Node? {
            var content = event.link.asText(
                texy.options.makeAutoLinksShorter,
                texy.options.obfuscateEmails
            )
            content = texy.protect(content, ContentType.TEXTUAL)
            return solveLinkReference(event.link, content)
        }
    }

    /**
     * Instead of factoryLink($dest, $mMod, $label).
     * TBD: Refactorize to get rid of inter-module calls.
     * We will possibly need to modify the processing.
     */
    private val linkProcessListener: AroundEventListener<LinkProcessEvent> = object : AroundEventListener<LinkProcessEvent> {
        override val eventClass: Class<*>
            get() = LinkProcessEvent::class.java

        @Throws(TexyException::class)
        override fun onEvent(event: LinkProcessEvent): Node? {
            var link: TexyLink? = null
            var linkType = TexyLink.Type.COMMON
            var dest = event.dest

            // References.
            if (dest!!.length > 1 && dest[0] == '[') {
                // [ref]
                if (dest[1] != '*') {
                    linkType = TexyLink.Type.BRACKET
                    dest = StringUtils.substring(dest, 1, -1)
                    link = getRef(dest)
                } else {
                    linkType = TexyLink.Type.IMAGE
                    dest = StringUtils.substring(dest, 2, -2).trim { it <= ' ' }
                    link = event.linkProvider!!.getLink(dest)
                }
            }

            // Normal link or an unresolved reference.
            if (null == link) {
                link = TexyLink(dest.trim { it <= ' ' })
                fixLink(link)
            }


            // TBD: What is this feature? Seems like expanding of some URL shortcut.
            if (event.label != null && link.url!!.contains("%s")) {
                var res = JTexyStringUtils.stringToText(event.label, null)
                res = JTexyStringUtils.encodeUrl(res)
                link.url = link.url!!.replace("%s", res)
            }
            link.modifier = event.modifier
            link.type = linkType
            event.link = link
            return null
        }
    }

    /**
     * Prepare link for a citation.
     *
     * TBD: Unify all these ***Link() methods not to be spread over modules.
     */
    fun citeLink(linkStr: String?): String? {
        var linkStr = linkStr ?: return null

        // [ref]
        if (linkStr[0] == '[') {
            linkStr = StringUtils.substring(linkStr, 1, -1)
            // @ Calling wildly across the modules :-/
            val ref = getRef(linkStr)
            if (null != ref) return JTexyStringUtils.prependUrlPrefix(
                texy.options.linkRootUrl, ref.url
            )
        }

        // Special supported case. TBD: Checked several times? See fixLink().
        return if (linkStr.startsWith("www.")) "http://$linkStr" else JTexyStringUtils.prependUrlPrefix(
            texy.options.linkRootUrl, linkStr
        )

        // Else just return the URL.
    }

    /**
     * Convenience method.
     * @returns solveLinkReference(link, new DOMText(content));
     */
    fun solveLinkReference(link: TexyLink?, content: String?): Node? {
        return solveLinkReference(link, DOMText(content))
    }

    /**
     * Creates a [  element for the given link.
 *
 * Needs to be public as it's called from PhraseModule (hack from Texy).
](...) */
    fun solveLinkReference(link: TexyLink?, content: Node?): Node? {
        log.finer("Link: $link Content: $content")
        if (link!!.url == null) return content
        val elm = DOMElement("a")
        var nofollow = false
        var popup = false

        // Modifier.
        if (link.modifier != null) {
            nofollow = link.modifier!!.classes.remove("nofollow")
            popup = link.modifier!!.classes.remove("popup")
            link.modifier!!.decorate(elm)
        }

        // href="..."
        run {
            val href: String?
            if (link.type == TexyLink.Type.IMAGE) {
                href = JTexyStringUtils.prependUrlPrefix(texy.options.imageRootUrl, link.url)
                //elm.setAttribute("onclick", this.imageOnClick);
            } else {
                href = JTexyStringUtils.prependUrlPrefix(texy.options.linkRootUrl, link.url)
                // Nofollow.
                if (nofollow || texy.options.forceNofollow && elm.getAttribute("href").contains("//"))
                    elm.setAttribute("rel", "nofollow")
            }
            log.finest("Resulting href = $href")
            elm.setAttribute("href", href)
        }

        // onclick popup
        if (popup) elm.setAttribute("onclick", texy.options.popupOnclick)
        if (content != null) elm.add(content)

        // TBD: Move to JTexy or DocumentContext?
        allDocumentLinks.add(elm.getAttribute("href"))
        return elm
        // TBD: Move options to this module?
    }

    /** Used in [.solveLinkReference].  */
    val allDocumentLinks: MutableList<String> = ArrayList()

    /**  Returns reference using this module's link reference map.  */
    val LINK_PROVIDER = LinkProvider { key: String -> getRef(key) }

    companion object {
        private val log = Logger.getLogger(LinkModule::class.java.name)

        /**
         * Callback for: http://davidgrudl.com   david@grudl.com.
         * Fires a LinkEvent.
         */
        private val URL_OR_MAIL_PH: PatternHandler = object : PatternHandler {
            override val name: String
                get() = "urlOrEmail"

            @Throws(TexyException::class)
            override fun handle(parser: TexyParser, groups: List<MatchWithOffset>, ri: RegexpInfo): Node? {
                //    [0] => URL
                if (log.isLoggable(Level.FINEST)) for (match in groups) log.finest("  " + match.toString())
                val link: TexyLink? = TexyLink.fromString(groups[0].match)
                link ?.let { fixLink(it) }
                if (link == null) return null

                log.finest("Link: $link")
                val isEmail = "link/email" == ri.name
                parser.texy ?: throw Exception("No parent parser (parser.texy) in the current parser. Should not happen I guess?")
                return parser.texy.invokeAroundHandlers(LinkEvent(parser, link, isEmail))
            }
        }

        /** Link definition pattern  */
        private val PAT_LINK_DEFINITION: Pattern = Pattern.compile(
            "^\\[([^\\[\\]#\\?\\*\\n]+)\\]: +(\\S+)(\\ .+)?" + RegexpPatterns.TEXY_MODIFIER + "?\\s*()$",
            Pattern.MULTILINE or Pattern.UNGREEDY or Pattern.UNICODE_CASE
        )

        /** New reference event listener - no-op.  */
        private val NEW_REF_LISTENER: TexyEventListener<NewReferenceEvent> = object : AroundEventListener<NewReferenceEvent> {
            override val eventClass: Class<*>
                get() = NewReferenceEvent::class.java

            @Throws(TexyException::class)
            override fun onEvent(event: NewReferenceEvent): Node? {
                return null
            }
        }

        /**
         * Checks and corrects $URL.
         * @param  TexyLink
         * @return void
         */
        private fun fixLink(link: TexyLink?) {
            // Remove soft hyphens; if not removed by Texy::process().
            link!!.url = link.url!!.replace("\u00C2\u00AD", "")

            // www.
            if (StringUtils.startsWithIgnoreCase(link.url, "www.")) {
                link.url = "http://" + link.url
            } else if (link.url!!.matches(("(?u)^" + RegexpPatterns.TEXY_EMAIL + "$").toRegex())) {
                link.url = "mailto:" + link.url
            } else if (!UrlChecker.checkURL(link.url, UrlChecker.Type.ANCHOR)) {
                link.url = null
            } else {
                // Replace unwanted &amp; .
                link.url = link.url!!.replace("&amp;", "&")
            }
        }
    }
}
