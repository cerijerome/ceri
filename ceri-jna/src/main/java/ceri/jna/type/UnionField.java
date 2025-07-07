package ceri.jna.type;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import com.sun.jna.Union;
import ceri.common.function.Excepts.Consumer;

/**
 * Methods to provide typed access to union fields.
 */
public class UnionField<U extends Union, T> {
	public final String name;
	private final Function<U, T> getter;
	private final BiConsumer<U, T> setter;

	public static <U extends Union, T> UnionField<U, T> of(String name, Function<U, T> getter,
		BiConsumer<U, T> setter) {
		return new UnionField<>(name, getter, setter);
	}

	private UnionField(String name, Function<U, T> getter, BiConsumer<U, T> setter) {
		this.name = name;
		this.getter = getter;
		this.setter = setter;
	}

	public T get(U union) {
		if (union == null) return null;
		set(union);
		return Objects.requireNonNull(getter).apply(union);
	}

	public void set(U union, T value) {
		if (union == null) return;
		Objects.requireNonNull(setter).accept(union, value);
	}

	public void set(U union) {
		if (union != null) union.setType(name);
	}

	public T read(U union) {
		return union == null ? null : Struct.readField(union, name);
	}

	public void write(U union, T value) {
		if (union != null) union.writeField(name, value);
	}

	public <E extends Exception> void write(U union, Consumer<E, T> consumer) throws E {
		if (union == null) return;
		consumer.accept(get(union));
		write(union);
	}

	public void write(U union) {
		if (union != null) union.writeField(name);
	}
}
