package cz.dynawest.jtexy.modules

import cz.dynawest.jtexy.RegexpInfo
import cz.dynawest.jtexy.RegexpPatterns
import cz.dynawest.jtexy.TexyException
import cz.dynawest.jtexy.events.TexyEvent
import cz.dynawest.jtexy.parsers.*
import cz.dynawest.jtexy.util.JTexyStringUtils
import cz.dynawest.jtexy.util.MatchWithOffset
import cz.dynawest.jtexy.util.StringsReplaceCallback
import cz.dynawest.jtexy.util.UrlChecker
import cz.dynawest.openjdkregex.Pattern
import org.apache.commons.lang.StringEscapeUtils
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.math.NumberUtils
import org.dom4j.Node
import org.dom4j.dom.DOMElement
import java.awt.Dimension
import java.io.*
import java.util.*
import java.util.logging.*
import javax.imageio.ImageIO
import javax.imageio.ImageReader

/**
 *
 * @author Ondrej Zizka
 */
class ImageModule : TexyModule() {
    // --- Module meta-info --- //
    override fun getPatternHandlerByName(name: String): PatternHandler? {
        return if (imagePH.name == name) imagePH else null
    }

    override val eventListeners: Array<TexyEventListener<*>>
        get() = arrayOf(beforeParse, imageListener)
    // --- Settings --- //
    /** Prefix to be prepended to all images src="...".  */
    var urlPrefix = ""

    /** Default images alt="...".  */
    var defaultAlt = ""
    var leftClass: String? = null
    var rightClass: String? = null
    var fileRoot: String? = null
    var onLoadJS: String? = null

    /**
     * Image pattern handler -
     * [* small.jpg 80x13 | small-over.jpg | big.jpg .(alternative text)[class]{style}>]:LINK.
     */
    protected var imagePH: PatternHandler = object : PatternHandler {
        override val name: String
            get() = "imagePattern"

        @Throws(TexyException::class)
        override fun handle(parser: TexyParser, groups: List<MatchWithOffset?>?, pattern: RegexpInfo?): Node? {
            //    [1] => URLs
            //    [2] => .(title)[class]{style}<>
            //    [3] => * < >
            //    [4] => url | [ref] | [*image*]
            for (match in groups!!) {
                log.finest("  " + match.toString()) ///
            }
            val urls = groups[1]!!.match!!.trim { it <= ' ' }
            val align = groups[3]!!.match
            val modStr = StringUtils.defaultString(groups[2]!!.match) + align!!.trim { it <= ' ' }
            val mod = TexyModifier(modStr)
            val linkStr = groups[4]!!.match
            val img = createImage(urls) ?: return null
            var link: TexyLink? = null
            if ("" != linkStr) {
                if (":" == linkStr) {
                    link = TexyLink(if (img.linkedUrl != null) img.linkedUrl else img.url)
                    link.raw = ":"
                    link.type = TexyLink.Type.IMAGE
                } else {
                    // @ Fire a processLinkEvent instead of calling LinkModule directly.
                    val linkEvent = LinkProcessEvent(
                        parser, mod, modStr, urls, null, LINK_PROVIDER
                    )
                    parser.texy.invokeAroundHandlers(linkEvent)
                    link = linkEvent.getLink()
                }
            }
            val ev = ImageEvent(parser, img, link, mod)
            return parser.texy.invokeAroundHandlers(ev)
        }
    }

    /**
     * BeforeParseListener
     */
    val beforeParse: BeforeParseListener = object : BeforeParseListener {
        override val eventClass: Class<*>
            get() = BeforeParseEvent::class.java

        @Throws(TexyException::class)
        override fun onEvent(event_: TexyEvent): Node? {
            val event = event_ as BeforeParseEvent

            // [*image*]: urls .(title)[class]{style}
            /*$text = preg_replace_callback(
                '#^\[\*([^\n]+)\*\]:\ +(.+)\ *'.TEXY_MODIFIER.'?\s*()$#mUu',
                array($this, 'patternReferenceDef'),
                $text
            );*/

            // Since patternReferenceDef() returns "", callback is not necessary,
            // but once written this way...
            val REGEX = "(?mUu)^\\[\\*([^\\n]+)\\*\\]:\\ +(.+)\\ *" + RegexpPatterns.Companion.TEXY_MODIFIER + "?\\s*()$"
            val cbProcessReference = StringsReplaceCallback { groups ->
                patternReferenceDef(groups) // Needs $this.
            }
            val res = JTexyStringUtils.replaceWithCallback(event.text, REGEX, cbProcessReference)
            event.text = res
            return null
        } //@Override public String beforeParse(JTexy texy, String text, boolean singleLine) {}
    }

    /**
     * ImageEventListener
     */
    var imageListener: ImageEventListener = object : ImageEventListener {
        override val eventClass: Class<*>
            get() = ImageEvent::class.java

        @Throws(TexyException::class)
        override fun onEvent(event: ImageEvent): Node? {
            val img = event.img
            val mod = img!!.modifier
            val alt = mod.title
            mod.title = null
            val hAlign = mod.hAlign
            mod.hAlign = null
            val elm = DOMElement("img")
            mod.decorate(elm)
            elm.setAttribute("src", JTexyStringUtils.prependUrlPrefix(urlPrefix, img.url))

            // alt
            if (null == elm.getAttributeNode("alt")) {
                elm.setAttribute("alt", defaultAlt)
            }

            // hAlign
            if (null != hAlign) {
                var cls: String? = null
                if ("left" == hAlign) {
                    cls = leftClass
                } else if ("left" == hAlign) {
                    cls = rightClass
                }
                if (cls != null) elm.setAttribute("class", elm.getAttribute("class") + " " + cls)
            }


            // Width and height.

            // org.apache.poi.ss.util.ImageUtils ?
            // org.apache.sanselan.ImageParser ?
            val imgPath = fileRoot + img.url
            val imgFile = File(imgPath)
            if (img.width == null && img.height == null) if (imgFile.isFile) {
                // TBD: Compute the dimensions if only one set.
                log.finest("Reading image from: $imgPath")
                val imgDim = getImageSizeB(imgPath)
                img.width = imgDim!!.width
                img.height = imgDim.height
                val ratio = (img.width!! / img.height!!).toDouble()
            }
            if (null != img.width) elm.setAttribute("width", "" + img.width)
            if (null != img.height) elm.setAttribute("height", "" + img.height)

            // onmouseover 
            if (img.overUrl != null) {
                // TBD: onmoouseover
                val overSrc = JTexyStringUtils.prependUrlPrefix(urlPrefix, img.overUrl)
                elm.setAttribute("onmouseover", "this.src='" + StringEscapeUtils.escapeJavaScript(overSrc) + "'")
                elm.setAttribute("onmouseout", "this.src='" + StringEscapeUtils.escapeJavaScript(elm.getAttribute("src")) + "'")
                if (onLoadJS != null) elm.setAttribute("onload", onLoadJS!!.replace("%i", overSrc!!))
                // TBD: $tx->summary['preload'][] = $overSrc;
            }
            allDocumentImages.add(img)

            // TBD: if ($link) return $tx->linkModule->solve(NULL, $link, $el);
            if (event.link != null) {
                // TODO: Add the additional element to the LinkEvent.
                // TBD:  Extract some method to avoid firing an event?
                //return getTexy().invokeAroundHandlers( new LinkModule.LinkEvent(null, event.link, elm))
                log.warning("Image link feature not implemented yet.")
            }
            return elm
        }
    } // imageListener

    // TODO: Reset in BeforeParseEventListener.
    // TBD:  Refactor to have this in some ParsingContext or DocumentContext.
    private val allDocumentImages: MutableList<ImageInfo?> = ArrayList<Any?>()

    // References - predefined images. //
    // References map 
    private val refs: MutableMap<String?, ImageInfo?> = HashMap<Any?, Any?>()
    protected fun setRef(key: String?, img: ImageInfo?) {
        refs[key!!.lowercase(Locale.getDefault())] = img
    }

    protected fun getRef(key: String?): ImageInfo? {
        val img = refs[key!!.lowercase(Locale.getDefault())] ?: return null
        return img.clone() // Cloning because we will change it.
    }

    /**  Returns reference using this module's link reference map.  */
    protected val LINK_PROVIDER = LinkProvider { key ->
        val img = getRef(key) ?: return@LinkProvider null
        val link = TexyLink(if (img.linkedUrl == null) img.url else img.linkedUrl)
        link.modifier = img.modifier
        link
    }

    /**
     * Converts image reference string, using module's reference definitions.
     * Used as a callback in beforeParse.
     * Callback for: [*image*]: urls .(title)[class]{style}.
     */
    private fun patternReferenceDef(groups: Array<String?>): String {
        //    [1] => [* (reference) *]
        //    [2] => urls
        //    [3] => .(title)[class]{style}<>
        val ref = if (groups.size > 1) groups[1] else null
        val urls = if (groups.size > 2) groups[2] else null
        val modStr = if (groups.size > 3) groups[3] else null
        val mod = TexyModifier(modStr)
        var img = getRef(ref)
        if (null == img) img = createImage(ref)
        img!!.modifier = mod
        setRef(ref, img)
        return ""
    }

    /** Image information.  */
    class ImageInfo : Cloneable {
        var url: String? = null
        var overUrl: String? = null
        var linkedUrl: String? = null
        var asMax = false
        var width: Int? = null
        var height: Int? = null
        var modifier = TexyModifier()
        protected var refName: String? = null

        constructor()
        constructor(url: String?, asMax: Boolean, width: Int?, height: Int?) {
            this.url = url
            this.asMax = asMax
            this.width = width
            this.height = height
        }

        public override fun clone(): ImageInfo {
            return ImageInfo(url, asMax, width, height)
        }
    }

    companion object {
        private val log = Logger.getLogger(ImageModule::class.java.name)

        /** Reads image's size.  */
        private fun getImageSize(filePath: String): Dimension? {
            if (log.isLoggable(Level.FINE)) log.fine("Reading image: $filePath")
            return try {
                val file = File(filePath)
                val img = ImageIO.read(file) ?: return null
                Dimension(img.width, img.height)
            } catch (ex: IOException) {
                null
            }
        }

        /** Reads image's size. Might be faster.  */
        private fun getImageSizeB(filePath: String): Dimension? {
            val file = File(filePath)
            if (!file.exists()) return null
            if (log.isLoggable(Level.FINE)) log.fine("Reading image: $filePath")
            return try {
                val `in` = ImageIO.createImageInputStream(file)
                try {
                    val readers: Iterator<*> = ImageIO.getImageReaders(`in`)
                    if (!readers.hasNext()) return null
                    val reader = readers.next() as ImageReader
                    try {
                        reader.input = `in`
                        Dimension(reader.getWidth(0), reader.getHeight(0))
                    } finally {
                        reader.dispose()
                    }
                } finally {
                    `in`?.close()
                }
            } catch (ex: Throwable) {
                null
            }
        }

        /**
         * Image information.  Texy: part of factoryImage().
         */
        fun createImage(def: String?): ImageInfo? {
            /* Moved elsewhere to allow this method to be static.
        ImageInfo img = getRef(def);
        if( null != img )
            return img;
         */
            if (StringUtils.isBlank(def)) return null
            val parts = StringUtils.split(def, '|')
            val img = ImageInfo()

            // Dimensions?     "bla/bla.png 50x50"
            val pat: Pattern = Pattern.Companion.compile("(?U)^(.*) (\\d+|\\?) *(X|x) *(\\d+|\\?) *()$")
            val mat = pat.matcher(parts[0])
            if (mat!!.matches()) {
                img.url = mat.group(1)
                img.asMax = "X" == mat.group(3)
                var i = mat.group(2)
                img.width = if ("?" == i) null else NumberUtils.createInteger(i)
                i = mat.group(4)
                img.height = if ("?" == i) null else NumberUtils.createInteger(i)
            } else {
                img.url = def!!.trim { it <= ' ' }
            }
            if (!UrlChecker.checkURL(img.url, UrlChecker.Type.IMAGE)) img.url = null

            // onmouseover image
            if (parts.size > 1) {
                val tmp = parts[1].trim { it <= ' ' }
                if (tmp != "" && UrlChecker.checkURL(tmp, UrlChecker.Type.ANCHOR)) img.overUrl = tmp
            }

            // linked image
            if (parts.size > 2) {
                val tmp = parts[2].trim { it <= ' ' }
                if (tmp != "" && UrlChecker.checkURL(tmp, UrlChecker.Type.ANCHOR)) img.linkedUrl = tmp
            }
            return img
        }
    }
}
