package ceri.home.io.pcirlinc;

import java.io.IOException;
import java.util.Arrays;

public abstract class PcIrLincAction {

	public abstract void execute(PcIrLinc pcIrLinc) throws IOException, InterruptedException;

	public static PcIrLincAction createPreset(final PcIrLincType type, final short vendor,
		final PcIrLincButton button) {
		return new PcIrLincAction() {
			@Override
			public void execute(PcIrLinc pcIrLinc) throws IOException, InterruptedException {
				pcIrLinc.sendPreset(type, vendor, button);
			}
		};
	}

	public static PcIrLincAction createLearned(byte[] learned, final int count) {
		final byte[] copy = Arrays.copyOf(learned, learned.length);
		return new PcIrLincAction() {
			@Override
			public void execute(PcIrLinc pcIrLinc) throws IOException, InterruptedException {
				pcIrLinc.sendLearnedIr(copy, count);
			}
		};
	}

}
