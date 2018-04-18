package ceri.process.tesseract;

import java.io.File;
import java.io.IOException;
import ceri.process.util.Parameters;
import ceri.process.util.Processor;

public class Tesseract {
	// Make sure tesseract is on env path.
	private static final String TESSERACT = "tesseract";
	private static final String STDIN = "stdin";
	private static final String STDOUT = "stdout";
	private static final String PAGE_SEG_MODE_OPTION = "-psm";
	private final Processor processor;
	private String inputName;
	private String outputBase;
	private Integer pageSegMode;

	public Tesseract() {
		this(Processor.DEFAULT);
	}

	public Tesseract(Processor processor) {
		this.processor = processor;
	}

	public Tesseract input(File file) {
		this.inputName = file.getPath();
		return this;
	}

	public Tesseract inputName(String inputName) {
		this.inputName = inputName;
		return this;
	}

	public Tesseract outputBase(String outputBase) {
		this.outputBase = outputBase;
		return this;
	}

	public Tesseract pageSegMode(Integer pageSegMode) {
		this.pageSegMode = pageSegMode;
		return this;
	}

	public String exec() throws IOException {
		Parameters p = new Parameters();
		p.add(inputName == null ? STDIN : inputName);
		p.add(outputBase == null ? STDOUT : outputBase);
		if (pageSegMode != null) p.add(PAGE_SEG_MODE_OPTION).add(pageSegMode);
		return exec(p);
	}

	private String exec(Parameters params) throws IOException {
		return processor.exec(Parameters.of(TESSERACT).add(params));
	}

}
