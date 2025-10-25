package ceri.common.xml;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertPrivateConstructor;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Test;
import org.xml.sax.SAXException;
import ceri.common.test.Assert;
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
		Assert.thrown(() -> Xml.document(null));
	}

	@Test
	public void testUnvalidatedDocument() throws SAXException {
		var doc = Xml.unvalidatedDocument(xmlWithDtd);
		assertEquals(doc.getChildNodes().getLength(), 2);
	}

	@Test
	public void testExecute() {
		Assert.thrown(() -> Xml.execute(() -> Assert.throwIt(new ParserConfigurationException())));
		Assert.thrown(() -> Xml.execute(() -> Assert.throwIt(new IOException())));
	}
}
