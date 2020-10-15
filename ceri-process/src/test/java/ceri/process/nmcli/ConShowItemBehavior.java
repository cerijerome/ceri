package ceri.process.nmcli;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
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
		assertIterable(ConShowItem.fromOutput("A  B  C"));
	}

	@Test
	public void shouldDetermineIfNull() {
		assertThat(ConShowItem.of(null, null, null, null).isNull(), is(true));
		assertThat(ConShowItem.of("", null, null, null).isNull(), is(false));
		assertThat(ConShowItem.of(null, "", null, null).isNull(), is(false));
		assertThat(ConShowItem.of(null, null, "", null).isNull(), is(false));
		assertThat(ConShowItem.of(null, null, null, "").isNull(), is(false));
	}

}
