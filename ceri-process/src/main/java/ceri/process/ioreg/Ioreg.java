package ceri.process.ioreg;

import java.io.IOException;
import ceri.common.process.Parameters;
import ceri.common.process.Processor;

public class Ioreg {
	private static final String IOREG = "ioreg";
	private final Processor processor;

	public static Ioreg of() {
		return of(Processor.DEFAULT);
	}

	public static Ioreg of(Processor processor) {
		return new Ioreg(processor);
	}

	private Ioreg(Processor processor) {
		this.processor = processor;
	}

	public String exec(String... parameters) throws IOException {
		return exec(Parameters.ofAll(parameters));
	}

	public String exec(Parameters params) throws IOException {
		return processor.exec(Parameters.of(IOREG).addAll(params));
	}

}
