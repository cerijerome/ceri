package ceri;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.common.util.HashCoderBehavior;
import ceri.geo.GeoUtilTest;
import ceri.image.CropperBehavior;
import ceri.image.ImageUtilTest;
import ceri.image.Java2dImageBehavior;
import ceri.image.eps.EpsImageTypeBehavior;
import ceri.image.magick.MagickImageBehavior;
import ceri.image.spi.CropperServiceImplBehavior;


@RunWith(Suite.class)
@Suite.SuiteClasses({
	HashCoderBehavior.class,
	GeoUtilTest.class,
	CropperBehavior.class,
	ImageUtilTest.class,
	Java2dImageBehavior.class,
	EpsImageTypeBehavior.class,
	MagickImageBehavior.class,
	CropperServiceImplBehavior.class,
})
public class _Tests {
	public static void main(String... args) {
		//TestUtil.exec(_Tests.class);
	}
}
