
package cz.dynawest.jtexy;

import cz.dynawest.jtexy.dom4j.io.ProtectedHTMLWriter;
import junit.framework.TestCase;
import org.dom4j.dom.DOMElement;
import org.dom4j.dom.DOMText;

/**
 *
 * @author Ondrej Zizka
 */
public class ProtectedHTMLWriterTest extends TestCase {

	public void testProtectHTMLWriter() throws TexyException {


		DOMElement elm = new DOMElement("foo");
		elm.add( new DOMText("Ondra") );
		elm.add( new DOMElement("bar") );
		elm.add( new DOMText("Zizka") );

		Protector prot = new ProtectorArray();
		String str = ProtectedHTMLWriter.fromElement(elm, prot);
		//assertEquals("All wrong", "\u0017\u0017", str);
		//assertEquals("All wrong", "\u0014\u0018\u0014Ondra\u0014\u0019\u0014Zizka\u0014\u001A\u0014", str);

		str = prot.unprotectAll(str);
		assertEquals("All wrong", "<foo>Ondra<bar/>Zizka</foo>", str);


	}

}// class
