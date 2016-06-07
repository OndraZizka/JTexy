package cz.dynawest.jtexy.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.dom4j.util.NodeComparator;

/**
 *
 * @author Martin Večeřa
 */
public class ResourceUtils {

	public static InputStream loadResource(String res) {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream("cz/dynawest/jtexy/" + res);
	}

	public static String loadResourceAsString(String res) {
		InputStream is = loadResource(res);

		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		} catch (UnsupportedEncodingException ex) {
			Logger.getLogger(ResourceUtils.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		}

		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException ex) {
			Logger.getLogger(ResourceUtils.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			try {
				is.close();
			} catch (IOException ex) {
				Logger.getLogger(ResourceUtils.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		return sb.toString();
	}

	public static boolean compareXml(String expectedXml, String actualXml) {
		try {
			SAXReader reader = new SAXReader();
			Document expectedXmlDoc = reader.read(new StringReader(expectedXml));
			Document actualXmlDoc = reader.read(new StringReader(actualXml));
			NodeComparator comparator = new NodeComparator();

			return comparator.compare(expectedXmlDoc, actualXmlDoc) == 0;
		} catch (DocumentException ex) {
			Logger.getLogger(ResourceUtils.class.getName()).log(Level.SEVERE, null, ex);
			return false;
		}
	}

	private static String htmlBlock2Xml(String html) {
		return "<?xml version=\"1.0\"?>\n<html>\n" + html + "\n</html>";

	}

	public static boolean compareHtml(String expectedXml, String actualXml) {
		return compareXml(htmlBlock2Xml(expectedXml), htmlBlock2Xml(actualXml));
	}

}
