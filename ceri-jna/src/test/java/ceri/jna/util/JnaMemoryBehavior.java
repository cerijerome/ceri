package ceri.jna.util;

import static ceri.common.test.Assert.assertAllNotEqual;
import static ceri.common.test.Assert.assertArray;
import static ceri.common.test.Assert.assertEquals;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import com.sun.jna.Memory;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.test.TestUtil;
import ceri.jna.test.JnaTestUtil;
import ceri.jna.type.CLong;
import ceri.jna.type.CUlong;

public class JnaMemoryBehavior {
	private Memory m0;
	private JnaMemory m;
	private JnaMemory.Reader r;
	private JnaMemory.Writer w;

	@After
	public void after() {
		m0 = TestUtil.close(m0);
		r = null;
		w = null;
		m = null;
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		m0 = JnaUtil.mallocBytes(1, 2, 3, 4, 5);
		var t = JnaMemory.of(m0, 1, 3);
		var eq0 = JnaMemory.of(m0, 1, 3);
		var eq1 = JnaMemory.of(m0.share(1), 0, 3);
		var ne0 = JnaMemory.of(m0, 0, 3);
		var ne1 = JnaMemory.of(m0, 1, 4);
		TestUtil.exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1);
	}

	@Test
	public void shouldSetBytes() {
		init(4);
		assertEquals(m.setByte(0, 0x80), 1);
		assertEquals(m.getByte(0), (byte) 0x80);
		assertEquals(m.setBytes(1, 0x80, 0xff, 0x7f), 4);
		assertArray(m.copy(1, 3), 0x80, 0xff, 0x7f);
	}

	@Test
	public void shouldCopyToJnaMemory() {
		init(3);
		assertEquals(m(0x80, 0xff, 0, 0x7f, 0xff).copyTo(1, m), 4);
		assertArray(m.copy(0), 0xff, 0, 0x7f);
	}

	@Test
	public void shouldCopyToMemory() {
		init(3);
		assertEquals(m(0x80, 0xff, 0, 0x7f, 0xff).copyTo(1, m0), 4);
		JnaTestUtil.assertMemory(m0, 0, 0xff, 0, 0x7f);
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
		init(4);
		assertEquals(m.fill(1, 0x80), 4);
		assertArray(m.copy(0), 0, 0x80, 0x80, 0x80);
	}

	@Test
	public void shouldCopyFromJnaMemory() {
		init(5);
		assertEquals(m.copyFrom(1, m(0x80, 0xff, 0x7f)), 4);
		JnaTestUtil.assertPointer(m.pointer(), 0, 0, 0x80, 0xff, 0x7f, 0);
	}

	@Test
	public void shouldCopyFromMemory() {
		init(5);
		assertEquals(m.copyFrom(1, JnaTestUtil.mem(0x80, 0xff, 0x7f).m), 4);
		assertArray(m.copy(0), 0, 0x80, 0xff, 0x7f, 0);
	}

	@Test
	public void shouldCopyFromByteProvider() {
		init(5);
		assertEquals(m.copyFrom(1, ByteProvider.of(0x80, 0xff, 0x7f)), 4);
		assertArray(m.copy(0), 0, 0x80, 0xff, 0x7f, 0);
	}

	@Test
	public void shouldReadFromInputStream() throws IOException {
		init(5);
		assertEquals(m.readFrom(1, TestUtil.inputStream(0x80, 0xff, 0x7f)), 4);
		assertArray(m.copy(0), 0, 0x80, 0xff, 0x7f, 0);
	}

	@Test
	public void shouldSlice() {
		assertArray(m(0x80, 0, 0xff, 0x7f, 0x80).slice(2).copy(0), 0xff, 0x7f, 0x80);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		m0 = JnaUtil.mallocBytes(0x80, 0xff, 0x7f, 0, 0x80, 0xff, 0x7f, 0, 0x80);
		assertEquals(JnaMemory.of(m0).toString(),
			String.format("%s@%x[0x80,0xff,0x7f,0x00,0x80,0xff,0x7f,...](9)",
				JnaMemory.class.getSimpleName(), PointerUtil.peer(m0)));
	}

	@Test
	public void shouldSetCLong() {
		init(24);
		m.set(0, new CLong(-111));
		m.setMsb(8, new CLong(222));
		m.setLsb(16, new CLong(-333));
		assertEquals(m.getCLong(0), new CLong(-111));
		assertEquals(m.getCLongMsb(8), new CLong(222));
		assertEquals(m.getCLongLsb(16), new CLong(-333));
	}

	@Test
	public void shouldSetCUlong() {
		init(24);
		m.set(0, new CUlong(-111));
		m.setMsb(8, new CUlong(222));
		m.setLsb(16, new CUlong(-333));
		assertEquals(m.getCUlong(0), new CUlong(-111));
		assertEquals(m.getCUlongMsb(8), new CUlong(222));
		assertEquals(m.getCUlongLsb(16), new CUlong(-333));
	}

	@Test
	public void shouldSetIntType() {
		init(24);
		m.set(0, new CLong(-111));
		m.setMsb(8, new CLong(222));
		m.setLsb(16, new CLong(-333));
		assertEquals(m.getFrom(0, new CLong()), new CLong(-111));
		assertEquals(m.getFromMsb(8, new CLong()), new CLong(222));
		assertEquals(m.getFromLsb(16, new CLong()), new CLong(-333));
	}

	@Test
	public void shouldWriteCLong() {
		initRw(24);
		w.write(new CLong(-111)).writeMsb(new CLong(222)).writeLsb(new CLong(-333));
		assertEquals(r.readCLong(), new CLong(-111));
		assertEquals(r.readCLongMsb(), new CLong(222));
		assertEquals(r.readCLongLsb(), new CLong(-333));
	}

	@Test
	public void shouldWriteCUlong() {
		initRw(24);
		w.write(new CUlong(-111)).writeMsb(new CUlong(222)).writeLsb(new CUlong(-333));
		assertEquals(r.readCUlong(), new CUlong(-111));
		assertEquals(r.readCUlongMsb(), new CUlong(222));
		assertEquals(r.readCUlongLsb(), new CUlong(-333));
	}

	@Test
	public void shouldWriteIntType() {
		initRw(24);
		w.write(new CLong(-111)).writeMsb(new CUlong(222)).writeLsb(new CLong(-333));
		assertEquals(r.readInto(new CLong()), new CLong(-111));
		assertEquals(r.readIntoMsb(new CUlong()), new CUlong(222));
		assertEquals(r.readIntoLsb(new CLong()), new CLong(-333));
	}

	@Test
	public void shouldReadIntoMemory() {
		var r = m(0x80, 0xff, 0, 0x7f).reader(0);
		try (Memory m = new Memory(3)) {
			assertEquals(r.readInto(m), 3);
			JnaTestUtil.assertMemory(m, 0, 0x80, 0xff, 0);
		}
	}

	@Test
	public void shouldWriteFromMemory() {
		m0 = JnaUtil.calloc(5);
		JnaMemory.of(m0).writer(0).writeFrom(JnaTestUtil.mem(0x80, 0xff, 0, 0x7f).m);
		JnaTestUtil.assertMemory(m0, 0, 0x80, 0xff, 0, 0x7f, 0);
	}

	@Test
	public void shouldSliceReader() {
		var r = m(0x80, 0xff, 0, 0x7f).reader(0).skip(1).slice();
		assertArray(r.readBytes(), 0xff, 0, 0x7f);
	}

	@Test
	public void shouldSliceWriter() {
		m0 = JnaUtil.calloc(5);
		JnaMemory.of(m0).writer(0).skip(1).slice().writeBytes(0x80, 0xff, 0, 0x7f);
		JnaTestUtil.assertMemory(m0, 0, 0, 0x80, 0xff, 0, 0x7f);
	}

	@Test
	public void shouldProvidePointer() {
		m0 = JnaUtil.mallocBytes(1, 2, 3);
		assertEquals(JnaMemory.of(m0).pointer(), m0);
		assertEquals(JnaMemory.of(m0, 1, 1).pointer(), m0.share(1));
		assertEquals(JnaMemory.of(null).pointer(), null);
	}

	private void init(int size) {
		m0 = JnaUtil.calloc(size);
		m = JnaMemory.of(m0);
	}

	private void initRw(int size) {
		init(size);
		r = m.reader(0);
		w = m.writer(0);
	}

	private static JnaMemory m(int... bytes) {
		return JnaMemory.of(JnaTestUtil.mem(bytes).m);
	}
}
