package ceri.image;

import java.awt.image.BufferedImage;

public class Cropper {
	private static final float HD_QUALITY = 1.0f;
	private static final float SD_QUALITY_DEF = 0.2f;
	private final CropParams params;
	private final float sdQuality;
	
	public Cropper(CropParams params, float sdQuality) {
		this.params = params;
		this.sdQuality = sdQuality;
	}
	
	public void crop(BufferedImage image) {
		if (params.resolution == Resolution.HD) cropHd(image);
		else cropSd(image);
	}
	
	private void cropHd(BufferedImage image) {
		
	}
	
	private void cropSd(BufferedImage image) {
		
	}
	
}
