package ceri.common.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImmutableUtil {

	private ImmutableUtil() {}

	public static <T> Set<T> copyAsSet(Collection<? extends T> set) {
		return Collections.unmodifiableSet(new HashSet<>(set));
	}

	public static <K, V> Map<K, V> copyAsMap(Map<? extends K, ? extends V> map) {
		return Collections.unmodifiableMap(new HashMap<>(map));
	}

	public static <T> List<T> copyAsList(Collection<? extends T> list) {
		return Collections.unmodifiableList(new ArrayList<>(list));
	}

	public static <T> List<T> arrayAsList(T[] array) {
		List<T> list = new ArrayList<>();
		Collections.addAll(list, array);
		return Collections.unmodifiableList(list);
	}

	public static <T> Set<T> arrayAsSet(T[] array) {
		Set<T> set = new HashSet<>();
		Collections.addAll(set, array);
		return Collections.unmodifiableSet(set);
	}

	@SafeVarargs
	public static <T> List<T> asList(T... array) {
		return arrayAsList(array);
	}
	
	@SafeVarargs
	public static <T> Set<T> asSet(T... array) {
		return arrayAsSet(array);
	}
	
}