package ceri.jna.clib;

import static ceri.common.validation.ValidationUtil.validateRange;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.sun.jna.Pointer;
import ceri.common.data.TypeTranscoder;
import ceri.common.function.Excepts.Closeable;
import ceri.common.reflect.ReflectUtil;
import ceri.jna.clib.jna.CMman;
import ceri.jna.clib.jna.CUnistd;

public class Mmap implements Closeable<IOException> {
	private final Pointer address;
	public final long length;

	public enum Protection {
		READ(CMman.PROT_READ),
		WRITE(CMman.PROT_WRITE),
		EXEC(CMman.PROT_EXEC);

		private static final TypeTranscoder<Protection> xcoder =
			TypeTranscoder.of(t -> t.value, Protection.class);
		public static final List<Protection> RW = List.of(READ, WRITE);
		public final int value;

		private Protection(int value) {
			this.value = value;
		}
	}

	public enum Visibility {
		SHARED(CMman.MAP_SHARED),
		PRIVATE(CMman.MAP_PRIVATE);

		public final int value;

		private Visibility(int value) {
			this.value = value;
		}
	}

	public enum Option {
		FIXED(CMman.MAP_FIXED),
		ANONYMOUS(CMman.MAP_ANONYMOUS),
		NO_RESERVE(CMman.MAP_NORESERVE);

		private static final TypeTranscoder<Option> xcoder =
			TypeTranscoder.of(t -> t.value, Option.class);
		public final int value;

		private Option(int value) {
			this.value = value;
		}
	}

	public static class Builder {
		final Visibility visibility;
		final FileDescriptor fd;
		final Set<Protection> protections = new HashSet<>();
		final Set<Option> options = new HashSet<>();
		Pointer address = null;
		final long length;
		final int offset;

		private Builder(Visibility visibility, long length, FileDescriptor fd, int offset) {
			this.visibility = visibility;
			this.length = length;
			this.fd = fd;
			this.offset = offset;
		}

		public Builder address(Pointer address) {
			this.address = address;
			return this;
		}

		public Builder protections(Protection... protections) {
			return protections(Arrays.asList(protections));
		}

		public Builder protections(Collection<Protection> protections) {
			this.protections.addAll(protections);
			return this;
		}

		public Builder options(Option... options) {
			return options(Arrays.asList(options));
		}

		public Builder options(Collection<Option> options) {
			this.options.addAll(options);
			return this;
		}

		public Mmap map() throws IOException {
			return new Mmap(mmap(), length);
		}

		private Pointer mmap() throws IOException {
			int protection = Protection.xcoder.encodeInt(protections);
			int flags = visibility.value | Option.xcoder.encodeInt(options);
			if (fd == null) return CMman.mmap(address, length, protection, flags, -1, offset);
			return fd.apply(f -> CMman.mmap(address, length, protection, flags, f, offset));
		}
	}

	/**
	 * Calculates the minimum length based on page size.
	 */
	public static long length(long length) throws IOException {
		long pageSize = CUnistd.getpagesize();
		return Math.ceilDiv(length, pageSize) * pageSize;
	}

	public static Builder anonymous(Visibility visibility, long length) {
		return file(visibility, length, null, 0).options(Option.ANONYMOUS);
	}

	public static Builder file(Visibility visibility, long length, FileDescriptor fd, int offset) {
		return new Builder(visibility, length, fd, offset);
	}

	private Mmap(Pointer address, long length) {
		this.address = address;
		this.length = length;
	}

	public Pointer address(long offset) {
		validateRange(offset, 0, this.length - 1);
		return address.share(offset);
	}

	@Override
	public void close() throws IOException {
		CMman.munmap(address, length);
	}

	@Override
	public String toString() {
		return String.format("%s@%x+%x", ReflectUtil.name(getClass()), Pointer.nativeValue(address),
			length);
	}
}
