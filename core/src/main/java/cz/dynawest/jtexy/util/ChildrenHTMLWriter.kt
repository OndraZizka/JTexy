
package cz.dynawest.jtexy.util;

import java.io.IOException;
import java.io.Writer;
import org.dom4j.io.HTMLWriter;
import org.dom4j.io.OutputFormat;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This mutation of HTML writer will only write child nodes of the root.
 * E.g.:   <p>Hi</p> instead of <!DOCTYPE ...><html><p>Hi</p></html>
 *
 * @author Ondrej Zizka
 */
final class ChildrenHTMLWriter extends HTMLWriter {

	int depth = 0;

	Writer activeWriter = voidWriter;
	
	Writer realWriter;
	
	// No-op writer for nodes which will not be written.
	private static Writer voidWriter = new Writer() {
		@Override	public void write(char[] cbuf, int off, int len) throws IOException {	}
		@Override	public void flush() throws IOException { }
		@Override	public void close() throws IOException { }
	};


	// -- Const --

	public ChildrenHTMLWriter(Writer writer, OutputFormat format) {
		super(voidWriter, format);
		this.realWriter = writer;
	}

	public ChildrenHTMLWriter(Writer writer) {
		super(voidWriter);
		this.realWriter = writer;
	}


	// -- Overrides --

	/** Upon root element start, switch to real writer. */
	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(namespaceURI, localName, qName, attributes);
		if( ++depth == 1 )
			activeWriter = realWriter;
	}

	/** Upon root element end, switch back to void writer. */
	@Override
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
		if( --depth == 0 )
			activeWriter = voidWriter;
		super.endElement(namespaceURI, localName, qName);
	}









}
