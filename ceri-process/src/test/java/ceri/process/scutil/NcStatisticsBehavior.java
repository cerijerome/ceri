package ceri.process.scutil;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertApprox;
import static ceri.common.test.AssertUtil.assertEquals;
import java.util.Map;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class NcStatisticsBehavior {
	private static final String output = TestUtil.resource("statistics-output.txt");

	@Test
	public void shouldNotBreachEqualsContract() {
		NcStatistics t = NcStatistics.builder().add(Map.of("BytesIn", 100)).build();
		NcStatistics eq0 = NcStatistics.builder().add(Map.of("BytesIn", 100)).build();
		NcStatistics eq1 = NcStatistics.builder().add("BytesIn", 100).build();
		NcStatistics ne0 = NcStatistics.builder().add(Map.of("BytesOut", 100)).build();
		NcStatistics ne1 = NcStatistics.builder().add(Map.of("BytesIn", 99)).build();
		TestUtil.exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1);
	}

	@Test
	public void shouldCreateFromOutput() {
		NcStatistics ns = NcStatistics.from(output);
		assertEquals(ns.bytesIn(), 20337);
		assertEquals(ns.bytesOut(), 16517);
		assertEquals(ns.errorsIn(), 10);
		assertEquals(ns.errorsOut(), 0);
		assertEquals(ns.packetsIn(), 77);
		assertEquals(ns.packetsOut(), 118);
	}

	@Test
	public void shouldCalculateErrorRate() {
		NcStatistics ns = NcStatistics.builder().build();
		assertApprox(ns.packetErrorRateIn(), 0.0);
		assertApprox(ns.packetErrorRateOut(), 0.0);
		ns = NcStatistics.from(output);
		assertApprox(ns.packetErrorRateIn(), 0.13);
		assertApprox(ns.packetErrorRateOut(), 0.0);
	}

}
