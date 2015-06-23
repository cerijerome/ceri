package ceri;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.image.CropperBehavior;
import ceri.image.ImageUtilTest;
import ceri.image.eps.EpsDomainBehavior;
import ceri.image.eps.EpsImageTypeBehavior;
import ceri.image.geo.GeoUtilTest;
import ceri.image.magick.MagickImageBehavior;
import ceri.image.spi.CropperServiceImplBehavior;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	GeoUtilTest.class, 
	CropperBehavior.class,
	ImageUtilTest.class, 
	Java2dImageBehavior.class, 
	EpsDomainBehavior.class,
	EpsImageTypeBehavior.class, 
	MagickImageBehavior.class, 
	CropperServiceImplBehavior.class, })
public class _Tests {
	public static void main(String... args) {
		//TestUtil.exec(_Tests.class);
	}
}
