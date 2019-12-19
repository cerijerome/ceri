package ceri.common.process;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;
import ceri.common.function.ExceptionSupplier;

/**
 * Abstracts ProcessBuilder role.
 */
public interface ProcessCommand {
	static ProcessCommand NULL = of(ProcessUtil::nullProcess, List::of);

	/**
	 * Starts the process for the command.
	 */
	Process start() throws IOException;

	/**
	 * Returns the command parameters.
	 */
	List<String> command();

	/**
	 * String representation of the full command.
	 */
	default String asString() {
		return ProcessUtil.toString(command());
	}

	/**
	 * Wrap a ProcessBuilder.
	 */
	static ProcessCommand of(ProcessBuilder builder) {
		return of(builder::start, builder::command);
	}

	static ProcessCommand of(ExceptionSupplier<IOException, Process> starter,
		Supplier<List<String>> commandSupplier) {
		return new ProcessCommand() {
			@Override
			public Process start() throws IOException {
				return starter == null ? null : starter.get();
			}

			@Override
			public List<String> command() {
				return commandSupplier == null ? List.of() : commandSupplier.get();
			}
		};
	}
}
