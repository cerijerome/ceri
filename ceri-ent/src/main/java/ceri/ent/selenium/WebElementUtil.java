package ceri.ent.selenium;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import org.openqa.selenium.WebElement;

public class WebElementUtil {
	private static final Pattern WHITESPACE = Pattern.compile("\\p{Zs}+");
	private static final String INNER_HTML = "innerHTML";
	
	private WebElementUtil() {}
	
	public static String html(WebElement element) {
		if (element == null) return null;
		return trim(element.getAttribute(INNER_HTML));
	}
	
	public static List<String> htmls(Collection<WebElement> elements) {
		if (elements == null) return null;
		List<String> htmls = new ArrayList<>();
		for (WebElement element : elements) htmls.add(html(element));
		return htmls;
	}
	
	public static String trim(String s) {
		if (s == null) return null;
		s = WHITESPACE.matcher(s).replaceAll(" ");
		return s.trim();
	}

}
