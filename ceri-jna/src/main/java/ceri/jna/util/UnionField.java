package ceri.jna.util;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.function.ObjLongConsumer;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import com.sun.jna.Union;

/**
 * Methods to provide access to union fields, automatically setting type for get and set.
 */
public class UnionField<U extends Union> {
	public final String name;

	public static <U extends Union, T> UnionField.Type<U, T> of(String name, Function<U, T> getter,
		BiConsumer<U, T> setter) {
		return new UnionField.Type<>(name, getter, setter);
	}

	public static <U extends Union> UnionField.Int<U> ofInt(String name, ToIntFunction<U> getter,
		ObjIntConsumer<U> setter) {
		return new UnionField.Int<>(name, getter, setter);
	}

	public static <U extends Union> UnionField.Long<U> ofLong(String name, ToLongFunction<U> getter,
		ObjLongConsumer<U> setter) {
		return new UnionField.Long<>(name, getter, setter);
	}

	public static class Type<U extends Union, T> extends UnionField<U> {
		private final Function<U, T> getter;
		private final BiConsumer<U, T> setter;

		private Type(String name, Function<U, T> getter, BiConsumer<U, T> setter) {
			super(name);
			this.getter = getter;
			this.setter = setter;
		}

		public T get(U union) {
			if (super.autoRead(union) == null) return null;
			return Objects.requireNonNull(getter).apply(union);
		}

		public U set(U union, T value) {
			if (super.activate(union) != null) Objects.requireNonNull(setter).accept(union, value);
			return union;
		}

		public T read(U union) {
			if (super.readField(union) == null) return null;
			return Objects.requireNonNull(getter).apply(union);
		}

		public U write(U union, T value) {
			if (union != null) Objects.requireNonNull(setter).accept(union, value);
			return super.writeField(union);
		}
	}

	public static class Int<U extends Union> extends UnionField<U> {
		private final ToIntFunction<U> getter;
		private final ObjIntConsumer<U> setter;

		private Int(String name, ToIntFunction<U> getter, ObjIntConsumer<U> setter) {
			super(name);
			this.getter = getter;
			this.setter = setter;
		}

		public int get(U union) {
			if (super.autoRead(union) == null) return 0;
			return Objects.requireNonNull(getter).applyAsInt(union);
		}

		public U set(U union, int value) {
			if (super.activate(union) != null) Objects.requireNonNull(setter).accept(union, value);
			return union;
		}

		public int read(U union) {
			if (super.readField(union) == null) return 0;
			return Objects.requireNonNull(getter).applyAsInt(union);
		}

		public U write(U union, int value) {
			if (union != null) Objects.requireNonNull(setter).accept(union, value);
			return super.writeField(union);
		}
	}

	public static class Long<U extends Union> extends UnionField<U> {
		private final ToLongFunction<U> getter;
		private final ObjLongConsumer<U> setter;

		private Long(String name, ToLongFunction<U> getter, ObjLongConsumer<U> setter) {
			super(name);
			this.getter = getter;
			this.setter = setter;
		}

		public long get(U union) {
			if (super.autoRead(union) == null) return 0;
			return Objects.requireNonNull(getter).applyAsLong(union);
		}

		public U set(U union, long value) {
			if (super.activate(union) != null) Objects.requireNonNull(setter).accept(union, value);
			return union;
		}

		public long read(U union) {
			if (super.readField(union) == null) return 0L;
			return Objects.requireNonNull(getter).applyAsLong(union);
		}

		public U write(U union, long value) {
			if (union != null) Objects.requireNonNull(setter).accept(union, value);
			return super.writeField(union);
		}
	}

	private UnionField(String name) {
		this.name = name;
	}

	public U activate(U union) {
		if (union != null) union.setType(name);
		return union;
	}

	public U write(U union) {
		if (union != null) union.writeField(name);
		return union;
	}

	private U autoRead(U union) {
		if (union != null) {
			if (union.getAutoRead()) union.readField(name);
			else union.setType(name);
		}
		return union;
	}

	private U readField(U union) {
		if (union != null) union.readField(name);
		return union;
	}

	private U writeField(U union) {
		if (union != null) union.writeField(name);
		return union;
	}
}
