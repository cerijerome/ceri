package ceri.image.spi;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import ceri.image.Cropper;
import ceri.image.CropperMock;
import ceri.image.Image;
import ceri.image.ImageMock;

public class CropperServiceImplBehavior {
	private static final Downloader NULL_DOWNLOADER = new DownloaderMock(null);
	private static final Cropper NULL_CROPPER = new CropperMock(null);
	private static final Image.Factory NULL_FACTORY = ImageMock.factory(null);

	@Test(expected = CropperServiceException.class)
	public void shouldPreventNonMatchingImagePaths() throws Exception {
		CropperServiceImpl.Builder builder = CropperServiceImpl.builder(null, null);
		builder.allowImagePath("d\\.e\\.f/"); // Matches whole path
		builder.cropper("xyz", NULL_CROPPER);
		CropperService service = builder.build();
		service.cropImage("/xyz/d.e.f/xxxxx");
	}

	@Test
	public void shouldAllowMatchingImagePaths() throws Exception {
		CropperServiceImpl.Builder builder =
			CropperServiceImpl.builder(NULL_DOWNLOADER, NULL_FACTORY);
		builder.allowImagePath("abc\\.com/.*");
		builder.allowImagePath("d\\.e\\.f/.*");
		builder.cropper("key", NULL_CROPPER);
		CropperService service = builder.build();
		service.cropImage("/key/abc.com/xxxxx");
		service.cropImage("/key/d.e.f/");
	}

	@Test
	public void shouldMatchCropperByKey() throws Exception {
		CropperMock cropper1 = new CropperMock(new byte[0]);
		CropperMock cropper2 = new CropperMock(new byte[0]);
		CropperServiceImpl.Builder builder =
			CropperServiceImpl.builder(NULL_DOWNLOADER, NULL_FACTORY);
		builder.allowImagePath(".*");
		builder.cropper("key1", cropper1);
		builder.cropper("key2", cropper2);
		CropperService service = builder.build();
		assertTrue(service.cropImage("/key1/xxxxx") == cropper1.data);
		assertTrue(service.cropImage("key2/xxxxx") == cropper2.data);
	}

}
