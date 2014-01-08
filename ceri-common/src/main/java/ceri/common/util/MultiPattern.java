package ceri.common.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.collection.ImmutableUtil;

public class MultiPattern {
	private final List<Pattern> patterns;

	public static class Builder {
		List<Pattern> patterns = new ArrayList<>();

		Builder() {}
		
		public Builder patternStrings(Collection<String> patterns) {
			for (String pattern : patterns)
				this.patterns.add(Pattern.compile(pattern));
			return this;
		}

		public Builder pattern(String... patterns) {
			for (String pattern : patterns)
				this.patterns.add(Pattern.compile(pattern));
			return this;
		}

		public Builder patterns(Collection<Pattern> patterns) {
			this.patterns.addAll(patterns);
			return this;
		}

		public Builder pattern(Pattern... patterns) {
			Collections.addAll(this.patterns, patterns);
			return this;
		}

		public MultiPattern build() {
			return new MultiPattern(this);
		}
	}

	MultiPattern(Builder builder) {
		patterns = ImmutableUtil.copyAsList(builder.patterns);
	}

	public static Builder builder() {
		return new Builder();
	}
	
	public Matcher find(CharSequence s) {
		for (Pattern pattern : patterns) {
			Matcher m = pattern.matcher(s);
			if (m.find()) return m;
		}
		return null;
	}
	
}
