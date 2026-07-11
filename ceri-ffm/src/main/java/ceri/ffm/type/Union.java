package ceri.ffm.type;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.UnionLayout;
import java.nio.ByteOrder;
import java.util.Objects;
import ceri.common.collect.Lists;
import ceri.common.concurrent.Lazy;
import ceri.common.reflect.Reflect;
import ceri.common.text.Transformer;
import ceri.common.util.Hasher;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Native;
import ceri.ffm.core.Segments;

public class Union<T extends Union<T>> extends Group<T, UnionLayout> {
	private static final Lazy.ForClass<Group.Config<? extends Union<?>, UnionLayout>> cache =
		Lazy.forClass(c -> new Builder<>(Reflect.unchecked(c)).build(false));
	private volatile MemorySegment memory = null; // last used segment
	private volatile int activeIndex = Group.INVALID;

	/**
	 * Operational support for union types.
	 */
	public static class Supporter<T extends Union<T>> extends Group.Supporter<T, UnionLayout> {

		private Supporter(Group.Config<T, UnionLayout> config, UnionLayout layout) {
			super(config, layout);
		}

		@Override
		public Native.Kind kind() {
			return Native.Kind.UNION;
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

		@Override
		T rawGet(MemorySegment memory, long offset, long length) {
			var union = val();
			rawRead(memory, offset, length, union);
			return union;
		}

		@Override
		void rawRead(MemorySegment memory, long offset, long length, T union) {
			var member = union.activeMember(config);
			if (member != null) member.read(union, memory, offset);
			memory(union, memory, offset);
		}

		@Override
		void rawWrite(MemorySegment memory, long offset, long length, T union) {
			var member = union.autoActiveMember(config);
			if (member != null) member.write(union, memory, offset);
			memory(union, memory, offset);
		}

		private void memory(T union, MemorySegment memory, long offset) {
			union.memory(Segments.slice(memory, offset, layoutSize()));
		}
	}

	/**
	 * Builds union operation support instance.
	 */
	private static class Builder<T extends Union<T>> extends Group.Config.Builder<T, UnionLayout> {

		private Builder(Class<T> type) {
			super(type);
		}

		@Override
		Group.Member<?> member(Group.Member.Builder member) {
			return member.build(0L);
		}

		@Override
		UnionLayout layout() {
			return Layouts.union(layouts);
		}
	}

	static <T extends Union<T>> Config<T, UnionLayout> config(Class<T> cls) {
		return Reflect.unchecked(cache.get(cls));
	}

	/**
	 * Returns operational support for the type.
	 */
	public static <T extends Union<T>> Supporter<T> support(Class<T> cls) {
		return Reflect.unchecked(Supports.DEF.from(cls));
	}

	/**
	 * Creates a support instance for the type.
	 */
	static <T extends Union<T>> Supporter<T> supportFor(Class<T> cls) {
		var config = config(cls);
		return new Supporter<>(config, config.layout());
	}

	/**
	 * Creates an instance of the union, with flexible array member initialized to given count.
	 */
	public static <T extends Union<T>> T init(Class<T> cls) {
		if (cls == null) return null;
		return support(cls).init(null);
	}

	/**
	 * Sets the active member by index.
	 */
	public T active(int index) {
		return active(config(), index);
	}

	/**
	 * Sets the active member by name.
	 */
	public T active(String name) {
		var config = config();
		return active(config, config.indexOf(name));
	}

	/**
	 * Sets the currently active member value.
	 */
	public T set(Object value) {
		var member = activeMember(config());
		if (member != null) member.set(this, value);
		return typedThis();
	}

	/**
	 * Gets the currently active member value.
	 */
	public <R> R get() {
		var member = autoActiveMember(config());
		return member == null ? null : Reflect.unchecked(member.get(this));
	}

	/**
	 * Reads the current active member value from the last referenced memory segment.
	 */
	public T read() {
		var member = activeMember(config());
		var memory = memory();
		if (member != null && memory != null) member.read(this, memory, 0L);
		return typedThis();
	}

	/**
	 * Writes the currently active member value to the last referenced memory segment.
	 */
	public T write() {
		var config = config();
		var member = autoActiveMember(config);
		if (member != null) member.write(this, memory(config), 0L);
		return typedThis();
	}

	@Override
	public int hashCode() {
		var config = config();
		var member = activeMember(config);
		if (member == null) return hashCode(config);
		return Hasher.deep(member.get(this));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Union<?> u)) return false;
		var config = config();
		var member = activeMember(config);
		if (member == null) return equals(config, u);
		return Objects.deepEquals(member.get(this), member.get(u));
	}

	// shared

	@Override
	String memberString(Transformer transformer, Group.Member<?> member) {
		var s = super.memberString(transformer, member);
		if (config().member(activeIndex()) == member) s += " *";
		return s;
	}

	@Override
	Config<T, UnionLayout> configFor(Class<T> cls) {
		return config(cls);
	}

	void memory(MemorySegment memory) {
		this.memory = memory;
	}

	<R> Group.Member<R> activeMember(Config<T, ?> config) {
		return config.member(activeIndex());
	}

	Group.Member<?> autoActiveMember(Config<T, ?> config) {
		Group.Member<?> member = config.member(activeIndex());
		if (member != null) return member;
		return findActiveMember(config);
	}

	// support

	private int activeIndex() {
		return activeIndex;
	}

	private T active(Config<T, ?> config, int index) {
		if (Lists.in(config.members(), index)) activeIndex = index;
		return typedThis();
	}

	private Group.Member<?> findActiveMember(Config<T, ?> config) {
		for (int i = 0; i < config.members().size(); i++) {
			var member = config.member(i);
			var current = member.get(this);
			if (current == null || Objects.deepEquals(current, member.val())) continue;
			active(i);
			return Reflect.unchecked(member);
		}
		return null;
	}

	private MemorySegment memory() {
		return memory;
	}

	private MemorySegment memory(Config<T, ?> config) {
		var memory = memory();
		if (!Segments.isAlive(memory)) {
			memory = Segments.auto().allocate(config.layout());
			memory(memory);
		}
		return memory;
	}
}
