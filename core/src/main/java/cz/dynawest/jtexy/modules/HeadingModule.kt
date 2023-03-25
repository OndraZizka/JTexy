package cz.dynawest.jtexy.modules

import cz.dynawest.jtexy.RegexpInfo
import cz.dynawest.jtexy.TexyException
import cz.dynawest.jtexy.events.*
import cz.dynawest.jtexy.modules.HeadingModule.Options.Balancing
import cz.dynawest.jtexy.parsers.*
import cz.dynawest.jtexy.parsers.TexyBlockParser
import cz.dynawest.jtexy.util.*
import org.apache.commons.lang.StringUtils
import org.dom4j.Node
import org.dom4j.dom.DOMElement
import java.util.*
import java.util.logging.*

/**
 * Heading module - takes care of headings:
 *
 * <pre>
 * Heading
 * =======
 *
 * ###  Heading ###
</pre> *
 *
 * Handlers and listeners:
 *
 *  *  private PatternHandler underlinedHeadingPH = new PatternHandler()
 *  *  private PatternHandler surroundedHeadingPH = new PatternHandler()
 *  *  private HeadingEventListener headingListener = new HeadingEventListener()
 *  *  private AfterParseListener afterParse = new AfterParseListener()
 *
 * @author Ondrej Zizka
 */
class HeadingModule : TexyModule() {
    // TODO: Create some `ParsingContext` for each parsed document
    //       and store these state variables there.
    class Options {
        /** More chars in surrounded heading means bigger heading.  */
        var moreMeansHigher = false
        var generateIDs = true
        var idPrefix = "toc-"

        enum class Balancing {
            DYNAMIC, STATIC
        }

        var balancing = Balancing.DYNAMIC
    }

    protected class Context {
        // Context state.
        var documentTitle: String? = null

        /** Level of the top heading.  */
        var topLevel = 1

        /**
         * TOC.  Re-set in BeforeParseListener.
         * TODO: Make thread-safe.
         */
        var toc: MutableList<HeadingInfo> = LinkedList()

        /** Used auto-generated TOC IDs.  */
        var usedIDs: Set<String> = HashSet()
    }

    val opt = Options()
    private val ctx = Context()

    override val eventListeners: Array<TexyEventListener<*>>
        // --- Module meta-info --- //
        get() = arrayOf(
            beforeParse,
            headingListener,
            afterParse
        )

    override fun getPatternHandlerByName(name: String): PatternHandler? {
        if ("surrounded" == name) return surroundedHeadingPH
        return if ("underlined" == name) underlinedHeadingPH else null
    }

    /**
     * Callback for underlined heading.
     *
     * Heading .(title)[class]{style}>
     * -------------------------------
     */
    private val underlinedHeadingPH: PatternHandler = object : PatternHandler {
        override val name: String
            get() = "underlined"

        @Throws(TexyException::class)
        override fun handle(parser: TexyParser, groups: List<MatchWithOffset>, pattern: RegexpInfo): Node? {

            //    [1] => Heading content.
            //    [2] => .(title)[class]{style}<>
            //    [3] => Underline chars.
            val underChar = groups[3].match!![0]
            var level = 1
            when (underChar) {
                '#' -> level = 0
                '*' -> level = 1
                '=' -> level = 2
                '-' -> level = 3
            }
            val mod = TexyModifier(groups[2].match!!)
            val event = HeadingEvent(parser, groups[1].match!!, mod, level, false)
            return texy.invokeAroundHandlers(event)
        }
    }

    /**
     * Callback for surrounded heading.
     *
     * ### Heading .(title)[class]{style}>
     *
     * @param  TexyBlockParser
     * @param  array      regexp matches
     * @param  string     pattern name
     * @return TexyHtml|string|FALSE
     */
    private val surroundedHeadingPH: PatternHandler = object : PatternHandler {
        override val name: String
            get() = "surrounded"

        @Throws(TexyException::class)
        override fun handle(parser: TexyParser, groups: List<MatchWithOffset>, pattern: RegexpInfo): Node? {
            //    [1] => ###
            //    [2] => Content.
            //    [3] => .(title)[class]{style}<>
            val lineChars = groups!![1]!!.match
            var content = groups[2]!!.match
            val mod = TexyModifier(groups[3]!!.match!!)
            var level = Math.min(7, Math.max(2, lineChars!!.length))
            level = if (opt.moreMeansHigher) 7 - level else level - 2
            content = StringUtils.stripEnd(content, lineChars[0].toString() + " ")
            val event = HeadingEvent(parser, content, mod, level, true)
            return texy.invokeAroundHandlers(event)
        }
    }

    /**
     * Heading listener.
     */
    private val headingListener: HeadingEventListener = object : HeadingEventListener {
        override val eventClass: Class<*>
            get() = HeadingEvent::class.java

        @Throws(TexyException::class)
        override fun onEvent(event: HeadingEvent): Node? {
            var level = event.level + ctx.topLevel
            level = Math.max(1, level)
            level = Math.min(6, level)
            val elm = DOMElement("h$level")
            event.modifier.decorate(elm) // TODO: Doesn't work?

            // Parse the heading content (e.g "New **Java** //library// - `JTexy`").
            TexyLineParser(texy, elm).parse(event.text!!)
            ctx.toc.add(HeadingInfo(elm, level, event.isSurrounded))
            return elm
        }
    }

    /**
     * BeforeParseEvent - reset internal state. TODO: Move elsewhere (some ParsingContext).
     */
    private val beforeParse: BeforeParseListener = object : BeforeParseListener {
        override val eventClass: Class<*>
            get() = BeforeParseEvent::class.java

        @Throws(TexyException::class)
        override fun onEvent(event_: BeforeAfterEvent): Node? {
            val event = event_ as BeforeParseEvent
            ctx.documentTitle = null
            ctx.toc = ArrayList()
            ctx.usedIDs = HashSet()
            return null
        }
    }

    /**
     * AfterParseEvent -
     */
    private val afterParse: AfterParseListener = object : AfterParseListener {
        override val eventClass: Class<*>
            get() = AfterParseEvent::class.java

        @Throws(TexyException::class)
        override fun onEvent(event_: AfterParseEvent): Node? {
            val event = event_ as AfterParseEvent
            if (event.isSingleLine) return null


            // Sesypat underlined levely na hromadu, aby šly po sobě po jedné.
            if (opt.balancing == Balancing.DYNAMIC) {
                balance(ctx.toc, ctx.topLevel)
            }


            // Generate IDs (where not set yet).
            if (opt.generateIDs) {
                var tocUID = 0
                for (item in ctx.toc) {
                    if (item.elm.getAttribute("id").length != 0) continue

                    // TBD: Texy! has toText() here, which would mean to call
                    // ProtectedHTMLWriter.fromElement(item.elm, getTexy().getProtector());
                    // Isn't it too expensive? Is this safe?
                    val titleText = item.elm.textTrim
                    if ("" == titleText) continue
                    item.titleText = titleText

                    // ID HTML attribute.
                    // TODO: unprotectAll(titleText) to get full text.
                    log.finer(Debug.showCodes(titleText))
                    val titleUnprot = event.parser.texy.protector.unprotectAll(titleText)
                    var idAttr = (StringUtils.defaultString(opt.idPrefix)
                            + JTexyStringUtils.webalize(titleUnprot))
                    if (ctx.usedIDs.contains(idAttr)) idAttr += ++tocUID
                    item.id = idAttr
                    item.elm.setAttribute("id", item.id)
                } // for each heading
            } // Generate IDs.


            // Document title. TBD: Move to (future) ParsingContext.
            if (ctx.documentTitle == null && ctx.toc.size != 0) {
                val item = ctx.toc[0]
                ctx.documentTitle = StringUtils.defaultString(item!!.titleText, item.elm.textTrim)
            }
            return null
        } // onEvent()
    } // AfterParseListener afterParse{}

    protected enum class Type {
        UNDERLINED, SURROUNDED
    }

    protected class HeadingInfo {
        var elm: DOMElement
        var level: Int
        var type: Type
        var titleText: String? = null
        var id: String? = null

        constructor(elm: DOMElement, level: Int, type: Type) {
            this.elm = elm
            this.level = level
            this.type = type
        }

        constructor(elm: DOMElement, level: Int, surrounded: Boolean) {
            this.elm = elm
            this.level = level
            type = if (surrounded) Type.SURROUNDED else Type.UNDERLINED
        }
    }

    companion object {
        private val log = Logger.getLogger(HeadingModule::class.java.name)

        /**
         * Balances the levels of underlined headings to make them ascending sequentially.
         */
        private fun balance(toc_: List<HeadingInfo>, topLevel_: Int) {
            var top = topLevel_
            var min = 100
            var max = 0
            val set: SortedSet<Int> = TreeSet()
            var map: IntArray? = null

            // Get the min, max bounds.
            for (headingInfo in toc_) {
                if (headingInfo!!.type == Type.SURROUNDED) {
                    min = Math.min(min, headingInfo.level)
                    top = topLevel_ - min
                } else if (headingInfo.type == Type.UNDERLINED) {
                    set.add(headingInfo.level)
                    max = Math.max(max, headingInfo.level)
                }
            }
            // Beware: For each level used, there must be a match.
            map = IntArray(max + 1)
            var realLevel = 0
            for (level in set) map[level!!] = realLevel++

            // Determine the dynamic level of each heading.
            for (item in toc_) {
                if (item!!.type == Type.SURROUNDED) item.level = item.level + top else if (item.type == Type.UNDERLINED) item.level =
                    map[item.level] + topLevel_
                item.elm.name = "h" + item.level
            }
        } // balance()
    }
}
