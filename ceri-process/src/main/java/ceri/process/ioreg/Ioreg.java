package ceri.process.ioreg;

import java.io.IOException;
import ceri.process.util.Parameters;
import ceri.process.util.Processor;

public class Ioreg {
	private static final String IOREG = "ioreg";
	private final Processor processor;

	public Ioreg() {
		this(Processor.DEFAULT);
	}

	public Ioreg(Processor processor) {
		this.processor = processor;
	}

	public String exec(String... parameters) throws IOException {
		return exec(Parameters.of(parameters));
	}

	private String exec(Parameters params) throws IOException {
		return processor.exec(Parameters.of(IOREG).add(params));
	}

}
