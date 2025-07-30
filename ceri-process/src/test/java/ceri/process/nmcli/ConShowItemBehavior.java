package ceri.process.nmcli;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;

public class ConShowItemBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		ConShowItem t = ConShowItem.of("name", "uuid", "type", "device");
		ConShowItem eq0 = ConShowItem.of("name", "uuid", "type", "device");
		ConShowItem ne0 = ConShowItem.NULL;
		ConShowItem ne1 = ConShowItem.of("Name", "uuid", "type", "device");
		ConShowItem ne2 = ConShowItem.of("name", "", "type", "device");
		ConShowItem ne3 = ConShowItem.of("name", "uuid", "types", "device");
		ConShowItem ne4 = ConShowItem.of("name", "uuid", "type", "dev");
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3, ne4);
	}

	@Test
	public void shouldReturnNoResultForHeaderOutputOnly() {
		assertOrdered(ConShowItem.fromOutput("A  B  C"));
	}

	@Test
	public void shouldDetermineIfNull() {
		assertTrue(ConShowItem.of(null, null, null, null).isNull());
		assertFalse(ConShowItem.of("", null, null, null).isNull());
		assertFalse(ConShowItem.of(null, "", null, null).isNull());
		assertFalse(ConShowItem.of(null, null, "", null).isNull());
		assertFalse(ConShowItem.of(null, null, null, "").isNull());
	}

}
