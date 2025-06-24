package ceri.jna.util;

import static ceri.jna.util.JnaUtil.DEFAULT_CHARSET;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Objects;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.collection.ArrayUtil;
import ceri.common.text.StringUtil;

/**
 * Utilities for nul-terminated strings.
 */
public class NulTerm {

	private NulTerm() {}

	/**
	 * Truncates a string up to the first char 0, if present.
	 */
	public static String truncate(String s) {
		if (StringUtil.empty(s)) return s;
		return s.substring(0, truncateLen(s, 0, s.length()));
	}

	/**
	 * Truncates bytes up to the first 0, if present.
	 */
	public static byte[] truncate(byte[] bytes) {
		if (bytes == null) return null;
		return slice(bytes, 0, truncateLen(bytes, 0, bytes.length));
	}

	/**
	 * Trims any trailing char 0.
	 */
	public static String trim(String s) {
		if (StringUtil.empty(s)) return s;
		return s.substring(0, trimLen(s, 0, s.length()));
	}

	/**
	 * Trims any trailing 0.
	 */
	public static byte[] trim(byte[] bytes) {
		if (bytes == null) return null;
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
		if (bytes == null) return null;
		int len = truncateLen(bytes, 0, bytes.length);
		return charset.decode(ByteBuffer.wrap(bytes, 0, len)).toString();
	}

	/**
	 * Decodes the bytes into a string, up to the first 0 or array limit.
	 */
	public static String readTruncate(Memory m) {
		return readTruncate(DEFAULT_CHARSET, m);
	}

	/**
	 * Decodes the bytes into a string, up to the first 0 or array limit.
	 */
	public static String readTruncate(Charset charset, Memory m) {
		return readTruncate(charset, m, JnaUtil.intSize(m));
	}

	/**
	 * Decodes the bytes into a string, up to the first 0 or array limit.
	 */
	public static String readTruncate(Pointer p, int len) {
		return readTruncate(DEFAULT_CHARSET, p, len);
	}

	/**
	 * Decodes the bytes into a string, up to the first 0 or array limit.
	 */
	public static String readTruncate(Charset charset, Pointer p, int len) {
		if (PointerUtil.validate(p,0,len) == null) return null;
		return readTruncate(charset, JnaUtil.bytes(p, 0L, len));
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
		if (bytes == null) return null;
		int len = trimLen(bytes, 0, bytes.length);
		return charset.decode(ByteBuffer.wrap(bytes, 0, len)).toString();
	}

	/**
	 * Decodes the bytes into a string, dropping any trailing 0.
	 */
	public static String readTrim(Memory m) {
		return readTrim(DEFAULT_CHARSET, m);
	}

	/**
	 * Decodes the bytes into a string, dropping any trailing 0.
	 */
	public static String readTrim(Charset charset, Memory m) {
		return readTrim(charset, m, JnaUtil.intSize(m));
	}

	/**
	 * Decodes the bytes into a string, dropping any trailing 0.
	 */
	public static String readTrim(Pointer p, int len) {
		return readTrim(DEFAULT_CHARSET, p, len);
	}

	/**
	 * Decodes the bytes into a string, dropping any trailing 0.
	 */
	public static String readTrim(Charset charset, Pointer p, int len) {
		if (PointerUtil.validate(p, 0, len) == null) return null;
		return readTrim(charset, JnaUtil.bytes(p, 0L, len));
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
		if (s == null || dest == null || dest.length == 0) return 0;
		byte[] src = s.getBytes(charset);
		int n = Math.min(src.length, dest.length - 1);
		ArrayUtil.copy(src, 0, dest, 0, n);
		dest[n] = 0;
		return n + 1;
	}

	/**
	 * Encodes the string to bytes with a trailing 0, truncating if needed. Returns the number of
	 * bytes written.
	 */
	public static int write(String s, Memory m) {
		return write(s, DEFAULT_CHARSET, m);
	}
	
	/**
	 * Encodes the string to bytes with a trailing 0, truncating if needed. Returns the number of
	 * bytes written.
	 */
	public static int write(String s, Charset charset, Memory m) {
		return write(s, charset, m, JnaUtil.intSize(m));
	}
	
	/**
	 * Encodes the string to bytes with a trailing 0, truncating if needed. Returns the number of
	 * bytes written.
	 */
	public static int write(String s, Pointer p, int len) {
		return write(s, DEFAULT_CHARSET, p, len);
	}
	
	/**
	 * Encodes the string to bytes with a trailing 0, truncating if needed. Returns the number of
	 * bytes written.
	 */
	public static int write(String s, Charset charset, Pointer p, int len) {
		if (s == null || len == 0) return 0;
		Objects.requireNonNull(p);
		byte[] src = s.getBytes(charset);
		int n = Math.min(src.length, len - 1);
		JnaUtil.write(p, src, n);
		p.setByte(n, (byte) 0);
		return n + 1;
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

	/**
	 * Encodes the string to bytes, padding with 0, and truncating if needed. Returns the number of
	 * bytes written.
	 */
	public static int writePad(String s, Memory m) {
		return writePad(s, DEFAULT_CHARSET, m);
	}

	/**
	 * Encodes the string to bytes, padding with 0, and truncating if needed. Returns the number of
	 * bytes written.
	 */
	public static int writePad(String s, Charset charset, Memory m) {
		return writePad(s, charset, m, JnaUtil.intSize(m));
	}

	/**
	 * Encodes the string to bytes, padding with 0, and truncating if needed. Returns the number of
	 * bytes written.
	 */
	public static int writePad(String s, Pointer p, int len) {
		return writePad(s, DEFAULT_CHARSET, p, len);
	}

	/**
	 * Encodes the string to bytes, padding with 0, and truncating if needed. Returns the number of
	 * bytes written.
	 */
	public static int writePad(String s, Charset charset, Pointer p, int len) {
		int n = write(s, charset, p, len);
		JnaUtil.fill(p, n, len - n, 0);
		return len;
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
