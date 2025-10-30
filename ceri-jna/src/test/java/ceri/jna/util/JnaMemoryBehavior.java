package ceri.jna.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import com.sun.jna.Memory;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.test.Assert;
import ceri.common.test.Testing;
import ceri.jna.test.JnaAssert;
import ceri.jna.test.JnaTesting;
import ceri.jna.type.CLong;
import ceri.jna.type.CUlong;

public class JnaMemoryBehavior {
	private Memory m0;
	private JnaMemory m;
	private JnaMemory.Reader r;
	private JnaMemory.Writer w;

	@After
	public void after() {
		m0 = Testing.close(m0);
		r = null;
		w = null;
		m = null;
	}

	@Test
	public void shouldNotBreachEqualsContract() {
		m0 = Jna.mallocBytes(1, 2, 3, 4, 5);
		var t = JnaMemory.of(m0, 1, 3);
		var eq0 = JnaMemory.of(m0, 1, 3);
		var eq1 = JnaMemory.of(m0.share(1), 0, 3);
		var ne0 = JnaMemory.of(m0, 0, 3);
		var ne1 = JnaMemory.of(m0, 1, 4);
		Testing.exerciseEquals(t, eq0, eq1);
		Assert.notEqualAll(t, ne0, ne1);
	}

	@Test
	public void shouldSetBytes() {
		init(4);
		Assert.equal(m.setByte(0, 0x80), 1);
		Assert.equal(m.getByte(0), (byte) 0x80);
		Assert.equal(m.setBytes(1, 0x80, 0xff, 0x7f), 4);
		Assert.array(m.copy(1, 3), 0x80, 0xff, 0x7f);
	}

	@Test
	public void shouldCopyToJnaMemory() {
		init(3);
		Assert.equal(m(0x80, 0xff, 0, 0x7f, 0xff).copyTo(1, m), 4);
		Assert.array(m.copy(0), 0xff, 0, 0x7f);
	}

	@Test
	public void shouldCopyToMemory() {
		init(3);
		Assert.equal(m(0x80, 0xff, 0, 0x7f, 0xff).copyTo(1, m0), 4);
		JnaAssert.memory(m0, 0, 0xff, 0, 0x7f);
	}

	@Test
	public void shouldCopyToByteReceiver() {
		var b = ByteArray.Mutable.of(3);
		Assert.equal(m(0x80, 0xff, 0, 0x7f, 0xff).copyTo(1, b), 4);
		Assert.array(b.copy(0), 0xff, 0, 0x7f);
	}

	@Test
	public void shouldWriteToOutputStream() throws IOException {
		var out = new ByteArrayOutputStream();
		Assert.equal(m(0x80, 0xff, 0, 0x7f, 0xff).writeTo(1, out), 5);
		Assert.array(out.toByteArray(), 0xff, 0, 0x7f, 0xff);
	}

	@Test
	public void shouldFillBytes() {
		init(4);
		Assert.equal(m.fill(1, 0x80), 4);
		Assert.array(m.copy(0), 0, 0x80, 0x80, 0x80);
	}

	@Test
	public void shouldCopyFromJnaMemory() {
		init(5);
		Assert.equal(m.copyFrom(1, m(0x80, 0xff, 0x7f)), 4);
		JnaAssert.pointer(m.pointer(), 0, 0, 0x80, 0xff, 0x7f, 0);
	}

	@Test
	public void shouldCopyFromMemory() {
		init(5);
		Assert.equal(m.copyFrom(1, JnaTesting.mem(0x80, 0xff, 0x7f).m), 4);
		Assert.array(m.copy(0), 0, 0x80, 0xff, 0x7f, 0);
	}

	@Test
	public void shouldCopyFromByteProvider() {
		init(5);
		Assert.equal(m.copyFrom(1, ByteProvider.of(0x80, 0xff, 0x7f)), 4);
		Assert.array(m.copy(0), 0, 0x80, 0xff, 0x7f, 0);
	}

	@Test
	public void shouldReadFromInputStream() throws IOException {
		init(5);
		Assert.equal(m.readFrom(1, Testing.inputStream(0x80, 0xff, 0x7f)), 4);
		Assert.array(m.copy(0), 0, 0x80, 0xff, 0x7f, 0);
	}

	@Test
	public void shouldSlice() {
		Assert.array(m(0x80, 0, 0xff, 0x7f, 0x80).slice(2).copy(0), 0xff, 0x7f, 0x80);
	}

	@Test
	public void shouldProvideStringRepresentation() {
		m0 = Jna.mallocBytes(0x80, 0xff, 0x7f, 0, 0x80, 0xff, 0x7f, 0, 0x80);
		Assert.equal(JnaMemory.of(m0).toString(),
			String.format("%s@%x[0x80,0xff,0x7f,0x00,0x80,0xff,0x7f,...](9)",
				JnaMemory.class.getSimpleName(), Pointers.peer(m0)));
	}

	@Test
	public void shouldSetCLong() {
		init(24);
		m.set(0, new CLong(-111));
		m.setMsb(8, new CLong(222));
		m.setLsb(16, new CLong(-333));
		Assert.equal(m.getCLong(0), new CLong(-111));
		Assert.equal(m.getCLongMsb(8), new CLong(222));
		Assert.equal(m.getCLongLsb(16), new CLong(-333));
	}

	@Test
	public void shouldSetCUlong() {
		init(24);
		m.set(0, new CUlong(-111));
		m.setMsb(8, new CUlong(222));
		m.setLsb(16, new CUlong(-333));
		Assert.equal(m.getCUlong(0), new CUlong(-111));
		Assert.equal(m.getCUlongMsb(8), new CUlong(222));
		Assert.equal(m.getCUlongLsb(16), new CUlong(-333));
	}

	@Test
	public void shouldSetIntType() {
		init(24);
		m.set(0, new CLong(-111));
		m.setMsb(8, new CLong(222));
		m.setLsb(16, new CLong(-333));
		Assert.equal(m.getFrom(0, new CLong()), new CLong(-111));
		Assert.equal(m.getFromMsb(8, new CLong()), new CLong(222));
		Assert.equal(m.getFromLsb(16, new CLong()), new CLong(-333));
	}

	@Test
	public void shouldWriteCLong() {
		initRw(24);
		w.write(new CLong(-111)).writeMsb(new CLong(222)).writeLsb(new CLong(-333));
		Assert.equal(r.readCLong(), new CLong(-111));
		Assert.equal(r.readCLongMsb(), new CLong(222));
		Assert.equal(r.readCLongLsb(), new CLong(-333));
	}

	@Test
	public void shouldWriteCUlong() {
		initRw(24);
		w.write(new CUlong(-111)).writeMsb(new CUlong(222)).writeLsb(new CUlong(-333));
		Assert.equal(r.readCUlong(), new CUlong(-111));
		Assert.equal(r.readCUlongMsb(), new CUlong(222));
		Assert.equal(r.readCUlongLsb(), new CUlong(-333));
	}

	@Test
	public void shouldWriteIntType() {
		initRw(24);
		w.write(new CLong(-111)).writeMsb(new CUlong(222)).writeLsb(new CLong(-333));
		Assert.equal(r.readInto(new CLong()), new CLong(-111));
		Assert.equal(r.readIntoMsb(new CUlong()), new CUlong(222));
		Assert.equal(r.readIntoLsb(new CLong()), new CLong(-333));
	}

	@Test
	public void shouldReadIntoMemory() {
		var r = m(0x80, 0xff, 0, 0x7f).reader(0);
		try (Memory m = new Memory(3)) {
			Assert.equal(r.readInto(m), 3);
			JnaAssert.memory(m, 0, 0x80, 0xff, 0);
		}
	}

	@Test
	public void shouldWriteFromMemory() {
		m0 = Jna.calloc(5);
		JnaMemory.of(m0).writer(0).writeFrom(JnaTesting.mem(0x80, 0xff, 0, 0x7f).m);
		JnaAssert.memory(m0, 0, 0x80, 0xff, 0, 0x7f, 0);
	}

	@Test
	public void shouldSliceReader() {
		var r = m(0x80, 0xff, 0, 0x7f).reader(0).skip(1).slice();
		Assert.array(r.readBytes(), 0xff, 0, 0x7f);
	}

	@Test
	public void shouldSliceWriter() {
		m0 = Jna.calloc(5);
		JnaMemory.of(m0).writer(0).skip(1).slice().writeBytes(0x80, 0xff, 0, 0x7f);
		JnaAssert.memory(m0, 0, 0, 0x80, 0xff, 0, 0x7f);
	}

	@Test
	public void shouldProvidePointer() {
		m0 = Jna.mallocBytes(1, 2, 3);
		Assert.equal(JnaMemory.of(m0).pointer(), m0);
		Assert.equal(JnaMemory.of(m0, 1, 1).pointer(), m0.share(1));
		Assert.equal(JnaMemory.of(null).pointer(), null);
	}

	private void init(int size) {
		m0 = Jna.calloc(size);
		m = JnaMemory.of(m0);
	}

	private void initRw(int size) {
		init(size);
		r = m.reader(0);
		w = m.writer(0);
	}

	private static JnaMemory m(int... bytes) {
		return JnaMemory.of(JnaTesting.mem(bytes).m);
	}
}
