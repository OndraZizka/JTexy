/* Copied from Dom4j. */
package cz.dynawest.jtexy.dom4j.io

import org.dom4j.*
import org.dom4j.io.OutputFormat
import org.xml.sax.SAXException
import java.io.*
import java.util.*

/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */ /**
 *
 *
 * `HTMLWriter` takes a DOM4J tree and formats it to a stream as
 * HTML. This formatter is similar to XMLWriter but it outputs the text of CDATA
 * and Entity sections rather than the serialised format as in XML, it has an
 * XHTML mode, it retains whitespace in certain elements such as &lt;PRE&gt;,
 * and it supports certain elements which have no corresponding close tag such
 * as for &lt;BR&gt; and &lt;P&gt;.
 *
 *
 *
 *
 * The OutputFormat passed in to the constructor is checked for isXHTML() and
 * isExpandEmptyElements(). See [OutputFormat]for details.
 * Here are the rules for **this class ** based on an OutputFormat, "format",
 * passed in to the constructor: <br></br><br></br>
 *
 *
 *  * If an element is in [ getOmitElementCloseSet][.getOmitElementCloseSet], then it is treated specially:
 *
 *
 *  * It never expands, since some browsers treat this as two separate
 * Horizontal Rules: &lt;HR&gt;&lt;/HR&gt;
 *  * If [format.isXHTML()][org.dom4j.io.OutputFormat.isXHTML], then
 * it has a space before the closing single-tag slash, since Netscape 4.x-
 * treats this: &lt;HR /&gt; as an element named "HR" with an attribute named
 * "/", but that's better than when it refuses to recognize this: &lt;hr/&gt;
 * which it thinks is an element named "HR/".
 *
 *
 *
 *  * If [format.isXHTML()][org.dom4j.io.OutputFormat.isXHTML], all
 * elements must have either a close element, or be a closed single tag.
 *  * If [ format.isExpandEmptyElements()][org.dom4j.io.OutputFormat.isExpandEmptyElements]() is true, all elements are expanded except
 * as above.
 *
 *
 * **Examples **
 *
 *
 *
 *
 *
 *
 *
 *
 * If isXHTML == true, CDATA sections look like this:
 *
 * <PRE>
 *
 * **&lt;myelement&gt;&lt;![CDATA[My data]]&gt;&lt;/myelement&gt; **
 *
</PRE> *
 *
 * Otherwise, they look like this:
 *
 * <PRE>
 *
 * **&lt;myelement&gt;My data&lt;/myelement&gt; **
 *
</PRE> *
 *
 *
 *
 *
 *
 * Basically, [OutputFormat.isXHTML()][] ==
 * `true` will produce valid XML, while [ ][org.dom4j.io.OutputFormat.isExpandEmptyElements] determines whether empty elements are
 * expanded if isXHTML is true, excepting the special HTML single tags.
 *
 *
 *
 *
 * Also, HTMLWriter handles tags whose contents should be preformatted, that is,
 * whitespace-preserved. By default, this set includes the tags &lt;PRE&gt;,
 * &lt;SCRIPT&gt;, &lt;STYLE&gt;, and &lt;TEXTAREA&gt;, case insensitively. It
 * does not include &lt;IFRAME&gt;. Other tags, such as &lt;CODE&gt;,
 * &lt;KBD&gt;, &lt;TT&gt;, &lt;VAR&gt;, are usually rendered in a different
 * font in most browsers, but don't preserve whitespace, so they also don't
 * appear in the default list. HTML Comments are always whitespace-preserved.
 * However, the parser you use may store comments with linefeed-only text nodes
 * (\n) even if your platform uses another line.separator character, and
 * HTMLWriter outputs Comment nodes exactly as the DOM is set up by the parser.
 * See examples and discussion here: {@link#setPreformattedTags(java.util.Set)
 * * setPreformattedTags}
 *
 *
 *
 *
 * **Examples **
 *
 * <blockquote>
 *
 *
 * **Pretty Printing **
 *
 *
 *
 *
 * This example shows how to pretty print a string containing a valid HTML
 * document to a string. You can also just call the static methods of this
 * class: <br></br>
 * [prettyPrintHTML(String)][.prettyPrintHTML]or <br></br>
 * [ prettyPrintHTML(String,boolean,boolean,boolean,boolean)][.prettyPrintHTML] or, <br></br>
 * [prettyPrintXHTML(String)][.prettyPrintXHTML]for XHTML (note
 * the X)
 *
 *
 * <pre>
 * String testPrettyPrint(String html) {
 * StringWriter sw = new StringWriter();
 * OutputFormat format = OutputFormat.createPrettyPrint();
 * // These are the default values for createPrettyPrint,
 * // so you needn't set them:
 * // format.setNewlines(true);
 * // format.setTrimText(true);&lt;/font&gt;
 * format.setXHTML(true);
 * HTMLWriter writer = new HTMLWriter(sw, format);
 * Document document = DocumentHelper.parseText(html);
 * writer.write(document);
 * writer.flush();
 * return sw.toString();
 * }
</pre> *
 *
 *
 *
 * This example shows how to create a "squeezed" document, but one that will
 * work in browsers even if the browser line length is limited. No newlines are
 * included, no extra whitespace at all, except where it it required by
 * [setPreformattedTags][.setPreformattedTags].
 *
 *
 * <pre>
 * String testCrunch(String html) {
 * StringWriter sw = new StringWriter();
 * OutputFormat format = OutputFormat.createPrettyPrint();
 * format.setNewlines(false);
 * format.setTrimText(true);
 * format.setIndent(&quot;&quot;);
 * format.setXHTML(true);
 * format.setExpandEmptyElements(false);
 * format.setNewLineAfterNTags(20);
 * org.dom4j.io.HTMLWriter writer = new HTMLWriter(sw, format);
 * org.dom4j.Document document = DocumentHelper.parseText(html);
 * writer.write(document);
 * writer.flush();
 * return sw.toString();
 * }
</pre> *
 *
</blockquote> *
 *
 * @author [James Strachan ](mailto:james.strachan@metastuff.com)
 * @author Laramie Crocker
 * @version $Revision: 1.21 $
 */
class HTMLWriter : XMLWriter {
    private val formatStack: Stack<FormatState> = Stack()
    private var lastText = ""
    private var tagsOuput = 0

    // legal values are 0+, but -1 signifies lazy initialization.
    private var newLineAfterNTags = -1
    private var preformattedTags = DEFAULT_PREFORMATTED_TAGS

    /**
     * The qualified element names which should have no close element tag.
     */
    private var omitElementCloseSet: MutableSet<String> = mutableSetOf()

    constructor(writer: Writer) : super(writer, DEFAULT_HTML_FORMAT)
    constructor(writer: Writer, format: OutputFormat) : super(writer, format)
    constructor() : super(DEFAULT_HTML_FORMAT)
    constructor(format: OutputFormat) : super(format)
    constructor(out: OutputStream) : super(out, DEFAULT_HTML_FORMAT)
    constructor(out: OutputStream, format: OutputFormat) : super(out, format)

    @Throws(SAXException::class)
    override fun startCDATA() {
    }

    @Throws(SAXException::class)
    override fun endCDATA() {
    }

    // Overloaded methods
    // added isXHTML() stuff so you get the CDATA brackets if you desire.
    @Throws(IOException::class)
    override fun writeCDATA(text: String?) {
        // XXX: Should we escape entities?
        // writer.write( escapeElementEntities( text ) );
        if (outputFormat.isXHTML) {
            super.writeCDATA(text)
        } else {
            writer.write(text)
        }
        lastOutputNodeType = Node.CDATA_SECTION_NODE.toInt()
    }

    @Throws(IOException::class)
    override fun writeEntity(entity: Entity) {
        writer.write(entity.text)
        lastOutputNodeType = Node.ENTITY_REFERENCE_NODE.toInt()
    }

    @Throws(IOException::class)
    override fun writeDeclaration() {
    }

    @Throws(IOException::class)
    override fun writeString(text: String) {
        /*
         * DOM stores \n at the end of text nodes that are newlines. This is
         * significant if we are in a PRE section. However, we only want to
         * output the system line.separator, not \n. This is a little brittle,
         * but this function appears to be called with these lineseparators as a
         * separate TEXT_NODE. If we are in a preformatted section, output the
         * right line.separator, otherwise ditch. If the single \n character is
         * not the text, then do the super thing to output the text.
         *
         * Also, we store the last text that was not a \n since it may be used
         * by writeElement in this class to line up preformatted tags.
         */
        if (text == "\n") {
            if (!formatStack.empty()) {
                super.writeString(lineSeparator)
            }
            return
        }
        lastText = text
        if (formatStack.empty()) {
            super.writeString(text.trim { it <= ' ' })
        } else {
            super.writeString(text)
        }
    }

    /**
     * Overriden method to not close certain element names to avoid wierd
     * behaviour from browsers for versions up to 5.x
     *
     * @param qualifiedName
     * DOCUMENT ME!
     *
     * @throws IOException
     * DOCUMENT ME!
     */
    @Throws(IOException::class)
    override fun writeClose(qualifiedName: String) {
        if (!omitElementClose(qualifiedName)) {
            super.writeClose(qualifiedName)
        }
    }

    @Throws(IOException::class)
    override fun writeEmptyElementClose(qualifiedName: String) {
        if (outputFormat.isXHTML) {
            // xhtml, always check with format object whether to expand or not.
            if (omitElementClose(qualifiedName)) {
                // it was a special omit tag, do it the XHTML way: "<br/>",
                // ignoring the expansion option, since <br></br> is OK XML,
                // but produces twice the linefeeds desired in the browser.
                // for netscape 4.7, though all are fine with it, write a space
                // before the close slash.
                writer.write(" />")
            } else {
                super.writeEmptyElementClose(qualifiedName)
            }
        } else {
            // html, not xhtml
            if (omitElementClose(qualifiedName)) {
                // it was a special omit tag, do it the old html way: "<br>".
                writer.write(">")
            } else {
                // it was NOT a special omit tag, check with format object
                // whether to expand or not.
                super.writeEmptyElementClose(qualifiedName)
            }
        }
    }

    protected fun omitElementClose(qualifiedName: String): Boolean {
        return internalGetOmitElementCloseSet().contains(
            qualifiedName.uppercase(Locale.getDefault())
        )
    }

    private fun internalGetOmitElementCloseSet(): Set<String> {
        if (omitElementCloseSet == null) {
            omitElementCloseSet = HashSet()
            loadOmitElementCloseSet(omitElementCloseSet)
        }
        return omitElementCloseSet
    }

    // If you change this, change the javadoc for getOmitElementCloseSet.
    protected fun loadOmitElementCloseSet(set: MutableSet<String>) {
        set.add("AREA")
        set.add("BASE")
        set.add("BR")
        set.add("COL")
        set.add("HR")
        set.add("IMG")
        set.add("INPUT")
        set.add("LINK")
        set.add("META")
        set.add("P")
        set.add("PARAM")
    }
    // let the people see the set, but not modify it.

    /**
     * A clone of the Set of elements that can have their close-tags omitted.
     * By default, it should be "AREA", "BASE", "BR", "COL", "HR", "IMG", "INPUT", "LINK", "META", "P", "PARAM"
     */
    fun getOmitElementCloseSet(): Set<String> {
        return internalGetOmitElementCloseSet()
    }

    /**
     * To use the empty set, pass an empty Set, or null:
     *
     * <pre>
     *   setOmitElementCloseSet(new HashSet());
     *     or
     *   setOmitElementCloseSet(null);
     * </pre>
     */
    fun setOmitElementCloseSet(newSet: Set<String>?) {
        // resets, and safely empties it out if newSet is null.
        omitElementCloseSet = HashSet()
        if (newSet == null) return

        omitElementCloseSet = HashSet()
        var aTag: Any
        val iter = newSet.iterator()
        while (iter.hasNext()) {
            aTag = iter.next()
            if (aTag != null) {
                omitElementCloseSet.add(aTag.toString().uppercase(Locale.getDefault()))
            }
        }
    }

    /**
     * @see .setPreformattedTags
     */
    fun getPreformattedTags(): Set<String> {
        return preformattedTags
    }

    /**
     * Override the default set, which includes PRE, SCRIPT, STYLE, and
     * TEXTAREA, case insensitively.
     *
     * **Setting Preformatted Tags **
     *
     * Pass in a Set of Strings, one for each tag name that should be treated
     * like a PRE tag. You may pass in null or an empty Set to assign the empty
     * set, in which case no tags will be treated as preformatted, except that
     * HTML Comments will continue to be preformatted. If a tag is included in
     * the set of preformatted tags, all whitespace within the tag will be
     * preserved, including whitespace on the same line preceding the close tag.
     * This will generally make the close tag not line up with the start tag,
     * but it preserves the intention of the whitespace within the tag.
     *
     * The browser considers leading whitespace before the close tag to be
     * significant, but leading whitespace before the open tag to be
     * insignificant. For example, if the HTML author doesn't put the close
     * TEXTAREA tag flush to the left margin, then the TEXTAREA control in the
     * browser will have spaces on the last line inside the control. This may be
     * the HTML author's intent. Similarly, in a PRE, the browser treats a
     * flushed left close PRE tag as different from a close tag with leading
     * whitespace. Again, this must be left up to the HTML author.
     *
     *
     * **Examples **
     *
     * <blockquote>
     *
     *
     * Here is an example of how you can set the PreformattedTags list using
     * setPreformattedTags to include IFRAME, as well as the default set, if you
     * have an instance of this class named myHTMLWriter:
     *
     * <pre>
     * Set current = myHTMLWriter.getPreformattedTags();
     * current.add(&quot;IFRAME&quot;);
     * myHTMLWriter.setPreformattedTags(current);
     *
     * //The set is now &lt;b&gt;PRE, SCRIPT, STYLE, TEXTAREA, IFRAME&lt;/b&gt;
     *
    </pre> *
     *
     * Similarly, you can simply replace it with your own:
     *
     * <pre>
     *
     *
     * HashSet newset = new HashSet();
     * newset.add(&quot;PRE&quot;);
     * newset.add(&quot;TEXTAREA&quot;);
     * myHTMLWriter.setPreformattedTags(newset);
     *
     * //The set is now &lt;b&gt;{PRE, TEXTAREA}&lt;/b&gt;
     *
     *
    </pre> *
     *
     * You can remove all tags from the preformatted tags list, with an empty
     * set, like this:
     *
     * <pre>
     *
     * myHTMLWriter.setPreformattedTags(new HashSet());
     *
     * //The set is now &lt;b&gt;{}&lt;/b&gt;
     *
    </pre> *
     *
     * or with null, like this:
     *
     * <pre>
     *
     * myHTMLWriter.setPreformattedTags(null);
     *
     * //The set is now &lt;b&gt;{}&lt;/b&gt;
     *
     *
    </pre> *
     *
    </blockquote> *
     *
     * @param newSet
     * DOCUMENT ME!
     */
    fun setPreformattedTags(newSet: Set<String>?) {
        // no fancy merging, just set it, assuming they did a
        // getExcludeTrimTags() first if they wanted to preserve the default
        // set.
        // resets, and safely empties it out if newSet is null.
        preformattedTags = HashSet()
        if (newSet != null) {
            var aTag: Any
            val iter = newSet.iterator()
            while (iter.hasNext()) {
                aTag = iter.next()
                if (aTag != null) {
                    preformattedTags.add(aTag.toString().uppercase(Locale.getDefault()))
                }
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param qualifiedName
     * DOCUMENT ME!
     *
     * @return true if the qualifiedName passed in matched (case-insensitively)
     * a tag in the preformattedTags set, or false if not found or if
     * the set is empty or null.
     *
     * @see .setPreformattedTags
     */
    fun isPreformattedTag(qualifiedName: String): Boolean {
        // A null set implies that the user called setPreformattedTags(null),
        // which means they want no tags to be preformatted.
        return (preformattedTags != null
                && preformattedTags!!.contains(qualifiedName.uppercase(Locale.getDefault())))
    }

    /**
     * This override handles any elements that should not remove whitespace,
     * such as &lt;PRE&gt;, &lt;SCRIPT&gt;, &lt;STYLE&gt;, and &lt;TEXTAREA&gt;.
     * Note: the close tags won't line up with the open tag, but we can't alter
     * that. See javadoc note at setPreformattedTags.
     *
     * @param element
     * DOCUMENT ME!
     *
     * @throws IOException
     * When the stream could not be written to.
     *
     * @see .setPreformattedTags
     */
    @Throws(IOException::class)
    override fun writeElement(element: Element) {
        if (newLineAfterNTags == -1) { // lazy initialization check
            lazyInitNewLinesAfterNTags()
        }
        if (newLineAfterNTags > 0) {
            if (tagsOuput > 0 && tagsOuput % newLineAfterNTags == 0) {
                super.writer.write(lineSeparator)
            }
        }
        tagsOuput++
        val qualifiedName = element.qualifiedName
        val saveLastText = lastText
        val size = element.nodeCount()
        if (isPreformattedTag(qualifiedName)) {
            val currentFormat = outputFormat
            val saveNewlines = currentFormat!!.isNewlines
            val saveTrimText = currentFormat.isTrimText
            val currentIndent = currentFormat.indent

            // You could have nested PREs, or SCRIPTS within PRE... etc.,
            // therefore use push and pop.
            formatStack.push(
                FormatState(
                    saveNewlines, saveTrimText,
                    currentIndent
                )
            )
            try {
                // do this manually, since it won't be done while outputting
                // the tag.
                super.writePrintln()
                if (saveLastText.trim { it <= ' ' }.length == 0 && currentIndent != null && currentIndent.length > 0) {
                    // We are indenting, but we want to line up with the close
                    // tag. lastText was the indent (whitespace, no \n) before
                    // the preformatted start tag. So write it out instead of
                    // the current indent level. This makes it line up with its
                    // close tag.
                    super.writer.write(justSpaces(saveLastText))
                }

                // actually, newlines are handled in this class by writeString,
                // depending on if the stack is empty.
                currentFormat.isNewlines = false
                currentFormat.isTrimText = false
                currentFormat.indent = ""

                // This line is the recursive one:
                super.writeElement(element)
            } finally {
                val state = formatStack.pop() as FormatState
                currentFormat.isNewlines = state.isNewlines
                currentFormat.isTrimText = state.isTrimText
                currentFormat.setIndent(state.indent)
            }
        } else {
            super.writeElement(element)
        }
    }

    private fun justSpaces(text: String): String {
        val size = text.length
        val res = StringBuffer(size)
        var c: Char
        for (i in 0 until size) {
            c = text[i]
            when (c) {
                '\r', '\n' -> continue
                else -> res.append(c)
            }
        }
        return res.toString()
    }

    private fun lazyInitNewLinesAfterNTags() {
        newLineAfterNTags = if (outputFormat.isNewlines) {
            // don't bother, newlines are going to happen anyway.
            0
        } else {
            outputFormat.newLineAfterNTags
        }
    }

    // Allows us to the current state of the format in this struct on the
    // formatStack.
    private inner class FormatState(newLines: Boolean, trimText: Boolean, indent: String) {
        val isNewlines = newLines
        val isTrimText = trimText
        val indent = indent
    }

    companion object {
        private val lineSeparator = System.getProperty("line.separator")
        protected val DEFAULT_PREFORMATTED_TAGS = mutableSetOf<String>()

        init {
            // If you change this list, update the javadoc examples, above in the
            // class javadoc, in writeElement, and in setPreformattedTags().
            DEFAULT_PREFORMATTED_TAGS.add("PRE")
            DEFAULT_PREFORMATTED_TAGS.add("SCRIPT")
            DEFAULT_PREFORMATTED_TAGS.add("STYLE")
            DEFAULT_PREFORMATTED_TAGS.add("TEXTAREA")
        }

        protected val DEFAULT_HTML_FORMAT = OutputFormat("  ", true).apply { isTrimText = true; isSuppressDeclaration = true }

        /**
         * Convenience method to just get a String result, but **As XHTML **.
         *
         * @param html
         * DOCUMENT ME!
         *
         * @return a pretty printed String from the source string, preserving
         * whitespace in the defaultPreformattedTags set, but conforming to
         * XHTML: no close tags are omitted (though if empty, they will be
         * converted to XHTML empty tags: &lt;HR/&gt; Use one of the write
         * methods if you want stream output.
         *
         * @throws java.io.IOException
         * @throws java.io.UnsupportedEncodingException
         * @throws org.dom4j.DocumentException
         */
        @Throws(IOException::class, UnsupportedEncodingException::class, DocumentException::class)
        fun prettyPrintXHTML(html: String?): String {
            return prettyPrintHTML(html, true, true, true, false)
        }
        /**
         * DOCUMENT ME!
         *
         * @param html
         * DOCUMENT ME!
         * @param newlines
         * DOCUMENT ME!
         * @param trim
         * DOCUMENT ME!
         * @param isXHTML
         * DOCUMENT ME!
         * @param expandEmpty
         * DOCUMENT ME!
         *
         * @return a pretty printed String from the source string, preserving
         * whitespace in the defaultPreformattedTags set, and leaving the
         * close tags off of the default omitElementCloseSet set. This
         * override allows you to specify various formatter options. Use one
         * of the write methods if you want stream output.
         *
         * @throws java.io.IOException
         * @throws java.io.UnsupportedEncodingException
         * @throws org.dom4j.DocumentException
         */
        // Convenience methods, static, with bunch-o-defaults
        /**
         * Convenience method to just get a String result.
         *
         * @param html
         * DOCUMENT ME!
         *
         * @return a pretty printed String from the source string, preserving
         * whitespace in the defaultPreformattedTags set, and leaving the
         * close tags off of the default omitElementCloseSet set. Use one of
         * the write methods if you want stream output.
         *
         * @throws java.io.IOException
         * @throws java.io.UnsupportedEncodingException
         * @throws org.dom4j.DocumentException
         */
        @JvmOverloads
        @Throws(IOException::class, UnsupportedEncodingException::class, DocumentException::class)
        fun prettyPrintHTML(
            html: String?, newlines: Boolean = true,
            trim: Boolean = true, isXHTML: Boolean = false, expandEmpty: Boolean = true
        ): String {
            val sw = StringWriter()
            val format = OutputFormat.createPrettyPrint()
            format.isNewlines = newlines
            format.isTrimText = trim
            format.isXHTML = isXHTML
            format.isExpandEmptyElements = expandEmpty
            val writer = HTMLWriter(sw, format)
            val document = DocumentHelper.parseText(html)
            writer.write(document)
            writer.flush()
            return sw.toString()
        }
    }
}
