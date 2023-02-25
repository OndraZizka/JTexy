package cz.dynawest.jtexy.dom4j

import cz.dynawest.jtexy.JTexy
import cz.dynawest.jtexy.TexyException
import cz.dynawest.jtexy.dom4j.io.ProtectedHTMLWriter
import org.dom4j.dom.DOMElement
import java.io.*

/**
 *
 * @author Ondrej Zizka
 */
object Dom4jUtils {
    /**
     * @returns concatenated element's text nodes, CDATA and entities.
     */
    fun getInnerText(elm: DOMElement): String {
        return elm.text
    }

    /**
     * DONE: Define what this  does and rewrite it.
     * @see ProtectedHTMLWriter.fromElement
     */
    @Deprecated(" ")
    @Throws(TexyException::class)
    fun getInnerCode(texy: JTexy, elm: DOMElement?): String {

        /*
		String str = elm.asXML();
		str = StringUtils.substringAfter(str, ">");
		str = StringUtils.substringBeforeLast(str, "<");
		str = StringEscapeUtils.unescapeHtml(str);
		return str;
		/ **/

        // -- or --
        val sw = StringWriter()
        val myHw = ProtectedHTMLWriter(texy.protector, sw)
        try {
            myHw.write(elm)
        } catch (ex: IOException) {
            throw TexyException(ex)
        }
        return sw.toString()

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
		/ **/

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
		/ **/
    }
}
