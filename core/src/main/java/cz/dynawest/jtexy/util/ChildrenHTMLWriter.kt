package cz.dynawest.jtexy.util

import org.dom4j.io.HTMLWriter
import org.dom4j.io.OutputFormat
import org.xml.sax.Attributes
import org.xml.sax.SAXException
import java.io.*

/**
 * This mutation of HTML writer will only write child nodes of the root.
 * E.g.:
 *
 *Hi instead of  <html>
 *
 *Hi</html>
 *
 * @author Ondrej Zizka
 */
internal class ChildrenHTMLWriter : HTMLWriter {
    var depth = 0
    var activeWriter = voidWriter
    var realWriter: Writer

    // -- Const --
    constructor(writer: Writer, format: OutputFormat?) : super(voidWriter, format) {
        realWriter = writer
    }

    constructor(writer: Writer) : super(voidWriter) {
        realWriter = writer
    }
    // -- Overrides --
    /** Upon root element start, switch to real writer.  */
    @Throws(SAXException::class)
    override fun startElement(namespaceURI: String, localName: String, qName: String, attributes: Attributes) {
        super.startElement(namespaceURI, localName, qName, attributes)
        if (++depth == 1) activeWriter = realWriter
    }

    /** Upon root element end, switch back to void writer.  */
    @Throws(SAXException::class)
    override fun endElement(namespaceURI: String, localName: String, qName: String) {
        if (--depth == 0) activeWriter = voidWriter
        super.endElement(namespaceURI, localName, qName)
    }

    companion object {
        // No-op writer for nodes which will not be written.
        private val voidWriter: Writer = object : Writer() {
            @Throws(IOException::class)
            override fun write(cbuf: CharArray, off: Int, len: Int) {
            }

            @Throws(IOException::class)
            override fun flush() {
            }

            @Throws(IOException::class)
            override fun close() {
            }
        }
    }
}
