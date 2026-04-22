package ceri.ffm.type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import ceri.common.array.Dimensions;
import ceri.common.array.RawArray;
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
import ceri.common.util.Hasher;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Native;
import ceri.ffm.reflect.Refine;
import ceri.ffm.util.Args;

public abstract class Group<T extends Group<T>> {
	private static final String INDENT = "  ";
	private static final String FIELDS = Fields.class.getSimpleName();
	static final int INVALID = -1;

	/**
	 * Struct fields in order. All fields must be named in subclasses, not just the added fields.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public static @interface Fields {
		String[] value();
	}

	/**
	 * Provides actions for a struct member.
	 */
	private record Actions<T>(Functions.Operator<T> init, Functions.ObjIntFunction<T, T> flexInit,
		Update<T> update, Write<T> write) {

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
			void accept(MemorySegment memory, long offset, T t);
		}

		/**
		 * Initializes a type instance with optional flex array size.
		 */
		public T init(T t, int flexSize) {
			if (flexInit() == null || flexSize < 0) return init().apply(t);
			return flexInit().apply(t, flexSize);
		}

		/**
		 * Updates a type instance from memory.
		 */
		public T update(MemorySegment memory, long offset, T t) {
			if (t == null) t = init(null, -1);
			return update().apply(memory, offset, t);
		}

		/**
		 * Writes a type instance to memory.
		 */
		public void write(MemorySegment memory, long offset, T t) {
			if (t == null) t = init(null, -1);
			write().accept(memory, offset, t);
		}
	}

	/**
	 * Member configuration.
	 */
	static class Member<T> {
		final String name;
		final Native.Kind.Spec spec;
		final Dimensions dims;
		final long offset;
		final MemoryLayout layout;
		final VarHandle accessor;
		final Actions<T> actions;
		final boolean flex;

		static class Builder {
			final String name;
			final Native.Kind.Spec spec;
			final Refine.Context context;
			final VarHandle accessor;
			Dimensions dims = Dimensions.NONE;
			MemoryLayout layout = null;
			Actions<?> actions = null;
			boolean flex = false;

			Builder(Field field) {
				this.name = field.getName();
				spec = Native.Kind.spec(Generics.typed(field));
				context = Refine.context(field);
				accessor = Handles.handle(field);
			}

			Builder flex(boolean flex) {
				this.flex = flex;
				return this;
			}

			Builder dims(Dimensions dims) {
				this.dims = dims;
				return this;
			}

			Builder layout(MemoryLayout layout) {
				this.layout = layout;
				return this;
			}

			<T> Builder actions(Functions.Operator<T> init, Functions.ObjIntFunction<T, T> flexInit,
				Actions.Update<T> update, Actions.Write<T> write) {
				actions = new Actions<>(init, flexInit, update, write);
				return this;
			}

			Member<?> build(long offset) {
				return new Member<>(name, spec, dims, offset, layout, accessor, actions, flex);
			}
		}

		private Member(String name, Native.Kind.Spec spec, Dimensions dims, long offset,
			MemoryLayout layout, VarHandle accessor, Actions<T> actions, boolean flex) {
			this.name = name;
			this.spec = spec;
			this.dims = dims;
			this.offset = offset;
			this.layout = layout;
			this.accessor = accessor;
			this.actions = actions;
			this.flex = flex;
		}

		@Override
		public String toString() {
			return String.format("0x%02x %s =%s", offset, desc(), layout);
		}

		String desc() {
			return (flex ? spec.array().toString() : spec.array().toString(dims)) + ' ' + name;
		}

		T get(Group<?> group) {
			return Handles.get(accessor, group);
		}

		void set(Group<?> group, T value) {
			accessor.set(group, value);
		}

		T val() {
			return actions.init(null, INVALID);
		}

		void init(Group<?> group) {
			init(group, INVALID);
		}

		void init(Group<?> group, int flexSize) {
			var current = get(group);
			var updated = actions.init(current, flexSize);
			if (current != updated) set(group, updated);
		}

		void read(Group<?> group, MemorySegment memory, long offset) {
			var current = get(group);
			var updated = actions.update(memory, offset + this.offset, current);
			if (updated != null && updated != current) set(group, updated);
		}

		void write(Group<?> group, MemorySegment memory, long offset) {
			var current = get(group);
			actions.write(memory, offset + this.offset, current);
		}
	}

	/**
	 * Operational support for group types.
	 */
	public static abstract class Support<T extends Group<T>, L extends GroupLayout>
		extends ceri.ffm.type.Support.Typed<T, L> {
		final Config<T> config;

		static class Config<T extends Group<T>> {
			private final Class<T> type;
			private final Functions.Supplier<T> constructor;
			private final Map<String, Integer> lookup;
			private final List<Member<?>> members;

			Config(Class<T> type, Functions.Supplier<T> constructor, List<Member<?>> members) {
				this.type = type;
				this.constructor = constructor;
				this.members = Immutable.wrap(members);
				lookup = lookup(members);
			}
		}

		Support(Config<T> config, L layout) {
			super(layout);
			this.config = config;
		}

		@Override
		public boolean immutable() {
			return false;
		}

		@Override
		public Class<T> type() {
			return config.type;
		}

		@Override
		public T val() {
			return config.constructor.get();
		}

		@Override
		public T init(T group) {
			if (group == null) group = val();
			for (var member : members())
				member.init(group);
			return group;
		}

		@Override
		public final String toString() {
			return ToString.ofName(Reflect.name(type()), Layouts.desc(layout()))
				.childrens(members()).toString();
		}

		abstract String typeName();

		boolean flex() {
			return Group.flex(members());
		}
		
		int[] indexes(String... names) {
			if (names == null) return null;
			int[] indexes = new int[names.length];
			for (int i = 0; i < indexes.length; i++)
				indexes[i] = indexOf(names[i]);
			return indexes;
		}

		int indexOf(String name) {
			return config.lookup.getOrDefault(name, INVALID);
		}

		<R> Member<R> member(int index) {
			return Reflect.unchecked(Lists.at(members(), index));
		}

		List<Member<?>> members() {
			return config.members;
		}

		int hash(T group) {
			return Hasher.deep(values(group));
		}

		boolean equals(T groupL, T groupR) {
			if (groupL == groupR) return true;
			if (groupL == null || groupR == null) return false;
			return Objects.deepEquals(values(groupL), values(groupR));
		}

		private Object[] values(T group) {
			if (group == null) return null;
			Object[] values = new Object[members().size()];
			for (int i = 0; i < values.length; i++)
				values[i] = member(i).get(group);
			return values;
		}
	}

	static abstract class Builder<T extends Group<T>> {
		final Class<T> type;
		final Functions.Supplier<T> constructor;
		final List<MemoryLayout> layouts = Lists.of();
		final List<Member<?>> members = Lists.of();

		Builder(Class<T> type) {
			this.type = type;
			constructor = Handles.asSupplier(Handles.constructor(type));
		}

		abstract Support<T, ? extends GroupLayout> build();

		abstract Member<?> member(Member.Builder member);

		void addMembers(boolean allowFlex) {
			var classFields = classFields(type);
			var names = fieldNames(type);
			for (int i = 0; i < names.size(); i++) {
				var field = findField(type, classFields, names.get(i));
				var b = new Member.Builder(field);
				b.flex(allowFlex && i == names.size() - 1 && isFlex(b.spec));
				populate(b);
				var member = member(b);
				layouts.add(member.layout);
				members.add(member);
			}
			verifyClassFields(type, classFields);
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
			var s = Group.supportWith(support, member);
			if (member.spec.array().isArray()) arrayMember(s, member);
			else member.layout(s.layout()).actions(s::init, null, s::update, s::write);
		}

		private <A> void arrayMember(ceri.ffm.type.Support<?, A, ?> support,
			Member.Builder member) {
			var dims = arrayDims(constructor.get(), member);
			var nul = member.context.nul();
			var layout = Layouts.array(support.layout(), dims);
			member.layout(layout).dims(dims).actions(t -> deepInit(support, t, dims),
				member.flex ? (t, n) -> deepFlexInit(support, t, n) : null,
				(m, o, t) -> deepRead(support, m, o, t, nul),
				(m, o, t) -> support.deepWrite(m, o, t, nul));
		}

		private static Object deepInit(ceri.ffm.type.Support<?, ?, ?> support, Object t,
			Dimensions dims) {
			if (t == null) return support.deepInitOf(dims);
			return support.deepInit(t);
		}

		private static Object deepFlexInit(ceri.ffm.type.Support<?, ?, ?> support, Object t,
			int flexSize) {
			if (t == null || RawArray.length(t) != flexSize) return support.arrayInit(flexSize);
			return support.deepInit(t);
		}

		private static Object deepRead(ceri.ffm.type.Support<?, ?, ?> support, MemorySegment m,
			long o, Object t, boolean nul) {
			support.deepRead(m, o, t, nul);
			return t;
		}
	}

	public String toString(Args args) {
		var support = support();
		var b = new StringBuilder(support.typeName()).append(' ')
			.append(support.type().getSimpleName()).append(" {");
		for (var member : support.config.members)
			b.append(Strings.EOL).append(Text.prefixLines(INDENT, memberString(args, member)));
		if (!support.config.members.isEmpty()) b.append(Strings.EOL);
		return b.append('}').toString();
	}

	@Override
	public int hashCode() {
		return support().hash(typedThis());
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Group<?> g)) return false;
		var support = support();
		if (support != g.support()) return false;
		return support.equals(typedThis(), Reflect.unchecked(g));
	}

	@Override
	public String toString() {
		return toString(Args.FULL);
	}

	// shared

	abstract Support<T, ? extends GroupLayout> support();

	String memberString(Args args, Group.Member<?> member) {
		return member.desc() + " = " + args.arg(member.get(this)) + ';';
	}

	T typedThis() {
		return Reflect.unchecked(this);
	}

	static boolean flex(List<Member<?>> members) {
		return !members.isEmpty() && Lists.last(members).flex;
	}

	// support

	private static <T> ceri.ffm.type.Support<T, ?, ?>
		supportWith(ceri.ffm.type.Support<?, ?, ?> support, Member.Builder member) {
		return Reflect
			.unchecked(support.with(member.name, member.context.align(), member.context.order()));
	}

	private static boolean isFlex(Native.Kind.Spec spec) {
		return spec.array().dimensions() == 1;
	}

	private static Map<String, Integer> lookup(List<Member<?>> members) {
		var map = Maps.<String, Integer>of();
		for (int i = 0; i < members.size(); i++)
			map.put(members.get(i).name, i);
		return Immutable.wrap(map);
	}

	private static <A> Dimensions arrayDims(Group<?> struct, Member.Builder member) {
		A array = Handles.get(member.accessor, struct);
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
