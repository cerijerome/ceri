package ceri.common.text;

import static ceri.common.validation.ValidationUtil.validateMin;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Stream;
import ceri.common.function.ExceptionBiConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.ExceptionIntFunction;
import ceri.common.function.ExceptionObjIntConsumer;
import ceri.common.util.BasicUtil;
import ceri.common.util.Truth;

/**
 * Utility for joining a sequence of objects as a string.
 */
public class Joiner {
	/** No separator, prefix or suffix. */
	public static final Joiner NONE = of("");
	/** Array-style joining. */
	public static final Joiner ARRAY = of("[", ", ", "]");
	/** Array-style joining, without spaces. */
	public static final Joiner ARRAY_COMPACT = of("[", ",", "]");
	/** List-style joining. */
	public static final Joiner LIST = of("{", ", ", "}");
	/** List-style joining, without spaces. */
	public static final Joiner LIST_COMPACT = of("{", ",", "}");
	/** Parameter-style joining. */
	public static final Joiner PARAM = of("(", ", ", ")");
	/** Parameter-style joining, without spaces. */
	public static final Joiner PARAM_COMPACT = of("(", ",", ")");
	/** Comma separator without prefix or suffix. */
	public static final Joiner COMMA = of(",");
	/** Pipe separator without prefix or suffix. */
	public static final Joiner OR = of("|");
	/** Colon separator without prefix or suffix. */
	public static final Joiner COLON = of(":");
	public final String prefix;
	public final String suffix;
	public final String separator;
	public final Integer max;
	public final String remainder;
	public final Truth showCount;
	public final String countFormat;

	public static Joiner of(String separator) {
		return of("", separator, "");
	}

	public static Joiner of(String prefix, String separator, String suffix) {
		return of(prefix, separator, suffix, null);
	}

	public static Joiner of(String prefix, String separator, String suffix, Integer max) {
		return builder().prefix(prefix).suffix(suffix).separator(separator).max(max).build();
	}

	public static class Builder {
		String prefix = "";
		String suffix = "";
		String separator = ", ";
		Integer max = null;
		String remainder = "...";
		Truth showCount = Truth.maybe; // show count if max exceeded
		String countFormat = "(%d)";

		private Builder() {}

		public Builder prefix(String prefix) {
			this.prefix = BasicUtil.defaultValue(prefix, "");
			return this;
		}

		public Builder suffix(String suffix) {
			this.suffix = BasicUtil.defaultValue(suffix, "");
			return this;
		}

		public Builder separator(String separator) {
			this.separator = BasicUtil.defaultValue(separator, "");
			return this;
		}

		public Builder max(Integer max) {
			this.max = max;
			return this;
		}

		public Builder remainder(String remainder) {
			this.remainder = BasicUtil.defaultValue(remainder, "");
			return this;
		}

		public Builder showCount(Truth showCount) {
			this.showCount = BasicUtil.defaultValue(showCount, Truth.maybe);
			return this;
		}

		public Builder countFormat(String countFormat) {
			this.countFormat = BasicUtil.defaultValue(countFormat, "");
			return this;
		}

		public Joiner build() {
			return new Joiner(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	private Joiner(Builder builder) {
		this.prefix = builder.prefix;
		this.suffix = builder.suffix;
		this.separator = builder.separator;
		this.max = builder.max;
		this.remainder = builder.remainder;
		this.showCount = builder.showCount;
		this.countFormat = builder.countFormat;
	}

	/**
	 * Returns true if the given count would be limited to the configured maximum.
	 */
	public boolean limited(int count) {
		return max != null && count > max;
	}

	/**
	 * Returns the actual joined item count, limited to the configured maximum.
	 */
	public int count(int count) {
		count = Math.max(0, count);
		return max != null ? Math.min(max, count) : count;
	}

	/* joining methods */

	/**
	 * Join items based on index.
	 */
	public <E extends Exception> String joinIndex(ExceptionIntFunction<E, ?> indexFn, int count)
		throws E {
		return joinIndex(indexFn, 0, count);
	}

	/**
	 * Join items based on index.
	 */
	public <E extends Exception> String joinIndex(ExceptionIntFunction<E, ?> indexFn, int offset,
		int count) throws E {
		if (indexFn == null) return "";
		return joinIndex((b, i) -> b.append(indexFn.apply(i)), offset, count);
	}

	/**
	 * Join items based on index.
	 */
	public <E extends Exception> String
		joinIndex(ExceptionObjIntConsumer<E, StringBuilder> indexAppender, int count) throws E {
		return joinIndex(indexAppender, 0, count);
	}

	/**
	 * Join items based on index.
	 */
	public <E extends Exception> String joinIndex(
		ExceptionObjIntConsumer<E, StringBuilder> indexAppender, int offset, int count) throws E {
		return appendIndex(new StringBuilder(), indexAppender, offset, count).toString();
	}

	/**
	 * Join items based on type.
	 */
	@SafeVarargs
	public final <T> String joinAll(T... ts) {
		return joinAll(StringBuilder::append, ts);
	}

	/**
	 * Join items based on type.
	 */
	@SafeVarargs
	public final <E extends Exception, T> String joinAll(ExceptionFunction<E, T, ?> stringFn,
		T... ts) throws E {
		if (stringFn == null) return "";
		return joinAll((b, t) -> b.append(stringFn.apply(t)), ts);
	}

	/**
	 * Join items based on type.
	 */
	@SafeVarargs
	public final <E extends Exception, T> String
		joinAll(ExceptionBiConsumer<E, StringBuilder, T> appender, T... ts) throws E {
		return appendAll(new StringBuilder(), appender, ts).toString();
	}

	/**
	 * Join items based on type.
	 */
	public <T> String join(Collection<T> collection) {
		return join(StringBuilder::append, collection);
	}

	/**
	 * Join items based on type.
	 */
	public <E extends Exception, T> String join(ExceptionFunction<E, T, ?> stringFn,
		Collection<T> collection) throws E {
		if (stringFn == null) return "";
		return join((b, t) -> b.append(stringFn.apply(t)), collection);
	}

	/**
	 * Join items based on type.
	 */
	public <E extends Exception, T> String join(ExceptionBiConsumer<E, StringBuilder, T> appender,
		Collection<T> collection) throws E {
		return append(new StringBuilder(), appender, collection).toString();
	}

	/**
	 * Join items based on type; count is not available.
	 */
	public <T> String join(Stream<T> stream) {
		return join(StringBuilder::append, stream);
	}

	/**
	 * Join items based on type; count is not available.
	 */
	public <E extends Exception, T> String join(ExceptionFunction<E, T, ?> stringFn,
		Stream<T> stream) throws E {
		if (stringFn == null) return "";
		return join((b, t) -> b.append(stringFn.apply(t)), stream);
	}

	/**
	 * Join items based on type; count is not available.
	 */
	public <E extends Exception, T> String join(ExceptionBiConsumer<E, StringBuilder, T> appender,
		Stream<T> stream) throws E {
		return append(new StringBuilder(), appender, stream).toString();
	}

	/**
	 * Join items based on type; count is not available.
	 */
	public <T> String join(Iterator<T> iterator) {
		return join(StringBuilder::append, iterator);
	}

	/**
	 * Join items based on type, up to count.
	 */
	public <T> String join(Iterator<T> iterator, int count) {
		return join(StringBuilder::append, iterator, count);
	}

	/**
	 * Join items based on type; count is not available.
	 */
	public <E extends Exception, T> String join(ExceptionFunction<E, T, ?> stringFn,
		Iterator<T> iterator) throws E {
		if (stringFn == null) return "";
		return join((b, t) -> b.append(stringFn.apply(t)), iterator);
	}

	/**
	 * Join items based on type, up to count.
	 */
	public <E extends Exception, T> String join(ExceptionFunction<E, T, ?> stringFn,
		Iterator<T> iterator, int count) throws E {
		if (stringFn == null) return "";
		return join((b, t) -> b.append(stringFn.apply(t)), iterator, count);
	}

	/**
	 * Join items based on type; count is not available.
	 */
	public <E extends Exception, T> String join(ExceptionBiConsumer<E, StringBuilder, T> appender,
		Iterator<T> iterator) throws E {
		return append(new StringBuilder(), appender, iterator).toString();
	}

	/**
	 * Join items based on type, up to count.
	 */
	public <E extends Exception, T> String join(ExceptionBiConsumer<E, StringBuilder, T> appender,
		Iterator<T> iterator, int count) throws E {
		return appendTo(new StringBuilder(), appender, iterator, count).toString();
	}

	/* appending methods */

	/**
	 * Append items based on index.
	 */
	public <E extends Exception> StringBuilder appendIndex(StringBuilder sb,
		ExceptionIntFunction<E, ?> indexFn, int count) throws E {
		return appendIndex(sb, indexFn, 0, count);
	}

	/**
	 * Append items based on index.
	 */
	public <E extends Exception> StringBuilder appendIndex(StringBuilder sb,
		ExceptionIntFunction<E, ?> indexFn, int offset, int count) throws E {
		if (indexFn == null) return sb;
		return appendIndex(sb, (b, i) -> b.append(indexFn.apply(i)), offset, count);
	}

	/**
	 * Append items based on index.
	 */
	public <E extends Exception> StringBuilder appendIndex(StringBuilder sb,
		ExceptionObjIntConsumer<E, StringBuilder> indexAppender, int count) throws E {
		return appendIndex(sb, indexAppender, 0, count);
	}

	/**
	 * Append items based on index.
	 */
	public <E extends Exception> StringBuilder appendIndex(StringBuilder sb,
		ExceptionObjIntConsumer<E, StringBuilder> indexAppender, int offset, int count) throws E {
		if (sb == null || indexAppender == null) return sb;
		validateMin(offset, 0, "offset");
		validateMin(count, 0, "count");
		sb.append(prefix);
		for (int i = 0; i < count; i++) {
			if (i > 0) sb.append(separator);
			if (atMax(i) && count > max) {
				sb.append(remainder);
				break;
			}
			indexAppender.accept(sb, offset + i);
		}
		sb.append(suffix);
		appendCount(sb, count);
		return sb;
	}

	/**
	 * Append items based on type.
	 */
	@SafeVarargs
	public final <T> StringBuilder appendAll(StringBuilder sb, T... ts) {
		return appendAll(sb, StringBuilder::append, ts);
	}

	/**
	 * Append items based on type.
	 */
	@SafeVarargs
	public final <E extends Exception, T> StringBuilder appendAll(StringBuilder sb,
		ExceptionFunction<E, T, ?> stringFn, T... ts) throws E {
		if (stringFn == null) return sb;
		return appendAll(sb, (b, t) -> b.append(stringFn.apply(t)), ts);
	}

	/**
	 * Append items based on type.
	 */
	@SafeVarargs
	public final <E extends Exception, T> StringBuilder appendAll(StringBuilder sb,
		ExceptionBiConsumer<E, StringBuilder, T> appender, T... ts) throws E {
		if (ts == null) return sb;
		return append(sb, appender, Arrays.asList(ts));
	}

	/**
	 * Append items based on type.
	 */
	public <T> StringBuilder append(StringBuilder sb, Collection<T> collection) {
		return append(sb, StringBuilder::append, collection);
	}

	/**
	 * Append items based on type.
	 */
	public <E extends Exception, T> StringBuilder append(StringBuilder sb,
		ExceptionFunction<E, T, ?> stringFn, Collection<T> collection) throws E {
		if (stringFn == null) return sb;
		return append(sb, (b, t) -> b.append(stringFn.apply(t)), collection);
	}

	/**
	 * Append items based on type.
	 */
	public <E extends Exception, T> StringBuilder append(StringBuilder sb,
		ExceptionBiConsumer<E, StringBuilder, T> appender, Collection<T> collection) throws E {
		if (collection == null) return sb;
		return appendTo(sb, appender, collection.iterator(), collection.size());
	}

	/**
	 * Append items based on type; count is not available.
	 */
	public <T> StringBuilder append(StringBuilder sb, Stream<T> stream) {
		return append(sb, StringBuilder::append, stream);
	}

	/**
	 * Append items based on type; count is not available.
	 */
	public <E extends Exception, T> StringBuilder append(StringBuilder sb,
		ExceptionFunction<E, T, ?> stringFn, Stream<T> stream) throws E {
		if (stringFn == null) return sb;
		return append(sb, (b, t) -> b.append(stringFn.apply(t)), stream);
	}

	/**
	 * Append items based on type; count is not available.
	 */
	public <E extends Exception, T> StringBuilder append(StringBuilder sb,
		ExceptionBiConsumer<E, StringBuilder, T> appender, Stream<T> stream) throws E {
		if (stream == null) return sb;
		return append(sb, appender, stream.iterator());
	}

	/**
	 * Append items based on type; count is not available.
	 */
	public <T> StringBuilder append(StringBuilder sb, Iterator<T> iterator) {
		return append(sb, StringBuilder::append, iterator);
	}

	/**
	 * Append items based on type; count is not available.
	 */
	public <E extends Exception, T> StringBuilder append(StringBuilder sb,
		ExceptionFunction<E, T, ?> stringFn, Iterator<T> iterator) throws E {
		if (stringFn == null) return sb;
		return append(sb, (b, t) -> b.append(stringFn.apply(t)), iterator);
	}

	/**
	 * Append items based on type; count is not available.
	 */
	public <E extends Exception, T> StringBuilder append(StringBuilder sb,
		ExceptionBiConsumer<E, StringBuilder, T> appender, Iterator<T> iterator) throws E {
		return appendTo(sb, appender, iterator, null);
	}

	/**
	 * Append items based on type, up to count.
	 */
	public <T> StringBuilder append(StringBuilder sb, Iterator<T> iterator, int count) {
		return append(sb, StringBuilder::append, iterator, count);
	}

	/**
	 * Append items based on type, up to count.
	 */
	public <E extends Exception, T> StringBuilder append(StringBuilder sb,
		ExceptionFunction<E, T, ?> stringFn, Iterator<T> iterator, int count) throws E {
		if (stringFn == null) return sb;
		return append(sb, (b, t) -> b.append(stringFn.apply(t)), iterator, count);
	}

	/**
	 * Append items based on type, up to count.
	 */
	public <E extends Exception, T> StringBuilder append(StringBuilder sb,
		ExceptionBiConsumer<E, StringBuilder, T> appender, Iterator<T> iterator, int count)
		throws E {
		return appendTo(sb, appender, iterator, count);
	}

	/* Support methods */

	private <E extends Exception, T> StringBuilder appendTo(StringBuilder sb,
		ExceptionBiConsumer<E, StringBuilder, T> appender, Iterator<T> iterator, Integer count)
		throws E {
		if (sb == null || iterator == null || appender == null) return sb;
		sb.append(prefix);
		for (int i = 0;; i++) {
			if (count != null && i >= count) break;
			if (!iterator.hasNext()) break;
			if (i > 0) sb.append(separator);
			var t = iterator.next();
			if (atMax(i) && iterator.hasNext()) {
				sb.append(remainder);
				break;
			}
			appender.accept(sb, t);
		}
		sb.append(suffix);
		appendCount(sb, count);
		return sb;
	}

	private boolean atMax(int i) {
		return max != null && i == max - 1;
	}

	private void appendCount(StringBuilder b, Integer count) {
		if (count == null || showCount.no()) return;
		if (showCount.yes() || (max != null && count > max))
			b.append(StringUtil.format(countFormat, count));
	}
}
