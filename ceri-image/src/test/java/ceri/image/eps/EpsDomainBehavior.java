package ceri.image.eps;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.image.ebay.EpsDomain;

public class EpsDomainBehavior {

	@Test
	public void shouldMatchDomain() {
		String url =
			"http://i1.ebayimg.com/00/s/NDU2WDgwMA==/$(KGrHqR,!pQFD8qB!CEcBRL)dcN67w~~48_20.JPG";
		assertThat(EpsDomain.domain(url), is(EpsDomain.ebayimg));
		url = "http://ebayimg.com/00/s/NDU2WDgwMA==/$(KGrHqR,!pQFD8qB!CEcBRL)dcN67w~~48_20.JPG";
		assertThat(EpsDomain.domain(url), is((EpsDomain)null));
	}

}
