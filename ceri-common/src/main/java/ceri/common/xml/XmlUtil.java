package ceri.common.xml;

import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XmlUtil {

	private XmlUtil() {}

	public static InputSource input(String s) {
		return new InputSource(new StringReader(s));
	}

	/**
	 * Creates an xml document object.
	 */
	public static Document document(String s) throws SAXException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = dbf.newDocumentBuilder();
			return builder.parse(input(s));
		} catch (IOException | ParserConfigurationException e) {
			throw new IllegalStateException("Should not happen", e);
		}
	}

	/**
	 * Creates an xml document object that doesn't validate DTDs.
	 */
	public static Document unvalidatedDocument(String s) throws SAXException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			dbf.setNamespaceAware(true);
			dbf.setFeature(XercesConstants.LOAD_DTD_GRAMMAR_FEATURE, false);
			dbf.setFeature(XercesConstants.LOAD_EXTERNAL_DTD_FEATURE, false);
			DocumentBuilder builder = dbf.newDocumentBuilder();
			return builder.parse(input(s));
		} catch (IOException | ParserConfigurationException e) {
			throw new IllegalStateException("Should not happen", e);
		}
	}

}