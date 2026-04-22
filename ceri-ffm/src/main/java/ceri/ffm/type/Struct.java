package ceri.ffm.type;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.StructLayout;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Map;
import ceri.common.array.RawArray;
import ceri.common.collect.Lists;
import ceri.common.collect.Maps;
import ceri.common.math.Maths;
import ceri.common.reflect.Reflect;
import ceri.ffm.core.Layouts;

public class Struct<T extends Struct<T>> extends Group<T> {
	private static final Map<Class<?>, Support<?>> cache = Maps.syncWeak();
	private volatile Support<T> support = null;

	/**
	 * Operational support for struct types.
	 */
	public static class Support<T extends Struct<T>> extends Group.Support<T, StructLayout> {

		private Support(Group.Support.Config<T> config, StructLayout layout) {
			super(config, layout);
		}

		@Override
		public Support<T> with(String name, long align, ByteOrder order) {
			if (align < layout().byteAlignment()) align = 0L;
			return Layouts.with(this, l -> new Support<T>(config, l), layout(), name, align, null);
		}

		/**
		 * Initializes struct members, with given flexible array size.
		 */
		public T init(int flexSize) {
			return init(null, flexSize);
		}

		/**
		 * Initializes struct members, with given flexible array size.
		 */
		public T init(T struct, int flexSize) {
			if (struct == null) struct = val();
			for (var member : members())
				member.init(struct, flexSize);
			return struct;
		}

		/**
		 * Returns the byte size of the given struct, including flexible array member if present.
		 */
		public long size(T struct) {
			if (struct == null) return 0L;
			if (!flex()) return layoutSize();
			var last = Lists.last(members());
			int flexSize = RawArray.length(last.accessor.get(struct));
			return Math.max(layoutSize(), flexScale(last, flexSize));
		}

		/**
		 * Allocates memory and writes the struct to the memory, including a flexible array member
		 * if present.
		 */
		public MemorySegment alloc(SegmentAllocator allocator, int flexSize) {
			if (allocator == null) return null;
			return allocator.allocate(flexSize(flexSize));
		}

		/**
		 * Allocates memory and writes the struct to the memory, including flexible array member if
		 * present.
		 */
		@Override
		public MemorySegment alloc(SegmentAllocator allocator, T value) {
			if (allocator == null) return null;
			var memory = allocator.allocate(size(value));
			rawWrite(memory, 0L, value);
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
			if (length < size(1)) return false;
			for (int i : indexes) {
				var member = member(i);
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
			if (length < size(1)) return false;
			for (int i : indexes) {
				var member = member(i);
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
		T rawGet(MemorySegment memory, long offset) {
			var struct = val();
			rawRead(memory, offset, struct);
			return struct;
		}

		@Override
		void rawRead(MemorySegment memory, long offset, T struct) {
			for (var member : members())
				member.read(struct, memory, offset);
		}

		@Override
		void rawWrite(MemorySegment memory, long offset, T struct) {
			for (var member : members())
				member.write(struct, memory, offset);
		}

		@Override
		String typeName() {
			return "struct";
		}

		private long flexSize(int flexSize) {
			if (!flex()) return layoutSize();
			var last = Lists.last(members());
			return Math.max(layoutSize(), flexScale(last, flexSize));
		}
	}

	private static class Builder<T extends Struct<T>> extends Group.Builder<T> {
		private long offset = 0L;
		private long align = 1L;

		private Builder(Class<T> type) {
			super(type);
		}

		@Override
		Support<T> build() {
			addMembers(true);
			return new Support<>(new Group.Support.Config<>(type, constructor, members), layout());
		}

		@Override
		Member<?> member(Member.Builder b) {
			offset += addPadding(layouts, offset, b.layout.byteAlignment());
			var member = b.build(offset);
			align = Math.max(align, b.layout.byteAlignment());
			offset += b.layout.byteSize();
			return member;
		}

		private StructLayout layout() {
			if (!Group.flex(members)) addPadding(layouts, offset, align);
			return Layouts.struct(layouts);
		}
	}

	/**
	 * Returns operational support for the type.
	 */
	public static <T extends Struct<T>> Support<T> support(Class<T> cls) {
		if (cls == null) return null;
		return Reflect.unchecked(cache.computeIfAbsent(cls, _ -> new Builder<>(cls).build()));
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
	Support<T> support() {
		var support = this.support;
		if (support == null) {
			support = support(Reflect.getClass(this));
			this.support = support;
		}
		return support;
	}

	// support

	private static long addPadding(List<MemoryLayout> layouts, long offset, long align) {
		var padding = Layouts.padding(offset, align);
		if (padding != 0) layouts.add(MemoryLayout.paddingLayout(padding));
		return padding;
	}

	private static long flexScale(Member<?> flex, int count) {
		return ((SequenceLayout) flex.layout).elementLayout().scale(flex.offset, count);
	}
}
