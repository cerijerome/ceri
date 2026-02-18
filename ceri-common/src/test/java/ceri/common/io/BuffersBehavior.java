package ceri.common.io;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import org.junit.Test;
import ceri.common.array.DynamicArray;
import ceri.common.data.Bytes;
import ceri.common.test.Assert;

public class BuffersBehavior {

	@Test
	public void shouldCreateCharBuffer() {
		Assert.equal(Buffers.CHAR.of((char[]) null), null);
		Assert.equal(Buffers.CHAR.of((String) null), null);
		Assert.buffer(Buffers.CHAR.of('a', '\n', '\0'), "a\n\0");
		Assert.buffer(Buffers.CHAR.of("a\n\0"), "a\n\0");
	}

	@Test
	public void shouldGetStringFromCharBuffer() {
		Assert.string(Buffers.CHAR.getString(null), null);
		var b = Buffers.CHAR.of("abc\0");
		Assert.string(Buffers.CHAR.getStringAt(b, 1), "bc\0");
		assertBounds(b, 4, 4);
	}

	@Test
	public void shouldPutCharsInBuffer() {
		var b = Buffers.CHAR.of("abcde".toCharArray());
		Assert.equal(Buffers.CHAR.putAt(b, 1, '\n', '\0'), 2);
		Assert.equal(Buffers.CHAR.put(b, 'D'), 1);
		Assert.buffer(b.rewind(), "a\n\0De");
	}

	@Test
	public void shouldCopyCharSequenceToBuffer() {
		var b = Buffers.CHAR.of("abcde".toCharArray());
		Assert.equal(Buffers.CHAR.copyAt("\n\0", b, 1), 2);
		Assert.equal(Buffers.CHAR.copy("D", b), 1);
		Assert.buffer(b.rewind(), "a\n\0De");
	}

	@Test
	public void shouldPutBytesInBuffer() {
		var b = Buffers.BYTE.of(-1, 1, 0, -1, 1);
		Assert.equal(Buffers.BYTE.putAt(b, 1, -1, 1), 2);
		Assert.equal(Buffers.BYTE.put(b, 0), 1);
		Assert.buffer(b.rewind(), -1, -1, 1, 0, 1);
	}

	@Test
	public void shouldPutShortsInBuffer() {
		var b = Buffers.SHORT.of(-1, 1, 0, -1, 1);
		Assert.equal(Buffers.SHORT.putAt(b, 1, -1, 1), 2);
		Assert.equal(Buffers.SHORT.put(b, 0), 1);
		Assert.buffer(b.rewind(), -1, -1, 1, 0, 1);
	}

	@Test
	public void shouldPutIntsInBuffer() {
		var b = Buffers.INT.of(-1, 1, 0, -1, 1);
		Assert.equal(Buffers.INT.putAt(b, 1, -1, 1), 2);
		Assert.equal(Buffers.INT.put(b, 0), 1);
		Assert.buffer(b.rewind(), -1, -1, 1, 0, 1);
	}

	@Test
	public void shouldPutLongsInBuffer() {
		var b = Buffers.LONG.of(-1, 1, 0, -1, 1);
		Assert.equal(Buffers.LONG.putAt(b, 1, -1, 1), 2);
		Assert.equal(Buffers.LONG.put(b, 0), 1);
		Assert.buffer(b.rewind(), -1, -1, 1, 0, 1);
	}

	@Test
	public void shouldPutFloatsInBuffer() {
		var b = Buffers.FLOAT.of(-1, 1, 0, -1, 1);
		Assert.equal(Buffers.FLOAT.putAt(b, 1, -1, 1), 2);
		Assert.equal(Buffers.FLOAT.put(b, 0), 1);
		Assert.buffer(b.rewind(), -1, -1, 1, 0, 1);
	}

	@Test
	public void shouldPutDoublesInBuffer() {
		var b = Buffers.DOUBLE.of(-1, 1, 0, -1, 1);
		Assert.equal(Buffers.DOUBLE.putAt(b, 1, -1, 1), 2);
		Assert.equal(Buffers.DOUBLE.put(b, 0), 1);
		Assert.buffer(b.rewind(), -1, -1, 1, 0, 1);
	}

	@Test
	public void shouldSetPosition() {
		Assert.equal(Buffers.position(null), 0);
		Assert.equal(Buffers.position(null, 2), 0);
		var b = Buffers.BYTE.of(-1, 1, 0);
		Assert.equal(Buffers.position(b, 2), 2);
		Assert.equal(Buffers.position(b), 2);
		Assert.equal(Buffers.remaining(b), 1);
	}

	@Test
	public void shouldSetLimit() {
		Assert.equal(Buffers.limit(null), 0);
		Assert.equal(Buffers.limit(null, 2), 0);
		Assert.equal(Buffers.remaining(null), 0);
		var b = Buffers.BYTE.of(-1, 1, 0);
		Assert.equal(Buffers.limit(b, 2), 2);
		Assert.equal(Buffers.limit(b), 2);
		Assert.equal(Buffers.remaining(b), 2);
	}

	@Test
	public void shouldGetBytesFromBuffer() {
		Assert.equal(Buffers.bytes(null), null);
		Assert.array(Buffers.bytes(Buffers.CHAR.of('a', '\0')), 0, 0x61, 0, 0);
		Assert.array(Buffers.bytes(Buffers.BYTE.of(-1, 1)), -1, 1);
		Assert.array(Buffers.bytes(Buffers.SHORT.of(-1, 1)), -1, -1, 0, 1);
		Assert.array(Buffers.bytes(Buffers.INT.of(-1, 1)), -1, -1, -1, -1, 0, 0, 0, 1);
		Assert.array(Buffers.bytes(Buffers.LONG.of(-1, 1)), -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0,
			0, 0, 0, 0, 1);
		Assert.array(Buffers.bytes(Buffers.FLOAT.of(-1, 1)), fbytes(-1, 1));
		Assert.array(Buffers.bytes(Buffers.DOUBLE.of(-1, 1)), dbytes(-1, 1));
	}

	@Test
	public void shouldSliceBuffer() {
		var c = c("abc\n\0");
		Assert.equal(Buffers.CHAR.slice(null), null);
		Assert.buffer(Buffers.CHAR.slice(c), "abc\n\0");
		Assert.buffer(Buffers.CHAR.slice(c, 2), "ab");
		Assert.equal(Buffers.CHAR.sliceAt(null, 0), null);
		Assert.buffer(Buffers.CHAR.sliceAt(c, 2), "c\n\0");
		Assert.buffer(c, "abc\n\0");
	}

	@Test
	public void shouldBoundBuffer() {
		var c = c("abc\n\0");
		assertBounds(c, 0, 5);
		Assert.equal(Buffers.CHAR.bound(null, 0), null);
		assertBounds(Buffers.CHAR.bound(c, 3), 0, 3);
		assertBounds(Buffers.CHAR.bound(c.position(1), 4), 1, 3);
		c.position(0).limit(5);
		Assert.equal(Buffers.CHAR.boundAt(null, 0), null);
		Assert.equal(Buffers.CHAR.boundAt(null, 0, 1), null);
		assertBounds(Buffers.CHAR.boundAt(c, 1), 1, 5);
		assertBounds(Buffers.CHAR.boundAt(c, 2, 2), 2, 4);
	}

	@Test
	public void shouldApplyFunctionToBuffer() {
		var c = c("abc\n\0");
		Assert.equal(Buffers.CHAR.apply(null, 0, x -> x), null);
		Assert.equal(Buffers.CHAR.apply(c, null), null);
		Buffers.CHAR.apply(c, 3, _ -> Assert.equal(Buffers.CHAR.getString(c), "abc"));
		Assert.equal(Buffers.CHAR.applyAt(null, 0, x -> x), null);
		Assert.equal(Buffers.CHAR.applyAt(c, 0, 1, null), null);
		Buffers.CHAR.applyAt(c, 1, 3, _ -> Assert.equal(Buffers.CHAR.getString(c), "bc\n"));
		assertBounds(c, 0, 5);
	}

	@Test
	public void shouldProvideReadOnlyView() {
		Assert.equal(Buffers.LONG.readOnly(null), null);
		var c = Buffers.CHAR.of("a\0");
		Assert.same(Buffers.CHAR.readOnly(c), c);
		var b = b(-1, 1);
		Assert.unsupportedOp(() -> Buffers.BYTE.readOnly(b).put(0, (byte) 0));
	}

	@Test
	public void shouldProvideMismatch() {
		Assert.equal(Buffers.CHAR.mismatch(null, c("")), 0);
		Assert.equal(Buffers.CHAR.mismatch(c(""), null), 0);
		Assert.equal(Buffers.CHAR.mismatch(c(""), c("")), -1);
		Assert.equal(Buffers.CHAR.mismatch(c("ab\0"), c("abc")), 2);
	}

	@Test
	public void shouldAdaptByteBuffer() {
		var b = b(0, 0x61, 0, 0);
		var c = Buffers.CHAR.from(b);
		Assert.buffer(c, 'a', '\0');
		c.put(1, 'b');
		Assert.buffer(b, 0, 0x61, 0, 0x62);
	}

	@Test
	public void shouldCopyFromByteBuffer() {
		Assert.equal(Buffers.BYTE.from(null), null);
		Assert.equal(Buffers.BYTE.copyFrom(null), null);
		Assert.buffer(Buffers.CHAR.copyFrom(b(0, 0x61, 0, 0)), 'a', '\0');
		Assert.buffer(Buffers.BYTE.copyFrom(b(-1, 1, 0)), -1, 1, 0);
		Assert.buffer(Buffers.SHORT.copyFrom(b(-1, -1, 0, 0)), -1, 0);
		Assert.buffer(Buffers.INT.copyFrom(b(-1, -1, -1, -1, 0, 0, 0, 0)), -1, 0);
		Assert.buffer(
			Buffers.LONG.copyFrom(b(-1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0)), -1,
			0);
		Assert.buffer(Buffers.FLOAT.copyFrom(b(fbytes(-1, 1))), -1, 1);
		Assert.buffer(Buffers.DOUBLE.copyFrom(b(dbytes(-1, 1))), -1, 1);
	}

	@Test
	public void shouldGetArray() {
		Assert.equal(Buffers.CHAR.get(null), null);
		Assert.array(Buffers.CHAR.get(c("abc\0"), 2), 'a', 'b');
		Assert.array(Buffers.CHAR.getAt(c("abc\0"), 1), 'b', 'c', '\0');
	}

	@Test
	public void shouldCopyToArray() {
		var array = new int[5];
		Assert.equal(Buffers.INT.copy(null, array), 0);
		Assert.equal(Buffers.INT.copy(i(-1, 1), (int[]) null), 0);
		Assert.equal(Buffers.INT.copy(i(-1, 1), array), 2);
		Assert.equal(Buffers.INT.copyAt(i(1), 0, array), 1);
		Assert.equal(Buffers.INT.copyAt(i(1, -1, 0), 1, array, 3), 2);
		Assert.array(array, 1, 1, 0, -1, 0);
	}

	@Test
	public void shouldCopyToBuffer() {
		var array = new int[5];
		var i = IntBuffer.wrap(array);
		Assert.equal(Buffers.INT.copy((IntBuffer) null, i), 0);
		Assert.equal(Buffers.INT.copy(i(-1, 1), (IntBuffer) null), 0);
		Assert.equal(Buffers.INT.copy(i(-1, 1), i()), 0);
		Assert.equal(Buffers.INT.copy(i(-1, 1), i), 2);
		Assert.equal(Buffers.INT.copyAt(i(1), 0, i, 0), 1);
		Assert.equal(Buffers.INT.copyAt(i(1, -1, 0), 1, i, 3), 2);
		Assert.array(array, 1, 1, 0, -1, 0);
	}

	private static void assertBounds(Buffer buffer, int position, int limit) {
		Assert.equal(buffer.position(), position);
		Assert.equal(buffer.limit(), limit);
	}

	private static byte[] fbytes(float... fs) {
		var array = DynamicArray.bytes();
		for (var f : fs)
			array.append(Bytes.toMsb(Float.floatToIntBits(f)));
		return array.truncate();
	}

	private static byte[] dbytes(double... ds) {
		var array = DynamicArray.bytes();
		for (var d : ds)
			array.append(Bytes.toMsb(Double.doubleToLongBits(d)));
		return array.truncate();
	}

	private static CharBuffer c(String s) {
		return Buffers.CHAR.of(s);
	}

	private static ByteBuffer b(int... bytes) {
		return Buffers.BYTE.of(bytes);
	}

	private static ByteBuffer b(byte... bytes) {
		return Buffers.BYTE.of(bytes);
	}

	private static IntBuffer i(int... is) {
		return Buffers.INT.of(is);
	}
}
