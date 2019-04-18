package ceri.common.data;

import static ceri.common.validation.ValidationUtil.validateMin;
import static ceri.common.validation.ValidationUtil.validateNotNull;
import static ceri.common.validation.ValidationUtil.validateRange;
import java.util.BitSet;
import java.util.function.Consumer;
import ceri.common.collection.ArrayUtil;
import ceri.common.collection.ImmutableByteArray;
import ceri.common.text.Utf8Util;
import ceri.common.util.BasicUtil;

public class DataEncoder {
	private final byte[] data;
	private int start;
	private int length;
	private int mark = 0;
	private int offset = 0;

	public static interface EncodableField {
		/**
		 * Returns the offset after encoding.
		 */
		int encode(byte[] data, int offset);
	}

	public static interface Encodable {
		default int size() {
			return 0;
		}

		default ImmutableByteArray encode() {
			int size = size();
			if (size == 0) return ImmutableByteArray.EMPTY;
			return DataEncoder.encode(size, this::encode);
		}

		default void encode(DataEncoder encoder) {
			BasicUtil.unused(encoder);
		}
	}

	public static ImmutableByteArray encode(int size, Consumer<DataEncoder> consumer) {
		byte[] data = new byte[size];
		consumer.accept(DataEncoder.of(data));
		return ImmutableByteArray.wrap(data);
	}

	public static DataEncoder of(int size) {
		return of(new byte[size]);
	}

	public static DataEncoder of(byte[] data) {
		return of(data, 0);
	}

	public static DataEncoder of(byte[] data, int offset) {
		return of(data, offset, data.length - offset);
	}

	public static DataEncoder of(byte[] data, int offset, int length) {
		validateNotNull(data);
		ArrayUtil.validateSlice(data.length, offset, length);
		return new DataEncoder(data, offset, length);
	}

	private DataEncoder(byte[] data, int start, int length) {
		this.data = data;
		this.start = start;
		this.length = length;
	}

	public DataEncoder skip(int count) {
		incrementOffset(count);
		return this;
	}

	public DataEncoder rewind(int count) {
		validateMin(offset - count, 0);
		offset -= count;
		return this;
	}

	public DataEncoder mark() {
		mark = offset;
		return this;
	}

	public DataEncoder reset() {
		return offset(mark);
	}

	public DataEncoder offset(int offset) {
		setOffset(offset);
		return this;
	}

	public int offset() {
		return offset;
	}

	public int remaining() {
		return length - offset;
	}

	public int total() {
		return length;
	}

	public ImmutableByteArray data() {
		return ImmutableByteArray.wrap(data, start, length);
	}

	public ImmutableByteArray slice(int offset) {
		return slice(offset, length - offset);
	}

	public ImmutableByteArray slice(int offset, int length) {
		return data().slice(offset, length);
	}

	public DataEncoder encode(BitSet bitSet) {
		return copy(ImmutableByteArray.wrap(bitSet.toByteArray()));
	}

	public DataEncoder encodeByte(int value) {
		data[position(Byte.BYTES)] = (byte) (value & ByteUtil.BYTE_MASK);
		return this;
	}

	public DataEncoder fill(int value, int count) {
		for (int i = 0; i < count; i++) encodeByte(value);
		return this;
	}
	
	public DataEncoder encodeShortMsb(int value) {
		ByteUtil.writeBigEndian(value, data, position(Short.BYTES), Short.BYTES);
		return this;
	}

	public DataEncoder encodeShortLsb(int value) {
		ByteUtil.writeLittleEndian(value, data, position(Short.BYTES), Short.BYTES);
		return this;
	}

	public DataEncoder encodeIntMsb(int value) {
		ByteUtil.writeBigEndian(value, data, position(Integer.BYTES), Integer.BYTES);
		return this;
	}

	public DataEncoder encodeIntLsb(int value) {
		ByteUtil.writeLittleEndian(value, data, position(Integer.BYTES), Integer.BYTES);
		return this;
	}

	public DataEncoder encodeAscii(String value, int length) {
		return copyWithPadding(ByteUtil.toAscii(value), 0, length);		
	}

	public DataEncoder encodeAscii(String value) {
		return copy(ByteUtil.toAscii(value));
	}

	public DataEncoder encodeUtf8(String value, int length) {
		return copyWithPadding(Utf8Util.encode(value), 0, length);		
	}
	
	public DataEncoder encodeUtf8(String value) {
		setOffset(Utf8Util.encodeTo(value, data, offset));
		return this;
	}

	public DataEncoder encode(EncodableField encodable) {
		setOffset(encodable.encode(data, offset));
		return this;
	}

	public DataEncoder copy(ByteProvider data) {
		return copy(data, 0);
	}

	public DataEncoder copy(ByteProvider data, int offset) {
		return copy(data, offset, data.length() - offset);
	}

	public DataEncoder copy(ByteProvider data, int offset, int length) {
		data.copyTo(offset, this.data, position(length), length);
		return this;
	}

	private DataEncoder copyWithPadding(byte[] data, int offset, int length) {
		return copyWithPadding(ByteProvider.wrap(data), offset, length);
	}
	
	private DataEncoder copyWithPadding(ByteProvider data, int offset, int length) {
		int len = Math.min(length, data.length() - offset);
		copy(data, offset, len);
		return skip(length - len);
	}


	private int position(int count) {
		return start + incrementOffset(count);
	}

	private int incrementOffset(int count) {
		return setOffset(offset + count);
	}

	private int setOffset(int offset) {
		validateRange(offset, 0, length);
		int old = this.offset;
		this.offset = offset;
		return old;
	}

}
