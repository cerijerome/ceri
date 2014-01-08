/**
 * Created on Feb 14, 2006
 */
package ceri.common.comparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A list of comparators to be applied in sequence. If the first comparator
 * returns 0, the next comparator is checked. If the end of the list is reached,
 * 0 is returned.
 */
public class ComparatorSequence<T> implements Comparator<T> {
	private final List<Comparator<? super T>> comparators;

	public static class Builder<T> {
		private final List<Comparator<? super T>> comparators = new ArrayList<>();

		Builder() {
		}
		
		@SafeVarargs
		public final Builder<T> add(Comparator<? super T>... comparators) {
			Collections.addAll(this.comparators, comparators);
			return this;
		}

		public Builder<T> add(Collection<? extends Comparator<? super T>> comparators) {
			this.comparators.addAll(comparators);
			return this;
		}

		public boolean isEmpty() {
			return comparators.isEmpty();
		}
		
		public ComparatorSequence<T> build() {
			return new ComparatorSequence<>(comparators);
		}
	}

	ComparatorSequence(Collection<? extends Comparator<? super T>> comparators) {
		this.comparators = Collections.unmodifiableList(new ArrayList<>(comparators));
	}

	public static <T> Builder<T> builder() {
		return new Builder<>();
	}
	
	public List<Comparator<? super T>> comparators() {
		return comparators;
	}

	/**
	 * Comparator interface.
	 */
	@Override
	public int compare(T lhs, T rhs) {
		for (Comparator<? super T> comparator : comparators) {
			int result = comparator.compare(lhs, rhs);
			if (result != 0) return result;
		}
		return 0;
	}

}
