package ceri.common.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import ceri.common.data.TypeTranscoder.Remainder;

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
	public final FieldTranscoder<T> remove(T...ts) {
		return ts.length == 0 ? this : remove(Arrays.asList(ts));
	}
	
	public FieldTranscoder<T> remove(Collection<T> ts) {
		if (!ts.isEmpty()) accessor().remove(xcoder.encode(ts));
		return this;
	}

	@SafeVarargs
	public final FieldTranscoder<T> add(T... ts) {
		return ts.length == 0 ? this : add(Arrays.asList(ts));
	}

	public FieldTranscoder<T> add(Collection<T> ts) {
		if (!ts.isEmpty()) accessor().add(xcoder.encode(ts));
		return this;
	}

	@SafeVarargs
	public final FieldTranscoder<T> set(T... ts) {
		return set(Arrays.asList(ts));
	}

	public FieldTranscoder<T> set(Collection<T> ts) {
		accessor().set(xcoder.encode(ts));
		return this;
	}

	public FieldTranscoder<T> set(Remainder<T> rem) {
		accessor().set(xcoder.encode(rem));
		return this;
	}

	public T get() {
		return xcoder.decode(accessor().get());
	}

	public Set<T> getAll() {
		return xcoder.decodeAll(accessor().get());
	}

	public Remainder<T> getWithRemainder() {
		return xcoder.decodeWithRemainder(accessor().get());
	}

	public boolean isValid() {
		return xcoder.isValid(accessor().get());
	}

}
