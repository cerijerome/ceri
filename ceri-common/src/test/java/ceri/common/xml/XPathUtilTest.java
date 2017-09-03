package ceri.common.xml;

import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
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
		assertException(RuntimeException.class, () -> XPathUtil.compile("-"));
	}

	@Test
	public void testNode() throws XPathException {
		InputSource in = XmlUtil.input(XML);
		Node root = XPathUtil.node("*", in);
		Node node = XPathUtil.node("//c1/text()", root);
		assertThat(node.getTextContent(), is("C11"));
		in = XmlUtil.input(XML);
		assertNull(XPathUtil.node("//d0", in));
		assertNull(XPathUtil.node("//d0", root));
	}

	@Test
	public void testNodeListFromInputSource() throws XPathException {
		InputSource in = XmlUtil.input(XML);
		List<Node> nodes = XPathUtil.nodeList("//*[@n='0']", in);
		assertThat(nodes.size(), is(3));
		assertThat(nodes.get(0).getNodeName(), is("a0"));
		assertThat(nodes.get(1).getNodeName(), is("b0"));
		assertThat(nodes.get(2).getNodeName(), is("b0"));
	}

	@Test
	public void testNodeListFromNode() throws XPathException {
		InputSource in = XmlUtil.input(XML);
		Node root = XPathUtil.node("*", in);
		assertThat(root.getNodeName(), is("a0"));
		List<Node> nodes = XPathUtil.nodeList("//*[text()='B0']/following-sibling::*", root);
		assertThat(nodes.size(), is(3));
		assertThat(nodes.get(0).getNodeName(), is("b1"));
		assertThat(nodes.get(1).getNodeName(), is("b0"));
		assertThat(nodes.get(2).getNodeName(), is("b1"));
	}

}
