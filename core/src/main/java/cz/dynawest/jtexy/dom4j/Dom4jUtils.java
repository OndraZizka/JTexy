
package cz.dynawest.jtexy.dom4j;

import cz.dynawest.jtexy.TexyException;
import cz.dynawest.jtexy.JTexy;
import java.io.IOException;
import java.io.StringWriter;
import org.dom4j.dom.DOMElement;
import cz.dynawest.jtexy.dom4j.io.ProtectedHTMLWriter;

/**
 *
 * @author Ondrej Zizka
 */
public class Dom4jUtils {


	/**
	 * @returns concatenated element's text nodes, CDATA and entities.
	 */
	public static String getInnerText( DOMElement elm ){
		String str = elm.getText();
		return str;
	}



	/**
	 * DONE: Define what this  does and rewrite it.
	 * @deprecated
	 * @see ProtectedHTMLWriter#fromElement(org.dom4j.dom.DOMElement, cz.dynawest.jtexy.Protector) 
	 */
	public static String getInnerCode( JTexy texy, DOMElement elm ) throws TexyException {

		/*
		String str = elm.asXML();
		str = StringUtils.substringAfter(str, ">");
		str = StringUtils.substringBeforeLast(str, "<");
		str = StringEscapeUtils.unescapeHtml(str);
		return str;
		/**/

		// -- or --

		StringWriter sw = new StringWriter();
		ProtectedHTMLWriter myHw = new ProtectedHTMLWriter( texy.getProtector(), sw );
		try {
			myHw.write(elm);
		} catch (IOException ex) {
			throw new TexyException(ex);
		}
		return sw.toString();

		// -- or --

		/*
		//SAXWriter saxw = new SAXWriter( new , null)
		StringWriter sw = new StringWriter();
		HTMLWriter myHw = new ChildrenHTMLWriter( sw );
		try {
			myHw.write(elm);
		} catch (IOException ex) {
			throw new TexyException(ex);
		}
		return sw.toString();
		/**/

		// -- or --

		/* org.w3c.dom.DOMException: Not supported yet
		StringBuilder sb = new StringBuilder();
		NodeList nodes = elm.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = (Node) nodes.item(i);
			String str = node.asXML();
			sb.append( str );
		}
		return sb.toString();
		/**/

	}

}// class
