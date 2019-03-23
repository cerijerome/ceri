package ceri.common.function;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class PredicateBuilder<T> {
	private final List<String> names = new ArrayList<>();
	private Predicate<T> predicate = null;

	public static <T> PredicateBuilder<T> of() {
		return new PredicateBuilder<>();
	}

	private PredicateBuilder() {}

	public PredicateBuilder<T> add(Predicate<T> predicate, String name) {
		this.predicate = this.predicate == null ? predicate : this.predicate.and(predicate);
		names.add(name);
		return this;
	}

	public Predicate<T> build() {
		if (predicate == null) return null;
		return FunctionUtil.namedPredicate(predicate, String.valueOf(names));
	}

}
