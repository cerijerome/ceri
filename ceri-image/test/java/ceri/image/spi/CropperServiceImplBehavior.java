package ceri.image.spi;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.junit.Test;
import ceri.image.Cropper;

public class CropperServiceImplBehavior {

	@Test
	public void shouldOnlyAllowMatchingImagePaths() throws Exception {
		CropperServiceImpl.Builder builder = CropperServiceImpl.builder(null);
		builder.allowImagePath("abc\\.com/");
		builder.allowImagePath("d\\.e\\.f/");
		CropperService service = builder.build();
		service.cropImage("/a.b.c/abc.com/xxxxx");
	}
	
	@Test
	public void should() throws Exception {
		CropperServiceImpl.Builder builder = CropperServiceImpl.builder(null);
		builder.allowImagePath("^[^\\.]+\\.ebayimg\\.com/.*");
		builder.cropper("200x200", Cropper.builder(200, 200).maxSizeIncrease(1.0f).build());
		builder.cropper("200x200a", Cropper.builder(200, 200).x2Quality(0.6f).build());
		builder.cropper("200x200b", Cropper.builder(200, 200).x2Quality(0.1f).build());
		CropperService service = builder.build();
	}

}
