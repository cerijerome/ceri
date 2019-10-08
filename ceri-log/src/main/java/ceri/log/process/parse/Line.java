package ceri.log.process.parse;

import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Line implements Value {
	public final String text;

	public Line(String text) {
		this.text = text;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(text);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Line)) return false;
		Line other = (Line) obj;
		if (!EqualsUtil.equals(text, other.text)) return false;
		return true;
	}

	@Override
	public String toString() {
		return text;
	}

}
