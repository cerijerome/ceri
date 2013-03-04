package ceri.image.ebay;

import java.io.IOException;
import org.junit.Test;
import ceri.image.Cropper;
import ceri.image.CropperMockHelper;
import ceri.image.spi.CropperService;
import ceri.image.spi.CropperServiceImpl;

public class CropperServiceImplBehavior {
	private final CropperMockHelper helper1 = new CropperMockHelper();
	private final CropperMockHelper helper2 = new CropperMockHelper();
	
	@Test
	public void should() throws IOException {
		CropperService service = CropperServiceImpl.builder()
			.register("1", helper1.cropper)
			.register("2", helper2.cropper)
			.imagePath("[^/\\.]*\\.ebayimg.com/.*")
			.build();
		service.cropImage("1/hello");
	}
	
	//@Test
	public void should0() throws IOException {
		CropperService service = CropperServiceImpl.builder()
			.register("100x150", Cropper.builder(100, 150).build())
			.register("150x150", Cropper.builder(150, 150).build())
			.imagePath("[^/\\.]*\\.ebayimg.com/.*")
			.build();
		service.cropImage("/100x150/i1.ebayimg.com/abcdefg~~40_20.jpg");
	}

}
