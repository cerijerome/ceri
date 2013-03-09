package ceri.image.eps;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.EnumSet;
import org.junit.Test;
import ceri.image.ebay.EpsImageType;

public class EpsImageTypeBehavior {

	@Test
	public void shouldFindLargestImageTypeByArea() {
		EnumSet<EpsImageType> types = EnumSet.of(EpsImageType._0, EpsImageType._1, EpsImageType._2);
		assertThat(EpsImageType.largestType(types), is(EpsImageType._1));
	}

	@Test
	public void shouldChangeEpsUrlsForNewImageSize() {
		String epsUrl =
			"http://i.ebayimg.com/00/s/NDU2WDgwMA==/$(KGrHqR,!pQFD8qB!CEcBRL)dcN67w~~48_20.JPG";
		assertThat(EpsImageType._1.url(epsUrl),
			is("http://i.ebayimg.com/00/s/NDU2WDgwMA==/$(KGrHqR,!pQFD8qB!CEcBRL)dcN67w~~48_1.JPG"));
		epsUrl = "/i.ebayimg.com/00/s/NDU2WDgwMA==/$(KGrHqR,!pQFD8qB!CEcBRL)dcN67w~~48_20.JPG";
		assertThat(EpsImageType._100.url(epsUrl),
			is("/i.ebayimg.com/00/s/NDU2WDgwMA==/$(KGrHqR,!pQFD8qB!CEcBRL)dcN67w~~48_100.JPG"));
		epsUrl = "00/s/NDU2WDgwMA==/$(KGrHqR,!pQFD8qB!CEcBRL)dcN67w~~48_20.JPG";
		assertThat(EpsImageType._0.url(epsUrl),
			is("00/s/NDU2WDgwMA==/$(KGrHqR,!pQFD8qB!CEcBRL)dcN67w~~48_0.JPG"));
	}

}
