package ceri.ent.htmlunit;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import ceri.common.util.BasicUtil;
import com.gargoylesoftware.htmlunit.html.DomNode;

public class XPathUtil {
	private static final Pattern WHITESPACE = Pattern.compile("\\p{Zs}+");

	private XPathUtil() {}

	public static List<DomNode> xPaths(DomNode dom, String xPath) {
		return BasicUtil.<List<DomNode>>uncheckedCast(dom.getByXPath(xPath));
	}

	public static String xPathText(DomNode node, String xPath) {
		DomNode dom = node.getFirstByXPath(xPath);
		if (dom == null) return null;
		return trim(dom.getTextContent());
	}

	public static List<String> xPathTexts(DomNode dom, String xPath) {
		List<String> texts = new ArrayList<>();
		for (Object obj : dom.getByXPath(xPath)) {
			DomNode node = (DomNode) obj;
			String s = trim(node.getTextContent());
			if (s != null && s.length() > 0) texts.add(s);
		}
		return texts;
	}

	public static String trim(String s) {
		if (s == null) return null;
		s = WHITESPACE.matcher(s).replaceAll(" ");
		return s.trim();
	}

}
