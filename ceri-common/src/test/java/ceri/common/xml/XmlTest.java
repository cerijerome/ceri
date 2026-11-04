package ceri.common.xml;

import org.junit.jupiter.api.Test;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class XmlTest {
	private static final String xml = Testing.resource("test.xml");
	private static final String xmlWithDtd = Testing.resource("test-with-dtd.xml");

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Xml.class);
	}

	@Test
	public void testDocument() {
		var doc = Xml.Dom.document(xml);
		Assert.equal(doc.getChildNodes().getLength(), 1);
		Assert.thrown(() -> Xml.Dom.document(null));
	}

	@Test
	public void testUnvalidatedDocument() {
		var doc = Xml.Dom.unvalidatedDocument(xmlWithDtd);
		Assert.equal(doc.getChildNodes().getLength(), 2);
	}

	@Test
	public void testEncode() {
		Assert.string(Xml.encode(null), null);
		Assert.string(Xml.encode(""), "");
		Assert.same(Xml.encode("test"), "test");
		Assert.string(Xml.encode("<\0'a' &\t\"b\">"),
			"&lt;?&apos;a&apos; &amp;\t&quot;b&quot;&gt;");
	}
}
