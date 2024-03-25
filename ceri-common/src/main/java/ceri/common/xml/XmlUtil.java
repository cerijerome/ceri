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
	private static final String XERCES_FEATURE_PREFIX = "http://apache.org/xml/features/";
	public static final String XERCES_LOAD_DTD_GRAMMAR_FEATURE =
		XERCES_FEATURE_PREFIX + "nonvalidating/load-dtd-grammar";
	public static final String XERCES_LOAD_EXTERNAL_DTD_FEATURE =
		XERCES_FEATURE_PREFIX + "nonvalidating/load-external-dtd";

	private XmlUtil() {}

	public static InputSource input(String s) {
		return new InputSource(new StringReader(s));
	}

	/**
	 * Creates an xml document object.
	 */
	public static Document document(String s) throws SAXException {
		return execute(() -> {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = dbf.newDocumentBuilder();
			return builder.parse(input(s));
		});
	}

	/**
	 * Creates an xml document object that doesn't validate DTDs.
	 */
	public static Document unvalidatedDocument(String s) throws SAXException {
		return execute(() -> {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			dbf.setNamespaceAware(true);
			dbf.setFeature(XERCES_LOAD_DTD_GRAMMAR_FEATURE, false);
			dbf.setFeature(XERCES_LOAD_EXTERNAL_DTD_FEATURE, false);
			DocumentBuilder builder = dbf.newDocumentBuilder();
			return builder.parse(input(s));
		});
	}

	static Document execute(DocumentSupplier supplier) throws SAXException {
		try {
			return supplier.document();
		} catch (IOException | ParserConfigurationException e) {
			throw new IllegalStateException("Should not happen", e);
		}
	}

	interface DocumentSupplier {
		Document document() throws IOException, SAXException, ParserConfigurationException;
	}

}
