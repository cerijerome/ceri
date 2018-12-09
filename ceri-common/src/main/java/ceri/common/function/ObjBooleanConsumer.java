package ceri.common.function;

import java.util.function.ObjIntConsumer;

public interface ObjBooleanConsumer<T> {

	void accept(T t, boolean value);
	
	static <T> ObjIntConsumer<T> toInt(ObjBooleanConsumer<T> objByte) {
		return (t, i) -> objByte.accept(t, i != 0);
	}

}
