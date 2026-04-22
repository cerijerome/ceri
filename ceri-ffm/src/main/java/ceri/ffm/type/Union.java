package ceri.ffm.type;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.UnionLayout;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import ceri.common.collect.Lists;
import ceri.common.collect.Maps;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect;
import ceri.common.util.Hasher;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Segments;
import ceri.ffm.util.Args;

public class Union<T extends Union<T>> extends Group<T> {
	private static final Map<Class<?>, Support<?>> cache = Maps.syncWeak();
	private volatile Support<T> support = null;
	private volatile MemorySegment memory = null; // last used segment
	private volatile int activeIndex = Group.INVALID;

	/**
	 * Operational support for union types.
	 */
	public static class Support<T extends Union<T>> extends Group.Support<T, UnionLayout> {

		private Support(Group.Support.Config<T> config, UnionLayout layout) {
			super(config, layout);
		}

		@Override
		public Support<T> with(String name, long align, ByteOrder order) {
			if (align < layout().byteAlignment()) align = 0L;
			return Layouts.with(this, l -> new Support<T>(config, l), layout(), name, 0L, null);
		}

		/**
		 * Sets the active member by index.
		 */
		public void active(T union, int index) {
			if (union != null && Lists.in(members(), index)) union.activeIndex(index);
		}

		/**
		 * Sets the active member by name.
		 */
		public void active(T union, String name) {
			active(union, indexOf(name));
		}

		/**
		 * Sets the active member value if set.
		 */
		public boolean setActive(T union, Object value) {
			var member = activeMember(union);
			if (member == null) return false;
			member.set(union, value);
			return true;
		}

		/**
		 * Gets the active member value if set, otherwise null.
		 */
		public <R> R getActive(T union) {
			var member = this.<R>autoActiveMember(union);
			if (member == null) return null;
			return member.get(union);
		}

		/**
		 * Reads the active member value from last memory segment if set.
		 */
		public boolean readActive(T union) {
			var member = activeMember(union);
			var memory = union.memory();
			if (member == null || memory == null) return false;
			member.read(union, memory, 0L);
			return true;
		}

		/**
		 * Writes active member to last memory segment if set or allocates new scratch memory.
		 */
		public boolean writeActive(T union) {
			var member = autoActiveMember(union);
			if (member == null) return false;
			member.write(union, Union.memory(union, layout()), 0L);
			return true;
		}

		@Override
		T rawGet(MemorySegment memory, long offset) {
			var union = val();
			rawRead(memory, offset, union);
			return union;
		}

		@Override
		void rawRead(MemorySegment memory, long offset, T union) {
			var member = activeMember(union);
			if (member != null) member.read(union, memory, offset);
			memory(union, memory, offset);
		}

		@Override
		void rawWrite(MemorySegment memory, long offset, T union) {
			var member = autoActiveMember(union);
			if (member != null) member.write(union, memory, offset);
			memory(union, memory, offset);
		}

		@Override
		String typeName() {
			return "union";
		}

		@Override
		int hash(T union) {
			var member = activeMember(union);
			if (member == null) return super.hash(union);
			return Hasher.deep(member.get(union));
		}

		@Override
		boolean equals(T unionL, T unionR) {
			var member = activeMember(unionL);
			if (member == null) return super.equals(unionL, unionR);
			return Objects.deepEquals(member.get(unionL), member.get(unionR));
		}

		private <R> Group.Member<R> autoActiveMember(T union) {
			if (union == null) return null;
			var member = this.<R>member(union.activeIndex());
			if (member == null) member = findActiveMember(union, members());
			return member;
		}

		private void memory(T union, MemorySegment memory, long offset) {
			union.memory(Segments.slice(memory, offset, size(1)));
		}

		private <R> Group.Member<R> activeMember(T union) {
			if (union == null) return null;
			return member(union.activeIndex());
		}
	}

	/**
	 * Builds union operation support instance.
	 */
	private static class Builder<T extends Union<T>> extends Group.Builder<T> {

		private Builder(Class<T> type) {
			super(type);
		}

		@Override
		Support<T> build() {
			addMembers(false);
			return new Support<>(new Group.Support.Config<>(type, constructor, members), layout());
		}

		@Override
		Group.Member<?> member(Group.Member.Builder member) {
			return member.build(0L);
		}

		private UnionLayout layout() {
			return Layouts.union(layouts);
		}
	}

	/**
	 * Returns operational support for the type.
	 */
	public static <T extends Union<T>> Support<T> support(Class<T> cls) {
		if (cls == null) return null;
		return Reflect.unchecked(cache.computeIfAbsent(cls, _ -> new Builder<>(cls).build()));
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
		return action((s, t) -> s.active(t, index));
	}

	/**
	 * Sets the active member by name.
	 */
	public T active(String name) {
		return action((s, t) -> s.active(t, name));
	}

	/**
	 * Sets the currently active member value.
	 */
	public T set(Object value) {
		return action((s, t) -> s.setActive(t, value));
	}

	/**
	 * Gets the currently active member value.
	 */
	public <R> R get() {
		return support().getActive(typedThis());
	}

	/**
	 * Reads the current active member value from the last referenced memory segment.
	 */
	public T read() {
		return action(Support::readActive);
	}

	/**
	 * Writes the currently active member value to the last referenced memory segment.
	 */
	public T write() {
		return action(Support::writeActive);
	}

	// shared

	@Override
	String memberString(Args args, Group.Member<?> member) {
		var s = super.memberString(args, member);
		if (support().member(activeIndex()) == member) s += " *";
		return s;
	}

	@Override
	Support<T> support() {
		var support = this.support;
		if (support == null) {
			support = support(Reflect.getClass(this));
			this.support = support;
		}
		return support;
	}

	void activeIndex(int index) {
		activeIndex = index;
	}

	int activeIndex() {
		return activeIndex;
	}

	void memory(MemorySegment memory) {
		this.memory = memory;
	}

	MemorySegment memory() {
		return memory;
	}

	// support

	private T action(Functions.BiConsumer<Support<T>, T> consumer) {
		var t = typedThis();
		consumer.accept(support(), t);
		return t;
	}

	private static <R> Group.Member<R> findActiveMember(Union<?> union,
		List<Group.Member<?>> members) {
		for (int i = 0; i < members.size(); i++) {
			var member = members.get(i);
			var current = member.get(union);
			if (current == null || Objects.deepEquals(current, member.val())) continue;
			union.active(i);
			return Reflect.unchecked(member);
		}
		return null;
	}

	private static MemorySegment memory(Union<?> union, MemoryLayout layout) {
		var memory = union.memory();
		if (!Segments.isAlive(memory)) {
			memory = Segments.auto().allocate(layout);
			union.memory(memory);
		}
		return memory;
	}
}
