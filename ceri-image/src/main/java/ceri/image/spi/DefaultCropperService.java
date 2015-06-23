package ceri.image.spi;

import ceri.image.Cropper;
import ceri.image.magick.MagickImage;

public class DefaultCropperService {
	private static final String EPS_PATH = "[^/]+\\.ebayimg\\.com/.*";
	private static final int MB = 1 * 1024 * 1024;
	private static final float MAX_SIZE = 9.0f;

	private DefaultCropperService() {}

	public static CropperService create() {
		ClientConnectionManager connectionManager = new PoolingClientConnectionManager();
		Downloader downloader = new HttpClientDownloader(connectionManager, MB);
		CropperServiceImpl.Builder builder =
			CropperServiceImpl.builder(downloader, MagickImage.FACTORY);
		builder.allowImagePath(EPS_PATH);
		builder.cropper("100x100", Cropper.builder(100, 100).maxSizeIncrease(MAX_SIZE).build());
		builder.cropper("150x150", Cropper.builder(150, 150).maxSizeIncrease(MAX_SIZE).build());
		builder.cropper("200x200", Cropper.builder(200, 200).maxSizeIncrease(MAX_SIZE).build());
		return builder.build();
	}

}
