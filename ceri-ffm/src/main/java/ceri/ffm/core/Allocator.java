package ceri.ffm.core;

import java.lang.foreign.Arena;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;

/**
 * Lazy supplier for an arena.
 */
public class Allocator implements Functions.Closeable {
	public static final Allocator GLOBAL = new Allocator(null, null, Arena.global(), Scope.global);
	private final Functions.Supplier<Arena> supplier;
	private final Functions.Consumer<Arena> closer;
	public final Scope scope;
	private Arena arena = null; // does global instance require volatile?

	public enum Scope {
		global,
		auto,
		shared,
		confined,
	}

	public static Allocator of(Scope scope) {
		return switch (scope) {
			case confined -> confined();
			case shared -> shared();
			case global -> GLOBAL;
			case null -> auto();
			default -> auto();
		};
	}

	public static Allocator auto() {
		return new Allocator(Arena::ofAuto, null, null, Scope.auto);
	}

	public static Allocator shared() {
		return new Allocator(Arena::ofShared, Arena::close, null, Scope.shared);
	}

	public static Allocator confined() {
		return new Allocator(Arena::ofConfined, Arena::close, null, Scope.confined);
	}

	private Allocator(Functions.Supplier<Arena> supplier, Functions.Consumer<Arena> closer,
		Arena arena, Scope scope) {
		this.supplier = supplier;
		this.closer = closer;
		this.scope = scope;
		this.arena = arena;
	}

	public Arena get() {
		if (arena == null) arena = supplier.get();
		return arena;
	}

	@SuppressWarnings("resource")
	public <E extends Exception> void accept(Excepts.Consumer<E, ? super Arena> consumer) throws E {
		if (consumer != null) consumer.accept(get());
	}

	@SuppressWarnings("resource")
	public <E extends Exception, R> R apply(Excepts.Function<E, ? super Arena, R> function)
		throws E {
		if (function == null) return null;
		return function.apply(get());
	}

	@Override
	public void close() {
		if (closer != null && arena != null) closer.accept(arena);
	}
}
