package ceri.common.io;

import java.util.Iterator;
import java.util.NoSuchElementException;
import ceri.common.collection.ArrayIterator;

public class BitIterator implements Iterator<Boolean> {
	private static final int MAX_MASK = 0x80;
	private static final int MIN_MASK = 0x01;
	private final Iterator<Byte> byteIterator;
	private final Start start;
	private int mask;
	private Byte currentByte = null;

	public static enum Start {
		high,
		low
	}

	public BitIterator(Start start, Iterator<Byte> byteIterator) {
		this.byteIterator = byteIterator;
		this.start = start;
		mask = initialMask();
	}

	public BitIterator(byte... bytes) {
		this(Start.low, bytes);
	}

	public BitIterator(Start start, byte... bytes) {
		this(start, ArrayIterator.createByte(bytes));
	}

	@Override
	public boolean hasNext() {
		return currentByte != null || byteIterator.hasNext();
	}

	@Override
	public Boolean next() {
		if (!hasNext()) throw new NoSuchElementException();
		if (currentByte == null) {
			currentByte = byteIterator.next();
			mask = initialMask();
		}
		Boolean val = (currentByte.byteValue() & mask) == 0 ? Boolean.FALSE : Boolean.TRUE;
		if (start == Start.high) {
			mask >>>= 1;
			if (mask < MIN_MASK) currentByte = null;
		} else {
			mask <<= 1;
			if (mask > MAX_MASK) currentByte = null;
		}
		return val;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	private int initialMask() {
		return start == Start.high ? MAX_MASK : MIN_MASK;
	}

}