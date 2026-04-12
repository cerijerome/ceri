package ceri.ffm.type;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.StructLayout;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Map;
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
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Native;
import ceri.ffm.reflect.Refine;

public class Struct<T extends Struct<T>> {
	private static final String INDENT = "  ";
	private static final int NO_FLEX = -1;
	private static final String FIELDS = Fields.class.getSimpleName();
	private static final Map<Class<?>, Support<?>> cache = Maps.syncWeak();
	private volatile Support<T> support = null;

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
	record Actions<T>(Functions.Operator<T> init, Functions.ObjIntFunction<T, T> flexInit,
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
			void accept(T t, MemorySegment memory, long offset);
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
		public void write(T t, MemorySegment memory, long offset) {
			if (t == null) t = init(null, -1);
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
		public final long offset;
		public final MemoryLayout layout;
		public final VarHandle accessor;
		public final Actions<T> actions;

		public static class Builder {
			private final String name;
			private final Native.Kind.Spec spec;
			private final boolean flex;
			private final Refine.Context context;
			private final VarHandle accessor;
			private Dimensions dims = Dimensions.NONE;
			private MemoryLayout layout = null;
			private Actions<?> actions = null;

			private Builder(Field field, boolean last) {
				this.name = field.getName();
				spec = Native.Kind.spec(Generics.typed(field));
				flex = last && spec.array().dimensions() == 1;
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

			public <T> Builder actions(Functions.Operator<T> init,
				Functions.ObjIntFunction<T, T> flexInit, Actions.Update<T> update,
				Actions.Write<T> write) {
				actions = new Actions<>(init, flexInit, update, write);
				return this;
			}

			public Member<?> build(long offset) {
				return new Member<>(name, spec, dims, offset, layout, accessor, actions);
			}
		}

		private Member(String name, Native.Kind.Spec spec, Dimensions dims, long offset,
			MemoryLayout layout, VarHandle accessor, Actions<T> actions) {
			this.name = name;
			this.spec = spec;
			this.dims = dims;
			this.offset = offset;
			this.layout = layout;
			this.accessor = accessor;
			this.actions = actions;
		}

		private boolean flex() {
			return actions.flexInit() != null;
		}

		private T get(Struct<?> struct) {
			return Handles.get(accessor, struct);
		}

		private void set(Struct<?> struct, T value) {
			accessor.set(struct, value);
		}

		private void init(Struct<?> struct, int flexSize) {
			var current = get(struct);
			var updated = actions.init(current, flexSize);
			if (current != updated) set(struct, updated);
		}

		private void read(Struct<?> struct, MemorySegment memory, long offset) {
			var current = get(struct);
			var updated = actions.update(memory, offset + this.offset, current);
			if (updated != null && updated != current) set(struct, updated);
		}

		private void write(Struct<?> struct, MemorySegment memory, long offset) {
			var current = get(struct);
			actions.write(current, memory, offset + this.offset);
		}

		public String desc() {
			return (flex() ? spec.array().toString() : spec.array().toString(dims)) + ' ' + name;
		}

		@Override
		public final String toString() {
			return String.format("0x%02x %s =%s", offset, desc(), layout);
		}
	}

	/**
	 * Operational support for struct types.
	 */
	public static class Support<T extends Struct<T>>
		extends ceri.ffm.type.Support.Typed<T, StructLayout> {
		private final Class<T> type;
		private final Functions.Supplier<T> constructor;
		private final List<Member<?>> members;
		private final boolean flex;

		private Support(Class<T> type, Functions.Supplier<T> constructor, StructLayout layout,
			List<Member<?>> members) {
			super(layout);
			this.type = type;
			this.constructor = constructor;
			this.members = members;
			flex = !members.isEmpty() && Lists.last(members).spec.array().dimensions() == 1;
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
			return val(NO_FLEX);
		}

		public T val(int flexSize) {
			return init(null, flexSize);
		}

		@Override
		public T init(T struct) {
			return init(struct, NO_FLEX);
		}

		/**
		 * Initializes struct members, with given flexible array size.
		 */
		public T init(T struct, int flexSize) {
			if (struct == null) struct = constructor.get();
			for (var member : members)
				member.init(struct, flexSize);
			return struct;
		}

		/**
		 * Returns the byte size of the given struct, including flexible array member if present.
		 */
		public long size(T struct) {
			if (struct == null) return 0L;
			if (!flex) return layoutSize();
			var last = Lists.last(members);
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
			rawWrite(value, memory, 0L);
			return memory;
		}

		public String desc() {
			var b = new StringBuilder("struct ").append(type().getSimpleName()).append(" {");
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
			var struct = constructor.get();
			rawRead(memory, offset, struct);
			return struct;
		}

		@Override
		void rawRead(MemorySegment memory, long offset, T struct) {
			for (var member : members)
				member.read(struct, memory, offset);
		}

		@Override
		void rawWrite(T struct, MemorySegment memory, long offset) {
			for (var member : members)
				member.write(struct, memory, offset);
		}

		// support

		private long flexSize(int flexSize) {
			if (!flex) return layoutSize();
			var last = Lists.last(members);
			return Math.max(layoutSize(), flexScale(last, flexSize));
		}
	}

	private static class Builder<T extends Struct<T>> {
		private final Class<T> type;
		private final Functions.Supplier<T> constructor;
		private long offset = 0L;
		private long align = 0L;
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

		private StructLayout layout() {
			if (!Layouts.isFlexArray(Lists.last(layouts))) addPadding(layouts, offset, align);
			return Layouts.struct(layouts);
		}

		private void addMembers(Class<?> cls) {
			var classFields = classFields(cls);
			var names = fieldNames(cls);
			int lastIndex = names.size() - 1;
			for (int i = 0; i <= lastIndex; i++) {
				var field = findField(cls, classFields, names.get(i));
				var member = new Member.Builder(field, i == lastIndex);
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

		private void member(ceri.ffm.type.Support<?, ?, ?> support, Member.Builder member) {
			var s = Struct.supportWith(support, member);
			if (member.spec.array().isArray()) arrayMember(s, member);
			else member.layout(support.layout()).actions(s::init, null, s::update, s::write);
		}

		private <A> void arrayMember(ceri.ffm.type.Support<?, A, ?> support,
			Member.Builder member) {
			var dims = arrayDims(constructor.get(), member);
			var nul = member.context.nul();
			var layout = Layouts.array(support.layout(), dims);
			member.layout(layout).dims(dims).actions(t -> deepInit(support, dims, t),
				member.flex ? (t, n) -> deepInitFlex(support, n, t) : null,
				(m, o, t) -> deepRead(support, nul, m, o, t),
				(t, m, o) -> support.deepWrite(t, m, o, nul));
		}

		private static Object deepInit(ceri.ffm.type.Support<?, ?, ?> support, Dimensions dims,
			Object t) {
			if (t == null) return support.deepVal(dims);
			return support.deepInit(t);
		}

		private static Object deepInitFlex(ceri.ffm.type.Support<?, ?, ?> support, int flexSize,
			Object t) {
			if (t == null || RawArray.length(t) != flexSize) return support.arrayVal(flexSize);
			return support.deepInit(t);
		}

		private static Object deepRead(ceri.ffm.type.Support<?, ?, ?> support, boolean nul,
			MemorySegment m, long o, Object t) {
			support.deepRead(m, o, t, nul);
			return t;
		}

		private void add(Member.Builder member) {
			var layout = member.layout;
			offset += addPadding(layouts, offset, layout.byteAlignment());
			layouts.add(layout);
			members.add(member.build(offset));
			align = Math.max(align, layout.byteAlignment());
			offset += layout.byteSize();
		}
	}

	/**
	 * Returns operational support for the type.
	 */
	public static <T extends Struct<T>> Support<T> support(Class<T> cls) {
		return Reflect.unchecked(cache.computeIfAbsent(cls, _ -> supportFor(cls)));
	}

	/**
	 * Creates an instance of the struct, with flexible array member initialized to given count.
	 */
	public static <T extends Struct<T>> T init(Class<T> cls) {
		return init(cls, NO_FLEX);
	}

	/**
	 * Creates an instance of the struct, with flexible array member initialized to given count.
	 */
	public static <T extends Struct<T>> T init(Class<T> cls, int flexSize) {
		if (cls == null) return null;
		return support(cls).init(null, flexSize);
	}

	/**
	 * Allocates memory for a struct with given flexible array member size.
	 */
	public static <T extends Struct<T>> MemorySegment alloc(SegmentAllocator allocator,
		Class<T> cls, int flexSize) {
		if (allocator == null || cls == null) return null;
		return allocator.allocate(flexSize(cls, flexSize));
	}

	/**
	 * Allocates memory for a struct with populated flexible array member.
	 */
	public static <T extends Struct<T>> MemorySegment alloc(SegmentAllocator allocator, T struct) {
		if (allocator == null || struct == null) return null;
		return allocator.allocate(flexSize(struct));
	}

	@Override
	public String toString() {
		var support = support();
		var b = new StringBuilder("struct ").append(support.type().getSimpleName()).append(" {");
		for (var member : support.members) {
			var value = member.desc() + " = " + RawArray.toString(member.get(this)) + ';';
			b.append(Strings.EOL).append(Text.prefixLines(INDENT, value));
		}
		if (!support.members.isEmpty()) b.append(Strings.EOL);
		return b.append('}').toString();
	}
	
	// member deep copy

	Support<T> support() {
		var support = this.support;
		if (support == null) {
			support = support(Reflect.getClass(this));
			this.support = support;
		}
		return support;
	}

	private static <T extends Struct<T>> Support<T> supportFor(Class<T> cls) {
		return new Builder<>(cls).build();
	}

	private static <T> ceri.ffm.type.Support<T, ?, ?>
		supportWith(ceri.ffm.type.Support<?, ?, ?> support, Member.Builder member) {
		return Reflect
			.unchecked(support.with(member.name, member.context.align(), member.context.order()));
	}

	private static <A> Dimensions arrayDims(Struct<?> struct, Member.Builder member) {
		A array = Handles.get(member.accessor, struct);
		if (array != null) return Dimensions.from(array);
		int count = member.spec.array().dimensions();
		var dims = member.context.dims(null);
		if (dims == null) return Dimensions.ofZeros(count);
		if (dims.count() == count) return dims;
		return MultiArray.fix(dims, count, false, 0);
	}

	private static long addPadding(List<MemoryLayout> layouts, long offset, long align) {
		var padding = Layouts.padding(offset, align);
		if (padding != 0) layouts.add(MemoryLayout.paddingLayout(padding));
		return padding;
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

	private static <T extends Struct<T>> long flexSize(T struct) {
		var support = support(Reflect.getClass(struct));
		if (!support.flex) return support.layoutSize();
		var last = Lists.last(support.members);
		int count = RawArray.length(last.accessor.get(struct));
		return Math.max(support.layoutSize(), flexScale(last, count));
	}

	private static <T extends Struct<T>> long flexSize(Class<T> cls, int count) {
		var support = support(cls);
		if (!support.flex) return support.layoutSize();
		var last = Lists.last(support.members);
		return Math.max(support.layoutSize(), flexScale(last, count));
	}

	private static long flexScale(Member<?> flex, int count) {
		return ((SequenceLayout) flex.layout).elementLayout().scale(flex.offset, count);
	}
}
