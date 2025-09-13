package ceri.ent.htmlunit;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import ceri.common.reflect.Reflect;

public class XPathUtil {
	private static final Pattern WHITESPACE = Pattern.compile("\\p{Zs}+");

	private XPathUtil() {}

	public static String attribute(DomElement dom, String attributeName) {
		String attribute = dom.getAttribute(attributeName);
		if (DomElement.ATTRIBUTE_NOT_DEFINED.equals(attribute)) return null;
		return attribute;
	}

	public static List<DomNode> xPaths(DomNode dom, String xPath) {
		return Reflect.unchecked(dom.getByXPath(xPath));
	}

	public static List<DomElement> xPathElements(DomNode dom, String xPath) {
		return Reflect.unchecked(dom.getByXPath(xPath));
	}

	public static String xPathText(DomNode node, String xPath) {
		return textContent(node.getFirstByXPath(xPath));
	}

	public static List<String> xPathTexts(DomNode dom, String xPath) {
		List<String> texts = new ArrayList<>();
		for (Object obj : dom.getByXPath(xPath)) {
			String s = textContent((DomNode) obj);
			if (s != null && s.length() > 0) texts.add(s);
		}
		return texts;
	}

	public static String textContent(DomNode dom) {
		if (dom == null) return null;
		return trim(dom.getTextContent());
	}

	public static String trim(String s) {
		if (s == null) return null;
		s = WHITESPACE.matcher(s).replaceAll(" ");
		return s.trim();
	}
}
