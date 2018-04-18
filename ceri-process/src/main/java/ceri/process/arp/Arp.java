package ceri.process.arp;

import java.io.IOException;
import java.util.List;
import ceri.process.util.Output;
import ceri.process.util.Parameters;
import ceri.process.util.Processor;

public class Arp {
	private static final String ARP = "arp";
	private final Processor processor;

	public Arp() {
		this(Processor.DEFAULT);
	}

	public Arp(Processor processor) {
		this.processor = processor;
	}

	public Output<List<ArpEntry>> all() throws IOException {
		return new Output<>(exec(Parameters.of("-a")), ArpEntry::fromOuput);
	}

	private String exec(Parameters params) throws IOException {
		return processor.exec(Parameters.of(ARP).add(params));
	}

}
