package ceri.common.xml;

import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.resource;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XmlUtilTest {
	private static final String xml = resource("test.xml");
	private static final String xmlWithDtd = resource("test-with-dtd.xml");

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(XmlUtil.class);
		assertPrivateConstructor(XercesConstants.class);
	}

	@Test
	public void testDocument() throws SAXException {
		Document doc = XmlUtil.document(xml);
		assertThat(doc.getChildNodes().getLength(), is(1));
		assertException(() -> XmlUtil.document(null));
	}

	@Test
	public void testUnvalidatedDocument() throws SAXException {
		Document doc = XmlUtil.unvalidatedDocument(xmlWithDtd);
		assertThat(doc.getChildNodes().getLength(), is(2));
	}

}
