package ceri.jna.util;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertByte;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.TestUtil.exerciseEquals;
import static ceri.jna.test.JnaTestUtil.assertMemory;
import static ceri.jna.test.JnaTestUtil.assertPointer;
import static ceri.jna.util.JnaUtil.nlong;
import static ceri.jna.util.JnaUtil.unlong;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.Test;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.test.TestUtil;

public class JnaMemoryBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		try (var m = JnaUtil.mallocBytes(1, 2, 3, 4, 5)) {
			JnaMemory t = JnaMemory.of(m, 1, 3);
			JnaMemory eq0 = JnaMemory.of(m, 1, 3);
			JnaMemory eq1 = JnaMemory.of(m.share(1), 0, 3);
			JnaMemory ne0 = JnaMemory.of(m, 0, 3);
			JnaMemory ne1 = JnaMemory.of(m, 1, 4);
			exerciseEquals(t, eq0, eq1);
			assertAllNotEqual(t, ne0, ne1);
		}
	}

	@Test
	public void shouldSetAndGetValues() {
		@SuppressWarnings("resource")
		var m = JnaMemory.of(new Memory(16));
		m.reader(0);
		assertEquals(m.setByte(0, 0x80), 1);
		assertEquals(m.getByte(0), (byte) 0x80);
		assertEquals(m.setBytes(1, 0x80, 0xff, 0x7f), 4);
		assertArray(m.copy(1, 3), 0x80, 0xff, 0x7f);
		assertEquals(m.setNlong(4, nlong(0x8fffffff)), 4 + Native.LONG_SIZE);
		assertEquals(m.getNlong(4), nlong(0x8fffffff));
		assertEquals(m.getUnlong(4), unlong(0x8fffffff));
		assertEquals(m.setNlongMsb(4, nlong(0x8fffffff)), 4 + Native.LONG_SIZE);
		assertEquals(m.getNlongMsb(4), nlong(0x8fffffff));
		assertEquals(m.getUnlongMsb(4), unlong(0x8fffffff));
		assertEquals(m.setNlongLsb(4, nlong(0x8fffffff)), 4 + Native.LONG_SIZE);
		assertEquals(m.getNlongLsb(4), nlong(0x8fffffff));
		assertEquals(m.getUnlongLsb(4), unlong(0x8fffffff));
	}

	@Test
	public void shouldCopyToJnaMemory() {
		@SuppressWarnings("resource")
		var m = JnaMemory.of(JnaUtil.calloc(3));
		assertEquals(m(0x80, 0xff, 0, 0x7f, 0xff).copyTo(1, m), 4);
		assertArray(m.copy(0), 0xff, 0, 0x7f);
	}

	@Test
	public void shouldCopyToMemory() {
		try (var m = JnaUtil.calloc(3)) {
			assertEquals(m(0x80, 0xff, 0, 0x7f, 0xff).copyTo(1, m), 4);
			assertMemory(m, 0, 0xff, 0, 0x7f);
		}
	}

	@Test
	public void shouldCopyToByteReceiver() {
		var b = ByteArray.Mutable.of(3);
		assertEquals(m(0x80, 0xff, 0, 0x7f, 0xff).copyTo(1, b), 4);
		assertArray(b.copy(0), 0xff, 0, 0x7f);
	}

	@Test
	public void shouldWriteToOutputStream() throws IOException {
		var out = new ByteArrayOutputStream();
		assertEquals(m(0x80, 0xff, 0, 0x7f, 0xff).writeTo(1, out), 5);
		assertArray(out.toByteArray(), 0xff, 0, 0x7f, 0xff);
	}

	@Test
	public void shouldFillBytes() {
		@SuppressWarnings("resource")
		var m = JnaMemory.of(JnaUtil.calloc(4));
		assertEquals(m.fill(1, 0x80), 4);
		assertArray(m.copy(0), 0, 0x80, 0x80, 0x80);
	}

	@Test
	public void shouldCopyFromJnaMemory() {
		@SuppressWarnings("resource")
		var m = JnaMemory.of(JnaUtil.calloc(5));
		assertEquals(m.copyFrom(1, m(0x80, 0xff, 0x7f)), 4);
		assertPointer(m.pointer(), 0, 0, 0x80, 0xff, 0x7f, 0);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldCopyFromMemory() {
		var m = JnaMemory.of(JnaUtil.calloc(5));
		assertEquals(m.copyFrom(1, JnaUtil.mallocBytes(0x80, 0xff, 0x7f)), 4);
		assertArray(m.copy(0), 0, 0x80, 0xff, 0x7f, 0);
	}

	@Test
	public void shouldCopyFromByteProvider() {
		@SuppressWarnings("resource")
		var m = JnaMemory.of(JnaUtil.calloc(5));
		assertEquals(m.copyFrom(1, ByteProvider.of(0x80, 0xff, 0x7f)), 4);
		assertArray(m.copy(0), 0, 0x80, 0xff, 0x7f, 0);
	}

	@Test
	public void shouldReadFromInputStream() throws IOException {
		@SuppressWarnings("resource")
		var m = JnaMemory.of(JnaUtil.calloc(5));
		assertEquals(m.readFrom(1, TestUtil.inputStream(0x80, 0xff, 0x7f)), 4);
		assertArray(m.copy(0), 0, 0x80, 0xff, 0x7f, 0);
	}

	@Test
	public void shouldSlice() {
		assertArray(m(0x80, 0, 0xff, 0x7f, 0x80).slice(2).copy(0), 0xff, 0x7f, 0x80);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		try (var m = JnaUtil.mallocBytes(0x80, 0xff, 0x7f, 0, 0x80, 0xff, 0x7f, 0, 0x80)) {
			assertEquals(JnaMemory.of(m).toString(),
				String.format("%s@%x[0x80, 0xff, 0x7f, 0x0, 0x80, 0xff, 0x7f, ...](9)",
					JnaMemory.class.getSimpleName(), PointerUtil.peer(m)));
		}
	}

	@Test
	public void shouldWriteAndReadValues() {
		@SuppressWarnings("resource")
		var m = JnaMemory.of(new Memory(128));
		m.writer(0).writeByte(0x80).writeNlong(nlong(0x80000000L)).writeNlong(unlong(0x80000000L))
			.writeNlongMsb(nlong(0x80000000L)).writeNlongMsb(unlong(0x80000000L))
			.writeNlongLsb(nlong(0x80000000L)).writeNlongLsb(unlong(0x80000000L));
		var r = m.reader(0);
		assertByte(r.readByte(), 0x80);
		assertEquals(r.readNlong(), nlong(0x80000000L));
		assertEquals(r.readUnlong(), unlong(0x80000000L));
		assertEquals(r.readNlongMsb(), nlong(0x80000000L));
		assertEquals(r.readUnlongMsb(), unlong(0x80000000L));
		assertEquals(r.readNlongLsb(), nlong(0x80000000L));
		assertEquals(r.readUnlongLsb(), unlong(0x80000000L));
	}

	@Test
	public void shouldReadIntoMemory() {
		var r = m(0x80, 0xff, 0, 0x7f).reader(0);
		try (Memory m = new Memory(3)) {
			assertEquals(r.readInto(m), 3);
			assertMemory(m, 0, 0x80, 0xff, 0);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldWriteFromMemory() {
		Memory m = JnaUtil.calloc(5);
		JnaMemory.of(m).writer(0).writeFrom(JnaUtil.mallocBytes(0x80, 0xff, 0, 0x7f));
		assertMemory(m, 0, 0x80, 0xff, 0, 0x7f, 0);
	}

	@Test
	public void shouldSliceReader() {
		var r = m(0x80, 0xff, 0, 0x7f).reader(0).skip(1).slice();
		assertArray(r.readBytes(), 0xff, 0, 0x7f);
	}

	@Test
	public void shouldSliceWriter() {
		try (Memory m = JnaUtil.calloc(5)) {
			JnaMemory.of(m).writer(0).skip(1).slice().writeBytes(0x80, 0xff, 0, 0x7f);
			assertMemory(m, 0, 0, 0x80, 0xff, 0, 0x7f);
		}
	}

	@Test
	public void shouldProvidePointer() {
		try (var m = JnaUtil.mallocBytes(1, 2, 3)) {
			assertEquals(JnaMemory.of(m).pointer(), m);
			assertEquals(JnaMemory.of(m, 1, 1).pointer(), m.share(1));
		}
		assertEquals(JnaMemory.of(null).pointer(), null);
	}

	@SuppressWarnings("resource")
	private static JnaMemory m(int... bytes) {
		return JnaMemory.of(JnaUtil.mallocBytes(bytes));
	}

}