package ceri.common.xml;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XPathUtil {
	private static final XPath XPATH = XPathFactory.newInstance().newXPath();

	private XPathUtil() {}

	public static XPathExpression compile(String expression) {
		try {
			return XPATH.compile(expression);
		} catch (XPathExpressionException e) {
			throw new RuntimeXPathException(e);
		}
	}

	public static InputSource input(String s) {
		return new InputSource(new StringReader(s));
	}
	
	public static Node node(String expression, InputSource in) 
		throws XPathExpressionException {
		return node(compile(expression), in);
	}

	public static Node node(String expression, Object dom) 
		throws XPathExpressionException {
		return node(compile(expression), dom);
	}

	public static Node node(XPathExpression expression, InputSource in)
		throws XPathExpressionException {
		List<Node> nodes = nodeList(expression, in);
		if (nodes.isEmpty()) return null;
		return nodes.get(0);
	}
	
	public static Node node(XPathExpression expression, Object dom)
		throws XPathExpressionException {
		List<Node> nodes = nodeList(expression, dom);
		if (nodes.isEmpty()) return null;
		return nodes.get(0);
	}
	
	public static List<Node> nodeList(String expression, InputSource in)
		throws XPathExpressionException {
		return nodeList(compile(expression), in);
	}
	
	public static List<Node> nodeList(String expression, Object dom)
		throws XPathExpressionException {
		return nodeList(compile(expression), dom);
	}
	
	public static List<Node> nodeList(XPathExpression expression, InputSource in)
		throws XPathExpressionException {
		return nodeList((NodeList) expression.evaluate(in, XPathConstants.NODESET));
	}

	public static List<Node> nodeList(XPathExpression expression, Object dom)
		throws XPathExpressionException {
		return nodeList((NodeList) expression.evaluate(dom, XPathConstants.NODESET));
	}

	private static List<Node> nodeList(NodeList nodeList) {
		int length = nodeList.getLength();
		List<Node> nodes = new ArrayList<>(length);
		for (int i = 0; i < length; i++)
			nodes.add(nodeList.item(i));
		return nodes;
	}

}
