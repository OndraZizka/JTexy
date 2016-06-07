
package cz.dynawest.jtexy.dom4j.io;

import cz.dynawest.jtexy.Protector;
import cz.dynawest.jtexy.Constants;
import cz.dynawest.jtexy.ContentType;
import java.io.StringWriter;
import org.dom4j.io.OutputFormat;




import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.dom4j.*;
import org.dom4j.dom.DOMElement;



/**
 * For the purposes of JTexy: Protects HTML tags 
 * using JTexy "protector" (see the Protector class).
 *
 * TBD: Optimize the writers - figure out how to use the same one
 *      for the whole serialization (should be possible).
 *      But not much important as the tree depth usually does not go over 3.
 *
 * @author Ondrej Zizka
 */
public class ProtectedHTMLWriter extends XMLWriter {

	private String HOLDER_ELEMENT_NAME = null;
	public String getHolderElementName() {		return HOLDER_ELEMENT_NAME;	}
	public void setHolderElementName(String name) {		this.HOLDER_ELEMENT_NAME = name;	}
	
	private final Protector protector;



	// --- Const --- //

	private void init(){
		this.setEscapeText( false );
		this.getOutputFormat().setXHTML( true );
		this.HOLDER_ELEMENT_NAME = Constants.HOLDER_ELEMENT_NAME; // I don't like this dep.
	}


	public ProtectedHTMLWriter(Protector protector, OutputStream out) throws UnsupportedEncodingException {
		super(out, OutputFormat.createPrettyPrint());
		this.protector = protector;
		this.init();
	}

	public ProtectedHTMLWriter(Protector protector, Writer writer) {
		super(writer, OutputFormat.createPrettyPrint());
		this.protector = protector;
		this.init();
	}

	/** Automatically takes the Protector and HOLDER_ELEMENT_NAME from the texy instance. */
	/*public ProtectedHTMLWriter(JTexy texy, Writer writer) {
		super(writer);
		this.protector = texy.getProtector();
		this.HOLDER_ELEMENT_NAME = texy.HOLDER_ELEMENT_NAME;
	}*/



	// --- Overrides --- //


	/**
	 *  Ignore Document stuff; In JTexy, we only care about a simple elements tree.
	 */
	@Override	public void write(Document doc) throws IOException {
		this.write( doc.getRootElement() );
	}



	/**
	 *  Write text as-is.
	 *
	 * Not used - we set this.setEscapeText( false ) in the constructor instead.
	 */
	//@Override protected void writeString(String text) throws IOException { }




	/**
	 * Writes tags as protected strings. Ommits "holder" elements.
	 */
	@Override	public void writeOpen(Element element) throws IOException {

		/* Don't write "holder" elements, which only serve as a container for sub-elements. */
		if( element.getName() == null ? this.HOLDER_ELEMENT_NAME == null : element.getName().equals(this.HOLDER_ELEMENT_NAME) )
			return;

		// Shade the real writer, provide a fake one.
		Writer realWriter = this.writer;
		this.writer = new StringWriter();

		// Get the content type.
		ContentType type = ContentType.fromElement(element);

		super.writeOpen(element);
		String protectedTag = this.writer.toString();

		// Unshade.
		realWriter.write( this.protector.protect( protectedTag, type ) );
		
		this.writer = realWriter;
	}


	/**
	 * Writes tags as protected strings. Ommits "holder" elements.
	 */
	@Override	public void writeClose(Element element) throws IOException {
		
		/* Don't write "holder" elements, which only serve as a container for sub-elements. */
		if( element.getName() == null ? this.HOLDER_ELEMENT_NAME == null : element.getName().equals(this.HOLDER_ELEMENT_NAME) )
			return;

		/* Don't write closing element of empty elements. TODO: Some elements should be closed even if empty - eg. <script>. */
		if( ! element.hasContent() )
			return;


		// Shade the real writer, provide a fake one.
		Writer realWriter = this.writer;
		this.writer = new StringWriter();
		super.writeClose(element);
		String protectedTag = this.writer.toString();

		// Get the content type.
		ContentType type = ContentType.fromElement( element );
		realWriter.write( this.protector.protect( protectedTag, type ) );
		this.writer = realWriter;
	}




	/**
	 * Encodes the element using protected strings.
	 */
	public static String fromElement( DOMElement elm, Protector protector ){

		StringWriter sw = new StringWriter();
		ProtectedHTMLWriter protectedHtmlWriter = new ProtectedHTMLWriter( protector, sw );
		try {
			// Write the DOM tree!
			protectedHtmlWriter.write(elm);
		} catch (IOException ex) {
			// Will hardly happen with StringWriter.
			throw new RuntimeException("IOEx with StringWriter?", ex);
		}
		return sw.toString();
		
	}




	/**
	 * Copied from XMLWriter, simplified and modified to call writeOpen() and writeClose().
	 */
	@Override
	protected void writeElement(Element element) throws IOException {

		lastOutputNodeType = Node.ELEMENT_NODE;


		String qualifiedName = element.getQualifiedName();

		// Indentation

		//writePrintln();
		// TBD: Store to the element, but where?
		if( element.getName() != Constants.HOLDER_ELEMENT_NAME
						&& ContentType.BLOCK == ContentType.fromElement(element) )
			writer.append( format.getLineSeparator() );
		indent();


		int size = element.nodeCount();

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
		writeOpen(element);

		// Inner nodes.
		if( size > 0 ) {

			// For JTexy, we don't need formatting. Perhaps we should rather modify XMLWriter?
			writeElementContent(element);

			writeClose(element);
		}

		// remove declared namespaceStack from stack
		/*while (namespaceStack.size() > previouslyDeclaredNamespaces) {
			namespaceStack.pop();
		}/**/

		lastOutputNodeType = Node.ELEMENT_NODE;
	}






}// class
