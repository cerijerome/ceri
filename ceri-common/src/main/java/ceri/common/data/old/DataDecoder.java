package ceri.common.data.old;

@Deprecated
class DataDecoder {
	// private final ImmutableByteArray data;
	// private int mark = 0;
	// private int offset = 0;
	//
	// public static <T> T decode(ImmutableByteArray data, Function<DataDecoder, T> fn) {
	// return fn.apply(of(data));
	// }
	//
	// public static DataDecoder of(ImmutableByteArray data, int offset) {
	// return of(data.slice(offset));
	// }
	//
	// public static DataDecoder of(ImmutableByteArray data, int offset, int length) {
	// return of(data.slice(offset, length));
	// }
	//
	// public static DataDecoder of(ImmutableByteArray data) {
	// validateNotNull(data);
	// return new DataDecoder(data);
	// }
	//
	// private DataDecoder(ImmutableByteArray data) {
	// this.data = data;
	// }
	//
	// public DataDecoder sub(int length) {
	// return DataDecoder.of(slice(length));
	// }
	//
	// public ImmutableByteArray slice() {
	// return slice(remaining());
	// }
	//
	// public ImmutableByteArray slice(int length) {
	// ImmutableByteArray data = this.data.slice(offset, length);
	// incrementOffset(data.length());
	// return data;
	// }
	//
	// public DataDecoder skip(int count) {
	// incrementOffset(count);
	// return this;
	// }
	//
	// public DataDecoder rewind(int count) {
	// validateMin(offset - count, 0);
	// offset -= count;
	// return this;
	// }
	//
	// public DataDecoder mark() {
	// mark = offset;
	// return this;
	// }
	//
	// public DataDecoder reset() {
	// return offset(mark);
	// }
	//
	// public DataDecoder offset(int offset) {
	// this.offset = offset;
	// return this;
	// }
	//
	// public int offset() {
	// return offset;
	// }
	//
	// public int remaining() {
	// return total() - offset;
	// }
	//
	// public int total() {
	// return data.length();
	// }
	//
	// public int decodeByte() {
	// return data.getByte(incrementOffset(Byte.BYTES)) & ByteUtil.BYTE_MASK;
	// }
	//
	//// public int decodeShortMsb() {
	//// return (int) ByteUtil.fromBigEndian(data, incrementOffset(Short.BYTES), Short.BYTES);
	//// }
	////
	//// public int decodeShortLsb() {
	//// return (int) ByteUtil.fromLittleEndian(data, incrementOffset(Short.BYTES), Short.BYTES);
	//// }
	////
	//// public int decodeIntMsb() {
	//// return (int) ByteUtil.fromBigEndian(data, incrementOffset(Integer.BYTES), Integer.BYTES);
	//// }
	////
	//// public int decodeIntLsb() {
	//// return (int) ByteUtil.fromLittleEndian(data, incrementOffset(Integer.BYTES),
	// Integer.BYTES);
	//// }
	//
	// public String decodeAscii(int length) {
	// return ByteUtil.fromAscii(data, incrementOffset(length), length);
	// }
	//
	// public String decodeUtf8(int length) {
	// return Utf8Util.decode(data, incrementOffset(length), length);
	// }
	//
	// public DataDecoder validateAscii(String value) {
	// ImmutableByteArray data = ByteUtil.toAscii(value);
	// if (this.data.matches(offset, data)) return skip(data.length());
	// String actual = ByteUtil.fromAscii(this.data, offset, data.length());
	// throw ExceptionUtil.exceptionf("Expected %s: %s", escape(value), escape(actual));
	// }
	//
	// public DataDecoder validateAscii(ByteProvider ascii) {
	// return validateAscii(ascii, 0);
	// }
	//
	// public DataDecoder validateAscii(ByteProvider ascii, int offset) {
	// return validateAscii(ascii, offset, ascii.length() - offset);
	// }
	//
	// public DataDecoder validateAscii(ByteProvider ascii, int offset, int length) {
	// if (data.matches(this.offset, ascii, offset, length)) return skip(length);
	// String actual = ByteUtil.fromAscii(data, this.offset, length);
	// String expected = ByteUtil.fromAscii(ascii, offset, length);
	// throw ExceptionUtil.exceptionf("Expected %s: %s", escape(expected), escape(actual));
	// }
	//
	// public DataDecoder validate(ByteProvider data) {
	// return validate(data, 0);
	// }
	//
	// public DataDecoder validate(ByteProvider data, int offset) {
	// return validate(data, offset, data.length() - offset);
	// }
	//
	// public DataDecoder validate(ByteProvider data, int offset, int length) {
	// if (this.data.matches(this.offset, data, offset, length)) return skip(length);
	// throw ExceptionUtil.exceptionf("Expected %s: %s", toHex(data, offset, length, " "),
	// toHex(this.data, this.offset, length, " "));
	// }
	//
	// private int incrementOffset(int count) {
	// validateMax(offset + count, data.length());
	// int old = offset;
	// offset += count;
	// return old;
	// }

}