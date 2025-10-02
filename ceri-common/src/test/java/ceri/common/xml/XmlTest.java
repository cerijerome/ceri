package ceri.common.xml;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Test;
import org.xml.sax.SAXException;
import ceri.common.test.TestUtil;

public class XmlTest {
	private static final String xml = TestUtil.resource("test.xml");
	private static final String xmlWithDtd = TestUtil.resource("test-with-dtd.xml");

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Xml.class);
	}

	@Test
	public void testDocument() throws SAXException {
		var doc = Xml.document(xml);
		assertEquals(doc.getChildNodes().getLength(), 1);
		assertThrown(() -> Xml.document(null));
	}

	@Test
	public void testUnvalidatedDocument() throws SAXException {
		var doc = Xml.unvalidatedDocument(xmlWithDtd);
		assertEquals(doc.getChildNodes().getLength(), 2);
	}

	@Test
	public void testExecute() {
		assertThrown(() -> Xml.execute(() -> {
			throw new ParserConfigurationException();
		}));
		assertThrown(() -> Xml.execute(() -> {
			throw new IOException();
		}));
	}
}
