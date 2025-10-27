package ceri.common.xml;

import javax.xml.xpath.XPathException;
import org.junit.Test;
import ceri.common.test.Assert;

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
		Assert.privateConstructor(XPaths.class);
	}

	@Test
	public void testCompilingABadExpressionThrowsRuntimeException() {
		Assert.runtime(() -> XPaths.compile("-"));
	}

	@Test
	public void testNode() throws XPathException {
		var in = Xml.input(XML);
		var root = XPaths.node("*", in);
		var node = XPaths.node("//c1/text()", root);
		Assert.equal(node.getTextContent(), "C11");
		in = Xml.input(XML);
		Assert.isNull(XPaths.node("//d0", in));
		Assert.isNull(XPaths.node("//d0", root));
	}

	@Test
	public void testNodeListFromInputSource() throws XPathException {
		var in = Xml.input(XML);
		var nodes = XPaths.nodeList("//*[@n='0']", in);
		Assert.equal(nodes.size(), 3);
		Assert.equal(nodes.get(0).getNodeName(), "a0");
		Assert.equal(nodes.get(1).getNodeName(), "b0");
		Assert.equal(nodes.get(2).getNodeName(), "b0");
	}

	@Test
	public void testNodeListFromNode() throws XPathException {
		var in = Xml.input(XML);
		var root = XPaths.node("*", in);
		Assert.equal(root.getNodeName(), "a0");
		var nodes = XPaths.nodeList("//*[text()='B0']/following-sibling::*", root);
		Assert.equal(nodes.size(), 3);
		Assert.equal(nodes.get(0).getNodeName(), "b1");
		Assert.equal(nodes.get(1).getNodeName(), "b0");
		Assert.equal(nodes.get(2).getNodeName(), "b1");
	}
}
