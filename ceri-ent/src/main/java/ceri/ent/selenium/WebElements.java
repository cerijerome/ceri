package ceri.ent.selenium;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import org.openqa.selenium.WebElement;
import ceri.common.collect.Immutable;

public class WebElements {
	private static final Pattern WHITESPACE = Pattern.compile("\\p{Zs}+");
	private static final String OUTER_HTML = "outerHTML";
	private static final String INNER_HTML = "innerHTML";

	private WebElements() {}

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
		return Immutable.adaptList(WebElements::innerHtml, elements);
	}

	public static List<String> outerHtmls(Collection<WebElement> elements) {
		if (elements == null) return null;
		return Immutable.adaptList(WebElements::outerHtml, elements);
	}

	public static List<String> texts(Collection<WebElement> elements) {
		if (elements == null) return null;
		return Immutable.adaptList(WebElements::text, elements);
	}

	public static String trim(String s) {
		if (s == null) return null;
		return WHITESPACE.matcher(s).replaceAll(" ").trim();
	}
}
