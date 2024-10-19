package ceri.common.text;

import java.io.IOException;

/**
 * Represent a char sub-sequence.
 */
public record SubSequence(CharSequence source, int start, int end) {

	public boolean isEmpty() {
		return StringUtil.empty(source()) || end() <= start() || start() < 0
			|| end() > source().length();
	}

	public boolean appendTo(Appendable b) throws IOException {
		if (isEmpty()) return false;
		b.append(source(), start(), end());
		return true;
	}

	public boolean append(StringBuilder b) {
		if (isEmpty()) return false;
		b.append(source(), start(), end());
		return true;
	}

	public CharSequence get() {
		if (isEmpty()) return "";
		return source.subSequence(start, end);
	}

	public String string() {
		return String.valueOf(get());
	}
}
