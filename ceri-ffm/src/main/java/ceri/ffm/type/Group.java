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
import ceri.common.io.Direction;
import ceri.common.reflect.Annotations;
import ceri.common.reflect.Handles;
import ceri.common.reflect.Reflect;
import ceri.common.text.Joiner;
import ceri.common.text.Strings;
import ceri.common.text.Text;
import ceri.common.text.ToString;
import ceri.common.text.Transformer;
import ceri.common.util.Hasher;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Formats;
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
	 * This is required as class field order is undefined in the language specification.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public static @interface Fields {
		String[] value();
	}

	/**
	 * Member configuration.
	 */
	public abstract static class Member<T> {
		private final String name;
		private final long offset;
		private final MemoryLayout layout;
		private final VarHandle accessor;
		private final Support<?, ?, ?, ?> support; // for element if flex array
		private final Direction direction;

		static class Builder {
			private final String name;
			private final TypeNode node;
			private final VarHandle accessor;
			private Support<?, ?, ?, ?> support;
			private Direction direction;
			private Boolean flexNul;
			MemoryLayout layout;

			private Builder(Field field) {
				this.name = field.getName();
				node = TypeNode.of(field);
				accessor = Handles.handle(field);
			}

			Member<?> build(long offset) {
				return flexNul == null ?
					new NonFlex<>(name, offset, layout, accessor, support, direction) :
					new Flex<>(name, offset, layout, accessor, support, direction, flexNul);
			}

			private Builder flex(boolean nul) {
				this.flexNul = nul;
				return this;
			}

			private Builder support(Support<?, ?, ?, ?> support) {
				this.support = support;
				return this;
			}

			private Builder layout(MemoryLayout layout) {
				this.layout = layout;
				return this;
			}

			private Builder direction(Direction direction) {
				this.direction = direction;
				return this;
			}
		}

		private Member(String name, long offset, MemoryLayout layout, VarHandle accessor,
			Support<?, ?, ?, ?> support, Direction direction) {
			this.name = name;
			this.offset = offset;
			this.layout = layout;
			this.accessor = accessor;
			this.support = support;
			this.direction = direction;
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

		/**
		 * Returns true if this member is a flex array.
		 */
		public abstract boolean flex();

		@Override
		public String toString() {
			return String.format("0x%02x %s %s", offset(), desc(), Layouts.string(layout));
		}

		/**
		 * Returns a string descriptor of the type.
		 */
		abstract String desc();

		/**
		 * Returns a type pointer for the memory location. This is the element type for flex arrays.
		 */
		PointerType.Raw pointer(MemorySegment memory, long offset) {
			return support.pointer(Memory.slice(memory, offset + offset()));
		}

		/**
		 * Gets the member value from the group.
		 */
		T get(Group<?, ?> group) {
			return Handles.get(accessor, group);
		}

		/**
		 * Sets the member value in the group.
		 */
		void set(Group<?, ?> group, T value) {
			Handles.set(accessor, group, value);
		}

		/**
		 * Returns a default member value.
		 */
		T def() {
			return initValue(null, INVALID);
		}

		/**
		 * Initialize the group member.
		 */
		void init(Group<?, ?> group) {
			init(group, INVALID);
		}

		/**
		 * Initialize the group member, with flex array size if applicable.
		 */
		void init(Group<?, ?> group, int flexSize) {
			var current = get(group);
			var updated = initValue(current, flexSize);
			if (current != updated) set(group, updated);
		}

		/**
		 * Read the group member value from memory.
		 */
		void read(Group<?, ?> group, MemorySegment memory, long offset) {
			if (!direction.in()) return;
			var current = get(group);
			var updated = updateValue(memory, offset + offset(), current);
			if (updated != null && updated != current) set(group, updated);
		}

		/**
		 * Write the group member value to memory.
		 */
		void write(Group<?, ?> group, MemorySegment memory, long offset) {
			if (!direction.out()) return;
			var current = get(group);
			writeValue(memory, offset + offset(), current);
		}

		/**
		 * Returns the member type support. For flex arrays, this is the element type support.
		 */
		abstract Support<?, ?, ?, ?> support();

		/**
		 * Initializes the member value, with flex array size if appropriate.
		 */
		abstract T initValue(T value, int flexSize);

		/**
		 * Updates and returns the member value from memory.
		 */
		abstract T updateValue(MemorySegment memory, long offset, T value);

		/**
		 * Writes the member value to memory.
		 */
		abstract void writeValue(MemorySegment memory, long offset, T value);
	}

	public static class NonFlex<T> extends Member<T> {

		private NonFlex(String name, long offset, MemoryLayout layout, VarHandle accessor,
			Support<?, ?, ?, ?> support, Direction direction) {
			super(name, offset, layout, accessor, support, direction);
		}

		@Override
		public boolean flex() {
			return false;
		}

		@Override
		String desc() {
			return support().typeDesc() + ' ' + name();
		}

		@Override
		Support<T, ?, ?, ?> support() {
			return Reflect.unchecked(super.support);
		}

		@Override
		T initValue(T value, int flexSize) {
			return support().init(value);
		}

		@Override
		T updateValue(MemorySegment memory, long offset, T value) {
			return support().update(memory, offset, value);
		}

		@Override
		void writeValue(MemorySegment memory, long offset, T value) {
			support().write(memory, offset, value);
		}
	}

	public static class Flex<T> extends Member<T> {
		private final boolean nul;

		private Flex(String name, long offset, MemoryLayout layout, VarHandle accessor,
			Support<?, ?, ?, ?> support, Direction direction, Boolean nul) {
			super(name, offset, layout, accessor, support, direction);
			this.nul = nul;
		}

		@Override
		public boolean flex() {
			return true;
		}

		@Override
		String desc() {
			return support().typeDesc() + "[] " + name();
		}

		long scale(int count) {
			return layout().elementLayout().scale(offset(), count);
		}

		@Override
		Support<?, T, ?, ?> support() {
			return Reflect.unchecked(super.support);
		}

		@Override
		T initValue(T value, int flexSize) {
			if (value == null || flexSize < 0) return support().initArray(value, initCount());
			if (RawArray.length(value) != flexSize) return support().initArray(flexSize);
			return support().initArray(value);
		}

		@Override
		T updateValue(MemorySegment memory, long offset, T value) {
			return support().updateArray(memory, offset, value, nul);
		}

		@Override
		void writeValue(MemorySegment memory, long offset, T value) {
			support().writeArray(memory, offset, value, 0, nul);
		}

		private int initCount() {
			return (int) layout().elementCount();
		}

		private SequenceLayout layout() {
			return Reflect.unchecked(super.layout);
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

			private Member.Builder populate(Member.Builder member, boolean allowFlex) {
				if (!member.node.isArray()) return setMember(member);
				var array = Handles.get(member.accessor, instance());
				if (!allowFlex) return setArrayMember(member, array);
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

		/**
		 * Returns true if the last member is a flex array.
		 */
		public boolean flex() {
			return flexMember() != null;
		}

		Class<T> type() {
			return type;
		}

		L layout() {
			return layout;
		}

		<R> Flex<R> flexMember() {
			var last = Lists.last(members);
			return (last == null || !last.flex()) ? null : Reflect.unchecked(last);
		}

		<R> Member<R> member(int index) {
			return Reflect.unchecked(Lists.at(members(), index));
		}

		<R> Member<R> member(String name) {
			return member(indexOf(name));
		}

		int indexOf(String name) {
			return nameIndex.getOrDefault(name, INVALID);
		}
	}

	/**
	 * Operational support for group types.
	 */
	public static abstract sealed class Supporter<T extends Group<T, L>, L extends GroupLayout>
		extends Support.Typed<T, L> permits Union.Supporter, Struct.Supporter {
		final Config<T, L> config;

		Supporter(Config<T, L> config, L layout) {
			super(layout);
			this.config = config;
		}

		/**
		 * Returns true if the last member is a flex array.
		 */
		public boolean flex() {
			return config.flex();
		}

		@Override
		public boolean mutable() {
			return true;
		}

		@Override
		public Class<T> type() {
			return config.type;
		}

		/**
		 * Returns a typed group member pointer from the memory location of the group.
		 */
		public <P extends PointerType.Raw> P pointer(int index, MemorySegment memory) {
			return pointer(index, memory, 0L);
		}

		/**
		 * Returns a typed group member pointer from the memory location of the group.
		 */
		public <P extends PointerType.Raw> P pointer(int index, MemorySegment memory, long offset) {
			return Group.pointer(config.member(index), memory, offset);
		}

		/**
		 * Returns a typed group member pointer from the group pointer.
		 */
		public <P extends PointerType.Raw> P pointer(int index, Pointer<T> pointer) {
			return pointer(index, PointerType.memory(pointer));
		}

		/**
		 * Returns a typed group member pointer from the memory location of the group.
		 */
		public <P extends PointerType.Raw> P pointer(String name, MemorySegment memory) {
			return pointer(name, memory, 0L);
		}

		/**
		 * Returns a typed group member pointer from the memory location of the group.
		 */
		public <P extends PointerType.Raw> P pointer(String name, MemorySegment memory,
			long offset) {
			return Group.pointer(config.member(name), memory, offset);
		}

		/**
		 * Returns a typed group member pointer from the group pointer.
		 */
		public <P extends PointerType.Raw> P pointer(String name, Pointer<T> pointer) {
			return pointer(name, PointerType.memory(pointer));
		}

		@Override
		public T init(T group) {
			if (group == null) group = def();
			for (var member : config.members())
				member.init(group);
			return group;
		}

		@Override
		public final String toString() {
			return ToString.ofName(Reflect.simple(type()), Layouts.string(layout()))
				.childrens(config.members()).toString();
		}

		@Override
		T def() {
			return config.constructor.get();
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

	Group() {}

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
		return toString(Formats.VERBOSE);
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
		return member.desc() + " = " + transformer.apply(member.get(this)); // + ';';
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

	static Supports supports() {
		return Supports.fixed();
	}

	// support

	private static <P extends PointerType.Raw> P pointer(Member<?> member, MemorySegment memory,
		long offset) {
		return member == null ? null : Reflect.unchecked(member.pointer(memory, offset));
	}

	private static Member.Builder setMember(Member.Builder member) {
		return setMember(supports().from(member.node), member);
	}

	private static <U> Member.Builder setMember(Support<U, ?, ?, ?> support,
		Member.Builder member) {
		var context = member.node.context();
		return member.layout(support.layout()).direction(context.direction()).support(support);
	}

	private static Member.Builder setArrayMember(Member.Builder member, Object array) {
		if (array == null) return setMember(member);
		var dims = Dimensions.from(array);
		var support = supports().arrayFrom(member.node, dims);
		return setMember(support, member);
	}

	private static <A> Member.Builder setFlexMember(Member.Builder member, int size) {
		Support<?, A, ?, ?> support = Reflect.unchecked(supports().from(member.node.component()));
		var context = member.node.context();
		var layout = MemoryLayout.sequenceLayout(size, support.layout());
		return member.layout(layout).direction(context.direction()).support(support)
			.flex(context.nul());
	}

	private static int flexDims(TypeNode node, Object array) {
		if (node.typed().array().dimensions() != 1) return INVALID;
		int dims = (array != null) ? RawArray.length(array) : node.context().dims().dim(0);
		return dims <= 1 ? dims : INVALID;
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
		throw Exceptions.illegalArg("@%s not found on %s: %s", FIELDS, Reflect.name(cls), name);
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
