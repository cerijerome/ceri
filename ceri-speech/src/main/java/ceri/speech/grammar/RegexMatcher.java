/**
 * 
 */
package ceri.speech.grammar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class RegexMatcher implements RuleMatcher {
	private final String ruleRegex;
	private final Integer maxTags; // null => no max
	private final List<String> tags;

	private RegexMatcher(String ruleRegex, Integer maxTags, String... tags) {
		this.ruleRegex = ruleRegex;
		this.maxTags = maxTags;
		List<String> list = new ArrayList<>();
		Collections.addAll(list, tags);
		this.tags = Collections.unmodifiableList(list);
	}

	public static RegexMatcher createExact(String rule, String... tags) {
		return new RegexMatcher(rule, tags.length, tags);
	}

	public static RegexMatcher createAny(String rule, String... tags) {
		return new RegexMatcher(rule, null, tags);
	}

	@Override
	public boolean matches(String rule, List<String> tags) {
		if (!rule.matches(ruleRegex)) return false;
		return matchTags(tags);
	}

	@Override
	public String toString() {
		return "rule=" + ruleRegex + ":max=" + maxTags + ":tags=" + tags;
	}

	private boolean matchTags(List<String> tags) {
		if (maxTags != null && tags.size() > maxTags) return false;
		Iterator<String> tagIterator = tags.iterator();
		Iterator<String> regexIterator = this.tags.iterator();
		while (regexIterator.hasNext()) {
			if (!tagIterator.hasNext()) return false;
			String tag = tagIterator.next();
			String regex = regexIterator.next();
			if (!tag.matches(regex)) return false;
		}
		return true;
	}

}