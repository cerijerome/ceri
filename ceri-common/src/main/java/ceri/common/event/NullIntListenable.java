package ceri.common.event;

import java.util.function.IntConsumer;

public class NullIntListenable implements IntListenable, IntListenable.Indirect {
	private static final NullIntListenable INSTANCE = new NullIntListenable();

	public static NullIntListenable of() {
		return INSTANCE;
	}

	private NullIntListenable() {}

	@Override
	public IntListenable listeners() {
		return this;
	}

	@Override
	public boolean listen(IntConsumer listener) {
		return false;
	}

	@Override
	public boolean unlisten(IntConsumer listener) {
		return false;
	}

}
