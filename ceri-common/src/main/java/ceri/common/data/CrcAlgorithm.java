package ceri.common.data;

import static ceri.common.data.ByteUtil.BYTE_MASK;
import static ceri.common.data.ByteUtil.INT_MASK;
import static ceri.common.data.ByteUtil.maskOfBits;
import static ceri.common.data.ByteUtil.shiftBits;
import static ceri.common.math.MathUtil.ubyte;
import ceri.common.data.Crc.EntryAccessor;
import ceri.common.util.HashCoder;

/**
 * Encapsulates the algorithm used to generate CRC values.
 * <pre>
 * poly = powers as bits excluding highest
 * init = initial value
 * refin = true?: reverse bits of each input byte
 * refout = true: reverse bits of result
 * xorout = value to xor with result (after refout)
 * check = result of running ascii bytes "123456789"
 * </pre>
 */
public class CrcAlgorithm {
	public final int width;
	public final long poly;
	public final long init;
	public final boolean refIn;
	public final boolean refOut;
	public final long xorOut;
	private final long mask;
	private final long lastBit;
	private final int shift0;
	private final int shift1;
	private final int shift2;
	private final EntryAccessor cache;

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
		return builder(width).poly(poly).init(init).ref(refIn, refOut).xorOut(xorOut).config();
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

		public CrcAlgorithm config() {
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
		mask = ByteUtil.maskInt(width);
		poly = (builder.powers != null ? maskOfBits(builder.powers) : builder.poly) & mask;
		xorOut = builder.xorOut & mask;
		init = reverse(refOut, builder.init);
		lastBit = 1 << (width - 1);
		shift0 = refIn ? 0 : -Math.max(0, width - Byte.SIZE);
		shift1 = refOut ? 0 : Math.max(0, width - Byte.SIZE);
		shift2 = refOut ? Byte.SIZE : -Byte.SIZE;
		cache = cache();
	}

	public int bytes() {
		return (width + Byte.SIZE - 1) / Byte.SIZE;
	}
	
	/**
	 * Returns the check value, processing ascii chars 123456789. 
	 */
	public long check() {
		return start().add(Crc.checkBytes).crc();
	}

	/**
	 * Create an instance to start CRC generation.
	 */
	public Crc start() {
		return new Crc(this);
	}

	/**
	 * Used by Crc object to complete the CRC value.
	 */
	long complete(long crc) {
		return (crc ^ xorOut) & mask;
	}

	/**
	 * Used by Crc object to process one byte.
	 */
	long apply(long crc, byte val) {
		return (entry(shiftBits(crc, shift1) ^ val) ^ shiftBits(crc, shift2)) & mask;
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(width, poly, init, refIn, refOut, xorOut);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof CrcAlgorithm)) return false;
		CrcAlgorithm other = (CrcAlgorithm) obj;
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
			bool(refOut), hex(xorOut)).toString();
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
		byte[] entries = new byte[Crc.CACHE_SIZE];
		for (int i = 0; i < entries.length; i++)
			entries[i] = (byte) createEntry(i);
		return i -> entries[ubyte(i)] & BYTE_MASK;
	}

	private EntryAccessor intCache() {
		int[] entries = new int[Crc.CACHE_SIZE];
		for (int i = 0; i < entries.length; i++)
			entries[i] = (int) createEntry(i);
		return i -> entries[ubyte(i)] & INT_MASK;
	}

	private EntryAccessor longCache() {
		long[] entries = new long[Crc.CACHE_SIZE];
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
		return reverse(refOut, r) & mask;
	}

	private long reverse(boolean reverse, long value) {
		return reverse ? ByteUtil.reverse(value, width) : value;
	}
}