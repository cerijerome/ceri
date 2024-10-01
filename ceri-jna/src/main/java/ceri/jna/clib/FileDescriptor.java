package ceri.jna.clib;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import ceri.common.collection.EnumUtil;
import ceri.common.collection.StreamUtil;
import ceri.common.data.FieldTranscoder;
import ceri.common.data.MaskTranscoder;
import ceri.common.data.TypeTranscoder;
import ceri.common.data.ValueField;
import ceri.common.function.ExceptionIntConsumer;
import ceri.common.function.ExceptionIntFunction;
import ceri.common.function.ExceptionIntSupplier;
import ceri.common.io.Connector;
import ceri.common.text.StringUtil;
import ceri.jna.clib.jna.CFcntl;

/**
 * Encapsulates a file descriptor as a closable resource.
 */
public interface FileDescriptor extends Connector {
	/** A stateless, no-op instance. */
	FileDescriptor.Fixable NULL = new Null() {};

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

		private static final TypeTranscoder<Open> xcoder = new TypeTranscoder<>(t -> t.value,
			MaskTranscoder.NULL, EnumUtil.enums(Open.class), StreamUtil.mergeError()) {
			@Override
			protected long decodeWithRemainder(Collection<Open> receiver, long value) {
				var rem = super.decodeWithRemainder(receiver, value);
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
			return StringUtil.join("|", decode(value));
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
	void accept(ExceptionIntConsumer<IOException> consumer) throws IOException;

	/**
	 * Use the file descriptor value.
	 */
	<T> T apply(ExceptionIntFunction<IOException, T> function) throws IOException;

	/**
	 * Provides access to status flags.
	 */
	FieldTranscoder<IOException, Open> flags();

	/**
	 * Set blocking or non-blocking mode.
	 */
	default void blocking(boolean enabled) throws IOException {
		if (enabled) flags().remove(Open.NONBLOCK);
		else flags().add(Open.NONBLOCK);
	}

	/**
	 * A file descriptor that is state-aware, with state change notifications.
	 */
	interface Fixable extends FileDescriptor, Connector.Fixable {}

	/**
	 * A stateless, no-op implementation.
	 */
	interface Null extends Connector.Null, FileDescriptor.Fixable {
		static FieldTranscoder<IOException, Open> FLAGS =
			FileDescriptor.flagField(() -> 0, x -> {});

		@Override
		default void accept(ExceptionIntConsumer<IOException> consumer) throws IOException {}

		@Override
		default <T> T apply(ExceptionIntFunction<IOException, T> function) throws IOException {
			return null;
		}

		@Override
		default FieldTranscoder<IOException, Open> flags() {
			return FLAGS;
		}
	}

	/**
	 * Creates a flag access field transcoder from getter and setter.
	 */
	static FieldTranscoder<IOException, Open> flagField(ExceptionIntSupplier<IOException> getFn,
		ExceptionIntConsumer<IOException> setFn) {
		return Open.xcoder.field(ValueField.ofInt(getFn, setFn));
	}
}
