package ceri.ffm;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.function.Function;
import ceri.common.function.Accessible;

public class Memory {
	private final MemorySegment mem;
	private final Accessible<MemorySegment> accessible;

	public static Memory allocate(int size) {
		return allocate(a -> a.allocate(size));
	}

	@SuppressWarnings("resource")
	public static Memory allocate(Function<? super Arena, MemorySegment> allocator) {
		return new Memory(allocator.apply(Arena.ofAuto()));
	}

	private Memory(MemorySegment mem) {
		this.mem = mem;
		accessible = Accessible.of(mem);
	}

	public Accessible<MemorySegment> mem() {
		return accessible;
	}

	public void fill(int value) {
		mem.fill((byte) value);
	}
}
