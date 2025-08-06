package ceri.jna.clib;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import ceri.common.collection.Enums;
import ceri.common.data.Field;
import ceri.common.data.TypeTranscoder;
import ceri.common.function.Excepts.IntConsumer;
import ceri.common.function.Excepts.IntFunction;
import ceri.common.io.Connector;
import ceri.common.text.Joiner;
import ceri.jna.clib.jna.CFcntl;

/**
 * Encapsulates a file descriptor as a closable resource.
 */
public interface FileDescriptor extends Connector {
	/** A stateless, no-op instance. */
	FileDescriptor.Fixable NULL = new Null() {
		@Override
		public String toString() {
			return FileDescriptor.class.getSimpleName() + ".NULL";
		}
	};
	/** Typed flag accessor. */
	Field.Types<IOException, FileDescriptor, Open> FLAGS = flagField().types(Open.xcoder);

	/**
	 * Flags for CLib open() and specific CFcntl() calls.
	 */
	enum Open {
		RDONLY(CFcntl.O_RDONLY),
		WRONLY(CFcntl.O_WRONLY),
		RDWR(CFcntl.O_RDWR),
		CREAT(CFcntl.O_CREAT),
		EXCL(CFcntl.O_EXCL),
		NOCTTY(CFcntl.O_NOCTTY),
		TRUNC(CFcntl.O_TRUNC),
		APPEND(CFcntl.O_APPEND),
		NONBLOCK(CFcntl.O_NONBLOCK),
		DSYNC(CFcntl.O_DSYNC),
		ASYNC(CFcntl.O_ASYNC),
		DIRECTORY(CFcntl.O_DIRECTORY),
		NOFOLLOW(CFcntl.O_NOFOLLOW),
		CLOEXEC(CFcntl.O_CLOEXEC),
		SYNC(CFcntl.O_SYNC);

		private static final TypeTranscoder<Open> xcoder =
			new TypeTranscoder<>(t -> t.value, Enums.stream(Open.class)) {
				@Override
				public long decodeRemainder(Collection<Open> receiver, long value) {
					var rem = super.decodeRemainder(receiver, value);
					if ((value & CFcntl.O_ACCMODE) != 0) receiver.remove(RDONLY);
					return rem;
				}
			};
		public final int value;

		public static int encode(Open... flags) {
			return encode(Arrays.asList(flags));
		}

		public static int encode(Iterable<Open> flags) {
			return xcoder.encodeInt(flags);
		}

		public static Set<Open> decode(int value) {
			return xcoder.decodeAll(value);
		}

		public static String string(int value) {
			return Joiner.OR.join(decode(value));
		}

		private Open(int value) {
			this.value = value;
		}
	}

	/**
	 * Provide access to the underlying descriptor value.
	 */
	// int fd() throws IOException;

	/**
	 * Use the file descriptor value.
	 */
	void accept(IntConsumer<IOException> consumer) throws IOException;

	/**
	 * Use the file descriptor value.
	 */
	<T> T apply(IntFunction<IOException, T> function) throws IOException;

	/**
	 * Reads the status flags.
	 */
	int flags() throws IOException;

	/**
	 * Writes the status flags.
	 */
	void flags(int flags) throws IOException;

	/**
	 * Set blocking or non-blocking mode.
	 */
	default void blocking(boolean enabled) throws IOException {
		if (enabled) FLAGS.remove(this, Open.NONBLOCK);
		else FLAGS.add(this, Open.NONBLOCK);
	}

	/**
	 * A file descriptor that is state-aware, with state change notifications.
	 */
	interface Fixable extends FileDescriptor, Connector.Fixable {}

	/**
	 * A stateless, no-op implementation.
	 */
	interface Null extends Connector.Null, FileDescriptor.Fixable {
		@Override
		default void accept(IntConsumer<IOException> consumer) throws IOException {}

		@Override
		default <T> T apply(IntFunction<IOException, T> function) throws IOException {
			return null;
		}

		@Override
		default int flags() throws IOException {
			return 0;
		}

		@Override
		default void flags(int flags) throws IOException {}
	}

	private static Field.Long<IOException, FileDescriptor> flagField() {
		return Field.ofUint(FileDescriptor::flags, FileDescriptor::flags);
	}
}
