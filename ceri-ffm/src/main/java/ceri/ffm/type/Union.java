package ceri.ffm.type;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.UnionLayout;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import ceri.common.array.Dimensions;
import ceri.common.collect.Immutable;
import ceri.common.collect.Lists;
import ceri.common.collect.Maps;
import ceri.common.except.Exceptions;
import ceri.common.function.Functions;
import ceri.common.reflect.Annotations;
import ceri.common.reflect.Generics;
import ceri.common.reflect.Handles;
import ceri.common.reflect.Reflect;
import ceri.common.text.Joiner;
import ceri.common.text.Strings;
import ceri.common.text.Text;
import ceri.common.text.ToString;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Native;
import ceri.ffm.core.Segments;
import ceri.ffm.reflect.Refine;
import ceri.ffm.test.FfmTesting;
import ceri.ffm.type.Struct.Fields;
import ceri.ffm.util.Args;

public class Union<T extends Union<T>> {
	private static final int INACTIVE = -1;
	private static final String INDENT = "  ";
	private static final String FIELDS = Fields.class.getSimpleName();
	private static final Map<Class<?>, Support<?>> cache = Maps.syncWeak();
	private volatile Support<T> support = null;
	private volatile MemorySegment memory = null; // last used segment
	private volatile int activeIndex = INACTIVE;

	@Fields({ "s", "b" })
	public static class S0 extends Struct<S0> {
		public static final Struct.Support<S0> $ = Struct.support(S0.class);
		public short s;
		public byte b;
	}

	@Fields({ "l", "s", "s0" })
	public static class U0 extends Union<U0> {
		public static final Union.Support<U0> $ = Union.support(U0.class);
		public long l;
		public short s;
		public S0[] s0 = new S0[2];
	}

	@Fields({ "s", "b", "u0" })
	public static class S1 extends Struct<S1> {
		public static final Struct.Support<S1> $ = Struct.support(S1.class);
		public short s;
		public byte b;
		public U0 u0;
	}

	public static void main(String[] args) {
		S1 s = S1.$.init();
		s.u0.l = 0x123456789abcdefL;
		FfmTesting.arg(s);
		var m = S1.$.alloc(Segments.auto(), s);
		FfmTesting.print(m);
		FfmTesting.arg(s);
		s.u0.active(2).read();
		FfmTesting.arg(s);
	}

	// superclass = Group -> Union, Struct
	// - same field name lookup (not needed for union?)
	// - same member type lookup

	/**
	 * Provides actions for a union member.
	 */
	record Actions<T>(Functions.Operator<T> init, Update<T> update, Write<T> write) {

		/**
		 * Updates type instance from memory.
		 */
		public interface Update<T> {
			T apply(MemorySegment memory, long offset, T t);
		}

		/**
		 * Writes type instance to memory.
		 */
		public interface Write<T> {
			void accept(T t, MemorySegment memory, long offset);
		}

		/**
		 * Initializes a type instance.
		 */
		public T init(T t) {
			return init().apply(t);
		}

		/**
		 * Updates a type instance from memory.
		 */
		public T update(MemorySegment memory, long offset, T t) {
			if (t == null) t = init(null);
			return update().apply(memory, offset, t);
		}

		/**
		 * Writes a type instance to memory.
		 */
		public void write(T t, MemorySegment memory, long offset) {
			if (t == null) t = init(null);
			write().accept(t, memory, offset);
		}
	}

	/**
	 * Member configuration.
	 */
	static class Member<T> {
		public final String name;
		public final Native.Kind.Spec spec;
		public final Dimensions dims;
		public final MemoryLayout layout;
		public final VarHandle accessor;
		public final Actions<T> actions;

		public static class Builder {
			private final String name;
			private final Native.Kind.Spec spec;
			private final Refine.Context context;
			private final VarHandle accessor;
			private Dimensions dims = Dimensions.NONE;
			private MemoryLayout layout = null;
			private Actions<?> actions = null;

			private Builder(Field field) {
				this.name = field.getName();
				spec = Native.Kind.spec(Generics.typed(field));
				context = Refine.context(field);
				accessor = Handles.handle(field);
			}

			public Builder dims(Dimensions dims) {
				this.dims = dims;
				return this;
			}

			public Builder layout(MemoryLayout layout) {
				this.layout = layout;
				return this;
			}

			public <T> Builder actions(Functions.Operator<T> init, Actions.Update<T> update,
				Actions.Write<T> write) {
				actions = new Actions<>(init, update, write);
				return this;
			}

			public Member<?> build() {
				return new Member<>(name, spec, dims, layout, accessor, actions);
			}
		}

		private Member(String name, Native.Kind.Spec spec, Dimensions dims, MemoryLayout layout,
			VarHandle accessor, Actions<T> actions) {
			this.name = name;
			this.spec = spec;
			this.dims = dims;
			this.layout = layout;
			this.accessor = accessor;
			this.actions = actions;
		}

		private T get(Union<?> union) {
			return Handles.get(accessor, union);
		}

		private void set(Union<?> union, T value) {
			accessor.set(union, value);
		}

		private void init(Union<?> union) {
			var current = get(union);
			var updated = actions.init(current);
			if (current != updated) set(union, updated);
		}

		private void read(Union<?> union, MemorySegment memory, long offset) {
			var current = get(union);
			var updated = actions.update(memory, offset, current);
			if (updated != null && updated != current) set(union, updated);
		}

		private void write(Union<?> union, MemorySegment memory, long offset) {
			var current = get(union);
			actions.write(current, memory, offset);
		}

		private T val() {
			return actions.init(null);
		}

		public String desc() {
			return spec.array().toString(dims) + ' ' + name;
		}

		@Override
		public final String toString() {
			return String.format("%s =%s", desc(), layout);
		}
	}

	/**
	 * Operational support for union types.
	 */
	public static class Support<T extends Union<T>>
		extends ceri.ffm.type.Support.Typed<T, UnionLayout> {
		private final Class<T> type;
		private final Functions.Supplier<T> constructor;
		private final Map<String, Integer> lookup;
		private final List<Member<?>> members;

		private Support(Class<T> type, Functions.Supplier<T> constructor, UnionLayout layout,
			List<Member<?>> members) {
			super(layout);
			this.type = type;
			this.constructor = constructor;
			this.members = members;
			lookup = lookup(members);
		}

		@Override
		public boolean immutable() {
			return false;
		}

		@Override
		public Class<T> type() {
			return type;
		}

		@Override
		public Support<T> with(String name, long align, ByteOrder order) {
			return this; // not modifiable
		}

		@Override
		public T val() {
			return constructor.get();
		}

		@Override
		public T init(T union) {
			if (union == null) union = val();
			for (var member : members)
				member.init(union);
			return union;
		}

		/**
		 * Sets the active member by index.
		 */
		public void active(T union, int index) {
			if (union != null && Lists.in(members, index)) union.activeIndex(index);
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

		public String desc() {
			var b = new StringBuilder("union ").append(type().getSimpleName()).append(" {");
			for (var member : members)
				b.append(Strings.EOL).append(INDENT).append(member.desc()).append(';');
			if (!members.isEmpty()) b.append(Strings.EOL);
			return b.append('}').toString();
		}

		@Override
		public final String toString() {
			return ToString.ofClass(this, Reflect.name(type())).childrens(members).toString();
		}

		@Override
		T rawGet(MemorySegment memory, long offset) {
			var union = constructor.get();
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
		void rawWrite(T union, MemorySegment memory, long offset) {
			var member = autoActiveMember(union);
			if (member != null) member.write(union, memory, offset);
			memory(union, memory, offset);
		}

		private <R> Member<R> autoActiveMember(T union) {
			if (union == null) return null;
			var member = this.<R>member(union.activeIndex());
			if (member == null) member = findActiveMember(union, members);
			return member;
		}

		private void memory(T union, MemorySegment memory, long offset) {
			union.memory(Segments.slice(memory, offset, size(1)));
		}

		private int indexOf(String name) {
			return lookup.getOrDefault(name, INACTIVE);
		}

		private <R> Member<R> member(int index) {
			return Reflect.unchecked(Lists.at(members, index));
		}

		private <R> Member<R> activeMember(T union) {
			if (union == null) return null;
			return member(union.activeIndex());
		}
	}

	private static class Builder<T extends Union<T>> {
		private final Class<T> type;
		private final Functions.Supplier<T> constructor;
		private final List<MemoryLayout> layouts = Lists.of();
		private final List<Member<?>> members = Lists.of();

		private Builder(Class<T> type) {
			this.type = type;
			constructor = Handles.asSupplier(Handles.constructor(type));
		}

		private Support<T> build() {
			addMembers(type);
			return new Support<>(type, constructor, layout(), Immutable.wrap(members));
		}

		private UnionLayout layout() {
			return Layouts.union(layouts);
		}

		private void addMembers(Class<?> cls) {
			var classFields = classFields(cls);
			var names = fieldNames(cls);
			int lastIndex = names.size() - 1;
			for (int i = 0; i <= lastIndex; i++) {
				var field = findField(cls, classFields, names.get(i));
				var member = new Member.Builder(field);
				populate(member);
				add(member);
			}
			verifyClassFields(cls, classFields);
		}

		private void populate(Member.Builder member) {
			switch (member.spec.kind()) {
				case primitive, boxed -> primitive(member);
				case intType -> intType(member);
				// case string -> stringMember(member);
				// case buffer -> bufferMember(member);
				// case pointer, pointerType -> Layouts.POINTER;
				case struct -> struct(member);
				case union -> union(member);
				// case union -> TBD;
				default -> throw Exceptions.illegalArg("Unsupported type: " + member.spec.typed());
			}
		}

		private void primitive(Member.Builder member) {
			member(Primitive.of(member.spec.component()), member);
		}

		private void intType(Member.Builder member) {
			member(IntType.support(member.spec.component()), member);
		}

		private void struct(Member.Builder member) {
			member(Struct.support(member.spec.component()), member);
		}

		private void union(Member.Builder member) {
			member(Union.support(member.spec.component()), member);
		}

		private void member(ceri.ffm.type.Support<?, ?, ?> support, Member.Builder member) {
			var s = Union.supportWith(support, member);
			if (member.spec.array().isArray()) arrayMember(s, member);
			else member.layout(support.layout()).actions(s::init, s::update, s::write);
		}

		private <A> void arrayMember(ceri.ffm.type.Support<?, A, ?> support,
			Member.Builder member) {
			var dims = arrayDims(constructor.get(), member);
			var nul = member.context.nul();
			var layout = Layouts.array(support.layout(), dims);
			member.layout(layout).dims(dims).actions(t -> deepInit(support, dims, t),
				(m, o, t) -> deepRead(support, nul, m, o, t),
				(t, m, o) -> support.deepWrite(t, m, o, nul));
		}

		private static Object deepInit(ceri.ffm.type.Support<?, ?, ?> support, Dimensions dims,
			Object t) {
			if (t == null) return support.deepVal(dims);
			return support.deepInit(t);
		}

		private static Object deepRead(ceri.ffm.type.Support<?, ?, ?> support, boolean nul,
			MemorySegment m, long o, Object t) {
			support.deepRead(m, o, t, nul);
			return t;
		}

		private void add(Member.Builder member) {
			var layout = member.layout;
			layouts.add(layout);
			members.add(member.build());
		}
	}

	/**
	 * Returns operational support for the type.
	 */
	public static <T extends Union<T>> Support<T> support(Class<T> cls) {
		return Reflect.unchecked(cache.computeIfAbsent(cls, _ -> supportFor(cls)));
	}

	/**
	 * Creates an instance of the union, with flexible array member initialized to given count.
	 */
	public static <T extends Union<T>> T init(Class<T> cls) {
		if (cls == null) return null;
		return support(cls).init(null);
	}

	public T active(int index) {
		return action((s, t) -> s.active(t, index));
	}

	public T active(String name) {
		return action((s, t) -> s.active(t, name));
	}

	public T set(Object value) {
		return action((s, t) -> s.setActive(t, value));
	}

	public <R> R get() {
		return support().getActive(typedThis());
	}

	public T read() {
		return action(Support::readActive);
	}

	public T write() {
		return action(Support::writeActive);
	}

	private T action(Functions.BiConsumer<Support<T>, T> consumer) {
		var t = typedThis();
		consumer.accept(support(), t);
		return t;
	}

	public String toString(Args args) {
		var support = support();
		var b = new StringBuilder("union ").append(support.type().getSimpleName()).append(" {");
		for (int i = 0; i < support.members.size(); i++) {
			var member = support.member(i);
			var value = member.desc() + " = " + args.arg(member.get(this)) + ';';
			b.append(Strings.EOL).append(Text.prefixLines(INDENT, value));
			if (activeIndex == i) b.append(" *");
		}
		if (!support.members.isEmpty()) b.append(Strings.EOL);
		return b.append('}').toString();
	}

	@Override
	public String toString() {
		return toString(Args.DEFAULT);
	}

	//

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

	T typedThis() {
		return Reflect.unchecked(this);
	}

	private static <T extends Union<T>> Support<T> supportFor(Class<T> cls) {
		return new Builder<>(cls).build();
	}

	private static <T> ceri.ffm.type.Support<T, ?, ?>
		supportWith(ceri.ffm.type.Support<?, ?, ?> support, Member.Builder member) {
		return Reflect
			.unchecked(support.with(member.name, member.context.align(), member.context.order()));
	}

	private static <R> Member<R> findActiveMember(Union<?> union, List<Member<?>> members) {
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

	private static Map<String, Integer> lookup(List<Member<?>> members) {
		var map = Maps.<String, Integer>of();
		for (int i = 0; i < members.size(); i++)
			map.put(members.get(i).name, i);
		return Immutable.wrap(map);
	}

	private static <A> Dimensions arrayDims(Union<?> union, Member.Builder member) {
		A array = Handles.get(member.accessor, union);
		if (array != null) return Dimensions.from(array);
		int count = member.spec.array().dimensions();
		var dims = member.context.dims(null);
		if (dims == null) return Dimensions.ofZeros(count);
		if (dims.count() == count) return dims;
		return MultiArray.fix(dims, count, false, 0);
	}

	private static void verifyClassFields(Class<?> cls, Map<String, Field> classFields) {
		if (classFields.isEmpty()) return;
		throw Exceptions.illegalArg("@%s missing from %s: %s", FIELDS, Reflect.name(cls),
			Joiner.COMMA_COMPACT.join(classFields.keySet()));
	}

	private static Field findField(Class<?> cls, Map<String, Field> classFields, String name) {
		var field = classFields.remove(name);
		if (field != null) return field;
		throw Exceptions.illegalArg("@%s not found in %s: %s", FIELDS, Reflect.name(cls), name);
	}

	private static Map<String, Field> classFields(Class<?> cls) {
		var map = Maps.<String, Field>link();
		for (var field : cls.getFields()) {
			var mods = field.getModifiers();
			if (Modifier.isPublic(mods) && !Modifier.isTransient(mods) && !Modifier.isStatic(mods))
				map.put(field.getName(), field);
		}
		return map;
	}

	private static List<String> fieldNames(Class<?> cls) {
		var fields = Annotations.value(cls, Fields.class, Fields::value);
		if (fields != null) return Lists.wrap(fields);
		throw new IllegalStateException(String
			.format("@%s ({...}) annotation must be declared on %s", FIELDS, Reflect.name(cls)));
	}
}
