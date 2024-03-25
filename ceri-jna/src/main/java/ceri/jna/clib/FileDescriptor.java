package ceri.jna.clib;

import java.io.IOException;
import ceri.common.data.FieldTranscoder;
import ceri.common.data.ValueField;
import ceri.common.function.ExceptionIntConsumer;
import ceri.common.function.ExceptionIntFunction;
import ceri.common.function.ExceptionIntSupplier;
import ceri.common.io.Connector;

/**
 * Encapsulates a file descriptor as a closable resource.
 */
public interface FileDescriptor extends Connector {
	/** A stateless, no-op instance. */
	FileDescriptor.Fixable NULL = new Null() {};

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
	FieldTranscoder<IOException, OpenFlag> flags();

	/**
	 * A file descriptor that is state-aware, with state change notifications.
	 */
	interface Fixable extends FileDescriptor, Connector.Fixable {}

	/**
	 * A stateless, no-op implementation.
	 */
	interface Null extends Connector.Null, FileDescriptor.Fixable {
		static FieldTranscoder<IOException, OpenFlag> FLAGS =
			FileDescriptor.flagField(() -> 0, x -> {});

		@Override
		default void accept(ExceptionIntConsumer<IOException> consumer) throws IOException {}

		@Override
		default <T> T apply(ExceptionIntFunction<IOException, T> function) throws IOException {
			return null;
		}

		@Override
		default FieldTranscoder<IOException, OpenFlag> flags() {
			return FLAGS;
		}
	}

	/**
	 * Creates a flag access field transcoder from getter and setter.
	 */
	static FieldTranscoder<IOException, OpenFlag> flagField(ExceptionIntSupplier<IOException> getFn,
		ExceptionIntConsumer<IOException> setFn) {
		return OpenFlag.xcoder.field(ValueField.ofInt(getFn, setFn));
	}
}
