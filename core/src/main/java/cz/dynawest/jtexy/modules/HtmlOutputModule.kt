package cz.dynawest.jtexy.modules

import cz.dynawest.jtexy.TexyException
import cz.dynawest.jtexy.dtd.Dtd
import cz.dynawest.jtexy.dtd.DtdElement
import cz.dynawest.jtexy.dtd.HtmlDtdTemplate
import cz.dynawest.jtexy.events.PostProcessEvent
import cz.dynawest.jtexy.events.TexyEventListener
import cz.dynawest.jtexy.util.JTexyStringUtils
import cz.dynawest.openjdkregex.Matcher
import cz.dynawest.openjdkregex.Pattern
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.WordUtils
import org.dom4j.Node
import org.dom4j.dom.DOMText
import java.util.*
import java.util.logging.*

/**
 * Fix HTML structure, force conformance to DTD.
 *
 * @author Ondrej Zizka
 */
class HtmlOutputModule : TexyModule() {
    override val eventListeners: Array<TexyEventListener<*>>
        // -- Module meta-info -- //
        get() = arrayOf(postProcessListener)

    override fun getPatternHandlerByName(name: String): PatternHandler? {
        return null
    }

    protected override val propsFilePath: String?
        protected get() = null // No props file.
    // -- Config --
    /** Indent HTML code?  */
    var indent = true

    /** Base indent level.  */
    var baseIndent = 0

    /** Wrap width, doesn't include indent space.  */
    var lineWrapWidth = 80

    /** Remove optional HTML end tags?  */
    var removeOptional = false

    /** Output XML?  */
    private var isXml = false
    // -- Context --
    /** Indent space counter.  */
    private var space = 0

    /**   */
    private lateinit var tagUsedCount: CounterMap

    /**   */
    private lateinit var tagStack: Stack<StackItem>

    /** Content DTD used, when context is not defined.  */
    private lateinit var baseDTD: DtdElement
    private val htmlDTD = HtmlDtdTemplate()

    /**
     * TODO.
     *
     * Converts *** ... *** ... .
     * into     *** ... **** ... *.
     * And other neat tricks.
     */
    private val postProcessListener: TexyEventListener<PostProcessEvent> = object : TexyEventListener<PostProcessEvent> {
        override val eventClass: Class<*>
            get() = PostProcessEvent::class.java

        @Throws(TexyException::class)
        override fun onEvent(event: PostProcessEvent): Node? {
            return DOMText(reset().postProcess(event.text))
        }
    }

    /**
     * Reset for the new invocation.
     */
    private fun reset(): HtmlOutputModule {
        space = baseIndent
        tagStack = object : Stack<StackItem>() {
            @Synchronized
            override fun peek() = if (this.isEmpty()) null else super.peek()
        }
        tagUsedCount = CounterMap()
        isXml = false //texy.getOutputMode() & Constants.XML;

        // Special "base content" - Whatever can be in <div>, <html>, <body>. Plus <html> itself.
        val baseDTD = DtdElement("root")
        baseDTD.addAll(htmlDTD.rootDtdElement.getElement("div")!!.getElements())
        baseDTD.addAll(htmlDTD.rootDtdElement.getElement("html")!!.getElements())
        //this.baseDTD.addAll( htmlDTD.getDtd().getElement("head").getElements() );
        baseDTD.addAll(htmlDTD.rootDtdElement.getElement("body")!!.getElements())
        baseDTD.add(htmlDTD.rootDtdElement.getElement("html")!!)
        this.baseDTD = baseDTD

        return this
    }

    /**
     * TODO.
     *
     * Converts *** ... *** ... .
     * into     *** ... **** ... *.
     * And other neat tricks.
     */
    private fun postProcess(str: String?): String {
        var str = str
        reset()

        // Wellform and reformat each element.
        /* @
		str = preg_replace_callback(
			'#(.*)<(?:(!--.*--)|(/?)([a-z][a-z0-9._:-]*)(|[ \n].*)\s*(/?))>()#Uis',
			array($this, 'cb'),
			str . '</end/>'
		);*/run {
            val sb = StringBuffer(str!!.length * 12 / 10)
            val pat: Pattern = Pattern.compile("(?Uis)(.*)<(?:(!--.*--)|(/?)([a-z][a-z0-9._:-]*)(|[ \\n].*)\\s*(/?))>()")
            val mat = pat.matcher(str)
            while (mat!!.find()) {
                val replacement = callback(mat, this.htmlDTD.dtd)
                mat.appendReplacement(sb, replacement)
            }
        }


        // Empty the stack.
        for (elm in tagStack) str += "</" + elm!!.tag + ">" //["close"];

        // Right trim.
        str = str!!.replace("[\t ]+(\n|\r|$)".toRegex(), "$1")

        // Join double \r to single \n.
        str = str.replace("\r\r", "\n")
        str = str.replace('\r', '\n')

        // Greedy chars.
        str = str.replace("\u0007 *".toRegex(), "")
        // Back-tabs.
        str = str.replace("\\t? *\u0008".toRegex(), "")

        // Line wrap.
        if (lineWrapWidth > 0) {
            // @ str = preg_replace_callback( "#^(\t*)(.*)$#m",  array($this, "wrap"), str );
            val pat: Pattern = Pattern.compile("(?m)^(\\t*)(.*)$")
            val mat = pat.matcher(str)
            while (mat!!.find()) {
            }
            // TODO
        }

        // Remove HTML 4.01 optional end tags.
        if (!isXml && removeOptional) {
            str = str.replace("(?u)\\s*</(colgroup|dd|dt|li|option|p|td|tfoot|th|thead|tr)>".toRegex(), "")
        }
        return str
    }

    internal class StackItem(var tag: String, var open: String?, var close: String?, var dtdContent: Set<DtdElement>, var indent: Int) {
        override fun toString(): String {
            return "StackItem{ tag=" + tag + ", indent=" + indent + ", open=" + open + ", close=" + close + ", dtdContent{" + dtdContent!!.size + "}}"
        }
    }

    internal class CounterMap {
        var map: MutableMap<String, Int> = HashMap()
        operator fun get(key: String): Int? {
            return map[key]
        }

        fun used(key: String): Boolean {
            return this[key, 0] != 0
        }

        fun notUsed(key: String): Boolean {
            return this[key, 0] == 0
        }

        operator fun get(key: String, def: Int): Int {
            val `value` = map[key]
            return `value` ?: def
        }

        fun increment(key: String): Int {
            return adjust(key, 1)
        }

        fun decrement(key: String): Int {
            return adjust(key, -1)
        }

        fun adjust(key: String, delta: Int): Int {
            var `value` = map[key]
            if (`value` == null) `value` = delta else `value` += delta
            map[key] = `value`
            return `value`
        }
    }

    /**
     * Callback function: <tag> | </tag> | ....
     *
     * TODO: Change string to StringBuilder.
     */
    private fun callback(matcher: Matcher, dtd: Dtd): String? {
        // html tag
        // list(, mText, $mComment, $mEnd, $mTag, $mAttr, $mEmpty) = $matches;
        //    [1] => text
        //    [1] => !-- comment --
        //    [2] => /
        //    [3] => TAG
        //    [4] => ... (attributes)
        //    [5] => /   (empty)
        val mText = matcher.group(1)!!
        val mComment = matcher.group(2)
        val mEnd = matcher.group(3)
        val mTag = matcher.group(4)!!
        var mAttr = matcher.group(5)
        val mEmpty = matcher.group(6)
        val bEndTag = "/" == mEnd
        var str: String? = ""

        // Phase #1 - stuff between tags.
        if (mText.isNotEmpty()) {
            val item = tagStack.peek() // @ reset()
            var elm: DtdElement
            // Text not allowed?


            if (item != null) {
                val elm = dtd[item.tag]
                if (elm  != null && elm.getElement("%DATA") != null)
                    true
                // TODO: This was a really weird construct from the Kotlin converter. I guess the str should stay intact?
            }
            else if (tagUsedCount["pre", 0] != 0 || tagUsedCount["textarea", 0] != 0 || tagUsedCount["script", 0] != 0)
                str = JTexyStringUtils.freezeSpaces(mText)
            else
                str = mText.replace("[ \n]+".toRegex(), " ")
        }


        // Phase #2 - HTML comment.
        if (StringUtils.isNotEmpty(mComment)) return str + "<" + JTexyStringUtils.freezeSpaces(mComment!!) + ">"


        // Phase #3 - HTML tag.    // (empty means contains "/")
        val bEmpty = !mEmpty!!.isEmpty() || dtd.contains(mTag) && dtd[mTag]!!.hasNoChildren()
        if (bEmpty && bEndTag) return str // Wrong tag: </tag/>
        if (bEndTag) {  // End tag.

            // Has a start tag?
            if (0 == tagUsedCount[mTag, 0]) // @ empty(this.tagUsed[$mTag])
                return str

            // Autoclose the tags.
            val tmpStack: Stack<StackItem> = Stack()
            var back = true
            val it = tagStack.iterator()
            while (it.hasNext()) {
                val item = it.next()
                val stackTag = item!!.tag
                str += item.close
                space -= item.indent
                tagUsedCount.decrement(stackTag)
                back = back && htmlDTD.inlineElements.contains(DtdElement(stackTag)) // isset(TexyHtml::$inlineElements[$tag]);
                it.remove() // Otherwise ConcurrentModificationException. //this.tagStack.remove(item); // unset(this.tagStack[$i]);
                if (mTag == stackTag) break
                tmpStack.push(item) // array_unshift($tmp, $item);
            }

            //@ if (!$back || !$tmp) return $s;
            if (!back || tmpStack.isEmpty()) return str

            // Allowed-check (nejspis neni ani potreba)
            run {
                val item = this.tagStack.peek()
                val dtdContent = if (item != null) item.dtdContent else this.baseDTD.getElements()
                if (!dtdContent.contains(DtdElement(tmpStack.firstElement()!!.tag))) return str // TODO:  Change dtdContent to DtdElement.
            }

            // Autoopen tags.
            for (item in tmpStack) {
                str += item!!.open
                space += item.indent
                tagUsedCount.increment(item.tag)
                tagStack.push(item) // array_unshift(this.tagStack, $item);
            }
        } else { // start tag
            var dtdContent = baseDTD.getElements()
            var allowed: Boolean

            // Unknown (non-html) tag.
            if (!htmlDTD.dtd.contains(mTag)) {
                allowed = true
                val item = tagStack.peek() // @ reset()
                if (item != null) dtdContent = item.dtdContent
            } else {
                // Optional end tag closing.
                //for( StackItem item : this.tagStack )  // $i => $item
                val it = tagStack.iterator()
                while (it.hasNext()) {
                    val item = it.next()

                    // Is tag allowed here?
                    dtdContent = item!!.dtdContent
                    if (dtdContent.contains(DtdElement(mTag))) break
                    val tag = item.tag
                    // Auto-close hidden, optional and inline tags.
                    val tagDtd = DtdElement(tag)
                    if (null != item.close
                        && !htmlDTD.getOptionalEndElements().contains(tagDtd)
                        && !htmlDTD.inlineElements.contains(tagDtd)
                    ) break

                    // Close it.
                    str += item.close
                    space -= item.indent
                    tagUsedCount.decrement(tag)
                    //this.tagStack.remove(item); // unset(this.tagStack[$i]);
                    it.remove()
                    dtdContent = baseDTD.getElements()
                }

                // Is tag allowed in this content?
                allowed = dtdContent.contains(DtdElement(mTag))

                // Check deep element prohibitions.
                if (allowed) {
                    val prohibs = htmlDTD.dtd.getProbibitionsOf(DtdElement(mTag))
                    if (prohibs != null) for (dtdElement in prohibs) {
                        if (0 == tagUsedCount[dtdElement.name, 0]) {
                            allowed = false
                            break
                        }
                    }
                }
            } // else (is an HTML element)


            // Empty elements se neukladaji do zasobniku.
            if (!mEmpty.isEmpty()) {
                if (!allowed) return str
                if (isXml) mAttr += " /"
                val indent_ = indent && tagUsedCount.notUsed("pre") && tagUsedCount.notUsed("textarea")
                val len = str!!.length + mTag.length + mAttr!!.length + 5 + Math.max(
                    0,
                    space
                ) // max() is a quick fix, indentation still broken.
                if (indent && "br" == mTag) {
                    // Formatting exception
                    val sb = StringBuilder(len)
                    sb.append(str.replaceFirst("\\s+$".toRegex(), ""))
                    sb.append('<').append(mTag).append(mAttr).append(">\n")
                    for (i in 0 until space) sb.append("\t")
                    return sb.append("\u0007").toString()
                }
                if (indent && !htmlDTD.inlineElements.contains(DtdElement(mTag))) {
                    val sb = StringBuilder(len).append("\r")
                    sb.append(str)
                    for (i in 0..space) sb.append("\t")
                    sb.append('<').append(mTag).append(mAttr).append('>')
                    for (i in 0..space) sb.append("\t")
                    return sb.toString()
                }
                return StringBuilder(len).append(str).append('<').append(mTag).append(mAttr).append('>').toString()
            }
            var open: String? = null
            var close: String? = null
            var indent_ = 0

            /* // @ Commented out in Texy
			if( !isset(TexyHtml::$inlineElements[$mTag])) {
				// block tags always decorate with \n
				str += "\n";
				$close = "\n";
			}
			*/
            if (allowed) {
                open = StringBuilder(2 + mTag.length + mAttr!!.length).append('<').append(mTag).append(mAttr).append('>').toString()

                // Receive new content (ins & del are special cases). TBD: Move the exception to DTD?
                val elDtd = htmlDTD.dtd[mTag]
                if (elDtd!!.hasChildren() && "ins" != mTag && "del" != mTag) dtdContent = elDtd.getElements()

                // Format output (if indent is enabled and it's not inline element).
                if (indent && !htmlDTD.inlineElements.contains(DtdElement(mTag))) {
                    // Create indented close tag for this element.
                    close = StringBuilder(6 + mTag.length + space)
                        .append("\u0008</").append(mTag).append(">\n").append(StringUtils.repeat("\t", space)).toString()
                    // Print indented open tag.
                    str += StringBuilder(3 + open.length + space)
                        .append('\n').append(StringUtils.repeat("\t", space++)).append(open).append("\u0007").toString()
                    indent_ = 1
                } else {
                    // Create plain close tag.
                    close = StringBuilder(3 + mTag.length).append("</").append(mTag).append('>').toString() //"</mTag>";
                    // Print plain open tag.
                    str += open
                }

                // TODO: problematic formatting of select / options, object / params
            }


            // Open tag, put to stack, increase counter
            val item = StackItem(mTag, open, close, dtdContent, indent_)
            tagStack.push(item) // array_unshift()
            tagUsedCount.increment(mTag)
        }
        return str
    } // callback()

    /**
     * Callback function: wrap lines.
     */
    private fun wrap(space: String, str: String): String {
        return space + WordUtils.wrap(
            str, lineWrapWidth, """
     
     $space
     """.trimIndent(), false
        )
    }

    companion object {
        private val log = Logger.getLogger(HtmlOutputModule::class.java.name)
    }
}
