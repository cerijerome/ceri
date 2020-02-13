package ceri.common.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

/**
 * Combines an integer field accessor with a type transcoder to allow callers to get/set
 * types on a field that is a plain integer. Typically instantiated from TypeTranscoder.field. 
 */
public class FieldTranscoder<T> {
	private final IntAccessor accessor;
	private final TypeTranscoder<T> xcoder;

	public static <T> FieldTranscoder<T> of(IntAccessor accessor,
		TypeTranscoder<T> xcoder) {
		return new FieldTranscoder<>(accessor, xcoder);
	}

	FieldTranscoder(IntAccessor accessor, TypeTranscoder<T> xcoder) {
		this.accessor = accessor;
		this.xcoder = xcoder;
	}

	public IntAccessor accessor() {
		return accessor;
	}

	@SafeVarargs
	public final void add(T... ts) {
		add(Arrays.asList(ts));
	}

	public void add(Collection<T> ts) {
		accessor().set(accessor.get() | xcoder.encode(ts));
	}

	@SafeVarargs
	public final void set(T... ts) {
		set(Arrays.asList(ts));
	}

	public void set(Collection<T> ts) {
		accessor().set(xcoder.encode(ts));
	}

	public T get() {
		return xcoder.decode(accessor().get());
	}

	public Set<T> getAll() {
		return xcoder.decodeAll(accessor().get());
	}

	public boolean isValid() {
		return xcoder.isValid(accessor().get());
	}

}
