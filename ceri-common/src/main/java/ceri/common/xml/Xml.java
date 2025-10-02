package ceri.common.xml;

import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Xml {
	private static final String FEATURE_URL = "http://apache.org/xml/features/";
	public static final String LOAD_DTD_GRAMMAR = FEATURE_URL + "nonvalidating/load-dtd-grammar";
	public static final String LOAD_EXTERNAL_DTD = FEATURE_URL + "nonvalidating/load-external-dtd";

	private Xml() {}

	interface DocumentSupplier {
		Document document() throws IOException, SAXException, ParserConfigurationException;
	}

	public static InputSource input(String s) {
		return new InputSource(new StringReader(s));
	}

	/**
	 * Creates an xml document object.
	 */
	public static Document document(String s) throws SAXException {
		return execute(() -> {
			var dbf = DocumentBuilderFactory.newInstance();
			var builder = dbf.newDocumentBuilder();
			return builder.parse(input(s));
		});
	}

	/**
	 * Creates an xml document object that doesn't validate DTDs.
	 */
	public static Document unvalidatedDocument(String s) throws SAXException {
		return execute(() -> {
			var dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			dbf.setNamespaceAware(true);
			dbf.setFeature(LOAD_DTD_GRAMMAR, false);
			dbf.setFeature(LOAD_EXTERNAL_DTD, false);
			var builder = dbf.newDocumentBuilder();
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
}
