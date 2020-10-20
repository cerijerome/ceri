package ceri.common.xml;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import java.util.List;
import javax.xml.xpath.XPathException;
import org.junit.Test;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class XPathUtilTest {
	private static final String XML = "<a0 n='0'>" + //
		"	A" + //
		"	<b0 n='0'>B0</b0>" + //
		"	<b1 n='1'>B1</b1>" + //
		"	<b0 n='0'>" + //
		"		<c1 n='1'>C11</c1>" + //
		"		<c2 n='2'>C21</c2>" + //
		"	</b0>" + //
		"	<b1>" + //
		"		<c1 n='1'>C12</c1>" + //
		"	</b1>" + //
		"</a0>"; //

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(XPathUtil.class);
	}

	@Test
	public void testCompilingABadExpressionThrowsRuntimeException() {
		assertThrown(RuntimeException.class, () -> XPathUtil.compile("-"));
	}

	@Test
	public void testNode() throws XPathException {
		InputSource in = XmlUtil.input(XML);
		Node root = XPathUtil.node("*", in);
		Node node = XPathUtil.node("//c1/text()", root);
		assertEquals(node.getTextContent(), "C11");
		in = XmlUtil.input(XML);
		assertNull(XPathUtil.node("//d0", in));
		assertNull(XPathUtil.node("//d0", root));
	}

	@Test
	public void testNodeListFromInputSource() throws XPathException {
		InputSource in = XmlUtil.input(XML);
		List<Node> nodes = XPathUtil.nodeList("//*[@n='0']", in);
		assertEquals(nodes.size(), 3);
		assertEquals(nodes.get(0).getNodeName(), "a0");
		assertEquals(nodes.get(1).getNodeName(), "b0");
		assertEquals(nodes.get(2).getNodeName(), "b0");
	}

	@Test
	public void testNodeListFromNode() throws XPathException {
		InputSource in = XmlUtil.input(XML);
		Node root = XPathUtil.node("*", in);
		assertEquals(root.getNodeName(), "a0");
		List<Node> nodes = XPathUtil.nodeList("//*[text()='B0']/following-sibling::*", root);
		assertEquals(nodes.size(), 3);
		assertEquals(nodes.get(0).getNodeName(), "b1");
		assertEquals(nodes.get(1).getNodeName(), "b0");
		assertEquals(nodes.get(2).getNodeName(), "b1");
	}

}
