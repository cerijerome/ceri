package ceri.common.xml;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertRte;
import javax.xml.xpath.XPathException;
import org.junit.Test;

public class XPathsTest {
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
		assertPrivateConstructor(XPaths.class);
	}

	@Test
	public void testCompilingABadExpressionThrowsRuntimeException() {
		assertRte(() -> XPaths.compile("-"));
	}

	@Test
	public void testNode() throws XPathException {
		var in = Xml.input(XML);
		var root = XPaths.node("*", in);
		var node = XPaths.node("//c1/text()", root);
		assertEquals(node.getTextContent(), "C11");
		in = Xml.input(XML);
		assertNull(XPaths.node("//d0", in));
		assertNull(XPaths.node("//d0", root));
	}

	@Test
	public void testNodeListFromInputSource() throws XPathException {
		var in = Xml.input(XML);
		var nodes = XPaths.nodeList("//*[@n='0']", in);
		assertEquals(nodes.size(), 3);
		assertEquals(nodes.get(0).getNodeName(), "a0");
		assertEquals(nodes.get(1).getNodeName(), "b0");
		assertEquals(nodes.get(2).getNodeName(), "b0");
	}

	@Test
	public void testNodeListFromNode() throws XPathException {
		var in = Xml.input(XML);
		var root = XPaths.node("*", in);
		assertEquals(root.getNodeName(), "a0");
		var nodes = XPaths.nodeList("//*[text()='B0']/following-sibling::*", root);
		assertEquals(nodes.size(), 3);
		assertEquals(nodes.get(0).getNodeName(), "b1");
		assertEquals(nodes.get(1).getNodeName(), "b0");
		assertEquals(nodes.get(2).getNodeName(), "b1");
	}
}
