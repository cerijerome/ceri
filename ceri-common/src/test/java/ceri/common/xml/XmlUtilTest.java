package ceri.common.xml;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.TestUtil.resource;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XmlUtilTest {
	private static final String xml = resource("test.xml");
	private static final String xmlWithDtd = resource("test-with-dtd.xml");

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(XmlUtil.class);
	}

	@Test
	public void testDocument() throws SAXException {
		Document doc = XmlUtil.document(xml);
		assertEquals(doc.getChildNodes().getLength(), 1);
		assertThrown(() -> XmlUtil.document(null));
	}

	@Test
	public void testUnvalidatedDocument() throws SAXException {
		Document doc = XmlUtil.unvalidatedDocument(xmlWithDtd);
		assertEquals(doc.getChildNodes().getLength(), 2);
	}

	@Test
	public void testExecute() {
		assertThrown(() -> XmlUtil.execute(() -> {
			throw new ParserConfigurationException();
		}));
		assertThrown(() -> XmlUtil.execute(() -> {
			throw new IOException();
		}));
	}

}
