package ceri.common.function;

import java.util.function.ObjIntConsumer;

public interface ObjByteConsumer<T> {

	void accept(T t, byte value);
	
	static <T> ObjIntConsumer<T> toInt(ObjByteConsumer<T> objByte) {
		return (t, i) -> objByte.accept(t, (byte) i);
	}

}
