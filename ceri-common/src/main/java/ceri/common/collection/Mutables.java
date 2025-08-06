package ceri.common.collection;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import ceri.common.function.Functions;

/**
 * Mutable collection support.
 */
public class Mutables {
	public static final CollectionSupplier supplier = CollectionSupplier.of();

	private Mutables() {}

	@SafeVarargs
	public static <T, C extends Collection<T>> C addAll(C collection, T... ts) {
		if (collection != null && ts != null) Collections.addAll(collection, ts);
		return collection;
	}

	@SafeVarargs
	public static <T, C extends Collection<T>> C addAll(Functions.Supplier<C> supplier, T... ts) {
		return supplier == null ? null : addAll(supplier.get(), ts);
	}

	@SafeVarargs
	public static <T> Set<T> asSet(T... ts) {
		return addAll(supplier.<T>set(), ts);
	}

	@SafeVarargs
	public static <T> List<T> asList(T... ts) {
		return addAll(supplier.<T>list(), ts);
	}

	/**
	 * Makes a reversed mutable copy of the list. Returns an empty list if the list is null.
	 */
	public static <T> List<T> reversed(List<? extends T> list) {
		var reversed = supplier.<T>list().get();
		if (list != null) reversed.addAll(list);
		Collections.reverse(reversed);
		return reversed;
	}
	
	/**
	 * Creates an identity hash set backed by a map.
	 */
	public static <T> Set<T> identitySet() {
		return Collections.newSetFromMap(new IdentityHashMap<>());
	}

}
