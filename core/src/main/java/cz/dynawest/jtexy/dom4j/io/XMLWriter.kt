/* Copied from Dom4j just to make the fields protected instead of private. */
package cz.dynawest.jtexy.dom4j.io

import org.dom4j.*
import org.dom4j.io.OutputFormat
import org.dom4j.tree.NamespaceStack
import org.xml.sax.*
import org.xml.sax.ext.LexicalHandler
import org.xml.sax.helpers.XMLFilterImpl
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
 * `XMLWriter` takes a DOM4J tree and formats it to a stream as
 * XML. It can also take SAX events too so can be used by SAX clients as this
 * object implements the [org.xml.sax.ContentHandler]and [ ] interfaces. as well. This formatter performs typical document
 * formatting. The XML declaration and processing instructions are always on
 * their own lines. An [OutputFormat]object can be used to define how
 * whitespace is handled when printing and allows various configuration options,
 * such as to allow suppression of the XML declaration, the encoding declaration
 * or whether empty documents are collapsed.
 *
 *
 *
 *
 * There are `write(...)` methods to print any of the standard
 * DOM4J classes, including `Document` and `Element`,
 * to either a `Writer` or an `OutputStream`.
 * Warning: using your own `Writer` may cause the writer's
 * preferred character encoding to be ignored. If you use encodings other than
 * UTF8, we recommend using the method that takes an OutputStream instead.
 *
 *
 * @author [James Strachan ](mailto:jstrachan@apache.org)
 * @author Joseph Bowbeer
 * @version $Revision: 1.83.2.2 $
 */
open class XMLWriter : XMLFilterImpl, LexicalHandler {
    /** Should entityRefs by resolved when writing ?  */
    private var resolveEntityRefs = true

    /**
     * Stores the last type of node written so algorithms can refer to the
     * previous node type
     */
    protected var lastOutputNodeType = 0

    /**
     * Stores if the last written element node was a closing tag or an opening
     * tag.
     */
    private var lastElementClosed = false

    /** Stores the xml:space attribute value of preserve for whitespace flag  */
    protected var preserve = false

    /** The Writer used to output to  */
    protected var writer: Writer
        set(writer) { field = writer; autoFlush = false }


    /** The Stack of namespaceStack written so far  */
    protected var namespaceStack = NamespaceStack()

    /**
     * Lets subclasses get at the current format object, so they can call
     * setTrimText, setNewLines, etc. Put in to support the HTMLWriter, in the
     * way that it pushes the current newline/trim state onto a stack and
     * overrides the state within preformatted tags.
     *
     * @return DOCUMENT ME!
     */
    /** The format used by this writer  */
    protected var outputFormat: OutputFormat

    /**
     * DOCUMENT ME!
     *
     * @return true if text thats output should be escaped. This is enabled by
     * default. It could be disabled if the output format is textual,
     * like in XSLT where we can have xml, html or text output.
     */
    /**
     * Sets whether text output should be escaped or not. This is enabled by
     * default. It could be disabled if the output format is textual, like in
     * XSLT where we can have xml, html or text output.
     *
     * @param escapeText
     * DOCUMENT ME!
     */
    /** whether we should escape text  */
    var isEscapeText = true

    /**
     * The initial number of indentations (so you can print a whole document indented, if you like)
     * Set the initial indentation level. This can be used to output a document
     * (or, more likely, an element) starting at a given indent level, so it's
     * not always flush against the left margin. Default: 0
     *
     * @param indentLevel
     * the number of indents to start with
     */
    protected var indentLevel = 0

    /** buffer used when escaping strings  */
    private val buffer = StringBuffer()

    /**
     * whether we have added characters before from the same chunk of characters
     */
    private var charsAdded = false
    private var lastChar = 0.toChar()

    /** Whether a flush should occur after writing a document  */
    private var autoFlush = false

    /** Lexical handler we should delegate to  */
    private var lexicalHandler: LexicalHandler? = null

    /**
     * Whether comments should appear inside DTD declarations - defaults to
     * false
     */
    private val showCommentsInDTDs = false

    /** Is the writer curerntly inside a DTD definition?  */
    private var inDTD = false

    /** The namespaces used for the current element when consuming SAX events  */
    private var namespacesMap: MutableMap<String, String>? = null

    /**
     * Sets the maximum allowed character code that should be allowed unescaped
     * such as 127 in US-ASCII (7 bit) or 255 in ISO- (8 bit) or -1 to not
     * escape any characters (other than the special XML characters like &lt;
     * &gt; &amp;) If this is not explicitly set then it is defaulted from the
     * encoding.
     *
     * @param maximumAllowedCharacter
     * The maximumAllowedCharacter to set
     */
    /**
     * what is the maximum allowed character code such as 127 in US-ASCII (7
     * bit) or 255 in ISO- (8 bit) or -1 to not escape any characters (other
     * than the special XML characters like &lt; &gt; &amp;)
     */
    var maximumAllowedCharacter = 0
        /**
         * Returns the maximum allowed character code that should be allowed
         * unescaped which defaults to 127 in US-ASCII (7 bit) or 255 in ISO- (8
         * bit).
         *
         * @return DOCUMENT ME!
         */
        get() {
            if (field == 0) {
                field = defaultMaximumAllowedCharacter()
            }
            return field
        }

    @JvmOverloads
    constructor(writer: Writer, format: OutputFormat = DEFAULT_FORMAT) {
        this.writer = writer
        outputFormat = format
        namespaceStack.push(Namespace.NO_NAMESPACE)
    }

    constructor() {
        outputFormat = DEFAULT_FORMAT
        writer = BufferedWriter(OutputStreamWriter(System.out))
        autoFlush = true
        namespaceStack.push(Namespace.NO_NAMESPACE)
    }

    constructor(out: OutputStream) {
        outputFormat = DEFAULT_FORMAT
        writer = createWriter(out, outputFormat.encoding)
        autoFlush = true
        namespaceStack.push(Namespace.NO_NAMESPACE)
    }

    constructor(out: OutputStream, format: OutputFormat) {
        outputFormat = format
        writer = createWriter(out, format.encoding)
        autoFlush = true
        namespaceStack.push(Namespace.NO_NAMESPACE)
    }

    constructor(format: OutputFormat) {
        outputFormat = format
        writer = createWriter(System.out, format.encoding)
        autoFlush = true
        namespaceStack.push(Namespace.NO_NAMESPACE)
    }


    @Throws(UnsupportedEncodingException::class)
    fun setOutputStream(out: OutputStream) {
        writer = createWriter(out, outputFormat.encoding)
        autoFlush = true
    }


    /**
     * Flushes the underlying Writer
     *
     * @throws IOException
     * DOCUMENT ME!
     */
    @Throws(IOException::class)
    fun flush() {
        writer.flush()
    }

    /**
     * Closes the underlying Writer
     *
     * @throws IOException
     * DOCUMENT ME!
     */
    @Throws(IOException::class)
    fun close() {
        writer.close()
    }

    /**
     * Writes the new line text to the underlying Writer
     *
     * @throws IOException
     * DOCUMENT ME!
     */
    @Throws(IOException::class)
    fun println() {
        writer.write(outputFormat.lineSeparator)
    }

    /**
     * Writes the given [Attribute].
     *
     * @param attribute
     * `Attribute` to output.
     *
     * @throws IOException
     * DOCUMENT ME!
     */
    @Throws(IOException::class)
    fun write(attribute: Attribute) {
        writeAttribute(attribute)
        if (autoFlush) flush()
    }

    /**
     * This will print the `Document` to the current Writer.
     *
     * Warning: using your own Writer may cause the writer's preferred character
     * encoding to be ignored. If you use encodings other than UTF8, we
     * recommend using the method that takes an OutputStream instead.
     *
     * Note: as with all Writers, you may need to flush() yours after this
     * method returns.
     *
     * @param doc
     * `Document` to format.
     *
     * @throws IOException
     * if there's any problem writing.
     */
    @Throws(IOException::class)
    open fun write(doc: Document) {
        writeDeclaration()
        if (doc.docType != null) {
            indent()
            writeDocType(doc.docType)
        }
        var i = 0
        val size = doc.nodeCount()
        while (i < size) {
            val node = doc.node(i)
            writeNode(node)
            i++
        }
        writePrintln()
        if (autoFlush) {
            flush()
        }
    }

    /**
     * Writes the `[Element]`, including its `[ ]`
     * s, and its value, and all its content (child nodes) to the current
     * Writer.
     *
     * @param element
     * `Element` to output.
     *
     * @throws IOException
     * DOCUMENT ME!
     */
    @Throws(IOException::class)
    fun write(element: Element) {
        writeElement(element)
        if (autoFlush) {
            flush()
        }
    }

    /**
     * Writes the given [CDATA].
     *
     * @param cdata
     * `CDATA` to output.
     *
     * @throws IOException
     * DOCUMENT ME!
     */
    @Throws(IOException::class)
    fun write(cdata: CDATA) {
        writeCDATA(cdata.text)
        if (autoFlush) {
            flush()
        }
    }

    /**
     * Writes the given [Comment].
     *
     * @param comment
     * `Comment` to output.
     *
     * @throws IOException
     * DOCUMENT ME!
     */
    @Throws(IOException::class)
    fun write(comment: Comment) {
        writeComment(comment.text)
        if (autoFlush) {
            flush()
        }
    }

    /**
     * Writes the given [DocumentType].
     *
     * @param docType
     * `DocumentType` to output.
     *
     * @throws IOException
     * DOCUMENT ME!
     */
    @Throws(IOException::class)
    fun write(docType: DocumentType?) {
        writeDocType(docType)
        if (autoFlush) {
            flush()
        }
    }

    /**
     * Writes the given [Entity].
     *
     * @param entity
     * `Entity` to output.
     *
     * @throws IOException
     * DOCUMENT ME!
     */
    @Throws(IOException::class)
    fun write(entity: Entity) {
        writeEntity(entity)
        if (autoFlush) {
            flush()
        }
    }

    /**
     * Writes the given [Namespace].
     *
     * @param namespace
     * `Namespace` to output.
     *
     * @throws IOException
     * DOCUMENT ME!
     */
    @Throws(IOException::class)
    fun write(namespace: Namespace?) {
        writeNamespace(namespace)
        if (autoFlush) {
            flush()
        }
    }

    /**
     * Writes the given [ProcessingInstruction].
     *
     * @param processingInstruction
     * `ProcessingInstruction` to output.
     *
     * @throws IOException
     * DOCUMENT ME!
     */
    @Throws(IOException::class)
    fun write(processingInstruction: ProcessingInstruction) {
        writeProcessingInstruction(processingInstruction)
        if (autoFlush) {
            flush()
        }
    }

    /**
     *
     *
     * Print out a [String], Perfoms the necessary entity escaping and
     * whitespace stripping.
     *
     *
     * @param text
     * is the text to output
     *
     * @throws IOException
     * DOCUMENT ME!
     */
    @Throws(IOException::class)
    fun write(text: String) {
        writeString(text)
        if (autoFlush) {
            flush()
        }
    }

    /**
     * Writes the given [Text].
     *
     * @param text
     * `Text` to output.
     *
     * @throws IOException
     * DOCUMENT ME!
     */
    @Throws(IOException::class)
    fun write(text: Text) {
        writeString(text.text)
        if (autoFlush) {
            flush()
        }
    }

    /**
     * Writes the given [Node].
     *
     * @param node
     * `Node` to output.
     *
     * @throws IOException
     * DOCUMENT ME!
     */
    @Throws(IOException::class)
    fun write(node: Node) {
        writeNode(node)
        if (autoFlush) {
            flush()
        }
    }

    /**
     * Writes the given object which should be a String, a Node or a List of
     * Nodes.
     *
     * @param object
     * is the object to output.
     *
     * @throws IOException
     * DOCUMENT ME!
     */
    @Throws(IOException::class)
    fun write(`object`: Any?) {
        if (`object` is Node) {
            write(`object`)
        } else if (`object` is String) {
            write(`object`)
        } else if (`object` is List<*>) {
            val list = `object`
            var i = 0
            val size = list.size
            while (i < size) {
                write(list[i])
                i++
            }
        } else if (`object` != null) {
            throw IOException("Invalid object: $`object`")
        }
    }

    /**
     *
     *
     * Writes the opening tag of an [Element], including its [ ]s but without its content.
     *
     *
     * @param element
     * `Element` to output.
     *
     * @throws IOException
     * DOCUMENT ME!
     */
    @Throws(IOException::class)
    open fun writeOpen(element: Element) {
        writer.write("<")
        writer.write(element.qualifiedName)
        writeAttributes(element)
        if (!element.hasContent()) writer.write("/>") else writer.write(">")
    }

    /**
     *
     *
     * Writes the closing tag of an [Element]
     *
     *
     * @param element
     * `Element` to output.
     *
     * @throws IOException
     * DOCUMENT ME!
     */
    @Throws(IOException::class)
    open fun writeClose(element: Element) {
        writeClose(element.qualifiedName)
    }

    // XMLFilterImpl methods
    // -------------------------------------------------------------------------
    @Throws(IOException::class, SAXException::class)
    override fun parse(source: InputSource) {
        installLexicalHandler()
        super.parse(source)
    }

    @Throws(SAXNotRecognizedException::class, SAXNotSupportedException::class)
    override fun setProperty(name: String, value: Any) {
        for (i in LEXICAL_HANDLER_NAMES.indices) {
            if (LEXICAL_HANDLER_NAMES[i] == name) {
                setLexicalHandler(value as LexicalHandler)
                return
            }
        }
        super.setProperty(name, value)
    }

    @Throws(SAXNotRecognizedException::class, SAXNotSupportedException::class)
    override fun getProperty(name: String): Any {
        for (i in LEXICAL_HANDLER_NAMES.indices) {
            if (LEXICAL_HANDLER_NAMES[i] == name) {
                return getLexicalHandler()!!
            }
        }
        return super.getProperty(name)
    }

    fun setLexicalHandler(handler: LexicalHandler?) {
        if (handler == null) {
            throw NullPointerException("Null lexical handler")
        } else {
            lexicalHandler = handler
        }
    }

    fun getLexicalHandler(): LexicalHandler? {
        return lexicalHandler
    }

    // ContentHandler interface
    // -------------------------------------------------------------------------
    override fun setDocumentLocator(locator: Locator) {
        super.setDocumentLocator(locator)
    }

    @Throws(SAXException::class)
    override fun startDocument() {
        try {
            writeDeclaration()
            super.startDocument()
        } catch (e: IOException) {
            handleException(e)
        }
    }

    @Throws(SAXException::class)
    override fun endDocument() {
        super.endDocument()
        if (autoFlush) {
            try {
                flush()
            } catch (e: IOException) {
            }
        }
    }

    @Throws(SAXException::class)
    override fun startPrefixMapping(prefix: String, uri: String) {
        if (namespacesMap == null) {
            namespacesMap = mutableMapOf<String, String>()
        }
        namespacesMap!![prefix] = uri
        super.startPrefixMapping(prefix, uri)
    }

    @Throws(SAXException::class)
    override fun endPrefixMapping(prefix: String) {
        super.endPrefixMapping(prefix)
    }

    @Throws(SAXException::class)
    override fun startElement(
        namespaceURI: String, localName: String,
        qName: String, attributes: Attributes
    ) {
        try {
            charsAdded = false
            writePrintln()
            indent()
            writer.write("<")
            writer.write(qName)
            writeNamespaces()
            writeAttributes(attributes)
            writer.write(">")
            ++indentLevel
            lastOutputNodeType = Node.ELEMENT_NODE.toInt()
            lastElementClosed = false
            super.startElement(namespaceURI, localName, qName, attributes)
        } catch (e: IOException) {
            handleException(e)
        }
    }

    @Throws(SAXException::class)
    override fun endElement(namespaceURI: String, localName: String, qName: String) {
        try {
            charsAdded = false
            --indentLevel
            if (lastElementClosed) {
                writePrintln()
                indent()
            }

            // XXXX: need to determine this using a stack and checking for
            // content / children
            val hadContent = true
            if (hadContent) {
                writeClose(qName)
            } else {
                writeEmptyElementClose(qName)
            }
            lastOutputNodeType = Node.ELEMENT_NODE.toInt()
            lastElementClosed = true
            super.endElement(namespaceURI, localName, qName)
        } catch (e: IOException) {
            handleException(e)
        }
    }

    @Throws(SAXException::class)
    override fun characters(ch: CharArray, start: Int, length: Int) {
        if (ch == null || ch.size == 0 || length <= 0) {
            return
        }
        try {
            /*
             * we can't use the writeString method here because it's possible we
             * don't receive all characters at once and calling writeString
             * would cause unwanted spaces to be added in between these chunks
             * of character arrays.
             */
            var string = String(ch, start, length)
            if (isEscapeText) {
                string = escapeElementEntities(string)
            }
            if (outputFormat!!.isTrimText) {
                if (lastOutputNodeType == Node.TEXT_NODE.toInt() && !charsAdded) {
                    writer.write(' '.code)
                } else if (charsAdded && Character.isWhitespace(lastChar)) {
                    writer.write(' '.code)
                } else if (lastOutputNodeType == Node.ELEMENT_NODE.toInt() && outputFormat!!.isPadText && lastElementClosed
                    && Character.isWhitespace(ch[0])
                ) {
                    writer.write(PAD_TEXT)
                }
                var delim = ""
                val tokens = StringTokenizer(string)
                while (tokens.hasMoreTokens()) {
                    writer.write(delim)
                    writer.write(tokens.nextToken())
                    delim = " "
                }
            } else {
                writer.write(string)
            }
            charsAdded = true
            lastChar = ch[start + length - 1]
            lastOutputNodeType = Node.TEXT_NODE.toInt()
            super.characters(ch, start, length)
        } catch (e: IOException) {
            handleException(e)
        }
    }

    @Throws(SAXException::class)
    override fun ignorableWhitespace(ch: CharArray, start: Int, length: Int) {
        super.ignorableWhitespace(ch, start, length)
    }

    @Throws(SAXException::class)
    override fun processingInstruction(target: String, data: String) {
        try {
            indent()
            writer.write("<?")
            writer.write(target)
            writer.write(" ")
            writer.write(data)
            writer.write("?>")
            writePrintln()
            lastOutputNodeType = Node.PROCESSING_INSTRUCTION_NODE.toInt()
            super.processingInstruction(target, data)
        } catch (e: IOException) {
            handleException(e)
        }
    }

    // DTDHandler interface
    // -------------------------------------------------------------------------
    @Throws(SAXException::class)
    override fun notationDecl(name: String, publicID: String, systemID: String) {
        super.notationDecl(name, publicID, systemID)
    }

    @Throws(SAXException::class)
    override fun unparsedEntityDecl(
        name: String, publicID: String,
        systemID: String, notationName: String
    ) {
        super.unparsedEntityDecl(name, publicID, systemID, notationName)
    }

    // LexicalHandler interface
    // -------------------------------------------------------------------------
    @Throws(SAXException::class)
    override fun startDTD(name: String, publicID: String, systemID: String) {
        inDTD = true
        try {
            writeDocType(name, publicID, systemID)
        } catch (e: IOException) {
            handleException(e)
        }
        if (lexicalHandler != null) {
            lexicalHandler!!.startDTD(name, publicID, systemID)
        }
    }

    @Throws(SAXException::class)
    override fun endDTD() {
        inDTD = false
        if (lexicalHandler != null) {
            lexicalHandler!!.endDTD()
        }
    }

    @Throws(SAXException::class)
    override fun startCDATA() {
        try {
            writer.write("<![CDATA[")
        } catch (e: IOException) {
            handleException(e)
        }
        if (lexicalHandler != null) {
            lexicalHandler!!.startCDATA()
        }
    }

    @Throws(SAXException::class)
    override fun endCDATA() {
        try {
            writer.write("]]>")
        } catch (e: IOException) {
            handleException(e)
        }
        if (lexicalHandler != null) {
            lexicalHandler!!.endCDATA()
        }
    }

    @Throws(SAXException::class)
    override fun startEntity(name: String) {
        try {
            writeEntityRef(name)
        } catch (e: IOException) {
            handleException(e)
        }
        if (lexicalHandler != null) {
            lexicalHandler!!.startEntity(name)
        }
    }

    @Throws(SAXException::class)
    override fun endEntity(name: String) {
        if (lexicalHandler != null) {
            lexicalHandler!!.endEntity(name)
        }
    }

    @Throws(SAXException::class)
    override fun comment(ch: CharArray, start: Int, length: Int) {
        if (showCommentsInDTDs || !inDTD) {
            try {
                charsAdded = false
                writeComment(ch.copyOfRange(start, start+length).concatToString())
            } catch (e: IOException) {
                handleException(e)
            }
        }
        if (lexicalHandler != null) {
            lexicalHandler!!.comment(ch, start, length)
        }
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    @Throws(IOException::class)
    protected open fun writeElement(element: Element) {
        val size = element.nodeCount()
        val qualifiedName = element.qualifiedName
        writePrintln()
        indent()
        writer.write("<")
        writer.write(qualifiedName)
        val previouslyDeclaredNamespaces = namespaceStack.size()
        val ns = element.namespace
        if (isNamespaceDeclaration(ns)) {
            namespaceStack.push(ns)
            writeNamespace(ns)
        }

        // Print out additional namespace declarations
        var textOnly = true
        for (i in 0 until size) {
            val node = element.node(i)
            if (node is Namespace) {
                val additional = node
                if (isNamespaceDeclaration(additional)) {
                    namespaceStack.push(additional)
                    writeNamespace(additional)
                }
            } else if (node is Element) {
                textOnly = false
            } else if (node is Comment) {
                textOnly = false
            }
        }
        writeAttributes(element)
        lastOutputNodeType = Node.ELEMENT_NODE.toInt()
        if (size <= 0) {
            writeEmptyElementClose(qualifiedName)
        } else {
            writer.write(">")
            if (textOnly) {
                // we have at least one text node so lets assume
                // that its non-empty
                writeElementContent(element)
            } else {
                // we know it's not null or empty from above
                ++indentLevel
                writeElementContent(element)
                --indentLevel
                writePrintln()
                indent()
            }
            writer.write("</")
            writer.write(qualifiedName)
            writer.write(">")
        }

        // remove declared namespaceStack from stack
        while (namespaceStack.size() > previouslyDeclaredNamespaces) {
            namespaceStack.pop()
        }
        lastOutputNodeType = Node.ELEMENT_NODE.toInt()
    }

    /**
     * Determines if element is a special case of XML elements where it contains
     * an xml:space attribute of "preserve". If it does, then retain whitespace.
     *
     * @param element
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected fun isElementSpacePreserved(element: Element): Boolean {
        val attr = element.attribute("space") as Attribute
        var preserveFound = preserve // default to global state
        if (attr != null) {
            preserveFound = if ("xml" == attr.namespacePrefix && "preserve" == attr.text) {
                true
            } else {
                false
            }
        }
        return preserveFound
    }

    /**
     * Outputs the content of the given element. If whitespace trimming is
     * enabled then all adjacent text nodes are appended together before the
     * whitespace trimming occurs to avoid problems with multiple text nodes
     * being created due to text content that spans parser buffers in a SAX
     * parser.
     *
     * @param element
     * DOCUMENT ME!
     *
     * @throws IOException
     * DOCUMENT ME!
     */
    @Throws(IOException::class)
    protected fun writeElementContent(element: Element) {
        var trim = outputFormat!!.isTrimText
        val oldPreserve = preserve
        if (trim) { // verify we have to before more expensive test
            preserve = isElementSpacePreserved(element)
            trim = !preserve
        }
        if (trim) {
            // concatenate adjacent text nodes together
            // so that whitespace trimming works properly
            var lastTextNode: Text? = null
            var buff: StringBuffer? = null
            var textOnly = true
            var i = 0
            val size = element.nodeCount()
            while (i < size) {
                val node = element.node(i)
                if (node is Text) {
                    if (lastTextNode == null) {
                        lastTextNode = node
                    } else {
                        if (buff == null) {
                            buff = StringBuffer(lastTextNode.text)
                        }
                        buff.append(node.text)
                    }
                } else {
                    if (!textOnly && outputFormat!!.isPadText) {
                        // only add the PAD_TEXT if the text itself starts with
                        // whitespace
                        var firstChar = 'a'
                        if (buff != null) {
                            firstChar = buff[0]
                        } else if (lastTextNode != null) {
                            firstChar = lastTextNode.text[0]
                        }
                        if (Character.isWhitespace(firstChar)) {
                            writer.write(PAD_TEXT)
                        }
                    }
                    if (lastTextNode != null) {
                        if (buff != null) {
                            writeString(buff.toString())
                            buff = null
                        } else {
                            writeString(lastTextNode.text)
                        }
                        if (outputFormat!!.isPadText) {
                            // only add the PAD_TEXT if the text itself ends
                            // with whitespace
                            var lastTextChar = 'a'
                            if (buff != null) {
                                lastTextChar = buff[buff.length - 1]
                            } else if (lastTextNode != null) {
                                val txt = lastTextNode.text
                                lastTextChar = txt[txt.length - 1]
                            }
                            if (Character.isWhitespace(lastTextChar)) {
                                writer.write(PAD_TEXT)
                            }
                        }
                        lastTextNode = null
                    }
                    textOnly = false
                    writeNode(node)
                }
                i++
            }
            if (lastTextNode != null) {
                if (!textOnly && outputFormat!!.isPadText) {
                    // only add the PAD_TEXT if the text itself starts with
                    // whitespace
                    var firstChar = 'a'
                    firstChar = if (buff != null) {
                        buff[0]
                    } else {
                        lastTextNode.text[0]
                    }
                    if (Character.isWhitespace(firstChar)) {
                        writer.write(PAD_TEXT)
                    }
                }
                if (buff != null) {
                    writeString(buff.toString())
                    buff = null
                } else {
                    writeString(lastTextNode.text)
                }
                lastTextNode = null
            }
        } else {
            var lastTextNode: Node? = null
            var i = 0
            val size = element.nodeCount()
            while (i < size) {
                val node = element.node(i)
                lastTextNode = if (node is Text) {
                    writeNode(node)
                    node
                } else {
                    if (lastTextNode != null && outputFormat!!.isPadText) {
                        // only add the PAD_TEXT if the text itself ends with
                        // whitespace
                        val txt = lastTextNode.text
                        val lastTextChar = txt[txt.length - 1]
                        if (Character.isWhitespace(lastTextChar)) {
                            writer.write(PAD_TEXT)
                        }
                    }
                    writeNode(node)

                    // if ((lastTextNode != null) && format.isPadText()) {
                    // writer.write(PAD_TEXT);
                    // }
                    null
                }
                i++
            }
        }
        preserve = oldPreserve
    }

    @Throws(IOException::class)
    protected open fun writeCDATA(text: String?) {
        writer.write("<![CDATA[")
        if (text != null) {
            writer.write(text)
        }
        writer.write("]]>")
        lastOutputNodeType = Node.CDATA_SECTION_NODE.toInt()
    }

    @Throws(IOException::class)
    protected fun writeDocType(docType: DocumentType?) {
        if (docType != null) {
            docType.write(writer)
            writePrintln()
        }
    }

    @Throws(IOException::class)
    protected fun writeNamespace(namespace: Namespace?) {
        if (namespace != null) {
            writeNamespace(namespace.prefix, namespace.uri)
        }
    }

    /**
     * Writes the SAX namepsaces
     *
     * @throws IOException
     * DOCUMENT ME!
     */
    @Throws(IOException::class)
    protected fun writeNamespaces() {
        if (namespacesMap != null) {
            val iter: Iterator<*> = namespacesMap!!.entries.iterator()
            while (iter
                    .hasNext()
            ) {
                val (key, value) = iter.next() as Map.Entry<*, *>
                val prefix = key as String
                val uri = value as String
                writeNamespace(prefix, uri)
            }
            namespacesMap = null
        }
    }

    /**
     * Writes the SAX namepsaces
     *
     * @param prefix
     * the prefix
     * @param uri
     * the namespace uri
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    protected fun writeNamespace(prefix: String?, uri: String?) {
        if (prefix != null && prefix.length > 0) {
            writer.write(" xmlns:")
            writer.write(prefix)
            writer.write("=\"")
        } else {
            writer.write(" xmlns=\"")
        }
        writer.write(uri)
        writer.write("\"")
    }

    @Throws(IOException::class)
    protected fun writeProcessingInstruction(pi: ProcessingInstruction) {
        // indent();
        writer.write("<?")
        writer.write(pi.name)
        writer.write(" ")
        writer.write(pi.text)
        writer.write("?>")
        writePrintln()
        lastOutputNodeType = Node.PROCESSING_INSTRUCTION_NODE.toInt()
    }

    @Throws(IOException::class)
    protected open fun writeString(text: String) {
        var text: String? = text
        if (text != null && text.length > 0) {
            if (isEscapeText) {
                text = escapeElementEntities(text)
            }

            // if (format.isPadText()) {
            // if (lastOutputNodeType == Node.ELEMENT_NODE) {
            // writer.write(PAD_TEXT);
            // }
            // }
            if (outputFormat!!.isTrimText) {
                var first = true
                val tokenizer = StringTokenizer(text)
                while (tokenizer.hasMoreTokens()) {
                    val token = tokenizer.nextToken()
                    if (first) {
                        first = false
                        if (lastOutputNodeType == Node.TEXT_NODE.toInt()) {
                            writer.write(" ")
                        }
                    } else {
                        writer.write(" ")
                    }
                    writer.write(token)
                    lastOutputNodeType = Node.TEXT_NODE.toInt()
                    lastChar = token[token.length - 1]
                }
            } else {
                lastOutputNodeType = Node.TEXT_NODE.toInt()
                writer.write(text)
                lastChar = text[text.length - 1]
            }
        }
    }

    /**
     * This method is used to write out Nodes that contain text and still allow
     * for xml:space to be handled properly.
     *
     * @param node
     * DOCUMENT ME!
     *
     * @throws IOException
     * DOCUMENT ME!
     */
    @Throws(IOException::class)
    protected fun writeNodeText(node: Node) {
        var text = node.text
        if (text != null && text.length > 0) {
            if (isEscapeText) {
                text = escapeElementEntities(text)
            }
            lastOutputNodeType = Node.TEXT_NODE.toInt()
            writer.write(text)
            lastChar = text[text.length - 1]
        }
    }

    @Throws(IOException::class)
    protected fun writeNode(node: Node) {
        val nodeType = node.nodeType
        when (nodeType) {
            Node.ELEMENT_NODE -> writeElement(node as Element)
            Node.ATTRIBUTE_NODE -> writeAttribute(node as Attribute)
            Node.TEXT_NODE -> writeNodeText(node)
            Node.CDATA_SECTION_NODE -> writeCDATA(node.text)
            Node.ENTITY_REFERENCE_NODE -> writeEntity(node as Entity)
            Node.PROCESSING_INSTRUCTION_NODE -> writeProcessingInstruction(node as ProcessingInstruction)
            Node.COMMENT_NODE -> writeComment(node.text)
            Node.DOCUMENT_NODE -> write(node as Document)
            Node.DOCUMENT_TYPE_NODE -> writeDocType(node as DocumentType)
            Node.NAMESPACE_NODE -> {}
            else -> throw IOException("Invalid node type: $node")
        }
    }

    protected fun installLexicalHandler() {
        val parent = parent ?: throw NullPointerException("No parent for filter")

        // try to register for lexical events
        for (i in LEXICAL_HANDLER_NAMES.indices) {
            try {
                parent.setProperty(LEXICAL_HANDLER_NAMES[i], this)
                break
            } catch (ex: SAXNotRecognizedException) {
                // ignore
            } catch (ex: SAXNotSupportedException) {
                // ignore
            }
        }
    }

    @Throws(IOException::class)
    protected fun writeDocType(name: String?, publicID: String?, systemID: String?) {
        var hasPublic = false
        writer.write("<!DOCTYPE ")
        writer.write(name)
        if (publicID != null && publicID != "") {
            writer.write(" PUBLIC \"")
            writer.write(publicID)
            writer.write("\"")
            hasPublic = true
        }
        if (systemID != null && systemID != "") {
            if (!hasPublic) {
                writer.write(" SYSTEM")
            }
            writer.write(" \"")
            writer.write(systemID)
            writer.write("\"")
        }
        writer.write(">")
        writePrintln()
    }

    @Throws(IOException::class)
    protected open fun writeEntity(entity: Entity) {
        if (!resolveEntityRefs()) {
            writeEntityRef(entity.name)
        } else {
            writer.write(entity.text)
        }
    }

    @Throws(IOException::class)
    protected fun writeEntityRef(name: String?) {
        writer.write("&")
        writer.write(name)
        writer.write(";")
        lastOutputNodeType = Node.ENTITY_REFERENCE_NODE.toInt()
    }

    @Throws(IOException::class)
    protected fun writeComment(text: String?) {
        if (outputFormat!!.isNewlines) {
            println()
            indent()
        }
        writer.write("<!--")
        writer.write(text)
        writer.write("-->")
        lastOutputNodeType = Node.COMMENT_NODE.toInt()
    }

    /**
     * Writes the attributes of the given element
     *
     * @param element
     * DOCUMENT ME!
     *
     * @throws IOException
     * DOCUMENT ME!
     */
    @Throws(IOException::class)
    protected fun writeAttributes(element: Element) {
        // I do not yet handle the case where the same prefix maps to
        // two different URIs. For attributes on the same element
        // this is illegal; but as yet we don't throw an exception
        // if someone tries to do this
        var i = 0
        val size = element.attributeCount()
        while (i < size) {
            val attribute = element.attribute(i)
            val ns = attribute.namespace
            if (ns != null && ns !== Namespace.NO_NAMESPACE && ns !== Namespace.XML_NAMESPACE) {
                val prefix = ns.prefix
                val uri = namespaceStack.getURI(prefix)
                if (ns.uri != uri) {
                    writeNamespace(ns)
                    namespaceStack.push(ns)
                }
            }

            // If the attribute is a namespace declaration, check if we have
            // already written that declaration elsewhere (if that's the case,
            // it must be in the namespace stack
            val attName = attribute.name
            if (attName.startsWith("xmlns:")) {
                val prefix = attName.substring(6)
                if (namespaceStack.getNamespaceForPrefix(prefix) == null) {
                    val uri = attribute.value
                    namespaceStack.push(prefix, uri)
                    writeNamespace(prefix, uri)
                }
            } else if (attName == "xmlns") {
                if (namespaceStack.defaultNamespace == null) {
                    val uri = attribute.value
                    namespaceStack.push(null, uri)
                    writeNamespace(null, uri)
                }
            } else {
                val quote = outputFormat!!.attributeQuoteCharacter
                writer.write(" ")
                writer.write(attribute.qualifiedName)
                writer.write("=")
                writer.write(quote.code)
                writeEscapeAttributeEntities(attribute.value)
                writer.write(quote.code)
            }
            i++
        }
    }

    @Throws(IOException::class)
    protected fun writeAttribute(attribute: Attribute) {
        writer.write(" ")
        writer.write(attribute.qualifiedName)
        writer.write("=")
        val quote = outputFormat!!.attributeQuoteCharacter
        writer.write(quote.code)
        writeEscapeAttributeEntities(attribute.value)
        writer.write(quote.code)
        lastOutputNodeType = Node.ATTRIBUTE_NODE.toInt()
    }

    @Throws(IOException::class)
    protected fun writeAttributes(attributes: Attributes) {
        var i = 0
        val size = attributes.length
        while (i < size) {
            writeAttribute(attributes, i)
            i++
        }
    }

    @Throws(IOException::class)
    protected fun writeAttribute(attributes: Attributes, index: Int) {
        val quote = outputFormat!!.attributeQuoteCharacter
        writer.write(" ")
        writer.write(attributes.getQName(index))
        writer.write("=")
        writer.write(quote.code)
        writeEscapeAttributeEntities(attributes.getValue(index))
        writer.write(quote.code)
    }

    @Throws(IOException::class)
    protected fun indent() {
        val indent = outputFormat!!.indent
        if (indent != null && indent.length > 0) {
            for (i in 0 until indentLevel) {
                writer.write(indent)
            }
        }
    }

    /**
     *
     *
     * This will print a new line only if the newlines flag was set to true
     *
     *
     * @throws IOException
     * DOCUMENT ME!
     */
    @Throws(IOException::class)
    protected fun writePrintln() {
        if (outputFormat!!.isNewlines) {
            val seperator = outputFormat!!.lineSeparator
            if (lastChar != seperator[seperator.length - 1]) {
                writer.write(outputFormat!!.lineSeparator)
            }
        }
    }

    /**
     * Get an OutputStreamWriter, use preferred encoding.
     *
     * @param outStream
     * DOCUMENT ME!
     * @param encoding
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws UnsupportedEncodingException
     * DOCUMENT ME!
     */
    @Throws(UnsupportedEncodingException::class)
    protected fun createWriter(outStream: OutputStream?, encoding: String?): Writer {
        return BufferedWriter(OutputStreamWriter(outStream, encoding))
    }

    /**
     *
     *
     * This will write the declaration to the given Writer. Assumes XML version
     * 1.0 since we don't directly know.
     *
     *
     * @throws IOException
     * DOCUMENT ME!
     */
    @Throws(IOException::class)
    protected open fun writeDeclaration() {
        val encoding = outputFormat!!.encoding

        // Only print of declaration is not suppressed
        if (!outputFormat!!.isSuppressDeclaration) {
            // Assume 1.0 version
            if (encoding == "UTF8") {
                writer.write("<?xml version=\"1.0\"")
                if (!outputFormat!!.isOmitEncoding) {
                    writer.write(" encoding=\"UTF-8\"")
                }
                writer.write("?>")
            } else {
                writer.write("<?xml version=\"1.0\"")
                if (!outputFormat!!.isOmitEncoding) {
                    writer.write(" encoding=\"$encoding\"")
                }
                writer.write("?>")
            }
            if (outputFormat!!.isNewLineAfterDeclaration) {
                println()
            }
        }
    }

    @Throws(IOException::class)
    protected open fun writeClose(qualifiedName: String) {
        writer.write("</")
        writer.write(qualifiedName)
        writer.write(">")
    }

    @Throws(IOException::class)
    protected open fun writeEmptyElementClose(qualifiedName: String) {
        // Simply close up
        if (!outputFormat!!.isExpandEmptyElements) {
            writer.write("/>")
        } else {
            writer.write("></")
            writer.write(qualifiedName)
            writer.write(">")
        }
    }

    protected val isExpandEmptyElements: Boolean
        protected get() = outputFormat!!.isExpandEmptyElements

    /**
     * This will take the pre-defined entities in XML 1.0 and convert their
     * character representation to the appropriate entity reference, suitable
     * for XML attributes.
     *
     * @param text
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected fun escapeElementEntities(text: String): String {
        var block: CharArray? = null
        var i: Int
        var last = 0
        val size = text.length
        i = 0
        while (i < size) {
            var entity: String? = null
            val c = text[i]
            when (c) {
                '<' -> entity = "&lt;"
                '>' -> entity = "&gt;"
                '&' -> entity = "&amp;"
                '\t', '\n', '\r' ->
                    // don't encode standard whitespace characters
                    if (preserve) {
                        entity = c.toString()
                    }

                else -> if (c.code < 32 || shouldEncodeChar(c)) {
                    entity = "&#" + c.code + ";"
                }
            }
            if (entity != null) {
                if (block == null) {
                    block = text.toCharArray()
                }
                buffer.append(block, last, i - last)
                buffer.append(entity)
                last = i + 1
            }
            i++
        }
        if (last == 0) {
            return text
        }
        if (last < size) {
            if (block == null) {
                block = text.toCharArray()
            }
            buffer.append(block, last, i - last)
        }
        val answer = buffer.toString()
        buffer.setLength(0)
        return answer
    }

    @Throws(IOException::class)
    protected fun writeEscapeAttributeEntities(txt: String?) {
        if (txt != null) {
            val escapedText = escapeAttributeEntities(txt)
            writer.write(escapedText)
        }
    }

    /**
     * This will take the pre-defined entities in XML 1.0 and convert their
     * character representation to the appropriate entity reference, suitable
     * for XML attributes.
     *
     * @param text
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected fun escapeAttributeEntities(text: String): String {
        val quote = outputFormat!!.attributeQuoteCharacter
        var block: CharArray? = null
        var i: Int
        var last = 0
        val size = text.length
        i = 0
        while (i < size) {
            var entity: String? = null
            val c = text[i]
            when (c) {
                '<' -> entity = "&lt;"
                '>' -> entity = "&gt;"
                '\'' -> if (quote == '\'') {
                    entity = "&apos;"
                }

                '\"' -> if (quote == '\"') {
                    entity = "&quot;"
                }

                '&' -> entity = "&amp;"
                '\t', '\n', '\r' -> {}
                else -> if (c.code < 32 || shouldEncodeChar(c)) {
                    entity = "&#" + c.code + ";"
                }
            }
            if (entity != null) {
                if (block == null) {
                    block = text.toCharArray()
                }
                buffer.append(block, last, i - last)
                buffer.append(entity)
                last = i + 1
            }
            i++
        }
        if (last == 0) {
            return text
        }
        if (last < size) {
            if (block == null) {
                block = text.toCharArray()
            }
            buffer.append(block, last, i - last)
        }
        val answer = buffer.toString()
        buffer.setLength(0)
        return answer
    }

    /**
     * Should the given character be escaped. This depends on the encoding of
     * the document.
     *
     * @param c
     * DOCUMENT ME!
     *
     * @return boolean
     */
    protected fun shouldEncodeChar(c: Char): Boolean {
        val max = maximumAllowedCharacter
        return max > 0 && c.code > max
    }

    /**
     * Returns the maximum allowed character code that should be allowed
     * unescaped which defaults to 127 in US-ASCII (7 bit) or 255 in ISO- (8
     * bit).
     *
     * @return DOCUMENT ME!
     */
    protected fun defaultMaximumAllowedCharacter(): Int {
        val encoding = outputFormat!!.encoding
        if (encoding != null) {
            if (encoding == "US-ASCII") {
                return 127
            }
        }

        // no encoding for things like ISO-*, UTF-8 or UTF-16
        return -1
    }

    protected fun isNamespaceDeclaration(ns: Namespace?): Boolean {
        if (ns != null && ns !== Namespace.XML_NAMESPACE) {
            val uri = ns.uri
            if (uri != null) {
                if (!namespaceStack.contains(ns)) {
                    return true
                }
            }
        }
        return false
    }

    @Throws(SAXException::class)
    protected fun handleException(e: IOException?) {
        throw SAXException(e)
    }

    // Laramie Crocker 4/8/2002 10:38AM
    fun resolveEntityRefs(): Boolean {
        return resolveEntityRefs
    }

    fun setResolveEntityRefs(resolve: Boolean) {
        resolveEntityRefs = resolve
    }

    companion object {
        private const val PAD_TEXT = " "
        protected val LEXICAL_HANDLER_NAMES = arrayOf(
            "http://xml.org/sax/properties/lexical-handler",
            "http://xml.org/sax/handlers/LexicalHandler"
        )
        protected val DEFAULT_FORMAT = OutputFormat()
    }
}
