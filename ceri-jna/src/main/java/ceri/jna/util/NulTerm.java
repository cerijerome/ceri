package ceri.jna.util;

import static ceri.jna.util.JnaUtil.DEFAULT_CHARSET;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import ceri.common.collection.ArrayUtil;

/**
 * Utilities for nul-terminated strings.
 */
public class NulTerm {

	private NulTerm() {}

	/**
	 * Truncates a string up to the first char 0, if present.
	 */
	public static String truncate(String s) {
		return s.substring(0, truncateLen(s, 0, s.length()));
	}

	/**
	 * Truncates bytes up to the first 0, if present.
	 */
	public static byte[] truncate(byte[] bytes) {
		return slice(bytes, 0, truncateLen(bytes, 0, bytes.length));
	}

	/**
	 * Trims any trailing char 0.
	 */
	public static String trim(String s) {
		return s.substring(0, trimLen(s, 0, s.length()));
	}

	/**
	 * Trims any trailing 0.
	 */
	public static byte[] trim(byte[] bytes) {
		return slice(bytes, 0, trimLen(bytes, 0, bytes.length));
	}

	/**
	 * Decodes the bytes into a string, up to the first 0 or array limit.
	 */
	public static String readTruncate(byte[] bytes) {
		return readTruncate(DEFAULT_CHARSET, bytes);
	}

	/**
	 * Decodes the bytes into a string, up to the first 0 or array limit.
	 */
	public static String readTruncate(Charset charset, byte[] bytes) {
		int len = truncateLen(bytes, 0, bytes.length);
		return charset.decode(ByteBuffer.wrap(bytes, 0, len)).toString();
	}

	/**
	 * Decodes the bytes into a string, dropping any trailing 0.
	 */
	public static String readTrim(byte[] bytes) {
		return readTrim(DEFAULT_CHARSET, bytes);
	}

	/**
	 * Decodes the bytes into a string, dropping any trailing 0.
	 */
	public static String readTrim(Charset charset, byte[] bytes) {
		int len = trimLen(bytes, 0, bytes.length);
		return charset.decode(ByteBuffer.wrap(bytes, 0, len)).toString();
	}

	/**
	 * Encodes the string to bytes with a trailing 0, truncating if needed. Returns the number of
	 * bytes written.
	 */
	public static int write(String s, byte[] dest) {
		return write(s, DEFAULT_CHARSET, dest);
	}

	/**
	 * Encodes the string to bytes with a trailing 0, truncating if needed. Returns the number of
	 * bytes written.
	 */
	public static int write(String s, Charset charset, byte[] dest) {
		if (s == null || dest.length == 0) return 0;
		byte[] src = s.getBytes(charset);
		int len = Math.min(src.length, dest.length - 1);
		ArrayUtil.copy(src, 0, dest, 0, len);
		dest[len] = 0;
		return len + 1;
	}

	/**
	 * Encodes the string to bytes, padding with 0, and truncating if needed. Returns the number of
	 * bytes written.
	 */
	public static int writePad(String s, byte[] dest) {
		return writePad(s, DEFAULT_CHARSET, dest);
	}

	/**
	 * Encodes the string to bytes, padding with 0, and truncating if needed. Returns the number of
	 * bytes written.
	 */
	public static int writePad(String s, Charset charset, byte[] dest) {
		int n = write(s, charset, dest);
		ArrayUtil.fill(dest, n, 0);
		return dest.length;
	}

	private static int truncateLen(String chars, int start, int length) {
		for (int i = 0; i < length; i++)
			if (chars.charAt(start + i) == 0) return i;
		return length;
	}

	private static int truncateLen(byte[] bytes, int start, int length) {
		for (int i = 0; i < length; i++)
			if (bytes[start + i] == 0) return i;
		return length;
	}

	private static int trimLen(String chars, int start, int length) {
		for (int i = length - 1; i >= 0; i--)
			if (chars.charAt(start + i) != 0) return i + 1;
		return 0;
	}

	private static int trimLen(byte[] bytes, int start, int length) {
		for (int i = length - 1; i >= 0; i--)
			if (bytes[start + i] != 0) return i + 1;
		return 0;
	}

	private static byte[] slice(byte[] bytes, int start, int length) {
		if (length == 0) return ArrayUtil.EMPTY_BYTE;
		if (start == 0 && bytes.length == length) return bytes;
		return ArrayUtil.copyOf(bytes, start, length);
	}
}
