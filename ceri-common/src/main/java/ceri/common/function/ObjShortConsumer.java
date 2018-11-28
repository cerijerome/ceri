package ceri.common.function;

import java.util.function.ObjIntConsumer;

public interface ObjShortConsumer<T> {

	void accept(T t, short value);
	
	static <T> ObjIntConsumer<T> toInt(ObjShortConsumer<T> objShort) {
		return (t, i) -> objShort.accept(t, (short) i);
	}

}
