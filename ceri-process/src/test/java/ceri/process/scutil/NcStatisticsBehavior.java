package ceri.process.scutil;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.assertApprox;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
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
		exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1);
	}

	@Test
	public void shouldCreateFromOutput() {
		NcStatistics ns = NcStatistics.from(output);
		assertThat(ns.bytesIn(), is(20337));
		assertThat(ns.bytesOut(), is(16517));
		assertThat(ns.errorsIn(), is(10));
		assertThat(ns.errorsOut(), is(0));
		assertThat(ns.packetsIn(), is(77));
		assertThat(ns.packetsOut(), is(118));
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
