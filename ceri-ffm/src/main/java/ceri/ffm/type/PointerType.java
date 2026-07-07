package ceri.ffm.type;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.MemorySegment;
import java.nio.ByteOrder;
import ceri.common.function.Functions;
import ceri.common.reflect.Handles;
import ceri.common.util.Basics;
import ceri.ffm.core.Layouts;
import ceri.ffm.core.Native;
import ceri.ffm.core.Segments;

/**
 * An opaque base container for a memory segment.
 */
public abstract class PointerType {
	private static final String CONST = "const ";
	private final MemorySegment memory;

	public static class myptr extends PointerType {
		public myptr(MemorySegment memory) {
			super(memory);
		}
	}
	
	public static void main(String[] args) {
		var p = new myptr(Segments.auto().allocate(13));
		System.out.println(p);
	}
	
	/**
	 * Typed pointer constructor.
	 */
	interface Create<P extends PointerType, S extends Support<?, ?, ?>> {
		/**
		 * Creates a new pointer instance.
		 */
		P apply(MemorySegment memory, S support, boolean constant);
	}

	/**
	 * Operational support for pointers.
	 */
	public static class Supporter<P extends PointerType> extends Support.Typed<P, AddressLayout> {
		private final Config<P, ?> config;

		record Config<P extends PointerType, S extends Support<?, ?, ?>>(Class<P> type,
			Native.Kind kind, S support, Create<P, S> create, boolean constant) {

			Config<P, S> asConst() {
				return constant() ? this :
					new Config<>(type(), kind(), support(), create(), constant);
			}

			P create(MemorySegment memory) {
				return create().apply(memory, support(), constant());
			}
		}

		static <P extends PointerType, S extends Support<?, ?, ?>> Supporter<P> of(Class<P> type,
			Native.Kind kind, S support, Create<P, S> create, boolean constant) {
			var config = new Config<>(type, kind, support, create, constant);
			return new Supporter<>(config, Layouts.POINTER);
		}

		Supporter(Config<P, ?> config, AddressLayout layout) {
			super(layout);
			this.config = config;
		}

		/**
		 * Creates an instance for the memory segment.
		 */
		public P of(MemorySegment memory) {
			if (memory == null) return null;
			return config.create(memory);
		}

		/**
		 * Returns support for constant pointer data. No change if already constant.
		 */
		public Supporter<P> asConst() {
			var config = this.config.asConst();
			return config == this.config ? this : new Supporter<>(config, layout());
		}

		@Override
		public Native.Kind kind() {
			return config.kind();
		}

		@Override
		public Class<P> type() {
			return config.type();
		}

		@Override
		public String typeDesc() {
			return (config.constant() && !config.support().isVoid() ? CONST : "")
				+ Native.wrap(config.support().typeDesc()) + '*';
		}

		@Override
		public P val() {
			return config.create(MemorySegment.NULL);
		}

		@Override
		public Supporter<P> align(long align) {
			var layout = Layouts.align(layout(), align);
			return layout == layout() ? this : new Supporter<>(config, layout);
		}

		@Override
		public Supporter<P> order(ByteOrder order) {
			var layout = Layouts.order(layout(), order);
			return layout == layout() ? this : new Supporter<>(config, layout);
		}

		@Override
		protected P rawGet(MemorySegment memory, long offset, long length) {
			return config.create(memory.get(layout(), offset));
		}

		@Override
		protected void rawWrite(MemorySegment memory, long offset, long length, P value) {
			memory.set(layout(), offset, value.memory());
		}
	}

	/**
	 * Creates a support instance for the type.
	 */
	static <P extends PointerType> Supporter<P> supportFor(Class<P> cls) {
		var create = constructorFor(cls);
		var config = new Supporter.Config<>(cls, Native.Kind.pointerType, Support.VOID,
			(m, _, _) -> create.apply(m), true);
		return new Supporter<>(config, Layouts.POINTER);
	}

	protected PointerType(MemorySegment memory) {
		this.memory = Basics.def(memory, MemorySegment.NULL);
	}

	/**
	 * Returns true if the contained memory segment has native address 0.
	 */
	public boolean isNull() {
		return Segments.isNull(memory);
	}

	/**
	 * Returns the pointer address.
	 */
	public long address() {
		return Segments.address(memory());
	}

	/**
	 * Provides a simple descriptor for the pointer type.
	 */
	public String desc() {
		return typeString() + "*";
	}

	@Override
	public String toString() {
		return String.format("%s(@%x)", desc(), address());
	}

	// shared
	
	/**
	 * Returns the memory segment of the pointer.
	 */
	MemorySegment memory() {
		return memory;
	}

	/**
	 * Provides a string representation of the pointer type.
	 */
	String typeString() {
		return getClass().getSimpleName();
	}
	
	// support
	
	private static <P extends PointerType> Functions.Function<MemorySegment, P>
		constructorFor(Class<P> cls) {
		return Handles.asFunction(Handles.constructor(cls, MemorySegment.class));
	}
}
