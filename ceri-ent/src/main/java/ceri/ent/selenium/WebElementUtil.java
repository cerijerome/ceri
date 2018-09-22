package ceri.ent.selenium;

import static ceri.common.collection.StreamUtil.toList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import org.openqa.selenium.WebElement;

public class WebElementUtil {
	private static final Pattern WHITESPACE = Pattern.compile("\\p{Zs}+");
	private static final String OUTER_HTML = "outerHTML";
	private static final String INNER_HTML = "innerHTML";

	private WebElementUtil() {}

	public static String innerHtml(WebElement element) {
		if (element == null) return null;
		return trim(element.getAttribute(INNER_HTML));
	}

	public static String outerHtml(WebElement element) {
		if (element == null) return null;
		return trim(element.getAttribute(OUTER_HTML));
	}

	public static String text(WebElement element) {
		if (element == null) return null;
		return trim(element.getText());
	}

	public static List<String> innerHtmls(Collection<WebElement> elements) {
		if (elements == null) return null;
		return toList(elements.stream().map(WebElementUtil::innerHtml));
	}

	public static List<String> outerHtmls(Collection<WebElement> elements) {
		if (elements == null) return null;
		return toList(elements.stream().map(WebElementUtil::outerHtml));
	}

	public static List<String> texts(Collection<WebElement> elements) {
		if (elements == null) return null;
		return toList(elements.stream().map(WebElementUtil::text));
	}

	public static String trim(String s) {
		if (s == null) return null;
		return WHITESPACE.matcher(s).replaceAll(" ").trim();
	}

}
