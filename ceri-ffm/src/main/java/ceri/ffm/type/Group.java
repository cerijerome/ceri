package ceri.ffm.type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SequenceLayout;
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
import ceri.common.reflect.Handles;
import ceri.common.reflect.Reflect;
import ceri.common.text.Joiner;
import ceri.common.text.Strings;
import ceri.common.text.Text;
import ceri.common.text.ToString;
import ceri.common.text.Transformer;
import ceri.common.util.Hasher;
import ceri.ffm.core.Caller;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Segments;
import ceri.ffm.reflect.TypeNode;

/**
 * Provides core functionality for structs and unions.
 */
public abstract class Group<T extends Group<T, L>, L extends GroupLayout> {
	private static final String INDENT = "  ";
	private static final String FIELDS = Fields.class.getSimpleName();
	static final int INVALID = -1;
	private volatile Config<T, L> config = null;

	/**
	 * Group fields in order. All fields must be named in subclasses, not just the added fields.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public static @interface Fields {
		String[] value();
	}

	/**
	 * Provides actions for a group member.
	 */
	private record Actions<T>(Functions.Operator<T> init, Functions.ObjIntFunction<T, T> flexInit,
		Segments.Update<T> update, Segments.Write<T> write) {

		/**
		 * Initializes a type instance with optional flex array size (structs).
		 */
		private T init(T t, int flexSize) {
			if (flexInit() == null || flexSize < 0) return init().apply(t);
			return flexInit().apply(t, flexSize);
		}

		/**
		 * Updates a type instance from memory.
		 */
		private T update(MemorySegment memory, long offset, T t) {
			if (t == null) t = init(null, -1);
			return update().apply(memory, offset, t);
		}

		/**
		 * Writes a type instance to memory.
		 */
		private void write(MemorySegment memory, long offset, T t) {
			if (t == null) t = init(null, -1);
			write().accept(memory, offset, t);
		}
	}

	/**
	 * Member configuration.
	 */
	public static class Member<T> {
		private final String name;
		private final long offset;
		private final MemoryLayout layout;
		private final VarHandle accessor;
		private final Support<?, ?, ?> support;
		private final Actions<T> actions;
		private final boolean flex;

		static class Builder {
			private final String name;
			private final TypeNode node;
			private final VarHandle accessor;
			private Support<?, ?, ?> support;
			private Actions<?> actions = null;
			private boolean flex = false;
			MemoryLayout layout = null;

			private Builder(Field field) {
				this.name = field.getName();
				node = TypeNode.of(field);
				accessor = Handles.handle(field);
			}

			Member<?> build(long offset) {
				return new Member<>(name, offset, layout, accessor, support, actions, flex);
			}

			private Builder flex(boolean flex) {
				this.flex = flex;
				return this;
			}

			private Builder support(Support<?, ?, ?> support) {
				this.support = support;
				return this;
			}

			private Builder layout(MemoryLayout layout) {
				this.layout = layout;
				return this;
			}

			private <T> Builder actions(Functions.Operator<T> init,
				Functions.ObjIntFunction<T, T> flexInit, Segments.Update<T> update,
				Segments.Write<T> write) {
				actions = new Actions<>(init, flexInit, update, write);
				return this;
			}
		}

		private Member(String name, long offset, MemoryLayout layout, VarHandle accessor,
			Support<?, ?, ?> support, Actions<T> actions, boolean flex) {
			this.name = name;
			this.offset = offset;
			this.layout = layout;
			this.accessor = accessor;
			this.support = support;
			this.actions = actions;
			this.flex = flex;
		}

		/**
		 * Returns the member name.
		 */
		public String name() {
			return name;
		}

		/**
		 * Returns the byte offset without the group layout.
		 */
		public long offset() {
			return offset;
		}

		@Override
		public String toString() {
			return String.format("0x%02x %s %s", offset(), desc(), Layouts.desc(layout));
		}

		String desc() {
			return support.typeDesc() + (flex ? "[]" : "") + ' ' + name();
		}

		T get(Group<?, ?> group) {
			return Handles.get(accessor, group);
		}

		void set(Group<?, ?> group, T value) {
			accessor.set(group, value);
		}

		T val() {
			return actions.init(null, INVALID);
		}

		void init(Group<?, ?> group) {
			init(group, INVALID);
		}

		void init(Group<?, ?> group, int flexSize) {
			var current = get(group);
			var updated = actions.init(current, flexSize);
			if (current != updated) set(group, updated);
		}

		void read(Group<?, ?> group, MemorySegment memory, long offset) {
			var current = get(group);
			var updated = actions.update(memory, offset + offset(), current);
			if (updated != null && updated != current) set(group, updated);
		}

		void write(Group<?, ?> group, MemorySegment memory, long offset) {
			var current = get(group);
			actions.write(memory, offset + offset(), current);
		}

		boolean flex() {
			return flex;
		}

		long flexScale(int count) {
			return ((SequenceLayout) layout).elementLayout().scale(offset(), count);
		}
	}

	/**
	 * Group configuration.
	 */
	public static class Config<T extends Group<T, L>, L extends GroupLayout> {
		private final Class<T> type;
		private final Functions.Supplier<T> constructor;
		private final Map<String, Integer> nameIndex;
		private final L layout;
		private final List<Member<?>> members;

		static abstract class Builder<T extends Group<T, L>, L extends GroupLayout> {
			final Class<T> type;
			final Functions.Supplier<T> constructor;
			final List<MemoryLayout> layouts = Lists.of();
			final List<Member<?>> members = Lists.of();
			T instance = null;

			Builder(Class<T> type) {
				this.type = type;
				constructor = Handles.asSupplier(Handles.constructor(type));
			}

			Group.Config<T, L> build(boolean allowFlex) {
				addMembers(allowFlex);
				return new Group.Config<>(type, constructor, members, layout());
			}

			abstract Member<?> member(Member.Builder member);

			abstract L layout();

			private void addMembers(boolean allowFlex) {
				var classFields = classFields(type);
				var names = fieldNames(type);
				for (int i = 0; i < names.size(); i++) {
					var field = findField(type, classFields, names.get(i));
					var b = new Member.Builder(field);
					populate(b, allowFlex && i == names.size() - 1);
					var member = member(b);
					layouts.add(member.layout);
					members.add(member);
				}
				verifyClassFields(type, classFields);
			}

			private Member.Builder populate(Member.Builder member, boolean flex) {
				if (!member.node.isArray()) return setMember(member);
				var array = Handles.get(member.accessor, instance());
				if (!flex) return setArrayMember(member, array);
				int flexDims = flexDims(member.node, array);
				if (flexDims == INVALID) return setArrayMember(member, array);
				return setFlexMember(member, flexDims);
			}

			private T instance() {
				if (instance == null) instance = constructor.get();
				return instance;
			}
		}

		Config(Class<T> type, Functions.Supplier<T> constructor, List<Member<?>> members,
			L layout) {
			this.type = type;
			this.constructor = constructor;
			this.members = Immutable.wrap(members);
			this.layout = layout;
			nameIndex = nameIndex(members);
		}

		/**
		 * Returns the list of members.
		 */
		public List<Member<?>> members() {
			return members;
		}

		Class<T> type() {
			return type;
		}

		L layout() {
			return layout;
		}

		boolean flex() {
			return Group.flex(members());
		}

		<R> Member<R> member(int index) {
			return Reflect.unchecked(Lists.at(members(), index));
		}

		int indexOf(String name) {
			return nameIndex.getOrDefault(name, INVALID);
		}
	}

	/**
	 * Operational support for group types.
	 */
	public static abstract class Supporter<T extends Group<T, L>, L extends GroupLayout>
		extends Support.Typed<T, L> {
		final Config<T, L> config;

		Supporter(Config<T, L> config, L layout) {
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
			for (var member : config.members())
				member.init(group);
			return group;
		}

		@Override
		public final String toString() {
			return ToString.ofName(Reflect.simple(type()), Layouts.desc(layout()))
				.childrens(config.members()).toString();
		}
	}

	/**
	 * Iterates over each group member. Returns the number of members consumed.
	 */
	public static int forEachMember(Group<?, ?> group,
		Functions.BiConsumer<Member<?>, Object> consumer) {
		if (group == null || consumer == null) return 0;
		var members = group.config().members();
		members.forEach(m -> consumer.accept(m, m.get(group)));
		return members.size();
	}

	protected Group() {}

	@Override
	public int hashCode() {
		return Hasher.deep(values());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		return (obj instanceof Group<?, ?> g) && equals(config(), g);
	}

	@Override
	public String toString() {
		return toString(Caller.Transform.FULL);
	}

	// shared

	String toString(Transformer transformer) {
		var config = config();
		var b = new StringBuilder(config.type().getSimpleName()).append(" {");
		for (var member : config.members())
			b.append(Strings.EOL)
				.append(Text.prefixLines(INDENT, memberString(transformer, member)));
		if (!config.members().isEmpty()) b.append(Strings.EOL);
		return b.append('}').toString();
	}

	Config<T, L> config() {
		var config = this.config;
		if (config == null) {
			config = configFor(Reflect.unchecked(getClass()));
			this.config = config;
		}
		return config;
	}

	abstract Config<T, L> configFor(Class<T> cls);

	String memberString(Transformer transformer, Group.Member<?> member) {
		return member.desc() + " = " + transformer.apply(member.get(this)) + ';';
	}

	T typedThis() {
		return Reflect.unchecked(this);
	}

	int hashCode(Config<T, ?> config) {
		return Hasher.deep(values(config));
	}

	boolean equals(Config<T, ?> config, Group<?, ?> group) {
		return Objects.deepEquals(values(config), group.values());
	}

	Object[] values() {
		return values(config());
	}

	Object[] values(Config<T, ?> config) {
		Object[] values = new Object[config.members().size()];
		for (int i = 0; i < values.length; i++)
			values[i] = config.member(i).get(this);
		return values;
	}

	static boolean flex(List<Member<?>> members) {
		return !members.isEmpty() && Lists.last(members).flex;
	}

	// support

	private static Member.Builder setMember(Member.Builder member) {
		return setMember(Supports.DEF.from(member.node), member);
	}

	private static <U> Member.Builder setMember(Support<U, ?, ?> support, Member.Builder member) {
		return member.layout(support.layout()).support(support).<U>actions(support::init, null,
			support::update, support::write);
	}

	private static Member.Builder setArrayMember(Member.Builder member, Object array) {
		if (array == null) return setMember(member);
		var dims = Dimensions.from(array);
		var support = Supports.DEF.arrayFrom(member.node, dims);
		return setMember(support, member);
	}

	private static <A> Member.Builder setFlexMember(Member.Builder member, int size) {
		Support<?, A, ?> support = Reflect.unchecked(Supports.DEF.from(member.node.component()));
		var layout = MemoryLayout.sequenceLayout(size, support.layout());
		var nul = member.node.context().nul();
		return member.flex(true).layout(layout).support(support).<A>actions(
			t -> support.initArray(t, size), (t, n) -> flexInit(support, t, n),
			(m, o, t) -> support.updateArray(m, o, t, nul),
			(m, o, t) -> support.writeArray(m, o, t, 0, nul));
	}

	private static int flexDims(TypeNode node, Object array) {
		if (node.typed().array().dimensions() != 1) return INVALID;
		int dims = (array != null) ? RawArray.length(array) : node.context().dims().dim(0);
		return dims <= 1 ? dims : INVALID;
	}

	private static <A> A flexInit(Support<?, A, ?> support, A array, int flexSize) {
		if (array == null || RawArray.length(array) != flexSize) return support.initArray(flexSize);
		return support.initArray(array);
	}

	private static Map<String, Integer> nameIndex(List<Member<?>> members) {
		var map = Maps.<String, Integer>of();
		for (int i = 0; i < members.size(); i++)
			map.put(members.get(i).name(), i);
		return Immutable.wrap(map);
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
