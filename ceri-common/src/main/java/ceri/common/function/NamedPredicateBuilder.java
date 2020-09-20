package ceri.common.function;

import java.util.function.Predicate;
import ceri.common.text.StringUtil;

/**
 * Builds a predicate with combined name. Returns null if no predicates added.
 */
public class NamedPredicateBuilder<E extends Exception, T> {
	private static final String WRAP_START = "(";
	private static final String WRAP_END = ")";
	private static final String NEGATE_START = "!" + WRAP_START;
	private static final String NEGATE_END = WRAP_END;
	private static final String AND_DELIMITER = "&";
	private static final String OR_DELIMITER = "|";
	private final StringBuilder desc = new StringBuilder();
	private ExceptionPredicate<E, T> predicate = null;

	public static <E extends Exception, T> NamedPredicateBuilder<E, T> of() {
		return new NamedPredicateBuilder<>();
	}

	public static <E extends Exception, T> NamedPredicateBuilder<E, T>
		of(ExceptionPredicate<E, T> predicate, String name) {
		return new NamedPredicateBuilder<E, T>().and(predicate, name);
	}

	private NamedPredicateBuilder() {}

	public NamedPredicateBuilder<E, T> and(ExceptionPredicate<E, T> predicate, String name) {
		if (this.predicate == null) return init(predicate, name);
		this.predicate = this.predicate.and(predicate);
		desc.append(AND_DELIMITER).append(name);
		return this;
	}

	public NamedPredicateBuilder<E, T> or(ExceptionPredicate<E, T> predicate, String name) {
		if (this.predicate == null) return init(predicate, name);
		this.predicate = this.predicate.or(predicate);
		desc.append(OR_DELIMITER).append(name);
		return this;
	}

	public NamedPredicateBuilder<E, T> negate() {
		if (predicate == null) return this;
		this.predicate = this.predicate.negate();
		desc.insert(0, NEGATE_START).append(NEGATE_END);
		return this;
	}

	private NamedPredicateBuilder<E, T> init(ExceptionPredicate<E, T> predicate, String name) {
		this.predicate = predicate;
		desc.append(name);
		return this;
	}

	public ExceptionPredicate<E, T> buildEx() {
		if (predicate == null) return null;
		return predicate.name(name());
	}

	public Predicate<T> build() {
		if (predicate == null) return null;
		return FunctionUtil.named(predicate.asPredicate(), name());
	}

	private String name() {
		return StringUtil.startsWith(desc, NEGATE_START) ? desc.toString() :
			WRAP_START + desc + WRAP_END;
	}

}
