package ceri.ffm.type;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.StructLayout;
import java.nio.ByteOrder;
import java.util.List;
import ceri.common.array.RawArray;
import ceri.common.collect.Lists;
import ceri.common.concurrent.Lazy;
import ceri.common.math.Maths;
import ceri.common.reflect.Reflect;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Native;

public class Struct<T extends Struct<T>> extends Group<T, StructLayout> {
	private static final Lazy.ForClass<Group.Config<? extends Struct<?>, StructLayout>> cache =
		Lazy.forClass(c -> new Builder<>(Reflect.unchecked(c)).build(true));

	/**
	 * Operational support for struct types.
	 */
	public static class Supporter<T extends Struct<T>> extends Group.Supporter<T, StructLayout> {

		private Supporter(Group.Config<T, StructLayout> config, StructLayout layout) {
			super(config, layout);
		}

		@Override
		public Native.Kind kind() {
			return Native.Kind.STRUCT;
		}

		@Override
		public Supporter<T> align(long align) {
			var layout = Layouts.align(layout(), align);
			return layout == layout() ? this : new Supporter<>(config, layout);
		}

		@Override
		public Supporter<T> order(ByteOrder order) {
			return this;
		}

		/**
		 * Returns a new instance with initialized struct members, and given flexible array size.
		 */
		public T init(int flexSize) {
			return init(null, flexSize);
		}

		/**
		 * Initializes struct members, with given flexible array size.
		 */
		public T init(T struct, int flexSize) {
			if (struct == null) struct = val();
			for (var member : config.members())
				member.init(struct, flexSize);
			return struct;
		}

		/**
		 * Returns the byte size of the given struct, including flexible array member if present.
		 */
		public long size(T struct) {
			if (struct == null) return 0L;
			if (!config.flex()) return layoutSize();
			var last = Lists.last(config.members());
			int flexSize = RawArray.length(last.get(struct));
			return Math.max(layoutSize(), last.flexScale(flexSize));
		}

		/**
		 * Allocates memory and writes the struct to the memory, including a flexible array member
		 * if present.
		 */
		public MemorySegment alloc(SegmentAllocator allocator, int flexSize) {
			return allocate(allocator, flexSize(flexSize));
		}

		/**
		 * Allocates memory and writes the struct to the memory, including flexible array member if
		 * present.
		 */
		@Override
		public MemorySegment alloc(SegmentAllocator allocator, T value) {
			if (allocator == null) return null;
			var memory = allocate(allocator, size(value));
			rawWrite(memory, 0L, memory.byteSize(), value);
			return memory;
		}

		/**
		 * Updates members from memory by index.
		 */
		public boolean read(MemorySegment memory, T struct, int... indexes) {
			return read(memory, 0L, struct, indexes);
		}

		/**
		 * Updates members from memory by index.
		 */
		public boolean read(MemorySegment memory, long offset, T struct, int... indexes) {
			return read(memory, offset, Long.MAX_VALUE, struct, indexes);
		}

		/**
		 * Updates members from memory by index.
		 */
		public boolean read(MemorySegment memory, long offset, long length, T struct,
			int... indexes) {
			if (memory == null || struct == null || indexes == null) return false;
			if (indexes.length == 0) return read(memory, offset, length, struct);
			offset = Maths.limit(offset, 0L, memory.byteSize());
			length = Maths.limit(length, 0L, memory.byteSize() - offset);
			if (length < layoutSize()) return false;
			for (int i : indexes) {
				var member = config.member(i);
				if (member != null) member.read(struct, memory, offset);
			}
			return true;
		}

		/**
		 * Updates members from memory by name.
		 */
		public boolean read(MemorySegment memory, T struct, String... names) {
			return read(memory, 0L, struct, names);
		}

		/**
		 * Updates members from memory by name.
		 */
		public boolean read(MemorySegment memory, long offset, T struct, String... names) {
			return read(memory, offset, Long.MAX_VALUE, struct, names);
		}

		/**
		 * Updates members from memory by name.
		 */
		public boolean read(MemorySegment memory, long offset, long length, T struct,
			String... names) {
			return read(memory, offset, length, struct, indexes(names));
		}

		/**
		 * Writes member values to memory by index.
		 */
		public boolean write(MemorySegment memory, T struct, int... indexes) {
			return write(memory, 0L, struct, indexes);
		}

		/**
		 * Writes member values to memory by index.
		 */
		public boolean write(MemorySegment memory, long offset, T struct, int... indexes) {
			return write(memory, offset, Long.MAX_VALUE, struct, indexes);
		}

		/**
		 * Writes member values to memory by index.
		 */
		public boolean write(MemorySegment memory, long offset, long length, T struct,
			int... indexes) {
			if (struct == null || memory == null || indexes == null) return false;
			if (indexes.length == 0) return write(memory, offset, length, struct);
			offset = Maths.limit(offset, 0L, memory.byteSize());
			length = Maths.limit(length, 0L, memory.byteSize() - offset);
			if (length < layoutSize()) return false;
			for (int i : indexes) {
				var member = config.member(i);
				if (member != null) member.write(struct, memory, offset);
			}
			return true;
		}

		/**
		 * Writes member values to memory by name.
		 */
		public boolean write(MemorySegment memory, T struct, String... names) {
			return write(memory, 0L, struct, names);
		}

		/**
		 * Writes member values to memory by name.
		 */
		public boolean write(MemorySegment memory, long offset, T struct, String... names) {
			return write(memory, offset, Long.MAX_VALUE, struct, names);
		}

		/**
		 * Writes member values to memory by name.
		 */
		public boolean write(MemorySegment memory, long offset, long length, T struct,
			String... names) {
			return write(memory, offset, length, struct, indexes(names));
		}

		@Override
		T rawGet(MemorySegment memory, long offset, long length) {
			var struct = val();
			rawRead(memory, offset, length, struct);
			return struct;
		}

		@Override
		void rawRead(MemorySegment memory, long offset, long length, T struct) {
			for (var member : config.members())
				member.read(struct, memory, offset);
		}

		@Override
		void rawWrite(MemorySegment memory, long offset, long length, T struct) {
			for (var member : config.members())
				member.write(struct, memory, offset);
		}

		private long flexSize(int flexSize) {
			var last = Lists.last(config.members());
			if (!last.flex()) return layoutSize();
			return Math.max(layoutSize(), last.flexScale(flexSize));
		}

		private int[] indexes(String... names) {
			if (names == null) return null;
			int[] indexes = new int[names.length];
			for (int i = 0; i < indexes.length; i++)
				indexes[i] = config.indexOf(names[i]);
			return indexes;
		}
	}

	private static class Builder<T extends Struct<T>>
		extends Group.Config.Builder<T, StructLayout> {
		private long offset = 0L;
		private long align = 1L;

		private Builder(Class<T> type) {
			super(type);
		}

		@Override
		Member<?> member(Member.Builder b) {
			offset += addPadding(layouts, offset, b.layout.byteAlignment());
			var member = b.build(offset);
			align = Math.max(align, b.layout.byteAlignment());
			offset += b.layout.byteSize();
			return member;
		}

		@Override
		StructLayout layout() {
			if (!Group.flex(members)) addPadding(layouts, offset, align);
			return Layouts.struct(layouts);
		}
	}

	static <T extends Struct<T>> Config<T, StructLayout> config(Class<T> cls) {
		return Reflect.unchecked(cache.get(cls));
	}

	/**
	 * Returns operational support for the type.
	 */
	public static <T extends Struct<T>> Supporter<T> support(Class<T> cls) {
		return Reflect.unchecked(Supports.DEF.from(cls));
	}

	/**
	 * Creates a support instance for the type.
	 */
	static <T extends Struct<T>> Supporter<T> supportFor(Class<T> cls) {
		var config = config(cls);
		return new Supporter<>(config, config.layout());
	}

	/**
	 * Creates an instance of the struct, with flexible array member initialized to given count.
	 */
	public static <T extends Struct<T>> T init(Class<T> cls) {
		return init(cls, INVALID);
	}

	/**
	 * Creates an instance of the struct, with flexible array member initialized to given count.
	 */
	public static <T extends Struct<T>> T init(Class<T> cls, int flexSize) {
		if (cls == null) return null;
		return support(cls).init(null, flexSize);
	}

	/**
	 * Allocates memory for a struct with populated flexible array member.
	 */
	public static <T extends Struct<T>> MemorySegment alloc(SegmentAllocator allocator, T struct) {
		if (allocator == null || struct == null) return null;
		var support = support(Reflect.getClass(struct));
		return support.alloc(allocator, struct);
	}

	// shared

	@Override
	Config<T, StructLayout> configFor(Class<T> cls) {
		return config(cls);
	}

	// support

	private static long addPadding(List<MemoryLayout> layouts, long offset, long align) {
		var padding = Layouts.padding(offset, align);
		if (padding != 0) layouts.add(MemoryLayout.paddingLayout(padding));
		return padding;
	}
}
