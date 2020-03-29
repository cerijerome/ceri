package ceri.common.data;

import static ceri.common.validation.ValidationUtil.*;

@Deprecated
public class Navigator implements Navigable {
	private final int length;
	private int offset = 0;
	private int mark = 0;

	protected Navigator(int length) {
		this.length = length;
	}

	@Override
	public int offset() {
		return offset;
	}

	@Override
	public Navigator offset(int offset) {
		validateRange(offset, 0, length);
		this.offset = offset;
		return this;
	}

	@Override
	public int length() {
		return length;
	}

	@Override
	public Navigator mark() {
		mark = offset;
		return this;
	}

	@Override
	public int marked() {
		return offset() - mark;
	}

	protected int inc(int length) {
		int old = offset;
		offset(offset + length);
		return old;
	}
}
