package ceri.ffm.type;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.nio.ByteOrder;
import java.util.List;
import ceri.common.reflect.Generics;
import ceri.common.reflect.Reflect;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Segments;
import ceri.ffm.core.Native;

public class Pointer<T> {
	public static final Pointer<?> NULL = new Pointer<>(Type.VOID, MemorySegment.NULL);
	private final Type<T> type;
	private final MemorySegment memory;

	// Functionality:
	// - arithmetic (layout)
	// - type instantiation from pointer
	// - type array instantiation from pointer
	// - void pointer: no arithmetic or instantiation
	// 

	public static void main(String[] args) {
		var t0 = Type.VOID;
		var t1 =
			new Type<>(Native.Kind.spec(new Generics.Token<Pointer<Pointer<int[]>>>() {}.typed));
		var t2 = Type.of(int.class);
		var p0 = Pointer.alloc(t0, 16);
		var p1 = Pointer.alloc(t1, 16);
		var p2 = Pointer.alloc(t2, 16);
		System.out.println(p0);
		System.out.println(p1);
		System.out.println(p2);
		System.out.println(p1.compact());
		System.out.println(p0.compact());
		System.out.println(p2.compact());
		List<String> l0 = List.of("");
		List<Integer> l1 = List.of(1);
		Class<List<String>> c0 = Reflect.unchecked(l0.getClass());
		Class<List<Integer>> c1 = Reflect.unchecked(l1.getClass());
		System.out.println(c0);
		System.out.println(c1);
	}

	public static class Type<T> {
		public static final Type<?> VOID = new Type<>(Native.Kind.Spec.VOID);
		private final Native.Kind.Spec spec;

		public static boolean isVoid(Type<?> type) {
			return VOID.equals(type);
		}

		public static <T> Type<T> of(Class<T> cls) {
			return of(Generics.Typed.of(cls));
		}

		public static <T> Type<T> of(Generics.Typed typed) {
			if (typed == null || typed.isUnbounded() || Generics.Typed.VOID.equals(typed))
				return Reflect.unchecked(VOID);
			var spec = Native.Kind.spec(typed);
			if (spec.kind() != null) return new Type<>(spec);
			throw new IllegalArgumentException("Unsupported type: " + typed);
		}

		private Type(Native.Kind.Spec spec) {
			this.spec = spec;
		}

		public Class<T> cls() {
			return spec.typed().cls();
		}

		public MemoryLayout layout() {
			return null; // TBD
		}

		@Override
		public String toString() {
			return spec.typed().toString();
		}
	}

	public static class Support<T> extends ceri.ffm.type.Support.Typed<Pointer<T>, AddressLayout> {
		public static final Support<?> VOID = new Support<>(Type.VOID, Layouts.POINTER);

		private final Type<T> type;

		private Support(Type<T> type, AddressLayout layout) {
			super(layout);
			this.type = type;
		}

		@Override
		public Class<Pointer<T>> type() {
			return Reflect.unchecked(Pointer.class);
		}

		@Override
		public Pointer<T> val() {
			return Pointer.ofNull(type);
		}

		@Override
		public Support<T> with(String name, long align, ByteOrder order) {
			return Layouts.with(this, l -> new Support<>(type, l), layout(), name, align, order);
		}

		public <U> Support<U> as(Type<U> type) {
			return this.type.equals(type) ? Reflect.unchecked(this) : new Support<>(type, layout());
		}

		@Override
		Pointer<T> rawGet(MemorySegment memory, long offset) {
			return new Pointer<>(type, memory.get(layout(), offset));
		}

		@Override
		void rawWrite(Pointer<T> value, MemorySegment memory, long offset) {
			memory.set(layout(), offset, memory(value));
		}
	}

	public static MemorySegment memory(Pointer<?> pointer) {
		return pointer == null ? MemorySegment.NULL : pointer.memory();
	}

	public static boolean isNull(Pointer<?> pointer) {
		return pointer == null || Segments.isNull(pointer.memory());
	}

	public static Pointer<?> alloc(long size) {
		return alloc(Type.VOID, size);
	}

	public static <T> Pointer<T> alloc(Type<T> type, long size) {
		return of(type, Segments.auto().allocate(size));
	}

	public static <T> Pointer<T> ofNull(Type<T> type) {
		return of(type, MemorySegment.NULL);
	}

	public static Pointer<?> of(MemorySegment memory) {
		return of(Type.VOID, memory);
	}

	public static <T> Pointer<T> of(Type<T> type, MemorySegment memory) {
		if (memory == null) memory = MemorySegment.NULL;
		if (Type.isVoid(type)) return new Pointer<>(Reflect.unchecked(Type.VOID), memory);
		return new Pointer<>(type, memory);
	}

	private Pointer(Type<T> type, MemorySegment memory) {
		this.type = type;
		this.memory = memory;
	}

	public Type<T> type() {
		return type;
	}

	public MemorySegment memory() {
		return memory;
	}

	public Pointer<?> asVoid() {
		return create(Type.VOID, memory);
	}

	public <U> Pointer<U> as(Type<U> type) {
		return create(type, memory);
	}

	public Pointer<T> share(long offset) {
		return share(offset, Long.MAX_VALUE);
	}

	public Pointer<T> share(long offset, long length) {
		return create(type, Segments.slice(memory, offset, length));
	}

	public long size() {
		return memory().byteSize();
	}

	public int sizeInt() {
		return Math.toIntExact(size());
	}

	public String compact() {
		int levels = 1;
		var typed = type.spec.typed();
		while (true) {
			if (!Pointer.class.equals(typed.cls())) break;
			levels++;
			typed = typed.type(0);
		}
		return typed + "*".repeat(levels);
	}

	@Override
	public String toString() {
		return String.format("%s<%s>@0x%x", Reflect.simple(getClass()), type, memory.address());
	}

	// support

	private <U> Pointer<U> create(Type<U> type, MemorySegment memory) {
		if (this.type.equals(type) && this.memory.equals(memory)) return Reflect.unchecked(this);
		return new Pointer<>(type, memory);
	}
}
