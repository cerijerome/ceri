package ceri.serial.ftdi;

import static ceri.common.test.AssertUtil.assertEquals;
import java.time.Instant;
import org.junit.Test;
import ceri.serial.ftdi.jna.LibFtdiStream.FTDIProgressInfo;

public class FtdiProgressInfoBehavior {
	private FTDIProgressInfo ftdiProg = sampleFtdiProg();

	@Test
	public void shouldProvideStringRepresentation() {
		var prog = FtdiProgressInfo.of(ftdiProg);
		assertEquals(prog.toString(), "progress=(total[6.666s@6kB/s], " +
			"current[3333B/222us@4kB/s], previous[2222B/111us], first[1111B])");
	}

	private static FTDIProgressInfo sampleFtdiProg() {
		var ftdiProg = new FTDIProgressInfo();
		ftdiProg.first.totalBytes = 1111;
		ftdiProg.first.time = Instant.ofEpochSecond(1000, 111111);
		ftdiProg.prev.totalBytes = 2222;
		ftdiProg.prev.time = Instant.ofEpochSecond(1000, 222222);
		ftdiProg.current.totalBytes = 3333;
		ftdiProg.current.time = Instant.ofEpochSecond(1000, 333333);
		ftdiProg.currentRate = 4444.4;
		ftdiProg.totalRate = 5555.5;
		ftdiProg.totalTime = 6.666;
		return ftdiProg;
	}
}
