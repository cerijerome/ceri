package ceri.log.util;

import java.io.IOException;
import java.util.Objects;
import ceri.common.text.ToString;

public class TestCloseable implements AutoCloseable {
	public final int value;

	public static TestCloseable of(String value) {
		return new TestCloseable(Integer.parseInt(value));
	}

	public TestCloseable(int value) {
		this.value = value;
	}

	@Override
	public void close() throws IOException {
		if (value == -1) throw new IOException("" + value);
		if (value == -2) throw new RuntimeException("" + value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof TestCloseable)) return false;
		TestCloseable other = (TestCloseable) obj;
		return value == other.value;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, value);
	}

}