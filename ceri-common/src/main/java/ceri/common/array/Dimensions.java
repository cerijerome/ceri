package ceri.common.array;

import java.util.Objects;
import ceri.common.data.IntProvider;
import ceri.common.function.Functions;

/**
 * Encapsulates maximum array dimensions, starting with outer dimension.
 */
public class Dimensions {
	private static final Functions.IntBiOperator GROW = DynamicArray.growX2(4);
	public static final Dimensions NONE = new Dimensions(IntProvider.empty());
	public final IntProvider dims;

	/**
	 * Extract dimensions from array instance.
	 */
	public static Dimensions from(Object array) {
		var maxes = DynamicArray.ints(GROW);
		max(maxes, 0, array);
		return of(maxes.wrap());
	}

	/**
	 * Creates an instance with given dimensions.
	 */
	public static Dimensions of(int... dims) {
		if (Array.INT.isEmpty(dims)) return NONE;
		return of(IntProvider.of(dims));
	}

	/**
	 * Creates an instance with given dimensions.
	 */
	public static Dimensions of(IntProvider dims) {
		if (dims == null || dims.length() == 0) return NONE;
		return new Dimensions(dims);
	}

	/**
	 * Creates an instance with dimensions of zero size.
	 */
	public static Dimensions ofZeros(int count) {
		return of(new int[Math.max(0, count)]);
	}

	/**
	 * Returns true if dimensions are null or empty.
	 */
	public static boolean isEmpty(Dimensions dims) {
		return dims == null || dims.isEmpty();
	}

	/**
	 * Creates an array instance of the component type, using the dimension sizes. Returns null if
	 * component type is null or dimensions are empty.
	 */
	public static <T> T create(Dimensions dims, Class<?> component) {
		return isEmpty(dims) ? null : dims.create(component);
	}

	/**
	 * Builds an instance.
	 */
	public static class Builder {
		private final DynamicArray.OfInt dims = DynamicArray.ints(GROW);

		/**
		 * Appends dimensions.
		 */
		public Builder add(int... dims) {
			this.dims.append(dims);
			return this;
		}

		/**
		 * Appends dimensions.
		 */
		public Builder add(IntProvider dims) {
			for (int i = 0; i < dims.length(); i++)
				this.dims.append(dims.getInt(i));
			return this;
		}

		/**
		 * Appends dimensions.
		 */
		public Builder add(Dimensions dims) {
			return add(dims.dims);
		}

		/**
		 * Returns a new instance.
		 */
		public Dimensions build() {
			return of(dims.wrap());
		}
	}

	/**
	 * Start building a new instance.
	 */
	public static Builder builder() {
		return new Builder();
	}

	private Dimensions(IntProvider dims) {
		this.dims = dims;
	}

	/**
	 * Creates an array instance of the component type, using the dimension sizes. Returns null if
	 * component type is null or dimensions are empty.
	 */
	public <T> T create(Class<?> component) {
		return RawArray.ofType(component, array());
	}

	/**
	 * Returns an array copy of the dimensions.
	 */
	public int[] array() {
		return dims.copy(0);
	}

	public boolean isEmpty() {
		return count() <= 0;
	}

	/**
	 * Returns the number of dimensions.
	 */
	public int count() {
		return dims.length();
	}

	/**
	 * Returns dimension sizes multiplied.
	 */
	public int total() {
		return dims.stream(0).reduce(Math::multiplyExact, 0);
	}

	/**
	 * Returns the outer dimension size.
	 */
	public int dim() {
		return dim(0);
	}

	/**
	 * Returns the dimension at given index, with negative index relative to the inner dimension.
	 */
	public int dim(int index) {
		if (index < 0) index = count() + index;
		if (index < 0 || index >= count()) return 0;
		return dims.getInt(index);
	}

	/**
	 * Iterates over every dimension combination in order.
	 */
	public void forEach(Functions.ObjIntConsumer<int[]> consumer) {
		var array = new int[count()];
		int total = total();
		for (int i = 0; i < total; i++) {
			extract(array, i);
			consumer.accept(array, i);
		}
	}

	/**
	 * Returns an instance without the outer dimension.
	 */
	public Dimensions inner() {
		if (count() <= 1) return NONE;
		return new Dimensions(dims.slice(1));
	}

	/**
	 * Returns an instance without the inner dimension.
	 */
	public Dimensions outer() {
		if (count() <= 1) return NONE;
		return new Dimensions(dims.slice(0, dims.length() - 1));
	}

	/**
	 * Returns the dimensions as array brackets.
	 */
	public String arrayString() {
		var b = new StringBuilder();
		for (int i = 0; i < count(); i++)
			b.append('[').append(dim(i)).append(']');
		return b.toString();
	}

	@Override
	public int hashCode() {
		return dims.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Dimensions other)) return false;
		return Objects.equals(dims, other.dims);
	}

	@Override
	public String toString() {
		return dims.toString();
	}

	// support

	private static void max(DynamicArray.OfInt maxes, int level, Object array) {
		if (array == null || !array.getClass().isArray()) return;
		int len = RawArray.length(array);
		maxes.set(level, Math.max(len, maxes.get(level)));
		for (int i = 0; i < len; i++)
			max(maxes, level + 1, RawArray.get(array, i));
	}

	private void extract(int[] array, int i) {
		for (int j = array.length - 1; j >= 0; j--) {
			int dim = Math.max(1, dim(j));
			array[j] = i % dim;
			i = i / dim;
		}
	}
}