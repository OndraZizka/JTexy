package cz.dynawest.jtexy.modules

import cz.dynawest.jtexy.RegexpInfo
import cz.dynawest.jtexy.TexyException
import cz.dynawest.jtexy.events.BeforeParseEvent
import cz.dynawest.jtexy.events.TexyEvent
import cz.dynawest.jtexy.events.TexyEventListener
import cz.dynawest.jtexy.parsers.TexyParser
import cz.dynawest.jtexy.util.MatchWithOffset
import cz.dynawest.jtexy.util.SimpleImageSize
import org.dom4j.Node
import org.dom4j.dom.DOMElement
import java.io.*

/**
 * Emoticon module.
 *
 * @author Ondrej Zizka
 */
class EmoticonModule : TexyModule() {
    private val enabled = false

    /** CSS class for emoticons  */
    private val cssClass: String? = null

    /** Root of relative images (default value is texy.imageModule.root)  */
    private val root: String? = null

    /** Physical location of images on server (default value is texy.imageModule.fileRoot)  */
    private val fileRoot: String? = null
    lateinit var regexpInfo: RegexpInfo

    val x = object : TexyEventListener<TexyEvent> {
        override val eventClass: Class<*> = BeforeParseEvent::class.java

        @Throws(TexyException::class)
        override fun onEvent(event: TexyEvent): Node? {
            if (!enabled) return null
            regexpInfo = createRegexpInfo()
            texy.addPattern(regexpInfo)
            return null
        }
    }

    override val eventListeners =
        // "Register" listeners.
        // BeforeParseEvent. TODO: Move to some void init(JTexy texy) callback.
        arrayOf(x)


    /**
     * Just one pattern handler - emoticon.
     */
    override fun getPatternHandlerByName(name: String): PatternHandler {
        return object : PatternHandler {
            override val name: String
                get() = "emoticon"

            @Throws(TexyException::class)
            override fun handle(parser: TexyParser, groups: List<MatchWithOffset>, pattern: RegexpInfo): Node? {
                val raw = groups[1].match
                return createEmotionElement(findEmoticon(raw), raw)
            }
        }
    }

    /**
     * Finds the emoticon as a prefix.
     */
    private fun findEmoticon(text: String?): String? {
        // For each emoticon...
        for (key in ICONS.keys) {
            if (text!!.startsWith(key!!)) return key
        }
        assert(false)
        return text // Shouldn't happen.
    }

    /**
     * RegexpInfo. Created upon init.
     */
    @Throws(TexyException::class)
    private fun createRegexpInfo(): RegexpInfo {
        if (ICONS.isEmpty()) throw TexyException("List of icons is empty.")
        val regex = StringBuilder("(?<=^|[\\x00-\\x20])(")
        for (emo in ICONS.keys) {
            regex.append(emo).append("+|")
        }
        regex.delete(regex.length - 3, regex.length - 1) // Cut last +|
        regex.append(")")
        return RegexpInfo.Companion.fromRegexp(regex.toString(), RegexpInfo.Type.LINE, "emoticon")
    }

    /**
     * Creates the emoticon element.
     */
    fun createEmotionElement(emoticon: String?, raw: String?): DOMElement {
        val iconFileName = ICONS[emoticon]
        val el = DOMElement("img")
        el.setAttribute("src", File(this.root, iconFileName).path)
        el.setAttribute("alt", raw)
        el.setAttribute("class", cssClass)

        // Actual file - check size.
        val file = File(fileRoot, iconFileName)
        if (file.exists()) try {
            val size = SimpleImageSize(file)
            el.setAttribute("width", "" + size.width)
            el.setAttribute("height", "" + size.height)
        } catch (ex: Exception) {
        }

        //this.jtexy.summary.getImages().add( el.getAttribute("src") );
        return el
    }

    companion object {
        /** Supported emoticons and image files  */
        val ICONS: MutableMap<String, String> = HashMap()

        init {
            ICONS[":-)"] = "smile.gif"
            ICONS[":-("] = "sad.gif"
            ICONS[";-)"] = "wink.gif"
            ICONS[":-D"] = "biggrin.gif"
            ICONS["8-O"] = "eek.gif"
            ICONS["8-)"] = "cool.gif"
            ICONS[":-?"] = "confused.gif"
            ICONS[":-x"] = "mad.gif"
            ICONS[":-P"] = "razz.gif"
            ICONS[":-|"] = "neutral.gif"
        }
    }
}
