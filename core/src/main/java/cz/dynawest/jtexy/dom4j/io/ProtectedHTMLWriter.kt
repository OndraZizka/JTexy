package cz.dynawest.jtexy.dom4j.io

import cz.dynawest.jtexy.*
import org.dom4j.*
import org.dom4j.dom.DOMElement
import org.dom4j.io.OutputFormat
import java.io.*

/**
 * For the purposes of JTexy: Protects HTML tags
 * using JTexy "protector" (see the Protector class).
 *
 * TBD: Optimize the writers - figure out how to use the same one
 * for the whole serialization (should be possible).
 * But not much important as the tree depth usually does not go over 3.
 *
 * @author Ondrej Zizka
 */
class ProtectedHTMLWriter : XMLWriter {
    var holderElementName: String? = null
    private val protector: Protector?

    // --- Const --- //
    private fun init() {
        this.isEscapeText = false
        this.outputFormat.isXHTML = true
        holderElementName = Constants.HOLDER_ELEMENT_NAME // I don't like this dep.
    }

    constructor(protector: Protector?, out: OutputStream?) : super(out, OutputFormat.createPrettyPrint()) {
        this.protector = protector
        init()
    }

    constructor(protector: Protector?, writer: Writer) : super(writer, OutputFormat.createPrettyPrint()) {
        this.protector = protector
        init()
    }
    /** Automatically takes the Protector and HOLDER_ELEMENT_NAME from the texy instance.  */ /*public ProtectedHTMLWriter(JTexy texy, Writer writer) {
		super(writer);
		this.protector = texy.getProtector();
		this.HOLDER_ELEMENT_NAME = texy.HOLDER_ELEMENT_NAME;
	}*/
    // --- Overrides --- //
    /**
     * Ignore Document stuff; In JTexy, we only care about a simple elements tree.
     */
    @Throws(IOException::class)
    override fun write(doc: Document) {
        this.write(doc.rootElement)
    }
    /**
     * Write text as-is.
     *
     * Not used - we set this.setEscapeText( false ) in the constructor instead.
     */
    //@Override protected void writeString(String text) throws IOException { }
    /**
     * Writes tags as protected strings. Ommits "holder" elements.
     */
    @Throws(IOException::class)
    override fun writeOpen(element: Element) {

        /* Don't write "holder" elements, which only serve as a container for sub-elements. */
        if (if (element.name == null) holderElementName == null else element.name == holderElementName) return

        // Shade the real writer, provide a fake one.
        val realWriter = writer
        writer = StringWriter()

        // Get the content type.
        val type: ContentType = ContentType.Companion.fromElement(element)
        super.writeOpen(element)
        val protectedTag = writer.toString()

        // Unshade.
        realWriter!!.write(protector!!.protect(protectedTag, type))
        writer = realWriter
    }

    /**
     * Writes tags as protected strings. Ommits "holder" elements.
     */
    @Throws(IOException::class)
    override fun writeClose(element: Element) {

        /* Don't write "holder" elements, which only serve as a container for sub-elements. */
        if (if (element.name == null) holderElementName == null else element.name == holderElementName) return

        /* Don't write closing element of empty elements. TODO: Some elements should be closed even if empty - eg. <script>. */if (!element.hasContent()) return


        // Shade the real writer, provide a fake one.
        val realWriter = writer
        writer = StringWriter()
        super.writeClose(element)
        val protectedTag = writer.toString()

        // Get the content type.
        val type: ContentType = ContentType.Companion.fromElement(element)
        realWriter!!.write(protector!!.protect(protectedTag, type))
        writer = realWriter
    }

    /**
     * Copied from XMLWriter, simplified and modified to call writeOpen() and writeClose().
     */
    @Throws(IOException::class)
    override fun writeElement(element: Element) {
        lastOutputNodeType = Node.ELEMENT_NODE.toInt()
        val qualifiedName = element.qualifiedName

        // Indentation

        //writePrintln();
        // TBD: Store to the element, but where?
        if (element.name !== Constants.HOLDER_ELEMENT_NAME
            && ContentType.BLOCK == ContentType.Companion.fromElement(element)
        ) writer.append(format.lineSeparator)
        indent()
        val size = element.nodeCount()

        // Start tag
        /*
		writer.write("<");
		writer.write(qualifiedName);
		// Removed namespace stuff...
		writeAttributes(element);

		if( size <= 0 ) {
			writeEmptyElementClose(qualifiedName);
		} else {
			writer.write(">");
		}
		 */

        // This should also close the element if empty. Check it.
        writeOpen(element)

        // Inner nodes.
        if (size > 0) {

            // For JTexy, we don't need formatting. Perhaps we should rather modify XMLWriter?
            writeElementContent(element)
            writeClose(element)
        }

        // remove declared namespaceStack from stack
        /*while (namespaceStack.size() > previouslyDeclaredNamespaces) {
			namespaceStack.pop();
		}/ **/lastOutputNodeType = Node.ELEMENT_NODE.toInt()
    }

    companion object {
        /**
         * Encodes the element using protected strings.
         */
        @JvmStatic
        fun fromElement(elm: DOMElement?, protector: Protector?): String {
            val sw = StringWriter()
            val protectedHtmlWriter = ProtectedHTMLWriter(protector, sw)
            try {
                // Write the DOM tree!
                protectedHtmlWriter.write(elm)
            } catch (ex: IOException) {
                // Will hardly happen with StringWriter.
                throw RuntimeException("IOEx with StringWriter?", ex)
            }
            return sw.toString()
        }
    }
}
