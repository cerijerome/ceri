package ceri.image.spi;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.junit.Test;
import ceri.image.Cropper;
import ceri.image.CropperMock;
import ceri.image.Image;
import ceri.image.ImageMock;
import ceri.image.magick.MagickImage;
import ceri.image.test.TestImage;

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
	public void should() throws Exception {
		String path =
			"http://i.ebayimg.com/00/s/NDU2WDgwMA==/$(KGrHqR,!pQFD8qB!CEcBRL)dcN67w~~48_20.JPG";
		ClientConnectionManager connectionManager = new PoolingClientConnectionManager();
		CropperServiceImpl.Builder builder =
			CropperServiceImpl.builder(new HttpClientDownloader(connectionManager, 0),
				MagickImage.FACTORY);
		builder.allowImagePath(".*");
		builder.cropper("100x100", Cropper.builder(100, 100).x2Quality(0.9f).build());
		builder.cropper("200x200", Cropper.builder(200, 200).x2Quality(0.2f).build());
		builder.cropper("300x300", Cropper.builder(300, 300).x2Quality(0.1f).build());
		CropperService service = builder.build();
		byte[] data =
			service
				.cropImage("100x100/i.ebayimg.com/00/s/NDU2WDgwMA==/$(KGrHqR,!pQFD8qB!CEcBRL)dcN67w~~48_20.JPG");
		try (OutputStream out = new FileOutputStream("doc/img/test.jpg")) {
			out.write(data);
		}

	}

	private CropperServiceImpl.Builder builder(TestImage testImage) throws IOException {
		return CropperServiceImpl
			.builder(new DownloaderMock(testImage.read()), MagickImage.FACTORY);
	}

}
