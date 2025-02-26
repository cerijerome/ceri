package ceri.common.data;

import static ceri.common.collection.ImmutableUtil.enumsMap;
import static ceri.common.data.ByteUtil.BYTE_MASK;
import static ceri.common.data.ByteUtil.INT_MASK;
import static ceri.common.data.ByteUtil.maskOfBits;
import static ceri.common.data.ByteUtil.shiftBits;
import static ceri.common.math.MathUtil.ubyte;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Encapsulates the algorithm used to generate CRC values. Generates an entry cache on creation.
 *
 * <pre>
 * See http://ross.net/crc/download/crc_v3.txt
 * poly = powers as bits (excluding highest bit)
 * init = initial value
 * refin = true?: reverse bits of each input byte
 * refout = true: reverse bits of result
 * xorout = value to xor with result (after refout)
 * check = result of running ascii bytes "123456789"
 * </pre>
 */
public class CrcAlgorithm {
	private static final int CACHE_SIZE = 1 << Byte.SIZE;
	public static final ByteProvider checkBytes = ByteUtil.toAscii("123456789");
	// Algorithm parameters
	public final int width;
	public final long poly;
	public final long init;
	public final boolean refIn;
	public final boolean refOut;
	public final long xorOut;
	// internal constants
	private final long mask;
	private final long lastBit;
	private final int shift0;
	private final int shift1;
	private final int shift2;
	private final EntryAccessor cache;

	/**
	 * Commonly used algorithms, cached on access.
	 */
	public static enum Std {
		none(() -> of(0, 0)),
		crc8Smbus(() -> of(8, 0x7), //
			"CRC-8/SMBUS", "CRC-8"),
		crc16Ibm3740(() -> of(16, 0x1021, -1), //
			"CRC-16/IBM-3740", "CRC-16/AUTOSAR", "CRC-16/CCITT-FALSE"),
		crc16Kermit(() -> of(16, 0x1021, 0, true), //
			"CRC-16/CCITT", "CRC-16/CCITT-TRUE", "CRC-16/V-41-LSB", "CRC-CCITT", "KERMIT"),
		crc16Xmodem(() -> of(16, 0x1021), //
			"CRC-16/XMODEM", "CRC-16/ACORN", "CRC-16/LTE", "CRC-16/V-41-MSB", "XMODEM", "ZMODEM"),
		crc24Ble(() -> of(24, 0x00065b, 0x555555, true), //
			"CRC-24/BLE"),
		crc32Bzip2(() -> of(32, 0x04c11db7, -1, false, false, -1), //
			"CRC-32/BZIP2", "CRC-32/AAL5", "CRC-32/DECT-B", "B-CRC-32"),
		crc32Cksum(() -> of(32, 0x04c11db7, 0, false, false, -1), //
			"CRC-32/CKSUM", "CKSUM", "CRC-32/POSIX"),
		crc32IsoHdlc(() -> of(32, 0x04c11db7, -1, true, true, -1), //
			"CRC-32/ISO-HDLC", "CRC-32", "CRC-32/ADCCP", "CRC-32/V-42", "CRC-32/XZ", "PKZIP"),
		crc32Mpeg2(() -> of(32, 0x04c11db7, -1), //
			"CRC-32/MPEG-2"),
		crc64GoIso(() -> of(64, 0x1b, -1, true, true, -1), //
			"CRC-64/GO-ISO"),
		crc64Xz(() -> of(64, 0x42f0e1eba9ea3693L, -1, true, true, -1), //
			"CRC-64/XZ");

		private static final Map<Std, CrcAlgorithm> cache = new ConcurrentHashMap<>();
		private static final Map<String, Std> nameLookup = enumsMap(t -> t.names, Std.class);
		private final Supplier<CrcAlgorithm> supplier;
		public final Set<String> names;

		public static Std from(String name) {
			return nameLookup.get(name.toUpperCase());
		}

		private Std(Supplier<CrcAlgorithm> supplier, String... names) {
			this.supplier = supplier;
			this.names = Set.of(names);
		}

		public long check() {
			return algorithm().check();
		}

		public Crc start() {
			return algorithm().start();
		}

		public CrcAlgorithm algorithm() {
			return cache.computeIfAbsent(this, _ -> supplier.get());
		}
	}

	private static interface EntryAccessor {
		long entry(int i);
	}

	/**
	 * Create an algorithm specifying width and poly. init is 0, refIn/Out are false, xorOut is 0.
	 */
	public static CrcAlgorithm of(int width, long poly) {
		return of(width, poly, 0);
	}

	/**
	 * Create an algorithm specifying width, poly, and init. refIn/Out are false, xorOut is 0.
	 */
	public static CrcAlgorithm of(int width, long poly, long init) {
		return of(width, poly, init, false);
	}

	/**
	 * Create an algorithm specifying width, poly, init, and both refIn/Out. xorOut is 0.
	 */
	public static CrcAlgorithm of(int width, long poly, long init, boolean ref) {
		return of(width, poly, init, ref, ref, 0);
	}

	/**
	 * Create an algorithm specifying width, poly, init, refIn/Out, and xorOut.
	 */
	public static CrcAlgorithm of(int width, long poly, long init, boolean refIn, boolean refOut,
		long xorOut) {
		return builder(width).poly(poly).init(init).ref(refIn, refOut).xorOut(xorOut).build();
	}

	public static class Builder {
		final int width;
		long poly = 0;
		int[] powers = null;
		long init = 0;
		boolean refIn = false;
		boolean refOut = false;
		long xorOut = 0;

		Builder(int width) {
			this.width = width;
		}

		public CrcAlgorithm.Builder poly(long poly) {
			this.poly = poly;
			return powers(null);
		}

		public CrcAlgorithm.Builder powers(int... powers) {
			this.powers = powers;
			return this;
		}

		public CrcAlgorithm.Builder init(long init) {
			this.init = init;
			return this;
		}

		public CrcAlgorithm.Builder ref(boolean refIn, boolean refOut) {
			this.refIn = refIn;
			this.refOut = refOut;
			return this;
		}

		public CrcAlgorithm.Builder xorOut(long xorOut) {
			this.xorOut = xorOut;
			return this;
		}

		public CrcAlgorithm build() {
			return new CrcAlgorithm(this);
		}
	}

	public static Builder builder(int width) {
		return new Builder(width);
	}

	CrcAlgorithm(CrcAlgorithm.Builder builder) {
		width = builder.width;
		refIn = builder.refIn;
		refOut = builder.refOut;
		mask = ByteUtil.mask(width);
		poly = (builder.powers != null ? maskOfBits(builder.powers) : builder.poly) & mask;
		xorOut = builder.xorOut & mask;
		init = reverse(refOut, builder.init);
		lastBit = 1L << (width - 1);
		shift0 = refIn ? 0 : -Math.max(0, width - Byte.SIZE);
		shift1 = refOut ? 0 : Math.max(0, width - Byte.SIZE);
		shift2 = refOut ? Byte.SIZE : -Byte.SIZE;
		cache = cache();
	}

	/**
	 * Returns the number of bytes needed to store the CRC.
	 */
	public int bytes() {
		return (width + Byte.SIZE - 1) / Byte.SIZE;
	}

	/**
	 * Returns the check value, processing ascii chars 123456789.
	 */
	public long check() {
		return start().add(checkBytes).crc();
	}

	/**
	 * Create an instance to start CRC generation.
	 */
	public Crc start() {
		return new Crc(this);
	}

	/**
	 * Used by Crc object to mask values.
	 */
	long mask(long value) {
		return value & mask;
	}

	/**
	 * Used by Crc object to complete the CRC value.
	 */
	long complete(long crc) {
		return mask(crc ^ xorOut);
	}

	/**
	 * Used by Crc object to process one byte.
	 */
	long apply(long crc, byte val) {
		return mask(entry(shiftBits(crc, shift1) ^ val) ^ shiftBits(crc, shift2));
	}

	@Override
	public int hashCode() {
		return Objects.hash(width, poly, init, refIn, refOut, xorOut);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof CrcAlgorithm other)) return false;
		if (width != other.width) return false;
		if (poly != other.poly) return false;
		if (init != other.init) return false;
		if (refIn != other.refIn) return false;
		if (refOut != other.refOut) return false;
		if (xorOut != other.xorOut) return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format("CRC-%d[%s,%s,%c,%c,%s]", width, hex(poly), hex(init), bool(refIn),
			bool(refOut), hex(xorOut));
	}

	private char bool(boolean b) {
		return b ? 'T' : 'F';
	}

	private String hex(long n) {
		return n == 0 ? "0" : "0x" + Long.toHexString(n);
	}

	private EntryAccessor cache() {
		if (width <= Byte.SIZE) return byteCache();
		if (width <= Integer.SIZE) return intCache();
		return longCache();
	}

	private EntryAccessor byteCache() {
		byte[] entries = new byte[CACHE_SIZE];
		for (int i = 0; i < entries.length; i++)
			entries[i] = (byte) createEntry(i);
		return i -> entries[ubyte(i)] & BYTE_MASK;
	}

	private EntryAccessor intCache() {
		int[] entries = new int[CACHE_SIZE];
		for (int i = 0; i < entries.length; i++)
			entries[i] = (int) createEntry(i);
		return i -> entries[ubyte(i)] & INT_MASK;
	}

	private EntryAccessor longCache() {
		long[] entries = new long[CACHE_SIZE];
		for (int i = 0; i < entries.length; i++)
			entries[i] = createEntry(i);
		return i -> entries[ubyte(i)];
	}

	private long entry(long i) {
		return cache.entry((int) i);
	}

	private long createEntry(long r) {
		r = shiftBits(reverse(refIn, r), shift0);
		for (int i = 0; i < Byte.SIZE; i++)
			r = (r & lastBit) != 0 ? r = (r << 1) ^ poly : r << 1;
		return mask(reverse(refOut, r));
	}

	private long reverse(boolean reverse, long value) {
		return reverse ? ByteUtil.reverse(value, width) : value;
	}

}