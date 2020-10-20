package ceri.common.collection;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;

public class IndexedBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Indexed<String> i0 = Indexed.of("7", 7);
		Indexed<String> i1 = Indexed.of("7", 7);
		Indexed<String> n0 = Indexed.of("7", 8);
		Indexed<String> n1 = Indexed.of("", 7);
		Indexed<String> n2 = Indexed.of("8", 7);
		exerciseEquals(i0, i1);
		assertAllNotEqual(i0, n0, n1, n2);
	}

	@Test
	public void shouldBeConsumed() {
		Indexed<String> indexed = Indexed.of("abc", 100);
		StringBuilder b = new StringBuilder();
		indexed.consume((s, i) -> b.append(i).append(":").append(s));
		assertEquals(b.toString(), "100:abc");
	}

	@Test
	public void shouldBeApplied() {
		Indexed<String> indexed = Indexed.of("abc", 100);
		assertEquals(indexed.apply((s, i) -> i + ":" + s), "100:abc");
	}

}
