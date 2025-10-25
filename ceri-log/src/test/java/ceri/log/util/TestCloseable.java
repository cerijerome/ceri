package ceri.log.util;

import static ceri.common.test.Assert.assertEquals;
import java.io.IOException;
import java.util.Objects;
import ceri.common.text.ToString;

public class TestCloseable implements AutoCloseable {
	public final int value;
	private volatile boolean closed = false;

	public static TestCloseable of(String value) {
		return new TestCloseable(Integer.parseInt(value));
	}

	public TestCloseable(int value) {
		this.value = value;
	}

	public boolean closed() {
		return closed;
	}

	public void assertClosed(boolean closed) {
		assertEquals(closed(), closed);
	}

	@Override
	public void close() throws IOException {
		closed = true;
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
		return (obj instanceof TestCloseable other) && value == other.value;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, value);
	}

}