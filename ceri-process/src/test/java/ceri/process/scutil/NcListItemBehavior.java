package ceri.process.scutil;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class NcListItemBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		NcListItem t = NcListItem.from("* (Connected)  X P --> D \"N\" [P:T]");
		NcListItem eq0 = NcListItem.from("* (Connected)  X P --> D \"N\" [P:T]");
		NcListItem eq1 = NcListItem.builder().enabled(true).state("Connected").passwordHash("X")
			.protocol("P").device("D").name("N").type("T").build();
		NcListItem ne0 = NcListItem.from("  (Connected)  X P --> D \"N\" [P:T]");
		NcListItem ne1 = NcListItem.from("* (No Service) X P --> D \"N\" [P:T]");
		NcListItem ne2 = NcListItem.from("* (Connected)  Y P --> D \"N\" [P:T]");
		NcListItem ne3 = NcListItem.from("* (Connected)  X Q --> D \"N\" [P:T]");
		NcListItem ne4 = NcListItem.from("* (Connected)  X P --> E \"N\" [P:T]");
		NcListItem ne5 = NcListItem.from("* (Connected)  X P --> D \"O\" [P:T]");
		NcListItem ne6 = NcListItem.from("* (Connected)  X P --> D \"N\" [P:U]");
		exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1, ne2, ne3, ne4, ne5, ne6);
	}

	@Test
	public void shouldShowStateOnlyForNoService() {
		assertThat(NcListItem.builder().state(NcServiceState.noService).build().toString(),
			is(NcServiceState.noService.toString()));
	}

	@Test
	public void shouldShowEnabledStateInString() {
		assertTrue(NcListItem.builder().enabled(true).build().toString().startsWith("*"));
		assertTrue(NcListItem.builder().enabled(false).build().toString().startsWith(" "));
	}

}
